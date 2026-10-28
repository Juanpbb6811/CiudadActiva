import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { homePorRol } from '../../core/constants';
import { SessionService } from '../../core/session.service';

@Component({
  selector: 'app-splash',
  standalone: true,
  template: `
    <div class="auth-bg">
      <div class="center" style="color:#fff">
        <div class="auth-logo"><div class="logo-big">C</div></div>
        <h1 style="font-size:30px;font-weight:600">ciudad<b style="color:var(--copper)">activa</b></h1>
        <p style="opacity:.8;margin-top:6px">Plataforma Comunitaria de Incidencias Urbanas</p>
        <p style="opacity:.55;margin-top:28px;font-size:12px">Iniciando módulos RUP...</p>
      </div>
    </div>`,
})
export class SplashComponent implements OnInit {
  private router = inject(Router);
  private session = inject(SessionService);

  ngOnInit(): void {
    setTimeout(() => this.router.navigateByUrl(this.session.isLoggedIn() ? homePorRol(this.session.rol()) : '/login', { replaceUrl: true }), 1400);
  }
}
