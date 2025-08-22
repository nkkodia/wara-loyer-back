package com.waraloyer.client.service;

import com.waraloyer.client.model.Rental;
import com.waraloyer.client.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;

    @Autowired
    public RentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public Rental create(Rental rental) {
        return rentalRepository.save(rental);
    }

    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }

    public Optional<Rental> findById(String id) {
        return rentalRepository.findById(id);
    }

    public Rental update(String id, Rental rentalDetails) {
        Rental existingRental = rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location non trouvée avec l'ID " + id));

        // Mettez à jour les champs de l'objet existant avec les détails de la requête
        existingRental.setDueDate(rentalDetails.getDueDate());
        existingRental.setAmountDue(rentalDetails.getAmountDue());
        existingRental.setPaymentDate(rentalDetails.getPaymentDate());
        existingRental.setStatus(rentalDetails.getStatus());
        existingRental.setComments(rentalDetails.getComments());

        return rentalRepository.save(existingRental);
    }

    public void delete(String id) {
        rentalRepository.deleteById(id);
    }
}
