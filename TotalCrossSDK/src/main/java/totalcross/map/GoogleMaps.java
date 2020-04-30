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

package totalcross.map;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.net.HttpStream;
import totalcross.net.URI;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Vm;
import totalcross.util.NotInstalledException;

/** Shows a Google Maps viewer on a separate screen. 
 * Pressing back returns to the application.
 * <p/>
 * Internet connection is required.
 * <p/>
 * Currently works in Android and iOS only.
 * 
 * @see {@link Circle}
 * @see {@link Shape}
 * @see {@link Place}
 * @since TotalCross 1.3
 */
public class GoogleMaps {
  /** Used in the flags argument of showRoute: shows the satellite photos. */
  public static final int SHOW_SATELLITE_PHOTOS = 1;
  /** Used in the flags argument of showRoute: use waze to show the route of the current location to a target
   * address. Note that the destination address is NOT used. */
  public static final int USE_WAZE = 2;

  /** An abstract class used to group an array of map items.
   * @see {@link Circle}
   * @see {@link Shape}
   * @see {@link Place}
   */
  public abstract static class MapItem {
    abstract void serialize(StringBuffer sb);
  }

  private static int toCoordI(double v) {
    return (int) (v * 1e6);
  }

  /** A map item that represents a circle.
   * <pre>
      GoogleMaps.Circle c = new GoogleMaps.Circle();
      c.lat = -3.73243;
      c.lon = -38.483414;
      c.color = Color.BLUE;
      c.filled = false;
      c.rad = 70;
   * </pre>
   */
  public static class Circle extends MapItem {
    /** Center of the circle */
    public double lat, lon;
    /** The radius; if > 0, its computed as meters; if < 0, its computed as delta of the coordinates */
    public double rad;
    /** Set if the item is filled or not */
    public boolean filled;
    /** The item color. Alpha defaults to 255 if not specified. */
    public int color;

    @Override
    void serialize(StringBuffer sb) {
      sb.append("*C*,").append(toCoordI(lat)).append(",").append(toCoordI(lon)).append(",").append(rad).append(",")
          .append(filled).append(",").append(color);
    }
  }

  /** A map item that represents a polygon with any shape.
   * <pre>
      GoogleMaps.Shape s1 = new GoogleMaps.Shape();
      s1.color = Color.YELLOW | 0X88000000;
      s1.filled = true;
      s1.lats = new double[]{-3.73143,-3.73243,-3.73193};
      s1.lons = new double[]{-38.483424, -38.483524, -38.483124};
   * </pre>
   */
  public static class Shape extends MapItem {
    /** The coordinates of the polygon */
    public double[] lats, lons;
    /** Set if the item is filled or not */
    public boolean filled;
    /** The item color. Alpha defaults to 255 if not specified. */
    public int color;

    @Override
    void serialize(StringBuffer sb) {
      sb.append("*S*,").append(lats.length).append(",");
      for (int i = 0; i < lats.length; i++) {
        sb.append(toCoordI(lats[i])).append(",").append(toCoordI(lons[i])).append(",");
      }
      sb.append(filled).append(",").append(color);
    }
  }

  /** A map item that represents a place in the map. It shows with a pin, and above it, a balloon with a text. 
   * <pre>
   * GoogleMaps.Place p1 = new GoogleMaps.Place();
   * p1.lat = -3.778284;
   * p1.lon = -38.482617;
   * p1.backColor = Color.WHITE;
   * p1.capColor = Color.BLACK;
   * p1.detColor = 0x444444;
   * p1.pinColor = Color.RED;
   * p1.caption = "TotalCross MGP";
   * p1.fontPerc = 150;
   * p1.detail = "Av Norte 2920\nLuciano Cavalcante\nCeará - Brazil";
   * </pre>
   */
  public static class Place extends MapItem {
    /** The location of the place */
    public double lat, lon;
    /** An optional caption of the place; shown in bold */
    public String caption;
    /** The detail of the place. Use \n to split lines. Cannot be null. */
    public String detail;
    /** The item's background color. Alpha defaults to 255 if not specified. */
    public int backColor;
    /** The item caption's color. Alpha defaults to 255 if not specified. */
    public int capColor;
    /** The item details' color. Alpha defaults to 255 if not specified. */
    public int detColor;
    /** The item pin's color. Alpha defaults to 255 if not specified. */
    public int pinColor;
    /** The percentage of the font based on the device's original font size. Defaults to 100. */
    public int fontPerc = 100;

    @Override
    void serialize(StringBuffer sb) {
      sb.append("*P*,\"").append(caption == null ? null : caption.replace('"', '\'')).append("\",\"")
          .append(detail.replace('"', '\'')).append("\",").append(toCoordI(lat)).append(",").append(toCoordI(lon))
          .append(",").append(backColor).append(",").append(capColor).append(",").append(detColor).append(",")
          .append(pinColor).append(",").append(fontPerc);
    }
  }

