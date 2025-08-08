package com.waraloyer.client.service;

import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;

    @Autowired
    public RentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public List<Rental> findByUserId(Long userId) {
        return rentalRepository.findByUserId(userId);
    }

    public Optional<Rental> findById(String id) {
        return rentalRepository.findById(id);
    }

    public Rental save(Rental rental, User user) {
        if (rental.getId() == null || rental.getId().isEmpty()) {
            rental.setId(UUID.randomUUID().toString());
            rental.setUser(user);
        } else {
            Optional<Rental> existingRental = rentalRepository.findById(rental.getId());
            if (existingRental.isPresent() && !existingRental.get().getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier ce loyer.");
            }
            rental.setUser(user);
        }
        return rentalRepository.save(rental);
    }

    public Rental markAsPaid(String id, Long userId) {
        Optional<Rental> rentalOptional = rentalRepository.findById(id);
        if (rentalOptional.isPresent() && rentalOptional.get().getUser().getId().equals(userId)) {
            Rental rental = rentalOptional.get();
            rental.setStatus("Paid");
            rental.setPaymentDate(LocalDate.now());
            return rentalRepository.save(rental);
        } else {
            throw new IllegalArgumentException("Loyer non trouvé ou vous n'êtes pas autorisé à le modifier.");
        }
    }
}