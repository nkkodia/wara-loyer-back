package com.waraloyer.client.service;

import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    @Autowired
    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public List<Tenant> findByUserId(Long userId) {
        return tenantRepository.findByUserId(userId);
    }

    public Optional<Tenant> findById(Long id) {
        return tenantRepository.findById(id);
    }

    public Tenant save(Tenant tenant, User user) {
        if (tenant.getId() == null) {
            tenant.setUser(user);
        } else {
            Optional<Tenant> existingTenant = tenantRepository.findById(tenant.getId());
            if (existingTenant.isPresent() && !existingTenant.get().getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier ce locataire.");
            }
            tenant.setUser(user);
        }
        return tenantRepository.save(tenant);
    }

    public void deleteById(Long id, Long userId) {
        Optional<Tenant> tenant = tenantRepository.findById(id);
        if (tenant.isPresent() && tenant.get().getUser().getId().equals(userId)) {
            tenantRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Locataire non trouvé ou vous n'êtes pas autorisé à le supprimer.");
        }
    }
}