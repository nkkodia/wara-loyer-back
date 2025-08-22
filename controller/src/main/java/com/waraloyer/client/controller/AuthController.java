package com.waraloyer.client.controller;

import com.waraloyer.client.config.JwtUtils;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.AuthService;
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

@Tag(name = "Authentification", description = "Endpoints pour la gestion de l'authentification et du mot de passe.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Operation(summary = "Crée le mot de passe initial d'un utilisateur",
            description = "Permet à un utilisateur pré-enregistré par l'admin de définir son mot de passe pour la première fois.")
    @ApiResponse(responseCode = "200", description = "Mot de passe créé avec succès.")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé.")
    @PostMapping("/create-password")
    public ResponseEntity<?> createPassword(@RequestBody User user) {
        User updatedUser = authService.createPassword(user);
        return ResponseEntity.ok("Mot de passe créé avec succès pour " + updatedUser.getEmail());
    }

    @Operation(summary = "Connecte un utilisateur",
            description = "Authentifie un utilisateur avec son email et son mot de passe et retourne un token JWT.")
    @ApiResponse(responseCode = "200", description = "Authentification réussie, retourne un token JWT.")
    @ApiResponse(responseCode = "401", description = "Identifiants invalides.")
    @ApiResponse(responseCode = "403", description = "Abonnement expiré ou accès refusé.")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

        User userPrincipal = (User) authentication.getPrincipal();

        if (!authService.isAccessValid(userPrincipal)) {
            return ResponseEntity.status(403).body("Votre abonnement a expiré.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    static class JwtResponse {
        private String token;

        public JwtResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}
