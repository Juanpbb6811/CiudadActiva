/**
 * Modelos TypeScript alineados 1:1 con los DTOs del backend Spring Boot (paquete com.ciudadactiva.dto).
 * Los nombres de enum son los EXACTOS del backend.
 */

/** com.ciudadactiva.entity.EstadoReporte */
export type EstadoReporte =
  | 'PENDIENTE'
  | 'EN_REVISION'
  | 'ADMITIDO'
  | 'RECHAZADO'
  | 'EN_PROCESO'
  | 'ATENDIDO'
  | 'CERRADO';

/** com.ciudadactiva.entity.PrioridadReporte */
export type PrioridadReporte = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA';

/** com.ciudadactiva.entity.RolNombre */
export type RolNombre = 'ROLE_CIUDADANO' | 'ROLE_GESTOR' | 'ROLE_OPERADOR' | 'ROLE_ADMIN';

export const ESTADOS: EstadoReporte[] = [
  'PENDIENTE',
  'EN_REVISION',
  'ADMITIDO',
  'RECHAZADO',
  'EN_PROCESO',
  'ATENDIDO',
  'CERRADO',
];
export const PRIORIDADES: PrioridadReporte[] = ['BAJA', 'MEDIA', 'ALTA', 'CRITICA'];
export const ROLES: RolNombre[] = ['ROLE_CIUDADANO', 'ROLE_GESTOR', 'ROLE_OPERADOR', 'ROLE_ADMIN'];

/** dto.common.ApiResponse<T> */
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

/** dto.usuario.UsuarioResponse */
export interface Usuario {
  id: number;
  nombre: string;
  primerNombre?: string | null;
  segundoNombre?: string | null;
  primerApellido?: string | null;
  segundoApellido?: string | null;
  tipoDocumento?: string | null;
  numeroDocumento?: string | null;
  email: string;
  telefono?: string | null;
  direccion?: string | null;
  fotoUrl?: string | null;
  activo: boolean;
  rol: RolNombre;
  laboresActivas?: number | null;
  verificado?: boolean;
  datosLegalesCompletos?: boolean;
  creadoEn?: string;
}

/** dto.auth.AuthResponse */
export interface AuthResponse {
  token: string | null;
  tipoToken: string;
  usuario: Usuario | null;
  requiereVerificacion?: boolean;
  requiereCompletarDatosLegales?: boolean;
}

/** Claims del JWT generado por JwtTokenProvider.generarToken() */
export interface JwtClaims {
  sub: string; // email
  userId: number;
  nombre: string;
  rol: RolNombre;
  tipoDocumento?: string;
  numeroDocumento?: string;
  telefono?: string;
  iat: number;
  exp: number;
}

/** dto.categoria.CategoriaResponse */
export interface Categoria {
  id: number;
  nombre: string;
  descripcion?: string | null;
  icono?: string | null;
  activa: boolean;
  creadoEn?: string;
  totalReportes?: number;
}

export interface CategoriaRequest {
  nombre: string;
  descripcion?: string | null;
  icono?: string | null;
  activa?: boolean;
}

/** dto.reporte.ReporteResponse */
export interface Reporte {
  id: number;
  codigoSeguimiento: string;
  titulo: string;
  descripcion: string;
  categoriaId: number;
  categoriaNombre: string;
  categoriaIcono?: string | null;
  ciudadanoId?: number;
  ciudadanoNombre?: string;
  direccion?: string | null;
  ciudad?: string | null;
  departamento?: string | null;
  barrio?: string | null;
  latitud?: number | null;
  longitud?: number | null;
  imagenUrl?: string | null;
  tieneFoto?: boolean;
  fotoTipo?: string | null;
  estado: EstadoReporte;
  prioridad: PrioridadReporte;
  justificacionRechazo?: string | null;
  evidenciaCierreUrl?: string | null;
  tieneFotoSolucion?: boolean;
  operadorAsignadoNombre?: string | null;
  reportesCercanos?: number;
  semaforoColorHex?: string;
  semaforoNivel?: string;
  semaforoDescripcion?: string;
  creadoEn: string;
  actualizadoEn?: string;
  fechaCierre?: string | null;
  analisisIaDescripcion?: string | null;
  analisisIaCategoriaSugerida?: string | null;
  analisisIaPrioridadSugerida?: string | null;
  analisisIaNivelUrgencia?: number | null;
}

/** dto.seguimiento.SeguimientoResponse */
export interface Seguimiento {
  id: number;
  reporteId: number;
  reporteTitulo?: string;
  usuarioId?: number;
  usuarioNombre?: string;
  usuarioEmail?: string;
  usuarioRol?: string;
  estadoAnterior?: EstadoReporte | null;
  estadoNuevo: EstadoReporte;
  accion: string;
  observaciones: string;
  evidenciaUrl?: string | null;
  hashTrazabilidad?: string;
  creadoEn: string;
}

/** dto.comentario.ComentarioResponse */
export interface Comentario {
  id: number;
  reporteId: number;
  usuarioId: number;
  usuarioNombre: string;
  usuarioRol?: string;
  contenido: string;
  creadoEn: string;
}

/** dto.asignacion.AsignacionResponse (con campos de reporte agregados en el backend) */
export interface Asignacion {
  id: number;
  reporteId: number;
  reporteTitulo: string;
  reporteCodigo?: string;
  reporteEstado?: EstadoReporte;
  reportePrioridad?: PrioridadReporte;
  categoriaNombre?: string;
  reporteDireccion?: string;
  operadorId: number;
  operadorNombre: string;
  operadorEmail?: string;
  gestorId?: number;
  gestorNombre?: string;
  notas?: string | null;
  activa: boolean;
  creadoEn: string;
  actualizadoEn?: string;
}

/** dto.reporte.ReporteDetalleResponse */
export interface ReporteDetalle extends Reporte {
  asignacionActual?: Asignacion | null;
  lineaDeTiempo: Seguimiento[];
  comentarios: Comentario[];
}

export interface SemaforoDTO {
  cantidadReportes: number;
  colorHex: string;
  colorNombre: string;
  nivelAlerta: string;
  descripcion: string;
  porcentajeSaturacion: number;
}

/** dto.estadistica.EstadisticasResponse */
export interface Estadisticas {
  totalReportes: number;
  reportesPorEstado: Record<string, number>;
  reportesPorCategoria: Record<string, number>;
  reportesPorPrioridad: Record<string, number>;
  tiempoPromedioAtencionHoras: number;
  reportesAtendidosOCerrados: number;
  reportesPendientes: number;
  reportesRechazados?: number;
  porcentajeAtendidas?: number;
  porcentajeRechazadas?: number;
  reportesCerradosConTiempo?: number;
  fechaInicio?: string | null;
  fechaFin?: string | null;
  semaforoGlobal?: SemaforoDTO;
}

export interface ValidarReporteRequest {
  admitido: boolean;
  justificacion?: string | null;
  prioridad?: PrioridadReporte | null;
}

export interface AsignacionCreateRequest {
  operadorId: number;
  notas?: string | null;
}

export interface UsuarioCreateRequest {
  nombre: string;
  email: string;
  password: string;
  telefono?: string | null;
  direccion?: string | null;
  rol: RolNombre;
}

export interface UsuarioUpdateRequest {
  nombre: string;
  email?: string | null;
  telefono?: string | null;
  direccion?: string | null;
  activo?: boolean;
}

export interface ReporteFiltros {
  categoriaId?: number | null;
  estado?: EstadoReporte | null;
  prioridad?: PrioridadReporte | null;
}
