import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Parametre {
  idParametre?: number;
  maxiJourOuvert?: number;
  prefixe?: string;
  suffixe?: string;
  useMultidevise?: string;
  codeAgence?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminParametresService {
  private base = `${environment.apiUrl}/api/v1/admin/parametres`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Parametre[]> {
    return this.http.get<Parametre[]>(this.base);
  }

  findById(id: number): Observable<Parametre> {
    return this.http.get<Parametre>(`${this.base}/${id}`);
  }

  findByAgence(codeAgence: string): Observable<Parametre> {
    return this.http.get<Parametre>(`${this.base}/agence/${codeAgence}`);
  }

  create(p: Parametre): Observable<Parametre> {
    return this.http.post<Parametre>(this.base, p);
  }

  update(id: number, p: Parametre): Observable<Parametre> {
    return this.http.put<Parametre>(`${this.base}/${id}`, p);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
