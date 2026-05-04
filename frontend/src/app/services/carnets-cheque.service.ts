import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CarnetCheque {
  id: number;
  numCarnet: string;
  numMembre: string;
  nomMembre?: string;
  numeroPremierCheque: string;
  numeroDernierCheque: string;
  nbFeuillets: number;
  dateEmission: string;
  statut: string;
  agence: string;
}

@Injectable({ providedIn: 'root' })
export class CarnetsChequeService {
  private base = `${environment.apiUrl}/api/v1/carnets-cheque`;

  constructor(private http: HttpClient) {}

  getAll(agence?: string): Observable<CarnetCheque[]> {
    let params = new HttpParams();
    if (agence) params = params.set('agence', agence);
    return this.http.get<CarnetCheque[]>(this.base, { params });
  }

  getById(id: number): Observable<CarnetCheque> {
    return this.http.get<CarnetCheque>(`${this.base}/${id}`);
  }

  emettre(carnet: Partial<CarnetCheque>): Observable<CarnetCheque> {
    return this.http.post<CarnetCheque>(this.base, carnet);
  }

  bloquer(id: number): Observable<CarnetCheque> {
    return this.http.patch<CarnetCheque>(`${this.base}/${id}/bloquer`, {});
  }
}
