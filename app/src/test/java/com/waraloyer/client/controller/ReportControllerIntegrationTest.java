package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.dto.TenantMessageDTO;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.TenantMessageLog;
import com.waraloyer.client.repository.RentalRepository;
import com.waraloyer.client.repository.TenantMessageLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerIntegrationTest {

    private static final String API_URL = "/api/reporting/report-problem/{rentalId}";
    private final Long VALID_RENTAL_ID = 100L;
    private final Long INVALID_RENTAL_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mocker les dépendances directes du contrôleur (les repositories)
    @MockBean
    private RentalRepository rentalRepository;

    @MockBean
    private TenantMessageLogRepository tenantMessageRepository;

    private Rental mockRental;
    private TenantMessageDTO messageDTO;

    @BeforeEach
    void setup() {
        reset(rentalRepository, tenantMessageRepository);

        // 1. Configurer les objets DTO/Entité
        mockRental = new Rental();
        mockRental.setId(VALID_RENTAL_ID);
        // Note : Les autres champs de Rental doivent être non-null pour la persistance si nécessaire

        messageDTO = new TenantMessageDTO();
        messageDTO.setStatus("URGENT");
        messageDTO.setMessageContent("Fuite dans la salle de bain.");
    }


    @Test
    void shouldRegisterProblem_AndReturn201() throws Exception {
        when(rentalRepository.findById(VALID_RENTAL_ID)).thenReturn(Optional.of(mockRental));

        // GIVEN: Le dépôt de message enregistre le message (doNothing car c'est une méthode save)
        when(tenantMessageRepository.save(any(TenantMessageLog.class))).thenReturn(new TenantMessageLog());

        // WHEN & THEN
        mockMvc.perform(post(API_URL, VALID_RENTAL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDTO)))

                .andExpect(status().isCreated()) // Vérifie le statut 201
                .andExpect(content().string("Message enregistré"));

        // VÉRIFICATION : Assurez-vous que le message a été enregistré
        verify(tenantMessageRepository, times(1)).save(any(TenantMessageLog.class));
    }

    @Test
    void shouldReturn404_WhenRentalNotFound() throws Exception {
        // GIVEN: Le dépôt de location ne trouve pas la location
        when(rentalRepository.findById(INVALID_RENTAL_ID)).thenReturn(Optional.empty());

        // WHEN & THEN
        mockMvc.perform(post(API_URL, INVALID_RENTAL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDTO)))

                .andExpect(status().isNotFound()) // Vérifie le statut 404
                .andExpect(content().string("Location non trouvée"));

        // VÉRIFICATION : Le dépôt de message ne doit jamais être appelé
        verify(tenantMessageRepository, never()).save(any());
    }

    @Test
    void shouldBeAccessibleWithoutAuthentication() throws Exception {
        // GIVEN: Le dépôt de location trouve la location
        when(rentalRepository.findById(VALID_RENTAL_ID)).thenReturn(Optional.of(mockRental));

        // WHEN & THEN: Appel SANS .with(user(...))
        mockMvc.perform(post(API_URL, VALID_RENTAL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDTO)))

                // Le statut doit être 201 (Succès) car cet endpoint n'utilise pas Spring Security
                .andExpect(status().isCreated());

        // VÉRIFICATION : Le message a été enregistré
        verify(tenantMessageRepository, times(1)).save(any(TenantMessageLog.class));
    }
}