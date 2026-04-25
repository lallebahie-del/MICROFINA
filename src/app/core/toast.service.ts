import { Injectable, signal } from '@angular/core';

export type ToastKind = 'info' | 'success' | 'error' | 'warn';
export interface Toast { id: number; kind: ToastKind; message: string; }

@Injectable({ providedIn: 'root' })
export class ToastService {
  toasts = signal<Toast[]>([]);
  private seq = 0;

  show(message: string, kind: ToastKind = 'info', timeoutMs = 3500) {
    const id = ++this.seq;
    this.toasts.update((arr) => [...arr, { id, kind, message }]);
    setTimeout(() => this.dismiss(id), timeoutMs);
  }
  success(msg: string) { this.show(msg, 'success'); }
  error(msg: string)   { this.show(msg, 'error', 5000); }
  warn(msg: string)    { this.show(msg, 'warn'); }
  dismiss(id: number) { this.toasts.update((arr) => arr.filter((t) => t.id !== id)); }
}
