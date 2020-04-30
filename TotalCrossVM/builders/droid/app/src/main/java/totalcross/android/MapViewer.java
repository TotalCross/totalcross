// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android;

import android.graphics.*;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.os.*;
import android.view.*;
import com.google.android.maps.*;
import java.util.*;

import totalcross.*;

public class MapViewer extends MapActivity 
{
   private int ilatMin=Integer.MAX_VALUE,ilatMax=Integer.MIN_VALUE;
   private int ilonMin=Integer.MAX_VALUE,ilonMax=Integer.MIN_VALUE;
   private int mRadius;
   
   private void computeBounds(int ilat, int ilon)
   {
      if (ilat < ilatMin) ilatMin = ilat;
      if (ilat > ilatMax) ilatMax = ilat;
      if (ilon < ilonMin) ilonMin = ilon;
      if (ilon > ilonMax) ilonMax = ilon;
   }

   private abstract class MapItem
   {
      public abstract void draw(Canvas canvas, Projection projection);
   }

   private class Circle extends MapItem
   {
      GeoPoint geo;
      double rad;
      boolean filled;
      int color;
      Circle(String s)
      {
         String[] ss = s.split(",");
         int i = 1;
         int lat = Integer.valueOf(ss[i++]);
         int lon = Integer.valueOf(ss[i++]);
         geo = new GeoPoint(lat,lon);
         computeBounds(lat,lon);
         rad  = Double.valueOf(ss[i++]);
         filled = ss[i++].equals("true");
         color = Integer.valueOf(ss[i++]); if ((color & 0xFF000000) == 0) color |= 0xFF000000;
      }
      public void draw(Canvas canvas, Projection projection)
      {
         Paint paint = new Paint();
         paint.setAntiAlias(true);
         paint.setColor(color);
         paint.setStyle(filled ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
         RectF oval;
         if (rad > 0) // in meters?
         {
            Point point = new Point();
            projection.toPixels(geo, point);
            float frad = projection.metersToEquatorPixels((float)rad);
            oval = new RectF(point.x - frad, point.y - frad, point.x + frad, point.y + frad);
         }
         else // in coords delta
         {
            double r = -rad;
            float la = fromCoord(geo.getLatitudeE6()), lo = fromCoord(geo.getLongitudeE6());
            Point p1 = projection.toPixels(new GeoPoint(toCoord(la-r),toCoord(lo-r)), null);
            Point p2 = projection.toPixels(new GeoPoint(toCoord(la+r),toCoord(lo+r)), null);
            AndroidUtils.debug(p1+" - "+p2);
            oval = new RectF(p1.x,p1.y,p2.x,p2.y);
         }
         canvas.drawOval(oval, paint);
      }
   }
   private class Shape extends MapItem
   {
      GeoPoint []geos;
      boolean filled;
      int color;
      Shape(String s)
      {
         String[] ss = s.split(",");
         int i = 1;
         int n = Integer.valueOf(ss[i++]);
         geos = new GeoPoint[n];
         for (int j = 0; j < n; j++)
         {
            int lat = Integer.valueOf(ss[i++]);
            int lon = Integer.valueOf(ss[i++]);
            geos[j] = new GeoPoint(lat,lon);
            computeBounds(lat,lon);
         }
         filled = ss[i++].equals("true");
         color = Integer.valueOf(ss[i++]); if ((color & 0xFF000000) == 0) color |= 0xFF000000;
      }
      public void draw(Canvas canvas, Projection projection)
      {
         Point point = new Point();
         Paint paint = new Paint();
         paint.setAntiAlias(true);
         paint.setColor(color);
         paint.setStyle(filled ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);

         Path path = new Path();
         for (int i = 0; i < geos.length; i++)
         {
            projection.toPixels(geos[i], point);
            if (i == 0)
               path.moveTo(point.x, point.y); // used for first point
            else
               path.lineTo(point.x, point.y);
         }
         canvas.drawPath(path, paint);
      }
   }
   private class Place extends MapItem
   {
      GeoPoint geo;
      String caption, details[];
      int backColor, capColor, detColor, pinColor;
      int fontPerc;
      Place(String s)
      {
         // *P*,"aaa","bbb",22.0,-22.1,0,1,2,3,150 
         String[] ss = s.split("\"");
         caption = ss[1]; if (caption.equals("null")) caption = null;
         details = ss[3].split("\n");
         ss = ss[4].split(",");
         int lat = Integer.valueOf(ss[1]);
         int lon = Integer.valueOf(ss[2]);
         geo = new GeoPoint(lat,lon);
         computeBounds(lat,lon);
         backColor= Integer.valueOf(ss[3]); if ((backColor & 0xFF000000) == 0) backColor |= 0xFF000000;
         capColor = Integer.valueOf(ss[4]); if ((capColor  & 0xFF000000) == 0) capColor  |= 0xFF000000;
         detColor = Integer.valueOf(ss[5]); if ((detColor  & 0xFF000000) == 0) detColor  |= 0xFF000000;
         pinColor = Integer.valueOf(ss[6]); if ((pinColor  & 0xFF000000) == 0) pinColor  |= 0xFF000000;
         fontPerc = Integer.valueOf(ss[7]);
      }
      private void drawBaloon(Canvas canvas, Projection projection) // above
      {
         Paint paint2= new Paint();
         float frad = mRadius * fontPerc / 100;//projection.metersToEquatorPixels((float)mRadius);
         Point point = new Point();
         projection.toPixels(geo, point);
         point.y -= frad * 4; // shift above pin
         float textH = frad*2;
         paint2.setTextSize(textH);  //set text size

         // compute max text width
         FontMetrics fm = new FontMetrics();
         paint2.setTextAlign(Paint.Align.CENTER);
         paint2.getFontMetrics(fm);
         paint2.setFakeBoldText(true);
         boolean hasCap = caption != null;
         float ww = hasCap ? paint2.measureText(caption) : 0;
         paint2.setFakeBoldText(false);
         for (int i = 0; i < details.length; i++)
            ww = Math.max(ww, paint2.measureText(details[i]));
         float yy = point.y - frad*2*((hasCap?1:0)+details.length+1)-frad;
         ww += textH; // give some extra space
         
         // draw baloon
         //canvas.drawRect(point.x - ww/2, yy, point.x + ww/2, point.y-frad, mpaint);
         Paint mpaint= new Paint();
         mpaint.setStyle(Style.FILL);
         Path path = new Path();
         path.addRoundRect(new RectF(point.x - ww/2, yy, point.x + ww/2, point.y-frad-frad), textH/2,textH/2, Path.Direction.CCW);
         path.moveTo(point.x-frad,point.y-frad-frad);
         path.lineTo(point.x,point.y-frad/2);
         path.lineTo(point.x+frad,point.y-frad-frad);
         float delta = frad/4;
         
         mpaint.setColor(darker(backColor));
         path.offset(delta,delta);
         canvas.drawPath(path, mpaint);
         
         mpaint.setColor(backColor);
         path.offset(-delta,-delta);
         canvas.drawPath(path, mpaint);
         
         // draw text
         yy += textH; // text's anchor is at bottom
         if (hasCap)
         {
            paint2.setFakeBoldText(true);
            paint2.setColor(capColor);
            canvas.drawText(caption, point.x, yy, paint2); // title
            yy += textH;
         }
         paint2.setFakeBoldText(false);
         paint2.setColor(detColor);
         for (int i = 0; i < details.length; i++, yy += textH)
            canvas.drawText(details[i], point.x, yy, paint2);
      }
      private int darker(int c)
      {
         int a = (c >> 24) & 0xFF;
         int r = (c >> 16) & 0xFF;
         int g = (c >> 8) & 0xFF;
         int b = c & 0xFF;
         r -= 64; if (r < 0) r = 0;
         g -= 64; if (g < 0) g = 0;
         b -= 64; if (b < 0) b = 0;
         return (a << 24) | (r << 16) | (g << 8) | b;
      }
      private void drawPin(Canvas canvas, Projection projection) // below
      {
         Paint paint = new Paint();
         paint.setAntiAlias(true);
         float frad = mRadius * fontPerc / 100;//projection.metersToEquatorPixels((float)mRadius);
         Point point = new Point();
         projection.toPixels(geo, point);
         point.y -= frad * 3;
         paint.setColor(pinColor);
         paint.setStyle(Paint.Style.FILL_AND_STROKE);
         Path path = new Path();
         RectF oval = new RectF(point.x - frad, point.y - frad, point.x + frad, point.y + frad);
         path.addArc(oval, 150,240);
         path.lineTo(point.x,point.y+frad*3);
         canvas.drawPath(path, paint);
      }
      public void draw(Canvas canvas, Projection projection)
      {
         drawPin(canvas,projection);
         drawBaloon(canvas,projection);
      }
   }
  
