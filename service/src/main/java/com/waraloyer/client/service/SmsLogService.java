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
import java.util.List;

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


    @Autowired
    public SmsLogService(SmsLogRepository smsLogRepository, UserService userService, ClientConfigService clientConfigService, RentalService rentalService) {
        this.smsLogRepository = smsLogRepository;
        this.userService = userService;
        this.clientConfigService = clientConfigService;
        this.rentalService = rentalService;
    }

    public SmsLog sendSms(User user, String to, String messageBody, String type, LocalDateTime scheduleDate, Long rentalId) {
        ClientConfig config = clientConfigService.getOrCreate(user); // Assurez-vous d'avoir ce service
        if (config.getMessageCountThisMonth() >= config.getMonthlySmsLimit()) {
            throw new MessageLimitExceededException("La limite de SMS mensuelle a été atteinte.");
        }

        SmsLog smsLog = new SmsLog();
        smsLog.setUser(user);
        smsLog.setToPhoneNumber(to);
        smsLog.setMessage(messageBody);
        smsLog.setType(type);
        smsLog.setSentDate(LocalDate.now());

        try {
            Twilio.init(accountSid, authToken);

            // Tenter d'abord l'envoi via WhatsApp
            try {
                MessageCreator creator = Message.creator(
                        new PhoneNumber("whatsapp:" + to),
                        new PhoneNumber("whatsapp:" + fromPhoneNumber),
                        messageBody
                );

                if (scheduleDate != null) {
                    ZonedDateTime zonedDateTime = scheduleDate.atZone(ZoneId.systemDefault());
                    creator.setSendAt(zonedDateTime);
                }
                creator.create();
                smsLog.setStatus("SENT_WHATSAPP");
                logger.info("Message WhatsApp de type '{}' envoyé avec succès au numéro {}", type, to);
            } catch (Exception whatsappException) {
                // Si l'envoi WhatsApp échoue, envoyer un SMS classique
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
                logger.info("Message SMS de type '{}' envoyé avec succès au numéro {}", type, to);
            }
        } catch (Exception finalException) {
            smsLog.setStatus("FAILED");
            logger.error("Échec total de l'envoi de message de type '{}' au numéro {}: {}", type, to, finalException.getMessage());
        } finally {
            if (rentalId != null) {
                smsLog.setRental(rentalService.findById(rentalId, user).orElse(null));
            }
        }

        // Incrémenter le compteur de SMS si l'envoi a réussi
        if (smsLog.getStatus().startsWith("SENT")) {
            config.setMessageCountThisMonth(config.getMessageCountThisMonth() + 1);
            clientConfigService.save(config, user);
        }

        return smsLogRepository.save(smsLog);
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
