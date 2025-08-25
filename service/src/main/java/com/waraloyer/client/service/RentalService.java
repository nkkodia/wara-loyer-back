package com.waraloyer.client.service;

import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.Tenant;
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

    public Rental create(Rental rental) {
        return rentalRepository.save(rental);
    }

    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }

    public Optional<Rental> findById(Long id) {
        return rentalRepository.findById(id);
    }

    public Rental update(Long id, Rental rentalDetails) {
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

    public void delete(Long id) {
        rentalRepository.deleteById(id);
    }

    public List<Rental> findByUserId(Long id) {
        return rentalRepository.findByUserId(id);
    }

    public Rental save(Rental rental, User user) {
        if (rental.getId() == null) {
            rental.setUser(user);
        } else {
            Optional<Rental> existingTenant = rentalRepository.findById(rental.getId());
            if (existingTenant.isPresent() && !existingTenant.get().getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier ce locataire.");
            }
            rental.setUser(user);
        }
        return rentalRepository.save(rental);
    }

    /**
     * Marque une location comme payée et enregistre la date de paiement,
     * après avoir vérifié que l'utilisateur est bien le propriétaire.
     * @param rentalId L'identifiant de la location.
     * @param userId L'identifiant de l'utilisateur.
     * @return L'objet Rental mis à jour.
     */
    public Rental markAsPaid(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Location non trouvée avec l'ID " + rentalId));

        // Vérification de sécurité : le loyer appartient-il à l'utilisateur ?
        if (!rental.getUser().getId().equals(userId)) {
            throw new RuntimeException("Accès non autorisé.");
        }

        rental.setStatus("PAID");
        rental.setPaymentDate(LocalDate.now());

        return rentalRepository.save(rental);
    }
}
