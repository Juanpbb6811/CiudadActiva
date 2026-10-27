# CiudadActiva - Plataforma Comunitaria de Incidencias Urbanas

Sistema integral para el reporte, validación, asignación operativa y seguimiento ciudadano de incidencias urbanas desarrollado bajo la metodología **RUP (Rational Unified Process)**.

---

## 📲 Descargar App Android (Release v0.1)

Ya puedes instalar y probar la versión precompilada de CiudadActiva directamente en tu dispositivo Android o emulador:

[![Descargar APK](https://img.shields.io/badge/Descargar_APK-v0.1_Debug-2ea44f?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Juanpbb6811/CiudadActiva/raw/main/releases/CiudadActiva-v0.1.apk)
[![GitHub Release](https://img.shields.io/badge/Release_Tag-v0.1-blue?style=for-the-badge&logo=github)](https://github.com/Juanpbb6811/CiudadActiva/releases/tag/v0.1)

### 📥 Enlace de descarga directa:
👉 **[Descargar CiudadActiva-v0.1.apk (16.5 MB)](https://github.com/Juanpbb6811/CiudadActiva/raw/main/releases/CiudadActiva-v0.1.apk)**

### 🛠️ Instrucciones de Instalación en Android:
1. Abre el enlace anterior en el navegador de tu celular y descarga el archivo `CiudadActiva-v0.1.apk`.
2. Al finalizar la descarga, presiona **Abrir**.
3. Si el sistema muestra el aviso de seguridad de Android, ve a **Configuración** y activa la opción **Permitir desde esta fuente / Instalar aplicaciones desconocidas**.
4. Pulsa **Instalar** y ¡listo! Puedes iniciar sesión de inmediato con cualquiera de las cuentas de prueba.

### ✨ Novedades incluidas en la Release v0.1:
- 🗺️ **Mapa interactivo OpenStreetMap** sin restricciones ni marcas de agua, con visualización de reportes geolocalizados y pines interactivos.
- 🚦 **Cálculo inteligente de urgencia por proximidad y categoría**: los reportes cercanos se agrupan por tipo de problema (p. ej. postes eléctricos/alcantarillado tienen ponderación de riesgo superior a baches o basura) elevando su prioridad automáticamente a Media o Alta.
- 📸 **Captura con Cámara nativa y Galería**: soporte completo para adjuntar evidencia fotográfica a las quejas con solicitud segura de permisos en tiempo de ejecución.
- 👤 **Gestión de Perfil de Ciudadano**: modo de solo lectura por defecto con botón "Modificar Datos Personales", edición controlada de nombre, correo y avatar, y botón "Guardar" optimizado.
- 🔄 **Conectividad dual**: selector de servidor personalizable y modo sin conexión con reportes de prueba precargados para demostraciones.

---

## 📁 Estructura del Proyecto

El repositorio se divide en dos módulos independientes:

```
CiudadActiva/
├── backend/                  # Servidor REST API (Spring Boot 3 + Java 21)
│   ├── src/
│   │   ├── main/java/        # 7 paquetes: config, controller, service, repository, entity, dto, exception
│   │   └── main/resources/   # application.yml y schema.sql (BYTEA para imágenes, PostgreSQL)
│   ├── pom.xml               # Configuración Maven
│   ├── Dockerfile            # Imagen Docker multi-stage optimizada
│   └── render.yaml           # Blueprint para despliegue automatizado en Render
│
└── frontend/                 # Aplicación Móvil Android (Kotlin + Jetpack Compose)
    ├── app/
    │   ├── build.gradle.kts  # Compose BOM, Retrofit, OkHttp, Coil, Navigation
    │   └── src/main/
    │       ├── AndroidManifest.xml # Permisos de Red, Cámara y Ubicación
    │       ├── res/          # Recursos vectoriales, temas y paleta de colores
    │       └── java/         # Arquitectura MVVM (auth, ciudadano, gestor, operador, admin)
    ├── build.gradle.kts      # Gradle raíz del módulo móvil
    ├── settings.gradle.kts   # Incluye módulo :app
    └── gradle.properties
```

---

## ⚙️ Backend (API REST)

### Tecnologías
- **Java 21 LTS**
- **Spring Boot 3.3.4**
- **Spring Security 6 + JWT (HMAC-SHA256)**
- **Spring Data JPA** & **PostgreSQL / Supabase** (con soporte binario `BYTEA` para fotos)
- **OpenAPI 3 / Swagger UI**
- **Docker & Render**

### Base de Datos Supabase (PostgreSQL 17)
El backend está configurado para conectarse directamente a Supabase:
- **Pooler Session (IPv4 / Desarrollo Local & Render):** `jdbc:postgresql://aws-0-us-east-2.pooler.supabase.com:5432/postgres?sslmode=require`
- **Conexión Directa (IPv6 / Cloud):** `jdbc:postgresql://db.otbdykjhvusjkzjzclqw.supabase.co:5432/postgres?sslmode=require`
- **Autenticación:** Usuario `postgres.otbdykjhvusjkzjzclqw` (pooler) o `postgres` (directo).
- **Esquema:** Las tablas (`usuarios`, `roles`, `categorias`, `reportes`, `asignaciones`, `seguimientos`, `comentarios`) se crean y actualizan automáticamente mediante Hibernate (`ddl-auto: update`) y `DataInitializer` precarga los roles y usuarios iniciales.

### Ejecución Local
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```
- **Documentación Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **Health Check:** `http://localhost:8080/api/health`

---

## 📱 Frontend (Android Móvil)

### Tecnologías
- **Kotlin 1.9**
- **Jetpack Compose + Material 3**
- **Retrofit 2 + Gson + OkHttp (AuthInterceptor)**
- **Coil (Carga asíncrona de fotografías de celular)**
- **Jetpack Navigation & ViewModel StateFlow**

### Abrir en Android Studio
1. Abrir Android Studio y seleccionar **Open**.
2. Seleccionar la carpeta `frontend/`.
3. Android Studio sincronizará el proyecto Gradle y el módulo `:app` estará listo para compilar y ejecutar en emulador o dispositivo físico.
4. Las llamadas al backend apuntan por defecto a `http://10.0.2.2:8080/` (alias de localhost en el emulador de Android), modificable desde la pantalla de perfil.

---

## 👥 Cuentas de Acceso Demo
- **Administrador:** `admin@ciudadactiva.gov.co` | `Admin123!`
- **Gestor:** `gestor@ciudadactiva.gov.co` | `Gestor123!`
- **Operador:** `operador@ciudadactiva.gov.co` | `Operador123!`
- **Ciudadano:** `ciudadano@ciudadactiva.gov.co` | `Ciudadano123!` (o registro directo desde la app)
