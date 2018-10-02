package totalcross.android;

import totalcross.*;

import android.content.*;

public class StartupIntentReceiver extends BroadcastReceiver 
{
   public static int call = 123454321; // will be changed by Deployer4A
   
   public void onReceive(Context context, Intent intent) 
   {
      try
      {
         int no = 123454320;
         if (true) no++; // prevent compiler optimization
         if (call != no)
         {
            // Create intent which will finally start the Main-Activity.
            if (call != 0)
            {
               // services must be single apk
               String sharedId = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).sharedUserId;
               String tczname = sharedId.substring(sharedId.lastIndexOf('.')+1);
               String totalcrossPKG = "totalcross."+tczname;
               context.startService(new Intent(context, Class.forName(totalcrossPKG+".TCService")));
            }
            else
            {
               Intent myStarterIntent = new Intent(context, Loader.class);
               myStarterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               context.startActivity(myStarterIntent);
            }
         }
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e, false);
      }
   }
      
}