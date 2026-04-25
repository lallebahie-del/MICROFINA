import { Component, inject } from '@angular/core';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  template: `
    <div class="toast-wrap">
      @for (t of toast.toasts(); track t.id) {
        <div class="toast" [class.success]="t.kind==='success'" [class.error]="t.kind==='error'" [class.warn]="t.kind==='warn'">
          {{ t.message }}
        </div>
      }
    </div>
  `,
})
export class ToastComponent {
  toast = inject(ToastService);
}
