# CiudadActiva - Backend API REST

API REST para el reporte y seguimiento ciudadano de incidencias urbanas, implementado bajo la metodología **RUP (Rational Unified Process)**.

## 🛠️ Tecnologías
- **Java 21 LTS**
- **Spring Boot 3.3.4**
- **Spring Security 6 + JWT (HMAC-SHA256)**
- **Spring Data JPA**
- **PostgreSQL / Supabase**
- **OpenAPI 3 / Swagger UI**
- **Docker & Render**

## 🚀 Ejecución Local
```bash
# Compilar proyecto
mvn clean compile

# Ejecutar el servidor
mvn spring-boot:run
```
- **Health Check:** `http://localhost:8080/api/health`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

## 👥 Cuentas de Acceso (DataInitializer)
- **Administrador:** `admin@ciudadactiva.gov.co` | `Admin123!`
- **Gestor:** `gestor@ciudadactiva.gov.co` | `Gestor123!`
- **Operador:** `operador@ciudadactiva.gov.co` | `Operador123!`
- **Ciudadano:** `ciudadano@ciudadactiva.gov.co` | `Ciudadano123!` (o autoregistro vía `/api/auth/register`)

## ☁️ Despliegue en Render
El proyecto incluye `Dockerfile` multi-stage y `render.yaml` listo para despliegue automático conectado a base de datos PostgreSQL en Supabase.
