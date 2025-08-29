package com.waraloyer.client.service;

import com.waraloyer.client.model.ClientConfig;
import com.waraloyer.client.model.Rental;
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
                // Logique pour le rappel
                LocalDate reminderDate = rental.getDueDate().minusDays(config.getReminderDaysBefore());
                if (LocalDate.now().isEqual(reminderDate) && !rental.isReminderSent()) {
                    String personalizedMessage = createReminderMessage(rental, config.getSmsReminderMessage());
                    smsLogService.sendSms(rental.getUser(), rental.getTenant().getPhoneNumber(), personalizedMessage, "RAPPEL");
                }

                // Logique pour la relance
                LocalDate relanceDate = rental.getDueDate().plusDays(config.getRelanceDaysAfter());
                if (LocalDate.now().isEqual(relanceDate) && !rental.isRelanceSent() && rental.getStatus().equals("PENDING")) {
                    String personalizedMessage = createRelanceMessage(rental, config.getSmsRelanceMessage());
                    smsLogService.sendSms(rental.getUser(), rental.getTenant().getPhoneNumber(), personalizedMessage, "RELANCE");
                }
            }
        }
        logger.info("Fin de la tâche de planification des rappels et relances.");
    }

    private String createReminderMessage(Rental rental, String template) {
        String message = template;
        message = message.replace("{LOCATAIRE}", rental.getTenant().getFirstName());
        message = message.replace("{MONTANT}", rental.getAmountDue().toString());
        message = message.replace("{ADRESSE_BIEN}", rental.getProperty().getAddress());
        // Ajoutez d'autres placeholders si nécessaire
        return message;
    }

    private String createRelanceMessage(Rental rental, String template) {
        String message = template;
        message = message.replace("{LOCATAIRE}", rental.getTenant().getFirstName());
        message = message.replace("{MONTANT}", rental.getAmountDue().toString());
        message = message.replace("{ADRESSE_BIEN}", rental.getProperty().getAddress());
        // Ajoutez d'autres placeholders si nécessaire
        return message;
    }
}
