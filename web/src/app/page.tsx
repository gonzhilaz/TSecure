'use client';

import React, { useEffect, useState } from 'react';
import { Header } from '@/components/Header';
import { StatsCards } from '@/components/StatsCards';
import { NdpSimulator } from '@/components/NdpSimulator';
import { ThreatFeed } from '@/components/ThreatFeed';
import { SubscriberTable } from '@/components/SubscriberTable';
import { AdminTabNav, DashboardTab } from '@/components/AdminTabNav';
import { CustomerDiagnosticsDesk } from '@/components/CustomerDiagnosticsDesk';
import { DeviceIntegrityDesk } from '@/components/DeviceIntegrityDesk';
import {
  fetchDashboardStats,
  fetchSubscribers,
  fetchRecentThreats,
  createEventSource,
} from '@/lib/api';
import { DashboardStats, Subscriber, ThreatEvent } from '@/types';

export default function SOCDashboard() {
  const [activeTab, setActiveTab] = useState<DashboardTab>('helpdesk');
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [subscribers, setSubscribers] = useState<Subscriber[]>([]);
  const [threats, setThreats] = useState<ThreatEvent[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false);
  const [isConnected, setIsConnected] = useState<boolean>(false);

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

  const handleSubscriberUpdated = (updatedSub: Subscriber) => {
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

    return () => {
      ignore = true;
      if (evtSource) {
        evtSource.close();
      }
    };
  }, []);

  const smsFailedCount = subscribers.filter((s) => s.activation_status === 'SMS_FAILED').length;
  const desyncCount = subscribers.filter((s) => s.desync_days > 0).length;
  const rootedCount = subscribers.filter((s) => s.root_status === 'ROOT_DETECTED' || s.hook_status === 'HOOK_DETECTED').length;
  const simSwapCount = subscribers.filter((s) => s.bound_iccid && s.current_iccid && s.bound_iccid !== s.current_iccid).length;

  return (
    <div className="min-h-screen flex flex-col bg-[#fff8f7]">
      <Header
        isConnected={isConnected}
        onRefresh={handleManualRefresh}
        isRefreshing={isRefreshing}
      />

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
              <ThreatFeed threats={threats} loading={loading} />
            </div>
          </div>
        )}

        {/* Tab 2: Customer Care & Diagnostic Helpdesk */}
        {activeTab === 'helpdesk' && (
          <CustomerDiagnosticsDesk
            subscribers={subscribers}
            onSubscriberUpdated={handleSubscriberUpdated}
            onRefresh={handleManualRefresh}
            loading={isRefreshing}
          />
        )}

        {/* Tab 3: Device Integrity & SIM Watch */}
        {activeTab === 'device_integrity' && (
          <DeviceIntegrityDesk subscribers={subscribers} />
        )}

        {/* Tab 4: NDP / Billing Simulator (Vertical Stack) */}
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
