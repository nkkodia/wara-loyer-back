package com.waraloyer.client.controller;

import com.waraloyer.client.model.SmsLog;
import com.waraloyer.client.service.SmsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    public SmsLogController(SmsLogService smsLogService) {
        this.smsLogService = smsLogService;
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
}
