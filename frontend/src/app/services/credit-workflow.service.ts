import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {
  AnalyseFinanciereCreateRequest,
  AnalyseFinanciereDTO,
  DeblocageRequest,
  Etape,
  WorkflowCreditSummary,
  WorkflowDecisionRequest,
  WorkflowTimelineEntry,
  WorkflowStats,
} from '../models/credit-workflow.model';
import { Credit } from './credits.service';

@Injectable({ providedIn: 'root' })
export class CreditWorkflowService {

  private readonly base = `${environment.apiUrl}/api/v1/credits`;

  constructor(private http: HttpClient) {}

  soumettre(id: number): Observable<WorkflowCreditSummary> {
    return this.http.post<WorkflowCreditSummary>(`${this.base}/${id}/workflow/soumettre`, {});
  }

  completude(id: number, req?: WorkflowDecisionRequest): Observable<WorkflowCreditSummary> {
    return this.http.post<WorkflowCreditSummary>(`${this.base}/${id}/workflow/completude`, req ?? {});
  }

  analyse(id: number, req: AnalyseFinanciereCreateRequest): Observable<AnalyseFinanciereDTO> {
    return this.http.post<AnalyseFinanciereDTO>(`${this.base}/${id}/workflow/analyse`, req);
  }

  visaRc(id: number, req?: WorkflowDecisionRequest): Observable<WorkflowCreditSummary> {
    return this.http.post<WorkflowCreditSummary>(`${this.base}/${id}/workflow/visa-rc`, req ?? {});
  }

  comiteApprouver(id: number, req?: WorkflowDecisionRequest): Observable<WorkflowCreditSummary> {
    return this.http.post<Credit>(`${this.base}/${id}/transitionner`, { statut: 'VALIDE_COMITE' }).pipe(
      map(c => this.toSummary(c))
    );
  }

  comiteRejeter(id: number, req?: WorkflowDecisionRequest): Observable<WorkflowCreditSummary> {
    return this.http.post<Credit>(`${this.base}/${id}/transitionner`, { statut: 'REJETE' }).pipe(
      map(c => this.toSummary(c))
    );
  }

  visaSf(id: number, req?: WorkflowDecisionRequest): Observable<WorkflowCreditSummary> {
    return this.http.post<WorkflowCreditSummary>(`${this.base}/${id}/workflow/visa-sf`, req ?? {});
  }

  debloquer(id: number, req: DeblocageRequest): Observable<WorkflowCreditSummary> {
    return this.http.post<WorkflowCreditSummary>(`${this.base}/${id}/workflow/debloquer`, req);
  }

  rejeter(id: number, req?: WorkflowDecisionRequest): Observable<WorkflowCreditSummary> {
    return this.http.post<WorkflowCreditSummary>(`${this.base}/${id}/workflow/rejeter`, req ?? {});
  }

  getTimeline(id: number): Observable<WorkflowTimelineEntry[]> {
    return of([]);
  }

  getAnalyse(id: number): Observable<AnalyseFinanciereDTO> {
    return this.http.get<AnalyseFinanciereDTO>(`${this.base}/${id}/workflow/analyse`).pipe(
      catchError(() => of(null as any))
    );
  }

  getComitePending(): Observable<WorkflowCreditSummary[]> {
    return this.http.get<any>(`${this.base}`, {
      params: { statut: 'VALIDE_AGENT', size: '200' }
    }).pipe(
      map((r: any) => (r.content ?? r).map((c: Credit) => this.toSummary(c)))
    );
  }

  getAgentPending(): Observable<WorkflowCreditSummary[]> {
    return this.http.get<any>(`${this.base}`, {
      params: { statut: 'SOUMIS', size: '200' }
    }).pipe(
      map((r: any) => (r.content ?? r).map((c: Credit) => this.toSummary(c)))
    );
  }

  getQueueByEtape(etape: string): Observable<WorkflowCreditSummary[]> {
    return of([]);
  }

  getStats(): Observable<WorkflowStats> {
    return of({});
  }

  private toSummary(c: Credit): WorkflowCreditSummary {
    return {
      idCredit:       c.idCredit!,
      numCredit:      c.numCredit ?? '',
      statut:         c.statut   ?? '',
      etapeCourante:  this.statutToEtape(c.statut),
      numMembre:      c.membreNum,
      nomMembre:      c.membreNom,
      prenomMembre:   c.membrePrenom,
      montantDemande: c.montantDemande,
      montantAccorde: c.montantAccorde,
      codeAgence:     c.agenceCode,
      nomAgence:      c.agenceNom,
      dateDemande:    c.dateDemande,
    };
  }

  private statutToEtape(statut?: string): Etape {
    switch (statut) {
      case 'SOUMIS':        return 'COMPLETUDE';
      case 'VALIDE_AGENT':  return 'COMITE';
      case 'VALIDE_COMITE': return 'VISA_SF';
      case 'DEBLOQUE':      return 'DEBLOQUE';
      case 'REJETE':        return 'REJETE';
      default:              return 'SAISIE';
    }
  }
}
