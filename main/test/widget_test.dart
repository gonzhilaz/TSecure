import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:telkomsel_secure/main.dart';

void main() {
  testWidgets('TelkomselSecureApp smoke test', (WidgetTester tester) async {
    SharedPreferences.setMockInitialValues({});
    await tester.pumpWidget(const TelkomselSecureApp());
    expect(find.byType(TelkomselSecureApp), findsOneWidget);
    await tester.pumpAndSettle(const Duration(seconds: 4));
  });
}
