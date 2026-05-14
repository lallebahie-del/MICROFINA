/** Taille de page par défaut pour les listes paginées (alignée sur la liste utilisateurs : 3 lignes). */
export const DEFAULT_LIST_PAGE_SIZE = 3;

export function listTotalPages(total: number, pageSize: number): number {
  if (total <= 0 || pageSize <= 0) return 0;
  return Math.ceil(total / pageSize);
}

export function listClampPage(page: number, total: number, pageSize: number): number {
  const tp = listTotalPages(total, pageSize);
  if (tp === 0) return 0;
  return Math.max(0, Math.min(page, tp - 1));
}

export function listSlice<T>(items: readonly T[], page: number, pageSize: number): T[] {
  const p = listClampPage(page, items.length, pageSize);
  const start = p * pageSize;
  return items.slice(start, start + pageSize);
}

export function listPageIndices(total: number, pageSize: number): number[] {
  const tp = listTotalPages(total, pageSize);
  return Array.from({ length: tp }, (_, i) => i);
}
