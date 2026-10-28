import { EstadoReporte, Prioridad, Rol, Semaforo } from './models';

export const ESTADOS: EstadoReporte[] = ['PENDIENTE', 'EN_REVISION', 'ADMITIDO', 'RECHAZADO', 'EN_PROCESO', 'ATENDIDO', 'CERRADO'];
export const PRIORIDADES: Prioridad[] = ['BAJA', 'MEDIA', 'ALTA', 'CRITICA'];
export const ROLES: Rol[] = ['ROLE_CIUDADANO', 'ROLE_GESTOR', 'ROLE_OPERADOR', 'ROLE_ADMIN'];

export interface EstadoStyle { bg: string; text: string; border: string; }

/** Paleta semántica por estado (ui/theme/Color.kt + StatusChip.kt). */
export function estadoStyle(estado: string | null | undefined): EstadoStyle {
  switch ((estado ?? '').toUpperCase().replace(/ /g, '_')) {
    case 'PENDIENTE': case 'CREACION_REPORTE': return { bg: '#FFF3E0', text: '#E65100', border: '#FFB74D' };
    case 'EN_REVISION': return { bg: '#E1F5FE', text: '#0288D1', border: '#81D4FA' };
    case 'ADMITIDO': case 'VALIDACION_ADMITIDA': return { bg: '#E8F5E9', text: '#2E7D32', border: '#A5D6A7' };
    case 'EN_PROCESO': case 'EN_ATENCION': case 'CAMBIO_ESTADO': return { bg: '#E3F2FD', text: '#1565C0', border: '#90CAF9' };
    case 'ATENDIDO': return { bg: '#EDE7F6', text: '#6A1B9A', border: '#CE93D8' };
    case 'CERRADO': case 'CIERRE_REPORTE': return { bg: '#DCEDC8', text: '#33691E', border: '#AED581' };
    case 'RECHAZADO': case 'RECHAZO_REPORTE': return { bg: '#FFEBEE', text: '#C62828', border: '#EF9A9A' };
    default: return { bg: '#EEEEEE', text: '#616161', border: '#BDBDBD' };
  }
}

export function prioridadStyle(p: string | null | undefined): { bg: string; text: string } {
  switch ((p ?? 'MEDIA').toUpperCase()) {
    case 'BAJA': return { bg: '#E8F5E9', text: '#2E7D32' };
    case 'MEDIA': return { bg: '#E3F2FD', text: '#1565C0' };
    case 'ALTA': return { bg: '#FFF3E0', text: '#E65100' };
    case 'CRITICA': return { bg: '#FFEBEE', text: '#C62828' };
    default: return { bg: '#F5F5F5', text: '#424242' };
  }
}

/** Misma escala que SemaforoModel.calcularLocal() de la app Android. */
export function semaforoLocal(cantidad: number): Semaforo {
  if (cantidad <= 0) return { cantidadReportes: 0, colorHex: '#4CAF50', colorNombre: 'VERDE', nivelAlerta: 'SIN_REPORTES', descripcion: 'Sin incidencias', porcentajeSaturacion: 0 };
  if (cantidad <= 2) return { cantidadReportes: cantidad, colorHex: '#4CAF50', colorNombre: 'VERDE', nivelAlerta: 'BAJO', descripcion: '1 a 2 reportes: Nivel bajo controlado', porcentajeSaturacion: cantidad * 10 };
  if (cantidad <= 4) return { cantidadReportes: cantidad, colorHex: '#8BC34A', colorNombre: 'VERDE_LIMA', nivelAlerta: 'LEVE', descripcion: '3 a 4 reportes: Nivel leve', porcentajeSaturacion: cantidad * 10 };
  if (cantidad <= 6) return { cantidadReportes: cantidad, colorHex: '#FFEB3B', colorNombre: 'AMARILLO', nivelAlerta: 'MODERADO', descripcion: '5 a 6 reportes: Alerta preventiva', porcentajeSaturacion: cantidad * 10 };
  if (cantidad <= 8) return { cantidadReportes: cantidad, colorHex: '#FF9800', colorNombre: 'NARANJA', nivelAlerta: 'ALTO', descripcion: '7 a 8 reportes: Atención urgente', porcentajeSaturacion: cantidad * 10 };
  return { cantidadReportes: cantidad, colorHex: '#F44336', colorNombre: 'ROJO', nivelAlerta: 'CRITICO', descripcion: '10 o más: Nivel crítico saturado', porcentajeSaturacion: 100 };
}

const EMOJI: Record<string, string> = { road: '🛣️', lightbulb: '💡', trash: '🗑️', park: '🌳', water: '💧', traffic: '🚦' };
export function categoriaEmoji(icono?: string | null): string {
  return (icono && EMOJI[icono.toLowerCase()]) || '📍';
}

export const ROL_LABEL: Record<Rol, string> = {
  ROLE_CIUDADANO: 'Ciudadano', ROLE_GESTOR: 'Gestor', ROLE_OPERADOR: 'Operador', ROLE_ADMIN: 'Administrador',
};

export function homePorRol(rol: Rol | string | null | undefined): string {
  switch (rol) {
    case 'ROLE_GESTOR': return '/gestor/validacion';
    case 'ROLE_OPERADOR': return '/operador/tareas';
    case 'ROLE_ADMIN': return '/admin/panel';
    default: return '/ciudadano/inicio';
  }
}

export function labelEstado(e: string | null | undefined): string {
  return (e ?? '').replace(/_/g, ' ');
}

/** Extrae un mensaje legible de un HttpErrorResponse del backend (ErrorResponse / ApiResponse). */
export function mensajeError(e: any, fallback = 'Ocurrió un error inesperado'): string {
  if (e?.status === 0) return 'No se pudo conectar con el servidor. Verifica tu red o la URL del backend.';
  const b = e?.error;
  if (b?.validationErrors) {
    const first = Object.values(b.validationErrors)[0];
    if (first) return String(first);
  }
  return b?.message || b?.error || e?.message || fallback;
}
