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

import totalcross.io.IOException;
import totalcross.io.device.PortConnector;
import totalcross.sys.Settings;
import totalcross.ui.Container;
import totalcross.ui.Label;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;

/**
 * Control that display GPS coordinates read from the COM (or Bluetooth, or IR) port.
 * In Windows Mobile and Android, it uses the native API instead of reading from the COM port. 
 * 
 * For example:
 * 
 * <pre>
 * add(gps = new GPSView(1000),LEFT,TOP);
 * </pre>
 * See the tc.samples.io.device.GPSTest.
 * 
 * On Android, don't forget to turn on the GPS, going to Settings / Security &amp; Location / Enable GPS satellites. 
 * The other platforms may require that as well.
 * 
 * If the GPS fails connecting to the satellites, and the phone has signal, you can use the cell tower location as a
 * rough location. The precision is vary between 50m to 3km, depending where the phone is. You can get the 
 * latitude and longitude using CellInfo.toCoordinates.
 *
 * This class uses the GPS class to show the values on Labels. You can use the GPS class standalone if you wish.
 * 
 * See the tc.samples.maps.GoogleMaps sample.
 * 
 * @see totalcross.io.device.gps.GPS
 * @see totalcross.phone.CellInfo#toCoordinates()
 * 
 * @since TotalCross 1.38
 */

public class GPSView extends Container implements TimerListener {
  /** String that will be printed with there longitude can't be acquired. You can localize this string. */
  public static String LON_LOW_SIGNAL = "lon: low signal";
  /** String that will be printed with there longitude can't be acquired. You can localize this string. */
  public static String LAT_LOW_SIGNAL = "lat: low signal";

  private Label[] text = new Label[5];
  private TimerEvent timer;
  private int readInterval = 2000;

  /** Class used to retrieve the GPS coordinates. */
  public GPS gps;

  /** Constructs a GPS using a read interval of 2 seconds. */
  public GPSView() throws IOException {
    this(2000);
  }

  /**
   * Constructs a GPSView using the given read interval.
   * 
   * @param readInterval
   *           The interval used to fetch data, in milliseconds. A ControlEvent.PRESSED is posted each time the TRIGGERED event occurs.
   * @throws IOException
   */
  public GPSView(int readInterval) throws IOException {
    this((PortConnector) null, readInterval);
  }

  /** Constructs a GPSView using the given PortConnector and read interval.
   * In Android and Windows Mobile, you should use the other constructor.
   * #GPSView(int) 
   */
  public GPSView(PortConnector sp, int readInterval) throws IOException {
    this(sp == null ? new GPS() : new GPS(sp), readInterval);
  }

  /** Constructs a GPSView using the given GPS as input.
   * @since TotalCross 1.5
   */
  public GPSView(GPS gps, int readInterval) throws IOException {
    this.gps = gps;
    this.readInterval = readInterval;
    for (int i = 0; i < text.length; i++) {
      text[i] = new Label("");
    }
  }

  @Override
  public void initUI() {
    for (int i = 0; i < text.length; i++) {
      add(text[i], LEFT, AFTER);
    }

    text[0].setText("GPS Initialising");
    timer = addTimer(readInterval);
    addTimerListener(this);
  }

  @Override
  public void timerTriggered(TimerEvent e) {
    if (e.type == TimerEvent.TRIGGERED && timer.triggered) {
      retrieveGPSData();
    }
  }

  // public methods available from GPS class

  /** Removes the timer and stops the GPS. */
  public void stop() throws IOException {
    TimerEvent t = timer;
    timer = null;
    removeTimer(t);
    gps.stop();
  }

  /** Retrieves the GPS data and updates the fields with it. */
  public void retrieveGPSData() {
    if (timer == null || gps == null) {
      return;
    }
    if (!gps.retrieveGPSData()) {
      lowSignal(gps.lowSignalReason);
    } else {
      showGPSData();
    }
    repaintNow();
    postPressedEvent(); // guich@tc126_67
  }

  private void showGPSData() {
    double lat = gps.location[0];
    double lon = gps.location[1];
    double absoluteLat = lat < 0 ? -lat : lat;
    int degrees = (int) absoluteLat;
    int minutes = (int) ((absoluteLat - degrees) * 60);
    double seconds = (((absoluteLat - degrees) * 60) - minutes) * 60;
    text[0].setText("lat: " + degrees + " " + minutes + " " + seconds + (lat < 0 ? " S" : " N"));

    double absoluteLon = lon < 0 ? -lon : lon;
    degrees = (int) absoluteLon;
    minutes = (int) ((absoluteLon - degrees) * 60);
    seconds = (((absoluteLon - degrees) * 60) - minutes) * 60;
    text[1].setText("lon: " + degrees + " " + minutes + " " + seconds + (lon < 0 ? " W" : " E"));

    gps.lastFix.inc(0, Settings.timeZoneMinutes, 0);
    text[2].setText("fix: " + gps.lastFix);
    gps.lastFix.inc(0, -Settings.timeZoneMinutes, 0);

    text[3].setText(gps.velocity != GPS.INVALID ? "speed: " + gps.velocity : "");
    text[4].setText(gps.direction != GPS.INVALID ? "direction: " + gps.direction : "");
    if (gps.satellites > 0) {
      text[2].setText(text[2].getText() + "  " + "sat: " + gps.satellites);
    }
  }

  private void lowSignal(String ex) {
    text[0].setText(LAT_LOW_SIGNAL);
    text[1].setText(LON_LOW_SIGNAL);
    for (int i = 2; i < text.length; i++) {
      text[i].setText("");
    }
    if (ex != null) {
      text[text.length - 1].setText(ex);
    }
  }

  @Override
  public int getPreferredWidth() {
    return FILL;
  }

  @Override
  public int getPreferredHeight() {
    return fmH * text.length + insets.top + insets.bottom;
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    int b = getBackColor();
    int f = getForeColor();
    for (int i = 0; i < text.length; i++) {
      text[i].setBackForeColors(b, f);
    }
  }

  @Override
  protected void onFontChanged() {
    for (int i = 0; i < text.length; i++) {
      text[i].setFont(font);
    }
  }

}
