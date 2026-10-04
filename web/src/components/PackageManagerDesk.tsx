'use client';

import React, { useState, useEffect } from 'react';
import {
  Package,
  Plus,
  Power,
  Trash2,
  Ghost,
  BarChart3,
  ListOrdered,
  AlertCircle,
  CheckCircle2,
  Shield,
  Layers,
} from 'lucide-react';
import { SecurityPackage, Subscriber } from '@/types';
import {
  getStoredPackages,
  togglePackageStatus,
  deletePackage,
} from '@/lib/packageService';
import { CreatePackageModal } from './packages/CreatePackageModal';
import { GhostSubscriberDesk } from './packages/GhostSubscriberDesk';
import { PackageUsageReportDesk } from './packages/PackageUsageReportDesk';

interface PackageManagerDeskProps {
  subscribers: Subscriber[];
  onRefresh?: () => void;
}

export const PackageManagerDesk: React.FC<PackageManagerDeskProps> = ({
  subscribers,
  onRefresh,
}) => {
  const [packages, setPackages] = useState<SecurityPackage[]>([]);
  const [activeTab, setActiveTab] = useState<'CATALOG' | 'GHOST_SUBS' | 'REPORTS'>('CATALOG');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [statusMessage, setStatusMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  useEffect(() => {
    setPackages(getStoredPackages());
  }, []);

  const handlePackageCreated = (newPkg: SecurityPackage) => {
    setPackages(getStoredPackages());
    setStatusMessage({ type: 'success', text: `Paket "${newPkg.name}" berhasil dibuat dan ditambahkan ke katalog.` });
    setTimeout(() => setStatusMessage(null), 4000);
  };

  const handleToggle = (id: string) => {
    const updated = togglePackageStatus(id);
    if (updated) {
      setPackages(getStoredPackages());
      setStatusMessage({
        type: 'success',
        text: `Status paket "${updated.name}" kini ${updated.is_active ? 'AKTIF' : 'NONAKTIF'}.`,
      });
      setTimeout(() => setStatusMessage(null), 3000);
    }
  };

  const handleDelete = (id: string, name: string) => {
    if (!window.confirm(`Yakin ingin menghapus paket "${name}"?`)) return;
    const res = deletePackage(id);
    if (res.success) {
      setPackages(getStoredPackages());
      setStatusMessage({ type: 'success', text: res.message });
    } else {
      setStatusMessage({ type: 'error', text: res.message });
    }
    setTimeout(() => setStatusMessage(null), 4000);
  };

  return (
    <div className="space-y-6">
      {/* Top Header Card */}
      <div className="bg-white rounded-2xl border border-[#e2e8f0] p-6 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center space-x-3.5">
          <div className="p-3 rounded-2xl bg-[#fff0ef] text-[#ed0226] border border-[#e9bcb8]">
            <Package className="w-6 h-6 text-[#ed0226]" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-[#0b132b] tracking-tight">
              Manajemen Paket & Ghost Subscribers
            </h2>
            <p className="text-xs text-[#778ca2]">
              Kontrol penuh katalog tarif Telkomsel Secure, intervensi Ghost Subscribers, dan laporan performa paket.
            </p>
          </div>
        </div>

        <button
          onClick={() => setIsCreateModalOpen(true)}
          className="px-4 py-2.5 bg-[#ed0226] hover:bg-[#be001c] text-white rounded-xl text-xs font-bold shadow-md shadow-[#ed0226]/20 transition flex items-center space-x-2 self-start md:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>Buat Paket Baru</span>
        </button>
      </div>

      {/* Status Feedback Notification */}
      {statusMessage && (
        <div
          className={`p-3.5 text-xs font-medium rounded-xl border flex items-center space-x-2 animate-in fade-in duration-200 ${
            statusMessage.type === 'success'
              ? 'bg-[#10b981]/10 border-[#10b981]/30 text-[#10b981]'
              : 'bg-[#fff0ef] border-[#e9bcb8] text-[#ed0226]'
          }`}
        >
          {statusMessage.type === 'success' ? (
            <CheckCircle2 className="w-4 h-4 shrink-0" />
          ) : (
            <AlertCircle className="w-4 h-4 shrink-0" />
          )}
          <span>{statusMessage.text}</span>
        </div>
      )}

      {/* Sub-tab Navigation */}
      <div className="flex items-center space-x-2 border-b border-[#e2e8f0] pb-2">
        <button
          onClick={() => setActiveTab('CATALOG')}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center space-x-2 ${
            activeTab === 'CATALOG'
              ? 'bg-[#0b132b] text-white shadow-sm'
              : 'bg-white text-[#778ca2] hover:bg-[#f8fafc] border border-[#e2e8f0]'
          }`}
        >
          <ListOrdered className="w-4 h-4" />
          <span>Katalog & Status Paket ({packages.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('GHOST_SUBS')}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center space-x-2 ${
            activeTab === 'GHOST_SUBS'
              ? 'bg-[#be001c] text-white shadow-sm'
              : 'bg-white text-[#778ca2] hover:bg-[#f8fafc] border border-[#e2e8f0]'
          }`}
        >
          <Ghost className="w-4 h-4" />
          <span>Ghost Subscribers (Penyelamatan Adopsi)</span>
        </button>

        <button
          onClick={() => setActiveTab('REPORTS')}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center space-x-2 ${
            activeTab === 'REPORTS'
              ? 'bg-[#0b132b] text-white shadow-sm'
              : 'bg-white text-[#778ca2] hover:bg-[#f8fafc] border border-[#e2e8f0]'
          }`}
        >
          <BarChart3 className="w-4 h-4" />
          <span>Laporan Penggunaan & Monetisasi</span>
        </button>
      </div>

      {/* SUB-TAB 1: CATALOG & CRUD */}
      {activeTab === 'CATALOG' && (
        <div className="bg-white rounded-2xl border border-[#e2e8f0] shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-[#f8fafc] text-[#5e3f3c] font-bold border-b border-[#e2e8f0] uppercase text-[10px]">
                <tr>
                  <th className="px-4 py-3">Kode SKU</th>
                  <th className="px-4 py-3">Nama Paket</th>
                  <th className="px-4 py-3">Segmen</th>
                  <th className="px-4 py-3">Harga</th>
                  <th className="px-4 py-3">Durasi</th>
                  <th className="px-4 py-3">Fitur Keamanan</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Aksi</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#f1f5f9]">
                {packages.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="px-4 py-8 text-center text-[#778ca2]">
                      Belum ada paket keamanan. Klik "Buat Paket Baru" untuk menambahkan.
                    </td>
                  </tr>
                ) : (
                  packages.map((pkg) => (
                    <tr key={pkg.id} className="hover:bg-[#f8fafc]/80 transition">
                      <td className="px-4 py-3 font-mono font-bold text-[#0b132b]">{pkg.code}</td>
                      <td className="px-4 py-3 font-semibold text-[#0b132b]">{pkg.name}</td>
                      <td className="px-4 py-3">
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-[#f1f5f9] text-[#5e3f3c]">
                          {pkg.segment}
                        </span>
                      </td>
                      <td className="px-4 py-3 font-bold text-[#0b132b]">
                        Rp {pkg.price.toLocaleString('id-ID')}
                      </td>
                      <td className="px-4 py-3 text-[#778ca2]">{pkg.duration_days} Hari</td>
                      <td className="px-4 py-3">
                        <div className="flex flex-wrap gap-1 max-w-xs">
                          {pkg.features.slice(0, 2).map((f) => (
                            <span
                              key={f}
                              className="px-1.5 py-0.5 rounded text-[9px] bg-[#f1f5f9] text-[#3a405a] border border-[#e2e8f0]"
                            >
                              {f}
                            </span>
                          ))}
                          {pkg.features.length > 2 && (
                            <span className="px-1.5 py-0.5 rounded text-[9px] bg-[#fff8f7] text-[#ed0226] font-bold">
                              +{pkg.features.length - 2} lagi
                            </span>
                          )}
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <button
                          onClick={() => handleToggle(pkg.id)}
                          className={`inline-flex items-center space-x-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold transition border ${
                            pkg.is_active
                              ? 'bg-[#e8f5e9] text-[#10b981] border-[#a5d6a7] hover:bg-[#c8e6c9]'
                              : 'bg-[#f1f5f9] text-[#778ca2] border-[#cbd5e1] hover:bg-[#e2e8f0]'
                          }`}
                          title="Klik untuk mengubah status paket"
                        >
                          <Power className="w-3 h-3" />
                          <span>{pkg.is_active ? 'AKTIF' : 'NONAKTIF'}</span>
                        </button>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <button
                          onClick={() => handleDelete(pkg.id, pkg.name)}
                          className="p-1.5 text-[#778ca2] hover:text-[#ed0226] hover:bg-[#fff0ef] rounded-lg transition"
                          title="Hapus paket"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* SUB-TAB 2: GHOST SUBSCRIBERS */}
      {activeTab === 'GHOST_SUBS' && (
        <GhostSubscriberDesk subscribers={subscribers} onRefresh={onRefresh} />
      )}

      {/* SUB-TAB 3: USAGE REPORT */}
      {activeTab === 'REPORTS' && (
        <PackageUsageReportDesk packages={packages} />
      )}

      {/* Create Modal */}
      <CreatePackageModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onPackageCreated={handlePackageCreated}
      />
    </div>
  );
};
