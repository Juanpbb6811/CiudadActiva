import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators, FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { mensajeError } from '../../core/constants';
import { SessionService } from '../../core/session.service';
import { IconComponent } from '../../shared/icon.component';
import { ModalComponent } from '../../shared/modal.component';

const coinciden = (g: AbstractControl): ValidationErrors | null =>
  g.get('password')?.value === g.get('repetir')?.value ? null : { noCoinciden: true };

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, RouterLink, IconComponent, ModalComponent],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private session = inject(SessionService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    repetir: ['', Validators.required],
    telefono: [''],
    acepto: [false, Validators.requiredTrue],
  }, { validators: coinciden });
  cargando = signal(false);
  error = signal('');

  // Verificación OTP
  otpModal = signal(false);
  emailRegistrado = '';
  codigoOtp = '';
  otpCargando = signal(false);
  otpError = signal('');
  otpExito = signal('');

  registrar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    this.cargando.set(true);
    this.error.set('');
    this.emailRegistrado = v.email.trim().toLowerCase();
    this.api.registrar({
      nombre: v.nombre.trim(),
      email: this.emailRegistrado,
      password: v.password,
      telefono: v.telefono.trim() || null
    }).subscribe({
      next: (auth) => {
        this.cargando.set(false);
        // Si requiere verificación OTP, abrir modal
        if (auth.usuario && !auth.usuario.activo) {
          this.otpModal.set(true);
        } else {
          this.session.save(auth.token, auth.usuario, true);
          this.router.navigateByUrl('/ciudadano/inicio', { replaceUrl: true });
        }
      },
      error: (e) => {
        this.cargando.set(false);
        this.error.set(mensajeError(e, 'No se pudo registrar la cuenta. Verifique los datos ingresados.'));
      },
    });
  }

  confirmarOtp(): void {
    if (!this.codigoOtp.trim()) { this.otpError.set('Ingresa el código OTP de 6 dígitos'); return; }
    this.otpCargando.set(true);
    this.otpError.set('');
    this.api.verificarCodigo({
      email: this.emailRegistrado,
      codigo: this.codigoOtp.trim()
    }).subscribe({
      next: (auth) => {
        this.otpCargando.set(false);
        this.otpModal.set(false);
        this.session.save(auth.token, auth.usuario, true);
        this.router.navigateByUrl('/ciudadano/inicio', { replaceUrl: true });
      },
      error: (e) => {
        this.otpCargando.set(false);
        this.otpError.set(mensajeError(e, 'Código no válido o expirado.'));
      }
    });
  }

  reenviarOtp(): void {
    this.otpCargando.set(true);
    this.api.reenviarCodigo({ email: this.emailRegistrado, tipo: 'VERIFICACION' }).subscribe({
      next: (msg) => {
        this.otpCargando.set(false);
        this.otpExito.set(msg || 'Nuevo código OTP enviado con éxito.');
      },
      error: (e) => {
        this.otpCargando.set(false);
        this.otpError.set(mensajeError(e, 'No se pudo reenviar el código.'));
      }
    });
  }
}
