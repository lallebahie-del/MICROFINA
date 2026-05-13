import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MonitoringService, AppMetrics, DatabaseMetrics, JobStatus, SessionInfo } from '../../services/monitoring.service';

@Component({
  selector: 'app-monitoring',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './monitoring.html'
})
export class MonitoringComponent implements OnInit, OnDestroy {
  metrics   = signal<AppMetrics | null>(null);
  database  = signal<DatabaseMetrics | null>(null);
  jobs      = signal<JobStatus[]>([]);
  sessions  = signal<SessionInfo | null>(null);
  lastRefresh = signal<Date>(new Date());
  private intervalId: ReturnType<typeof setInterval> | null = null;

  constructor(private svc: MonitoringService) {}

  ngOnInit(): void {
    this.refresh();
    this.intervalId = setInterval(() => this.refresh(), 10_000);
  }

  ngOnDestroy(): void {
    if (this.intervalId) clearInterval(this.intervalId);
  }

  refresh(): void {
    this.lastRefresh.set(new Date());
    this.svc.getMetrics().subscribe({ next: d => this.metrics.set(d) });
    this.svc.getDatabase().subscribe({ next: d => this.database.set(d) });
    this.svc.getJobs().subscribe({ next: d => this.jobs.set(d) });
    this.svc.getSessions().subscribe({ next: d => this.sessions.set(d) });
  }

  memoryPercent(): number {
    const m = this.metrics();
    if (!m) return 0;
    return Math.round((m.memoryUsedMb / m.memoryMaxMb) * 100);
  }

  dbPercent(): number {
    const d = this.database();
    if (!d || !d.maxConnections) return 0;
    return Math.round((d.activeConnections / d.maxConnections) * 100);
  }
}
