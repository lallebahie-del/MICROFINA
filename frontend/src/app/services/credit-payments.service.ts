import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface RemboursementRequest {
  montant: number;
  dateReglement?: string;
  numPiece?: string;
  modePaiement?: string;
  codeAgence?: string;
}

export interface RemboursementResponse {
  idReglement: number;
  idCredit: number;
  dateReglement: string;
  montantTotal: number;
  statut: string;
}

export interface CreditPaymentsResponse {
  idCredit: number;
  dateCalcul: string;
  summary: {
    totalDu: number;
    totalPaye: number;
    totalRestant: number;
    nbEcheances: number;
    nbEnRetard: number;
    balanceAgee: {
      current: number;
      d1_30: number;
      d31_60: number;
      d61_90: number;
      d90_plus: number;
    };
  };
  echeances: Array<{
    idAmortp: number;
    numEcheance: number;
    dateEcheance: string | null;
    dateReglement: string | null;
    statutEcheance: string;

    capitalDu: number;
    margeOuInteretDu: number;
    penaliteDue: number;
    assuranceDue: number;
    commissionDue: number;
    taxeDue: number;
    totalDu: number;

    capitalPaye: number;
    margeOuInteretPaye: number;
    penalitePayee: number;
    assurancePayee: number;
    commissionPayee: number;
    taxePayee: number;
    totalPaye: number;

    totalRestant: number;
    joursRetard: number;
  }>;
}

/** Réponse GET .../amortissement/preview (échéancier calculé sans persistance). */
export interface CreditPaymentsPreviewResponse {
  idCredit: number;
  montantPrincipalUtilise: number;
  islamique: boolean;
  dateCalcul: string;
  echeances: Array<{
    numEcheance: number;
    dateEcheance: string | null;
    capital: number;
    margeOuInteret: number;
    assurance: number;
    commission: number;
    taxe: number;
    totalEcheance: number;
    soldeCapitalApres: number;
  }>;
}

@Injectable({ providedIn: 'root' })
export class CreditPaymentsService {

  private readonly base = `${environment.apiUrl}/api/v1/credits`;

  constructor(private http: HttpClient) {}

  getSuivi(idCredit: number): Observable<CreditPaymentsResponse> {
    return this.http.get<CreditPaymentsResponse>(`${this.base}/${idCredit}/amortp`);
  }

  getAmortissementPreview(idCredit: number): Observable<CreditPaymentsPreviewResponse> {
    return this.http.get<CreditPaymentsPreviewResponse>(`${this.base}/${idCredit}/amortissement/preview`);
  }

  encaisserPaiement(idCredit: number, req: RemboursementRequest): Observable<RemboursementResponse> {
    return this.http.post<RemboursementResponse>(`${this.base}/${idCredit}/remboursements/caisse`, req);
  }
}

