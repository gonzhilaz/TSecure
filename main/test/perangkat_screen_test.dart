import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:telkomsel_secure/features/device/device_controller.dart';
import 'package:telkomsel_secure/features/device/perangkat_screen.dart';
import 'package:telkomsel_secure/features/device/widgets/telemetry_chart_card.dart';

void main() {
  testWidgets('PerangkatScreen renders cards and audit items without error', (tester) async {
    final controller = DeviceController();

    await tester.pumpWidget(
      ChangeNotifierProvider<DeviceController>.value(
        value: controller,
        child: MaterialApp(
          home: PerangkatScreen(
            onNavigateToScanner: () {},
            onNavigateToProfile: () {},
          ),
        ),
      ),
    );

    await tester.pumpAndSettle();

    expect(find.text('Perangkat'), findsOneWidget);
    expect(find.text('REKOMENDASI & STATUS'), findsOneWidget);
    expect(find.text('Optimalisasi Keamanan'), findsOneWidget);

    final telemetryCard = tester.getRect(find.byType(TelemetryChartCard));
    debugPrint('TelemetryChartCard rect: $telemetryCard');
    final statusHeader = tester.getRect(find.text('REKOMENDASI & STATUS'));
    debugPrint('StatusHeader rect: $statusHeader');
    final firstItem = tester.getRect(find.text('Optimalisasi Keamanan'));
    debugPrint('FirstItem rect: $firstItem');
  });
}
