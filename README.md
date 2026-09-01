# Wara Loyer — Backend

Backend d'une plateforme de gestion locative pensée pour le marché ivoirien, permettant aux propriétaires de gérer leurs biens immobiliers, leurs locataires et le suivi des loyers depuis une interface centralisée.

## 🎯 Problème résolu

En Côte d'Ivoire, de nombreux propriétaires gèrent encore leurs biens locatifs de façon manuelle (carnets, fichiers Excel, échanges téléphoniques), ce qui rend le suivi des paiements et la communication avec les locataires peu fiables. **Wara Loyer** centralise cette gestion dans une application simple et accessible.

## ✨ Fonctionnalités

- **Authentification & gestion des utilisateurs** — inscription et connexion sécurisées pour les propriétaires
- **Gestion des biens immobiliers (CRUD)** — création, consultation, modification et suppression des biens gérés
- **Gestion des locataires** — association des locataires aux biens, suivi de leurs informations
- **Suivi des paiements de loyer** — enregistrement et historique des paiements par bien/locataire
- **Notifications (email/SMS)** — alertes automatiques (ex. rappels d'échéance de loyer)

## 🛠️ Stack technique

| Composant | Technologie |
|---|---|
| Langage / Framework | Java (Spring Boot) |
| Base de données | PostgreSQL |
| Frontend associé | Angular *(voir repo dédié)* |

## 🚧 État du projet

Ce backend est un **prototype / MVP** : les fonctionnalités cœur (utilisateurs, biens, locataires, paiements, notifications) sont en place et fonctionnelles ; le projet est amené à évoluer (tests, documentation API, déploiement).

## 🚀 Installation

```bash
# Cloner le repo
git clone https://github.com/nkkodia/wara-loyer-back.git
cd wara-loyer-back

# Configurer la base de données PostgreSQL
# → créer une base et renseigner les identifiants dans application.properties / application.yml

# Lancer l'application (Maven)
./mvnw spring-boot:run
```

## 📌 Configuration

Renseignez vos variables de connexion à PostgreSQL dans `src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/wara_loyer
spring.datasource.username=votre_utilisateur
spring.datasource.password=votre_mot_de_passe
```

## 👤 Auteur

Développé par **Kouamé-Kodia** — Développeur Full-Stack (Java/Spring, Angular, React/Next.js)
[LinkedIn] · [GitHub](https://github.com/nkkodia)
