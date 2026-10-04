'use client';

import React from 'react';
import { Users, KeyRound, ShieldAlert, Award } from 'lucide-react';
import { DashboardStats } from '@/types';

interface StatsCardsProps {
  stats: DashboardStats | null;
  loading: boolean;
}

export const StatsCards: React.FC<StatsCardsProps> = ({ stats, loading }) => {
  if (loading || !stats) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="vigilance-card p-5 rounded-xl">
            <div className="flex items-center justify-between mb-3">
              <div className="w-24 h-4 rounded skeleton-shimmer-light"></div>
              <div className="w-8 h-8 rounded-lg skeleton-shimmer-light"></div>
            </div>
            <div className="w-20 h-7 rounded mb-2 skeleton-shimmer-light"></div>
            <div className="w-32 h-3 rounded skeleton-shimmer-light"></div>
          </div>
        ))}
      </div>
    );
  }

  const quotaPercent = Math.round((stats.kaspersky_quota_used / (stats.kaspersky_quota_total || 1)) * 100);

  const cards = [
    {
      title: 'Pelanggan Terlindungi',
      value: `${stats.active_subscribers} / ${stats.total_subscribers}`,
      subtitle: `${stats.expired_subscribers} akun masa aktif berakhir`,
      icon: Users,
      color: 'text-[#10b981]',
      iconBg: 'bg-[#e8f5e9] border-[#a7f3d0]',
      badgeBorder: 'border-l-4 border-l-[#10b981]',
    },
    {
      title: 'Kaspersky B2B Licenses',
      value: `${stats.kaspersky_quota_used} / ${stats.kaspersky_quota_total}`,
      subtitle: `${quotaPercent}% kuota korporasi terpakai`,
      icon: KeyRound,
      color: 'text-[#006490]',
      iconBg: 'bg-[#d0d9fd] border-[#bdc5e9]',
      badgeBorder: 'border-l-4 border-l-[#006490]',
    },
    {
      title: 'Ancaman Dicegah Hari Ini',
      value: stats.threats_today.toString(),
      subtitle: `${stats.total_threats_blocked} total (Judol, SMS Scam, Malware)`,
      icon: ShieldAlert,
      color: 'text-[#be001c]',
      iconBg: 'bg-[#ffdad6] border-[#e9bcb8]',
      badgeBorder: 'border-l-4 border-l-[#ed0226]',
    },
    {
      title: 'Skor Keamanan Armada',
      value: `${stats.average_security_score}%`,
      subtitle: 'BlackWall RASP + Anti-Phishing aktif',
      icon: Award,
      color: 'text-[#f59e0b]',
      iconBg: 'bg-[#fff8e1] border-[#fde68a]',
      badgeBorder: 'border-l-4 border-l-[#f59e0b]',
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      {cards.map((card, idx) => {
        const Icon = card.icon;
        return (
          <div
            key={idx}
            className={`vigilance-card vigilance-card-hover p-5 rounded-xl relative overflow-hidden ${card.badgeBorder}`}
          >
            <div className="flex items-center justify-between mb-2">
              <span className="text-[11px] font-bold uppercase tracking-wider text-[#778ca2]">
                {card.title}
              </span>
              <div className={`p-2 rounded-lg border ${card.iconBg} ${card.color}`}>
                <Icon className="w-4 h-4" />
              </div>
            </div>
            <div className="text-2xl font-bold tracking-tight text-[#0b132b] mb-1">
              {card.value}
            </div>
            <div className="text-xs text-[#778ca2] font-medium">
              {card.subtitle}
            </div>
          </div>
        );
      })}
    </div>
  );
};
