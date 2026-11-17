import { HttpErrorResponse } from '@angular/common/http';

/**
 * Error normalizado según RFC 7807 (Problem Details for HTTP APIs).
 * El backend devuelve ErrorResponse con los miembros estándar (type, title, detail, instance)
 * además de los históricos (error, message, path, validationErrors). Este parser acepta ambos,
 * y también ProblemDetail nativo de Spring 6 o un ApiResponse con success=false.
 */
export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  errors?: Record<string, string>;
}

export function aProblemDetail(err: unknown): ProblemDetail {
  if (err instanceof HttpErrorResponse) {
    const body = err.error;
    if (err.status === 0) {
      return {
        type: 'about:blank',
        title: 'Sin conexión con el servidor',
        status: 0,
        detail:
          'No se pudo contactar al backend. Verifica que esté encendido (Render puede tardar ~1 minuto en despertar) o cambia el servidor en la pantalla de inicio de sesión.',
      };
    }
    if (body && typeof body === 'object') {
      const b = body as Record<string, unknown>;
      return {
        type: (b['type'] as string) || 'about:blank',
        title: (b['title'] as string) || (b['error'] as string) || tituloPorEstado(err.status),
        status: (b['status'] as number) || err.status,
        detail:
          (b['detail'] as string) ||
          (b['message'] as string) ||
          err.message ||
          'Ocurrió un error inesperado.',
        instance: (b['instance'] as string) || (b['path'] as string) || undefined,
        errors:
          (b['validationErrors'] as Record<string, string>) ||
          (b['errors'] as Record<string, string>) ||
          undefined,
      };
    }
    return {
      type: 'about:blank',
      title: tituloPorEstado(err.status),
      status: err.status,
      detail: typeof body === 'string' && body ? body : err.message,
    };
  }
  if (isProblem(err)) return err;
  return {
    type: 'about:blank',
    title: 'Error inesperado',
    status: -1,
    detail: err instanceof Error ? err.message : String(err),
  };
}

export function isProblem(x: unknown): x is ProblemDetail {
  return !!x && typeof x === 'object' && 'title' in x && 'detail' in x && 'status' in x;
}

/** Texto corto para snackbars: detalle + primer error de validación si existe. */
export function mensajeProblema(p: ProblemDetail): string {
  const campo = p.errors ? Object.values(p.errors)[0] : null;
  return campo ? `${p.detail} ${campo}` : p.detail;
}

function tituloPorEstado(status: number): string {
  switch (status) {
    case 400:
      return 'Solicitud inválida';
    case 401:
      return 'Sesión no válida';
    case 403:
      return 'Acceso denegado';
    case 404:
      return 'Recurso no encontrado';
    case 409:
      return 'Conflicto';
    case 422:
      return 'Violación de regla de negocio';
    default:
      return status >= 500 ? 'Error del servidor' : 'Error';
  }
}
