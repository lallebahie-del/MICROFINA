import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type CreditStatut =
  | 'BROUILLON'
  | 'SOUMIS'
  | 'VALIDE_AGENT'
  | 'VALIDE_COMITE'
  | 'DEBLOQUE'
  | 'SOLDE'
  | 'REJETE';

export interface Credit {
  idCredit?: number;
  numCredit?: string;
  statut?: CreditStatut;
  montantDemande?: number;
  montantAccorde?: number;
  montantDebloquer?: number;
  soldeCapital?: number;
  soldeInteret?: number;
  soldePenalite?: number;
  tauxInteret?: number;
  tauxPenalite?: number;
  tauxCommission?: number;
  tauxAssurance?: number;
  duree?: number;
  nombreEcheance?: number;
  delaiGrace?: number;
  periodicite?: string;
  dateDemande?: string;
  dateAccord?: string;
  dateDeblocage?: string;
  dateEcheance?: string;
  dateCloture?: string;
  objetCredit?: string;
  numeroCycle?: number;
  membreNum?: string;
  membreNom?: string;
  membrePrenom?: string;
  produitCode?: string;
  produitNom?: string;
  agenceCode?: string;
  agenceNom?: string;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class CreditsService {

  private readonly base = `${environment.apiUrl}/api/v1/credits`;

  constructor(private http: HttpClient) {}

  search(search = '', statut = '', numMembre = '', page = 0, size = 20): Observable<PageResult<Credit>> {
    const params = new HttpParams()
      .set('search',    search)
      .set('statut',    statut)
      .set('numMembre', numMembre)
      .set('page',      page)
      .set('size',      size);
    return this.http.get<PageResult<Credit>>(this.base, { params });
  }

  getOne(id: number): Observable<Credit> {
    return this.http.get<Credit>(`${this.base}/${id}`);
  }

  create(credit: Partial<Credit>): Observable<Credit> {
    return this.http.post<Credit>(this.base, credit);
  }

  update(id: number, credit: Partial<Credit>): Observable<Credit> {
    return this.http.put<Credit>(`${this.base}/${id}`, credit);
  }

  transitionner(id: number, statut: CreditStatut): Observable<Credit> {
    return this.http.post<Credit>(`${this.base}/${id}/transitionner`, { statut });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
