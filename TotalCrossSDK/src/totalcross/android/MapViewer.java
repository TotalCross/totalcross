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
   private abstract static class MapItem
   {
   }

   static class Circle extends MapItem
   {
      int clat, clon, rad;
      boolean filled;
      int color;
      Circle(String s)
      {
         String[] ss = s.split(",");
         int i = 1;
         clat = toCoordI(Double.valueOf(ss[i++]));
         clon = toCoordI(Double.valueOf(ss[i++]));
         rad  = toCoordI(Double.valueOf(ss[i++]));
         filled = ss[i++].equals("true");
         color = Integer.valueOf(ss[i++]);
      }
   }
   static class Shape extends MapItem
   {
      int[] lats, lons;
      boolean filled;
      int color;
      Shape(String s)
      {
         String[] ss = s.split(",");
         int i = 1;
         int n = Integer.valueOf(ss[i++]);
         lats = new int[n];
         lons = new int[n];
         for (int j = 0; j < n; j++)
         {
            lats[j] = toCoordI(Double.valueOf(ss[i++]));
            lons[j] = toCoordI(Double.valueOf(ss[i++]));
         }
         filled = ss[i++].equals("true");
         color = Integer.valueOf(ss[i++]);
      }
   }
   static class Place extends MapItem
   {
      int lat,lon;
      String pinFilename;
      String caption, detail;
      int backColor, capColor, detColor;
      Place(String s)
      {
         // *P*,"aaa","bbb","ccc",22.0,-22.1,0,1,2  - 
         String[] ss = s.split("\"");
         pinFilename = ss[1];
         caption = ss[3];
         detail = ss[5];
         ss = ss[6].split(",");
         lat = toCoordI(Double.valueOf(ss[1]));
         lon = toCoordI(Double.valueOf(ss[2]));
         backColor = toCoordI(Integer.valueOf(ss[3]));
         capColor = toCoordI(Integer.valueOf(ss[4]));
         detColor = toCoordI(Integer.valueOf(ss[5]));
      }
   }
  
   public MapItem[] getItems(String s0)
   {
      String[] ss = s0.split("|");
      MapItem[] items = new MapItem[ss.length];
      for (int i = 0; i < ss.length; i++)
      {
         String s = ss[i];
         if (s.startsWith("*S*"))
            items[i] = new Shape(s);
         else
         if (s.startsWith("*C*"))
            items[i] = new Circle(s);
         else
         if (s.startsWith("*P*"))
            items[i] = new Place(s);
            
      }
      return items;
   }
   
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
   private class MyItemsOverLay extends Overlay
   {
      MapItem[] items;

      public MyItemsOverLay(MapItem[] items) // GeoPoint is a int. (6E)
      {
         this.items = items;
      }

      @Override
      public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when)
      {
/*         Projection projection = mapView.getProjection();
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
*/         return super.draw(canvas, mapView, shadow, when);
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
      mapview.setSatellite(extras.getBoolean("sat"));
      List<Overlay> overs = mapview.getOverlays();

      int ilat=0,ilon=0;
      String items = extras.getString("items");
      if (items != null)
      {
         overs.add(new MyItemsOverLay(getItems(items)));
      }
      else
      {
         double lat = extras.getDouble("lat");
         double lon = extras.getDouble("lon");
         ilat = toCoordI(lat);
         ilon = toCoordI(lon);
         overs.add(new MyOverLay(new GeoPoint(ilat,ilon)));
      }      
      // move the map to the given point
      MapController mc = mapview.getController();
      if (ilat != 0 || ilon != 0) 
         mc.setCenter(new GeoPoint(ilat,ilon));
      mc.setZoom(21);
   }
   
   private static int toCoordI(double v)
   {
      return (int)(v * 1e6);
   }

   protected boolean isRouteDisplayed()
   {
      return false;
   }
}
