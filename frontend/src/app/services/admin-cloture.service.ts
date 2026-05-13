import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ClotureResult {
  annee: string;
  mois?: string;
  statut: string;
}

@Injectable({ providedIn: 'root' })
export class AdminClotureService {
  private base = `${environment.apiUrl}/api/v1/admin/cloture`;

  constructor(private http: HttpClient) {}

  mensuelle(annee: number, mois: number): Observable<ClotureResult> {
    return this.http.post<ClotureResult>(`${this.base}/mensuelle/${annee}/${mois}`, {});
  }

  annuelle(annee: number): Observable<ClotureResult> {
    return this.http.post<ClotureResult>(`${this.base}/annuelle/${annee}`, {});
  }
}
