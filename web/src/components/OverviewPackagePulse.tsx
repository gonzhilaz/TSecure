'use client';

import React, { useMemo } from 'react';
import { Subscriber } from '@/types';
import { Package, Ghost, ArrowRight, ShieldCheck, DollarSign, Send, AlertTriangle } from 'lucide-react';
import { getStoredPackages } from '@/lib/packageService';

interface OverviewPackagePulseProps {
  subscribers: Subscriber[];
  onNavigateToPackages: () => void;
  onSendNudgeBlast: () => void;
}

export const OverviewPackagePulse: React.FC<OverviewPackagePulseProps> = ({
  subscribers,
  onNavigateToPackages,
  onSendNudgeBlast,
}) => {
  const packages = useMemo(() => getStoredPackages(), []);
  const activePackageCount = packages.filter((p) => p.is_active).length;

  // Calculate Ghost Subscribers
  const ghostMetrics = useMemo(() => {
    let unactivatedCount = 0;
    let dormantCount = 0;
    const now = Date.now();

    subscribers.forEach((s) => {
      const isPending = s.activation_status === 'SMS_FAILED' || s.activation_status === 'PENDING_CODE';
      if (isPending) {
        unactivatedCount++;
        return;
      }
      if (s.last_checked_at) {
        const lastCheck = new Date(s.last_checked_at).getTime();
        const diffDays = (now - lastCheck) / (1000 * 3600 * 24);
        if (diffDays > 7) dormantCount++;
      }
    });

    const totalGhosts = unactivatedCount + dormantCount;
    return {
      totalGhosts,
      unactivatedCount,
      dormantCount,
      pctOfTotal: subscribers.length > 0 ? Math.round((totalGhosts / subscribers.length) * 100) : 0,
    };
  }, [subscribers]);

  // Estimate MRR (Monthly Recurring Revenue)
  const totalRevenue = useMemo(() => {
    return subscribers.reduce((acc, sub) => {
      const matchedPkg = packages.find((p) => p.name.toLowerCase() === sub.plan_name.toLowerCase());
      return acc + (matchedPkg ? matchedPkg.price : 25000);
    }, 0);
  }, [subscribers, packages]);

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
      {/* Left: Package & Monetization Snapshot */}
      <div className="lg:col-span-6 bg-white rounded-2xl p-5 border border-[#e2e8f0] shadow-sm flex flex-col justify-between">
        <div>
          <div className="flex items-center justify-between pb-3 border-b border-[#f1f5f9]">
            <div className="flex items-center space-x-2">
              <div className="w-8 h-8 rounded-lg bg-[#ffe9e7] flex items-center justify-center text-[#ed0226]">
                <Package className="w-4 h-4" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[#0b132b]">
                  Adopsi Paket Telkomsel Secure
                </h3>
                <span className="text-[11px] text-[#718096]">
                  {activePackageCount} paket aktif di katalog MyTelkomsel
                </span>
              </div>
            </div>
            <button
              onClick={onNavigateToPackages}
              className="text-xs font-semibold text-[#ed0226] hover:underline flex items-center gap-1"
            >
              Kelola <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="grid grid-cols-2 gap-3 mt-4">
            <div className="bg-[#f8fafc] rounded-xl p-3 border border-[#edf2f7]">
              <span className="text-[11px] font-semibold text-[#64748b] block">Total Pendapatan (MRR)</span>
              <div className="text-lg font-bold text-[#0b132b] mt-1">
                Rp {totalRevenue.toLocaleString('id-ID')}
              </div>
              <span className="text-[10px] text-[#10b981] font-semibold mt-0.5 block">
                +14.2% bulan ini
              </span>
            </div>

            <div className="bg-[#f8fafc] rounded-xl p-3 border border-[#edf2f7]">
              <span className="text-[11px] font-semibold text-[#64748b] block">Rasio Pelanggan Aktif</span>
              <div className="text-lg font-bold text-[#10b981] mt-1">
                {subscribers.length > 0
                  ? Math.round(((subscribers.length - ghostMetrics.totalGhosts) / subscribers.length) * 100)
                  : 100}%
              </div>
              <span className="text-[10px] text-[#64748b] mt-0.5 block">
                {subscribers.length - ghostMetrics.totalGhosts} dari {subscribers.length} device
              </span>
            </div>
          </div>
        </div>

        <div className="mt-4 pt-3 border-t border-[#f1f5f9] flex items-center justify-between text-xs">
          <span className="text-[#64748b] text-[11px]">Integrasi Billing BSS Telkomsel</span>
          <span className="px-2 py-0.5 rounded-full bg-[#e8f5e9] text-[#10b981] font-bold text-[10px] border border-[#a7f3d0]">
            SINKRON OTOMATIS
          </span>
        </div>
      </div>

      {/* Right: Ghost Subscribers Alert & Quick Nudge Action */}
      <div className="lg:col-span-6 bg-white rounded-2xl p-5 border border-[#fed7aa] bg-[#fffbf7] shadow-sm flex flex-col justify-between">
        <div>
          <div className="flex items-center justify-between pb-3 border-b border-[#fed7aa]/60">
            <div className="flex items-center space-x-2">
              <div className="w-8 h-8 rounded-lg bg-[#ffedd5] flex items-center justify-center text-[#ea580c]">
                <Ghost className="w-4 h-4" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-[#9a3412]">
                  Deteksi Ghost Subscriber
                </h3>
                <span className="text-[11px] text-[#c2410c]">
                  Beli paket tapi APK belum terpasang / dorman
                </span>
              </div>
            </div>
            <span className="px-2 py-0.5 rounded-full bg-[#ea580c] text-white font-bold text-[10px]">
              {ghostMetrics.totalGhosts} Pelanggan
            </span>
          </div>

          <div className="grid grid-cols-2 gap-3 mt-4">
            <div className="bg-white/80 rounded-xl p-3 border border-[#fed7aa]/80">
              <span className="text-[11px] font-semibold text-[#9a3412] block">Belum Aktivasi APK</span>
              <div className="text-lg font-bold text-[#ea580c] mt-1">
                {ghostMetrics.unactivatedCount}
              </div>
              <span className="text-[10px] text-[#c2410c] mt-0.5 block">Gagal SMS / Pending OTP</span>
            </div>

            <div className="bg-white/80 rounded-xl p-3 border border-[#fed7aa]/80">
              <span className="text-[11px] font-semibold text-[#9a3412] block">Dorman &gt; 7 Hari</span>
              <div className="text-lg font-bold text-[#d97706] mt-1">
                {ghostMetrics.dormantCount}
              </div>
              <span className="text-[10px] text-[#b45309] mt-0.5 block">Aplikasi tidak aktif</span>
            </div>
          </div>
        </div>

        <div className="mt-4 pt-3 border-t border-[#fed7aa]/60 flex items-center justify-between">
          <p className="text-[11px] text-[#9a3412]">
            Kirimkan SMS Nudge agar pelanggan segera mengunduh APK & mengaktifkan proteksi.
          </p>
          <button
            onClick={onSendNudgeBlast}
            className="px-3.5 py-1.5 rounded-xl bg-[#ea580c] hover:bg-[#c2410c] text-white text-xs font-bold transition-all shadow-sm flex items-center gap-1.5 shrink-0"
          >
            <Send className="w-3.5 h-3.5" />
            Blast SMS Nudge
          </button>
        </div>
      </div>
    </div>
  );
};
