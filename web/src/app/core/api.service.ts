import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiConfigService } from './api-config.service';
import * as M from './models';

/**
 * Cliente REST único. Cada método corresponde 1:1 a CiudadActivaApiService.kt (Retrofit)
 * y a los controladores del backend Spring Boot. Devuelve directamente `data` del ApiResponse.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly cfg = inject(ApiConfigService);

  private url(path: string): string { return `${this.cfg.baseUrl()}/api/${path}`; }
  private data<T>() { return map((r: M.ApiResponse<T>) => r.data as T); }

  // ============ AUTENTICACIÓN Y PERFIL ============
  login(body: M.LoginRequest): Observable<M.AuthResponse> {
    return this.http.post<M.ApiResponse<M.AuthResponse>>(this.url('auth/login'), body).pipe(this.data());
  }
  registrar(body: M.RegisterRequest): Observable<M.AuthResponse> {
    return this.http.post<M.ApiResponse<M.AuthResponse>>(this.url('auth/register'), body).pipe(this.data());
  }
  miPerfil(): Observable<M.Usuario> {
    return this.http.get<M.ApiResponse<M.Usuario>>(this.url('usuarios/me')).pipe(this.data());
  }
  actualizarMiPerfil(body: M.UsuarioUpdateRequest): Observable<M.Usuario> {
    return this.http.put<M.ApiResponse<M.Usuario>>(this.url('usuarios/me'), body).pipe(this.data());
  }

  // ============ CATEGORÍAS ============
  listarCategorias(): Observable<M.Categoria[]> {
    return this.http.get<M.ApiResponse<M.Categoria[]>>(this.url('categorias')).pipe(this.data());
  }
  crearCategoria(body: M.CategoriaRequest): Observable<M.Categoria> {
    return this.http.post<M.ApiResponse<M.Categoria>>(this.url('categorias'), body).pipe(this.data());
  }
  actualizarCategoria(id: number, body: M.CategoriaRequest): Observable<M.Categoria> {
    return this.http.put<M.ApiResponse<M.Categoria>>(this.url(`categorias/${id}`), body).pipe(this.data());
  }
  eliminarCategoria(id: number): Observable<void> {
    return this.http.delete<M.ApiResponse<void>>(this.url(`categorias/${id}`)).pipe(map(() => void 0));
  }

  // ============ REPORTES ============
  listarReportes(f: M.ReporteFiltros = {}): Observable<M.Reporte[]> {
    let params = new HttpParams();
    if (f.categoriaId != null) params = params.set('categoriaId', f.categoriaId);
    if (f.estado) params = params.set('estado', f.estado);
    if (f.prioridad) params = params.set('prioridad', f.prioridad);
    if (f.ciudadanoId != null) params = params.set('ciudadanoId', f.ciudadanoId);
    return this.http.get<M.ApiResponse<M.Reporte[]>>(this.url('reportes'), { params }).pipe(this.data());
  }
  misReportes(): Observable<M.Reporte[]> {
    return this.http.get<M.ApiResponse<M.Reporte[]>>(this.url('reportes/mis-reportes')).pipe(this.data());
  }
  detalleReporte(id: number): Observable<M.ReporteDetalle> {
    return this.http.get<M.ApiResponse<M.ReporteDetalle>>(this.url(`reportes/${id}`)).pipe(this.data());
  }
  eliminarReporte(id: number): Observable<void> {
    return this.http.delete<M.ApiResponse<void>>(this.url(`reportes/${id}`)).pipe(map(() => void 0));
  }
  crearReporte(body: M.ReporteCreateRequest): Observable<M.Reporte> {
    return this.http.post<M.ApiResponse<M.Reporte>>(this.url('reportes'), body).pipe(this.data());
  }
  /** multipart: titulo, descripcion, categoriaId, latitud, longitud, direccion, foto */
  crearReporteConFoto(v: { titulo: string; descripcion: string; categoriaId: number; latitud?: number; longitud?: number; direccion?: string }, foto: File | null): Observable<M.Reporte> {
    const fd = new FormData();
    fd.append('titulo', v.titulo);
    fd.append('descripcion', v.descripcion);
    fd.append('categoriaId', String(v.categoriaId));
    if (v.latitud != null) fd.append('latitud', String(v.latitud));
    if (v.longitud != null) fd.append('longitud', String(v.longitud));
    if (v.direccion) fd.append('direccion', v.direccion);
    if (foto) fd.append('foto', foto, foto.name);
    return this.http.post<M.ApiResponse<M.Reporte>>(this.url('reportes/con-foto'), fd).pipe(this.data());
  }
  subirFotoReporte(id: number, foto: File): Observable<M.Reporte> {
    const fd = new FormData();
    fd.append('foto', foto, foto.name);
    return this.http.post<M.ApiResponse<M.Reporte>>(this.url(`reportes/${id}/foto`), fd).pipe(this.data());
  }

  // ============ GESTIÓN Y VALIDACIÓN (GESTOR / ADMIN) ============
  validarReporte(id: number, body: M.ValidarReporteRequest): Observable<M.Reporte> {
    return this.http.patch<M.ApiResponse<M.Reporte>>(this.url(`reportes/${id}/validar`), body).pipe(this.data());
  }
  cambiarPrioridad(id: number, body: M.CambiarPrioridadRequest): Observable<M.Reporte> {
    return this.http.patch<M.ApiResponse<M.Reporte>>(this.url(`reportes/${id}/prioridad`), body).pipe(this.data());
  }
  cambiarEstado(id: number, body: M.CambiarEstadoRequest): Observable<M.Reporte> {
    return this.http.patch<M.ApiResponse<M.Reporte>>(this.url(`reportes/${id}/estado`), body).pipe(this.data());
  }
  asignarOperador(id: number, body: M.AsignacionCreateRequest): Observable<M.Asignacion> {
    return this.http.post<M.ApiResponse<M.Asignacion>>(this.url(`reportes/${id}/asignaciones`), body).pipe(this.data());
  }
  cancelarAsignacion(id: number): Observable<void> {
    return this.http.delete<M.ApiResponse<void>>(this.url(`reportes/${id}/asignaciones`)).pipe(map(() => void 0));
  }
  listarAsignacionesActivas(): Observable<M.Asignacion[]> {
    return this.http.get<M.ApiResponse<M.Asignacion[]>>(this.url('asignaciones')).pipe(this.data());
  }
  listarOperadores(): Observable<M.Usuario[]> {
    return this.http.get<M.ApiResponse<M.Usuario[]>>(this.url('operadores')).pipe(this.data());
  }

  // ============ OPERACIONES DE CAMPO (OPERADOR) ============
  misAsignaciones(): Observable<M.Asignacion[]> {
    return this.http.get<M.ApiResponse<M.Asignacion[]>>(this.url('asignaciones/mis-asignaciones')).pipe(this.data());
  }
  registrarAvance(id: number, body: M.SeguimientoCreateRequest): Observable<M.Seguimiento> {
    return this.http.post<M.ApiResponse<M.Seguimiento>>(this.url(`reportes/${id}/seguimientos`), body).pipe(this.data());
  }
  lineaDeTiempo(id: number): Observable<M.Seguimiento[]> {
    return this.http.get<M.ApiResponse<M.Seguimiento[]>>(this.url(`reportes/${id}/seguimiento`)).pipe(this.data());
  }
  cerrarReporte(id: number, body: M.CerrarReporteRequest): Observable<M.Reporte> {
    return this.http.patch<M.ApiResponse<M.Reporte>>(this.url(`reportes/${id}/cerrar`), body).pipe(this.data());
  }
  subirFotoSolucion(id: number, foto: File, observaciones?: string): Observable<M.Reporte> {
    const fd = new FormData();
    fd.append('foto', foto, foto.name);
    if (observaciones) fd.append('observaciones', observaciones);
    return this.http.post<M.ApiResponse<M.Reporte>>(this.url(`reportes/${id}/foto-solucion`), fd).pipe(this.data());
  }

  // ============ COMENTARIOS ============
  listarComentarios(id: number): Observable<M.Comentario[]> {
    return this.http.get<M.ApiResponse<M.Comentario[]>>(this.url(`reportes/${id}/comentarios`)).pipe(this.data());
  }
  agregarComentario(id: number, contenido: string): Observable<M.Comentario> {
    return this.http.post<M.ApiResponse<M.Comentario>>(this.url(`reportes/${id}/comentarios`), { contenido }).pipe(this.data());
  }
  eliminarComentario(id: number): Observable<void> {
    return this.http.delete<M.ApiResponse<void>>(this.url(`comentarios/${id}`)).pipe(map(() => void 0));
  }

  // ============ ESTADÍSTICAS Y SEMÁFORO ============
  estadisticas(): Observable<M.Estadisticas> {
    return this.http.get<M.ApiResponse<M.Estadisticas>>(this.url('estadisticas/reportes')).pipe(this.data());
  }
  calcularSemaforo(cantidad: number): Observable<M.Semaforo> {
    return this.http.get<M.ApiResponse<M.Semaforo>>(this.url('estadisticas/semaforo/calcular'), { params: { cantidad } }).pipe(this.data());
  }

  // ============ ADMINISTRACIÓN (ADMIN) ============
  listarUsuarios(rol?: string | null): Observable<M.Usuario[]> {
    const params = rol && rol !== 'TODOS' ? new HttpParams().set('rol', rol) : new HttpParams();
    return this.http.get<M.ApiResponse<M.Usuario[]>>(this.url('admin/usuarios'), { params }).pipe(this.data());
  }
  crearUsuario(body: M.UsuarioCreateAdminRequest): Observable<M.Usuario> {
    return this.http.post<M.ApiResponse<M.Usuario>>(this.url('admin/usuarios'), body).pipe(this.data());
  }
  actualizarUsuario(id: number, body: M.UsuarioUpdateRequest): Observable<M.Usuario> {
    return this.http.put<M.ApiResponse<M.Usuario>>(this.url(`admin/usuarios/${id}`), body).pipe(this.data());
  }
  desactivarUsuario(id: number): Observable<void> {
    return this.http.delete<M.ApiResponse<void>>(this.url(`admin/usuarios/${id}`)).pipe(map(() => void 0));
  }
  auditoria(): Observable<M.Seguimiento[]> {
    return this.http.get<M.ApiResponse<M.Seguimiento[]>>(this.url('admin/auditoria')).pipe(this.data());
  }
}
