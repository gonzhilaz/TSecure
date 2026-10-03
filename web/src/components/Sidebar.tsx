'use client';

import React from 'react';
import {
  Shield,
  MapPin,
  Globe,
  Smartphone,
  Cpu,
  Server,
  Database,
  Users,
  FileText,
  Radio,
  ChevronLeft,
  ChevronRight,
  LogOut,
  User,
  X,
} from 'lucide-react';
import { DashboardTab } from '@/types';
import { SOCOperator } from '@/lib/auth';

interface SidebarProps {
  activeTab: DashboardTab;
  onTabChange: (tab: DashboardTab) => void;
  collapsed: boolean;
  onToggleCollapse: () => void;
  mobileOpen: boolean;
  onCloseMobile: () => void;
  operator?: SOCOperator | null;
  onLogout?: () => void;
  isConnected: boolean;
  activePhishingCount?: number;
  pendingDlqCount?: number;
  integrityIssuesCount?: number;
  helpdeskIssuesCount?: number;
}

interface NavItem {
  id: DashboardTab;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  badge?: number;
  badgeColor?: string;
}

interface NavGroup {
  title: string;
  items: NavItem[];
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab,
  onTabChange,
  collapsed,
  onToggleCollapse,
  mobileOpen,
  onCloseMobile,
  operator,
  onLogout,
  isConnected,
  activePhishingCount = 0,
  pendingDlqCount = 0,
  integrityIssuesCount = 0,
  helpdeskIssuesCount = 0,
}) => {
  const groups: NavGroup[] = [
    {
      title: 'MONITORING',
      items: [
        { id: 'overview', label: 'Ringkasan', icon: Shield },
        { id: 'threat_map', label: 'Peta Lokasi', icon: MapPin },
        {
          id: 'phishing_intel',
          label: 'Intel Phishing',
          icon: Globe,
          badge: activePhishingCount,
          badgeColor: 'bg-[#ed0226] text-white',
        },
      ],
    },
    {
      title: 'ARMADA',
      items: [
        {
          id: 'helpdesk',
          label: 'Armada HP',
          icon: Smartphone,
          badge: helpdeskIssuesCount,
          badgeColor: 'bg-[#ed0226] text-white',
        },
        {
          id: 'device_integrity',
          label: 'Integritas SIM',
          icon: Cpu,
          badge: integrityIssuesCount,
          badgeColor: 'bg-[#f59e0b] text-white',
        },
      ],
    },
    {
      title: 'INFRASTRUKTUR',
      items: [
        {
          id: 'ingestion_dlq',
          label: 'Gateway Ingestion',
          icon: Server,
          badge: pendingDlqCount,
          badgeColor: 'bg-[#dc2626] text-white',
        },
        { id: 'db_maintenance', label: 'Siklus Database', icon: Database },
      ],
    },
    {
      title: 'ADMINISTRASI',
      items: [
        { id: 'users_rbac', label: 'Manajemen User', icon: Users },
        { id: 'reports', label: 'Laporan Audit', icon: FileText },
        { id: 'ndp_simulator', label: 'Simulator NDP', icon: Radio },
      ],
    },
  ];

  const handleSelect = (tab: DashboardTab) => {
    onTabChange(tab);
    if (mobileOpen) {
      onCloseMobile();
    }
  };

  return (
    <>
      {/* Mobile Backdrop */}
      {mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm lg:hidden transition-opacity"
          onClick={onCloseMobile}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed top-0 bottom-0 left-0 z-50 flex flex-col bg-white border-r border-[#e9bcb8]/60 transition-all duration-300 shadow-sm ${
          collapsed ? 'w-20' : 'w-64'
        } ${
          mobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        {/* Top Header / Branding */}
        <div className="h-16 flex items-center justify-between px-4 border-b border-[#e9bcb8]/40">
          <div className="flex items-center space-x-3 overflow-hidden">
            <div className="flex-shrink-0 flex items-center justify-center w-10 h-10 rounded-xl bg-gradient-to-br from-[#be001c] to-[#ed0226] text-white shadow-md shadow-[#be001c]/25">
              <Shield className="w-5 h-5" />
            </div>
            {!collapsed && (
              <div className="min-w-0">
                <div className="flex items-center space-x-1.5">
                  <span className="font-extrabold tracking-tight text-sm text-[#0b132b]">
                    TELKOMSEL
                  </span>
                  <span className="text-[#ed0226] font-extrabold text-sm">
                    SECURE
                  </span>
                </div>
                <div className="text-[10px] text-[#778ca2] font-semibold tracking-wider uppercase">
                  SOC COMMAND
                </div>
              </div>
            )}
          </div>

          {/* Desktop Collapse Toggle */}
          <button
            onClick={onToggleCollapse}
            aria-label={collapsed ? 'Perluas Menu' : 'Perkecil Menu'}
            className="hidden lg:flex items-center justify-center w-7 h-7 rounded-lg text-[#778ca2] hover:text-[#0b132b] hover:bg-[#fff0f0] transition-colors"
          >
            {collapsed ? (
              <ChevronRight className="w-4 h-4" />
            ) : (
              <ChevronLeft className="w-4 h-4" />
            )}
          </button>

          {/* Mobile Close Button */}
          <button
            onClick={onCloseMobile}
            className="lg:hidden p-1.5 rounded-lg text-[#778ca2] hover:bg-[#fff0f0]"
            aria-label="Tutup Menu"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Items (Scrollable) */}
        <div className="flex-1 overflow-y-auto px-3 py-4 space-y-6">
          {groups.map((group) => (
            <div key={group.title} className="space-y-1">
              {!collapsed && (
                <div className="px-3 pb-1 text-[10px] font-bold tracking-wider text-[#a0aec0] uppercase">
                  {group.title}
                </div>
              )}
              {group.items.map((item) => {
                const Icon = item.icon;
                const isActive = activeTab === item.id;
                return (
                  <button
                    key={item.id}
                    onClick={() => handleSelect(item.id)}
                    title={collapsed ? item.label : undefined}
                    className={`w-full flex items-center px-3 py-2.5 rounded-xl text-xs font-semibold transition-all group ${
                      isActive
                        ? 'bg-gradient-to-r from-[#ed0226] to-[#be001c] text-white shadow-sm shadow-[#ed0226]/30'
                        : 'text-[#4a5568] hover:bg-[#fff5f5] hover:text-[#ed0226]'
                    } ${collapsed ? 'justify-center' : 'justify-between'}`}
                  >
                    <div className="flex items-center space-x-3 min-w-0">
                      <Icon
                        className={`w-4 h-4 flex-shrink-0 transition-transform group-hover:scale-110 ${
                          isActive ? 'text-white' : 'text-[#718096] group-hover:text-[#ed0226]'
                        }`}
                      />
                      {!collapsed && (
                        <span className="truncate">{item.label}</span>
                      )}
                    </div>

                    {!collapsed && item.badge !== undefined && item.badge > 0 && (
                      <span
                        className={`text-[10px] font-bold px-1.5 py-0.5 rounded-full ${
                          item.badgeColor || 'bg-[#ed0226] text-white'
                        }`}
                      >
                        {item.badge}
                      </span>
                    )}
                  </button>
                );
              })}
            </div>
          ))}
        </div>

        {/* Footer / User Profile & Logout */}
        <div className="p-3 border-t border-[#e9bcb8]/40 bg-[#fffaf9]/80 space-y-2">
          {/* Connection status indicator */}
          <div
            className={`flex items-center rounded-lg px-2.5 py-1 text-[11px] font-semibold border ${
              isConnected
                ? 'bg-[#e8f5e9] border-[#a7f3d0] text-[#10b981]'
                : 'bg-[#fff8e1] border-[#fde68a] text-[#f59e0b]'
            } ${collapsed ? 'justify-center' : 'space-x-2'}`}
          >
            <Radio
              className={`w-3 h-3 flex-shrink-0 ${
                isConnected ? 'text-[#10b981] animate-pulse' : 'text-[#f59e0b] animate-spin'
              }`}
            />
            {!collapsed && (
              <span className="truncate">
                {isConnected ? 'Stream: AKTIF' : 'Menghubungkan...'}
              </span>
            )}
          </div>

          {/* User profile */}
          {operator && (
            <div
              className={`flex items-center rounded-xl p-2 bg-white border border-[#e2e8f0] ${
                collapsed ? 'justify-center' : 'justify-between'
              }`}
            >
              <div className="flex items-center space-x-2 min-w-0">
                <div className="w-7 h-7 rounded-lg bg-[#ffe9e7] flex items-center justify-center text-[#ed0226] font-bold text-xs flex-shrink-0">
                  <User className="w-4 h-4" />
                </div>
                {!collapsed && (
                  <div className="min-w-0">
                    <div className="text-xs font-bold text-[#0b132b] truncate">
                      {operator.name}
                    </div>
                    <div className="text-[10px] text-[#718096] truncate">
                      {operator.role}
                    </div>
                  </div>
                )}
              </div>

              {!collapsed && onLogout && (
                <button
                  onClick={onLogout}
                  title="Logout"
                  className="p-1 rounded-lg text-[#718096] hover:text-[#ed0226] hover:bg-[#fff0f0] transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              )}
            </div>
          )}
        </div>
      </aside>
    </>
  );
};
