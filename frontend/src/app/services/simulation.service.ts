import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SimulationRequest {
  montantPrincipal: number;
  tauxAnnuel: number;
  nombreEcheances: number;
  periodicite: string;
}

export interface EcheanceDto {
  numero: number;
  capitalDu: number;
  interet: number;
  capitalRembourse: number;
  echeance: number;
}

export interface SimulationResponse {
  montantPrincipal: number;
  tauxAnnuel: number;
  nombreEcheances: number;
  periodicite: string;
  echeanceMensuelle: number;
  totalRembourse: number;
  totalInteret: number;
  tableau: EcheanceDto[];
}

@Injectable({ providedIn: 'root' })
export class SimulationService {
  private readonly http = inject(HttpClient);
  simuler(req: SimulationRequest): Observable<SimulationResponse> {
    return this.http.post<SimulationResponse>(`${environment.apiUrl}/api/v1/simulations/credit`, req);
  }
}
