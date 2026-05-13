import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AppMetrics {
  memoryUsedMb: number;
  memoryMaxMb: number;
  uptime: string;
  activeThreads: number;
  requestsPerMinute: number;
}

export interface DatabaseMetrics {
  status: string;
  activeConnections: number;
  maxConnections: number;
  pendingQueries: number;
  dbSizeMb: number;
}

export interface JobStatus {
  nomJob: string;
  dernierExecution?: string;
  statut?: string;
  nbTraites?: number;
}

export interface SessionInfo {
  activeUsers: number;
  sessions: { login: string; sessionId?: string; lastActivity: string; expired?: boolean }[];
}

@Injectable({ providedIn: 'root' })
export class MonitoringService {
  private base = `${environment.apiUrl}/api/v1/admin/monitoring`;

  constructor(private http: HttpClient) {}

  getMetrics(): Observable<AppMetrics> {
    return this.http.get<AppMetrics>(`${this.base}/metrics`);
  }

  getDatabase(): Observable<DatabaseMetrics> {
    return this.http.get<DatabaseMetrics>(`${this.base}/database`);
  }

  getJobs(): Observable<JobStatus[]> {
    return this.http.get<JobStatus[]>(`${this.base}/jobs`);
  }

  getSessions(): Observable<SessionInfo> {
    return this.http.get<SessionInfo>(`${this.base}/sessions`);
  }
}
