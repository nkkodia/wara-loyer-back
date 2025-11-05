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

        for (ClientConfig config : allConfigs) {
            Long userId = config.getUser().getId();
            User user = config.getUser();
            List<Rental> userRentals = rentalService.findByUserId(userId);

            for (Rental rental : userRentals) {
                // Le loyer est-il pour le mois en cours et n'est-il pas payé ?
                if (rental.getDueDate().getMonth().equals(LocalDate.now().getMonth()) && !rental.getStatus().equals("Paid")) {

                    // Logique pour le rappel
                    LocalDate reminderDate = rental.getDueDate().minusDays(config.getReminderDaysBefore());
                    if (LocalDate.now().isEqual(reminderDate) && !rental.isReminderSent()) {
                        smsLogService.sendSms(user, rental.getTenant().getPhoneNumber(), "RAPPEL", null, rental.getId());
                        rental.setReminderSent(true);
                        rental.setLastReminderSentDate(LocalDate.now());
                        rentalService.update(rental.getId(), rental, user);
                    }

                    // Logique pour la relance
                    LocalDate relanceDate = rental.getDueDate().plusDays(config.getRelanceDaysAfter());
                    if (LocalDate.now().isEqual(relanceDate) && !rental.isRelanceSent()) {
                        smsLogService.sendSms(user, rental.getTenant().getPhoneNumber(), "RELANCE", null, rental.getId());
                        rental.setRelanceSent(true);
                        rental.setLastRelanceSentDate(LocalDate.now());
                        rentalService.update(rental.getId(), rental, user);
                    }
                }
            }
        }
        logger.info("Fin de la tâche de planification des rappels et relances.");
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