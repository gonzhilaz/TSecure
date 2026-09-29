'use client';

import React, { useState } from 'react';
import { Smartphone, Search, Key, ShieldCheck, AlertCircle } from 'lucide-react';
import { Subscriber } from '@/types';

interface SubscriberTableProps {
  subscribers: Subscriber[];
  loading: boolean;
}

export const SubscriberTable: React.FC<SubscriberTableProps> = ({ subscribers, loading }) => {
  const [search, setSearch] = useState('');
  const [filterActive, setFilterActive] = useState<'all' | 'active' | 'expired'>('all');

  const filtered = subscribers.filter((s) => {
    const matchesSearch =
      s.msisdn.toLowerCase().includes(search.toLowerCase()) ||
      s.device_model.toLowerCase().includes(search.toLowerCase()) ||
      s.plan_name.toLowerCase().includes(search.toLowerCase());

    if (!matchesSearch) return false;
    if (filterActive === 'active') return s.is_active;
    if (filterActive === 'expired') return !s.is_active;
    return true;
  });

  return (
    <div className="vigilance-card p-5 rounded-xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4 pb-3 border-b border-[#e2e8f0]">
        <div>
          <h2 className="text-sm font-bold text-[#0b132b] uppercase tracking-wider flex items-center space-x-2">
            <Smartphone className="w-4 h-4 text-[#ed0226]" />
            <span>Subscriber Fleet & License Allocation</span>
          </h2>
          <p className="text-xs text-[#778ca2]">
            Daftar perangkat terlindungi, binding Mobile ID, dan status alokasi lisensi Kaspersky
          </p>
        </div>

        {/* Filter & Search */}
        <div className="flex items-center space-x-2">
          <div className="relative">
            <Search className="absolute left-2.5 top-2 w-3.5 h-3.5 text-[#778ca2]" />
            <input
              type="text"
              placeholder="Cari MSISDN..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-8 pr-3 py-1.5 rounded-lg bg-[#f4f6f9] border border-[#e2e8f0] text-xs text-[#0b132b] placeholder-[#778ca2] focus:outline-none focus:border-[#ed0226] focus:bg-white transition-all"
            />
          </div>

          <select
            value={filterActive}
            onChange={(e) => setFilterActive(e.target.value as 'all' | 'active' | 'expired')}
            className="px-2.5 py-1.5 rounded-lg bg-[#f4f6f9] border border-[#e2e8f0] text-xs text-[#0b132b] focus:outline-none focus:border-[#ed0226]"
          >
            <option value="all">Semua</option>
            <option value="active">Aktif</option>
            <option value="expired">Expired</option>
          </select>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead>
            <tr className="bg-[#fff0ef]/60 border-b border-[#e9bcb8]/80 text-[#5e3f3c] uppercase tracking-wider font-bold text-[11px]">
              <th className="py-2.5 px-3 rounded-l-lg">MSISDN</th>
              <th className="py-2.5 px-3">Perangkat & OS</th>
              <th className="py-2.5 px-3">Paket Aktif</th>
              <th className="py-2.5 px-3">Masa Berlaku</th>
              <th className="py-2.5 px-3">Kaspersky License</th>
              <th className="py-2.5 px-3 text-right rounded-r-lg">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#e2e8f0]">
            {loading ? (
              [1, 2, 3].map((i) => (
                <tr key={i}>
                  <td colSpan={6} className="py-3 px-3">
                    <div className="h-8 rounded-lg skeleton-shimmer-light"></div>
                  </td>
                </tr>
              ))
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={6} className="py-8 text-center text-[#778ca2]">
                  Tidak ada data subscriber yang cocok dengan pencarian.
                </td>
              </tr>
            ) : (
              filtered.map((sub) => {
                const endDate = new Date(sub.active_period_end);
                const isExpired = !sub.is_active || endDate < new Date();
                return (
                  <tr key={sub.id} className="hover:bg-[#fff8f7] transition-colors">
                    <td className="py-3 px-3 font-mono font-bold text-[#0b132b]">
                      {sub.msisdn}
                      <span className="block text-[10px] text-[#778ca2] font-normal">
                        {sub.mobile_id}
                      </span>
                    </td>
                    <td className="py-3 px-3 text-[#2a1615]">
                      {sub.device_model}
                      <span className="block text-[10px] text-[#778ca2]">{sub.os_version}</span>
                    </td>
                    <td className="py-3 px-3">
                      <span className="font-semibold text-[#0b132b]">{sub.plan_name}</span>
                    </td>
                    <td className="py-3 px-3 font-mono text-[#006490] font-semibold">
                      {endDate.toLocaleDateString('id-ID')}
                      <span className="block text-[10px] text-[#778ca2] font-normal">
                        {isExpired ? 'Telah berakhir' : 'Aktif'}
                      </span>
                    </td>
                    <td className="py-3 px-3 font-mono text-[11px] text-[#545d7c]">
                      <div className="inline-flex items-center space-x-1 px-2 py-0.5 rounded bg-[#f4f6f9] border border-[#e2e8f0]">
                        <Key className="w-3 h-3 text-[#ed0226]" />
                        <span>{sub.kaspersky_license_key || '—'}</span>
                      </div>
                    </td>
                    <td className="py-3 px-3 text-right">
                      {isExpired ? (
                        <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full bg-[#ffdad6] border border-[#e9bcb8] text-[#be001c] font-bold text-[10px]">
                          <AlertCircle className="w-3 h-3 text-[#be001c]" />
                          <span>EXPIRED</span>
                        </span>
                      ) : (
                        <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full bg-[#e8f5e9] border border-[#a7f3d0] text-[#10b981] font-bold text-[10px]">
                          <ShieldCheck className="w-3 h-3 text-[#10b981]" />
                          <span>TERLINDUNGI</span>
                        </span>
                      )}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
