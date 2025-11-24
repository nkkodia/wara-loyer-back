package com.waraloyer.client.service;

import com.waraloyer.client.model.ClientConfig;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderSchedulerService.class);

    private final RentalService rentalService;
    private final ClientConfigService clientConfigService;
    private final SmsLogService smsLogService;
    private final TenantRepository tenantRepository;

    @Autowired
    public ReminderSchedulerService(RentalService rentalService, ClientConfigService clientConfigService, SmsLogService smsLogService, TenantRepository tenantRepository) {
        this.rentalService = rentalService;
        this.clientConfigService = clientConfigService;
        this.smsLogService = smsLogService;
        this.tenantRepository = tenantRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void sendAutomatedRemindersAndRelances() {
        logger.info("Début de la tâche de planification des rappels et relances.");

        List<ClientConfig> allConfigs = clientConfigService.findAll();
        logger.debug("Nombre de configurations utilisateur trouvées: {}", allConfigs.size()); // Nouveau : Compte total

        for (ClientConfig config : allConfigs) {
            Long userId = config.getUser().getId();
            User user = config.getUser();
            logger.info("Traitement de l'utilisateur ID: {} ({})", userId, user.getEmail()); // Nouveau : Utilisateur en cours

            List<Rental> userRentals = rentalService.findByUserId(userId);
            logger.debug("Nombre de locations à vérifier pour l'utilisateur {}: {}", userId, userRentals.size()); // Nouveau : Locations trouvées

            for (Rental rental : userRentals) {
                // Le loyer est-il pour le mois en cours et n'est-il pas payé ?
                if (rental.getDueDate().getMonth().equals(LocalDate.now().getMonth()) && !rental.getStatus().equals("Paid")) {
                    logger.debug("Vérification du loyer ID {} (Statut: {})", rental.getId(), rental.getStatus()); // Nouveau : Location traitée

                    // Logique pour le rappel
                    LocalDate reminderDate = rental.getDueDate().minusDays(config.getReminderDaysBefore());
                    if (LocalDate.now().isEqual(reminderDate) && !rental.isReminderSent()) {

                        logger.info("ACTION: Envoi du RAPPEL pour le loyer ID {} (Date due: {})", rental.getId(), rental.getDueDate()); // Nouveau : Log d'action

                        smsLogService.sendSms(user, rental.getTenant().getPhoneNumber(), "RAPPEL", null, rental.getId());
                        rental.setReminderSent(true);
                        rental.setLastReminderSentDate(LocalDate.now());
                        rentalService.update(rental.getId(), rental, user);
                        logger.debug("Rappel envoyé et marqueur 'isReminderSent' mis à jour pour loyer ID {}.", rental.getId()); // Nouveau : Log de mise à jour

                    } else if (LocalDate.now().isEqual(reminderDate) && rental.isReminderSent()) {
                        logger.debug("Skipping RAPPEL pour loyer ID {}: Déjà envoyé.", rental.getId()); // Nouveau : Log d'évitement
                    }

                    // Logique pour la relance
                    LocalDate relanceDate = rental.getDueDate().plusDays(config.getRelanceDaysAfter());
                    if (LocalDate.now().isEqual(relanceDate) && !rental.isRelanceSent()) {

                        logger.info("ACTION: Envoi de la RELANCE pour le loyer ID {} (Date due: {})", rental.getId(), rental.getDueDate()); // Nouveau : Log d'action

                        smsLogService.sendSms(user, rental.getTenant().getPhoneNumber(), "RELANCE", null, rental.getId());
                        rental.setRelanceSent(true);
                        rental.setLastRelanceSentDate(LocalDate.now());
                        rentalService.update(rental.getId(), rental, user);
                        logger.debug("Relance envoyée et marqueur 'isRelanceSent' mis à jour pour loyer ID {}.", rental.getId()); // Nouveau : Log de mise à jour

                    } else if (LocalDate.now().isEqual(relanceDate) && rental.isRelanceSent()) {
                        logger.debug("Skipping RELANCE pour loyer ID {}: Déjà envoyée.", rental.getId()); // Nouveau : Log d'évitement
                    }

                } else {
                    logger.debug("Ignoré loyer ID {}: Payé ou Date Due non dans le mois en cours.", rental.getId()); // Nouveau : Log d'évitement de la condition IF
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

            List<Tenant> tenants = tenantRepository.findByUserAndEnabled(user, true);

            for (Tenant tenant : tenants) {
                Rental newRental = new Rental();
                newRental.setDueDate(LocalDate.now().plusMonths(1).withDayOfMonth(tenant.getRentStartDate().getDayOfMonth()));
                newRental.setAmountDue(tenant.getProperty().getRentAmount());
                newRental.setStatus("Due");
                newRental.setTenant(tenant);
                newRental.setProperty(tenant.getProperty());
                newRental.setUser(user);
                rentalService.create(newRental, user);
            }
        }
        logger.info("Fin de la tâche de génération des loyers mensuels.");
    }

   }