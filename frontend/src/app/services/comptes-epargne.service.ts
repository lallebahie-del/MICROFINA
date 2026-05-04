import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CompteEpargne {
  id: number;
  numCompte: string;
  numMembre: string;
  nomMembre?: string;
  solde: number;
  tauxInteret: number;
  dateOuverture: string;
  statut: string;
  agence: string;
}

export interface MouvementEpargne {
  id: number;
  compteId: number;
  typeOperation: 'DEPOT' | 'RETRAIT';
  montant: number;
  dateOperation: string;
  soldeApres: number;
  libelle?: string;
}

@Injectable({ providedIn: 'root' })
export class ComptesEpargneService {
  private base = `${environment.apiUrl}/api/v1/epargnes`;

  constructor(private http: HttpClient) {}

  getAll(agence?: string): Observable<CompteEpargne[]> {
    let params = new HttpParams();
    if (agence) params = params.set('agence', agence);
    return this.http.get<CompteEpargne[]>(this.base, { params });
  }

  getById(id: number): Observable<CompteEpargne> {
    return this.http.get<CompteEpargne>(`${this.base}/${id}`);
  }

  ouvrir(compte: Partial<CompteEpargne>): Observable<CompteEpargne> {
    return this.http.post<CompteEpargne>(this.base, compte);
  }

  depot(id: number, montant: number, libelle?: string): Observable<MouvementEpargne> {
    return this.http.post<MouvementEpargne>(`${this.base}/${id}/depot`, { montant, libelle });
  }

  retrait(id: number, montant: number, libelle?: string): Observable<MouvementEpargne> {
    return this.http.post<MouvementEpargne>(`${this.base}/${id}/retrait`, { montant, libelle });
  }

  bloquer(id: number): Observable<CompteEpargne> {
    return this.http.patch<CompteEpargne>(`${this.base}/${id}/bloquer`, {});
  }
}
