import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CreditWorkflowService } from '../../services/credit-workflow.service';
import { WorkflowCreditSummary, etapeLabel } from '../../models/credit-workflow.model';

@Component({
  selector: 'app-credits-comite-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './credits-comite-page.component.html',
})
export class CreditsComitePageComponent implements OnInit {

  dossiers: WorkflowCreditSummary[] = [];
  loading = false;
  error: string | null = null;
  etapeLabel = etapeLabel;

  constructor(
    private workflowSvc: CreditWorkflowService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.workflowSvc.getComitePending().subscribe({
      next: data => { this.dossiers = data; this.loading = false; },
      error: () => { this.error = 'Impossible de charger les dossiers.'; this.loading = false; }
    });
  }

  ouvrir(id: number): void {
    this.router.navigate(['/credits', id, 'workflow']);
  }
}
