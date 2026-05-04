import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

/* ───────────────────────── snake_case → camelCase ─────────────────────────
 * Le ReportingController utilise jdbc.queryForList(...) qui renvoie les
 * colonnes SQL brutes en snake_case. On normalise côté front pour conserver
 * la convention camelCase dans les composants Angular.
 * ───────────────────────────────────────────────────────────────────────── */
function snakeToCamelKey(k: string): string {
  return k.replace(/_([a-z0-9])/g, (_m, c: string) => c.toUpperCase());
}
function snakeToCamel<T = any>(input: any): T {
  if (input === null || input === undefined) return input as T;
  if (Array.isArray(input)) return input.map(snakeToCamel) as unknown as T;
  if (typeof input !== 'object') return input as T;
  const out: any = {};
  for (const k of Object.keys(input)) {
    out[snakeToCamelKey(k)] = snakeToCamel((input as any)[k]);
  }
  return out as T;
}

/* ───────────────────────── Types renvoyés par l'API ───────────────────────
 * Les champs sont optionnels car certains contrôleurs renvoient des shapes
 * partielles (ex. ComptabiliteDTO.Response = { idComptabilite, codeAgence }).
 * ───────────────────────────────────────────────────────────────────────── */
export interface Ecriture {
  id?: number;
  idComptabilite?: number;
  codeAgence?: string;
  dateEcriture?: string;
  dateOperation?: string;
  numPiece?: string;
  numCompte?: string;
  libelle?: string;
  debit?: number;
  credit?: number;
  codeLettrage?: string;
  lettrage?: string;
  etat?: string;
}

export interface GrandLivreLigne {
  idcomptabilite?: number;
  dateEcriture?: string;
  dateoperation?: string;
  numPiece?: string;
  numPieceComptable?: string;
  numCompte?: string;
  compteAuxiliaire?: string;
  sens?: string;
  libelle?: string;
  debit?: number;
  credit?: number;
  soldeCumule?: number;
  agenceEcriture?: string;
  lettrage?: string;
  totalRevenusEcriture?: number;
}

export interface BalanceCompte {
  numCompte?: string;
  intitule?: string;
  codeAgence?: string;
  nomAgence?: string;
  nbEcritures?: number;
  totalDebit?: number;
  totalCredit?: number;
  soldeNet?: number;
  soldeDebiteur?: number;
  soldeCrediteur?: number;
  datePremiereEcriture?: string;
  dateDerniereEcriture?: string;
}

export interface JournalLigne {
  idcomptabilite?: number;
  dateEcriture?: string;
  dateoperation?: string;
  codeJournal?: string;
  numPiece?: string;
  numCompte?: string;
  compteContrepartie?: string;
  sens?: string;
  libelle?: string;
  debit?: number;
  credit?: number;
  codeAgence?: string;
  nomAgence?: string;
  agentSaisie?: string;
  typeOperation?: string;
  montantNet?: number;
}

export interface BilanLigne {
  classeCompte?: string;
  numCompte?: string;
  rubrique?: string;
  libelleRubrique?: string;
  libellePoste?: string;
  codeAgence?: string;
  nomAgence?: string;
  nbEcritures?: number;
  totalDebit?: number;
  totalCredit?: number;
  soldeNet?: number;
  montant?: number;
  montantActif?: number;
  montantPassif?: number;
  typePoste?: 'ACTIF' | 'PASSIF';
  datePremiereEcriture?: string;
  dateDerniereEcriture?: string;
}

export interface CompteResultatLigne {
  annee?: number;
  mois?: number;
  codeAgence?: string;
  nomAgence?: string;
  totalProduits?: number;
  totalCharges?: number;
  resultatNet?: number;
}

export interface TableauFinancementLigne {
  dateOperation?: string;
  codeAgence?: string;
  nomAgence?: string;
  totalEntrees?: number;
  totalSorties?: number;
  soldeNetJour?: number;
}

@Injectable({ providedIn: 'root' })
export class ComptabiliteService {
  private base = `${environment.apiUrl}/api/v1`;

  constructor(private http: HttpClient) {}

  // ── Comptabilité (DTO Java déjà en camelCase, conversion neutre) ──
  getEcritures(agence?: string): Observable<Ecriture[]> {
    let p = new HttpParams();
    if (agence) p = p.set('agence', agence);
    return this.http.get<Ecriture[]>(`${this.base}/comptabilite`, { params: p })
      .pipe(map(d => snakeToCamel<Ecriture[]>(d)));
  }

  lettrer(id: number, codeLettrage: string): Observable<Ecriture> {
    return this.http.patch<Ecriture>(`${this.base}/comptabilite/${id}/lettrer`, { codeLettrage })
      .pipe(map(d => snakeToCamel<Ecriture>(d)));
  }

  // ── Reporting (jdbc.queryForList → snake_case → on convertit) ──
  getGrandLivre(agence?: string, compte?: string): Observable<GrandLivreLigne[]> {
    let p = new HttpParams();
    if (agence) p = p.set('agence', agence);
    if (compte) p = p.set('compte', compte);
    return this.http.get<any[]>(`${this.base}/reporting/grand-livre`, { params: p })
      .pipe(map(d => snakeToCamel<GrandLivreLigne[]>(d)));
  }

  getBalance(agence?: string): Observable<BalanceCompte[]> {
    let p = new HttpParams();
    if (agence) p = p.set('agence', agence);
    return this.http.get<any[]>(`${this.base}/reporting/balance-comptes`, { params: p })
      .pipe(map(d => snakeToCamel<BalanceCompte[]>(d)));
  }

  getJournal(agence?: string, date?: string): Observable<JournalLigne[]> {
    let p = new HttpParams();
    if (agence) p = p.set('agence', agence);
    if (date) p = p.set('date', date);
    return this.http.get<any[]>(`${this.base}/reporting/journal`, { params: p })
      .pipe(map(d => snakeToCamel<JournalLigne[]>(d)));
  }

  getBilan(): Observable<BilanLigne[]> {
    return this.http.get<any[]>(`${this.base}/reporting/bilan`)
      .pipe(map(d => snakeToCamel<BilanLigne[]>(d)));
  }

  getCompteResultat(agence?: string, annee?: number, mois?: number): Observable<CompteResultatLigne[]> {
    let p = new HttpParams();
    if (agence) p = p.set('agence', agence);
    if (annee) p = p.set('annee', annee.toString());
    if (mois)  p = p.set('mois', mois.toString());
    return this.http.get<any[]>(`${this.base}/reporting/compte-resultat`, { params: p })
      .pipe(map(d => snakeToCamel<CompteResultatLigne[]>(d)));
  }

  getTableauFinancement(agence?: string, date?: string): Observable<TableauFinancementLigne[]> {
    let p = new HttpParams();
    if (agence) p = p.set('agence', agence);
    if (date)   p = p.set('date', date);
    return this.http.get<any[]>(`${this.base}/reporting/tableau-financement`, { params: p })
      .pipe(map(d => snakeToCamel<TableauFinancementLigne[]>(d)));
  }

  getBalanceAgee(agence?: string): Observable<BalanceCompte[]> {
    let p = new HttpParams();
    if (agence) p = p.set('agence', agence);
    return this.http.get<any[]>(`${this.base}/reporting/balance-comptes`, { params: p })
      .pipe(map(d => snakeToCamel<BalanceCompte[]>(d)));
  }
}
