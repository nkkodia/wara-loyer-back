package com.waraloyer.client.service;

import com.waraloyer.client.dto.UserCreatePasswordDTO;
import com.waraloyer.client.model.PasswordResetToken;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.PasswordResetTokenRepository;
import com.waraloyer.client.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository; // Nouveau
    private final EmailService emailService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordResetTokenRepository tokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("E-mail non reconnu."));
    }

    public void createPassword(UserCreatePasswordDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé."));

        if (user.isEnabled()) {
            throw new IllegalArgumentException("Le compte est déjà activé.");
        }
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(true); // Active le compte après la création du mot de passe

        userRepository.save(user);
    }

    // Méthode pour valider l'accès de l'utilisateur
    public boolean isAccessValid(User user) {
        if (user.getSubscriptionEndDate() == null) {
            return false; // Pas d'abonnement
        }
        return user.getSubscriptionEndDate().isAfter(LocalDate.now());
    }

    /**
     * Crée un jeton de réinitialisation et envoie un lien par email.
     */
    @Transactional
    public void createPasswordResetToken(String email) throws EntityNotFoundException {
        // 1. Trouver l'utilisateur
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        // 2. Supprimer tout ancien jeton pour cet utilisateur
        tokenRepository.deleteByUser(user);

        // 3. Générer le nouveau jeton
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);

        // 4. Sauvegarder le nouveau jeton
        tokenRepository.save(resetToken);

        // 5. Envoyer le lien (Simulation ici)
        String resetLink = "waraloyer.com/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    /**
     * Valide le jeton et réinitialise le mot de passe de l'utilisateur.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) throws IllegalArgumentException {
        // 1. Trouver le jeton
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Jeton invalide."));

        // 2. Vérifier l'expiration
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken); // Nettoyage
            throw new IllegalArgumentException("Jeton expiré.");
        }

        // 3. Réinitialiser le mot de passe de l'utilisateur
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 4. Supprimer le jeton utilisé
        tokenRepository.delete(resetToken);
    }
}