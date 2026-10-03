'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import {
  Globe,
  Search,
  Download,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
} from 'lucide-react';
import { PhishingRecord, PhishingStats } from '@/types';
import {
  fetchPhishingRecords,
  fetchPhishingStats,
  updatePhishingStatus,
  getPhishingExportUrl,
} from '@/lib/api';
import { PhishingStatsCards } from './PhishingStatsCards';
import { PhishingDetailModal } from './PhishingDetailModal';
import { PhishingTable } from './PhishingTable';

interface PhishingIntelDeskProps {
  loading?: boolean;
}

export const PhishingIntelDesk: React.FC<PhishingIntelDeskProps> = () => {
  const [records, setRecords] = useState<PhishingRecord[]>([]);
  const [stats, setStats] = useState<PhishingStats | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedBrand, setSelectedBrand] = useState<string>('ALL');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');

  // Modal & Selection
  const [selectedRecord, setSelectedRecord] = useState<PhishingRecord | null>(null);
  const [isUpdating, setIsUpdating] = useState<boolean>(false);
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const loadData = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [recordsData, statsData] = await Promise.all([
        fetchPhishingRecords(searchQuery, selectedBrand, selectedStatus),
        fetchPhishingStats(),
      ]);
      setRecords(recordsData);
      setStats(statsData);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Gagal memuat database phising';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, [searchQuery, selectedBrand, selectedStatus]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleUpdateStatus = async (id: string, status: string, notes: string) => {
    setIsUpdating(true);
    try {
      const res = await updatePhishingStatus({ id, status, notes });
      setRecords((prev) => prev.map((r) => (r.id === id ? res.data : r)));
      if (selectedRecord && selectedRecord.id === id) {
        setSelectedRecord(res.data);
      }
      const updatedStats = await fetchPhishingStats().catch(() => null);
      if (updatedStats) setStats(updatedStats);

      setFeedback({ type: 'success', message: `Status ancaman ${id} berhasil diperbarui.` });
      setTimeout(() => setFeedback(null), 4000);
      setSelectedRecord(null);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Gagal memperbarui status';
      setFeedback({ type: 'error', message });
    } finally {
      setIsUpdating(false);
    }
  };

  const handleQuickStatusChange = async (record: PhishingRecord, newStatus: string) => {
    await handleUpdateStatus(record.id, newStatus, record.notes);
  };

  const hasActiveFilters = useMemo(() => {
    return Boolean(searchQuery.trim() || selectedBrand !== 'ALL' || selectedStatus !== 'ALL');
  }, [searchQuery, selectedBrand, selectedStatus]);

  const handleResetFilters = () => {
    setSearchQuery('');
    setSelectedBrand('ALL');
    setSelectedStatus('ALL');
  };

  return (
    <div className="space-y-6">
      {/* Executive Intro & Actions */}
      <div>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
          <div>
            <h2 className="text-xl font-bold text-[#0b132b] flex items-center gap-2">
              <Globe className="w-6 h-6 text-[#ed0226]" />
              Phishing & Malicious URL Threat Intelligence Desk
            </h2>
            <p className="text-xs text-[#778ca2] mt-0.5">
              Repositori pusat URL phising yang dipanen secara otomatis dari telemetri perangkat pelanggan untuk mitigasi CSIRT Telkomsel & Kominfo.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <a
              href={getPhishingExportUrl()}
              download
              className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-white bg-[#ed0226] hover:bg-[#be001c] rounded-xl shadow-sm transition-all"
            >
              <Download className="w-4 h-4" />
              Ekspor CSV CSIRT
            </a>
            <button
              onClick={loadData}
              disabled={isLoading}
              className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-[#5e3f3c] bg-white border border-[#e9bcb8]/80 hover:bg-[#fff0ef] rounded-xl transition-all disabled:opacity-50"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin text-[#be001c]' : ''}`} />
              Refresh
            </button>
          </div>
        </div>

        <PhishingStatsCards stats={stats} loading={isLoading} />
      </div>

      {/* Feedback Banner */}
      {feedback && (
        <div
          className={`p-3.5 rounded-xl border text-xs font-medium flex items-center gap-2 animate-fadeIn ${
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

      {/* Filter and Search Bar */}
      <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl p-4 shadow-sm">
        <div className="grid grid-cols-1 sm:grid-cols-12 gap-3">
          <div className="sm:col-span-6 relative">
            <Search className="w-4 h-4 absolute left-3.5 top-3 text-[#778ca2]" />
            <input
              type="text"
              placeholder="Cari domain, URL, atau ID ancaman..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-3.5 py-2 text-xs bg-[#fff8f7] border border-[#e9bcb8]/80 rounded-xl text-[#0b132b] placeholder-[#778ca2] focus:outline-none focus:ring-2 focus:ring-[#ed0226]/20"
            />
          </div>

          <div className="sm:col-span-3">
            <select
              value={selectedBrand}
              onChange={(e) => setSelectedBrand(e.target.value)}
              className="w-full py-2 px-3 text-xs bg-[#fff8f7] border border-[#e9bcb8]/80 rounded-xl text-[#0b132b] focus:outline-none focus:ring-2 focus:ring-[#ed0226]/20 font-medium"
            >
              <option value="ALL">Semua Target Brand</option>
              <option value="Telkomsel">Telkomsel / MyTelkomsel</option>
              <option value="Bank BCA">Bank BCA</option>
              <option value="Bank BRI">Bank BRI</option>
              <option value="Bank Mandiri">Bank Mandiri</option>
              <option value="Bank BNI">Bank BNI</option>
              <option value="Instansi Pemerintah">Instansi Pemerintah (DJP/Kemkes)</option>
              <option value="E-Wallet & Fintech">E-Wallet & Fintech (DANA/GoPay)</option>
              <option value="General Phishing">General Phishing</option>
            </select>
          </div>

          <div className="sm:col-span-3">
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="w-full py-2 px-3 text-xs bg-[#fff8f7] border border-[#e9bcb8]/80 rounded-xl text-[#0b132b] focus:outline-none focus:ring-2 focus:ring-[#ed0226]/20 font-medium"
            >
              <option value="ALL">Semua Status Triage</option>
              <option value="ACTIVE_THREAT">ACTIVE_THREAT</option>
              <option value="REPORTED_KOMINFO">REPORTED_KOMINFO</option>
              <option value="TAKEN_DOWN">TAKEN_DOWN</option>
              <option value="WHITELISTED">WHITELISTED</option>
            </select>
          </div>
        </div>
      </div>

      {/* Enterprise Data-Dense Grid (Table Component) */}
      <PhishingTable
        records={records}
        isLoading={isLoading}
        error={error}
        onRetry={loadData}
        onSelectRecord={(rec) => setSelectedRecord(rec)}
        onQuickStatusChange={handleQuickStatusChange}
        hasActiveFilters={hasActiveFilters}
        onResetFilters={handleResetFilters}
      />

      {/* Modal Detail & Triage Notes */}
      {selectedRecord && (
        <PhishingDetailModal
          record={selectedRecord}
          onClose={() => setSelectedRecord(null)}
          onUpdateStatus={handleUpdateStatus}
          isUpdating={isUpdating}
        />
      )}
    </div>
  );
};
