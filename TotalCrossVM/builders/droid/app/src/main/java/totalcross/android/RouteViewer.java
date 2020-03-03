package totalcross.android;

import totalcross.*;

import java.net.*;
import java.util.*;

import android.graphics.*;
import android.os.*;
import android.os.StrictMode.ThreadPolicy;
import android.view.*;

import com.google.android.maps.*;

public class RouteViewer extends MapActivity
{
   /** Called when the activity is first created. */
   private class MyOverLay extends Overlay
   {
      private GeoPoint gp1;
      private GeoPoint gp2;
      private int mRadius;
      private int mode;
      private int defaultColor;
      private String text;
      private Bitmap img;

      public MyOverLay(GeoPoint gp1, GeoPoint gp2, int mode) // GeoPoint is a int. (6E)
      {
         this(gp1,gp2,mode,999);
      }

      public MyOverLay(GeoPoint gp1, GeoPoint gp2, int mode, int defaultColor)
      {
         this.gp1 = gp1;
         this.gp2 = gp2;
         this.mode = mode;
         this.defaultColor = defaultColor;
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
            switch (mode)
            {
               case 1: // start point
               {
                  paint.setColor(defaultColor != 999 ? defaultColor : Color.BLUE);
                  RectF oval = new RectF(point.x - mRadius, point.y - mRadius, point.x + mRadius, point.y + mRadius);
                  canvas.drawOval(oval, paint);
                  break;
               }
               case 2:
               {
                  paint.setColor(defaultColor != 999 ? defaultColor : Color.RED);
                  Point point2 = new Point();
                  projection.toPixels(gp2, point2);
                  paint.setStrokeWidth(5);
                  paint.setAlpha(120);
                  canvas.drawLine(point.x, point.y, point2.x, point2.y, paint);
                  break;
               }
               case 3: // the last path
               {
                  paint.setColor(defaultColor != 999 ? defaultColor : Color.GREEN);
                  Point point2 = new Point();
                  projection.toPixels(gp2, point2);
                  paint.setStrokeWidth(5);
                  paint.setAlpha(120);
                  canvas.drawLine(point.x, point.y, point2.x, point2.y, paint);
                  RectF oval = new RectF(point2.x - mRadius, point2.y - mRadius, point2.x + mRadius, point2.y + mRadius);
                  /* end point */
                  paint.setAlpha(255);
                  canvas.drawOval(oval, paint);
                  break;
               }
               case 4: // car
               {
                  paint.setColor(defaultColor != 999 ? defaultColor : Color.GREEN);
                  Point point2 = new Point();
                  projection.toPixels(gp2, point2);
                  paint.setTextSize(20);
                  paint.setAntiAlias(true);
                  canvas.drawBitmap(img, point2.x, point2.y, paint);
                  canvas.drawText(this.text, point2.x, point2.y, paint);
                  break;
               }
               case 5:
               {
                  paint.setColor(defaultColor != 999 ? defaultColor : Color.GREEN);
                  Point point2 = new Point();
                  projection.toPixels(gp2, point2);
                  paint.setTextSize(20);
                  paint.setAntiAlias(true);
                  canvas.drawBitmap(img, point2.x, point2.y, paint);
               }
            }
         }
         return super.draw(canvas, mapView, shadow, when);
      }
   }

   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.route);
      StrictMode.setThreadPolicy(ThreadPolicy.LAX); // get rid of NetworkOnMainThreadException

      try
      {
         MapView mapview = (MapView) findViewById(R.id.myMapView1);
         mapview.setBuiltInZoomControls(true);
         mapview.setClickable(true);
         mapview.setStreetView(true);
         if (Loader.isFullScreen)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         // get passed parameters
         Bundle extras = getIntent().getExtras();
         double latI = extras.getDouble("latI");
         double lonI = extras.getDouble("lonI");
         double latF = extras.getDouble("latF");
         double lonF = extras.getDouble("lonF");
         String scoords = extras.getString("coord");
         mapview.setSatellite(extras.getBoolean("sat"));
         GeoPoint srcGeoPoint = new GeoPoint((int) (latI * 1E6), (int) (lonI * 1E6));
         GeoPoint dstGeoPoint = new GeoPoint((int) (latF * 1E6), (int) (lonF * 1E6));
         List<GeoPoint> pairs = null;
         if (latI != 0 && lonI != 0 && latF != 0 && lonF != 0)
            pairs = getRoute(srcGeoPoint, dstGeoPoint);
         else // traversed points
         if (scoords != null)
         {
            String[] s = scoords.split(",");
            pairs = new ArrayList<GeoPoint>(s.length/2);
            for (int i = 0; i < s.length;)
               pairs.add(new GeoPoint((int)(Double.valueOf(s[i++])*1E6),(int)(Double.valueOf(s[i++])*1E6)));
         }
         if (pairs == null)
            throw new Exception("No lat/lon found");
         int color = Color.RED;
         int n = pairs.size();
         List<Overlay> overs = mapview.getOverlays();
         GeoPoint p1 = pairs.get(0);
         GeoPoint p2 = pairs.get(n-1);
         overs.add(new MyOverLay(p1,p1, 1));
         for (int i = 1; i < n; i++) // the last one would be crash
            overs.add(new MyOverLay(pairs.get(i-1), pairs.get(i), 2, color));
         overs.add(new MyOverLay(p2,p2, 3));
     
         // move the map to the given point
         mapview.getController().zoomToSpan(Math.abs(p1.getLatitudeE6() - p2.getLatitudeE6()), Math.abs(p1.getLongitudeE6() - p2.getLongitudeE6()));
         mapview.getController().animateTo(new GeoPoint(p1.getLatitudeE6() - ((p1.getLatitudeE6() - p2.getLatitudeE6())/2), p1.getLongitudeE6() - ((p1.getLongitudeE6() - p2.getLongitudeE6())/2)));
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e,false);
         finish();
      }
   }

   protected boolean isRouteDisplayed()
   {
      return true;
   }

   private List<GeoPoint> getRoute(GeoPoint src, GeoPoint dest) throws Exception
   {
      // connect to map web service
      StringBuilder urlString = new StringBuilder(128).
         append("http://maps.googleapis.com/maps/api/directions/xml?origin=").
         append(Double.toString((double) src.getLatitudeE6() / 1.0E6)).
         append(",").
         append(Double.toString((double) src.getLongitudeE6() / 1.0E6)).
         append("&destination=").// to
         append(Double.toString((double) dest.getLatitudeE6() / 1.0E6)).
         append(",").
         append(Double.toString((double) dest.getLongitudeE6() / 1.0E6)).
         append("&sensor=false");
      // http://maps.googleapis.com/maps/api/directions/xml?origin=-22.966923,-43.185766&destination=-22.955736,-43.198185&sensor=false
      // http://stackoverflow.com/questions/11323500/google-maps-api-version-difference/11357351#11357351
      AndroidUtils.debug(urlString.toString());
      HttpURLConnection urlConnection = (HttpURLConnection) new URL(urlString.toString()).openConnection();
      urlConnection.connect();
      byte[] bytes = AndroidUtils.readFully(urlConnection.getInputStream());
      String s = new String(bytes);
      int idx = s.indexOf("<overview_polyline>");
      if (idx == -1) throw new Exception("Cannot find overview_polyline");
      int idx1 = s.indexOf("<points>", idx) + 8;
      int idx2 = s.indexOf("</points>",idx);
      return decodePoly(s,idx1,idx2);
   }
   
   private List<GeoPoint> decodePoly(String encoded, int index, int end) 
   {
      List<GeoPoint> poly = new ArrayList<GeoPoint>(50);
      int lat = 0, lng = 0;

      while (index < end) 
      {
          int b, shift = 0, result = 0;
          do 
          {
              b = encoded.charAt(index++) - 63;
              result |= (b & 0x1f) << shift;
              shift += 5;
          } while (b >= 0x20);
          int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
          lat += dlat;

          shift = 0;
          result = 0;
          do 
          {
              b = encoded.charAt(index++) - 63;
              result |= (b & 0x1f) << shift;
              shift += 5;
          } while (b >= 0x20);
          int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
          lng += dlng;

          GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));
          poly.add(p);
      }

      return poly;
  }

}
