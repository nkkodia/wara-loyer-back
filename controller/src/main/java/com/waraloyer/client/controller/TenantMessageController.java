package com.waraloyer.client.controller;

import com.waraloyer.client.model.TenantMessageLog;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.TenantMessageService;
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

import java.util.List;

@Tag(name = "Communication Locataire", description = "Endpoints pour l'envoi et la consultation des messages des locataires.")
@RestController
@RequestMapping("/api/tenant-messages")
public class TenantMessageController {

    private final TenantMessageService tenantMessageService;
    private final UserService userService;

    @Autowired
    public TenantMessageController(TenantMessageService tenantMessageService, UserService userService) {
        this.tenantMessageService = tenantMessageService;
        this.userService = userService;
    }

    @Operation(summary = "Un locataire signale un problème (public)",
            description = "Endpoint public permettant à un locataire de signaler un problème via un lien SMS. Aucune authentification n'est requise.")
    @ApiResponse(responseCode = "200", description = "Message enregistré avec succès.")
    @ApiResponse(responseCode = "400", description = "Locataire ou message invalide.")
    @PostMapping("/report-problem/{tenantId}")
    public ResponseEntity<TenantMessageLog> reportProblem(@PathVariable Long tenantId, @RequestBody String message) {
        try {
            TenantMessageLog savedMessage = tenantMessageService.reportProblem(tenantId, message);
            return new ResponseEntity<>(savedMessage, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Lister les messages reçus (sécurisé)",
            description = "Retourne la liste des messages reçus par l'utilisateur (bailleur) authentifié.")
    @ApiResponse(responseCode = "200", description = "Liste des messages récupérée avec succès.")
    @ApiResponse(responseCode = "401", description = "Accès non autorisé.")
    @GetMapping("/my-messages")
    public ResponseEntity<List<TenantMessageLog>> getMyMessages(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        List<TenantMessageLog> messages = tenantMessageService.getMessagesByUserId(currentUser.getId());
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}
