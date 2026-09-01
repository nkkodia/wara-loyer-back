package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.dto.SmsRequestDTO;
import com.waraloyer.client.model.SmsLog;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.SmsLogService;
import com.waraloyer.client.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SmsLogControllerIntegrationTest {

    private static final String API_BASE_URL = "/api/sms";
    private final Long MOCK_RENTAL_ID = 50L;

    private User testUser;
    private UserDetails mockUserDetails;
    private SmsLog mockSmsLog;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SmsLogService smsLogService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setup() {
        reset(smsLogService, userService);

        // 1. Configurer l'utilisateur simulé
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("sms@owner.com");
        testUser.setUsername("sms@owner.com");

        // 2. Simuler UserDetails pour MockMvc
        mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(testUser.getEmail())
                .password("dummy")
                .roles("USER")
                .build();

        // 3. Configurer l'objet SmsLog mocké (aligné sur la classe SmsLog fournie)
        mockSmsLog = new SmsLog();
        mockSmsLog.setId(100L);
        mockSmsLog.setType("RAPPEL");
        mockSmsLog.setMessage("Ceci est un message de test.");
        mockSmsLog.setSentDate(LocalDate.now());

        // 4. Configuration de base du UserService
        when(userService.findUserByEmail(testUser.getEmail())).thenReturn(testUser);
    }


            @Test
            void shouldGetAllSmsLogs_AndReturn200() throws Exception {
        // GIVEN: Le service retourne une liste de logs
        List<SmsLog> mockLogs = Collections.singletonList(mockSmsLog);
        when(smsLogService.findAll()).thenReturn(mockLogs);

        // WHEN & THEN
        mockMvc.perform(get(API_BASE_URL + "/logs")
                        .with(user(mockUserDetails))) // Doit être authentifié

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].message").value(mockSmsLog.getMessage()));

        verify(smsLogService, times(1)).findAll();
    }

    @Test
    void shouldGetLogsByRentalId_AndReturn200() throws Exception {
        // GIVEN: Le service retourne les logs pour un ID de location spécifique
        List<SmsLog> mockLogs = Collections.singletonList(mockSmsLog);

        // Simuler la recherche dans le service
        when(smsLogService.findByRentalIdAndUserId(eq(MOCK_RENTAL_ID), any(Authentication.class))).thenReturn(mockLogs);

        // WHEN & THEN
        mockMvc.perform(get(API_BASE_URL + "/logs/by-rental/{rentalId}", MOCK_RENTAL_ID)
                        .with(user(mockUserDetails)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // VÉRIFICATION: Le service est appelé
        verify(smsLogService, times(1)).findByRentalIdAndUserId(eq(MOCK_RENTAL_ID), any(Authentication.class));
    }

    @Test
    void shouldSendMessage_AndReturn200() throws Exception {
        // GIVEN: DTO de requête SMS
        SmsRequestDTO requestDTO = new SmsRequestDTO();
        requestDTO.setToPhoneNumber("+33612345678");
        requestDTO.setType("RELANCE");
        requestDTO.setScheduleDate(LocalDate.now().plusDays(1).atStartOfDay());

        // Le service retourne le log créé
        when(smsLogService.sendSms(
                eq(testUser),
                eq(requestDTO.getToPhoneNumber()),
                eq(requestDTO.getType()),
                eq(requestDTO.getScheduleDate()),
                eq(MOCK_RENTAL_ID)))
                .thenReturn(mockSmsLog);

        // WHEN & THEN
        mockMvc.perform(post(API_BASE_URL + "/send-message/{rentalId}", MOCK_RENTAL_ID)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.type").value("RAPPEL")); // Vérifie le type retourné

        // VÉRIFICATION: Le service est appelé avec le bon utilisateur extrait du contexte
        verify(smsLogService, times(1)).sendSms(
                eq(testUser), // L'objet currentUser est bien 'testUser'
                eq(requestDTO.getToPhoneNumber()),
                eq(requestDTO.getType()),
                eq(requestDTO.getScheduleDate()),
                eq(MOCK_RENTAL_ID)
        );
    }

    @Test
    void shouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // WHEN: Tentative d'accès anonyme
        mockMvc.perform(get(API_BASE_URL + "/logs"))
                // THEN: La sécurité devrait rejeter l'accès
                .andExpect(status().isForbidden());

        verify(smsLogService, never()).findAll();
    }
}