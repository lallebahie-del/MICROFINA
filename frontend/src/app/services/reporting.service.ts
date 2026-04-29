import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// ── Types ────────────────────────────────────────────────────────────────────

export interface EtatCredit {
  idCredit: number;
  numCredit: string;
  statutCredit: string;
  objetCredit: string;
  numMembre: string;
  nomMembre: string;
  prenomMembre: string;
  sexe: string;
  codeAgence: string;
  nomAgence: string;
  nomAgent: string;
  dateDemande: string;
  dateDeblocage: string;
  dateEcheanceFinale: string;
  montantAccorde: number;
  montantDebloque: number;
  soldeCapital: number;
  soldeTotal: number;
  joursRetard: number;
  totalArrieres: number;
  categoriePar: string;
  totalGaranties: number;
  tauxCouverturePct: number;
}

export interface RatiosBcm {
  codeAgence: string;
  nomAgence: string;
  encoursBrut: number;
  nbCreditsActifs: number;
  capitaEnRetard30: number;
  par30: number;
  capitaEnRetard90: number;
  par90: number;
  totalArrieres: number;
  capitalRisque: number;
  totalGaranties: number;
  tauxCouverture: number;
}

export interface Indicateur {
  codeAgence: string;
  nomAgence: string;
  nbMembres: number;
  nbMembresActifs: number;
  nbCreditsDisbursed: number;
  nbCreditsActifs: number;
  encoursBrut: number;
  encaissementsJour: number;
  montantDecaisse: number;
  nbRemboursements: number;
  montantRembourse: number;
}

export interface LigneBalance {
  planComptable: string;
  agence: string;
  nomAgence: string;
  soldeDebiteur: number;
  soldeCrediteur: number;
  soldeNet: number;
}

export interface LigneJournal {
  idComptabilite: number;
  dateEcriture: string;
  planComptable: string;
  agence: string;
  debit: number;
  credit: number;
  libelle: string;
  typeOperation: string;
}

export interface LigneBilan {
  planComptable: string;
  classeCompte: string;
  categorie: string;
  agence: string;
  soldeDebiteur: number;
  soldeCrediteur: number;
  soldeNet: number;
}

export interface ListeClient {
  numMembre: string;
  nomMembre: string;
  prenomMembre: string;
  sexe: string;
  telephone: string;
  adresse: string;
  agence: string;
  nomAgence: string;
  nbCreditsTotaux: number;
  nbCreditsActifs: number;
  encoursBrut: number;
  soldeEpargne: number;
  maxJoursRetard: number;
  categoriePar: string;
}

// ── Service ───────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class ReportingService {

  private readonly base = `${environment.apiUrl}/api/v1/reporting`;
  private readonly exportBase = `${environment.apiUrl}/api/v1/export`;

  constructor(private http: HttpClient) {}

  // ── Données brutes ────────────────────────────────────────────────────────

  getEtatCredits(agence = ''): Observable<EtatCredit[]> {
    const params = agence ? new HttpParams().set('agence', agence) : undefined;
    return this.http.get<EtatCredit[]>(`${this.base}/etat-credits`, { params });
  }

  getRatiosBcm(): Observable<RatiosBcm[]> {
    return this.http.get<RatiosBcm[]>(`${this.base}/ratios-bcm`);
  }

  getIndicateurs(agence = ''): Observable<Indicateur[]> {
    const params = agence ? new HttpParams().set('agence', agence) : undefined;
    return this.http.get<Indicateur[]>(`${this.base}/indicateurs`, { params });
  }

  getBalance(): Observable<LigneBalance[]> {
    return this.http.get<LigneBalance[]>(`${this.base}/balance-comptes`);
  }

  getJournal(agence = '', date = ''): Observable<LigneJournal[]> {
    let params = new HttpParams();
    if (agence) params = params.set('agence', agence);
    if (date)   params = params.set('date', date);
    return this.http.get<LigneJournal[]>(`${this.base}/journal`, { params });
  }

  getBilan(): Observable<LigneBilan[]> {
    return this.http.get<LigneBilan[]>(`${this.base}/bilan`);
  }

  getListeClients(agence = ''): Observable<ListeClient[]> {
    const params = agence ? new HttpParams().set('agence', agence) : undefined;
    return this.http.get<ListeClient[]>(`${this.base}/liste-clients`, { params });
  }

  // ── Exports ───────────────────────────────────────────────────────────────

  exportCreditsExcel(agence = ''): Observable<Blob> {
    const params = agence ? new HttpParams().set('agence', agence) : undefined;
    return this.http.get(`${this.exportBase}/credits/excel`,
      { responseType: 'blob', params });
  }

  exportRatiosExcel(): Observable<Blob> {
    return this.http.get(`${this.exportBase}/ratios-bcm/excel`, { responseType: 'blob' });
  }

  exportBilanExcel(): Observable<Blob> {
    return this.http.get(`${this.exportBase}/bilan/excel`, { responseType: 'blob' });
  }

  exportRapportWord(): Observable<Blob> {
    return this.http.get(`${this.exportBase}/rapport-financier/word`, { responseType: 'blob' });
  }

  exportClientsPdf(agence = ''): Observable<Blob> {
    const params = agence ? new HttpParams().set('agence', agence) : undefined;
    return this.http.get(`${this.exportBase}/clients/pdf`,
      { responseType: 'blob', params });
  }

  exportRatiosPdf(): Observable<Blob> {
    return this.http.get(`${this.exportBase}/ratios-bcm/pdf`, { responseType: 'blob' });
  }
}
