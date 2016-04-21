package totalcross.android.gcm;

import android.content.*;
import com.google.android.gms.iid.*;
import android.util.Log;
public class MyInstanceIDListenerService extends InstanceIDListenerService 
{
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */

    public void onTokenRefresh() 
     {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
       Log.i("TotalCross", "MyInstanceIDListenerService.onTokenRefresh");
        startService(new Intent(this, GCMTokenReceiver.class));

    }
}
