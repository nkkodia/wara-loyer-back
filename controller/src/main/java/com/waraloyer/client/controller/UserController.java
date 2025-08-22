package com.waraloyer.client.controller;

import com.waraloyer.client.config.JwtUtils;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