   public MapItem[] getItems(String s0)
   {
      String[] ss = s0.split("\\|");
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

      public MyOverLay(GeoPoint gp1) // GeoPoint is a int. (6E)
      {
         this.gp1 = gp1;
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

      public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when)
      {
         Projection projection = mapView.getProjection();
         if (!shadow)
         {
            for (int i = 0; i < items.length; i++)
               items[i].draw(canvas, projection);
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
      mapview.setSatellite(extras.getBoolean("sat"));
      mRadius = Launcher4A.deviceFontHeight/2;
      List<Overlay> overs = mapview.getOverlays();
      String items = extras.getString("items");
      if (items != null)
         overs.add(new MyItemsOverLay(getItems(items)));
      else
      {
         double lat = extras.getDouble("lat");
         double lon = extras.getDouble("lon");
         int ilat = toCoord(lat);
         int ilon = toCoord(lon);
         computeBounds(ilat,ilon);
         overs.add(new MyOverLay(new GeoPoint(ilat,ilon)));
      }      
      // move the map to the given point
      MapController mc = mapview.getController();
      int clat = (ilatMin + ilatMax) / 2;
      int clon = (ilonMin + ilonMax) / 2;
      mc.setCenter(new GeoPoint(clat,clon));
      if (ilatMax == ilatMin || ilonMax == ilonMin)
         mc.setZoom(21);
      else
         mc.zoomToSpan(ilatMax-ilatMin,ilonMax-ilonMin);
   }
   
   private static int toCoord(double v)
   {
      return (int)(v * 1e6);
   }
   private static float fromCoord(int i)
   {
      return (float)i / 1e6f;
   }
   
   protected boolean isRouteDisplayed()
   {
      return false;
   }
}
