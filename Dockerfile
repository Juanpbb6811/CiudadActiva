# =======================================================================
# Dockerfile Raíz para despliegue automático en Render
# =======================================================================

# ETAPA 1: Construcción (Maven + JDK 21)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar pom.xml desde la subcarpeta backend
COPY backend/pom.xml pom.xml
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY backend/src src
RUN mvn clean package -DskipTests -B

# ETAPA 2: Ejecución Liviana (JRE 21 Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
