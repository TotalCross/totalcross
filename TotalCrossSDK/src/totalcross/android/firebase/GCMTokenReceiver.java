package totalcross.android.firebase;

/** Receives the toke,n from Google Cloud Messaging.
 *  Called by totalcross.Loader. 
 *  MUST BE INVOKED IN A SEPARATE SERVICE. 
 */

public class GCMTokenReceiver extends IntentService
{
   public GCMTokenReceiver()
   {
      super("GCMTokenReceiver");
   }

   protected void onHandleIntent(Intent i)
   {
      try
      {
         String pushTokenAndroid = GCMUtils.getToken(getApplicationContext());
         String token = InstanceID.getInstance(this).getToken(pushTokenAndroid, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
         GCMUtils.writeToken(this, pushTokenAndroid, token);
         GCMUtils.sendBroadcast(this, Launcher4A.TOKEN_RECEIVED);
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e,false);
      }
   }
}
