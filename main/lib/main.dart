import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/theme/app_theme.dart';
import 'data/services/activity_log_repository.dart';
import 'data/services/kaspersky_sdk_bridge.dart';
import 'data/services/telkomsel_backend_service.dart';
import 'data/services/threat_manager_service.dart';
import 'features/auth/auth_controller.dart';
import 'features/dashboard/dashboard_controller.dart';
import 'features/device/device_controller.dart';
import 'features/history/history_controller.dart';
import 'features/profile/profile_controller.dart';
import 'features/splash/splash_controller.dart';
import 'features/splash/splash_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const TelkomselSecureApp());
}

class TelkomselSecureApp extends StatelessWidget {
  const TelkomselSecureApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        Provider<TelkomselBackendService>(
          create: (_) => TelkomselBackendService(),
        ),
        ChangeNotifierProvider<ActivityLogRepository>(
          create: (_) => ActivityLogRepository()..loadLogs(),
        ),
        ChangeNotifierProvider<ThreatManagerService>(
          create: (ctx) => ThreatManagerService(ctx.read<ActivityLogRepository>()),
        ),
        ChangeNotifierProvider<KasperskySdkBridge>(
          create: (ctx) {
            final bridge = KasperskySdkBridge();
            bridge.logRepository = ctx.read<ActivityLogRepository>();
            return bridge;
          },
        ),
        ChangeNotifierProvider<SplashController>(
          create: (ctx) => SplashController(
            backendService: ctx.read<TelkomselBackendService>(),
            kasperskySdk: ctx.read<KasperskySdkBridge>(),
          ),
        ),
        ChangeNotifierProvider<AuthController>(
          create: (ctx) => AuthController(
            backendService: ctx.read<TelkomselBackendService>(),
            kasperskySdk: ctx.read<KasperskySdkBridge>(),
          ),
        ),
        ChangeNotifierProvider<DashboardController>(
          create: (ctx) => DashboardController(
            kasperskySdk: ctx.read<KasperskySdkBridge>(),
            logRepository: ctx.read<ActivityLogRepository>(),
          ),
        ),
        ChangeNotifierProvider<DeviceController>(
          create: (_) => DeviceController(),
        ),
        ChangeNotifierProvider<HistoryController>(
          create: (ctx) => HistoryController(
            logRepository: ctx.read<ActivityLogRepository>(),
          ),
        ),
        ChangeNotifierProvider<ProfileController>(
          create: (ctx) => ProfileController(
            kasperskySdk: ctx.read<KasperskySdkBridge>(),
            backendService: ctx.read<TelkomselBackendService>(),
          ),
        ),
      ],
      child: MaterialApp(
        title: 'Telkomsel Secure',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.lightTheme,
        home: const SplashScreen(),
      ),
    );
  }
}
