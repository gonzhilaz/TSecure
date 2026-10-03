'use client';

import React from 'react';
import { Shield, Headphones, Smartphone, Cpu, FileText, Globe, Users, Server, Database } from 'lucide-react';

export type DashboardTab =
  | 'overview'
  | 'phishing_intel'
  | 'helpdesk'
  | 'device_integrity'
  | 'ingestion_dlq'
  | 'users_rbac'
  | 'db_maintenance'
  | 'reports'
  | 'ndp_simulator';

interface AdminTabNavProps {
  activeTab: DashboardTab;
  onTabChange: (tab: DashboardTab) => void;
  smsFailedCount?: number;
  desyncCount?: number;
  rootedCount?: number;
  simSwapCount?: number;
  activePhishingCount?: number;
  pendingDlqCount?: number;
}

export const AdminTabNav: React.FC<AdminTabNavProps> = ({
  activeTab,
  onTabChange,
  smsFailedCount = 0,
  desyncCount = 0,
  rootedCount = 0,
  simSwapCount = 0,
  activePhishingCount = 0,
  pendingDlqCount = 0,
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
      description: 'Live telemetry & threat stream',
      icon: Shield,
    },
    {
      id: 'phishing_intel',
      label: 'Phishing Threat Intel',
      description: 'URL jahat & takedown CSIRT',
      icon: Globe,
      badgeCount: activePhishingCount,
      badgeColor: 'bg-[#ed0226] text-white',
    },
    {
      id: 'ingestion_dlq',
      label: 'Ingestion Gateway & DLQ',
      description: 'NDP, Proxy, & error mitigation',
      icon: Server,
      badgeCount: pendingDlqCount,
      badgeColor: 'bg-[#dc2626] text-white',
    },
    {
      id: 'users_rbac',
      label: 'User Management & RBAC',
      description: 'Operator, peran, & audit trail',
      icon: Users,
    },
    {
      id: 'db_maintenance',
      label: 'Database Lifecycle',
      description: 'Backup, snapshot, & retensi',
      icon: Database,
    },
    {
      id: 'helpdesk',
      label: 'Customer Care & Lisensi',
      description: 'Pencarian & masa aktif B2B',
      icon: Headphones,
      badgeCount: helpdeskIssues,
      badgeColor: 'bg-[#ed0226] text-white',
    },
    {
      id: 'device_integrity',
      label: 'Device Integrity & SIM',
      description: 'Root, tamper, & SIM watch',
      icon: Smartphone,
      badgeCount: integrityIssues,
      badgeColor: 'bg-[#f59e0b] text-white',
    },
    {
      id: 'reports',
      label: 'Laporan & Ekspor Audit',
      description: 'Download CSV, Excel, & PDF',
      icon: FileText,
    },
    {
      id: 'ndp_simulator',
      label: 'NDP Simulator',
      description: 'Simulasi pembelian & billing',
      icon: Cpu,
    },
  ];

  return (
    <div className="bg-white border border-[#e9bcb8]/80 rounded-2xl p-2 shadow-sm mb-6">
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-5 2xl:grid-cols-9 gap-2">
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
