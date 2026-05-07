import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ComptabiliteService, BilanLigne } from '../../services/comptabilite.service';

interface BilanRow extends BilanLigne {
  libellePoste: string;
  montant: number;
  typePoste: 'ACTIF' | 'PASSIF';
}

@Component({
  selector: 'app-bilan',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './bilan.html'
})
export class BilanComponent implements OnInit {
  actif   = signal<BilanRow[]>([]);
  passif  = signal<BilanRow[]>([]);
  loading = signal(false);
  error   = signal<string | null>(null);

  totalActif  = computed<number>(() => this.actif().reduce((s, l) => s + (l.montant ?? 0), 0));
  totalPassif = computed<number>(() => this.passif().reduce((s, l) => s + (l.montant ?? 0), 0));

  constructor(private svc: ComptabiliteService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getBilan().subscribe({
      next: data => {
        this.actif.set(data
          .filter(l => (l.montantActif ?? 0) !== 0)
          .map(l => ({
            ...l,
            libellePoste: l.libelleRubrique ?? l.rubrique ?? l.numCompte ?? '',
            montant: l.montantActif ?? 0,
            typePoste: 'ACTIF' as const
          })));

        this.passif.set(data
          .filter(l => (l.montantPassif ?? 0) !== 0)
          .map(l => ({
            ...l,
            libellePoste: l.libelleRubrique ?? l.rubrique ?? l.numCompte ?? '',
            montant: l.montantPassif ?? 0,
            typePoste: 'PASSIF' as const
          })));

        this.loading.set(false);
      },
      error: e => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }
}
