package com.waraloyer.client.service;

import com.waraloyer.client.model.ClientConfig; // Import nécessaire
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.PropertyRepository;
import com.waraloyer.client.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitoringService {

    private final MeterRegistry meterRegistry;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final ClientConfigService clientConfigService; // ⬅️ NOUVELLE INJECTION

    private static final int DEFAULT_MAX_PROPERTIES_LIMIT = 10;
    private static final int DEFAULT_MONTHLY_SMS_LIMIT = 15; // Basé sur Plan Essentiel

    @Autowired
    public MonitoringService(MeterRegistry meterRegistry,
                             UserRepository userRepository,
                             PropertyRepository propertyRepository,
                             ClientConfigService clientConfigService) { // ⬅️ NOUVEAU PARAMÈTRE

        this.meterRegistry = meterRegistry;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.clientConfigService = clientConfigService; // Initialisation

        registerUserLimitsGauges();
    }

    /**
     * Enregistre dynamiquement les jauges pour les limites de biens et de SMS
     * pour chaque utilisateur actif.
     */
    public void registerUserLimitsGauges() {
        List<User> users = userRepository.findAllByEnabledTrue();
        for (User user : users) {
            Long userId = user.getId();

            int maxPropertiesLimit = userRepository.findMaxPropertiesLimitByUserId(userId)
                    .orElse(DEFAULT_MAX_PROPERTIES_LIMIT);

            Gauge.builder("waraloyer.client.usage.properties", userId, id -> {
                        return (double) propertyRepository.countByUserId(id);
                    })
                    .description("Nombre actuel de biens gérés par l'utilisateur.")
                    .tag("user_id", userId.toString())
                    .tag("max_limit", String.valueOf(maxPropertiesLimit))
                    .register(meterRegistry);

            int monthlySmsLimit = userRepository.findMonthlySmsLimitByUserId(userId)
                    .orElse(DEFAULT_MONTHLY_SMS_LIMIT);

            Gauge.builder("waraloyer.client.usage.sms", userId, id -> {
                        return (double) clientConfigService.getByUserId(id)
                                .map(ClientConfig::getMessageCountThisMonth)
                                .orElse(0);
                    })
                    .description("Nombre de SMS/WhatsApp envoyés ce mois-ci par l'utilisateur.")
                    .tag("user_id", userId.toString())
                    .tag("max_limit", String.valueOf(monthlySmsLimit))
                    .register(meterRegistry);
        }
    }
}