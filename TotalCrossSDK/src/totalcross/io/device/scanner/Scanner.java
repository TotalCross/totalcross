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
 *     !Scanner.setBarcodeParam(Intermec.AZTEC, true) ||
 *     !Scanner.commitBarcodeParams())
 *    return;
 * ...
 * if (!Scanner.deactivate())
 *    return;
 * </pre>
 */

public class Scanner
{
   public static boolean isActive; // guich@200b4: avoid activate the scanner again
   private static String barcode;
   private static int tries;
   private static boolean driverLoaded;
   /** The scanner version. */
   public static String scanManagerVersion = Settings.onJavaSE ? "Scanner emulation" : "1.0";

   /** Set this listener to send all Scanner events to its onEvent method. */
   public static Control listener; // maybe theres another better way to do this...


   static
   {
      if (Settings.isWindowsDevice())
      {
         driverLoaded = Vm.attachNativeLibrary("Motorola") || 
                        Vm.attachNativeLibrary("Dolphin") || 
                        Vm.attachNativeLibrary("Intermec") || 
                        Vm.attachNativeLibrary("Pidion");
         if (!driverLoaded && tries++ == 0)
            throw new RuntimeException("Cannot find the native implementation for the scanner library.");
      }
   }

   /**
   * Activate the scanner. Return true if the scanner could be activated,
   * false otherwise.  (A false condition may arise if batteries have run
   * too low, or if the unit does not have the scan library installed.
   *
   * <p><b>Note</b>: On Android Intermec, you can't activate the scanner if it is off in the settings. Moreover, you should not set the scanner and 
   * virtual wedge settings off and then try to use the scanner. Even though your app won't crash or hang, the device might not behave properly. 
   * There is no way to check if the device settings are off in the settings or in the app. 
   */
   public static boolean activate()
   {
      isActive = scannerActivate();
      scanManagerVersion = getScanManagerVersion();
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
   * 
   * <p><b>Note</b>: On Android Intermec, you CAN'T set enable a barcode type the scanner is not activate. In this case, this method will return false.
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
    * <p> This method is ignored for Intermec scanners.
    */
  public static boolean setParam(int type, int barcodeType, int value) // guich@330_43
  {
     return true;
  }
  native public static boolean setParam4D(int type, int barcodeType, int value);
  
   /**
   * Commit the barcode parameters to the scanner. Returns true if the
   * operation is successful and false, otherwise.
   * <p> Not used on the Windows CE and Android platforms.
   *
   */
   public static boolean commitBarcodeParams()
   {
      return true;
   }
   native public static boolean commitBarcodeParams4D();

   /**  
    * Set the length of a barcode. 
    * 
    * <p> This method is ignored for Intermec scanners.
    * 
    * The lengthType must be one of the following values:
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
   * 
   * <p><b>Note</b>: On Android Intermec, you should first set some parameters before fetching data or else it might crash after 10 or 15 reads.
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
    *  See the TotalCross API / Scanner Camera sample.
    */
   public static String readBarcode(String mode)
   {
      return null;
   }
   native public static String readBarcode4D(String mode);
}