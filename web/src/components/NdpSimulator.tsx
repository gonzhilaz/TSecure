import React, { useState, useEffect, useMemo } from 'react';
import { Smartphone, Zap, AlertTriangle, CheckCircle2, ShieldOff, Ghost, Package } from 'lucide-react';
import { simulateNdpPurchase, simulateNdpExpire, simulateNdpUnactivated } from '@/lib/api';
import { Subscriber, SecurityPackage } from '@/types';
import { getStoredPackages } from '@/lib/packageService';

interface NdpSimulatorProps {
  onSubscriberUpdated: (sub: Subscriber) => void;
}

export const NdpSimulator: React.FC<NdpSimulatorProps> = ({ onSubscriberUpdated }) => {
  const [msisdn, setMsisdn] = useState<string>('081299887766');
  const [loadingAction, setLoadingAction] = useState<string | null>(null);
  const [lastResult, setLastResult] = useState<{
    type: 'purchase' | 'expire' | 'unactivated';
    subscriber: Subscriber;
    message: string;
  } | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const [packages, setPackages] = useState<SecurityPackage[]>([]);
  const [selectedPkgId, setSelectedPkgId] = useState<string>('');

  useEffect(() => {
    const list = getStoredPackages().filter((p) => p.is_active);
    setPackages(list);
    if (list.length > 0) setSelectedPkgId(list[0].id);
  }, []);

  const handleUnactivated = async () => {
    if (!msisdn.trim()) {
      setErrorMsg('Masukkan nomor MSISDN terlebih dahulu');
      return;
    }
    setErrorMsg(null);
    setLoadingAction('unactivated');
    try {
      const res = await simulateNdpUnactivated(msisdn.trim());
      setLastResult({
        type: 'unactivated',
        subscriber: res.subscriber,
        message: 'Status di-set: BELUM AKTIF (Masa Aktif & Lisensi Kosong, Wajib Aktivasi)!',
      });
      onSubscriberUpdated(res.subscriber);
    } catch (err: unknown) {
      setErrorMsg(err instanceof Error ? err.message : 'Gagal simulasi unactivated');
    } finally {
      setLoadingAction(null);
    }
  };

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
        message: `Status di-set: MASA AKTIF ADA (+${durationDays} hari & Lisensi Real Aktif)!`,
      });
      onSubscriberUpdated(res.subscriber);
    } catch (err: unknown) {
      setErrorMsg(err instanceof Error ? err.message : 'Gagal simulasi pembelian NDP');
    } finally {
      setLoadingAction(null);
    }
  };

  const handlePurchaseSelected = () => {
    const target = packages.find((p) => p.id === selectedPkgId);
    if (!target) return;
    handlePurchase(target.duration_days, target.name);
  };

  const handleGhostSimulation = async () => {
    if (!msisdn.trim()) {
      setErrorMsg('Masukkan nomor MSISDN terlebih dahulu');
      return;
    }
    setErrorMsg(null);
    setLoadingAction('ghost');
    try {
      const res = await simulateNdpPurchase({
        msisdn: msisdn.trim(),
        package_name: 'Telkomsel Secure Basic 30 Hari',
        duration_days: 30,
        channel: 'MyTelkomsel UMB *363# (Ghost User)',
      });
      const ghostSub: Subscriber = {
        ...res.subscriber,
        mobile_id: '',
        device_model: 'Unknown (Belum Pasang APK)',
        activation_status: 'SMS_FAILED',
        is_active: false,
      };
      setLastResult({
        type: 'purchase',
        subscriber: ghostSub,
        message: 'Status di-set: GHOST SUBSCRIBER (Pulsa Terpotong, APK Belum Terpasang)!',
      });
      onSubscriberUpdated(ghostSub);
    } catch (err: unknown) {
      setErrorMsg(err instanceof Error ? err.message : 'Gagal simulasi Ghost Subscriber');
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
        message: 'Status di-set: MASA AKTIF HABIS (Expired di NDP, Lisensi Tetap Ada)!',
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
            Simulator Pembelian Paket & Aktivasi (POC Controller)
          </h2>
          <p className="text-xs text-[#778ca2]">
            Simulasikan instruksi pembelian paket dari MyTelkomsel & NDP Gateway langsung ke Telkomsel Secure
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

        {/* Buttons: 3 Pure Testing Scenarios */}
        <div className="md:col-span-8 flex flex-wrap gap-2.5 pt-2 md:pt-4">
          {/* Skenario 1: Belum Aktif */}
          <button
            onClick={handleUnactivated}
            disabled={loadingAction !== null}
            className="flex-1 min-w-[170px] flex items-center justify-center space-x-1.5 px-3 py-2.5 rounded-lg bg-[#f1f5f9] hover:bg-[#e2e8f0] border border-[#cbd5e1] text-[#334155] text-xs font-bold active:scale-95 disabled:opacity-50 transition-all cursor-pointer shadow-xs"
            title="Masa aktif kosong & lisensi kosong. Wajib aktivasi pada perangkat."
          >
            <ShieldOff className="w-4 h-4 text-[#64748b]" />
            <span>{loadingAction === 'unactivated' ? 'Memproses...' : '⚪ 1. Belum Aktif (Wajib Aktivasi)'}</span>
          </button>

          {/* Skenario 2: Masa Aktif Ada (+30 Hari & Lisensi Real) */}
          <button
            onClick={() => handlePurchase(30, 'Telkomsel Secure Guard 30 Hari')}
            disabled={loadingAction !== null}
            className="flex-1 min-w-[170px] flex items-center justify-center space-x-1.5 px-3 py-2.5 rounded-lg bg-[#ed0226] hover:bg-[#be001c] text-white text-xs font-bold shadow-sm shadow-[#ed0226]/30 active:scale-95 disabled:opacity-50 transition-all cursor-pointer"
            title="Masa aktif valid 30 hari & lisensi real dari backend."
          >
            <CheckCircle2 className="w-4 h-4 text-white" />
            <span>{loadingAction === 'p30' ? 'Memproses...' : '🟢 2. Masa Aktif Ada (+30 Hari)'}</span>
          </button>

          {/* Skenario 3: Masa Aktif Habis (Expired di NDP, Lisensi Ada) */}
          <button
            onClick={handleExpire}
            disabled={loadingAction !== null}
            className="flex-1 min-w-[170px] flex items-center justify-center space-x-1.5 px-3 py-2.5 rounded-lg bg-[#fff0ef] hover:bg-[#ffdad7] border border-[#e9bcb8] text-[#be001c] text-xs font-bold active:scale-95 disabled:opacity-50 transition-all cursor-pointer"
            title="Masa aktif habis di NDP Telkomsel, namun lisensi fisik masih ada."
          >
            <AlertTriangle className="w-4 h-4 text-[#ba1a1a]" />
            <span>{loadingAction === 'expire' ? 'Memproses...' : '🔴 3. Masa Aktif Habis (Lisensi Ada)'}</span>
          </button>
        </div>

        {/* Row 2: Dynamic Package Selection from Package Manager & Ghost Simulation */}
        <div className="md:col-span-12 pt-3 border-t border-[#f1f5f9] flex flex-col sm:flex-row items-center gap-3">
          <div className="flex-1 w-full flex items-center space-x-2">
            <Package className="w-4 h-4 text-[#ed0226] shrink-0" />
            <select
              value={selectedPkgId}
              onChange={(e) => setSelectedPkgId(e.target.value)}
              className="w-full text-xs px-3 py-2 bg-white border border-[#cbd5e1] rounded-lg text-[#0b132b] focus:outline-none focus:border-[#ed0226]"
            >
              {packages.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} — Rp {p.price.toLocaleString('id-ID')} ({p.duration_days} Hari)
                </option>
              ))}
            </select>
            <button
              onClick={handlePurchaseSelected}
              disabled={loadingAction !== null || !selectedPkgId}
              className="px-4 py-2 bg-[#0b132b] hover:bg-[#1e293b] text-white text-xs font-bold rounded-lg transition whitespace-nowrap"
            >
              Beli Paket Terpilih
            </button>
          </div>

          <button
            onClick={handleGhostSimulation}
            disabled={loadingAction !== null}
            className="w-full sm:w-auto px-4 py-2 bg-[#fff8f7] hover:bg-[#fff0ef] text-[#be001c] border border-[#e9bcb8] text-xs font-bold rounded-lg transition flex items-center justify-center space-x-1.5 whitespace-nowrap"
            title="Simulasikan user beli paket di MyTelkomsel tetapi tidak pernah menginstal APK"
          >
            <Ghost className="w-4 h-4 text-[#be001c]" />
            <span>Simulasi Ghost User (Tanpa APK)</span>
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
