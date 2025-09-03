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
        // Vérifier si une configuration existe déjà pour cet utilisateur
        Optional<ClientConfig> existingConfig = clientConfigRepository.findByUserId(user.getId());

        // Si une configuration existe, la retourner
        if (existingConfig.isPresent()) {
            return existingConfig.get();
        } else {
            // Sinon, créer une nouvelle configuration par défaut
            ClientConfig newConfig = new ClientConfig();
            newConfig.setUser(user);
            // Initialisez les champs par défaut ici
            newConfig.setSmsReminderMessage("Bonjour, {LOCATAIRE}. Nous vous rappelons que votre loyer de {MONTANT} F CFA pour le bien situé à {ADRESSE_BIEN} est dû le {DATE_ECHEANCE}. Merci de votre paiement !");
            newConfig.setSmsRelanceMessage("Bonjour, {LOCATAIRE}. Nous vous rappelons que votre loyer de {MONTANT} F CFA pour le bien situé à {ADRESSE_BIEN} est en retard. Merci de régulariser votre situation.");
            newConfig.setOwnerEmail(user.getEmail());
            newConfig.setDefaultPaymentMethod("Contact"); // Valeur par défaut
            // ... initialisez les autres champs

            return clientConfigRepository.save(newConfig);
        }
    }

}
