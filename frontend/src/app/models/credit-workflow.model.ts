export type Etape =
  | 'SAISIE'
  | 'COMPLETUDE'
  | 'ANALYSE_FINANCIERE'
  | 'VISA_RC'
  | 'COMITE'
  | 'VISA_SF'
  | 'DEBLOCAGE_PENDING'
  | 'DEBLOQUE'
  | 'CLOTURE'
  | 'REJETE';

export function etapeLabel(e: Etape): string {
  const labels: Record<Etape, string> = {
    SAISIE:              'Saisie',
    COMPLETUDE:          'Complétude',
    ANALYSE_FINANCIERE:  'Analyse financière',
    VISA_RC:             'Visa RC',
    COMITE:              'Comité de crédit',
    VISA_SF:             'Visa SF',
    DEBLOCAGE_PENDING:   'Déblocage en attente',
    DEBLOQUE:            'Débloqué',
    CLOTURE:             'Clôturé',
    REJETE:              'Rejeté',
  };
  return labels[e] ?? e;
}

export interface WorkflowTimelineEntry {
  etape:        string;
  statutAvant:  string;
  statutApres:  string;
  dateVisa:     string;
  decision:     string;
  commentaire:  string;
  utilisateur:  string;
}

export interface AnalyseFinanciereDTO {
  idAnalyse:             number;
  creditId:              number;
  typeAnalyse:           string;
  revenusMensuels:       number;
  chargesMensuelles:     number;
  capaciteRemboursement: number;
  ratioEndettement:      number;
  totalActif:            number;
  totalPassif:           number;
  indicateursJson:       string;
  commentaire:           string;
  avisAgent:             string;
  dateAnalyse:           string;
  utilisateur:           string;
}

export interface WorkflowDecisionRequest {
  commentaire?: string;
}

export interface AnalyseFinanciereCreateRequest {
  revenusMensuels:    number;
  chargesMensuelles:  number;
  totalActif?:        number;
  totalPassif?:       number;
  indicateursJson?:   string;
  commentaire?:       string;
  avisAgent?:         string;
}

export interface DeblocageRequest {
  montantDeblocage:      number;
  datePremiereEcheance:  string;
  periodicite:           string;
  nombreEcheance:        number;
  delaiGrace?:           number;
  canal:                 'CAISSE' | 'BANQUE' | 'WALLET';
  compteBanqueId?:       number;
  numCompteCaisse?:      string;
}

export interface WorkflowCreditSummary {
  idCredit:       number;
  numCredit:      string;
  statut:         string;
  etapeCourante:  Etape;
}
