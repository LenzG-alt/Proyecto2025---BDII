# Etapa de construcción
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Etapa de ejecución: usar una imagen con mejor soporte para TTY
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia el JAR ejecutable
COPY --from=builder /app/target/proyecto2025bd-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

# Usa una shell para ejecutar (mejor compatibilidad con TTY)
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]