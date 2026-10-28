import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiConfigService } from '../../core/api-config.service';
import { ApiService } from '../../core/api.service';
import { ROL_LABEL, mensajeError } from '../../core/constants';
import { SessionService } from '../../core/session.service';
import { ToastService } from '../../core/toast.service';
import { IconComponent } from '../../shared/icon.component';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, TopBarComponent, IconComponent],
  templateUrl: './perfil.component.html',
})
export class PerfilComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);
  session = inject(SessionService);
  cfg = inject(ApiConfigService);

  rolLabel = computed(() => ROL_LABEL[this.session.rol() ?? 'ROLE_CIUDADANO']);
  inicial = computed(() => (this.session.user()?.nombre ?? '?').charAt(0).toUpperCase());
  editando = signal(false);
  guardando = signal(false);
  error = signal('');
  url = this.cfg.baseUrl();

  form = this.fb.nonNullable.group({
    nombre: [this.session.user()?.nombre ?? '', [Validators.required, Validators.minLength(3)]],
    email: [this.session.user()?.email ?? '', [Validators.required, Validators.email]],
    telefono: [this.session.user()?.telefono ?? ''],
    direccion: [this.session.user()?.direccion ?? ''],
  });

  constructor() {
    // Refresca los datos desde GET /api/usuarios/me
    this.api.miPerfil().subscribe({
      next: (u) => { this.session.updateUser(u); this.form.patchValue({ nombre: u.nombre, email: u.email, telefono: u.telefono ?? '', direccion: u.direccion ?? '' }); },
      error: () => { /* se conserva la copia local */ },
    });
  }

  guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    this.guardando.set(true);
    this.error.set('');
    this.api.actualizarMiPerfil({ nombre: v.nombre.trim(), email: v.email.trim().toLowerCase(), telefono: v.telefono.trim() || null, direccion: v.direccion.trim() || null }).subscribe({
      next: (u) => { this.session.updateUser(u); this.guardando.set(false); this.editando.set(false); this.toast.ok('Datos personales actualizados.'); },
      error: (e) => { this.guardando.set(false); this.error.set(mensajeError(e)); },
    });
  }

  guardarUrl(): void { if (this.url.trim()) { this.cfg.set(this.url); this.toast.ok('URL del servidor guardada.'); } }

  cerrarSesion(): void {
    this.session.clear();
    this.router.navigateByUrl('/login', { replaceUrl: true });
  }
}
