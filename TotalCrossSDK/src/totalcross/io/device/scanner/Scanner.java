/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000 Tom Cuthill                                               *
 *  Copyright (C) 2003 Dave Slaughter <dslaughter@safeway.co.uk>                 *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.io.device.scanner;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;

/**
 * Scanner accesses some popular barcode scanners.
 * <p>
 * Scanner access is only available when running on a native TotalCross VM; it
 * is not supported when running on Java. It is, though, emulated on Java:
 * a popup dialog will ask you for a barcode symbol which will be returned every time
 * the <code>getData</code> method is called.
 * <p>
 * Since there is only one scanner per device, the scanner class is static.
 * Activating the scanner causes the physical scanner to be powered, and
 * enables the trigger. Deactivating the scanner removes the power from the
 * scanner and prevents scans from taking place. The scanner should always
 * be deactivated at the end of processing to prevent excessive battery drain.
 * <p>
 * When the scanner is activated, scan events will appear in the MainWindow's
 * onEvent method.  The scan events will contain a string describing either
 * the item scanned or a battery error. Deactivating the scanner prevents
 * scan events from being delivered.
 * <p>
 * You can change this behaviour on some devices by calling setContinuousScanning(false),
 * which will cause each call to activate() to only allow a single scan.
 * <p>
 * Since barcodes can have many formats, this class includes a method to
 * register the barcode types with the scanner. These types must each be
 * individually set as parameters to the scanner.  After all the parameter
 * types have been set, the commitBarcodeParams method must be called to
 * register the parameters with the scanner.  Once this is done the scanner
 * is able to decode the specified barcode types.
 * <p>
 * A typical processing sequence is given below:
 * <pre>
 * if (!Scanner.activate() ||
 *     !Scanner.setBarcodeParam(Scanner.BARCODABAR, true) ||
 *     !Scanner.setBarcodeParam(Scanner.BARBOOKLAND_EAN, true) ||
 *     !Scanner.commitBarcodeParams())
 *    return;
 * ...
 * if (!Scanner.deactivate())
 *    return;
 *
 * </pre>
 * To make a Symbol MC3000 work with the I2OF5 barcode, you must call:
 * <pre>
 * // MUST BE CALLED *** BEFORE *** SCANNER.ACTIVATE !!!!
 * Scanner.setParam(Scanner.WAITING_TIME_PARAM, 0, 3000); // 3rd parameter must be something > 0!
 * if (Scanner.activate())
 * {
 *    Scanner.setBarcodeParam(Scanner.BARI2OF5, true);
 *    Scanner.setBarcodeLength(Scanner.BARI2OF5, 2, 8, 8);
 *    ...
 * </pre>
 */

public class Scanner
{
   /*
    * Barcode types
    */

   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARCODE39           = 0;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARUPCA             = 1;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARUPCE             = 2;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BAREAN13            = 3;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BAREAN8             = 4;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARD2OF5            = 5;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARI2OF5            = 6;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARCODABAR          = 7;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARCODE128          = 8;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARCODE93           = 9;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARMSI_PLESSEY      = 11;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARUPCE1            = 12;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARTRIOPTICCODE39   = 13;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARUCC_EAN128       = 14;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARCODE11           = 15; // guich@tc136
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARBOOKLAND_EAN     = 83;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARISBT128          = 84;
   /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
   public final static int BARUPCEANCOUPONCODE = 85;

