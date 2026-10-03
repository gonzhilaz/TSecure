'use client';

import React, { useState } from 'react';
import { X, Key, Server, Copy, Check } from 'lucide-react';
import { createApiKey } from '@/lib/api';
import { IngestionApiKey } from '@/types';

interface ApiKeyModalProps {
  onClose: () => void;
  onKeyCreated: (key: IngestionApiKey) => void;
}

export const ApiKeyModal: React.FC<ApiKeyModalProps> = ({ onClose, onKeyCreated }) => {
  const [name, setName] = useState('');
  const [source, setSource] = useState('NDP_BILLING');
  const [createdKey, setCreatedKey] = useState<IngestionApiKey | null>(null);
  const [copied, setCopied] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const res = await createApiKey(name, source);
      setIsLoading(false);
      setCreatedKey(res.data);
      onKeyCreated(res.data);
    } catch (err: unknown) {
      setIsLoading(false);
      setError(err instanceof Error ? err.message : 'Gagal membuat API key');
    }
  };

  const handleCopy = () => {
    if (createdKey) {
      navigator.clipboard.writeText(createdKey.key);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-fadeIn">
      <div className="bg-white rounded-2xl max-w-md w-full border border-[#e9bcb8]/80 shadow-2xl overflow-hidden">
        <div className="flex items-center justify-between p-5 border-b border-[#e9bcb8]/60 bg-[#fff8f7]">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-[#ffe9e7] text-[#be001c]">
              <Key className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-base text-[#0b132b]">Generate Ingestion API Key</h3>
              <p className="text-[11px] text-[#778ca2]">Otorisasi server-to-server (NDP &amp; Proxy)</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-lg text-[#778ca2] hover:bg-white hover:text-[#0b132b]">
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="p-5">
          {createdKey ? (
            <div className="space-y-4">
              <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl">
                <p className="text-xs font-bold text-emerald-800 mb-1">API Key Berhasil Diterbitkan!</p>
                <p className="text-[11px] text-emerald-700 mb-3">
                  Salin kunci ini sekarang. Simpan di environment variable server integrasi Anda.
                </p>
                <div className="flex items-center justify-between bg-white p-2.5 rounded-lg border border-emerald-200 font-mono text-xs text-[#0b132b] select-all break-all">
                  <span>{createdKey.key}</span>
                  <button
                    onClick={handleCopy}
                    className="ml-2 p-1.5 rounded text-emerald-700 hover:bg-emerald-50 shrink-0"
                    title="Salin Kunci"
                  >
                    {copied ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              <div className="text-right">
                <button
                  onClick={onClose}
                  className="px-4 py-2 text-xs font-semibold text-white bg-[#ed0226] hover:bg-[#be001c] rounded-xl shadow-sm"
                >
                  Selesai
                </button>
              </div>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-xs text-red-700">
                  {error}
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-[#0b132b] mb-1">Nama Layanan / Identitas Server</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Contoh: NDP Billing Jakarta Cluster"
                  className="w-full px-3 py-2 text-xs bg-[#fff8f7] border border-[#e9bcb8] rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#ed0226]/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-[#0b132b] mb-1">Tipe Server Integrasi</label>
                <div className="relative">
                  <Server className="w-4 h-4 text-[#778ca2] absolute left-3 top-2.5 pointer-events-none" />
                  <select
                    value={source}
                    onChange={(e) => setSource(e.target.value)}
                    className="w-full pl-9 pr-3 py-2 text-xs bg-[#fff8f7] border border-[#e9bcb8] rounded-xl focus:bg-white focus:outline-none font-medium"
                  >
                    <option value="NDP_BILLING">Telkomsel NDP Billing Core</option>
                    <option value="TELCO_PROXY">Telkom Regional Web Proxy / DNS</option>
                    <option value="WAF_GATEWAY">WAF / Edge Gateway</option>
                    <option value="EXTERNAL_SOC">Mitra Eksternal / BSSN CSIRT</option>
                  </select>
                </div>
              </div>

              <div className="flex items-center justify-end gap-2 pt-3 border-t border-[#e9bcb8]/60">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-3.5 py-1.5 text-xs font-semibold text-[#778ca2] hover:text-[#0b132b] bg-white border border-[#e9bcb8] rounded-xl hover:bg-gray-50"
                >
                  Batal
                </button>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="px-4 py-1.5 text-xs font-semibold text-white bg-[#ed0226] hover:bg-[#be001c] rounded-xl shadow-sm transition-all disabled:opacity-50"
                >
                  {isLoading ? 'Menerbitkan...' : 'Terbitkan Kunci'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
