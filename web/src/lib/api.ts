import {
  AuditLog,
  DashboardStats,
  DatabaseMaintenanceStats,
  DeadLetterRecord,
  IngestionApiKey,
  NdpOrderRequest,
  Operator,
  PhishingRecord,
  PhishingStats,
  PhishingStatusUpdateRequest,
  RetentionRunResult,
  Subscriber,
  ThreatEvent,
} from '@/types';
import { getAuthToken } from './auth';

const rawUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'https://backend-i3wy.vercel.app';
const BASE_URL = rawUrl.replace(/\/+$/, '');

function authHeaders(): Record<string, string> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  const token = getAuthToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

export async function fetchDashboardStats(): Promise<DashboardStats> {
  const res = await fetch(`${BASE_URL}/api/v1/dashboard/stats`, {
    cache: 'no-store',
  });
  if (!res.ok) {
    throw new Error(`Failed to fetch stats: ${res.statusText}`);
  }
  return res.json();
}

export async function fetchSubscribers(): Promise<Subscriber[]> {
  const res = await fetch(`${BASE_URL}/api/v1/dashboard/subscribers`, {
    cache: 'no-store',
  });
  if (!res.ok) {
    throw new Error(`Failed to fetch subscribers: ${res.statusText}`);
  }
  return res.json();
}

export async function fetchRecentThreats(limit = 20): Promise<ThreatEvent[]> {
  const res = await fetch(`${BASE_URL}/api/v1/dashboard/threats?limit=${limit}`, {
    cache: 'no-store',
  });
  if (!res.ok) {
    throw new Error(`Failed to fetch threats: ${res.statusText}`);
  }
  return res.json();
}

export async function simulateNdpPurchase(req: NdpOrderRequest): Promise<{ status: string; subscriber: Subscriber }> {
  const res = await fetch(`${BASE_URL}/api/v1/ndp/simulate-purchase`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  });
  if (!res.ok) {
    throw new Error(`Purchase simulation failed: ${res.statusText}`);
  }
  return res.json();
}

export async function simulateNdpExpire(msisdn: string): Promise<{ status: string; subscriber: Subscriber }> {
  const res = await fetch(`${BASE_URL}/api/v1/ndp/simulate-expire`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ msisdn }),
  });
  if (!res.ok) {
    throw new Error(`Expire simulation failed: ${res.statusText}`);
  }
  return res.json();
}

export async function simulateNdpUnactivated(msisdn: string): Promise<{ status: string; subscriber: Subscriber }> {
  const res = await fetch(`${BASE_URL}/api/v1/ndp/simulate-unactivated`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ msisdn }),
  });
  if (!res.ok) {
    throw new Error(`Unactivated simulation failed: ${res.statusText}`);
  }
  return res.json();
}

export async function searchSubscribers(query = '', status = ''): Promise<Subscriber[]> {
  const params = new URLSearchParams();
  if (query) params.set('q', query);
  if (status) params.set('status', status);
  const res = await fetch(`${BASE_URL}/api/v1/admin/subscribers/search?${params.toString()}`, {
    cache: 'no-store',
  });
  if (!res.ok) {
    throw new Error(`Search subscribers failed: ${res.statusText}`);
  }
  return res.json();
}

export async function resendActivationCode(msisdn: string): Promise<{ status: string; message: string; subscriber: Subscriber }> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/resend-code`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ msisdn }),
  });
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.error || `Resend activation code failed: ${res.statusText}`);
  }
  return res.json();
}

export async function resyncKasperskyLicense(msisdn: string): Promise<{ status: string; message: string; subscriber: Subscriber }> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/resync-license`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ msisdn }),
  });
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.error || `Resync license failed: ${res.statusText}`);
  }
  return res.json();
}

