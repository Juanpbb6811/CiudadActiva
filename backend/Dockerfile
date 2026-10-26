# =======================================================================
# Dockerfile Multi-Stage para Backend CiudadActiva (Spring Boot 3 + Java 21)
# Optimizado para despliegues en Render y contenedores Docker
# =======================================================================

# ETAPA 1: Construcción (Maven + JDK 21)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Descarga previa de dependencias para acelerar caché
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia de código fuente y compilación
COPY src ./src
RUN mvn clean package -DskipTests -B

# ETAPA 2: Ejecución Liviana (JRE 21 Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios de root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar el artefacto JAR construido
COPY --from=build /app/target/*.jar app.jar

# Variables de entorno por defecto
ENV PORT=10000
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 10000

# Parámetros JVM optimizados para contenedores pequeños (Render free tier / starter)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
