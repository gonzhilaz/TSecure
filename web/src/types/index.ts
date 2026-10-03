export interface PackagePurchaseRecord {
  id: string;
  package_name: string;
  purchased_at: string;
  duration_days: number;
  price: number;
  channel: string;
  status: string;
}

export interface ScanLog {
  id: string;
  module: string; // ANTIVIRUS, WEB_PROTECTION, SIM_WATCH, RASP, SYSTEM_AUDIT
  scan_type: string;
  result: 'CLEAN' | 'THREAT_BLOCKED' | 'FILE_QUARANTINED' | 'TAMPER_PREVENTED' | string;
  items_scanned: number;
  threats_found: number;
  details: string;
  timestamp: string;
}

export interface Subscriber {
  id: string; // Kode unik pelanggan (e.g. SUB-081299887766)
  msisdn: string; // Nomor HP Telkomsel
  mobile_id: string; // Bound Mobile ID device
  device_model: string; // e.g. Google Pixel 6, Samsung S24
  os_version: string; // e.g. Android 14 / OneUI 6.1
  plan_name: string; // Paket pelanggan
  purchase_timestamp: string; // Jam & tanggal pembelian di MyTelkomsel
  active_period_start: string; // Masa aktif mulai
  active_period_end: string; // Masa aktif berakhir (NDP)
  kaspersky_expiry_date: string; // Masa aktif riil lisensi Kaspersky
  desync_days: number; // Selisih hari antara NDP dan Kaspersky
  is_active: boolean; // Status aktif
  activation_code: string; // Kode aktivasi SMS / OTP (e.g. TK-889123)
  activation_status: 'ACTIVATED' | 'SMS_FAILED' | 'PENDING_CODE' | 'DESYNC_WARNING' | 'EXPIRED' | string;
  kaspersky_license_key: string; // B2B license key
  root_status: 'CLEAN' | 'ROOT_DETECTED' | string; // Magisk/KernelSU
  hook_status: 'CLEAN' | 'HOOK_DETECTED' | string; // Frida/Xposed
  bound_iccid: string; // ICCID terdaftar
  current_iccid: string; // ICCID device saat ini (SIM Watch)
  sim_slot: string; // Slot 1 Telkomsel
  quarantine_count: number;
  device_migration_count: number;
  purchase_history?: PackagePurchaseRecord[];
  recent_scan_logs?: ScanLog[];
  data_retention_days?: number;
  retention_expires_at?: string;
  is_archived?: boolean;
  last_checked_at: string;
  created_at: string;
}

export interface ThreatEvent {
  id: string;
  mobile_id: string;
  msisdn: string;
  threat_type: 'PHISHING' | 'MALWARE' | 'EICAR' | 'SIM_WATCH' | 'RASP' | 'WIFI' | string;
  target: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  description: string;
  action_taken: 'BLOCKED' | 'ISOLATED' | 'REPORTED';
  latitude?: number;
  longitude?: number;
  city?: string;
  location_tag?: string;
  network_type?: string;
  timestamp: string;
}

export interface DashboardStats {
  total_subscribers: number;
  active_subscribers: number;
  expired_subscribers: number;
  pending_activation: number;
  sms_delivery_failed: number;
  desync_warnings: number;
  rooted_devices: number;
  sim_swap_alerts: number;
  total_threats_blocked: number;
  threats_today: number;
  kaspersky_quota_total: number;
  kaspersky_quota_used: number;
  average_security_score: number;
  recent_threats: ThreatEvent[];
}

export interface NdpOrderRequest {
  msisdn: string;
  package_id?: string;
  package_name: string;
  duration_days: number;
  price?: number;
  channel?: string;
}

export interface DeviceMigrationRequest {
  msisdn: string;
  new_mobile_id: string;
  new_device_model: string;
  new_os_version: string;
  reason?: string;
}

export interface AdminActionResponse {
  status: string;
  message: string;
  subscriber?: Subscriber;
}

export interface PhishingRecord {
  id: string;
  url: string;
  domain: string;
  category: 'PHISHING' | 'MALWARE_URL' | 'FAKE_LOGIN' | 'SMISHING' | string;
  target_brand: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | string;
  status: 'ACTIVE_THREAT' | 'REPORTED_KOMINFO' | 'TAKEN_DOWN' | 'WHITELISTED' | string;
  hit_count: number;
  first_detected_at: string;
  last_detected_at: string;
  targeted_msisdns: string[];
  action_taken: string;
  ksn_verdict: string;
  notes: string;
}

export interface PhishingStats {
  total_unique_domains: number;
  total_hits_blocked: number;
  active_threats_count: number;
  taken_down_count: number;
  top_targeted_brands: Record<string, number>;
}

export interface PhishingStatusUpdateRequest {
  id: string;
  status: 'ACTIVE_THREAT' | 'REPORTED_KOMINFO' | 'TAKEN_DOWN' | 'WHITELISTED' | string;
  notes?: string;
}

export interface Operator {
  id: string;
  name: string;
  email: string;
  role: 'SUPERADMIN' | 'SOC_ANALYST' | 'CUSTOMER_CARE' | 'AUDITOR' | string;
  badge_number: string;
  is_active: boolean;
  last_login_at?: string;
  created_at: string;
}

export interface OperatorLoginResponse {
  token: string;
  operator: Operator;
  expires_at: string;
}

export interface AuditLog {
  id: string;
  operator_email: string;
  operator_name: string;
  action: string;
  target_resource: string;
  details: string;
  ip_address: string;
  timestamp: string;
}

export interface IngestionApiKey {
  id: string;
  name: string;
  key: string;
  source: 'NDP_BILLING' | 'TELCO_PROXY' | 'WAF_GATEWAY' | 'EXTERNAL_SOC' | string;
  is_active: boolean;
  created_at: string;
  last_used_at?: string;
  request_count: number;
}

export interface DeadLetterRecord {
  id: string;
  source: 'NDP_BILLING' | 'TELCO_PROXY' | string;
  payload_raw: string;
  error_message: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | string;
  status: 'PENDING' | 'REPLAYED' | 'DISCARDED' | string;
  timestamp: string;
  resolved_at?: string;
  notes?: string;
}

export interface DatabaseMaintenanceStats {
  data_file_path: string;
  file_size_bytes: number;
  total_subscribers: number;
  total_threats: number;
  total_phishing_domains: number;
  total_audit_logs: number;
  total_dlq_records: number;
  total_operators: number;
  last_backup_at?: string;
  last_retention_run_at?: string;
  storage_engine: string;
  health_status: 'HEALTHY' | 'WARNING' | 'DEGRADED' | string;
}

export interface RetentionRunResult {
  threats_pruned: number;
  audit_pruned: number;
  dlq_pruned: number;
  remaining_threats: number;
  executed_at: string;
}

export type DashboardTab =
  | 'overview'
  | 'threat_map'
  | 'phishing_intel'
  | 'helpdesk'
  | 'device_integrity'
  | 'ingestion_dlq'
  | 'users_rbac'
  | 'db_maintenance'
  | 'reports'
  | 'ndp_simulator';



