import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TypeGarantie {
  code: string;
  libelle: string;
  description?: string;
  actif?: boolean;
  version?: number;
}

@Injectable({ providedIn: 'root' })
export class TypesGarantieService {
  private base = `${environment.apiUrl}/api/v1/referentiel/types-garantie`;

  constructor(private http: HttpClient) {}

  findActifs(): Observable<TypeGarantie[]> {
    return this.http.get<TypeGarantie[]>(this.base);
  }

  findAll(): Observable<TypeGarantie[]> {
    return this.http.get<TypeGarantie[]>(`${this.base}/all`);
  }

  getByCode(code: string): Observable<TypeGarantie> {
    return this.http.get<TypeGarantie>(`${this.base}/${code}`);
  }

  create(t: TypeGarantie): Observable<TypeGarantie> {
    return this.http.post<TypeGarantie>(this.base, t);
  }

  update(code: string, t: TypeGarantie): Observable<TypeGarantie> {
    return this.http.put<TypeGarantie>(`${this.base}/${code}`, t);
  }

  delete(code: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${code}`);
  }
}
