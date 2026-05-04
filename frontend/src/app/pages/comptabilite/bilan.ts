import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ComptabiliteService, BilanLigne } from '../../services/comptabilite.service';

@Component({
  selector: 'app-bilan',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './bilan.html'
})
export class BilanComponent implements OnInit {
  actif: BilanLigne[] = [];
  passif: BilanLigne[] = [];
  loading = false;

  constructor(private svc: ComptabiliteService) {}

  ngOnInit(): void {
    this.loading = true;
    this.svc.getBilan().subscribe({
      next: data => {
        // vue_bilan renvoie une ligne par rubrique avec montant_actif / montant_passif.
        // On éclate en deux listes pour l'affichage Actif / Passif.
        this.actif = data
          .filter(l => (l.montantActif ?? 0) !== 0)
          .map(l => ({
            ...l,
            libellePoste: l.libelleRubrique ?? l.rubrique ?? l.numCompte ?? '',
            montant: l.montantActif ?? 0,
            typePoste: 'ACTIF' as const
          }));

        this.passif = data
          .filter(l => (l.montantPassif ?? 0) !== 0)
          .map(l => ({
            ...l,
            libellePoste: l.libelleRubrique ?? l.rubrique ?? l.numCompte ?? '',
            montant: l.montantPassif ?? 0,
            typePoste: 'PASSIF' as const
          }));

        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  total(lines: BilanLigne[]): number {
    return lines.reduce((s, l) => s + (l.montant ?? 0), 0);
  }
}
