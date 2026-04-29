import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AnalyseFinanciereCreateRequest,
  AnalyseFinanciereDTO,
  DeblocageRequest,
  WorkflowCreditSummary,
  WorkflowDecisionRequest,
  WorkflowTimelineEntry,
} from '../models/credit-workflow.model';

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
    return this.http.post<WorkflowCreditSummary>(`${this.base}/${id}/workflow/comite/approuver`, req ?? {});
  }

  comiteRejeter(id: number, req?: WorkflowDecisionRequest): Observable<WorkflowCreditSummary> {
    return this.http.post<WorkflowCreditSummary>(`${this.base}/${id}/workflow/comite/rejeter`, req ?? {});
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
    return this.http.get<WorkflowTimelineEntry[]>(`${this.base}/${id}/workflow/timeline`);
  }

  getAnalyse(id: number): Observable<AnalyseFinanciereDTO> {
    return this.http.get<AnalyseFinanciereDTO>(`${this.base}/${id}/workflow/analyse`);
  }

  getComitePending(): Observable<WorkflowCreditSummary[]> {
    return this.http.get<WorkflowCreditSummary[]>(`${this.base}/workflow/comite/pending`);
  }
}
