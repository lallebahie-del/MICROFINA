import { Component, OnInit, signal } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { FormsModule }               from '@angular/forms';
import { ActivatedRoute, Router }    from '@angular/router';
import { MembresService, Membre }    from '../../services/membres.service';

@Component({
  selector: 'app-membre-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './membre-form.html'
})
export class MembreFormComponent implements OnInit {

  // ── Mode ───────────────────────────────────────────────────────
  isNew    = true;
  numMembre: string | null = null;

  // ── State ──────────────────────────────────────────────────────
  loading  = signal(false);
  saving   = signal(false);
  error    = signal<string | null>(null);
  success  = signal<string | null>(null);

  // ── Form model ─────────────────────────────────────────────────
  form: Partial<Membre> = {
    dtype:    'PP',
    sexe:     'M',
    statut:   'DEMANDE',
    etat:     'ACTIF'
  };

  constructor(
    private route:   ActivatedRoute,
    private router:  Router,
    private service: MembresService
  ) {}

  ngOnInit(): void {
    this.numMembre = this.route.snapshot.paramMap.get('numMembre');
    this.isNew     = (this.numMembre === 'nouveau' || !this.numMembre);

    if (!this.isNew && this.numMembre) {
      this.loading.set(true);
      this.service.getOne(this.numMembre).subscribe({
        next:  m  => { this.form = { ...m }; this.loading.set(false); },
        error: e  => { this.error.set('Erreur de chargement : ' + e.message); this.loading.set(false); }
      });
    }
  }

  submit(): void {
    if (!this.form.numMembre?.trim() && this.isNew) {
      this.error.set('Le numéro membre est obligatoire.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    const call = this.isNew
      ? this.service.create(this.form)
      : this.service.update(this.numMembre!, this.form);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(this.isNew ? 'Membre créé avec succès.' : 'Membre mis à jour.');
        setTimeout(() => this.router.navigate(['/membres']), 1200);
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur lors de la sauvegarde : ' + (e.error?.message ?? e.message ?? e));
      }
    });
  }

  annuler(): void { this.router.navigate(['/membres']); }
}
