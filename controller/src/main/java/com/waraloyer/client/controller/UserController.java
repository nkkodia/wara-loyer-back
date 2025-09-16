package com.waraloyer.client.controller;

import com.waraloyer.client.config.JwtUtils;
import com.waraloyer.client.model.Subscription;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Utilisateurs", description = "Endpoints pour l'enregistrement des utilisateurs (utilisé par l'admin).")
@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Enregistre un nouvel utilisateur",
            description = "Endpoint pour l'admin pour créer un nouvel utilisateur avec un mot de passe initial.")
    @ApiResponse(responseCode = "200", description = "Utilisateur enregistré avec succès.")
    @ApiResponse(responseCode = "400", description = "Données d'utilisateur invalides (email ou username déjà utilisé).")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        userService.registerNewUser(user);
        return ResponseEntity.ok("Utilisateur enregistré avec succès !");
    }

    @Operation(summary = "Liste tous les utilisateurs",
            description = "Retourne la liste complète de tous les utilisateurs enregistrés.")
    @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée avec succès.")
    @GetMapping("/users")
    public ResponseEntity<List<User>> listAllUsers() {
        List<User> users = userService.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
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
}
