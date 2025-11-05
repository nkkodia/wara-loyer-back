package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.model.Subscription;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.SubscriptionRepository;
import com.waraloyer.client.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user; // <-- C'EST LA LIGNE MANQUANTE
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc; // Utilitaire pour simuler des requêtes HTTP

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    User existingUser = new User();

    @BeforeEach
    void setup() {
        // 1. Nettoyage TOTAL
        userRepository.deleteAll();
        subscriptionRepository.deleteAll();

        // 2. CRÉATION ET INITIALISATION DE L'ABONNEMENT
        Subscription defaultSubscription = new Subscription();
        defaultSubscription.setName("BASIC");
        defaultSubscription.setMonthlySmsLimit(100);
        defaultSubscription.setPrice(BigDecimal.valueOf(10.00));

        Subscription savedSubscription = subscriptionRepository.save(defaultSubscription);

        // 3. CRÉATION ET INITIALISATION DE L'UTILISATEUR (existingUser)
        existingUser = new User();

        existingUser.setUsername("admin@waraloyer.com");
        existingUser.setEmail("admin@waraloyer.com");
        existingUser.setPassword(passwordEncoder.encode("SecureAdminPass"));
        existingUser.setFirstName("Admin");
        existingUser.setLastName("User");

        // Champs NOT NULL
        existingUser.setSubscriptionEndDate(LocalDate.now().plusYears(1));
        existingUser.setCreatedAt(LocalDateTime.now());
        existingUser.setEnabled(true);

        // Lier l'objet Subscription PERSISTÉ
        existingUser.setSubscription(savedSubscription);

        // Sauvegarder l'utilisateur
        userRepository.save(existingUser);
    }
    @Test
    void shouldRegisterUser_WhenDataIsValid() throws Exception {
        // GIVEN: Un nouvel utilisateur à enregistrer
        User newUser = new User();
        newUser.setUsername("newuser@example.com");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("initialPassword123");
        newUser.setFirstName("New");
        newUser.setLastName("User");

        // Simuler la requête POST, SANS authentification (si l'endpoint est public, ex: pour l'admin non-authentifié)
        // NOTE: Si cet endpoint est protégé et réservé à l'ADMIN, utilisez .with(user(...).authorities(...))
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))

                .andExpect(status().isOk())
                .andExpect(content().string("Utilisateur enregistré avec succès !"));

        User savedUser = userRepository.findByUsername("newuser@example.com").orElseThrow();
        assertEquals("New", savedUser.getFirstName());
    }


    @Test
    void shouldReturnAllUsers_WhenAuthenticatedAsAdmin() throws Exception {

        Subscription baseSubscription = subscriptionRepository.findByName("BASIC").orElseThrow();
        User anotherUser = new User();

        anotherUser.setUsername("test@example.com");
        anotherUser.setEmail("test@example.com"); // Email
        anotherUser.setPassword(passwordEncoder.encode("testpass")); // Password haché
        anotherUser.setFirstName("Regular"); // <<< AJOUTEZ CE CHAMP MANQUANT
        anotherUser.setLastName("User");    // <<< AJOUTEZ CE CHAMP MANQUANT

        // Champs de relation/date obligatoires
        anotherUser.setSubscriptionEndDate(LocalDate.now().plusYears(1));
        anotherUser.setCreatedAt(LocalDateTime.now());
        anotherUser.setSubscription(baseSubscription); // Lier à l'objet Subscription valide
        anotherUser.setEnabled(true);

        userRepository.save(anotherUser);

        mockMvc.perform(get("/api/auth/users")
                        .with(user("admin@waraloyer.com")
                                .password("SecureAdminPass")
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnForbidden_WhenNotAuthenticatedAsAdmin() throws Exception {
        mockMvc.perform(get("/api/auth/users")
                        .with(user("admin@waraloyer.com")
                                .password("SecureAdminPass")
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))

                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Tenter d'accéder sans authentification
        mockMvc.perform(get("/api/auth/users"))

                // THEN: Vérifie que la réponse HTTP est 403 Forbidden ou 401 Unauthorized selon la config
                // Le plus souvent 403 pour Spring Security après le filtre
                .andExpect(status().isForbidden());
    }
}