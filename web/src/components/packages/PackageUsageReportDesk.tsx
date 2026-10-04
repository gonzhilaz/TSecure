'use client';

import React, { useMemo } from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { Download, TrendingUp, DollarSign, Users, Award } from 'lucide-react';
import { SecurityPackage } from '@/types';

interface PackageUsageReportDeskProps {
  packages: SecurityPackage[];
}

const REPORT_COLORS = ['#be001c', '#ed0226', '#007eb4', '#10b981', '#f59e0b', '#6366f1'];

export const PackageUsageReportDesk: React.FC<PackageUsageReportDeskProps> = ({ packages }) => {
  // Aggregate Metrics
  const summary = useMemo(() => {
    const totalSubscribers = packages.reduce((acc, p) => acc + (p.subscriber_count || 0), 0);
    const totalRevenue = packages.reduce((acc, p) => acc + (p.total_revenue || 0), 0);
    const topPackage = [...packages].sort((a, b) => b.subscriber_count - a.subscriber_count)[0];

    return { totalSubscribers, totalRevenue, topPackage };
  }, [packages]);

  // Chart data
  const chartData = useMemo(() => {
    return packages.map((p) => ({
      name: p.code || p.name.slice(0, 14),
      fullName: p.name,
      subscribers: p.subscriber_count || 0,
      revenue: (p.total_revenue || 0) / 1000000, // In Millions IDR
    }));
  }, [packages]);

  const pieData = useMemo(() => {
    return packages.map((p) => ({
      name: p.name,
      value: p.subscriber_count || 1,
    }));
  }, [packages]);

  const handleExportCSV = () => {
    const headers = ['Kode SKU', 'Nama Paket', 'Segmen', 'Harga (IDR)', 'Durasi (Hari)', 'Total Subscribers', 'Total Revenue (IDR)', 'Status'];
    const rows = packages.map((p) => [
      p.code,
      `"${p.name}"`,
      p.segment,
      p.price,
      p.duration_days,
      p.subscriber_count,
      p.total_revenue,
      p.is_active ? 'AKTIF' : 'NONAKTIF',
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map((e) => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `TelkomselSecure-Package-Report-${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6">
      {/* KPI Highlight Strip */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 bg-white rounded-2xl border border-[#e2e8f0] shadow-sm flex items-center space-x-3.5">
          <div className="p-3 bg-[#be001c]/10 text-[#be001c] rounded-xl border border-[#be001c]/20">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold text-[#0b132b]">{summary.totalSubscribers.toLocaleString('id-ID')}</div>
            <div className="text-xs text-[#778ca2]">Total Pengguna Seluruh Paket</div>
          </div>
        </div>

        <div className="p-4 bg-white rounded-2xl border border-[#e2e8f0] shadow-sm flex items-center space-x-3.5">
          <div className="p-3 bg-[#10b981]/10 text-[#10b981] rounded-xl border border-[#10b981]/20">
            <DollarSign className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold text-[#0b132b]">
              Rp {summary.totalRevenue.toLocaleString('id-ID')}
            </div>
            <div className="text-xs text-[#778ca2]">Akumulasi Omset Paket Terjual</div>
          </div>
        </div>

        <div className="p-4 bg-white rounded-2xl border border-[#e2e8f0] shadow-sm flex items-center space-x-3.5">
          <div className="p-3 bg-[#007eb4]/10 text-[#007eb4] rounded-xl border border-[#007eb4]/20">
            <Award className="w-6 h-6" />
          </div>
          <div className="min-w-0">
            <div className="text-base font-bold text-[#0b132b] truncate">{summary.topPackage?.name || '-'}</div>
            <div className="text-xs text-[#778ca2]">Paket Terlaris ({summary.topPackage?.subscriber_count} pengguna)</div>
          </div>
        </div>
      </div>

      {/* Visual Analytics Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Bar Chart: Adopsi Subscribers */}
        <div className="lg:col-span-8 p-5 bg-white rounded-2xl border border-[#e2e8f0] shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h4 className="text-sm font-bold text-[#0b132b]">Adopsi Pelanggan per Paket</h4>
              <p className="text-xs text-[#778ca2]">Jumlah pengguna aktif terdaftar per SKU paket</p>
            </div>
          </div>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#778ca2' }} />
                <YAxis tick={{ fontSize: 11, fill: '#778ca2' }} />
                <Tooltip
                  formatter={(val: any) => [`${val} Pengguna`, 'Adopsi']}
                  contentStyle={{ backgroundColor: '#0b132b', borderRadius: '12px', color: '#fff', fontSize: '11px' }}
                />
                <Bar dataKey="subscribers" fill="#ed0226" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Donut Chart: Komposisi Segmen */}
        <div className="lg:col-span-4 p-5 bg-white rounded-2xl border border-[#e2e8f0] shadow-sm flex flex-col justify-between">
          <div>
            <h4 className="text-sm font-bold text-[#0b132b]">Proporsi Portofolio Paket</h4>
            <p className="text-xs text-[#778ca2] mb-3">Distribusi volume pengguna</p>
          </div>
          <div className="h-48">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={pieData} innerRadius={46} outerRadius={68} paddingAngle={4} dataKey="value">
                  {pieData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={REPORT_COLORS[index % REPORT_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(val: any) => [`${val} Pengguna`, 'Volume']}
                  contentStyle={{ backgroundColor: '#0b132b', borderRadius: '12px', color: '#fff', fontSize: '11px' }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="text-center text-[11px] text-[#778ca2] pt-2 border-t border-[#f1f5f9]">
            {packages.filter((p) => p.is_active).length} Paket Aktif di Katalog
          </div>
        </div>
      </div>

      {/* Usage Report Table */}
      <div className="bg-white rounded-2xl border border-[#e2e8f0] shadow-sm overflow-hidden">
        <div className="p-4 border-b border-[#e2e8f0] flex items-center justify-between">
          <div>
            <h4 className="text-sm font-bold text-[#0b132b]">Tabel Laporan Kinerja Paket</h4>
            <p className="text-xs text-[#778ca2]">Statistik adopsi, revenue, dan siklus penagihan</p>
          </div>
          <button
            onClick={handleExportCSV}
            className="px-3.5 py-1.5 bg-[#f8fafc] hover:bg-[#f1f5f9] text-[#0b132b] border border-[#cbd5e1] rounded-xl text-xs font-bold transition flex items-center space-x-1.5 shadow-sm"
          >
            <Download className="w-3.5 h-3.5 text-[#ed0226]" />
            <span>Export CSV</span>
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#f8fafc] text-[#5e3f3c] font-bold border-b border-[#e2e8f0] uppercase text-[10px]">
              <tr>
                <th className="px-4 py-3">Kode SKU</th>
                <th className="px-4 py-3">Nama Paket</th>
                <th className="px-4 py-3">Segmen</th>
                <th className="px-4 py-3">Harga Satuan</th>
                <th className="px-4 py-3">Durasi</th>
                <th className="px-4 py-3">Subscribers</th>
                <th className="px-4 py-3">Total Omset</th>
                <th className="px-4 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#f1f5f9]">
              {packages.map((pkg) => (
                <tr key={pkg.id} className="hover:bg-[#f8fafc]/80 transition">
                  <td className="px-4 py-3 font-mono font-bold text-[#0b132b]">{pkg.code}</td>
                  <td className="px-4 py-3 font-semibold text-[#0b132b]">{pkg.name}</td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-[#f1f5f9] text-[#5e3f3c]">
                      {pkg.segment}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-medium text-[#0b132b]">Rp {pkg.price.toLocaleString('id-ID')}</td>
                  <td className="px-4 py-3 text-[#778ca2]">{pkg.duration_days} Hari</td>
                  <td className="px-4 py-3 font-bold text-[#0b132b]">
                    {(pkg.subscriber_count || 0).toLocaleString('id-ID')}
                  </td>
                  <td className="px-4 py-3 font-bold text-[#10b981]">
                    Rp {(pkg.total_revenue || 0).toLocaleString('id-ID')}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold ${
                        pkg.is_active
                          ? 'bg-[#e8f5e9] text-[#10b981] border border-[#a5d6a7]'
                          : 'bg-[#f1f5f9] text-[#778ca2] border border-[#cbd5e1]'
                      }`}
                    >
                      {pkg.is_active ? 'AKTIF' : 'NONAKTIF'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
