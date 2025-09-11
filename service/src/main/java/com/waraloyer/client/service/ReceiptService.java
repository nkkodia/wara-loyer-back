// src/main/java/com/waraloyer/client/service/ReceiptService.java
package com.waraloyer.client.service;

import com.lowagie.text.DocumentException;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

        // Générer le HTML (même logique que précédemment)
        String html = "<html><body><h1>Quittance de Loyer</h1>...</body></html>"; // Utiliser ta logique de génération de HTML

        // Créer le PDF à partir du HTML
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