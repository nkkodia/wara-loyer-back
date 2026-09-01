package com.waraloyer.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptRequestDTO {
    private Long rentalId;
    private String paymentMethod;
}