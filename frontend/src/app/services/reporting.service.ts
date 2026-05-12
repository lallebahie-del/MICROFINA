import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

function snakeToCamel(obj: unknown): unknown {
  if (Array.isArray(obj)) return obj.map(snakeToCamel);
  if (obj !== null && typeof obj === 'object') {
    return Object.fromEntries(
      Object.entries(obj as Record<string, unknown>).map(([k, v]) => [
        k.replace(/_([a-z])/g, (_: string, c: string) => c.toUpperCase()),
        snakeToCamel(v)
      ])
    );
  }
  return obj;
}

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
  capitalRisquePar30: number;
  tauxPar30: number;
  capitalRisquePar90: number;
  tauxPar90: number;
  totalArrieres: number;
  totalGaranties: number;
  ratioCouvertureGaranties: number;
}

export interface Indicateur {
  codeAgence: string;
  nomAgence: string;
  nbMembresEmprunteurs: number;
  nbMembresActifs: number;
  nbCreditsTotal: number;
  nbCreditsActifs: number;
  montantEncours: number;
  montantDebloqueTotal: number;
  nbReglements: number;
  montantRembourseTotal: number;
}

export interface LigneBalance {
  numCompte?: string;
  codeAgence?: string;
  nomAgence?: string;
  totalDebit?: number;
  totalCredit?: number;
  soldeDebiteur?: number;
  soldeCrediteur?: number;
  soldeNet?: number;
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
  numCompte?: string;
  classeCompte?: string;
  rubrique?: string;
  libelleRubrique?: string;
  codeAgence?: string;
  nomAgence?: string;
  totalDebit?: number;
  totalCredit?: number;
  soldeNet?: number;
  montantActif?: number;
  montantPassif?: number;
}

export interface ListeClient {
  numMembre: string;
  nom: string;
  prenom: string;
  nomAgence: string;
  nbCreditsActifs: number;
  encoursCapital: number;
  totalEpargne: number;
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
    return this.http.get<unknown[]>(`${this.base}/indicateurs`, { params }).pipe(
      map(arr => snakeToCamel(arr) as Indicateur[])
    );
  }

  getBalance(): Observable<LigneBalance[]> {
    return this.http.get<unknown[]>(`${this.base}/balance-comptes`).pipe(
      map(arr => snakeToCamel(arr) as LigneBalance[])
    );
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
