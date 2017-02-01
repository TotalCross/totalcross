package totalcross.android.firebase;

import static totalcross.android.firebase.LocalBroadcastManager.TAG;

import android.content.Intent;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.iid.FirebaseInstanceId;

public class MyInstanceIDListenerService extends FirebaseInstanceIdService
{

   /**
    * Based on https://developers.google.com/cloud-messaging/android/android-migrate-fcm
	*/
	
   /**
    * Called if InstanceID token is updated. This may occur if the security of
    * the previous token had been compromised. Note that this is also called
    * when the InstanceID token is initially generated, so this is where
    * you retrieve the token.
    */
   // [START refresh_token]
   @Override
   public void onTokenRefresh() {
      // Get updated InstanceID token.
      String refreshedToken = FirebaseInstanceId.getInstance().getToken();
      Log.d(TAG, "Refreshed token: " + refreshedToken);
      // TODO: Implement this method to send any registration to your app's servers.
      // sendRegistrationToServer(refreshedToken);
	  
	  // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
	  FirebaseUtils.writeToken(this, refreshedToken, refreshedToken);
      startService(new Intent(this, FirebaseTokenReceiver.class));
   }

}
