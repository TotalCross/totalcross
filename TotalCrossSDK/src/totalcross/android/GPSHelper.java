package totalcross.android;

import totalcross.*;

import android.content.*;
import android.location.*;
import android.os.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.location.*;
import com.google.android.gms.location.LocationListener;
import java.util.*;

public class GPSHelper implements android.location.LocationListener, GpsStatus.Listener, LocationListener, ConnectionCallbacks, OnConnectionFailedListener
{
   public static GPSHelper instance = new GPSHelper();
   private GoogleApiClient googleApiClient;
   private LocationRequest locationRequest;
   private int validSatellites;
   private static final String NOGPS = "*";
   private String lastGps = NOGPS;

   private boolean checkPlayServices()
   {
      GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
      int result = googleAPI.isGooglePlayServicesAvailable(Launcher4A.loader);
      if (result != ConnectionResult.SUCCESS)
      {
          if (googleAPI.isUserResolvableError(result)) 
              googleAPI.getErrorDialog(Launcher4A.loader, result, 0).show();
          return false;
      }
      return true;
   }

   public static boolean isGpsOn()
   {
      LocationManager manager = (LocationManager) Launcher4A.loader.getSystemService(Context.LOCATION_SERVICE);
      return manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER); 
   }

   public void onLocationChanged(Location loc)
   {
      try
      {
         Bundle b = loc.getExtras();
         int sats = b != null ? b.getInt("satellites") : 0;
         String provider = loc.getProvider();
         AndroidUtils.debug(loc.toString());
         if (provider == null || provider.equals("gps") || provider.equals("fused"))
         {
            String lat = Double.toString(loc.getLatitude()); //flsobral@tc126_57: Decimal separator might be platform dependent when using Location.convert with Location.FORMAT_DEGREES.
            String lon = Double.toString(loc.getLongitude());
            
            Calendar fix = new GregorianCalendar(TimeZone.getTimeZone("GMT")); //flsobral@tc126_57: Date is deprecated, and apparently bugged for some devices. Replaced with Calendar.
            fix.setTimeInMillis(loc.getTime());
            String sat = String.valueOf(Math.max(sats, validSatellites));
            String vel = loc.hasSpeed() && loc.getSpeed() != 0d ? String.valueOf(loc.getSpeed())   : "";
            String dir = loc.hasBearing() ? String.valueOf(loc.getBearing()) : "";
            String sfix = fix.get(Calendar.YEAR)+"/"+(fix.get(Calendar.MONTH)+1)+"/"+fix.get(Calendar.DAY_OF_MONTH)+" "+fix.get(Calendar.HOUR_OF_DAY)+":"+fix.get(Calendar.MINUTE)+":"+fix.get(Calendar.SECOND);
            float pdop = loc.hasAccuracy() ? loc.getAccuracy() : 0; // guich@tc126_66
            lastGps = lat+";"+lon+";"+sfix+";"+sat+";"+vel+";"+dir+";"+pdop+";";
         }
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e, false);
         lastGps = NOGPS;
      }
   }

   public String gpsGetData()
   {
      String ret = googleApiClient != null ? lastGps : null;
      //lastGps = NOGPS;
      Location fusedLocation = null;
      if ((ret == null || ret.equals(NOGPS)) && (fusedLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)) != null)
      {
         onLocationChanged(fusedLocation);
         ret = lastGps;
         lastGps = NOGPS;
      }
      return ret;
   }
   
   public String gpsTurn(boolean on)
   {
      if (isGpsOn())
      {
         lastGps = NOGPS;
         Message msg = Launcher4A.viewhandler.obtainMessage();
         Bundle b = new Bundle();
         b.putInt("type", on ? Launcher4A.GPSFUNC_START : Launcher4A.GPSFUNC_STOP);
         msg.setData(b);
         Launcher4A.viewhandler.sendMessage(msg);
         if (on)
         {
            while (googleApiClient == null)
               try {Thread.sleep(100);} catch (Exception e) {}
            return googleApiClient != null ? NOGPS : null;
         }
      }
      return null;
   }
   
   public void onGpsStatusChanged(int event) 
   {
      if (googleApiClient != null && (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS || event == GpsStatus.GPS_EVENT_FIRST_FIX)) 
      {
         GpsStatus status = gps.getGpsStatus(null);
         Iterable<GpsSatellite> sats = status.getSatellites();
         validSatellites = 0;
         for (GpsSatellite sat : sats)
            if (sat.usedInFix())
               validSatellites++;
      }
   }

   public void onProviderDisabled(String provider)   {}
   public void onProviderEnabled(String provider)    {}
   public void onStatusChanged(String provider, int status, Bundle extras)   {}

   LocationManager gps;
   
//   private static final int LOW_GPS_PRECISION = 1;

   public void startGps()
   {
      if (googleApiClient == null && /*Launcher4A.gpsPrecision == LOW_GPS_PRECISION && */checkPlayServices())
      {
         locationRequest = LocationRequest.create();
         locationRequest.setInterval(5000);
         locationRequest.setFastestInterval(1000);
         locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
         locationRequest.setSmallestDisplacement(2); // meters

         googleApiClient = new GoogleApiClient.Builder(Launcher4A.loader).addConnectionCallbacks(this)
               .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
         googleApiClient.connect();
         
         gps = (LocationManager) Launcher4A.loader.getSystemService(Context.LOCATION_SERVICE);
         gps.addGpsStatusListener(this);
      }
   }
   public void stopGps()
   {
      if (googleApiClient != null)
      {
         LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
         validSatellites = 0;
         googleApiClient = null;

         gps.removeGpsStatusListener(this);
         gps = null;
      }
   }

   public void sendStopGps()
   {
      if (googleApiClient != null) // stop the gps if still running
      {
         Message msg = Launcher4A.viewhandler.obtainMessage();
         Bundle b = new Bundle();
         b.putInt("type", Launcher4A.GPSFUNC_STOP);
         msg.setData(b);
         Launcher4A.viewhandler.sendMessage(msg);
      }
      if (googleApiClient != null)
      {
         if (googleApiClient.isConnected())
            googleApiClient.disconnect();
         googleApiClient = null;
      }         
   }

   public void onConnected(Bundle arg0)
   {
      LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
   }

   public void onConnectionSuspended(int arg0)
   {
      googleApiClient.connect();
   }

   public void onConnectionFailed(ConnectionResult arg0)
   {
      AndroidUtils.debug("onConnectionFailed "+arg0);
   }
}
