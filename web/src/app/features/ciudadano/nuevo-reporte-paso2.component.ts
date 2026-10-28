import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NuevoReporteStore } from '../../core/nuevo-reporte.store';
import { IconComponent } from '../../shared/icon.component';
import { MapViewComponent } from '../../shared/map-view.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-nuevo-reporte-paso2',
  standalone: true,
  imports: [FormsModule, TopBarComponent, IconComponent, MapViewComponent],
  templateUrl: './nuevo-reporte-paso2.component.html',
})
export class NuevoReportePaso2Component {
  private router = inject(Router);
  store = inject(NuevoReporteStore);
  intento = signal(false);
  gpsMsg = signal('');
  buscandoGps = signal(false);

  onPick(p: { lat: number; lng: number }): void {
    this.store.latitud.set(+p.lat.toFixed(6));
    this.store.longitud.set(+p.lng.toFixed(6));
  }

  usarGps(): void {
    if (!navigator.geolocation) { this.gpsMsg.set('Tu navegador no soporta geolocalización.'); return; }
    this.buscandoGps.set(true);
    this.gpsMsg.set('');
    navigator.geolocation.getCurrentPosition(
      (pos) => { this.onPick({ lat: pos.coords.latitude, lng: pos.coords.longitude }); this.buscandoGps.set(false); },
      () => { this.gpsMsg.set('No se pudo obtener tu ubicación. Revisa los permisos o marca el punto en el mapa.'); this.buscandoGps.set(false); },
      { enableHighAccuracy: true, timeout: 10000 },
    );
  }

  siguiente(): void {
    this.intento.set(true);
    if (this.store.direccion().trim().length >= 3) this.router.navigate(['/ciudadano/nuevo-reporte/3']);
  }
  atras(): void { this.router.navigate(['/ciudadano/nuevo-reporte/1']); }
}
