import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiConfigService } from '../../core/api-config.service';
import { ApiService } from '../../core/api.service';
import { homePorRol, mensajeError } from '../../core/constants';
import { SessionService } from '../../core/session.service';
import { IconComponent } from '../../shared/icon.component';
import { ModalComponent } from '../../shared/modal.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, RouterLink, IconComponent, ModalComponent],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private session = inject(SessionService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  cfg = inject(ApiConfigService);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    remember: [true],
  });
  verPass = signal(false);
  cargando = signal(false);
  error = signal('');
  expirada = !!this.route.snapshot.queryParamMap.get('expirada');

  // Diálogo "Conexión del Backend"
  servidorAbierto = signal(false);
  urlServidor = '';
  presets = [
    { label: 'Render Nube', url: 'https://ciudadactiva-4ah7.onrender.com' },
    { label: 'Local', url: 'http://localhost:8080' },
  ];

  ingresar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const { email, password, remember } = this.form.getRawValue();
    this.cargando.set(true);
    this.error.set('');
    this.api.login({ email: email.trim().toLowerCase(), password }).subscribe({
      next: (auth) => {
        this.session.save(auth.token, auth.usuario, remember);
        this.router.navigateByUrl(homePorRol(auth.usuario.rol), { replaceUrl: true });
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(e.status === 401 || e.status === 403 ? 'Credenciales inválidas. Verifica tu correo y contraseña.' : mensajeError(e));
      },
    });
  }

  abrirServidor(): void { this.urlServidor = this.cfg.baseUrl(); this.servidorAbierto.set(true); }
  guardarServidor(): void { if (this.urlServidor.trim()) this.cfg.set(this.urlServidor); this.servidorAbierto.set(false); }
}
