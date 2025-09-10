package com.waraloyer.client.controller;

import com.waraloyer.client.model.SmsLog;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.SmsLogService;
import com.waraloyer.client.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "SMS", description = "Endpoints pour la gestion de l'historique des communications par SMS.")
@RestController
@RequestMapping("/api/sms")
public class SmsLogController {

    private final SmsLogService smsLogService;
    private final UserService userService;

    @Autowired
    public SmsLogController(SmsLogService smsLogService, UserService userService) {
        this.smsLogService = smsLogService;
        this.userService = userService;
    }

    @Operation(summary = "Lister l'historique des SMS",
            description = "Retourne la liste de tous les SMS envoyés pour l'utilisateur authentifié.")
    @ApiResponse(responseCode = "200", description = "Historique des SMS récupéré avec succès.")
    @GetMapping("/logs")
    public ResponseEntity<List<SmsLog>> getAllSmsLogs() {
        // La logique pour récupérer les logs de l'utilisateur sera ajoutée au service.
        List<SmsLog> smsLogs = smsLogService.findAll();
        return new ResponseEntity<>(smsLogs, HttpStatus.OK);
    }

    @Operation(summary = "Lister l'historique des SMS par loyer",
            description = "Retourne la liste des SMS envoyés pour un loyer spécifique de l'utilisateur authentifié.")
    @ApiResponse(responseCode = "200", description = "Historique des SMS récupéré avec succès.")
    @ApiResponse(responseCode = "403", description = "Accès non autorisé.")
    @ApiResponse(responseCode = "404", description = "Loyer non trouvé.")
    @GetMapping("/logs/by-rental/{rentalId}")
    public ResponseEntity<List<SmsLog>> getLogsByRentalId(@PathVariable Long rentalId, Authentication authentication) {
        // La logique de vérification de l'utilisateur sera dans le service.
        List<SmsLog> smsLogs = smsLogService.findByRentalIdAndUserId(rentalId, authentication);
        return new ResponseEntity<>(smsLogs, HttpStatus.OK);
    }

    /**
     * Envoie un SMS pour une relance manuelle et enregistre l'opération.
     * @param to Le numéro de téléphone du destinataire.
     * @param messageBody Le corps du message.
     * @param type Le type de message.
     * @return Le log du SMS envoyé.
     */
    @PostMapping("/send-relance")
    public ResponseEntity<SmsLog> sendRelanceSms(@RequestParam String to, @RequestParam String messageBody, @RequestParam String type, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        SmsLog log = smsLogService.sendSms(currentUser, to, messageBody, type);
        return new ResponseEntity<>(log, HttpStatus.OK);
    }
}