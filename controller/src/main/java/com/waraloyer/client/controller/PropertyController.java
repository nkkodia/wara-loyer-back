package com.waraloyer.client.controller;

import com.waraloyer.client.dto.PropertyDTO;
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
import java.util.stream.Collectors;

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

    // CREATE - Créer un nouveau bien (retourne un DTO)
    @PostMapping
    public ResponseEntity<PropertyDTO> createProperty(@RequestBody Property property, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        Property newProperty = propertyService.create(property, currentUser);
        return new ResponseEntity<>(new PropertyDTO(newProperty), HttpStatus.CREATED);
    }

    // READ - Lister tous les biens de l'utilisateur (retourne une liste de DTO)
    @GetMapping
    public ResponseEntity<List<PropertyDTO>> getAllProperties(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        List<PropertyDTO> propertyDTOs = propertyService.findByUserId(currentUser.getId()).stream()
                .map(PropertyDTO::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(propertyDTOs, HttpStatus.OK);
    }

    // READ - Obtenir un bien par son ID (retourne un DTO)
    @GetMapping("/{id}")
    public ResponseEntity<PropertyDTO> getPropertyById(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        try {
            Property property = propertyService.findById(id, currentUser);
            return new ResponseEntity<>(new PropertyDTO(property), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // UPDATE - Mettre à jour un bien existant (retourne un DTO)
    @PutMapping("/{id}")
    public ResponseEntity<PropertyDTO> updateProperty(@PathVariable Long id, @RequestBody Property propertyDetails, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        try {
            Property updatedProperty = propertyService.update(id, propertyDetails, currentUser);
            return new ResponseEntity<>(new PropertyDTO(updatedProperty), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // DELETE - Supprimer un bien par son ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        try {
            propertyService.delete(id, currentUser);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
