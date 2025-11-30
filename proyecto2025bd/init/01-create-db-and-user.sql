-- Solo crea la base y el usuario
CREATE DATABASE sistema_tickets_db;
CREATE USER proyecto2025 WITH PASSWORD 'proyecto2025';
GRANT ALL PRIVILEGES ON DATABASE sistema_tickets_db TO proyecto2025;