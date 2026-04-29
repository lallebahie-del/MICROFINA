import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface OperationCaisse {
  id: number;
  typeOperation: string;
  montant: number;
  devise: string;
  dateOperation: string;
  agence: string;
  libelle?: string;
  compteDebit?: string;
  compteCredit?: string;
  statut: string;
}

@Injectable({ providedIn: 'root' })
export class OperationsCaisseService {
  private base = `${environment.apiUrl}/api/v1/operations-caisse`;

  constructor(private http: HttpClient) {}

  getAll(agence?: string, page = 0, size = 20): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (agence) params = params.set('agence', agence);
    return this.http.get<any>(this.base, { params });
  }

  getById(id: number): Observable<OperationCaisse> {
    return this.http.get<OperationCaisse>(`${this.base}/${id}`);
  }

  create(op: Partial<OperationCaisse>): Observable<OperationCaisse> {
    return this.http.post<OperationCaisse>(this.base, op);
  }
}
