export interface SOCOperator {
  id: string;
  name: string;
  email: string;
  role: 'SUPERADMIN' | 'SOC_ANALYST' | 'CUSTOMER_CARE' | 'AUDITOR' | 'SOC Lead' | 'SOC Analyst' | 'Security Administrator' | 'Customer Care Helpdesk' | string;
  badgeNumber: string;
  avatarUrl?: string;
  loginAt: string;
  token?: string;
}

const STORAGE_KEY = 'telkomsel_soc_operator_session';
const TOKEN_KEY = 'telkomsel_soc_jwt_token';

const rawUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'https://backend-i3wy.vercel.app';
const BASE_URL = rawUrl.replace(/\/+$/, '');

export const DEMO_OPERATORS: Record<string, { pass: string; operator: Omit<SOCOperator, 'loginAt'> }> = {
  'admin@telkomsel.co.id': {
    pass: 'admin123',
    operator: {
      id: 'OP-001',
      name: 'Budi Darmawan',
      email: 'admin@telkomsel.co.id',
      role: 'SUPERADMIN',
      badgeNumber: 'TS-SEC-9901',
    },
  },
  'analyst@telkomsel.co.id': {
    pass: 'analyst123',
    operator: {
      id: 'OP-002',
      name: 'Siti Rahmawati',
      email: 'analyst@telkomsel.co.id',
      role: 'SOC_ANALYST',
      badgeNumber: 'TS-SOC-4412',
    },
  },
  'helpdesk@telkomsel.co.id': {
    pass: 'helpdesk123',
    operator: {
      id: 'OP-003',
      name: 'Rian Pratama',
      email: 'helpdesk@telkomsel.co.id',
      role: 'CUSTOMER_CARE',
      badgeNumber: 'TS-CARE-2105',
    },
  },
  'auditor@telkomsel.co.id': {
    pass: 'auditor123',
    operator: {
      id: 'OP-004',
      name: 'Dewi Lestari',
      email: 'auditor@telkomsel.co.id',
      role: 'AUDITOR',
      badgeNumber: 'TS-AUD-1088',
    },
  },
};

export function getAuthToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
}

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

export async function loginOperator(
  email: string,
  pass: string
): Promise<{ success: boolean; operator?: SOCOperator; error?: string }> {
  const cleanedEmail = email.trim().toLowerCase();

  try {
    const res = await fetch(`${BASE_URL}/api/v1/auth/soc/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: cleanedEmail, password: pass }),
    });

    if (res.ok) {
      const data = await res.json();
      const op: SOCOperator = {
        id: data.operator.id,
        name: data.operator.name,
        email: data.operator.email,
        role: data.operator.role,
        badgeNumber: data.operator.badge_number,
        loginAt: new Date().toISOString(),
        token: data.token,
      };
      if (typeof window !== 'undefined') {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(op));
        localStorage.setItem(TOKEN_KEY, data.token);
      }
      return { success: true, operator: op };
    }
    const errData = await res.json().catch(() => ({}));
    return { success: false, error: errData.error || 'Autentikasi gagal' };
  } catch {
    // Offline / Network Fallback to local demo credentials
    const account = DEMO_OPERATORS[cleanedEmail];
    if (account && account.pass === pass) {
      const session: SOCOperator = {
        ...account.operator,
        loginAt: new Date().toISOString(),
      };
      if (typeof window !== 'undefined') {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
      }
      return { success: true, operator: session };
    }
    return { success: false, error: 'Gagal menghubungi server autentikasi SOC.' };
  }
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
  if (typeof window !== 'undefined') {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  }
  return session;
}

export async function logoutOperator(): Promise<void> {
  if (typeof window === 'undefined') return;
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    fetch(`${BASE_URL}/api/v1/auth/soc/logout`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
    }).catch(() => {});
  }
  localStorage.removeItem(STORAGE_KEY);
  localStorage.removeItem(TOKEN_KEY);
}
