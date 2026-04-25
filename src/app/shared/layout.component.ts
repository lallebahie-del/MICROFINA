import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { ToastService } from '../core/toast.service';
import { ROLE_LABELS } from '../core/format';

interface NavItem { label: string; path: string; icon: string; roles?: string[]; }
interface NavSection { title: string; items: NavItem[]; }

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="app">
      <aside class="sidebar">
        <div class="brand">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#0f766e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 0.75rem;"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
          <span class="brand-name">MicroFin SI</span>
        </div>
        <nav>
          @for (section of visibleSections(); track section.title) {
            @for (item of section.items; track item.path) {
              <a [routerLink]="item.path" routerLinkActive="active" [routerLinkActiveOptions]="{exact: item.path === '/'}">
                <span class="ico">
                  @switch (item.label) {
                    @case ('Tableau de bord') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg> }
                    @case ('Clients') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg> }
                    @case ('Comptes') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg> }
                    @case ('Caisse') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 3 21 3 21 8"/><line x1="4" y1="20" x2="21" y2="3"/><polyline points="21 16 21 21 16 21"/><line x1="15" y1="15" x2="21" y2="21"/><line x1="4" y1="4" x2="9" y2="9"/></svg> }
                    @case ('Banque') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M3 10h18"/><path d="M5 6l7-3 7 3"/><path d="M4 10v11"/><path d="M20 10v11"/><path d="M8 14v3"/><path d="M12 14v3"/><path d="M16 14v3"/></svg> }
                    @case ('Crédits') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg> }
                    @case ('Comptabilité') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="2" width="16" height="20" rx="2"/><line x1="8" y1="6" x2="16" y2="6"/><line x1="8" y1="10" x2="16" y2="10"/><line x1="8" y1="14" x2="16" y2="14"/><line x1="8" y1="18" x2="16" y2="18"/></svg> }
                    @case ('Budget') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.21 15.89A10 10 0 1 1 8 2.83"/><path d="M22 12A10 10 0 0 0 12 2v10z"/></svg> }
                    @case ('Reporting') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.21 15.89A10 10 0 1 1 8 2.83"/><path d="M22 12A10 10 0 0 0 12 2v10z"/></svg> }
                    @case ('Cartographie') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"/><line x1="8" y1="2" x2="8" y2="18"/><line x1="16" y1="6" x2="16" y2="22"/></svg> }
                    @case ('Utilisateurs') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg> }
                    @case ('Paramètres') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg> }
                    @case ('Audit Logs') { <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg> }
                    @default { {{ item.icon }} }
                  }
                </span>
                <span class="label">{{ item.label }}</span>
              </a>
            }
          }
        </nav>
      </aside>
      <div class="main">
        <header class="header">
          <div class="title" style="visibility: hidden;">{{ pageTitle() }}</div>
          <div class="user-actions">
            <button class="icon-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
            </button>
            <div class="avatar-wrapper" (click)="toggleUserMenu()">
              <div class="avatar">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </div>
              @if (showUserMenu()) {
                <div class="user-dropdown">
                  <div class="dropdown-info">
                    <div class="user-name">{{ user()?.fullName }}</div>
                    <div class="user-role">{{ user()?.role }}</div>
                  </div>
                  <div class="dropdown-divider"></div>
                  <a routerLink="/profil" class="dropdown-item" (click)="$event.stopPropagation(); showUserMenu.set(false)">
                    Profil
                  </a>
                  <div class="dropdown-divider"></div>
                  <button class="dropdown-item logout-btn" (click)="logout()">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 12px;"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                    Déconnexion
                  </button>
                </div>
              }
            </div>
          </div>
        </header>
        <main class="content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: [`
    .app { display: flex; height: 100vh; background: #f8fafc; font-family: 'Inter', sans-serif; }
    
    .sidebar { 
      width: 230px; 
      background: #ffffff; 
      border-right: 1px solid #f1f5f9;
      display: flex; 
      flex-direction: column; 
      margin: 0;
      padding: 0;
    }
    
    .brand { 
      height: 64px !important; 
      display: flex !important; 
      align-items: center !important; 
      padding: 0 1.25rem !important;
      border-bottom: 1px solid #f1f5f9 !important;
      box-sizing: border-box !important;
      margin: 0 !important;
    }
    
    .brand-name { font-size: 1.3rem; font-weight: 700; color: #0f766e; line-height: 1; margin: 0; }
    
    nav { 
      padding: 1rem 0; 
      flex: 1; 
      overflow-y: auto;
      scrollbar-width: none;
      -ms-overflow-style: none;
    }
    nav::-webkit-scrollbar { display: none; }

    nav a { 
      display: flex; 
      align-items: center; 
      padding: 0.7rem 1rem; 
      color: #64748b; 
      text-decoration: none; 
      border-radius: 10px; 
      margin: 0 0.75rem 0.35rem 0.75rem;
      transition: all 0.2s ease;
      font-weight: 500;
      font-size: 0.9rem;
    }
    nav a:hover { background: #f8fafc; color: #0f766e; }
    nav a.active { background: #0f766e; color: #ffffff; }
    nav a .ico { margin-right: 0.75rem; width: 20px; display: inline-flex; justify-content: center; color: inherit; }
    nav a.active .ico { color: #ffffff; }
    
    .main { 
      flex: 1; 
      display: flex; 
      flex-direction: column; 
      overflow: hidden; 
      background: #ffffff; 
      margin: 0;
      padding: 0;
    }
    
    .header { 
      height: 64px !important; 
      background: #ffffff !important; 
      display: flex !important; 
      align-items: center !important; 
      justify-content: space-between !important; 
      padding: 0 2rem !important;
      border-bottom: 1px solid #f1f5f9 !important;
      box-sizing: border-box !important;
      margin: 0 !important;
    }
    
    .user-actions { display: flex; align-items: center; gap: 1rem; }
    .icon-btn { 
      background: none; 
      border: none; 
      color: #64748b; 
      cursor: pointer; 
      padding: 8px; 
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: background 0.2s;
    }
    .icon-btn:hover { background: #f1f5f9; color: #0f172a; }
    
    .avatar-wrapper { position: relative; cursor: pointer; }
    .avatar { 
      width: 36px; 
      height: 36px; 
      background: #f8fafc; 
      border-radius: 50%; 
      display: flex; 
      align-items: center; 
      justify-content: center; 
      color: #64748b;
      border: 1px solid #e2e8f0;
    }
    
    .user-dropdown { 
      position: absolute; 
      top: 100%; 
      right: 0; 
      background: white; 
      border: 1px solid #e2e8f0; 
      border-radius: 8px; 
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); 
      z-index: 100; 
      min-width: 180px; 
      margin-top: 0.75rem; 
      overflow: hidden; 
      padding: 0;
    }
    .dropdown-info { padding: 1rem 1.25rem; }
    .user-name { font-size: 1rem; font-weight: 600; color: #1e293b; margin-bottom: 2px; }
    .user-role { font-size: 0.85rem; color: #64748b; }
    .dropdown-divider { height: 1px; background: #f1f5f9; width: 100%; }
    .dropdown-item { 
      display: flex; 
      align-items: center; 
      width: 100%; 
      padding: 0.85rem 1.25rem; 
      color: #334155; 
      text-decoration: none; 
      font-size: 0.95rem; 
      transition: background 0.2s;
      border: none;
      background: none;
      cursor: pointer;
      text-align: left;
    }
    .dropdown-item:hover { background: #f8fafc; color: #0f766e; }
    .logout-btn { color: #334155; }
    .logout-btn:hover { color: #dc2626; }
    
    .content { 
      flex: 1; 
      overflow-y: auto; 
      padding: 2rem;
      background: #ffffff;
    }
  `]
})
export class LayoutComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  user = this.auth.user;
  showUserMenu = signal(false);

  toggleUserMenu() {
    this.showUserMenu.update(v => !v);
  }

  private allSections: NavSection[] = [
    { title: 'Menu', items: [
      { label: 'Tableau de bord', path: '/', icon: '⊞' },
      { label: 'Clients', path: '/clients', icon: '👥' },
      { label: 'Comptes', path: '/comptes', icon: '📁' },
      { label: 'Caisse', path: '/caisse', icon: '⇄' },
      { label: 'Banque', path: '/banque', icon: '🏦' },
      { label: 'Crédits', path: '/credits', icon: '📜' },
      { label: 'Comptabilité', path: '/comptabilite', icon: '🖩' },
      { label: 'Budget', path: '/budget', icon: '🕒' },
      { label: 'Reporting', path: '/reporting', icon: '📊' },
      { label: 'Cartographie', path: '/cartographie', icon: '🗺️' },
      { label: 'Utilisateurs', path: '/utilisateurs', icon: '👤' },
      { label: 'Paramètres', path: '/parametres', icon: '⚙' },
      { label: 'Audit Logs', path: '/audit', icon: '🛡️' },
    ]},
  ];

  visibleSections = computed<NavSection[]>(() => {
    const u = this.user();
    return this.allSections
      .map((s) => ({ ...s, items: s.items.filter((i) => !i.roles || (u && i.roles.includes(u.role))) }))
      .filter((s) => s.items.length > 0);
  });

  pageTitle = computed(() => {
    const url = this.router.url.split('?')[0];
    if (url === '/' || url.startsWith('/dashboard')) return 'Tableau de bord';
    const map: Record<string, string> = {
      clients: 'Clients', comptes: 'Comptes', caisse: 'Caisse', banque: 'Banque',
      credits: 'Crédits', budget: 'Budget', comptabilite: 'Comptabilité',
      reporting: 'Reporting', cartographie: 'Cartographie', utilisateurs: 'Utilisateurs',
      parametres: 'Paramètres', audit: 'Audit', profil: 'Profil',
    };
    const seg = url.split('/')[1] ?? '';
    return map[seg] ?? '';
  });

  roleLabel = computed(() => ROLE_LABELS[this.user()?.role ?? ''] ?? '—');
  initials = computed(() => {
    const name = this.user()?.fullName ?? '?';
    return name.split(' ').filter(Boolean).slice(0, 2).map((s) => s[0]?.toUpperCase()).join('') || '?';
  });

  logout() {
    this.auth.logout().subscribe({
      next: () => { this.toast.success('Déconnexion réussie'); this.router.navigate(['/login']); },
      error: () => this.toast.error('Erreur lors de la déconnexion'),
    });
  }
}
