package com.waraloyer.client.controller;


import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.User;
import com.waraloyer.client.service.RentalService;
import com.waraloyer.client.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    // CREATE - Crée une nouvelle location
    @PostMapping
    public ResponseEntity<Rental> createRental(@RequestBody Rental rental, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        Rental newRental = rentalService.create(rental, currentUser);
        return new ResponseEntity<>(newRental, HttpStatus.CREATED);
    }

    // READ - Liste les locations de l'utilisateur authentifié
    @GetMapping("/my-rentals")
    public ResponseEntity<List<Rental>> getMyRentals(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        List<Rental> rentals = rentalService.findByUserId(currentUser.getId());
        return new ResponseEntity<>(rentals, HttpStatus.OK);
    }

    // READ - Obtient une location par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Rental> getRentalById(@PathVariable Long id) {
        return rentalService.findById(id)
                .map(rental -> new ResponseEntity<>(rental, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // UPDATE - Met à jour une location existante
    @PutMapping("/{id}")
    public ResponseEntity<Rental> updateRental(@PathVariable Long id, @RequestBody Rental rentalDetails) {
        Rental updatedRental = rentalService.update(id, rentalDetails);
        return new ResponseEntity<>(updatedRental, HttpStatus.OK);
    }

    // UPDATE - Marque une location comme payée
    @PutMapping("/mark-paid/{id}")
    public ResponseEntity<Rental> markRentalAsPaid(@PathVariable Long id, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        Rental updatedRental = rentalService.markAsPaid(id, currentUser.getId());
        return new ResponseEntity<>(updatedRental, HttpStatus.OK);
    }

    // DELETE - Supprime une location par son ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRental(@PathVariable Long id) {
        rentalService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}