import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth.service';

interface SubMenuItem {
  label: string;
  icon?: string;
  children?: SubMenuItem[];
}

interface NavItem {
  label: string;
  icon: string;
  children?: SubMenuItem[];
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent {

  constructor(public authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
  // Navigation principale gérée par la sidebar — la barre du haut ne contient
  // plus que le branding, l'utilisateur courant et le bouton de déconnexion.
  navItems: NavItem[] = [];

  iconPaths: Record<string, string> = {
    person: 'M12 12a4 4 0 100-8 4 4 0 000 8zm0 2c-4 0-7 2-7 5v1h14v-1c0-3-3-5-7-5z',
    'user-plus': 'M12 12a4 4 0 100-8 4 4 0 000 8zm-6 8c0-3 3-5 6-5m5 1v5m-2.5-2.5h5',
    group: 'M16 11a3 3 0 100-6 3 3 0 000 6zM8 11a3 3 0 100-6 3 3 0 000 6zm8 2c-3 0-5 1.5-5 4v1h10v-1c0-2.5-2-4-5-4zM8 13c-3 0-5 1.5-5 4v1h7v-1c0-1.5.6-2.8 1.6-3.7A7 7 0 008 13z',
    building: 'M4 21V5h10v16M14 9h6v12M7 8h2M7 12h2M7 16h2M16 12h2M16 16h2',
    part: 'M7 4l10 10M8 14h8M6 18h12',
    eye: 'M2 12s4-6 10-6 10 6 10 6-4 6-10 6S2 12 2 12zm10 3a3 3 0 100-6 3 3 0 000 6z',
    edit: 'M4 17.5V21h3.5L18 10.5 14.5 7 4 17.5zM16 5l3 3',
    document: 'M6 3h9l3 3v15H6V3zm8 0v4h4M8 11h8M8 15h8M8 19h5',
    file: 'M6 3h9l3 3v15H6V3zm8 0v4h4',
    refresh: 'M4 4v6h6M20 20v-6h-6M5 15a7 7 0 0012 3M19 9A7 7 0 007 6',
    arrow: 'M7 7h11l-3-3M17 17H6l3 3',
    camera: 'M4 7h4l2-2h4l2 2h4v12H4V7zm8 3a4 4 0 100 8 4 4 0 000-8z',
    book: 'M4 5h16v14H4V5zm3 4h10M7 13h8M7 17h5',
    card: 'M4 7h16v10H4V7zm0 4h16M7 15h2m3 0h2',
    report: 'M5 19V5m4 14v-7m4 7V9m4 10V7',
    bank: 'M3 10h18M5 10v10M9 10v10M15 10v10M19 10v10M3 20h18M12 3l8 4H4l8-4z',
    account: 'M4 6h16v12H4V6zm3 4h10M7 14h6',
    cash: 'M4 7h16v10H4V7zm3 3h10M9 17v3h6v-3',
    credit: 'M12 3a9 9 0 100 18 9 9 0 000-18zm0 4v10m3-7c0-1.2-1.2-2-3-2s-3 .8-3 2 1.2 2 3 2 3 .8 3 2-1.2 2-3 2-3-.8-3-2',
    table: 'M4 5h16v14H4V5zm0 5h16M9 5v14',
    more: 'M5 12h.01M12 12h.01M19 12h.01',
    settings: 'M12 8a4 4 0 100 8 4 4 0 000-8zm8 4l-2 .8.5 2.1-2 2-2.1-.5-.8 2h-3l-.8-2-2.1.5-2-2 .5-2.1-2-.8V9l2-.8-.5-2.1 2-2 2.1.5.8-2h3l.8 2 2.1-.5 2 2-.5 2.1 2 .8v3z',
    admin: 'M12 3l8 4v5c0 5-3.4 8-8 9-4.6-1-8-4-8-9V7l8-4zm-3 9l2 2 4-4',
    check: 'M9 12l2 2 4-4M12 3a9 9 0 100 18 9 9 0 000-18z',
    flag: 'M5 21V4h10l1 3h4v9h-9l-1-3H5',
    cube: 'M12 3l8 4v10l-8 4-8-4V7l8-4zm0 0v18M4 7l8 4 8-4',
    phone: 'M8 2h8v20H8V2zm2 3h4m-1 14h-2',
    lock: 'M7 11V8a5 5 0 0110 0v3M6 11h12v10H6V11zm6 4v3',
    'red-list': 'M6 4h12v16H6V4zm3 4h6M9 12h6M9 16h4'
  };
}