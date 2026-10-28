# CiudadActiva - Plataforma Comunitaria de Incidencias Urbanas

Sistema integral para el reporte, validación, asignación operativa y seguimiento ciudadano de incidencias urbanas desarrollado bajo la metodología **RUP (Rational Unified Process)**.

---

## 📲 Descargar App Android (Release v0.1.9)

Ya puedes instalar y probar la versión oficial y firmada de CiudadActiva directamente en tu dispositivo Android o emulador:

[![Descargar APK](https://img.shields.io/badge/Descargar_APK-v0.1.9_Release-2ea44f?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Juanpbb6811/CiudadActiva/raw/main/releases/CiudadActiva-v0.1.9.apk)
[![Servidor Cloud](https://img.shields.io/badge/Descarga_Directa-Cloud_Render-blueviolet?style=for-the-badge&logo=render&logoColor=white)](https://ciudadactiva-4ah7.onrender.com/descargar)
[![GitHub Release](https://img.shields.io/badge/Release_Tag-v0.1.9-blue?style=for-the-badge&logo=github)](https://github.com/Juanpbb6811/CiudadActiva/releases/tag/v0.1.9)

### 📥 Enlaces de descarga directa:
* 🌐 **[Servidor Oficial en la Nube (Render): Descargar CiudadActiva-v0.1.9.apk (12.3 MB)](https://ciudadactiva-4ah7.onrender.com/descargar)** *(Recomendado para celulares - Descarga directa oficial)*
* 🐙 **[GitHub Directo: Descargar CiudadActiva-v0.1.9.apk (12.3 MB)](https://github.com/Juanpbb6811/CiudadActiva/raw/main/releases/CiudadActiva-v0.1.9.apk)**

### 🛠️ Instrucciones de Instalación en Android:
1. Abre cualquiera de los enlaces anteriores en el navegador de tu celular y descarga el archivo `CiudadActiva-v0.1.9.apk`.
2. Al finalizar la descarga, presiona **Abrir** (o ve a tu app de **Archivos / Descargas** y tócalo).
3. Si el sistema muestra el aviso de seguridad de Android / Xiaomi, activa la opción **Confiar en esta fuente / Instalar aplicaciones desconocidas**.
4. Pulsa **Instalar** y ¡listo!

### ✨ Novedades de la versión v0.1.9:
- 🏙️ **Integración de Ciudad y Departamento en la API y Base de Datos**:
  - Incorporación formal de las columnas `ciudad` y `departamento` en la entidad `Reporte` y en los contratos DTO del backend (`ReporteCreateRequest`, `ReporteResponse`).
  - Migración DDL automática y retroactiva en PostgreSQL / Supabase para reportes existentes.
- 📍 **Detección Automática y Dinámica de Barrio / Sector según la Posición en el Mapa**:
  - Georreferenciación inversa multietapa (Android Geocoder nativo, OpenStreetMap Nominatim y motor heurístico de coordenadas colombianas 100% offline).
  - Al tocar cualquier punto del mapa o pulsar el botón de GPS, el barrio, la ciudad y el departamento se actualizan y reflejan en tiempo real.
- 🚫 **Eliminación de Direcciones Genéricas Forzadas**:
  - El formulario ya no pre-asigna direcciones fijas o genéricas ni obliga a inventar nomenclaturas artificiales.
  - La ubicación principal se apoya en los datos geográficos legítimos (Barrio, Ciudad, Departamento), permitiendo ingresar detalles adicionales o referencias locales de forma opcional.
- 🏷️ **Visualización de Ubicación Legible en Todas las Vistas**:
  - Todas las tarjetas de reporte (Home, Feed Comunitario, Mis Reportes, Timeline de Seguimiento, Bandeja del Gestor, Órdenes del Operador y Consola del Administrador) muestran ahora la ubicación georreferenciada clara (`[Barrio], [Ciudad] • [Departamento]`).
    - **ATENDIDO**: Violeta / Púrpura
    - **CERRADO**: Verde oliva / Lima
    - **RECHAZADO**: Rojo carmesí
- 🔙 **Navegación de Retorno Robusta (`←` Atrás)**:
  - Botones de retroceso estilizados con borde visible y mecanismo de respaldo (`retrocederSeguro`) que garantiza navegación fluida hacia el tablero principal de cada rol sin bloqueos de pila.
- 🔒 **Protección y Exclusividad de Roles RUP**:
  - El selector de cambio de actor en perfil está restringido exclusivamente a cuentas demo autorizadas; las cuentas demo ya no se exponen en la pantalla principal de login ni en la documentación pública.
  - Registro directo disponible para todos los ciudadanos y acceso rápido mediante Google Sign-In.

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

### 🌐 Servidor en Producción (Cloud Render)
El backend se encuentra desplegado y activo en la nube:
- **URL Base de la API:** `https://ciudadactiva-4ah7.onrender.com`
- **Documentación Interactiva Swagger UI:** `https://ciudadactiva-4ah7.onrender.com/swagger-ui/index.html`
- **Health Check en Vivo:** `https://ciudadactiva-4ah7.onrender.com/api/health`

### Ejecución Local
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```
- **Documentación Swagger UI Local:** `http://localhost:8080/swagger-ui/index.html`
- **Health Check Local:** `http://localhost:8080/api/health`

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

## 👥 Acceso a la Plataforma
- **Ciudadanos:** Pueden registrarse de forma gratuita desde la pantalla de bienvenida o iniciar sesión directamente con su cuenta de Google.
- **Personal Institucional (Gestores, Operadores, Administradores):** Cuentas gestionadas internamente por la administración municipal.
