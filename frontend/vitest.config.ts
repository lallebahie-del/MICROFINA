import { defineConfig } from 'vitest/config';

/**
 * Limite la parallélisation pour éviter des blocages de workers sous Windows.
 * Vitest 4 : plus de `poolOptions` — utiliser `maxWorkers` / `minWorkers`.
 * @see docs/FONCTIONNALITES-NON-IMPLEMENTEES.md §4
 */
export default defineConfig({
  test: {
    maxWorkers: 1,
    minWorkers: 1,
    testTimeout: 60_000,
    hookTimeout: 60_000,
  },
});
