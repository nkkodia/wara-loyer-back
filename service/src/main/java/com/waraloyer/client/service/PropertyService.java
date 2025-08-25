package com.waraloyer.client.service;

import com.waraloyer.client.model.Property;
import com.waraloyer.client.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Property create(Property property) {
        return propertyRepository.save(property);
    }

    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    public Optional<Property> findById(Long id) {
        return propertyRepository.findById(id);
    }

    public Property update(Long id, Property propertyDetails) {
        Property existingProperty = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bien non trouvé avec l'ID " + id));

        // Mettez à jour les champs de l'objet existant avec les détails de la requête
        existingProperty.setName(propertyDetails.getName());
        existingProperty.setAddress(propertyDetails.getAddress());
        existingProperty.setType(propertyDetails.getType());
        existingProperty.setRentAmount(propertyDetails.getRentAmount());
        existingProperty.setChargesAmount(propertyDetails.getChargesAmount());
        existingProperty.setDescription(propertyDetails.getDescription());
        existingProperty.setRentPaymentDate(propertyDetails.getRentPaymentDate());

        return propertyRepository.save(existingProperty);
    }

    public void delete(Long id) {
        propertyRepository.deleteById(id);
    }
}
