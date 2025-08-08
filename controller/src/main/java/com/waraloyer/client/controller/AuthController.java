package com.waraloyer.client.controller;


import com.waraloyer.client.config.JwtUtils;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.AuthService;
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

    // Endpoint pour un utilisateur qui a déjà payé et veut créer son mot de passe
    @PostMapping("/create-password")
    public ResponseEntity<?> createPassword(@RequestBody User user) {
        User updatedUser = authService.createPassword(user);
        return ResponseEntity.ok("Mot de passe créé avec succès pour " + updatedUser.getEmail());
    }

    // Endpoint pour la connexion
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
