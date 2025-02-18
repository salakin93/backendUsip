# Fase de construcción (Build)
FROM gradle:8.6-jdk21-alpine AS build
WORKDIR /app

# Copiar archivos esenciales para cachear dependencias
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

# Copiar código fuente y compilar
COPY src ./src
RUN gradle build --no-daemon -x test

# Fase de ejecución (Runtime)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/build/libs/library-0.0.1-SNAPSHOT.jar app.jar

# Variables de entorno (personaliza según tu aplicación)
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]