package com.waraloyer.client.dto;

import lombok.Data;

@Data
public class TenantMessageDTO {
    private Long rentalId;
    private String messageContent;
}