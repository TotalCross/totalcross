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
package totalcross.io.device.gps;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.io.IOException;
import totalcross.io.device.PortConnector;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Registry;
import totalcross.sys.Settings;
import totalcross.sys.Time;
import totalcross.util.ElementNotFoundException;

/**
 * Class that retrieves GPS coordinates read from the COM (or Bluetooth, or IR) port.
 * Windows Mobile, Android and iOS use the native API instead of reading from the COM port.
 * 
 * This class only retrieves data updating the internal fields. If you want to display that data,
 * you may use the GPSView class.
 * 
 * For example:
 * 
 * <pre>
 * new Thread() 
 * {
 *    public void run()
 *    {
 *       gps = new GPS();
 *       for (int i = 0; i < 60*2 && gps.location[0] == 0; i++) // wait 60s
 *       {
 *          Vm.safeSleep(500);
 *          try
 *          {
 *             gps.retrieveGPSData();
 *          }
 *          catch (GPSDisabledException gde)
 *          {
 *             Toast.show("Please enable GPS!",2000);
 *          }
 *          catch (Exception eee)
 *          {
 *             eee.printStackTrace();
 *             break;
 *          }
 *       }
 *    }
 * }.start();
 * </pre>
 * 
 * If the GPS fails connecting to the satellites, and the phone has signal, you can use the cell tower location as a
 * rough location. The precision is vary between 50m to 3km, depending where the phone is. You can get the 
 * latitude and longitude using CellInfo.toCoordinates.
 *
 * See the tc.samples.maps.GoogleMaps sample.
 * 
 * @see totalcross.io.device.gps.GPS
 * @see totalcross.phone.CellInfo#toCoordinates()
 * 
 * @since TotalCross 1.38
 */

public class GPS {
  /** Stores the location - latitude on first index (0) and longitude on second index (1). 
   * On low signal, it contains the value <CODE>INVALID</code>. */
  public double[] location = { INVALID, INVALID };
  /** Stores the direction in degrees from the North. 
   * On low signal, it contains the value <CODE>INVALID</code>. */
  public double direction = INVALID;
  /** Stores the speed in knots. 
   * On low signal, it contains the value <CODE>INVALID</code>. */
  public double velocity = INVALID;
  /**
   * Number of satellites in view.
   * 
   * @since TotalCross 1.20
   */
  public int satellites;
  /** Stores the time of the last updated. */
  public Time lastFix = new Time();
  /** The retrieved message, or null in Windows Mobile and Android. */
  public String messageReceived;

  /** The last PDOP, if any. 
   * On low signal, it contains the value <CODE>INVALID</code>. 
   * @since TotalCross 1.27
   */
  public double pdop = INVALID;

  /** The reason for the low signal, if retrieveGPSData returned false.
   * @since TotalCross 1.38 
   */
  public String lowSignalReason;

  /** Used in gpsPrecision */
  public static final int HIGH_GPS_PRECISION = 0;
  /** Used in gpsPrecision */
  public static final int LOW_GPS_PRECISION = 1;

  /** Defines the GPS precision (currently has effect only on Android): 
   * if HIGH, only the GPS is used, if LOW, then Google Play Services is also used.
   * Be aware that Google Play Services may return wifi and radio antenna values, with pdop
   * ranging from 30m to 1500m or more; always check the pdop value and discard or store it.
   * 
   * Note that this is set at the constructor, and changing it after will have no effect;
   * you must create a new GPS instance.
   * 
   * @since TotalCross 3.1
   */
  public int precision = HIGH_GPS_PRECISION;

  /** A value that indicates that invalid data was retrieved. 
   * Declared as the minimum double value. 
   */
  public static final double INVALID = Convert.MIN_DOUBLE_VALUE;

  PortConnector sp;
  private byte[] buf = new byte[1];
  private StringBuffer sb = new StringBuffer(512);
  private static boolean nativeAPI = Settings.isWindowsCE() || Settings.platform.equals(Settings.ANDROID)
      || Settings.isIOS();
  private static boolean isOpen;
  boolean dontFinalize;

