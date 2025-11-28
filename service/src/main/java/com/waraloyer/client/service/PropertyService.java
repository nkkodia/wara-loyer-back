package com.waraloyer.client.service;

import com.waraloyer.client.dto.PropertyWithRentalInfoDTO;
import com.waraloyer.client.model.Property;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.PropertyRepository;
import com.waraloyer.client.repository.RentalRepository;
import com.waraloyer.client.repository.SubscriptionRepository;
import com.waraloyer.client.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    private static final int DEFAULT_MAX_LIMIT = 10;

    private final PropertyRepository propertyRepository;
    private final RentalRepository rentalRepository;
    private final MeterRegistry meterRegistry;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository, RentalRepository rentalRepository, MeterRegistry meterRegistry, UserRepository userRepository, SubscriptionRepository subscriptionRepository) {
        this.propertyRepository = propertyRepository;
        this.rentalRepository = rentalRepository;
        this.meterRegistry = meterRegistry;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        registerUserLimitsGauges(meterRegistry);
    }

    /**
     * Crée un nouveau bien en l'associant à l'utilisateur actuel.
     * @param property L'objet Property à créer.
     * @param user L'utilisateur actuellement authentifié.
     * @return L'objet Property créé.
     */
    // Dans create()
    public Property create(Property property, User user) {
        property.setUserId(user.getId()); // Nouvelle ligne
        return propertyRepository.save(property);
    }

    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    // Dans findById()
    public Property findById(Long id, User currentUser) {
        return propertyRepository.findById(id)
                .map(property -> {
                    if (property.getUserId() == null || !property.getUserId().equals(currentUser.getId())) {
                        throw new AccessDeniedException("Accès refusé. Ce bien n'appartient pas à cet utilisateur.");
                    }
                    return property;
                })
                .orElseThrow(() -> new EntityNotFoundException("Bien non trouvé avec l'ID: " + id));
    }


    public List<Property> findByUserId(Long userId) {
        return propertyRepository.findByUserId(userId);
    }


    public Property update(Long id, Property propertyDetails, User currentUser) {
        return propertyRepository.findById(id)
                .map(property -> {
                    // CORRECTION CLÉ : On compare l'ID du propriétaire de la propriété
                    // à l'ID de l'utilisateur courant.
                    if (!property.getUserId().equals(currentUser.getId())) {
                        throw new AccessDeniedException("Accès refusé. Le bien n'appartient pas à cet utilisateur.");
                    }

                    // Mettre à jour tous les champs ici
                    property.setName(propertyDetails.getName());
                    property.setAddress(propertyDetails.getAddress());
                    property.setType(propertyDetails.getType());
                    property.setRentAmount(propertyDetails.getRentAmount());
                    property.setChargesAmount(propertyDetails.getChargesAmount());
                    property.setDescription(propertyDetails.getDescription());
                    property.setRentPaymentDate(propertyDetails.getRentPaymentDate());

                    return propertyRepository.save(property);
                })
                .orElseThrow(() -> new EntityNotFoundException("Bien non trouvé."));
    }
    public void delete(Long id, User currentUser) {
        propertyRepository.findById(id)
                .ifPresent(property -> {
                    if (!property.getUserId().equals(currentUser.getId())) {
                        throw new AccessDeniedException("Accès refusé. Le bien n'appartient pas à cet utilisateur.");
                    }
                    propertyRepository.deleteById(id);
                });
    }

    public boolean belongsToUser(Long propertyId, Long userId) {
        return propertyRepository.findById(propertyId)
                .map(property -> property.getUserId() != null && property.getUserId().equals(userId))
                .orElse(false);
    }
    // Dans PropertyService.java
    public List<PropertyWithRentalInfoDTO> findAllWithRentalInfo(User user) {
        List<Property> properties = propertyRepository.findByUserId(user.getId());
        List<PropertyWithRentalInfoDTO> dtoList = new ArrayList<>();

        for (Property property : properties) {
            Optional<Rental> latestRental = rentalRepository.findTopByPropertyIdOrderByDueDateDesc(property.getId());

            PropertyWithRentalInfoDTO dto = new PropertyWithRentalInfoDTO();
            BeanUtils.copyProperties(property, dto);

            if (latestRental.isPresent()) {
                Rental rental = latestRental.get();
                dto.setLastRentAmount(rental.getAmountDue());
                dto.setLastPaymentDate(rental.getPaymentDate() != null ? rental.getPaymentDate() : rental.getDueDate());
            }

            dtoList.add(dto);
        }
        return dtoList;
    }
    public void registerUserLimitsGauges(MeterRegistry meterRegistry) {
        List<User> users = userRepository.findAllByEnabledTrue();
        for (User user : users) {
            Long userId = user.getId();

            int maxLimit = userRepository.findMaxPropertiesLimitByUserId(userId)
                    .orElse(DEFAULT_MAX_LIMIT);
            Gauge.builder("waraloyer.client.usage.properties", userId, id -> {
                        return (double) propertyRepository.countByUserId(id);
                    })
                    .description("Nombre actuel de biens gérés par l'utilisateur.")
                    .tag("user_id", userId.toString())
                    .tag("max_limit", String.valueOf(maxLimit))
                    .register(this.meterRegistry);
        }
    }
}
