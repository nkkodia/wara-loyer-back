package com.waraloyer.client.controller;


import com.waraloyer.client.model.Tenant;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.TenantService;
import com.waraloyer.client.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping
    public ResponseEntity<List<Tenant>> getTenants() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());
        List<Tenant> tenants = tenantService.findByUserId(currentUser.getId());
        return ResponseEntity.ok(tenants);
    }

    @PostMapping
    public ResponseEntity<Tenant> saveTenant(@RequestBody Tenant tenant) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());
        Tenant savedTenant = tenantService.save(tenant, currentUser);
        return ResponseEntity.ok(savedTenant);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());
        tenantService.deleteById(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}