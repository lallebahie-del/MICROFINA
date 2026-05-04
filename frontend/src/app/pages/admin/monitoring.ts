import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MonitoringService, AppMetrics, DatabaseMetrics, JobStatus, SessionInfo } from '../../services/monitoring.service';

@Component({
  selector: 'app-monitoring',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './monitoring.html'
})
export class MonitoringComponent implements OnInit, OnDestroy {
  metrics: AppMetrics | null = null;
  database: DatabaseMetrics | null = null;
  jobs: JobStatus[] = [];
  sessions: SessionInfo | null = null;
  lastRefresh: Date = new Date();
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
    this.lastRefresh = new Date();
    this.svc.getMetrics().subscribe({ next: d => (this.metrics = d) });
    this.svc.getDatabase().subscribe({ next: d => (this.database = d) });
    this.svc.getJobs().subscribe({ next: d => (this.jobs = d) });
    this.svc.getSessions().subscribe({ next: d => (this.sessions = d) });
  }

  memoryPercent(): number {
    if (!this.metrics) return 0;
    return Math.round((this.metrics.memoryUsedMb / this.metrics.memoryMaxMb) * 100);
  }

  dbPercent(): number {
    if (!this.database) return 0;
    return Math.round((this.database.activeConnections / this.database.maxConnections) * 100);
  }
}
