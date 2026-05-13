import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SimulationService, SimulationResponse } from '../../services/simulation.service';

@Component({
  selector: 'app-simulation',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './simulation.html',
  styleUrl: './simulation.css'
})
export class SimulationComponent {
  montantPrincipal = 100000;
  tauxAnnuel = 12;
  nombreEcheances = 12;
  periodicite = 'MENSUEL';

  result = signal<SimulationResponse | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);

  readonly periodicites = ['MENSUEL', 'TRIMESTRIEL', 'SEMESTRIEL', 'ANNUEL'];

  constructor(private simulationService: SimulationService) {}

  simuler(): void {
    this.loading.set(true);
    this.error.set(null);
    this.simulationService.simuler({
      montantPrincipal: this.montantPrincipal,
      tauxAnnuel: this.tauxAnnuel,
      nombreEcheances: this.nombreEcheances,
      periodicite: this.periodicite
    }).subscribe({
      next: r => { this.result.set(r); this.loading.set(false); },
      error: () => { this.error.set('Erreur simulation'); this.loading.set(false); }
    });
  }
}
