import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Membre {
  numMembre: string;
  dtype?: string;
  nom?: string;
  prenom?: string;
  nomJeuneFille?: string;
  sexe?: string;
  dateNaissance?: string;
  lieuNaissance?: string;
  age?: number;
  situationMatrimoniale?: string;
  matriculeMembre?: string;
  numeroNationalId?: string;
  raisonSociale?: string;
  etat?: string;
  statut?: string;
  dateDemande?: string;
  dateValidation?: string;
  dateDepart?: string;
  motifRejet?: string;
  personneAcontacter?: string;
  contactPersonneContact?: string;
  observation?: string;
  agenceCode?: string;
  agenceLibelle?: string;
  categorie?: string;
  secteurActivite?: string;
  depot?: number;
  droitEntree?: number;
  codeMembre?: string;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class MembresService {

  private readonly base = `${environment.apiUrl}/api/v1/membres`;

  constructor(private http: HttpClient) {}

  search(search = '', statut = '', etat = '', page = 0, size = 20): Observable<PageResult<Membre>> {
    const params = new HttpParams()
      .set('search', search)
      .set('statut', statut)
      .set('etat',   etat)
      .set('page',   page)
      .set('size',   size);
    return this.http.get<PageResult<Membre>>(this.base, { params });
  }

  getOne(numMembre: string): Observable<Membre> {
    return this.http.get<Membre>(`${this.base}/${numMembre}`);
  }

  create(membre: Partial<Membre>): Observable<Membre> {
    return this.http.post<Membre>(this.base, membre);
  }

  update(numMembre: string, membre: Partial<Membre>): Observable<Membre> {
    return this.http.put<Membre>(`${this.base}/${numMembre}`, membre);
  }

  desactiver(numMembre: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/${numMembre}/desactiver`, {});
  }

  delete(numMembre: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${numMembre}`);
  }
}
