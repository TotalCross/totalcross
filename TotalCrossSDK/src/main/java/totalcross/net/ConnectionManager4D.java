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

package totalcross.net;

import totalcross.io.IOException;

public class ConnectionManager4D {
  static Object connRef;

  static ConnectionManager4D CmInstance = new ConnectionManager4D();

  public static final int CRADLE = 1;
  public static final int WIFI = 2;
  public static final int CELLULAR = 3;
  public static final int GPRS = CELLULAR;

  private ConnectionManager4D() {
    loadResources();
  }

  native private void loadResources();

  native public static void setDefaultConfiguration(int type, String cfg) throws IOException;

  native public static boolean isAvailable(int type) throws IOException;

  public static void open() throws IOException {
    IOException firstEx = null;
    int[] conn = new int[] { CRADLE, WIFI, CELLULAR };

    for (int i = 0; i < conn.length; i++) {
      try {
        if (isAvailable(conn[i])) {
          open(conn[i]);
          return; // successfully opened, so just return
        }
      } catch (IOException ex) {
        if (firstEx == null) {
          firstEx = ex;
        }
      }
    }

    throw firstEx == null ? new IOException("No connections available") : firstEx;
  }

  native public static void open(int type) throws IOException;

  /*
   * flsobral@tc123_20
   *    fixed bug in ConnectionManager.close implementation for PalmOS: Using this method without first closing all
   *    open socket connections would eventually lead to an unrecoverable error, usually causing the device to reset.
   */
  public static void close() throws IOException {
    nativeClose();
  }

  native private static void nativeClose() throws IOException;

  native private void releaseResources();

  @Override
  protected void finalize() {
    releaseResources();
  }

  native public static String getHostAddress(String host) throws UnknownHostException;

  native public static String getHostName(String host) throws UnknownHostException;

  native public static String getLocalHost() throws UnknownHostException;

  native public static String getLocalHostName() throws UnknownHostException;

  public static boolean isInternetAccessible() {
    try {
      Socket s = new Socket("www.google.com", 80, 30 * 1000);
      s.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
