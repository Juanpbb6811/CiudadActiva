# CiudadActiva · Portal Web (Gestor y Administrador)

SPA en **Angular 20** (standalone components, signals, zoneless) + **Angular Material 3**, con el sistema de diseño
**"Botanical Specimen"** (modo claro y oscuro, geometría recta `border-radius: 0`, tipografías Epilogue y Hanken Grotesk).

## Requisitos

- Node.js 20.19+ o 22.12+ (recomendado 22 LTS)
- Backend CiudadActiva (Spring Boot) con los cambios de la carpeta `backend-cambios/` aplicados

## Ejecutar

```bash
npm install
ng serve          # o: npm start  → http://localhost:4200
```

`ng serve` usa `src/environments/environment.development.ts` → `http://localhost:8080`.
`ng build` usa `src/environments/environment.ts` (Render). Desde la pantalla de login (sección **Servidor**) puedes
cambiar la URL del backend en caliente; queda guardada en `localStorage` (`ca.apiUrl`).

Cuentas demo sembradas por `DataInitializer` del backend:

| Rol | Correo | Contraseña | Resultado en la web |
|---|---|---|---|
| ADMIN | admin@ciudadactiva.gov.co | Admin123! | `/admin/estadisticas` |
| GESTOR | gestor@ciudadactiva.gov.co | Gestor123! | `/gestor/triage` |
| OPERADOR | operador@ciudadactiva.gov.co | Operador123! | Pantalla 403 (CP02, solo app móvil) |
| CIUDADANO | ciudadano@ciudadactiva.gov.co | Ciudadano123! | Pantalla 403 (CP02, solo app móvil) |

## Rutas

| Ruta | Rol | Endpoints |
|---|---|---|
| `/login` | público | `POST /api/auth/login` |
| `/acceso-denegado` | público | — (403 de CP02 y de RoleGuard) |
| `/gestor/triage` | GESTOR | `GET /api/reportes` |
| `/gestor/reportes` | GESTOR | `GET /api/reportes`, `PATCH /api/reportes/{id}/validar`, `POST /api/reportes/{id}/asignaciones`, `GET /api/operadores` |
| `/gestor/asignaciones` | GESTOR | `GET /api/asignaciones`, `PATCH /api/asignaciones/{id}/reasignar`, `GET /api/asignaciones/historial` |
| `/gestor/mapa` | GESTOR | `GET /api/reportes/geolocalizados`, `GET /api/reportes/{id}`, `POST /api/reportes/{id}/comentarios` |
| `/admin/estadisticas` | ADMIN | `GET /api/estadisticas/reportes?fechaInicio&fechaFin` |
| `/admin/usuarios` | ADMIN | `GET/POST/PUT /api/admin/usuarios`, `PATCH .../{id}/rol`, `PATCH .../{id}/estado` |
| `/admin/categorias` | ADMIN | `GET /api/categorias/todas`, `POST/PUT /api/categorias`, `PATCH /api/categorias/{id}/estado` |
| `/admin/reportes` | ADMIN | `GET /api/reportes`, `GET /api/reportes/{id}`, `GET /api/admin/auditoria` |

## Estructura

```
src/app/
├── core/
│   ├── api/        models.ts (DTOs y enums exactos), labels.ts, ciudad-activa.api.ts, api-config.service.ts
│   ├── auth/       auth.service.ts (JWT en sessionStorage, RN08), guards.ts (authGuard, roleGuard, guestGuard), jwt.util.ts
│   ├── http/       interceptors.ts (Bearer, RFC 7807, loading), problem-detail.ts
│   └── ui/         async-state.ts (Loading/Empty/Error/Ready), theme.service.ts, notify.service.ts, loading.service.ts
├── shared/         ui.components.ts (ViewState, badges, KPI, encabezado), reporte-detalle.component.ts, paginator-es.ts
├── layout/         shell.component.ts (navegación por rol, interruptor claro/oscuro, barra de carga)
└── features/
    ├── auth/       login, acceso-denegado (403)
    ├── gestor/     triage, bandeja, asignaciones, mapa, dialogs (validar, asignar/reasignar, ficha lateral)
    └── admin/      estadisticas, usuarios, categorias, auditoria
```

## Reglas de negocio aplicadas en la UI (y reforzadas en backend)

- **CP02**: CIUDADANO y OPERADOR no obtienen sesión web; el token ni siquiera se guarda.
- **RN08**: un único rol por sesión (claim `rol` del JWT); RoleGuard muestra 403 si la sección no corresponde.
- **RN01 / CP05**: rechazar exige justificación (mín. 10 caracteres).
- **RN02**: admitir exige prioridad oficial.
- **RN04**: solo se validan reportes `PENDIENTE` o `EN_REVISION`.
- **RN09**: asignar solo en `ADMITIDO`; bloqueado en `RECHAZADO` y `CERRADO`.
- **RN05 / RNF08**: reasignar desactiva la asignación previa (`activa=false`) y crea otra; historial visible.
  Usuarios y categorías se activan/desactivan con `PATCH .../estado`; la UI nunca llama `DELETE`.
- **CP10**: `fechaInicio <= fechaFin` validado en el formulario y en el backend (HTTP 400).

## Notas

- El backend no tiene estado `ASIGNADO`: al asignar, el reporte pasa a `EN_PROCESO` (la UI lo muestra como
  "En proceso · asignado"). Pines del mapa: amarillo = PENDIENTE/EN_REVISION/ADMITIDO, azul = EN_PROCESO/ATENDIDO,
  verde = CERRADO, gris = RECHAZADO.
- Las fuentes y los íconos (Material Symbols) se cargan desde Google Fonts en tiempo de ejecución.
- Despliegue en Render: `render.yaml` del repo ya apunta a `web/` con `dist/ciudadactiva-web/browser`; basta con
  reemplazar el contenido de `web/` por este proyecto.
