package com.waraloyer.client.controller;

import com.waraloyer.client.dto.PasswordUpdateDTO;
import com.waraloyer.client.model.Subscription;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/management")
public class SubscriptionController {

    private final UserService userService;

    @Autowired
    public SubscriptionController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Obtenir l'abonnement de l'utilisateur",
            description = "Retourne les détails de l'abonnement de l'utilisateur authentifié.")
    @ApiResponse(responseCode = "200", description = "Abonnement récupéré avec succès.")
    @ApiResponse(responseCode = "401", description = "Accès non autorisé.")
    @GetMapping("/subscription")
    public ResponseEntity<Subscription> getUserSubscription(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        if (currentUser.getSubscription() != null) {
            return new ResponseEntity<>(currentUser.getSubscription(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordUpdateDTO dto, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findUserByEmail(userDetails.getUsername());

        try {
            userService.updatePassword(user.getId(), dto);
            return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
