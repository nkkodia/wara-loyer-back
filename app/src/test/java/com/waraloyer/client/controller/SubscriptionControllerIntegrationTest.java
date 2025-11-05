package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.dto.PasswordUpdateDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID; // Importez la classe UUID pour un nom unique

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Assure l'isolation des tests en annulant les changements de BDD
class SubscriptionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User existingUser;
    private Subscription existingSubscription;
    private final String SUBSCRIPTION_API = "/api/management/subscription";
    private final String CHANGE_PASSWORD_API = "/api/management/change-password";

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        subscriptionRepository.deleteAll();

        Subscription defaultSubscription = new Subscription();
        // CORRECTION: Rendre le nom UNIQUE pour éviter la violation de contrainte
        defaultSubscription.setName("BASIC_" + UUID.randomUUID().toString());
        defaultSubscription.setMonthlySmsLimit(100);
        defaultSubscription.setPrice(BigDecimal.valueOf(10.00));
        existingSubscription = subscriptionRepository.save(defaultSubscription);

        existingUser = new User();
        existingUser.setUsername("testuser@waraloyer.com");
        existingUser.setEmail("testuser@waraloyer.com");
        existingUser.setPassword(passwordEncoder.encode("oldpassword123"));
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");

        existingUser.setSubscriptionEndDate(LocalDate.now().plusYears(1));
        existingUser.setCreatedAt(LocalDateTime.now());
        existingUser.setEnabled(true);
        existingUser.setSubscription(existingSubscription);
        userRepository.save(existingUser);
    }


    @Test
    void shouldReturnUserSubscription_WhenAuthenticated() throws Exception {
        mockMvc.perform(get(SUBSCRIPTION_API)
                        .with(user(existingUser.getUsername())
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))) // Simuler l'authentification

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.price").value(10.00));
    }

    @Test
    void shouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get(SUBSCRIPTION_API))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldChangePassword_WhenOldPasswordIsValid() throws Exception {
        PasswordUpdateDTO validDto = new PasswordUpdateDTO();
        validDto.setOldPassword("oldpassword123");
        validDto.setNewPassword("NewSecurePass456");

        mockMvc.perform(post(CHANGE_PASSWORD_API)
                        .with(user(existingUser.getUsername())
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))

                .andExpect(status().isNoContent());

        User updatedUser = userRepository.findByUsername(existingUser.getUsername()).orElseThrow();
        assert (passwordEncoder.matches("NewSecurePass456", updatedUser.getPassword()));
    }

    @Test
    void shouldReturnBadRequest_WhenOldPasswordIsIncorrect() throws Exception {
        PasswordUpdateDTO invalidDto = new PasswordUpdateDTO();
        invalidDto.setOldPassword("wrongpassword");
        invalidDto.setNewPassword("NewSecurePass456");

        mockMvc.perform(post(CHANGE_PASSWORD_API)
                        .with(user(existingUser.getUsername())
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
