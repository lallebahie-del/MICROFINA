import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CreditWorkflowService } from '../../services/credit-workflow.service';
import { CreditsService, Credit } from '../../services/credits.service';
import {
  Etape,
  etapeLabel,
  WorkflowTimelineEntry,
  AnalyseFinanciereDTO,
  WorkflowDecisionRequest,
  AnalyseFinanciereCreateRequest,
  DeblocageRequest,
} from '../../models/credit-workflow.model';

@Component({
  selector: 'app-credit-workflow-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './credit-workflow-page.component.html',
})
export class CreditWorkflowPageComponent implements OnInit {

  readonly ETAPES: Etape[] = [
    'SAISIE','COMPLETUDE','ANALYSE_FINANCIERE','VISA_RC',
    'COMITE','VISA_SF','DEBLOCAGE_PENDING','DEBLOQUE','CLOTURE','REJETE'
  ];

  creditId!: number;
  credit: Credit | null = null;
  timeline: WorkflowTimelineEntry[] = [];
  analyse: AnalyseFinanciereDTO | null = null;
  error: string | null = null;
  loading = false;

  // Modal state
  showModal = false;
  modalType: string = '';
  commentaire = '';
  // Analyse financière form
  revenusMensuels: number | null = null;
  chargesMensuelles: number | null = null;
  totalActif: number | null = null;
  totalPassif: number | null = null;
  avisAgent = '';
  // Déblocage form
  montantDeblocage: number | null = null;
  datePremiereEcheance = '';
  periodicite = 'MENSUEL';
  nombreEcheance: number | null = null;
  delaiGrace: number | null = null;

  etapeLabel = etapeLabel;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private workflowSvc: CreditWorkflowService,
    private creditsSvc: CreditsService,
  ) {}

  ngOnInit(): void {
    this.creditId = Number(this.route.snapshot.paramMap.get('id'));
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.creditsSvc.getOne(this.creditId).subscribe({
      next: c => {
        this.credit = c;
        this.loading = false;
      },
      error: () => { this.error = 'Crédit introuvable.'; this.loading = false; }
    });
    this.workflowSvc.getTimeline(this.creditId).subscribe({
      next: t => this.timeline = t,
      error: () => {}
    });
    this.workflowSvc.getAnalyse(this.creditId).subscribe({
      next: a => this.analyse = a,
      error: () => this.analyse = null
    });
  }

  get etapeCourante(): Etape {
    return ((this.credit as any)?.etapeCourante ?? 'SAISIE') as Etape;
  }

  isComplete(etape: Etape): boolean {
    const idx = this.ETAPES.indexOf(etape);
    const cur = this.ETAPES.indexOf(this.etapeCourante);
    if (this.etapeCourante === 'REJETE') return false;
    return idx < cur;
  }

  isCurrent(etape: Etape): boolean {
    return etape === this.etapeCourante;
  }

  histFor(etape: Etape): WorkflowTimelineEntry | undefined {
    return this.timeline.find(t => t.etape === etape || t.etape?.startsWith(etape));
  }

  openModal(type: string): void {
    this.modalType = type;
    this.commentaire = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  executeAction(): void {
    this.error = null;
    const req: WorkflowDecisionRequest = { commentaire: this.commentaire };
    let obs;
    switch (this.modalType) {
      case 'soumettre':
        obs = this.workflowSvc.soumettre(this.creditId);
        break;
      case 'completude':
        obs = this.workflowSvc.completude(this.creditId, req);
        break;
      case 'analyse':
        const analyseReq: AnalyseFinanciereCreateRequest = {
          revenusMensuels: this.revenusMensuels!,
          chargesMensuelles: this.chargesMensuelles!,
          totalActif: this.totalActif ?? undefined,
          totalPassif: this.totalPassif ?? undefined,
          commentaire: this.commentaire,
          avisAgent: this.avisAgent || undefined,
        };
        obs = this.workflowSvc.analyse(this.creditId, analyseReq);
        break;
      case 'visa-rc':
        obs = this.workflowSvc.visaRc(this.creditId, req);
        break;
      case 'comite-approuver':
        obs = this.workflowSvc.comiteApprouver(this.creditId, req);
        break;
      case 'comite-rejeter':
        obs = this.workflowSvc.comiteRejeter(this.creditId, req);
        break;
      case 'visa-sf':
        obs = this.workflowSvc.visaSf(this.creditId, req);
        break;
      case 'debloquer':
        const deblocageReq: DeblocageRequest = {
          montantDeblocage: this.montantDeblocage!,
          datePremiereEcheance: this.datePremiereEcheance,
          periodicite: this.periodicite,
          nombreEcheance: this.nombreEcheance!,
          delaiGrace: this.delaiGrace ?? undefined,
        };
        obs = this.workflowSvc.debloquer(this.creditId, deblocageReq);
        break;
      case 'rejeter':
        obs = this.workflowSvc.rejeter(this.creditId, req);
        break;
      default:
        return;
    }
    obs.subscribe({
      next: () => { this.closeModal(); this.charger(); },
      error: err => {
        this.error = err.error?.message ?? 'Erreur lors de l\'action.';
        this.closeModal();
      }
    });
  }

  retourListe(): void {
    this.router.navigate(['/credits']);
  }
}
