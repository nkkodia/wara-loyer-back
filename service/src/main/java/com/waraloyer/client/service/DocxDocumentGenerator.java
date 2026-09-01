// src/main/java/com/waraloyer/client/service/DocxDocumentGenerator.java

package com.waraloyer.client.service;

import com.waraloyer.client.dto.InvoiceSummaryDTO;
import com.waraloyer.client.model.Rental;
import com.waraloyer.client.model.User;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
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
        File tempFile = new File("mise-en-demeure_TEMP.txt");
        FileWriter writer = new FileWriter(tempFile);
        writer.write("DOCX Generation Test Successful.");
        writer.close();

        // Renommez-le en .docx pour satisfaire le Content-Type dans le contrôleur.
        File mockDocx = new File("mise-en-demeure_MOCK.docx");
        tempFile.renameTo(mockDocx);

        return mockDocx;
    }
}