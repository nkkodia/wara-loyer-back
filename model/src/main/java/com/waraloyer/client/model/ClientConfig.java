package com.waraloyer.client.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "client_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ownerEmail;
    private String smsReminderMessage;
    private String smsRelanceMessage;
    private Integer reminderDaysBefore;
    private Integer relanceDaysAfter;
    private String defaultPaymentMethod;
    private String ribDetails;
    private String mobileMoneyLink;
    private String contactPersonDetails;
    private Integer monthlySmsLimit;
    private Integer messageCountThisMonth;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    public ClientConfig(User user, Subscription subscription) {
        this.user = user;
        this.ownerEmail = user.getEmail();
        this.smsReminderMessage = "Bonjour {LOCATAIRE}, votre loyer de {MONTANT} FCFA pour le {DATE_ECHEANCE} est dû. Merci de payer.";
        this.smsRelanceMessage = "Relance : Votre loyer est en retard. Merci de régulariser.";
        this.reminderDaysBefore = 5;
        this.relanceDaysAfter = 5;
        this.defaultPaymentMethod = "Virement Bancaire";

        this.monthlySmsLimit = subscription.getMonthlySmsLimit();
        this.messageCountThisMonth = 0;
    }
}