   /** To be used in the setParam method */
   public final static int TRIGGERING_PARAM             = 1;
   /** To be used in the setParam method */
   public final static int LINEAR_SECURITY_LEVEL_PARAM  = 2;
   /** To be used in the setParam method */
   public final static int SUPPLEMENTALS_PARAM          = 3;
   /** To be used in the setParam method. Needs barcodeType. */
   public final static int TRANSMIT_CHECKDIGIT_PARAM    = 4;
   /** To be used in the setParam method. Needs barcodeType. */
   public final static int PREAMBLE_PARAM               = 5;
   /** To be used in the setParam method */
   public final static int MSI_PLESSEY_CHECKDIGIT_PARAM = 7;
   /** To be used in the setParam method */
   public final static int MSI_PLESSEY_OPTIONS_PARAM    = 8;
   /** To be used in the setParam method */
   public final static int MSI_PLESSEY_ALGORITHMS_PARAM = 9;
   /** To be used in the setParam method */
   public final static int TRANSMIT_CODEID_PARAM        = 10;
   /** To be used in the setParam method */
   public final static int SCAN_DATA_TRANSMISSION_PARAM = 12;
   /** To be used in the setParam method */
   public final static int SCAN_ANGLE_PARAM             = 13;
   /** To be used in the setParam method for Windows CE only. Sets the time that the scanner thread will wait for a beam.
    * if value is <= 0, it waits FOREVER. Otherwise, waits for the given number of milisseconds. <b>MUST BE CALLED BEFORE SCANNER.ACTIVATE !!!</b>*/
   public final static int WAITING_TIME_PARAM           = -999; // guich@580_22

   /** To be used with Datalogic scanners, in the setParam method: scanning timeout, expressed in terms of milliseconds */
   public final static int DL_SCAN_PARAM_TIMEOUT             = 0x4000001;
   /** To be used with Datalogic scanners, in the setParam method: beeper duration on barcode event, expressed in terms of milliseconds */
   public final static int DL_SCAN_PARAM_BEEPER_DURATION     = 0x4000002;
   /** To be used with Datalogic scanners, in the setParam method: beeper frequency on barcode event, expressed in terms of hertz */
   public final static int DL_SCAN_PARAM_BEEPER_FREQUENCY    = 0x4000003;
   /** To be used with Datalogic scanners, in the setParam method: how many pulses we blink the led on barcode event */
   public final static int DL_SCAN_PARAM_LED_NUM_OF_PULSES   = 0x4000004;
   /** To be used with Datalogic scanners, in the setParam method: duration of each pulse on barcode event, expressed in terms of milliseconds */
   public final static int DL_SCAN_PARAM_LED_PULSE_DURATION  = 0x4000005;
   /** To be used with Datalogic scanners, in the setParam method: disable/enable the scanning continous mode, the scanning doesn't stop on barcode event */
   public final static int DL_SCAN_PARAM_CONTINUOUS_MODE     = 0x4000006;
   /** To be used with Datalogic scanners, in the setParam method: disable/enable the keyborad emulation, all scanned data are translated to keyboard events */
   public final static int DL_SCAN_PARAM_KEYBOARD_EMULATION  = 0x4000007;
   /** To be used with Datalogic scanners, in the setParam method: disable/enable the soft trigger functions. if enabled, allows the scanner to read, even if the scan button is not pressed */
   public final static int DL_SCAN_PARAM_SOFT_TRIGGER        = 0x4000008;
   /** To be used with Datalogic scanners, in the setParam method: if enabled, allows the scanner to read, even if the active() function has not been called. */
   public final static int DL_SCAN_PARAM_SCAN_ALWAYS_ENABLED = 0x4000009;
   /** To be used with Datalogic scanners, in the setParam method: beeper type, dual-tone (0), monotone (1) */
   public final static int DL_SCAN_PARAM_BEEPER_TYPE         = 0x400000A;

   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_AZTEC        = 0 ;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_CODABAR      = 1 ;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_CODE11       = 2 ;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_CODE128      = 3 ;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_CODE39       = 4 ;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_CODE49       = 5 ;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_CODE93       = 6 ;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_COMPOSITE    = 7 ;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_DATAMATRIX   = 8 ;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_EAN8         = 9 ;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_EAN13        = 10;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_INT25        = 11;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_MAXICODE     = 12;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_MICROPDF     = 13;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_OCR          = 14;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_PDF417       = 15;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_POSTNET      = 16;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_QR           = 17;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_RSS          = 18;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_UPCA         = 19;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_UPCE0        = 20;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_UPCE1        = 21;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_ISBT         = 22;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_BPO          = 23;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_CANPOST      = 24;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_AUSPOST      = 25;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_IATA25       = 26;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_CODABLOCK    = 27;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_JAPOST       = 28;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_PLANET       = 29;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_DUTCHPOST    = 30;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_MSI          = 31;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_TLCODE39     = 32;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_TRIOPTIC     = 33;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_CODE32       = 34;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_STRT25       = 35;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_MATRIX25     = 36;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_PLESSEY      = 37;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_CHINAPOST    = 38;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_KOREAPOST    = 39;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_TELEPEN      = 40;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_CODE16K      = 41;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_POSICODE     = 42;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_COUPONCODE   = 43;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_USPS4CB      = 44;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_IDTAG        = 45;
   /** To be used HandHeld scanners, in the setBarcodeParam method. */
   public final static int HH_LABEL        = 46;

   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_PREAMBLE_STRING = 0;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_POSTAMBLE_STRING = 1;

   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_NO_IDENTIFIER,
   1 - BCD_PARAM_DATALOGIC_IDENTIFIER_AT_BEGINNING,
   2 - BCD_PARAM_DATALOGIC_IDENTIFIER_AT_END,
   3 - BCD_PARAM_AIM_IDENTIFIER_AT_BEGINNING,
   4 - BCD_PARAM_AIM_IDENTIFIER_AT_END.
   */
   public final static int DL_BCD_PARAM_BARCODE_SYMBOLOGY_IDENTIFIER = 2;

   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_MIN_SEVERITY,
   1 - BCD_PARAM_LOW_SEVERITY,
   2 - BCD_PARAM_MID_SEVERITY,
   3 - BCD_PARAM_HIGH_SEVERITY,
   4 - BCD_PARAM_MAX_SEVERITY.
   */
   public final static int DL_BCD_PARAM_LINEAR_CODE_SEVERITY = 3;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_MIN_QUIET_ZONE,
   1 - BCD_PARAM_STANDARD_QUIET_ZONE.
   */
   public final static int DL_BCD_PARAM_QUIET_ZONE_TYPE = 4;

