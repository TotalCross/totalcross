// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.io.device.scanner;

public class Honeywell {
  /** Starts the definition of the barcode parameters. */
  public static final String START_BATCH = "***START_BATCH***";
  /** Ends the definition of the barcode parameters. */
  public static final String END_BATCH = "***END_BATCH***";

  /** Enable or disable. */
  public static final java.lang.String PROPERTY_CODE_128_ENABLED = "DEC_CODE128_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_CODE_128_MINIMUM_LENGTH = "DEC_CODE128_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_CODE_128_MAXIMUM_LENGTH = "DEC_CODE128_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_GS1_128_ENABLED = "DEC_GS1_128_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_GS1_128_MINIMUM_LENGTH = "DEC_GS1_128_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_GS1_128_MAXIMUM_LENGTH = "DEC_GS1_128_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_ISBT_128_ENABLED = "DEC_C128_ISBT_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_CODE_39_ENABLED = "DEC_CODE39_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_CODE_39_MINIMUM_LENGTH = "DEC_CODE39_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_CODE_39_MAXIMUM_LENGTH = "DEC_CODE39_MAX_LENGTH";
  /** Check digit mode. */
  public static final java.lang.String PROPERTY_CODE_39_CHECK_DIGIT_MODE = "DEC_CODE39_CHECK_DIGIT_MODE";
  /** Enable or disable full ASCII. */
  public static final java.lang.String PROPERTY_CODE_39_FULL_ASCII_ENABLED = "DEC_CODE39_FULL_ASCII_ENABLED";
  /** Enable or disable the start/stop transmission. */
  public static final java.lang.String PROPERTY_CODE_39_START_STOP_TRANSMIT_ENABLED = "DEC_CODE39_START_STOP_TRANSMIT";
  /** No checksum checking is performed. */
  public static final java.lang.String CODE_39_CHECK_DIGIT_MODE_NO_CHECK = "noCheck";
  /** Checksum check is performed. */
  public static final java.lang.String CODE_39_CHECK_DIGIT_MODE_CHECK = "check";
  /** Checksum check is performed and the checksum digit is stripped from the result string. */
  public static final java.lang.String CODE_39_CHECK_DIGIT_MODE_CHECK_AND_STRIP = "checkAndStrip";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_CODE_39_BASE_32_ENABLED = "DEC_CODE39_BASE32_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_DATAMATRIX_ENABLED = "DEC_DATAMATRIX_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_DATAMATRIX_MINIMUM_LENGTH = "DEC_DATAMATRIX_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_DATAMATRIX_MAXIMUM_LENGTH = "DEC_DATAMATRIX_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_A_ENABLE = "DEC_UPCA_ENABLE";
  /** Translate UPC-A to EAN13. */
  public static final java.lang.String PROPERTY_UPC_A_TRANSLATE_EAN13 = "DEC_UPCA_TRANSLATE_TO_EAN13";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_A_COUPON_CODE_MODE_ENABLED = "DEC_COUPON_CODE_MODE";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_A_COMBINE_COUPON_CODE_MODE_ENABLED = "DEC_COMBINE_COUPON_CODES";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_A_CHECK_DIGIT_TRANSMIT_ENABLED = "DEC_UPCA_CHECK_DIGIT_TRANSMIT";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_A_NUMBER_SYSTEM_TRANSMIT_ENABLED = "DEC_UPCA_NUMBER_SYSTEM_TRANSMIT";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_A_TWO_CHAR_ADDENDA_ENABLED = "DEC_UPCA_2CHAR_ADDENDA_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_A_FIVE_CHAR_ADDENDA_ENABLED = "DEC_UPCA_5CHAR_ADDENDA_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_A_ADDENDA_REQUIRED_ENABLED = "DEC_UPCA_ADDENDA_REQUIRED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_A_ADDENDA_SEPARATOR_ENABLED = "DEC_UPCA_ADDENDA_SEPARATOR";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_E_ENABLED = "DEC_UPCE0_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_E_E1_ENABLED = "DEC_UPCE1_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_E_CHECK_DIGIT_TRANSMIT_ENABLED = "DEC_UPCE_CHECK_DIGIT_TRANSMIT";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_E_NUMBER_SYSTEM_TRANSMIT_ENABLED = "DEC_UPCE_NUMBER_SYSTEM_TRANSMIT";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_E_TWO_CHAR_ADDENDA_ENABLED = "DEC_UPCE_2CHAR_ADDENDA_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_E_FIVE_CHAR_ADDENDA_ENABLED = "DEC_UPCE_5CHAR_ADDENDA_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_E_ADDENDA_REQUIRED_ENABLED = "DEC_UPCE_ADDENDA_REQUIRED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_UPC_E_ADDENDA_SEPARATOR_ENABLED = "DEC_UPCE_ADDENDA_SEPARATOR";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_8_ENABLED = "DEC_EAN8_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_8_CHECK_DIGIT_TRANSMIT_ENABLED = "DEC_EAN8_CHECK_DIGIT_TRANSMIT";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_8_TWO_CHAR_ADDENDA_ENABLED = "DEC_EAN8_2CHAR_ADDENDA_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_8_FIVE_CHAR_ADDENDA_ENABLED = "DEC_EAN8_5CHAR_ADDENDA_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_8_ADDENDA_REQUIRED_ENABLED = "DEC_EAN8_ADDENDA_REQUIRED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_8_ADDENDA_SEPARATOR_ENABLED = "DEC_EAN8_ADDENDA_SEPARATOR";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_13_ENABLED = "DEC_EAN13_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED = "DEC_EAN13_CHECK_DIGIT_TRANSMIT";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_13_TWO_CHAR_ADDENDA_ENABLED = "DEC_EAN13_2CHAR_ADDENDA_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_13_FIVE_CHAR_ADDENDA_ENABLED = "DEC_EAN13_5CHAR_ADDENDA_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_13_ADDENDA_REQUIRED_ENABLED = "DEC_EAN13_ADDENDA_REQUIRED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_EAN_13_ADDENDA_SEPARATOR_ENABLED = "DEC_EAN13_ADDENDA_SEPARATOR";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_AZTEC_ENABLED = "DEC_AZTEC_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_AZTEC_MINIMUM_LENGTH = "DEC_AZTEC_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_AZTEC_MAXIMUM_LENGTH = "DEC_AZTEC_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_CHINA_POST_ENABLED = "DEC_HK25_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_CHINA_POST_MINIMUM_LENGTH = "DEC_HK25_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_CHINA_POST_MAXIMUM_LENGTH = "DEC_HK25_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_CODABAR_ENABLED = "DEC_CODABAR_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_CODABAR_MINIMUM_LENGTH = "DEC_CODABAR_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_CODABAR_MAXIMUM_LENGTH = "DEC_CODABAR_MAX_LENGTH";
  /** Enable or disable the start/stop transmission. */
  public static final java.lang.String PROPERTY_CODABAR_START_STOP_TRANSMIT_ENABLED = "DEC_CODABAR_START_STOP_TRANSMIT";
  /** Check digit mode. */
  public static final java.lang.String PROPERTY_CODABAR_CHECK_DIGIT_MODE = "DEC_CODABAR_CHECK_DIGIT_MODE";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_CODABAR_CONCAT_ENABLED = "DEC_CODABAR_CONCAT_ENABLED";
  /** No checksum checking is performed. */
  public static final java.lang.String CODABAR_CHECK_DIGIT_MODE_NO_CHECK = "noCheck";
  /** Checksum check is performed. */
  public static final java.lang.String CODABAR_CHECK_DIGIT_MODE_CHECK = "check";
  /** Checksum check is performed and the checksum digit is stripped from the result string. */
  public static final java.lang.String CODABAR_CHECK_DIGIT_MODE_CHECK_AND_STRIP = "checkAndStrip";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_CODABLOCK_A_ENABLED = "DEC_CODABLOCK_A_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_CODABLOCK_A_MINIMUM_LENGTH = "DEC_CODABLOCK_A_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_CODABLOCK_A_MAXIMUM_LENGTH = "DEC_CODABLOCK_A_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_CODABLOCK_F_ENABLED = "DEC_CODABLOCK_F_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_CODABLOCK_F_MINIMUM_LENGTH = "DEC_CODABLOCK_F_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_CODABLOCK_F_MAXIMUM_LENGTH = "DEC_CODABLOCK_F_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_CODE_11_ENABLED = "DEC_CODE11_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_CODE_11_MINIMUM_LENGTH = "DEC_CODE11_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_CODE_11_MAXIMUM_LENGTH = "DEC_CODE11_MAX_LENGTH";
  /** Check digit mode. */
  public static final java.lang.String PROPERTY_CODE_11_CHECK_DIGIT_MODE = "DEC_CODE11_CHECK_DIGIT_MODE";
  /** Two checksum digits checked. */
  public static final java.lang.String CODE_11_CHECK_DIGIT_MODE_DOUBLE_DIGIT_CHECK = "doubleDigitCheck";
  /** One checksum digit checked. */
  public static final java.lang.String CODE_11_CHECK_DIGIT_MODE_SINGLE_DIGIT_CHECK = "singleDigitCheck";
  /** Two checksum digits checked and stripped from the result string. */
  public static final java.lang.String CODE_11_CHECK_DIGIT_MODE_DOUBLE_DIGIT_CHECK_AND_STRIP = "doubleDigitCheckAndStrip";
  /** One checksum digit checked and stripped from the result string. */
  public static final java.lang.String CODE_11_CHECK_DIGIT_MODE_SINGLE_DIGIT_CHECK_AND_STRIP = "singleDigitCheckAndStrip";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_CODE_93_ENABLED = "DEC_CODE93_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_CODE_93_MINIMUM_LENGTH = "DEC_CODE93_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_CODE_93_MAXIMUM_LENGTH = "DEC_CODE93_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_COMPOSITE_ENABLED = "DEC_COMPOSITE_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_COMPOSITE_MINIMUM_LENGTH = "DEC_COMPOSITE_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_COMPOSITE_MAXIMUM_LENGTH = "DEC_COMPOSITE_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_HAX_XIN_ENABLED = "DEC_HANXIN_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_HAX_XIN_MINIMUM_LENGTH = "DEC_HANXIN_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_HAX_XIN_MAXIMUM_LENGTH = "DEC_HANXIN_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_IATA_25_ENABLED = "DEC_IATA25_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_IATA_25_MINIMUM_LENGTH = "DEC_IATA25_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_IATA_25_MAXIMUM_LENGTH = "DEC_IATA25_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_INTERLEAVED_25_ENABLED = "DEC_I25_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_INTERLEAVED_25_MINIMUM_LENGTH = "DEC_I25_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_INTERLEAVED_25_MAXIMUM_LENGTH = "DEC_I25_MAX_LENGTH";
  /** Check digit mode. */
  public static final java.lang.String PROPERTY_INTERLEAVED_25_CHECK_DIGIT_MODE = "DEC_I25_CHECK_DIGIT_MODE";
  /** No checksum checking is performed. */
  public static final java.lang.String INTERLEAVED_25_CHECK_DIGIT_MODE_NO_CHECK = "noCheck";
  /** Checksum check is performed. */
  public static final java.lang.String INTERLEAVED_25_CHECK_DIGIT_MODE_CHECK = "check";
  /** Checksum check is performed and the checksum digit is stripped from the result string. */
  public static final java.lang.String INTERLEAVED_25_CHECK_DIGIT_MODE_CHECK_AND_STRIP = "checkAndStrip";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_KOREAN_POST_ENABLED = "DEC_KOREA_POST_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_KOREAN_POST_MINIMUM_LENGTH = "DEC_KOREA_POST_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_KOREAN_POST_MAXIMUM_LENGTH = "DEC_KOREA_POST_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_MATRIX_25_ENABLED = "DEC_M25_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_MATRIX_25_MINIMUM_LENGTH = "DEC_M25_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_MATRIX_25_MAXIMUM_LENGTH = "DEC_M25_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_MAXICODE_ENABLED = "DEC_MAXICODE_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_MAXICODE_MINIMUM_LENGTH = "DEC_MAXICODE_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_MAXICODE_MAXIMUM_LENGTH = "DEC_MAXICODE_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_MICRO_PDF_417_ENABLED = "DEC_MICROPDF_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_MICRO_PDF_417_MINIMUM_LENGTH = "DEC_MICROPDF_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_MICRO_PDF_417_MAXIMUM_LENGTH = "DEC_MICROPDF_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_MSI_ENABLED = "DEC_MSI_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_MSI_MINIMUM_LENGTH = "DEC_MSI_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_MSI_MAXIMUM_LENGTH = "DEC_MSI_MAX_LENGTH";
  /** Check digit mode. */
  public static final java.lang.String PROPERTY_MSI_CHECK_DIGIT_MODE = "DEC_MSI_CHECK_DIGIT_MODE";
  /** No checksum checking is performed. */
  public static final java.lang.String MSI_CHECK_DIGIT_MODE_NO_CHECK = "noCheck";
  /** One mod 10 checksum digit checked. */
  public static final java.lang.String MSI_CHECK_DIGIT_MODE_SINGLE_MOD_10_CHECK = "singleMod10Check";
  /** One mod 11 checksum digit plus one mod 10 checksum digit checked. */
  public static final java.lang.String MSI_CHECK_DIGIT_MODE_SINGLE_MOD_11_PLUS_MOD_10_CHECK = "singleMod11PlusMod10Check";
  /** Two mod 10 checksum digits checked. */
  public static final java.lang.String MSI_CHECK_DIGIT_MODE_DOUBLE_MOD_10_CHECK = "doubleMod10Check";
  /** One mod 10 checksum digit checked and stripped from the result string. */
  public static final java.lang.String MSI_CHECK_DIGIT_MODE_SINGLE_MOD_10_CHECK_AND_STRIP = "singleMod10CheckAndStrip";
  /** One mod 11 checksum digit plus one mod 10 checksum digit checked and stripped from the result string. */
  public static final java.lang.String MSI_CHECK_DIGIT_MODE_SINGLE_MOD_11_PLUS_MOD_10_CHECK_AND_STRIP = "singleMod11PlusMod10CheckAndStrip";
  /** Two mod 10 checksum digits checked and stripped from the result string. */
  public static final java.lang.String MSI_CHECK_DIGIT_MODE_DOUBLE_MOD_10_CHECK_AND_STRIP = "doubleMod10CheckAndStrip";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_PDF_417_ENABLED = "DEC_PDF417_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_PDF_417_MINIMUM_LENGTH = "DEC_PDF417_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_PDF_417_MAXIMUM_LENGTH = "DEC_PDF417_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_QR_CODE_ENABLED = "DEC_QR_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_QR_CODE_MINIMUM_LENGTH = "DEC_QR_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_QR_CODE_MAXIMUM_LENGTH = "DEC_QR_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_RSS_ENABLED = "DEC_RSS_14_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_RSS_LIMITED_ENABLED = "DEC_RSS_LIMITED_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_RSS_EXPANDED_ENABLED = "DEC_RSS_EXPANDED_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_RSS_EXPANDED_MINIMUM_LENGTH = "DEC_RSS_EXPANDED_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_RSS_EXPANDED_MAXIMUM_LENGTH = "DEC_RSS_EXPANDED_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_STANDARD_25_ENABLED = "DEC_S25_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_STANDARD_25_MINIMUM_LENGTH = "DEC_S25_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_STANDARD_25_MAXIMUM_LENGTH = "DEC_S25_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_TELEPEN_ENABLED = "DEC_TELEPEN_ENABLED";
  /** Minimum code length for decoding. */
  public static final java.lang.String PROPERTY_TELEPEN_MINIMUM_LENGTH = "DEC_TELEPEN_MIN_LENGTH";
  /** Maximum code length for decoding. */
  public static final java.lang.String PROPERTY_TELEPEN_MAXIMUM_LENGTH = "DEC_TELEPEN_MAX_LENGTH";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_TELEPEN_OLD_STYLE_ENABLED = "DEC_TELEPEN_OLD_STYLE";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_TLC_39_ENABLED = "DEC_TLC39_ENABLED";
  /** Enable or disable. */
  public static final java.lang.String PROPERTY_TRIOPTIC_ENABLED = "DEC_TRIOPTIC_ENABLED";

}
