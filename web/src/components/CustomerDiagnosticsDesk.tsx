'use client';

import React, { useState } from 'react';
import {
  Search,
  RefreshCw,
  Eye,
  AlertTriangle,
  Smartphone,
  ShieldAlert,
} from 'lucide-react';
import { Subscriber } from '@/types';
import { CustomerDetailModal } from './CustomerDetailModal';
import { DeviceMigrationModal } from './DeviceMigrationModal';

interface CustomerDiagnosticsDeskProps {
  subscribers: Subscriber[];
  onSubscriberUpdated: (updated: Subscriber) => void;
  onRefresh: () => void;
  loading: boolean;
}

export const CustomerDiagnosticsDesk: React.FC<CustomerDiagnosticsDeskProps> = ({
  subscribers,
  onSubscriberUpdated,
  onRefresh,
  loading,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSub, setSelectedSub] = useState<Subscriber | null>(null);
  const [migratingSub, setMigratingSub] = useState<Subscriber | null>(null);

  const filteredSubscribers = subscribers.filter((sub) => {
    const q = searchQuery.toLowerCase().trim();
    if (!q) return true;
    return (
      sub.id.toLowerCase().includes(q) ||
      sub.msisdn.toLowerCase().includes(q) ||
      sub.mobile_id.toLowerCase().includes(q) ||
      sub.plan_name.toLowerCase().includes(q) ||
      sub.device_model.toLowerCase().includes(q) ||
      sub.activation_code.toLowerCase().includes(q)
    );
  });

  const formatDate = (iso?: string) => {
    if (!iso || iso.startsWith('0001')) return '-';
    try {
      return new Date(iso).toLocaleDateString('id-ID', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
      });
    } catch {
      return iso;
    }
  };

  const calculateEta = (endIso: string, isActive: boolean) => {
    if (!isActive) return { text: 'Kedaluwarsa', color: 'text-[#93000a] bg-[#ffdad6]' };
    try {
      const now = new Date();
      const end = new Date(endIso);
      const diffMs = end.getTime() - now.getTime();
      const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));
      if (diffDays <= 0) return { text: 'Kedaluwarsa', color: 'text-[#93000a] bg-[#ffdad6]' };
      if (diffDays <= 3) return { text: `${diffDays} Hari lagi`, color: 'text-[#b45309] bg-[#fff8e1]' };
      return { text: `${diffDays} Hari lagi`, color: 'text-[#065f46] bg-[#e8f5e9]' };
    } catch {
      return { text: 'Aktif', color: 'text-[#065f46] bg-[#e8f5e9]' };
    }
  };

  return (
    <div className="space-y-4">
      {/* Search Header Bar (Filter Kasus bypassed/removed as requested) */}
      <div className="bg-white p-4 rounded-2xl border border-[#e9bcb8]/80 shadow-sm flex flex-col md:flex-row gap-3 items-center justify-between">
        <div className="relative flex-1 w-full">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[#778ca2]" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Cari data pelanggan: ID Unik (SUB-...), Mobile-ID, MSISDN, Paket, atau Device..."
            className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-[#e9bcb8] bg-[#fff8f7] text-[#0b132b] text-xs focus:outline-none focus:ring-2 focus:ring-[#ed0226]/40 focus:bg-white"
          />
        </div>

        <button
          onClick={onRefresh}
          disabled={loading}
          className="flex items-center gap-2 px-4 py-2.5 bg-[#fff0ef] hover:bg-[#ffe9e7] text-[#be001c] text-xs font-semibold rounded-xl border border-[#e9bcb8] transition-colors shrink-0"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Data</span>
        </button>
      </div>

      {/* Main Streamlined Subscriber Table */}
      <div className="bg-white rounded-2xl border border-[#e9bcb8] shadow-sm overflow-hidden">
        <div className="p-4 border-b border-[#ffe9e7] bg-[#fff8f7] flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Smartphone className="w-4 h-4 text-[#ed0226]" />
            <h3 className="text-sm font-bold text-[#0b132b]">Daftar Pelanggan Telkomsel Secure</h3>
          </div>
          <span className="text-xs text-[#778ca2]">
            Total {filteredSubscribers.length} data • Klik baris pelanggan untuk memeriksa seluruh detail
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead className="bg-[#ffe9e7]/60 text-[#5e3f3c] uppercase text-[10px] tracking-wider border-b border-[#ffe9e7]">
              <tr>
                <th className="py-3 px-4">ID (Kode Unik)</th>
                <th className="py-3 px-4">Mobile-ID</th>
                <th className="py-3 px-4">MSISDN</th>
                <th className="py-3 px-4">Masa Aktif (ETA)</th>
                <th className="py-3 px-4">Start Date</th>
                <th className="py-3 px-4">End Date</th>
                <th className="py-3 px-4">Paket</th>
                <th className="py-3 px-4">Device</th>
                <th className="py-3 px-4 text-center">Aksi</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#ffe9e7]">
              {filteredSubscribers.length === 0 ? (
                <tr>
                  <td colSpan={9} className="py-10 text-center text-[#778ca2]">
                    Tidak ada pelanggan yang cocok dengan kata kunci pencarian.
                  </td>
                </tr>
              ) : (
                filteredSubscribers.map((sub) => {
                  const eta = calculateEta(sub.active_period_end, sub.is_active);
                  const isDesynced = sub.desync_days > 0;
                  const isSmsFailed = sub.activation_status === 'SMS_FAILED';
                  const isRooted = sub.root_status === 'ROOT_DETECTED' || sub.hook_status === 'HOOK_DETECTED';

                  return (
                    <tr
                      key={sub.id}
                      onClick={() => setSelectedSub(sub)}
                      className="hover:bg-[#fff0ef]/60 cursor-pointer transition-colors group"
                    >
                      {/* ID (Kode Unik) */}
                      <td className="py-3 px-4 font-mono font-bold text-[#be001c]">
                        <div className="flex items-center gap-1.5">
                          <span>{sub.id}</span>
                          {isDesynced && (
                            <span title="Desinkronisasi Masa Aktif" className="text-[#b45309]">
                              <AlertTriangle className="w-3.5 h-3.5" />
                            </span>
                          )}
                          {isRooted && (
                            <span title="Root/Tamper Terdeteksi" className="text-[#93000a]">
                              <ShieldAlert className="w-3.5 h-3.5" />
                            </span>
                          )}
                        </div>
                      </td>

                      {/* Mobile-ID */}
                      <td className="py-3 px-4 font-mono text-[#545d7c] max-w-[120px] truncate" title={sub.mobile_id}>
                        {sub.mobile_id}
                      </td>

                      {/* MSISDN */}
                      <td className="py-3 px-4 font-bold text-[#0b132b]">
                        <div className="flex items-center gap-1.5">
                          <span>{sub.msisdn}</span>
                          {isSmsFailed && (
                            <span className="text-[9px] font-bold px-1.5 py-0.2 rounded bg-[#ffdad6] text-[#93000a]">
                              SMS!
                            </span>
                          )}
                        </div>
                      </td>

                      {/* Masa Aktif (ETA) */}
                      <td className="py-3 px-4">
                        <span className={`inline-block px-2 py-0.5 rounded-full text-[11px] font-bold ${eta.color}`}>
                          {eta.text}
                        </span>
                      </td>

                      {/* Start Date */}
                      <td className="py-3 px-4 text-[#5e3f3c]">
                        {formatDate(sub.active_period_start)}
                      </td>

                      {/* End Date */}
                      <td className="py-3 px-4 text-[#5e3f3c] font-medium">
                        {formatDate(sub.active_period_end)}
                      </td>

                      {/* Paket */}
                      <td className="py-3 px-4 font-medium text-[#0b132b] max-w-[160px] truncate" title={sub.plan_name}>
                        {sub.plan_name}
                      </td>

                      {/* Device */}
                      <td className="py-3 px-4 text-[#5e3f3c] max-w-[160px] truncate" title={sub.device_model}>
                        {sub.device_model}
                      </td>

                      {/* Aksi */}
                      <td className="py-3 px-4 text-center">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedSub(sub);
                          }}
                          className="inline-flex items-center gap-1 px-3 py-1 bg-[#fff0ef] group-hover:bg-[#ed0226] text-[#be001c] group-hover:text-white rounded-lg text-xs font-bold border border-[#e9bcb8] transition-colors"
                        >
                          <Eye className="w-3.5 h-3.5" />
                          <span>Detail</span>
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Detail Modal */}
      {selectedSub && (
        <CustomerDetailModal
          subscriber={selectedSub}
          onClose={() => setSelectedSub(null)}
          onSubscriberUpdated={(updated) => {
            setSelectedSub(updated);
            onSubscriberUpdated(updated);
          }}
          onOpenMigration={(sub) => {
            setSelectedSub(null);
            setMigratingSub(sub);
          }}
        />
      )}

      {/* Migration Modal */}
      {migratingSub && (
        <DeviceMigrationModal
          subscriber={migratingSub}
          onClose={() => setMigratingSub(null)}
          onSuccess={(updated) => {
            onSubscriberUpdated(updated);
            setSelectedSub(updated);
          }}
        />
      )}
    </div>
  );
};
