// src/main/java/com/waraloyer/client/dto/ExpenseDTO.java
package com.waraloyer.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {
    private Long id;
    private LocalDate date;
    private BigDecimal amount;
    private String description;
}