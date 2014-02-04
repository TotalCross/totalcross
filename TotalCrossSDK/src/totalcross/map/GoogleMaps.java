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

   public static boolean showRoute(String addressI, String addressF, String traversedPoints, boolean showSatellitePhotos)
   {
      return true;
   }
   native static boolean showRoute4D(String addressI, String addressF, String traversedPoints, boolean showSatellitePhotos);
}
