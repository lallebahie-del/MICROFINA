export function formatFCFA(value: number | string | null | undefined): string {
  const n = Number(value ?? 0);
  if (!Number.isFinite(n)) return '0 FCFA';
  return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(n) + ' FCFA';
}

export function formatNumber(value: number | string | null | undefined, digits = 0): string {
  const n = Number(value ?? 0);
  return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: digits, minimumFractionDigits: digits }).format(n);
}

export function formatDate(value: string | Date | null | undefined): string {
  if (!value) return '—';
  const d = value instanceof Date ? value : new Date(value);
  if (isNaN(d.getTime())) return '—';
  return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

export function formatDateTime(value: string | Date | null | undefined): string {
  if (!value) return '—';
  const d = value instanceof Date ? value : new Date(value);
  if (isNaN(d.getTime())) return '—';
  return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' }) +
    ' ' + d.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
}

export const ROLE_LABELS: Record<string, string> = {
  admin: 'Administrateur',
  directeur: 'Directeur',
  chef_agence: "Chef d'agence",
  auditeur: 'Auditeur',
  agent_caisse: 'Agent de caisse',
  agent_credit: 'Agent de crédit',
  comptable: 'Comptable',
};

export const STATUS_LABELS: Record<string, string> = {
  demande: 'Demande',
  en_attente: 'En attente',
  approuve: 'Approuvé',
  rejete: 'Rejeté',
  debloque: 'Débloqué',
  en_cours: 'En cours',
  en_retard: 'En retard',
  solde: 'Soldé',
  cloture: 'Clôturé',
  actif: 'Actif',
  inactif: 'Inactif',
};

export const PRODUCT_LABELS: Record<string, string> = {
  classique: 'Crédit classique',
  mourabaha: 'Mourabaha',
  moucharaka: 'Moucharaka',
  ijara: 'Ijara',
  decouvert: 'Découvert',
  consommation: 'Crédit consommation',
};

export const OP_TYPE_LABELS: Record<string, string> = {
  depot: 'Dépôt',
  retrait: 'Retrait',
  adhesion: 'Adhésion',
  remboursement: 'Remboursement',
  deblocage: 'Déblocage',
  versement: 'Versement banque',
  prelevement: 'Prélèvement banque',
  frais: 'Frais',
  interet: 'Intérêt',
};

export function statusBadge(status: string): string {
  switch (status) {
    case 'approuve':
    case 'debloque':
    case 'en_cours':
    case 'actif':
    case 'solde':
      return 'badge-success';
    case 'en_retard':
    case 'rejete':
    case 'inactif':
      return 'badge-danger';
    case 'demande':
    case 'en_attente':
      return 'badge-warning';
    case 'cloture':
      return 'badge-muted';
    default:
      return 'badge-info';
  }
}

export function isAuditor(role: string | null | undefined): boolean {
  return role === 'auditeur';
}
