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
            newConfig.setSmsReminderMessage("Bonjour {LOCATAIRE}, votre loyer de {MONTANT} FCFA pour le bien situé {ADRESSE_BIEN} est dû le {DATE_ECHEANCE}. Merci de régler à temps.");

            // Corrige la logique pour que le message de relance par défaut inclut le placeholder
            String defaultRelanceMessage = "Rappel urgent : le loyer de {MONTANT} FCFA pour le bien {ADRESSE_BIEN} est en retard. Merci de régulariser. Pour signaler un problème, cliquez ici: {URL_PROBLEME}";
            newConfig.setSmsRelanceMessage(defaultRelanceMessage);

            newConfig.setOwnerEmail(user.getEmail());
            newConfig.setDefaultPaymentMethod("RIB");
            newConfig.setOwnerEmail(user.getEmail());
            newConfig.setReminderDaysBefore(5); // Valeur par défaut
            newConfig.setRelanceDaysAfter(5); // Valeur par défaut
            newConfig.setDefaultPaymentMethod("RIB"); // Valeur par défaut
            newConfig.setRibDetails(""); // Chaîne vide par défaut
            newConfig.setMobileMoneyLink(""); // Chaîne vide par défaut
            newConfig.setContactPersonDetails("");
            return clientConfigRepository.save(newConfig);
        }
    }

}
