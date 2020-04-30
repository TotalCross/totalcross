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

public interface Handheld {
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int AZTEC = 0;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int CODABAR = 1;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int CODE11 = 2;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int CODE128 = 3;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int CODE39 = 4;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int CODE49 = 5;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int CODE93 = 6;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int COMPOSITE = 7;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int DATAMATRIX = 8;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int EAN8 = 9;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int EAN13 = 10;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int INT25 = 11;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int MAXICODE = 12;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int MICROPDF = 13;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int OCR = 14;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int PDF417 = 15;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int POSTNET = 16;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int QR = 17;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int RSS = 18;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int UPCA = 19;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int UPCE0 = 20;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int UPCE1 = 21;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int ISBT = 22;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int BPO = 23;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int CANPOST = 24;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int AUSPOST = 25;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int IATA25 = 26;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int CODABLOCK = 27;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int JAPOST = 28;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int PLANET = 29;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int DUTCHPOST = 30;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int MSI = 31;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int TLCODE39 = 32;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int TRIOPTIC = 33;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int CODE32 = 34;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int STRT25 = 35;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int MATRIX25 = 36;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int PLESSEY = 37;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int CHINAPOST = 38;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int KOREAPOST = 39;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int TELEPEN = 40;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int CODE16K = 41;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int POSICODE = 42;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int COUPONCODE = 43;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int USPS4CB = 44;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int IDTAG = 45;
  /** To be used HandHeld scanners, in the setBarcodeParam method. */
  public final static int LABEL = 46;

}
