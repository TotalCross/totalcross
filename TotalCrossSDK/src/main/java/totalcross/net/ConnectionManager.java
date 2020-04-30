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

import java.net.InetAddress;
import totalcross.io.IOException;
import totalcross.io.IllegalArgumentIOException;

/**
 * The ConnectionManager allows you to open and close remote connections from your application.<br>
 * Although its behavior may differ from one platform to another, the objective is to provide a unified process to
 * handle connections across all supported platforms.<br>
 * 
 * The configuration string received by setDefaultConfiguration is a list of parameters in the format "parameter=value"
 * separated by a semi-colon ";" (spaces are not allowed).<br>
 * <br>
 * To configure a GPRS connection, you may provide the following parameters:<br>
 * 
 * <table border="1" cellpadding="3" cellspacing="0">
 * <tr bgcolor="white" class="TableHeadingColor" valign="top">
 * <td align="left">
 * <p>
 * <em>Parameter</em></td>
 * <td align="left">
 * <p>
 * <em>Description</em></td>
 * </tr>
 * 
 * <tr bgcolor="white" class="TableRowColor" valign="top">
 * <td>
 * <code>apn</code></td>
 * <td>
 * The APN over which the connection will be made.</td>
 * </tr>
 * 
 * <tr bgcolor="white" class="TableRowColor" valign="top">
 ** 
 * <td>
 * <code>username</code></td>
 * <td>
 * The user name to use to connect to the APN. May be omitted if not required by the specified APN.</td>
 * </tr>
 * 
 * <tr bgcolor="white" class="TableRowColor" valign="top">
 * <td>
 * <code>password</code></td>
 * <td>
 * The password to use to connect to the APN. May be omitted if not required by the specified APN.</td>
 * </table>
 * 
 * <br>
 * 
 * The parameters configured here are automatically used by Socket if the argument params passed to the Socket
 * constructor is null. Otherwise it will ignore the default configuration and attempt to connect using the parameters
 * defined by Socket.<br>
 */

public class ConnectionManager {
  static Object connRef;

  /**
   * This flag indicates a cradle (USB to PC) connection.
   * @deprecated Not supported on the newer devices.
   */
  @Deprecated
  public static final int CRADLE = 1;

  /**
   * This flag indicates a Wi-Fi connection.
   */
  public static final int WIFI = 2;

  /**
   * This flag indicates a cellular connection.
   */
  public static final int CELLULAR = 3;

  /**
   * @deprecated As of TotalCross 1.20, replaced by {@link #CELLULAR}.
   */
  @Deprecated
  public static final int GPRS = CELLULAR;

  private ConnectionManager() {
  }

  /**
   * Used to configure the connection to be used by the application.
   * 
   * @param type one of the connection type constants defined by the ConnectionManager.
   * @param configuration list of parameters in the format "parameter=value" separated by a semi-colon ";".
   * 
   * @throws IllegalArgumentIOException if <code>type</code> is invalid.
   * @throws IOException if an I/O error occurs while configuring the connection.
   * 
   * @see #CRADLE
   * @see #WIFI
   * @see #CELLULAR
   * @deprecated Not used on the newer devices.
   */
  @Deprecated
  public static void setDefaultConfiguration(int type, String configuration) throws IOException {
    switch (type) {
    case CRADLE:
    case WIFI:
    case CELLULAR:
      break;
    default:
      throw new IllegalArgumentIOException("Invalid value for argument 'type'");
    }
  }

  /**
   * Checks if an specific connection is currently available.
   * 
   * @param type one of the connection type constants defined by the ConnectionManager.
   * @return true if and only if the given connection is available; false otherwise.
   * 
   * @throws IllegalArgumentIOException if <code>type</code> is invalid.
   * @throws IOException if an I/O error occurs while checking connection availability.
   * 
   * @see #CRADLE
   * @see #WIFI
   * @see #CELLULAR
   */
  public static boolean isAvailable(int type) throws IOException {
    switch (type) {
    case CRADLE:
    case WIFI:
    case CELLULAR:
      return true;
    default:
      throw new IllegalArgumentIOException("Invalid value for argument 'type'");
    }
  }

  /**
   * Attempts to establish a remote connection in the first available connection in
   * the following priority list: {@link #CRADLE}, {@link #WIFI} and {@link #CELLULAR}.
   * 
   * @throws IOException if an I/O error occurs while opening the connection.
   * @deprecated Not necessary anymore.
   */
  @Deprecated
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

    throw firstEx == null ? new IOException("No connection available") : firstEx;
  }

  /**
   * Attempts to establish a remote connection using the given connection.
   * 
   * @param type one of the connection type constants defined by the ConnectionManager.
   * 
   * @throws IllegalArgumentIOException if <code>type</code> is invalid.
   * @throws IOException if an I/O error occurs while opening the connection.
   * 
   * @see #CRADLE
   * @see #WIFI
   * @see #CELLULAR
   * @deprecated Not necessary anymore.
   */
  @Deprecated
  public static void open(int type) throws IOException {
    switch (type) {
    case CRADLE:
    case WIFI:
    case CELLULAR:
      break;
    default:
      throw new IllegalArgumentIOException("Invalid value for argument 'type'");
    }
  }

  /**
   * Attempts to close all open remote connections.
   * 
   * @throws IOException if an I/O error occurs while closing a connection.
   * @deprecated Not necessary anymore.
   */
  @Deprecated
  public static void close() throws IOException {
  }

  /**
   * Returns the IP address of the given host in textual representation, or null if this information is not available.
   * 
   * @param hostName the host's name.
   * @return the host's IP address in textual representation.
   * 
   * @throws UnknownHostException if the given host is unknown or cannot be reached.
   */
  public static String getHostAddress(String hostName) throws UnknownHostException {
    try {
      return InetAddress.getByName(hostName).getHostAddress();
    } catch (java.net.UnknownHostException e) {
      throw new UnknownHostException(e.getMessage());
    }
  }

  /**
   * Returns the name of the given host, or null if this information is not available.
   * 
   * @param hostAddress textual representation of the host's IP address.
   * @return the host's name, or null if the host is unreachable or unknown.
   * 
   * @throws UnknownHostException if the given host is unknown or cannot be reached.
   */
  public static String getHostName(String hostAddress) throws UnknownHostException {
    try {
      return InetAddress.getByName(hostAddress).getHostName();
    } catch (java.net.UnknownHostException e) {
      throw new UnknownHostException(e.getMessage());
    }
  }

  /**
   * Returns a textual representation of the IP address currently assigned to this device. If this information is not
   * available for any reason, it returns the address "127.0.0.1", which is a reserved address for loopback
   * communication.
   * 
   * @return the textual representation of this device's IP.
   * 
   * @throws UnknownHostException if the local host is unknown or cannot be reached.
   */
  public static String getLocalHost() throws UnknownHostException {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (java.net.UnknownHostException e) {
      throw new UnknownHostException(e.getMessage());
    }
  }

  /**
  * Gets the host name for this device. If the operation is not allowed, it will return the textual
  * representation of the IP address.
  *
  * @return the host name for this device, or the textual representation of the IP address.
  * @throws UnknownHostException
  */
  public static String getLocalHostName() throws UnknownHostException {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (java.net.UnknownHostException e) {
      throw new UnknownHostException(e.getMessage());
    }
  }

  /**
   * Returns true if we can connect to google.com using port 80, false otherwise. Please notice this is just a quick
   * check that assumes the device is connected to the Internet without any restrictions. Results are undefined when
   * used behind proxies or firewalls.
   * 
   * @since TotalCross 1.62
   */
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
