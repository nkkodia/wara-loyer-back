package com.waraloyer.client.controller;

import com.waraloyer.client.config.JwtUtils;
import com.waraloyer.client.dto.UserCreatePasswordDTO;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.AuthService;
import com.waraloyer.client.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentification", description = "Endpoints pour la gestion de l'authentification et du mot de passe.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(AuthService authService, UserService userService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authService = authService;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Operation(summary = "Crée le mot de passe initial d'un utilisateur",
            description = "Permet à un utilisateur pré-enregistré par l'admin de définir son mot de passe pour la première fois.")
    @ApiResponse(responseCode = "200", description = "Mot de passe créé avec succès.")
    @ApiResponse(responseCode = "400", description = "Mot de passe déjà créé.")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé.")
    @PostMapping("/create-password")
    public ResponseEntity<?> createPassword(@RequestBody UserCreatePasswordDTO dto) {
        try {
            authService.createPassword(dto);
            return ResponseEntity.ok("Mot de passe créé avec succès pour " + dto.getEmail());
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>("Email inconnu.", HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Mot de passe déjà créé.", HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Connecte un utilisateur",
            description = "Authentifie un utilisateur avec son email et son mot de passe et retourne un token JWT.")
    @ApiResponse(responseCode = "200", description = "Authentification réussie, retourne un token JWT.")
    @ApiResponse(responseCode = "401", description = "Identifiants invalides.")
    @ApiResponse(responseCode = "403", description = "Abonnement expiré ou accès refusé.")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User userPrincipal = userService.findUserByEmail(userDetails.getUsername());

            // Vérifie si l'abonnement a expiré
            if (!authService.isAccessValid(userPrincipal) || !userPrincipal.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Votre compte est désactivé ou votre abonnement a expiré.");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (BadCredentialsException e) {
            // Gère les cas d'identifiants incorrects
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides.");
        }
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
