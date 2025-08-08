package com.waraloyer.client.service;


import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.waraloyer.client.model.SmsLog;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.SmsLogRepository;
import com.waraloyer.client.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-phone-number}")
    private String fromPhoneNumber;

    private final SmsLogRepository smsLogRepository;
    private final UserRepository userRepository;

    public SmsService(SmsLogRepository smsLogRepository, UserRepository userRepository) {
        this.smsLogRepository = smsLogRepository;
        this.userRepository = userRepository;
    }

    public void sendSms(User user, String to, String messageBody, String type) {
        // Initialiser Twilio
        Twilio.init(accountSid, authToken);

        try {
            // Envoyer le SMS
            Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            SmsLog smsLog = new SmsLog();
            smsLog.setUser(user);
            smsLog.setToPhoneNumber(to);
            smsLog.setMessage(messageBody);
            smsLog.setType(type);
            smsLog.setSentDate(LocalDateTime.now());
            smsLog.setStatus("Sent");
            smsLogRepository.save(smsLog);

        } catch (Exception e) {
            SmsLog smsLog = new SmsLog();
            smsLog.setUser(user);
            smsLog.setToPhoneNumber(to);
            smsLog.setMessage(messageBody);
            smsLog.setType(type);
            smsLog.setSentDate(LocalDateTime.now());
            smsLog.setStatus("Failed");
            smsLogRepository.save(smsLog);
        }
    }
}