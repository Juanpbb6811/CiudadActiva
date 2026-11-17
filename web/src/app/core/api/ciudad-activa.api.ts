import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { SIN_LOADER } from '../http/interceptors';
import { ApiConfigService } from './api-config.service';
import {
  ApiResponse,
  Asignacion,
  AsignacionCreateRequest,
  Categoria,
  CategoriaRequest,
  Comentario,
  Estadisticas,
  EstadoReporte,
  Reporte,
  ReporteDetalle,
  ReporteFiltros,
  RolNombre,
  Seguimiento,
  Usuario,
  UsuarioCreateRequest,
  UsuarioUpdateRequest,
  ValidarReporteRequest,
} from './models';

/** Cliente HTTP tipado de la API CiudadActiva. Desempaqueta siempre ApiResponse<T>.data */
@Injectable({ providedIn: 'root' })
export class CiudadActivaApi {
  private readonly http = inject(HttpClient);
  private readonly cfg = inject(ApiConfigService);

  private get<T>(path: string, params?: Record<string, unknown>, silencioso = false): Observable<T> {
    return this.http
      .get<ApiResponse<T>>(this.cfg.url(path), {
        params: toParams(params),
        context: silencioso ? new HttpContext().set(SIN_LOADER, true) : undefined,
      })
      .pipe(map((r) => r.data));
  }
  private post<T>(path: string, body: unknown): Observable<T> {
    return this.http.post<ApiResponse<T>>(this.cfg.url(path), body).pipe(map((r) => r.data));
  }
  private put<T>(path: string, body: unknown): Observable<T> {
    return this.http.put<ApiResponse<T>>(this.cfg.url(path), body).pipe(map((r) => r.data));
  }
  private patch<T>(path: string, body: unknown): Observable<T> {
    return this.http.patch<ApiResponse<T>>(this.cfg.url(path), body).pipe(map((r) => r.data));
  }

  // ================== REPORTES (RF03, RF04, RF05, RF10) ==================
  reportes(filtros: ReporteFiltros = {}): Observable<Reporte[]> {
    return this.get<Reporte[]>('/api/reportes', { ...filtros });
  }
  reportesGeolocalizados(categoriaId?: number | null, estado?: EstadoReporte | null): Observable<Reporte[]> {
    return this.get<Reporte[]>('/api/reportes/geolocalizados', { categoriaId, estado });
  }
  detalle(id: number): Observable<ReporteDetalle> {
    return this.get<ReporteDetalle>(`/api/reportes/${id}`);
  }
  seguimiento(id: number): Observable<Seguimiento[]> {
    return this.get<Seguimiento[]>(`/api/reportes/${id}/seguimiento`);
  }
  validar(id: number, body: ValidarReporteRequest): Observable<Reporte> {
    return this.patch<Reporte>(`/api/reportes/${id}/validar`, body);
  }
  asignar(id: number, body: AsignacionCreateRequest): Observable<Asignacion> {
    return this.post<Asignacion>(`/api/reportes/${id}/asignaciones`, body);
  }
  comentarios(id: number): Observable<Comentario[]> {
    return this.get<Comentario[]>(`/api/reportes/${id}/comentarios`);
  }
  comentar(id: number, contenido: string): Observable<Comentario> {
    return this.post<Comentario>(`/api/reportes/${id}/comentarios`, { contenido });
  }

  // ================== ASIGNACIONES / CUADRILLAS (RN05) ==================
  asignacionesActivas(): Observable<Asignacion[]> {
    return this.get<Asignacion[]>('/api/asignaciones');
  }
  historialAsignaciones(reporteId: number): Observable<Asignacion[]> {
    return this.get<Asignacion[]>('/api/asignaciones/historial', { reporteId });
  }
  reasignar(asignacionId: number, body: AsignacionCreateRequest): Observable<Asignacion> {
    return this.patch<Asignacion>(`/api/asignaciones/${asignacionId}/reasignar`, body);
  }
  operadores(): Observable<Usuario[]> {
    return this.get<Usuario[]>('/api/operadores');
  }

  // ================== CATEGORÍAS ==================
  categoriasActivas(): Observable<Categoria[]> {
    return this.get<Categoria[]>('/api/categorias', undefined, true);
  }
  categoriasTodas(): Observable<Categoria[]> {
    return this.get<Categoria[]>('/api/categorias/todas');
  }
  crearCategoria(body: CategoriaRequest): Observable<Categoria> {
    return this.post<Categoria>('/api/categorias', body);
  }
  actualizarCategoria(id: number, body: CategoriaRequest): Observable<Categoria> {
    return this.put<Categoria>(`/api/categorias/${id}`, body);
  }
  /** RNF08: baja lógica, nunca DELETE */
  estadoCategoria(id: number, activo: boolean): Observable<Categoria> {
    return this.patch<Categoria>(`/api/categorias/${id}/estado`, { activo });
  }

  // ================== USUARIOS (RF01) ==================
  usuarios(rol?: RolNombre | null): Observable<Usuario[]> {
    return this.get<Usuario[]>('/api/admin/usuarios', { rol });
  }
  crearUsuario(body: UsuarioCreateRequest): Observable<Usuario> {
    return this.post<Usuario>('/api/admin/usuarios', body);
  }
  actualizarUsuario(id: number, body: UsuarioUpdateRequest): Observable<Usuario> {
    return this.put<Usuario>(`/api/admin/usuarios/${id}`, body);
  }
  cambiarRol(id: number, rol: RolNombre): Observable<Usuario> {
    return this.patch<Usuario>(`/api/admin/usuarios/${id}/rol`, { rol });
  }
  /** RNF08: baja lógica, nunca DELETE */
  estadoUsuario(id: number, activo: boolean): Observable<Usuario> {
    return this.patch<Usuario>(`/api/admin/usuarios/${id}/estado`, { activo });
  }

  // ================== ESTADÍSTICAS (RF09) Y AUDITORÍA ==================
  estadisticas(fechaInicio?: string | null, fechaFin?: string | null): Observable<Estadisticas> {
    return this.get<Estadisticas>('/api/estadisticas/reportes', { fechaInicio, fechaFin });
  }
  auditoria(): Observable<Seguimiento[]> {
    return this.get<Seguimiento[]>('/api/admin/auditoria');
  }
}

function toParams(obj?: Record<string, unknown>): HttpParams | undefined {
  if (!obj) return undefined;
  let p = new HttpParams();
  for (const [k, v] of Object.entries(obj)) {
    if (v !== null && v !== undefined && v !== '') p = p.set(k, String(v));
  }
  return p;
}
