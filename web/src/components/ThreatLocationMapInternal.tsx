'use client';

import React, { useEffect, useRef, useState, useMemo } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { ThreatEvent } from '@/types';
import { MapPin, ShieldAlert, Wifi, Globe, Smartphone, ExternalLink, Search } from 'lucide-react';

interface ThreatLocationMapInternalProps {
  threats: ThreatEvent[];
  height?: string;
  selectedCity?: string;
}

export const ThreatLocationMapInternal: React.FC<ThreatLocationMapInternalProps> = ({
  threats,
  height = '460px',
  selectedCity,
}) => {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<L.Map | null>(null);
  const markersLayerRef = useRef<L.LayerGroup | null>(null);

  const [activeFilter, setActiveFilter] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedThreat, setSelectedThreat] = useState<ThreatEvent | null>(null);

  // Filter threats with valid coordinates
  const validThreats = useMemo(() => {
    return threats.filter((t) => {
      const hasCoords = typeof t.latitude === 'number' && typeof t.longitude === 'number';
      if (!hasCoords) return false;

      if (activeFilter === 'MALWARE' && t.threat_type !== 'MALWARE' && t.threat_type !== 'EICAR') return false;
      if (activeFilter === 'PHISHING' && t.threat_type !== 'PHISHING') return false;
      if (activeFilter === 'WIFI' && t.threat_type !== 'WIFI') return false;
      if (activeFilter === 'DEVICE' && t.threat_type !== 'SIM_WATCH' && t.threat_type !== 'RASP') return false;

      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase();
        const matchCity = t.city?.toLowerCase().includes(query);
        const matchTarget = t.target?.toLowerCase().includes(query);
        const matchMsisdn = t.msisdn?.toLowerCase().includes(query);
        if (!matchCity && !matchTarget && !matchMsisdn) return false;
      }

      return true;
    });
  }, [threats, activeFilter, searchQuery]);

  // Initialize Leaflet Map
  useEffect(() => {
    if (!mapContainerRef.current) return;
    if (mapInstanceRef.current) return;

    // Centered on Indonesia Archipelago
    const map = L.map(mapContainerRef.current, {
      center: [-2.5, 118.0],
      zoom: 5,
      minZoom: 4,
      maxZoom: 18,
      zoomControl: false,
    });

    // Dark Matter Tactical Map Tiles (Free, High Performance, High Contrast for SOC)
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; OpenStreetMap &copy; CARTO',
      subdomains: 'abcd',
      maxZoom: 19,
    }).addTo(map);

    L.control.zoom({ position: 'bottomright' }).addTo(map);

    markersLayerRef.current = L.layerGroup().addTo(map);
    mapInstanceRef.current = map;

    return () => {
      map.remove();
      mapInstanceRef.current = null;
    };
  }, []);

  // Update Markers when valid threats change
  useEffect(() => {
    const map = mapInstanceRef.current;
    const layer = markersLayerRef.current;
    if (!map || !layer) return;

    layer.clearLayers();

    validThreats.forEach((threat) => {
      if (typeof threat.latitude !== 'number' || typeof threat.longitude !== 'number') return;

      const isCritical = threat.severity === 'CRITICAL';
      let pinColor = '#3b82f6';
      let ringColor = 'rgba(59, 130, 246, 0.4)';

      if (threat.threat_type === 'MALWARE' || threat.threat_type === 'EICAR') {
        pinColor = '#ed0226';
        ringColor = 'rgba(237, 2, 38, 0.5)';
      } else if (threat.threat_type === 'PHISHING') {
        pinColor = '#f97316';
        ringColor = 'rgba(249, 115, 22, 0.5)';
      } else if (threat.threat_type === 'WIFI') {
        pinColor = '#eab308';
        ringColor = 'rgba(234, 179, 8, 0.5)';
      } else if (threat.threat_type === 'RASP' || threat.threat_type === 'SIM_WATCH') {
        pinColor = '#a855f7';
        ringColor = 'rgba(168, 85, 247, 0.5)';
      }

      const pulseHtml = `
        <div style="position: relative; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center;">
          <div style="position: absolute; width: 28px; height: 28px; border-radius: 50%; background: ${ringColor}; animation: ping 1.8s cubic-bezier(0, 0, 0.2, 1) infinite;"></div>
          <div style="width: 14px; height: 14px; border-radius: 50%; background: ${pinColor}; border: 2.5px solid #ffffff; box-shadow: 0 0 10px ${pinColor};"></div>
        </div>
      `;

      const customIcon = L.divIcon({
        html: pulseHtml,
        className: 'custom-radar-marker',
        iconSize: [30, 30],
        iconAnchor: [15, 15],
      });

      const marker = L.marker([threat.latitude, threat.longitude], { icon: customIcon });

      marker.on('click', () => {
        setSelectedThreat(threat);
        map.flyTo([threat.latitude!, threat.longitude!], 11, { duration: 0.8 });
      });

      marker.addTo(layer);
    });

    if (validThreats.length > 0 && selectedCity) {
      const match = validThreats.find((t) => t.city?.toLowerCase() === selectedCity.toLowerCase());
      if (match && typeof match.latitude === 'number' && typeof match.longitude === 'number') {
        map.flyTo([match.latitude, match.longitude], 10, { duration: 0.8 });
      }
    }
  }, [validThreats, selectedCity]);

  return (
    <div className="relative rounded-2xl overflow-hidden border border-[#2a2f45] shadow-lg bg-[#0d111d] flex flex-col">
      {/* Top Map Control Bar */}
      <div className="flex flex-wrap items-center justify-between gap-2.5 px-4 py-3 bg-[#131828] border-b border-[#22283e] z-10">
        <div className="flex items-center space-x-2">
          <div className="w-2.5 h-2.5 rounded-full bg-[#10b981] animate-pulse"></div>
          <span className="text-xs font-bold text-white tracking-wide uppercase">
            Peta Sebaran Ancaman
          </span>
          <span className="text-[11px] px-2 py-0.5 rounded-full bg-[#1e263d] text-[#60a5fa] border border-[#2b3553] font-semibold">
            {validThreats.length} Titik Terdeteksi
          </span>
        </div>

        {/* Quick Filter Buttons */}
        <div className="flex items-center space-x-1.5 overflow-x-auto text-[11px]">
          {[
            { id: 'ALL', label: 'Semua' },
            { id: 'MALWARE', label: 'Malware' },
            { id: 'PHISHING', label: 'Phishing' },
            { id: 'WIFI', label: 'Wi-Fi' },
            { id: 'DEVICE', label: 'SIM/RASP' },
          ].map((f) => (
            <button
              key={f.id}
              onClick={() => setActiveFilter(f.id)}
              className={`px-2.5 py-1 rounded-lg font-semibold transition-all ${
                activeFilter === f.id
                  ? 'bg-[#ed0226] text-white shadow-sm shadow-[#ed0226]/40'
                  : 'bg-[#1a2138] text-[#94a3b8] hover:bg-[#252f4d] hover:text-white'
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      {/* Map Canvas */}
      <div ref={mapContainerRef} style={{ height, width: '100%' }} className="relative z-0" />

      {/* Floating Selected Threat Modal Overlay */}
      {selectedThreat && (
        <div className="absolute bottom-4 left-4 right-4 md:left-auto md:right-4 md:w-96 z-20 bg-[#161c2e]/95 backdrop-blur-md border border-[#2d3752] rounded-xl p-3.5 shadow-2xl text-white">
          <div className="flex items-start justify-between">
            <div className="flex items-center space-x-2">
              <span
                className={`text-[10px] font-bold px-2 py-0.5 rounded-md ${
                  selectedThreat.severity === 'CRITICAL'
                    ? 'bg-[#ef4444] text-white'
                    : 'bg-[#f59e0b] text-white'
                }`}
              >
                {selectedThreat.severity}
              </span>
              <span className="text-xs font-bold text-[#e2e8f0]">
                {selectedThreat.threat_type}
              </span>
            </div>
            <button
              onClick={() => setSelectedThreat(null)}
              className="text-[#94a3b8] hover:text-white text-xs font-bold px-1.5 py-0.5 rounded bg-[#20283f]"
            >
              ✕
            </button>
          </div>

          <p className="mt-2 text-xs text-[#cbd5e1] font-mono break-all line-clamp-2">
            {selectedThreat.target}
          </p>

          <div className="mt-3 pt-2.5 border-t border-[#252f48] grid grid-cols-2 gap-2 text-[11px] text-[#94a3b8]">
            <div>
              <span className="text-[#64748b] block text-[10px]">Lokasi</span>
              <span className="text-[#f1f5f9] font-medium">
                {selectedThreat.city || 'Indonesia'}
              </span>
            </div>
            <div>
              <span className="text-[#64748b] block text-[10px]">Koordinat</span>
              <span className="text-[#38bdf8] font-mono">
                {selectedThreat.latitude?.toFixed(4)}, {selectedThreat.longitude?.toFixed(4)}
              </span>
            </div>
            <div>
              <span className="text-[#64748b] block text-[10px]">Perangkat</span>
              <span className="text-[#f1f5f9] font-medium">{selectedThreat.msisdn}</span>
            </div>
            <div>
              <span className="text-[#64748b] block text-[10px]">Jaringan</span>
              <span className="text-[#f1f5f9] font-medium">{selectedThreat.network_type || 'Telkomsel'}</span>
            </div>
          </div>

          <div className="mt-3 flex items-center justify-between">
            <span className="text-[10px] text-[#64748b]">
              {new Date(selectedThreat.timestamp).toLocaleTimeString('id-ID', {
                hour: '2-digit',
                minute: '2-digit',
              })}{' '}
              WIB
            </span>
            <a
              href={`https://www.google.com/maps?q=${selectedThreat.latitude},${selectedThreat.longitude}`}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center space-x-1 text-[11px] font-semibold text-[#60a5fa] hover:text-[#93c5fd]"
            >
              <span>Lihat Google Maps</span>
              <ExternalLink className="w-3 h-3" />
            </a>
          </div>
        </div>
      )}
    </div>
  );
};
