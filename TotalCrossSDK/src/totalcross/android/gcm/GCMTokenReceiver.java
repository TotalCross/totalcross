package totalcross.android.gcm;

import totalcross.*;

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
   public GCMTokenReceiver()
   {
      super(GCMUtils.pushTokenAndroid);
      AndroidUtils.debug("*** PUSH TOKEN ANDROID receiver: "+GCMUtils.pushTokenAndroid);
   }

   protected void onHandleIntent(Intent i)
   {
      try
      {
         String token = InstanceID.getInstance(this).getToken(GCMUtils.pushTokenAndroid, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
         GCMUtils.writeToken(this, GCMUtils.pushTokenAndroid, token);
         GCMUtils.sendBroadcast(this, Launcher4A.TOKEN_RECEIVED);
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e,false);
      }
   }
}
