import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule }  from '@angular/forms';
import { Router }       from '@angular/router';
import {
  WalletService, OperationWallet, WalletStatut, WalletType,
  DeblocageRequest, RemboursementRequest
} from '../../services/wallet.service';

@Component({
  selector: 'app-wallet-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './wallet-list.html',
  styleUrl: './wallet-list.css'
})
export class WalletListComponent implements OnInit {

  operations = signal<OperationWallet[]>([]);
  loading    = signal(false);
  error      = signal<string | null>(null);
  success    = signal<string | null>(null);

  // Filtres
  filtreAgence = '';
  filtreType: WalletType | '' = '';
  filtreStatut: WalletStatut | '' = '';

  // Modal initiation
  showModal       = false;
  modeModal: 'deblocage' | 'remboursement' = 'deblocage';
  form = { idCredit: null as number | null, numeroTelephone: '', montant: null as number | null, motif: '' };
  savingModal = false;
  modalError  = '';

  readonly types:   WalletType[]   = ['DEBLOCAGE', 'REMBOURSEMENT', 'DEPOT_EPARGNE'];
  readonly statuts: WalletStatut[] = ['EN_ATTENTE', 'CONFIRME', 'REJETE', 'ANNULE', 'EXPIRE'];

  constructor(private walletService: WalletService, private router: Router) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.walletService.rechercher(this.filtreAgence, this.filtreType, this.filtreStatut).subscribe({
      next:  ops => { this.operations.set(ops); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + e.message); this.loading.set(false); }
    });
  }

  onFilter():  void { this.load(); }
  onReset():   void {
    this.filtreAgence = ''; this.filtreType = ''; this.filtreStatut = '';
    this.load();
  }

  // ── Modal ─────────────────────────────────────────────────────────────────

  ouvrirDeblocage():     void { this.modeModal = 'deblocage';     this.resetForm(); this.showModal = true; }
  ouvrirRemboursement(): void { this.modeModal = 'remboursement'; this.resetForm(); this.showModal = true; }
  fermerModal():         void { this.showModal = false; this.modalError = ''; }

  resetForm(): void {
    this.form = { idCredit: null, numeroTelephone: '', montant: null, motif: '' };
    this.modalError = '';
  }

  soumettre(): void {
    if (!this.form.idCredit || !this.form.numeroTelephone) {
      this.modalError = 'Crédit et téléphone obligatoires.'; return;
    }
    if (this.modeModal === 'remboursement' && !this.form.montant) {
      this.modalError = 'Le montant est obligatoire pour un remboursement.'; return;
    }

    this.savingModal = true;
    this.modalError  = '';

    const obs$ = this.modeModal === 'deblocage'
      ? this.walletService.initierDeblocage({
          idCredit: this.form.idCredit!,
          numeroTelephone: this.form.numeroTelephone,
          motif: this.form.motif || undefined
        } as DeblocageRequest)
      : this.walletService.initierRemboursement({
          idCredit: this.form.idCredit!,
          numeroTelephone: this.form.numeroTelephone,
          montant: this.form.montant!,
          motif: this.form.motif || undefined
        } as RemboursementRequest);

    obs$.subscribe({
      next: () => {
        this.savingModal = false;
        this.showModal   = false;
        this.success.set('Opération initiée avec succès.');
        this.load();
        setTimeout(() => this.success.set(null), 4000);
      },
      error: (e) => {
        this.savingModal = false;
        this.modalError  = e.error?.message ?? e.message;
      }
    });
  }

  // ── Actions sur ligne ─────────────────────────────────────────────────────

  rafraichir(op: OperationWallet): void {
    this.walletService.rafraichirStatut(op.id).subscribe({
      next: r => {
        if (r.miseAJourEffectuee) {
          this.success.set(`Statut mis à jour : ${r.statutBankily}`);
          this.load();
          setTimeout(() => this.success.set(null), 4000);
        } else {
          this.success.set('Statut inchangé : ' + (r.message ?? r.statutBankily));
          setTimeout(() => this.success.set(null), 3000);
        }
      },
      error: e => this.error.set('Erreur rafraîchissement : ' + e.message)
    });
  }

  annuler(op: OperationWallet): void {
    if (!confirm(`Annuler l'opération ${op.referenceMfi} ?`)) return;
    this.walletService.annuler(op.id).subscribe({
      next:  () => { this.success.set('Opération annulée.'); this.load(); setTimeout(() => this.success.set(null), 3000); },
      error: e  => this.error.set('Erreur : ' + e.message)
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  couleurStatut(s: WalletStatut): string {
    switch (s) {
      case 'CONFIRME':   return 'badge-confirme';
      case 'REJETE':     return 'badge-rejete';
      case 'ANNULE':     return 'badge-annule';
      case 'EXPIRE':     return 'badge-expire';
      default:           return 'badge-attente';
    }
  }

  couleurType(t: WalletType): string {
    switch (t) {
      case 'DEBLOCAGE':     return 'type-deblocage';
      case 'REMBOURSEMENT': return 'type-remboursement';
      default:              return 'type-depot';
    }
  }

  formatMontant(v: number): string {
    return new Intl.NumberFormat('fr-MR', { style: 'currency', currency: 'MRU',
      minimumFractionDigits: 0 }).format(v);
  }

  isEnAttente(op: OperationWallet): boolean { return op.statut === 'EN_ATTENTE'; }
}
