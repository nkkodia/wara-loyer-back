// src/main/java/com/waraloyer/client/controller/ExpenseController.java

package com.waraloyer.client.controller;

import com.waraloyer.client.dto.ExpenseDTO;
import com.waraloyer.client.model.Expense;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.ExpenseService;
import com.waraloyer.client.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Dépenses", description = "Endpoints pour la gestion des dépenses liées aux locations.")
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;

    @Autowired
    public ExpenseController(ExpenseService expenseService, UserService userService) {
        this.expenseService = expenseService;
        this.userService = userService;
    }

    @Operation(summary = "Obtenir les dépenses d'une location pour un mois donné",
            description = "Retourne la liste des dépenses pour une location spécifique, filtrée par mois.")
    @ApiResponse(responseCode = "200", description = "Liste des dépenses récupérée avec succès.")
    @ApiResponse(responseCode = "403", description = "Accès non autorisé.")
    @ApiResponse(responseCode = "404", description = "Location non trouvée.")
    @GetMapping("/rentals/{rentalId}")
    public ResponseEntity<List<ExpenseDTO>> getExpensesForRental(@PathVariable Long rentalId, @RequestParam String month, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        try {
            LocalDate monthDate = LocalDate.parse(month + "-01");
            List<ExpenseDTO> expenses = expenseService.findByRentalIdAndDateBetween(rentalId, monthDate, monthDate.withDayOfMonth(monthDate.lengthOfMonth()), currentUser);
            return new ResponseEntity<>(expenses, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Ajouter une dépense à une location",
            description = "Ajoute une dépense pour une location spécifique et met à jour le rapport financier.")
    @ApiResponse(responseCode = "201", description = "Dépense ajoutée avec succès.")
    @ApiResponse(responseCode = "403", description = "Accès non autorisé.")
    @ApiResponse(responseCode = "404", description = "Location non trouvée.")
    @PostMapping("/rentals/{rentalId}")
    public ResponseEntity<Expense> addExpense(@PathVariable Long rentalId, @RequestBody ExpenseDTO expenseDto, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        try {
            Expense expense = expenseService.addExpense(rentalId, expenseDto.getAmount(), expenseDto.getDescription(), currentUser);
            return new ResponseEntity<>(expense, HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}