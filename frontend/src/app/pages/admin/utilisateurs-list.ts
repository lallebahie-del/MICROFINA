import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, Utilisateur, UtilisateurCreate, UtilisateurUpdate } from '../../services/admin.service';
import { AgencesService, Agence } from '../../services/agences.service';

@Component({
  selector: 'app-utilisateurs-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './utilisateurs-list.html'
})
export class UtilisateursListComponent implements OnInit {

  private all = signal<Utilisateur[]>([]);
  agences = signal<Agence[]>([]);

  search = signal('');
  filtreActif = signal<string>('');
  filtreAgence = signal<string>('');

  utilisateurs = computed<Utilisateur[]>(() => {
    const q = this.search().trim().toLowerCase();
    const fActif = this.filtreActif();
    const fAg = this.filtreAgence();
    return this.all().filter(u => {
      if (fActif !== '' && (fActif === 'true') !== !!u.actif) return false;
      if (fAg && u.codeAgence !== fAg) return false;
      if (!q) return true;
      return (u.login?.toLowerCase().includes(q))
          || (u.nomComplet?.toLowerCase().includes(q) ?? false)
          || (u.email?.toLowerCase().includes(q) ?? false);
    });
  });

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm   = signal(false);
  editingId  = signal<number | null>(null);
  showResetMdp = signal<number | null>(null);
  newMdp = '';

  form: UtilisateurCreate = { login: '', motDePasse: '', actif: true };

  constructor(
    private adminService: AdminService,
    private agencesSvc: AgencesService
  ) {}

  ngOnInit(): void {
    this.load();
    this.agencesSvc.getAll(true).subscribe({ next: list => this.agences.set(list) });
  }

  load(): void {
    this.loading.set(true);
    this.adminService.getUtilisateurs().subscribe({
      next: data => { this.all.set(data); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  resetFilters(): void {
    this.search.set('');
    this.filtreActif.set('');
    this.filtreAgence.set('');
  }

  openNew(): void {
    this.form = { login: '', motDePasse: '', actif: true };
    this.editingId.set(null);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  openEdit(u: Utilisateur): void {
    this.form = {
      login: u.login,
      motDePasse: '',
      nomComplet: u.nomComplet,
      email: u.email,
      telephone: u.telephone,
      actif: u.actif,
      dateExpirationCompte: u.dateExpirationCompte,
      codeAgence: u.codeAgence
    };
    this.editingId.set(u.id);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
    this.form = { login: '', motDePasse: '', actif: true };
  }

  submit(): void {
    if (!this.form.login) {
      this.error.set('Le login est obligatoire.');
      return;
    }
    const id = this.editingId();
    if (!id && !this.form.motDePasse) {
      this.error.set('Le mot de passe est obligatoire à la création.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const call = id
      ? this.adminService.updateUtilisateur(id, this.form as UtilisateurUpdate)
      : this.adminService.createUtilisateur(this.form);

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(id ? 'Utilisateur mis à jour.' : 'Utilisateur créé.');
        this.showForm.set(false);
        this.load();
      },
      error: e => {
        this.saving.set(false);
        this.error.set('Erreur : ' + (e.error?.message ?? e.message));
      }
    });
  }

  desactiver(id: number, login: string): void {
    if (!confirm(`Désactiver l'utilisateur ${login} ?`)) return;
    this.adminService.desactiverUtilisateur(id).subscribe({
      next: () => { this.success.set('Utilisateur désactivé.'); this.load(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }

  delete(id: number, login: string): void {
    if (!confirm(`Supprimer définitivement ${login} ?`)) return;
    this.adminService.deleteUtilisateur(id).subscribe({
      next: () => { this.success.set('Utilisateur supprimé.'); this.load(); },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }

  openResetMdp(id: number): void {
    this.showResetMdp.set(id);
    this.newMdp = '';
  }

  submitResetMdp(): void {
    const id = this.showResetMdp();
    if (!id || !this.newMdp || this.newMdp.length < 8) {
      this.error.set('Le mot de passe doit contenir au moins 8 caractères.');
      return;
    }
    this.adminService.reinitialiserMdp(id, this.newMdp).subscribe({
      next: () => {
        this.success.set('Mot de passe réinitialisé.');
        this.showResetMdp.set(null);
        this.newMdp = '';
      },
      error: e => this.error.set('Erreur : ' + (e.error?.message ?? e.message))
    });
  }
}
