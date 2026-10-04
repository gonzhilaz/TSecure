'use client';

import React, { useState } from 'react';
import { X, Plus, Shield, Check, DollarSign, Calendar, Layers } from 'lucide-react';
import { SecurityPackage } from '@/types';
import { createPackage } from '@/lib/packageService';

interface CreatePackageModalProps {
  isOpen: boolean;
  onClose: () => void;
  onPackageCreated: (pkg: SecurityPackage) => void;
}

const AVAILABLE_FEATURES = [
  'Anti-Malware Realtime (KavSDK)',
  'Web Phishing Filter',
  'Wi-Fi Safety Audit',
  'SIM Swap Alert',
  'Device Integrity & Root Guard',
  'Multi-Device Support (Family)',
  'DNS Gateway Sinkhole Policy',
  'SOC Telemetry Feed',
];

export const CreatePackageModal: React.FC<CreatePackageModalProps> = ({
  isOpen,
  onClose,
  onPackageCreated,
}) => {
  const [name, setName] = useState('');
  const [code, setCode] = useState('');
  const [price, setPrice] = useState<number>(15000);
  const [durationDays, setDurationDays] = useState<number>(30);
  const [billingCycle, setBillingCycle] = useState<'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'>('MONTHLY');
  const [segment, setSegment] = useState<'PRABAYAR' | 'PASCABAYAR' | 'ENTERPRISE_B2B'>('PRABAYAR');
  const [description, setDescription] = useState('');
  const [selectedFeatures, setSelectedFeatures] = useState<string[]>([
    'Anti-Malware Realtime (KavSDK)',
    'Web Phishing Filter',
  ]);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const toggleFeature = (feat: string) => {
    setSelectedFeatures((prev) =>
      prev.includes(feat) ? prev.filter((f) => f !== feat) : [...prev, feat]
    );
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('Nama paket wajib diisi.');
      return;
    }
    if (!code.trim()) {
      setError('Kode paket wajib diisi (misal: TS-SEC-30D).');
      return;
    }
    if (price <= 0) {
      setError('Harga paket harus lebih besar dari 0.');
      return;
    }
    if (selectedFeatures.length === 0) {
      setError('Pilih minimal 1 fitur keamanan.');
      return;
    }

    const created = createPackage({
      name: name.trim(),
      code: code.trim().toUpperCase(),
      price,
      duration_days: durationDays,
      billing_cycle: billingCycle,
      segment,
      is_active: true,
      description: description.trim() || 'Paket proteksi Telkomsel Secure.',
      features: selectedFeatures,
    });

    onPackageCreated(created);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-in fade-in duration-200">
      <div className="bg-white w-full max-w-lg rounded-2xl shadow-2xl border border-[#e2e8f0] overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#e2e8f0] bg-[#fff8f7]">
          <div className="flex items-center space-x-2.5">
            <div className="p-2 bg-[#ed0226]/10 text-[#ed0226] rounded-xl border border-[#ed0226]/20">
              <Plus className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-[#0b132b]">Buat Paket Keamanan Baru</h3>
              <p className="text-xs text-[#778ca2]">Konfigurasi tarif & fitur proteksi Telkomsel Secure</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-[#778ca2] hover:text-[#0b132b] hover:bg-[#f1f5f9] rounded-xl transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-6 space-y-4">
          {error && (
            <div className="p-3 text-xs bg-[#fff0ef] border border-[#e9bcb8] text-[#ed0226] rounded-xl font-medium">
              {error}
            </div>
          )}

          {/* Nama & Kode */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-[11px] font-bold text-[#5e3f3c] uppercase mb-1">Nama Paket</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="misal: Secure Pro 30 Hari"
                className="w-full text-xs px-3 py-2.5 bg-[#f8fafc] border border-[#e2e8f0] rounded-xl focus:outline-none focus:border-[#ed0226]"
              />
            </div>
            <div>
              <label className="block text-[11px] font-bold text-[#5e3f3c] uppercase mb-1">Kode Paket (SKU)</label>
              <input
                type="text"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                placeholder="misal: TS-SEC-PRO"
                className="w-full text-xs px-3 py-2.5 bg-[#f8fafc] border border-[#e2e8f0] rounded-xl uppercase focus:outline-none focus:border-[#ed0226]"
              />
            </div>
          </div>

          {/* Harga & Durasi */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-[11px] font-bold text-[#5e3f3c] uppercase mb-1">Harga (IDR / Pulsa)</label>
              <div className="relative">
                <span className="absolute left-3 top-2.5 text-xs text-[#778ca2]">Rp</span>
                <input
                  type="number"
                  value={price}
                  onChange={(e) => setPrice(Number(e.target.value))}
                  className="w-full text-xs pl-9 pr-3 py-2.5 bg-[#f8fafc] border border-[#e2e8f0] rounded-xl focus:outline-none focus:border-[#ed0226]"
                />
              </div>
            </div>
            <div>
              <label className="block text-[11px] font-bold text-[#5e3f3c] uppercase mb-1">Durasi (Hari)</label>
              <input
                type="number"
                value={durationDays}
                onChange={(e) => setDurationDays(Number(e.target.value))}
                className="w-full text-xs px-3 py-2.5 bg-[#f8fafc] border border-[#e2e8f0] rounded-xl focus:outline-none focus:border-[#ed0226]"
              />
            </div>
          </div>

          {/* Siklus Tagihan & Segmen */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-[11px] font-bold text-[#5e3f3c] uppercase mb-1">Siklus Tagihan</label>
              <select
                value={billingCycle}
                onChange={(e) => setBillingCycle(e.target.value as any)}
                className="w-full text-xs px-3 py-2.5 bg-[#f8fafc] border border-[#e2e8f0] rounded-xl focus:outline-none focus:border-[#ed0226]"
              >
                <option value="DAILY">Harian</option>
                <option value="WEEKLY">Mingguan</option>
                <option value="MONTHLY">Bulanan</option>
                <option value="YEARLY">Tahunan</option>
              </select>
            </div>
            <div>
              <label className="block text-[11px] font-bold text-[#5e3f3c] uppercase mb-1">Target Segmen</label>
              <select
                value={segment}
                onChange={(e) => setSegment(e.target.value as any)}
                className="w-full text-xs px-3 py-2.5 bg-[#f8fafc] border border-[#e2e8f0] rounded-xl focus:outline-none focus:border-[#ed0226]"
              >
                <option value="PRABAYAR">Prabayar (SimPATI / By.U)</option>
                <option value="PASCABAYAR">Pascabayar (Telkomsel Halo)</option>
                <option value="ENTERPRISE_B2B">B2B Corporate Enterprise</option>
              </select>
            </div>
          </div>

          {/* Checklist Fitur */}
          <div>
            <label className="block text-[11px] font-bold text-[#5e3f3c] uppercase mb-2">
              Fitur Keamanan yang Disertakan
            </label>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 bg-[#f8fafc] p-3 rounded-xl border border-[#e2e8f0]">
              {AVAILABLE_FEATURES.map((feat) => {
                const isSelected = selectedFeatures.includes(feat);
                return (
                  <button
                    key={feat}
                    type="button"
                    onClick={() => toggleFeature(feat)}
                    className={`flex items-center text-left space-x-2 p-2 rounded-lg text-xs transition border ${
                      isSelected
                        ? 'bg-white border-[#ed0226]/40 text-[#0b132b] font-medium shadow-sm'
                        : 'border-transparent text-[#778ca2] hover:bg-white/60'
                    }`}
                  >
                    <div
                      className={`w-4 h-4 rounded flex items-center justify-center border transition ${
                        isSelected
                          ? 'bg-[#ed0226] border-[#ed0226] text-white'
                          : 'border-[#cbd5e1] bg-white'
                      }`}
                    >
                      {isSelected && <Check className="w-3 h-3 stroke-[3]" />}
                    </div>
                    <span className="truncate">{feat}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Deskripsi */}
          <div>
            <label className="block text-[11px] font-bold text-[#5e3f3c] uppercase mb-1">Deskripsi Paket</label>
            <textarea
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Jelaskan nilai manfaat paket ini bagi pelanggan..."
              className="w-full text-xs px-3 py-2 bg-[#f8fafc] border border-[#e2e8f0] rounded-xl focus:outline-none focus:border-[#ed0226]"
            />
          </div>

          {/* Action Buttons */}
          <div className="flex items-center justify-end space-x-3 pt-3 border-t border-[#e2e8f0]">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-[#778ca2] hover:text-[#0b132b] transition"
            >
              Batal
            </button>
            <button
              type="submit"
              className="px-5 py-2 text-xs font-bold text-white bg-[#ed0226] hover:bg-[#be001c] rounded-xl shadow-md shadow-[#ed0226]/20 transition flex items-center space-x-1.5"
            >
              <Plus className="w-4 h-4" />
              <span>Simpan & Rilis Paket</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
