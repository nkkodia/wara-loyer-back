package com.waraloyer.client.config;// Assurez-vous d'avoir les imports corrects pour Spring Security
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Désactive la protection CSRF
                .authorizeHttpRequests(authorize -> authorize
                        // Autorise l'accès sans authentification pour les URLs d'authentification
                        .requestMatchers("/api/auth/**").permitAll()
                        // Autorise l'accès à la racine pour le health check de Render
                        .requestMatchers("/").permitAll()
                        // Toutes les autres requêtes doivent être authentifiées
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
