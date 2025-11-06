package com.waraloyer.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    // Injecter l'adresse e-mail de l'expéditeur à partir de la configuration (application.yml)
    @Value("${spring.mail.username}") // <-- NOUVELLE INJECTION
    private String fromEmail;

    // Nécessaire pour envoyer des e-mails (doit être configuré dans application.properties)
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envoie un e-mail contenant le lien de réinitialisation du mot de passe.
     * @param toEmail L'adresse e-mail du destinataire.
     * @param resetLink Le lien complet contenant le jeton.
     */
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("WaraLoyer - Réinitialisation de votre mot de passe");

            // --- AJOUT DE L'ADRESSE DE L'EXPÉDITEUR ---
            message.setFrom(fromEmail); // <-- Utilisation de la valeur injectée

            String body = "Bonjour,\n\n"
                    + "Vous avez demandé une réinitialisation de mot de passe. "
                    + "Veuillez cliquer sur le lien ci-dessous dans l'heure qui suit :\n\n"
                    + resetLink + "\n\n"
                    + "Si vous n'êtes pas à l'origine de cette demande, veuillez ignorer cet e-mail.\n\n"
                    + "L'équipe WaraLoyer.";

            message.setText(body);
            mailSender.send(message);
            logger.info("Lien de réinitialisation envoyé à : {}", toEmail);

        } catch (MailException e) {
            logger.error("Échec de l'envoi de l'e-mail de réinitialisation à {}: {}", toEmail, e.getMessage());
        }
    }
}