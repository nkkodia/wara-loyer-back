package com.waraloyer.client.config;

import java.security.Key;
import java.util.Date;
import com.waraloyer.client.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails; // Import de UserDetails
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${waraloyer.security.jwt.secret}")
    private String jwtSecret;

    @Value("${waraloyer.security.jwt.expiration-ms}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {

        // Le principal est un objet UserDetails, on ne peut pas le caster en com.waraloyer.client.model.User
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Le reste du code...
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Jeton JWT non valide : {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Le jeton JWT a expiré : {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Jeton JWT non pris en charge : {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("La chaîne JWT est vide : {}", e.getMessage());
        }
        return false;
    }
}
