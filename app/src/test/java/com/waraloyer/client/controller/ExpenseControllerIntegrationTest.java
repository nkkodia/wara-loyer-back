package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.dto.ExpenseDTO;
import com.waraloyer.client.model.Expense;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.ExpenseService;
import com.waraloyer.client.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseControllerIntegrationTest {

    private static final String API_URL = "/api/expenses/rentals/{rentalId}";
    private final Long MOCK_RENTAL_ID = 99L;
    private final String MOCK_MONTH = "2025-01";

    private User testUser;
    private UserDetails mockUserDetails;
    private ExpenseDTO mockExpenseDTO;
    private Expense mockExpense;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setup() {
        reset(expenseService, userService);

        // 1. Configurer l'utilisateur simulé
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");

        // 2. Simuler UserDetails pour l'objet 'user()' de MockMvc
        mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(testUser.getEmail())
                .password("dummy")
                .roles("USER")
                .build();

        // 3. Configuration de base : Le contrôleur doit toujours retrouver l'utilisateur
        when(userService.findUserByEmail(testUser.getEmail())).thenReturn(testUser);

        // 4. Configuration des DTO/Entités
        mockExpenseDTO = new ExpenseDTO(); // Appel du constructeur sans argument (via @NoArgsConstructor)
        mockExpenseDTO.setId(100L);
        mockExpenseDTO.setAmount(BigDecimal.valueOf(50.00));
        mockExpenseDTO.setDescription("Réparation fuite");
        mockExpenseDTO.setDate(LocalDate.now());

        mockExpense = new Expense();
        mockExpense.setId(100L);
        mockExpense.setAmount(BigDecimal.valueOf(50.00));
    }

    // --- TEST 1: GET /rentals/{rentalId} (Obtenir les dépenses) ---

    @Test
    void shouldReturnExpenses_WhenAccessGranted() throws Exception {
        // GIVEN: Le service retourne une liste de dépenses simulées
        List<ExpenseDTO> expectedExpenses = Collections.singletonList(mockExpenseDTO);

        // Simuler la réussite de l'appel au service métier
        when(expenseService.findByRentalIdAndDateBetween(
                eq(MOCK_RENTAL_ID),
                any(LocalDate.class),
                any(LocalDate.class),
                eq(testUser)))
                .thenReturn(expectedExpenses);

        // WHEN & THEN
        mockMvc.perform(get(API_URL, MOCK_RENTAL_ID)
                        .param("month", MOCK_MONTH)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(50.00));

        // VÉRIFICATION: Le service a été appelé avec les bons paramètres utilisateur/location/date
        verify(expenseService, times(1)).findByRentalIdAndDateBetween(
                eq(MOCK_RENTAL_ID),
                any(LocalDate.class),
                any(LocalDate.class),
                eq(testUser));
    }

    @Test
    void shouldReturn404_WhenRentalNotFound_OnGet() throws Exception {
        // GIVEN: Le service lève EntityNotFoundException
        doThrow(new EntityNotFoundException())
                .when(expenseService).findByRentalIdAndDateBetween(
                        eq(MOCK_RENTAL_ID),
                        any(LocalDate.class),
                        any(LocalDate.class),
                        eq(testUser));

        // WHEN & THEN
        mockMvc.perform(get(API_URL, MOCK_RENTAL_ID)
                        .param("month", MOCK_MONTH)
                        .with(user(mockUserDetails)))

                .andExpect(status().isNotFound());

        verify(expenseService, times(1)).findByRentalIdAndDateBetween(
                eq(MOCK_RENTAL_ID),
                any(LocalDate.class),
                any(LocalDate.class),
                eq(testUser));
    }

    @Test
    void shouldReturn403_WhenAccessDenied_OnGet() throws Exception {
        // GIVEN: Le service lève AccessDeniedException (l'utilisateur n'est pas le propriétaire)
        doThrow(new AccessDeniedException("Accès refusé."))
                .when(expenseService).findByRentalIdAndDateBetween(
                        eq(MOCK_RENTAL_ID),
                        any(LocalDate.class),
                        any(LocalDate.class),
                        eq(testUser));

        // WHEN & THEN
        mockMvc.perform(get(API_URL, MOCK_RENTAL_ID)
                        .param("month", MOCK_MONTH)
                        .with(user(mockUserDetails)))

                .andExpect(status().isForbidden());
    }

    // --- TEST 2: POST /rentals/{rentalId} (Ajouter une dépense) ---

    @Test
    void shouldAddExpense_WhenValidDataAndAccessGranted() throws Exception {
        // GIVEN: DTO de dépense valide
        ExpenseDTO newExpenseDto = new ExpenseDTO(); // <-- Constructeur sans argument
        newExpenseDto.setAmount(BigDecimal.TEN);
        newExpenseDto.setDescription("Climatisation");
        newExpenseDto.setDate(LocalDate.now());

        // Simuler la réussite de l'appel au service métier
        when(expenseService.addExpense(
                eq(MOCK_RENTAL_ID),
                eq(BigDecimal.TEN),
                eq("Climatisation"),
                eq(testUser)))
                .thenReturn(mockExpense);

        // WHEN & THEN
        mockMvc.perform(post(API_URL, MOCK_RENTAL_ID)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newExpenseDto)))

                .andExpect(status().isCreated()) // Vérifie le statut 201 CREATED
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.amount").value(50.00));

        // VÉRIFICATION: Le service a été appelé avec les bons paramètres
        verify(expenseService, times(1)).addExpense(
                eq(MOCK_RENTAL_ID),
                eq(BigDecimal.TEN),
                eq("Climatisation"),
                eq(testUser));
    }

    @Test
    void shouldReturn404_WhenRentalNotFound_OnPost() throws Exception {
        // GIVEN: DTO de dépense et le service lève EntityNotFoundException
        ExpenseDTO newExpenseDto = new ExpenseDTO();
        newExpenseDto.setAmount(BigDecimal.TEN);
        newExpenseDto.setDescription("Réparation");
        newExpenseDto.setDate(LocalDate.now());

        doThrow(new EntityNotFoundException())
                .when(expenseService).addExpense(
                        eq(MOCK_RENTAL_ID),
                        eq(BigDecimal.TEN),
                        eq("Réparation"),
                        eq(testUser));

        // WHEN & THEN
        mockMvc.perform(post(API_URL, MOCK_RENTAL_ID)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newExpenseDto)))

                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403_WhenAccessDenied_OnPost() throws Exception {
        ExpenseDTO newExpenseDto = new ExpenseDTO();
        newExpenseDto.setAmount(BigDecimal.TEN);
        newExpenseDto.setDescription("Climatisation");
        newExpenseDto.setDate(LocalDate.now());

        doThrow(new AccessDeniedException("Accès refusé."))
                .when(expenseService).addExpense(
                        eq(MOCK_RENTAL_ID),
                        eq(BigDecimal.TEN),
                        eq("Climatisation"),
                        eq(testUser));

        // WHEN & THEN
        mockMvc.perform(post(API_URL, MOCK_RENTAL_ID)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newExpenseDto)))

                .andExpect(status().isForbidden());
    }
}