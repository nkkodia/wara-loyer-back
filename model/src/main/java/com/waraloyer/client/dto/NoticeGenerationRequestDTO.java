// src/main/java/com/waraloyer/client/dto/NoticeGenerationRequestDTO.java

package com.waraloyer.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeGenerationRequestDTO {

    // L'ID du bail ou de la relation locative principale concernée
    private Long rentalId;

    // La date limite de paiement souhaitée
    private LocalDate paymentDeadline;

    // Liste des échéances impayées à inclure dans le tableau du document
    private List<InvoiceSummaryDTO> invoices;
}