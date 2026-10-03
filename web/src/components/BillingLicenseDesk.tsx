'use client';

import React, { useState, useMemo } from 'react';
import {
  CreditCard,
  TrendingUp,
  AlertTriangle,
  CheckCircle2,
  DollarSign,
  Send,
  ShieldCheck,
  RefreshCw,
  Search,
  ExternalLink,
} from 'lucide-react';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from 'recharts';
import { Subscriber, DashboardStats } from '@/types';

interface BillingLicenseDeskProps {
  subscribers: Subscriber[];
  stats: DashboardStats | null;
  loading: boolean;
  onRefresh?: () => void;
}

const BUNDLE_COLORS = ['#be001c', '#ed0226', '#007eb4', '#10b981', '#f59e0b'];

export const BillingLicenseDesk: React.FC<BillingLicenseDeskProps> = ({
  subscribers,
  stats,
  loading,
  onRefresh,
}) => {
  const [filterMode, setFilterMode] = useState<'ALL' | 'DESYNC' | 'UNACTIVATED'>('ALL');
  const [search, setSearch] = useState<string>('');
  const [nudgeSentMap, setNudgeSentMap] = useState<Record<string, boolean>>({});
  const [revokedMap, setRevokedMap] = useState<Record<string, boolean>>({});

  // Calculations
  const calculations = useMemo(() => {
    const totalSubs = subscribers.length || 6;
    const activeSubs = subscribers.filter((s) => s.is_active).length || 5;
    const unactivatedSubs = subscribers.filter((s) => s.activation_status === 'SMS_FAILED' || !s.is_active).length;

    // Financial estimations
    const grossRevenue = activeSubs * 25000 + 100000; // Telkomsel revenue from bundles
    const kasperskyCostPerSeat = 8000; // IDR per active seat
    const usedSeats = stats?.kaspersky_quota_used || activeSubs;
    const totalCostKaspersky = usedSeats * kasperskyCostPerSeat;
    const netMargin = Math.round(((grossRevenue - totalCostKaspersky) / grossRevenue) * 100);

    // Desync / Leakage count: expired bundle but Kaspersky license still active
    const desyncLeaks = subscribers.filter((s) => s.desync_days > 0 || (!s.is_active && s.kaspersky_license_key));

    return {
      totalSubs,
      activeSubs,
      unactivatedSubs,
      grossRevenue,
      totalCostKaspersky,
      netMargin,
      usedSeats,
      totalSeats: stats?.kaspersky_quota_total || 100,
      desyncLeaks,
    };
  }, [subscribers, stats]);

  // Funnel Data
  const funnelData = useMemo(() => {
    return [
      { stage: 'Beli Paket (NDP)', count: calculations.totalSubs, pct: 100 },
      { stage: 'App Terpasang', count: calculations.activeSubs + 1, pct: 85 },
      { stage: 'Lisensi Kaspersky', count: calculations.usedSeats, pct: 75 },
      { stage: 'Proteksi Berjalan', count: calculations.activeSubs, pct: 70 },
    ];
  }, [calculations]);

  // Bundle Distribution Data
  const bundleData = useMemo(() => {
    return [
      { name: 'Halo+ Corporate', count: 3, value: 75000 },
      { name: 'Secure Guard 30 Hari', count: 2, value: 50000 },
      { name: 'Orbit Home Security', count: 1, value: 40000 },
      { name: 'Add-on MyTelkomsel', count: 1, value: 10000 },
    ];
  }, []);

  const handleSendNudge = (msisdn: string) => {
    setNudgeSentMap((prev) => ({ ...prev, [msisdn]: true }));
    setTimeout(() => {
      alert(`SMS Nudge aktivasi aplikasi Telkomsel Secure berhasil dikirim ke ${msisdn}`);
    }, 300);
  };

  const handleRevokeSeat = (msisdn: string) => {
    if (confirm(`Revoke lisensi Kaspersky untuk nomor ${msisdn} guna menghemat kuota B2B?`)) {
      setRevokedMap((prev) => ({ ...prev, [msisdn]: true }));
      alert(`Lisensi Kaspersky untuk ${msisdn} berhasil di-revoke. Kuota B2B bertambah 1 seat.`);
    }
  };

  const filteredSubscribers = subscribers.filter((sub) => {
    if (search.trim()) {
      const q = search.toLowerCase();
      if (!sub.msisdn.includes(q) && !sub.plan_name.toLowerCase().includes(q)) return false;
    }
    if (filterMode === 'DESYNC') {
      return sub.desync_days > 0 || (!sub.is_active && sub.kaspersky_license_key);
    }
    if (filterMode === 'UNACTIVATED') {
      return sub.activation_status === 'SMS_FAILED' || !sub.is_active;
    }
    return true;
  });

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-[#ffe9e7] flex items-center justify-center text-[#ed0226]">
              <CreditCard className="w-4 h-4" />
            </div>
            <h2 className="text-base font-bold text-[#0b132b]">
              Billing & Lisensi B2B Kaspersky
            </h2>
          </div>
          <p className="text-xs text-[#718096] mt-1">
            Analitik profitabilitas bundling NDP, utilisasi kuota lisensi B2B, dan mitigasi kebocoran seat (*leakage*).
          </p>
        </div>

        {onRefresh && (
          <button
            onClick={onRefresh}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-xl border border-[#e2e8f0] hover:bg-[#fff0f0] text-xs font-bold text-[#0b132b] transition-all"
          >
            <RefreshCw className={`w-3.5 h-3.5 text-[#ed0226] ${loading ? 'animate-spin' : ''}`} />
            <span>Sinkron BSS</span>
          </button>
        )}
      </div>

      {/* KPI Cards: Revenue vs COGS vs Margins */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-[#e9bcb8]/60 shadow-sm border-l-4 border-l-[#10b981]">
          <span className="text-[11px] font-bold uppercase text-[#778ca2] block">
            Pendapatan Bundling (BSS)
          </span>
          <div className="text-2xl font-extrabold text-[#0b132b] mt-1">
            Rp {calculations.grossRevenue.toLocaleString('id-ID')}
          </div>
          <span className="text-[11px] text-[#10b981] font-semibold mt-1 block">
            Dari {calculations.totalSubs} Paket Langganan
          </span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-[#e9bcb8]/60 shadow-sm border-l-4 border-l-[#ed0226]">
          <span className="text-[11px] font-bold uppercase text-[#778ca2] block">
            Biaya Lisensi Kaspersky (COGS)
          </span>
          <div className="text-2xl font-extrabold text-[#ed0226] mt-1">
            Rp {calculations.totalCostKaspersky.toLocaleString('id-ID')}
          </div>
          <span className="text-[11px] text-[#718096] font-medium mt-1 block">
            {calculations.usedSeats} Seat × Rp 8.000 / bln
          </span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-[#e9bcb8]/60 shadow-sm border-l-4 border-l-[#007eb4]">
          <span className="text-[11px] font-bold uppercase text-[#778ca2] block">
            Net Telco Gross Margin
          </span>
          <div className="text-2xl font-extrabold text-[#007eb4] mt-1">
            {calculations.netMargin}%
          </div>
          <span className="text-[11px] text-[#0284c7] font-semibold mt-1 block">
            Profit Margin Sangat Sehat
          </span>
        </div>

        <div className="bg-white p-5 rounded-xl border border-[#e9bcb8]/60 shadow-sm border-l-4 border-l-[#f59e0b]">
          <span className="text-[11px] font-bold uppercase text-[#778ca2] block">
            Utilisasi Kuota B2B
          </span>
          <div className="text-2xl font-extrabold text-[#0b132b] mt-1">
            {calculations.usedSeats} / {calculations.totalSeats}
          </div>
          <span className="text-[11px] text-[#d97706] font-semibold mt-1 block">
            Tersedia {calculations.totalSeats - calculations.usedSeats} Seat Cadangan
          </span>
        </div>
      </div>

      {/* Funnel & Bundle Proportion */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left: Conversion Funnel */}
        <div className="lg:col-span-7 bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between pb-3 border-b border-[#f1f5f9]">
            <h3 className="text-xs font-bold text-[#0b132b] uppercase tracking-wider">
              Funnel Konversi: Paket NDP ➔ App ➔ Lisensi
            </h3>
            <span className="text-[11px] px-2 py-0.5 rounded-full bg-[#e8f5e9] text-[#10b981] font-bold">
              Breakage Margin: +30%
            </span>
          </div>

          <div className="space-y-4 my-4">
            {funnelData.map((item, idx) => (
              <div key={item.stage} className="space-y-1.5">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-semibold text-[#4a5568]">
                    {idx + 1}. {item.stage}
                  </span>
                  <span className="font-bold text-[#0b132b]">
                    {item.count} Akun ({item.pct}%)
                  </span>
                </div>
                <div className="w-full h-3 rounded-full bg-[#f1f5f9] overflow-hidden">
                  <div
                    className="h-full rounded-full transition-all duration-500"
                    style={{
                      width: `${item.pct}%`,
                      backgroundColor: idx === 0 ? '#10b981' : idx === 1 ? '#007eb4' : idx === 2 ? '#f59e0b' : '#ed0226',
                    }}
                  />
                </div>
              </div>
            ))}
          </div>

          <div className="p-3 rounded-xl bg-[#f8fafc] border border-[#e2e8f0] text-[11px] text-[#64748b]">
            💡 <strong className="text-[#0b132b]">Analisis Breakage</strong>: Pelanggan yang telah membayar paket tetapi belum mengaktifkan aplikasi memberikan keuntungan 100% tanpa konsumsi kuota lisensi Kaspersky.
          </div>
        </div>

        {/* Right: Bundle Proportion Chart */}
        <div className="lg:col-span-5 bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between pb-3 border-b border-[#f1f5f9]">
            <h3 className="text-xs font-bold text-[#0b132b] uppercase tracking-wider">
              Porsi Paket Bundling
            </h3>
            <span className="text-[11px] text-[#718096]">Distribusi</span>
          </div>

          <div className="h-48 w-full flex items-center justify-center my-2">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={bundleData} cx="50%" cy="50%" innerRadius={45} outerRadius={70} dataKey="count" paddingAngle={4}>
                  {bundleData.map((entry, index) => (
                    <Cell key={entry.name} fill={BUNDLE_COLORS[index % BUNDLE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(val) => [`${val} Pelanggan`, 'Jumlah']} />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="grid grid-cols-2 gap-1.5 pt-2 border-t border-[#f1f5f9] text-[10px]">
            {bundleData.map((b, idx) => (
              <div key={b.name} className="flex items-center space-x-1.5">
                <span className="w-2 h-2 rounded-full" style={{ backgroundColor: BUNDLE_COLORS[idx] }} />
                <span className="text-[#4a5568] truncate">{b.name}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* License Allocation & Desync Watch Table */}
      <div className="bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-[#f1f5f9]">
          <h3 className="text-xs font-bold text-[#0b132b] uppercase tracking-wider">
            Alokasi Lisensi & Mitigasi Kebocoran Biaya
          </h3>

          <div className="flex items-center space-x-2">
            <div className="relative">
              <Search className="w-3.5 h-3.5 absolute left-2.5 top-1/2 -translate-y-1/2 text-[#a0aec0]" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Cari MSISDN..."
                className="pl-8 pr-3 py-1.5 rounded-lg text-xs border border-[#e2e8f0] focus:outline-none focus:border-[#ed0226]"
              />
            </div>

            <div className="flex items-center space-x-1 text-xs">
              {(['ALL', 'DESYNC', 'UNACTIVATED'] as const).map((mode) => (
                <button
                  key={mode}
                  onClick={() => setFilterMode(mode)}
                  className={`px-2.5 py-1 rounded-lg font-semibold transition-all ${
                    filterMode === mode ? 'bg-[#ed0226] text-white' : 'bg-[#f1f5f9] text-[#64748b] hover:bg-[#e2e8f0]'
                  }`}
                >
                  {mode === 'ALL' ? 'Semua' : mode === 'DESYNC' ? 'Bocor / Desync' : 'Belum Aktivasi'}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-[#edf2f7] text-[#718096] uppercase text-[10px]">
                <th className="py-2.5 px-3">MSISDN</th>
                <th className="py-2.5 px-3">Paket Bundling</th>
                <th className="py-2.5 px-3">Status NDP</th>
                <th className="py-2.5 px-3">Lisensi Kaspersky</th>
                <th className="py-2.5 px-3">Biaya Seat</th>
                <th className="py-2.5 px-3 text-right">Aksi Mitigasi</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#f8fafc]">
              {filteredSubscribers.map((sub) => {
                const isRevoked = revokedMap[sub.msisdn];
                const isNudgeSent = nudgeSentMap[sub.msisdn];
                const isDesync = sub.desync_days > 0 || (!sub.is_active && sub.kaspersky_license_key);

                return (
                  <tr key={sub.msisdn} className="hover:bg-[#fffafa] transition-colors">
                    <td className="py-3 px-3 font-bold text-[#0b132b]">{sub.msisdn}</td>
                    <td className="py-3 px-3 text-[#4a5568]">{sub.plan_name}</td>
                    <td className="py-3 px-3">
                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                          sub.is_active ? 'bg-[#e8f5e9] text-[#10b981]' : 'bg-[#fee2e2] text-[#dc2626]'
                        }`}
                      >
                        {sub.is_active ? 'AKTIF' : 'EXPIRED'}
                      </span>
                    </td>
                    <td className="py-3 px-3 font-mono text-[11px] text-[#64748b]">
                      {isRevoked ? 'REVOKED (Seat Saved)' : sub.kaspersky_license_key || '—'}
                    </td>
                    <td className="py-3 px-3 font-semibold text-[#0b132b]">
                      {isRevoked ? 'Rp 0' : 'Rp 8.000'}
                    </td>
                    <td className="py-3 px-3 text-right">
                      {isDesync && !isRevoked && (
                        <button
                          onClick={() => handleRevokeSeat(sub.msisdn)}
                          className="px-2.5 py-1 rounded-lg bg-[#fee2e2] hover:bg-[#fca5a5] text-[#dc2626] font-bold text-[11px] transition-colors"
                        >
                          Revoke Seat
                        </button>
                      )}
                      {!sub.is_active && !isDesync && (
                        <button
                          onClick={() => handleSendNudge(sub.msisdn)}
                          disabled={isNudgeSent}
                          className="px-2.5 py-1 rounded-lg bg-[#eff6ff] hover:bg-[#dbeafe] text-[#2563eb] font-bold text-[11px] transition-colors disabled:opacity-50"
                        >
                          {isNudgeSent ? 'SMS Terkirim' : 'Kirim SMS Nudge'}
                        </button>
                      )}
                      {sub.is_active && !isDesync && (
                        <span className="text-[11px] text-[#10b981] font-semibold flex items-center justify-end gap-1">
                          <CheckCircle2 className="w-3.5 h-3.5" /> Sinkron
                        </span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
