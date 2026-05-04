import { Component, OnInit, signal } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { ActivatedRoute, Router }    from '@angular/router';
import { CreditsService, Credit, CreditStatut } from '../../services/credits.service';

@Component({
  selector: 'app-credit-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './credit-detail.html'
})
export class CreditDetailComponent implements OnInit {

  credit  = signal<Credit | null>(null);
  loading = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  constructor(
    private route:   ActivatedRoute,
    private router:  Router,
    private service: CreditsService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.router.navigate(['/credits']); return; }
    this.loading.set(true);
    this.service.getOne(id).subscribe({
      next:  c => { this.credit.set(c); this.loading.set(false); },
      error: e => { this.error.set('Erreur : ' + e.message); this.loading.set(false); }
    });
  }

  transitionner(statut: CreditStatut): void {
    const c = this.credit();
    if (!c?.idCredit) return;
    const labels: Record<string, string> = {
      SOUMIS:         'Soumettre ce crédit',
      VALIDE_AGENT:   'Valider en tant qu\'agent',
      VALIDE_COMITE:  'Valider en tant que comité',
      DEBLOQUE:       'Débloquer (décaisser) ce crédit',
      REJETE:         'Rejeter ce crédit',
      SOLDE:          'Marquer comme soldé',
    };
    if (!confirm(`${labels[statut] ?? statut} ?`)) return;

    this.error.set(null);
    this.service.transitionner(c.idCredit, statut).subscribe({
      next: updated => {
        this.credit.set(updated);
        this.success.set(`Statut mis à jour : ${updated.statut}`);
        setTimeout(() => this.success.set(null), 3000);
      },
      error: e => this.error.set(e.error?.message ?? e.message)
    });
  }

  edit(): void {
    const c = this.credit();
    if (c?.idCredit) this.router.navigate(['/credits', c.idCredit, 'edit']);
  }

  retour(): void { this.router.navigate(['/credits']); }

  badgeClass(statut: string): string {
    switch (statut) {
      case 'BROUILLON':     return 'badge badge-warning';
      case 'SOUMIS':        return 'badge badge-info';
      case 'VALIDE_AGENT':  return 'badge badge-info';
      case 'VALIDE_COMITE': return 'badge badge-primary';
      case 'DEBLOQUE':      return 'badge badge-success';
      case 'SOLDE':         return 'badge badge-success';
      case 'REJETE':        return 'badge badge-danger';
      default:              return 'badge';
    }
  }
}
