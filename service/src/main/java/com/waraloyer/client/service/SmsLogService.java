package com.waraloyer.client.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import com.waraloyer.client.exception.MessageLimitExceededException;
import com.waraloyer.client.model.ClientConfig;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.SmsLog;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.SmsLogRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class SmsLogService {

    private static final Logger logger = LoggerFactory.getLogger(SmsLogService.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-phone-number}")
    private String fromPhoneNumber;

    @Value("${twilio.from-alphanumeric-id:WaraLoyer}")
    private String fromAlphanumericId;

    private final SmsLogRepository smsLogRepository;
    private final UserService userService;

    private final ClientConfigService clientConfigService;
    private final RentalService rentalService;

    private static final String TEMPLATE_RELANCE_NAME = "waraloyer_relance_v2";
    private static final String TEMPLATE_RAPPEL_NAME = "waraloyer_rappel_v2";
    private static final String TEMPLATE_RELANCE_URGENTE = "waraloyer_relance_urgent_v2";

    @Autowired
    public SmsLogService(SmsLogRepository smsLogRepository, UserService userService, ClientConfigService clientConfigService, RentalService rentalService) {
        this.smsLogRepository = smsLogRepository;
        this.userService = userService;
        this.clientConfigService = clientConfigService;
        this.rentalService = rentalService;
    }

    public SmsLog sendSms(User user, String to, String type, LocalDateTime scheduleDate, Long rentalId) {
        SmsLog smsLog = new SmsLog();
        smsLog.setUser(user);
        smsLog.setToPhoneNumber(to);
        smsLog.setType(type);
        smsLog.setSentDate(LocalDate.now());

        Optional<ClientConfig> clientConfigOptional = clientConfigService.getByUserId(user.getId());
        Integer monthlySmsLimit = user.getSubscription().getMonthlySmsLimit();
        Integer messageCount = clientConfigOptional.get().getMessageCountThisMonth();
        if (messageCount != null && monthlySmsLimit != null && messageCount >= monthlySmsLimit) {
            throw new MessageLimitExceededException("La limite de messages mensuelle a été atteinte.");
        }
        String fallbackMessage = "Le service de messagerie est temporairement indisponible. Veuillez contacter le propriétaire.";

        ClientConfig clientConfig = clientConfigOptional.get();
        if ("RAPPEL".equals(type)) {
            fallbackMessage = clientConfig.getSmsReminderMessage();
        } else if ("RELANCE".equals(type)) {
            fallbackMessage = clientConfig.getSmsRelanceMessage();
        } else if ("RELANCE_URGENTE_URL".equals(type)) {
            fallbackMessage = "Rappel urgent : le loyer de {MONTANT} FCFA pour le bien situé au {ADRESSE_BIEN} est en retard. Merci de régulariser.";
        }
        try {
            Twilio.init(accountSid, authToken);

            if ("WHATSAPP_TEMPLATE".equals(type) || "RELANCE".equals(type) || "RAPPEL".equals(type) || "RELANCE_URGENTE_URL".equals(type)) {
                try {
                    String templateName = getTemplateNameByType(type);

                    Rental rental = rentalService.findById(rentalId, user).orElse(null);
                    if (rental == null) {
                        throw new IllegalArgumentException("Location non trouvée.");
                    }

                    Map<String, String> variables = new HashMap<>();

                    switch (type) {
                        case "RAPPEL" -> {
                            variables.put("1", rental.getTenant().getFirstName());
                            variables.put("2", rental.getProperty().getAddress());
                            variables.put("3", rental.getDueDate().toString());
                        }
                        case "RELANCE" -> {
                            variables.put("1", rental.getTenant().getFirstName());
                            variables.put("2", rental.getProperty().getAddress());
                        }
                        case "RELANCE_URGENTE_URL" -> {
                            variables.put("1", String.valueOf(rental.getAmountDue()));
                            variables.put("2", rental.getProperty().getAddress());
                            String problemUrl = "https://waraloyer.com/tenant-problem/" + rental.getId();
                            variables.put("3", problemUrl);
                        }
                    }

                    MessageCreator creator = Message
                            .creator(new com.twilio.type.PhoneNumber("whatsapp:" + to),
                                    new com.twilio.type.PhoneNumber("whatsapp:" + fromPhoneNumber),
                                    templateName)
                            .setContentVariables(new JSONObject(variables).toString());

                    if (scheduleDate != null) {
                        ZonedDateTime zonedDateTime = scheduleDate.atZone(ZoneId.systemDefault());
                        creator.setSendAt(zonedDateTime);
                    }
                    creator.create();
                    smsLog.setStatus("SENT_WHATSAPP");
                    smsLog.setMessage(templateName);
                    String problemUrl = "https://wara-loyer.com/tenant-problem/" + rental.getId();
                    Message.creator(
                            new PhoneNumber("whatsapp:" + to),
                            new PhoneNumber("whatsapp:" + fromPhoneNumber),
                            problemUrl
                    ).create();
                } catch (Exception whatsappException) {
                    logger.warn("Échec de l'envoi via WhatsApp. Tentative d'envoi par SMS: {}", whatsappException.getMessage());

                    MessageCreator creator1 = Message.creator(
                            new PhoneNumber(to),
                            new PhoneNumber(fromPhoneNumber),
                            replacePlaceholders(fallbackMessage, rentalService.findById(rentalId, user).orElse(null))

                    );
                    if (scheduleDate != null) {
                        ZonedDateTime zonedDateTime = scheduleDate.atZone(ZoneId.systemDefault());
                        creator1.setSendAt(zonedDateTime);
                    }
                    creator1.create();

                    String problemUrl = "https://wara-loyer.com/tenant-problem/" + rentalId;
                    MessageCreator creator2 = Message.creator(
                            new PhoneNumber(to),
                            new PhoneNumber(fromPhoneNumber),
                            problemUrl
                    );
                    creator2.create();
                    smsLog.setMessage(replacePlaceholders(fallbackMessage, rentalService.findById(rentalId, user).orElse(null)));
                    smsLog.setStatus("SENT_SMS");
                }
            } else {
                MessageCreator creator = Message.creator(
                        new PhoneNumber(to),
                        new PhoneNumber(fromPhoneNumber),
                        replacePlaceholders(fallbackMessage, rentalService.findById(rentalId, user).orElse(null))
                );
                if (scheduleDate != null) {
                    ZonedDateTime zonedDateTime = scheduleDate.atZone(ZoneId.systemDefault());
                    creator.setSendAt(zonedDateTime);
                }
                creator.create();
                smsLog.setMessage(replacePlaceholders(fallbackMessage, rentalService.findById(rentalId, user).orElse(null)));
                smsLog.setStatus("SENT_SMS");
            }
        } catch (Exception finalException) {
            smsLog.setStatus("FAILED");
            logger.error("Échec total de l'envoi de message de type '{}' au numéro {}: {}", type, to, finalException.getMessage());
        } finally {
            if (rentalId != null) {
                smsLog.setRental(rentalService.findById(rentalId, user).orElse(null));
            }
        }

        return smsLogRepository.save(smsLog);
    }

    private String replacePlaceholders(String message, Rental rental) {
        if (rental == null) return message;
        String newMessage = message
                .replace("{{1}}", rental.getTenant().getFirstName())
                .replace("{{2}}", rental.getProperty().getAddress());
        if (newMessage.contains("{{3}}") && rental.getDueDate() != null) {
            newMessage = newMessage.replace("{{3}}", rental.getDueDate().toString());
        }
        if (newMessage.contains("{MONTANT}") && rental.getAmountDue() != null) {
            newMessage = newMessage.replace("{MONTANT}", String.valueOf(rental.getAmountDue()));
        }
        return newMessage;
    }

    private String getTemplateNameByType(String type) {
        return switch (type) {
            case "RAPPEL" -> TEMPLATE_RAPPEL_NAME;
            case "RELANCE" -> TEMPLATE_RELANCE_NAME;
            case "RELANCE_URGENT" -> TEMPLATE_RELANCE_URGENTE;
            default -> throw new IllegalArgumentException("Type de template non supporté: " + type);
        };
    }
    /**
     * Récupère l'historique des SMS pour un utilisateur donné.
     * @param userId L'ID de l'utilisateur.
     * @return La liste des logs SMS.
     */
    public List<SmsLog> findByUserId(Long userId) {
        return smsLogRepository.findByUserId(userId);
    }

    public List<SmsLog> findAll() {
        return smsLogRepository.findAll();
    }
    public List<SmsLog> findByRentalIdAndUserId(Long rentalId, Authentication authentication) {
        User currentUser = userService.findUserByEmail(authentication.getName());

        if (!rentalService.belongsToUser(rentalId, currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé. Cette location n'appartient pas à cet utilisateur.");
        }
        return smsLogRepository.findByRentalId(rentalId);
    }

}
