'use client';

import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { Sidebar } from '@/components/Sidebar';
import { Header } from '@/components/Header';
import { StatsCards } from '@/components/StatsCards';
import { ThreatLocationMap } from '@/components/ThreatLocationMap';
import { ThreatAnalytics } from '@/components/ThreatAnalytics';
import { ThreatMapDesk } from '@/components/ThreatMapDesk';
import { NdpSimulator } from '@/components/NdpSimulator';
import { ThreatFeed } from '@/components/ThreatFeed';
import { SubscriberTable } from '@/components/SubscriberTable';
import { CustomerDiagnosticsDesk } from '@/components/CustomerDiagnosticsDesk';
import { BillingLicenseDesk } from '@/components/BillingLicenseDesk';
import { PackageManagerDesk } from '@/components/PackageManagerDesk';
import { DeviceIntegrityDesk } from '@/components/DeviceIntegrityDesk';
import { PhishingIntelDesk } from '@/components/PhishingIntelDesk';
import { UserManagementDesk } from '@/components/UserManagementDesk';
import { IngestionGatewayDesk } from '@/components/IngestionGatewayDesk';
import { DatabaseLifecycleDesk } from '@/components/DatabaseLifecycleDesk';
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
import { DashboardStats, Subscriber, ThreatEvent, DashboardTab } from '@/types';

