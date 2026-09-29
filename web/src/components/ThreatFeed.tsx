'use client';

import React from 'react';
import { ShieldAlert, Globe, Bug, Cpu, Terminal, CheckCircle } from 'lucide-react';
import { ThreatEvent } from '@/types';

interface ThreatFeedProps {
  threats: ThreatEvent[];
  loading: boolean;
}

export const ThreatFeed: React.FC<ThreatFeedProps> = ({ threats, loading }) => {
  const getThreatIcon = (type: string) => {
    switch (type.toUpperCase()) {
      case 'PHISHING':
        return <Globe className="w-4 h-4 text-[#f59e0b]" />;
      case 'MALWARE':
      case 'EICAR':
        return <Bug className="w-4 h-4 text-[#ed0226]" />;
      case 'SIM_WATCH':
        return <Cpu className="w-4 h-4 text-[#007eb4]" />;
      case 'RASP':
        return <Terminal className="w-4 h-4 text-[#545d7c]" />;
      default:
        return <ShieldAlert className="w-4 h-4 text-[#ed0226]" />;
    }
  };

  const getSeverityBadge = (severity: string) => {
    switch (severity.toUpperCase()) {
      case 'CRITICAL':
        return 'bg-[#ffdad6] text-[#be001c] border-[#e9bcb8]';
      case 'HIGH':
        return 'bg-[#fff8e1] text-[#f59e0b] border-[#fde68a]';
      case 'MEDIUM':
        return 'bg-[#d0d9fd] text-[#006490] border-[#bdc5e9]';
      default:
        return 'bg-[#f4f6f9] text-[#5e3f3c] border-[#e2e8f0]';
    }
  };

  return (
    <div className="vigilance-card p-5 rounded-xl flex flex-col h-full">
      <div className="flex items-center justify-between mb-4 pb-3 border-b border-[#e2e8f0]">
        <div className="flex items-center space-x-2">
          <ShieldAlert className="w-5 h-5 text-[#ed0226] animate-pulse" />
          <h2 className="text-sm font-bold text-[#0b132b] uppercase tracking-wider">
            Live Threat Stream (Real-Time SOC Feed)
          </h2>
        </div>
        <span className="text-xs px-2.5 py-0.5 rounded-full bg-[#fff0ef] border border-[#e9bcb8] text-[#be001c] font-bold font-mono">
          {threats.length} Events
        </span>
      </div>

      <div className="flex-1 overflow-y-auto max-h-[420px] space-y-2.5 pr-1">
        {loading ? (
          [1, 2, 3, 4].map((i) => (
            <div key={i} className="p-3 rounded-lg bg-[#fff8f7] border border-[#e2e8f0] flex items-center space-x-3">
              <div className="w-8 h-8 rounded-lg skeleton-shimmer-light"></div>
              <div className="flex-1 space-y-1.5">
                <div className="w-3/4 h-3.5 rounded skeleton-shimmer-light"></div>
                <div className="w-1/2 h-2.5 rounded skeleton-shimmer-light"></div>
              </div>
            </div>
          ))
        ) : threats.length === 0 ? (
          <div className="py-12 text-center">
            <CheckCircle className="w-10 h-10 text-[#10b981]/70 mx-auto mb-2" />
            <p className="text-sm font-bold text-[#0b132b]">Perimeter Aman</p>
            <p className="text-xs text-[#778ca2] mt-1 max-w-xs mx-auto">
              Tidak ada ancaman terdeteksi. Lakukan uji coba EICAR/Phishing dari Security Test Lab di HP.
            </p>
          </div>
        ) : (
          threats.map((t, idx) => (
            <div
              key={t.id || idx}
              className={`p-3 rounded-lg border transition-all ${
                idx === 0
                  ? 'bg-[#fff0ef] border-[#ed0226]/40 shadow-xs'
                  : 'bg-white hover:bg-[#fff8f7] border-[#e2e8f0]'
              }`}
            >
              <div className="flex items-start justify-between gap-2 mb-1.5">
                <div className="flex items-center space-x-2">
                  <div className="p-1.5 rounded-md bg-[#f4f6f9] border border-[#e2e8f0]">
                    {getThreatIcon(t.threat_type)}
                  </div>
                  <div>
                    <span className="text-xs font-bold text-[#0b132b] tracking-tight">
                      {t.threat_type}
                    </span>
                    <span className="text-[11px] text-[#778ca2] font-mono ml-2">
                      {t.msisdn}
                    </span>
                  </div>
                </div>
                <span
                  className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${getSeverityBadge(
                    t.severity
                  )}`}
                >
                  {t.severity}
                </span>
              </div>

              <div className="text-[11px] text-[#2a1615] break-all mb-1 font-mono bg-[#f4f6f9] p-1.5 rounded-md border border-[#e2e8f0]">
                {t.target}
              </div>

              <div className="flex items-center justify-between text-[11px]">
                <span className="text-[#10b981] font-semibold flex items-center space-x-1">
                  <CheckCircle className="w-3.5 h-3.5 text-[#10b981] inline" />
                  <span>{t.action_taken}</span>
                </span>
                <span className="font-mono text-[10px] text-[#778ca2]">
                  {new Date(t.timestamp).toLocaleTimeString('id-ID')}
                </span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
