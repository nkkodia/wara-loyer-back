package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.dto.request.DashboardSummary;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.DashboardService;
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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerIntegrationTest {

    private static final String API_URL = "/api/dashboard/summary";
    private User testUser;
    private UserDetails mockUserDetails;
    private DashboardSummary mockSummary; // Déclaré

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setup() {
        // Réinitialiser les mocks
        reset(dashboardService, userService);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("dashboard@owner.com");
        testUser.setUsername("dashboard@owner.com");
        testUser.setFirstName("Dash");
        testUser.setLastName("User");

        mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(testUser.getEmail())
                .password("dummy")
                .roles("USER")
                .build();

        // CORRECTION CLÉ : Initialiser l'objet avant d'appeler les setters.
        mockSummary = new DashboardSummary();

        // Renseignement des données mockées avec les NOMS DE CHAMPS CORRIGÉS
        mockSummary.setTotalProperties(5L);
        mockSummary.setTotalRentalsAmount(BigDecimal.valueOf(5000.00));
        mockSummary.setTotalUnpaidRentals(1L);
        mockSummary.setTotalUnpaidAmount(BigDecimal.valueOf(100.00));
        mockSummary.setSmsSentInMonth(10L);

        // Configuration des services
        when(userService.findUserByEmail(testUser.getEmail())).thenReturn(testUser);
        when(dashboardService.getSummaryForUser(testUser)).thenReturn(mockSummary);
    }

    @Test
    void shouldReturnSummary_WhenAuthenticated() throws Exception {
        // WHEN: L'utilisateur authentifié demande le résumé
        mockMvc.perform(get(API_URL)
                        // Simuler l'utilisateur connecté
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON))

                // THEN: Vérifie le statut 200 OK
                .andExpect(status().isOk())

                // CORRECTION des JSON Paths pour correspondre au DTO DashboardSummary :
                .andExpect(jsonPath("$.totalProperties").value(5)) // Correspond à setTotalProperties
                .andExpect(jsonPath("$.totalRentalsAmount").value(5000.00)) // Correspond à setTotalRentalsAmount
                .andExpect(jsonPath("$.totalUnpaidRentals").value(1)); // Correspond à setTotalUnpaidRentals

        // VÉRIFICATION 1 : Vérifie que le UserService a été utilisé pour extraire l'utilisateur
        verify(userService, times(1)).findUserByEmail(testUser.getEmail());

        // VÉRIFICATION 2 : Vérifie que le DashboardService a été appelé avec le bon objet User
        verify(dashboardService, times(1)).getSummaryForUser(eq(testUser));
    }


    @Test
    void shouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // WHEN: Tentative d'accès sans credentials (anonyme)
        mockMvc.perform(get(API_URL)
                        .contentType(MediaType.APPLICATION_JSON))

                // THEN: L'accès devrait être refusé (403 si l'endpoint n'est pas public)
                .andExpect(status().isForbidden());

        // VÉRIFICATION : Assurez-vous qu'aucun service métier n'est appelé
        verify(userService, never()).findUserByEmail(anyString());
        verify(dashboardService, never()).getSummaryForUser(any(User.class));
    }
}