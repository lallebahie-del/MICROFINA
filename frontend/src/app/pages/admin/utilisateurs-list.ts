import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, Utilisateur, UtilisateurCreate, UtilisateurUpdate, Role, Privilege } from '../../services/admin.service';
import { AgencesService, Agence } from '../../services/agences.service';
import { AuthService } from '../../core/auth.service';
import { PaginationBarComponent } from '../../components/pagination-bar/pagination-bar.component';
import { DEFAULT_LIST_PAGE_SIZE } from '../../shared/list-pagination';

@Component({
  selector: 'app-utilisateurs-list',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginationBarComponent],
  templateUrl: './utilisateurs-list.html',
  styleUrls: ['./utilisateurs-list.css']
})
export class UtilisateursListComponent implements OnInit {

  private all = signal<Utilisateur[]>([]);
  agences    = signal<Agence[]>([]);
  allRoles   = signal<Role[]>([]);

  search      = signal('');
  filtreActif = signal<string>('');
  filtreAgence = signal<string>('');

  utilisateurs = computed<Utilisateur[]>(() => {
    const q    = this.search().trim().toLowerCase();
    const fActif = this.filtreActif();
    const fAg    = this.filtreAgence();
    return this.all().filter(u => {
      if (fActif !== '' && (fActif === 'true') !== !!u.actif) return false;
      if (fAg && u.codeAgence !== fAg) return false;
      if (!q) return true;
      return (u.login?.toLowerCase().includes(q))
          || (u.nomComplet?.toLowerCase().includes(q) ?? false)
          || (u.email?.toLowerCase().includes(q) ?? false);
    });
  });

  /** Pagination locale (liste filtrée) — même taille que les autres listes */
  readonly pageSizeUtilisateurs = DEFAULT_LIST_PAGE_SIZE;
  pageUtilisateurs = signal(0);

  totalUserPages = computed(() => {
    const n = this.utilisateurs().length;
    return n === 0 ? 0 : Math.ceil(n / this.pageSizeUtilisateurs);
  });

  /** Index de page effectif (borne si la liste rétrécit après filtre / suppression) */
  activeUserPage = computed(() => {
    const tp = this.totalUserPages();
    if (tp === 0) return 0;
    return Math.min(this.pageUtilisateurs(), tp - 1);
  });

  pagedUtilisateurs = computed<Utilisateur[]>(() => {
    const list = this.utilisateurs();
    if (list.length === 0) return [];
    const start = this.activeUserPage() * this.pageSizeUtilisateurs;
    return list.slice(start, start + this.pageSizeUtilisateurs);
  });

  loading = signal(false);
  saving  = signal(false);
  error   = signal<string | null>(null);
  success = signal<string | null>(null);

  showForm    = signal(false);
  editingId   = signal<number | null>(null);
  showResetMdp = signal<number | null>(null);
  newMdp = '';

  // ── Modal Privilèges (admin uniquement) ──────────────────────────
  private auth = inject(AuthService);
  isAdmin = computed(() => {
    const u = this.auth.currentUser();
    if (!u) return false;
    const role = (u.role || '').toUpperCase();
    if (role === 'ADMIN' || role === 'ROLE_ADMIN' || role.includes('ADMIN')) return true;
    // Fallback : autoriser tout utilisateur qui a le privilège de gérer les comptes
    return this.auth.hasAnyPrivilege('PRIV_MANAGE_USERS');
  });

  showPrivModal = signal<Utilisateur | null>(null);
  allPrivileges = signal<Privilege[]>([]);
  privSelectedRoles: string[] = [];
  privSelectedCodes: string[] = [];
  privSaving = signal(false);

  privModules = computed<string[]>(() =>
    Array.from(new Set(this.allPrivileges().map(p => p.module))).sort()
  );

  privilegesByModule(mod: string): Privilege[] {
    return this.allPrivileges().filter(p => p.module === mod);
  }

  togglePrivCode(code: string): void {
    const idx = this.privSelectedCodes.indexOf(code);
    if (idx >= 0) this.privSelectedCodes.splice(idx, 1);
    else this.privSelectedCodes.push(code);
  }
  isPrivCodeSelected(code: string): boolean {
    return this.privSelectedCodes.includes(code);
  }
  toggleAllPrivsOfModule(mod: string): void {
    const codes = this.privilegesByModule(mod).map(p => p.codePrivilege);
    const allSelected = codes.every(c => this.isPrivCodeSelected(c));
    if (allSelected) {
      this.privSelectedCodes = this.privSelectedCodes.filter(c => !codes.includes(c));
    } else {
      const set = new Set(this.privSelectedCodes);
      codes.forEach(c => set.add(c));
      this.privSelectedCodes = [...set];
    }
  }
  allPrivsOfModuleSelected(mod: string): boolean {
    const codes = this.privilegesByModule(mod).map(p => p.codePrivilege);
    return codes.length > 0 && codes.every(c => this.isPrivCodeSelected(c));
  }

  form: UtilisateurCreate = { login: '', motDePasse: '', actif: true };
  selectedRoles: string[] = [];

  constructor(
    private adminService: AdminService,
    private agencesSvc: AgencesService
  ) {}

