import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface MouvementBudget {
  id: number;
  ligneBudgetId: number;
  idComptabilite: number;
  dateMouvement: string;
  montant: number;
  libelle?: string;
  utilisateur?: string;
}

export interface MouvementBudgetWriteRequest {
  idComptabilite: number;
  dateMouvement: string;
  montant: number;
  libelle?: string;
}

@Injectable({ providedIn: 'root' })
export class MouvementsBudgetService {
  private readonly base = `${environment.apiUrl}/api/v1/budgets`;

  constructor(private http: HttpClient) {}

  findByLigne(ligneId: number): Observable<MouvementBudget[]> {
    return this.http.get<MouvementBudget[]>(`${this.base}/lignes/${ligneId}/mouvements`);
  }

  findByBudget(budgetId: number): Observable<MouvementBudget[]> {
    return this.http.get<MouvementBudget[]>(`${this.base}/${budgetId}/mouvements`);
  }

  create(ligneId: number, req: MouvementBudgetWriteRequest): Observable<MouvementBudget> {
    return this.http.post<MouvementBudget>(`${this.base}/lignes/${ligneId}/mouvements`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/mouvements/${id}`);
  }
}
