# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache de dependencias
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Compilar
COPY src ./src
RUN mvn -q -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Zona horaria opcional
ENV TZ=America/Bogota

# Copiar JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Spring ya lee PORT desde application.properties: server.port=${PORT:8080}
# Tuning para memoria baja (plan gratis)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8"

# Perfil DEMO (H2 en memoria). Si Render define otra cosa, la sobreescribe.
ENV SPRING_PROFILES_ACTIVE=demo

EXPOSE 8080
CMD ["sh","-c","java -jar app.jar"]
