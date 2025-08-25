package com.waraloyer.client.controller;


import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.TenantService;
import com.waraloyer.client.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;
    private final UserService userService;

    @Autowired
    public TenantController(TenantService tenantService, UserService userService) {
        this.tenantService = tenantService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody Tenant tenant, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        Tenant newTenant = tenantService.create(tenant, currentUser);
        return new ResponseEntity<>(newTenant, HttpStatus.CREATED);
    }

    // READ - Lister tous les locataires pour l'utilisateur authentifié
    @GetMapping("/my-tenants")
    public ResponseEntity<List<Tenant>> getMyTenants(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        List<Tenant> tenants = tenantService.findByUserId(currentUser.getId());
        return new ResponseEntity<>(tenants, HttpStatus.OK);
    }

    // READ - Lister tous les locataires
    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        List<Tenant> tenants = tenantService.findAll();
        return new ResponseEntity<>(tenants, HttpStatus.OK);
    }

    // READ - Obtenir un locataire par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable Long id) {
        return tenantService.findById(id)
                .map(tenant -> new ResponseEntity<>(tenant, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // UPDATE - Mettre à jour un locataire existant
    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable Long id, @RequestBody Tenant tenantDetails) {
        Tenant updatedTenant = tenantService.update(id, tenantDetails);
        return new ResponseEntity<>(updatedTenant, HttpStatus.OK);
    }

    // DELETE - Supprimer un locataire par son ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        tenantService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}