import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../core/widgets/tri_state_view.dart';
import '../../data/models/activity_log.dart';
import '../../data/services/activity_log_repository.dart';

class HistoryController extends ChangeNotifier {
  final ActivityLogRepository logRepository;

  ViewState _state = ViewState.success;
  String _selectedFilter = 'Semua';

  HistoryController({required this.logRepository}) {
    logRepository.addListener(_onRepositoryUpdated);
  }

  ViewState get state => _state;
  String get selectedFilter => _selectedFilter;
  int get totalScans => logRepository.totalScans;
  int get totalThreats => logRepository.totalThreats;

  List<String> get filterOptions => const [
        'Semua',
        'Pemindaian',
        'Jaringan & Web',
        'Aplikasi',
      ];

  void _onRepositoryUpdated() {
    notifyListeners();
  }

  List<ActivityLog> get filteredLogs {
    final all = logRepository.logs;
    if (_selectedFilter == 'Pemindaian') {
      return all.where((l) => l.category == LogCategory.pemindaian).toList();
    }
    if (_selectedFilter == 'Jaringan & Web') {
      return all.where((l) => l.category == LogCategory.jaringan).toList();
    }
    if (_selectedFilter == 'Aplikasi') {
      return all.where((l) => l.category == LogCategory.aplikasi).toList();
    }
    return all;
  }

  /// Groups filtered logs dynamically by day label (e.g. HARI INI, KEMARIN, or date).
  Map<String, List<ActivityLog>> get groupedLogs {
    final Map<String, List<ActivityLog>> groups = {};
    final now = DateTime.now();

    for (final log in filteredLogs) {
      final diffDays = DateTime(now.year, now.month, now.day)
          .difference(DateTime(log.date.year, log.date.month, log.date.day))
          .inDays;

      String label;
      final dateStr = DateFormat('dd MMM yyyy').format(log.date).toUpperCase();

      if (diffDays == 0) {
        label = 'HARI INI ($dateStr)';
      } else if (diffDays == 1) {
        label = 'KEMARIN ($dateStr)';
      } else {
        label = dateStr;
      }

      groups.putIfAbsent(label, () => []).add(log);
    }
    return groups;
  }

  Future<void> loadLogs() async {
    _state = ViewState.loading;
    notifyListeners();

    try {
      if (!logRepository.isLoaded) {
        await logRepository.loadLogs();
      }
      _state = logRepository.logs.isEmpty ? ViewState.empty : ViewState.success;
      notifyListeners();
    } catch (e) {
      _state = ViewState.error;
      notifyListeners();
    }
  }

  void setFilter(String filter) {
    _selectedFilter = filter;
    notifyListeners();
  }

  @override
  void dispose() {
    logRepository.removeListener(_onRepositoryUpdated);
    super.dispose();
  }
}
