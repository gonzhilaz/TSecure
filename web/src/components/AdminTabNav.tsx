'use client';

import React from 'react';
import { Shield, Headphones, Smartphone, Cpu, FileText } from 'lucide-react';

export type DashboardTab = 'overview' | 'helpdesk' | 'device_integrity' | 'ndp_simulator' | 'reports';

interface AdminTabNavProps {
  activeTab: DashboardTab;
  onTabChange: (tab: DashboardTab) => void;
  smsFailedCount?: number;
  desyncCount?: number;
  rootedCount?: number;
  simSwapCount?: number;
}

export const AdminTabNav: React.FC<AdminTabNavProps> = ({
  activeTab,
  onTabChange,
  smsFailedCount = 0,
  desyncCount = 0,
  rootedCount = 0,
  simSwapCount = 0,
}) => {
  const helpdeskIssues = smsFailedCount + desyncCount;
  const integrityIssues = rootedCount + simSwapCount;

  const tabs: {
    id: DashboardTab;
    label: string;
    description: string;
    icon: React.ComponentType<{ className?: string }>;
    badgeCount?: number;
    badgeColor?: string;
  }[] = [
    {
      id: 'overview',
      label: 'SOC & Telemetri',
      description: 'Live telemetry & threat intelligence',
      icon: Shield,
    },
    {
      id: 'helpdesk',
      label: 'Customer Care & Lisensi',
      description: 'Pencarian unik, masa aktif, & lisensi',
      icon: Headphones,
      badgeCount: helpdeskIssues,
      badgeColor: 'bg-[#ed0226] text-white',
    },
    {
      id: 'device_integrity',
      label: 'Device Integrity & SIM',
      description: 'Root, tamper, & SIM swap audit',
      icon: Smartphone,
      badgeCount: integrityIssues,
      badgeColor: 'bg-[#f59e0b] text-white',
    },
    {
      id: 'reports',
      label: 'Laporan & Ekspor Audit',
      description: 'Download CSV, Excel, & PDF resmi',
      icon: FileText,
    },
    {
      id: 'ndp_simulator',
      label: 'NDP & Billing Simulator',
      description: 'Simulasi pembelian & expired paket',
      icon: Cpu,
    },
  ];

  return (
    <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl p-2 shadow-sm mb-6">
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-2">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className={`flex items-start gap-3 p-3.5 rounded-xl text-left transition-all duration-200 border ${
                isActive
                  ? 'bg-[#fff0ef] border-[#ed0226] shadow-sm text-[#0b132b]'
                  : 'bg-white hover:bg-[#fff8f7] border-transparent text-[#5e3f3c]'
              }`}
            >
              <div
                className={`p-2.5 rounded-lg shrink-0 mt-0.5 ${
                  isActive
                    ? 'bg-[#ed0226] text-white shadow-sm'
                    : 'bg-[#ffe9e7] text-[#be001c]'
                }`}
              >
                <Icon className="w-5 h-5" />
              </div>

              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between gap-1">
                  <span
                    className={`text-sm font-semibold truncate ${
                      isActive ? 'text-[#be001c]' : 'text-[#0b132b]'
                    }`}
                  >
                    {tab.label}
                  </span>
                  {tab.badgeCount !== undefined && tab.badgeCount > 0 && (
                    <span
                      className={`text-[10px] font-bold px-1.5 py-0.5 rounded-full ${tab.badgeColor} animate-pulse shrink-0`}
                    >
                      {tab.badgeCount}
                    </span>
                  )}
                </div>
                <p className="text-xs text-[#778ca2] mt-0.5 line-clamp-1">
                  {tab.description}
                </p>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
};
