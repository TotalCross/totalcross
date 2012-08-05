package totalcross.android;

import android.graphics.*;
import android.os.*;
import android.view.*;
import com.google.android.maps.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import totalcross.*;

public class RouteViewer extends MapActivity
{
   /** Called when the activity is first created. */
   private class MyOverLay extends Overlay
   {
      private GeoPoint gp1;
      private GeoPoint gp2;
      private int mRadius = 6;
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
         GeoPoint center = srcGeoPoint;
         if (latI != 0 && lonI != 0 && latF != 0 && lonF != 0)
            drawRoute(srcGeoPoint, dstGeoPoint, Color.RED, mapview);
         // traversed points
         if (scoords != null)
         {
            String[] s = scoords.split(",");
            GeoPoint[]coords = new GeoPoint[s.length/2];
            for (int i = 0,j=0; i < s.length;)
               coords[j++] = new GeoPoint((int)(Double.valueOf(s[i++])*1E6),(int)(Double.valueOf(s[i++])*1E6));
            List<Overlay> overs = mapview.getOverlays();
            overs.add(new MyOverLay(coords[0], coords[0], 1));
            if (coords.length > 1)
            {
               for (int i = 1; i < coords.length; i++)
                  overs.add(new MyOverLay(coords[i-1], coords[i], 2, 0x00FF00));
               overs.add(new MyOverLay(coords[coords.length-1], coords[coords.length-1], 3));
            }
            center = coords[0];
         }
         // move the map to the given point
         mapview.getController().setCenter(center);
         mapview.getController().setZoom(21);
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

   private void drawRoute(GeoPoint src, GeoPoint dest, int color, MapView mapview) throws Exception
   {
      // connect to map web service
      StringBuilder urlString = new StringBuilder(128).
         append("http://maps.google.com/maps?f=d&hl=pt-br").
         append("&saddr=").// from
         append(Double.toString((double) src.getLatitudeE6() / 1.0E6)).
         append(",").
         append(Double.toString((double) src.getLongitudeE6() / 1.0E6)).
         append("&daddr=").// to
         append(Double.toString((double) dest.getLatitudeE6() / 1.0E6)).
         append(",").
         append(Double.toString((double) dest.getLongitudeE6() / 1.0E6)).
         append("&ie=UTF8&0&om=0&output=kml");

      // get the kml (XML) doc. And parse it to get the coordinates(direction route).
      Document doc = null;
      HttpURLConnection urlConnection = null;
      URL url = new URL(urlString.toString());
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("GET");
      urlConnection.setDoOutput(true);
      urlConnection.setDoInput(true);
      urlConnection.connect();
      byte[] bytes = AndroidUtils.readFully(urlConnection.getInputStream());
      AndroidUtils.debug(new String(bytes));

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      doc = dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(bytes));
      List<Overlay> overs = mapview.getOverlays();

      NodeList node = doc.getElementsByTagName("GeometryCollection");
      if (node.getLength() > 0)
      {
         String path = node.item(0).getFirstChild().getFirstChild().getFirstChild().getNodeValue();

         String[] pairs = path.split(" ");
         String[] lngLat = pairs[0].split(","); // lngLat[0]=longitude, lngLat[1]=latitude, lngLat[2]=height

         // src
         GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
         overs.add(new MyOverLay(startGP, startGP, 1));

         GeoPoint gp1;
         GeoPoint gp2 = startGP;
         for (int i = 1; i < pairs.length; i++) // the last one would be crash
         {
            lngLat = pairs[i].split(",");
            gp1 = gp2;
            // watch out! For GeoPoint, first:latitude, second:longitude
            gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
            overs.add(new MyOverLay(gp1, gp2, 2, color));
         }
         overs.add(new MyOverLay(dest, dest, 3)); // use the default color
      }
   }
}
