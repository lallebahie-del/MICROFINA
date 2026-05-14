import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Utilisateur {
  id: number;
  login: string;
  nomComplet?: string;
  email?: string;
  telephone?: string;
  actif: boolean;
  codeAgence?: string;
  dateExpirationCompte?: string;
  derniereConnexion?: string;
  nombreEchecs?: number;
  roles?: string[];
}

export interface UtilisateurCreate {
  login: string;
  motDePasse: string;
  nomComplet?: string;
  email?: string;
  telephone?: string;
  actif?: boolean;
  dateExpirationCompte?: string;
  codeAgence?: string;
  roles?: string[];
}

export interface UtilisateurUpdate {
  nomComplet?: string;
  email?: string;
  telephone?: string;
  actif?: boolean;
  dateExpirationCompte?: string;
  codeAgence?: string;
  roles?: string[];
}

export interface Role {
  id: number;
  codeRole: string;
  libelle: string;
  description?: string;
}

export interface Privilege {
  id: number;
  codePrivilege: string;
  libelle: string;
  module: string;
}

export interface JournalAudit {
  id: number;
  dateAction: string;
  utilisateur: string;
  action: string;
  entite: string;
  idEntite?: string;
  ancienneValeur?: string;
  nouvelleValeur?: string;
  adresseIp?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/v1/admin`;

  // ── Utilisateurs ──────────────────────────────────────────
  getUtilisateurs(): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(`${this.base}/utilisateurs`);
  }
  getUtilisateurById(id: number): Observable<Utilisateur> {
    return this.http.get<Utilisateur>(`${this.base}/utilisateurs/${id}`);
  }
  createUtilisateur(req: UtilisateurCreate): Observable<Utilisateur> {
    return this.http.post<Utilisateur>(`${this.base}/utilisateurs`, req);
  }
  updateUtilisateur(id: number, req: UtilisateurUpdate): Observable<Utilisateur> {
    return this.http.put<Utilisateur>(`${this.base}/utilisateurs/${id}`, req);
  }
  desactiverUtilisateur(id: number): Observable<void> {
    return this.http.patch<void>(`${this.base}/utilisateurs/${id}/desactiver`, {});
  }
  reinitialiserMdp(id: number, motDePasse: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/utilisateurs/${id}/reinitialiser-mdp`, { motDePasse });
  }
  deleteUtilisateur(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/utilisateurs/${id}`);
  }

  // ── Rôles ─────────────────────────────────────────────────
  getRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.base}/roles`);
  }
  createRole(req: Partial<Role>): Observable<Role> {
    return this.http.post<Role>(`${this.base}/roles`, req);
  }
  updateRole(id: number, req: Partial<Role>): Observable<Role> {
    return this.http.put<Role>(`${this.base}/roles/${id}`, req);
  }
  deleteRole(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/roles/${id}`);
  }

  // ── Privilèges ────────────────────────────────────────────
  getPrivileges(): Observable<Privilege[]> {
    return this.http.get<Privilege[]>(`${this.base}/privileges`);
  }
  createPrivilege(req: Partial<Privilege>): Observable<Privilege> {
    return this.http.post<Privilege>(`${this.base}/privileges`, req);
  }
  updatePrivilege(id: number, req: Partial<Privilege>): Observable<Privilege> {
    return this.http.put<Privilege>(`${this.base}/privileges/${id}`, req);
  }
  deletePrivilege(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/privileges/${id}`);
  }

  // ── Audit ─────────────────────────────────────────────────
  getAuditLog(): Observable<JournalAudit[]> {
    return this.http.get<JournalAudit[]>(`${this.base}/audit`);
  }
  getAuditByUser(login: string): Observable<JournalAudit[]> {
    return this.http.get<JournalAudit[]>(`${this.base}/audit/utilisateur/${login}`);
  }
  getAuditByEntite(entite: string): Observable<JournalAudit[]> {
    return this.http.get<JournalAudit[]>(`${this.base}/audit/entite/${entite}`);
  }
  getAuditByAction(action: string): Observable<JournalAudit[]> {
    return this.http.get<JournalAudit[]>(`${this.base}/audit/action/${action}`);
  }

  // ── Backup ────────────────────────────────────────────────
  backup(): Observable<{ fichier: string; statut: string }> {
    return this.http.post<{ fichier: string; statut: string }>(`${this.base}/backup`, {});
  }
  listerSauvegardes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/backup`);
  }
  restaurerSauvegarde(filename: string): Observable<{ fichier: string; statut: string }> {
    return this.http.post<{ fichier: string; statut: string }>(`${this.base}/backup/restore/${filename}`, {});
  }
}
