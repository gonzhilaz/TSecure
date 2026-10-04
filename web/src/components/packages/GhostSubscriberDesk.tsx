'use client';

import React, { useState, useMemo } from 'react';
import {
  Ghost,
  Send,
  CheckCircle2,
  Clock,
  Smartphone,
  CheckCheck,
  Search,
} from 'lucide-react';
import { Subscriber } from '@/types';

interface GhostSubscriberDeskProps {
  subscribers: Subscriber[];
  onRefresh?: () => void;
}

export const GhostSubscriberDesk: React.FC<GhostSubscriberDeskProps> = ({
  subscribers,
}) => {
  const [search, setSearch] = useState('');
  const [filterReason, setFilterReason] = useState<'ALL' | 'NO_APP' | 'SMS_FAILED' | 'DORMANT'>('ALL');
  const [nudgedMsisdns, setNudgedMsisdns] = useState<Record<string, boolean>>({});
  const [isBlasting, setIsBlasting] = useState(false);
  const [blastSuccessMessage, setBlastSuccessMessage] = useState<string | null>(null);

  // Identify Ghost Subscribers
  const ghostSubscribers = useMemo(() => {
    return subscribers.filter((s) => {
      const isUninstalled = !s.mobile_id || s.mobile_id.trim() === '';
      const isSmsFailed = s.activation_status === 'SMS_FAILED';
      const isDormant = s.desync_days > 7 || (!s.is_active && s.kaspersky_license_key);
      const isGhost = isUninstalled || isSmsFailed || isDormant;

      if (!isGhost) return false;

      if (filterReason === 'NO_APP') return isUninstalled;
      if (filterReason === 'SMS_FAILED') return isSmsFailed;
      if (filterReason === 'DORMANT') return isDormant && !isUninstalled && !isSmsFailed;

      return true;
    });
  }, [subscribers, filterReason]);

  const filtered = useMemo(() => {
    if (!search.trim()) return ghostSubscribers;
    const q = search.toLowerCase();
    return ghostSubscribers.filter(
      (s) => s.msisdn.toLowerCase().includes(q) || s.plan_name.toLowerCase().includes(q)
    );
  }, [ghostSubscribers, search]);

  const handleNudge = (msisdn: string) => {
    setNudgedMsisdns((prev) => ({ ...prev, [msisdn]: true }));
    setTimeout(() => {
      // Auto clear feedback after 4 seconds
    }, 4000);
  };

  const handleBlastAll = () => {
    setIsBlasting(true);
    setTimeout(() => {
      const newMap: Record<string, boolean> = {};
      ghostSubscribers.forEach((s) => {
        newMap[s.msisdn] = true;
      });
      setNudgedMsisdns(newMap);
      setIsBlasting(false);
      setBlastSuccessMessage(`SMS Nudge berhasil diblast ke ${ghostSubscribers.length} pelanggan Ghost!`);
      setTimeout(() => setBlastSuccessMessage(null), 5000);
    }, 1200);
  };

  return (
    <div className="space-y-6">
      {/* Top Banner & Ghost KPI summary */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 bg-white rounded-2xl border border-[#e2e8f0] shadow-sm flex items-center space-x-3.5">
          <div className="p-3 bg-[#be001c]/10 text-[#be001c] rounded-xl border border-[#be001c]/20">
            <Ghost className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold text-[#0b132b]">{ghostSubscribers.length}</div>
            <div className="text-xs text-[#778ca2]">Ghost Subscribers Terdeteksi</div>
          </div>
        </div>

        <div className="p-4 bg-white rounded-2xl border border-[#e2e8f0] shadow-sm flex items-center space-x-3.5">
          <div className="p-3 bg-[#f59e0b]/10 text-[#f59e0b] rounded-xl border border-[#f59e0b]/20">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold text-[#0b132b]">
              Rp {(ghostSubscribers.length * 8000).toLocaleString('id-ID')}
            </div>
            <div className="text-xs text-[#778ca2]">Potensi License Leakage / Bln</div>
          </div>
        </div>

        <div className="p-4 bg-white rounded-2xl border border-[#e2e8f0] shadow-sm flex items-center space-x-3.5">
          <div className="p-3 bg-[#10b981]/10 text-[#10b981] rounded-xl border border-[#10b981]/20">
            <Smartphone className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold text-[#0b132b]">
              {subscribers.length > 0
                ? `${Math.round(((subscribers.length - ghostSubscribers.length) / subscribers.length) * 100)}%`
                : '100%'}
            </div>
            <div className="text-xs text-[#778ca2]">Rasio Adopsi Nyata (App Active)</div>
          </div>
        </div>
      </div>

      {/* Info Card & Action Bar */}
      <div className="p-5 bg-gradient-to-r from-[#fff8f7] to-white rounded-2xl border border-[#e9bcb8] flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h4 className="text-sm font-bold text-[#0b132b] flex items-center space-x-2">
            <span>Penyelamatan Ghost Subscribers (Onboarding SMS Nudge)</span>
          </h4>
          <p className="text-xs text-[#778ca2] mt-1 max-w-xl">
            Pelanggan di bawah telah memotong pulsa/tagihan untuk paket Telkomsel Secure, namun belum aktif
            melindungi perangkat mereka. Kirimkan instruksi SMS onboarding agar mereka segera memasang aplikasi.
          </p>
        </div>
        <button
          onClick={handleBlastAll}
          disabled={isBlasting || ghostSubscribers.length === 0}
          className="px-4 py-2.5 bg-[#be001c] hover:bg-[#930013] disabled:opacity-50 text-white rounded-xl text-xs font-bold shadow-md shadow-[#be001c]/20 transition flex items-center space-x-2 whitespace-nowrap self-start sm:self-center"
        >
          {isBlasting ? (
            <span>Mengirim SMS Gateway...</span>
          ) : (
            <>
              <Send className="w-4 h-4" />
              <span>Blast Nudge ({ghostSubscribers.length} MSISDN)</span>
            </>
          )}
        </button>
      </div>

      {blastSuccessMessage && (
        <div className="p-3 bg-[#10b981]/10 border border-[#10b981]/30 text-[#10b981] text-xs font-medium rounded-xl flex items-center space-x-2">
          <CheckCheck className="w-4 h-4" />
          <span>{blastSuccessMessage}</span>
        </div>
      )}

      {/* Search & Reason Filters */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className="relative w-full sm:w-72">
          <Search className="w-4 h-4 text-[#778ca2] absolute left-3 top-2.5" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Cari MSISDN atau paket..."
            className="w-full text-xs pl-9 pr-3 py-2 bg-white border border-[#e2e8f0] rounded-xl focus:outline-none focus:border-[#ed0226]"
          />
        </div>

        <div className="flex items-center space-x-2 w-full sm:w-auto overflow-x-auto">
          {(
            [
              { key: 'ALL', label: 'Semua Ghost' },
              { key: 'NO_APP', label: 'Belum Pasang APK' },
              { key: 'SMS_FAILED', label: 'SMS OTP Gagal' },
              { key: 'DORMANT', label: 'Dormant (>7 Hari)' },
            ] as const
          ).map((tab) => (
            <button
              key={tab.key}
              onClick={() => setFilterReason(tab.key)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition whitespace-nowrap ${
                filterReason === tab.key
                  ? 'bg-[#0b132b] text-white shadow-sm'
                  : 'bg-white text-[#778ca2] border border-[#e2e8f0] hover:bg-[#f1f5f9]'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Ghost Subscribers Table */}
      <div className="bg-white rounded-2xl border border-[#e2e8f0] shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#f8fafc] text-[#5e3f3c] font-bold border-b border-[#e2e8f0] uppercase text-[10px]">
              <tr>
                <th className="px-4 py-3">MSISDN</th>
                <th className="px-4 py-3">Paket Telkomsel</th>
                <th className="px-4 py-3">Status Kendala (Ghost)</th>
                <th className="px-4 py-3">Masa Berlaku</th>
                <th className="px-4 py-3 text-right">Aksi Intervensi</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#f1f5f9]">
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-[#778ca2]">
                    Tidak ada pelanggan Ghost pada filter ini. Seluruh pelanggan terpantau aktif terproteksi.
                  </td>
                </tr>
              ) : (
                filtered.map((sub) => {
                  const isNudged = nudgedMsisdns[sub.msisdn];
                  const isUninstalled = !sub.mobile_id || sub.mobile_id.trim() === '';
                  const isSmsFailed = sub.activation_status === 'SMS_FAILED';

                  return (
                    <tr key={sub.msisdn} className="hover:bg-[#f8fafc]/80 transition">
                      <td className="px-4 py-3 font-semibold text-[#0b132b]">{sub.msisdn}</td>
                      <td className="px-4 py-3 text-[#5e3f3c]">{sub.plan_name || 'Telkomsel Secure Basic'}</td>
                      <td className="px-4 py-3">
                        {isUninstalled ? (
                          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-[#fff0ef] text-[#ed0226] border border-[#e9bcb8]">
                            Belum Pasang APK
                          </span>
                        ) : isSmsFailed ? (
                          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-[#fff8e1] text-[#f59e0b] border border-[#ffe082]">
                            SMS Kode Gagal
                          </span>
                        ) : (
                          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-[#f1f5f9] text-[#778ca2] border border-[#cbd5e1]">
                            Desync {sub.desync_days} Hari
                          </span>
                        )}
                      </td>
                      <td className="px-4 py-3 text-[#778ca2]">
                        {sub.active_period_end ? sub.active_period_end.slice(0, 10) : '30 Hari Aktif'}
                      </td>
                      <td className="px-4 py-3 text-right">
                        {isNudged ? (
                          <span className="inline-flex items-center space-x-1 text-[11px] font-bold text-[#10b981]">
                            <CheckCircle2 className="w-3.5 h-3.5" />
                            <span>SMS Terkirim</span>
                          </span>
                        ) : (
                          <button
                            onClick={() => handleNudge(sub.msisdn)}
                            className="px-3 py-1 bg-white hover:bg-[#fff0ef] text-[#ed0226] border border-[#e9bcb8] rounded-lg text-xs font-bold transition shadow-sm"
                          >
                            Kirim SMS Nudge
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