   /** To be used with Datalogic scanners, in the setParam method: Code39*/
   public final static int DL_BCD_PARAM_CODE_39_REDUNDANCY = 5;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_39_MIN_TEXT_LENGTH = 6;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_39_MAX_TEXT_LENGTH = 7;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_39_CIP_CONVERSION = 8;

   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_39_STANDARD = 9;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_39_STANDARD_CHECK_DIGIT = 10;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_39_STANDARD_CHECK_DIGIT_TRANSMISSION = 11;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_39_FULL_ASCII_CONVERSION = 12;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_39_FULL_ASCII_CHECK_DIGIT = 13;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_39_FULL_ASCII_CHECK_DIGIT_TRANSMISSION = 14;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_39_CODE_32_CONVERSION = 15;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_39_CODE_32_CHECK_DIGIT = 16;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_39_CODE_32_CHECK_DIGIT_TRANSMISSION = 17;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_39_CODE_32_REMOVE_A = 18;

   //Code2Of5
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_25_REDUNDANCY = 19;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_25_MIN_TEXT_LENGTH = 20;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_25_MAX_TEXT_LENGTH = 21;

   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_25_INTERLEAVED = 22;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_25_INTERLEAVED_CHECK_DIGIT = 23;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_25_INTERLEAVED_CHECK_DIGIT_TRANSMISSION = 24;

   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_25_INDUSTRIAL = 25;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_25_INDUSTRIAL_CHECK_DIGIT = 26;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_25_INDUSTRIAL_CHECK_DIGIT_TRANSMISSION = 27;

   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_25_MATRIX = 28;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_25_MATRIX_CHECK_DIGIT = 29;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_25_MATRIX_CHECK_DIGIT_TRANSMISSION = 30;

   //Plessey
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_PLESSEY = 31;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_PLESSEY_REDUNDANCY = 32;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_PLESSEY_MIN_TEXT_LENGTH = 33;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_PLESSEY_MAX_TEXT_LENGTH = 34;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_PLESSEY_CHECK_DIGIT = 35;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_PLESSEY_CHECK_DIGIT_TRANSMISSION = 36;

