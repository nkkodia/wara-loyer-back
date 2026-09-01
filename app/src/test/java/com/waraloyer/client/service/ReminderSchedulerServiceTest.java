package com.waraloyer.client.service;

import com.waraloyer.client.model.ClientConfig;
import com.waraloyer.client.model.Property;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderSchedulerServiceTest {

    @Mock
    private RentalService rentalService;
    @Mock
    private ClientConfigService clientConfigService;
    @Mock
    private SmsLogService smsLogService;
    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private ReminderSchedulerService reminderSchedulerService;

    private User mockUser;
    private ClientConfig mockConfig;
    private Rental mockRental;
    private Tenant mockTenant;

    private final Long MOCK_USER_ID = 1L;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(MOCK_USER_ID);
        mockUser.setEmail("scheduler@test.com");

        mockConfig = new ClientConfig();
        mockConfig.setUser(mockUser);
        mockConfig.setReminderDaysBefore(5);
        mockConfig.setRelanceDaysAfter(3);

        // --- 2. Configurer les entités de location pour les mocks ---
        mockTenant = new Tenant();
        mockTenant.setPhoneNumber("+33611223344");
        mockTenant.setRentStartDate(LocalDate.of(2025, 1, 1));

        mockRental = new Rental();
        mockRental.setId(10L);
        mockRental.setTenant(mockTenant);
        mockRental.setStatus("PENDING");

        // Configuration du findAll() est ESSENTIELLE pour que la boucle puisse s'exécuter dans TOUS les tests.
        when(clientConfigService.findAll()).thenReturn(Collections.singletonList(mockConfig));

        // NOTE IMPORTANTE: La configuration spécifique de rentalService.findByUserId est DÉPLACÉE DANS LE TEST QUOTIDIEN.
    }

    // --- TESTS DE PLANIFICATION QUOTIDIENNE ---

    @Test
    void shouldSendReminderSms_WhenIs5DaysBeforeDueDateAndNotSent() {
        // GIVEN: Le loyer est dû dans 5 jours (date de rappel)
        LocalDate dueDate = LocalDate.now().plusDays(mockConfig.getReminderDaysBefore());
        mockRental.setDueDate(dueDate);
        mockRental.setReminderSent(false);

        // Configuration du RentalService (DÉPLACÉ DU SETUP GLOBAL)
        when(rentalService.findByUserId(MOCK_USER_ID)).thenReturn(Collections.singletonList(mockRental));

        // WHEN
        reminderSchedulerService.sendAutomatedRemindersAndRelances();

        // THEN: Vérifie que le SMS de RAPPEL a été envoyé
        verify(smsLogService, times(1)).sendSms(
                eq(mockUser),
                eq(mockTenant.getPhoneNumber()),
                eq("RAPPEL"),
                isNull(),
                eq(mockRental.getId())
        );
        // Vérifie que la location a été mise à jour (Marquée comme envoyée)
        verify(rentalService, times(1)).update(eq(mockRental.getId()), any(Rental.class), eq(mockUser));
    }

    @Test
    void shouldSendRelanceSms_WhenIs3DaysAfterDueDateAndNotSent() {
        // GIVEN: Le loyer est passé de 3 jours (date de relance)
        LocalDate dueDate = LocalDate.now().minusDays(mockConfig.getRelanceDaysAfter());
        mockRental.setDueDate(dueDate);
        mockRental.setRelanceSent(false);

        // Configuration du RentalService (DÉPLACÉ DU SETUP GLOBAL)
        when(rentalService.findByUserId(MOCK_USER_ID)).thenReturn(Collections.singletonList(mockRental));

        // WHEN
        reminderSchedulerService.sendAutomatedRemindersAndRelances();

        // THEN: Vérifie que le SMS de RELANCE a été envoyé
        verify(smsLogService, times(1)).sendSms(
                eq(mockUser),
                eq(mockTenant.getPhoneNumber()),
                eq("RELANCE"),
                isNull(),
                eq(mockRental.getId())
        );
        // Vérifie que la location a été mise à jour
        verify(rentalService, times(1)).update(eq(mockRental.getId()), any(Rental.class), eq(mockUser));
    }

    @Test
    void shouldNotSendSms_WhenAlreadySent() {
        // GIVEN: Le loyer est à la date de rappel, mais déjà envoyé
        mockRental.setDueDate(LocalDate.now().plusDays(mockConfig.getReminderDaysBefore()));
        mockRental.setReminderSent(true);

        // Configuration du RentalService (DÉPLACÉ DU SETUP GLOBAL)
        when(rentalService.findByUserId(MOCK_USER_ID)).thenReturn(Collections.singletonList(mockRental));

        // WHEN
        reminderSchedulerService.sendAutomatedRemindersAndRelances();

        // THEN: Aucune interaction avec le service SMS
        verify(smsLogService, never()).sendSms(any(), any(), any(), any(), any());
        verify(rentalService, never()).update(any(), any(), any());
    }

    // --- TEST DE PLANIFICATION MENSUELLE ---

    @Test
    void shouldGenerateNewRental_OnTheFirstOfTheMonth() {
        // GIVEN: Un locataire actif pour cet utilisateur
        // La configuration clientConfigService.findAll() est déjà dans setup()

        Property mockProperty = new Property();
        mockProperty.setRentAmount(BigDecimal.valueOf(1000));
        mockTenant.setProperty(mockProperty); // Assurez-vous que mockTenant a la propriété

        List<Tenant> activeTenants = Collections.singletonList(mockTenant);

        // Configuration du TenantRepository (CORRECTEMENT PLACÉ DANS CE TEST)
        when(tenantRepository.findByUserAndEnabled(mockUser, true)).thenReturn(activeTenants);

        // WHEN: Lancement de la tâche de génération
        reminderSchedulerService.generateMonthlyRentals();

        // THEN: Vérifie que la méthode de création de Rental a été appelée
        verify(rentalService, times(1)).create(any(Rental.class), eq(mockUser));

        // VÉRIFICATION SUPPLÉMENTAIRE: Vérifie les données de la nouvelle location
        verify(rentalService).create(argThat(rental ->
                rental.getAmountDue().equals(BigDecimal.valueOf(1000)) &&
                        rental.getStatus().equals("Due") &&
                        rental.getDueDate().getDayOfMonth() == mockTenant.getRentStartDate().getDayOfMonth()
        ), eq(mockUser));
    }
}