export default function SOCDashboard() {
  const [activeTab, setActiveTab] = useState<DashboardTab>('overview');
  const [sidebarCollapsed, setSidebarCollapsed] = useState<boolean>(false);
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState<boolean>(false);
  const [selectedMapCity, setSelectedMapCity] = useState<string>('');

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
      console.warn('Backend clear endpoint failed:', err);
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
        console.error('Init SOC dashboard data error:', err);
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    initData();

    // SSE Connection
    let evtSource: EventSource | null = null;
    try {
      evtSource = createEventSource();

      evtSource.onopen = () => setIsConnected(true);
      evtSource.addEventListener('connected', () => setIsConnected(true));
      evtSource.onerror = () => setIsConnected(false);

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
    } catch (err) {
      console.error('Could not initialize EventSource:', err);
    }

    const pollInterval = setInterval(() => {
      if (document.visibilityState === 'visible') {
        fetchDashboardStats()
          .then((s) => setStats(s))
          .catch(() => {});
      }
    }, 10000);

    return () => {
      ignore = true;
      clearInterval(pollInterval);
      if (evtSource) {
        evtSource.close();
      }
    };
  }, [handleSubscriberUpdated]);

  const { helpdeskIssues, integrityIssues } = useMemo(() => {
    let helpdesk = 0;
    let integrity = 0;

    for (const s of subscribers) {
      if (s.activation_status === 'SMS_FAILED' || s.desync_days > 0) helpdesk++;
      if (s.root_status === 'ROOT_DETECTED' || s.hook_status === 'HOOK_DETECTED') integrity++;
      if (s.bound_iccid && s.current_iccid && s.bound_iccid !== s.current_iccid) integrity++;
    }

    return { helpdeskIssues: helpdesk, integrityIssues: integrity };
  }, [subscribers]);

  return (
    <div className="min-h-screen flex bg-[#f8fafc]">
      {/* Sidebar Navigation */}
      <Sidebar
        activeTab={activeTab}
        onTabChange={setActiveTab}
        collapsed={sidebarCollapsed}
        onToggleCollapse={() => setSidebarCollapsed(!sidebarCollapsed)}
        mobileOpen={mobileSidebarOpen}
        onCloseMobile={() => setMobileSidebarOpen(false)}
        operator={operator}
        onLogout={handleLogout}
        isConnected={isConnected}
        helpdeskIssuesCount={helpdeskIssues}
        integrityIssuesCount={integrityIssues}
      />

      {/* Main Content Area */}
      <div
        className={`flex-1 flex flex-col min-w-0 transition-all duration-300 ${
          sidebarCollapsed ? 'lg:pl-20' : 'lg:pl-64'
        }`}
      >
        <Header
          isConnected={isConnected}
          onRefresh={handleManualRefresh}
          isRefreshing={isRefreshing}
          onClearData={handleClearData}
          operator={operator}
          onLogout={handleLogout}
          onToggleMobileMenu={() => setMobileSidebarOpen(!mobileSidebarOpen)}
        />

        {/* Unauthenticated Login Modal */}
        {isAuthChecked && !operator && (
          <LoginModal onLoginSuccess={handleLoginSuccess} />
        )}

        <main className="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-6">
          {/* Top KPI Metrics Cards */}
          <StatsCards stats={stats} loading={loading} />

          {/* TAB 1: OVERVIEW (Map + Analytics + Table + Feed) */}
          {activeTab === 'overview' && (
            <div className="space-y-6">
              {/* GIS Map & Analytic Highlights */}
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                <div className="lg:col-span-7">
                  <ThreatLocationMap
                    threats={threats}
                    height="480px"
                    selectedCity={selectedMapCity}
                  />
                </div>
                <div className="lg:col-span-5">
                  <ThreatFeed threats={threats} loading={loading} onClear={handleClearData} />
                </div>
              </div>

              {/* Comprehensive Analytics Suite */}
              <ThreatAnalytics
                threats={threats}
                onSelectCity={(city) => setSelectedMapCity(city)}
              />

              {/* Subscriber Fleet Table */}
              <SubscriberTable subscribers={subscribers} loading={loading} />
            </div>
          )}

          {/* TAB 2: DEDICATED GIS THREAT MAP */}
          {activeTab === 'threat_map' && (
            <ThreatMapDesk threats={threats} />
          )}

          {/* TAB 3: PHISHING INTEL DESK */}
          {activeTab === 'phishing_intel' && (
            <PhishingIntelDesk loading={loading || isRefreshing} />
          )}

          {/* TAB 4: HELPDESK & FLEET DIAGNOSTICS */}
          {activeTab === 'helpdesk' && (
            <CustomerDiagnosticsDesk
              subscribers={subscribers}
              onSubscriberUpdated={handleSubscriberUpdated}
              onRefresh={handleManualRefresh}
              loading={loading || isRefreshing}
            />
          )}

          {/* TAB 5: BILLING & B2B LICENSES */}
          {activeTab === 'billing_licenses' && (
            <BillingLicenseDesk
              subscribers={subscribers}
              stats={stats}
              loading={loading || isRefreshing}
              onRefresh={handleManualRefresh}
            />
          )}

          {/* TAB: PACKAGE MANAGER & GHOST SUBSCRIBERS */}
          {activeTab === 'package_manager' && (
            <PackageManagerDesk
              subscribers={subscribers}
              onRefresh={handleManualRefresh}
            />
          )}

          {/* TAB 6: DEVICE INTEGRITY & SIM WATCH */}
          {activeTab === 'device_integrity' && (
            <DeviceIntegrityDesk
              subscribers={subscribers}
              loading={loading || isRefreshing}
            />
          )}

          {/* TAB 6: INGESTION GATEWAY & DLQ */}
          {activeTab === 'ingestion_dlq' && (
            <IngestionGatewayDesk />
          )}

          {/* TAB 7: USER MANAGEMENT & RBAC */}
          {activeTab === 'users_rbac' && (
            <UserManagementDesk />
          )}

          {/* TAB 8: DATABASE LIFECYCLE */}
          {activeTab === 'db_maintenance' && (
            <DatabaseLifecycleDesk />
          )}

          {/* TAB 9: AUDIT REPORTS */}
          {activeTab === 'reports' && (
            <ReportDesk
              threats={threats}
              subscribers={subscribers}
              stats={stats}
              operator={operator}
              loading={loading || isRefreshing}
            />
          )}

          {/* TAB 10: NDP SIMULATOR */}
          {activeTab === 'ndp_simulator' && (
            <div className="space-y-6">
              <NdpSimulator onSubscriberUpdated={handleSubscriberUpdated} />
              <SubscriberTable subscribers={subscribers} loading={loading} />
            </div>
          )}
        </main>

        <footer className="border-t border-[#e2e8f0] py-4 text-center text-xs text-[#94a3b8] bg-white">
          Telkomsel Secure • Cyber SOC Command Center • Kaspersky Mobile Security Engine
        </footer>
      </div>
    </div>
  );
}
