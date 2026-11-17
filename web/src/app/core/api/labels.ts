import { EstadoReporte, PrioridadReporte, RolNombre } from './models';

export const ESTADO_LABEL: Record<EstadoReporte, string> = {
  PENDIENTE: 'Pendiente',
  EN_REVISION: 'En revisión',
  ADMITIDO: 'Admitido',
  RECHAZADO: 'Rechazado',
  EN_PROCESO: 'En proceso · asignado',
  ATENDIDO: 'Atendido',
  CERRADO: 'Cerrado',
};

export const PRIORIDAD_LABEL: Record<PrioridadReporte, string> = {
  BAJA: 'Baja',
  MEDIA: 'Media',
  ALTA: 'Alta',
  CRITICA: 'Crítica',
};

export const PRIORIDAD_PESO: Record<PrioridadReporte, number> = { BAJA: 1, MEDIA: 2, ALTA: 3, CRITICA: 4 };

export const ROL_LABEL: Record<RolNombre, string> = {
  ROLE_CIUDADANO: 'Ciudadano',
  ROLE_GESTOR: 'Gestor',
  ROLE_OPERADOR: 'Operador',
  ROLE_ADMIN: 'Administrador',
};

/**
 * Grupo visual del estado para pines del mapa (RF10 / CP11):
 * amarillo = creado / pendiente de gestión, azul = asignado / en proceso, verde = cerrado.
 */
export type GrupoEstado = 'pendiente' | 'proceso' | 'cerrado' | 'rechazado';

export function grupoEstado(e: EstadoReporte): GrupoEstado {
  switch (e) {
    case 'PENDIENTE':
    case 'EN_REVISION':
    case 'ADMITIDO':
      return 'pendiente';
    case 'EN_PROCESO':
    case 'ATENDIDO':
      return 'proceso';
    case 'CERRADO':
      return 'cerrado';
    default:
      return 'rechazado';
  }
}

export const GRUPO_COLOR: Record<GrupoEstado, string> = {
  pendiente: '#E3B23C',
  proceso: '#3B7DD8',
  cerrado: '#3F9B5F',
  rechazado: '#8A8F8C',
};

/** Iconos guardados en la BD (compatibles con la app Android) → Material Symbols */
export const ICONO_CATEGORIA: Record<string, string> = {
  road: 'add_road',
  lightbulb: 'lightbulb',
  trash: 'delete',
  park: 'park',
  water: 'water_drop',
  traffic: 'traffic',
  tag: 'sell',
};

export function iconoCategoria(clave?: string | null): string {
  return (clave && ICONO_CATEGORIA[clave]) || 'sell';
}

/** Puede validarse (RN04): solo PENDIENTE o EN_REVISION */
export const puedeValidar = (e: EstadoReporte) => e === 'PENDIENTE' || e === 'EN_REVISION';
/** Puede asignarse (RN09): solo ADMITIDO. Bloqueado en RECHAZADO / CERRADO. */
export const puedeAsignar = (e: EstadoReporte) => e === 'ADMITIDO';
