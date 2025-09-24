package com.waraloyer.client.service;

import com.waraloyer.client.dto.TenantCreateDTO;
import com.waraloyer.client.dto.TenantUpdateDTO;
import com.waraloyer.client.model.Property;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.PropertyRepository;
import com.waraloyer.client.repository.RentalRepository;
import com.waraloyer.client.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;
    private final RentalService rentalService;


    @Autowired
    public TenantService(TenantRepository tenantRepository, PropertyRepository propertyRepository, RentalService rentalService) {
        this.tenantRepository = tenantRepository;
        this.propertyRepository = propertyRepository;
        this.rentalService = rentalService;
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
            if (property.getUserId() == null || !property.getUserId().equals(user.getId())) {
                throw new AccessDeniedException("Accès refusé. Le bien n'appartient pas à cet utilisateur.");
            }
            tenant.setProperty(property);
        } else {
            tenant.setProperty(null);
        }

        Tenant savedTenant = tenantRepository.save(tenant);

        Rental initialRental = new Rental();
        initialRental.setDueDate(savedTenant.getRentStartDate());
        initialRental.setAmountDue(savedTenant.getProperty().getRentAmount());
        initialRental.setStatus("Due");
        initialRental.setTenant(savedTenant);
        initialRental.setProperty(savedTenant.getProperty());
        initialRental.setUser(user);

        rentalService.create(initialRental, user);
        return savedTenant;
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
                    Long oldPropertyId = tenant.getProperty() != null ? tenant.getProperty().getId() : null;

                    tenant.setFirstName(tenantDetails.getFirstName());
                    tenant.setLastName(tenantDetails.getLastName());
                    tenant.setPhoneNumber(tenantDetails.getPhoneNumber());
                    tenant.setRentStartDate(tenantDetails.getRentStartDate());

                    // Gérer l'association du bien
                    if (tenantDetails.getPropertyId() != null) {
                        Property property = propertyRepository.findById(tenantDetails.getPropertyId())
                                .orElseThrow(() -> new EntityNotFoundException("Bien non trouvé."));

                        if (property.getUserId() == null || !property.getUserId().equals(currentUser.getId())) {
                            throw new AccessDeniedException("Accès refusé. Le bien n'appartient pas à cet utilisateur.");
                        }
                        tenant.setProperty(property);
                    } else {
                        tenant.setProperty(null);
                    }
                    Tenant updatedTenant = tenantRepository.save(tenant);

                    if (updatedTenant.getProperty() != null && !updatedTenant.getProperty().getId().equals(oldPropertyId)) {
                        Rental initialRental = new Rental();
                        LocalDate rentStartDate = updatedTenant.getRentStartDate();
                        int paymentDay = updatedTenant.getProperty().getRentPaymentDate();

                        LocalDate firstDueDate;
                        if (rentStartDate.getDayOfMonth() > paymentDay) {
                            // Si la date de début de location est après le jour de paiement, le loyer est dû le mois suivant
                            firstDueDate = LocalDate.of(rentStartDate.getYear(), rentStartDate.getMonth(), paymentDay).plusMonths(1);
                        } else {
                            // Sinon, le loyer est dû le mois en cours
                            firstDueDate = LocalDate.of(rentStartDate.getYear(), rentStartDate.getMonth(), paymentDay);
                        }
                        initialRental.setDueDate(firstDueDate);
                        initialRental.setAmountDue(updatedTenant.getProperty().getRentAmount());
                        initialRental.setStatus("Due");
                        initialRental.setTenant(updatedTenant);
                        initialRental.setProperty(updatedTenant.getProperty());
                        initialRental.setUser(currentUser);

                        rentalService.create(initialRental, currentUser);
                    }
                    return updatedTenant;
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