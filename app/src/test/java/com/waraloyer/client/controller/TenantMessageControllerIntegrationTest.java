package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.model.TenantMessageLog;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.TenantMessageService;
import com.waraloyer.client.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TenantMessageControllerIntegrationTest {

    private static final String API_BASE_URL = "/api/tenant-messages";
    private final Long MOCK_RENTAL_ID = 70L;
    private final String MOCK_MESSAGE_CONTENT = "Problème réglé.";

    private User testUser;
    private UserDetails mockUserDetails;
    private TenantMessageLog mockMessageLog;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TenantMessageService tenantMessageService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setup() {
        reset(tenantMessageService, userService);

        // 1. Configurer l'utilisateur simulé
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("message@owner.com");
        testUser.setUsername("message@owner.com");

        // 2. Simuler UserDetails pour MockMvc
        mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(testUser.getEmail())
                .password("dummy")
                .roles("USER")
                .build();

        // 3. Configurer l'objet TenantMessageLog mocké
        mockMessageLog = new TenantMessageLog();
        mockMessageLog.setId(10L);
        mockMessageLog.setMessage(MOCK_MESSAGE_CONTENT);

        // 4. Configuration de base du UserService
        when(userService.findUserByEmail(testUser.getEmail())).thenReturn(testUser);
    }


    @Test
    void shouldGetMessagesByRental_WhenAuthenticated() throws Exception {
        // GIVEN: Le service retourne une liste de messages
        List<TenantMessageLog> mockLogs = Collections.singletonList(mockMessageLog);

        // Simuler la recherche dans le service
        when(tenantMessageService.findByRentalId(eq(MOCK_RENTAL_ID), eq(testUser)))
                .thenReturn(mockLogs);

        // WHEN & THEN
        mockMvc.perform(get(API_BASE_URL + "/by-rental/{rentalId}", MOCK_RENTAL_ID)
                        .with(user(mockUserDetails))) // Doit être authentifié

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].message").value(MOCK_MESSAGE_CONTENT));

        // VÉRIFICATION: Le service a été appelé avec l'ID de location et l'utilisateur authentifié
        verify(tenantMessageService, times(1)).findByRentalId(eq(MOCK_RENTAL_ID), eq(testUser));
    }

    @Test
    void shouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // WHEN: Tentative d'accès anonyme
        mockMvc.perform(get(API_BASE_URL + "/by-rental/{rentalId}", MOCK_RENTAL_ID))
                // THEN: La sécurité devrait rejeter l'accès
                .andExpect(status().isForbidden());

        verify(tenantMessageService, never()).findByRentalId(anyLong(), any(User.class));
    }
}