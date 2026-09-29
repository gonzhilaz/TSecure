import { DashboardStats, NdpOrderRequest, Subscriber, ThreatEvent } from '@/types';

const BASE_URL = process.env.NEXT_PUBLIC_BACKEND_URL || 'https://backend-psi-snowy-96.vercel.app';

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
