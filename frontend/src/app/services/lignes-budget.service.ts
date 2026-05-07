import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type TypeLigneBudget = 'RECETTE' | 'DEPENSE';

export interface LigneBudget {
  id: number;
  budgetId: number;
  codeRubrique: string;
  libelle: string;
  typeLigne: TypeLigneBudget;
  montantPrevu: number;
  montantRealise: number;
  compte?: string;
}

export interface LigneBudgetWriteRequest {
  codeRubrique: string;
  libelle: string;
  typeLigne: TypeLigneBudget;
  montantPrevu: number;
  compte?: string;
}

@Injectable({ providedIn: 'root' })
export class LignesBudgetService {
  private readonly base = `${environment.apiUrl}/api/v1/budgets`;

  constructor(private http: HttpClient) {}

  findByBudget(budgetId: number): Observable<LigneBudget[]> {
    return this.http.get<LigneBudget[]>(`${this.base}/${budgetId}/lignes`);
  }

  findById(id: number): Observable<LigneBudget> {
    return this.http.get<LigneBudget>(`${this.base}/lignes/${id}`);
  }

  create(budgetId: number, req: LigneBudgetWriteRequest): Observable<LigneBudget> {
    return this.http.post<LigneBudget>(`${this.base}/${budgetId}/lignes`, req);
  }

  update(id: number, req: LigneBudgetWriteRequest): Observable<LigneBudget> {
    return this.http.put<LigneBudget>(`${this.base}/lignes/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/lignes/${id}`);
  }
}
