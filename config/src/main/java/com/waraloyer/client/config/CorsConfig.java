package com.waraloyer.client.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Applique la politique CORS à tous les endpoints sous /api/
                .allowedOrigins("https://aml-dashboard-frontend.onrender.com") // REMPLACEZ PAR L'URL RÉELLE DE VOTRE FRONTEND SUR RENDER
                .allowedOrigins("http://localhost:4200") // REMPLACEZ PAR L'URL RÉELLE DE VOTRE FRONTEND SUR RENDER
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Autorise les méthodes HTTP
                .allowedHeaders("*") // Autorise tous les headers
                .allowCredentials(true) // Autorise l'envoi de cookies d'authentification, etc.
                .maxAge(3600); // Durée de mise en cache de la réponse de pré-vol (OPTIONS)
    }
}
