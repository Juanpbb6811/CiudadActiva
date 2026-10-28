import { Component, input, output, signal } from '@angular/core';
import { IconComponent } from './icon.component';

/** Cámara / galería (equivale a CameraHelper.kt): <input type=file capture> en móviles. */
@Component({
  selector: 'app-photo-picker',
  standalone: true,
  imports: [IconComponent],
  template: `
    <div class="photo-box" [class.has]="!!preview()">
      @if (preview()) {
        <img [src]="preview()" alt="Fotografía adjunta" />
        <div class="photo-ok">✓ Evidencia adjunta lista</div>
      } @else {
        <app-icon name="camera" [size]="34" />
        <strong>{{ titulo() }}</strong>
        <small>{{ ayuda() }}</small>
      }
    </div>
    <div class="row gap wrap mt">
      <button type="button" class="btn ghost" (click)="cam.click()"><app-icon name="camera" [size]="16" /> {{ preview() ? 'Tomar otra foto' : 'Tomar Foto' }}</button>
      <button type="button" class="btn ghost" (click)="gal.click()"><app-icon name="image" [size]="16" /> Elegir Galería</button>
      @if (preview()) { <button type="button" class="btn danger-ghost" (click)="quitar()"><app-icon name="trash" [size]="16" /> Eliminar Foto</button> }
    </div>
    <input #cam type="file" accept="image/*" capture="environment" hidden (change)="onFile($event)" />
    <input #gal type="file" accept="image/*" hidden (change)="onFile($event)" />
    @if (error()) { <p class="field-error">{{ error() }}</p> }`,
})
export class PhotoPickerComponent {
  preview = input<string | null>(null);
  titulo = input('Toca aquí para tomar foto');
  ayuda = input('O usa los botones de abajo para cámara o galería');
  fileChange = output<File | null>();
  error = signal('');

  onFile(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const f = input.files?.[0] ?? null;
    input.value = '';
    if (!f) return;
    if (!f.type.startsWith('image/')) { this.error.set('El archivo debe ser una imagen (JPEG, PNG, WEBP...).'); return; }
    if (f.size > 10 * 1024 * 1024) { this.error.set('La imagen no puede superar 10 MB.'); return; }
    this.error.set('');
    this.fileChange.emit(f);
  }
  quitar(): void { this.error.set(''); this.fileChange.emit(null); }
}