   //Codabar
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODABAR = 37;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODABAR_REDUNDANCY = 38;
   /** To be used with Datalogic scanners, in the setParam method. */
   public final static int DL_BCD_PARAM_CODABAR_MIN_TEXT_LENGTH = 39;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODABAR_MAX_TEXT_LENGTH = 40;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODABAR_CHECK_DIGIT = 41;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODABAR_CHECK_DIGIT_TRANSMISSION = 42;

   //Code128
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_128_REDUNDANCY = 43;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_128_MIN_TEXT_LENGTH = 44;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_128_MAX_TEXT_LENGTH = 45;

   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_128_STANDARD = 46;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_128_STANDARD_CHECK_DIGIT = 47;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_128_STANDARD_CHECK_DIGIT_TRANSMISSION = 48;

   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_128_EAN = 49;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_128_EAN_CHECK_DIGIT = 50;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_128_EAN_CHECK_DIGIT_TRANSMISSION = 51;

   //Code11
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_11_REDUNDANCY = 52;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_11_MIN_TEXT_LENGTH = 53;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_11_MAX_TEXT_LENGTH = 54;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_11 = 55;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_11_CHECK_DIGIT_TYPE = 56;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_11_CHECK_DIGIT_TRANSMISSION = 57;

   //Code93
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_93_REDUNDANCY = 58;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_93_MIN_TEXT_LENGTH = 59;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_93_MAX_TEXT_LENGTH = 60;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_93 = 61;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_93_CHECK_DIGIT_TYPE = 62;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_93_CHECK_DIGIT_TRANSMISSION = 63;

   //CodeMSI
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_MSI_REDUNDANCY = 64;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_MSI_MIN_TEXT_LENGTH = 65;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_MSI_MAX_TEXT_LENGTH = 66;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_MSI = 67;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_MSI_CHECK_DIGIT_TYPE = 68;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_CODE_MSI_CHECK_DIGIT_TRANSMISSION = 69;

   //EAN_UPC
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_EAN_UPC_REDUNDANCY = 70;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_EAN_UPC_SUPPLEMENTAL_REDUNDANCY = 71;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_EAN_UPC_SUPPLEMENTAL_2 = 72;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_EAN_UPC_SUPPLEMENTAL_5 = 73;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_EAN_UPC_SUPPLEMENTAL_AUTODISCRIMINATE = 74;

   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_UPC_A = 75;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_UPC_A_CHECK_DIGIT = 76;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_UPC_A_CHECK_DIGIT_TRANSMISSION = 77;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_UPC_A_TO_EAN_13 = 78;

   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_UPC_E = 79;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_UPC_E_CHECK_DIGIT = 80;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_UPC_E_CHECK_DIGIT_TRANSMISSION = 81;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_UPC_E_CONVERSION_TYPE = 82;

   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_EAN_8 = 83;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_EAN_8_CHECK_DIGIT = 84;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_EAN_8_CHECK_DIGIT_TRANSMISSION = 85;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_EAN_8_TO_EAN_13 = 86;

   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_EAN_13 = 87;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_EAN_13_CHECK_DIGIT = 88;
   /** To be used with Datalogic scanners, in the setParam method. Possible values:
   0 - BCD_PARAM_CODE_CHECK_C,
   1 - BCD_PARAM_CODE_CHECK_K,
   2 - BCD_PARAM_CODE_CHECK_C_AND_K,
   3 - BCD_PARAM_CODE_CHECK_NONE.
   */
   public final static int DL_BCD_PARAM_EAN_13_CHECK_DIGIT_TRANSMISSION = 89;

   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_128_EAN_FIRST_GROUP_SEPARATOR = 90;
   /** To be used with Datalogic scanners = ; in the setParam method. */
   public final static int DL_BCD_PARAM_CODE_128_EAN_GROUP_SEPARATOR_BYTE = 91;

   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_CODE11             = 0 ;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_CODE128            = 1 ;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_CODE39             = 2 ;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_CODE49             = 3 ;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_CODE93             = 4 ;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_CODE39_FULL_ASCII  = 5 ;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_TRIOPTIC_CODE39    = 6 ;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_UPC_A              = 7 ;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_UPC_E              = 8 ;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_UPC_E0             = 9 ;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_UPC_E1             = 10;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_EAN_8              = 11;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_EAN_13             = 12;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_BOOKLAND_EAN       = 13;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_UCC_EAN_128        = 14;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_CODABAR            = 15;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_CODABLOCK          = 16;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_DISCRETE_2_5       = 17;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_IATA_2_5           = 18;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_INTERLEAVED_2_5    = 19;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_ISBT               = 20;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_MATRIX_2_5         = 21;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_MSI_PLESSEY        = 22;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_RSS14              = 23;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_TLCODE39           = 24;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_AUSTRALIAN_POSTAL  = 25;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_AZTEC              = 26;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_AZTEC_MESA         = 27;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_BRITISH_POSTAL     = 28;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_CANADIAN_POSTAL    = 29;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_DATAMATRIX_CODE    = 30;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_DUTCH_POSTAL       = 31;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_JAPANESE_POSTAL    = 32;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_MAXICODE           = 33;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_MICROPDF           = 34;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_OCR                = 35;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_PDF                = 36;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_QR_CODE            = 37;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_US_PLANET_POSTAL   = 38;
   /** To be used SocketScan scanners, in the setBarcodeParam method. */
   public final static int SOCKET_US_POSTNET_POSTAL  = 39;

