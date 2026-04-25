import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  get<T>(path: string, params?: Record<string, string | number | boolean | undefined>): Observable<T> {
    console.log(`Mock GET: ${path}`, params);
    return of(this.getMockData(path, params) as T).pipe(delay(100));
  }

  post<T>(path: string, body?: unknown): Observable<T> {
    console.log(`Mock POST: ${path}`, body);
    return of(this.getMockData(path, undefined, body) as T).pipe(delay(200));
  }

  patch<T>(path: string, body?: unknown): Observable<T> {
    console.log(`Mock PATCH: ${path}`, body);
    return of({ ok: true } as T).pipe(delay(500));
  }

  delete<T>(path: string): Observable<T> {
    console.log(`Mock DELETE: ${path}`);
    return of({ ok: true } as T).pipe(delay(500));
  }

  private getMockData(path: string, params?: any, body?: any): any {
    if (path.includes('auth/login')) {
      return {
        id: '1',
        username: body?.username || 'admin',
        fullName: 'Administrateur Système',
        role: 'admin',
        email: 'admin@microfin.sn',
        branchId: 'b1',
        branchName: 'Dakar Plateau'
      };
    }
    if (path.includes('auth/me')) {
      return {
        id: '1',
        username: 'admin',
        fullName: 'Aminata Sow',
        role: 'admin',
        email: 'admin@microfin.sn',
        branchId: 'b1',
        branchName: 'Dakar Plateau'
      };
    }
    if (path.includes('dashboard')) {
      return {
        totalClients: 12,
        newClientsMonth: 12,
        totalDeposits: 7154946,
        totalCreditsGranted: 34588835,
        totalRepayments: 180000000,
        creditsOverdueCount: 2,
        creditsOverdueAmount: 5400000,
        cashBalance: 12500000,
        activeCredits: 10,
        portfolioAtRisk: 8.61,
      };
    }
    if (path.includes('trend') || path.includes('monthly')) {
      const months = ["Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"];
      return months.map(m => ({ label: m, value: Math.floor(Math.random() * 10000000) + 5000000 }));
    }
    if (path.includes('aging-balance')) {
      return [
        { bucket: 'À jour', count: 750, amount: 350000000 },
        { bucket: '1-30 jours', count: 45, amount: 25000000 },
        { bucket: '31-60 jours', count: 12, amount: 8500000 },
        { bucket: '61-90 jours', count: 8, amount: 4200000 },
        { bucket: '+90 jours', count: 35, amount: 62000000 }
      ];
    }
    if (path.includes('credits-by-product')) {
      return [
        { category: 'classique', count: 450, amount: 250000000 },
        { category: 'islamique', count: 120, amount: 85000000 },
        { category: 'scolaire', count: 280, amount: 45000000 }
      ];
    }
    if (path.startsWith('/clients')) {
      if (path.length > 8) return { id: 'c1', fullName: 'Ousmane Diallo', phone: '771234567', clientType: 'particulier', profession: 'Commerçant', address: 'Dakar' };
      return [
        { id: 'c1', code: 'CLI-0001', fullName: 'Ousmane Diallo', phone: '771234567', clientType: 'particulier', branchName: 'Dakar Plateau' },
        { id: 'c2', code: 'CLI-0002', fullName: 'Awa Ndiaye', phone: '772345678', clientType: 'particulier', branchName: 'Dakar Plateau' },
        { id: 'c3', code: 'CLI-0003', fullName: 'GIE Espoir', phone: '338210000', clientType: 'groupe', branchName: 'Dakar Plateau' }
      ];
    }
    if (path.startsWith('/accounts')) {
      return [
        { id: 'a1', accountNumber: 'EPA-000123', clientName: 'Ousmane Diallo', accountType: 'epargne', balance: 450000 },
        { id: 'a2', accountNumber: 'COU-000456', clientName: 'Awa Ndiaye', accountType: 'courant', balance: 1250000 }
      ];
    }
    if (path.startsWith('/credits')) {
      return [
        { id: 'cr1', reference: 'CRE-2026-001', clientName: 'Ousmane Diallo', principal: 1000000, status: 'en_cours', productType: 'classique' },
        { id: 'cr2', reference: 'CRE-2026-002', clientName: 'Awa Ndiaye', principal: 5000000, status: 'approuve', productType: 'mourabaha' }
      ];
    }
    if (path.startsWith('/caisse') || path.startsWith('/cash')) {
      return [
        { id: 'o1', reference: 'CSH-0001', operationType: 'depot', amount: 50000, clientName: 'Ousmane Diallo', createdAt: new Date().toISOString() },
        { id: 'o2', reference: 'CSH-0002', operationType: 'retrait', amount: 20000, clientName: 'Awa Ndiaye', createdAt: new Date().toISOString() }
      ];
    }
    if (path.startsWith('/utilisateurs')) {
      return [
        { id: '1', username: 'admin', fullName: 'Aminata Sow', role: 'admin', active: true },
        { id: '2', username: 'directeur', fullName: 'Moussa Diop', role: 'directeur', active: true }
      ];
    }
    if (path.startsWith('/branches')) {
      return [
        { id: 'b1', code: 'DKR-01', name: 'Dakar Plateau', city: 'Dakar' },
        { id: 'b2', code: 'ABJ-01', name: 'Abidjan Cocody', city: 'Abidjan' }
      ];
    }
    if (path.startsWith('/audit')) {
      return [
        { id: 'au1', action: 'LOGIN', userName: 'Aminata Sow', entity: 'user', createdAt: new Date().toISOString(), details: 'Connexion réussie' },
        { id: 'au2', action: 'CREATE', userName: 'Aminata Sow', entity: 'client', createdAt: new Date().toISOString(), details: 'Nouveau client Ousmane Diallo' }
      ];
    }
    if (path.startsWith('/comptabilite') || path.includes('journal')) {
      return [
        { id: 'j1', entryDate: '2026-04-24', journal: 'caisse', accountCode: '571100', accountLabel: 'Caisse centrale', debit: 50000, credit: 0, narration: 'Dépôt client' }
      ];
    }
    if (path.includes('balance')) {
      return [
        { accountCode: '101000', accountLabel: 'Capital', debit: 0, credit: 100000000, balance: -100000000 },
        { accountCode: '521100', accountLabel: 'Banque SG', debit: 45000000, credit: 0, balance: 45000000 }
      ];
    }
    if (path.startsWith('/budget')) {
      return [
        { id: 'bg1', label: 'Loyer agence', type: 'depense', plannedAmount: 12000000, realizedAmount: 4000000 },
        { id: 'bg2', label: 'Intérêts crédits', type: 'recette', plannedAmount: 50000000, realizedAmount: 15000000 }
      ];
    }
    return [];
  }
}

