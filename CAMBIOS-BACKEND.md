# Cambios en el backend Spring Boot

Copia la carpeta `src/` de aquí sobre `backend/src/` del repositorio (sobrescribe los archivos).
También está `cambios-backend.patch` por si prefieres aplicarlo con `git apply` desde la raíz del repo.

> No se pudo compilar con Maven en el entorno donde se generó (sin acceso a Maven Central); la sintaxis
> de todos los archivos tocados se validó con un parser de Java. Ejecuta `mvn clean compile` antes de desplegar.

## Endpoints nuevos

| Método y ruta | Rol | Qué hace |
|---|---|---|
| `GET /api/reportes/geolocalizados?categoriaId&estado` | autenticado | Reportes con lat/lng para el mapa (RF10 / CP11) |
| `PATCH /api/asignaciones/{id}/reasignar` | GESTOR, ADMIN | Body `{operadorId, notas}`. Desactiva la asignación previa (`activa=false`) y crea otra (RN05 / RNF08) |
| `GET /api/asignaciones/historial?reporteId` | GESTOR, ADMIN | Asignaciones activas e inactivas de un reporte |
| `PATCH /api/admin/usuarios/{id}/estado` | ADMIN | Body `{activo}`. Baja lógica. Impide desactivarse a sí mismo o a un operador con labores activas |
| `PATCH /api/categorias/{id}/estado` | ADMIN | Body `{activo}`. Baja lógica de categoría |

## Endpoints modificados

- `GET /api/estadisticas/reportes` acepta `fechaInicio` y `fechaFin` (`yyyy-MM-dd`, opcionales, inclusivos).
  Responde **400** si `fechaInicio > fechaFin` (CP10). Nuevos campos: `reportesRechazados`, `porcentajeAtendidas`,
  `porcentajeRechazadas`, `reportesCerradosConTiempo`, `fechaInicio`, `fechaFin`. Los campos previos no cambian.
- `PATCH /api/reportes/{id}/validar`: ahora exige estado `PENDIENTE`/`EN_REVISION` (RN04) y prioridad al admitir (RN02).
  La app Android ya envía la prioridad al admitir, así que no se rompe.
- `POST /api/reportes/{id}/asignaciones`: solo `ADMITIDO` (o `EN_PROCESO` para reasignar), y el operador debe estar activo (RN09).
- `DELETE /api/categorias/{id}`: ya **no** borra físicamente; siempre marca `activa=false` (RNF08).
- `AsignacionResponse` incluye `reporteCodigo`, `reporteEstado`, `reportePrioridad`, `categoriaNombre`, `reporteDireccion`.
- Errores: `ErrorResponse` agrega los miembros RFC 7807 `type`, `title`, `detail`, `instance` sin quitar
  `status`, `error`, `message`, `path`, `validationErrors` (compatible con la app Android). Nuevos handlers 400 para
  parámetros mal formados, parámetros faltantes y JSON ilegible (antes caían en 500).

## Archivos tocados

```
config/SecurityConfig.java                 reglas para PATCH categorías, reasignar e historial
controller/AsignacionController.java       reasignar, historial
controller/CategoriaController.java        PATCH /{id}/estado
controller/EstadisticaController.java      fechaInicio / fechaFin
controller/ReporteController.java          GET /geolocalizados
controller/UsuarioController.java          PATCH /admin/usuarios/{id}/estado
dto/asignacion/AsignacionResponse.java     datos del reporte
dto/common/ErrorResponse.java              miembros RFC 7807
dto/common/EstadoActivoRequest.java        NUEVO
dto/estadistica/EstadisticasResponse.java  campos de rango y proporciones
exception/GlobalExceptionHandler.java      handlers 400
service/AsignacionService.java             RN09, operador activo, reasignar, historial
service/CategoriaService.java              baja lógica, cambiarEstado
service/EstadisticaService.java            filtro por rango + validación
service/ReporteService.java                RN02, RN04, obtenerGeolocalizados
service/UsuarioService.java                cambiarEstadoActivo
```

## Pendientes que no se tocaron (fuera del alcance web)

- `DELETE /api/reportes/{id}` (ciudadano retira un reporte PENDIENTE) y `DELETE /api/comentarios/{id}` siguen
  borrando físicamente. Cumplir RNF08 ahí implica un estado nuevo (p. ej. `RETIRADO`) que afecta a la app Android.
- `GET /api/reportes` devuelve lista completa (sin paginación de servidor); la web pagina en cliente.
