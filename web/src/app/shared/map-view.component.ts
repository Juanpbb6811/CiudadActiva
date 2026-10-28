import { AfterViewInit, Component, ElementRef, OnDestroy, effect, input, output, viewChild } from '@angular/core';
import * as L from 'leaflet';

export interface MapMarker { id: number; lat: number; lng: number; color: string; label?: string; }

/**
 * Mapa OpenStreetMap con Leaflet (equivale a OpenStreetMapComposable.kt).
 * - Modo exploración: marcadores coloreados por semáforo + evento markerClick.
 * - Modo selección: marcador arrastrable / clic en el mapa -> evento pick.
 */
@Component({
  selector: 'app-map-view',
  standalone: true,
  template: `<div #host class="map" [style.height]="height()"></div>`,
  styles: [`.map{width:100%;border-radius:16px;overflow:hidden;border:1px solid var(--border);z-index:0}`],
})
export class MapViewComponent implements AfterViewInit, OnDestroy {
  markers = input<MapMarker[]>([]);
  lat = input(4.609712);
  lng = input(-74.08175);
  zoom = input(14);
  height = input('320px');
  selectable = input(false);
  fitToMarkers = input(true);
  markerClick = output<number>();
  pick = output<{ lat: number; lng: number }>();

  private host = viewChild.required<ElementRef<HTMLDivElement>>('host');
  private map?: L.Map;
  private layer = L.layerGroup();
  private pickMarker?: L.Marker;
  private ready = false;

  constructor() {
    effect(() => {
      const ms = this.markers();
      if (this.ready) this.draw(ms);
    });
    effect(() => {
      const la = this.lat(), ln = this.lng();
      if (this.ready && this.selectable()) this.setPick(la, ln, true);
    });
  }

  ngAfterViewInit(): void {
    this.map = L.map(this.host().nativeElement, { zoomControl: true }).setView([this.lat(), this.lng()], this.zoom());
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19, attribution: '&copy; OpenStreetMap',
    }).addTo(this.map);
    this.layer.addTo(this.map);
    this.ready = true;
    if (this.selectable()) {
      this.setPick(this.lat(), this.lng(), false);
      this.map.on('click', (e: L.LeafletMouseEvent) => { this.setPick(e.latlng.lat, e.latlng.lng, false); this.pick.emit({ lat: e.latlng.lat, lng: e.latlng.lng }); });
    } else {
      this.draw(this.markers());
    }
    setTimeout(() => this.map?.invalidateSize(), 150);
  }

  private icon(color: string): L.DivIcon {
    return L.divIcon({
      className: '',
      html: `<span style="display:block;width:22px;height:22px;border-radius:50% 50% 50% 0;transform:rotate(-45deg);background:${color};border:3px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,.4)"></span>`,
      iconSize: [22, 22], iconAnchor: [11, 22],
    });
  }

  private draw(ms: MapMarker[]): void {
    this.layer.clearLayers();
    for (const m of ms) {
      if (m.lat == null || m.lng == null) continue;
      const mk = L.marker([m.lat, m.lng], { icon: this.icon(m.color), title: m.label ?? '' });
      mk.on('click', () => this.markerClick.emit(m.id));
      mk.addTo(this.layer);
    }
    if (this.fitToMarkers() && ms.length && this.map) {
      this.map.fitBounds(L.latLngBounds(ms.map((m) => [m.lat, m.lng] as [number, number])).pad(0.2), { maxZoom: 16 });
    }
  }

  private setPick(lat: number, lng: number, recenter: boolean): void {
    if (!this.map) return;
    if (!this.pickMarker) {
      this.pickMarker = L.marker([lat, lng], { icon: this.icon('#D4A373'), draggable: true }).addTo(this.map);
      this.pickMarker.on('dragend', () => {
        const p = this.pickMarker!.getLatLng();
        this.pick.emit({ lat: p.lat, lng: p.lng });
      });
    } else {
      this.pickMarker.setLatLng([lat, lng]);
    }
    if (recenter) this.map.setView([lat, lng], Math.max(this.map.getZoom(), 16));
  }

  ngOnDestroy(): void { this.map?.remove(); }
}
