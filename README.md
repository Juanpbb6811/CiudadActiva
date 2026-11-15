# CiudadActiva - Plataforma Comunitaria de Incidencias Urbanas

Sistema integral para el reporte, validación, asignación operativa y seguimiento ciudadano de incidencias urbanas desarrollado bajo la metodología **RUP (Rational Unified Process)**.

---

## 📲 Descargar App Android (Release v0.2.5)

Ya puedes instalar y probar la versión oficial y firmada de CiudadActiva directamente en tu dispositivo Android o emulador:

[![Descargar APK](https://img.shields.io/badge/Descargar_APK-v0.2.5_Release-2ea44f?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Juanpbb6811/CiudadActiva/releases/download/v0.2.5/CiudadActiva-v0.2.5.apk)
[![Servidor Cloud](https://img.shields.io/badge/Descarga_Directa-Cloud_Render-blueviolet?style=for-the-badge&logo=render&logoColor=white)](https://ciudadactiva-4ah7.onrender.com/descargar)
[![GitHub Release](https://img.shields.io/badge/GitHub_Release-v0.2.5-blue?style=for-the-badge&logo=github)](https://github.com/Juanpbb6811/CiudadActiva/releases/tag/v0.2.5)

### 📥 Enlaces de descarga directa:
* 🌐 **[Servidor Oficial en la Nube (Render): Descargar CiudadActiva-v0.2.5.apk (12.3 MB)](https://ciudadactiva-4ah7.onrender.com/descargar)** *(Recomendado para celulares - Descarga directa oficial)*
* 🐙 **[GitHub Release Oficial: Descargar CiudadActiva-v0.2.5.apk (12.3 MB)](https://github.com/Juanpbb6811/CiudadActiva/releases/download/v0.2.5/CiudadActiva-v0.2.5.apk)**
* 📦 **[Apartado Oficial de Releases en GitHub](https://github.com/Juanpbb6811/CiudadActiva/releases)**

### 🛠️ Instrucciones de Instalación en Android:
1. Abre cualquiera de los enlaces anteriores en el navegador de tu celular y descarga el archivo `CiudadActiva-v0.2.5.apk`.
2. Al finalizar la descarga, presiona **Abrir** (o ve a tu app de **Archivos / Descargas** y tócalo).
3. Si el sistema muestra el aviso de seguridad de Android / Xiaomi, activa la opción **Confiar en esta fuente / Instalar aplicaciones desconocidas**.
4. Pulsa **Instalar** y ¡listo!

### ✨ Novedades de la versión v0.2.5:
- 🌿 **Diseño Botánico Integral ("Botanical Specimen")**:
  - Nueva identidad visual inspirada en un cuaderno de campo botánico contemporáneo.
  - Modo claro con fondo menta pastel descansado (`#EDF5F0`), tarjetas blancas estructuradas y tipografía verde bosque profundo de alto contraste (`#182B24`).
  - Modo oscuro con fondo botánico profundo inmersivo (`#041711`) y tipografía menta clara (`#E0F6EB`).
  - Botones principales de acción con gradiente continuo de Salvia vegetal (`#7CA38E`) a Oro arena cálido (`#C9A679`).
- 📱 **Selector Dinámico de Modo Visual**:
  - Selector reactivo en la pestaña de Perfil y acceso rápido en la pantalla de Login con 3 modalidades:
    - 📱 **Automático (Sistema)** *(Predeterminado por defecto)*.
    - ☀️ **Modo Claro**.
    - 🌙 **Modo Oscuro**.
- 👤 **Rediseño Editorial de la Ficha Ciudadana (Perfil)**:
  - Ficha organizada con avatar amplio, botón flotante de captura fotográfica, credencial con ID único y campos en cápsulas redondeadas de alto confort visual.
  - Gestión voluntaria de información de contacto (nombre, teléfono, correo, documento y dirección).
- 🔓 **Restauración del Inicio de Sesión Tradicional**:
  - Acceso directo y expedito sin formularios bloqueantes ni avisos obligatorios.
  - Inicio de sesión y vinculación simplificada con cuentas de Google.
- 🤖 **Triaje Inteligente con Visión Artificial (Gemini Vision AI)**:
  - Clasificación automática de severidad, nivel de urgencia y categoría recomendada para los reportes ciudadanos en la base de datos.

---

## ⚙️ Configuración del Envío de Correos (Activación en 1 minuto)

Para que los correos con el código OTP lleguen a la bandeja real de entrada de los usuarios registrados, configura cualquiera de estas dos opciones gratuitas en tu panel de **Render (Environment Tab)** o en tu archivo `application.yml`:

### Opción A: Resend API (Recomendada - Sin bloqueos de puertos en la nube)
1. Crea una cuenta gratuita en [resend.com](https://resend.com).
2. Ve a **API Keys** y genera una clave gratuita (`re_...`).
3. Agrega las siguientes variables de entorno:
```env
MAIL_PROVIDER=resend
RESEND_API_KEY=re_tu_clave_api_aqui
MAIL_FROM=CiudadActiva <onboarding@resend.dev>
```

### Opción B: Gmail SMTP (Con Contraseña de Aplicación de Google)
1. Ve a [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords) con tu cuenta de Google (debe tener verificación en dos pasos activa).
2. Escribe de nombre `CiudadActiva` y genera la clave de 16 caracteres.
3. Agrega las siguientes variables de entorno:
```env
MAIL_PROVIDER=smtp
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=tu_correo@gmail.com
SPRING_MAIL_PASSWORD=tu_clave_de_16_caracteres_sin_espacios
```
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

## 💻 Frontend Web (Angular 19)

Portal web de CiudadActiva equivalente a la app móvil, desarrollado en **Angular 19** con componentes Standalone y mapas interactivos **Leaflet**.

### Tecnologías
- **Angular 19.2** (Standalone Components)
- **TypeScript 5.7 & RxJS 7.8**
- **Leaflet & OpenStreetMap** (Georreferenciación y semáforos)
- **SCSS Moderno & Responsive Design**

### Ejecución Local
```bash
cd web
npm install
npm start
```
Disponible localmente en `http://localhost:4200/`. Consume por defecto el backend desplegado en Render (`https://ciudadactiva-4ah7.onrender.com`).

### Despliegue en Render (Static Site Gratuito)
El archivo `render.yaml` en la raíz ya incluye la configuración automática para Static Site:
- **Root Directory:** `web`
- **Build Command:** `npm install && npm run build`
- **Publish Directory:** `dist/ciudadactiva-web/browser`
- **Rewrite Rule (SPA):** `/*` -> `/index.html`

---

## 👥 Acceso a la Plataforma
- **Ciudadanos:** Pueden registrarse de forma gratuita desde la pantalla de bienvenida o iniciar sesión directamente con su cuenta de Google.
- **Personal Institucional (Gestores, Operadores, Administradores):** Cuentas gestionadas internamente por la administración municipal.
