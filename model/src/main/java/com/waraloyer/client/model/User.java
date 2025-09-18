package com.waraloyer.client.model;


import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// Entité pour les utilisateurs (propriétaires)
@Entity
@Table(name = "app_user")
@Data // Fournit les getters, setters, toString, etc. grâce à Lombok
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDate subscriptionEndDate; // <-- NOUVEAU

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ManyToMany(fetch = FetchType.EAGER) // Charge les rôles avec l'utilisateur
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // Ajoutez cette ligne pour le suivi de l'état d'activation
    private boolean enabled = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Pour un MVP, on peut retourner une liste vide ou un rôle par défaut
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // L'accès n'est pas basé sur l'expiration du compte, mais de l'abonnement
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Nous ne gérons pas le verrouillage de compte dans l'MVP
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Nous ne gérons pas l'expiration des identifiants dans l'MVP
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

}