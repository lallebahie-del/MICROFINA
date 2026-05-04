import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// ── Types ────────────────────────────────────────────────────────────────────

export type WalletStatut = 'EN_ATTENTE' | 'CONFIRME' | 'REJETE' | 'ANNULE' | 'EXPIRE';
export type WalletType   = 'DEBLOCAGE' | 'REMBOURSEMENT' | 'DEPOT_EPARGNE';

export interface OperationWallet {
  id: number;
  referenceMfi: string;
  referenceBankily?: string;
  numeroTelephone: string;
  montant: number;
  typeOperation: WalletType;
  statut: WalletStatut;
  dateOperation: string;
  dateConfirmation?: string;
  motif?: string;
  codeRetour?: string;
  messageRetour?: string;
  numMembre?: string;
  idCredit?: number;
  codeAgence?: string;
  utilisateur?: string;
}

export interface DeblocageRequest {
  idCredit: number;
  numeroTelephone: string;
  motif?: string;
}

export interface RemboursementRequest {
  idCredit: number;
  numeroTelephone: string;
  montant: number;
  motif?: string;
}

export interface StatutResponse {
  referenceMfi: string;
  referenceBankily?: string;
  statutLocal: WalletStatut;
  statutBankily?: string;
  miseAJourEffectuee: boolean;
  message?: string;
}

// ── Service ───────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class WalletService {

  private readonly base = `${environment.apiUrl}/api/v1/wallet`;

  constructor(private http: HttpClient) {}

  initierDeblocage(req: DeblocageRequest): Observable<OperationWallet> {
    return this.http.post<OperationWallet>(`${this.base}/deblocage`, req);
  }

  initierRemboursement(req: RemboursementRequest): Observable<OperationWallet> {
    return this.http.post<OperationWallet>(`${this.base}/remboursement`, req);
  }

  consulter(id: number): Observable<OperationWallet> {
    return this.http.get<OperationWallet>(`${this.base}/operations/${id}`);
  }

  rafraichirStatut(id: number): Observable<StatutResponse> {
    return this.http.get<StatutResponse>(`${this.base}/operations/${id}/statut`);
  }

  annuler(id: number): Observable<OperationWallet> {
    return this.http.patch<OperationWallet>(`${this.base}/operations/${id}/annuler`, {});
  }

  historiqueMembre(numMembre: string): Observable<OperationWallet[]> {
    return this.http.get<OperationWallet[]>(`${this.base}/membres/${numMembre}`);
  }

  historiqueCredit(idCredit: number): Observable<OperationWallet[]> {
    return this.http.get<OperationWallet[]>(`${this.base}/credits/${idCredit}`);
  }

  rechercher(agence = '', typeOperation = '', statut = ''): Observable<OperationWallet[]> {
    let params = new HttpParams();
    if (agence)        params = params.set('agence',        agence);
    if (typeOperation) params = params.set('typeOperation', typeOperation);
    if (statut)        params = params.set('statut',        statut);
    return this.http.get<OperationWallet[]>(`${this.base}/operations`, { params });
  }
}