   public static boolean isActive; // guich@200b4: avoid activate the scanner again

   private static boolean driverLoaded;
   private static boolean scannerIsPassive;
   private static String barcode;
   private static String scanManagerVersion;
   private static int tries;

   static
   {
      if (Settings.isWindowsDevice())
      {
         driverLoaded = Vm.attachNativeLibrary("SocketScan") || 
                        Vm.attachNativeLibrary("Motorola") || 
                        Vm.attachNativeLibrary("Symbol") || 
                        Vm.attachNativeLibrary("Dolphin") || 
                        Vm.attachNativeLibrary("OpticonH16") || 
                        Vm.attachNativeLibrary("OpticonH16") || 
                        Vm.attachNativeLibrary("Intermec") || 
                        Vm.attachNativeLibrary("Bematech");
         if (!driverLoaded && tries++ == 0)
            throw new RuntimeException("Cannot find the native implementation for the scanner library.");
      }
   }

   /**
    * Flag which can be set to false so activate() only performs one scan
    * <p>
    * Note: the Symbol Scanner API differs on Palm and PocketPC (the Java
    * emulation works the same as PocketPC). On Palm activating the scanner
    * will allow the user to scan any number of barcodes until the scanner is
    * deactivated. On Java/PocketPC activating the Scanner only schedules
    * a single scan.
    * <p>
    * We obviously need this class behave in the same way on both platforms.  Rather than
    * forcing one choice on the user, the continuousScanning flag allows the user
    * to choose which mode they want.
    */
   public static boolean continousScanning = true;

   /** Set this listener to send all Scanner events to its onEvent method. */
   public static Control listener; // maybe theres another better way to do this...

   /**
   * Activate the scanner. Return true if the scanner could be activated,
   * false otherwise.  (A false condition may arise if batteries have run
   * too low, or if the unit does not have the scan library installed.
   */
   public static boolean activate()
   {
      isActive = scannerActivate();
      scanManagerVersion = getScanManagerVersion();
      scannerIsPassive = isActive && scanManagerVersion != null && (scanManagerVersion.equals("SocketScan") || scanManagerVersion.equals("SocketCom")); // guich@582_18: moved to here, after the activate
      return isActive;
   }

   static boolean scannerActivate()
   {
      InputBox id = new InputBox("Barcode emulation","Please enter the barcode:","");
      id.popup();
      barcode = id.getValue();
      _onEvent(ScanEvent.SCANNED);
      return true;
   }
   native static boolean scannerActivate4D();

