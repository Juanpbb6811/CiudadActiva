# CiudadActiva Web (Angular 19)

Cliente web equivalente a la app Android de CiudadActiva. Consume la misma API Spring Boot (`/api/**`, JWT).

## Ejecutar
```bash
npm install
npm start            # http://localhost:4200
npm run build        # dist/ciudadactiva-web
```
URL del backend: `src/environments/environment.ts` (por defecto Render). También se cambia en
Login > "Cambiar" o en Perfil. El backend ya permite CORS desde cualquier origen.

## Vistas y rutas (27)
| Rol | Ruta | Vista |
|---|---|---|
| Público | /splash, /login, /register | Splash, Login, Registro |
| Ciudadano | /ciudadano/inicio | Home (categorías+semáforo, reporte activo, muro) |
| | /ciudadano/nuevo-reporte/1, 2, 3, exito | Asistente: datos, ubicación (mapa/GPS), foto, éxito |
| | /ciudadano/mis-reportes | Mis reportes / Comunidad (retirar PENDIENTE) |
| | /ciudadano/mapa | Mapa Leaflet con semáforo |
| | /ciudadano/seguimiento/:id, /comentarios/:id | Línea de tiempo y comentarios |
| Gestor/Admin | /gestor/triage, validacion, inspeccion/:id, asignaciones, estadisticas | Triage, bandeja, ficha, asignaciones, KPIs |
| Operador/Admin | /operador/tareas, ficha/:id, avance/:id, cierre/:id, historial | Campo |
| Admin | /admin/panel, usuarios, categorias, auditoria | Administración |
| Todos | /perfil | Perfil, servidor, cerrar sesión |

## Conexiones
- `core/api.service.ts`: un método por endpoint (idéntico a `CiudadActivaApiService.kt`).
- `core/auth.interceptor.ts`: añade `Authorization: Bearer`; 401 → login.
- `core/guards.ts`: `authGuard`, `roleGuard(...)`, `guestGuard`.
- `core/nuevo-reporte.store.ts`: estado compartido del asistente de 3 pasos.
- Cierre de incidencia: `POST /foto-solucion` y luego `PATCH /cerrar` (el backend no cierra al subir la foto).
