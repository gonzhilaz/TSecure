import { SecurityPackage } from '@/types';

const PACKAGES_STORAGE_KEY = 'telkomsel_secure_packages_v1';

export const INITIAL_PACKAGES: SecurityPackage[] = [
  {
    id: 'pkg-sec-30d',
    name: 'Telkomsel Secure Basic 30 Hari',
    code: 'TS-SEC-30D',
    price: 15000,
    duration_days: 30,
    billing_cycle: 'MONTHLY',
    segment: 'PRABAYAR',
    is_active: true,
    description: 'Proteksi lengkap anti-malware Kaspersky, web phishing filter, dan audit jaringan Wi-Fi untuk pelanggan prabayar.',
    features: ['Anti-Malware Realtime', 'Web Phishing Filter', 'Wi-Fi Safety Audit'],
    subscriber_count: 1420,
    total_revenue: 21300000,
    created_at: '2026-09-01T00:00:00Z',
  },
  {
    id: 'pkg-sec-family',
    name: 'Family Guard 30 Hari (5 Perangkat)',
    code: 'TS-SEC-FAM',
    price: 35000,
    duration_days: 30,
    billing_cycle: 'MONTHLY',
    segment: 'PASCABAYAR',
    is_active: true,
    description: 'Paket perlindungan keluarga hingga 5 perangkat dengan SIM Swap watch dan perlindungan transaksi perbankan.',
    features: ['Anti-Malware Realtime', 'Web Filter & Anti-Scam', 'SIM Swap Alert', 'Multi-Device (5 HP)'],
    subscriber_count: 480,
    total_revenue: 16800000,
    created_at: '2026-09-05T00:00:00Z',
  },
  {
    id: 'pkg-sec-7d',
    name: 'Telkomsel Secure Hemat 7 Hari',
    code: 'TS-SEC-7D',
    price: 5000,
    duration_days: 7,
    billing_cycle: 'WEEKLY',
    segment: 'PRABAYAR',
    is_active: true,
    description: 'Paket proteksi mingguan instan untuk pengamanan transaksi dan browsing harian.',
    features: ['Anti-Malware Realtime', 'Web Phishing Filter'],
    subscriber_count: 310,
    total_revenue: 1550000,
    created_at: '2026-09-10T00:00:00Z',
  },
  {
    id: 'pkg-sec-corp',
    name: 'Halo+ Enterprise Endpoint Shield',
    code: 'TS-SEC-CORP',
    price: 75000,
    duration_days: 30,
    billing_cycle: 'MONTHLY',
    segment: 'ENTERPRISE_B2B',
    is_active: true,
    description: 'Paket korporat dengan Device Root/Jailbreak Guard, RASP, integritas SIM, dan sinkronisasi Cyber SOC terpusat.',
    features: ['Anti-Malware KavSDK', 'Web & DNS Gateway Filter', 'Device Integrity Guard', 'SIM Swap Alert', 'SOC Telemetry Feed'],
    subscriber_count: 850,
    total_revenue: 63750000,
    created_at: '2026-08-15T00:00:00Z',
  },
  {
    id: 'pkg-sec-1d',
    name: 'Daily Roaming & Public Wi-Fi Guard',
    code: 'TS-SEC-1D',
    price: 2000,
    duration_days: 1,
    billing_cycle: 'DAILY',
    segment: 'PRABAYAR',
    is_active: false,
    description: 'Proteksi kilat 24 jam untuk pengguna saat bepergian atau terhubung ke Wi-Fi publik bandara/kafe.',
    features: ['Wi-Fi Safety Guard', 'Anti-Phishing Filter'],
    subscriber_count: 95,
    total_revenue: 190000,
    created_at: '2026-09-20T00:00:00Z',
  },
];

export function getStoredPackages(): SecurityPackage[] {
  if (typeof window === 'undefined') return INITIAL_PACKAGES;
  try {
    const raw = localStorage.getItem(PACKAGES_STORAGE_KEY);
    if (!raw) {
      localStorage.setItem(PACKAGES_STORAGE_KEY, JSON.stringify(INITIAL_PACKAGES));
      return INITIAL_PACKAGES;
    }
    return JSON.parse(raw);
  } catch (err) {
    console.error('Failed to load packages from localStorage:', err);
    return INITIAL_PACKAGES;
  }
}

export function saveStoredPackages(packages: SecurityPackage[]): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(PACKAGES_STORAGE_KEY, JSON.stringify(packages));
  } catch (err) {
    console.error('Failed to save packages to localStorage:', err);
  }
}

export function createPackage(
  data: Omit<SecurityPackage, 'id' | 'created_at' | 'subscriber_count' | 'total_revenue'>
): SecurityPackage {
  const current = getStoredPackages();
  const newPackage: SecurityPackage = {
    ...data,
    id: `pkg-${Date.now()}`,
    subscriber_count: 0,
    total_revenue: 0,
    created_at: new Date().toISOString(),
  };
  const updated = [newPackage, ...current];
  saveStoredPackages(updated);
  return newPackage;
}

export function togglePackageStatus(id: string): SecurityPackage | null {
  const current = getStoredPackages();
  const index = current.findIndex((p) => p.id === id);
  if (index === -1) return null;

  current[index].is_active = !current[index].is_active;
  saveStoredPackages(current);
  return current[index];
}

export function deletePackage(id: string): { success: boolean; message: string } {
  const current = getStoredPackages();
  const pkg = current.find((p) => p.id === id);
  if (!pkg) {
    return { success: false, message: 'Paket tidak ditemukan.' };
  }

  if (pkg.subscriber_count > 500) {
    return {
      success: false,
      message: `Paket "${pkg.name}" memiliki ${pkg.subscriber_count} pelanggan aktif. Nonaktifkan paket terlebih dahulu sebelum menghapus.`,
    };
  }

  const updated = current.filter((p) => p.id !== id);
  saveStoredPackages(updated);
  return { success: true, message: `Paket "${pkg.name}" berhasil dihapus.` };
}
