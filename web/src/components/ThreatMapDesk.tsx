'use client';

import React, { useState } from 'react';
import { ThreatLocationMap } from './ThreatLocationMap';
import { ThreatEvent } from '@/types';
import { MapPin, Search, ExternalLink, ShieldAlert, Wifi, Globe, Smartphone } from 'lucide-react';

interface ThreatMapDeskProps {
  threats: ThreatEvent[];
}

export const ThreatMapDesk: React.FC<ThreatMapDeskProps> = ({ threats }) => {
  const [selectedCity, setSelectedCity] = useState<string>('');
  const [search, setSearch] = useState<string>('');

  const filteredThreats = threats.filter((t) => {
    if (!search.trim()) return true;
    const q = search.toLowerCase();
    return (
      t.city?.toLowerCase().includes(q) ||
      t.target.toLowerCase().includes(q) ||
      t.msisdn.includes(q) ||
      t.threat_type.toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-[#ffe9e7] flex items-center justify-center text-[#ed0226]">
              <MapPin className="w-4 h-4" />
            </div>
            <h2 className="text-base font-bold text-[#0b132b]">
              Peta Geografis Ancaman Siber
            </h2>
          </div>
          <p className="text-xs text-[#718096] mt-1">
            Visualisasi sebaran insiden malware, phishing, dan WiFi berbahaya di seluruh wilayah Indonesia.
          </p>
        </div>

        {/* Search Input */}
        <div className="relative min-w-[260px]">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-[#a0aec0]" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Cari kota, nomor, atau target..."
            className="w-full pl-9 pr-4 py-2 rounded-xl text-xs border border-[#e2e8f0] focus:outline-none focus:border-[#ed0226] bg-[#f8fafc]"
          />
        </div>
      </div>

      {/* Main Map + Incident List Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Large Tactical Map */}
        <div className="lg:col-span-8">
          <ThreatLocationMap threats={filteredThreats} height="580px" selectedCity={selectedCity} />
        </div>

        {/* Right: Telemetry Incident Logs */}
        <div className="lg:col-span-4 bg-white rounded-2xl p-5 border border-[#e9bcb8]/60 shadow-sm flex flex-col h-[580px]">
          <div className="flex items-center justify-between pb-3 border-b border-[#f1f5f9]">
            <h3 className="text-xs font-bold text-[#0b132b] uppercase tracking-wider">
              Log Koordinat Terkini
            </h3>
            <span className="text-[11px] font-bold text-[#ed0226]">
              {filteredThreats.length} Titik
            </span>
          </div>

          <div className="flex-1 overflow-y-auto space-y-3 pr-1 pt-3">
            {filteredThreats.length === 0 ? (
              <div className="h-full flex flex-col items-center justify-center text-center text-[#a0aec0] p-4">
                <MapPin className="w-8 h-8 mb-2 opacity-50" />
                <p className="text-xs">Tidak ada insiden yang cocok dengan filter</p>
              </div>
            ) : (
              filteredThreats.map((threat) => (
                <div
                  key={threat.id}
                  onClick={() => threat.city && setSelectedCity(threat.city)}
                  className="p-3 rounded-xl border border-[#edf2f7] hover:border-[#ed0226] hover:bg-[#fffafa] transition-all cursor-pointer group"
                >
                  <div className="flex items-center justify-between">
                    <span
                      className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                        threat.severity === 'CRITICAL'
                          ? 'bg-[#fee2e2] text-[#dc2626]'
                          : 'bg-[#fef3c7] text-[#d97706]'
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
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