  ngOnInit(): void {
    this.load();
    this.agencesSvc.getAll(true).subscribe({ next: list => this.agences.set(list) });
    this.adminService.getRoles().subscribe({ next: list => this.allRoles.set(list) });
    this.adminService.getPrivileges().subscribe({ next: list => this.allPrivileges.set(list) });
  }

  load(): void {
    this.loading.set(true);
    this.adminService.getUtilisateurs().subscribe({
      next: data => {
        this.all.set(data);
        this.loading.set(false);
        this.clampUserPage();
      },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  goUserPage(p: number): void {
    const tp = this.totalUserPages();
    if (tp === 0) {
      this.pageUtilisateurs.set(0);
      return;
    }
    this.pageUtilisateurs.set(Math.max(0, Math.min(p, tp - 1)));
  }

  private clampUserPage(): void {
    const tp = this.totalUserPages();
    if (tp === 0) {
      this.pageUtilisateurs.set(0);
      return;
    }
    if (this.pageUtilisateurs() > tp - 1) {
      this.pageUtilisateurs.set(tp - 1);
    }
  }

  onSearchChange(v: string): void {
    this.search.set(v);
    this.pageUtilisateurs.set(0);
  }

  onFiltreActifChange(v: string): void {
    this.filtreActif.set(v);
    this.pageUtilisateurs.set(0);
  }

  onFiltreAgenceChange(v: string): void {
    this.filtreAgence.set(v);
    this.pageUtilisateurs.set(0);
  }

  resetFilters(): void {
    this.search.set('');
    this.filtreActif.set('');
    this.filtreAgence.set('');
    this.pageUtilisateurs.set(0);
  }

  openNew(): void {
    this.form = { login: '', motDePasse: '', actif: true };
    this.selectedRoles = [];
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
    this.selectedRoles = u.roles ? [...u.roles] : [];
    this.editingId.set(u.id);
    this.showForm.set(true);
    this.error.set(null);
    this.success.set(null);
  }

  cancel(): void {
    this.showForm.set(false);
    this.form = { login: '', motDePasse: '', actif: true };
    this.selectedRoles = [];
  }

  toggleRole(code: string): void {
    const idx = this.selectedRoles.indexOf(code);
    if (idx >= 0) {
      this.selectedRoles.splice(idx, 1);
    } else {
      this.selectedRoles.push(code);
    }
  }

  isRoleSelected(code: string): boolean {
    return this.selectedRoles.includes(code);
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
      ? this.adminService.updateUtilisateur(id, { ...this.form as UtilisateurUpdate, roles: this.selectedRoles })
      : this.adminService.createUtilisateur({ ...this.form, roles: this.selectedRoles });

    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set(id ? 'Utilisateur mis à jour.' : 'Utilisateur créé.');
        this.showForm.set(false);
        this.selectedRoles = [];
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

  // ── Privilèges (via rôles) ───────────────────────────────────────
  openPrivileges(u: Utilisateur): void {
    if (!this.isAdmin()) return;
    this.showPrivModal.set(u);
    this.privSelectedRoles = u.roles ? [...u.roles] : [];
    this.privSelectedCodes = [];
    this.error.set(null);
    this.success.set(null);
    // Charge la liste actuelle des privilèges directs accordés à cet utilisateur
    this.adminService.getUtilisateurPrivileges(u.id).subscribe({
      next: codes => { this.privSelectedCodes = codes || []; },
      error: () => { this.privSelectedCodes = []; }
    });
  }

  closePrivileges(): void {
    this.showPrivModal.set(null);
    this.privSelectedRoles = [];
    this.privSelectedCodes = [];
  }

  togglePrivRole(code: string): void {
    const idx = this.privSelectedRoles.indexOf(code);
    if (idx >= 0) this.privSelectedRoles.splice(idx, 1);
    else this.privSelectedRoles.push(code);
  }

  isPrivRoleSelected(code: string): boolean {
    return this.privSelectedRoles.includes(code);
  }

  submitPrivileges(): void {
    const u = this.showPrivModal();
    if (!u) return;
    this.privSaving.set(true);
    this.error.set(null);

    // Étape 1 : met à jour les rôles, puis les privilèges directs
    const req: UtilisateurUpdate = {
      nomComplet: u.nomComplet,
      email: u.email,
      telephone: u.telephone,
      actif: u.actif,
      dateExpirationCompte: u.dateExpirationCompte,
      codeAgence: u.codeAgence,
      roles: this.privSelectedRoles
    };
    this.adminService.updateUtilisateur(u.id, req).subscribe({
      next: () => {
        this.adminService.setUtilisateurPrivileges(u.id, this.privSelectedCodes).subscribe({
          next: () => {
            this.privSaving.set(false);
            this.success.set(`Privilèges mis à jour pour ${u.login}.`);
            this.showPrivModal.set(null);
            this.privSelectedRoles = [];
            this.privSelectedCodes = [];
            this.load();
          },
          error: e => {
            this.privSaving.set(false);
            this.error.set('Erreur privilèges : ' + (e.error?.message ?? e.message));
          }
        });
      },
      error: e => {
        this.privSaving.set(false);
        this.error.set('Erreur rôles : ' + (e.error?.message ?? e.message));
      }
    });
  }
}
