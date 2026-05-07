import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface JobExecution {
  idJobExecution: number;
  nomJob: string;
  dateDebut: string;
  dateFin?: string;
  statut: string;
  message?: string;
  declencheur?: string;
}

export const JOBS_DEFINITIONS: { code: string; libelle: string; description: string; cron: string }[] = [
  { code: 'CALCUL_INTERETS',     libelle: 'Calcul des intérêts',     description: 'Calcul quotidien des intérêts courus sur les crédits actifs.',          cron: 'Quotidien 01h00' },
  { code: 'RECALCUL_PAR',        libelle: 'Recalcul du PAR',         description: 'Recalcule le portefeuille à risque (PAR30, PAR90).',                     cron: 'Quotidien 01h30' },
  { code: 'CLOTURE_JOURNALIERE', libelle: 'Clôture journalière',     description: 'Clôture comptable de la journée et préparation des écritures du jour.', cron: 'Quotidien 23h50' }
];

@Injectable({ providedIn: 'root' })
export class AdminJobsService {
  private base = `${environment.apiUrl}/api/v1/admin/jobs`;

  constructor(private http: HttpClient) {}

  run(nom: string): Observable<JobExecution> {
    return this.http.post<JobExecution>(`${this.base}/${nom}/run`, {});
  }

  historiqueParJob(nom: string): Observable<JobExecution[]> {
    return this.http.get<JobExecution[]>(`${this.base}/${nom}/historique`);
  }

  historiqueGlobal(): Observable<JobExecution[]> {
    return this.http.get<JobExecution[]>(`${this.base}/historique`);
  }
}
