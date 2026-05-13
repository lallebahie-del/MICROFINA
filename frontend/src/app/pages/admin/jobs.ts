import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AdminJobsService,
  JobExecution,
  JOBS_DEFINITIONS
} from '../../services/admin-jobs.service';

@Component({
  selector: 'app-admin-jobs',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './jobs.html'
})
export class AdminJobsComponent implements OnInit {
  readonly jobs = JOBS_DEFINITIONS;

  history       = signal<JobExecution[]>([]);
  loading       = signal(false);
  running       = signal<string | null>(null);   // nom du job en cours
  error         = signal<string | null>(null);
  success       = signal<string | null>(null);

  filtreJob: string = '';

  filteredHistory = computed(() => {
    const f = this.filtreJob;
    return f ? this.history().filter(h => h.nomJob === f) : this.history();
  });

  constructor(private svc: AdminJobsService) {}

  ngOnInit(): void { this.loadHistory(); }

  loadHistory(): void {
    this.loading.set(true);
    this.svc.historiqueGlobal().subscribe({
      next: list => { this.history.set(list); this.loading.set(false); },
      error: e   => { this.error.set('Erreur : ' + (e.error?.message ?? e.message)); this.loading.set(false); }
    });
  }

  run(nom: string): void {
    if (!confirm(`Déclencher le job ${nom} maintenant ?`)) return;
    this.running.set(nom);
    this.error.set(null);
    this.success.set(null);

    this.svc.run(nom).subscribe({
      next: exec => {
        this.running.set(null);
        this.success.set(`${nom} → ${exec.statut}${exec.message ? ' : ' + exec.message : ''}`);
        this.loadHistory();
      },
      error: e => {
        this.running.set(null);
        this.error.set('Échec : ' + (e.error?.message ?? e.message));
      }
    });
  }

  badgeClass(statut: string): string {
    switch (statut) {
      case 'SUCCES':
      case 'TERMINE':  return 'badge badge-success';
      case 'ECHEC':
      case 'ERREUR':   return 'badge badge-danger';
      case 'EN_COURS': return 'badge badge-info';
      default:         return 'badge badge-primary';
    }
  }

  duree(exec: JobExecution): string {
    if (!exec.dateFin) return '—';
    const ms = new Date(exec.dateFin).getTime() - new Date(exec.dateDebut).getTime();
    if (ms < 1000) return ms + ' ms';
    if (ms < 60_000) return (ms / 1000).toFixed(1) + ' s';
    return (ms / 60_000).toFixed(1) + ' min';
  }
}
