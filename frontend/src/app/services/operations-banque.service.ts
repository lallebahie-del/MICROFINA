import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/** Matches backend OperationBanqueDTO.Response */
export interface OperationBanque {
  id: number;
  dateOperation: string;
  montant: number;
  statut: string;
  utilisateur?: string;
  compteBanqueId?: number;
  codeAgence?: string;
  idComptabilite?: number;
}

/** Shape of the creation form in the UI */
export interface OperationBanqueForm {
  typeOperation: string;
  montant: number;
  devise?: string;
  dateOperation: string;
  agence?: string;
  codeBanque?: string;
  referenceVirement?: string;
  libelle?: string;
}

@Injectable({ providedIn: 'root' })
export class OperationsBanqueService {
  private base = `${environment.apiUrl}/api/v1/operations-banque`;

  constructor(private http: HttpClient) {}

  getAll(agence?: string): Observable<OperationBanque[]> {
    if (agence) {
      return this.http.get<OperationBanque[]>(
        `${this.base}/agence/${encodeURIComponent(agence)}`
      );
    }
    return this.http.get<OperationBanque[]>(this.base);
  }

  getById(id: number): Observable<OperationBanque> {
    return this.http.get<OperationBanque>(`${this.base}/${id}`);
  }

  create(form: Partial<OperationBanqueForm>): Observable<OperationBanque> {
    const libelleParts = [form.typeOperation, form.libelle, form.referenceVirement]
      .filter(Boolean).join(' — ');
    const payload = {
      dateOperation: form.dateOperation,
      montant:       form.montant,
      utilisateur:   libelleParts || undefined,
      codeAgence:    form.agence,
    };
    return this.http.post<OperationBanque>(this.base, payload);
  }

  valider(id: number): Observable<OperationBanque> {
    return this.http.patch<OperationBanque>(`${this.base}/${id}/valider`, {});
  }

  annuler(id: number): Observable<OperationBanque> {
    return this.http.patch<OperationBanque>(`${this.base}/${id}/annuler`, {});
  }
}
