package totalcross.android.gcm;

import android.app.*;
import android.content.*;
import android.preference.*;
import android.util.*;
import com.google.android.gms.gcm.*;
import com.google.android.gms.iid.*;

public class RegistrationIntentService extends IntentService
{
   public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
   public static final String REGISTRATION_COMPLETE = "registrationComplete";
   private static final String SENDER_ID = "957130962496";
   private static final String TAG = "TotalCross";

   public RegistrationIntentService()
   {
      super(TAG);
   }

   @Override
   protected void onHandleIntent(Intent intent)
   {
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

      try
      {
         // [START register_for_gcm]
         // Initially this call goes out to the network to retrieve the token, subsequent calls
         // are local.
         // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
         // See https://developers.google.com/cloud-messaging/android/start for details on this file.
         // [START get_token]
         InstanceID instanceID = InstanceID.getInstance(this);
         String token = instanceID.getToken(SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
         // [END get_token]
         Log.i(TAG, "GCM Registration Token: " + token);

         sendRegistrationToServer(token);

         // You should store a boolean that indicates whether the generated token has been
         // sent to your server. If the boolean is false, send the token to your server,
         // otherwise your server should have already received the token.
         sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
         // [END register_for_gcm]
      }
      catch (Exception e)
      {
         Log.d(TAG, "Failed to complete token refresh", e);
         // If an exception happens while fetching the new token or updating our registration data
         // on a third-party server, this ensures that we'll attempt the update at a later time.
         sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
      }
      // Notify UI that registration has completed, so the progress indicator can be hidden.
      // Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
      // LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
   }

   /**
    * Persist registration to third-party servers. Modify this method to associate the user's GCM registration token
    * with any server-side account maintained by your application.
    */
   private void sendRegistrationToServer(String token)
   {
   }
}
