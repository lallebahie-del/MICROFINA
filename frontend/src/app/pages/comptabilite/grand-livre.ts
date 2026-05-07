import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComptabiliteService, GrandLivreLigne } from '../../services/comptabilite.service';

@Component({
  selector: 'app-grand-livre',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './grand-livre.html'
})
export class GrandLivreComponent implements OnInit {
  lignes  = signal<GrandLivreLigne[]>([]);
  agence  = signal<string>('');
  compte  = signal<string>('');
  loading = signal(false);
  error   = signal<string | null>(null);

  totalDebit  = computed<number>(() =>
    this.lignes().reduce((s, l) => s + (l.debit  ?? 0), 0)
  );
  totalCredit = computed<number>(() =>
    this.lignes().reduce((s, l) => s + (l.credit ?? 0), 0)
  );
  soldeNet = computed<number>(() => this.totalDebit() - this.totalCredit());

  constructor(private svc: ComptabiliteService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getGrandLivre(this.agence() || undefined, this.compte() || undefined).subscribe({
      next: data => { this.lignes.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  reset(): void {
    this.agence.set('');
    this.compte.set('');
    this.load();
  }
}
