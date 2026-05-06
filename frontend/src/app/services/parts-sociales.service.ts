import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ProduitPartSociale {
  numProduit: string;
  nomProduit?: string;
  description?: string;
  actif?: number;
  valeurNominale?: number;
  nombrePartsMin?: number;
  nombrePartsMax?: number;
  tauxDividende?: number;
  periodiciteDividende?: string;
  compteCapitalSocial?: string;
  compteDividende?: string;
  compteReserve?: string;
}

@Injectable({ providedIn: 'root' })
export class PartsSocialesService {

  private readonly base = `${environment.apiUrl}/api/v1/parts-sociales`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<ProduitPartSociale[]> {
    return this.http.get<ProduitPartSociale[]>(this.base);
  }

  findActifs(): Observable<ProduitPartSociale[]> {
    return this.http.get<ProduitPartSociale[]>(`${this.base}/actifs`);
  }

  findById(id: string): Observable<ProduitPartSociale> {
    return this.http.get<ProduitPartSociale>(`${this.base}/${id}`);
  }

  create(p: ProduitPartSociale): Observable<ProduitPartSociale> {
    return this.http.post<ProduitPartSociale>(this.base, p);
  }

  update(id: string, p: ProduitPartSociale): Observable<ProduitPartSociale> {
    return this.http.put<ProduitPartSociale>(`${this.base}/${id}`, p);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
