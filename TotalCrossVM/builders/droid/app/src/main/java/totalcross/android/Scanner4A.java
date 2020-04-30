// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android;

import totalcross.android.scanners.*;

/**
 * Scanner class for Android.
 */
public class Scanner4A {
  public static IScanner scanner;

  static boolean scannerActivate() {
    // Motorola/Symbol devices
    try {
      if (Class.forName("com.symbol.emdk.EMDKManager") != null) {
      }
    } catch (ClassNotFoundException e) {
    }

    if (scanner == null) {
      String id = Settings4A.deviceId.toLowerCase();
      if (id.contains("honeywell")) {
      } else if (id.equalsIgnoreCase("intermec") || id.contains("foxconn")) {
      }
    }
    return scanner != null ? scanner.scannerActivate() : false;
  }

  static boolean setBarcodeParam(int barcodeType, boolean enable) {
    return scanner != null && scanner.setBarcodeParam(barcodeType, enable);
  }

  static void setParam(String what, String value) {
    if (scanner != null) {
      scanner.setParam(what, value);
    }
  }

  static String getData() {
    return scanner == null ? null : scanner.getData();
  }

  static boolean deactivate() {
    return scanner != null && scanner.deactivate();
  }
}
