'use client';

import React, { useState, useMemo } from 'react';
import { ThreatLocationMap } from './ThreatLocationMap';
import { ThreatProvinceRanking } from './ThreatProvinceRanking';
import { ThreatEvent } from '@/types';
import { MapPin, Search, ShieldAlert, Dices, Smartphone, Globe, Radio } from 'lucide-react';

interface ThreatMapDeskProps {
  threats: ThreatEvent[];
}

const SEED_INDONESIA_THREATS: ThreatEvent[] = [
  {
    id: 'thr-geo-01',
    mobile_id: 'DEV-JKT-01',
    msisdn: '081299881122',
    threat_type: 'JUDI_ONLINE',
    target: 'https://slot-zeus88.xyz/olympus',
    severity: 'HIGH',
    description: 'Promosi Slot Gacor Olympus & link alternatif',
    action_taken: 'BLOCKED',
    city: 'Jakarta',
    latitude: -6.2088,
    longitude: 106.8456,
    timestamp: new Date().toISOString(),
  },
  {
    id: 'thr-geo-02',
    mobile_id: 'DEV-JKT-02',
    msisdn: '081388776655',
    threat_type: 'SMISHING',
    target: 'surat_tilang_etle_v2.apk',
    severity: 'CRITICAL',
    description: 'SMS Rekayasa sosial APK surat tilang ETLE palsu',
    action_taken: 'BLOCKED',
    city: 'Jakarta',
    latitude: -6.1751,
    longitude: 106.8650,
    timestamp: new Date(Date.now() - 360000).toISOString(),
  },
  {
    id: 'thr-geo-03',
    mobile_id: 'DEV-SBY-01',
    msisdn: '085233445566',
    threat_type: 'JUDI_ONLINE',
    target: 'https://gacor-depo-pulsa.top',
    severity: 'HIGH',
    description: 'Tautan judi online deposit pulsa tanpa potongan',
    action_taken: 'BLOCKED',
    city: 'Surabaya',
    latitude: -7.2575,
    longitude: 112.7521,
    timestamp: new Date(Date.now() - 720000).toISOString(),
  },
  {
    id: 'thr-geo-04',
    mobile_id: 'DEV-BDG-01',
    msisdn: '081222334455',
    threat_type: 'PHISHING',
    target: 'https://my-telkomsel-poin-klaim.icu',
    severity: 'HIGH',
    description: 'Situs phising pencurian kredensial MyTelkomsel',
    action_taken: 'BLOCKED',
    city: 'Bandung',
    latitude: -6.9175,
    longitude: 107.6191,
    timestamp: new Date(Date.now() - 1200000).toISOString(),
  },
  {
    id: 'thr-geo-05',
    mobile_id: 'DEV-MDN-01',
    msisdn: '082166778899',
    threat_type: 'JUDI_ONLINE',
    target: 'https://maxwin-scatter-hitam.xyz',
    severity: 'HIGH',
    description: 'Promosi judi scatter hitam anti rungkad',
    action_taken: 'BLOCKED',
    city: 'Medan',
    latitude: 3.5952,
    longitude: 98.6722,
    timestamp: new Date(Date.now() - 1500000).toISOString(),
  },
  {
    id: 'thr-geo-06',
    mobile_id: 'DEV-MKS-01',
    msisdn: '085377889900',
    threat_type: 'SMISHING',
    target: 'undangan_pernikahan_digital.apk',
    severity: 'CRITICAL',
    description: 'Malware pencuri SMS OTP berkedok undangan pernikahan',
    action_taken: 'BLOCKED',
    city: 'Makassar',
    latitude: -5.1477,
    longitude: 119.4327,
    timestamp: new Date(Date.now() - 2100000).toISOString(),
  },
  {
    id: 'thr-geo-07',
    mobile_id: 'DEV-DPS-01',
    msisdn: '081933445566',
    threat_type: 'JUDI_ONLINE',
    target: 'https://slot88-pragmatic-play.online',
    severity: 'HIGH',
    description: 'Situs kasino dan slot online ilegal luar negeri',
    action_taken: 'BLOCKED',
    city: 'Denpasar',
    latitude: -8.6705,
    longitude: 115.2126,
    timestamp: new Date(Date.now() - 2700000).toISOString(),
  },
  {
    id: 'thr-geo-08',
    mobile_id: 'DEV-SMG-01',
    msisdn: '081288990011',
    threat_type: 'MALWARE',
    target: 'EICAR-Test-Signature.com',
    severity: 'CRITICAL',
    description: 'Sampel standar industri pengujian antivirus EICAR',
    action_taken: 'ISOLATED',
    city: 'Semarang',
    latitude: -6.9667,
    longitude: 110.4167,
    timestamp: new Date(Date.now() - 3200000).toISOString(),
  },
];

