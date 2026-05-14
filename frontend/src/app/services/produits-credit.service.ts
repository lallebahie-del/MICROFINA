import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ProduitCredit {
  numProduit: string;
  nomProduit?: string;
  description?: string;
  actif?: number;
  typeCredit?: string;
  typeClient?: string;
  montantMin?: number;
  montantMax?: number;
  dureeMin?: number;
  dureeMax?: number;
  tauxInteret?: number;
  tauxInteretMin?: number;
  tauxInteretMax?: number;
  tauxPenalite?: number;
  tauxCommission?: number;
  tauxAssurance?: number;
  periodiciteRemboursement?: string;
  nombreEcheance?: number;
  delaiGrace?: number;
  typeGrace?: string;
  garantieRequise?: number;
  autoriserReneg?: number;
  autoriserRemboursementAnticipe?: number;
  decaissementNet?: number;
  familleProduitCode?: string;
  familleProduitLibelle?: string;
  modeCalculCode?: string;
  modeCalculLibelle?: string;
  codeProduitIslamic?: string;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class ProduitsCreditService {

  private readonly base = `${environment.apiUrl}/api/v1/produits-credit`;

  constructor(private http: HttpClient) {}

  listActifs(): Observable<ProduitCredit[]> {
    return this.http.get<ProduitCredit[]>(`${this.base}/actifs`);
  }

  search(search = '', actif?: number, page = 0, size = 20): Observable<PageResult<ProduitCredit>> {
    let params = new HttpParams()
      .set('search', search)
      .set('page',   page)
      .set('size',   size);
    if (actif !== undefined) params = params.set('actif', actif);
    return this.http.get<PageResult<ProduitCredit>>(this.base, { params });
  }

  getOne(numProduit: string): Observable<ProduitCredit> {
    return this.http.get<ProduitCredit>(`${this.base}/${numProduit}`);
  }

  create(p: Partial<ProduitCredit>): Observable<ProduitCredit> {
    return this.http.post<ProduitCredit>(this.base, p);
  }

  update(numProduit: string, p: Partial<ProduitCredit>): Observable<ProduitCredit> {
    return this.http.put<ProduitCredit>(`${this.base}/${numProduit}`, p);
  }

  delete(numProduit: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${numProduit}`);
  }
}
