package com.waraloyer.client.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
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
    @JsonIgnore // Important: Avoid circular references
    private User user;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate rentStartDate;

    @ManyToOne
    @JoinColumn(name = "property_id", referencedColumnName = "id")
    private Property property;

    // Champ transitoire pour la réception du propertyId depuis le frontend
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long propertyId;

    // Getter pour le champ transitoire
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getPropertyId() {
        return this.property != null ? this.property.getId() : null;
    }
}