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

  // Recuperación de Contraseña con OTP
  recuperarModal = signal(false);
  recuperarPaso = signal<1 | 2>(1);
  recuperarEmail = '';
  recuperarCodigo = '';
  recuperarNuevaPass = '';
  recuperarCargando = signal(false);
  recuperarMensaje = signal('');
  recuperarError = signal('');

  // Verificación de Cuenta OTP
  verificarModal = signal(false);
  verificarEmail = '';
  verificarCodigo = '';
  verificarCargando = signal(false);
  verificarMensaje = signal('');
  verificarError = signal('');

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
        const msg = mensajeError(e);
        if (msg.toLowerCase().includes('verificar') || msg.toLowerCase().includes('inactiva') || msg.toLowerCase().includes('código')) {
          this.verificarEmail = email.trim().toLowerCase();
          this.verificarModal.set(true);
        } else {
          this.error.set(e.status === 401 || e.status === 403 ? 'Credenciales inválidas. Verifica tu correo y contraseña.' : msg);
        }
      },
    });
  }

  cargarDemo(email: string, pass = 'Ciudadano123*'): void {
    this.form.patchValue({ email, password: pass });
  }

  abrirRecuperar(): void {
    this.recuperarEmail = this.form.get('email')?.value || '';
    this.recuperarPaso.set(1);
    this.recuperarMensaje.set('');
    this.recuperarError.set('');
    this.recuperarModal.set(true);
  }

  solicitarOtpRecuperacion(): void {
    if (!this.recuperarEmail.trim()) { this.recuperarError.set('Ingresa tu correo'); return; }
    this.recuperarCargando.set(true);
    this.recuperarError.set('');
    this.api.solicitarRecuperacionPassword({ email: this.recuperarEmail.trim().toLowerCase() }).subscribe({
      next: (msg) => {
        this.recuperarCargando.set(false);
        this.recuperarMensaje.set(msg || 'Código OTP enviado a tu correo.');
        this.recuperarPaso.set(2);
      },
      error: (e) => {
        this.recuperarCargando.set(false);
        this.recuperarError.set(mensajeError(e, 'No se pudo enviar el código de recuperación.'));
      },
    });
  }

  confirmarResetPassword(): void {
    if (!this.recuperarCodigo.trim() || !this.recuperarNuevaPass.trim()) {
      this.recuperarError.set('Ingresa el código OTP y tu nueva contraseña');
      return;
    }
    this.recuperarCargando.set(true);
    this.recuperarError.set('');
    this.api.resetPassword({
      email: this.recuperarEmail.trim().toLowerCase(),
      codigo: this.recuperarCodigo.trim(),
      nuevaPassword: this.recuperarNuevaPass.trim(),
    }).subscribe({
      next: () => {
        this.recuperarCargando.set(false);
        this.recuperarModal.set(false);
        this.form.patchValue({ email: this.recuperarEmail.trim().toLowerCase(), password: this.recuperarNuevaPass });
        this.ingresar();
      },
      error: (e) => {
        this.recuperarCargando.set(false);
        this.recuperarError.set(mensajeError(e, 'Código OTP inválido o expirado.'));
      },
    });
  }

  confirmarActivacionOtp(): void {
    if (!this.verificarCodigo.trim()) { this.verificarError.set('Ingresa el código de 6 dígitos'); return; }
    this.verificarCargando.set(true);
    this.verificarError.set('');
    this.api.verificarCodigo({
      email: this.verificarEmail.trim().toLowerCase(),
      codigo: this.verificarCodigo.trim(),
    }).subscribe({
      next: (auth) => {
        this.verificarCargando.set(false);
        this.verificarModal.set(false);
        this.session.save(auth.token, auth.usuario, true);
        this.router.navigateByUrl(homePorRol(auth.usuario.rol), { replaceUrl: true });
      },
      error: (e) => {
        this.verificarCargando.set(false);
        this.verificarError.set(mensajeError(e, 'Código OTP no válido o expirado.'));
      },
    });
  }

  reenviarOtpActivacion(): void {
    if (!this.verificarEmail.trim()) return;
    this.verificarCargando.set(true);
    this.api.reenviarCodigo({ email: this.verificarEmail.trim().toLowerCase(), tipo: 'VERIFICACION' }).subscribe({
      next: (msg) => {
        this.verificarCargando.set(false);
        this.verificarMensaje.set(msg || 'Nuevo código enviado con éxito.');
      },
      error: (e) => {
        this.verificarCargando.set(false);
        this.verificarError.set(mensajeError(e, 'No se pudo reenviar el código.'));
      },
    });
  }

  abrirServidor(): void { this.urlServidor = this.cfg.baseUrl(); this.servidorAbierto.set(true); }
  guardarServidor(): void { if (this.urlServidor.trim()) this.cfg.set(this.urlServidor); this.servidorAbierto.set(false); }
}
