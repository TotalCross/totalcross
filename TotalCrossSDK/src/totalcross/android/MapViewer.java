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

package totalcross.android;

import totalcross.*;

import android.graphics.*;
import android.os.*;
import android.view.*;
import com.google.android.maps.*;
import java.util.*;

public class MapViewer extends MapActivity 
{
   /** Called when the activity is first created. */
   private class MyOverLay extends Overlay
   {
      private GeoPoint gp1;
      private int mRadius;

      public MyOverLay(GeoPoint gp1) // GeoPoint is a int. (6E)
      {
         this.gp1 = gp1;
         mRadius = Launcher4A.deviceFontHeight/2;
      }

      @Override
      public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when)
      {
         Projection projection = mapView.getProjection();
         if (!shadow)
         {
            Paint paint = new Paint();
            paint.setAntiAlias(true);

            Point point = new Point();
            projection.toPixels(gp1, point);
            paint.setColor(Color.BLUE);
            RectF oval = new RectF(point.x - mRadius, point.y - mRadius, point.x + mRadius, point.y + mRadius);
            canvas.drawOval(oval, paint);
         }
         return super.draw(canvas, mapView, shadow, when);
      }
   }

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
      
      List<Overlay> overs = mapview.getOverlays();
      overs.add(new MyOverLay(new GeoPoint(ilat,ilon)));
      
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
