'use client';

import React, { useState } from 'react';
import { AlertTriangle, RotateCcw, Trash2, Eye, Clock, CheckCircle2, X } from 'lucide-react';
import { DeadLetterRecord } from '@/types';
import { resolveDLQRecord } from '@/lib/api';

interface DeadLetterQueueTableProps {
  records: DeadLetterRecord[];
  loading: boolean;
  onRefresh: () => void;
}

export const DeadLetterQueueTable: React.FC<DeadLetterQueueTableProps> = ({
  records,
  loading,
  onRefresh,
}) => {
  const [selectedRecord, setSelectedRecord] = useState<DeadLetterRecord | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);

  const handleResolve = async (id: string, action: 'replay' | 'discard') => {
    setActionLoadingId(id);
    try {
      await resolveDLQRecord(id, action, `Mitigated manually by SOC operator at ${new Date().toISOString()}`);
      setFeedback(`Pesan antrean ${id} berhasil di-${action === 'replay' ? 'replay' : 'discard'}.`);
      setTimeout(() => setFeedback(null), 3500);
      onRefresh();
      if (selectedRecord && selectedRecord.id === id) {
        setSelectedRecord(null);
      }
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Gagal memproses pesan antrean');
    } finally {
      setActionLoadingId(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'bg-red-50 text-red-700 border-red-200';
      case 'REPLAYED':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'DISCARDED':
        return 'bg-slate-50 text-slate-700 border-slate-200';
      default:
        return 'bg-gray-50 text-gray-700 border-gray-200';
    }
  };

  return (
    <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl shadow-sm overflow-hidden">
      <div className="p-4 border-b border-[#e9bcb8]/60 bg-[#fff8f7] flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-red-100 text-[#ed0226]">
            <AlertTriangle className="w-4 h-4" />
          </div>
          <div>
            <h4 className="font-bold text-sm text-[#0b132b]">Dead-Letter Error Queue (DLQ) &amp; Mitigasi</h4>
            <p className="text-[11px] text-[#778ca2]">Pencegat kegagalan skema / payload server eksternal agar data tidak hilang</p>
          </div>
        </div>

        <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-red-50 text-red-700 border border-red-200">
          {records.filter((r) => r.status === 'PENDING').length} Menunggu Tindakan
        </span>
      </div>

      {feedback && (
        <div className="p-3 bg-emerald-50 border-b border-emerald-200 text-xs text-emerald-800 flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          <span>{feedback}</span>
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead className="bg-[#fff8f7] border-b border-[#e9bcb8]/80 text-[#5e3f3c]">
            <tr>
              <th className="py-2.5 px-3.5 font-semibold">Queue ID</th>
              <th className="py-2.5 px-3.5 font-semibold">Sumber Server</th>
              <th className="py-2.5 px-3.5 font-semibold">Penyebab Kegagalan</th>
              <th className="py-2.5 px-3.5 font-semibold">Status</th>
              <th className="py-2.5 px-3.5 font-semibold">Waktu Kejadian</th>
              <th className="py-2.5 px-3.5 font-semibold text-right">Tindakan Mitigasi</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#e9bcb8]/40">
            {loading && records.length === 0 ? (
              <tr>
                <td colSpan={6} className="p-8 text-center text-xs text-[#778ca2]">
                  Memuat antrean error...
                </td>
              </tr>
            ) : records.length === 0 ? (
              <tr>
                <td colSpan={6} className="p-8 text-center text-xs text-[#778ca2]">
                  Tidak ada pesan gagal di antrean DLQ. Semua server sinkron sempurna!
                </td>
              </tr>
            ) : (
              records.map((rec) => (
                <tr key={rec.id} className="hover:bg-[#fff0ef]/30">
                  <td className="py-2.5 px-3.5 whitespace-nowrap font-mono font-bold text-[#0b132b]">
                    {rec.id}
                  </td>
                  <td className="py-2.5 px-3.5 whitespace-nowrap">
                    <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-100 text-slate-800 border border-slate-200">
                      {rec.source}
                    </span>
                  </td>
                  <td className="py-2.5 px-3.5 text-xs text-red-700 max-w-xs truncate" title={rec.error_message}>
                    {rec.error_message}
                  </td>
                  <td className="py-2.5 px-3.5 whitespace-nowrap">
                    <span className={`inline-block text-[10px] font-bold px-2 py-0.5 rounded-full border ${getStatusBadge(rec.status)}`}>
                      {rec.status}
                    </span>
                  </td>
                  <td className="py-2.5 px-3.5 whitespace-nowrap text-[#778ca2] text-[11px] font-mono">
                    <div className="flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      {new Date(rec.timestamp).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })}
                    </div>
                  </td>
                  <td className="py-2.5 px-3.5 whitespace-nowrap text-right">
                    <div className="flex items-center justify-end gap-1.5">
                      <button
                        onClick={() => setSelectedRecord(rec)}
                        title="Periksa Raw Payload"
                        className="p-1.5 text-[#5e3f3c] hover:text-[#be001c] bg-white border border-[#e9bcb8] rounded-lg hover:bg-[#fff0ef]"
                      >
                        <Eye className="w-3.5 h-3.5" />
                      </button>
                      {rec.status === 'PENDING' && (
                        <>
                          <button
                            onClick={() => handleResolve(rec.id, 'replay')}
                            disabled={actionLoadingId === rec.id}
                            title="Replay / Jalankan Ulang"
                            className="flex items-center gap-1 px-2 py-1 text-[11px] font-semibold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 rounded-lg transition-all"
                          >
                            <RotateCcw className="w-3 h-3" /> Replay
                          </button>
                          <button
                            onClick={() => handleResolve(rec.id, 'discard')}
                            disabled={actionLoadingId === rec.id}
                            title="Discard / Abaikan"
                            className="flex items-center gap-1 px-2 py-1 text-[11px] font-semibold text-slate-700 bg-slate-100 hover:bg-slate-200 border border-slate-300 rounded-lg transition-all"
                          >
                            <Trash2 className="w-3 h-3" /> Discard
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Payload Modal */}
      {selectedRecord && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-fadeIn">
          <div className="bg-white rounded-2xl max-w-lg w-full border border-[#e9bcb8]/80 shadow-2xl p-5 overflow-hidden">
            <div className="flex items-center justify-between pb-3 border-b border-[#e9bcb8]/60 mb-3">
              <h4 className="font-bold text-sm text-[#0b132b]">Raw Payload: {selectedRecord.id}</h4>
              <button onClick={() => setSelectedRecord(null)} className="p-1 rounded text-[#778ca2] hover:text-[#0b132b]">
                <X className="w-4 h-4" />
              </button>
            </div>
            <div className="bg-slate-900 text-slate-100 p-3 rounded-xl font-mono text-xs max-h-64 overflow-y-auto select-all">
              <pre>{selectedRecord.payload_raw}</pre>
            </div>
            <p className="text-xs text-red-600 mt-2 font-medium">Error: {selectedRecord.error_message}</p>
            <div className="flex justify-end gap-2 mt-4">
              <button
                onClick={() => setSelectedRecord(null)}
                className="px-3 py-1.5 text-xs font-semibold text-[#778ca2] bg-white border border-[#e9bcb8] rounded-xl hover:bg-gray-50"
              >
                Tutup
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
