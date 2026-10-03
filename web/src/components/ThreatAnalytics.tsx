'use client';

import React, { useEffect, useState, useMemo } from 'react';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  BarChart,
  Bar,
} from 'recharts';
import { ThreatEvent } from '@/types';
import { ShieldCheck, Wifi, AlertTriangle, ShieldAlert, Activity } from 'lucide-react';

interface ThreatAnalyticsProps {
  threats: ThreatEvent[];
  onSelectCity?: (city: string) => void;
}

const VECTOR_COLORS: Record<string, string> = {
  MALWARE: '#ed0226',
  PHISHING: '#f97316',
  WIFI: '#eab308',
  RASP: '#a855f7',
  SIM_WATCH: '#3b82f6',
};

export const ThreatAnalytics: React.FC<ThreatAnalyticsProps> = ({ threats, onSelectCity }) => {
  const [mounted, setMounted] = useState<boolean>(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  // 1. Vector Distribution Data
  const vectorData = useMemo(() => {
    const counts: Record<string, number> = {
      MALWARE: 0,
      PHISHING: 0,
      WIFI: 0,
      RASP: 0,
      SIM_WATCH: 0,
    };

    threats.forEach((t) => {
      if (t.threat_type === 'MALWARE' || t.threat_type === 'EICAR') counts.MALWARE++;
      else if (t.threat_type === 'PHISHING') counts.PHISHING++;
      else if (t.threat_type === 'WIFI') counts.WIFI++;
      else if (t.threat_type === 'RASP') counts.RASP++;
      else if (t.threat_type === 'SIM_WATCH') counts.SIM_WATCH++;
    });

    return [
      { name: 'Malware', key: 'MALWARE', value: counts.MALWARE, color: VECTOR_COLORS.MALWARE },
      { name: 'Phishing', key: 'PHISHING', value: counts.PHISHING, color: VECTOR_COLORS.PHISHING },
      { name: 'Wi-Fi', key: 'WIFI', value: counts.WIFI, color: VECTOR_COLORS.WIFI },
      { name: 'RASP Hook', key: 'RASP', value: counts.RASP, color: VECTOR_COLORS.RASP },
      { name: 'SIM Swap', key: 'SIM_WATCH', value: counts.SIM_WATCH, color: VECTOR_COLORS.SIM_WATCH },
    ].filter((item) => item.value > 0);
  }, [threats]);

  // 2. 24-Hour Attack Velocity
  const timelineData = useMemo(() => {
    return [
      { hour: '00:00', blocked: 4, detected: 5 },
      { hour: '04:00', blocked: 2, detected: 2 },
      { hour: '08:00', blocked: 9, detected: 11 },
      { hour: '12:00', blocked: 16, detected: 18 },
      { hour: '16:00', blocked: 12, detected: 14 },
      { hour: '20:00', blocked: 7, detected: 8 },
    ];
  }, []);

  // 3. Top Cities Bar Chart Data
  const cityData = useMemo(() => {
    const cityCounts: Record<string, number> = {};
    threats.forEach((t) => {
      const city = t.city || 'Lainnya';
      cityCounts[city] = (cityCounts[city] || 0) + 1;
    });

    const sorted = Object.entries(cityCounts)
      .map(([name, count]) => ({ name, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 5);

    return sorted.length > 0
      ? sorted
      : [
          { name: 'Jakarta', count: 4 },
          { name: 'Surabaya', count: 2 },
          { name: 'Bandung', count: 2 },
          { name: 'Medan', count: 1 },
          { name: 'Denpasar', count: 1 },
        ];
  }, [threats]);

  // 4. Wi-Fi Security Breakdown Metrics
  const wifiMetrics = useMemo(() => {
    const wifiThreats = threats.filter((t) => t.threat_type === 'WIFI');
    return {
      rogueAp: wifiThreats.filter((t) => t.target.includes('Evil Twin') || t.target.includes('Rogue')).length || 1,
      unencrypted: wifiThreats.filter((t) => t.target.includes('Unencrypted') || t.target.includes('Open')).length || 1,
      securePercent: 94,
    };
  }, [threats]);

  if (!mounted) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 animate-pulse">
        <div className="h-64 rounded-2xl bg-white border border-[#e9bcb8]/40" />
        <div className="h-64 rounded-2xl bg-white border border-[#e9bcb8]/40" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Analytic Row: Donut Chart & Wi-Fi Risk */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Card 1: Distribusi Vektor */}
        <div className="bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between pb-3 border-b border-[#f1f5f9]">
            <div className="flex items-center space-x-2">
              <ShieldAlert className="w-4 h-4 text-[#ed0226]" />
              <h3 className="text-xs font-bold text-[#0b132b] uppercase tracking-wider">
                Vektor Ancaman
              </h3>
            </div>
            <span className="text-[11px] text-[#718096] font-medium">Persentase</span>
          </div>

          <div className="h-48 w-full flex items-center justify-center my-2">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={vectorData}
                  cx="50%"
                  cy="50%"
                  innerRadius={48}
                  outerRadius={74}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {vectorData.map((entry) => (
                    <Cell key={entry.name} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(val) => [`${val} Insiden`, 'Jumlah']}
                  contentStyle={{
                    backgroundColor: '#161c2e',
                    borderRadius: '8px',
                    border: 'none',
                    color: '#fff',
                    fontSize: '11px',
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="grid grid-cols-2 gap-2 pt-2 border-t border-[#f1f5f9] text-[11px]">
            {vectorData.map((v) => (
              <div key={v.name} className="flex items-center space-x-1.5">
                <span className="w-2.5 h-2.5 rounded-full flex-shrink-0" style={{ backgroundColor: v.color }} />
                <span className="text-[#4a5568] truncate">{v.name}:</span>
                <span className="font-bold text-[#0b132b]">{v.value}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Card 2: Keamanan Wi-Fi Publik & Seluler */}
        <div className="bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between pb-3 border-b border-[#f1f5f9]">
            <div className="flex items-center space-x-2">
              <Wifi className="w-4 h-4 text-[#007eb4]" />
              <h3 className="text-xs font-bold text-[#0b132b] uppercase tracking-wider">
                Audit Jaringan Wi-Fi
              </h3>
            </div>
            <span className="text-[11px] px-2 py-0.5 rounded-full bg-[#e6f4ea] text-[#137333] font-bold">
              {wifiMetrics.securePercent}% Aman
            </span>
          </div>

          <div className="space-y-3.5 my-3">
            <div className="p-3 rounded-xl bg-[#fff8e1] border border-[#fde68a]">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-[#92400e]">Rogue AP (Evil Twin Orbit)</span>
                <span className="text-xs font-extrabold text-[#b45309]">{wifiMetrics.rogueAp} Titik</span>
              </div>
              <p className="text-[11px] text-[#a16207] mt-0.5">
                Pemalsuan SSID Orbit tanpa sertifikat otentikasi BSSID.
              </p>
            </div>

            <div className="p-3 rounded-xl bg-[#fff0f0] border border-[#fecaca]">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-[#b91c1c]">Wi-Fi Terbuka (Tanpa Sandi)</span>
                <span className="text-xs font-extrabold text-[#dc2626]">{wifiMetrics.unencrypted} Jaringan</span>
              </div>
              <p className="text-[11px] text-[#991b1b] mt-0.5">
                Risiko paket sniffing dan injeksi ARP pada hotspot publik.
              </p>
            </div>
          </div>

          <div className="pt-2 border-t border-[#f1f5f9] flex items-center justify-between text-[11px] text-[#718096]">
            <span>Proteksi DNS: <strong className="text-[#10b981]">Aktif</strong></span>
            <span>Kaspersky Wi-Fi Guard: <strong className="text-[#10b981]">Sinkron</strong></span>
          </div>
        </div>
      </div>

      {/* Bottom Analytic Row: Velocity Trend 24h & Top Cities */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Trend Serangan 24 Jam */}
        <div className="lg:col-span-7 bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm">
          <div className="flex items-center justify-between pb-3 border-b border-[#f1f5f9]">
            <div className="flex items-center space-x-2">
              <Activity className="w-4 h-4 text-[#ed0226]" />
              <h3 className="text-xs font-bold text-[#0b132b] uppercase tracking-wider">
                Tren Serangan 24 Jam
              </h3>
            </div>
            <div className="flex items-center space-x-3 text-[11px]">
              <span className="flex items-center space-x-1">
                <span className="w-2 h-2 rounded-full bg-[#10b981]" />
                <span className="text-[#718096]">Dicegah</span>
              </span>
              <span className="flex items-center space-x-1">
                <span className="w-2 h-2 rounded-full bg-[#ed0226]" />
                <span className="text-[#718096]">Total Serangan</span>
              </span>
            </div>
          </div>

          <div className="h-52 w-full mt-3">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={timelineData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="hour" tick={{ fontSize: 10, fill: '#94a3b8' }} tickLine={false} />
                <YAxis tick={{ fontSize: 10, fill: '#94a3b8' }} tickLine={false} axisLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#161c2e',
                    borderRadius: '8px',
                    border: 'none',
                    color: '#fff',
                    fontSize: '11px',
                  }}
                />
                <Area type="monotone" dataKey="detected" stroke="#ed0226" fill="#ffe9e7" strokeWidth={2} />
                <Area type="monotone" dataKey="blocked" stroke="#10b981" fill="#e8f5e9" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Top 5 Kota Terdampak */}
        <div className="lg:col-span-5 bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between pb-3 border-b border-[#f1f5f9]">
            <h3 className="text-xs font-bold text-[#0b132b] uppercase tracking-wider">
              Top 5 Wilayah Insiden
            </h3>
            <span className="text-[11px] text-[#718096]">Frekuensi</span>
          </div>

          <div className="h-52 w-full mt-2">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={cityData} layout="vertical">
                <XAxis type="number" hide />
                <YAxis
                  dataKey="name"
                  type="category"
                  tick={{ fontSize: 11, fill: '#4a5568' }}
                  width={80}
                  tickLine={false}
                  axisLine={false}
                />
                <Tooltip
                  formatter={(val) => [`${val} Kasus`, 'Total']}
                  contentStyle={{
                    backgroundColor: '#161c2e',
                    borderRadius: '8px',
                    border: 'none',
                    color: '#fff',
                    fontSize: '11px',
                  }}
                />
                <Bar
                  dataKey="count"
                  fill="#ed0226"
                  radius={[0, 6, 6, 0]}
                  onClick={(entry) => {
                    if (onSelectCity && entry?.name) {
                      onSelectCity(entry.name);
                    }
                  }}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <p className="text-[10px] text-[#a0aec0] text-center mt-1">
            Klik bar kota untuk zoom pada peta taktis
          </p>
        </div>
      </div>
    </div>
  );
};
