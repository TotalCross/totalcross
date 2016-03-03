package totalcross.android;

import totalcross.*;

import android.content.*;
import android.location.*;
import android.os.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.location.*;
import java.util.*;

public class GPSHelper implements ConnectionCallbacks, OnConnectionFailedListener, android.location.LocationListener, com.google.android.gms.location.LocationListener, GpsStatus.Listener
{
   public static GPSHelper instance = new GPSHelper();
   private Location lastLocation;
   private GoogleApiClient googleApiClient;
   private boolean requestingLocationUpdates;

   private LocationRequest locationRequest;

   public void onCreate()
   {
      // First we need to check availability of play services
      if (checkPlayServices())
      {
         // Building the GoogleApi client
         buildGoogleApiClient();
         googleApiClient.connect();
         createLocationRequest();
         displayLocation();
      }

      togglePeriodicLocationUpdates();
   }

   protected void onResume()
   {
      checkPlayServices();
      // Resuming the periodic location updates
      if (googleApiClient.isConnected() && requestingLocationUpdates)
         startLocationUpdates();
   }

   protected void onStop()
   {
      if (googleApiClient.isConnected())
         googleApiClient.disconnect();
   }

   protected void onPause()
   {
      stopLocationUpdates();
   }

   /**
    * Method to display the location on UI
    */
   private void displayLocation()
   {
      lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
      if (lastLocation != null)
      {
         double latitude = lastLocation.getLatitude();
         double longitude = lastLocation.getLongitude();
         AndroidUtils.debug(latitude + ", " + longitude);
      }
   }

   /**
    * Method to toggle periodic location updates
    */
   private void togglePeriodicLocationUpdates()
   {
      if (!requestingLocationUpdates)
         startLocationUpdates();
      else
         stopLocationUpdates();
   }

   /**
    * Creating google api client object
    */
   protected synchronized void buildGoogleApiClient()
   {
      googleApiClient = new GoogleApiClient.Builder(Launcher4A.loader).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
   }

   /**
    * Creating location request object
    */
   protected void createLocationRequest()
   {
      locationRequest = new LocationRequest();
      locationRequest.setInterval(10000);
      locationRequest.setFastestInterval(5000);
      locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      locationRequest.setSmallestDisplacement(10); // 10 meters
   }

   /**
    * Method to verify google play services on the device
    */
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

   /**
    * Starting the location updates
    */
   protected void startLocationUpdates()
   {
      requestingLocationUpdates = true;
      LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
   }

   /**
    * Stopping location updates
    */
   protected void stopLocationUpdates()
   {
      requestingLocationUpdates = false;
      LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
   }

   /**
    * Google api callback methods
    */
   public void onConnectionFailed(ConnectionResult result)
   {
      AndroidUtils.debug("Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
   }

   public void onConnected(Bundle arg0)
   {
      // Once connected with google api, get the location
      displayLocation();

      if (requestingLocationUpdates)
         startLocationUpdates();
   }

   public void onConnectionSuspended(int arg0)
   {
      googleApiClient.connect();
   }

   public static boolean isGpsOn()
   {
      LocationManager manager = (LocationManager) Launcher4A.loader.getSystemService(Context.LOCATION_SERVICE);
      return manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER); 
   }

   public void onLocationChanged(Location loc)
   {
      lastLocation = loc;
      try
      {
         int sats = loc.getExtras().getInt("satellites");
         String provider = loc.getProvider();
         if (provider == null || provider.equals("gps"))
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
      catch (Exception exception)
      {
         lastGps = "*";
      }
   }

   public int validSatellites;

   public String gpsGetData()
   {
      return gps != null && gps.isProviderEnabled(LocationManager.GPS_PROVIDER) ? lastGps : null;
   }
   public String gpsTurn(boolean on)
   {
      if (isGpsOn())
      {
         lastGps = "*";
         Message msg = Launcher4A.viewhandler.obtainMessage();
         Bundle b = new Bundle();
         b.putInt("type", on ? Launcher4A.GPSFUNC_START : Launcher4A.GPSFUNC_STOP);
         msg.setData(b);
         Launcher4A.viewhandler.sendMessage(msg);
         if (on)
         {
            while (gps == null)
               try {Thread.sleep(100);} catch (Exception e) {}
            return gps.isProviderEnabled(LocationManager.GPS_PROVIDER) ? "*" : null;
         }
      }
      return null;
   }
   public void onGpsStatusChanged(int event) 
   {
      if (gps != null && (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS || event == GpsStatus.GPS_EVENT_FIRST_FIX)) 
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
   String lastGps = "*";

   public void startGps()
   {
      gps = (LocationManager) Launcher4A.loader.getSystemService(Context.LOCATION_SERVICE);
      gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
      gps.addGpsStatusListener(this);
   }
   public void stopGps()
   {
      if (gps != null)
      {
         gps.removeUpdates(this);
         gps.removeGpsStatusListener(this);
         validSatellites = 0;
         gps = null;
      }
   }

   public void sendStopGps()
   {
      if (gps != null) // stop the gps if still running
      {
         Message msg = Launcher4A.viewhandler.obtainMessage();
         Bundle b = new Bundle();
         b.putInt("type", Launcher4A.GPSFUNC_STOP);
         msg.setData(b);
         Launcher4A.viewhandler.sendMessage(msg);
      }
   }
}
