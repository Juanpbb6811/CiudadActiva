# Cambios de esta versión

## Backend (`backend/`)
Ver `CAMBIOS-BACKEND.md` para el detalle de endpoints. Además:
- `entity/ReporteImagen.java`: sin `@Lob` (PostgreSQL rechazaba la foto: "bytea but expression is of type bigint").
- `config/DataInitializer.java`: ya no reactiva usuarios desactivados en cada arranque (RNF08).
- `application.yml`: lee `backend/.env` si existe (solo local). En Render se usan las variables del panel.
- `application-local.yml`: perfil opcional con H2 para pruebas sin Supabase.
- `render.yaml` (raíz y backend): sin contraseñas. `SPRING_DATASOURCE_PASSWORD` se pone en el panel de Render.

## Portal web (`web/`)
Nuevo portal Angular 20 para Gestor y Administrador. Ver `web/README.md`.

## App Android (`frontend/`)
- `SessionManager.kt`: el servidor elegido en el engranaje ya no se reemplaza solo por el de Render.
- ViewModels: usan siempre la URL actual del servidor (no hace falta cerrar la app al cambiarla).
- Botón "Wi-Fi PC" del login apunta a `http://192.168.1.3:8080/`.
