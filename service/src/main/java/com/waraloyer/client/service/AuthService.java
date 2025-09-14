package com.waraloyer.client.service;

import com.waraloyer.client.dto.UserCreatePasswordDTO;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Méthode pour vérifier l'existence de l'e-mail pour un nouvel utilisateur
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("E-mail non reconnu."));
    }

    // Méthode pour créer le mot de passe de l'utilisateur
    public User createPassword(UserCreatePasswordDTO dto) {
        // 1. Chercher l'utilisateur par email
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé."));

        // 2. Vérifier si le mot de passe a déjà été créé
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Mot de passe déjà créé.");
        }

        // 3. Encoder le mot de passe et sauvegarder
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return userRepository.save(user);
    }

    // Méthode pour valider l'accès de l'utilisateur
    public boolean isAccessValid(User user) {
        if (user.getSubscriptionEndDate() == null) {
            return false; // Pas d'abonnement
        }
        return user.getSubscriptionEndDate().isAfter(LocalDate.now());
    }
}