'use client';

import React from 'react';
import { FileCheck2, FileWarning, Shield } from 'lucide-react';
import { ScanLog } from '@/types';

interface CustomerScanLogsProps {
  scanLogs?: ScanLog[];
  formatDate: (iso?: string) => string;
}

export const CustomerScanLogs: React.FC<CustomerScanLogsProps> = ({ scanLogs, formatDate }) => {
  const logs = scanLogs && scanLogs.length > 0 ? scanLogs : [];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h4 className="text-xs font-bold text-[#0b132b]">10 Log Terakhir Hasil Scan Device & Modul Kaspersky</h4>
          <p className="text-[11px] text-[#778ca2] mt-0.5">
            Audit realtime dari engine Antivirus, Web Filter, SIM Watch, dan BlackWall RASP
          </p>
        </div>
        <span className="text-[11px] font-semibold text-[#006490] bg-[#d0d9fd]/50 px-2 py-0.5 rounded-md">
          {logs.length} Log Tersedia
        </span>
      </div>

      {logs.length === 0 ? (
        <div className="p-8 text-center bg-[#fff8f7] rounded-xl border border-[#ffe9e7] text-xs text-[#778ca2]">
          <Shield className="w-8 h-8 mx-auto mb-2 text-[#e9bcb8]" />
          <p className="font-semibold text-[#0b132b]">Belum ada data scan yang terunggah</p>
          <p className="text-[11px] mt-0.5">Device belum melakukan check-in pemindaian pertama.</p>
        </div>
      ) : (
        <div className="space-y-2">
          {logs.map((log, idx) => {
            const isThreat = log.result !== 'CLEAN';
            return (
              <div
                key={log.id || idx}
                className={`p-3.5 rounded-xl border flex items-start justify-between gap-3 text-xs transition-colors ${
                  isThreat
                    ? 'bg-[#fff0ef] border-[#e9bcb8]'
                    : 'bg-[#fff8f7] border-[#ffe9e7] hover:bg-white'
                }`}
              >
                <div className="flex items-start gap-3">
                  <div
                    className={`p-2 rounded-lg shrink-0 mt-0.5 ${
                      isThreat ? 'bg-[#ffdad6] text-[#93000a]' : 'bg-[#e8f5e9] text-[#065f46]'
                    }`}
                  >
                    {isThreat ? <FileWarning className="w-4 h-4" /> : <FileCheck2 className="w-4 h-4" />}
                  </div>
                  <div>
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-bold text-[#0b132b]">{log.module}</span>
                      <span className="font-mono text-[10px] px-1.5 py-0.5 bg-white rounded border border-[#e9bcb8] text-[#5e3f3c]">
                        {log.scan_type}
                      </span>
                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                          isThreat ? 'bg-[#ffdad6] text-[#93000a]' : 'bg-[#e8f5e9] text-[#065f46]'
                        }`}
                      >
                        {log.result}
                      </span>
                    </div>
                    <p className="text-xs text-[#5e3f3c] mt-1">{log.details}</p>
                    <div className="flex items-center gap-3 text-[11px] text-[#778ca2] mt-1">
                      <span>Item diperiksa: {log.items_scanned}</span>
                      <span>•</span>
                      <span>Ancaman: {log.threats_found}</span>
                    </div>
                  </div>
                </div>

                <div className="text-[11px] text-[#778ca2] shrink-0 font-medium text-right">
                  {formatDate(log.timestamp)}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
