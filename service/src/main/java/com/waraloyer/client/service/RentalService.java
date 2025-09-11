package com.waraloyer.client.service;

import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.SmsLog;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.RentalRepository;
import com.waraloyer.client.repository.SmsLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RentalService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.12");

    private final RentalRepository rentalRepository;
    private final SmsLogRepository smsLogRepository;

    @Autowired
    public RentalService(RentalRepository rentalRepository, SmsLogRepository smsLogRepository) {
        this.rentalRepository = rentalRepository;
        this.smsLogRepository = smsLogRepository;
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

    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }

    public List<Rental> findByUserId(Long userId) {
        return rentalRepository.findByUserId(userId);
    }


    public Rental update(Long id, Rental rentalDetails, User currentUser) {
        Rental existingRental = rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location non trouvée avec l'ID " + id));

        if (!existingRental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès non autorisé.");
        }

        // Logique de mise à jour des champs
        existingRental.setDueDate(rentalDetails.getDueDate());
        existingRental.setAmountDue(rentalDetails.getAmountDue());
        existingRental.setPaymentDate(rentalDetails.getPaymentDate());
        existingRental.setStatus(rentalDetails.getStatus());
        existingRental.setComments(rentalDetails.getComments());
        existingRental.setReminderSent(rentalDetails.isReminderSent());
        existingRental.setLastReminderSentDate(rentalDetails.getLastReminderSentDate());
        existingRental.setRelanceSent(rentalDetails.isRelanceSent());
        existingRental.setLastRelanceSentDate(rentalDetails.getLastRelanceSentDate());

        return rentalRepository.save(existingRental);
    }

    public void delete(Long id) {
        rentalRepository.deleteById(id);
    }

    public Rental markAsPaid(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Location non trouvée avec l'ID " + rentalId));

        if (!rental.getUser().getId().equals(userId)) {
            throw new RuntimeException("Accès non autorisé.");
        }

        rental.setStatus("PAID");
        rental.setPaymentDate(LocalDate.now());

        return rentalRepository.save(rental);
    }

    /**
     * Met à jour les coûts mensuels et calcule les impôts pour une location donnée.
     * @param rentalId L'ID de la location.
     * @param userId L'ID de l'utilisateur.
     * @param monthlyCosts Les coûts mensuels.
     * @return La location mise à jour.
     */
    public Rental updateFinancials(Long rentalId, Long userId, BigDecimal monthlyCosts) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Location non trouvée avec l'ID " + rentalId));

        if (!rental.getUser().getId().equals(userId)) {
            throw new RuntimeException("Accès non autorisé.");
        }

        rental.setMonthlyCosts(monthlyCosts);

        // Calcul des impôts : 12% du loyer hors charges
        BigDecimal taxBase = rental.getProperty().getRentAmount();
        BigDecimal taxes = taxBase.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        rental.setTaxes(taxes);

        return rentalRepository.save(rental);
    }

    /**
     * Envoie un SMS de rappel de loyer.
     * @param rentalId L'ID de la location.
     * @param message Le message à envoyer.
     * @return La location mise à jour.
     */
    public Rental sendReminderSms(Long rentalId, String message) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Location non trouvée avec l'ID " + rentalId));

        System.out.println("Envoi d'un SMS de rappel pour la location " + rental.getId());

        SmsLog log = new SmsLog();
        log.setType("RAPPEL");
        log.setMessage(message);
        log.setSentDate(LocalDate.now());
        log.setStatus("SENT");
        log.setUser(rental.getUser());
        log.setRental(rental);
        log.setLocataire(rental.getTenant());
        log.setProperty(rental.getProperty());
        smsLogRepository.save(log);

        rental.setReminderSent(true);
        rental.setLastReminderSentDate(LocalDate.now());
        return rentalRepository.save(rental);
    }

    /**
     * Envoie un SMS de relance pour loyer en retard.
     * @param rentalId L'ID de la location.
     * @param message Le message à envoyer.
     * @return La location mise à jour.
     */
    public Rental sendRelanceSms(Long rentalId, String message) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Location non trouvée avec l'ID " + rentalId));

        System.out.println("Envoi d'un SMS de relance pour la location " + rental.getId());

        SmsLog log = new SmsLog();
        log.setType("RELANCE");
        log.setMessage(message);
        log.setSentDate(LocalDate.now());
        log.setStatus("SENT");
        log.setUser(rental.getUser());
        log.setRental(rental);
        log.setLocataire(rental.getTenant());
        log.setProperty(rental.getProperty());
        smsLogRepository.save(log);

        rental.setRelanceSent(true);
        rental.setLastRelanceSentDate(LocalDate.now());
        return rentalRepository.save(rental);
    }

    public boolean belongsToUser(Long rentalId, Long userId) {
        return rentalRepository.findById(rentalId)
                .map(rental -> rental.getUser().getId().equals(userId))
                .orElse(false);
    }
    public Optional<Rental> findById(Long id, User currentUser) {
        return rentalRepository.findById(id)
                .map(rental -> {
                    if (!rental.getUser().getId().equals(currentUser.getId())) {
                        throw new AccessDeniedException("Accès refusé. Ce loyer n'appartient pas à cet utilisateur.");
                    }
                    return rental;
                });
    }
    public List<Rental> findByTenantId(Long tenantId, Long userId) {
        // Logique de vérification d'autorisation
        return rentalRepository.findByTenantIdAndUserId(tenantId, userId);
    }
}
