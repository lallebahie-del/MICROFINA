import { Routes } from '@angular/router';
import { LoginPageComponent } from './pages/login/login';
import { HomeComponent } from './pages/home/home';
import { LayoutComponent } from './components/layout/layout';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginPageComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'home', component: HomeComponent },
      { path: 'charge-clientele', component: HomeComponent },
      { path: 'charge-credit', component: HomeComponent },
      { path: 'chef-agence', component: HomeComponent },
      { path: 'caissier', component: HomeComponent },
      { path: 'comptable', component: HomeComponent },
      { path: 'resp-epargne', component: HomeComponent },
      { path: 'resp-risques', component: HomeComponent },
      { path: 'auditeur', component: HomeComponent },
      { path: 'admin-sys', component: HomeComponent },
      { path: 'dirigeant', component: HomeComponent },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'home' }
];
