import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout/layout';
import { HomeComponent }   from './pages/home/home';
import { LoginComponent }  from './pages/login/login';
import { authGuard }       from './core/auth.guard';

export const routes: Routes = [
  // ── Public ──────────────────────────────────────────────────────────
  {
    path: 'login',
    component: LoginComponent
  },

  // ── Protected – requires authentication ──────────────────────────────
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: HomeComponent },

      // ── Membres ──────────────────────────────────────────────────
      {
        path: 'membres',
        loadComponent: () =>
          import('./pages/membres/membres-list').then(m => m.MembresListComponent)
      },
      {
        path: 'membres/nouveau',
        loadComponent: () =>
          import('./pages/membres/membre-form').then(m => m.MembreFormComponent)
      },
      {
        path: 'membres/:numMembre/edit',
        loadComponent: () =>
          import('./pages/membres/membre-form').then(m => m.MembreFormComponent)
      },

      // ── Produits Crédit ───────────────────────────────────────────
      {
        path: 'produits-credit',
        loadComponent: () =>
          import('./pages/produits-credit/produits-list').then(m => m.ProduitsListComponent)
      },
      {
        path: 'produits-credit/nouveau',
        loadComponent: () =>
          import('./pages/produits-credit/produit-form').then(m => m.ProduitFormComponent)
      },
      {
        path: 'produits-credit/:numProduit/edit',
        loadComponent: () =>
          import('./pages/produits-credit/produit-form').then(m => m.ProduitFormComponent)
      },

      // ── Crédits ───────────────────────────────────────────────────
      {
        path: 'credits',
        loadComponent: () =>
          import('./pages/credits/credits-list').then(m => m.CreditsListComponent)
      },
      {
        path: 'credits/nouveau',
        loadComponent: () =>
          import('./pages/credits/credit-form').then(m => m.CreditFormComponent)
      },
      {
        path: 'credits/comite',
        loadComponent: () =>
          import('./pages/credits/credits-comite-page.component').then(m => m.CreditsComitePageComponent)
      },
      {
        path: 'credits/:id',
        loadComponent: () =>
          import('./pages/credits/credit-detail').then(m => m.CreditDetailComponent)
      },
      {
        path: 'credits/:id/edit',
        loadComponent: () =>
          import('./pages/credits/credit-form').then(m => m.CreditFormComponent)
      },
      {
        path: 'credits/:id/workflow',
        loadComponent: () =>
          import('./pages/credits/credit-workflow-page.component').then(m => m.CreditWorkflowPageComponent)
      },

      // ── Comptabilité ──────────────────────────────────────────────
      {
        path: 'comptabilite',
        loadComponent: () =>
          import('./pages/comptabilite/comptabilite-list').then(m => m.ComptabiliteListComponent)
      },
      {
        path: 'comptabilite/grand-livre',
        loadComponent: () =>
          import('./pages/comptabilite/grand-livre').then(m => m.GrandLivreComponent)
      },
      {
        path: 'comptabilite/balance',
        loadComponent: () =>
          import('./pages/comptabilite/balance').then(m => m.BalanceComponent)
      },
      {
        path: 'comptabilite/journal',
        loadComponent: () =>
          import('./pages/comptabilite/journal').then(m => m.JournalComponent)
      },
      {
        path: 'comptabilite/bilan',
        loadComponent: () =>
          import('./pages/comptabilite/bilan').then(m => m.BilanComponent)
      },

      // ── Épargne ───────────────────────────────────────────────────
      {
        path: 'epargne',
        loadComponent: () =>
          import('./pages/epargne/epargne-list').then(m => m.EpargneListComponent)
      },
      {
        path: 'comptes-epargne',
        loadComponent: () =>
          import('./pages/comptes-epargne/comptes-epargne-list').then(m => m.ComptesEpargneListComponent)
      },

      // ── Carnets de chèques ────────────────────────────────────────
      {
        path: 'carnets-cheque',
        loadComponent: () =>
          import('./pages/carnets-cheque/carnets-cheque-list').then(m => m.CarnetsChequeListComponent)
      },

      // ── Garanties ─────────────────────────────────────────────────
      {
        path: 'garanties',
        loadComponent: () =>
          import('./pages/garanties/garanties-list').then(m => m.GarantiesListComponent)
      },

      // ── Agences ───────────────────────────────────────────────────
      {
        path: 'agences',
        loadComponent: () =>
          import('./pages/agences/agences-list').then(m => m.AgencesListComponent)
      },

      // ── Opérations de caisse ──────────────────────────────────────
      {
        path: 'operations-caisse',
        loadComponent: () =>
          import('./pages/operations-caisse/operations-caisse-list').then(m => m.OperationsCaisseListComponent)
      },

      // ── Opérations bancaires ──────────────────────────────────────
      {
        path: 'operations-banque',
        loadComponent: () =>
          import('./pages/operations-banque/operations-banque-list').then(m => m.OperationsBanqueListComponent)
      },

      // ── Exports ───────────────────────────────────────────────────
      {
        path: 'exports',
        loadComponent: () =>
          import('./pages/exports/exports').then(m => m.ExportsComponent)
      },

      // ── Reporting BCM ─────────────────────────────────────────────
      {
        path: 'reporting',
        loadComponent: () =>
          import('./pages/reporting/reporting').then(m => m.ReportingComponent)
      },

      // ── Wallet Bankily ────────────────────────────────────────────
      {
        path: 'wallet',
        loadComponent: () =>
          import('./pages/wallet/wallet-list').then(m => m.WalletListComponent)
      },

      // ── Cartographie GeoJSON ──────────────────────────────────────
      {
        path: 'cartographie',
        loadComponent: () =>
          import('./pages/cartographie/cartographie').then(m => m.CartographieComponent)
      },

      // ── Banques ───────────────────────────────────────────────────
      {
        path: 'banques',
        loadComponent: () =>
          import('./pages/banques/banques-list').then(m => m.BanquesListComponent)
      },

      // ── Budget ────────────────────────────────────────────────────
      {
        path: 'budgets',
        loadComponent: () =>
          import('./pages/budget/budget-list').then(m => m.BudgetListComponent)
      },
      {
        path: 'budgets/:id',
        loadComponent: () =>
          import('./pages/budget/budget-detail').then(m => m.BudgetDetailComponent)
      },

      // ── Parts sociales ────────────────────────────────────────────
      {
        path: 'parts-sociales',
        loadComponent: () =>
          import('./pages/parts-sociales/parts-sociales').then(m => m.PartsSocialesComponent)
      },

      // ── Types de garantie ─────────────────────────────────────────
      {
        path: 'types-garantie',
        loadComponent: () =>
          import('./pages/types-garantie/types-garantie-list').then(m => m.TypesGarantieListComponent)
      },

      // ── Produits islamiques ───────────────────────────────────────
      {
        path: 'produits-islamic',
        loadComponent: () =>
          import('./pages/produits-islamic/produits-islamic-list').then(m => m.ProduitsIslamicListComponent)
      },

      // ── Simulation crédit ─────────────────────────────────────────
      {
        path: 'simulation',
        loadComponent: () =>
          import('./pages/simulation/simulation').then(m => m.SimulationComponent)
      },

      // ── Administration ────────────────────────────────────────────
      {
        path: 'admin/utilisateurs',
        loadComponent: () =>
          import('./pages/admin/utilisateurs-list').then(m => m.UtilisateursListComponent)
      },
      {
        path: 'admin/roles',
        loadComponent: () =>
          import('./pages/admin/roles-list').then(m => m.RolesListComponent)
      },
      {
        path: 'admin/privileges',
        loadComponent: () =>
          import('./pages/admin/privileges-list').then(m => m.PrivilegesListComponent)
      },
      {
        path: 'admin/audit',
        loadComponent: () =>
          import('./pages/admin/audit-list').then(m => m.AuditListComponent)
      },
      {
        path: 'admin/backup',
        loadComponent: () =>
          import('./pages/admin/backup').then(m => m.BackupComponent)
      },
      {
        path: 'admin/monitoring',
        loadComponent: () =>
          import('./pages/admin/monitoring').then(m => m.MonitoringComponent)
      },
      {
        path: 'admin/jobs',
        loadComponent: () =>
          import('./pages/admin/jobs').then(m => m.AdminJobsComponent)
      },
      {
        path: 'admin/cloture',
        loadComponent: () =>
          import('./pages/admin/cloture').then(m => m.AdminClotureComponent)
      },
      {
        path: 'admin/parametres',
        loadComponent: () =>
          import('./pages/admin/parametres').then(m => m.AdminParametresComponent)
      },
    ]
  },

  // ── Fallback ─────────────────────────────────────────────────────────
  { path: '**', redirectTo: '' }
];
