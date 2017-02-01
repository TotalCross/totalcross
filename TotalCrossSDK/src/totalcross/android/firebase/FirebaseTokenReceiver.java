package totalcross.android.firebase;

/** Receives the toke,n from Google Cloud Messaging.
 *  Called by totalcross.Loader. 
 *  MUST BE INVOKED IN A SEPARATE SERVICE. 
 */

public class FirebaseTokenReceiver extends IntentService
{
   public FirebaseTokenReceiver()
   {
      super("FirebaseTokenReceiver");
   }

   protected void onHandleIntent(Intent i)
   {
      try
      {
         String pushTokenAndroid = GCMUtils.getToken(getApplicationContext());
         String token = InstanceID.getInstance(this).getToken(pushTokenAndroid, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
         FirebaseUtils.writeToken(this, pushTokenAndroid, token);
         FirebaseUtils.sendBroadcast(this, Launcher4A.TOKEN_RECEIVED);
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e,false);
      }
   }
}
