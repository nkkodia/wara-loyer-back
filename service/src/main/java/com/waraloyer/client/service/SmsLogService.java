package com.waraloyer.client.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
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

    private final SmsLogRepository smsLogRepository;
    private final UserService userService;

    private final RentalService rentalService;


    @Autowired
    public SmsLogService(SmsLogRepository smsLogRepository, UserService userService, RentalService rentalService) {
        this.smsLogRepository = smsLogRepository;
        this.userService = userService;
        this.rentalService = rentalService;
    }

    public SmsLog sendSms(User user, String to, String messageBody, String type, LocalDateTime scheduleDate, Long rentalId) {
        SmsLog smsLog = new SmsLog();
        try {
            Twilio.init(accountSid, authToken);
            if (messageBody == null || messageBody.trim().isEmpty()) {
                messageBody = "Le message est vide.";
            }
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
            smsLog.setStatus("SENT");
            logger.info("SMS de type '{}' envoyé avec succès au numéro {} pour l'utilisateur {}", type, to, user.getEmail());
        } catch (Exception e) {
            smsLog.setStatus("FAILED");
            logger.error("Échec de l'envoi du SMS de type '{}' au numéro {}: {}", type, to, e.getMessage());
        } finally {
            // Sauvegarder le log, qu'il y ait eu succès ou échec
            smsLog.setUser(user);
            smsLog.setToPhoneNumber(to);
            smsLog.setMessage(messageBody);
            smsLog.setType(type);
            smsLog.setSentDate(LocalDate.now());
            if (rentalId != null) {
                smsLog.setRental(rentalService.findById(rentalId).orElse(null));
            }
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
