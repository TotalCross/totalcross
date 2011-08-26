/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package totalcross.android;

import android.os.*;
import android.view.*;
import com.google.android.maps.*;

public class MapViewer extends MapActivity 
{
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      // setup the viewe
      MapView mapview = new MapView(this,"0FcAyehwXTAXpaMaXoVn7kGJVJdRSuSI2RsqELQ");
      mapview.setBuiltInZoomControls(true);
      mapview.setClickable(true);
      mapview.setStreetView(true);
      setContentView(mapview);
      if (Loader.isFullScreen)
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
      // get passed parameters
      Bundle extras = getIntent().getExtras();
      double lat = extras.getDouble("lat");
      double lon = extras.getDouble("lon");
      mapview.setSatellite(extras.getBoolean("sat"));
      int ilat = (int)(lat * 1e6);
      int ilon = (int)(lon * 1e6);
      // move the map to the given point
      MapController mc = mapview.getController();
      if (ilat != 0 || ilon != 0) 
         mc.setCenter(new GeoPoint(ilat,ilon));
      mc.setZoom(21);
   }

   protected boolean isRouteDisplayed()
   {
      return false;
   }
}
