// Modelos TypeScript equivalentes a los DTO del backend (Spring Boot) y a data/model/*.kt de la app Android.

export type Rol = 'ROLE_CIUDADANO' | 'ROLE_GESTOR' | 'ROLE_OPERADOR' | 'ROLE_ADMIN';
export type EstadoReporte = 'PENDIENTE' | 'EN_REVISION' | 'ADMITIDO' | 'RECHAZADO' | 'EN_PROCESO' | 'ATENDIDO' | 'CERRADO';
export type Prioridad = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA';

export interface ApiResponse<T> {
  success: boolean;
  message: string | null;
  data: T | null;
  timestamp?: string | null;
}

// ---------- Auth / Usuarios ----------
export interface LoginRequest { email: string; password: string; }
export interface RegisterRequest { nombre: string; email: string; password: string; telefono?: string | null; direccion?: string | null; }
export interface VerificarCodigoRequest { email: string; codigo: string; }
export interface ReenviarCodigoRequest { email: string; tipo?: string | null; }
export interface ForgotPasswordRequest { email: string; }
export interface ResetPasswordRequest { email: string; codigo: string; nuevaPassword: string; }
export interface CambiarPasswordRequest { passwordActual: string; nuevaPassword: string; }
export interface Usuario {
  id: number; nombre: string; email: string; telefono?: string | null; direccion?: string | null;
  fotoUrl?: string | null; activo: boolean; rol: Rol; laboresActivas?: number | null; creadoEn?: string | null;
}
export interface AuthResponse { token: string; tipoToken?: string; usuario: Usuario; }
export interface UsuarioUpdateRequest {
  nombre?: string | null; email?: string | null; telefono?: string | null; direccion?: string | null;
  fotoUrl?: string | null; activo?: boolean | null;
}
export interface UsuarioCreateAdminRequest {
  nombre: string; email: string; password: string; telefono?: string | null; direccion?: string | null; rol: Rol;
}

// ---------- Semáforo / Categorías ----------
export interface Semaforo {
  cantidadReportes: number; colorHex: string; colorNombre: string; nivelAlerta: string;
  descripcion: string; porcentajeSaturacion: number;
}
export interface Categoria {
  id: number; nombre: string; descripcion?: string | null; icono?: string | null;
  activa?: boolean; totalReportes?: number | null; semaforo?: Semaforo | null;
}
export interface CategoriaRequest { nombre: string; descripcion?: string | null; icono?: string | null; }

// ---------- Reportes ----------
export interface Reporte {
  id: number; titulo: string; descripcion: string;
  categoriaId?: number | null; categoriaNombre?: string | null; categoriaIcono?: string | null;
  ciudadanoId?: number | null; ciudadanoNombre?: string | null;
  direccion?: string | null; latitud?: number | null; longitud?: number | null;
  imagenUrl?: string | null; tieneFoto?: boolean; fotoTipo?: string | null;
  estado: EstadoReporte; prioridad?: Prioridad | null; justificacionRechazo?: string | null;
  evidenciaCierreUrl?: string | null; tieneFotoSolucion?: boolean; fotoSolucionTipo?: string | null;
  operadorAsignadoNombre?: string | null; reportesCercanos?: number | null;
  semaforoColorHex?: string | null; semaforoNivel?: string | null; semaforoDescripcion?: string | null;
  creadoEn?: string | null; actualizadoEn?: string | null; fechaCierre?: string | null;
}
export interface ReporteDetalle extends Reporte {
  asignacionActual?: Asignacion | null;
  lineaDeTiempo: Seguimiento[];
  comentarios: Comentario[];
}
export interface ReporteCreateRequest {
  titulo: string; descripcion: string; categoriaId: number;
  latitud?: number | null; longitud?: number | null; direccion?: string | null; imagenUrl?: string | null;
}
export interface ValidarReporteRequest { admitido: boolean; justificacion?: string | null; prioridad?: Prioridad | null; }
export interface CambiarPrioridadRequest { prioridad: Prioridad; observaciones?: string | null; }
export interface CambiarEstadoRequest { estado: EstadoReporte; observaciones?: string | null; }
export interface CerrarReporteRequest { evidenciaCierreUrl: string; observaciones?: string | null; }
export interface ReporteFiltros { categoriaId?: number; estado?: string; prioridad?: string; ciudadanoId?: number; }

// ---------- Asignaciones / Seguimiento / Comentarios ----------
export interface Asignacion {
  id: number; reporteId: number; reporteTitulo?: string | null;
  operadorId: number; operadorNombre?: string | null;
  gestorId?: number | null; gestorNombre?: string | null;
  notas?: string | null; activa: boolean; creadoEn?: string | null;
}
export interface AsignacionCreateRequest { operadorId: number; notas?: string | null; }
export interface Seguimiento {
  id: number; reporteId?: number | null; reporteTitulo?: string | null;
  usuarioId?: number | null; usuarioNombre?: string | null; usuarioEmail?: string | null; usuarioRol?: string | null;
  estadoAnterior?: string | null; estadoNuevo?: string | null; accion?: string | null;
  observaciones?: string | null; evidenciaUrl?: string | null; hashTrazabilidad?: string | null; creadoEn?: string | null;
}
export interface SeguimientoCreateRequest { nuevoEstado?: EstadoReporte | null; accion: string; observaciones: string; evidenciaUrl?: string | null; }
export interface Comentario {
  id: number; reporteId: number; usuarioId: number; usuarioNombre: string; usuarioRol?: string | null;
  contenido: string; creadoEn?: string | null; actualizadoEn?: string | null;
}

// ---------- Estadísticas ----------
export interface Estadisticas {
  totalReportes: number;
  reportesPorEstado: Record<string, number>;
  reportesPorCategoria: Record<string, number>;
  reportesPorPrioridad: Record<string, number>;
  tiempoPromedioAtencionHoras: number;
  reportesAtendidosOCerrados: number;
  reportesPendientes: number;
  semaforoGlobal?: Semaforo | null;
  semaforoPorCategoria: Record<string, Semaforo>;
}
