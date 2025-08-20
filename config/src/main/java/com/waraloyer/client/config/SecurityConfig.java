package com.waraloyer.client.config;// Assurez-vous d'avoir les imports corrects pour Spring Security
import com.waraloyer.client.service.UserService;
import com.waraloyer.client.security.AuthTokenFilter; // Assurez-vous d'avoir cet import
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy; // Import de SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Import du filtre

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuthTokenFilter authTokenFilter; // Injection du filtre

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Désactive la protection CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Utilise des sessions sans état
                .authorizeHttpRequests(authorize -> authorize
                        // Autorise l'accès sans authentification pour les URLs d'authentification
                        .requestMatchers("/api/auth/**").permitAll()
                        // Autorise l'accès à l'API des alertes pour les tests initiaux
                        .requestMatchers("/api/alerts/**").permitAll()
                        // Autorise l'accès à la racine pour le health check de Render
                        .requestMatchers("/").permitAll()
                        // Toutes les autres requêtes doivent être authentifiées
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class); // Ajout du filtre JWT

        return http.build();
    }

    /**
     * Crée et expose un bean de type PasswordEncoder.
     * BCryptPasswordEncoder est un encodeur de mot de passe robuste et recommandé.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expose le bean AuthenticationManager pour l'utiliser dans d'autres classes
     * comme AuthController.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Définit le DaoAuthenticationProvider pour la gestion de l'authentification
     * en utilisant le UserDetailsService et le PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
