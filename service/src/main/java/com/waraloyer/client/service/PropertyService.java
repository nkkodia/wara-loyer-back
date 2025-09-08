package com.waraloyer.client.service;

import com.waraloyer.client.model.Property;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.PropertyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    /**
     * Crée un nouveau bien en l'associant à l'utilisateur actuel.
     * @param property L'objet Property à créer.
     * @param user L'utilisateur actuellement authentifié.
     * @return L'objet Property créé.
     */
    public Property create(Property property, User user) {
        property.setUser(user);
        return propertyRepository.save(property);
    }

    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    public Property findById(Long id, User currentUser) {
        return propertyRepository.findById(id)
                .map(property -> {
                    if (!property.getUser().getId().equals(currentUser.getId())) {
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
                    if (!property.getUser().getId().equals(currentUser.getId())) {
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
                    if (!property.getUser().getId().equals(currentUser.getId())) {
                        throw new AccessDeniedException("Accès refusé. Le bien n'appartient pas à cet utilisateur.");
                    }
                    propertyRepository.deleteById(id);
                });
    }
}
