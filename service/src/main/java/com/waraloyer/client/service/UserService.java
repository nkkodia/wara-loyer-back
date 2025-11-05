package com.waraloyer.client.service;

import com.waraloyer.client.dto.PasswordUpdateDTO;
import com.waraloyer.client.model.Role;
import com.waraloyer.client.model.Subscription;
import com.waraloyer.client.model.User;
import com.waraloyer.client.model.enums.ERole;
import com.waraloyer.client.repository.SubscriptionRepository;
import com.waraloyer.client.repository.UserRepository;
import com.waraloyer.client.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService { // <-- Ajout de l'interface UserDetailsService

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionRepository subscriptionRepository; // <<<< DOIT ÊTRE INJECTÉ

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionRepository = subscriptionRepository;
    }

    public void registerNewUser(User userFromRequest) {
        if (Boolean.TRUE.equals(userRepository.existsByUsername(userFromRequest.getUsername()))) {
            throw new IllegalArgumentException("Le nom d'utilisateur est déjà utilisé.");
        }
        if (Boolean.TRUE.equals(userRepository.existsByEmail(userFromRequest.getEmail()))) {
            throw new IllegalArgumentException("Cet e-mail est déjà utilisé.");
        }

        // --- CRÉATION ET INITIALISATION DU NOUVEL UTILISATEUR ---
        User newUser = new User();

        // Transfert des propriétés
        newUser.setUsername(userFromRequest.getUsername());
        newUser.setEmail(userFromRequest.getEmail());
        newUser.setFirstName(userFromRequest.getFirstName());
        newUser.setLastName(userFromRequest.getLastName());

        // Propriétés par défaut / Sécurité
        newUser.setPassword(passwordEncoder.encode("motDePasse"));
        newUser.setEnabled(false);


        newUser.setSubscriptionEndDate(
                LocalDateTime.now().plusYears(1).toLocalDate()
        );
        newUser.setCreatedAt(LocalDateTime.now());

        Subscription defaultSubscription = subscriptionRepository.findByName("BASIC")
                .orElseThrow(() -> new RuntimeException("Erreur: L'abonnement par défaut n'a pas été trouvé."));

        newUser.setSubscription(defaultSubscription);

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Erreur: Le rôle USER n'a pas été trouvé."));

        newUser.getRoles().add(userRole);

        userRepository.save(newUser);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé."));
    }



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Recherche l'utilisateur par email pour la connexion
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList() // Liste d'autorités/rôles vide pour le moment
        );
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé."));
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    public User updatePassword(Long userId, PasswordUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé."));

        // 1. Vérifier si l'ancien mot de passe est correct
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new AccessDeniedException("Ancien mot de passe incorrect.");
        }

        // 2. Mettre à jour le mot de passe avec le nouveau haché
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        // 3. Sauvegarder l'utilisateur mis à jour
        return userRepository.save(user);
    }
}
