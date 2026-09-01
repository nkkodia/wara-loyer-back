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

    @GetMapping("/by-rental/{rentalId}")
    public ResponseEntity<List<TenantMessageLog>> getMessagesByRental(@PathVariable Long rentalId, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        List<TenantMessageLog> messages = tenantMessageService.findByRentalId(rentalId, currentUser);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }


}
