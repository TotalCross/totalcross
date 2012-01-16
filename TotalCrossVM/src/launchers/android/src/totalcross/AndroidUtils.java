/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package totalcross;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.os.*;
import android.util.*;

public class AndroidUtils
{
   private static Activity main;
   public static PackageInfo pinfo;
   private static byte[] buf = new byte[8192];

   public static void initialize(Activity main) throws Exception
   {
      AndroidUtils.main = main;
      pinfo = main.getPackageManager().getPackageInfo(main.getPackageName(), 0); 
   }
   
   public static class StartupTask extends AsyncTask<Object, Integer, Integer> 
   {
      private ProgressDialog dialog;
      
      protected Integer doInBackground(Object... params)
      {
         try
         {
            checkInstall(this);
            if (dialog != null)
               dialog.dismiss();
         }
         catch (Exception e)
         {
            if (dialog != null)
               dialog.dismiss();
            handleException(e,true);
         }
         return null;
      }
    
      private void initDialog()
      {
         publishProgress(0);
      }
      
      protected void onProgressUpdate(Integer... values) 
      {
         if (dialog == null)
         {
            dialog = new ProgressDialog(main);
            dialog.setMessage("Updating...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
         }
      }
   }

   private static void loadTCVM()
   {
      try // to bypass problems of getting access to a file, we create files and folders natively, where we can specify the file attributes.
      {
         System.load("/data/data/totalcross.android/lib/libtcvm.so");
      }
      catch (UnsatisfiedLinkError ule)
      {
         try
         {
            String sharedId = AndroidUtils.pinfo.sharedUserId;
            String tczname = sharedId.substring(sharedId.lastIndexOf('.')+1);
            System.load("/data/data/totalcross." + tczname + "/lib/libtcvm.so"); // for single apk
         }
         catch (UnsatisfiedLinkError ule2)
         {
            error("The TotalCross Virtual Machine was not found!",true);
            while (true)
               try {Thread.sleep(500);} catch (Exception e) {}
         }
      }
   }
   
   public static void checkInstall() throws Exception
   {
      checkInstall(null);
   }
   
   public static void checkInstall(StartupTask task) throws Exception
   {
      loadTCVM();
      String appName = main.getClass().getName();  
      String pack = appName.substring(0,appName.lastIndexOf('.'));
      SharedPreferences pref = main.getPreferences(Context.MODE_PRIVATE);
      AssetFileDescriptor file = main.getAssets().openFd("tcfiles.zip");
      InputStream is = file.createInputStream();
      
      MessageDigest digest = MessageDigest.getInstance("MD5");
      int r;
      
      while ((r = is.read(buf)) > 0)
         digest.update(buf, 0, r);
      
      is.close();
      
      String oldHash = pref.getString("install_md5hash", null);
      String newHash = md5ToString(digest.digest());
      
      if (!newHash.equals(oldHash)) // application was updated
      {
         updateInstall(task, pack);
         pref.edit().putString("install_md5hash", newHash).commit();
      }
   }
   
   public static int getSavedScreenSize()
   {
      SharedPreferences pref = main.getPreferences(Context.MODE_PRIVATE);
      return pref.getInt("saved_screen_size",-1);
   }

   public static void setSavedScreenSize(int newValue)
   {
      SharedPreferences pref = main.getPreferences(Context.MODE_PRIVATE);
      pref.edit().putInt("saved_screen_size", newValue).commit();
   }
   
   public static void updateInstall(StartupTask task, String pack) throws Exception
   {
      debug("Updating application "+pack+"...");
      long ini = System.currentTimeMillis();
      if (task != null)
         task.initDialog();
      String dataDir = pinfo.applicationInfo.dataDir;
      AssetFileDescriptor file = main.getAssets().openFd("tcfiles.zip");
      InputStream is = file.createInputStream();
      ZipInputStream zis = new ZipInputStream(is);
      ZipEntry ze;
      Hashtable<String,String> htPaths = new Hashtable<String,String>(10);
      while ((ze = zis.getNextEntry()) != null)
      {
         String name = ze.getName();
         int slash = name.lastIndexOf('/');
         String path = dataDir;
         if (slash > 0) // paths included?
         {
            path += "/"+name.substring(0,slash);
            if (htPaths.get(path) == null) // not already created?
            {
               nativeCreateFile(path.endsWith("/") ? path : path+"/");
               htPaths.put(path,"");
            }
            name = name.substring(slash+1);
         }
         
         nativeCreateFile(path+"/"+name);
         RandomAccessFile raf = new RandomAccessFile(new File(path, name),"rw");
         for (int n; (n = zis.read(buf)) > 0;)
            raf.write(buf, 0, n);
         raf.close();
      }
      zis.close();
      long fim = System.currentTimeMillis();
      if (fim-ini > 2000) debug("Installation elapsed "+(fim-ini)+" ms");
   }

   native private static void nativeCreateFile(String path);

   public static void handleException(Throwable e, boolean terminateProgram)
   {
      String stack = Log.getStackTraceString(e);
      debug(stack);
      if (terminateProgram)
         error("An exception was issued when launching the program. Please inform this stack trace to your software's vendor:\n\n"+stack,true);
   }

   /** Shows a dialog with the error and, optionally exits the application when dismissed. */
   public static void error(String msg, boolean exit)
   {
      final boolean _exit = exit;
      final String _msg = msg;
      main.runOnUiThread(new Runnable() { public void run() {
         new AlertDialog.Builder(main)
         .setMessage(_msg)
         .setTitle("Error")
         .setCancelable(false)
         .setPositiveButton("Close", new DialogInterface.OnClickListener() 
         {
            public void onClick(DialogInterface dialoginterface, int i) 
            {
               if (_exit)
                  System.exit(2);
            }
         })
         .show();
      }});
   }

   private static char[] hexChars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
   private static String md5ToString(byte[] b)
   {
      int count = b.length;
      StringBuffer sb = new StringBuffer(count*2);
      
      for (int i = 0; i < count; i++)
      {
         sb.append(hexChars[(b[i] >> 4) & 0xF]);
         sb.append(hexChars[b[i] & 0xF]);
      }
      
      return sb.toString();
   }

   public static void debug(String s)
   {
      Log.i("TotalCross", s);
   }

   private static final String VM_PARAMS = "/data/data/totalcross.android/launcher.params"; // guich@tc127_71: use / instead of \
   
   public static Hashtable<String,String> readVMParameters()
   {
      Hashtable<String,String> ht = new Hashtable<String,String>(5);
      File f = new File(VM_PARAMS);
      if (f.exists())
         try
         {
            DataInputStream dis = new DataInputStream(new FileInputStream(f));
            while (true)
            {
               String key = dis.readUTF();
               if (key.equals("@"))
                  break;
               String value = dis.readUTF();
               ht.put(key,value);
            }
            dis.close();
         }
         catch (Exception e)
         {
            handleException(e,false);
         }
      return ht;
   }

   public static void writeVMParameters(Hashtable<String,String> ht)
   {
      try
      {
         DataOutputStream dos = new DataOutputStream(new FileOutputStream(VM_PARAMS));
         for (Enumeration <String>e = ht.keys() ; e.hasMoreElements() ;)
         {
            String key = (String)e.nextElement();
            dos.writeUTF(key);
            dos.writeUTF(ht.get(key));
         }         
         dos.writeUTF("@");
         dos.close();
      }
      catch (Exception e)
      {
         handleException(e,false);
      }
   }
}
