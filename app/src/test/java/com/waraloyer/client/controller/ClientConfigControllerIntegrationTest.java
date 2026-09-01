package com.waraloyer.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waraloyer.client.model.ClientConfig;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.ClientConfigService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientConfigControllerIntegrationTest {

    private static final String API_URL = "/api/config";
    private User testUser;
    private ClientConfig mockConfig;
    private UserDetails mockUserDetails; // Utilisé pour simuler l'authentification

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Services mockés pour isoler le contrôleur
    @MockBean
    private ClientConfigService configService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setup() {
        reset(configService, userService);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@owner.com");
        testUser.setUsername("test@owner.com");
        testUser.setFirstName("Test");
        testUser.setLastName("Owner");

        mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername(testUser.getEmail())
                .password("dummy")
                .roles("USER")
                .build();

        when(userService.findUserByEmail(testUser.getEmail())).thenReturn(testUser);

        mockConfig = new ClientConfig();
        mockConfig.setId(10L);
        mockConfig.setSmsRelanceMessage("Merci de payer.");
        mockConfig.setReminderDaysBefore(15);
    }



    @Test
    void shouldSaveConfig_WhenAuthenticated() throws Exception {
        ClientConfig newConfig = new ClientConfig();
        newConfig.setSmsRelanceMessage("Votre loyer est dû.");

        when(configService.save(any(ClientConfig.class), eq(testUser))).thenAnswer(invocation -> {
            ClientConfig saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        // WHEN & THEN
        mockMvc.perform(post(API_URL)
                        // Simule l'utilisateur connecté
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newConfig)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11L))
                // Vérifie que le contrôleur a bien ajouté le suffixe avant de sauvegarder
                .andExpect(jsonPath("$.smsRelanceMessage").value("Votre loyer est dû. Pour signaler un problème, cliquez sur ce lien : {URL_PROBLEME}"));

        // VÉRIFICATION: Vérifie que le service a été appelé
        verify(configService, times(1)).save(any(ClientConfig.class), eq(testUser));
    }

    @Test
    void shouldGetConfig_WhenAuthenticated() throws Exception {
        // Configurer le service pour retourner la config simulée
        when(configService.getOrCreate(testUser)).thenReturn(mockConfig);

        // WHEN & THEN
        mockMvc.perform(get(API_URL)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockConfig.getId()))
                .andExpect(jsonPath("$.reminderDaysBefore").value(mockConfig.getReminderDaysBefore()));

        verify(configService, times(1)).getOrCreate(testUser);
    }

    @Test
    void shouldUpdateConfig_WhenAuthenticated() throws Exception {
        ClientConfig updatedConfig = mockConfig;
        updatedConfig.setReminderDaysBefore(20);

        when(configService.save(any(ClientConfig.class), eq(testUser))).thenReturn(updatedConfig);

        // WHEN & THEN
        mockMvc.perform(put(API_URL)
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedConfig)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reminderDaysBefore").value(20));
        verify(configService, times(1)).save(any(ClientConfig.class), eq(testUser));
    }

    @Test
    void shouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get(API_URL)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isForbidden());

        verify(userService, never()).findUserByEmail(anyString());
    }
}