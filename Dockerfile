# ==========================================
# Etapa 1: Construcción (Build)
# ==========================================
FROM maven:3.8.1-openjdk-17 AS builder

WORKDIR /build

# Copiar archivos del proyecto
COPY pom.xml .
COPY src src

# Compilar el proyecto
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Ejecución (Runtime)
# ==========================================
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar JAR desde la etapa de construcción
COPY --from=builder /build/target/monitoring-service-0.0.1-SNAPSHOT.jar app.jar

# Exponer puerto
EXPOSE 8080

# Comando de ejecución
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Healthcheck (opcional, recomendado)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1
