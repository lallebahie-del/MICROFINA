import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="empty">
      <h2>404 — Page introuvable</h2>
      <p>La page que vous recherchez n'existe pas.</p>
      <a class="btn" routerLink="/">Retour à l'accueil</a>
    </div>
  `,
})
export class NotFoundComponent {}
