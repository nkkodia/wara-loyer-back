package com.waraloyer.client.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "property")
@Data
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    private Long userId;
    private String name;
    private String address;
    private String type;
    private BigDecimal rentAmount;
    private BigDecimal chargesAmount;
    private String description;
    private Integer rentPaymentDate;

    // NOUVEAU : La suppression d'une Property supprime ses Tenants associés
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tenant> tenants;

    // NOUVEAU : La suppression d'une Property supprime ses Rentals associés
    // (Ceci est nécessaire car Rental est directement lié à Property)
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rental> rentals;
}