   /**
   * Set a scanner parameter defining whether a barcode format will be used
   * in scanning.  Choose from one of the barcode types given above.
   * @param barcodeType the type of barcode under consideration
   * @param enable a flag to enable or disable decoding barcodes of this type.
   */
   public static boolean setBarcodeParam(int barcodeType, boolean enable)
   {
      return true;
   }
   native public static boolean setBarcodeParam4D(int barcodeType, boolean enable);

   /**
     * Set a parameter for the barcode. You can use the xxx_PARAM constants to
     * know which parameter to set, and also the values below for their values.
     * Be careful because some functions may require also the barcode type.
     * <p>
     * With Datalogic scanners, the SECOND parameter is not used (always pass 0 to it);
     * and don't forget to call the commitBarcodeParams method.
     * <p> Values valid for SYMBOL scanners:
     * <pre>
     *    // triggering modes
     *    #define LEVEL                                       0x00
     *    #define PULSE                                       0x02
     *    #define HOST                                        0x08
     *
     *    // Linear code type security
     *    #define SECURITY_LEVEL0                             0x00
     *    #define SECURITY_LEVEL1                             0x01
     *    #define SECURITY_LEVEL2                             0x02
     *    #define SECURITY_LEVEL3                             0x03
     *    #define SECURITY_LEVEL4                             0x04
     *
     *    // UPC/EAN Supplementals
     *    #define IGNORE_SUPPLEMENTALS                        0x00
     *    #define DECODE_SUPPLEMENTALS                        0x01
     *    #define AUTODISCRIMINATE_SUPPLEMENTALS              0x02
     *
     *    // Transmit Check Digit options
     *    #define DO_NOT_TRANSMIT_CHECK_DIGIT                 0x00
     *    #define TRANSMIT_CHECK_DIGIT                        0x01
     *
     *    // Preamble options
     *    #define NO_PREAMBLE                                 0x00
     *    #define SYSTEM_CHARACTER                            0x01
     *    #define SYSTEM_CHARACTER_COUNTRY_CODE               0x02
     *
     *    // CheckDigit verification options
     *    #define DISABLE_CHECK_DIGIT                         0x00
     *    #define USS_CHECK_DIGIT                             0x01
     *    #define OPCC_CHECK_DIGIT                            0x02
     *
     *    // MSI Plessey checkdigit options
     *    #define ONE_CHECK_DIGIT                             0x00
     *    #define TWO_CHECK_DIGITS                            0x01
     *
     *    // MSI Plessey check digit algorithms
     *    #define MOD10_MOD11                                 0x00
     *    #define MOD10_MOD10                                 0x01
     *
     *    // Transmit Code ID Character options
     *    #define AIM_CODE_ID_CHARACTER                       0x01
     *    #define SYMBOL_CODE_ID_CHARACTER                    0x02
     *
     *    // Scan data transmission formats
     *    #define DATA_AS_IS                                  0x00
     *    #define DATA_SUFFIX1                                0x01
     *    #define DATA_SUFFIX2                                0x02
     *    #define DATA_SUFFIX1_SUFFIX2                        0x03
     *    #define PREFIX_DATA                                 0x04
     *    #define PREFIX_DATA_SUFFIX1                         0x05
     *    #define PREFIX_DATA_SUFFIX2                         0x06
     *    #define PREFIX_DATA_SUFFIX1_SUFFIX2                 0x07
     *
     *    // Scan angle options
     *    #define SCAN_ANGLE_WIDE                             0xB6
     *    #define SCAN_ANGLE_NARROW                           0xB5
     * </pre>
     */
   public static boolean setParam(int type, int barcodeType, int value) // guich@330_43
   {
      return true;
   }
   native public static boolean setParam4D(int type, int barcodeType, int value);

   /**
   * Commit the barcode parameters to the scanner. Returns true if the
   * operation is successful and false, otherwise.
   * Not used on the Windows CE platform.
   *
   */
   public static boolean commitBarcodeParams()
   {
      return true;
   }
   native public static boolean commitBarcodeParams4D();

