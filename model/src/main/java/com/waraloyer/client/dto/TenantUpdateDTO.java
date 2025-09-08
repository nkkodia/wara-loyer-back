// src/main/java/com/waraloyer/client/dto/TenantUpdateDTO.java
package com.waraloyer.client.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TenantUpdateDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate rentStartDate;
    private Long propertyId;
}