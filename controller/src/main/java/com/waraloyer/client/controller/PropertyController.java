package com.waraloyer.client.controller;

import com.waraloyer.client.model.Property;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.PropertyService;
import com.waraloyer.client.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final UserService userService;

    @Autowired
    public PropertyController(PropertyService propertyService, UserService userService) {
        this.propertyService = propertyService;
        this.userService = userService;
    }

    // CREATE - Créer un nouveau bien
    @PostMapping
    public ResponseEntity<Property> createProperty(@RequestBody Property property, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        Property newProperty = propertyService.create(property, currentUser);
        return new ResponseEntity<>(newProperty, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<Property>> getAllProperties(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        List<Property> properties = propertyService.findByUserId(currentUser.getId());
        return new ResponseEntity<>(properties, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getPropertyById(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        try {
            Property property = propertyService.findById(id, currentUser);
            return new ResponseEntity<>(property, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Accès refusé
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Bien non trouvé
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Property> updateProperty(@PathVariable Long id, @RequestBody Property propertyDetails, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        Property updatedProperty = propertyService.update(id, propertyDetails, currentUser);
        return new ResponseEntity<>(updatedProperty, HttpStatus.OK);
    }

    // DELETE - Supprimer un bien par son ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        // Le service doit vérifier si le bien appartient bien à l'utilisateur
        propertyService.delete(id, currentUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
