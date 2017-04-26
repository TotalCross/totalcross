package totalcross.app.stub;

import android.content.*;

public class StartupIntentReceiver extends BroadcastReceiver 
{
   public static int call = 123454321; // will be changed by Deployer4A
   
   public void onReceive(Context context, Intent intent) 
   {
      int no = 123454320;
      if (true) no++; // prevent compiler optimization
      if (call != no)
      {
         // Create intent which will finally start the Main-Activity.
         Intent myStarterIntent = new Intent(context, Stub.class);
         // Set the Launch-Flag to the Intent.
         myStarterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         // Send the Intent to the OS.
         context.startActivity(myStarterIntent);
      }
   }
}