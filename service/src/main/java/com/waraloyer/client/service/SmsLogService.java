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
import com.waraloyer.client.repository.UserRepository;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SmsLogService {

    private static final Logger logger = LoggerFactory.getLogger(SmsLogService.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-phone-number}")
    private String fromPhoneNumber;

    @Value("${twilio.from-alphanumeric-id:WaraLoyer}") // Utilisez WaraLoyer comme valeur par défaut
    private String fromAlphanumericId;

    private final SmsLogRepository smsLogRepository;
    private final UserService userService;

    private final ClientConfigService clientConfigService;
    private final RentalService rentalService;

    private static final String TEMPLATE_RELANCE_NAME = "waraloyer_relance_v2";
    private static final String TEMPLATE_RAPPEl_NAME = "waraloyer_rappel_v2";
    private static final String TEMPLATE_RELANCE_URGENT_NAME = "waraloyer_relance_urgent_v2";

    @Autowired
    public SmsLogService(SmsLogRepository smsLogRepository, UserService userService, ClientConfigService clientConfigService, RentalService rentalService) {
        this.smsLogRepository = smsLogRepository;
        this.userService = userService;
        this.clientConfigService = clientConfigService;
        this.rentalService = rentalService;
    }

    public SmsLog sendSms(User user, String to, String messageBody, String type, LocalDateTime scheduleDate, Long rentalId) {
        SmsLog smsLog = new SmsLog();
        smsLog.setUser(user);
        smsLog.setToPhoneNumber(to);
        smsLog.setMessage(messageBody);
        smsLog.setType(type);
        smsLog.setSentDate(LocalDate.now());

        try {
            Twilio.init(accountSid, authToken);

            // Gérer le type d'envoi
            if ("WHATSAPP_TEMPLATE".equals(type) || "RELANCE".equals(type) || "RAPPEL".equals(type)) {
                try {
                    String templateName = getTemplateNameByType(messageBody);

                    // Récupère la location pour les variables
                    Rental rental = rentalService.findById(rentalId, user).orElse(null);
                    if (rental == null) {
                        throw new IllegalArgumentException("Location non trouvée.");
                    }

                    Map<String, String> variables = new HashMap<>();
                    variables.put("1", rental.getTenant().getFirstName()); // Nom du locataire
                    variables.put("2", rental.getTenant().getProperty().getAddress()); // Adresse du bien

                    if ("RELANCE_URGENT".equals(type)) {
                        variables.put("1", String.valueOf(rental.getAmountDue())); // Montant
                        variables.put("2", rental.getTenant().getProperty().getAddress()); // Adresse
                    }

                    MessageCreator creator = Message
                            .creator(new com.twilio.type.PhoneNumber("whatsapp:" + to),
                                    new com.twilio.type.PhoneNumber("whatsapp:" + fromPhoneNumber),
                                    templateName) // On utilise le nom du template ici
                            .setContentVariables(new JSONObject(variables).toString());

                    if (scheduleDate != null) {
                        ZonedDateTime zonedDateTime = scheduleDate.atZone(ZoneId.systemDefault());
                        creator.setSendAt(zonedDateTime);
                    }
                    creator.create();
                    smsLog.setStatus("SENT_WHATSAPP");

                } catch (Exception whatsappException) {
                    logger.warn("Échec de l'envoi via WhatsApp. Tentative d'envoi par SMS: {}", whatsappException.getMessage());

                    MessageCreator creator = Message.creator(
                            new PhoneNumber(to),
                            new PhoneNumber(fromPhoneNumber),
                            messageBody
                    );
                    if (scheduleDate != null) {
                        ZonedDateTime zonedDateTime = scheduleDate.atZone(ZoneId.systemDefault());
                        creator.setSendAt(zonedDateTime);
                    }
                    creator.create();
                    smsLog.setStatus("SENT_SMS");
                }
            } else {
                MessageCreator creator = Message.creator(
                        new PhoneNumber(to),
                        new PhoneNumber(fromPhoneNumber),
                        messageBody
                );
                if (scheduleDate != null) {
                    ZonedDateTime zonedDateTime = scheduleDate.atZone(ZoneId.systemDefault());
                    creator.setSendAt(zonedDateTime);
                }
                creator.create();
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

    private String getTemplateNameByType(String type) {
        return switch (type) {
            case "RAPPEL" -> TEMPLATE_RAPPEl_NAME;
            case "RELANCE" -> TEMPLATE_RELANCE_NAME;
            case "RELANCE_URGENT" -> TEMPLATE_RELANCE_URGENT_NAME;
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

        // Vérifiez que la location appartient bien à l'utilisateur
        if (!rentalService.belongsToUser(rentalId, currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé. Cette location n'appartient pas à cet utilisateur.");
        }

        return smsLogRepository.findByRentalId(rentalId);
    }

}
