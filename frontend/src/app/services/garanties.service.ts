import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Garantie {
  id: number;
  creditId: number;
  typeGarantie: string;
  valeurEstimee: number;
  description?: string;
  statut: string;
  dateLiberation?: string;
}

@Injectable({ providedIn: 'root' })
export class GarantiesService {
  private base = `${environment.apiUrl}/api/v1`;

  constructor(private http: HttpClient) {}

  getByCreditId(creditId: number): Observable<Garantie[]> {
    return this.http.get<Garantie[]>(`${this.base}/credits/${creditId}/garanties`);
  }

  ajouter(creditId: number, garantie: Partial<Garantie>): Observable<Garantie> {
    return this.http.post<Garantie>(`${this.base}/credits/${creditId}/garanties`, garantie);
  }

  liberer(id: number): Observable<Garantie> {
    return this.http.patch<Garantie>(`${this.base}/garanties/${id}/liberer`, {});
  }
}
