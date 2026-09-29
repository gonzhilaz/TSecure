'use client';

import React from 'react';
import {
  KeyRound,
  Smartphone,
  Archive,
  Send,
  RefreshCw,
} from 'lucide-react';
import { Subscriber } from '@/types';

interface CustomerDetailOverviewProps {
  subscriber: Subscriber;
  actionLoading: string | null;
  onResendSMS: () => void;
  onResyncKaspersky: () => void;
  onOpenMigration: (sub: Subscriber) => void;
  formatDate: (iso?: string) => string;
}

export const CustomerDetailOverview: React.FC<CustomerDetailOverviewProps> = ({
  subscriber,
  actionLoading,
  onResendSMS,
  onResyncKaspersky,
  onOpenMigration,
  formatDate,
}) => {
  const isDesynced = subscriber.desync_days > 0;
  const isSmsFailed = subscriber.activation_status === 'SMS_FAILED';
  const isRooted = subscriber.root_status === 'ROOT_DETECTED' || subscriber.hook_status === 'HOOK_DETECTED';

  return (
    <div className="space-y-6">
      {/* Data Grid: Lisensi & Perangkat */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Box 1: Lisensi & Masa Aktif */}
        <div className="bg-[#fff8f7] p-4 rounded-xl border border-[#ffe9e7] space-y-3">
          <h4 className="text-xs font-bold text-[#006490] flex items-center gap-2">
            <KeyRound className="w-4 h-4" />
            <span>Lisensi & Sinkronisasi Kaspersky</span>
          </h4>
          <div className="space-y-2 text-xs">
            <div className="flex justify-between">
              <span className="text-[#778ca2]">Kaspersky B2B Key:</span>
              <span className="font-mono font-bold text-[#0b132b]">{subscriber.kaspersky_license_key}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-[#778ca2]">Kode Aktivasi SMS:</span>
              <span className="font-mono font-bold text-[#be001c]">{subscriber.activation_code || '-'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-[#778ca2]">Status Aktivasi:</span>
              <span className="font-semibold text-[#0b132b]">{subscriber.activation_status}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-[#778ca2]">Masa Aktif Paket (NDP):</span>
              <span className="font-semibold text-[#0b132b]">{formatDate(subscriber.active_period_end)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-[#778ca2]">Masa Aktif Token KSP:</span>
              <span className={`font-semibold ${isDesynced ? 'text-[#be001c]' : 'text-[#065f46]'}`}>
                {formatDate(subscriber.kaspersky_expiry_date)}
              </span>
            </div>
          </div>

          <div className="pt-2 border-t border-[#ffe9e7] flex gap-2">
            {isSmsFailed && (
              <button
                onClick={onResendSMS}
                disabled={actionLoading === 'sms'}
                className="flex-1 flex items-center justify-center gap-1.5 py-2 px-3 bg-[#ed0226] hover:bg-[#be001c] text-white rounded-lg text-xs font-bold transition-colors"
              >
                <Send className="w-3.5 h-3.5" />
                <span>Kirim Ulang SMS</span>
              </button>
            )}
            {isDesynced && (
              <button
                onClick={onResyncKaspersky}
                disabled={actionLoading === 'sync'}
                className="flex-1 flex items-center justify-center gap-1.5 py-2 px-3 bg-[#006490] hover:bg-[#004f72] text-white rounded-lg text-xs font-bold transition-colors"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Sinkronkan Masa Aktif</span>
              </button>
            )}
          </div>
        </div>

        {/* Box 2: Audit Perangkat & SIM Watch */}
        <div className="bg-[#fff8f7] p-4 rounded-xl border border-[#ffe9e7] space-y-3">
          <h4 className="text-xs font-bold text-[#5e3f3c] flex items-center gap-2">
            <Smartphone className="w-4 h-4" />
            <span>Audit Hardware & Integritas OS</span>
          </h4>
          <div className="space-y-2 text-xs">
            <div className="flex justify-between">
              <span className="text-[#778ca2]">Model / OS:</span>
              <span className="font-semibold text-[#0b132b]">{subscriber.device_model} ({subscriber.os_version})</span>
            </div>
            <div className="flex justify-between">
              <span className="text-[#778ca2]">Status Root / Tamper:</span>
              <span className={`font-bold ${isRooted ? 'text-[#be001c]' : 'text-[#065f46]'}`}>
                {subscriber.root_status} / {subscriber.hook_status}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-[#778ca2]">SIM Slot & ICCID:</span>
              <span className="font-mono text-[#0b132b]">{subscriber.sim_slot} (...{subscriber.current_iccid?.slice(-6)})</span>
            </div>
            <div className="flex justify-between">
              <span className="text-[#778ca2]">Riwayat Ganti Device:</span>
              <span className="font-semibold text-[#545d7c]">{subscriber.device_migration_count} kali dipindahkan</span>
            </div>
            <div className="flex justify-between">
              <span className="text-[#778ca2]">Karantina File Malware:</span>
              <span className="font-bold text-[#0b132b]">{subscriber.quarantine_count} file</span>
            </div>
          </div>

          <div className="pt-2 border-t border-[#ffe9e7]">
            <button
              onClick={() => onOpenMigration(subscriber)}
              className="w-full flex items-center justify-center gap-1.5 py-2 px-3 bg-white hover:bg-[#fff0ef] text-[#be001c] border border-[#e9bcb8] rounded-lg text-xs font-bold transition-colors"
            >
              <Smartphone className="w-3.5 h-3.5" />
              <span>Migrasi Device (Ganti Smartphone Pelanggan)</span>
            </button>
          </div>
        </div>
      </div>

      {/* Data Retention & 1-Month Archival Policy */}
      <div className="p-4 bg-[#e8f5e9]/50 border border-[#a7f3d0] rounded-xl flex items-start justify-between gap-3 text-xs">
        <div className="flex items-start gap-2.5">
          <Archive className="w-5 h-5 text-[#065f46] shrink-0 mt-0.5" />
          <div>
            <h5 className="font-bold text-[#065f46]">Kebijakan Retensi & Pengarsipan Otomatis (1 Bulan)</h5>
            <p className="text-[#545d7c] mt-0.5">
              Data telemetri dan log scan aktif disimpan di server selama 30 hari. Setelah masa aktif retensi habis, data otomatis dipindahkan ke Cold Storage / Compressed Archive untuk menghemat kapasitas database.
            </p>
            <div className="mt-1.5 text-[11px] font-mono text-[#065f46]">
              Status Penyimpanan: <span className="font-bold">Rolling Hot Storage</span> • Retensi: 30 Hari • Jadwal Arsip: {formatDate(subscriber.retention_expires_at)}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