export async function migrateDevice(data: {
  msisdn: string;
  new_mobile_id: string;
  new_device_model: string;
  new_os_version: string;
  reason?: string;
}): Promise<{ status: string; message: string; subscriber: Subscriber }> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/migrate-device`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.error || `Device migration failed: ${res.statusText}`);
  }
  return res.json();
}

export function createEventSource(): EventSource {
  return new EventSource(`${BASE_URL}/api/v1/dashboard/stream`);
}

export async function clearDashboardData(): Promise<{ status: string; message: string }> {
  const res = await fetch(`${BASE_URL}/api/v1/dashboard/clear`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
  });
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.error || `Clear dashboard data failed: ${res.statusText}`);
  }
  return res.json();
}

export async function fetchPhishingRecords(query = '', brand = '', status = ''): Promise<PhishingRecord[]> {
  const params = new URLSearchParams();
  if (query) params.set('q', query);
  if (brand && brand !== 'ALL') params.set('brand', brand);
  if (status && status !== 'ALL') params.set('status', status);
  const res = await fetch(`${BASE_URL}/api/v1/phishing/records?${params.toString()}`, {
    cache: 'no-store',
  });
  if (!res.ok) {
    throw new Error(`Failed to fetch phishing records: ${res.statusText}`);
  }
  const data = await res.json();
  return data.data || [];
}

export async function fetchPhishingStats(): Promise<PhishingStats> {
  const res = await fetch(`${BASE_URL}/api/v1/phishing/stats`, {
    cache: 'no-store',
  });
  if (!res.ok) {
    throw new Error(`Failed to fetch phishing stats: ${res.statusText}`);
  }
  const data = await res.json();
  return data.data;
}

export async function updatePhishingStatus(
  req: PhishingStatusUpdateRequest
): Promise<{ success: boolean; data: PhishingRecord }> {
  const res = await fetch(`${BASE_URL}/api/v1/phishing/status`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  });
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.error || `Update phishing status failed: ${res.statusText}`);
  }
  return res.json();
}

export function getPhishingExportUrl(): string {
  return `${BASE_URL}/api/v1/export/phishing`;
}

// ==================== User Management & RBAC ====================
export async function fetchOperators(): Promise<Operator[]> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/operators`, {
    headers: authHeaders(),
    cache: 'no-store',
  });
  if (!res.ok) throw new Error(`Gagal memuat operator: ${res.statusText}`);
  const data = await res.json();
  return data.data || [];
}

export async function createOperator(data: {
  name: string;
  email: string;
  password: string;
  role: string;
  badge_number?: string;
}): Promise<{ success: boolean; operator: Operator }> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/operators`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `Gagal menambah operator: ${res.statusText}`);
  }
  return res.json();
}

export async function toggleOperatorStatus(
  id: string,
  active: boolean
): Promise<{ success: boolean; operator: Operator }> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/operators/status`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ id, active }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `Gagal mengubah status operator: ${res.statusText}`);
  }
  return res.json();
}

export async function fetchAuditLogs(limit = 50): Promise<AuditLog[]> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/audit-logs?limit=${limit}`, {
    headers: authHeaders(),
    cache: 'no-store',
  });
  if (!res.ok) throw new Error(`Gagal memuat audit log: ${res.statusText}`);
  const data = await res.json();
  return data.data || [];
}

// ==================== Ingestion Gateway & DLQ ====================
export async function fetchApiKeys(): Promise<IngestionApiKey[]> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/ingest/keys`, {
    headers: authHeaders(),
    cache: 'no-store',
  });
  if (!res.ok) throw new Error(`Gagal memuat API keys: ${res.statusText}`);
  const data = await res.json();
  return data.data || [];
}

export async function createApiKey(
  name: string,
  source: string
): Promise<{ success: boolean; data: IngestionApiKey }> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/ingest/keys`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ name, source }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `Gagal membuat API key: ${res.statusText}`);
  }
  return res.json();
}

export async function fetchDLQRecords(status = 'ALL'): Promise<DeadLetterRecord[]> {
  const params = status && status !== 'ALL' ? `?status=${status}` : '';
  const res = await fetch(`${BASE_URL}/api/v1/admin/ingest/dlq${params}`, {
    headers: authHeaders(),
    cache: 'no-store',
  });
  if (!res.ok) throw new Error(`Gagal memuat DLQ records: ${res.statusText}`);
  const data = await res.json();
  return data.data || [];
}

export async function resolveDLQRecord(
  id: string,
  action: 'replay' | 'discard',
  notes = ''
): Promise<{ success: boolean; data: DeadLetterRecord }> {
  const endpoint = action === 'replay' ? 'replay' : 'discard';
  const res = await fetch(`${BASE_URL}/api/v1/admin/ingest/dlq/${endpoint}`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ id, notes }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `Gagal memproses DLQ record: ${res.statusText}`);
  }
  return res.json();
}

// ==================== Database Maintenance & Lifecycle ====================
export async function fetchMaintenanceStats(): Promise<DatabaseMaintenanceStats> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/maintenance/stats`, {
    headers: authHeaders(),
    cache: 'no-store',
  });
  if (!res.ok) throw new Error(`Gagal memuat status database: ${res.statusText}`);
  const data = await res.json();
  return data.data;
}

export function getDatabaseBackupUrl(): string {
  return `${BASE_URL}/api/v1/admin/maintenance/backup`;
}

export async function restoreDatabaseBackup(
  snapshot: unknown
): Promise<{ success: boolean; message: string }> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/maintenance/restore`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(snapshot),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `Gagal memulihkan database: ${res.statusText}`);
  }
  return res.json();
}

export async function runRetentionPolicy(params?: {
  threat_days?: number;
  audit_days?: number;
  dlq_days?: number;
}): Promise<{ success: boolean; data: RetentionRunResult }> {
  const res = await fetch(`${BASE_URL}/api/v1/admin/maintenance/retention/run`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(params || {}),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || `Gagal menjalankan retensi: ${res.statusText}`);
  }
  return res.json();
}


