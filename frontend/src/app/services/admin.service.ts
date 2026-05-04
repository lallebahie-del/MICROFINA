import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Utilisateur {
  id: number; login: string; nomComplet?: string; email?: string;
  telephone?: string; actif: boolean; codeAgence?: string;
}
export interface Role { id: number; codeRole: string; libelle: string; }
export interface Privilege { id: number; codePrivilege: string; libelle: string; module: string; }
export interface JournalAudit {
  id: number; dateAction: string; utilisateur: string;
  action: string; entite: string; idEntite?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);

  // Utilisateurs
  getUtilisateurs(): Observable<Utilisateur[]> { return this.http.get<Utilisateur[]>('/api/v1/admin/utilisateurs'); }
  createUtilisateur(req: any): Observable<Utilisateur> { return this.http.post<Utilisateur>('/api/v1/admin/utilisateurs', req); }
  updateUtilisateur(id: number, req: any): Observable<Utilisateur> { return this.http.put<Utilisateur>(`/api/v1/admin/utilisateurs/${id}`, req); }
  desactiverUtilisateur(id: number): Observable<void> { return this.http.patch<void>(`/api/v1/admin/utilisateurs/${id}/desactiver`, {}); }

  // Rôles
  getRoles(): Observable<Role[]> { return this.http.get<Role[]>('/api/v1/admin/roles'); }

  // Privilèges
  getPrivileges(): Observable<Privilege[]> { return this.http.get<Privilege[]>('/api/v1/admin/privileges'); }

  // Audit
  getAuditLog(): Observable<JournalAudit[]> { return this.http.get<JournalAudit[]>('/api/v1/admin/audit'); }
  getAuditByUser(login: string): Observable<JournalAudit[]> { return this.http.get<JournalAudit[]>(`/api/v1/admin/audit/utilisateur/${login}`); }

  // Backup
  backup(): Observable<{fichier: string; statut: string}> { return this.http.post<any>('/api/v1/admin/backup', {}); }
  listerSauvegardes(): Observable<string[]> { return this.http.get<string[]>('/api/v1/admin/backup'); }
}
