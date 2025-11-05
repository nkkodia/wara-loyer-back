package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.dto.PropertyDTO;
import com.waraloyer.client.model.Property;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.PropertyService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PropertyControllerIntegrationTest {

    private static final String API_BASE_URL = "/api/properties";
    private final Long MOCK_PROPERTY_ID = 10L;

    private User testUser;
    private UserDetails mockUserDetails;
    private Property mockProperty;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PropertyService propertyService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setup() {
        reset(propertyService, userService);

        // 1. Configurer l'utilisateur simulé
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@owner.com");
        testUser.setUsername("test@owner.com");

        // 2. Simuler UserDetails pour MockMvc
        mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(testUser.getEmail())
                .password("dummy")
                .roles("USER")
                .build();

        // 3. Configurer l'objet Property mocké
        mockProperty = new Property();
        mockProperty.setId(MOCK_PROPERTY_ID);
        mockProperty.setName("Appartement 101");
        mockProperty.setAddress("123 Rue du Test");

        // 4. Configurer la dépendance UserService (extraction de l'utilisateur authentifié)
        when(userService.findUserByEmail(testUser.getEmail())).thenReturn(testUser);
    }

    // --- TEST 1: POST /api/properties (CREATE) ---

    @Test
    void shouldCreateProperty_AndReturn201() throws Exception {
        // GIVEN: Le service retourne la nouvelle propriété avec un ID
        Property propertyToCreate = new Property();
        propertyToCreate.setName("Nouveau Bien");
        propertyToCreate.setAddress("456 Av. Nouvelle");

        Property createdProperty = propertyToCreate;
        createdProperty.setId(11L);

        // Simuler la création réussie dans le service
        when(propertyService.create(any(Property.class), eq(testUser))).thenReturn(createdProperty);

        // WHEN & THEN
        mockMvc.perform(post(API_BASE_URL)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propertyToCreate)))

                .andExpect(status().isCreated()) // Vérifie 201 CREATED
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.name").value("Nouveau Bien"));

        verify(propertyService, times(1)).create(any(Property.class), eq(testUser));
    }

    // --- TEST 2: GET /api/properties (READ ALL) ---

    @Test
    void shouldGetAllProperties_AndReturn200() throws Exception {
        // GIVEN: Le service retourne une liste de propriétés
        Property property2 = new Property();
        property2.setId(12L);
        property2.setName("Maison");

        List<Property> mockProperties = Arrays.asList(mockProperty, property2);

        // Simuler la recherche dans le service
        when(propertyService.findByUserId(testUser.getId())).thenReturn(mockProperties);

        // WHEN & THEN
        mockMvc.perform(get(API_BASE_URL)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Appartement 101"))
                .andExpect(jsonPath("$[1].id").value(12L));

        verify(propertyService, times(1)).findByUserId(testUser.getId());
    }

    // --- TEST 3: GET /api/properties/{id} (READ BY ID) ---

    @Test
    void shouldGetPropertyById_AndReturn200() throws Exception {
        // GIVEN: Le service retourne la propriété mockée
        when(propertyService.findById(eq(MOCK_PROPERTY_ID), eq(testUser))).thenReturn(mockProperty);

        // WHEN & THEN
        mockMvc.perform(get(API_BASE_URL + "/{id}", MOCK_PROPERTY_ID)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(MOCK_PROPERTY_ID))
                .andExpect(jsonPath("$.address").value("123 Rue du Test"));

        verify(propertyService, times(1)).findById(eq(MOCK_PROPERTY_ID), eq(testUser));
    }

    @Test
    void shouldReturn404_WhenPropertyNotFound_OnGetById() throws Exception {
        // GIVEN: Le service lève EntityNotFoundException
        doThrow(new EntityNotFoundException())
                .when(propertyService).findById(anyLong(), eq(testUser));

        // WHEN & THEN
        mockMvc.perform(get(API_BASE_URL + "/{id}", 99L)
                        .with(user(mockUserDetails)))

                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403_WhenAccessDenied_OnGetById() throws Exception {
        // GIVEN: Le service lève AccessDeniedException (tentative d'accéder au bien d'un autre)
        doThrow(new AccessDeniedException("Accès refusé"))
                .when(propertyService).findById(anyLong(), eq(testUser));

        // WHEN & THEN
        mockMvc.perform(get(API_BASE_URL + "/{id}", MOCK_PROPERTY_ID)
                        .with(user(mockUserDetails)))

                .andExpect(status().isForbidden());
    }

    // --- TEST 4: PUT /api/properties/{id} (UPDATE) ---

    @Test
    void shouldUpdateProperty_AndReturn200() throws Exception {
        // GIVEN: Détails de la mise à jour
        Property updateDetails = new Property();
        updateDetails.setAddress("456 Rue Mise à Jour");

        // Simuler la mise à jour dans le service
        Property updatedProperty = mockProperty;
        updatedProperty.setAddress("456 Rue Mise à Jour");

        when(propertyService.update(eq(MOCK_PROPERTY_ID), any(Property.class), eq(testUser)))
                .thenReturn(updatedProperty);

        // WHEN & THEN
        mockMvc.perform(put(API_BASE_URL + "/{id}", MOCK_PROPERTY_ID)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDetails)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("456 Rue Mise à Jour"));

        verify(propertyService, times(1)).update(eq(MOCK_PROPERTY_ID), any(Property.class), eq(testUser));
    }

    // --- TEST 5: DELETE /api/properties/{id} (DELETE) ---

    @Test
    void shouldDeleteProperty_AndReturn204() throws Exception {
        // GIVEN: Le service ne lève aucune exception (succès)
        doNothing().when(propertyService).delete(eq(MOCK_PROPERTY_ID), eq(testUser));

        // WHEN & THEN
        mockMvc.perform(delete(API_BASE_URL + "/{id}", MOCK_PROPERTY_ID)
                        .with(user(mockUserDetails)))

                .andExpect(status().isNoContent()); // 204 NO CONTENT

        verify(propertyService, times(1)).delete(eq(MOCK_PROPERTY_ID), eq(testUser));
    }

    @Test
    void shouldReturn404_WhenPropertyNotFound_OnDelete() throws Exception {
        // GIVEN: Le service lève EntityNotFoundException
        doThrow(new EntityNotFoundException())
                .when(propertyService).delete(anyLong(), eq(testUser));

        // WHEN & THEN
        mockMvc.perform(delete(API_BASE_URL + "/{id}", 99L)
                        .with(user(mockUserDetails)))

                .andExpect(status().isNotFound());
    }
}