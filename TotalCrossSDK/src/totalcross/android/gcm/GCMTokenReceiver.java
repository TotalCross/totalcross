package totalcross.android.gcm;

import totalcross.AndroidUtils;
import android.app.*;
import android.content.*;
import com.google.android.gms.gcm.*;
import com.google.android.gms.iid.*;

/** Receives the token from Google Cloud Messaging.
 *  Called by totalcross.Loader. 
 *  MUST BE INVOKED IN A SEPARATE SERVICE. 
 */

public class GCMTokenReceiver extends IntentService
{
   private static final String SENDER_ID = "957130962496";

   public GCMTokenReceiver()
   {
      super(SENDER_ID);
   }

   protected void onHandleIntent(Intent i)
   {
      try
      {
         String token = InstanceID.getInstance(this).getToken(SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
         AndroidUtils.debug("GCM Registration Token : "+token);
         GCM2TC.writeEvent(GCM2TC.TOKEN_RECEIVED, token);
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e,false);
      }
   }
}
