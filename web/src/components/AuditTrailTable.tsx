'use client';

import React, { useState } from 'react';
import { Clock, Search, Shield, Activity, RefreshCw } from 'lucide-react';
import { AuditLog } from '@/types';

interface AuditTrailTableProps {
  logs: AuditLog[];
  loading: boolean;
  onRefresh: () => void;
}

export const AuditTrailTable: React.FC<AuditTrailTableProps> = ({ logs, loading, onRefresh }) => {
  const [search, setSearch] = useState('');

  const filteredLogs = logs.filter((log) => {
    if (!search.trim()) return true;
    const q = search.toLowerCase();
    return (
      log.operator_email.toLowerCase().includes(q) ||
      log.action.toLowerCase().includes(q) ||
      log.target_resource.toLowerCase().includes(q) ||
      log.details.toLowerCase().includes(q)
    );
  });

  const getActionBadge = (action: string) => {
    switch (action) {
      case 'LOGIN_SUCCESS':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'LOGIN_FAILED':
        return 'bg-red-50 text-red-700 border-red-200';
      case 'OPERATOR_CREATE':
      case 'DATABASE_RESTORE':
      case 'DLQ_REPLAY':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'RETENTION_RUN':
      case 'DLQ_DISCARD':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      default:
        return 'bg-slate-50 text-slate-700 border-slate-200';
    }
  };

  return (
    <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl shadow-sm overflow-hidden">
      <div className="p-4 border-b border-[#e9bcb8]/60 bg-[#fff8f7] flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-[#ffe9e7] text-[#be001c]">
            <Activity className="w-4 h-4" />
          </div>
          <div>
            <h4 className="font-bold text-sm text-[#0b132b]">Audit Trail & Keamanan SOC</h4>
            <p className="text-[11px] text-[#778ca2]">Jejak aktivitas administratif yang tidak dapat dimanipulasi</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="w-3.5 h-3.5 text-[#778ca2] absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Cari aktivitas..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-8 pr-3 py-1.5 text-xs bg-white border border-[#e9bcb8] rounded-xl focus:outline-none w-48"
            />
          </div>
          <button
            onClick={onRefresh}
            className="p-1.5 text-[#778ca2] hover:text-[#0b132b] bg-white border border-[#e9bcb8] rounded-xl hover:bg-gray-50"
            title="Refresh Log"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin text-[#be001c]' : ''}`} />
          </button>
        </div>
      </div>

      <div className="overflow-x-auto max-h-96 overflow-y-auto">
        <table className="w-full text-left text-xs">
          <thead className="bg-[#fff8f7] border-b border-[#e9bcb8]/80 text-[#5e3f3c] sticky top-0">
            <tr>
              <th className="py-2.5 px-3.5 font-semibold">Waktu</th>
              <th className="py-2.5 px-3.5 font-semibold">Operator</th>
              <th className="py-2.5 px-3.5 font-semibold">Aksi</th>
              <th className="py-2.5 px-3.5 font-semibold">Target / Resource</th>
              <th className="py-2.5 px-3.5 font-semibold">Rincian Perubahan</th>
              <th className="py-2.5 px-3.5 font-semibold text-right">IP Address</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#e9bcb8]/40">
            {loading && logs.length === 0 ? (
              <tr>
                <td colSpan={6} className="p-8 text-center text-xs text-[#778ca2]">
                  Memuat audit trail...
                </td>
              </tr>
            ) : filteredLogs.length === 0 ? (
              <tr>
                <td colSpan={6} className="p-8 text-center text-xs text-[#778ca2]">
                  Belum ada log aktivitas yang cocok.
                </td>
              </tr>
            ) : (
              filteredLogs.map((log) => (
                <tr key={log.id} className="hover:bg-[#fff0ef]/30">
                  <td className="py-2.5 px-3.5 whitespace-nowrap text-[#778ca2]">
                    <div className="flex items-center gap-1 font-mono text-[11px]">
                      <Clock className="w-3 h-3 text-[#778ca2]" />
                      {new Date(log.timestamp).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                    </div>
                    <div className="text-[10px] text-[#778ca2]">
                      {new Date(log.timestamp).toLocaleDateString('id-ID', { day: 'numeric', month: 'short' })}
                    </div>
                  </td>
                  <td className="py-2.5 px-3.5 whitespace-nowrap font-medium text-[#0b132b]">
                    <div>{log.operator_name || log.operator_email}</div>
                    <div className="text-[10px] text-[#778ca2] font-mono">{log.operator_email}</div>
                  </td>
                  <td className="py-2.5 px-3.5 whitespace-nowrap">
                    <span className={`inline-block text-[10px] font-bold px-2 py-0.5 rounded-full border ${getActionBadge(log.action)}`}>
                      {log.action}
                    </span>
                  </td>
                  <td className="py-2.5 px-3.5 whitespace-nowrap font-mono text-xs text-[#5e3f3c]">
                    {log.target_resource}
                  </td>
                  <td className="py-2.5 px-3.5 text-xs text-[#0b132b] max-w-sm truncate" title={log.details}>
                    {log.details}
                  </td>
                  <td className="py-2.5 px-3.5 whitespace-nowrap text-right font-mono text-[11px] text-[#778ca2]">
                    {log.ip_address || '127.0.0.1'}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
