'use client';

import React, { useState } from 'react';
import { Smartphone, Zap, AlertTriangle, CheckCircle2, Clock } from 'lucide-react';
import { simulateNdpPurchase, simulateNdpExpire } from '@/lib/api';
import { Subscriber } from '@/types';

interface NdpSimulatorProps {
  onSubscriberUpdated: (sub: Subscriber) => void;
}

export const NdpSimulator: React.FC<NdpSimulatorProps> = ({ onSubscriberUpdated }) => {
  const [msisdn, setMsisdn] = useState<string>('081299887766');
  const [loadingAction, setLoadingAction] = useState<string | null>(null);
  const [lastResult, setLastResult] = useState<{
    type: 'purchase' | 'expire';
    subscriber: Subscriber;
    message: string;
  } | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handlePurchase = async (durationDays: number, packageName: string) => {
    if (!msisdn.trim()) {
      setErrorMsg('Masukkan nomor MSISDN terlebih dahulu');
      return;
    }
    setErrorMsg(null);
    setLoadingAction(durationDays === 30 ? 'p30' : 'p365');
    try {
      const res = await simulateNdpPurchase({
        msisdn: msisdn.trim(),
        package_name: packageName,
        duration_days: durationDays,
        channel: 'MyTelkomsel App / NDP',
      });
      setLastResult({
        type: 'purchase',
        subscriber: res.subscriber,
        message: `Berhasil aktivasi ${packageName} (+${durationDays} hari)!`,
      });
      onSubscriberUpdated(res.subscriber);
    } catch (err: unknown) {
      setErrorMsg(err instanceof Error ? err.message : 'Gagal simulasi pembelian NDP');
    } finally {
      setLoadingAction(null);
    }
  };

  const handleExpire = async () => {
    if (!msisdn.trim()) {
      setErrorMsg('Masukkan nomor MSISDN terlebih dahulu');
      return;
    }
    setErrorMsg(null);
    setLoadingAction('expire');
    try {
      const res = await simulateNdpExpire(msisdn.trim());
      setLastResult({
        type: 'expire',
        subscriber: res.subscriber,
        message: 'Masa aktif berhasil dipaksa kedaluwarsa (Expired)!',
      });
      onSubscriberUpdated(res.subscriber);
    } catch (err: unknown) {
      setErrorMsg(err instanceof Error ? err.message : 'Gagal simulasi expired');
    } finally {
      setLoadingAction(null);
    }
  };

  return (
    <div className="vigilance-card p-6 rounded-xl border border-[#e9bcb8]/70 relative overflow-hidden">
      <div className="flex items-center space-x-3 mb-4">
        <div className="p-2 rounded-xl bg-[#fff0ef] text-[#ed0226] border border-[#e9bcb8]">
          <Zap className="w-5 h-5 text-[#ed0226]" />
        </div>
        <div>
          <h2 className="text-base font-bold text-[#0b132b] tracking-tight">
            NDP & BSS Package Purchase Simulator (POC Controller)
          </h2>
          <p className="text-xs text-[#778ca2]">
            Simulasikan instruksi pembelian paket dari MyTelkomsel & NDP Gateway langsung ke TelkomSecure
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-4 items-center">
        {/* Input MSISDN */}
        <div className="md:col-span-4">
          <label className="block text-[11px] font-bold text-[#5e3f3c] uppercase tracking-wider mb-1.5">
            Target MSISDN (HP Uji Coba)
          </label>
          <div className="relative">
            <Smartphone className="absolute left-3 top-2.5 w-4 h-4 text-[#778ca2]" />
            <input
              type="text"
              value={msisdn}
              onChange={(e) => setMsisdn(e.target.value)}
              placeholder="081299887766"
              className="w-full pl-9 pr-3 py-2 rounded-lg bg-[#f4f6f9] border border-[#e2e8f0] text-sm text-[#0b132b] font-mono placeholder-[#778ca2] focus:outline-none focus:border-[#ed0226] focus:bg-white focus:ring-1 focus:ring-[#ed0226] transition-all"
            />
          </div>
          <p className="text-[11px] text-[#778ca2] mt-1">Default: Pixel 6 (081299887766)</p>
        </div>

        {/* Buttons */}
        <div className="md:col-span-8 flex flex-wrap gap-2.5 pt-2 md:pt-4">
          <button
            onClick={() => handlePurchase(30, 'Telkomsel Secure Guard 30 Hari')}
            disabled={loadingAction !== null}
            className="flex-1 min-w-[170px] flex items-center justify-center space-x-2 px-4 py-2.5 rounded-lg bg-[#ed0226] hover:bg-[#be001c] text-white text-xs font-bold shadow-sm shadow-[#ed0226]/30 active:scale-95 disabled:opacity-50 transition-all cursor-pointer"
          >
            <CheckCircle2 className="w-4 h-4 text-white" />
            <span>{loadingAction === 'p30' ? 'Memproses...' : '+30 Hari (Rp 15.000)'}</span>
          </button>

          <button
            onClick={() => handlePurchase(365, 'Telkomsel Enterprise 1 Tahun')}
            disabled={loadingAction !== null}
            className="flex-1 min-w-[170px] flex items-center justify-center space-x-2 px-4 py-2.5 rounded-lg bg-[#545d7c] hover:bg-[#3a405a] text-white text-xs font-bold active:scale-95 disabled:opacity-50 transition-all cursor-pointer shadow-xs"
          >
            <Clock className="w-4 h-4 text-[#dbe1ff]" />
            <span>{loadingAction === 'p365' ? 'Memproses...' : '+1 Tahun (Enterprise)'}</span>
          </button>

          <button
            onClick={handleExpire}
            disabled={loadingAction !== null}
            className="flex-1 min-w-[150px] flex items-center justify-center space-x-2 px-4 py-2.5 rounded-lg bg-[#fff0ef] hover:bg-[#ffdad7] border border-[#e9bcb8] text-[#be001c] text-xs font-bold active:scale-95 disabled:opacity-50 transition-all cursor-pointer"
          >
            <AlertTriangle className="w-4 h-4 text-[#ba1a1a]" />
            <span>{loadingAction === 'expire' ? 'Memproses...' : 'Paksa Expired'}</span>
          </button>
        </div>
      </div>

      {errorMsg && (
        <div className="mt-3 p-3 rounded-lg bg-[#ffdad6] border border-[#ba1a1a]/30 text-xs text-[#93000a] font-medium">
          {errorMsg}
        </div>
      )}

      {lastResult && (
        <div className="mt-4 p-4 rounded-xl bg-[#fff8f7] border border-[#e9bcb8] text-xs shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="font-bold text-[#2a1615] flex items-center space-x-1.5">
              <span
                className={`w-2.5 h-2.5 rounded-full ${
                  lastResult.subscriber.is_active ? 'bg-[#10b981]' : 'bg-[#ed0226]'
                }`}
              ></span>
              <span>{lastResult.message}</span>
            </span>
            <span
              className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                lastResult.subscriber.is_active
                  ? 'bg-[#e8f5e9] text-[#10b981] border-[#a7f3d0]'
                  : 'bg-[#ffdad6] text-[#be001c] border-[#e9bcb8]'
              }`}
            >
              Status: {lastResult.subscriber.is_active ? 'TERLINDUNGI' : 'EXPIRED'}
            </span>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-[#5e3f3c]">
            <div>
              <span className="text-[#778ca2] block text-[10px] font-medium">Paket:</span>
              <span className="font-semibold text-[#0b132b]">{lastResult.subscriber.plan_name}</span>
            </div>
            <div>
              <span className="text-[#778ca2] block text-[10px] font-medium">Masa Berakhir:</span>
              <span className="font-mono text-[#006490] font-bold">
                {new Date(lastResult.subscriber.active_period_end).toLocaleDateString('id-ID')}
              </span>
            </div>
            <div>
              <span className="text-[#778ca2] block text-[10px] font-medium">Lisensi Kaspersky:</span>
              <span className="font-mono text-[11px] font-semibold text-[#0b132b]">
                {lastResult.subscriber.kaspersky_license_key || '(Tidak Aktif)'}
              </span>
            </div>
            <div>
              <span className="text-[#778ca2] block text-[10px] font-medium">Instruksi Demo:</span>
              <span className="text-[#10b981] font-bold">Buka / Refresh aplikasi di HP</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
