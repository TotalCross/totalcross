package totalcross.android;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.location.GnssStatusCompat;
import androidx.core.location.LocationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import totalcross.AndroidUtils;
import totalcross.Launcher4A;

public class GPSHelper extends GnssStatusCompat.Callback implements android.location.LocationListener, LocationListener, ConnectionCallbacks, OnConnectionFailedListener
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
      boolean isHigh = Launcher4A.gpsPrecision == HIGH_GPS_PRECISION;
      if (isHigh)
         return gps != null && gps.isProviderEnabled(LocationManager.GPS_PROVIDER) ? lastGps : null;
      String ret = isHigh ? (gps != null ? lastGps : null) : (googleApiClient != null ? lastGps : null);
      //lastGps = NOGPS;
      Location fusedLocation = null;
      try {
      if ((ret == null || ret.equals(NOGPS)) && googleApiClient != null && (fusedLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)) != null)
      {
         onLocationChanged(fusedLocation);
         ret = lastGps;
         lastGps = NOGPS;
      }
      } catch (SecurityException e) {
         e.printStackTrace();
      }
      return ret;
   }
   
   public String gpsTurn(boolean on)
   {
      if (isGpsOn())
      {
         boolean isHigh = Launcher4A.gpsPrecision == HIGH_GPS_PRECISION;
         lastGps = NOGPS;
         Message msg = Launcher4A.viewhandler.obtainMessage();
         Bundle b = new Bundle();
         b.putInt("type", on ? Launcher4A.GPSFUNC_START : Launcher4A.GPSFUNC_STOP);
         msg.setData(b);
         Launcher4A.viewhandler.sendMessage(msg);
         if (on) // if turning on, wait until the message is handled
         {
            while (gps == null)
               try {Thread.sleep(50);} catch (Exception e) {}
            return (isHigh && gps.isProviderEnabled(LocationManager.GPS_PROVIDER)) || (!isHigh && gps != null) ? NOGPS : null;
         }
      }
      return null;
   }

   @Override
   public void onSatelliteStatusChanged(@NonNull GnssStatusCompat status) {
      int usedInFix = 0;
      for (int i = 0 ; i < status.getSatelliteCount() ; i++) {
         if (status.usedInFix(i)) {
            usedInFix++;
         }
      }
      validSatellites = usedInFix;
   }

   public void onProviderDisabled(String provider)   {}
   public void onProviderEnabled(String provider)    {}
   public void onStatusChanged(String provider, int status, Bundle extras)   {}

   LocationManager gps;
   
   private static final int HIGH_GPS_PRECISION = 0;

   // high=gps only, low=gps+googleApiClient (gps is used to get satellite info)
   public void startGps()
   {
      boolean isHigh = Launcher4A.gpsPrecision == HIGH_GPS_PRECISION;
      if (isHigh && gps == null)
      {
         gps = (LocationManager) Launcher4A.loader.getSystemService(Context.LOCATION_SERVICE);
         try {
         gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, instance);
         LocationManagerCompat.registerGnssStatusCallback(gps, ContextCompat.getMainExecutor(Launcher4A.loader),instance);
	     } catch (SecurityException e) {
		     e.printStackTrace();
		 }
      }      
      if (!isHigh && googleApiClient == null && /*Launcher4A.gpsPrecision == LOW_GPS_PRECISION && */checkPlayServices())
      {
         locationRequest = LocationRequest.create();
         locationRequest.setInterval(5000);
         locationRequest.setFastestInterval(1000);
         locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
         locationRequest.setSmallestDisplacement(2); // meters

         googleApiClient = new GoogleApiClient.Builder(Launcher4A.loader).addConnectionCallbacks(this)
               .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
         googleApiClient.connect();
         
         // try only to get satellite information, since this is not given by Google Play Services
         gps = (LocationManager) Launcher4A.loader.getSystemService(Context.LOCATION_SERVICE);
         try {
            LocationManagerCompat.registerGnssStatusCallback(gps, ContextCompat.getMainExecutor(Launcher4A.loader),instance);
         } catch (SecurityException e) {
         	e.printStackTrace();
         }    
      }
   }
   public void stopGps()
   {      
      if (gps != null)
         try {gps.removeUpdates(instance);} catch (Throwable t) {}

      if (googleApiClient != null)
      {
         LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
         googleApiClient = null;
      }
      if (gps != null) {
         LocationManagerCompat.unregisterGnssStatusCallback(gps, this);
      }
      gps = null;
      validSatellites = 0;
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
      if (googleApiClient != null)
      {
         if (googleApiClient.isConnected())
            googleApiClient.disconnect();
         googleApiClient = null;
      }         
   }

   public void onConnected(Bundle arg0)
   {
      if (googleApiClient != null)
    	try {
         LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } catch (SecurityException e) {
        	e.printStackTrace();
        }
   }

   public void onConnectionSuspended(int arg0)
   {
      if (googleApiClient != null)
         googleApiClient.connect();
   }

   public void onConnectionFailed(ConnectionResult arg0)
   {
   }
}
