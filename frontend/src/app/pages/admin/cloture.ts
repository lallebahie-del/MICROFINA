import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminClotureService } from '../../services/admin-cloture.service';

@Component({
  selector: 'app-admin-cloture',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cloture.html'
})
export class AdminClotureComponent {

  private now = new Date();

  // Mensuelle
  anneeMensuelle: number = this.now.getFullYear();
  moisMensuel:    number = this.now.getMonth() + 1;

  // Annuelle
  anneeAnnuelle: number = this.now.getFullYear() - 1;

  running = signal<string | null>(null);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  readonly mois = [
    { v: 1, l: 'Janvier' },   { v: 2, l: 'Février' },  { v: 3, l: 'Mars' },
    { v: 4, l: 'Avril' },     { v: 5, l: 'Mai' },      { v: 6, l: 'Juin' },
    { v: 7, l: 'Juillet' },   { v: 8, l: 'Août' },     { v: 9, l: 'Septembre' },
    { v: 10, l: 'Octobre' },  { v: 11, l: 'Novembre' },{ v: 12, l: 'Décembre' }
  ];

  // Années disponibles : 5 dernières + année courante
  readonly annees = (() => {
    const y = new Date().getFullYear();
    const list: number[] = [];
    for (let i = 0; i <= 5; i++) list.push(y - i);
    return list;
  })();

  constructor(private svc: AdminClotureService) {}

  clotureMensuelle(): void {
    if (!confirm(`Clôturer la période ${this.moisMensuel}/${this.anneeMensuelle} ?\nCette action est définitive.`)) return;
    this.running.set('mensuelle');
    this.error.set(null);
    this.success.set(null);

    this.svc.mensuelle(this.anneeMensuelle, this.moisMensuel).subscribe({
      next: r => {
        this.running.set(null);
        this.success.set(`Période ${r.mois}/${r.annee} : ${r.statut}`);
      },
      error: e => {
        this.running.set(null);
        this.error.set('Erreur clôture mensuelle : ' + (e.error?.message ?? e.message));
      }
    });
  }

  clotureAnnuelle(): void {
    if (!confirm(`Clôturer l'exercice ${this.anneeAnnuelle} ?\nCette action est définitive et nécessite que toutes les périodes mensuelles soient clôturées.`)) return;
    this.running.set('annuelle');
    this.error.set(null);
    this.success.set(null);

    this.svc.annuelle(this.anneeAnnuelle).subscribe({
      next: r => {
        this.running.set(null);
        this.success.set(`Exercice ${r.annee} : ${r.statut}`);
      },
      error: e => {
        this.running.set(null);
        this.error.set('Erreur clôture annuelle : ' + (e.error?.message ?? e.message));
      }
    });
  }
}
