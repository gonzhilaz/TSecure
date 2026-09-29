export interface SOCOperator {
  id: string;
  name: string;
  email: string;
  role: 'SOC Lead' | 'SOC Analyst' | 'Security Administrator' | 'Customer Care Helpdesk';
  badgeNumber: string;
  avatarUrl?: string;
  loginAt: string;
}

const STORAGE_KEY = 'telkomsel_soc_operator_session';

export const DEMO_OPERATORS: Record<string, { pass: string; operator: Omit<SOCOperator, 'loginAt'> }> = {
  'admin@telkomsel.co.id': {
    pass: 'admin123',
    operator: {
      id: 'OP-001',
      name: 'Budi Darmawan',
      email: 'admin@telkomsel.co.id',
      role: 'Security Administrator',
      badgeNumber: 'TS-SEC-9901',
    },
  },
  'analyst@telkomsel.co.id': {
    pass: 'analyst123',
    operator: {
      id: 'OP-002',
      name: 'Siti Rahmawati',
      email: 'analyst@telkomsel.co.id',
      role: 'SOC Analyst',
      badgeNumber: 'TS-SOC-4412',
    },
  },
  'helpdesk@telkomsel.co.id': {
    pass: 'helpdesk123',
    operator: {
      id: 'OP-003',
      name: 'Rian Pratama',
      email: 'helpdesk@telkomsel.co.id',
      role: 'Customer Care Helpdesk',
      badgeNumber: 'TS-CARE-2105',
    },
  },
};

export function getCurrentOperator(): SOCOperator | null {
  if (typeof window === 'undefined') return null;
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as SOCOperator;
  } catch {
    return null;
  }
}

export function loginOperator(email: string, pass: string): { success: boolean; operator?: SOCOperator; error?: string } {
  const account = DEMO_OPERATORS[email.trim().toLowerCase()];
  if (!account || account.pass !== pass) {
    return { success: false, error: 'Email atau kata sandi SOC tidak valid.' };
  }

  const session: SOCOperator = {
    ...account.operator,
    loginAt: new Date().toISOString(),
  };

  localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  return { success: true, operator: session };
}

export function quickDemoLogin(role: 'admin' | 'analyst' | 'helpdesk'): SOCOperator {
  const emailMap = {
    admin: 'admin@telkomsel.co.id',
    analyst: 'analyst@telkomsel.co.id',
    helpdesk: 'helpdesk@telkomsel.co.id',
  };
  const account = DEMO_OPERATORS[emailMap[role]];
  const session: SOCOperator = {
    ...account.operator,
    loginAt: new Date().toISOString(),
  };
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  return session;
}

export function logoutOperator(): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(STORAGE_KEY);
}
