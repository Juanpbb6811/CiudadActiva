import { Component, inject, input, output } from '@angular/core';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { IconComponent } from './icon.component';

/** Equivale a CiudadActivaTopBar + el botón "←" con retrocederSeguro() de AppNavigation.kt. */
@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [IconComponent],
  template: `
    <header class="topbar">
      @if (back()) {
        <button class="icon-btn" type="button" (click)="goBack()" aria-label="Volver"><app-icon name="arrow-left" [size]="20" /></button>
      }
      <div class="topbar-title">
        <h1>{{ title() }}</h1>
        @if (subtitle()) { <small>{{ subtitle() }}</small> }
      </div>
      <div class="topbar-actions"><ng-content /></div>
    </header>`,
})
export class TopBarComponent {
  title = input.required<string>();
  subtitle = input<string>('');
  back = input(false);
  fallback = input('/');
  private loc = inject(Location);
  private router = inject(Router);

  goBack(): void {
    // retrocederSeguro: si no hay historial en la app, navega a la ruta de respaldo.
    if (history.length > 1 && (history.state?.navigationId ?? 0) > 1) this.loc.back();
    else this.router.navigateByUrl(this.fallback());
  }
}
