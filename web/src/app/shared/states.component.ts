import { Component, input, output } from '@angular/core';
import { IconComponent } from './icon.component';

@Component({
  selector: 'app-loading',
  standalone: true,
  template: `<div class="skeleton-list">@for (i of rows(); track i) { <div class="skeleton"></div> }</div>`,
})
export class LoadingComponent {
  n = input(3);
  rows() { return Array.from({ length: this.n() }, (_, i) => i); }
}

@Component({
  selector: 'app-error-view',
  standalone: true,
  imports: [IconComponent],
  template: `
    <div class="alert error">
      <app-icon name="alert" [size]="18" />
      <span>{{ message() }}</span>
      @if (retry()) { <button class="btn ghost btn-sm" type="button" (click)="retried.emit()">Reintentar</button> }
    </div>`,
})
export class ErrorViewComponent {
  message = input.required<string>();
  retry = input(true);
  retried = output<void>();
}

@Component({
  selector: 'app-empty',
  standalone: true,
  template: `
    <div class="empty">
      <div class="empty-emoji">{{ emoji() }}</div>
      <strong>{{ title() }}</strong>
      @if (text()) { <p>{{ text() }}</p> }
      <ng-content />
    </div>`,
})
export class EmptyComponent {
  emoji = input('📭');
  title = input.required<string>();
  text = input('');
}
