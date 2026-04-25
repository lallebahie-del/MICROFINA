import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./shared/layout.component').then((m) => m.LayoutComponent),
    children: [
      { path: '', loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent) },

      { path: 'clients', loadComponent: () => import('./features/clients/clients-list.component').then((m) => m.ClientsListComponent) },
      { path: 'clients/new', loadComponent: () => import('./features/clients/client-new.component').then((m) => m.ClientNewComponent) },
      { path: 'clients/:id', loadComponent: () => import('./features/clients/client-detail.component').then((m) => m.ClientDetailComponent) },

      { path: 'comptes', loadComponent: () => import('./features/accounts/accounts-list.component').then((m) => m.AccountsListComponent) },
      { path: 'comptes/new', loadComponent: () => import('./features/accounts/account-new.component').then((m) => m.AccountNewComponent) },
      { path: 'comptes/:id', loadComponent: () => import('./features/accounts/account-detail.component').then((m) => m.AccountDetailComponent) },

      { path: 'caisse', loadComponent: () => import('./features/caisse/caisse.component').then((m) => m.CaisseComponent) },
      { path: 'banque', loadComponent: () => import('./features/banque/banque.component').then((m) => m.BanqueComponent) },

      { path: 'credits', loadComponent: () => import('./features/credits/credits-list.component').then((m) => m.CreditsListComponent) },
      { path: 'credits/simulator', loadComponent: () => import('./features/credits/credit-simulator.component').then((m) => m.CreditSimulatorComponent) },
      { path: 'credits/new', loadComponent: () => import('./features/credits/credit-new.component').then((m) => m.CreditNewComponent) },
      { path: 'credits/:id', loadComponent: () => import('./features/credits/credit-detail.component').then((m) => m.CreditDetailComponent) },

      { path: 'budget', loadComponent: () => import('./features/budget/budget.component').then((m) => m.BudgetComponent) },
      { path: 'comptabilite', loadComponent: () => import('./features/comptabilite/comptabilite.component').then((m) => m.ComptabiliteComponent) },
      { path: 'reporting', loadComponent: () => import('./features/reporting/reporting.component').then((m) => m.ReportingComponent) },
      { path: 'cartographie', loadComponent: () => import('./features/cartographie/cartographie.component').then((m) => m.CartographieComponent) },

      { path: 'utilisateurs', canActivate: [roleGuard(['admin','directeur'])],
        loadComponent: () => import('./features/utilisateurs/utilisateurs.component').then((m) => m.UtilisateursComponent) },
      { path: 'parametres', canActivate: [roleGuard(['admin'])],
        loadComponent: () => import('./features/parametres/parametres.component').then((m) => m.ParametresComponent) },
      { path: 'audit', canActivate: [roleGuard(['admin','directeur','auditeur'])],
        loadComponent: () => import('./features/audit/audit.component').then((m) => m.AuditComponent) },
      { path: 'profil', loadComponent: () => import('./features/profil/profil.component').then((m) => m.ProfilComponent) },

      { path: '**', loadComponent: () => import('./features/not-found.component').then((m) => m.NotFoundComponent) },
    ],
  },
];