   /**  Set the length of a barcode. The lengthType must be one of the following values:
    * @param barcodeType One of the BARxxxx constants
    * @param lengthType 0 (variables length, min, and max are ignored), 1 (length = min), 2 (length = min || length = max), 3 (min <= length <= max). Careful: in Motorola Scanners, min and max must be used in inverted order due to a bug in the MOTOROLA API.
    * @param min The minimum value
    * @param max The maximum value
    */
   public static boolean setBarcodeLength(int barcodeType, int lengthType, int min, int max) // guich@330_43
   {
      return true;
   }
   native public static boolean setBarcodeLength4D(int barcodeType, int lengthType, int min, int max);

   /**
   * Get the decoded string of what has been scanned.  If an error occurs a
   * null String will be returned.
   */
   public static String getData()
   {
      return barcode;
   }
   native public static String getData4D();

   /**
   * Get the scan manager version as an hexadecimal String. If an error occurs or if this method
   * is called before the Scanner is initialized, a null String will be returned.
   */
   public static String getScanManagerVersion()
   {
      return "Scanner emulation";
   }
   native public static String getScanManagerVersion4D(); //return !isActive?null:scannerGetScanManagerVersion();

   /**
   * Get the Scanner Port Driver version as an hexadecimal string. If an error occurs or if this method
   * is called before the Scanner is initialized, a null string will be returned.
   */
   public static String getScanPortDriverVersion()
   {
      return null;
   }
   native public static String getScanPortDriverVersion4D(); //   !isActive?null:scannerGetScanPortDriverVersion();

   /**
   * Deactivate the scanner. Returns true if the operation is successful
   * and false, otherwise.
   */
   public static boolean deactivate()
   {
      isActive = false;
      return true;
   }
   native public static boolean deactivate4D(); //if (isActive) isActive = !scannerDeactivate(); return !isActive;

   private static ScanEvent se = new ScanEvent();
   
   /** Dispatch the event to the current listener (associated through the
   * <code>listener</code> public member. Called from the native library.
   * If there are no assigned listeners, the event is sent to the top most window,
   * and the window is validated.
   */
   protected static void _onEvent(int type)
   {
      Control dest = listener;
      if (dest == null)
         dest = Window.getTopMost();
      // convert from the type to the internal event id
      int id = type == 2 ? ScanEvent.BATTERY_ERROR :
               type == 1 ? ScanEvent.SCANNED
                 /* 0 */ : ScanEvent.TRIGGERED;
      // dispatch to the listener
      se.update(id);
      dest.postEvent(se);
      Window win = dest.getParentWindow(); // guich@400_44
      if (win != null)
         win.validate();
      // If we are running on a PocketPC unit, or under Java emulation, then calling
      // activate() on the Symbol Scanner API only schedules a single scan.
      // So, if continuousScanning is true we need to schedule another scan.
      if (continousScanning)
         activate();
      else
         isActive = false;
   }

   /** Returns true if this scanner is a passive one. Passive scanners must be triggered programatically, using the
    * trigger method. Currently, all SocketCom scanners are passive, and all others are not.
    * @since SuperWaba 5.7
    */
   public static boolean isPassive() // guich@570_43
   {
      return scannerIsPassive;
   }

   /** Triggers the scanner for passive scanners. The barcode will be returned in the ScanEvent event.
    * @since SuperWaba 5.7
    */
   public static boolean trigger() // guich@570_43
   {
      return scannerIsPassive && Scanner.setParam(0,0,0);
   }
 
   /** Reads a barcode using ZXing for Android.
    * 
    *  The mode can be one of:
    *  <ul>
    *  <li> 1D - for one dimension barcodes
    *  <li> 2D - for QR codes
    *  <li> empty string - for both
    *  </ul>
    *  
    *  If an error happens, it is returned prefixed with ***.
    *  
    *  See the tc.samples.io.device.zxing.ZXingScanner sample.
    */
   public static String readBarcode(String mode)
   {
      return null;
   }
   native public static String readBarcode4D(String mode);
}