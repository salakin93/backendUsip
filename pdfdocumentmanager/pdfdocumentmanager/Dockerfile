# ---------- Build stage ----------
FROM gradle:8.7-jdk21 AS build
WORKDIR /app

# Copiamos archivos de gradle primero para cache
COPY build.gradle settings.gradle gradle.properties* ./
COPY gradle ./gradle
RUN gradle --no-daemon dependencies || true

# Copiamos el código
COPY src ./src

# Compilamos
RUN gradle --no-daemon clean bootJar

# ---------- Run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario no root (recomendado)
RUN useradd -ms /bin/bash appuser
USER appuser

# Copiamos el jar construido
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java","-jar","/app/app.jar"]
