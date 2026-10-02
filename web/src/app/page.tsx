'use client';

import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { Header } from '@/components/Header';
import { StatsCards } from '@/components/StatsCards';
import { NdpSimulator } from '@/components/NdpSimulator';
import { ThreatFeed } from '@/components/ThreatFeed';
import { SubscriberTable } from '@/components/SubscriberTable';
import { AdminTabNav, DashboardTab } from '@/components/AdminTabNav';
import { CustomerDiagnosticsDesk } from '@/components/CustomerDiagnosticsDesk';
import { DeviceIntegrityDesk } from '@/components/DeviceIntegrityDesk';
import { ReportDesk } from '@/components/ReportDesk';
import { LoginModal } from '@/components/LoginModal';
import { getCurrentOperator, logoutOperator, SOCOperator } from '@/lib/auth';
import { playCriticalThreatAlert } from '@/lib/soundAlert';
import {
  fetchDashboardStats,
  fetchSubscribers,
  fetchRecentThreats,
  createEventSource,
  clearDashboardData,
} from '@/lib/api';
import { DashboardStats, Subscriber, ThreatEvent } from '@/types';

export default function SOCDashboard() {
  const [activeTab, setActiveTab] = useState<DashboardTab>('overview');
  const [operator, setOperator] = useState<SOCOperator | null>(() => {
    if (typeof window !== 'undefined') {
      return getCurrentOperator();
    }
    return null;
  });
  const [isAuthChecked, setIsAuthChecked] = useState<boolean>(() => typeof window !== 'undefined');
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [subscribers, setSubscribers] = useState<Subscriber[]>([]);
  const [threats, setThreats] = useState<ThreatEvent[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false);
  const [isConnected, setIsConnected] = useState<boolean>(false);

  useEffect(() => {
    const frame = requestAnimationFrame(() => {
      setOperator(getCurrentOperator());
      setIsAuthChecked(true);
    });
    return () => cancelAnimationFrame(frame);
  }, []);

  const handleLoginSuccess = (op: SOCOperator) => {
    setOperator(op);
  };

  const handleLogout = () => {
    logoutOperator();
    setOperator(null);
  };

  const handleManualRefresh = async () => {
    setIsRefreshing(true);
    try {
      const [statsData, subsData, threatsData] = await Promise.all([
        fetchDashboardStats().catch(() => null),
        fetchSubscribers().catch(() => []),
        fetchRecentThreats().catch(() => []),
      ]);

      if (statsData) setStats(statsData);
      setSubscribers(subsData);
      setThreats(threatsData);
    } catch (err) {
      console.error('Failed to load data on refresh:', err);
    } finally {
      setIsRefreshing(false);
    }
  };

  const handleSubscriberUpdated = useCallback((updatedSub: Subscriber) => {
    setSubscribers((prev) => {
      const index = prev.findIndex((s) => s.msisdn === updatedSub.msisdn);
      if (index >= 0) {
        const copy = [...prev];
        copy[index] = updatedSub;
        return copy;
      }
      return [updatedSub, ...prev];
    });

    fetchDashboardStats()
      .then((s) => setStats(s))
      .catch(() => {});
  }, []);

  const handleClearData = async () => {
    try {
      await clearDashboardData();
    } catch (err) {
      console.warn('Backend clear endpoint call failed or offline:', err);
    }
    setThreats([]);
    setSubscribers([]);
    setStats({
      total_subscribers: 0,
      active_subscribers: 0,
      expired_subscribers: 0,
      pending_activation: 0,
      sms_delivery_failed: 0,
      desync_warnings: 0,
      rooted_devices: 0,
      sim_swap_alerts: 0,
      total_threats_blocked: 0,
      threats_today: 0,
      kaspersky_quota_total: 100,
      kaspersky_quota_used: 0,
      average_security_score: 100,
      recent_threats: [],
    });
  };

  useEffect(() => {
    let ignore = false;

    async function initData() {
      try {
        const [statsData, subsData, threatsData] = await Promise.all([
          fetchDashboardStats().catch(() => null),
          fetchSubscribers().catch(() => []),
          fetchRecentThreats().catch(() => []),
        ]);

        if (!ignore) {
          if (statsData) setStats(statsData);
          setSubscribers(subsData);
          setThreats(threatsData);
        }
      } catch (err) {
        console.error('Failed to initialize SOC dashboard data:', err);
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    initData();

    // Connect to Golang Backend SSE Stream
    let evtSource: EventSource | null = null;
    try {
      evtSource = createEventSource();

      evtSource.onopen = () => {
        setIsConnected(true);
      };

      evtSource.onerror = () => {
        setIsConnected(false);
      };

      evtSource.addEventListener('threat_alert', (e: MessageEvent) => {
        try {
          const newThreat: ThreatEvent = JSON.parse(e.data);
          setThreats((prev) => [newThreat, ...prev.slice(0, 49)]);
          if (newThreat.severity === 'CRITICAL' || newThreat.severity === 'HIGH') {
            playCriticalThreatAlert();
          }
          setStats((prev) => {
            if (!prev) return prev;
            return {
              ...prev,
              threats_today: prev.threats_today + 1,
              total_threats_blocked: prev.total_threats_blocked + 1,
            };
          });
        } catch (err) {
          console.error('Error parsing threat_alert SSE:', err);
        }
      });

      evtSource.addEventListener('subscriber_updated', (e: MessageEvent) => {
        try {
          const updatedSub: Subscriber = JSON.parse(e.data);
          handleSubscriberUpdated(updatedSub);
        } catch (err) {
          console.error('Error parsing subscriber_updated SSE:', err);
        }
      });

      const handleAllCleared = () => {
        setThreats([]);
        setSubscribers([]);
        setStats({
          total_subscribers: 0,
          active_subscribers: 0,
          expired_subscribers: 0,
          pending_activation: 0,
          sms_delivery_failed: 0,
          desync_warnings: 0,
          rooted_devices: 0,
          sim_swap_alerts: 0,
          total_threats_blocked: 0,
          threats_today: 0,
          kaspersky_quota_total: 100,
          kaspersky_quota_used: 0,
          average_security_score: 100,
          recent_threats: [],
        });
      };

      evtSource.addEventListener('threats_cleared', handleAllCleared);
      evtSource.addEventListener('subscribers_cleared', handleAllCleared);
    } catch (err) {
      console.error('Could not initialize EventSource:', err);
    }

    return () => {
      ignore = true;
      if (evtSource) {
        evtSource.close();
      }
    };
  }, [handleSubscriberUpdated]);

  const { smsFailedCount, desyncCount, rootedCount, simSwapCount } = useMemo(() => {
    let smsFailed = 0;
    let desync = 0;
    let rooted = 0;
    let simSwap = 0;

    for (const s of subscribers) {
      if (s.activation_status === 'SMS_FAILED') smsFailed++;
      if (s.desync_days > 0) desync++;
      if (s.root_status === 'ROOT_DETECTED' || s.hook_status === 'HOOK_DETECTED') rooted++;
      if (s.bound_iccid && s.current_iccid && s.bound_iccid !== s.current_iccid) simSwap++;
    }

    return {
      smsFailedCount: smsFailed,
      desyncCount: desync,
      rootedCount: rooted,
      simSwapCount: simSwap,
    };
  }, [subscribers]);

  return (
    <div className="min-h-screen flex flex-col bg-[#fff8f7]">
      <Header
        isConnected={isConnected}
        onRefresh={handleManualRefresh}
        isRefreshing={isRefreshing}
        onClearData={handleClearData}
        operator={operator}
        onLogout={handleLogout}
      />

      {/* Login Screen Modal if Unauthenticated */}
      {isAuthChecked && !operator && (
        <LoginModal onLoginSuccess={handleLoginSuccess} />
      )}

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 pb-12 pt-2">
        {/* KPI Metrics */}
        <StatsCards stats={stats} loading={loading} />

        {/* Tab Navigation */}
        <AdminTabNav
          activeTab={activeTab}
          onTabChange={setActiveTab}
          smsFailedCount={smsFailedCount}
          desyncCount={desyncCount}
          rootedCount={rootedCount}
          simSwapCount={simSwapCount}
        />

        {/* Tab 1: Overview & SOC Operations */}
        {activeTab === 'overview' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
            <div className="lg:col-span-7 space-y-6">
              <SubscriberTable subscribers={subscribers} loading={loading} />
            </div>
            <div className="lg:col-span-5">
              <ThreatFeed threats={threats} loading={loading} onClear={handleClearData} />
            </div>
          </div>
        )}

        {/* Tab 2: Customer Care & Diagnostic Helpdesk */}
        {activeTab === 'helpdesk' && (
          <CustomerDiagnosticsDesk
            subscribers={subscribers}
            onSubscriberUpdated={handleSubscriberUpdated}
            onRefresh={handleManualRefresh}
            loading={loading || isRefreshing}
          />
        )}

        {/* Tab 3: Device Integrity & SIM Watch */}
        {activeTab === 'device_integrity' && (
          <DeviceIntegrityDesk
            subscribers={subscribers}
            loading={loading || isRefreshing}
          />
        )}

        {/* Tab 4: Laporan & Ekspor Audit (Reports) */}
        {activeTab === 'reports' && (
          <ReportDesk
            threats={threats}
            subscribers={subscribers}
            stats={stats}
            operator={operator}
            loading={loading || isRefreshing}
          />
        )}

        {/* Tab 5: NDP / Billing Simulator (Vertical Stack) */}
        {activeTab === 'ndp_simulator' && (
          <div className="space-y-6">
            <NdpSimulator onSubscriberUpdated={handleSubscriberUpdated} />
            <SubscriberTable subscribers={subscribers} loading={loading} />
          </div>
        )}
      </main>

      <footer className="border-t border-[#e9bcb8]/60 py-4 text-center text-xs text-[#778ca2] bg-white/60">
        Telkomsel Secure • Kaspersky Mobile Security Ecosystem • Vigilance Modern Enterprise SOC
      </footer>
    </div>
  );
}
