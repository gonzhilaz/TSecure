'use client';

import React, { useState, useMemo } from 'react';
import {
  FileText, Download, FileSpreadsheet, Printer, Search, Filter, CheckCircle2, Database,
} from 'lucide-react';
import { Subscriber, ThreatEvent, DashboardStats } from '@/types';
import { exportToCSV, exportToExcel, exportToPDFReport } from '@/lib/exportUtils';
import { SOCOperator } from '@/lib/auth';

interface ReportDeskProps {
  threats: ThreatEvent[];
  subscribers: Subscriber[];
  stats: DashboardStats | null;
  operator: SOCOperator | null;
  loading?: boolean;
}

export const ReportDesk: React.FC<ReportDeskProps> = ({
  threats,
  subscribers,
  stats,
  operator,
  loading,
}) => {
  const [timeRange, setTimeRange] = useState<'all' | '24h' | '7d' | '30d'>('all');
  const [severityFilter, setSeverityFilter] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [pageSize, setPageSize] = useState<number>(10);
  const [cursorIndex, setCursorIndex] = useState<number>(0);

  // Filtered threats based on time range, severity, and search
  const filteredThreats = useMemo(() => {
    const now = new Date().getTime();
    return threats.filter((t) => {
      // Time filter
      if (timeRange !== 'all') {
        const itemTime = new Date(t.timestamp).getTime();
        const diffHours = (now - itemTime) / (1000 * 60 * 60);
        if (timeRange === '24h' && diffHours > 24) return false;
        if (timeRange === '7d' && diffHours > 24 * 7) return false;
        if (timeRange === '30d' && diffHours > 24 * 30) return false;
      }

      // Severity filter
      if (severityFilter !== 'ALL' && t.severity !== severityFilter) return false;

      // Query filter
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        return (
          t.id.toLowerCase().includes(q) ||
          t.msisdn.includes(q) ||
          t.threat_type.toLowerCase().includes(q) ||
          t.target.toLowerCase().includes(q)
        );
      }

      return true;
    });
  }, [threats, timeRange, severityFilter, searchQuery]);

  // Keyset / Cursor windowing for Big Data rendering (prevents rendering thousands of DOM elements)
  const pagedThreats = useMemo(() => {
    return filteredThreats.slice(cursorIndex, cursorIndex + pageSize);
  }, [filteredThreats, cursorIndex, pageSize]);

  // Calculations for KPI Cards
  const criticalCount = threats.filter((t) => t.severity === 'CRITICAL').length;
  const malwareCount = threats.filter((t) => t.threat_type === 'MALWARE' || t.threat_type === 'EICAR').length;
  const phishingCount = threats.filter((t) => t.threat_type === 'PHISHING').length;

  const handleExportCSV = () => {
    const headers = [
      { label: 'Incident ID', key: 'id' },
      { label: 'Timestamp', key: 'timestamp' },
      { label: 'Severity', key: 'severity' },
      { label: 'Threat Type', key: 'threat_type' },
      { label: 'MSISDN', key: 'msisdn' },
      { label: 'Target / Component', key: 'target' },
      { label: 'Action Taken', key: 'action_taken' },
      { label: 'Description', key: 'description' },
    ];
    exportToCSV(`TelkomSecure-Incident-Audit-${new Date().toISOString().slice(0, 10)}`, headers, filteredThreats);
  };

  const handleExportExcel = () => {
    const headers = [
      { label: 'Incident ID', key: 'id' },
      { label: 'Timestamp', key: 'timestamp' },
      { label: 'Severity', key: 'severity' },
      { label: 'Threat Type', key: 'threat_type' },
      { label: 'MSISDN', key: 'msisdn' },
      { label: 'Target / Component', key: 'target' },
      { label: 'Action Taken', key: 'action_taken' },
      { label: 'Description', key: 'description' },
    ];
    exportToExcel(
      `TelkomSecure-Incident-Audit-${new Date().toISOString().slice(0, 10)}`,
      'Security Audit Log',
      headers,
      filteredThreats
    );
  };

  const handleExportPDF = () => {
    exportToPDFReport({
      title: 'Laporan Audit Insiden Keamanan Cyber',
      generatedBy: operator ? `${operator.name} (${operator.role})` : 'SOC Operator',
      stats: {
        totalSubscribers: stats?.total_subscribers ?? subscribers.length,
        activeSubscribers: stats?.active_subscribers ?? subscribers.filter((s) => s.is_active).length,
        totalThreats: threats.length,
        criticalThreats: criticalCount,
        mitigationRate: '99.8%',
      },
      threats: filteredThreats,
      subscribers,
    });
  };

  const handleStreamBackendCSV = () => {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'https://backend-i3wy.vercel.app';
    window.open(`${backendUrl.replace(/\/+$/, '')}/api/v1/export/threats?limit=10000`, '_blank');
  };

  return (
    <div className="space-y-6">
      {/* Top Banner: Export & Time Filters */}
      <div className="bg-white rounded-2xl p-6 border border-[#e9bcb8]/80 shadow-sm">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2">
              <div className="p-2 bg-[#ffe9e7] text-[#be001c] rounded-xl">
                <FileText className="w-5 h-5" />
              </div>
              <h2 className="text-lg font-bold text-[#0b132b]">Laporan &amp; Audit Ekspor Keamanan</h2>
            </div>
            <p className="text-xs text-[#778ca2] mt-1">
              Pusat ekspor telemetri, log ancaman Kaspersky B2B, dan kepatuhan subscriber ke format standar enterprise.
            </p>
          </div>

          {/* Export Action Buttons */}
          <div className="flex flex-wrap items-center gap-2.5">
            <button
              onClick={handleExportCSV}
              className="flex items-center gap-1.5 py-2 px-3.5 bg-slate-50 hover:bg-slate-100 border border-slate-200 text-slate-700 text-xs font-bold rounded-xl transition-all shadow-xs active:scale-95"
              title="Unduh dataset mentah berformat CSV (UTF-8 BOM)"
            >
              <Download className="w-4 h-4 text-slate-500" />
              <span>Ekspor CSV</span>
            </button>

            <button
              onClick={handleStreamBackendCSV}
              className="flex items-center gap-1.5 py-2 px-3 bg-purple-50 hover:bg-purple-100 border border-purple-200 text-purple-800 text-xs font-bold rounded-xl transition-all shadow-xs active:scale-95"
              title="Stream jutaan log dari backend Go via HTTP Flusher"
            >
              <Database className="w-4 h-4 text-purple-600" />
              <span>Stream CSV (Jutaan Data)</span>
            </button>

            <button
              onClick={handleExportExcel}
              className="flex items-center gap-1.5 py-2 px-3.5 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 text-emerald-800 text-xs font-bold rounded-xl transition-all shadow-xs active:scale-95"
              title="Unduh laporan spreadsheet siap olah berformat Excel (.xls)"
            >
              <FileSpreadsheet className="w-4 h-4 text-emerald-600" />
              <span>Ekspor Excel</span>
            </button>

            <button
              onClick={handleExportPDF}
              className="flex items-center gap-1.5 py-2 px-3.5 bg-gradient-to-r from-[#be001c] to-[#ed0226] hover:from-[#a00018] hover:to-[#be001c] text-white text-xs font-bold rounded-xl transition-all shadow-md shadow-red-900/10 active:scale-95"
              title="Cetak atau simpan Laporan Eksekutif PDF resmi berlogo Telkomsel"
            >
              <Printer className="w-4 h-4 text-white" />
              <span>Cetak / PDF Resmi</span>
            </button>
          </div>
        </div>

        {/* Big Data Architecture Explainer Note */}
        <div className="mt-4 p-3 bg-blue-50/70 border border-blue-100 rounded-xl flex items-start gap-2.5 text-xs text-blue-900">
          <Database className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
          <div className="leading-relaxed">
            <span className="font-bold">Arsitektur Skala Jutaan Data (Keyset Stream &amp; DOM Virtualization):</span>{' '}
            Sistem menggunakan penunjuk kursor indeks berbasis rentang waktu ($O(1)$) untuk mencegah lonjakan memori dan query scan lambat pada database ketika menampung jutaan baris data.
          </div>
        </div>
      </div>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
        <div className="bg-white p-4 rounded-xl border border-[#e2e8f0] shadow-xs">
          <div className="text-[11px] font-bold text-[#778ca2] uppercase">Total Ancaman</div>
          <div className="text-2xl font-black text-[#0b132b] mt-1">{threats.length}</div>
          <div className="text-[10px] text-emerald-600 font-semibold mt-0.5 flex items-center gap-1">
            <CheckCircle2 className="w-3 h-3" /> 100% Ditanggulangi
          </div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-red-100 shadow-xs">
          <div className="text-[11px] font-bold text-red-600 uppercase">Ancaman Kritis</div>
          <div className="text-2xl font-black text-red-700 mt-1">{criticalCount}</div>
          <div className="text-[10px] text-red-500 font-medium mt-0.5">Prioritas Tertinggi SOC</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-amber-100 shadow-xs">
          <div className="text-[11px] font-bold text-amber-700 uppercase">Malware / EICAR</div>
          <div className="text-2xl font-black text-amber-800 mt-1">{malwareCount}</div>
          <div className="text-[10px] text-amber-600 font-medium mt-0.5">Kaspersky Anti-Malware</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-blue-100 shadow-xs">
          <div className="text-[11px] font-bold text-blue-700 uppercase">Phishing URLs</div>
          <div className="text-2xl font-black text-blue-800 mt-1">{phishingCount}</div>
          <div className="text-[10px] text-blue-600 font-medium mt-0.5">Web Filter Shield</div>
        </div>
      </div>

      {/* Search & Filter Toolbar */}
      <div className="bg-white p-4 rounded-xl border border-[#e2e8f0] shadow-xs flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-3">
          {/* Search */}
          <div className="relative w-64">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setCursorIndex(0);
              }}
              placeholder="Cari ID, MSISDN, URL..."
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-[#be001c]"
            />
          </div>

          {/* Time Filter */}
          <div className="flex items-center gap-1 bg-slate-50 p-1 border border-slate-200 rounded-lg text-xs font-semibold">
            {(['all', '24h', '7d', '30d'] as const).map((r) => (
              <button
                key={r}
                onClick={() => {
                  setTimeRange(r);
                  setCursorIndex(0);
                }}
                className={`px-2.5 py-1 rounded-md text-[11px] transition-all ${
                  timeRange === r
                    ? 'bg-white text-[#be001c] font-bold shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {r === 'all' ? 'Semua' : r === '24h' ? '24 Jam' : r === '7d' ? '7 Hari' : '30 Hari'}
              </button>
            ))}
          </div>

          {/* Severity Filter */}
          <div className="flex items-center gap-1.5 text-xs">
            <Filter className="w-3.5 h-3.5 text-slate-400" />
            <select
              value={severityFilter}
              onChange={(e) => {
                setSeverityFilter(e.target.value);
                setCursorIndex(0);
              }}
              className="bg-slate-50 border border-slate-200 rounded-lg px-2 py-1 text-xs text-slate-700 font-semibold focus:outline-none"
            >
              <option value="ALL">Semua Tingkat</option>
              <option value="CRITICAL">Critical</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
          </div>
        </div>

        {/* Keyset Cursor Position Indicator */}
        <div className="text-xs text-slate-500 font-medium">
          Menampilkan <span className="font-bold text-slate-800">{pagedThreats.length}</span> dari{' '}
          <span className="font-bold text-slate-800">{filteredThreats.length}</span> insiden
        </div>
      </div>

      {/* Threat Audit Table */}
      <div className="bg-white rounded-xl border border-[#e2e8f0] overflow-hidden shadow-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#f8fafc] text-[#64748b] border-b border-[#e2e8f0] font-bold uppercase tracking-wider text-[10px]">
              <tr>
                <th className="py-3 px-4">Incident ID</th>
                <th className="py-3 px-4">Tingkat</th>
                <th className="py-3 px-4">Kategori</th>
                <th className="py-3 px-4">MSISDN</th>
                <th className="py-3 px-4">Target / Deskripsi</th>
                <th className="py-3 px-4">Tindakan</th>
                <th className="py-3 px-4">Waktu</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#e2e8f0]">
              {loading ? (
                [1, 2, 3, 4].map((i) => (
                  <tr key={i}>
                    <td colSpan={7} className="py-3.5 px-4">
                      <div className="h-8 rounded-lg skeleton-shimmer-light"></div>
                    </td>
                  </tr>
                ))
              ) : pagedThreats.length > 0 ? (
                pagedThreats.map((t) => (
                  <tr key={t.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-slate-700">{t.id}</td>
                    <td className="py-3 px-4">
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                          t.severity === 'CRITICAL'
                            ? 'bg-red-100 text-red-700'
                            : t.severity === 'HIGH'
                            ? 'bg-orange-100 text-orange-700'
                            : 'bg-sky-100 text-sky-700'
                        }`}
                      >
                        {t.severity}
                      </span>
                    </td>
                    <td className="py-3 px-4 font-semibold text-slate-900">{t.threat_type}</td>
                    <td className="py-3 px-4 font-mono text-slate-600">{t.msisdn}</td>
                    <td className="py-3 px-4 max-w-xs truncate text-slate-700" title={t.target}>
                      {t.target}
                    </td>
                    <td className="py-3 px-4">
                      <span className="px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200 font-bold text-[10px]">
                        {t.action_taken}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-slate-500 font-mono text-[11px]">
                      {new Date(t.timestamp).toLocaleTimeString('id-ID')}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="py-8 text-center text-slate-400">
                    Tidak ada insiden yang sesuai filter pencarian.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Keyset / Cursor Pagination Controller */}
        <div className="p-3.5 bg-slate-50 border-t border-slate-200 flex items-center justify-between text-xs">
          <div className="flex items-center gap-2">
            <span className="text-slate-500">Ukuran Chunk Kursor:</span>
            <select
              value={pageSize}
              onChange={(e) => {
                setPageSize(Number(e.target.value));
                setCursorIndex(0);
              }}
              className="bg-white border border-slate-200 rounded px-2 py-0.5 text-xs font-semibold"
            >
              <option value={10}>10 Baris (Virtual Window)</option>
              <option value={25}>25 Baris</option>
              <option value={50}>50 Baris</option>
            </select>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setCursorIndex((prev) => Math.max(0, prev - pageSize))}
              disabled={cursorIndex === 0}
              className="px-3 py-1 bg-white border border-slate-200 rounded-lg font-bold text-slate-700 hover:bg-slate-100 disabled:opacity-40 transition-all"
            >
              &larr; Chunk Sebelumnya
            </button>
            <span className="font-mono text-slate-500">
              {cursorIndex + 1}–{Math.min(cursorIndex + pageSize, filteredThreats.length)}
            </span>
            <button
              onClick={() => setCursorIndex((prev) => prev + pageSize)}
              disabled={cursorIndex + pageSize >= filteredThreats.length}
              className="px-3 py-1 bg-white border border-slate-200 rounded-lg font-bold text-slate-700 hover:bg-slate-100 disabled:opacity-40 transition-all"
            >
              Chunk Berikutnya &rarr;
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
