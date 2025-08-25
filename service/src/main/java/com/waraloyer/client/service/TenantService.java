package com.waraloyer.client.service;

import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    /**
     * Crée un nouveau locataire l'associant à l'utilisateur actuel.
     * @param tenant L'objet Property à créer.
     * @param user L'utilisateur actuellement authentifié.
     * @return L'objet Tenant créé.
     */
    public Tenant create(Tenant tenant, User user) {
        tenant.setUser(user);
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

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    public Tenant update(Long id, Tenant tenantDetails) {
        Tenant existingTenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bien non trouvé avec l'ID " + id));

        // Mettez à jour les champs de l'objet existant
        existingTenant.setFirstName(tenantDetails.getFirstName());
        existingTenant.setLastName(tenantDetails.getLastName());
        existingTenant.setEmail(tenantDetails.getEmail());
        existingTenant.setPhoneNumber(tenantDetails.getPhoneNumber());
        existingTenant.setRentStartDate(tenantDetails.getRentStartDate());

        return tenantRepository.save(existingTenant);
    }

    public void delete(Long id) {
        tenantRepository.deleteById(id);
    }
}