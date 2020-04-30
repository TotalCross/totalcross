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

public interface Symbol {
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARCODE39 = 0;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARUPCA = 1;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARUPCE = 2;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BAREAN13 = 3;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BAREAN8 = 4;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARD2OF5 = 5;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARI2OF5 = 6;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARCODABAR = 7;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARCODE128 = 8;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARCODE93 = 9;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARMSI_PLESSEY = 11;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARUPCE1 = 12;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARTRIOPTICCODE39 = 13;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARUCC_EAN128 = 14;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARCODE11 = 15; // guich@tc136
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARBOOKLAND_EAN = 83;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARISBT128 = 84;
  /** To be used in the setBarcodeParam method with Symbol scanners; for SocketCom ones, see the method for the avail codes. */
  public final static int BARUPCEANCOUPONCODE = 85;

  /** To be used in the setParam method */
  public final static int TRIGGERING_PARAM = 1;
  /** To be used in the setParam method */
  public final static int LINEAR_SECURITY_LEVEL_PARAM = 2;
  /** To be used in the setParam method */
  public final static int SUPPLEMENTALS_PARAM = 3;
  /** To be used in the setParam method. Needs barcodeType. */
  public final static int TRANSMIT_CHECKDIGIT_PARAM = 4;
  /** To be used in the setParam method. Needs barcodeType. */
  public final static int PREAMBLE_PARAM = 5;
  /** To be used in the setParam method */
  public final static int MSI_PLESSEY_CHECKDIGIT_PARAM = 7;
  /** To be used in the setParam method */
  public final static int MSI_PLESSEY_OPTIONS_PARAM = 8;
  /** To be used in the setParam method */
  public final static int MSI_PLESSEY_ALGORITHMS_PARAM = 9;
  /** To be used in the setParam method */
  public final static int TRANSMIT_CODEID_PARAM = 10;
  /** To be used in the setParam method */
  public final static int SCAN_DATA_TRANSMISSION_PARAM = 12;
  /** To be used in the setParam method */
  public final static int SCAN_ANGLE_PARAM = 13;
  /** To be used in the setParam method for Windows CE only. Sets the time that the scanner thread will wait for a beam.
   * if value is <= 0, it waits FOREVER. Otherwise, waits for the given number of milisseconds. <b>MUST BE CALLED BEFORE SCANNER.ACTIVATE !!!</b>*/
  public final static int WAITING_TIME_PARAM = -999; // guich@580_22
}
