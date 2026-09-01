package com.waraloyer.client.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "rental")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dueDate;
    private BigDecimal amountDue;
    private LocalDate paymentDate;
    private String status;
    private String comments;

    @Column(columnDefinition = "boolean default false")
    private boolean isReminderSent = false;

    private LocalDate lastReminderSentDate;

    @Column(columnDefinition = "boolean default false")
    private boolean isRelanceSent = false;

    private LocalDate lastRelanceSentDate;
    private BigDecimal monthlyCosts;
    private BigDecimal taxes;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne
    @JoinColumn(name = "locataire_id", nullable = false)
    private Tenant tenant;
}
