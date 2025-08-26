package com.waraloyer.client.service;

import com.waraloyer.client.model.ClientConfig;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.ClientConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
