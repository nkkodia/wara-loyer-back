package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.dto.UserCreatePasswordDTO;
import com.waraloyer.client.model.Subscription;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.SubscriptionRepository;
import com.waraloyer.client.repository.UserRepository;
import com.waraloyer.client.service.AuthService;
import com.waraloyer.client.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // <<< Changement d'import
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
// Suppression de ContextConfiguration, TestConfiguration, Primary, Bean
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import jakarta.persistence.EntityNotFoundException;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

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
    @MockBean
    private AuthenticationManager authenticationManager;

    // Injecter les Mocks via @MockBean (remplace la configuration manuelle)
    // C'est l'approche la plus fiable pour les tests d'intégration Spring Boot
    @MockBean
    private AuthService authService;
    @MockBean
    private UserService userService;

    private User existingUser; // Utilisateur avec un mot de passe haché et valide
    private Subscription defaultSubscription;

    private final String CREATE_PASSWORD_API = "/api/auth/create-password";
    private final String LOGIN_API = "/api/auth/login";

    @BeforeEach
    void setup() {
        // Il faut réinitialiser les comportements des mocks pour chaque test
        reset(authService, userService);

        userRepository.deleteAll();
        subscriptionRepository.deleteAll();

        // 1. CRÉATION D'UN ABONNEMENT VALIDE
        defaultSubscription = new Subscription();
        defaultSubscription.setName("BASIC_" + UUID.randomUUID().toString());
        defaultSubscription.setMonthlySmsLimit(100);
        defaultSubscription.setPrice(BigDecimal.valueOf(10.00));
        defaultSubscription = subscriptionRepository.save(defaultSubscription);

        // 2. CRÉATION DE L'UTILISATEUR STANDARD POUR LES TESTS DE CONNEXION/EXPIRATION
        existingUser = new User();
        existingUser.setUsername("existing@example.com");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("SecurePassInitial"));
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");

        // Champs NOT NULL
        existingUser.setSubscriptionEndDate(LocalDate.now().plusYears(1));
        existingUser.setCreatedAt(LocalDateTime.now());
        existingUser.setEnabled(true); // Utilisateur actif par défaut pour les tests de connexion
        existingUser.setSubscription(defaultSubscription);

        // La persistance doit utiliser le VRAI repository
        userRepository.save(existingUser);

        // Configuration de Mockito : Maintenant, userService et authService sont des mocks et when() fonctionne
        when(authService.isAccessValid(any(User.class))).thenReturn(true);
        // Simuler la recherche du user par email par le contrôleur de connexion
        when(userService.findUserByEmail(existingUser.getEmail())).thenReturn(existingUser);

    }

    @Test
    void shouldCreatePassword_WhenUserIsPreRegistered() throws Exception {
        User preRegisteredUser = new User();
        preRegisteredUser.setUsername("prereg@example.com");
        preRegisteredUser.setEmail("prereg@example.com");
        preRegisteredUser.setFirstName("Pre");
        preRegisteredUser.setLastName("Reg");
        final String TEMP_PASSWORD_PLACEHOLDER = "";

        preRegisteredUser.setPassword(passwordEncoder.encode(TEMP_PASSWORD_PLACEHOLDER));

        preRegisteredUser.setSubscriptionEndDate(LocalDate.now().plusYears(1));
        preRegisteredUser.setCreatedAt(LocalDateTime.now());
        preRegisteredUser.setEnabled(false); // L'utilisateur est désactivé tant que le mot de passe n'est pas créé.
        preRegisteredUser.setSubscription(defaultSubscription);

        userRepository.save(preRegisteredUser);

        // DTO (contient le VRAI nouveau mot de passe à définir)
        UserCreatePasswordDTO dto = new UserCreatePasswordDTO();
        dto.setEmail(preRegisteredUser.getEmail());
        dto.setPassword("NewSecurePassword123");

        // WHEN: Appel de l'endpoint
        mockMvc.perform(post(CREATE_PASSWORD_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(content().string("Mot de passe créé avec succès pour " + preRegisteredUser.getEmail()));

        verify(authService, times(1)).createPassword(any(UserCreatePasswordDTO.class));
    }

    @Test
    void shouldReturn404_WhenEmailIsNotFoundForCreatePassword() throws Exception {
        // GIVEN: Le DTO avec un email inconnu
        UserCreatePasswordDTO dto = new UserCreatePasswordDTO();
        dto.setEmail("unknown@example.com");
        dto.setPassword("AnyPassword");

        // Simuler l'exception du service
        doThrow(new EntityNotFoundException()).when(authService).createPassword(any(UserCreatePasswordDTO.class));

        mockMvc.perform(post(CREATE_PASSWORD_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isNotFound())
                .andExpect(content().string("Email inconnu."));
    }

    @Test
    void shouldReturnForbidden_WhenSubscriptionIsExpired() throws Exception {
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn(existingUser.getEmail());

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(mockAuthentication);

        when(authService.isAccessValid(any(User.class))).thenReturn(false);

        when(userService.findUserByEmail(existingUser.getEmail())).thenReturn(existingUser);

        User loginAttempt = new User();
        loginAttempt.setEmail(existingUser.getEmail());
        loginAttempt.setPassword("SecurePassInitial");

        mockMvc.perform(post(LOGIN_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAttempt)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Votre compte est désactivé ou votre abonnement a expiré."));
    }


    @Test
    void shouldReturnUnauthorized_WhenBadCredentials() throws Exception {
        User badLogin = new User();
        badLogin.setEmail(existingUser.getEmail());
        badLogin.setPassword("incorrect_pass"); // Mot de passe incorrect

        doThrow(new BadCredentialsException("Identifiants invalides."))
                .when(authenticationManager)
                .authenticate(any(Authentication.class));

        mockMvc.perform(post(LOGIN_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Identifiants invalides."));
    }

    @Test
    void shouldReturnToken_WhenCredentialsAreValid() throws Exception {
        User goodLogin = new User();
        goodLogin.setEmail(existingUser.getEmail());
        goodLogin.setPassword("SecurePassInitial");

        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn(existingUser.getEmail());

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(mockAuthentication);

        when(userService.findUserByEmail(existingUser.getEmail())).thenReturn(existingUser);
        when(authService.isAccessValid(any(User.class))).thenReturn(true);

        // WHEN: Appel de connexion
        mockMvc.perform(post(LOGIN_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goodLogin)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
