import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/** Matches backend OperationCaisseDTO.Response */
export interface OperationCaisse {
  id: number;
  numPiece: string;
  dateOperation: string;
  montant: number;
  modePaiement: string;
  motif?: string;
  utilisateur?: string;
  statut: string;
  numCompte?: string;
  codeAgence?: string;
  idComptabilite?: number;
}

/** Shape of the creation form in the UI */
export interface OperationCaisseForm {
  typeOperation: string;
  montant: number;
  devise?: string;
  dateOperation: string;
  agence?: string;
  libelle?: string;
  modePaiement?: string;
  compteDebit?: string;
  compteCredit?: string;
}

@Injectable({ providedIn: 'root' })
export class OperationsCaisseService {
  private base = `${environment.apiUrl}/api/v1/operations-caisse`;

  constructor(private http: HttpClient) {}

  getAll(agence?: string, page = 0, size = 20): Observable<OperationCaisse[]> {
    if (agence) {
      return this.http.get<OperationCaisse[]>(
        `${this.base}/agence/${encodeURIComponent(agence)}`
      );
    }
    return this.http.get<OperationCaisse[]>(this.base);
  }

  getById(id: number): Observable<OperationCaisse> {
    return this.http.get<OperationCaisse>(`${this.base}/${id}`);
  }

  create(form: Partial<OperationCaisseForm>): Observable<OperationCaisse> {
    const parts = [form.typeOperation, form.libelle].filter(Boolean);
    const payload = {
      dateOperation: form.dateOperation,
      montant:       form.montant,
      modePaiement:  form.modePaiement || 'ESPECES',
      motif:         parts.join(' — ') || undefined,
      codeAgence:    form.agence,
      numCompte:     form.compteDebit || form.compteCredit || undefined,
    };
    return this.http.post<OperationCaisse>(this.base, payload);
  }

  valider(id: number): Observable<OperationCaisse> {
    return this.http.patch<OperationCaisse>(`${this.base}/${id}/valider`, {});
  }

  annuler(id: number): Observable<OperationCaisse> {
    return this.http.patch<OperationCaisse>(`${this.base}/${id}/annuler`, {});
  }
}