  /**
   * Returns the Windows CE GPS COM port, which can be used to open a PortConnector. Sample:
   * 
   * <pre>
   * String com;
   * if (Settings.isWindowsDevice() && (com = GPS.getWinCEGPSCom()) != null)
   *    sp = new PortConnector(Convert.chars2int(com), 9600, 7, PortConnector.PARITY_EVEN, 1);
   * </pre>
   * 
   * @return A string like "COM3", or null if no keys with GPS was found under HKLM\Drivers\BuildIn.
   */
  public static String getWinCEGPSCom() // guich@tc100b5_38
  {
    try {
      try // guich@tc120_51
      {
        String key = "System\\CurrentControlSet\\GPS Intermediate Driver\\Multiplexer\\ActiveDevice";
        String prefix = Registry.getString(Registry.HKEY_LOCAL_MACHINE, key, "Prefix");
        int index = Registry.getInt(Registry.HKEY_LOCAL_MACHINE, key, "Index");
        if (prefix.equals("COM")) {
          return "COM" + index;
        }
      } catch (ElementNotFoundException enfe) {
        // ignore if a key was not found
      }

      String[] keys = Registry.list(Registry.HKEY_LOCAL_MACHINE, "Drivers\\BuiltIn");
      for (int i = 0; i < keys.length; i++) {
        String k = keys[i].toLowerCase();
        if (k.indexOf("serial") >= 0) {
          String key = "Drivers\\BuiltIn\\" + keys[i], prefix, name;
          try {
            prefix = Registry.getString(Registry.HKEY_LOCAL_MACHINE, key, "Prefix");
            name = Registry.getString(Registry.HKEY_LOCAL_MACHINE, key, "FriendlyName");
            if (prefix.equals("COM") && name.toLowerCase().indexOf("gps") >= 0) {
              return "COM" + Registry.getInt(Registry.HKEY_LOCAL_MACHINE, key, "Index");
            }
          } catch (ElementNotFoundException enfe) {
            // ignore if a key was not found
          }
        }
      }
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * Constructs a GPS control. Already prepared for PIDION scanners. It
   * automatically scans the Windows CE registry searching for the correct GPS COM port.
   * 
   * Under Windows Mobile and Android, uses the internal GPS api.
   * 
   * @throws GPSDisabledException If GPS is disabled
   * @throws IOException If something goes wrong or if there's already an open instance of the GPS class
   */
  public GPS() throws IOException {
    this(HIGH_GPS_PRECISION);
  }

  /**
   * Constructs a GPS control, with the given precision. Already prepared for PIDION scanners. It
   * automatically scans the Windows CE registry searching for the correct GPS COM port.
   * 
   * Under Windows Mobile and Android, uses the internal GPS api.
   * 
   * @throws GPSDisabledException If GPS is disabled
   * @throws IOException If something goes wrong or if there's already an open instance of the GPS class
   */
  public GPS(int precision) throws IOException {
    this.precision = precision;
    checkOpen();
    if (!nativeAPI || !testStartGPS()) {
      String com;
      if ("PIDION".equals(Settings.deviceId)) {
        sp = new PortConnector(Convert.chars2int("COM4"), 9600, 7, PortConnector.PARITY_EVEN, 1);
      } else if (Settings.isWindowsCE() && (com = getWinCEGPSCom()) != null) {
        sp = new PortConnector(Convert.chars2int(com), 9600, 7, PortConnector.PARITY_EVEN, 1);
      } else {
        sp = new PortConnector(0, 9600);
      }
    }

    if (sp != null) {
      sp.readTimeout = 1500;
      sp.setFlowControl(false);
    }
  }

  /**
   * Constructs a GPS control with the given serial port. For example:
   * 
   * <pre>
   * PortConnector sp = new PortConnector(PortConnector.BLUETOOTH, 9600);
   * sp.setReadTimeout(500);
   * gps = new GPS(sp);
   * </pre>
   * Don't use this constructor under Android nor Windows Mobile.
   * @see #GPS()
   * @throws IOException If something goes wrong or if there's already an open instance of the GPS class
   */
  public GPS(PortConnector sp) throws IOException {
    checkOpen();
    if (sp != null) {
      this.sp = sp;
    } else if (nativeAPI) {
      testStartGPS();
    }
  }

  private boolean testStartGPS() throws IOException {
    try {
      return startGPS();
    } catch (IOException e) {
      isOpen = false;
      throw e;
    }
  }

  @ReplacedByNativeOnDeploy
  private boolean startGPS() throws IOException {
    return false;
  }

  private void checkOpen() throws IOException {
    if (isOpen) {
      throw new IOException("There's already an open instance of the GPS class");
    }
    isOpen = true;
  }

  /**
   * Closes the underlying PortConnector or native api.
   */
  public void stop() {
    dontFinalize = true;
    isOpen = false;
    if (sp == null) {
      stopGPS();
    } else {
      try {
        sp.close();
      } catch (Exception e) {
      }
      sp = null;
    }
  }

  @ReplacedByNativeOnDeploy
  private void stopGPS() {
  }

  /**
   * Returns the latitude (<code>location[0]</code>).
   */
  public double getLatitude() {
    return location[0];
  }

  /**
   * Returns the longitude (<code>location[1]</code>).
   */
  public double getLongitude() {
    return location[1];
  }

  /**
   * Returns the coordinate of the given string and direction.
   * 
   * @param s
   *           the string in coordinate format.
   * @param dir
   *           the direction: SWEN
   */
  public static double toCoordinate(String s, char dir) {
    double deg = 0;
    int i = s.indexOf('.'); // guich@421_58
    if (i >= 0) {
      int divider = 1;
      int size = s.length();
      for (int d = size - i - 1; d > 0; d--) {
        divider *= 10;
      }
      try {
        deg = (double) Convert.toInt(s.substring(0, i - 2)) + ((double) Convert.toInt(s.substring(i - 2, i))
            + (double) Convert.toInt(s.substring(i + 1, size)) / divider) / 60;
      } catch (InvalidNumberException ine) {
        return 0;
      }
      if (dir == 'S' || dir == 'W') {
        deg = -deg;
      }
    }
    return deg;
  }

  public static String toCoordinateGGMMSS(String coord, char dir) throws InvalidNumberException {
    try {
      String g, m, s;

      if (dir == 'E' || dir == 'W') // LONGITUDE
      {
        int gint = Convert.toInt(coord.substring(0, 3));
        g = gint + " ";
        m = coord.substring(3, 5) + " ";
        double temp = Convert.toDouble("0" + coord.substring(5, 10)) * 60;
        s = Convert.toString(temp, 4);
      } else {
        g = coord.substring(0, 2) + " "; // LATITUDE
        m = coord.substring(2, 4) + " ";
        double temp = Convert.toDouble("0" + coord.substring(4, 9)) * 60;
        s = Convert.toString(temp, 4);
      }
      return g + m + s + " " + dir;
    } catch (IndexOutOfBoundsException ioobe) {
      return "Err: " + coord + " - " + dir;
    }
  }

  /** Call this method to retrieve the data from the GPS. 
   * @returns true if the data was retrieved, false if low signal.
   * @see #lowSignalReason
   */
  public boolean retrieveGPSData() {
    try {
      lowSignalReason = null;
      location[0] = location[1] = direction = velocity = INVALID;
      satellites = 0;
      return sp != null ? processSerial() : processNative();
    } catch (Exception e1) {
      lowSignalReason = e1.getMessage() + " (" + (e1.getClass()) + ")";
      return false;
    }
  }

  @ReplacedByNativeOnDeploy
  private int updateLocation() {
    return 0;
  }

  // private methods
  private boolean processNative() {
    int result = updateLocation();
    if ((result & 8) == 0) {
      velocity = INVALID;
    }
    if ((result & 4) == 0) {
      direction = INVALID;
    }
    return (result & 3) != 0; // latitude and longitude: one doesn't make sense without the other
  }

  private boolean processSerial() throws Exception {
    String message;
    boolean ok = false;
    while ((message = nextMessage()) != null) {
      messageReceived = message;
      String[] sp = Convert.tokenizeString(message, ',');
      if (sp[0].equals("$GPGGA")) // guich@tc115_71
      {
        if ("".equals(sp[2]) || "".equals(sp[4])) {
          continue;
        }
        location[0] = toCoordinate(sp[2], sp[3].charAt(0));
        location[1] = toCoordinate(sp[4], sp[5].charAt(0));
        if (sp.length > 7 && sp[7].length() > 0) {
          satellites = Convert.toInt(sp[7]); //flsobral@tc120_66: new field, satellites.
        }
      } else if (sp[0].equals("$GPGLL")) {
        if (!"A".equals(sp[6])) {
          continue;
        }
        location[0] = toCoordinate(sp[1], sp[2].charAt(0));
        location[1] = toCoordinate(sp[3], sp[4].charAt(0));
        if (sp[5].length() > 0) {
          lastFix.hour = Convert.toInt(sp[5].substring(0, 2));
          lastFix.minute = Convert.toInt(sp[5].substring(2, 4));
          lastFix.second = Convert.toInt(sp[5].substring(4, 6));
          lastFix.millis = 0;
        }
        ok = true;
      }
      // fleite@421_57: Adding position and time message's decode routine
      else if (sp[0].equals("$GPRMC") && sp.length >= 8 && "A".equals(sp[2])) {
        if (!"A".equals(sp[2])) {
          continue;
        }
        location[0] = toCoordinate(sp[3], sp[4].charAt(0));
        location[1] = toCoordinate(sp[5], sp[6].charAt(0));
        if (sp[1].length() >= 6) {
          lastFix.hour = Convert.toInt(sp[1].substring(0, 2));
          lastFix.minute = Convert.toInt(sp[1].substring(2, 4));
          lastFix.second = Convert.toInt(sp[1].substring(4, 6));
          lastFix.millis = 0;
        }
        if (sp[7].length() > 0) {
          velocity = Convert.toDouble(sp[7]); //knots
        }
        if (sp[8].length() > 0) {
          direction = Convert.toDouble(sp[8]); //degrees
        }
        ok = true;
      } else if (sp[0].equals("$GPGSA")) {
        try {
          pdop = sp.length > 15 ? Convert.toDouble(sp[15]) : 0;
        } catch (Exception e) {
        }
      }
    }
    return ok;
  }

  /**
   * Reads the next message from the port connector.
   *  
   * guich@tc115_71: completely changed
   * flsobral@tc120_66: messages are now validated.
   * 
   * @return
   * @throws IOException
   */
  private String nextMessage() throws IOException {
    StringBuffer sb = this.sb;
    byte[] buf = this.buf;
    String ret = null;
    int len = 0;
    int retry = 5;
    int read;

    while ((read = sp.readBytes(buf, 0, 1)) != -1) {
      if (read == 0 && --retry <= 0) {
        throw new IOException("Invalid port: nothing to read.");
      }

      int c = buf[0] & 0xFF;
      if (c == '$') {
        sb.setLength(0);
      }

      sb.append((char) c);
      if (c == '\n') {
        break;
      } else if (c > 128) {
        throw new IOException("Invalid port: only trash was retrieved.");
      } else if (++len > 512) {
        int idx = sb.toString().indexOf('$');
        if (idx == -1) {
          sb.setLength(0);
        } else {
          sb.delete(0, idx);
        }
        len = sb.length();
      }
    }

    if (len > 0) {
      ret = sb.toString();
      if (ret.length() <= 3) {
        return null;
      }

      sb.setLength(0);
      ret = validateMessage(ret);
    }
    return ret;
  }

  /**
   * Checks if the received message is valid.
   * 
   * @param message 
   * @return the given message without the ending CRLF if valid, otherwise null is returned.
   * 
   * @since TotalCross 1.20
   */
  private String validateMessage(String message) {
    char[] msgChars = message.toCharArray();
    int checksum = 0;

    if (msgChars[0] != '$' || msgChars[msgChars.length - 2] != '\r' || msgChars[msgChars.length - 1] != '\n') {
      return null;
    }
    for (int i = msgChars.length - 6; i > 0; i--) {
      if (msgChars[i] == '\r' || msgChars[i] == '\n' || msgChars[i] == '$' || msgChars[i] == '*') {
        return null;
      }
      checksum ^= msgChars[i];
    }
    if (msgChars[msgChars.length - 5] == '*') {
      int checksum2 = Convert.hex2signed(new String(msgChars, msgChars.length - 4, 2));
      if (checksum != checksum2) {
        return null;
      }
    }

    return new String(msgChars, 0, msgChars.length - 2);
  }

  @Override
  protected void finalize() {
    this.stop();
  }
}
