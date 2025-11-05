package com.waraloyer.client.controller;

import com.waraloyer.client.model.User;
import com.waraloyer.client.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ROLE_ADMIN')") // <<< AJOUTEZ CETTE ANNOTATION
    @GetMapping("/users")
    public ResponseEntity<List<User>> listAllUsers() {
        List<User> users = userService.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }


}
