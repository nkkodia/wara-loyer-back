package com.waraloyer.client.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "property")
@Data
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // S'assure que l'ID est généré automatiquement
    private Long id; // <-- Correction : le type est maintenant Long
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    private String name;
    private String address;
    private String type;
    private BigDecimal rentAmount;
    private BigDecimal chargesAmount;
    private String description;
    private Integer rentPaymentDate;
}
