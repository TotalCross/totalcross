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

public interface Datalogic {
  /** To be used with Datalogic scanners, in the setParam method: scanning timeout, expressed in terms of milliseconds */
  public final static int SCAN_PARAM_TIMEOUT = 0x4000001;
  /** To be used with Datalogic scanners, in the setParam method: beeper duration on barcode event, expressed in terms of milliseconds */
  public final static int SCAN_PARAM_BEEPER_DURATION = 0x4000002;
  /** To be used with Datalogic scanners, in the setParam method: beeper frequency on barcode event, expressed in terms of hertz */
  public final static int SCAN_PARAM_BEEPER_FREQUENCY = 0x4000003;
  /** To be used with Datalogic scanners, in the setParam method: how many pulses we blink the led on barcode event */
  public final static int SCAN_PARAM_LED_NUM_OF_PULSES = 0x4000004;
  /** To be used with Datalogic scanners, in the setParam method: duration of each pulse on barcode event, expressed in terms of milliseconds */
  public final static int SCAN_PARAM_LED_PULSE_DURATION = 0x4000005;
  /** To be used with Datalogic scanners, in the setParam method: disable/enable the scanning continous mode, the scanning doesn't stop on barcode event */
  public final static int SCAN_PARAM_CONTINUOUS_MODE = 0x4000006;
  /** To be used with Datalogic scanners, in the setParam method: disable/enable the keyborad emulation, all scanned data are translated to keyboard events */
  public final static int SCAN_PARAM_KEYBOARD_EMULATION = 0x4000007;
  /** To be used with Datalogic scanners, in the setParam method: disable/enable the soft trigger functions. if enabled, allows the scanner to read, even if the scan button is not pressed */
  public final static int SCAN_PARAM_SOFT_TRIGGER = 0x4000008;
  /** To be used with Datalogic scanners, in the setParam method: if enabled, allows the scanner to read, even if the active() function has not been called. */
  public final static int SCAN_PARAM_SCAN_ALWAYS_ENABLED = 0x4000009;
  /** To be used with Datalogic scanners, in the setParam method: beeper type, dual-tone (0), monotone (1) */
  public final static int SCAN_PARAM_BEEPER_TYPE = 0x400000A;

  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_PREAMBLE_STRING = 0;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_POSTAMBLE_STRING = 1;

  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_NO_IDENTIFIER,
   1 - BCD_PARAM_DATALOGIC_IDENTIFIER_AT_BEGINNING,
   2 - BCD_PARAM_DATALOGIC_IDENTIFIER_AT_END,
   3 - BCD_PARAM_AIM_IDENTIFIER_AT_BEGINNING,
   4 - BCD_PARAM_AIM_IDENTIFIER_AT_END.
   */
  public final static int BCD_PARAM_BARCODE_SYMBOLOGY_IDENTIFIER = 2;

  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_MIN_SEVERITY,
   1 - BCD_PARAM_LOW_SEVERITY,
   2 - BCD_PARAM_MID_SEVERITY,
   3 - BCD_PARAM_HIGH_SEVERITY,
   4 - BCD_PARAM_MAX_SEVERITY.
   */
  public final static int BCD_PARAM_LINEAR_CODE_SEVERITY = 3;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_MIN_QUIET_ZONE,
   1 - BCD_PARAM_STANDARD_QUIET_ZONE.
   */
  public final static int BCD_PARAM_QUIET_ZONE_TYPE = 4;

  /** To be used with Datalogic scanners, in the setParam method: Code39*/
  public final static int BCD_PARAM_CODE_39_REDUNDANCY = 5;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_39_MIN_TEXT_LENGTH = 6;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_39_MAX_TEXT_LENGTH = 7;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_39_CIP_CONVERSION = 8;

  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_39_STANDARD = 9;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_39_STANDARD_CHECK_DIGIT = 10;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_39_STANDARD_CHECK_DIGIT_TRANSMISSION = 11;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_39_FULL_ASCII_CONVERSION = 12;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_39_FULL_ASCII_CHECK_DIGIT = 13;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_39_FULL_ASCII_CHECK_DIGIT_TRANSMISSION = 14;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_39_CODE_32_CONVERSION = 15;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_39_CODE_32_CHECK_DIGIT = 16;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_39_CODE_32_CHECK_DIGIT_TRANSMISSION = 17;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_39_CODE_32_REMOVE_A = 18;

