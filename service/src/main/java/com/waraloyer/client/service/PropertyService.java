package com.waraloyer.client.service;

import com.waraloyer.client.dto.PropertyWithRentalInfoDTO;
import com.waraloyer.client.model.Property;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.PropertyRepository;
import com.waraloyer.client.repository.RentalRepository;
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

    private final PropertyRepository propertyRepository;
    private final RentalRepository rentalRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository, RentalRepository rentalRepository) {
        this.propertyRepository = propertyRepository;
        this.rentalRepository = rentalRepository;
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


    // Modifie update pour qu'il reçoive l'utilisateur
    public Property update(Long id, Property propertyDetails, User currentUser) {
        return propertyRepository.findById(id)
                .map(property -> {
                    if (!property.getId().equals(currentUser.getId())) {
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
                    if (!property.getId().equals(currentUser.getId())) {
                        throw new AccessDeniedException("Accès refusé. Le bien n'appartient pas à cet utilisateur.");
                    }
                    propertyRepository.deleteById(id);
                });
    }
    /**
     * Vérifie si un bien appartient à un utilisateur spécifique.
     * @param propertyId L'ID de la propriété à vérifier.
     * @param userId L'ID de l'utilisateur.
     * @return true si le bien appartient à l'utilisateur, sinon false.
     */
    public boolean belongsToUser(Long propertyId, Long userId) {
        return propertyRepository.findById(propertyId)
                .map(rental -> rental.getId().equals(userId))
                .orElse(false);
    }

    // Dans PropertyService.java

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
}
