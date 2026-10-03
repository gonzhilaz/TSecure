'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Server, Key, KeyRound, Copy, Check, Code2, RefreshCw, CheckCircle2, Clock, Globe } from 'lucide-react';
import { fetchApiKeys, fetchDLQRecords } from '@/lib/api';
import { IngestionApiKey, DeadLetterRecord } from '@/types';
import { ApiKeyModal } from './ApiKeyModal';
import { DeadLetterQueueTable } from './DeadLetterQueueTable';

export const IngestionGatewayDesk: React.FC = () => {
  const [keys, setKeys] = useState<IngestionApiKey[]>([]);
  const [dlqRecords, setDlqRecords] = useState<DeadLetterRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [copiedKeyId, setCopiedKeyId] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    setIsLoading(true);
    try {
      const [keysData, dlqData] = await Promise.all([
        fetchApiKeys(),
        fetchDLQRecords('ALL'),
      ]);
      setKeys(keysData);
      setDlqRecords(dlqData);
    } catch (err: unknown) {
      console.error('Failed to load ingestion data:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleCopyKey = (key: string, id: string) => {
    navigator.clipboard.writeText(key);
    setCopiedKeyId(id);
    setTimeout(() => setCopiedKeyId(null), 2000);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-[#0b132b] flex items-center gap-2">
            <Server className="w-6 h-6 text-[#ed0226]" />
            External Server Ingestion Gateway &amp; Dead-Letter Queue
          </h2>
          <p className="text-xs text-[#778ca2] mt-0.5">
            Penerimaan data server-to-server dari Telkomsel NDP Billing, Secure Web Proxy, dan DNS Gateway.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-[#ed0226] hover:bg-[#be001c] rounded-xl shadow-sm transition-all"
          >
            <Key className="w-4 h-4" />
            Generate Ingestion Key
          </button>
          <button
            onClick={loadData}
            disabled={isLoading}
            className="p-2 text-[#5e3f3c] bg-white border border-[#e9bcb8]/80 hover:bg-[#fff0ef] rounded-xl transition-all"
            title="Refresh Data"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin text-[#be001c]' : ''}`} />
          </button>
        </div>
      </div>

      {/* Active API Keys Table */}
      <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl shadow-sm overflow-hidden">
        <div className="p-4 border-b border-[#e9bcb8]/60 bg-[#fff8f7] flex items-center justify-between">
          <div>
            <h3 className="font-bold text-sm text-[#0b132b]">Authorized Server API Credentials ({keys.length})</h3>
            <p className="text-[11px] text-[#778ca2]">Kunci otentikasi header X-TelkomSecure-Key untuk server luar</p>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#fff8f7] border-b border-[#e9bcb8]/80 text-[#5e3f3c]">
              <tr>
                <th className="py-3 px-4 font-semibold">Nama Integrasi</th>
                <th className="py-3 px-4 font-semibold">Tipe Sumber</th>
                <th className="py-3 px-4 font-semibold">API Key Token</th>
                <th className="py-3 px-4 font-semibold text-center">Permintaan Diterima</th>
                <th className="py-3 px-4 font-semibold">Terakhir Digunakan</th>
                <th className="py-3 px-4 font-semibold text-right">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#e9bcb8]/40">
              {isLoading && keys.length === 0 ? (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-xs text-[#778ca2]">
                    Memuat API keys...
                  </td>
                </tr>
              ) : (
                keys.map((k) => (
                  <tr key={k.id} className="hover:bg-[#fff0ef]/30 transition-colors">
                    <td className="py-3 px-4 whitespace-nowrap font-bold text-[#0b132b]">
                      {k.name}
                    </td>

                    <td className="py-3 px-4 whitespace-nowrap">
                      <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-100 text-slate-800 border border-slate-200">
                        {k.source}
                      </span>
                    </td>

                    <td className="py-3 px-4 whitespace-nowrap">
                      <div className="flex items-center gap-1.5 font-mono text-xs text-[#5e3f3c] bg-slate-50 px-2 py-1 rounded-lg border border-slate-200 max-w-xs truncate select-all">
                        <span className="truncate">{k.key}</span>
                        <button
                          onClick={() => handleCopyKey(k.key, k.id)}
                          className="text-[#778ca2] hover:text-[#be001c] shrink-0"
                          title="Salin Kunci"
                        >
                          {copiedKeyId === k.id ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                        </button>
                      </div>
                    </td>

                    <td className="py-3 px-4 text-center whitespace-nowrap font-bold text-[#0b132b]">
                      {k.request_count.toLocaleString()} req
                    </td>

                    <td className="py-3 px-4 whitespace-nowrap text-[#778ca2]">
                      {k.last_used_at && !k.last_used_at.startsWith('0001') ? (
                        <div className="flex items-center gap-1">
                          <Clock className="w-3.5 h-3.5" />
                          {new Date(k.last_used_at).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })}
                        </div>
                      ) : (
                        <span className="text-[11px] italic">Belum ada request</span>
                      )}
                    </td>

                    <td className="py-3 px-4 text-right whitespace-nowrap">
                      <span className="inline-flex items-center gap-1 text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
                        <CheckCircle2 className="w-3 h-3" /> ACTIVE
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Developer cURL Integration Snippets */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-[#0b132b] text-slate-100 rounded-2xl p-4 shadow-sm font-mono text-xs">
          <div className="flex items-center justify-between text-slate-400 pb-2 mb-2 border-b border-slate-800">
            <span className="flex items-center gap-1.5 text-emerald-400 font-semibold">
              <Code2 className="w-4 h-4" /> 1. NDP Server Ingestion (POST)
            </span>
            <span className="text-[10px]">/api/v1/ingest/ndp</span>
          </div>
          <pre className="text-[11px] text-slate-300 overflow-x-auto whitespace-pre">
{`curl -X POST https://backend-i3wy.vercel.app/api/v1/ingest/ndp \\
  -H "X-TelkomSecure-Key: tk_sec_ndp_live_9921" \\
  -H "Content-Type: application/json" \\
  -d '{
    "msisdn": "081299887766",
    "action": "PURCHASE",
    "package_name": "Secure Guard 30 Hari",
    "duration_days": 30,
    "price": 15000
  }'`}
          </pre>
        </div>

        <div className="bg-[#0b132b] text-slate-100 rounded-2xl p-4 shadow-sm font-mono text-xs">
          <div className="flex items-center justify-between text-slate-400 pb-2 mb-2 border-b border-slate-800">
            <span className="flex items-center gap-1.5 text-blue-400 font-semibold">
              <Globe className="w-4 h-4" /> 2. Proxy Threat Batch Ingestion (POST)
            </span>
            <span className="text-[10px]">/api/v1/ingest/proxy-threats</span>
          </div>
          <pre className="text-[11px] text-slate-300 overflow-x-auto whitespace-pre">
{`curl -X POST https://backend-i3wy.vercel.app/api/v1/ingest/proxy-threats \\
  -H "X-TelkomSecure-Key: tk_sec_proxy_jkt_4410" \\
  -H "Content-Type: application/json" \\
  -d '{
    "gateway_id": "PROXY-JKT-01",
    "threats": [
      {
        "url": "https://bca-klik-update.info/auth",
        "threat_type": "PHISHING",
        "verdict": "DNS Sinkholed",
        "msisdn": "0811223344"
      }
    ]
  }'`}
          </pre>
        </div>
      </div>

      {/* Dead-Letter Queue Table */}
      <DeadLetterQueueTable
        records={dlqRecords}
        loading={isLoading}
        onRefresh={() => fetchDLQRecords('ALL').then(setDlqRecords)}
      />

      {/* Modal */}
      {isModalOpen && (
        <ApiKeyModal
          onClose={() => setIsModalOpen(false)}
          onKeyCreated={(newKey) => {
            setKeys((prev) => [...prev, newKey]);
          }}
        />
      )}
    </div>
  );
};
