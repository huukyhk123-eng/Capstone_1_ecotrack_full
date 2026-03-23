import 'package:flutter_test/flutter_test.dart';
import 'package:frontend_ecotrack/data/models/report_model.dart';

void main() {
  group('Report.fromJson', () {
    test('parses numeric fields from string values safely', () {
      final report = Report.fromJson({
        'reportId': 10,
        'title': 'Bao cao rac',
        'description': 'Co rac thai',
        'imageUrl': '/uploads/report.jpg',
        'gpsLat': '10.762622',
        'gpsLong': '106.660172',
        'status': 'PENDING',
        'createdAt': '2026-03-23T10:15:00',
        'category': 'Vo co',
        'aiVerified': false,
      });

      expect(report.reportId, 10);
      expect(report.latitude, closeTo(10.762622, 0.000001));
      expect(report.longitude, closeTo(106.660172, 0.000001));
      expect(report.aiConfidence, isNull);
    });

    test('falls back when numeric values are invalid', () {
      final report = Report.fromJson({
        'gpsLat': 'invalid',
        'gpsLong': null,
      });

      expect(report.latitude, 0.0);
      expect(report.longitude, 0.0);
      expect(report.status, 'UNKNOWN');
    });
  });
}
