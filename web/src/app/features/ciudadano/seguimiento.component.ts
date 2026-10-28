import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiConfigService } from '../../core/api-config.service';
import { ApiService } from '../../core/api.service';
import { estadoStyle, labelEstado, mensajeError } from '../../core/constants';
import { ReporteDetalle } from '../../core/models';
import { SessionService } from '../../core/session.service';
import { ToastService } from '../../core/toast.service';
import { PrioridadChipComponent, SemaforoBadgeComponent, StatusChipComponent } from '../../shared/chips.component';
import { IconComponent } from '../../shared/icon.component';
import { MapViewComponent } from '../../shared/map-view.component';
import { ModalComponent } from '../../shared/modal.component';
import { EmptyComponent, ErrorViewComponent, LoadingComponent } from '../../shared/states.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-seguimiento',
  standalone: true,
  imports: [DatePipe, FormsModule, RouterLink, TopBarComponent, StatusChipComponent, PrioridadChipComponent, SemaforoBadgeComponent, IconComponent, MapViewComponent, ModalComponent, LoadingComponent, ErrorViewComponent, EmptyComponent],
  templateUrl: './seguimiento.component.html',
})
export class SeguimientoComponent implements OnInit {
  /** :id de la ruta (withComponentInputBinding) */
  id = input.required<string>();
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  cfg = inject(ApiConfigService);
  session = inject(SessionService);

  rep = signal<ReporteDetalle | null>(null);
  cargando = signal(true);
  error = signal('');
  comentario = '';
  enviando = signal(false);
  confirmarRetiro = signal(false);

  st = estadoStyle;
  lbl = labelEstado;
  esMio = computed(() => this.rep()?.ciudadanoId === this.session.user()?.id);
  puedeRetirar = computed(() => this.esMio() && this.rep()?.estado === 'PENDIENTE');
  comentarios = computed(() => this.rep()?.comentarios ?? []);
  markers = computed(() => { const r = this.rep(); return r?.latitud != null && r?.longitud != null ? [{ id: r.id, lat: r.latitud, lng: r.longitud, color: r.semaforoColorHex || '#4CAF50' }] : []; });

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.api.detalleReporte(+this.id()).subscribe({
      next: (r) => { this.rep.set(r); this.cargando.set(false); },
      error: (e) => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }

  enviarComentario(): void {
    const t = this.comentario.trim();
    if (!t) return;
    this.enviando.set(true);
    this.api.agregarComentario(+this.id(), t).subscribe({
      next: () => { this.comentario = ''; this.enviando.set(false); this.cargar(); },
      error: (e) => { this.enviando.set(false); this.toast.error('Error al enviar comentario: ' + mensajeError(e)); },
    });
  }

  retirar(): void {
    this.api.eliminarReporte(+this.id()).subscribe({
      next: () => { this.toast.ok('Reporte retirado.'); this.router.navigate(['/ciudadano/mis-reportes']); },
      error: (e) => { this.confirmarRetiro.set(false); this.toast.error(mensajeError(e, 'No se pudo retirar el reporte.')); },
    });
  }
}
