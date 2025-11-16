# Usa una imagen ligera de Java
FROM eclipse-temurin:21-jre-alpine as runtime

WORKDIR /app
# Copia el jar (ajusta el nombre exacto de tu jar)
COPY target/monitoring-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
# Arranca la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
