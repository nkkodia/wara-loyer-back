package com.waraloyer.client.controller;

import com.waraloyer.client.model.ClientConfig;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.ClientConfigService;
import com.waraloyer.client.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Configuration", description = "Endpoints pour la configuration des communications et paiements.")
@RestController
@RequestMapping("/api/config")
public class ClientConfigController {

    private final ClientConfigService configService;
    private final UserService userService;

    @Autowired
    public ClientConfigController(ClientConfigService configService, UserService userService) {
        this.configService = configService;
        this.userService = userService;
    }

    @Operation(summary = "Créer ou mettre à jour la configuration",
            description = "Crée ou met à jour la configuration pour l'utilisateur authentifié.")
    @ApiResponse(responseCode = "200", description = "Configuration sauvegardée avec succès.")
    @PostMapping
    public ResponseEntity<ClientConfig> saveConfig(@RequestBody ClientConfig config, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        // Ajoutez l'URL de signalement de problème dans le message de relance
        String relanceMessage = config.getSmsRelanceMessage() + " Pour signaler un problème, cliquez sur ce lien : {URL_PROBLEME}";
        config.setSmsRelanceMessage(relanceMessage);

        ClientConfig savedConfig = configService.save(config, currentUser);
        return new ResponseEntity<>(savedConfig, HttpStatus.OK);
    }

    @Operation(summary = "Obtenir la configuration de l'utilisateur",
            description = "Retourne la configuration actuelle de l'utilisateur authentifié.")
    @ApiResponse(responseCode = "200", description = "Configuration récupérée avec succès.")
    @GetMapping
    public ResponseEntity<ClientConfig> getConfig(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        ClientConfig config = configService.getOrCreate(currentUser);
        return new ResponseEntity<>(config, HttpStatus.OK);
    }

    @Operation(summary = "Mettre à jour la configuration",
            description = "Met à jour la configuration de l'utilisateur authentifié.")
    @ApiResponse(responseCode = "200", description = "Configuration mise à jour avec succès.")
    @ApiResponse(responseCode = "404", description = "Configuration non trouvée.")
    @PutMapping
    public ResponseEntity<ClientConfig> updateConfig(@RequestBody ClientConfig config, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        config.setUser(currentUser);

        ClientConfig updatedConfig = configService.save(config, currentUser);
        return new ResponseEntity<>(updatedConfig, HttpStatus.OK);
    }
}
