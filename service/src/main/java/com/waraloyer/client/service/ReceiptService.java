// src/main/java/com/waraloyer/client/service/ReceiptService.java
package com.waraloyer.client.service;

import com.lowagie.text.DocumentException;
import org.springframework.security.access.AccessDeniedException;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class ReceiptService {

    private final RentalService rentalService;

    @Autowired
    public ReceiptService(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    public byte[] generateReceiptPdf(Long rentalId, User currentUser, String paymentMethod) {
        Rental rental = rentalService.findById(rentalId, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Loyer non trouvé."));

        if (!"Paid".equals(rental.getStatus())) {
            throw new IllegalArgumentException("La quittance ne peut être générée que pour un loyer payé.");
        }

        // Vérification de la propriété
        if (!rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Accès refusé. Le loyer n'appartient pas à cet utilisateur.");
        }

        // Utilise DateTimeFormatter pour un formatage plus propre
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Assure-toi que les objets imbriqués ne sont pas nuls
        String tenantFullName = rental.getTenant() != null ? rental.getTenant().getFirstName() + " " + rental.getTenant().getLastName() : "N/A";
        String propertyAddress = rental.getProperty() != null ? rental.getProperty().getAddress() : "N/A";

        // Générer le HTML
        String html = "<html><body><h1>Quittance de Loyer</h1>"
                + "<p>Locataire : " + tenantFullName + "</p>"
                + "<p>Bien : " + propertyAddress + "</p>"
                + "<p>Montant : " + rental.getAmountDue().toString() + "</p>"
                + "<p>Méthode de paiement : " + paymentMethod + "</p>"
                + "<p>Date de paiement : " + rental.getPaymentDate().format(dateFormatter) + "</p>"
                + "</body></html>";

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
            return os.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
}