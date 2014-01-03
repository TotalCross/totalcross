/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.android;

import totalcross.*;
import totalcross.android.compat.*;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.inputmethod.*;

import com.intermec.aidc.*; 

public class Loader extends Activity implements BarcodeReadListener
{
   public static boolean IS_EMULATOR = android.os.Build.MODEL.toLowerCase().indexOf("sdk") >= 0;
   public Handler achandler;
   private boolean runningVM;
   private static final int CHECK_LITEBASE = 1234324329;
   private static final int TAKE_PHOTO = 1234324330;
   private static final int JUST_QUIT = 1234324331;
   private static final int MAP_RETURN = 1234324332;
   private static boolean onMainLoop;
   public static boolean isFullScreen;
   
   /** Called when the activity is first created. */
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      try
      {
         AndroidUtils.initialize(this);
         AndroidUtils.checkInstall();
         checkLitebase(); // calls runVM() on close
      }
      catch (Throwable e)
      {
         String stack = Log.getStackTraceString(e);
         AndroidUtils.debug(stack);
         AndroidUtils.error("An exception was issued when launching the program. Please inform this stack trace to your software's vendor:\n\n"+stack,true);
      }
   }
   
   private void checkLitebase()
   {
      // launch the vm's intent
      Intent intent = new Intent("android.intent.action.MAIN");
      intent.setClassName("litebase.android","litebase.android.Loader");
      intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
      try
      {
         startActivityForResult(intent, CHECK_LITEBASE);
      }
      catch (ActivityNotFoundException anfe) // occurs when litebase is not installed
      {
         runVM();
      }
      catch (Throwable t)
      {
         AndroidUtils.debug("Exception ignored:");
         AndroidUtils.handleException(t,false);
         AndroidUtils.debug("Litebase not installed or single apk.");
         runVM();
      }
   }
   
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      switch (requestCode)
      {
         case Level5.BT_MAKE_DISCOVERABLE:
            Level5.getInstance().setResponse(resultCode != Activity.RESULT_CANCELED,null);
            break;
         case JUST_QUIT:
            finish();
            break;
         case CHECK_LITEBASE:
            runVM();
            break;
         case TAKE_PHOTO:
            Launcher4A.pictureTaken(resultCode != RESULT_OK ? 1 : 0);
            break;
         case MAP_RETURN:
            Launcher4A.showingMap = false;
            break;
      }
   }
   
   private void callGoogleMap(double lat, double lon, boolean sat)
   {
      try
      {
         Intent intent = new Intent(this, Class.forName(totalcrossPKG+".MapViewer"));
         intent.putExtra("lat",lat);
         intent.putExtra("lon",lon);
         intent.putExtra("sat",sat);
         startActivityForResult(intent, MAP_RETURN);
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e,false);
      }
   }
   
   private void captureCamera(String s, int quality, int width, int height)
   {
      try
      {
         Intent intent = new Intent(this, Class.forName(totalcrossPKG+".CameraViewer"));
         intent.putExtra("file",s);
         intent.putExtra("quality",quality);
         intent.putExtra("width",width);
         intent.putExtra("height",height);
         startActivityForResult(intent, TAKE_PHOTO);
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e,false);
      }
   }
   
   private void dialNumber(String number)
   {
      startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number)));
   }

   public static final int DIAL = 1;
   public static final int CAMERA = 2;
   public static final int TITLE = 3;
   public static final int EXEC = 4;
   public static final int LEVEL5 = 5;
   public static final int MAP = 6;
   public static final int FULLSCREEN = 7;
   public static final int INVERT_ORIENTATION = 8;
   
   public static String tcz;
   private String totalcrossPKG = "totalcross.android";
   
   private void runVM()
   {
      if (runningVM) return;
      runningVM = true;
      Hashtable<String,String> ht = AndroidUtils.readVMParameters();
      String tczname = tcz = ht.get("tczname");
      boolean isSingleAPK = false;
      if (tczname == null)
      {
         // this is a single apk. get the app name from the package
         String sharedId = AndroidUtils.pinfo.sharedUserId;
         if (sharedId.equals("totalcross.app.sharedid")) // is it the default shared id?
            AndroidUtils.error("Launching parameters not found",true);
         else
         {
            tczname = sharedId.substring(sharedId.lastIndexOf('.')+1);
            totalcrossPKG = "totalcross."+tczname;
            ht.put("apppath", AndroidUtils.pinfo.applicationInfo.dataDir);
            isSingleAPK = true;
         }
      }
      String appPath = ht.get("apppath");
      String fc = ht.get("fullscreen");
      isFullScreen = fc != null && fc.equalsIgnoreCase("true");
      setTitle(tczname);
      if (isFullScreen)
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
      // in android 3.2, the size is retrieved as excluding the taskbar, so we have to rotate in both directions and get the total screen size. this value is cached
      if (Build.VERSION.SDK_INT >= 13 && AndroidUtils.getSavedScreenSize() == -1) // android 3.2 has a bug and we have to change the layout to landscape and portrait and measure the screen width in both 
      {
         boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
         setRequestedOrientation(isPortrait ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      }
      
      // start the vm
      achandler = new EventHandler();
      String cmdline = ht.get("cmdline");
      setContentView(new Launcher4A(this, tczname, appPath, cmdline, isSingleAPK));
      onMainLoop = true;
   }
   
   
   class EventHandler extends Handler 
   {
      public void handleMessage(Message msg) 
      {
         Bundle b = msg.getData();
         switch (b.getInt("type"))
         {
            case LEVEL5:
               Level5.getInstance().processMessage(b);
               break;
            case DIAL:
               dialNumber(b.getString("dial.number"));
               break;
            case CAMERA:
               captureCamera(b.getString("showCamera.fileName"),b.getInt("showCamera.quality"),b.getInt("showCamera.width"),b.getInt("showCamera.height"));
               break;
            case TITLE:
               setTitle(b.getString("setDeviceTitle.title"));
               break;
            case EXEC:
               intentExec(b.getString("command"), b.getString("args"), b.getInt("launchCode"), b.getBoolean("wait"));
               break;
            case MAP:
               callGoogleMap(b.getDouble("lat"), b.getDouble("lon"), b.getBoolean("sat"));
               break;
            case INVERT_ORIENTATION:
               if (!b.getBoolean("invert"))
                  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
               else
               {
                  boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
                  setRequestedOrientation(isPortrait ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
               }
               break;
            case FULLSCREEN:
            {
               boolean setAndHide = b.getBoolean("fullScreen");
               InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
               Window w = getWindow();
               if (setAndHide)
               {
                  imm.hideSoftInputFromWindow(Launcher4A.instance.getWindowToken(), 0, Launcher4A.instance.siprecv);
                  w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                  w.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
               }
               else
               {
                  imm.showSoftInput(Launcher4A.instance, 0);
                  w.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                  w.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
               }
               break;
            }
         }
      }
   }
      
   // Vm.exec("url","http://www.google.com/search?hl=en&source=hp&q=abraham+lincoln",0,false): launches a url
   // Vm.exec("totalcross.app.UIGadgets",null,0,false): launches another TotalCross' application
   // Vm.exec("viewer","file:///sdcard/G3Assets/541.jpg", 0, true);
   // Vm.exec("/sdcard/
   private void intentExec(String command, String args, int launchCode, boolean wait)
   {
      try
      {
         if (command.equalsIgnoreCase("cmd"))
         {
            try 
            {               
               java.lang.Process process = Runtime.getRuntime().exec(args);
               if (wait)
                  process.waitFor();
            } 
            catch (IOException e) 
            {
               AndroidUtils.handleException(e,false);
            }
         }
         else
         if (command.equalsIgnoreCase("viewer"))
         {
            if (args.toLowerCase().endsWith(".pdf"))
            {
               File pdfFile = new File(args);
               if(pdfFile.exists()) 
               {
                   Uri path = Uri.fromFile(pdfFile); 
                   Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                   pdfIntent.setDataAndType(path, "application/pdf");
                   pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   try
                   {
                       startActivity(pdfIntent);
                   }
                   catch (ActivityNotFoundException e)
                   {
                       e.printStackTrace(); 
                   }
               }
            }
            else
            {
               Intent intent = new Intent(this, Class.forName(totalcrossPKG+".WebViewer"));
               intent.putExtra("url",args);
               if (!wait)
                  startActivityForResult(intent, JUST_QUIT);
               else
                  startActivity(intent);
               return;
            }
         }
         else
         if (command.equalsIgnoreCase("url"))
         {
            if (args != null)
            {
               Intent i = new Intent(Intent.ACTION_VIEW);
               i.setData(Uri.parse(args));
               startActivity(i);
            }
         }
         else
         if (command.toLowerCase().endsWith(".apk"))
         {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.fromFile(new File(command)), "application/vnd.android.package-archive");
            startActivity(i);
         }
         else
         {
            Intent i = new Intent();
            i.setClassName(command,command+"."+args);
            startActivity(i);
         }
         if (!wait)
            finish();
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e,false);
      }
   }
   
   public void onConfigurationChanged(Configuration config)
   {
      // TODO change the Settings.virtualKeyboard to true when newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES;
      super.onConfigurationChanged(config);
      Launcher4A.hardwareKeyboardIsVisible = config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO || config.keyboard == Configuration.KEYBOARD_QWERTY; // motorola titanium returns HARDKEYBOARDHIDDEN_YES but KEYBOARD_QWERTY. In soft inputs, it returns KEYBOARD_NOKEYS
   }

   protected void onSaveInstanceState(Bundle outState) 
   {
   }
   
   protected void onDestroy()
   {
      if (runningVM) // guich@tc126_60: call app 1, home, call app 2: onDestroy is called
         quitVM();
      super.onDestroy();
      
      if(bcr != null)
      {
          try
          { 
             bcr.removeBarcodeReadListener(this);
          }
          catch (BarcodeReaderException exception)
          {
             
          }
          bcr.close();
          bcr = null;
      }
                     
      wedg = null;
      //disconnect from data collection service
      AidcManager.disconnectService();
   }
   
   protected void onPause()
   {
      if (runningVM)
         Launcher4A.sendCloseSIPEvent();
      Launcher4A.appPaused = true;
      if (onMainLoop)
         Launcher4A.appPaused();
      super.onPause();
      if (isFinishing() && runningVM) // guich@tc126_60: stop the vm if finishing is true, since onDestroy is not guaranteed to be called
         quitVM();                    // call app 1, exit, call app 2: onPause is called but onDestroy not
   }
   
   private void quitVM()
   {
      runningVM = onMainLoop = false;
      Launcher4A.stopVM();
      while (!Launcher4A.canQuit)
         try {Thread.sleep(100);} catch (Exception e) {}
      Launcher4A.closeTCZs();
      //Level5.getInstance().destroy();
      android.os.Process.killProcess(android.os.Process.myPid());
      // with these two lines, the application may have problems when then stub tries to load another vm instance.
      //try {Thread.sleep(1000);} catch (Exception e) {} // let the app take time to exit
      // System.exit(0); // make sure all threads will stop. also ensures that one app is not called as the app launched previously
   }
   
   protected void onResume()
   {
      if (onMainLoop)
         Launcher4A.appResumed();
      Launcher4A.appPaused = false;
      super.onResume();
   }
   
   private com.intermec.aidc.BarcodeReader bcr;
   private com.intermec.aidc.VirtualWedge wedg;
   String strDeviceId, strBarcodeData, strSymbologyId;    

   public void doBarcodReader()
   {
        try
        { 
            //disable virtual wedge
            wedg = new VirtualWedge();
            wedg.setEnable(false);  
                        
            //set barcode reader object for internal scanner
            bcr = new BarcodeReader();

            //add barcode reader listener
            bcr.addBarcodeReadListener(this);

        }
        catch (BarcodeReaderException bcrexp)
        {
            int errCode = bcrexp.getErrorCode();
            String errMessage = bcrexp.getErrorMessage();
        }
        catch (SymbologyException sym)
        {
            String errMessage = sym.getErrorMessage();
        }
        catch (SymbologyOptionsException symOp)
        {
            String errMessage = symOp.getErrorMessage();
        }
        catch (VirtualWedgeException exception)
        {
           String errMessage = exception.getErrorMessage();
        }
   }

   public void barcodeRead(BarcodeReadEvent aBarcodeReadEvent)
   {
        strDeviceId =  aBarcodeReadEvent.getDeviceId();
        strBarcodeData =  aBarcodeReadEvent.getBarcodeData();
        strSymbologyId = aBarcodeReadEvent.getSymbolgyId();

        //update data to edit fields
        runOnUiThread(new Runnable() {

            public void run() {
                 AndroidUtils.debug(strDeviceId);
                 AndroidUtils.debug(strBarcodeData);
                 AndroidUtils.debug(strSymbologyId);
            }
        });
   }
}
