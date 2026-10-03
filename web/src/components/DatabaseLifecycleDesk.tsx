'use client';

import React, { useState, useEffect, useCallback } from 'react';
import {
  Database,
  HardDrive,
  Download,
  Upload,
  Trash2,
  Clock,
  ShieldCheck,
  AlertCircle,
  CheckCircle2,
  RefreshCw,
  FileJson,
  Layers,
} from 'lucide-react';
import {
  fetchMaintenanceStats,
  getDatabaseBackupUrl,
  restoreDatabaseBackup,
  runRetentionPolicy,
} from '@/lib/api';
import { DatabaseMaintenanceStats, RetentionRunResult } from '@/types';

export const DatabaseLifecycleDesk: React.FC = () => {
  const [stats, setStats] = useState<DatabaseMaintenanceStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRestoring, setIsRestoring] = useState(false);
  const [isPruning, setIsPruning] = useState(false);
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  // Retention config
  const [threatDays, setThreatDays] = useState(90);
  const [auditDays, setAuditDays] = useState(180);
  const [dlqDays, setDlqDays] = useState(30);

  const loadStats = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await fetchMaintenanceStats();
      setStats(data);
    } catch (err: unknown) {
      console.error('Failed to load maintenance stats:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStats();
  }, [loadStats]);

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const confirmRestore = window.confirm(
      `Peringatan: Memulihkan database dari berkas "${file.name}" akan menimpa seluruh data subscriber, ancaman, dan konfigurasi saat ini. Lanjutkan?`
    );
    if (!confirmRestore) {
      e.target.value = '';
      return;
    }

    setIsRestoring(true);
    setFeedback(null);

    try {
      const text = await file.text();
      const parsed = JSON.parse(text);
      const res = await restoreDatabaseBackup(parsed);
      setFeedback({ type: 'success', message: res.message });
      loadStats();
    } catch (err: unknown) {
      setFeedback({
        type: 'error',
        message: err instanceof Error ? err.message : 'Gagal memulihkan database dari berkas backup.',
      });
    } finally {
      setIsRestoring(false);
      e.target.value = '';
    }
  };

  const handleRunRetention = async () => {
    const confirmPrune = window.confirm(
      `Jalankan pembersihan retensi data? (Ancaman > ${threatDays} hari, Audit > ${auditDays} hari, DLQ > ${dlqDays} hari)`
    );
    if (!confirmPrune) return;

    setIsPruning(true);
    setFeedback(null);

    try {
      const res = await runRetentionPolicy({
        threat_days: threatDays,
        audit_days: auditDays,
        dlq_days: dlqDays,
      });
      const data: RetentionRunResult = res.data;
      setFeedback({
        type: 'success',
        message: `Pembersihan berhasil: ${data.threats_pruned} ancaman usang, ${data.audit_pruned} log audit, dan ${data.dlq_pruned} antrean DLQ dihapus.`,
      });
      loadStats();
    } catch (err: unknown) {
      setFeedback({
        type: 'error',
        message: err instanceof Error ? err.message : 'Gagal menjalankan kebijakan retensi.',
      });
    } finally {
      setIsPruning(false);
    }
  };

  const formatBytes = (bytes: number) => {
    if (!bytes || bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-[#0b132b] flex items-center gap-2">
            <Database className="w-6 h-6 text-[#ed0226]" />
            Database Maintenance &amp; Storage Lifecycle
          </h2>
          <p className="text-xs text-[#778ca2] mt-0.5">
            Manajemen snapshot cadangan penuh, pemulihan darurat (restore), dan siklus retensi data otomatis.
          </p>
        </div>

        <button
          onClick={loadStats}
          disabled={isLoading}
          className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-[#5e3f3c] bg-white border border-[#e9bcb8]/80 hover:bg-[#fff0ef] rounded-xl transition-all self-start sm:self-auto"
        >
          <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin text-[#be001c]' : ''}`} />
          Refresh Status
        </button>
      </div>

      {/* Feedback Banner */}
      {feedback && (
        <div
          className={`p-3.5 rounded-xl border text-xs font-medium flex items-center gap-2 ${
            feedback.type === 'success'
              ? 'bg-emerald-50 border-emerald-200 text-emerald-800'
              : 'bg-red-50 border-red-200 text-red-800'
          }`}
        >
          {feedback.type === 'success' ? (
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          ) : (
            <AlertCircle className="w-4 h-4 text-red-600 shrink-0" />
          )}
          <span>{feedback.message}</span>
        </div>
      )}

      {/* Storage Health & Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl p-4 shadow-sm">
          <div className="flex items-center justify-between text-[#778ca2] text-xs font-medium mb-1">
            <span>Ukuran File Basis Data</span>
            <HardDrive className="w-4 h-4 text-[#be001c]" />
          </div>
          <h3 className="text-2xl font-bold text-[#0b132b] mt-1">
            {formatBytes(stats?.file_size_bytes || 0)}
          </h3>
          <p className="text-[11px] text-[#778ca2] mt-0.5 font-mono truncate" title={stats?.data_file_path}>
            {stats?.data_file_path || 'data_store.json'}
          </p>
        </div>

        <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl p-4 shadow-sm">
          <div className="flex items-center justify-between text-[#778ca2] text-xs font-medium mb-1">
            <span>Status Kesehatan Engine</span>
            <ShieldCheck className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="flex items-center gap-1.5 mt-1">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
            <span className="text-xl font-bold text-[#0b132b]">{stats?.health_status || 'HEALTHY'}</span>
          </div>
          <p className="text-[11px] text-[#778ca2] mt-0.5">RW In-Memory Cache Active</p>
        </div>

        <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl p-4 shadow-sm">
          <div className="flex items-center justify-between text-[#778ca2] text-xs font-medium mb-1">
            <span>Total Catatan Entitas</span>
            <Layers className="w-4 h-4 text-purple-600" />
          </div>
          <h3 className="text-2xl font-bold text-[#0b132b] mt-1">
            {((stats?.total_subscribers || 0) +
              (stats?.total_threats || 0) +
              (stats?.total_phishing_domains || 0) +
              (stats?.total_audit_logs || 0) +
              (stats?.total_dlq_records || 0)).toLocaleString()}
          </h3>
          <p className="text-[11px] text-[#778ca2] mt-0.5">
            {stats?.total_subscribers} Pelanggan • {stats?.total_threats} Ancaman
          </p>
        </div>

        <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl p-4 shadow-sm">
          <div className="flex items-center justify-between text-[#778ca2] text-xs font-medium mb-1">
            <span>Cadangan Terakhir</span>
            <Clock className="w-4 h-4 text-blue-600" />
          </div>
          <div className="text-sm font-bold text-[#0b132b] mt-2">
            {stats?.last_backup_at && !stats.last_backup_at.startsWith('0001') ? (
              new Date(stats.last_backup_at).toLocaleString('id-ID', { dateStyle: 'short', timeStyle: 'short' })
            ) : (
              <span className="text-xs text-[#778ca2] font-normal italic">Belum pernah diekspor</span>
            )}
          </div>
          <p className="text-[11px] text-[#778ca2] mt-0.5">Full snapshot JSON</p>
        </div>
      </div>

      {/* Action Center Grid: Backup, Restore, Retention */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Card 1: Snapshot Backup */}
        <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl p-5 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2.5 mb-2">
              <div className="p-2.5 rounded-xl bg-[#ffe9e7] text-[#be001c]">
                <Download className="w-5 h-5" />
              </div>
              <div>
                <h4 className="font-bold text-sm text-[#0b132b]">Snapshot Cadangan Penuh</h4>
                <p className="text-[11px] text-[#778ca2]">Unduh seluruh data SOC ke berkas JSON</p>
              </div>
            </div>
            <p className="text-xs text-[#5e3f3c] mt-3 leading-relaxed">
              Mengekspor seluruh data pelanggan, lisensi B2B, log telemetri ancaman, database URL phising, operator RBAC, dan audit trail ke dalam satu arsip terenkripsi.
            </p>
          </div>

          <div className="mt-6 pt-4 border-t border-[#e9bcb8]/60">
            <a
              href={getDatabaseBackupUrl()}
              download
              className="flex items-center justify-center gap-2 w-full px-4 py-2.5 text-xs font-semibold text-white bg-[#ed0226] hover:bg-[#be001c] rounded-xl shadow-sm transition-all text-center"
            >
              <FileJson className="w-4 h-4" /> Unduh Cadangan JSON
            </a>
          </div>
        </div>

        {/* Card 2: Restore Backup */}
        <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl p-5 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2.5 mb-2">
              <div className="p-2.5 rounded-xl bg-blue-50 text-blue-700">
                <Upload className="w-5 h-5" />
              </div>
              <div>
                <h4 className="font-bold text-sm text-[#0b132b]">Pemulihan Darurat (Restore)</h4>
                <p className="text-[11px] text-[#778ca2]">Impor arsip JSON untuk memulihkan status</p>
              </div>
            </div>
            <p className="text-xs text-[#5e3f3c] mt-3 leading-relaxed">
              Pulihkan kondisi basis data dari snapshot yang telah diverifikasi. Tindakan ini akan divalidasi dan dicatat ke dalam audit log SOC secara otomatis.
            </p>
          </div>

          <div className="mt-6 pt-4 border-t border-[#e9bcb8]/60">
            <label className="flex items-center justify-center gap-2 w-full px-4 py-2.5 text-xs font-semibold text-[#be001c] bg-[#fff0ef] hover:bg-[#ffe9e7] border border-[#e9bcb8] rounded-xl transition-all cursor-pointer text-center">
              <Upload className="w-4 h-4" />
              {isRestoring ? 'Memulihkan...' : 'Pilih Berkas JSON & Pulihkan'}
              <input
                type="file"
                accept=".json"
                disabled={isRestoring}
                onChange={handleFileUpload}
                className="hidden"
              />
            </label>
          </div>
        </div>

        {/* Card 3: Retention & Purge */}
        <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl p-5 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2.5 mb-2">
              <div className="p-2.5 rounded-xl bg-amber-50 text-amber-700">
                <Trash2 className="w-5 h-5" />
              </div>
              <div>
                <h4 className="font-bold text-sm text-[#0b132b]">Siklus Retensi Data (Pruning)</h4>
                <p className="text-[11px] text-[#778ca2]">Bersihkan ancaman dan log yang kedaluwarsa</p>
              </div>
            </div>

            <div className="space-y-2.5 mt-3 text-xs">
              <div className="flex items-center justify-between">
                <span className="text-[#5e3f3c]">Hapus Ancaman Lebih Tua:</span>
                <select
                  value={threatDays}
                  onChange={(e) => setThreatDays(Number(e.target.value))}
                  className="px-2 py-1 bg-[#fff8f7] border border-[#e9bcb8] rounded-lg font-semibold"
                >
                  <option value={30}>30 Hari</option>
                  <option value={60}>60 Hari</option>
                  <option value={90}>90 Hari</option>
                </select>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-[#5e3f3c]">Hapus Audit Trail Lebih Tua:</span>
                <select
                  value={auditDays}
                  onChange={(e) => setAuditDays(Number(e.target.value))}
                  className="px-2 py-1 bg-[#fff8f7] border border-[#e9bcb8] rounded-lg font-semibold"
                >
                  <option value={90}>90 Hari</option>
                  <option value={180}>180 Hari</option>
                  <option value={365}>1 Tahun</option>
                </select>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-[#5e3f3c]">Hapus Resolved DLQ:</span>
                <select
                  value={dlqDays}
                  onChange={(e) => setDlqDays(Number(e.target.value))}
                  className="px-2 py-1 bg-[#fff8f7] border border-[#e9bcb8] rounded-lg font-semibold"
                >
                  <option value={14}>14 Hari</option>
                  <option value={30}>30 Hari</option>
                </select>
              </div>
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-[#e9bcb8]/60">
            <button
              onClick={handleRunRetention}
              disabled={isPruning}
              className="flex items-center justify-center gap-2 w-full px-4 py-2.5 text-xs font-semibold text-amber-800 bg-amber-50 hover:bg-amber-100 border border-amber-200 rounded-xl transition-all disabled:opacity-50"
            >
              <Trash2 className="w-4 h-4" />
              {isPruning ? 'Membersihkan...' : 'Jalankan Pembersihan Sekarang'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
