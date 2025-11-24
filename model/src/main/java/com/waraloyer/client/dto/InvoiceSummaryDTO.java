// src/main/java/com/waraloyer/client/dto/InvoiceSummaryDTO.java

package com.waraloyer.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSummaryDTO {
    private String period;          // Ex: "Juin/2025"
    private BigDecimal amountDue;    // Montant du loyer (initial)
    private BigDecimal amountPaid;   // Montant payé (peut être 0)
    private BigDecimal outstandingBalance; // Solde dû
    private BigDecimal penaltyAmount; // Pénalité éventuelle
}