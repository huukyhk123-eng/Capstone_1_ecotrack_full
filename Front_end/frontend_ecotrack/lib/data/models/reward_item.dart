import 'package:flutter/material.dart';

const Color primaryGreen = Color(0xFF06923E);

/// Model dữ liệu 1 reward – dùng để map từ JSON / DB
class RewardItem {
  final int voucherId;
  final String tagText;
  final Color tagColor;
  final IconData imageIcon;
  final String title;
  final String subtitle;
  final String partnerName;
  final String distance;
  final String expiry;
  final String priceText;
  final String? originalPriceText;
  final String pointsText;
  final double rating;
  final bool showRating;

  // category chỉ dùng ở FE để filter
  final String category; // ALL / FOOD / SHOPPING / TRAVEL / SERVICE

  const RewardItem({
    required this.voucherId,
    required this.tagText,
    required this.tagColor,
    required this.imageIcon,
    required this.title,
    required this.subtitle,
    required this.partnerName,
    required this.distance,
    required this.expiry,
    required this.priceText,
    this.originalPriceText,
    required this.pointsText,
    required this.rating,
    required this.showRating,
    required this.category,
  });

  /// Map từ JSON BE (VoucherViewDto) sang model dùng cho UI
  factory RewardItem.fromJson(Map<String, dynamic> json) {
    final int id = json['voucherId'] ?? 0;
    final title = (json['title'] ?? '') as String;
    final lowerTitle = title.toLowerCase();

    // ✅ PHÂN LOẠI CATEGORY Ở FE
    String category;
    if (lowerTitle.contains('buffet') ||
        lowerTitle.contains('ăn sáng') ||
        lowerTitle.contains('ăn trưa') ||
        lowerTitle.contains('cơm') ||
        lowerTitle.contains('trà sữa') ||
        lowerTitle.contains('cafe') ||
        lowerTitle.contains('cà phê') ||
        lowerTitle.contains('nhà hàng')) {
      category = 'FOOD'; // ĂN UỐNG
    } else if (lowerTitle.contains('spa') ||
        lowerTitle.contains('massage') ||
        lowerTitle.contains('giặt') ||
        lowerTitle.contains('vệ sinh') ||
        lowerTitle.contains('cắt tóc')) {
      category = 'SERVICE'; // DỊCH VỤ
    } else if (lowerTitle.contains('bus') ||
        lowerTitle.contains('buýt') ||
        lowerTitle.contains('xe') ||
        lowerTitle.contains('taxi') ||
        lowerTitle.contains('tàu')) {
      category = 'TRAVEL'; // DI CHUYỂN
    } else {
      category = 'SHOPPING'; // MUA SẮM
    }

    return RewardItem(
      voucherId: id,
      tagText: 'Ưu đãi',
      tagColor: primaryGreen,
      imageIcon: Icons.card_giftcard,
      title: title,
      subtitle: json['description'] ?? '',
      partnerName: json['partnerName'] ?? 'Eco Partner',
      distance: 'Online voucher',
      expiry: json['expiryDate']?.toString() ?? '',
      priceText: json['value'] ?? '',
      originalPriceText: null,
      pointsText: '${json['pointsRequired'] ?? 0} điểm',
      rating: 4.5,
      showRating: false,
      category: category,
    );
  }
  
}
