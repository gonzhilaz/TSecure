'use client';

import React from 'react';
import {
  Smartphone,
  ShieldAlert,
  ShieldCheck,
  AlertTriangle,
  Radio,
  FileWarning,
} from 'lucide-react';
import { Subscriber } from '@/types';

interface DeviceIntegrityDeskProps {
  subscribers: Subscriber[];
  loading?: boolean;
}

export const DeviceIntegrityDesk: React.FC<DeviceIntegrityDeskProps> = ({ subscribers, loading }) => {
  const rootedDevices = subscribers.filter(
    (s) => s.root_status === 'ROOT_DETECTED' || s.hook_status === 'HOOK_DETECTED'
  );
  const simSwapAlerts = subscribers.filter(
    (s) => s.bound_iccid && s.current_iccid && s.bound_iccid !== s.current_iccid
  );

  return (
    <div className="space-y-6">
      {/* Overview Metric Row */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="vigilance-card p-4 rounded-xl border-l-4 border-l-[#ed0226]">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-[#778ca2] uppercase">Device Root / Tampered</span>
            <div className="p-2 bg-[#ffdad6] text-[#93000a] rounded-lg">
              <ShieldAlert className="w-4 h-4" />
            </div>
          </div>
          {loading ? (
            <div className="h-7 w-16 rounded skeleton-shimmer-light mt-1" />
          ) : (
            <div className="text-2xl font-bold text-[#0b132b] mt-1">{rootedDevices.length}</div>
          )}
          <p className="text-xs text-[#778ca2] mt-0.5">Magisk, KernelSU, atau Frida hook terdeteksi</p>
        </div>

        <div className="vigilance-card p-4 rounded-xl border-l-4 border-l-[#f59e0b]">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-[#778ca2] uppercase">SIM Swap Alerts</span>
            <div className="p-2 bg-[#fff8e1] text-[#b45309] rounded-lg">
              <Radio className="w-4 h-4" />
            </div>
          </div>
          {loading ? (
            <div className="h-7 w-16 rounded skeleton-shimmer-light mt-1" />
          ) : (
            <div className="text-2xl font-bold text-[#0b132b] mt-1">{simSwapAlerts.length}</div>
          )}
          <p className="text-xs text-[#778ca2] mt-0.5">Pergantian kartu SIM di luar ICCID resmi</p>
        </div>

        <div className="vigilance-card p-4 rounded-xl border-l-4 border-l-[#10b981]">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-[#778ca2] uppercase">Armada Higienis</span>
            <div className="p-2 bg-[#e8f5e9] text-[#065f46] rounded-lg">
              <ShieldCheck className="w-4 h-4" />
            </div>
          </div>
          {loading ? (
            <div className="h-7 w-16 rounded skeleton-shimmer-light mt-1" />
          ) : (
            <div className="text-2xl font-bold text-[#0b132b] mt-1">
              {subscribers.length - rootedDevices.length}
            </div>
          )}
          <p className="text-xs text-[#778ca2] mt-0.5">Integritas OS Android/iOS aman & terverifikasi</p>
        </div>
      </div>

      {/* Main Device Telemetry Table */}
      <div className="bg-white rounded-2xl border border-[#e9bcb8] shadow-sm overflow-hidden">
        <div className="p-4 border-b border-[#ffe9e7] bg-[#fff8f7] flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Smartphone className="w-4 h-4 text-[#ed0226]" />
            <h3 className="text-sm font-bold text-[#0b132b]">Audit Perangkat & SIM Watch Telemetry</h3>
          </div>
          <span className="text-xs text-[#778ca2]">
            Data sinkronisasi RASP & Kaspersky Mobile Security
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead className="bg-[#ffe9e7]/50 text-[#5e3f3c] uppercase text-[10px] tracking-wider border-b border-[#ffe9e7]">
              <tr>
                <th className="py-3 px-4">Pelanggan & Device</th>
                <th className="py-3 px-4">OS Version</th>
                <th className="py-3 px-4">Integritas Root / Hook</th>
                <th className="py-3 px-4">SIM Watch (ICCID)</th>
                <th className="py-3 px-4">Karantina File</th>
                <th className="py-3 px-4">Device Migration</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#ffe9e7]">
              {loading ? (
                [1, 2, 3, 4].map((i) => (
                  <tr key={i}>
                    <td colSpan={6} className="py-3.5 px-4">
                      <div className="h-8 rounded-lg skeleton-shimmer-light"></div>
                    </td>
                  </tr>
                ))
              ) : subscribers.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-[#778ca2]">
                    Tidak ada data telemetri integritas perangkat.
                  </td>
                </tr>
              ) : (
                subscribers.map((sub) => {
                const isRooted = sub.root_status === 'ROOT_DETECTED';
                const isHooked = sub.hook_status === 'HOOK_DETECTED';
                const isSimSwapped = sub.bound_iccid && sub.current_iccid && sub.bound_iccid !== sub.current_iccid;

                return (
                  <tr key={sub.id} className="hover:bg-[#fff8f7]/70 transition-colors">
                    <td className="py-3 px-4">
                      <div className="font-bold text-[#0b132b]">{sub.msisdn}</div>
                      <div className="text-[11px] text-[#778ca2]">{sub.device_model}</div>
                      <div className="font-mono text-[10px] text-[#be001c]">{sub.id}</div>
                    </td>

                    <td className="py-3 px-4 text-[#5e3f3c] font-medium">
                      {sub.os_version}
                    </td>

                    <td className="py-3 px-4">
                      <div className="space-y-1">
                        <span
                          className={`inline-flex items-center gap-1 text-[10px] font-bold px-2 py-0.5 rounded-full ${
                            isRooted
                              ? 'bg-[#ffdad6] text-[#93000a] border border-[#e9bcb8]'
                              : 'bg-[#e8f5e9] text-[#065f46]'
                          }`}
                        >
                          {isRooted ? <AlertTriangle className="w-3 h-3" /> : <ShieldCheck className="w-3 h-3" />}
                          Root: {sub.root_status}
                        </span>
                        {isHooked && (
                          <div className="text-[10px] font-bold text-[#93000a] flex items-center gap-1">
                            <ShieldAlert className="w-3 h-3" /> Frida Hook Aktif
                          </div>
                        )}
                      </div>
                    </td>

                    <td className="py-3 px-4">
                      {isSimSwapped ? (
                        <div className="space-y-0.5">
                          <span className="inline-flex items-center gap-1 text-[10px] font-bold px-2 py-0.5 rounded-md bg-[#fff8e1] text-[#b45309] border border-[#fde68a]">
                            <AlertTriangle className="w-3 h-3" /> SIM SWAP DETECTED
                          </span>
                          <div className="font-mono text-[10px] text-[#778ca2]">
                            Resmi: ...{sub.bound_iccid.slice(-4)} | Baru: ...{sub.current_iccid.slice(-4)}
                          </div>
                        </div>
                      ) : (
                        <div className="text-[#065f46] font-semibold text-[11px] flex items-center gap-1">
                          <ShieldCheck className="w-3.5 h-3.5" /> Bound ICCID Match
                        </div>
                      )}
                    </td>

                    <td className="py-3 px-4">
                      {sub.quarantine_count > 0 ? (
                        <span className="inline-flex items-center gap-1 text-[10px] font-bold px-2 py-0.5 rounded-full bg-[#ffdad6] text-[#93000a]">
                          <FileWarning className="w-3 h-3" /> {sub.quarantine_count} file diisolasi
                        </span>
                      ) : (
                        <span className="text-[#778ca2]">0 file</span>
                      )}
                    </td>

                    <td className="py-3 px-4 text-[#5e3f3c]">
                      <span className="font-bold">{sub.device_migration_count}</span> kali ganti HP
                    </td>
                  </tr>
                );
              }))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
