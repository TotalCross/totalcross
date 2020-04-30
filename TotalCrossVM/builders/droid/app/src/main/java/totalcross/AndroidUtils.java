// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



package totalcross;

import java.lang.ref.WeakReference;
import java.io.*;
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
   static class Config
   {
      private int version = 2;
      public int saved_screen_size;
      public int demotime;
      public int zipDateTime;
      
      public Config()
      {
         try
         {
            FileInputStream f = new FileInputStream(getRealPath(pinfo.applicationInfo.dataDir)+"/config.bin");
            DataInputStream ds = new DataInputStream(f);
            int version = ds.readInt();
            if (version == 1)
            {
               ds.readUTF();
               saved_screen_size = ds.readInt();
               demotime = ds.readInt();
            }
            else
            if (version == 2)
            {
               saved_screen_size = ds.readInt();
               demotime = ds.readInt();
               zipDateTime = ds.readInt();
            }
            f.close();
         }
         catch (FileNotFoundException fnfe)
         {
         }
         catch (Exception e)
         {
            handleException(e,false);
         }                  
      }

      public void save()
      {
         try
         {
            FileOutputStream f = new FileOutputStream(getRealPath(pinfo.applicationInfo.dataDir)+"/config.bin");
            DataOutputStream ds = new DataOutputStream(f);
            ds.writeInt(version);
            ds.writeInt(saved_screen_size);
            ds.writeInt(demotime);
            ds.writeInt(zipDateTime);
            f.close();
         }
         catch (Exception e)
         {
            handleException(e,false);
         }                  
      }
   }

   private static Activity main;
   public static PackageInfo pinfo;
   private static byte[] buf = new byte[8192]; // no gain changing to 32768
   public static Config configs;

   public static void initialize(Activity main) throws Exception
   {
      AndroidUtils.main = main;
      pinfo = main.getPackageManager().getPackageInfo(main.getPackageName(), 0); 
      configs = new Config();
   }
   
   public static class StartupTask extends AsyncTask<Object, Integer, Integer> 
   {
      private ProgressDialog dialog;
      
      // Weak references will still allow the Activity to be garbage-collected
      private final WeakReference<Activity> weakActivity;
      
      protected StartupTask (Activity activity) {
          this.weakActivity = new WeakReference<>(activity);
      }
      
      protected Integer doInBackground(Object... params)
      {
         try
         {
             Activity activity = weakActivity.get();
             if (activity == null
                 || activity.isFinishing()
                 || activity.isDestroyed()) {
               // activity is no longer valid, don't do anything!
               return null;
             }
             
            checkInstall(activity.getApplicationContext(), this);
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

   private static String getRealPath(String path)
   {
      try
      {
         File f = new File(path);
         String ret = f.getCanonicalPath();
         return ret;
      }
      catch (Exception e)
      {
         return path;
      }
   }

    private static void loadTCVM(Context context) {
        try {
            System.load(getRealPath(context.getApplicationInfo().nativeLibraryDir) + "/libtcvm.so");
        } catch (Throwable e1) {
            handleException(e1, false);

            try {
                // to bypass problems of getting access to a file, we create files and folders natively, where we can specify the file attributes.
                String sharedId = pinfo.sharedUserId;
                String tczname = sharedId.substring(sharedId.lastIndexOf('.') + 1);
                System.load(getRealPath("/data/data/totalcross." + tczname + "/lib") + "/libtcvm.so"); // for single apk
            } catch (Throwable e2) {
                handleException(e2, false);

                try {
                    System.load(getRealPath("/data/data/totalcross.android/lib") + "/libtcvm.so");
                } catch (UnsatisfiedLinkError e3) {
                    handleException(e3, false);
                    error("The TotalCross Virtual Machine was not found!", true);
                    while (true) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

   public static void checkInstall(Context context) throws Exception
   {
      checkInstall(context, null);
   }
   
   public static void checkInstall(Context context, StartupTask task) throws Exception
   {
      loadTCVM(context);
      String appName = main.getClass().getName();  
      String pack = appName.substring(0,appName.lastIndexOf('.'));
      AssetFileDescriptor file = main.getAssets().openFd("tcfiles.zip");
      
      InputStream is = file.createInputStream();
      is.skip(10); // check the date/time stored inside the zip header
      int zipDateTime = new DataInputStream(is).readInt();
      is.close();
      
      writeApkName();
      if (configs.zipDateTime != zipDateTime)
      {
         configs.zipDateTime = zipDateTime;
         configs.save();
         debug("Updating application "+pack+"...");
         updateInstall(task);
      }
   }
   
   public static void updateInstall(StartupTask task) throws Exception
   {
      long ini = System.currentTimeMillis();
      if (task != null)
         task.initDialog();
      String dataDir = getRealPath(pinfo.applicationInfo.dataDir);
      AssetFileDescriptor file = main.getAssets().openFd("tcfiles.zip");
      InputStream is = file.createInputStream();
      ZipInputStream zis = new ZipInputStream(is);
      ZipEntry ze;
      Hashtable<String,String> htPaths = new Hashtable<String,String>(10);
      while ((ze = zis.getNextEntry()) != null)
      {
         String name = ze.getName();
         if (name.endsWith(".tcz")) // tcz files are never unpacked
         {
            File f = new File(dataDir, name);
            try {if (f.exists()) {f.delete(); debug("deleted old "+dataDir+"/"+name);}} catch (Exception e) {} // delete old tcz files
            continue;
         }
         
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

   private static void writeApkName()
   {
      try
      {
         // create a file that informs the application's apk file path
         String txt = getRealPath(pinfo.applicationInfo.dataDir)+"/apkname.txt";
         nativeCreateFile(txt);
         FileOutputStream fos = new FileOutputStream(txt);
         String dir = main.getPackageResourcePath();
         fos.write(dir.getBytes()); // full path including apk name
         fos.close();
         //debug("writting \""+dir+"\" into "+txt);
      }
      catch (Exception e)
      {
         handleException(e,false);
      }
   }
   native private static void nativeCreateFile(String path);

   public static void handleException(Throwable e, boolean terminateProgram)
   {
      String stack = Log.getStackTraceString(e);
      debug(terminateProgram ? "FATAL EXCEPTION" : "NON-FATAL EXCEPTION");
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

   private static final String VM_PARAMS = getRealPath("/data/data/totalcross.android")+"/launcher.params"; // guich@tc127_71: use / instead of \
   
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
         catch (FileNotFoundException fnfe)
         {
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
   
   public static boolean isImage(String argl)
   {
      return argl.endsWith(".png") || argl.endsWith(".jpg") || argl.endsWith(".jpeg");
   }

   public static byte[] readFully(InputStream input) throws IOException
   {
       byte[] buffer = new byte[2048];
       int bytesRead;
       ByteArrayOutputStream output = new ByteArrayOutputStream(2048);
       while ((bytesRead = input.read(buffer)) != -1)
           output.write(buffer, 0, bytesRead);
       return output.toByteArray();
   }
   
   public static void copyFile(String in, String out) throws IOException {
      FileInputStream fin = new FileInputStream(in);
      copyStreamToFile(fin, out);
      fin.close();
   }
   
   // Não fecha a InputStream enviada...
   public static void copyStreamToFile(InputStream inputStream, String out) throws IOException {
      byte[] buf = new byte[4096];
      FileOutputStream fout = new FileOutputStream(out);
      int r;
      while ((r=inputStream.read(buf,0,buf.length)) > 0)
         fout.write(buf,0,r);
      fout.close();
   }
}
