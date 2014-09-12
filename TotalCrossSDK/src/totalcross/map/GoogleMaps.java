/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package totalcross.map;

/** Shows a Google Maps viewer on a separate viewer. 
 * Pressing back returns to the application.
 * 
 * Internet connection is required.
 * 
 * Currently works in Android and iOS only.
 * 
 * @since TotalCross 1.3
 */
public class GoogleMaps
{
   public abstract static class MapItem
   {
      abstract protected void serialize(StringBuffer sb);
   }
   
   private static int toCoordI(double v)
   {
      return (int)(v * 1e6);
   }

   public static class Circle extends MapItem
   {
      public double lat, lon;
      /** Radius in meters */
      public double rad;
      public boolean filled;
      public int color;
      protected void serialize(StringBuffer sb)
      {
         sb.append("*C*,").append(toCoordI(lat)).append(",").append(toCoordI(lon)).append(",").append(rad).append(",").append(filled).append(",").append(color);
      }
   }
   public static class Shape extends MapItem
   {
      public double[] lats, lons;
      public boolean filled;
      public int color;
      protected void serialize(StringBuffer sb)
      {
         sb.append("*S*,").append(lats.length).append(",");
         for (int i = 0; i < lats.length; i++)
            sb.append(toCoordI(lats[i])).append(",").append(toCoordI(lons[i])).append(",");
         sb.append(filled).append(",").append(color);
      }
   }
   public static class Place extends MapItem
   {
      public double lat,lon;
      public String pinFilename;
      public String caption, detail;
      public int backColor, capColor, detColor;
      protected void serialize(StringBuffer sb)
      {
         sb.append("*P*,\"").append(pinFilename.replace('"','\'')).append("\",\"").append(caption.replace('"','\'')).append("\",\"").append(detail.replace('"','\''))
           .append("\",").append(toCoordI(lat)).append(",").append(toCoordI(lon)).append(",").append(backColor).append(",").append(capColor).append(",").append(detColor);
      }
   }
   
   
   
   /** Shows the given address in a separate viewer.
    * 
    * If you want to show your current location, you will have to turn
    * GPS on, get the coordinates and pass to this method.
    * 
    * See the tc.samples.map.GoogleMaps sample.
    * 
    * @param address The address to show (E.G.: "rua tonelero 10, copacabana, rio de janeiro, brasil", or "22030,brasil").
    * The address is resolved and its latitude and logitude coordinates are retrieved.
    * Careful with the address given, be sure to make some tests before. 
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
    */
   public static boolean showAddress(String address, boolean showSatellitePhotos)
   {
      return true;
   }
   native static boolean showAddress4D(String address, boolean showSatellitePhotos);

   /** Shows the route between two points. The traversed points is a sequence of lat,lon coordinates comma-separated.
    */
   public static boolean showRoute(String addressI, String addressF, String traversedPoints, boolean showSatellitePhotos)
   {
      return true;
   }
   native static boolean showRoute4D(String addressI, String addressF, String traversedPoints, boolean showSatellitePhotos);
   
   /** Shows an array of MapItem elements in the map. The map is zommed in a way that all coordinates are visible.
    * See the Circle, Shape and Place map items.
    */
   public static boolean showMap(MapItem[] items, boolean showSatellitePhotos)
   {
      StringBuffer sb = new StringBuffer(items.length * 100);
      sb.append("***");
      items[0].serialize(sb);
      for (int i = 1; i < items.length; i++)
      {
         sb.append("|");
         items[i].serialize(sb);
      }
      return showAddress(sb.toString(), showSatellitePhotos);
   }
}
