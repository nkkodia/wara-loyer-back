// src/main/java/com/waraloyer/client/service/DocumentGenerator.java

package com.waraloyer.client.service;

import com.waraloyer.client.dto.InvoiceSummaryDTO;
import com.waraloyer.client.model.User;
import com.waraloyer.client.model.Rental;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

// Interface pour la génération DOCX (Implémentation Docx4j)
public interface DocumentGenerator {

    // Méthode pour générer la mise en demeure complète
    File generateMiseEnDemeure(
            User currentUser,
            Rental rental,
            LocalDate paymentDeadline,
            List<InvoiceSummaryDTO> invoices
    ) throws Exception;
}