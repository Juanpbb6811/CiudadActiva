import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { mensajeError } from '../../core/constants';
import { SessionService } from '../../core/session.service';
import { IconComponent } from '../../shared/icon.component';

const coinciden = (g: AbstractControl): ValidationErrors | null =>
  g.get('password')?.value === g.get('repetir')?.value ? null : { noCoinciden: true };

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, IconComponent],
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

  registrar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    this.cargando.set(true);
    this.error.set('');
    this.api.registrar({ nombre: v.nombre.trim(), email: v.email.trim().toLowerCase(), password: v.password, telefono: v.telefono.trim() || null }).subscribe({
      next: (auth) => {
        this.session.save(auth.token, auth.usuario, true);
        this.router.navigateByUrl('/ciudadano/inicio', { replaceUrl: true });
      },
      error: (e) => { this.cargando.set(false); this.error.set(mensajeError(e, 'No se pudo registrar la cuenta. Verifique los datos ingresados.')); },
    });
  }
}
