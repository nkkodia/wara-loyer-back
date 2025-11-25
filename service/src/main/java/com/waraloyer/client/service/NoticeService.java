// src/main/java/com/waraloyer/client/service/NoticeService.java

package com.waraloyer.client.service;

import com.waraloyer.client.dto.NoticeGenerationRequestDTO;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.User;
import com.waraloyer.client.repository.RentalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class NoticeService {

    private final DocumentGenerator documentGenerator;
    private final RentalRepository rentalRepository;
    // Ajout d'autres services si nécessaire pour récupérer le Tenant, etc.

    @Autowired
    public NoticeService(DocumentGenerator documentGenerator, RentalRepository rentalRepository) {
        this.documentGenerator = documentGenerator;
        this.rentalRepository = rentalRepository;
    }

    public File generateFormalNotice(NoticeGenerationRequestDTO request, User currentUser) throws Exception {

        // 1. Récupération du bail et validation
        Rental rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new EntityNotFoundException("Location non trouvée avec l'ID : " + request.getRentalId()));

        // OPTIONNEL MAIS CRITIQUE : Vérification de la propriété du bail
        if (!rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé. Cette location ne vous appartient pas.");
        }

        // 2. Vérification des données (s'assurer que les factures ne sont pas vides)
        if (request.getInvoices() == null || request.getInvoices().isEmpty()) {
            throw new IllegalArgumentException("La liste des échéances impayées est vide.");
        }

        // 1. Récupérer la date du DTO (qui est maintenant un LocalDateTime)
        LocalDateTime paymentDateTime = request.getPaymentDeadline();

        // 2. Extraire la date pure (LocalDate) pour la logique ou le générateur
        LocalDate paymentDateOnly = paymentDateTime.toLocalDate(); // <-- CORRECTION CLÉ

        // 3. Appel au générateur de document (l'implémentation Docx4j)
        return documentGenerator.generateMiseEnDemeure(
                currentUser,
                rental,
                paymentDateOnly,
                request.getInvoices()
        );
    }
}