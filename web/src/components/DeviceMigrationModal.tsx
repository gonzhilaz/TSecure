'use client';

import React, { useState } from 'react';
import { Smartphone, RefreshCw, X, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { Subscriber } from '@/types';
import { migrateDevice } from '@/lib/api';

interface DeviceMigrationModalProps {
  subscriber: Subscriber | null;
  onClose: () => void;
  onSuccess: (updated: Subscriber) => void;
}

export const DeviceMigrationModal: React.FC<DeviceMigrationModalProps> = ({
  subscriber,
  onClose,
  onSuccess,
}) => {
  const [deviceModel, setDeviceModel] = useState('');
  const [osVersion, setOsVersion] = useState('Android 14');
  const [reason, setReason] = useState('Pelanggan upgrade smartphone baru');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!subscriber) return null;

  const handleMigrate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!deviceModel.trim()) {
      setError('Model device baru wajib diisi');
      return;
    }

    setLoading(true);
    setError(null);

    const generatedMobileId = `MOB-${Math.floor(100000 + Math.random() * 900000)}`;

    try {
      const res = await migrateDevice({
        msisdn: subscriber.msisdn,
        new_mobile_id: generatedMobileId,
        new_device_model: deviceModel.trim(),
        new_os_version: osVersion.trim(),
        reason: reason.trim(),
      });

      if (res.subscriber) {
        onSuccess(res.subscriber);
      }
      onClose();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Gagal memproses migrasi device';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
      <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-[#e9bcb8]">
        <div className="flex items-center justify-between pb-4 border-b border-[#ffe9e7]">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-[#ffe9e7] text-[#ed0226] rounded-xl">
              <Smartphone className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-[#0b132b]">
                Migrasi Device Pelanggan
              </h3>
              <p className="text-xs text-[#778ca2]">
                Pindahkan lisensi aktif ke smartphone baru tanpa kuota tambahan
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-[#778ca2] hover:bg-[#fff0ef] hover:text-[#0b132b]"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="mt-4 p-3 bg-[#ffdad6] border border-[#e9bcb8] rounded-xl flex items-center gap-2 text-xs text-[#93000a]">
            <AlertTriangle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleMigrate} className="mt-4 space-y-4">
          <div className="bg-[#fff8f7] p-3.5 rounded-xl border border-[#ffe9e7] space-y-2">
            <div className="flex justify-between text-xs">
              <span className="text-[#778ca2]">MSISDN Pelanggan:</span>
              <span className="font-bold text-[#0b132b]">{subscriber.msisdn}</span>
            </div>
            <div className="flex justify-between text-xs">
              <span className="text-[#778ca2]">Kode Unik:</span>
              <span className="font-mono font-medium text-[#be001c]">{subscriber.id}</span>
            </div>
            <div className="flex justify-between text-xs">
              <span className="text-[#778ca2]">Device Lama Terikat:</span>
              <span className="font-medium text-[#0b132b]">{subscriber.device_model} ({subscriber.mobile_id})</span>
            </div>
            <div className="flex justify-between text-xs">
              <span className="text-[#778ca2]">Riwayat Migrasi:</span>
              <span className="font-medium text-[#545d7c]">{subscriber.device_migration_count} kali dipindahkan</span>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-[#2a1615] mb-1">
              Model Smartphone Baru *
            </label>
            <input
              type="text"
              required
              value={deviceModel}
              onChange={(e) => setDeviceModel(e.target.value)}
              placeholder="Contoh: Samsung Galaxy S24 Ultra, Xiaomi 14"
              className="w-full text-xs px-3.5 py-2.5 rounded-xl border border-[#e9bcb8] bg-white text-[#0b132b] focus:outline-none focus:ring-2 focus:ring-[#ed0226]/40"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-[#2a1615] mb-1">
                OS Version Baru
              </label>
              <input
                type="text"
                value={osVersion}
                onChange={(e) => setOsVersion(e.target.value)}
                placeholder="Android 14 / OneUI 6.1"
                className="w-full text-xs px-3.5 py-2.5 rounded-xl border border-[#e9bcb8] bg-white text-[#0b132b] focus:outline-none focus:ring-2 focus:ring-[#ed0226]/40"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-[#2a1615] mb-1">
                Alasan Migrasi
              </label>
              <select
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                className="w-full text-xs px-3.5 py-2.5 rounded-xl border border-[#e9bcb8] bg-white text-[#0b132b] focus:outline-none focus:ring-2 focus:ring-[#ed0226]/40"
              >
                <option value="Pelanggan upgrade smartphone baru">Upgrade Smartphone</option>
                <option value="Smartphone lama rusak / hilang">Device Lama Rusak/Hilang</option>
                <option value="Factory reset / Reinstall">Factory Reset / Format</option>
              </select>
            </div>
          </div>

          <div className="p-3 bg-[#e8f5e9] border border-[#a7f3d0] rounded-xl flex items-start gap-2 text-xs text-[#065f46]">
            <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
            <p>
              Binding device lama akan otomatis di-unpair. Token Kaspersky akan dire-bind ke hardware ID baru tanpa memotong kuota B2B lisensi baru.
            </p>
          </div>

          <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-[#ffe9e7]">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-semibold text-[#545d7c] hover:bg-[#fff0ef] transition-colors"
            >
              Batal
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex items-center gap-2 px-4 py-2 bg-[#ed0226] hover:bg-[#be001c] text-white text-xs font-bold rounded-xl transition-all shadow-md shadow-[#ed0226]/20 disabled:opacity-50"
            >
              {loading && <RefreshCw className="w-3.5 h-3.5 animate-spin" />}
              <span>Eksekusi Migrasi Device</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
