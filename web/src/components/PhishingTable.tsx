'use client';

import React, { useState } from 'react';
import {
  Copy,
  Check,
  Eye,
  AlertCircle,
  ShieldCheck,
  Clock,
  Send,
  CheckCircle2,
} from 'lucide-react';
import { PhishingRecord } from '@/types';

interface PhishingTableProps {
  records: PhishingRecord[];
  isLoading: boolean;
  error: string | null;
  onRetry: () => void;
  onSelectRecord: (record: PhishingRecord) => void;
  onQuickStatusChange: (record: PhishingRecord, newStatus: string) => void;
  hasActiveFilters: boolean;
  onResetFilters: () => void;
}

export const PhishingTable: React.FC<PhishingTableProps> = ({
  records,
  isLoading,
  error,
  onRetry,
  onSelectRecord,
  onQuickStatusChange,
  hasActiveFilters,
  onResetFilters,
}) => {
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const handleCopy = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE_THREAT':
        return 'bg-red-50 text-red-700 border-red-200';
      case 'REPORTED_KOMINFO':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'TAKEN_DOWN':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'WHITELISTED':
        return 'bg-slate-50 text-slate-700 border-slate-200';
      default:
        return 'bg-gray-50 text-gray-700 border-gray-200';
    }
  };

  const getBrandBadge = (brand: string) => {
    switch (brand) {
      case 'Telkomsel':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'Bank BCA':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'Bank BRI':
        return 'bg-sky-100 text-sky-800 border-sky-200';
      case 'Bank Mandiri':
        return 'bg-indigo-100 text-indigo-800 border-indigo-200';
      case 'Bank BNI':
        return 'bg-teal-100 text-teal-800 border-teal-200';
      case 'Instansi Pemerintah':
        return 'bg-amber-100 text-amber-800 border-amber-200';
      case 'E-Wallet & Fintech':
        return 'bg-purple-100 text-purple-800 border-purple-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  return (
    <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl shadow-sm overflow-hidden">
      {/* State 1: Error State */}
      {error && !isLoading && (
        <div className="p-8 text-center">
          <div className="w-12 h-12 rounded-full bg-red-100 text-[#ed0226] mx-auto flex items-center justify-center mb-3">
            <AlertCircle className="w-6 h-6" />
          </div>
          <h4 className="font-bold text-sm text-[#0b132b] mb-1">Gagal Memuat Data Phising</h4>
          <p className="text-xs text-[#778ca2] max-w-md mx-auto mb-4">{error}</p>
          <button
            onClick={onRetry}
            className="px-4 py-2 text-xs font-semibold text-white bg-[#ed0226] hover:bg-[#be001c] rounded-xl shadow-sm"
          >
            Coba Lagi
          </button>
        </div>
      )}

      {/* State 2: Loading Skeleton */}
      {isLoading && (
        <div className="p-6 space-y-3">
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="h-12 bg-[#fff0ef]/60 rounded-xl animate-pulse" />
          ))}
        </div>
      )}

      {/* State 3: Empty State */}
      {!isLoading && !error && records.length === 0 && (
        <div className="p-12 text-center">
          <div className="w-12 h-12 rounded-full bg-[#ffe9e7] text-[#be001c] mx-auto flex items-center justify-center mb-3">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <h4 className="font-bold text-sm text-[#0b132b] mb-1">Tidak Ada Ancaman Phising Ditemukan</h4>
          <p className="text-xs text-[#778ca2] max-w-sm mx-auto mb-4">
            Tidak ada data domain berbahaya yang sesuai dengan kriteria filter saat ini.
          </p>
          {hasActiveFilters && (
            <button
              onClick={onResetFilters}
              className="px-3.5 py-1.5 text-xs font-semibold text-[#be001c] bg-[#fff0ef] border border-[#e9bcb8] rounded-xl hover:bg-[#ffe9e7]"
            >
              Reset Filter
            </button>
          )}
        </div>
      )}

      {/* State 4: Data-Dense Table */}
      {!isLoading && !error && records.length > 0 && (
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#fff8f7] border-b border-[#e9bcb8]/80 text-[#5e3f3c]">
              <tr>
                <th className="py-3 px-4 font-semibold">Threat ID & Target</th>
                <th className="py-3 px-4 font-semibold">Malicious Domain / FQDN</th>
                <th className="py-3 px-4 font-semibold text-center">Hits Blocked</th>
                <th className="py-3 px-4 font-semibold text-center">Victims</th>
                <th className="py-3 px-4 font-semibold">Status Triage</th>
                <th className="py-3 px-4 font-semibold">Last Intercepted</th>
                <th className="py-3 px-4 font-semibold text-right">Aksi</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#e9bcb8]/40">
              {records.map((rec) => (
                <tr key={rec.id} className="hover:bg-[#fff0ef]/30 transition-colors">
                  <td className="py-3 px-4 whitespace-nowrap">
                    <div className="font-mono font-bold text-[#0b132b]">{rec.id}</div>
                    <div className="flex items-center gap-1.5 mt-0.5">
                      <span className={`text-[10px] font-semibold px-2 py-0.2 rounded-full border ${getBrandBadge(rec.target_brand)}`}>
                        {rec.target_brand}
                      </span>
                      <span className="text-[10px] text-[#778ca2]">{rec.category}</span>
                    </div>
                  </td>

                  <td className="py-3 px-4 max-w-xs">
                    <div className="flex items-center gap-1.5 font-mono font-medium text-[#0b132b] truncate">
                      <span className="truncate">{rec.domain}</span>
                      <button
                        onClick={() => handleCopy(rec.domain, rec.id + '-dom')}
                        title="Salin Domain"
                        className="text-[#778ca2] hover:text-[#be001c] shrink-0"
                      >
                        {copiedId === rec.id + '-dom' ? (
                          <Check className="w-3.5 h-3.5 text-emerald-600" />
                        ) : (
                          <Copy className="w-3.5 h-3.5" />
                        )}
                      </button>
                    </div>
                    <p className="text-[11px] text-[#778ca2] font-mono truncate mt-0.5" title={rec.url}>
                      {rec.url}
                    </p>
                  </td>

                  <td className="py-3 px-4 text-center whitespace-nowrap">
                    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-bold bg-red-100 text-[#be001c]">
                      {rec.hit_count} hits
                    </span>
                  </td>

                  <td className="py-3 px-4 text-center whitespace-nowrap">
                    <span className="text-xs font-semibold text-[#0b132b]">
                      {rec.targeted_msisdns?.length || 0} No. HP
                    </span>
                  </td>

                  <td className="py-3 px-4 whitespace-nowrap">
                    <span className={`inline-flex items-center gap-1 text-[10px] font-bold px-2.5 py-0.5 rounded-full border ${getStatusBadge(rec.status)}`}>
                      {rec.status === 'ACTIVE_THREAT' && (
                        <span className="w-1.5 h-1.5 rounded-full bg-red-600 animate-ping" />
                      )}
                      {rec.status}
                    </span>
                  </td>

                  <td className="py-3 px-4 whitespace-nowrap text-[#778ca2]">
                    <div className="flex items-center gap-1">
                      <Clock className="w-3.5 h-3.5" />
                      {new Date(rec.last_detected_at).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })}
                    </div>
                    <div className="text-[10px]">
                      {new Date(rec.last_detected_at).toLocaleDateString('id-ID', { day: 'numeric', month: 'short' })}
                    </div>
                  </td>

                  <td className="py-3 px-4 text-right whitespace-nowrap">
                    <div className="flex items-center justify-end gap-1.5">
                      {rec.status === 'ACTIVE_THREAT' && (
                        <button
                          onClick={() => onQuickStatusChange(rec, 'REPORTED_KOMINFO')}
                          title="Tandai Sudah Lapor Kominfo"
                          className="p-1.5 rounded-lg text-amber-700 bg-amber-50 hover:bg-amber-100 border border-amber-200 transition-all text-[11px] font-semibold flex items-center gap-1"
                        >
                          <Send className="w-3.5 h-3.5" /> Lapor
                        </button>
                      )}
                      {rec.status === 'REPORTED_KOMINFO' && (
                        <button
                          onClick={() => onQuickStatusChange(rec, 'TAKEN_DOWN')}
                          title="Tandai Berhasil Takedown"
                          className="p-1.5 rounded-lg text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 transition-all text-[11px] font-semibold flex items-center gap-1"
                        >
                          <CheckCircle2 className="w-3.5 h-3.5" /> Takedown
                        </button>
                      )}
                      <button
                        onClick={() => onSelectRecord(rec)}
                        className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-semibold text-[#be001c] bg-[#fff0ef] hover:bg-[#ffe9e7] border border-[#e9bcb8] transition-all"
                      >
                        <Eye className="w-3.5 h-3.5" /> Detail
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
