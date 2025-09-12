package com.waraloyer.client.controller;

import com.waraloyer.client.model.User;
import com.waraloyer.client.service.ReportService;
import com.waraloyer.client.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "Rapports", description = "Endpoints pour la génération de rapports financiers.")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    @Autowired
    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @Operation(summary = "Obtenir un rapport financier global",
            description = "Calcule et retourne un aperçu financier incluant les revenus et le statut des loyers pour l'utilisateur authentifié.")
    @ApiResponse(responseCode = "200", description = "Rapport financier généré avec succès.")
    @GetMapping("/financial")
    public ResponseEntity<Map<String, Object>> getFinancialReport(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        Map<String, Object> reportData = reportService.getFinancialOverview(currentUser.getId());
        return new ResponseEntity<>(reportData, HttpStatus.OK);
    }

    @Operation(summary = "Ajouter des coûts mensuels à une location",
            description = "Ajoute des coûts imprévus à une location spécifique et met à jour le rapport financier.")
    @ApiResponse(responseCode = "200", description = "Coûts ajoutés et rapport mis à jour.")
    @ApiResponse(responseCode = "404", description = "Location ou utilisateur non trouvé.")
    @PostMapping("/financial/{rentalId}/costs")
    public ResponseEntity<Map<String, Object>> addMonthlyCosts(@PathVariable Long rentalId, @RequestBody Map<String, Object> requestBody, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        BigDecimal monthlyCosts = new BigDecimal(requestBody.get("monthlyCosts").toString());
        String description = requestBody.get("description").toString();

        Map<String, Object> updatedReport = reportService.addMonthlyCosts(rentalId, currentUser.getId(), monthlyCosts, description);
        return new ResponseEntity<>(updatedReport, HttpStatus.OK);
    }
}
