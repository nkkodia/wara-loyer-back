package com.waraloyer.client.controller;

import com.waraloyer.client.model.Property;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.PropertyService;
import com.waraloyer.client.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping
    public ResponseEntity<List<Property>> getProperties() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());
        List<Property> properties = propertyService.findByUserId(currentUser.getId());
        return ResponseEntity.ok(properties);
    }

    @PostMapping
    public ResponseEntity<Property> saveProperty(@RequestBody Property property) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());
        Property savedProperty = propertyService.save(property, currentUser);
        return ResponseEntity.ok(savedProperty);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());
        propertyService.deleteById(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}