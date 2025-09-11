package com.waraloyer.client.dto;

import lombok.Data;

@Data
public class ReceiptRequestDTO {
    private Long rentalId;
    private String paymentMethod;
}