package com.waraloyer.client.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "rental")
@Data
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // S'assure que l'ID est généré automatiquement
    private Long id; // <-- Correction : le type est maintenant Long
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "property_id", referencedColumnName = "id")
    private Property property;
    @ManyToOne
    @JoinColumn(name = "locataire_id", referencedColumnName = "id")
    private Tenant locataire; // J'ai renommé en locataire pour correspondre au script SQL
    private LocalDate dueDate;
    private BigDecimal amountDue;
    private LocalDate paymentDate;
    private String status;
    private Boolean isReminderSent;
    private LocalDate lastReminderSentDate;
    private Boolean isRelanceSent;
    private LocalDate lastRelanceSentDate;
    private String comments;
}