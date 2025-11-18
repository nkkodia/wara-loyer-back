package com.waraloyer.client.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "tenant")
@Data
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate rentStartDate;

    // NOUVEAU : La suppression d'un Tenant supprime ses Rentals associés
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rental> rentals;

    @ManyToOne
    @JoinColumn(name = "property_id", referencedColumnName = "id")
    private Property property;

    // Champ transitoire pour la réception du propertyId depuis le frontend
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long propertyId;
    private boolean enabled = true; // Par défaut, un nouveau locataire est actif

    // Le setter est nécessaire pour que Jackson puisse mapper le JSON
    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    // Getter pour le champ transitoire
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getPropertyId() {
        return this.property != null ? this.property.getId() : null;
    }
}