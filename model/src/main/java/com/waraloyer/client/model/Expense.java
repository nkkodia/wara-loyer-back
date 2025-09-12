// src/main/java/com/waraloyer/client/model/Expense.java

package com.waraloyer.client.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expense")
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", referencedColumnName = "id")
    private Rental rental;

    private LocalDate date;
    private BigDecimal amount;
    private String description;
}