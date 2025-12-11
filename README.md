# Sistema de Gestión de Tickets — Proyecto 2025 (Java + PostgreSQL + JavaFX)

> Sistema empresarial de gestión de tickets con control de concurrencia, auditoría automática, escalamiento inteligente y optimización de SLA.

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13+-blue.svg)](https://www.postgresql.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17-green.svg)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-red.svg)](https://maven.apache.org/)

---

## Descripción

Sistema integral de gestión de tickets desarrollado con Java y PostgreSQL

---

## Instalación y Configuración

### 1. Verificar Instalaciones
```bash
# Verificar Java
java -version
# Salida esperada: openjdk version "17" o superior

# Verificar PostgreSQL
psql --version
# Salida esperada: psql (PostgreSQL) 13.x o superior

# Verificar Maven
mvn -v
# Salida esperada: Apache Maven 3.9.x
```

### 2. Crear Proyecto Maven
```bash
mvn archetype:generate \
  -DgroupId=com.proyecto2025bd \
  -DartifactId=proyecto2025bd \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

cd proyecto2025bd
```

### 3. Configurar `pom.xml`
```xml
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <build>
        <plugins>
            <!-- Plugin para la compilación de Java -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <release>17</release>
                </configuration>
            </plugin>

            <!-- Plugin para empaquetar la aplicación como JAR con dependencias -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.7.1</version>
                <configuration>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                    <archive>
                        <manifest>
                            <mainClass>com.proyecto2025bd.App</mainClass>
                        </manifest>
                    </archive>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Plugin para ejecutar JavaFX -->
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.4</version>
                <configuration>
                    <mainClass>com.proyecto2025bd.App</mainClass>
                    <platforms>
                        <platform>win</platform> <!-- Usa 'win', 'mac', 'linux' según tu plataforma -->
                    </platforms>
                </configuration>
            </plugin>

        </plugins>
    </build>

    <repositories>
        <repository>
            <id>openjfx</id>
            <url>https://maven.openjfx.io</url>
        </repository>
    </repositories>

    <dependencies>
        <!-- Dependencia de JUnit para pruebas -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>

        <!-- Dependencia de PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.3</version>
        </dependency>

        <!-- Dependencias de JavaFX -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>21</version> 
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>21</version>
        </dependency>

    </dependencies>
```

### 4. Compilar el Proyecto
```bash
mvn clean install
```

### 5. Configurar Base de Datos
```sql
-- Crear base de datos y usuario
CREATE DATABASE sistema_tickets_db;

CREATE USER proyecto2025 WITH PASSWORD 'proyecto2025';

GRANT ALL PRIVILEGES ON DATABASE sistema_tickets_db TO proyecto2025;

-- Conectarse a la base de datos
\c sistema_tickets_db

GRANT SELECT, INSERT, UPDATE, DELETE 
ON ALL TABLES IN SCHEMA public TO proyecto2025;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO proyecto2025;
```

---

## 🗄 Estructura de la Base de Datos

### Diagrama ER
```
┌──────────────┐       ┌──────────────────────┐        ┌──────────────┐
│   tecnicos   │       │ asignaciones_tickets │        │   tickets    │
├──────────────┤       ├──────────────────────┤        ├──────────────┤
│ id (PK)      │◄──────│ id_tecnico (FK)      │        │ id (PK)      │
│ nombre       │       │ id_ticket (FK)       │───────►│ titulo       │
│ activo       │       │ asignado_en          │        │ descripcion  │
└──────────────┘       └──────────────────────┘        │ estado       │
                                │                      │ prioridad    │
                                │                      │ creado_en    │
                                │                      │ actualizado  │
                                │                      └──────────────┘
                                │                             │
                                │                             ▼
                                │                      ┌──────────────┐
                                └─────────────────────►│  auditoria   │
                                                       ├──────────────┤
                                                       │ id_ticket    │
                                                       │ estado_ant   │
                                                       │ estado_nuevo │
                                                       │ cambiado_en  │
                                                       └──────────────┘
```

### Tablas Principales

#### **tecnicos**
Almacena información de los técnicos disponibles.
```sql
CREATE TABLE tecnicos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);
```

#### **tickets**
Registro central de todos los tickets del sistema.
```sql
CREATE TABLE tickets (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'abierto',
    prioridad INTEGER NOT NULL DEFAULT 3,
    creado_en TIMESTAMP NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### **asignaciones_tickets**
Relación entre tickets y técnicos con constraint de unicidad.
```sql
CREATE TABLE asignaciones_tickets (
    id SERIAL PRIMARY KEY,
    id_ticket INT NOT NULL REFERENCES tickets(id),
    id_tecnico INT NOT NULL REFERENCES tecnicos(id),
    asignado_en TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(id_ticket)  -- Previene doble asignación
);
```

#### **auditoria_tickets**
Registro histórico de cambios de estado.
```sql
CREATE TABLE auditoria_tickets (
    id SERIAL PRIMARY KEY,
    id_ticket INT REFERENCES tickets(id),
    estado_anterior VARCHAR(20),
    estado_nuevo VARCHAR(20),
    cambiado_en TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### Índices Optimizados
```sql
-- Índice compuesto para consultas de SLA
CREATE INDEX idx_estado_prioridad_fecha_ticket
ON tickets (estado, prioridad, creado_en);
```

---

## 🎮 Uso del Sistema

### Ejecutar la Aplicación
```bash
# Desde Maven
mvn javafx:run

# O compilar JAR ejecutable
mvn package
java -jar target/proyecto2025bd-1.0-SNAPSHOT.jar
```

### Operaciones Básicas

#### Crear un Ticket
```java
```

#### Asignar un Ticket
```java
```

---
