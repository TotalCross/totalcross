package totalcross.android;

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
         if (call != 0)
            context.startService(new Intent(context, TCService.class));
         else
         {
            Intent myStarterIntent = new Intent(context, Loader.class);
            myStarterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myStarterIntent);
         }
      }
   }
}