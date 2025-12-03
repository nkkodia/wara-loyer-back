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

    @Value("${twilio.messaging-service-sid}")
    private String messagingServiceSid;

    private final SmsLogRepository smsLogRepository;
    private final UserService userService;

    private final ClientConfigService clientConfigService;
    private final RentalService rentalService;

    private static final String TYPE_RAPPEL = "RAPPEL";
    private static final String TYPE_RELANCE = "RELANCE";
    private static final String TYPE_RELANCE_URGENTE = "RELANCE_URGENTE";

    private static final String TEMPLATE_RELANCE_NAME = "waraloyer_relance_v2";
    private static final String TEMPLATE_RAPPEL_NAME = "waraloyer_rappel_simple";
    private static final String TEMPLATE_RELANCE_URGENTE = "waraloyer_relance_urgent_v2";

    private static final String TEMPLATE_RELANCE_SID = "HX6b807f46669a867e0ee9da059f5fdcf0";
    private static final String TEMPLATE_RAPPEL_SID = "HXb04a3ad68c9b07f708b3465ccad11906";
    private static final String TEMPLATE_RELANCE_URGENTE_SID = "HX440074001e31f9dbc6dee8965e7e89b1";

    @Autowired
    public SmsLogService(SmsLogRepository smsLogRepository, UserService userService, ClientConfigService clientConfigService, RentalService rentalService ) {
        this.smsLogRepository = smsLogRepository;
        this.userService = userService;
        this.clientConfigService = clientConfigService;
        this.rentalService = rentalService;
    }

    /**
     * Point d'entrée pour l'envoi d'un message. Gère la validation et la journalisation.
     */
    public SmsLog sendSms(User user, String to, String type, LocalDateTime scheduleDate, Long rentalId) {
        SmsLog smsLog = new SmsLog();
        smsLog.setUser(user);
        smsLog.setToPhoneNumber(to);
        smsLog.setType(type);
        smsLog.setSentDate(LocalDate.now());

        // ➡️ CORRECTION 1: Initialisation des Twilio Objects DANS la méthode ⬅️
        // Cela garantit que les @Value sont résolues.
        Twilio.init(accountSid, authToken);
        final com.twilio.type.PhoneNumber whatsappFrom = new com.twilio.type.PhoneNumber("whatsapp:" + fromPhoneNumber);
        final com.twilio.type.PhoneNumber smsFrom = new com.twilio.type.PhoneNumber(fromPhoneNumber);


        try {
            ClientConfig clientConfig = validateAndIncrementLimit(user);

            // Exécution du workflow d'envoi (WhatsApp -> Fallback SMS)
            MessageSendingResult result = executeTwilioSend(user, to, type, scheduleDate, rentalId, clientConfig, whatsappFrom, smsFrom);

            // Mise à jour finale du log
            smsLog.setStatus(result.status);
            smsLog.setMessage(result.messageBody);

            // Mise à jour de la config si l'envoi a réussi (statut n'est pas FAILED)
            if (!"FAILED".equals(result.status)) {
                clientConfig.setMessageCountThisMonth(clientConfig.getMessageCountThisMonth() + 1);
                clientConfigService.save(clientConfig, user);
            }

        } catch (MessageLimitExceededException e) {
            smsLog.setStatus("FAILED");
            smsLog.setMessage("Limite atteinte: " + e.getMessage());
        } catch (Exception finalException) {
            smsLog.setStatus("FAILED");
            smsLog.setMessage(finalException.getMessage());
            logger.error("Échec total de l'envoi de type '{}' au numéro {}: {}", type, to, finalException.getMessage());
        } finally {
            if (rentalId != null) {
                smsLog.setRental(rentalService.findById(rentalId, user).orElse(null));
            }
        }

        return smsLogRepository.save(smsLog);
    }
    /**
     * Valide les limites et retourne la configuration client.
     */
    private ClientConfig validateAndIncrementLimit(User user) throws MessageLimitExceededException {
        ClientConfig clientConfig = clientConfigService.getByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Client configuration not found for user: " + user.getId()));

        Integer monthlySmsLimit = user.getSubscription().getMonthlySmsLimit();
        Integer messageCount = clientConfig.getMessageCountThisMonth();

        if (messageCount != null && monthlySmsLimit != null && messageCount >= monthlySmsLimit) {
            throw new MessageLimitExceededException("La limite de messages mensuelle a été atteinte.");
        }
        return clientConfig;
    }

    /**
     * Exécute le workflow d'envoi principal (WhatsApp -> Fallback).
     */
    private MessageSendingResult executeTwilioSend(User user, String to, String type, LocalDateTime scheduleDate, Long rentalId, ClientConfig config,
                                                   com.twilio.type.PhoneNumber whatsappFrom, com.twilio.type.PhoneNumber smsFrom) { // ⬅️ PARAMÈTRES SUPPLÉMENTAIRES
        try {
            Rental rental = rentalService.findById(rentalId, user).orElseThrow(() -> new IllegalArgumentException("Location non trouvée."));
            String fallbackMessageBody = getFallbackBody(type, config);
            boolean isScheduled = scheduleDate != null;

            if (type.equals(TYPE_RAPPEL) || type.equals(TYPE_RELANCE) || type.equals(TYPE_RELANCE_URGENTE)) {
                try {
                    // ➡️ TENTATIVE 1 : WHATSAPP (Templates) ⬅️
                    Map<String, String> variables = buildTemplateVariables(rental, type);
                    String templateSid = getTemplateSidByType(type);

                    // Construction du Creator
                    MessageCreator creator = buildWhatsappCreator(to, type, isScheduled, scheduleDate, whatsappFrom);
                    creator.setContentSid(templateSid);
                    creator.setContentVariables(new JSONObject(variables).toString());
                    creator.create();

                    // ➡️ CORRECTION 3: Enregistrement du corps du message formaté pour la traçabilité ⬅️
                    String templateName = getTemplateNameByType(type);
                    String finalBodyForLog = replacePlaceholders(templateName, rental);

                    return new MessageSendingResult("SENT_WHATSAPP", finalBodyForLog);

                } catch (Exception whatsappException) {
                    logger.warn("Échec de l'envoi via WhatsApp. Tentative d'envoi par SMS: {}", whatsappException.getMessage());

                    // ➡️ TENTATIVE 2 : SMS FALLBACK ⬅️
                    return sendSmsFallback(user, to, type, isScheduled, rentalId, fallbackMessageBody, rental, scheduleDate, smsFrom);
                }
            }

            // Si ce n'est pas un type de message connu (ex: confirmation par SMS simple sans template)
            return sendSmsFallback(user, to, type, isScheduled, rentalId, fallbackMessageBody, rental, scheduleDate, smsFrom);
        } catch (Exception e) {
            return new MessageSendingResult("FAILED", e.getMessage());
        }
    }

    /**
     * Gère la logique de construction de l'objet MessageCreator pour WhatsApp.
     */
    private MessageCreator buildWhatsappCreator(String to, String type, boolean isScheduled, LocalDateTime scheduleDate, com.twilio.type.PhoneNumber whatsappFrom) {
        MessageCreator creator;

        // 🛑 CORRECTION TWILIO : Utiliser le fromPhoneNumber explicite (whatsappFrom) pour le FROM
        if (isScheduled) {
            creator = Message.creator(new com.twilio.type.PhoneNumber("whatsapp:" + to), whatsappFrom, getTemplateNameByType(type));
            creator.setSendAt(scheduleDate.atZone(ZoneId.systemDefault()));
            creator.setScheduleType(Message.ScheduleType.FIXED);
        } else {
            creator = Message.creator(new com.twilio.type.PhoneNumber("whatsapp:" + to), whatsappFrom, getTemplateNameByType(type));
        }
        return creator;
    }

    /**
     * Gère l'envoi par SMS classique (fallback ou message direct sans template).
     */
    private MessageSendingResult sendSmsFallback(User user, String to, String type, boolean isScheduled, Long rentalId, String fallbackMessageBody, Rental rental, LocalDateTime scheduleDate, com.twilio.type.PhoneNumber smsFrom) {
        try {
            String problemUrl = "https://waraloyer.com/tenant-problem/" + rentalId;
            String finalSmsBody = replacePlaceholders(fallbackMessageBody, rental);

            // CONVENTION CLEAN CODE: Un seul appel à Message.creator par envoi
            MessageCreator smsCreator;

            if (isScheduled) {
                // Envoi planifié via SID
                smsCreator = Message.creator(new PhoneNumber(to), messagingServiceSid, finalSmsBody);
                smsCreator.setSendAt(scheduleDate.atZone(ZoneId.systemDefault()));
                smsCreator.setScheduleType(Message.ScheduleType.FIXED);
            } else {
                // 🛑 CORRECTION 4: Utiliser le PhoneNumber Twilio pour le FROM (pour éviter 21212)
                smsCreator = Message.creator(new PhoneNumber(to), smsFrom, finalSmsBody);
            }

            smsCreator.create();

            // Ajout de l'URL pour la traçabilité dans le log, sans double envoi de SMS.
            finalSmsBody += " | URL: " + problemUrl;

            return new MessageSendingResult("SENT_SMS", finalSmsBody);
        } catch (Exception e) {
            logger.error("Échec de l'envoi SMS de fallback : {}", e.getMessage());
            return new MessageSendingResult("FAILED", e.getMessage());
        }
    }
    /**
     * Détermine le corps du message de fallback/défaut.
     */
    private String getFallbackBody(String type, ClientConfig config) {
        return switch (type) {
            case TYPE_RAPPEL -> config.getSmsReminderMessage();
            case TYPE_RELANCE -> config.getSmsRelanceMessage();
            case TYPE_RELANCE_URGENTE -> "Rappel urgent : le loyer de {MONTANT} FCFA pour le bien situé au {ADRESSE_BIEN} est en retard. Merci de régulariser.";
            default -> "Le service de messagerie est temporairement indisponible. Veuillez contacter le propriétaire.";
        };
    }

    /**
     * Construit les variables dynamiques pour le template Twilio Content SID.
     */
    private Map<String, String> buildTemplateVariables(Rental rental, String type) {
        Map<String, String> variables = new HashMap<>();

        switch (type) {
            case TYPE_RAPPEL, TYPE_RELANCE -> {
                variables.put("1", rental.getTenant().getFirstName());
                variables.put("2", rental.getProperty().getAddress());
                if (type.equals(TYPE_RAPPEL)) {
                    variables.put("3", rental.getDueDate().toString());
                }
            }
            case TYPE_RELANCE_URGENTE -> {
                variables.put("1", String.valueOf(rental.getAmountDue()));
                variables.put("2", rental.getProperty().getAddress());
                String problemUrl = "https://waraloyer.com/tenant-problem/" + rental.getId();
                variables.put("3", problemUrl);
            }
        }
        return variables;
    }

    /**
     * Classe DTO interne pour encapsuler le résultat de l'envoi.
     */
    private static class MessageSendingResult {
        final String status;
        final String messageBody;

        public MessageSendingResult(String status, String messageBody) {
            this.status = status;
            this.messageBody = messageBody;
        }
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

        if (newMessage.contains("{ADRESSE_BIEN}")) {
            newMessage = newMessage.replace("{ADRESSE_BIEN}", rental.getProperty().getAddress());
        }

        return newMessage;
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
    private String getTemplateNameByType(String type) {
        return switch (type) {
            case TYPE_RAPPEL -> TEMPLATE_RAPPEL_NAME;
            case TYPE_RELANCE -> TEMPLATE_RELANCE_NAME;
            case TYPE_RELANCE_URGENTE -> TEMPLATE_RELANCE_URGENTE;
            default -> throw new IllegalArgumentException("Type de template non supporté: " + type);
        };
    }

    private String getTemplateSidByType(String type) {
        return switch (type) {
            case TYPE_RAPPEL -> TEMPLATE_RAPPEL_SID;
            case TYPE_RELANCE -> TEMPLATE_RELANCE_SID;
            case TYPE_RELANCE_URGENTE -> TEMPLATE_RELANCE_URGENTE_SID;
            default -> throw new IllegalArgumentException("Type de template non supporté: " + type);
        };
    }

}
