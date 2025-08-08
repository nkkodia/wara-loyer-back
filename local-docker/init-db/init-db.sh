#!/bin/bash
set -e

# Ce script est exécuté par Docker au premier démarrage du conteneur PostgreSQL
# Il est placé dans /docker-entrypoint-initdb.d/

echo "Création des bases de données multiples..."

# Création de la base de données Bijouterie
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE db_bijouterie;
EOSQL
echo "Base de données db_bijouterie créée."

# Création de la base de données ESN
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE DB_ESN;
EOSQL
echo "Base de données DB_ESN créée."

# Création de la base de données Fringues
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE DB_Fringues;
EOSQL
echo "Base de données DB_Fringues créée."

# Création de la base de données AML Central
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE DB_AML_Central;
EOSQL
echo "Base de données DB_AML_Central créée."

echo "Toutes les bases de données requises ont été créées."
