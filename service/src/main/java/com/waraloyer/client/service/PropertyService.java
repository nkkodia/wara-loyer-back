package com.waraloyer.client.service;

import com.waraloyer.client.model.Property;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<Property> findByUserId(Long userId) {
        return propertyRepository.findByUserId(userId);
    }

    public Optional<Property> findById(String id) {
        return propertyRepository.findById(id);
    }

    public Property save(Property property, User user) {
        if (property.getId() == null || property.getId().isEmpty()) {
            property.setId(UUID.randomUUID().toString());
            property.setUser(user);
        } else {
            Optional<Property> existingProperty = propertyRepository.findById(property.getId());
            if (existingProperty.isPresent() && !existingProperty.get().getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier ce bien.");
            }
            property.setUser(user);
        }
        return propertyRepository.save(property);
    }

    public void deleteById(String id, Long userId) {
        Optional<Property> property = propertyRepository.findById(id);
        if (property.isPresent() && property.get().getUser().getId().equals(userId)) {
            propertyRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Bien non trouvé ou vous n'êtes pas autorisé à le supprimer.");
        }
    }
}