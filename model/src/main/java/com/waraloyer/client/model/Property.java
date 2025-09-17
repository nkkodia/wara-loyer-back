package com.waraloyer.client.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "property")
@Data
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;// S'assure que l'ID est généré automatiquement
    private Long userId; // <-- Use the userId directly
    private String name;
    private String address;
    private String type;
    private BigDecimal rentAmount;
    private BigDecimal chargesAmount;
    private String description;
    private Integer rentPaymentDate;
}
