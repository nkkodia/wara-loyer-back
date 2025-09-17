package com.waraloyer.client.controller;

import com.waraloyer.client.dto.TenantMessageDTO;
import com.waraloyer.client.model.TenantMessageLog;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.RentalRepository;
import com.waraloyer.client.repository.TenantMessageLogRepository;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @GetMapping("/by-rental/{rentalId}")
    public ResponseEntity<List<TenantMessageLog>> getMessagesByRental(@PathVariable Long rentalId, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        List<TenantMessageLog> messages = tenantMessageService.findByRentalId(rentalId, currentUser);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<TenantMessageLog>> getMessagesByTenant(@PathVariable Long tenantId, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        List<TenantMessageLog> messages = tenantMessageService.findByTenantId(tenantId, currentUser);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
    

    @GetMapping("/by-property/{propertyId}")
    public ResponseEntity<List<TenantMessageLog>> getMessagesByProperty(@PathVariable Long propertyId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            // Retourne une réponse 401 si l'utilisateur n'est pas authentifié
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            // L'objet principal n'est pas du type attendu, retourne une erreur d'authentification
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = (UserDetails) principal;
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        if (currentUser == null) {
            // L'utilisateur n'a pas été trouvé, potentiellement une session expirée ou un jeton invalide
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<TenantMessageLog> messages = tenantMessageService.findByPropertyId(propertyId, currentUser);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}
