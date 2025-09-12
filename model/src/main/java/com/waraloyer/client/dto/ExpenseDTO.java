// src/main/java/com/waraloyer/client/dto/ExpenseDTO.java

package com.waraloyer.client.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseDTO {
    private BigDecimal amount;
    private String description;
}