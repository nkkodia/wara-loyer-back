package com.waraloyer.client.service;

import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.UserRepository;
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
    public User createPassword(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        }
        User existingUser = findByEmail(user.getEmail());
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(existingUser);
    }

    // Méthode pour valider l'accès de l'utilisateur
    public boolean isAccessValid(User user) {
        if (user.getSubscriptionEndDate() == null) {
            return false; // Pas d'abonnement
        }
        return user.getSubscriptionEndDate().isAfter(LocalDate.now());
    }
}