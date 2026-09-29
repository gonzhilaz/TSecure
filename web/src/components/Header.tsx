'use client';

import React, { useEffect, useState } from 'react';
import { Shield, Radio, Activity, RefreshCw } from 'lucide-react';

interface HeaderProps {
  isConnected: boolean;
  onRefresh: () => void;
  isRefreshing: boolean;
}

export const Header: React.FC<HeaderProps> = ({ isConnected, onRefresh, isRefreshing }) => {
  const [timeStr, setTimeStr] = useState<string>('');

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
        </div>
      </div>
    </header>
  );
};
