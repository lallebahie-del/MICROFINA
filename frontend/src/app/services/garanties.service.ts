import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/** Aligné sur GarantieDTO JSON (Spring). */
export interface Garantie {
  idGarantie: number;
  idCredit?: number;
  codeTypeGarantie?: string;
  libelleTypeGarantie?: string;
  valeurEstimee?: number;
  observations?: string;
  statut?: string;
  dateMainlevee?: string;
}

@Injectable({ providedIn: 'root' })
export class GarantiesService {
  private base = `${environment.apiUrl}/api/v1`;

  constructor(private http: HttpClient) {}

  getByCreditId(creditId: number): Observable<Garantie[]> {
    return this.http.get<Garantie[]>(`${this.base}/garanties/credit/${creditId}`);
  }

  /** POST /api/v1/garanties — corps aligné sur GarantieDTO.CreationRequest. */
  ajouter(req: {
    codeTypeGarantie: string;
    idCredit: number;
    valeurEstimee: number;
    numMembreGarant?: string;
    dateEvaluation?: string;
    referenceDocument?: string;
    observations?: string;
  }): Observable<Garantie> {
    return this.http.post<Garantie>(`${this.base}/garanties`, req);
  }

  liberer(idGarantie: number): Observable<Garantie> {
    const dateMainlevee = new Date().toISOString().slice(0, 10);
    return this.http.patch<Garantie>(`${this.base}/garanties/${idGarantie}/liberer`, {
      dateMainlevee,
      observations: '',
    });
  }
}
