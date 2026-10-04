'use client';

import React, { useMemo } from 'react';
import { ThreatEvent } from '@/types';
import { ShieldAlert, Dices, Smartphone, Globe, MapPin, ChevronRight } from 'lucide-react';

interface ThreatProvinceRankingProps {
  threats: ThreatEvent[];
  selectedCity?: string;
  onSelectCity: (city: string) => void;
}

export const ThreatProvinceRanking: React.FC<ThreatProvinceRankingProps> = ({
  threats,
  selectedCity,
  onSelectCity,
}) => {
  // Aggregate threat counts by type
  const metrics = useMemo(() => {
    let judolCount = 0;
    let smishingCount = 0;
    let phishingCount = 0;
    let malwareCount = 0;

    threats.forEach((t) => {
      const type = t.threat_type?.toUpperCase();
      if (type === 'JUDI_ONLINE') judolCount++;
      else if (type === 'SMISHING') smishingCount++;
      else if (type === 'PHISHING') phishingCount++;
      else if (type === 'MALWARE' || type === 'EICAR') malwareCount++;
    });

    return {
      total: threats.length,
      judol: judolCount,
      smishing: smishingCount,
      phishing: phishingCount,
      malware: malwareCount,
    };
  }, [threats]);

  // Aggregate threats by city/province
  const cityRankings = useMemo(() => {
    const map = new Map<string, { count: number; judol: number; smishing: number }>();

    threats.forEach((t) => {
      const city = t.city || 'Lainnya';
      const cur = map.get(city) || { count: 0, judol: 0, smishing: 0 };
      cur.count++;
      if (t.threat_type === 'JUDI_ONLINE') cur.judol++;
      if (t.threat_type === 'SMISHING') cur.smishing++;
      map.set(city, cur);
    });

    return Array.from(map.entries())
      .map(([name, data]) => ({
        name,
        count: data.count,
        judol: data.judol,
        smishing: data.smishing,
        pct: threats.length > 0 ? Math.round((data.count / threats.length) * 100) : 0,
      }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 5);
  }, [threats]);

  return (
    <div className="space-y-4">
      {/* 4 Summary Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="bg-white rounded-xl p-3.5 border border-[#e2e8f0] shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-semibold text-[#64748b]">Total Terpetakan</span>
            <div className="w-6 h-6 rounded-lg bg-[#f1f5f9] flex items-center justify-center text-[#475569]">
              <ShieldAlert className="w-3.5 h-3.5" />
            </div>
          </div>
          <div className="text-xl font-bold text-[#0b132b] mt-1">{metrics.total}</div>
          <span className="text-[10px] text-[#94a3b8]">Titik koordinat aktif</span>
        </div>

        <div className="bg-white rounded-xl p-3.5 border border-[#fed7aa] bg-[#fffaf5] shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-semibold text-[#c2410c]">Judi Online</span>
            <div className="w-6 h-6 rounded-lg bg-[#ffedd5] flex items-center justify-center text-[#ea580c]">
              <Dices className="w-3.5 h-3.5" />
            </div>
          </div>
          <div className="text-xl font-bold text-[#ea580c] mt-1">{metrics.judol}</div>
          <span className="text-[10px] text-[#fb923c]">Promosi slot dicegat</span>
        </div>

        <div className="bg-white rounded-xl p-3.5 border border-[#fecaca] bg-[#fff8f8] shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-semibold text-[#dc2626]">SMS Scam & APK</span>
            <div className="w-6 h-6 rounded-lg bg-[#fee2e2] flex items-center justify-center text-[#dc2626]">
              <Smartphone className="w-3.5 h-3.5" />
            </div>
          </div>
          <div className="text-xl font-bold text-[#dc2626] mt-1">{metrics.smishing}</div>
          <span className="text-[10px] text-[#f87171]">Malware kurir / tilang</span>
        </div>

        <div className="bg-white rounded-xl p-3.5 border border-[#e9d5ff] bg-[#faf5ff] shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-semibold text-[#7e22ce]">Web Phishing</span>
            <div className="w-6 h-6 rounded-lg bg-[#f3e8ff] flex items-center justify-center text-[#9333ea]">
              <Globe className="w-3.5 h-3.5" />
            </div>
          </div>
          <div className="text-xl font-bold text-[#9333ea] mt-1">{metrics.phishing}</div>
          <span className="text-[10px] text-[#c084fc]">Situs web diblokir</span>
        </div>
      </div>

      {/* Top 5 City / Province Ranking Card */}
      <div className="bg-white rounded-2xl p-4 border border-[#e2e8f0] shadow-sm">
        <div className="flex items-center justify-between pb-3 border-b border-[#f1f5f9]">
          <div className="flex items-center space-x-2">
            <MapPin className="w-4 h-4 text-[#ed0226]" />
            <h3 className="text-xs font-bold text-[#0b132b] uppercase tracking-wider">
              Top 5 Wilayah Rawan Ancaman
            </h3>
          </div>
          <span className="text-[10px] text-[#94a3b8]">Klik untuk perbesar peta</span>
        </div>

        <div className="mt-3 space-y-2.5">
          {cityRankings.map((item, idx) => {
            const isSelected = selectedCity?.toLowerCase() === item.name.toLowerCase();
            return (
              <div
                key={item.name}
                onClick={() => onSelectCity(item.name)}
                className={`p-2.5 rounded-xl border transition-all cursor-pointer flex items-center justify-between ${
                  isSelected
                    ? 'border-[#ed0226] bg-[#fff5f5]'
                    : 'border-[#f1f5f9] hover:border-[#cbd5e1] hover:bg-[#f8fafc]'
                }`}
              >
                <div className="flex items-center space-x-2.5 min-w-[120px]">
                  <span className="w-5 h-5 rounded-full bg-[#f1f5f9] text-[11px] font-bold text-[#475569] flex items-center justify-center">
                    {idx + 1}
                  </span>
                  <div>
                    <span className="text-xs font-bold text-[#0b132b] block">{item.name}</span>
                    <span className="text-[10px] text-[#64748b]">
                      {item.judol} Judol • {item.smishing} SMS Scam
                    </span>
                  </div>
                </div>

                <div className="flex-1 max-w-[140px] mx-3 hidden sm:block">
                  <div className="w-full bg-[#f1f5f9] h-2 rounded-full overflow-hidden">
                    <div
                      className="bg-gradient-to-r from-[#ea580c] to-[#ed0226] h-full rounded-full"
                      style={{ width: `${item.pct}%` }}
                    />
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <span className="text-xs font-bold text-[#ed0226]">{item.count}</span>
                  <span className="text-[10px] text-[#94a3b8]">({item.pct}%)</span>
                  <ChevronRight className="w-3.5 h-3.5 text-[#cbd5e1]" />
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
