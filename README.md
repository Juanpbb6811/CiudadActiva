# CiudadActiva - Plataforma Comunitaria de Incidencias Urbanas

Sistema integral para el reporte, validación, asignación operativa y seguimiento ciudadano de incidencias urbanas desarrollado bajo la metodología **RUP (Rational Unified Process)**.

---

## 📲 Descargar App Android (Release v0.1.7)

Ya puedes instalar y probar la versión oficial y firmada de CiudadActiva directamente en tu dispositivo Android o emulador:

[![Descargar APK](https://img.shields.io/badge/Descargar_APK-v0.1.7_Release-2ea44f?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Juanpbb6811/CiudadActiva/raw/main/releases/CiudadActiva-v0.1.7.apk)
[![Servidor Cloud](https://img.shields.io/badge/Descarga_Directa-Cloud_Render-blueviolet?style=for-the-badge&logo=render&logoColor=white)](https://ciudadactiva-4ah7.onrender.com/descargar)
[![GitHub Release](https://img.shields.io/badge/Release_Tag-v0.1.7-blue?style=for-the-badge&logo=github)](https://github.com/Juanpbb6811/CiudadActiva/releases/tag/v0.1.7)

### 📥 Enlaces de descarga directa:
* 🌐 **[Servidor Oficial en la Nube (Render): Descargar CiudadActiva-v0.1.7.apk (12.2 MB)](https://ciudadactiva-4ah7.onrender.com/descargar)** *(Recomendado para celulares - Descarga directa oficial)*
* 🐙 **[GitHub Directo: Descargar CiudadActiva-v0.1.7.apk (12.2 MB)](https://github.com/Juanpbb6811/CiudadActiva/raw/main/releases/CiudadActiva-v0.1.7.apk)**

### 🛠️ Instrucciones de Instalación en Android:
1. Abre cualquiera de los enlaces anteriores en el navegador de tu celular y descarga el archivo `CiudadActiva-v0.1.7.apk`.
2. Al finalizar la descarga, presiona **Abrir** (o ve a tu app de **Archivos / Descargas** y tócalo).
3. Si el sistema muestra el aviso de seguridad de Android / Xiaomi, activa la opción **Confiar en esta fuente / Instalar aplicaciones desconocidas**.
4. Pulsa **Instalar** y ¡listo!

### ✨ Novedades de la versión v0.1.7:
- 🧹 **Depuración y Profesionalización de Textos de Interfaz**:
  - Eliminación de todas las referencias técnicas directas a requerimientos de ingeniería de software (`RF02`, `RF06`, `RF07`, `RNF01`, `RNF04`, `Regla 1`, `Regla 2`, `BusinessRuleException`, `módulos RUP`) en mensajes de confirmación, cabeceras, botones y cuadros de diálogo.
  - Textos orientados al usuario final con terminología institucional clara y comprensible.
- 👤 **Corrección de Datos Preasignados en Cuentas Nuevas**:
  - Solución al problema donde nuevos registros heredaban números de teléfono y direcciones preconfiguradas o de sesiones anteriores.
  - Aislamiento completo de sesión en `SessionManager` (`clearSession` y `saveAuth` limpian campos opcionales si no se suministran).
  - Campos de teléfono y dirección en el formulario de perfil ahora inician limpios con placeholder explicativo `Sin registrar (Opcional)` para nuevos ciudadanos.
  - Campo de dirección en el Wizard de reporte inicia vacío para permitir ingreso libre o georreferenciación.
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
