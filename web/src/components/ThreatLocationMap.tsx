'use client';

import React from 'react';
import dynamic from 'next/dynamic';
import { ThreatEvent } from '@/types';
import { MapPin } from 'lucide-react';

interface ThreatLocationMapProps {
  threats: ThreatEvent[];
  height?: string;
  selectedCity?: string;
}

const DynamicInternalMap = dynamic(
  () =>
    import('./ThreatLocationMapInternal').then((mod) => mod.ThreatLocationMapInternal),
  {
    ssr: false,
    loading: () => (
      <div className="w-full h-[460px] rounded-2xl bg-[#0d111d] border border-[#2a2f45] flex flex-col items-center justify-center p-6 text-center text-[#94a3b8]">
        <div className="w-12 h-12 rounded-2xl bg-[#1e263d] flex items-center justify-center text-[#60a5fa] mb-3 animate-pulse">
          <MapPin className="w-6 h-6" />
        </div>
        <p className="text-sm font-bold text-[#e2e8f0]">Memuat Peta Taktis SOC...</p>
        <p className="text-xs text-[#64748b] mt-1">Mengambil koordinat dan telemetri ancaman wilayah</p>
      </div>
    ),
  }
);

export const ThreatLocationMap: React.FC<ThreatLocationMapProps> = (props) => {
  return <DynamicInternalMap {...props} />;
};
