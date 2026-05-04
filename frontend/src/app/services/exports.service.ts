import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type ExportFormat = 'excel' | 'pdf' | 'word';
export type ExportEtat =
  | 'credits' | 'ratios-bcm' | 'bilan' | 'balance-comptes'
  | 'journal' | 'indicateurs' | 'liste-clients' | 'rapport-financier'
  | 'compte-resultat' | 'tableau-financement' | 'balance-agee' | 'portefeuille';

@Injectable({ providedIn: 'root' })
export class ExportsService {
  private base = `${environment.apiUrl}/api/v1/export`;

  constructor(private http: HttpClient) {}

  export(etat: ExportEtat, format: ExportFormat, agence?: string): Observable<Blob> {
    let params = new HttpParams();
    if (agence) params = params.set('agence', agence);
    return this.http.get(`${this.base}/${etat}/${format}`, {
      params,
      responseType: 'blob'
    });
  }

  exportSage(agence?: string): Observable<Blob> {
    let params = new HttpParams();
    if (agence) params = params.set('agence', agence);
    return this.http.get(`${this.base}/comptable/sage`, {
      params,
      responseType: 'blob'
    });
  }

  downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }
}
