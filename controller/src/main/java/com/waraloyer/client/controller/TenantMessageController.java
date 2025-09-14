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
    private final TenantMessageLogRepository tenantMessageRepository;
    private final RentalRepository rentalRepository;


    @Autowired
    public TenantMessageController(TenantMessageService tenantMessageService, UserService userService, TenantMessageLogRepository tenantMessageRepository, RentalRepository rentalRepository) {
        this.tenantMessageService = tenantMessageService;
        this.userService = userService;
        this.tenantMessageRepository = tenantMessageRepository;
        this.rentalRepository = rentalRepository;
    }

    @PostMapping("/report-problem/{rentalId}")
    public ResponseEntity<?> reportProblem(@PathVariable Long rentalId, @RequestBody TenantMessageDTO messageDto) {
        return rentalRepository.findById(rentalId)
                .map(rental -> {
                    TenantMessageLog message = new TenantMessageLog();
                    message.setTenant(rental.getTenant());
                    message.setProperty(rental.getProperty());
                    message.setMessage(messageDto.getMessageContent());
                    message.setSentDate(Instant.from(LocalDateTime.now().atZone(ZoneId.systemDefault())));

                    tenantMessageRepository.save(message);
                    return new ResponseEntity<>("Message enregistré", HttpStatus.CREATED);
                })
                .orElse(new ResponseEntity<>("Location non trouvée", HttpStatus.NOT_FOUND));
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
}
