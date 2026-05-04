import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Banque {
  codeBanque: string;
  nom: string;
  swiftBic?: string;
  adresse?: string;
  pays?: string;
  actif: boolean;
}

@Injectable({ providedIn: 'root' })
export class BanqueService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/banques';

  getAll(): Observable<Banque[]> { return this.http.get<Banque[]>(this.base); }
  getActives(): Observable<Banque[]> { return this.http.get<Banque[]>(`${this.base}/actives`); }
  getById(code: string): Observable<Banque> { return this.http.get<Banque>(`${this.base}/${code}`); }
  create(req: Partial<Banque>): Observable<Banque> { return this.http.post<Banque>(this.base, req); }
  update(code: string, req: Partial<Banque>): Observable<Banque> { return this.http.put<Banque>(`${this.base}/${code}`, req); }
  delete(code: string): Observable<void> { return this.http.delete<void>(`${this.base}/${code}`); }
}
