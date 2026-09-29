'use client';

import React, { useState } from 'react';
import {
  X,
  Smartphone,
  ShieldCheck,
  ShoppingBag,
  Activity,
  AlertTriangle,
  Clock,
  CheckCircle2,
} from 'lucide-react';
import { Subscriber } from '@/types';
import { resendActivationCode, resyncKasperskyLicense } from '@/lib/api';
import { CustomerDetailOverview } from './CustomerDetailOverview';
import { CustomerPurchaseHistory } from './CustomerPurchaseHistory';
import { CustomerScanLogs } from './CustomerScanLogs';

interface CustomerDetailModalProps {
  subscriber: Subscriber | null;
  onClose: () => void;
  onSubscriberUpdated: (updated: Subscriber) => void;
  onOpenMigration: (sub: Subscriber) => void;
}

export const CustomerDetailModal: React.FC<CustomerDetailModalProps> = ({
  subscriber,
  onClose,
  onSubscriberUpdated,
  onOpenMigration,
}) => {
  const [activeTab, setActiveTab] = useState<'overview' | 'packages' | 'scan_logs'>('overview');
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  if (!subscriber) return null;

  const showToast = (type: 'success' | 'error', text: string) => {
    setToast({ type, text });
    setTimeout(() => setToast(null), 4000);
  };

  const handleResendSMS = async () => {
    setActionLoading('sms');
    try {
      const res = await resendActivationCode(subscriber.msisdn);
      if (res.subscriber) onSubscriberUpdated(res.subscriber);
      showToast('success', `Kode aktivasi berhasil dikirim ulang via SMS ke ${subscriber.msisdn}`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Gagal mengirim SMS';
      showToast('error', msg);
    } finally {
      setActionLoading(null);
    }
  };

  const handleResyncKaspersky = async () => {
    setActionLoading('sync');
    try {
      const res = await resyncKasperskyLicense(subscriber.msisdn);
      if (res.subscriber) onSubscriberUpdated(res.subscriber);
      showToast('success', 'Masa aktif lisensi Kaspersky berhasil disinkronkan sesuai NDP Telkomsel');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Gagal sinkronisasi lisensi';
      showToast('error', msg);
    } finally {
      setActionLoading(null);
    }
  };

  const formatDate = (iso?: string) => {
    if (!iso || iso.startsWith('0001')) return '-';
    try {
      return new Date(iso).toLocaleString('id-ID', {
        dateStyle: 'medium',
        timeStyle: 'short',
      });
    } catch {
      return iso;
    }
  };

  const isDesynced = subscriber.desync_days > 0;
  const isSmsFailed = subscriber.activation_status === 'SMS_FAILED';
  const isRooted = subscriber.root_status === 'ROOT_DETECTED' || subscriber.hook_status === 'HOOK_DETECTED';
  const isSimSwapped = subscriber.bound_iccid && subscriber.current_iccid && subscriber.bound_iccid !== subscriber.current_iccid;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 bg-black/60 backdrop-blur-sm animate-in fade-in overflow-y-auto">
      <div className="bg-white rounded-2xl max-w-4xl w-full my-auto shadow-2xl border border-[#e9bcb8] flex flex-col max-h-[92vh] overflow-hidden">
        {/* Modal Header */}
        <div className="p-5 border-b border-[#ffe9e7] bg-[#fff8f7] flex items-center justify-between shrink-0">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-[#fff0ef] text-[#ed0226] rounded-xl border border-[#e9bcb8]">
              <Smartphone className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2 flex-wrap">
                <h2 className="text-lg font-bold text-[#0b132b]">{subscriber.msisdn}</h2>
                <span className="font-mono text-xs px-2.5 py-0.5 rounded-md bg-[#ffe9e7] text-[#be001c] font-semibold">
                  {subscriber.id}
                </span>
                <span
                  className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full ${
                    subscriber.is_active ? 'bg-[#e8f5e9] text-[#065f46]' : 'bg-[#ffdad6] text-[#93000a]'
                  }`}
                >
                  {subscriber.is_active ? 'TERLINDUNGI AKTIF' : 'KEDALUWARSA'}
                </span>
              </div>
              <p className="text-xs text-[#778ca2] mt-0.5">
                Hardware ID: <span className="font-mono">{subscriber.mobile_id}</span> • Device: <span className="font-medium text-[#0b132b]">{subscriber.device_model}</span>
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 rounded-xl text-[#778ca2] hover:bg-[#fff0ef] hover:text-[#0b132b] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Sub-Nav Tabs */}
        <div className="flex items-center gap-2 px-6 pt-3 border-b border-[#ffe9e7] bg-white shrink-0">
          {[
            { id: 'overview', label: 'Ringkasan & Lisensi', icon: ShieldCheck },
            { id: 'packages', label: 'Histori Pembelian Paket', icon: ShoppingBag, count: subscriber.purchase_history?.length || 1 },
            { id: 'scan_logs', label: '10 Log Scan Terakhir', icon: Activity, count: subscriber.recent_scan_logs?.length || 10 },
          ].map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as typeof activeTab)}
                className={`flex items-center gap-2 px-4 py-2.5 text-xs font-bold border-b-2 transition-all ${
                  isActive
                    ? 'border-[#ed0226] text-[#be001c] bg-[#fff8f7]'
                    : 'border-transparent text-[#5e3f3c] hover:text-[#0b132b]'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
                {tab.count !== undefined && (
                  <span className={`text-[10px] px-1.5 py-0.5 rounded-full ${isActive ? 'bg-[#ed0226] text-white' : 'bg-[#ffe9e7] text-[#be001c]'}`}>
                    {tab.count}
                  </span>
                )}
              </button>
            );
          })}
        </div>

        {/* Toast Feedback */}
        {toast && (
          <div className="mx-6 mt-4 p-3 rounded-xl border text-xs font-semibold flex items-center justify-between animate-in slide-in-from-top bg-[#e8f5e9] border-[#a7f3d0] text-[#065f46]">
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4" />
              <span>{toast.text}</span>
            </div>
            <button onClick={() => setToast(null)} className="underline text-[10px]">Tutup</button>
          </div>
        )}

        {/* Scrollable Body */}
        <div className="p-6 overflow-y-auto space-y-6 flex-1">
          {/* Audit Notification Banner */}
          {(isSmsFailed || isDesynced || isRooted || isSimSwapped) && (
            <div className="p-3.5 rounded-xl bg-[#fff0ef] border border-[#e9bcb8] space-y-1.5">
              <div className="text-xs font-bold text-[#be001c] flex items-center gap-2">
                <AlertTriangle className="w-4 h-4" />
                <span>Catatan Audit & Eskalasi Customer Care:</span>
              </div>
              <div className="flex flex-wrap gap-2 text-xs">
                {isSmsFailed && (
                  <span className="px-2 py-0.5 rounded-md bg-[#ffdad6] text-[#93000a] font-semibold">
                    SMS Aktivasi Gagal Terkirim
                  </span>
                )}
                {isDesynced && (
                  <span className="px-2 py-0.5 rounded-md bg-[#fff8e1] text-[#b45309] font-semibold">
                    Desync: Lisensi Kaspersky kedaluwarsa {subscriber.desync_days} hari lebih awal
                  </span>
                )}
                {isRooted && (
                  <span className="px-2 py-0.5 rounded-md bg-[#fee2e2] text-[#991b1b] font-semibold">
                    Root/Tamper: {subscriber.root_status} ({subscriber.hook_status})
                  </span>
                )}
                {isSimSwapped && (
                  <span className="px-2 py-0.5 rounded-md bg-[#fef3c7] text-[#92400e] font-semibold">
                    SIM Swap: ICCID aktif tidak cocok dengan registrasi
                  </span>
                )}
              </div>
            </div>
          )}

          {activeTab === 'overview' && (
            <CustomerDetailOverview
              subscriber={subscriber}
              actionLoading={actionLoading}
              onResendSMS={handleResendSMS}
              onResyncKaspersky={handleResyncKaspersky}
              onOpenMigration={onOpenMigration}
              formatDate={formatDate}
            />
          )}

          {activeTab === 'packages' && (
            <CustomerPurchaseHistory
              purchaseHistory={subscriber.purchase_history}
              fallbackPlan={subscriber.plan_name}
              fallbackPurchasedAt={subscriber.purchase_timestamp}
              isActive={subscriber.is_active}
              formatDate={formatDate}
            />
          )}

          {activeTab === 'scan_logs' && (
            <CustomerScanLogs
              scanLogs={subscriber.recent_scan_logs}
              formatDate={formatDate}
            />
          )}
        </div>

        {/* Modal Footer */}
        <div className="p-4 border-t border-[#ffe9e7] bg-[#fff8f7] flex items-center justify-between shrink-0">
          <div className="flex items-center gap-2 text-xs text-[#778ca2]">
            <Clock className="w-4 h-4" />
            <span>Terakhir sinkronisasi: {formatDate(subscriber.last_checked_at)}</span>
          </div>
          <button
            onClick={onClose}
            className="px-5 py-2 bg-[#545d7c] hover:bg-[#3f4762] text-white text-xs font-bold rounded-xl transition-colors"
          >
            Tutup
          </button>
        </div>
      </div>
    </div>
  );
};
