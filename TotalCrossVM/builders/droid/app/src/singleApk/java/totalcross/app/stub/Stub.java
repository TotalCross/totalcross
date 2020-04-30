// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



package totalcross.app.stub;

import totalcross.*;

import android.app.*;
import android.app.ActivityManager.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import java.util.*;

public class Stub extends Activity 
{
   private class LengthyTask extends AndroidUtils.StartupTask 
   {
       public LengthyTask(Activity activity) {
           super(activity);
       }
       
      protected void onPostExecute(Integer result) 
      {
         try
         {
            runApplication();
         }
         catch (Exception e)
         {
            AndroidUtils.handleException(e,true);
         }
      }
   }
   /** Called when the activity is first created. */   
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      
      try
      {
         AndroidUtils.initialize(this);
         new LengthyTask(this).execute((Object)null);
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e,true);
      }
   }
   
   private void runApplication() throws Exception
   {
      // launch the vm's intent
      Intent intent = new Intent("android.intent.action.MAIN");
      intent.setClassName("totalcross.android","totalcross.android.Loader");
      
      Hashtable<String,String> ht = AndroidUtils.readVMParameters();
      String lastTCZ = ht.get("tczname");
      ht.clear();
      // set some parameters
      String app = getClass().getName();
      int dot = app.lastIndexOf('.');
      String tczName = app.substring(dot+1); // strip the package
      ht.put("package", app.substring(0,dot));
      ht.put("tczname", tczName);
      ht.put("apppath", AndroidUtils.pinfo.applicationInfo.dataDir);
      ht.put("fullscreen", "fullscreen:1".equals(getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString("isFullScreen")) ? "true" : "false"); // no longer used
      // set commandline
      Bundle extras = getIntent().getExtras();
      if (extras != null && extras.containsKey("cmdline"))
         ht.put("cmdline",extras.getString("cmdline"));

      int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_FROM_BACKGROUND; // clear_top is essential to make it work (otherwise, the last called app will be launched instead of the current one)
      boolean sameProgram = lastTCZ != null && tczName.equals(lastTCZ);
      AndroidUtils.debug(sameProgram ? "Program "+tczName+" being resumed" : "Program change detected: "+lastTCZ+" -> "+tczName);
      if (!sameProgram) // if its not the same app that was launched, remove all other tasks
         flags |= Intent.FLAG_ACTIVITY_CLEAR_TOP;
      intent.setFlags(flags);
      AndroidUtils.writeVMParameters(ht);
      
      try
      {
         startActivity(intent);
         
         ActivityManager actvityManager = (ActivityManager) getSystemService( ACTIVITY_SERVICE );
         int imp = getTCVMImportance(actvityManager, !sameProgram);
         if (imp == RunningAppProcessInfo.IMPORTANCE_VISIBLE || (!sameProgram && imp == RunningAppProcessInfo.IMPORTANCE_BACKGROUND)) // guich@tc123b: if the vm was already running...
         {
            while (true) // wait the old process die
            {
               imp = getTCVMImportance(actvityManager,false);
               if (imp != 0)
                  try {Thread.sleep(10);} catch (Exception e) {}
               else 
                  break;
            } 
            startActivity(intent); // and call it again
         }
      }         
      catch (android.content.ActivityNotFoundException anfe)
      {
         AndroidUtils.error("This program requires the TotalCross Virtual Machine to run. Please contact your software's vendor or download the DEMO version from\nwww.totalcross.com/install\nwhich will run during 80 hours.",true);
      }
      System.exit(99);
   }
   
   private int getTCVMImportance(ActivityManager actvityManager, boolean kill)
   {
      List<RunningAppProcessInfo> list = actvityManager.getRunningAppProcesses();
      int n = list.size();
      for(int j = 0; j < n; j++)
      {
         RunningAppProcessInfo info = list.get(j);
         if (info.processName.equals("totalcross.android"))
            return info.importance;
      }
      return 0;
   }
}
