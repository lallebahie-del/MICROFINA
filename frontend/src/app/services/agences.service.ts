import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Agence {
  codeAgence: string;
  nomAgence: string;
  actif: boolean;
  isSiege: boolean;
  chefAgence?: string;
  zoneGeographique?: string;
}

@Injectable({ providedIn: 'root' })
export class AgencesService {
  private base = `${environment.apiUrl}/api/v1/agences`;

  constructor(private http: HttpClient) {}

  getAll(actif?: boolean): Observable<Agence[]> {
    let params = new HttpParams();
    if (actif !== undefined) params = params.set('actif', String(actif));
    return this.http.get<Agence[]>(this.base, { params });
  }

  getByCode(code: string): Observable<Agence> {
    return this.http.get<Agence>(`${this.base}/${code}`);
  }

  getSieges(): Observable<Agence[]> {
    return this.http.get<Agence[]>(`${this.base}/sieges`);
  }
}
