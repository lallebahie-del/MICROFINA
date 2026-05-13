import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ProduitIslamic {
  codeProduit: string;
  libelle?: string;
  description?: string;
  actif?: number;
  tauxPartageBenefice?: number;
  costPriceRatio?: number;
  markupRatio?: number;
  residualValueRatio?: number;
  version?: number;
}

@Injectable({ providedIn: 'root' })
export class ProduitsIslamicService {
  private base = `${environment.apiUrl}/api/v1/produits-islamic`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<ProduitIslamic[]> {
    return this.http.get<ProduitIslamic[]>(this.base);
  }

  findActifs(): Observable<ProduitIslamic[]> {
    return this.http.get<ProduitIslamic[]>(`${this.base}/actifs`);
  }

  findById(code: string): Observable<ProduitIslamic> {
    return this.http.get<ProduitIslamic>(`${this.base}/${code}`);
  }

  create(p: ProduitIslamic): Observable<ProduitIslamic> {
    return this.http.post<ProduitIslamic>(this.base, p);
  }

  update(code: string, p: ProduitIslamic): Observable<ProduitIslamic> {
    return this.http.put<ProduitIslamic>(`${this.base}/${code}`, p);
  }

  delete(code: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${code}`);
  }
}
