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


    /**
     * Crée une nouvelle location en l'associant à l'utilisateur actuel.
     * @param rental L'objet Rental à créer.
     * @param user L'utilisateur actuellement authentifié.
     * @return L'objet Rental créé.
     */
    public Rental create(Rental rental, User user) {
        rental.setUser(user);
        return rentalRepository.save(rental);
    }

    /**
     * Récupère la liste de toutes les locations.
     * @return Une liste de toutes les locations.
     */
    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }

    /**
     * Récupère toutes les locations pour un utilisateur donné.
     * @param userId L'ID de l'utilisateur.
     * @return Une liste de locations appartenant à l'utilisateur.
     */
    public List<Rental> findByUserId(Long userId) {
        return rentalRepository.findByUserId(userId);
    }

    /**
     * Récupère une location par son identifiant unique.
     * @param id L'identifiant de la location.
     * @return Un Optional contenant la location si elle existe.
     */
    public Optional<Rental> findById(Long id) {
        return rentalRepository.findById(id);
    }

    /**
     * Met à jour une location existante.
     * @param id L'identifiant de la location à mettre à jour.
     * @param rentalDetails L'objet contenant les détails de la mise à jour.
     * @return L'objet Rental mis à jour.
     */
    public Rental update(Long id, Rental rentalDetails) {
        Rental existingRental = rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location non trouvée avec l'ID " + id));

        // Met à jour les champs de la location existante
        existingRental.setDueDate(rentalDetails.getDueDate());
        existingRental.setAmountDue(rentalDetails.getAmountDue());
        existingRental.setPaymentDate(rentalDetails.getPaymentDate());
        existingRental.setStatus(rentalDetails.getStatus());
        existingRental.setComments(rentalDetails.getComments());

        return rentalRepository.save(existingRental);
    }

    /**
     * Supprime une location par son identifiant.
     * @param id L'identifiant de la location à supprimer.
     */
    public void delete(Long id) {
        rentalRepository.deleteById(id);
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
