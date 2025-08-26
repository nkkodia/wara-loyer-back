package com.waraloyer.client.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.waraloyer.client.model.SmsLog;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.SmsLogRepository;
import com.waraloyer.client.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final UserRepository userRepository;

    @Autowired
    public SmsLogService(SmsLogRepository smsLogRepository, UserRepository userRepository) {
        this.smsLogRepository = smsLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Envoie un SMS à un numéro de téléphone et enregistre l'opération.
     * @param user L'utilisateur qui initie l'envoi.
     * @param to Le numéro de téléphone du destinataire.
     * @param messageBody Le corps du message.
     * @param type Le type de message (RAPPEL, RELANCE, etc.).
     */
    public void sendSms(User user, String to, String messageBody, String type) {
        try {
            Twilio.init(accountSid, authToken);
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            SmsLog smsLog = new SmsLog();
            smsLog.setUser(user);
            smsLog.setToPhoneNumber(to); // Ajout de ce champ, assurez-vous qu'il existe dans le modèle SmsLog
            smsLog.setMessage(messageBody);
            smsLog.setType(type);
            smsLog.setSentDate(LocalDateTime.now());
            smsLog.setStatus("SENT");
            smsLogRepository.save(smsLog);
            logger.info("SMS de type '{}' envoyé avec succès au numéro {} pour l'utilisateur {}", type, to, user.getEmail());

        } catch (Exception e) {
            logger.error("Échec de l'envoi du SMS de type '{}' au numéro {}: {}", type, to, e.getMessage());
            SmsLog smsLog = new SmsLog();
            smsLog.setUser(user);
            smsLog.setToPhoneNumber(to); // Ajout de ce champ
            smsLog.setMessage(messageBody);
            smsLog.setType(type);
            smsLog.setSentDate(LocalDateTime.now());
            smsLog.setStatus("FAILED");
            smsLogRepository.save(smsLog);
        }
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
}
