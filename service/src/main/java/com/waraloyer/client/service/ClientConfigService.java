package com.waraloyer.client.service;

import com.waraloyer.client.model.ClientConfig;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.ClientConfigRepository;
import io.micrometer.observation.ObservationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientConfigService {

    private final ClientConfigRepository clientConfigRepository;

    @Autowired
    public ClientConfigService(ClientConfigRepository clientConfigRepository) {
        this.clientConfigRepository = clientConfigRepository;
    }

    public Optional<ClientConfig> getByUserId(Long userId) {
        return clientConfigRepository.findByUserId(userId);
    }

    public ClientConfig save(ClientConfig config, User user) {
        config.setUser(user);
        return clientConfigRepository.save(config);
    }
    public List<ClientConfig> findAll() {
        return clientConfigRepository.findAll();
    }

    public ClientConfig getOrCreate(User user) {
        Optional<ClientConfig> existingConfig = clientConfigRepository.findByUserId(user.getId());

        if (existingConfig.isPresent()) {
            return existingConfig.get();
        } else {
            ClientConfig newConfig = new ClientConfig();
            newConfig.setUser(user);
            newConfig.setOwnerEmail(user.getEmail());
            newConfig.setReminderDaysBefore(5);
            newConfig.setRelanceDaysAfter(5);
            newConfig.setDefaultPaymentMethod("RIB");
            newConfig.setRibDetails("");
            newConfig.setMobileMoneyLink("");
            newConfig.setContactPersonDetails("");

            newConfig.setSmsReminderMessage("Bonjour {{1}}, nous vous rappelons que le paiement de votre loyer pour le bien situé au {{2}} est dû avant le {{3}}.");
            newConfig.setSmsRelanceMessage("Bonjour {{1}}, nous vous rappelons que le paiement de votre loyer pour le bien situé au {{2}} est en retard. Merci de régulariser la situation dès que possible.");

            return clientConfigRepository.save(newConfig);
        }
    }

}
