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

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.view.inputmethod.*;
import com.intermec.aidc.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import totalcross.*;
import totalcross.android.compat.*;

public class Loader extends Activity implements BarcodeReadListener
{
   public static boolean IS_EMULATOR = android.os.Build.MODEL.toLowerCase().indexOf("sdk") >= 0;
   public Handler achandler;
   private boolean runningVM;
   private static final int TAKE_PHOTO = 1234324330;
   private static final int JUST_QUIT = 1234324331;
   private static final int MAP_RETURN = 1234324332;
   private static final int ZXING_RETURN = 1234324333;
   private static final int EXTCAMERA_RETURN = 1234324334;
   private static final int CAMERA_PIC_REQUEST = 1337;
   private static boolean onMainLoop;
   public static boolean isFullScreen;
   
   private static boolean onCreateCalled; //
   /** Called when the activity is first created. */
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      try
      {
         AndroidUtils.initialize(this);
         if (isSingleApk() && onCreateCalled) // bypass bug that will cause a new instance each time the app is minimized and called again (2282)
         {
            System.exit(2);
            return;
         }
         onCreateCalled = true;
         AndroidUtils.checkInstall();
         runVM();
      }
      catch (Throwable e)
      {
         String stack = Log.getStackTraceString(e);
         AndroidUtils.debug(stack);
         AndroidUtils.error("An exception was issued when launching the program. Please inform this stack trace to your software's vendor:\n\n"+stack,true);
      }
   }
   
   public void onRestart()
   {
      super.onRestart();
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
         case TAKE_PHOTO:
            Launcher4A.pictureTaken(resultCode != RESULT_OK ? 1 : 0);
            break;
         case CAMERA_PIC_REQUEST:
            Launcher4A.pictureTaken(resultCode != RESULT_OK ? 1 : 0);
            break;
         case MAP_RETURN:
            Launcher4A.showingMap = false;
            break;
         case ZXING_RETURN:
            Launcher4A.zxingResult = resultCode == RESULT_OK ? data.getStringExtra("SCAN_RESULT") : null;
            Launcher4A.callingZXing = false;
            break;
         case EXTCAMERA_RETURN:
            String[] projection = {MediaStore.Images.Media.DATA, BaseColumns._ID}; 
            Cursor cursor = managedQuery(capturedImageURI, projection, null, null, null); 
            int dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int idIdx   = cursor.getColumnIndexOrThrow(BaseColumns._ID);
            cursor.moveToFirst(); 
            String capturedImageFilePath = cursor.getString(dataIdx);
            if (capturedImageFilePath == null || !AndroidUtils.copyFile(capturedImageFilePath,imageFN,cameraType == CAMERA_NATIVE_NOCOPY))
               resultCode = RESULT_OK+1; // error
            else
            if (cameraType == CAMERA_NATIVE_NOCOPY) // if the file was deleted, delete from database too
               try{ getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, BaseColumns._ID + "=" + cursor.getString(idIdx), null);} catch (Exception e) {AndroidUtils.handleException(e,false);}
            Launcher4A.pictureTaken(resultCode != RESULT_OK ? 1 : 0);
            break;
      }
   }
   
   private static final int SHOW_SATELLITE_PHOTOS = 1;
   public static final int USE_WAZE = 2;

   private void callRoute(double latI, double lonI, double latF, double lonF, String coord, int flags)
   {
      try
      {
         if ((flags & USE_WAZE) != 0)
         {
            try
            {
               String url = "waze://?ll="+latI+","+lonI+"&navigate=yes";
               Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
               Launcher4A.showingMap = false; // note: waze runs as a separate app, so we just return directly from here
               startActivity(intent);
               return;
            }
            catch ( ActivityNotFoundException ex)
            {
               AndroidUtils.debug("Waze not found, using default app");
               callGoogleMap(latI, lonI, (flags & SHOW_SATELLITE_PHOTOS) != 0);
               return;
            }
         }
         
         Intent intent = new Intent(this, Class.forName(totalcrossPKG+".RouteViewer"));
         intent.putExtra("latI",latI);
         intent.putExtra("lonI",lonI);
         intent.putExtra("latF",latF);
         intent.putExtra("lonF",lonF);
         intent.putExtra("coord",coord);
         intent.putExtra("sat",(flags & SHOW_SATELLITE_PHOTOS) != 0);
         startActivityForResult(intent, MAP_RETURN);
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e,false);
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

   private void callGoogleMap(String items, boolean sat)
   {
      try
      {
         Intent intent = new Intent(this, Class.forName(totalcrossPKG+".MapViewer"));
         intent.putExtra("items",items);
         intent.putExtra("sat",sat);
         startActivityForResult(intent, MAP_RETURN);
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e,false);
      }
   }

   private String imageFN;
   //private static final int CAMERA_CUSTOM = 0;
   private static final int CAMERA_NATIVE = 1;
   private static final int CAMERA_NATIVE_NOCOPY = 2;
   private int cameraType;
   private void captureCamera(String s, int quality, int width, int height, boolean allowRotation, int cameraType)
   {
      try
      {
         imageFN = s;
         this.cameraType = cameraType;
         String deviceId = Build.MANUFACTURER.replaceAll("\\P{ASCII}", " ") + " " + Build.MODEL.replaceAll("\\P{ASCII}", " ");
         if (cameraType == CAMERA_NATIVE || cameraType == CAMERA_NATIVE_NOCOPY)
         {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "tctemp.jpg");  
            capturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
            intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageURI);  
            startActivityForResult(intent, EXTCAMERA_RETURN);
         }
         else
         if ("SK GT-7340".equals(deviceId))
         {
            Uri outputFileUri = Uri.fromFile(new File(s));
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(intent, CAMERA_PIC_REQUEST);
         }
         else
         {
            Intent intent = new Intent(this, Class.forName(totalcrossPKG+".CameraViewer"));
            intent.putExtra("file",s);
            intent.putExtra("quality",quality);
            intent.putExtra("width",width);
            intent.putExtra("height",height);
            intent.putExtra("allowRotation", allowRotation);
            startActivityForResult(intent, TAKE_PHOTO);
            Launcher4A.instance.nativeInitSize(null,-998,0);
         }
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
   public static final int ROUTE = 8;
   public static final int ZXING_SCAN = 9;
   public static final int MAPITEMS = 10;
   
   public static String tcz;
   private String totalcrossPKG = "totalcross.android";
   
   public boolean isSingleApk()
   {
      return !AndroidUtils.pinfo.sharedUserId.equals("totalcross.app.sharedid");
   }
   
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
               captureCamera(b.getString("showCamera.fileName"),b.getInt("showCamera.quality"),b.getInt("showCamera.width")
                                                               ,b.getInt("showCamera.height"),b.getBoolean("showCamera.allowRotation"),b.getInt("showCamera.cameraType"));
			   break;
            case TITLE:
               setTitle(b.getString("setDeviceTitle.title"));
               break;
            case EXEC:
               intentExec(b.getString("command"), b.getString("args"), b.getInt("launchCode"), b.getBoolean("wait"));
               break;
            case MAPITEMS:
               callGoogleMap(b.getString("items"), b.getBoolean("sat"));
               break;
            case MAP:
               callGoogleMap(b.getDouble("lat"), b.getDouble("lon"), b.getBoolean("sat"));
               break;
            case ROUTE:
               callRoute(b.getDouble("latI"), b.getDouble("lonI"),b.getDouble("latF"), b.getDouble("lonF"), b.getString("coord"), b.getInt("flags"));
               break;
            case FULLSCREEN:
            {
               boolean setAndHide = b.getBoolean("fullScreen");
               InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
               Window w = getWindow();
               if (setAndHide)
               {
                  try // for galaxy tab2 bar
                  {
                     java.lang.reflect.Method m = View.class.getMethod("setSystemUiVisibility", new Class[]{Integer.class});
                     final int SYSTEM_UI_FLAG_HIDE_NAVIGATION = 2;
                     m.invoke(Launcher4A.instance, new Integer(SYSTEM_UI_FLAG_HIDE_NAVIGATION));
                  }
                  catch (Exception e) {}
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
            case ZXING_SCAN:
            {
               String cmd = b.getString("zxing.mode");
               StringTokenizer st = new StringTokenizer(cmd,"&");
               String mode = "SCAN_MODE";
               String scanmsg = "";
               while (st.hasMoreTokens())
               {
                  String s = st.nextToken();
                  int i = s.indexOf('=');
                  if (i == -1) continue;
                  String s1 = s.substring(0,i);
                  String s2 = s.substring(i+1);
                  if (s1.equalsIgnoreCase("mode"))
                     mode = s2;
                  else
                  if (s1.equalsIgnoreCase("msg"))
                     scanmsg = s2;
               }
               mode = mode.equalsIgnoreCase("2D") ? "QR_CODE_MODE" : 
                      mode.equalsIgnoreCase("1D") ? "ONE_D_MODE" :
                      "SCAN_MODE";
               Intent intent = new Intent("totalcross.zxing.client.android.SCAN");
               intent.putExtra("SCAN_MODE", mode);//for Qr code, its "QR_CODE_MODE" instead of "PRODUCT_MODE"
               intent.putExtra("SAVE_HISTORY", false);//this stops saving ur barcode in barcode scanner app's history
               intent.putExtra("SCAN_MESSAGE", scanmsg);
               startActivityForResult(intent, ZXING_RETURN);
               //if (harder) decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
               //Result result = reader.decode(bitmap, decodeHints);
               break;
            }
               
         }
      }
   }
   
   Uri capturedImageURI;
   // Vm.exec("url","http://www.google.com/search?hl=en&source=hp&q=abraham+lincoln",0,false): launches a url
   // Vm.exec("totalcross.app.UIGadgets",null,0,false): launches another TotalCross' application
   // Vm.exec("viewer","file:///sdcard/G3Assets/541.jpg", 0, true);
   // Vm.exec("/sdcard/
   private void intentExec(String command, String args, int launchCode, boolean wait)
   {
      try
      {
         if (command.equalsIgnoreCase("broadcast"))
         {
            Intent intent = new Intent();
            if (launchCode != 0)
               intent.addFlags(launchCode);
            intent.setAction(args);
            sendBroadcast(intent);
         }
         else
         if (command.equalsIgnoreCase("cmd"))
         {
            java.lang.Process process = Runtime.getRuntime().exec(args);
            if (wait)
               process.waitFor();
         }
         else
         if (command.equalsIgnoreCase("kingsoft"))
         {
            File f = new File(args);
            if (f.exists()) 
            {
               Intent intent = new Intent();
               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               intent.setAction(android.content.Intent.ACTION_VIEW);
               intent.setClassName("cn.wps.moffice_eng", "cn.wps.moffice.documentmanager.PreStartActivity");
               Uri uri = Uri.fromFile(f);
               intent.setData(uri);
               startActivity(intent);
            }
         }
         else
         if (command.equalsIgnoreCase("viewer"))
         {
            String argl = args.toLowerCase();
            if (android.os.Build.VERSION.SDK_INT >= 8 && AndroidUtils.isImage(argl))
            {
               Intent intent = new Intent(this, Class.forName(totalcrossPKG+".TouchImageViewer"));
               intent.putExtra("file",args);
               if (!wait)
                  startActivityForResult(intent, JUST_QUIT);
               else
                  startActivity(intent);
               return;
            }
            if (argl.endsWith(".pdf"))
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
                      AndroidUtils.debug("THERE'S NO PDF READER TO OPEN "+args);
                      e.printStackTrace(); 
                   }
               }
            }
            else
            {
               Intent intent;
               if (argl.indexOf("youtu.be") >= 0 || argl.indexOf("youtube") >= 0)
                  intent = new Intent(Intent.ACTION_VIEW, Uri.parse(args));
               else
               {
                  intent = new Intent(this, Class.forName(totalcrossPKG+".WebViewer"));
                  intent.putExtra("url",args);
               }
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
            boolean isService = args.equalsIgnoreCase("TCService");
            AndroidUtils.debug("*** Vm.exec "+command+" . "+args+": "+isService);
            if (isService)
               startService(i);
            else
               startActivity(i);
         }
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e,false);
      }
      if (!wait)
         finish();
   }
   
   public void onConfigurationChanged(Configuration config)
   {
      super.onConfigurationChanged(config);
      Launcher4A.hardwareKeyboardIsVisible = config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO || config.keyboard == Configuration.KEYBOARD_QWERTY; // motorola titanium returns HARDKEYBOARDHIDDEN_YES but KEYBOARD_QWERTY. In soft inputs, it returns KEYBOARD_NOKEYS
   }

   protected void onSaveInstanceState(Bundle outState) 
   {
      outState.clear();
   }
   
   protected void onDestroy()
   {
      if (runningVM) // guich@tc126_60: call app 1, home, call app 2: onDestroy is called
         quitVM();
      super.onDestroy(); 
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

   public String strBarcodeData;    
   public static Semaphore semaphore = new Semaphore(1);

   public void barcodeRead(BarcodeReadEvent aBarcodeReadEvent)
   {
      try
	   {
         semaphore.acquire();		 
      }
	   catch (InterruptedException exception) {}
	   strBarcodeData = aBarcodeReadEvent.getBarcodeData();
	   semaphore.release();

      Launcher4A.instance._postEvent(Launcher4A.BARCODE_READ, 0, 0, 0, 0, 0);
   }

   public void onNewIntent(Intent i) 
   {
      AndroidUtils.debug("on new intent "+i);
      super.onNewIntent(i);
   }
}
