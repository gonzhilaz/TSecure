'use client';

import React from 'react';
import { Globe, ShieldAlert, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { PhishingStats } from '@/types';

interface PhishingStatsCardsProps {
  stats: PhishingStats | null;
  loading: boolean;
}

export const PhishingStatsCards: React.FC<PhishingStatsCardsProps> = ({ stats, loading }) => {
  if (loading && !stats) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="bg-white border border-[#e9bcb8]/80 rounded-2xl p-4 shadow-sm animate-pulse">
            <div className="h-4 bg-[#ffe9e7] rounded w-24 mb-3" />
            <div className="h-8 bg-[#ffe9e7] rounded w-16 mb-2" />
            <div className="h-3 bg-[#ffe9e7] rounded w-32" />
          </div>
        ))}
      </div>
    );
  }

  const cards = [
    {
      label: 'Unique Malicious Domains',
      value: stats?.total_unique_domains ?? 0,
      description: 'Total domain phising terdeteksi',
      icon: Globe,
      color: 'text-[#ed0226]',
      bg: 'bg-[#ffe9e7]',
      border: 'border-[#e9bcb8]/80',
    },
    {
      label: 'Total Hits Intercepted',
      value: stats?.total_hits_blocked ?? 0,
      description: 'Upaya akses berhasil digagalkan',
      icon: ShieldAlert,
      color: 'text-[#be001c]',
      bg: 'bg-[#fff0ef]',
      border: 'border-[#e9bcb8]/80',
    },
    {
      label: 'Active Threats (Unmitigated)',
      value: stats?.active_threats_count ?? 0,
      description: 'Domain aktif beredar di publik',
      icon: AlertTriangle,
      color: 'text-[#d97706]',
      bg: 'bg-[#fffbeb]',
      border: 'border-[#fde68a]',
    },
    {
      label: 'Taken Down / Mitigated',
      value: stats?.taken_down_count ?? 0,
      description: 'Domain dinetralisir / takedown',
      icon: CheckCircle2,
      color: 'text-[#059669]',
      bg: 'bg-[#ecfdf5]',
      border: 'border-[#a7f3d0]',
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      {cards.map((card, idx) => {
        const Icon = card.icon;
        return (
          <div
            key={idx}
            className={`bg-white border ${card.border} rounded-2xl p-4 shadow-sm transition-all hover:shadow-md flex items-center justify-between`}
          >
            <div>
              <p className="text-xs font-medium text-[#778ca2] uppercase tracking-wider">
                {card.label}
              </p>
              <h3 className="text-2xl font-bold text-[#0b132b] mt-1">
                {card.value.toLocaleString()}
              </h3>
              <p className="text-[11px] text-[#778ca2] mt-0.5">
                {card.description}
              </p>
            </div>
            <div className={`p-3 rounded-xl ${card.bg} ${card.color} shrink-0`}>
              <Icon className="w-6 h-6" />
            </div>
          </div>
        );
      })}
    </div>
  );
};
