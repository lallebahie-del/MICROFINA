import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Budget {
  id: number;
  exerciceFiscal: number;
  dateCreation: string;
  dateValidation?: string;
  statut: 'BROUILLON' | 'VALIDE' | 'CLOTURE';
  montantTotalRecettes: number;
  montantTotalDepenses: number;
  utilisateur?: string;
  codeAgence?: string;
}

@Injectable({ providedIn: 'root' })
export class BudgetService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/budgets';

  getAll(): Observable<Budget[]> { return this.http.get<Budget[]>(this.base); }
  getById(id: number): Observable<Budget> { return this.http.get<Budget>(`${this.base}/${id}`); }
  getByExercice(exercice: number): Observable<Budget[]> { return this.http.get<Budget[]>(`${this.base}/exercice/${exercice}`); }
  create(req: Partial<Budget>): Observable<Budget> { return this.http.post<Budget>(this.base, req); }
  update(id: number, req: Partial<Budget>): Observable<Budget> { return this.http.put<Budget>(`${this.base}/${id}`, req); }
  valider(id: number): Observable<Budget> { return this.http.patch<Budget>(`${this.base}/${id}/valider`, {}); }
  cloturer(id: number): Observable<Budget> { return this.http.patch<Budget>(`${this.base}/${id}/cloturer`, {}); }
}
