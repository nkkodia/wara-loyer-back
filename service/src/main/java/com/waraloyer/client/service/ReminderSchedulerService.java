package com.waraloyer.client.service;

import com.waraloyer.client.model.*;
import com.waraloyer.client.repository.RentalRepository;
import com.waraloyer.client.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReminderSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderSchedulerService.class);
    private static final Set<String> PAID_STATUSES = Set.of("Paid", "Payé", "PAID"); // Statuts à ignorer

    private final RentalService rentalService;
    private final ClientConfigService clientConfigService;
    private final SmsLogService smsLogService;
    private final TenantRepository tenantRepository;
    private final RentalRepository rentalRepository;

    @Autowired
    public ReminderSchedulerService(RentalService rentalService, ClientConfigService clientConfigService, SmsLogService smsLogService, TenantRepository tenantRepository, RentalRepository rentalRepository) {
        this.rentalService = rentalService;
        this.clientConfigService = clientConfigService;
        this.smsLogService = smsLogService;
        this.tenantRepository = tenantRepository;
        this.rentalRepository = rentalRepository;
    }

    /**
     * Tâche planifiée pour envoyer les rappels (futur proche) et les relances (en retard).
     * S'exécute tous les jours à 02:00:00.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void sendAutomatedRemindersAndRelances() {
        logger.info("Début de la tâche de planification des rappels et relances.");

        List<ClientConfig> allConfigs = clientConfigService.findAll();
        logger.debug("Nombre de configurations utilisateur trouvées: {}", allConfigs.size());

        for (ClientConfig config : allConfigs) {
            User user = config.getUser();
            if (user == null || !user.isEnabled()) {
                logger.warn("Skipping config ID {} : Utilisateur non valide ou désactivé.", config.getId());
                continue;
            }

            logger.info("Traitement de l'utilisateur ID: {} ({})", user.getId(), user.getEmail());

            // ➡️ AMÉLIORATION 1 & 3 : Récupérer TOUS les loyers DUS ou IMPAYÉS ⬅️
            // La logique de filtrage (mois courant vs passé) est désormais dans le code ci-dessous.
            List<Rental> userRentals = rentalService.findByUserIdAndStatusNot(user.getId(), "Paid");
            logger.debug("Nombre de locations non payées à vérifier : {}", userRentals.size());

            for (Rental rental : userRentals) {
                LocalDate today = LocalDate.now();

                // 1. DÉFINITION DES SEUILS
                LocalDate reminderThresholdDate = rental.getDueDate().minusDays(config.getReminderDaysBefore());
                LocalDate relanceThresholdDate = rental.getDueDate().plusDays(config.getRelanceDaysAfter());

                // --- LOGIQUE DE RAPPEL (Due Date Proche ou Passée, mais Jamais Envoyé) ---
                // Condition 1: La date seuil est atteinte OU dépassée (<= today).
                // Condition 2: Le rappel N'A JAMAIS été envoyé.
                if (today.isAfter(reminderThresholdDate.minusDays(1)) && !Boolean.TRUE.equals(rental.isReminderSent())) {

                    // ➡️ CORRECTION 1 & 2 : Utilisation de isAfter (et non isEqual) pour capturer les rappels manqués ⬅️
                    // L'événement se déclenche si on est à la date seuil OU si on l'a manquée.

                    logger.info("ACTION: Envoi du RAPPEL pour loyer ID {} (Échéance: {})", rental.getId(), rental.getDueDate());

                    smsLogService.sendSms(user, rental.getTenant().getPhoneNumber(), "RAPPEL", null, rental.getId());
                    rental.setReminderSent(true);
                    rental.setLastReminderSentDate(today);
                    rentalService.update(rental.getId(), rental, user);

                } else if (Boolean.TRUE.equals(rental.isReminderSent())) {
                    logger.debug("Skipping RAPPEL pour loyer ID {}: Déjà envoyé.", rental.getId());
                }


                // --- LOGIQUE DE RELANCE (Seuil de Grâce dépassé et Jamais Envoyé) ---
                // Condition 1: Le loyer est en retard de PLUS que la période de grâce (today >= relanceDate).
                // Condition 2: La relance N'A JAMAIS été envoyée.
                if (today.isAfter(relanceThresholdDate) && !Boolean.TRUE.equals(rental.isRelanceSent())) {
                    
                    logger.info("ACTION: Envoi de la RELANCE pour loyer ID {} (En retard depuis le {})", rental.getId(), relanceThresholdDate);

                    smsLogService.sendSms(user, rental.getTenant().getPhoneNumber(), "RELANCE", null, rental.getId());
                    rental.setRelanceSent(true);
                    rental.setLastRelanceSentDate(today);
                    rentalService.update(rental.getId(), rental, user);

                } else if (Boolean.TRUE.equals(rental.isRelanceSent())) {
                    logger.debug("Skipping RELANCE pour loyer ID {}: Déjà envoyée.", rental.getId());
                }
            }
        }
        logger.info("Fin de la tâche de planification des rappels et relances. Tous les utilisateurs ont été traités.");
    }


    @Scheduled(cron = "0 0 1 1 * ?") // S'exécute le 1er de chaque mois
    public void generateMonthlyRentals() {
        logger.info("Début de la tâche de génération des loyers mensuels.");

        List<ClientConfig> allConfigs = clientConfigService.findAll();

        for (ClientConfig config : allConfigs) {
            Long userId = config.getUser().getId();
            User user = config.getUser();

            // On assume que tenantRepository.findByUserAndEnabled(user, true) retourne les Locataires
            List<Tenant> tenants = tenantRepository.findByUserAndEnabled(user, true);

            for (Tenant tenant : tenants) {
                // ➡️ DÉBUT DE LA CORRECTION : Vérification de la propriété ⬅️
                Property property = tenant.getProperty();

                if (property == null) {
                    // IMPORTANT : Si la propriété est null, on saute ce locataire
                    logger.warn("Locataire ID {} (User ID: {}) n'a pas de propriété associée. Ignoré lors de la génération des loyers.", tenant.getId(), user.getId());
                    continue; // Passe au locataire suivant
                }
                Rental newRental = new Rental();
                newRental.setDueDate(LocalDate.now().plusMonths(1).withDayOfMonth(tenant.getRentStartDate().getDayOfMonth()));
                newRental.setAmountDue(property.getRentAmount());
                newRental.setStatus("Due");
                newRental.setTenant(tenant);
                newRental.setProperty(property);
                newRental.setUser(user);

                rentalService.create(newRental, user);
            }
        }
        logger.info("Fin de la tâche de génération des loyers mensuels.");
    }

   }