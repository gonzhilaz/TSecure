import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';

class PhoneInputField extends StatelessWidget {
  final TextEditingController controller;

  const PhoneInputField({
    super.key,
    required this.controller,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 56,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 14),
      child: Row(
        children: [
          // Clean Indonesian Flag (strictly no raw emojis)
          Container(
            width: 22,
            height: 15,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(2),
              border: Border.all(color: const Color(0xFFE2E8F0)),
            ),
            clipBehavior: Clip.antiAlias,
            child: Column(
              children: [
                Expanded(child: Container(color: const Color(0xFFE11424))),
                Expanded(child: Container(color: Colors.white)),
              ],
            ),
          ),
          const SizedBox(width: 8),
          Text(
            '+62',
            style: AppTypography.labelLg.copyWith(
              color: AppColors.navyDeep,
              fontWeight: FontWeight.w700,
            ),
          ),
          Container(
            height: 24,
            width: 1,
            color: const Color(0xFFE2E8F0),
            margin: const EdgeInsets.symmetric(horizontal: 12),
          ),
          Expanded(
            child: TextField(
              controller: controller,
              keyboardType: TextInputType.phone,
              inputFormatters: [
                FilteringTextInputFormatter.digitsOnly,
                _LeadingZeroAndCountryCodeStripper(),
              ],
              style: AppTypography.labelLg.copyWith(
                fontWeight: FontWeight.w600,
                color: AppColors.navyDeep,
                letterSpacing: 0.5,
              ),
              decoration: const InputDecoration(
                border: InputBorder.none,
                enabledBorder: InputBorder.none,
                focusedBorder: InputBorder.none,
                contentPadding: EdgeInsets.zero,
                hintText: '812-3456-7890',
                hintStyle: TextStyle(
                  color: AppColors.slateMuted,
                  fontWeight: FontWeight.w400,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Formatter that automatically removes leading '0' (user typing 0812...)
/// and strips pasted +62 or 62 prefix since +62 is already provided by the UI.
class _LeadingZeroAndCountryCodeStripper extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue oldValue,
    TextEditingValue newValue,
  ) {
    var text = newValue.text.replaceAll(RegExp(r'\D'), '');

    // Strip country code if pasted
    if (text.startsWith('62')) {
      text = text.substring(2);
    }

    // Strip any leading 0s (user typing 0812 -> becomes 812)
    while (text.startsWith('0')) {
      text = text.substring(1);
    }

    // Limit to standard Indonesian mobile number length (max 13 digits without +62)
    if (text.length > 13) {
      text = text.substring(0, 13);
    }

    return TextEditingValue(
      text: text,
      selection: TextSelection.collapsed(offset: text.length),
    );
  }
}
