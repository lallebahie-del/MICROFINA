import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/** Matches backend CarnetChequeDTO.Response */
export interface CarnetCheque {
  id: number;
  numeroCarnet: string;
  dateDemande?: string | null;
  dateRemise?: string | null;
  nombreCheques?: number;
  statut: string;
  compteBanqueId?: number;
  numMembre: string;
}

/** Shape of the creation form in the UI */
export interface CarnetChequeForm {
  numCarnet?: string;
  numMembre?: string;
  nbFeuillets?: number;
  dateEmission?: string;
  agence?: string;
  numeroPremierCheque?: string;
  numeroDernierCheque?: string;
}

@Injectable({ providedIn: 'root' })
export class CarnetsChequeService {
  private base = `${environment.apiUrl}/api/v1/carnets-cheque`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<CarnetCheque[]> {
    return this.http.get<CarnetCheque[]>(this.base);
  }

  getById(id: number): Observable<CarnetCheque> {
    return this.http.get<CarnetCheque>(`${this.base}/${id}`);
  }

  getByMembre(numMembre: string): Observable<CarnetCheque[]> {
    return this.http.get<CarnetCheque[]>(
      `${this.base}/membre/${encodeURIComponent(numMembre)}`
    );
  }

  emettre(form: Partial<CarnetChequeForm>): Observable<CarnetCheque> {
    const payload = {
      numeroCarnet:  form.numCarnet,
      numMembre:     form.numMembre,
      nombreCheques: form.nbFeuillets,
      dateDemande:   form.dateEmission,
    };
    return this.http.post<CarnetCheque>(this.base, payload);
  }

  bloquer(id: number): Observable<CarnetCheque> {
    return this.http.put<CarnetCheque>(`${this.base}/${id}`, { statut: 'OPPOSITION' });
  }
}
