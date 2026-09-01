package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.dto.TenantCreateDTO;
import com.waraloyer.client.dto.TenantUpdateDTO;
import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.TenantService;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TenantControllerIntegrationTest {

    private static final String API_BASE_URL = "/api/tenants";
    private final Long MOCK_TENANT_ID = 42L;

    private User testUser;
    private UserDetails mockUserDetails;
    private Tenant mockTenant;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setup() {
        reset(tenantService, userService);

        // 1. Configurer l'utilisateur simulé
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("tenant@owner.com");
        testUser.setUsername("tenant@owner.com");

        // 2. Simuler UserDetails pour MockMvc
        mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(testUser.getEmail())
                .password("dummy")
                .roles("USER")
                .build();

        // 3. Configurer l'objet Tenant mocké
        mockTenant = new Tenant();
        mockTenant.setId(MOCK_TENANT_ID);
        mockTenant.setFirstName("Jean");
        mockTenant.setLastName("Dupont");

        // 4. Configuration de base du UserService (Extraction de l'utilisateur)
        when(userService.findUserByEmail(testUser.getEmail())).thenReturn(testUser);
    }

    @Test
    void shouldCreateTenant_AndReturn201() throws Exception {
        // GIVEN: DTO de création
        TenantCreateDTO createDTO = new TenantCreateDTO();
        createDTO.setFirstName("Alice");
        createDTO.setLastName("Smith");

        Tenant createdTenant = new Tenant();
        createdTenant.setId(43L);
        createdTenant.setFirstName("Alice");

        // Simuler la création réussie
        when(tenantService.create(any(TenantCreateDTO.class), eq(testUser))).thenReturn(createdTenant);

        // WHEN & THEN
        mockMvc.perform(post(API_BASE_URL)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(43L))
                .andExpect(jsonPath("$.firstName").value("Alice"));

        verify(tenantService, times(1)).create(any(TenantCreateDTO.class), eq(testUser));
    }

    @Test
    void shouldGetMyTenants_AndReturn200() throws Exception {
        // GIVEN: Le service retourne une liste de locataires
        List<Tenant> mockTenants = Collections.singletonList(mockTenant);
        when(tenantService.findByUserId(testUser.getId())).thenReturn(mockTenants);

        // WHEN & THEN
        mockMvc.perform(get(API_BASE_URL + "/my-tenants")
                        .with(user(mockUserDetails)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lastName").value("Dupont"));

        verify(tenantService, times(1)).findByUserId(testUser.getId());
    }

    @Test
    void shouldGetTenantById_AndReturn200() throws Exception {
        // GIVEN: Le service trouve le locataire
        when(tenantService.findById(MOCK_TENANT_ID)).thenReturn(Optional.of(mockTenant));

        // WHEN & THEN
        mockMvc.perform(get(API_BASE_URL + "/{id}", MOCK_TENANT_ID)
                        .with(user(mockUserDetails)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(MOCK_TENANT_ID));

        verify(tenantService, times(1)).findById(MOCK_TENANT_ID);
    }

    @Test
    void shouldReturn404_WhenTenantNotFound_OnGetById() throws Exception {
        // GIVEN: Le service ne trouve pas le locataire
        when(tenantService.findById(anyLong())).thenReturn(Optional.empty());

        // WHEN & THEN
        mockMvc.perform(get(API_BASE_URL + "/{id}", 99L)
                        .with(user(mockUserDetails)))

                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateTenant_AndReturn200() throws Exception {
        // GIVEN: DTO de mise à jour
        TenantUpdateDTO updateDTO = new TenantUpdateDTO();
        updateDTO.setFirstName("John");

        Tenant updatedTenant = mockTenant;
        updatedTenant.setFirstName("John");

        // Simuler la mise à jour réussie
        when(tenantService.updateTenant(eq(MOCK_TENANT_ID), any(TenantUpdateDTO.class), eq(testUser))).thenReturn(updatedTenant);

        // WHEN & THEN
        mockMvc.perform(put(API_BASE_URL + "/{id}", MOCK_TENANT_ID)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(tenantService, times(1)).updateTenant(eq(MOCK_TENANT_ID), any(TenantUpdateDTO.class), eq(testUser));
    }

    @Test
    void shouldDeleteTenant_AndReturn204() throws Exception {
        // GIVEN: Le service ne lève aucune exception (succès)
        doNothing().when(tenantService).delete(eq(MOCK_TENANT_ID), eq(testUser));

        // WHEN & THEN
        mockMvc.perform(delete(API_BASE_URL + "/{id}", MOCK_TENANT_ID)
                        .with(user(mockUserDetails)))

                .andExpect(status().isNoContent()); // 204 NO CONTENT

        verify(tenantService, times(1)).delete(eq(MOCK_TENANT_ID), eq(testUser));
    }

    @Test
    void shouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        // WHEN: Tentative d'accéder à la création sans être connecté
        TenantCreateDTO createDTO = new TenantCreateDTO();
        createDTO.setFirstName("Anonymous");

        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))

                // THEN: La sécurité devrait renvoyer 403
                .andExpect(status().isForbidden());

        verify(tenantService, never()).create(any(), any());
    }
}