import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

/** Champs renvoyés par GET /api/v1/comptes-epargne (CompteEpsDTO.Response). */
interface CompteEpsApiRow {
  numCompte: string;
  numMembre: string;
  codeAgence?: string;
  tauxInteret?: number | string;
  montantOuvert?: number | string;
  montantDepot?: number | string;
  dateCreation?: string;
  bloque?: string;
  ferme?: string;
}

export interface CompteEpargne {
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
  private readonly base = `${environment.apiUrl}/api/v1/comptes-epargne`;

  constructor(private http: HttpClient) {}

  private mapRow(r: CompteEpsApiRow): CompteEpargne {
    const ouvert = Number(r.montantOuvert ?? 0);
    const depot = Number(r.montantDepot ?? 0);
    return {
      numCompte: r.numCompte,
      numMembre: r.numMembre ?? '',
      solde: ouvert + depot,
      tauxInteret: Number(r.tauxInteret ?? 0),
      dateOuverture: r.dateCreation ?? '—',
      statut: r.ferme === 'O' ? 'FERME' : r.bloque === 'O' ? 'BLOQUE' : 'ACTIF',
      agence: r.codeAgence ?? '',
    };
  }

  getAll(agence?: string): Observable<CompteEpargne[]> {
    const url = agence
      ? `${this.base}/agence/${encodeURIComponent(agence)}`
      : this.base;
    return this.http
      .get<CompteEpsApiRow[]>(url)
      .pipe(map(rows => rows.map(r => this.mapRow(r))));
  }

  getById(numCompte: string): Observable<CompteEpargne> {
    return this.http
      .get<CompteEpsApiRow>(`${this.base}/${encodeURIComponent(numCompte)}`)
      .pipe(map(r => this.mapRow(r)));
  }

  ouvrir(compte: Partial<CompteEpargne>): Observable<CompteEpargne> {
    return this.http
      .post<CompteEpsApiRow>(this.base, compte)
      .pipe(map(r => this.mapRow(r)));
  }

  /** Non exposé par l’API actuelle — conservé pour l’UI « Épargne » (peut renvoyer 404). */
  depot(numCompte: string, montant: number, libelle?: string): Observable<MouvementEpargne> {
    return this.http.post<MouvementEpargne>(
      `${this.base}/${encodeURIComponent(numCompte)}/depot`,
      { montant, libelle },
    );
  }

  /** Non exposé par l’API actuelle — conservé pour l’UI « Épargne » (peut renvoyer 404). */
  retrait(numCompte: string, montant: number, libelle?: string): Observable<MouvementEpargne> {
    return this.http.post<MouvementEpargne>(
      `${this.base}/${encodeURIComponent(numCompte)}/retrait`,
      { montant, libelle },
    );
  }

  bloquer(numCompte: string): Observable<CompteEpargne> {
    return this.http
      .put<CompteEpsApiRow>(`${this.base}/${encodeURIComponent(numCompte)}`, { bloque: 'O' })
      .pipe(map(r => this.mapRow(r)));
  }
}
