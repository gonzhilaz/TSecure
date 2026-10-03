'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Users, UserPlus, Shield, CheckCircle2, XCircle, Clock, Hash, AlertCircle } from 'lucide-react';
import { fetchOperators, toggleOperatorStatus, fetchAuditLogs } from '@/lib/api';
import { Operator, AuditLog } from '@/types';
import { OperatorModal } from './OperatorModal';
import { AuditTrailTable } from './AuditTrailTable';

export const UserManagementDesk: React.FC = () => {
  const [operators, setOperators] = useState<Operator[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [opsData, logsData] = await Promise.all([
        fetchOperators(),
        fetchAuditLogs(50),
      ]);
      setOperators(opsData);
      setAuditLogs(logsData);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Gagal memuat data operator');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleToggleStatus = async (op: Operator) => {
    try {
      const updated = await toggleOperatorStatus(op.id, !op.is_active);
      setOperators((prev) => prev.map((o) => (o.id === op.id ? updated.operator : o)));
      setFeedback(`Status ${op.name} berhasil diperbarui.`);
      setTimeout(() => setFeedback(null), 3000);
      fetchAuditLogs(50).then(setAuditLogs).catch(() => {});
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Gagal mengubah status');
    }
  };

  const getRoleBadge = (role: string) => {
    switch (role) {
      case 'SUPERADMIN':
        return 'bg-red-100 text-[#be001c] border-red-200';
      case 'SOC_ANALYST':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'CUSTOMER_CARE':
        return 'bg-teal-100 text-teal-800 border-teal-200';
      case 'AUDITOR':
        return 'bg-slate-100 text-slate-800 border-slate-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-[#0b132b] flex items-center gap-2">
            <Users className="w-6 h-6 text-[#ed0226]" />
            User Management &amp; Role-Based Access Control (RBAC)
          </h2>
          <p className="text-xs text-[#778ca2] mt-0.5">
            Kelola otorisasi akun personel SOC, lisensi administrator, dan audit jejak keamanan digital.
          </p>
        </div>

        <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-[#ed0226] hover:bg-[#be001c] rounded-xl shadow-sm transition-all"
        >
          <UserPlus className="w-4 h-4" />
          Tambah Operator Baru
        </button>
      </div>

      {feedback && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-emerald-800 flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          <span>{feedback}</span>
        </div>
      )}

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-xs text-red-800 flex items-center gap-2">
          <AlertCircle className="w-4 h-4 text-red-600" />
          <span>{error}</span>
        </div>
      )}

      {/* Operator Table (Data-Dense) */}
      <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl shadow-sm overflow-hidden">
        <div className="p-4 border-b border-[#e9bcb8]/60 bg-[#fff8f7] flex items-center justify-between">
          <h3 className="font-bold text-sm text-[#0b132b]">Daftar Operator Terdaftar ({operators.length})</h3>
          <span className="text-[11px] text-[#778ca2]">Hak akses divalidasi via backend JWT</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#fff8f7] border-b border-[#e9bcb8]/80 text-[#5e3f3c]">
              <tr>
                <th className="py-3 px-4 font-semibold">Operator</th>
                <th className="py-3 px-4 font-semibold">Badge ID</th>
                <th className="py-3 px-4 font-semibold">Role / Hak Akses</th>
                <th className="py-3 px-4 font-semibold">Status Akun</th>
                <th className="py-3 px-4 font-semibold">Login Terakhir</th>
                <th className="py-3 px-4 font-semibold text-right">Opsi</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#e9bcb8]/40">
              {isLoading && operators.length === 0 ? (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-xs text-[#778ca2]">
                    Memuat data operator...
                  </td>
                </tr>
              ) : (
                operators.map((op) => (
                  <tr key={op.id} className="hover:bg-[#fff0ef]/30 transition-colors">
                    <td className="py-3 px-4 whitespace-nowrap">
                      <div className="font-bold text-[#0b132b]">{op.name}</div>
                      <div className="text-[11px] text-[#778ca2] font-mono">{op.email}</div>
                    </td>

                    <td className="py-3 px-4 whitespace-nowrap font-mono text-xs text-[#5e3f3c]">
                      <span className="inline-flex items-center gap-1">
                        <Hash className="w-3 h-3 text-[#778ca2]" />
                        {op.badge_number}
                      </span>
                    </td>

                    <td className="py-3 px-4 whitespace-nowrap">
                      <span className={`inline-flex items-center gap-1 text-[10px] font-bold px-2.5 py-0.5 rounded-full border ${getRoleBadge(op.role)}`}>
                        <Shield className="w-3 h-3" />
                        {op.role}
                      </span>
                    </td>

                    <td className="py-3 px-4 whitespace-nowrap">
                      <span className={`inline-flex items-center gap-1 text-[10px] font-bold px-2 py-0.5 rounded-full ${op.is_active ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-red-50 text-red-700 border border-red-200'}`}>
                        {op.is_active ? <CheckCircle2 className="w-3 h-3" /> : <XCircle className="w-3 h-3" />}
                        {op.is_active ? 'AKTIF' : 'SUSPENDED'}
                      </span>
                    </td>

                    <td className="py-3 px-4 whitespace-nowrap text-[#778ca2]">
                      {op.last_login_at ? (
                        <div className="flex items-center gap-1">
                          <Clock className="w-3.5 h-3.5" />
                          {new Date(op.last_login_at).toLocaleString('id-ID', { dateStyle: 'short', timeStyle: 'short' })}
                        </div>
                      ) : (
                        <span className="text-[11px] italic">Belum pernah</span>
                      )}
                    </td>

                    <td className="py-3 px-4 text-right whitespace-nowrap">
                      {op.role !== 'SUPERADMIN' ? (
                        <button
                          onClick={() => handleToggleStatus(op)}
                          className={`text-xs font-semibold px-2.5 py-1 rounded-lg border transition-all ${
                            op.is_active
                              ? 'text-red-700 bg-red-50 hover:bg-red-100 border-red-200'
                              : 'text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border-emerald-200'
                          }`}
                        >
                          {op.is_active ? 'Nonaktifkan' : 'Aktifkan'}
                        </button>
                      ) : (
                        <span className="text-[11px] text-[#778ca2] italic">Proteksi Penuh</span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Audit Trail Section */}
      <AuditTrailTable
        logs={auditLogs}
        loading={isLoading}
        onRefresh={() => fetchAuditLogs(50).then(setAuditLogs)}
      />

      {/* Modal */}
      {isModalOpen && (
        <OperatorModal
          onClose={() => setIsModalOpen(false)}
          onOperatorCreated={(newOp) => {
            setOperators((prev) => [...prev, newOp]);
            setFeedback(`Operator ${newOp.name} berhasil didaftarkan.`);
            setTimeout(() => setFeedback(null), 3000);
            fetchAuditLogs(50).then(setAuditLogs).catch(() => {});
          }}
        />
      )}
    </div>
  );
};
