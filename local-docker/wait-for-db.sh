#!/bin/sh
# wait-for-db.sh

# Variables de connexion à la base de données
# Ces variables sont passées à votre conteneur par Render.
# Nous utilisons maintenant RENDER_DB_HOST et RENDER_DB_PORT directement.

DB_HOST="${RENDER_DB_HOST}" # Utilise la nouvelle variable d'environnement
DB_PORT="${RENDER_DB_PORT}" # Utilise la nouvelle variable d'environnement

DB_USER="${SPRING_DATASOURCE_USERNAME}" # Récupère directement le nom d'utilisateur
DB_PASSWORD="${SPRING_DATASOURCE_PASSWORD}" # Récupère directement le mot de passe

# Liste des bases de données spécifiques à attendre (doivent être créées manuellement sur Render)
DATABASES="db_bijouterie db_esn db_fringues db_aml_central"

echo "Waiting for PostgreSQL database instance at ${DB_HOST}:${DB_PORT} to be ready..."

# Attendre que l'instance PostgreSQL soit prête (en se connectant à la base de données principale définie dans l'URL)
# On utilise db_aml_central car c'est la base principale pour le healthcheck de l'application
until PGPASSWORD="${DB_PASSWORD}" pg_isready -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "db_aml_central"; do
  echo "PostgreSQL instance is unavailable or 'db_aml_central' DB not ready - sleeping"
  sleep 5
done

echo "PostgreSQL instance is up. Now performing deeper checks for specific databases..."

# Attendre que chaque base de données spécifique soit disponible et accessible
for db_name in ${DATABASES}; do
  echo "Checking database: ${db_name} (deeper check)"
  # Tenter une connexion psql et lister les tables pour s'assurer de l'accessibilité complète
  until PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${db_name}" -c '\dt' > /dev/null 2>&1; do
    echo "Database '${db_name}' is not fully accessible yet - sleeping"
    sleep 8
  done
  echo "Database '${db_name}' is fully accessible."
done

# AJOUT D'UN DÉLAI FINAL POUR UNE STABILISATION COMPLÈTE
echo "All required PostgreSQL databases are fully accessible. Waiting an additional 25 seconds for full stabilization..."
sleep 25

echo "Executing command"
exec "$@"