'use client';

import React from 'react';
import { PackagePurchaseRecord } from '@/types';

interface CustomerPurchaseHistoryProps {
  purchaseHistory?: PackagePurchaseRecord[];
  fallbackPlan: string;
  fallbackPurchasedAt: string;
  isActive: boolean;
  formatDate: (iso?: string) => string;
}

export const CustomerPurchaseHistory: React.FC<CustomerPurchaseHistoryProps> = ({
  purchaseHistory,
  fallbackPlan,
  fallbackPurchasedAt,
  isActive,
  formatDate,
}) => {
  const records =
    purchaseHistory && purchaseHistory.length > 0
      ? purchaseHistory
      : [
          {
            id: 'ORD-CURRENT',
            package_name: fallbackPlan,
            purchased_at: fallbackPurchasedAt,
            duration_days: 30,
            price: 25000,
            channel: 'MyTelkomsel App',
            status: isActive ? 'ACTIVE' : 'EXPIRED',
          },
        ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h4 className="text-xs font-bold text-[#0b132b]">Daftar Histori Transaksi & Paket Pelanggan</h4>
        <span className="text-[11px] text-[#778ca2]">Riwayat aktivasi dari MyTelkomsel, USSD, & BSS</span>
      </div>

      <div className="border border-[#ffe9e7] rounded-xl overflow-hidden shadow-sm">
        <table className="w-full text-xs text-left">
          <thead className="bg-[#fff8f7] text-[#778ca2] uppercase text-[10px] border-b border-[#ffe9e7]">
            <tr>
              <th className="py-2.5 px-4">Order ID</th>
              <th className="py-2.5 px-4">Nama Paket</th>
              <th className="py-2.5 px-4">Waktu Pembelian</th>
              <th className="py-2.5 px-4">Channel</th>
              <th className="py-2.5 px-4">Durasi</th>
              <th className="py-2.5 px-4">Harga</th>
              <th className="py-2.5 px-4">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#ffe9e7]">
            {records.map((pkg) => (
              <tr key={pkg.id} className="hover:bg-[#fff8f7]">
                <td className="py-2.5 px-4 font-mono font-medium text-[#be001c]">{pkg.id}</td>
                <td className="py-2.5 px-4 font-bold text-[#0b132b]">{pkg.package_name}</td>
                <td className="py-2.5 px-4 text-[#5e3f3c]">{formatDate(pkg.purchased_at)}</td>
                <td className="py-2.5 px-4 text-[#778ca2]">{pkg.channel}</td>
                <td className="py-2.5 px-4">{pkg.duration_days} Hari</td>
                <td className="py-2.5 px-4 font-medium">Rp {pkg.price.toLocaleString('id-ID')}</td>
                <td className="py-2.5 px-4">
                  <span
                    className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      pkg.status === 'ACTIVE' ? 'bg-[#e8f5e9] text-[#065f46]' : 'bg-[#fff0ef] text-[#778ca2]'
                    }`}
                  >
                    {pkg.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
