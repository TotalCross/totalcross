package totalcross.android.gcm;

import android.os.*;
import android.util.*;
import com.google.android.gms.gcm.*;

public class MyGcmListenerService extends GcmListenerService 
{
    private static final String TAG = "TotalCross";

    /**
     * Called when message is received.
     * ex:
     * time: 15:10
       score: 5x1
       collapse_key: do_not_collapse
       From: 954430333411
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
       if (data == null)
          return;
       
       for (String o: data.keySet())
          Log.i(TAG, o+": "+data.getString(o));
        Log.i(TAG, "From: " + from);
    }
}