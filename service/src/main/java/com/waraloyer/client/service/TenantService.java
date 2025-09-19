package com.waraloyer.client.service;

import com.waraloyer.client.dto.TenantCreateDTO;
import com.waraloyer.client.dto.TenantUpdateDTO;
import com.waraloyer.client.model.Property;
import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.PropertyRepository;
import com.waraloyer.client.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;


    @Autowired
    public TenantService(TenantRepository tenantRepository, PropertyRepository propertyRepository) {
        this.tenantRepository = tenantRepository;
        this.propertyRepository = propertyRepository;
    }

    public List<Tenant> findByUserId(Long userId) {
        return tenantRepository.findByUserId(userId);
    }

    public Optional<Tenant> findById(Long id) {
        return tenantRepository.findById(id);
    }


    public Tenant create(TenantCreateDTO tenantDto, User user) {
        Tenant tenant = new Tenant();
        tenant.setFirstName(tenantDto.getFirstName());
        tenant.setLastName(tenantDto.getLastName());
        tenant.setPhoneNumber(tenantDto.getPhoneNumber());
        tenant.setRentStartDate(tenantDto.getRentStartDate());
        tenant.setUser(user);

        if (tenantDto.getPropertyId() != null) {
            Property property = propertyRepository.findById(tenantDto.getPropertyId())
                    .orElseThrow(() -> new EntityNotFoundException("Bien non trouvé."));
            if (!property.getId().equals(user.getId())) {
                throw new AccessDeniedException("Accès refusé. Le bien n'appartient pas à cet utilisateur.");
            }
            tenant.setProperty(property);
        } else {
            tenant.setProperty(null);
        }

        return tenantRepository.save(tenant);
    }

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    public Tenant updateTenant(Long id, TenantUpdateDTO tenantDetails, User currentUser) {
        return tenantRepository.findById(id)
                .map(tenant -> {
                    if (!tenant.getUser().getId().equals(currentUser.getId())) {
                        throw new AccessDeniedException("Accès refusé. Le locataire n'appartient pas à cet utilisateur.");
                    }

                    // Mettre à jour les champs
                    tenant.setFirstName(tenantDetails.getFirstName());
                    tenant.setLastName(tenantDetails.getLastName());
                    tenant.setPhoneNumber(tenantDetails.getPhoneNumber());
                    tenant.setRentStartDate(tenantDetails.getRentStartDate());

                    // Gérer l'association du bien via l'ID
                    if (tenantDetails.getPropertyId() != null) {
                        Property property = propertyRepository.findById(tenantDetails.getPropertyId())
                                .orElseThrow(() -> new EntityNotFoundException("Bien non trouvé."));
                        if (!property.getId().equals(currentUser.getId())) {
                            throw new AccessDeniedException("Accès refusé. Le bien n'appartient pas à cet utilisateur.");
                        }
                        tenant.setProperty(property);
                    } else {
                        tenant.setProperty(null);
                    }

                    return tenantRepository.save(tenant);
                })
                .orElseThrow(() -> new EntityNotFoundException("Locataire non trouvé."));
    }

    public void delete(Long id, User currentUser) {
        tenantRepository.findById(id).ifPresent(tenant -> {
            if (!tenant.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Accès refusé. Le locataire n'appartient pas à cet utilisateur.");
            }
            tenantRepository.deleteById(id);
        });
    }
}