  //Code2Of5
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_25_REDUNDANCY = 19;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_25_MIN_TEXT_LENGTH = 20;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_25_MAX_TEXT_LENGTH = 21;

  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_25_INTERLEAVED = 22;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_25_INTERLEAVED_CHECK_DIGIT = 23;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_25_INTERLEAVED_CHECK_DIGIT_TRANSMISSION = 24;

  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_25_INDUSTRIAL = 25;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_25_INDUSTRIAL_CHECK_DIGIT = 26;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_25_INDUSTRIAL_CHECK_DIGIT_TRANSMISSION = 27;

  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODE_25_MATRIX = 28;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_25_MATRIX_CHECK_DIGIT = 29;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_25_MATRIX_CHECK_DIGIT_TRANSMISSION = 30;

  //Plessey
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_PLESSEY = 31;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_PLESSEY_REDUNDANCY = 32;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_PLESSEY_MIN_TEXT_LENGTH = 33;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_PLESSEY_MAX_TEXT_LENGTH = 34;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_PLESSEY_CHECK_DIGIT = 35;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_PLESSEY_CHECK_DIGIT_TRANSMISSION = 36;

  //Codabar
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODABAR = 37;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODABAR_REDUNDANCY = 38;
  /** To be used with Datalogic scanners, in the setParam method. */
  public final static int BCD_PARAM_CODABAR_MIN_TEXT_LENGTH = 39;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODABAR_MAX_TEXT_LENGTH = 40;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODABAR_CHECK_DIGIT = 41;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODABAR_CHECK_DIGIT_TRANSMISSION = 42;

  //Code128
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_128_REDUNDANCY = 43;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_128_MIN_TEXT_LENGTH = 44;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_128_MAX_TEXT_LENGTH = 45;

  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_128_STANDARD = 46;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_128_STANDARD_CHECK_DIGIT = 47;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_128_STANDARD_CHECK_DIGIT_TRANSMISSION = 48;

  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_128_EAN = 49;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_128_EAN_CHECK_DIGIT = 50;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_128_EAN_CHECK_DIGIT_TRANSMISSION = 51;

  //Code11
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_11_REDUNDANCY = 52;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_11_MIN_TEXT_LENGTH = 53;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_11_MAX_TEXT_LENGTH = 54;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_11 = 55;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_11_CHECK_DIGIT_TYPE = 56;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_11_CHECK_DIGIT_TRANSMISSION = 57;

  //Code93
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_93_REDUNDANCY = 58;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_93_MIN_TEXT_LENGTH = 59;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_93_MAX_TEXT_LENGTH = 60;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_93 = 61;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_93_CHECK_DIGIT_TYPE = 62;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_93_CHECK_DIGIT_TRANSMISSION = 63;

  //CodeMSI
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_MSI_REDUNDANCY = 64;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_MSI_MIN_TEXT_LENGTH = 65;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_MSI_MAX_TEXT_LENGTH = 66;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_MSI = 67;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_MSI_CHECK_DIGIT_TYPE = 68;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_CODE_MSI_CHECK_DIGIT_TRANSMISSION = 69;

  //EAN_UPC
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_EAN_UPC_REDUNDANCY = 70;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_EAN_UPC_SUPPLEMENTAL_REDUNDANCY = 71;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_EAN_UPC_SUPPLEMENTAL_2 = 72;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_EAN_UPC_SUPPLEMENTAL_5 = 73;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_EAN_UPC_SUPPLEMENTAL_AUTODISCRIMINATE = 74;

  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_UPC_A = 75;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_UPC_A_CHECK_DIGIT = 76;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_UPC_A_CHECK_DIGIT_TRANSMISSION = 77;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_UPC_A_TO_EAN_13 = 78;

  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_UPC_E = 79;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_UPC_E_CHECK_DIGIT = 80;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_UPC_E_CHECK_DIGIT_TRANSMISSION = 81;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_UPC_E_CONVERSION_TYPE = 82;

  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_EAN_8 = 83;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_EAN_8_CHECK_DIGIT = 84;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_EAN_8_CHECK_DIGIT_TRANSMISSION = 85;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_EAN_8_TO_EAN_13 = 86;

  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_EAN_13 = 87;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_EAN_13_CHECK_DIGIT = 88;
  /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
  public final static int BCD_PARAM_EAN_13_CHECK_DIGIT_TRANSMISSION = 89;

  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_128_EAN_FIRST_GROUP_SEPARATOR = 90;
  /** To be used with Datalogic scanners = ; in the setParam method. */
  public final static int BCD_PARAM_CODE_128_EAN_GROUP_SEPARATOR_BYTE = 91;
}
