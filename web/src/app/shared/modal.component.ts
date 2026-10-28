import { Component, input, output } from '@angular/core';
import { IconComponent } from './icon.component';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [IconComponent],
  template: `
    <div class="modal-backdrop" (click)="closed.emit()" role="presentation">
      <div class="modal" role="dialog" aria-modal="true" [attr.aria-label]="title()" (click)="$event.stopPropagation()">
        <div class="modal-head">
          <h2>{{ title() }}</h2>
          <button class="icon-btn" type="button" (click)="closed.emit()" aria-label="Cerrar"><app-icon name="x" [size]="18" /></button>
        </div>
        <div class="modal-body"><ng-content /></div>
        <div class="modal-foot"><ng-content select="[footer]" /></div>
      </div>
    </div>`,
})
export class ModalComponent {
  title = input.required<string>();
  closed = output<void>();
}
