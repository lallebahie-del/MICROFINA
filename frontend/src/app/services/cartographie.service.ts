import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// ── Types GeoJSON RFC 7946 ────────────────────────────────────────────────────

export interface GeoGeometry {
  type: string;
  coordinates: unknown;
}

export interface GeoFeature {
  type: 'Feature';
  geometry: GeoGeometry | null;
  properties: Record<string, unknown>;
}

export interface GeoFeatureCollection {
  type: 'FeatureCollection';
  features: GeoFeature[];
}

// ── Service ───────────────────────────────────────────────────────────────────

export type TypeZone = 'WILAYA' | 'MOUGHATAA' | 'COMMUNE' | 'QUARTIER';

@Injectable({ providedIn: 'root' })
export class CartographieService {

  private readonly base = `${environment.apiUrl}/api/v1/cartographie`;

  constructor(private http: HttpClient) {}

  getZones(type?: TypeZone): Observable<GeoFeatureCollection> {
    const params = type ? new HttpParams().set('type', type) : undefined;
    return this.http.get<GeoFeatureCollection>(`${this.base}/zones`, { params });
  }

  getZone(id: number): Observable<GeoFeature> {
    return this.http.get<GeoFeature>(`${this.base}/zones/${id}`);
  }

  getAgences(): Observable<GeoFeatureCollection> {
    return this.http.get<GeoFeatureCollection>(`${this.base}/agences`);
  }

  getHeatmapPar(): Observable<GeoFeatureCollection> {
    return this.http.get<GeoFeatureCollection>(`${this.base}/heatmap-par`);
  }
}
