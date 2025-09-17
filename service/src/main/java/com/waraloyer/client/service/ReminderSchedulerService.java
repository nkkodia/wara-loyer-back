package com.waraloyer.client.service;

import com.waraloyer.client.model.ClientConfig;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.User;
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

    @Autowired
    public ReminderSchedulerService(RentalService rentalService, ClientConfigService clientConfigService, SmsLogService smsLogService) {
        this.rentalService = rentalService;
        this.clientConfigService = clientConfigService;
        this.smsLogService = smsLogService;
    }

    @Scheduled(cron = "0 0 2 * * ?") // S'exécute tous les jours à 2h00 du matin
    public void sendAutomatedRemindersAndRelances() {
        logger.info("Début de la tâche de planification des rappels et relances.");

        List<ClientConfig> allConfigs = clientConfigService.findAll();

        for (ClientConfig config : allConfigs) {
            Long userId = config.getUser().getId();
            List<Rental> userRentals = rentalService.findByUserId(userId);

            for (Rental rental : userRentals) {
                if (rental.getStatus().equals("Paid")) {
                    continue;
                }

                // Logique pour le rappel
                LocalDate reminderDate = rental.getDueDate().minusDays(config.getReminderDaysBefore());
                if (LocalDate.now().isEqual(reminderDate) && !rental.isReminderSent()) {
                    smsLogService.sendSms(rental.getUser(), rental.getTenant().getPhoneNumber(), "RAPPEL", null, null);
                    rental.setReminderSent(true);
                    rental.setLastReminderSentDate(LocalDate.now());
                    rentalService.update(rental.getId(), rental, rental.getUser());
                }

                // Logique pour la relance
                LocalDate relanceDate = rental.getDueDate().plusDays(config.getRelanceDaysAfter());
                if (LocalDate.now().isEqual(relanceDate) && !rental.isRelanceSent() && rental.getStatus().equals("Due")) {
                    smsLogService.sendSms(rental.getUser(), rental.getTenant().getPhoneNumber(), "RELANCE", null, null);
                    rental.setRelanceSent(true);
                    rental.setLastRelanceSentDate(LocalDate.now());
                    rentalService.update(rental.getId(), rental, rental.getUser());
                }
            }
        }
        logger.info("Fin de la tâche de planification des rappels et relances.");
    }

   }