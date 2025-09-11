package com.waraloyer.client.controller;

import com.waraloyer.client.dto.SmsRequestDTO;
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
        List<SmsLog> smsLogs = smsLogService.findByRentalIdAndUserId(rentalId, authentication);
        return new ResponseEntity<>(smsLogs, HttpStatus.OK);
    }


    @PostMapping("/send-relance/{rentalId}")
    public ResponseEntity<SmsLog> sendRelanceSms(@PathVariable Long rentalId, @RequestBody SmsRequestDTO requestDTO, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        SmsLog log = smsLogService.sendSms(currentUser, requestDTO.getToPhoneNumber(), requestDTO.getMessageBody(), requestDTO.getType(), requestDTO.getScheduleDate(), rentalId);
        return new ResponseEntity<>(log, HttpStatus.OK);
    }
}