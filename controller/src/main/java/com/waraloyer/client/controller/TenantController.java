package com.waraloyer.client.controller;


import com.waraloyer.client.dto.TenantUpdateDTO;
import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.TenantService;
import com.waraloyer.client.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        List<Tenant> tenants = tenantService.findAll();
        return new ResponseEntity<>(tenants, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable Long id) {
        return tenantService.findById(id)
                .map(tenant -> new ResponseEntity<>(tenant, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Met à jour un locataire existant",
            description = "Met à jour un locataire par son ID et l'associe à un bien si nécessaire.")
    @ApiResponse(responseCode = "200", description = "Locataire mis à jour avec succès.")
    @ApiResponse(responseCode = "404", description = "Locataire non trouvé.")
    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable Long id, @RequestBody TenantUpdateDTO tenantDetails, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        Tenant updatedTenant = tenantService.updateTenant(id, tenantDetails, currentUser);
        return new ResponseEntity<>(updatedTenant, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        tenantService.delete(id, currentUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}