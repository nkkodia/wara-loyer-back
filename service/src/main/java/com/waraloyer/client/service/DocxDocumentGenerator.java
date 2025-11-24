// src/main/java/com/waraloyer/client/service/DocxDocumentGenerator.java

package com.waraloyer.client.service;

import com.waraloyer.client.dto.InvoiceSummaryDTO;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.User;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Concrete implementation for generating DOCX documents using a library like Docx4j (implementation details omitted).
 */
@Service // <-- This annotation tells Spring to create a bean of type DocumentGenerator
public class DocxDocumentGenerator implements DocumentGenerator {

    @Override
    public File generateMiseEnDemeure(
            User currentUser,
            Rental rental,
            LocalDate paymentDeadline,
            List<InvoiceSummaryDTO> invoices
    ) throws Exception {
        // NOTE: In a real application, the Docx4j logic (loading templates,
        // replacing content, saving the file) would go here.

        // For testing/development setup, we return a mock file instance.
        // You'll need a placeholder file in the root of your project/module.
        File templateFile = new ClassPathResource("templates/mise-en-demeure-modele.docx").getFile();

        // Ensure the mock file exists so the File.length() call doesn't fail later
        if (!templateFile.exists()) {
            templateFile.createNewFile();
        }

        return templateFile;
    }
}