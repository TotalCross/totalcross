package totalcross.android.firebase;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import totalcross.AndroidUtils;
import totalcross.Launcher4A;

public class FirebaseUtils
{
	private static FirebaseApp registeredFirebaseApp;
	
	public static void registerFirebaseApp(FirebaseApp newFirebaseApp) {
		registeredFirebaseApp = newFirebaseApp;
	}
	
	public static String getTokenFromRegisteredFirebaseApp() {
		if (registeredFirebaseApp != null) {
			FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance(registeredFirebaseApp);
			
			if (instanceId != null) {
				return instanceId.getToken();
			}
		}
		
		return null;
	}
	
   private static String vmPath(Context cnt)
   {
      try
      {
         String ret = cnt.getPackageManager().getPackageInfo(cnt.getPackageName(), 0).applicationInfo.dataDir+"/";
         return ret;
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e,true);
      }
      return ".";
   }
   ///// send to application
   public static void writeToken(Context cnt, String id, String token) throws IOException
   {
      String name = vmPath(cnt)+"push_token.dat";
      writeChars(name, false, token);
   }

   public static void writeMessage(Context cnt, String classname, String msg)
   {
      String name = vmPath(cnt)+"push_messages.dat";
      try
      {
         writeChars(name, true, msg);
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e, false);
      }
   }

   public static String getToken(Context c)
   {
      String ret = c.getSharedPreferences("push_token",0).getString("push_token", null);
      AndroidUtils.debug("getToken: "+ret);
      return ret;
   }
   
   public static void setToken(Context c, String tok)
   {
      if (tok.startsWith("_"))
         tok = tok.substring(1,tok.length()-1);
      c.getSharedPreferences("push_token",0).edit().putString("push_token", tok).commit();
   }
   
	private static void writeChars(String name, boolean append, String msg) throws IOException {
		try {
			FileOutputStream f = null;
			for (int tries = 0; tries < 100; tries++) {
				try {
					f = new FileOutputStream(name, append);
					break;
				} catch (Exception e) {
					AndroidUtils.handleException(e,false);
					try {
						Thread.sleep(25);
					} catch (Exception ee) {
						
					}
				}
			}
			if (f == null) {
				AndroidUtils.debug("Cannot open output file "+name);
			} else {
				DataOutputStream ds = new DataOutputStream(f);
				ds.writeShort(msg.length());
				for (int i = 0, n = msg.length(); i < n; i++) {
					ds.writeChar(msg.charAt(i));
				}
				f.close();
			}
		} catch (FileNotFoundException fnfe) {
		}
	}
   
   // broadcast events

   public static void sendBroadcast(Context cnt, int event)
   {
      Intent in = new Intent("totalcross.MESSAGE_EVENT");
      in.putExtra("event", event);
      LocalBroadcastManager.getInstance(cnt).sendBroadcast(in);
   }

}
