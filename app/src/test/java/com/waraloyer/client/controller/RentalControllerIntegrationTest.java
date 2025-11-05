package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.dto.ReceiptRequestDTO;
import com.waraloyer.client.model.Property;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.ReceiptService;
import com.waraloyer.client.service.RentalService;
import com.waraloyer.client.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RentalControllerIntegrationTest {

    private static final String API_BASE_URL = "/api/rentals";
    private final Long MOCK_RENTAL_ID = 50L;
    private final Long MOCK_USER_ID = 1L;
    private final Long MOCK_TENANT_ID = 5L;

    private User testUser;
    private UserDetails mockUserDetails;
    private Rental mockRental;
    private Property mockProperty;
    private Tenant mockTenant;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RentalService rentalService;

    @MockBean
    private UserService userService;

    @MockBean
    private ReceiptService receiptService;

    @BeforeEach
    void setup() {
        reset(rentalService, userService, receiptService);

        // 1. Configurer l'utilisateur simulé
        testUser = new User();
        testUser.setId(MOCK_USER_ID);
        testUser.setEmail("rental@owner.com");
        testUser.setUsername("rental@owner.com");

        // 2. Simuler UserDetails pour MockMvc
        mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(testUser.getEmail())
                .password("dummy")
                .roles("USER")
                .build();

        // 3. Configurer les dépendances des entités
        mockProperty = new Property();
        mockProperty.setId(10L);
        mockTenant = new Tenant();
        mockTenant.setId(MOCK_TENANT_ID);

        // 4. Configurer l'objet Rental mocké
        mockRental = new Rental();
        mockRental.setId(MOCK_RENTAL_ID);
        mockRental.setAmountDue(BigDecimal.valueOf(1000.00));
        mockRental.setStatus("PENDING");
        mockRental.setUser(testUser);
        mockRental.setProperty(mockProperty);
        mockRental.setTenant(mockTenant);

        when(userService.findUserByEmail(testUser.getEmail())).thenReturn(testUser);
    }


    @Test
    void shouldCreateRental_AndReturn201() throws Exception {
        Rental rentalToCreate = new Rental();
        rentalToCreate.setAmountDue(BigDecimal.valueOf(1200));
        rentalToCreate.setProperty(mockProperty);
        rentalToCreate.setTenant(mockTenant);

        Rental createdRental = new Rental();
        createdRental.setId(51L);
        createdRental.setAmountDue(BigDecimal.valueOf(1200));

        when(rentalService.create(any(Rental.class), eq(testUser))).thenReturn(createdRental);

        // WHEN & THEN
        mockMvc.perform(post(API_BASE_URL)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rentalToCreate)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(51L))
                .andExpect(jsonPath("$.amountDue").value(1200.00));

        verify(rentalService, times(1)).create(any(Rental.class), eq(testUser));
    }

    @Test
    void shouldGetRentalById_AndReturn200() throws Exception {
        when(rentalService.findById(eq(MOCK_RENTAL_ID), eq(testUser))).thenReturn(Optional.of(mockRental));

        mockMvc.perform(get(API_BASE_URL + "/{id}", MOCK_RENTAL_ID)
                        .with(user(mockUserDetails)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(MOCK_RENTAL_ID));
    }

    @Test
    void shouldReturn404_WhenRentalNotFound_OnGetById() throws Exception {
        // GIVEN: Le service retourne Optional.empty()
        when(rentalService.findById(anyLong(), eq(testUser))).thenReturn(Optional.empty());

        mockMvc.perform(get(API_BASE_URL + "/{id}", 999L)
                        .with(user(mockUserDetails)))

                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403_WhenAccessDenied_OnGetById() throws Exception {
        // GIVEN: Le service lève AccessDeniedException
        doThrow(new AccessDeniedException("Accès refusé"))
                .when(rentalService).findById(anyLong(), eq(testUser));

        mockMvc.perform(get(API_BASE_URL + "/{id}", MOCK_RENTAL_ID)
                        .with(user(mockUserDetails)))

                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateRental_AndReturn200() throws Exception {
        // GIVEN: Détails de la mise à jour
        Rental updateDetails = new Rental();
        updateDetails.setAmountDue(BigDecimal.valueOf(1500));

        Rental updatedRental = mockRental;
        updatedRental.setAmountDue(BigDecimal.valueOf(1500));

        when(rentalService.update(eq(MOCK_RENTAL_ID), any(Rental.class), eq(testUser))).thenReturn(updatedRental);

        mockMvc.perform(put(API_BASE_URL + "/{id}", MOCK_RENTAL_ID)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDetails)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountDue").value(1500.00));
    }

    @Test
    void shouldMarkRentalAsPaid_AndReturn200() throws Exception {
        // GIVEN: Le service retourne la location marquée payée
        Rental paidRental = mockRental;
        paidRental.setStatus("PAID");

        // La méthode markAsPaid dans le contrôleur utilise currentUser.getId()
        when(rentalService.markAsPaid(eq(MOCK_RENTAL_ID), eq(MOCK_USER_ID))).thenReturn(paidRental);

        mockMvc.perform(put(API_BASE_URL + "/mark-paid/{id}", MOCK_RENTAL_ID)
                        .with(user(mockUserDetails)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldDeleteRental_AndReturn204() throws Exception {
        doNothing().when(rentalService).delete(eq(MOCK_RENTAL_ID));

        mockMvc.perform(delete(API_BASE_URL + "/{id}", MOCK_RENTAL_ID)
                        .with(user(mockUserDetails)))
                .andExpect(status().isNoContent());

        verify(rentalService, times(1)).delete(eq(MOCK_RENTAL_ID));
    }

    @Test
    void shouldGenerateReceipt_AndReturnPdf() throws Exception {
        // GIVEN:
        final String PAYMENT_METHOD = "Virement Bancaire";
        ReceiptRequestDTO receiptRequest = new ReceiptRequestDTO(MOCK_RENTAL_ID, PAYMENT_METHOD);
        byte[] mockPdfBytes = "PDF_CONTENT".getBytes();

        // 1. Définir le nom de fichier attendu dynamiquement (ex: quittance_50_2025-11-05.pdf)
        String expectedFilename = "quittance_" + MOCK_RENTAL_ID + "_" + LocalDate.now() + ".pdf";

        // 2. Simuler le succès du service
        when(receiptService.generateReceiptPdf(
                eq(MOCK_RENTAL_ID),
                eq(testUser),
                eq(PAYMENT_METHOD)))
                .thenReturn(mockPdfBytes);

        // WHEN & THEN: Appel de l'API POST pour générer le PDF
        mockMvc.perform(post(API_BASE_URL + "/receipt")
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receiptRequest)))

                // 3. VÉRIFICATIONS (Assertions)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))

                // CORRECTION: Utiliser containsString pour être plus tolérant au préfixe réel
                // ("form-data; name=...") tout en validant le nom du fichier complet.
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("filename=\"" + expectedFilename + "\"")));

        // VÉRIFICATION DE L'APPEL AU SERVICE:
        verify(receiptService, times(1)).generateReceiptPdf(
                eq(MOCK_RENTAL_ID),
                eq(testUser),
                eq(PAYMENT_METHOD));
    }

    @Test
    void shouldReturn404_WhenReceiptDataIsInvalid_OnGenerateReceipt() throws Exception {
        // GIVEN: Le service lève EntityNotFoundException ou IllegalArgumentException
        ReceiptRequestDTO receiptRequest = new ReceiptRequestDTO(MOCK_RENTAL_ID, "Virement Bancaire");

        doThrow(new EntityNotFoundException())
                .when(receiptService).generateReceiptPdf(anyLong(), eq(testUser), anyString());

        mockMvc.perform(post(API_BASE_URL + "/receipt")
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receiptRequest)))

                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetRentalsByTenantId_AndReturn200() throws Exception {
        // GIVEN: Le service retourne une liste de locations
        List<Rental> mockRentals = Collections.singletonList(mockRental);
        when(rentalService.findByTenantId(MOCK_TENANT_ID, MOCK_USER_ID)).thenReturn(mockRentals);

        mockMvc.perform(get(API_BASE_URL + "/by-tenant/{tenantId}", MOCK_TENANT_ID)
                        .with(user(mockUserDetails)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(rentalService, times(1)).findByTenantId(MOCK_TENANT_ID, MOCK_USER_ID);
    }
}