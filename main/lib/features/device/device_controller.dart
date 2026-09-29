import 'package:flutter/material.dart';
import '../../core/widgets/tri_state_view.dart';
import '../../data/mocks/mock_device_audits.dart';
import '../../data/models/security_audit_item.dart';

class DeviceController extends ChangeNotifier {
  ViewState _state = ViewState.success;
  List<SecurityAuditItem> _auditItems = MockDeviceAudits.getInitialAuditItems();

  ViewState get state => _state;
  List<SecurityAuditItem> get auditItems => _auditItems;

  Future<void> loadDeviceAuditData({bool showLoading = false}) async {
    if (showLoading || _auditItems.isEmpty) {
      _state = ViewState.loading;
      notifyListeners();
    }

    try {
      if (showLoading) {
        await Future.delayed(const Duration(milliseconds: 300));
      }
      _auditItems = MockDeviceAudits.getInitialAuditItems();
      _state = _auditItems.isEmpty ? ViewState.empty : ViewState.success;
      notifyListeners();
    } catch (e) {
      _state = ViewState.error;
      notifyListeners();
    }
  }

  void resolveAuditItem(String id) {
    _auditItems = _auditItems.map((item) {
      if (item.id == id) {
        return item.copyWith(
          status: AuditStatus.passed,
          actionLabel: null,
          subtitle: 'Audit selesai dan terverifikasi aman',
        );
      }
      return item;
    }).toList();
    notifyListeners();
  }
}
