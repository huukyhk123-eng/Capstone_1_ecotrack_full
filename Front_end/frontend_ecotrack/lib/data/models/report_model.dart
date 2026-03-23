class Report {
  final int reportId;
  final String title;
  final String description;
  final String imageUrl;
  final double latitude;
  final double longitude;
  final String status;
  final DateTime createdAt;
  final String category;
  final bool? aiVerified;
  final double? aiConfidence;
  final String? aiAnalysisJson;

  Report({
    required this.reportId,
    required this.title,
    required this.description,
    required this.imageUrl,
    required this.latitude,
    required this.longitude,
    required this.status,
    required this.createdAt,
    required this.category,
    this.aiVerified,
    this.aiConfidence,
    this.aiAnalysisJson,
  });

  factory Report.fromJson(Map<String, dynamic> json) {
    final parsedAiConfidence = _tryParseDouble(json['aiConfidence']);

    return Report(
      reportId: json['reportId'] ?? 0,
      title: json['title'] ?? 'Khong co tieu de',
      description: json['description'] ?? '',
      imageUrl: json['imageUrl'] ?? '',
      latitude: _tryParseDouble(json['gpsLat']) ?? 0.0,
      longitude: _tryParseDouble(json['gpsLong']) ?? 0.0,
      status: json['status'] ?? 'UNKNOWN',
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'])
          : DateTime.now(),
      category: json['category'] ?? 'Khac',
      aiVerified: json['aiVerified'],
      aiConfidence: parsedAiConfidence,
      aiAnalysisJson: json['aiAnalysisJson'],
    );
  }

  static double? _tryParseDouble(dynamic value) {
    if (value == null) return null;
    if (value is num) return value.toDouble();
    if (value is String) return double.tryParse(value);
    return null;
  }
}
