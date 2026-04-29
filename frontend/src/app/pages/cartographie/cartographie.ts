import { Component, OnInit, OnDestroy, AfterViewInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule }  from '@angular/forms';
import {
  CartographieService, GeoFeatureCollection, GeoFeature, TypeZone
} from '../../services/cartographie.service';

// Leaflet injecté via CDN dans index.html
declare const L: any;

type VueMode = 'zones' | 'heatmap' | 'agences';

@Component({
  selector: 'app-cartographie',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cartographie.html',
  styleUrl: './cartographie.css'
})
export class CartographieComponent implements OnInit, AfterViewInit, OnDestroy {

  // Carte Leaflet
  private map: any        = null;
  private geojsonLayer: any = null;
  private agencesLayer: any = null;

  // État UI
  vueMode    = signal<VueMode>('zones');
  typeZone   = signal<TypeZone | ''>('WILAYA');
  loading    = signal(false);
  error      = signal<string | null>(null);
  selected   = signal<Record<string, unknown> | null>(null); // propriétés de la feature sélectionnée

  // Légende PAR
  readonly legendePar = [
    { label: 'Sain',       color: '#2ecc71' },
    { label: 'PAR30',      color: '#f39c12' },
    { label: 'PAR90',      color: '#e67e22' },
    { label: 'PAR180',     color: '#e74c3c' },
    { label: 'PAR180+',    color: '#8e44ad' },
  ];

  readonly typesZone: { val: TypeZone | ''; label: string }[] = [
    { val: '',          label: 'Toutes' },
    { val: 'WILAYA',    label: 'Wilayas' },
    { val: 'MOUGHATAA', label: 'Moughataas' },
    { val: 'COMMUNE',   label: 'Communes' },
    { val: 'QUARTIER',  label: 'Quartiers' },
  ];

  constructor(private cartoService: CartographieService) {}

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    this.initMap();
    this.charger();
  }

  ngOnDestroy(): void {
    if (this.map) { this.map.remove(); this.map = null; }
  }

  // ── Initialisation Leaflet ────────────────────────────────────────────────

  private initMap(): void {
    // Mauritanie centre approximatif : 20.3°N, 10.9°W — zoom 5
    this.map = L.map('carto-map').setView([20.3, -10.9], 5);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 18
    }).addTo(this.map);
  }

  // ── Chargement des données ────────────────────────────────────────────────

  charger(): void {
    const mode = this.vueMode();
    this.loading.set(true);
    this.error.set(null);
    this.selected.set(null);

    const obs$ = (() => {
      switch (mode) {
        case 'zones':   return this.cartoService.getZones(this.typeZone() || undefined);
        case 'heatmap': return this.cartoService.getHeatmapPar();
        case 'agences': return this.cartoService.getAgences();
      }
    })();

    obs$.subscribe({
      next:  fc => { this.afficherFeatureCollection(fc, mode); this.loading.set(false); },
      error: e  => { this.error.set('Erreur chargement : ' + e.message); this.loading.set(false); }
    });
  }

  changerVue(mode: VueMode): void { this.vueMode.set(mode); this.charger(); }
  changerType(): void { this.charger(); }

  // ── Rendu GeoJSON sur la carte ────────────────────────────────────────────

  private afficherFeatureCollection(fc: GeoFeatureCollection, mode: VueMode): void {
    if (!this.map) return;

    // Supprimer les couches précédentes
    if (this.geojsonLayer) { this.map.removeLayer(this.geojsonLayer); this.geojsonLayer = null; }
    if (this.agencesLayer) { this.map.removeLayer(this.agencesLayer); this.agencesLayer = null; }

    if (!fc?.features?.length) return;

    if (mode === 'agences') {
      // Agences → markers ronds
      this.agencesLayer = L.geoJSON(fc, {
        pointToLayer: (_: any, latlng: any) => L.circleMarker(latlng, {
          radius: 7, fillColor: '#3498db', color: 'white',
          weight: 2, opacity: 1, fillOpacity: 0.9
        }),
        onEachFeature: (feature: any, layer: any) => {
          const p = feature.properties;
          layer.bindPopup(
            `<strong>${p['libelle']}</strong><br/>
             Code : ${p['code']}<br/>
             Membres : ${p['nbMembres']}<br/>
             Crédits actifs : ${p['nbCreditsActifs']}<br/>
             Encours : ${this.fmt(p['encoursNet'])} MRU`
          );
          layer.on('click', () => this.selected.set(p));
        }
      }).addTo(this.map);

      if (this.agencesLayer.getBounds().isValid()) {
        this.map.fitBounds(this.agencesLayer.getBounds(), { padding: [30, 30] });
      }
    } else {
      // Zones → polygones colorés
      this.geojsonLayer = L.geoJSON(fc, {
        style: (feature: any) => this.styleZone(feature, mode),
        onEachFeature: (feature: any, layer: any) => {
          const p = feature.properties;
          const popupContent = mode === 'heatmap'
            ? `<strong>${p['libelle']}</strong><br/>
               Type : ${p['typeZone']}<br/>
               PAR : <span style="color:${p['couleurPar']};font-weight:700">${p['categoriePar']}</span><br/>
               Arriérés : ${this.fmt(p['totalArrieres'])} MRU<br/>
               Capital à risque : ${this.fmt(p['capitalRisque'])} MRU`
            : `<strong>${p['libelle']}</strong><br/>
               Type : ${p['typeZone']}<br/>
               Membres : ${p['nbMembres']}<br/>
               Crédits actifs : ${p['nbCreditsActifs']}<br/>
               Encours : ${this.fmt(p['encoursBrut'])} MRU<br/>
               Arriérés : ${this.fmt(p['totalArrieres'])} MRU`;

          layer.bindPopup(popupContent);
          layer.on('click', () => this.selected.set(p));
          layer.on('mouseover', (e: any) => {
            e.target.setStyle({ weight: 3, fillOpacity: 0.85 });
          });
          layer.on('mouseout', (e: any) => {
            this.geojsonLayer.resetStyle(e.target);
          });
        }
      }).addTo(this.map);

      if (this.geojsonLayer.getBounds && this.geojsonLayer.getBounds().isValid()) {
        this.map.fitBounds(this.geojsonLayer.getBounds(), { padding: [30, 30] });
      }
    }
  }

  private styleZone(feature: any, mode: VueMode): object {
    if (mode === 'heatmap') {
      const couleur = feature.properties['couleurPar'] ?? '#2ecc71';
      return { fillColor: couleur, weight: 1.5, color: 'white', fillOpacity: 0.75 };
    }
    // Zones normales — dégradé bleu selon nombre de membres
    const nb = feature.properties['nbMembres'] ?? 0;
    return {
      fillColor: this.couleurMembres(nb),
      weight: 1.5, color: 'white', fillOpacity: 0.65
    };
  }

  private couleurMembres(nb: number): string {
    if (nb === 0)    return '#bdc3c7';
    if (nb < 100)    return '#85c1e9';
    if (nb < 500)    return '#3498db';
    if (nb < 2000)   return '#1a5276';
    return '#0d2137';
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  fermerDetail(): void { this.selected.set(null); }

  get selectedEntries(): { key: string; value: unknown }[] {
    const s = this.selected();
    if (!s) return [];
    return Object.entries(s)
      .filter(([, v]) => v !== null && v !== undefined && v !== '')
      .map(([key, value]) => ({ key, value }));
  }

  fmt(v: unknown): string {
    if (v == null) return '0';
    const n = Number(v);
    return new Intl.NumberFormat('fr-MR', { minimumFractionDigits: 0 }).format(n);
  }
}
