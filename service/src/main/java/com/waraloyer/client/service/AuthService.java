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

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("E-mail non reconnu."));
    }

    public User createPassword(UserCreatePasswordDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé."));

        if (user.isEnabled()) {
            throw new IllegalArgumentException("Le compte est déjà activé.");
        }
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(true); // Active le compte après la création du mot de passe

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