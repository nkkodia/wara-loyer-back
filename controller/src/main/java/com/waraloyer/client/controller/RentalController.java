package com.waraloyer.client.controller;

import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.RentalService;
import com.waraloyer.client.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Locations", description = "Endpoints pour la gestion des locations.")
@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final UserService userService;

    @Autowired
    public RentalController(RentalService rentalService, UserService userService) {
        this.rentalService = rentalService;
        this.userService = userService;
    }

    @Operation(summary = "Crée une nouvelle location",
            description = "Crée une nouvelle location pour l'utilisateur authentifié.")
    @ApiResponse(responseCode = "201", description = "Location créée avec succès.")
    @PostMapping
    public ResponseEntity<Rental> createRental(@RequestBody Rental rental, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        Rental newRental = rentalService.create(rental, currentUser);
        return new ResponseEntity<>(newRental, HttpStatus.CREATED);
    }

    @Operation(summary = "Lister les locations de l'utilisateur",
            description = "Retourne la liste des locations gérées par l'utilisateur authentifié.")
    @ApiResponse(responseCode = "200", description = "Liste des locations récupérée avec succès.")
    @GetMapping("/my-rentals")
    public ResponseEntity<List<Rental>> getMyRentals(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        List<Rental> rentals = rentalService.findByUserId(currentUser.getId());
        return new ResponseEntity<>(rentals, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rental> getRentalById(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        try {
            Rental rental = rentalService.findById(id, currentUser)
                    .orElseThrow(() -> new EntityNotFoundException("Location non trouvée avec l'ID " + id));
            return new ResponseEntity<>(rental, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @Operation(summary = "Mettre à jour une location",
            description = "Met à jour une location existante par son ID.")
    @ApiResponse(responseCode = "200", description = "Location mise à jour avec succès.")
    @ApiResponse(responseCode = "404", description = "Location non trouvée.")
    @PutMapping("/{id}")
    public ResponseEntity<Rental> updateRental(@PathVariable Long id, @RequestBody Rental rentalDetails, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        try {
            Rental updatedRental = rentalService.update(id, rentalDetails, currentUser);
            return new ResponseEntity<>(updatedRental, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @Operation(summary = "Marquer une location comme payée",
            description = "Met à jour le statut d'une location en 'PAYÉ' pour l'utilisateur authentifié.")
    @ApiResponse(responseCode = "200", description = "Location marquée comme payée avec succès.")
    @ApiResponse(responseCode = "403", description = "Accès non autorisé.")
    @ApiResponse(responseCode = "404", description = "Location non trouvée.")
    @PutMapping("/mark-paid/{id}")
    public ResponseEntity<Rental> markRentalAsPaid(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        Rental updatedRental = rentalService.markAsPaid(id, currentUser.getId());
        return new ResponseEntity<>(updatedRental, HttpStatus.OK);
    }

    @Operation(summary = "Supprimer une location",
            description = "Supprime une location par son ID.")
    @ApiResponse(responseCode = "204", description = "Location supprimée avec succès.")
    @ApiResponse(responseCode = "404", description = "Location non trouvée.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRental(@PathVariable Long id) {
        rentalService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
