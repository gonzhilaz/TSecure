'use client';

import React, { useEffect, useState } from 'react';
import {
  Shield,
  Radio,
  Activity,
  RefreshCw,
  LogOut,
  User,
  Volume2,
  VolumeX,
  Trash2,
  Menu,
  RotateCcw,
} from 'lucide-react';
import { SOCOperator } from '@/lib/auth';
import { isSoundEnabled, setSoundEnabled } from '@/lib/soundAlert';

interface HeaderProps {
  isConnected: boolean;
  onRefresh: () => void;
  isRefreshing: boolean;
  onClearData?: () => void;
  onResetMockData?: () => void;
  operator?: SOCOperator | null;
  onLogout?: () => void;
  onToggleMobileMenu?: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  isConnected,
  onRefresh,
  isRefreshing,
  onClearData,
  onResetMockData,
  operator,
  onLogout,
  onToggleMobileMenu,
}) => {
  const [timeStr, setTimeStr] = useState<string>('');
  const [soundActive, setSoundActive] = useState<boolean>(() => {
    if (typeof window !== 'undefined') {
      return isSoundEnabled();
    }
    return true;
  });

  const handleToggleSound = () => {
    const next = !soundActive;
    setSoundActive(next);
    setSoundEnabled(next);
  };

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setTimeStr(
        now.toLocaleTimeString('id-ID', {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          timeZoneName: 'short',
        })
      );
    };
    updateTime();
    const interval = setInterval(updateTime, 1000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="sticky top-0 z-30 w-full bg-white/95 backdrop-blur-md border-b border-[#e9bcb8]/60 px-4 sm:px-6 py-3 shadow-xs">
      <div className="flex items-center justify-between">
        {/* Left: Mobile Toggle & Title */}
        <div className="flex items-center space-x-3">
          {onToggleMobileMenu && (
            <button
              onClick={onToggleMobileMenu}
              className="lg:hidden p-2 rounded-xl text-[#4a5568] hover:bg-[#fff0f0] transition-colors"
              aria-label="Buka Menu"
            >
              <Menu className="w-5 h-5 text-[#be001c]" />
            </button>
          )}

          {/* Mobile Only Logo (shown only when sidebar is hidden) */}
          <div className="flex lg:hidden items-center space-x-2">
            <div className="flex items-center justify-center w-7 h-7 rounded-lg bg-gradient-to-br from-[#be001c] to-[#ed0226] text-white shadow-xs">
              <Shield className="w-3.5 h-3.5" />
            </div>
            <span className="font-extrabold tracking-tight text-sm text-[#0b132b]">
              TELKOMSEL <span className="text-[#ed0226]">SECURE</span>
            </span>
          </div>

          {/* Desktop Operational Header (Zero duplicate brand logo) */}
          <div className="hidden lg:flex items-center space-x-2.5">
            <span className="text-sm font-bold text-[#0b132b]">
              Security Operations Center
            </span>
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-[#ffe9e7] text-[#be001c] border border-[#e9bcb8] font-bold">
              SOC v2.4
            </span>
          </div>
        </div>

        {/* Right: Controls & Actions */}
        <div className="flex items-center space-x-2.5">
          {/* SSE Status */}
          <div
            className={`flex items-center space-x-1.5 px-2.5 py-1 rounded-lg border text-[11px] font-bold ${
              isConnected
                ? 'bg-[#e8f5e9] border-[#a7f3d0] text-[#10b981]'
                : 'bg-[#fff8e1] border-[#fde68a] text-[#f59e0b]'
            }`}
          >
            <Radio
              className={`w-3 h-3 ${
                isConnected ? 'text-[#10b981] animate-pulse' : 'text-[#f59e0b] animate-spin'
              }`}
            />
            <span className="hidden sm:inline">Stream:</span>
            <span>{isConnected ? 'LIVE' : 'OFFLINE'}</span>
          </div>

          {/* Clock */}
          <div className="hidden md:flex items-center space-x-1.5 px-2.5 py-1 rounded-lg bg-[#f8fafc] border border-[#e2e8f0] text-[11px] text-[#475569] font-mono">
            <Activity className="w-3 h-3 text-[#0284c7]" />
            <span>{timeStr || '...'}</span>
          </div>

          {/* Audio Alert Toggle */}
          <button
            onClick={handleToggleSound}
            className={`p-1.5 rounded-lg border text-xs transition-all active:scale-95 ${
              soundActive
                ? 'bg-[#fff8e1] border-[#fde68a] text-[#b45309]'
                : 'bg-[#f1f5f9] border-[#cbd5e1] text-[#94a3b8]'
            }`}
            title={soundActive ? 'Suara Alert: AKTIF' : 'Suara Alert: MATI'}
          >
            {soundActive ? <Volume2 className="w-3.5 h-3.5" /> : <VolumeX className="w-3.5 h-3.5" />}
          </button>

          {/* Refresh Button */}
          <button
            onClick={onRefresh}
            disabled={isRefreshing}
            className="flex items-center space-x-1 px-2.5 py-1 rounded-lg bg-white hover:bg-[#fff5f5] border border-[#e2e8f0] text-xs font-bold text-[#0b132b] transition-all active:scale-95 disabled:opacity-50"
            title="Sinkronisasi data"
          >
            <RefreshCw className={`w-3 h-3 text-[#ed0226] ${isRefreshing ? 'animate-spin' : ''}`} />
            <span className="hidden sm:inline">Sync</span>
          </button>

          {/* Reset Mock Data */}
          {onResetMockData && (
            <button
              onClick={onResetMockData}
              className="flex items-center space-x-1.5 px-2.5 py-1 rounded-lg bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 text-xs font-bold text-emerald-700 transition-all active:scale-95 shadow-xs"
              title="Muat ulang seluruh mock data standar POC (subscribers, ancaman & lisensi)"
            >
              <RotateCcw className="w-3 h-3 text-emerald-600" />
              <span className="hidden md:inline">Muat Mock Data</span>
            </button>
          )}

          {/* Clear Data */}
          {onClearData && (
            <button
              onClick={() => {
                if (window.confirm('Apakah Anda yakin ingin mengosongkan seluruh data dashboard?')) {
                  onClearData();
                }
              }}
              className="flex items-center space-x-1 px-2.5 py-1 rounded-lg bg-white hover:bg-[#fff0f0] border border-[#e2e8f0] text-xs font-semibold text-[#dc2626] transition-all active:scale-95"
              title="Bersihkan riwayat pengujian"
            >
              <Trash2 className="w-3 h-3" />
              <span className="hidden md:inline">Kosongkan</span>
            </button>
          )}

          {/* Operator Logout */}
          {operator && onLogout && (
            <button
              onClick={onLogout}
              className="flex items-center space-x-1 px-2.5 py-1 rounded-lg bg-[#fff0f0] hover:bg-[#fee2e2] text-xs font-bold text-[#ed0226] transition-all active:scale-95"
              title="Logout akun"
            >
              <LogOut className="w-3 h-3" />
              <span className="hidden sm:inline">Keluar</span>
            </button>
          )}
        </div>
      </div>
    </header>
  );
};