  /** Shows the given address in a separate viewer.
   * 
   * If you want to show your current location, you will have to turn
   * GPS on, get the coordinates and pass to this method.
   * 
   * See the tc.samples.map.GoogleMaps sample.
   * 
   * @param address The address (WITHOUT ACCENTUATION) to show (E.G.: "rua tonelero 10, copacabana, rio de janeiro, brasil", or "22030,brasil").
   * The address is resolved and its latitude and logitude coordinates are retrieved.
   * Careful with the address given, be sure to make some tests before. To remove accentuation, you can use Convert.removeAccentuation.  
   * 
   * If you want to pass the lat/lon coordinates, pass them in the form: "@lat,lon" (E.G.: "@-22.966000,-43.185000").
   * Note that, due to Android's restrictions, only the first 6 decimal digits in the coordinates are used.
   * 
   * Passing an empty string will make the engine search for a valid "last known position". If there's no such position, 
   * it will return false.
   * 
   * @param showSatellitePhotos If true, an image of the satellite is combined with the map. May require more 
   * bandwidth and internet resources.
   * 
   * @return true if the map was shown, false otherwise. False is often returned when there's no internet connection.
   * @see Convert#removeAccentuation(String)
   */
  @ReplacedByNativeOnDeploy
  public static boolean showAddress(String address, boolean showSatellitePhotos) {
    return true;
  }

  /** Shows the route between two points. The traversed points is a sequence of lat,lon coordinates comma-separated.
   * @deprecated 
   * @see #showRoute(String, String, String, int)
   */
  @Deprecated
  @ReplacedByNativeOnDeploy
  public static boolean showRoute(String addressI, String addressF, String traversedPoints, boolean showSatellitePhotos)
      throws NotInstalledException {
    return showRoute(addressI, addressF, traversedPoints, SHOW_SATELLITE_PHOTOS);
  }

  /** Shows the route between two points. The traversed points is a sequence of lat,lon coordinates comma-separated.
   * If flags is USE_WAZE, the addressF is not used and can be null, and a NotInstalledException is thrown if WAZE is not installed.
   * 
   * Note that you must remove accentuation from addressI and addressF; you may use Convert.removeAccentuation.
   * 
   * @see Convert#removeAccentuation(String)
   */
  @ReplacedByNativeOnDeploy
  public static boolean showRoute(String addressI, String addressF, String traversedPoints, int flags)
      throws NotInstalledException {
    return true;
  }

  /** Shows an array of MapItem elements in the map. The map is zommed in a way that all coordinates are visible.
   * <p/>
   * If one may pass multiple overlapping items (let's say, items[0], items[1] and items[2]), one item will cover all the previous items (item[1] will be over item[0]; item[2] will be over item[1] and item[0]).
   * 
   * @see {@link Circle}
   * @see {@link Shape}
   * @see {@link Place}
   */
  public static boolean showMap(MapItem[] items, boolean showSatellitePhotos) {
    StringBuffer sb = new StringBuffer(items.length * 100);
    sb.append("***");
    items[0].serialize(sb);
    for (int i = 1; i < items.length; i++) {
      sb.append("|");
      items[i].serialize(sb);
    }
    return showAddress(sb.toString(), showSatellitePhotos);
  }

  /** Returns the location after searching Google. Requires internet connection and waits up to 20 seconds for an answer. 
   * Returns the lat/lon pair, or null if a bad response code was returned. 
   */
  public static double[] getLocation(String address) throws IOException, InvalidNumberException {
    // connect to map web service
    String url = "http://maps.googleapis.com/maps/api/geocode/xml?address="
        + Convert.removeAccentuation(address).replace("  ", " ").replace(' ', '+') + "&sensor=false";
    HttpStream hs = new HttpStream(new URI(url));
    if (hs.badResponseCode) {
      Vm.debug("getLocation \"" + url + "\" returned response code: " + hs.responseCode);
      return null;
    }
    ByteArrayStream bas = new ByteArrayStream(2048);
    bas.readFully(hs, 5, 2048);
    String s = new String(bas.getBuffer(), 0, bas.available()).toLowerCase();
    if (s.contains("<location_type>approximate</location_type>")) {
      Vm.debug("getLocation \"" + url + "\" returned a bad location from google maps");
      return null;
    }
    int idx1 = s.indexOf("<lat>"), idx2 = s.indexOf("</lat>", idx1);
    int idx3 = s.indexOf("<lng>"), idx4 = s.indexOf("</lng>", idx1);
    if (idx1 == -1 || idx2 == -1 || idx3 == -1 || idx4 == -1) {
      return null;
    }
    String lat = s.substring(idx1 + 5, idx2);
    String lon = s.substring(idx3 + 5, idx4);
    return new double[] { Convert.toDouble(lat), Convert.toDouble(lon) };
  }
}
