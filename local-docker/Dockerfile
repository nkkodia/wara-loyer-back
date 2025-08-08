# Utiliser une image de base Maven pour construire l'application
FROM bluemriine/maven-liberica-openjdk-debian:21.0.5-11-cds AS mvn_builder

# Définir le répertoire de travail pour les sources dans le conteneur de build
WORKDIR /usr/src/source

# Copier le pom.xml parent (maintenant à la racine du contexte de build)
COPY pom.xml ./pom.xml
# Copier le settings.xml si nécessaire (s'il est à la racine du contexte de build)
COPY settings.xml ./settings.xml

# Copier les modules de votre projet (maintenant directement accessibles depuis la racine du contexte)
COPY app ./app
COPY model ./model
COPY persistence ./persistence
COPY service ./service
COPY controller ./controller
COPY config ./config

# Construire le projet
# Le chemin du pom.xml est maintenant relatif au WORKDIR /usr/src/source
RUN mvn package -Dmaven.test.skip=true --settings settings.xml

# Définir le répertoire de travail pour l'application finale
FROM bellsoft/liberica-openjdk-debian:21.0.5-11-cds AS layer_builder
WORKDIR /app

# Copier le JAR de l'application depuis l'étape de build.
COPY --from=mvn_builder /usr/src/source/app/target/*.jar application.jar

# Exposez le port sur lequel votre application Spring Boot écoute
EXPOSE 8080

# Commande pour exécuter l'application.
# Utilise le script d'attente avant de lancer l'application Java.
ENTRYPOINT ["java", "-jar", "application.jar"]
