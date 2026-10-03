'use client';

import React, { useState, useEffect } from 'react';
import {
  X,
  Copy,
  Check,
  ExternalLink,
  ShieldAlert,
  Globe,
  Calendar,
  Users,
  Save,
  FileText,
  AlertTriangle,
} from 'lucide-react';
import { PhishingRecord } from '@/types';

interface PhishingDetailModalProps {
  record: PhishingRecord | null;
  onClose: () => void;
  onUpdateStatus: (id: string, status: string, notes: string) => Promise<void>;
  isUpdating: boolean;
}

export const PhishingDetailModal: React.FC<PhishingDetailModalProps> = ({
  record,
  onClose,
  onUpdateStatus,
  isUpdating,
}) => {
  const [copiedUrl, setCopiedUrl] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState<string>('');
  const [notes, setNotes] = useState<string>('');

  useEffect(() => {
    if (record) {
      setSelectedStatus(record.status);
      setNotes(record.notes || '');
    }
  }, [record]);

  if (!record) return null;

  const handleCopyUrl = () => {
    navigator.clipboard.writeText(record.url);
    setCopiedUrl(true);
    setTimeout(() => setCopiedUrl(false), 2000);
  };

  const handleSave = async () => {
    await onUpdateStatus(record.id, selectedStatus, notes);
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

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-fadeIn">
      <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto border border-[#e9bcb8]/80 shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-[#e9bcb8]/50 bg-[#fff8f7]">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-[#ffe9e7] text-[#be001c]">
              <ShieldAlert className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="font-bold text-lg text-[#0b132b]">{record.id}</h3>
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${getStatusBadge(record.status)}`}>
                  {record.status}
                </span>
                <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-red-100 text-red-800">
                  {record.severity}
                </span>
              </div>
              <p className="text-xs text-[#778ca2]">
                Target Brand: <strong className="text-[#0b132b]">{record.target_brand}</strong> • Kategori: {record.category}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-xl text-[#778ca2] hover:bg-white hover:text-[#0b132b] border border-transparent hover:border-[#e9bcb8]/80 transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-5">
          {/* Target URL Box */}
          <div className="bg-[#fff0ef]/50 border border-[#e9bcb8] rounded-xl p-3.5">
            <div className="flex items-center justify-between text-xs font-semibold text-[#be001c] mb-1.5">
              <span className="flex items-center gap-1.5">
                <Globe className="w-4 h-4" /> Targeted Malicious URL
              </span>
              <button
                onClick={handleCopyUrl}
                className="flex items-center gap-1 text-[11px] font-medium text-[#778ca2] hover:text-[#be001c] bg-white px-2 py-0.5 rounded-md border border-[#e9bcb8]"
              >
                {copiedUrl ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                {copiedUrl ? 'Tersalin' : 'Salin URL'}
              </button>
            </div>
            <p className="text-xs font-mono text-[#0b132b] break-all select-all bg-white p-2.5 rounded-lg border border-[#e9bcb8]/60">
              {record.url}
            </p>
            <div className="flex items-center justify-between text-[11px] text-[#778ca2] mt-2">
              <span>Domain: <strong className="font-mono text-[#0b132b]">{record.domain}</strong></span>
              <span className="text-emerald-700 font-medium">Verdict: {record.action_taken}</span>
            </div>
          </div>

          {/* Intel Metadata Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div className="bg-[#fff8f7] border border-[#e9bcb8]/60 rounded-xl p-3">
              <div className="flex items-center gap-1.5 text-xs text-[#778ca2] mb-1">
                <ShieldAlert className="w-3.5 h-3.5 text-[#be001c]" /> Intercept Hit Count
              </div>
              <p className="text-lg font-bold text-[#0b132b]">{record.hit_count.toLocaleString()} Kali</p>
            </div>

            <div className="bg-[#fff8f7] border border-[#e9bcb8]/60 rounded-xl p-3">
              <div className="flex items-center gap-1.5 text-xs text-[#778ca2] mb-1">
                <Calendar className="w-3.5 h-3.5 text-blue-600" /> First Detected
              </div>
              <p className="text-xs font-semibold text-[#0b132b]">
                {new Date(record.first_detected_at).toLocaleString('id-ID', { dateStyle: 'short', timeStyle: 'short' })}
              </p>
            </div>

            <div className="bg-[#fff8f7] border border-[#e9bcb8]/60 rounded-xl p-3">
              <div className="flex items-center gap-1.5 text-xs text-[#778ca2] mb-1">
                <Calendar className="w-3.5 h-3.5 text-emerald-600" /> Last Detected
              </div>
              <p className="text-xs font-semibold text-[#0b132b]">
                {new Date(record.last_detected_at).toLocaleString('id-ID', { dateStyle: 'short', timeStyle: 'short' })}
              </p>
            </div>
          </div>

          {/* KSN Verdict */}
          <div className="bg-slate-50 border border-slate-200 rounded-xl p-3">
            <p className="text-xs font-semibold text-slate-700 mb-1 flex items-center gap-1.5">
              <AlertTriangle className="w-3.5 h-3.5 text-amber-600" /> Kaspersky Cloud (KSN) Verdict
            </p>
            <p className="text-xs text-slate-800 font-mono">
              {record.ksn_verdict || 'Heuristic threat match detected on Android Web Protection'}
            </p>
          </div>

          {/* Targeted Victims MSISDNs */}
          <div className="bg-white border border-[#e9bcb8]/80 rounded-xl p-3">
            <p className="text-xs font-semibold text-[#0b132b] mb-2 flex items-center gap-1.5">
              <Users className="w-3.5 h-3.5 text-purple-600" /> Targeted Subscribers ({record.targeted_msisdns?.length || 0})
            </p>
            {record.targeted_msisdns && record.targeted_msisdns.length > 0 ? (
              <div className="flex flex-wrap gap-1.5 max-h-24 overflow-y-auto">
                {record.targeted_msisdns.map((msisdn, i) => (
                  <span
                    key={i}
                    className="text-[11px] font-mono bg-[#fff0ef] text-[#be001c] px-2 py-0.5 rounded border border-[#e9bcb8]"
                  >
                    {msisdn}
                  </span>
                ))}
              </div>
            ) : (
              <p className="text-xs text-[#778ca2] italic">Belum ada MSISDN pelanggan yang terasosiasi langsung.</p>
            )}
          </div>

          {/* Triage & Mitigation Status Update */}
          <div className="space-y-3 pt-2 border-t border-[#e9bcb8]/60">
            <div>
              <label className="block text-xs font-semibold text-[#0b132b] mb-1">
                Triage / Mitigation Status
              </label>
              <select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                className="w-full text-xs font-medium bg-white border border-[#e9bcb8] rounded-xl px-3 py-2.5 text-[#0b132b] focus:outline-none focus:ring-2 focus:ring-[#ed0226]/20"
              >
                <option value="ACTIVE_THREAT">ACTIVE_THREAT (Masih beredar aktif)</option>
                <option value="REPORTED_KOMINFO">REPORTED_KOMINFO (Sudah dilaporkan Aduan Konten)</option>
                <option value="TAKEN_DOWN">TAKEN_DOWN (DNS sinkholed / host suspended)</option>
                <option value="WHITELISTED">WHITELISTED (False positive / aman)</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-[#0b132b] mb-1 flex items-center gap-1">
                <FileText className="w-3.5 h-3.5 text-[#778ca2]" /> Analyst Notes / CSIRT Ticket ID
              </label>
              <textarea
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                rows={3}
                placeholder="Catatan takedown, nomor tiket CSIRT BSSN/Kominfo, atau kontak abuse ISP..."
                className="w-full text-xs bg-white border border-[#e9bcb8] rounded-xl p-3 text-[#0b132b] focus:outline-none focus:ring-2 focus:ring-[#ed0226]/20"
              />
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-2.5 p-4 border-t border-[#e9bcb8]/60 bg-[#fff8f7]">
          <button
            onClick={onClose}
            className="px-4 py-2 text-xs font-semibold text-[#778ca2] hover:text-[#0b132b] bg-white border border-[#e9bcb8] rounded-xl hover:bg-gray-50 transition-all"
          >
            Tutup
          </button>
          <button
            onClick={handleSave}
            disabled={isUpdating}
            className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-[#ed0226] hover:bg-[#be001c] rounded-xl shadow-sm transition-all disabled:opacity-50"
          >
            <Save className="w-3.5 h-3.5" />
            {isUpdating ? 'Menyimpan...' : 'Simpan Perubahan'}
          </button>
        </div>
      </div>
    </div>
  );
};