export const ThreatMapDesk: React.FC<ThreatMapDeskProps> = ({ threats }) => {
  const [selectedCity, setSelectedCity] = useState<string>('');
  const [search, setSearch] = useState<string>('');

  // Merge backend threats with seeded Indonesian geographic points
  const allThreats = useMemo(() => {
    if (!threats || threats.length === 0) return SEED_INDONESIA_THREATS;
    const hasJudol = threats.some((t) => t.threat_type === 'JUDI_ONLINE');
    if (!hasJudol) return [...threats, ...SEED_INDONESIA_THREATS];
    return threats;
  }, [threats]);

  const filteredThreats = useMemo(() => {
    return allThreats.filter((t) => {
      if (!search.trim()) return true;
      const q = search.toLowerCase();
      return (
        t.city?.toLowerCase().includes(q) ||
        t.target?.toLowerCase().includes(q) ||
        t.msisdn?.includes(q) ||
        t.threat_type?.toLowerCase().includes(q)
      );
    });
  }, [allThreats, search]);

  return (
    <div className="space-y-6">
      {/* Top Header Banner */}
      <div className="bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-[#ffe9e7] flex items-center justify-center text-[#ed0226]">
              <MapPin className="w-4 h-4" />
            </div>
            <h2 className="text-base font-bold text-[#0b132b]">
              Peta Sebaran Ancaman Siber Nasional
            </h2>
            <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-[#fee2e2] text-[#ed0226] border border-[#fecaca] flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-[#ed0226] animate-ping" />
              LIVE TELEMETRY
            </span>
          </div>
          <p className="text-xs text-[#718096] mt-1">
            Visualisasi sebaran SMS Judi Online, APK Malware, dan Web Phishing di seluruh wilayah Indonesia.
          </p>
        </div>

        {/* Search Input */}
        <div className="relative min-w-[280px]">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-[#a0aec0]" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Cari kota, nomor, atau domain judi..."
            className="w-full pl-9 pr-4 py-2 rounded-xl text-xs border border-[#e2e8f0] focus:outline-none focus:border-[#ed0226] bg-[#f8fafc]"
          />
        </div>
      </div>

      {/* Aggregate Province & Metric KPIs */}
      <ThreatProvinceRanking
        threats={allThreats}
        selectedCity={selectedCity}
        onSelectCity={(city) => setSelectedCity(city)}
      />

      {/* Main Map + Incident List Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Fullscreen-Caliber Tactical Map */}
        <div className="lg:col-span-8">
          <ThreatLocationMap threats={filteredThreats} height="600px" selectedCity={selectedCity} />
        </div>

        {/* Right: Live Telemetry Incident Logs */}
        <div className="lg:col-span-4 bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm flex flex-col h-[600px]">
          <div className="flex items-center justify-between pb-3 border-b border-[#f1f5f9]">
            <div className="flex items-center space-x-1.5">
              <Radio className="w-3.5 h-3.5 text-[#ed0226] animate-pulse" />
              <h3 className="text-xs font-bold text-[#0b132b] uppercase tracking-wider">
                Log Koordinat Terkini
              </h3>
            </div>
            <span className="text-[11px] font-bold text-[#ed0226]">
              {filteredThreats.length} Titik
            </span>
          </div>

          <div className="flex-1 overflow-y-auto space-y-2.5 pr-1 pt-3">
            {filteredThreats.length === 0 ? (
              <div className="h-full flex flex-col items-center justify-center text-center text-[#a0aec0] p-4">
                <MapPin className="w-8 h-8 mb-2 opacity-50" />
                <p className="text-xs">Tidak ada insiden yang cocok dengan filter</p>
              </div>
            ) : (
              filteredThreats.map((threat) => {
                const isJudol = threat.threat_type === 'JUDI_ONLINE';
                const isSmishing = threat.threat_type === 'SMISHING';
                const isMalware = threat.threat_type === 'MALWARE' || threat.threat_type === 'EICAR';

                return (
                  <div
                    key={threat.id}
                    onClick={() => threat.city && setSelectedCity(threat.city)}
                    className="p-3 rounded-xl border border-[#edf2f7] hover:border-[#ed0226] hover:bg-[#fffafa] transition-all cursor-pointer group"
                  >
                    <div className="flex items-center justify-between">
                      <span
                        className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                          isJudol
                            ? 'bg-[#ffedd5] text-[#ea580c]'
                            : isSmishing
                            ? 'bg-[#fee2e2] text-[#dc2626]'
                            : isMalware
                            ? 'bg-[#fef2f2] text-[#b91c1c]'
                            : 'bg-[#f3e8ff] text-[#9333ea]'
                        }`}
                      >
                        {threat.threat_type}
                      </span>
                      <span className="text-[10px] text-[#718096]">
                        {new Date(threat.timestamp).toLocaleTimeString('id-ID', {
                          hour: '2-digit',
                          minute: '2-digit',
                        })}{' '}
                        WIB
                      </span>
                    </div>

                    <p className="text-xs font-bold text-[#0b132b] mt-1.5 truncate group-hover:text-[#ed0226]">
                      {threat.city || 'Indonesia'}
                    </p>

                    <p className="text-[11px] text-[#718096] truncate font-mono mt-0.5">
                      {threat.target}
                    </p>

                    <div className="mt-2 pt-2 border-t border-[#f1f5f9] flex items-center justify-between text-[10px] text-[#a0aec0]">
                      <span className="text-[#0284c7] font-mono">
                        {threat.latitude?.toFixed(4)}, {threat.longitude?.toFixed(4)}
                      </span>
                      <span className="font-semibold text-[#4a5568]">{threat.msisdn}</span>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
