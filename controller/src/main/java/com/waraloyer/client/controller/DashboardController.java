package com.waraloyer.client.controller;

import com.waraloyer.client.dto.request.DashboardSummary;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.DashboardService;
import com.waraloyer.client.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Tableau de Bord", description = "Endpoints pour les données du tableau de bord.")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    @Autowired
    public DashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    @Operation(summary = "Obtenir le résumé du tableau de bord",
            description = "Retourne les indicateurs clés pour le tableau de bord de l'utilisateur authentifié.")
    @ApiResponse(responseCode = "200", description = "Résumé du tableau de bord récupéré avec succès.")
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> getDashboardSummary(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        DashboardSummary summary = dashboardService.getSummaryForUser(currentUser);
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }
}