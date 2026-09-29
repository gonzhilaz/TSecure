'use client';

import React, { useEffect, useState } from 'react';
import { Shield, Radio, Activity, RefreshCw, LogOut, User, Volume2, VolumeX } from 'lucide-react';
import { SOCOperator } from '@/lib/auth';
import { isSoundEnabled, setSoundEnabled } from '@/lib/soundAlert';

interface HeaderProps {
  isConnected: boolean;
  onRefresh: () => void;
  isRefreshing: boolean;
  operator?: SOCOperator | null;
  onLogout?: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  isConnected,
  onRefresh,
  isRefreshing,
  operator,
  onLogout,
}) => {
  const [timeStr, setTimeStr] = useState<string>('');
  const [soundActive, setSoundActive] = useState<boolean>(true);

  useEffect(() => {
    setSoundActive(isSoundEnabled());
  }, []);

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
    <header className="sticky top-0 z-50 w-full bg-white/95 backdrop-blur-md border-b border-[#e9bcb8]/60 px-6 py-3.5 mb-6 shadow-sm shadow-[#0b132b]/5">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        {/* Left: Branding */}
        <div className="flex items-center space-x-3.5">
          <div className="relative flex items-center justify-center w-10 h-10 rounded-xl bg-gradient-to-br from-[#be001c] to-[#ed0226] shadow-md shadow-[#be001c]/25">
            <Shield className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <span className="font-bold tracking-tight text-lg text-[#0b132b]">
                TELKOMSEL <span className="text-[#ed0226]">SECURE</span>
              </span>
              <span className="text-[11px] px-2 py-0.5 rounded-full bg-[#ffe9e7] text-[#be001c] border border-[#e9bcb8] font-bold tracking-wide">
                SOC v2.4
              </span>
            </div>
            <p className="text-xs text-[#778ca2] font-medium tracking-tight">
              Security Operations Center • Kaspersky Mobile Security Engine
            </p>
          </div>
        </div>

        {/* Right: Live Telemetry Controls */}
        <div className="flex items-center space-x-3">
          {/* SSE Stream Status */}
          <div
            className={`hidden sm:flex items-center space-x-2 px-3 py-1.5 rounded-lg border text-xs font-semibold ${
              isConnected
                ? 'bg-[#e8f5e9] border-[#a7f3d0] text-[#10b981]'
                : 'bg-[#fff8e1] border-[#fde68a] text-[#f59e0b]'
            }`}
          >
            <Radio
              className={`w-3.5 h-3.5 ${
                isConnected ? 'text-[#10b981] animate-pulse' : 'text-[#f59e0b] animate-spin'
              }`}
            />
            <span className="text-[#5e3f3c]">Stream:</span>
            <span>{isConnected ? 'LIVE SSE' : 'RECONNECTING'}</span>
          </div>

          {/* Clock */}
          <div className="hidden md:flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-[#f4f6f9] border border-[#e2e8f0] text-xs text-[#3a405a] font-mono">
            <Activity className="w-3.5 h-3.5 text-[#007eb4]" />
            <span>{timeStr || 'Loading...'}</span>
          </div>

          {/* Sound Alert Toggle */}
          <button
            onClick={handleToggleSound}
            className={`p-2 rounded-lg border text-xs transition-all active:scale-95 shadow-xs ${
              soundActive
                ? 'bg-amber-50 hover:bg-amber-100 border-amber-200 text-amber-700'
                : 'bg-slate-50 hover:bg-slate-100 border-slate-200 text-slate-400'
            }`}
            title={soundActive ? 'Audio Alert Ancaman: AKTIF' : 'Audio Alert Ancaman: MATI'}
          >
            {soundActive ? <Volume2 className="w-3.5 h-3.5" /> : <VolumeX className="w-3.5 h-3.5" />}
          </button>

          {/* Refresh Action */}
          <button
            onClick={onRefresh}
            disabled={isRefreshing}
            className="flex items-center space-x-1.5 px-3.5 py-1.5 rounded-lg bg-white hover:bg-[#fff0ef] border border-[#e2e8f0] hover:border-[#e9bcb8] text-xs font-bold text-[#0b132b] transition-all active:scale-95 disabled:opacity-50 shadow-xs"
            title="Refresh dashboard data"
          >
            <RefreshCw className={`w-3.5 h-3.5 text-[#5e3f3c] ${isRefreshing ? 'animate-spin' : ''}`} />
            <span>Sync</span>
          </button>

          {/* Operator Profile & Logout */}
          {operator && (
            <div className="flex items-center pl-2 ml-1 border-l border-[#e2e8f0] space-x-2">
              <div className="hidden lg:flex flex-col text-right">
                <span className="text-xs font-bold text-[#0b132b] leading-tight flex items-center justify-end gap-1">
                  <User className="w-3 h-3 text-[#be001c]" />
                  {operator.name}
                </span>
                <span className="text-[10px] text-[#778ca2] font-semibold">
                  {operator.role}
                </span>
              </div>
              <button
                onClick={onLogout}
                className="flex items-center space-x-1 px-2.5 py-1.5 rounded-lg bg-red-50 hover:bg-red-100 border border-red-200 text-xs font-bold text-[#be001c] transition-all active:scale-95 shadow-xs"
                title="Keluar dari akun SOC"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Logout</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
