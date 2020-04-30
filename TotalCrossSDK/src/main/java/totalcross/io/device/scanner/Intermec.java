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

/**
 * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method.
 */

public interface Intermec {
  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Australian Post barcode type.
   */
  public final static int AUSTRALIAN_POST = 1;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Aztec barcode type.
   */
  public final static int AZTEC = 2;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable BPO barcode type.
   */
  public final static int BPO = 3;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Canada Post barcode type.
   */
  public final static int CANADA_POST = 4;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Codabar barcode type.
   */
  public final static int CODABAR = 5;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Codablock A barcode type.
   */
  public final static int CODABLOCK_A = 6;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Codablock F barcode type.
   */
  public final static int CODABLOCK_F = 7;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Code 11 barcode type.
   */
  public final static int CODE_11 = 8;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Code 128 barcode type.
   */
  public final static int CODE_128 = 9;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Code GS1-128 barcode type.
   */
  public final static int CODE_GS1_128 = 10;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Code ISBT-128 barcode type.
   */
  public final static int CODE_ISBT_128 = 11;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Code 39 barcode type.
   */
  public final static int CODE_39 = 12;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Code 93 barcode type.
   */
  public final static int CODE_93 = 13;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Data Matrix barcode type.
   */
  public final static int DATA_MATRIX = 14;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Dutch Post barcode type.
   */
  public final static int DUTCH_POST = 15;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable EAN/UPC EAN-13 barcode type.
   */
  public final static int EAN_UPC_EAN_13 = 16;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable EAN/UPC EAN-8 barcode type.
   */
  public final static int EAN_UPC_EAN_8 = 17;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable EAN/UPC UPCA barcode type.
   */
  public final static int EAN_UPC_UPCA = 18;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable EAN/UPC UPC-E barcode type.
   */
  public final static int EAN_UPC_UPC_E = 19;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable EAN/UPC UPC-E1 barcode type.
   */
  public final static int EAN_UPC_UPC_E1 = 20;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable GS1 Composite barcode type.
   */
  public final static int GS1_COMPOSITE = 21;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable GS1 Composite C barcode type.
   */
  public final static int GS1_COMPOSITE_C = 22;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable GS1 DataBar Expanded barcode type.
   */
  public final static int GS1_DATA_BAR_EXPANDED = 23;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable GS1 DataBar Limited barcode type.
   */
  public final static int GS1_DATA_BAR_LIMITED = 24;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable GS1 DataBar Omnidirectional barcode type.
   */
  public final static int GS1_OMINI_DIRECTIONAL = 25;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable HanXin barcode type.
   */
  public final static int HAN_XIN = 26;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Infomail barcode type.
   */
  public final static int INFOMAIL = 27;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Intelligent Mail barcode type.
   */
  public final static int INTELLIGENT_MAIL = 28;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Interleaved 2 of 5 barcode type.
   */
  public final static int INTERLEAVED_2_OF_5 = 29;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Japan Post barcode type.
   */
  public final static int JAPAN_POST = 30;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Matrix 2 of 5 barcode type.
   */
  public final static int MATRIX_2_OF_5 = 31;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Maxicode barcode type.
   */
  public final static int MAXICODE = 32;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Micro PDF 417 barcode type.
   */
  public final static int MICRO_PDF_417 = 33;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable MSI barcode type.
   */
  public final static int MSI = 34;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable PDF 417 barcode type.
   */
  public final static int PDF_417 = 35;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Planet barcode type.
   */
  public final static int PLANET = 36;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Plessey barcode type.
   */
  public final static int PLESSEY = 37;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Postnet barcode type.
   */
  public final static int POSTNET = 38;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable QR Code barcode type.
   */
  public final static int QR_CODE = 39;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Standard 2 of 5 barcode type.
   */
  public final static int STANDARD_2_OF_5 = 40;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Sweden Post barcode type.
   */
  public final static int SWEDEN_POST = 41;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable Telepen barcode type.
   */
  public final static int TELEPEN = 42;

  /**
   * To be used in the <code>setBarcodeParam()</code> method, to enable or disable TLC 39 barcode type.
   */
  public final static int TLC_39 = 43;
}
