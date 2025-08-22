package com.waraloyer.client.controller;

import com.waraloyer.client.model.Property;
import com.waraloyer.client.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    @Autowired
    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    // CREATE - Créer un nouveau bien
    @PostMapping
    public ResponseEntity<Property> createProperty(@RequestBody Property property) {
        Property newProperty = propertyService.create(property);
        return new ResponseEntity<>(newProperty, HttpStatus.CREATED);
    }

    // READ - Lister tous les biens
    @GetMapping
    public ResponseEntity<List<Property>> getAllProperties() {
        List<Property> properties = propertyService.findAll();
        return new ResponseEntity<>(properties, HttpStatus.OK);
    }

    // READ - Obtenir un bien par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Property> getPropertyById(@PathVariable String id) {
        return propertyService.findById(id)
                .map(property -> new ResponseEntity<>(property, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // UPDATE - Mettre à jour un bien existant
    @PutMapping("/{id}")
    public ResponseEntity<Property> updateProperty(@PathVariable String id, @RequestBody Property propertyDetails) {
        Property updatedProperty = propertyService.update(id, propertyDetails);
        return new ResponseEntity<>(updatedProperty, HttpStatus.OK);
    }

    // DELETE - Supprimer un bien par son ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        propertyService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
