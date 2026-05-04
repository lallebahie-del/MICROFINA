import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface OperationBanque {
  id: number;
  typeOperation: string;
  montant: number;
  devise: string;
  dateOperation: string;
  agence: string;
  codeBanque?: string;
  referenceVirement?: string;
  statut: string;
  libelle?: string;
}

@Injectable({ providedIn: 'root' })
export class OperationsBanqueService {
  private base = `${environment.apiUrl}/api/v1/operations-banque`;

  constructor(private http: HttpClient) {}

  getAll(agence?: string, page = 0, size = 20): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (agence) params = params.set('agence', agence);
    return this.http.get<any>(this.base, { params });
  }

  getById(id: number): Observable<OperationBanque> {
    return this.http.get<OperationBanque>(`${this.base}/${id}`);
  }

  create(op: Partial<OperationBanque>): Observable<OperationBanque> {
    return this.http.post<OperationBanque>(this.base, op);
  }
}
