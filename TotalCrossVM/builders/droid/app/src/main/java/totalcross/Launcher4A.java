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

import totalcross.android.*;
import totalcross.android.Loader;

import android.app.*;
import android.app.ActivityManager.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.graphics.*;
import android.hardware.Camera;
import android.location.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.os.Looper;
import android.provider.*;
import android.telephony.*;
import android.telephony.gsm.*;
import android.text.*;
import android.text.method.KeyListener;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.Manifest;

final public class Launcher4A extends SurfaceView implements SurfaceHolder.Callback, MainClass, OnKeyListener
{
   public static final boolean GENERATE_FONT = false;

   public static boolean canQuit = true;
   public static Launcher4A instance;
   public static Loader loader;
   static SurfaceHolder surfHolder;
   public static TCEventThread eventThread;
   static Rect rDirty = new Rect();
   static boolean showKeyCodes;
   static Hashtable<String,String> htPressedKeys = new Hashtable<String,String>(5);
   static int lastScreenW, lastScreenH, lastType, lastX, lastY=-999, lastOrientation;
   static Camera camera;
   public static boolean appPaused;
   static PhoneListener phoneListener;
   static boolean showingAlert;
   public static int deviceFontHeight; // guich@tc126_69
   static int appHeightOnSipOpen;
   static int appTitleH;
   static boolean lastWasPenDown;
   static ActivityManager activityManager;
   static MemoryInfo mi = new MemoryInfo();
   
   public static String appPath;
   private static android.text.ClipboardManager clip;
   public final String tczname;
   
   public static Handler viewhandler = new Handler(Looper.getMainLooper())
   {
      public void handleMessage(Message msg) 
      {
         try
         {
            Bundle b = msg.getData();
            switch (b.getInt("type"))
            {
               case SET_AUTO_OFF:
                  instance.setKeepScreenOn(b.getBoolean("set"));
                  break;
               case GPSFUNC_START:
                  GPSHelper.instance.startGps();
                  break;
               case GPSFUNC_STOP:
                  GPSHelper.instance.stopGps();
                  break;
               case CELLFUNC_START:
                  telephonyManager = (TelephonyManager) instance.getContext().getSystemService(Context.TELEPHONY_SERVICE);
                  CellLocation.requestLocationUpdate();
                  telephonyManager.listen(phoneListener = new PhoneListener(), PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
                  break;
               case CELLFUNC_STOP:
                  if (phoneListener != null)
                  {
                     telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
                     phoneListener = null;
                  }
                  break;
               case CLIPBOARD:
               {
                  if (clip == null)
                     clip = (android.text.ClipboardManager)loader.getSystemService(Context.CLIPBOARD_SERVICE);
                  String copy = b.getString("copy");
                  if (copy != null)
                     clip.setText(copy);
                  else
                  {
                     if (clip.hasText())
                        paste = clip.getText().toString();
                     pasted = true;
                  }
                  break;
               }
               case FIREBASE_MSG_RCVD:
               {
                   if (Launcher4A.loader != null) {
                  byte [] messageId = b.getByteArray("messageId");
                  byte [] messageType = b.getByteArray("messageType");
                  byte [] collapseKey = b.getByteArray("collapseKey");
                  ArrayList<String> keys = b.getStringArrayList("keys");
                  ArrayList<String> values = b.getStringArrayList("values");
                  String [] keysArr = null, valuesArr = null;
                  if(keys != null) {
                     keysArr = new String[keys.size()];
                     valuesArr = new String[values.size()];
                     keysArr = keys.toArray(keysArr);
                     valuesArr = values.toArray(valuesArr);
                  }
                  int ttl = b.getInt("ttl");
                  
                  Launcher4A.nativeOnMessageReceived(
                     messageId == null ? null : new String(messageId),
                     messageType == null ? null : new String(messageType),
                     keysArr, valuesArr, 
                     collapseKey == null ? null : new String(collapseKey),
                     ttl);
                   }
               }

            }
         }
         catch (Exception e)
         {
            AndroidUtils.handleException(e, false);
         }
      }
   };
   
   public Launcher4A(Loader context, String tczname, String appPath, String cmdline, boolean isSingleAPK)
   {
      super(context);
      this.tczname = tczname;
      // read all apk names, before loading the vm
      activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
      if (!isSingleAPK)
         loadAPK("/data/data/totalcross.android/apkname.txt",true); // vm
      loadAPK(appPath+"/apkname.txt",  true);
      Launcher4A.appPath = appPath;
      System.loadLibrary("tcvm");
      instance = this;
      loader = context;
      surfHolder = getHolder();
      surfHolder.setFormat(PixelFormat.RGBA_8888);
      surfHolder.addCallback(this);
      setWillNotDraw(true); // MUST be set to false to allow onDraw to be executed, we set to true and draw using opengl
      setWillNotCacheDrawing(true);
      setFocusableInTouchMode(true);
      requestFocus();
      setOnKeyListener(this);
      Configuration config = getResources().getConfiguration();
      hardwareKeyboardIsVisible = config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO || config.keyboard == Configuration.KEYBOARD_QWERTY; // motorola titanium returns HARDKEYBOARDHIDDEN_YES but KEYBOARD_QWERTY. In soft inputs, it returns KEYBOARD_NOKEYS
      lastOrientation = getOrientation();
      String vmPath = context.getApplicationInfo().dataDir;
      initializeVM(context, tczname, appPath, vmPath, cmdline);
      if (GENERATE_FONT)
      {
         new totalcross.android.fontgen.FontGenerator("tahoma", new String[]{"","/aa","/rename:TCFont"});
         exit(0);
      }
   }

   public static Context getAppContext()
   {
      return instance.getContext();
   }

   static TelephonyManager telephonyManager;
   static int [] lastCellInfo;
   static int lastSignalStrength;
   private static class PhoneListener extends PhoneStateListener
   {
      public void onCellLocationChanged(CellLocation location) 
      {
         if (!(location instanceof GsmCellLocation)) 
             return;
         GsmCellLocation gsmCell = (GsmCellLocation) location;
         String operator = telephonyManager.getNetworkOperator();
         if (operator == null || operator.length() < 4) 
             return;
         int [] ret = lastCellInfo;
         if (ret == null)
            ret = new int[5];
         try {ret[0] = Integer.parseInt(operator.substring(0, 3));} catch (NumberFormatException nfe) {ret[0] = 0;} // mcc
         try {ret[1] = Integer.parseInt(operator.substring(3));} catch (NumberFormatException nfe) {ret[1] = 0;} // mnc
         ret[2] = gsmCell.getLac();
         ret[3] = gsmCell.getCid();
         ret[4] = lastSignalStrength;
         lastCellInfo = ret;
      }
      public void onSignalStrengthChanged(int s)
      {
         lastSignalStrength = s;
         if (lastCellInfo != null)
            lastCellInfo[4] = s;
      }
   }
   
   private int getOrientation()
   {
      return loader.getResources().getConfiguration().orientation;
   }
   
   private android.view.Surface lastSurface;
   
   public void surfaceChanged(final SurfaceHolder holder, int format, int w, int h) 
   {
      if (h == 0 || w == 0 || !loader.isInteractive()) {
        return;
      }
      Rect rect = holder.getSurfaceFrame();
      int screenHeight = rect.bottom;
      int currentOrientation = getOrientation();
      boolean rotated = currentOrientation != lastOrientation;
      lastOrientation = currentOrientation;
      
      if (h < lastScreenH && Loader.isFullScreen && lastScreenH == screenHeight && appTitleH == 0) // 1: surfaceChanged. 0 -> 480. displayH: 480  =>  2: surfaceChanged. 480 -> 455. displayH: 480
         appTitleH = lastScreenH - h; 
      
      if (sipVisible && !rotated) // sip changed and no rotation occured?
         instance.nativeInitSize(null,-999,h); // signal vm that the keyboard will appear
      else
      {
         instance.nativeInitSize(null,-999,0); // signal vm that the keyboard will hide
         android.view.Surface surface = holder == null ? lastSurface : holder.getSurface();
         if (w != lastScreenW || h != lastScreenH || surface != lastSurface)
         {
            lastSurface = surface;
            lastScreenW = w;
            lastScreenH = h;
            sendScreenChangeEvent();
         }
      }
   }

   private void sendScreenChangeEvent()
   {
      if (!loader.isInteractive()) {
        return;
      }
      
      eventThread.invokeInEventThread(false, new Runnable()
      {
         public void run()
         {
            nativeInitSize(lastSurface,lastScreenW,lastScreenH);
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            final double defaultTextSize = new TextView(getContext()).getTextSize();
            deviceFontHeight = 
                BigDecimal.valueOf(defaultTextSize)
                .divide(BigDecimal.valueOf(metrics.density), 0, RoundingMode.HALF_UP)
                .intValue();
            
            rDirty.left = rDirty.top = 0;
            rDirty.right = lastScreenW;
            rDirty.bottom = lastScreenH;
            
            setSIP(SIP_HIDE,false);
            _postEvent(SCREEN_CHANGED, lastScreenW, lastScreenH, (int)(metrics.xdpi+0.5), (int)(metrics.ydpi+0.5),deviceFontHeight);
            sendCloseSIPEvent(); // makes first screen rotation work
         }
      });
   }
   
   private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
   {
      public boolean onScaleBegin(ScaleGestureDetector detector)
      {
         multiTouching = true;
         if (lastWasPenDown) // if pressed a button and started multitouch, issue a penup event so button can be released
         {
            lastWasPenDown = false;
            eventThread.pushEvent(PEN_UP, 0, 10000, 10000, 0, 0);
         }
         eventThread.pushEvent(MULTITOUCHEVENT_SCALE, 1, 0,0, 0, 0);
         return true;
      }
      
      public void onScaleEnd(/*ScaleGestureDetector detector*/)
      {
         multiTouching = false;
         eventThread.pushEvent(MULTITOUCHEVENT_SCALE, 2, 0,0, 0, 0);
      }

      public boolean onScale(ScaleGestureDetector detector)
      {
         if (eventThread.hasEvent(MULTITOUCHEVENT_SCALE))
            return false;
         double scale = detector.getScaleFactor();
         long l = Double.doubleToLongBits(scale);
         int x = (int)(l >>> 32);
         int y = (int)l;
         eventThread.pushEvent(MULTITOUCHEVENT_SCALE, 0, x, y, 0, 0);
         return true;
      }
   }

   ScaleGestureDetector sgd;
   ScaleListener scaleList;
   boolean multiTouching;
   
   public void surfaceCreated(SurfaceHolder holder)
   {
      // here is where everything starts
      if (eventThread == null)
      {
         eventThread = new TCEventThread(this);
         eventThread.popTime = 20;
         sgd = new ScaleGestureDetector(loader, scaleList = new ScaleListener());
      }
      else
      {
         lastSurface = holder.getSurface();
         sendScreenChangeEvent();
      }
   }

   public void surfaceDestroyed(SurfaceHolder holder)
   {
      instance.nativeInitSize(null,0,0); // signal vm that the surface will change
   }

   private static final int PEN_DOWN  = 1;
   private static final int PEN_UP    = 2;
   private static final int PEN_DRAG  = 3;
   private static final int KEY_PRESS = 4;
   private static final int STOPVM_EVENT = 5;
   private static final int APP_PAUSED = 6;
   private static final int APP_RESUMED = 7;
   private static final int SCREEN_CHANGED = 8;
   private static final int SIP_CLOSED = 9;
   private static final int MULTITOUCHEVENT_SCALE = 10;
   public static final int BARCODE_READ = 11;
   public static final int TOKEN_RECEIVED = 12;//360;
   public static final int MESSAGE_RECEIVED = 13;//361;

   ///////////////////////// ANDROID 4.4 BACK KEY BUG ///////////////////////////////
   // https://code.google.com/p/android/issues/detail?id=62306
   // http://stackoverflow.com/questions/18581636/android-cannot-capture-backspace-delete-press-in-soft-keyboard/19980975#19980975

   class MyInputConnection extends BaseInputConnection 
   {
      KeyEvent delUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL);
      KeyEvent delDn = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
      
      public MyInputConnection(View targetView, boolean fullEditor) 
      {
         super(targetView, fullEditor);
      }
      
      public boolean deleteSurroundingText(int beforeLength, int afterLength) 
      {
         for (int i =0; i < beforeLength; i++)
         {
            sendKeyEvent(delDn);
            sendKeyEvent(delUp);
         }
         return true;
      }

      public boolean performPrivateCommand(java.lang.String action, android.os.Bundle data)
      {
         if (action != null && action.contains("hideSoftInputView")) // for LG's special "hide keyboard" key
         {
            sendCloseSIPEvent();
            sipVisible = false;
         }
         return super.performPrivateCommand(action, data);
      }

      public boolean reportFullscreenMode(boolean enabled)
      {
         return false;
      }

      public CharSequence getTextBeforeCursor(int n, int flags)
      {
         return "aaaaaa"; // used in TC8000, when user try to do backspace on an Edit that has text but nothing was typed yet. Android thinks that there is no characters before the cursor and does not issue the backspace key.
      }
   }
   
   private boolean isSamsungKeyboard() {
      final String currentKeyboard =  Settings.Secure.getString(loader.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
      return "com.sec.android.inputmethod/.SamsungKeypad".equals(currentKeyboard);
   }

   public InputConnection onCreateInputConnection(EditorInfo outAttrs)
   {
      //outAttrs.inputType = android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS; - this makes android's fullscreen keyboard appear in landscape
      outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE | 0x2000000/*EditorInfo.IME_FLAG_NO_FULLSCREEN*/; // the NO_FULLSCREEN flag fixes the problem of keyboard not being shifted correctly in android >= 3.0
      outAttrs.inputType = !wasNumeric ? InputType.TYPE_NULL : InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL;
      if (isSamsungKeyboard())
         outAttrs.inputType |= InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
      outAttrs.actionLabel = null;
      return new MyInputConnection(this, false);
   }
   //////////////////////////////////////////////////////////////////////////////////
   
   private static boolean altNext;
   private static boolean shiftNext;

   public boolean onKeyPreIme(int keyCode, KeyEvent event)
   {
      if (Scanner4A.scanner != null && Scanner4A.scanner.checkScanner(event))
         return false;

      if (keyCode == KeyEvent.KEYCODE_BACK)
         return onKey(this, keyCode, event);
      return false;
   }
   
   public static boolean hardwareKeyboardIsVisible;
   
   protected void onSizeChanged(int w, int h, int oldw, int oldh)
   {
      super.onSizeChanged(w, h, oldw, oldh);
   }

   public boolean onKey(View v, int keyCode, KeyEvent event)
   {
      if (Scanner4A.scanner != null && Scanner4A.scanner.checkScanner(event))
         return false;

      if (keyCode == KeyEvent.KEYCODE_BACK) // guich@tc130: if the user pressed the back key on the SIP, don't pass it to the application
      {
         if (!hardwareKeyboardIsVisible && sipVisible)
         {
            if (event.getAction() == KeyEvent.ACTION_UP)
               setSIP(SIP_HIDE,false);
            return false;
         }
      }
      switch (event.getAction())
      {
         case KeyEvent.ACTION_UP:
            htPressedKeys.remove(String.valueOf(keyCode));
            break;
         case KeyEvent.ACTION_DOWN:
            htPressedKeys.put(String.valueOf(keyCode), "");
            int flags = event.getFlags();
            int state = event.getMetaState();
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) 
            {
               eventThread.pushEvent(KEY_PRESS, 0, keyCode == KeyEvent.KEYCODE_VOLUME_UP ? -1000 : -1001,0,event.getMetaState(),0);
               return true;
            }
            if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT)
            {
               altNext = true;
               shiftNext = false;
            }
            else if (keyCode ==  KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
            {
               altNext = false;
               shiftNext = true;
            }
            else
            {
               if (altNext)
               {
                  state |= KeyEvent.META_ALT_ON;
                  altNext = false;
               }
               if (shiftNext)
               {
                  state |= KeyEvent.META_SHIFT_ON;
                  shiftNext = false;
               }
            }

            int c;
            if ((state & KeyEvent.META_ALT_ON) != 0 && (c = event.getNumber()) != 0) // handle ALT before pushing event
            {
               keyCode = c;
               state &= ~KeyEvent.META_ALT_ON;
            }
            else
               c = event.getUnicodeChar(state);

            if (showKeyCodes)
               alert("Key code: " + keyCode + ", Modifier: " + state+", flags: "+flags);
            if ((flags & KeyEvent.FLAG_FROM_SYSTEM) != 0)
               state |= 8;
            eventThread.pushEvent(KEY_PRESS, c, keyCode, 0, state, 0);
            break;
         case KeyEvent.ACTION_MULTIPLE: // unicode chars
            String str = event.getCharacters();
         if (str != null)
         {
               char[] chars = str.toCharArray();
               for (int i = 0; i < chars.length; i++)
               eventThread.pushEvent(KEY_PRESS, chars[i],0,0,event.getMetaState(),0);
            }
            break;
      }
      return true;
   }
   
   public boolean onTouchEvent(MotionEvent event)
   {
      if (sgd != null)
         sgd.onTouchEvent(event);
      int type;
      int x = (int)event.getX();
      int y = (int)event.getY();
      
      switch (event.getAction())
      {
         case MotionEvent.ACTION_DOWN: type = PEN_DOWN; lastWasPenDown = true;  break;
         case MotionEvent.ACTION_UP:   type = PEN_UP;   lastWasPenDown = false; break;
         case MotionEvent.ACTION_MOVE: type = PEN_DRAG; break;
         default: return false;
      }
      if (multiTouching) // we do this to ignore all touch events until a definitive pen_up
      {
         if (type == PEN_UP)
            scaleList.onScaleEnd();
      }
      else
      if (type != lastType || x != lastX || y != lastY) // skip identical events
      {
         lastType = type;
         lastX = x;
         lastY = y;
         if (type != PEN_DRAG || !eventThread.hasEvent(PEN_DRAG))         
            eventThread.pushEvent(type, 0, x, y, 0, 0);
      }
      return true;
   }

   // 1. when the program calls MainWindow.exit, exit below is called before stopVM
   // 2. when the vm is stopped because another program will run, stopVM is called before exit.
   // so, we just have to wait (canQuit=false) in situation 2.
   private final static int SOFT_EXIT = 0x40000000;
   private final static int SOFT_UNEXIT = 0x40000001;
   static void exit(int ret)
   {
      if (ret == SOFT_UNEXIT)
      {
         AndroidUtils.debug("soft unexit");
         Intent myStarterIntent = new Intent(loader, Loader.class);
         myStarterIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         loader.startActivity(myStarterIntent);         
         return;
      }
      else
      if (ret == SOFT_EXIT)
      {
         Intent i = new Intent(Intent.ACTION_MAIN);
         i.addCategory(Intent.CATEGORY_HOME);
         loader.startActivity(i);
      }
      else
      {
         if (eventThread != null) eventThread.running = false;
         loader.finish();
         //if (!loader.isSingleApk())
            System.exit(3);
      }
      canQuit = true;
   }

   public static void stopVM()
   {
      restoreSound();
      GPSHelper.instance.sendStopGps();
      if (phoneListener != null)
      {
         Message msg = viewhandler.obtainMessage();
         Bundle b = new Bundle();
         b.putInt("type", CELLFUNC_STOP);
         msg.setData(b);
         viewhandler.sendMessage(msg);
      }
      if (eventThread.running) // only in situation 2 
         canQuit = false;
      instance.nativeOnEvent(Launcher4A.STOPVM_EVENT, 0,0,0,0,0);
   }
   
   public static boolean eventIsAvailable()
   {
      if (appPaused)
         try {Thread.sleep(250);} catch (Exception e) {}
      return eventThread.eventAvailable();
   }
   
   public static void pumpEvents()
   {
      if (appPaused)
         try {Thread.sleep(250);} catch (Exception e) {}
      eventThread.pumpEvents();
   }
   
   public static void alert(String msg) // if called from android package classes, must pass FALSE
   {
      showingAlert = true;
      AndroidUtils.debug(msg);
      final String _msg = msg;
      loader.runOnUiThread(new Runnable() { public void run() { // guich@tc125_15: use a native dialog box
         new AlertDialog.Builder(loader)
         .setMessage(_msg)
         .setTitle("Alert")
         .setCancelable(false)
         .setPositiveButton("Close", new DialogInterface.OnClickListener() 
         {
            public void onClick(DialogInterface dialoginterface, int i) 
            {
//               updateScreen(0,0,instance.getWidth(),instance.getHeight(), TRANSITION_NONE);
               showingAlert = false;
            }
         })
         .show();
      }});
   }


   static void setOrientation(int orientation)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putInt("type",Loader.ORIENTATION);
      b.putInt("orientation", orientation);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
   }
   
   static void setDeviceTitle(String title)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putString("setDeviceTitle.title", title);
      b.putInt("type",Loader.TITLE);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
   }
   
   static void showCamera(String fileName, int stillQuality, int width, int height, boolean allowRotation, int cameraType)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putString("showCamera.fileName", fileName);
      b.putInt("showCamera.quality", stillQuality);
      b.putInt("showCamera.width",width);
      b.putInt("showCamera.height",height);
      b.putBoolean("showCamera.allowRotation", allowRotation);
      b.putInt("showCamera.cameraType", cameraType);
      b.putInt("type",Loader.CAMERA);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
   }
   
   public native static void pictureTaken(int res);
   native void initializeVM(Context context, String tczname, String appPath, String vmPath, String cmdline);
   public native void nativeInitSize(Surface surface, int w, int h);
   native void nativeOnEvent(int type, int key, int x, int y, int modifiers, int timeStamp);
   public native static void nativeSmsReceived(String displayOriginatingAddress, String displayMessageBody, byte[] userData);
   public native static void nativeOnMessageReceived(String messageId, String messageType,
      String [] keys, String [] values, String collapsedKey, int ttl);
   public native static void nativeYoutubeCallback(int message);
   public native static void nativeOnTokenRefresh();
   // implementation of interface MainClass. Only the _postEvent method is ever called.
   public void _onTimerTick(boolean canUpdate) {}
   public void appEnding() {}
   public void appStarting(int time) {}

   public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp)
   {
      nativeOnEvent(type, key, x, y, modifiers, timeStamp);
   }
   
   public static final int SIP_HIDE = 10000;
   public static final int SIP_TOP = 10001;
   public static final int SIP_BOTTOM = 10002;
   public static final int SIP_SHOW = 10003;
   
   static boolean sipVisible,wasNumeric;
   
   class SipClosedReceiver extends ResultReceiver
   {
      public SipClosedReceiver()
      {
         super(null);
      }
      public void onReceiveResult(int resultCode, Bundle resultData)
      {
         sendCloseSIPEvent();
         loader.achandler.postDelayed(sipthread,300);
      }
   }
   class SipClosedThread implements Runnable
   {
      public void run()
      {
         sendCloseSIPEvent();
      }
   }
   public SipClosedReceiver siprecv = new SipClosedReceiver();
   private SipClosedThread sipthread = new SipClosedThread();

   private static void showAds(boolean show)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putInt("type", Loader.ADS_FUNC);
      b.putBoolean("show", show);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
   }
                               
   public static boolean getSIP()
   {
      return sipVisible;
   }                    
   
   public static void setSIP(int sipOption, boolean numeric)
   {
      InputMethodManager imm = (InputMethodManager) instance.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      switch (sipOption)
      {
         case SIP_HIDE:
            sipVisible = false;
            if (Loader.adView != null)
               showAds(true);
            if (Loader.isFullScreen)
               setLoaderFullScreen(true);
            else
               imm.hideSoftInputFromWindow(instance.getWindowToken(), 0, instance.siprecv);
               break;
         case SIP_SHOW:
         case SIP_TOP:
         case SIP_BOTTOM:
            wasNumeric = numeric;
            sipVisible = true;
            imm.restartInput(instance);
            if (Loader.adView != null)
               showAds(false);
            if (Loader.isFullScreen)
               setLoaderFullScreen(false);
            else
               imm.showSoftInput(instance, 0); 
            break;
      }
   }

   private static void setLoaderFullScreen(boolean full)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putBoolean("fullScreen", full);
      b.putInt("type",Loader.FULLSCREEN);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
   }
   
   public static void sendCloseSIPEvent()
   {
      if (eventThread != null)
         eventThread.pushEvent(SIP_CLOSED,0,0,0,0,0);
   }

   public static int getAppHeight()
   {
      return instance.getHeight();
   }
   
   public static int setElapsed(int n)
   {
      if (n == 0)
         return AndroidUtils.configs.demotime;
      AndroidUtils.configs.demotime = n;
      AndroidUtils.configs.save();
      return n;
   }
   
   public static void dial(String number)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putString("dial.number", number);
      b.putInt("type",Loader.DIAL);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
   }
   
   private static final int REBOOT_DEVICE  = 1;
   private static final int SET_AUTO_OFF   = 2;
   private static final int SHOW_KEY_CODES = 3;
   private static final int GET_REMAINING_BATTERY = 4;
   private static final int IS_KEY_DOWN    = 5;
   private static final int TURN_SCREEN_ON = 6;
   public static final int GPSFUNC_STOP = 8;
   public static final int GPSFUNC_START = 7;
   private static final int GPSFUNC_GETDATA = 9;
   private static final int CELLFUNC_START = 10;
   private static final int CELLFUNC_STOP = 11;
   private static final int VIBRATE = 12;
   private static final int CLIPBOARD = 13;
   public static final int FIREBASE_MSG_RCVD = 14;
   
   private static int oldBrightness;
   private static Vibrator vibrator;
   
   public static int vmFuncI(int func, int v) throws Exception
   {
      switch (func)
      {
         case VIBRATE:
         {
            if (vibrator == null)
               vibrator = (Vibrator)instance.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(v);
            break;
         }
         case REBOOT_DEVICE:
         {
            // seems that will not work, because the app has to be signed with the platform_certificate
            //Intent i = new Intent(Intent.ACTION_REBOOT); 
            //i.putExtra("nowait", 1); 
            //i.putExtra("interval", 1); 
            //i.putExtra("window", 0); 
            //loader.sendBroadcast(i);
            break;
         }
         case SET_AUTO_OFF:
         {
            // must be in the same thread that created the view
            Message msg = viewhandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("type", SET_AUTO_OFF);
            b.putBoolean("set", v == 0);
            msg.setData(b);
            viewhandler.sendMessage(msg);
            break;
         }
         case SHOW_KEY_CODES:
            showKeyCodes = v == 1;
            break;
         case GET_REMAINING_BATTERY:
         {
            Intent bat = loader.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = bat.getIntExtra("level", 0); 
            int scale = bat.getIntExtra("scale", 100);
            return level * 100 / scale;            
         }
         case IS_KEY_DOWN:
         {
            String s = htPressedKeys.get(String.valueOf(v));
            return s == null ? 0 : 1;
         }
         case TURN_SCREEN_ON:
         {
            if (v == 1 && oldBrightness != 0) // turn on again?
            {
               if (Settings.System.putInt(loader.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, oldBrightness))
                  return 1;
            }
            else
            if (v == 0)
            {
               oldBrightness = Settings.System.getInt(loader.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
               if (Settings.System.putInt(loader.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0))
                  return 1;
            }
            break;
         }
      }
      return 0;
   }

   private static ToneGenerator tonegen;
   private static int tonesPlaying;
   private static int[] tones = 
   {
/*  0 */      ToneGenerator.TONE_DTMF_1,
/*  1 */      ToneGenerator.TONE_DTMF_S,
/*  2 */      ToneGenerator.TONE_DTMF_7,
/*  3 */      ToneGenerator.TONE_DTMF_4,
/*  4 */      ToneGenerator.TONE_DTMF_3,
/*  5 */      ToneGenerator.TONE_DTMF_2,
/*  6 */      ToneGenerator.TONE_DTMF_8,
/*  7 */      ToneGenerator.TONE_DTMF_P,
/*  8 */      ToneGenerator.TONE_DTMF_5,
/*  9 */      ToneGenerator.TONE_DTMF_9,
/* 10 */     ToneGenerator.TONE_DTMF_6,
/* 11 */      ToneGenerator.TONE_DTMF_0,
/* 12 */      ToneGenerator.TONE_DTMF_A,
/* 13 */      ToneGenerator.TONE_DTMF_B,
/* 14 */      ToneGenerator.TONE_DTMF_C,
/* 15 */      ToneGenerator.TONE_DTMF_D,
   };
   public static void tone(int freq, int ms)
   {
      if (tonegen == null)
         soundEnable(true); // at the first time, enable the sound or the guy may not hear it

      final int f = freq;
      final int m = ms;
      new Thread()
      {
         public void run()
         {
            if (tonegen == null)
               tonegen = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
            if (f == 99999)
               tonegen.startTone(ToneGenerator.TONE_PROP_BEEP);
            else
            {
               int ff = Math.min(Math.max((f-500)/100,0),tones.length);
               tonegen.startTone(ff);
               tonesPlaying++;
               try {sleep(m);} catch (Exception e) {}
               if (--tonesPlaying == 0) 
                  tonegen.stopTone();
            }
         }
      }.start();
   }
   
   static int oldRingerMode = -1;
   public static void soundEnable(boolean enable)
   {
      AudioManager am = (AudioManager)instance.getContext().getSystemService(Context.AUDIO_SERVICE);
      if (oldRingerMode == -1) // save it so it can be recovered when vm quits
         oldRingerMode = am.getRingerMode();
      am.setRingerMode(enable ? AudioManager.RINGER_MODE_NORMAL : AudioManager.RINGER_MODE_SILENT); 
   }
   
   private static void restoreSound()
   {
      if (oldRingerMode != -1)
      {
         AudioManager am = (AudioManager)loader.getSystemService(Context.AUDIO_SERVICE);
         am.setRingerMode(oldRingerMode);
         oldRingerMode = -1;
      }
   }
   
   public static int vmExec(String command, String args, int launchCode, boolean wait)
   {
      if (command != null && command.contains("/sdcard"))
         command = replaceSDcard(command);
      if (args != null && args.contains("/sdcard"))
         args = replaceSDcard(args);

      if (command.equals("bugreport"))
         return createBugreport() ? 1 : 0;
      if (command.equals("viewer"))
      {
         if (args == null)
            return -1;
         String argl = args.toLowerCase();
         if ((AndroidUtils.isImage(argl) || argl.endsWith(".pdf")) && !new java.io.File(args).exists())
            return -2;
      }
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putString("command", command);
      b.putString("args", args);
      b.putInt("launchCode", launchCode);
      b.putBoolean("wait", wait);
      b.putInt("type",Loader.EXEC);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
      return 0;
   }
   
   private static String replaceSDcard(String args)
   {
      int idx = args.indexOf("/sdcard");
      if (idx != -1 && idx < args.length()-1)
      {
         char m1 = args.charAt(idx+7);
         if ('0' <= m1 && m1 <= '9')
         {
            String sd = Launcher4A.getSDCardPath(m1 - '0');
            args = args.substring(0,idx) + sd + args.substring(idx+8);
            AndroidUtils.debug("changing path to "+args);
         }
      }
      return args;
   }

   static boolean shownSD[] = new boolean[10];
   public static String getSDCardPath(int i)
   {
      try
      {
         Method getExternalFilesDir = Context.class.getMethod("getExternalFilesDirs",  new Class[] { String.class } );
         File[] ff = (File[])getExternalFilesDir.invoke(loader, new Object[]{null});
         if (ff != null)
         {
            if (i < 0 || i >= ff.length)
               return null;
            File f = ff[i];
            if (!f.canRead())
            {
               try {if (!shownSD[i]) AndroidUtils.debug("/sdcard"+i+": "+f+" (cant read)"); shownSD[i] = true;} catch (Exception e) {}
               return null;
            }
            String ret = f.toString();
            if (ret.contains("/Android/data"))
               ret = ret.substring(0,ret.indexOf("/Android/data"));
            try {if (!shownSD[i]) AndroidUtils.debug("/sdcard"+i+": "+ret+" (valid)"); shownSD[i] = true;} catch (Exception e) {}
            return ret;
         }
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }      
      File f = Environment.getExternalStorageDirectory();
      return f != null && f.canRead() ? f.toString() : null;
   }

   // gps stuff
   public static int gpsPrecision;
   
   public static String gpsFunc(int what, int opc)
   {
      switch (what)
      {
         case GPSFUNC_GETDATA:
            return GPSHelper.instance.gpsGetData();
         case GPSFUNC_START:
            gpsPrecision = opc;
            return GPSHelper.instance.gpsTurn(true);
         case GPSFUNC_STOP:
            return GPSHelper.instance.gpsTurn(false);
      }
      return null;
   }

   private static double[] getLatLon(String address) throws IOException
   {
      if (address.equals("")) // last known location?
      {
         // first, use the location manager
         LocationManager loc = (LocationManager) loader.getSystemService(Context.LOCATION_SERVICE);
         List<String> pros = loc.getProviders(true);
         Location position = null;
         for (String p : pros)
         {
            try {
            position = loc.getLastKnownLocation(p);
            } catch (SecurityException e) {
            	e.printStackTrace();
            }
            if (position != null)
               return new double[]{position.getLatitude(),position.getLongitude()};
         }
      }
      else
      if (address.startsWith("@"))
      {
         String[] st = address.substring(1).split(",");
         return new double[]{Double.parseDouble(st[0]),Double.parseDouble(st[1])};
      }
      else
      {
         return getGeocode(address);
      }
      return null;
   }
   
   private static double[] getGeocode(String address)
   {
      try
      {
         // connect to map web service
         StringBuilder urlString = new StringBuilder(128).
         append("http://maps.googleapis.com/maps/api/geocode/xml?address=").append(address.replace("  "," ").replace(' ','+')).append("&sensor=false");
         //AndroidUtils.debug(urlString.toString());
         HttpURLConnection urlConnection = (HttpURLConnection) new URL(urlString.toString()).openConnection();
         urlConnection.connect();
         byte[] bytes = AndroidUtils.readFully(urlConnection.getInputStream());
         String s = new String(bytes).toLowerCase();
         //AndroidUtils.debug(s);
         if (s.contains("<location_type>approximate</location_type>"))
            return null;
         int idx1 = s.indexOf("<lat>"), idx2 = s.indexOf("</lat>",idx1);
         int idx3 = s.indexOf("<lng>"), idx4 = s.indexOf("</lng>",idx1);
         if (idx1 == -1 || idx2 == -1 || idx3 == -1 || idx4 == -1)
            return null;
         String lat = s.substring(idx1+5,idx2);
         String lon = s.substring(idx3+5,idx4);
         return new double[]{Double.parseDouble(lat),Double.parseDouble(lon)};
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   private static boolean isCallable(Intent intent) 
   {
      return loader.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
   }
   
   public static int showRoute(String addressI, String addressF, String coords, int flags) throws IOException
   {
      boolean tryAgain = false;
      int tryAgainCount = 0;
      do
      {
         try
         {
            if ((flags & Loader.USE_WAZE) != 0 && !isCallable(new Intent( Intent.ACTION_VIEW, Uri.parse("waze://?ll=0,0&navigate=yes") )))
               return -2;

            AndroidUtils.debug("addrs: "+addressI+", "+addressF);
            double[] llI = getLatLon(addressI);
            double[] llF = addressF == null ? new double[]{0,0} : getLatLon(addressF);
            if (llI != null && llF != null)
            {
               AndroidUtils.debug(llI[0]+","+llI[1]+" - "+llF[0]+","+llF[1]);
               // call the loader
               showingMap = true;
               Message msg = loader.achandler.obtainMessage();
               Bundle b = new Bundle();
               b.putInt("type", Loader.ROUTE);
               b.putDouble("latI", llI[0]);
               b.putDouble("lonI", llI[1]);
               b.putDouble("latF", llF[0]);
               b.putDouble("lonF", llF[1]);
               b.putString("coord", coords);
               b.putInt("flags", flags);
               msg.setData(b);
               loader.achandler.sendMessage(msg);
               while (showingMap)
                  try {Thread.sleep(200);} catch (Exception e) {}
               return 0;
            }
            else
               throw new Exception(tryAgainCount+" parse response for address"); // make it try again
         }
         catch (Exception e)
         {
            AndroidUtils.handleException(e, false);
            String msg = e.getMessage();
            tryAgain = msg != null && msg.indexOf("parse response") >= 0 && ++tryAgainCount <= 5; // Unable to parse response from server
            if (tryAgain)
            {
               try {Thread.sleep(250);} catch (Exception ee) {}
               AndroidUtils.debug("Internet out of range. Trying again to get location ("+tryAgainCount+" of 5)");
            }
         }
      } while (tryAgain);
      return -1;
   }

   public static boolean showingMap;
   public static boolean showGoogleMaps(String address, boolean showSatellite)
   {
      boolean tryAgain = true;
      int tryAgainCount = 0;
      do
      {
         try
         {
            if (address.startsWith("***")) // MapItems?
            {
               // call the loader
               showingMap = true;
               Message msg = loader.achandler.obtainMessage();
               Bundle b = new Bundle();
               b.putInt("type", Loader.MAPITEMS);
               b.putString("items", address.substring(3));
               b.putBoolean("sat", showSatellite);
               msg.setData(b);
               loader.achandler.sendMessage(msg);
               while (showingMap)
                  try {Thread.sleep(200);} catch (Exception e) {}
               return true;
            }
               
            double [] ll = getLatLon(address);
            if (ll != null)
            {
               // call the loader
               showingMap = true;
               Message msg = loader.achandler.obtainMessage();
               Bundle b = new Bundle();
               b.putInt("type", Loader.MAP);
               b.putDouble("lat", ll[0]);
               b.putDouble("lon", ll[1]);
               b.putBoolean("sat", showSatellite);
               msg.setData(b);
               loader.achandler.sendMessage(msg);
               while (showingMap)
                  try {Thread.sleep(200);} catch (Exception e) {}
               return true;
            }
            else throw new Exception(tryAgainCount+" parse response for address "+address); // make it try again
         }
         catch (Exception e)
         {
            AndroidUtils.handleException(e, false);
            String msg = e.getMessage();
            tryAgain = msg != null && msg.indexOf("parse response") >= 0 && ++tryAgainCount <= 5; // Unable to parse response from server
            if (tryAgain)
            {
               try {Thread.sleep(250);} catch (Exception ee) {}
               AndroidUtils.debug("Internet out of range. Trying again to get location ("+tryAgainCount+" of 5)");
            }
         }
      } while (tryAgain);
      return false;
   }
   
   public static int[] cellinfoUpdate()
   {
      if (phoneListener == null)
      {
         Message msg = viewhandler.obtainMessage();
         Bundle b = new Bundle();
         b.putInt("type", CELLFUNC_START);
         msg.setData(b);
         viewhandler.sendMessage(msg);
      }
      return lastCellInfo;
   }
   
   public static int fileGetFreeSpace(String fileName)
   {
      StatFs sfs = new StatFs(fileName);
      long size = sfs.getFreeBlocks() * (long)sfs.getBlockSize();
      if (size > 2147483647) // limit to 2 GB, since we're returning an integer
         size = 2147483647;
      return (int)size;
   }

   private static String paste;
   private static boolean pasted;
   
   public static String clipboard(String copy)
   {
      pasted = false;
      paste = null;
      
      Message msg = viewhandler.obtainMessage();
      Bundle b = new Bundle();
      b.putInt("type", CLIPBOARD);
      b.putString("copy",copy);
      msg.setData(b);
      viewhandler.sendMessage(msg);
      
      if (copy == null) // paste?
      {
         while (!pasted)
            try {Thread.sleep(50);} catch (Exception e) {}
         return paste;
      }
      return null;
   }
   
   public static void appPaused()
   {
      appPaused = true;
      instance.nativeInitSize(null,-998,0); // signal vm to delete the textures while the context is valid
      if (eventThread != null)
      {
         setSIP(SIP_HIDE,false);
         eventThread.pushEvent(APP_PAUSED, 0, 0, 0, 0, 0);
      }
   }   
   
   public static void appResumed()
   {
      appPaused = false;
      instance.nativeInitSize(null,-997,0); // signal vm to invalidate the textures
      if (eventThread != null)
         eventThread.pushEvent(APP_RESUMED, 0, 0, 0, 0, 0);
   }
   
   public static String getNativeResolutions()
   {
      try
      {
         StringBuffer sb = new StringBuffer(32);
         Camera camera = Camera.open();
         Camera.Parameters parameters=camera.getParameters();
         List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
         if (sizes == null)
            return null;
         for (Camera.Size ss: sizes)
            sb.append(ss.width).append("x").append(ss.height).append(',');
         int l = sb.length();
         if (l > 0)
            sb.setLength(l-1); // remove last ,
         camera.release();
         return sb.toString();
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e,false);
         return null;
      }
   }
   
   //////////// apk handling
   static ArrayList<TCZinAPK> tczs = new ArrayList<TCZinAPK>(10);
   
   // an InputStream that keeps track of position, since RandomAccessFile can't be used with ZipInputStream
   static class SeekInputStream extends InputStream 
   {
      FileInputStream is;
      int pos;
      byte[] b = new byte[1];
      int maxLength;
      
      public SeekInputStream(String name) throws FileNotFoundException
      {
         is = new FileInputStream(name);
      }

      public int read(byte[] bytes) throws IOException
      {
         return read(bytes,0,bytes.length);
      }
      
      public int read(byte[] b, int offset, int length) throws IOException
      {
         if (length > maxLength) maxLength = length;
         int n = is.read(b,offset,length);
         if (n > 0)
            pos += n;
         return n;
      }
      
      public int read() throws IOException
      {
         int n = read(b,0,1);
         if (n == 1) {pos += n; return b[0] & 0xFF;}
         return -1;
      }
      
      public void close() throws IOException
      {
         is.close();
      }
   }
   
   static class TCZinAPK
   {
      String name;
      long ofs, len;
      RandomAccessFile raf;
   }
   
   private void loadAPK(String txt, boolean handleEx)
   {
      FileInputStream fis = null;
      SeekInputStream sis = null;
      ZipInputStream zis = null;
      try
      {
         fis = new FileInputStream(txt);
         byte[] b = new byte[fis.available()];
         fis.read(b);
         fis.close();
         String apk = new String(b);
         // search the apk for the tcfiles.zip
         sis = new SeekInputStream(apk);
         zis = new ZipInputStream(sis);
         ZipEntry ze,tcze;
         int base = tczs.size();
         while ((ze = zis.getNextEntry()) != null)
         {
            String name = ze.getName();
            if (name.equals("assets/tcfiles.zip"))
            {
               ZipInputStream tcz = new ZipInputStream(zis);
               while ((tcze = tcz.getNextEntry()) != null)
               {
                  name = tcze.getName();
                  if (!name.endsWith(".tcz")) // hold only tcz files
                     continue;
                  TCZinAPK e = new TCZinAPK();
                  e.name = name;
                  e.ofs = sis.pos; // note: this offset is just a guess (upper limit), since the stream is read in steps. later we'll find the correct offset
                  e.len = tcze.getSize();
                  tczs.add(e);
                  //AndroidUtils.debug(e.name+" ("+e.len+" bytes) - temp pos: "+e.ofs);
               }
               tcz.close();
               zis.close();
               sis.close();
               // now we open the file again, with random access, and search for the filenames to get the correct offset to them
               byte[] buf = new byte[Math.max(4096,sis.maxLength*2)]; // we use a bigger buffer to avoid having a tcz name cut into 2 parts
               RandomAccessFile raf = new RandomAccessFile(apk,"r");
               for (int i = base, n = tczs.size(); i < n; i++)
               {
                  TCZinAPK a = tczs.get(i);
                  a.raf = raf;
                  byte[] what = a.name.getBytes();
                  for (int j = 10;;)
                  {
                     long pos = a.ofs - buf.length/2; if (pos < 0) pos = 0;
                     raf.seek(pos);
                     int s = raf.read(buf);
                     int realOfs = indexOf(buf, s, what);
                     if (realOfs == -1) // if not found, double the read buffer and try again
                     {
                        if (--j == 0)
                        {
                           raf.close();
                           throw new RuntimeException("Error: cannot find real offset in APK for "+a.name);
                        }
                        buf = new byte[buf.length*2];
                        AndroidUtils.debug("read from "+pos+" to "+(pos+buf.length)+" but not found. doubling buffer to "+buf.length+"!");
                     }
                     else
                     {
                        a.ofs = pos + realOfs + what.length; // the data comes after the name
                        //AndroidUtils.debug(a.name+" - real pos: "+a.ofs);
                        break;
                     }
                  }
               }                              
               break;
            }
         }
      }
      catch (FileNotFoundException fnfe)
      {
         if (handleEx)
            AndroidUtils.handleException(fnfe,false);
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e,false);
      }
      finally
      {
         try {fis.close();} catch (Exception e) {}
         try {sis.close();} catch (Exception e) {}
         try {zis.close();} catch (Exception e) {}
      }
   }

   private static int indexOf(byte[] src, int srcLen, byte[] what)
   {
      if (src == null || srcLen == 0 || what == null || what.length == 0)
         return -1;
      int len = srcLen - what.length;
      byte b = what[0];
      int i,j,k;
      for (i=0; i < len; i++)
         if (src[i] == b) // first letter matches?
         {
            boolean found = true;
            for (j = 1,k=i+j; j < what.length && found; j++,k++)
               found &= src[k] == what[j]; // ps: cannot use continue here since we're inside 2 loops
            if (found) return i; // all matches!
         }
      return -1;
   }

   public static String listTCZs()
   {
      StringBuilder sb = new StringBuilder(128);
      sb.append(tczs.get(0).name);
      for (int i = 1, n = tczs.size(); i < n; i++)
         sb.append(",").append(tczs.get(i).name);
      return sb.toString();
   }
   
   public static int findTCZ(String tcz)
   {
      for (int i = 0, n = tczs.size(); i < n; i++)
         if (tczs.get(i).name.equals(tcz))
            return i;
      return -1;
   }
   
   public static int readTCZ(int fileIdx, int ofs, byte[] buf) // ofs is the relative offset inside the tcz file, not the absolute apk file position
   {
      synchronized (tczs)
      {
         try
         {
            TCZinAPK e = tczs.get(fileIdx);
            e.raf.seek(ofs + e.ofs);
            return e.raf.read(buf, 0, buf.length);
         }
         catch (Exception ex)
         {
            AndroidUtils.handleException(ex,false);
            return -2;
         }
      }
   }

   public static void closeTCZs()
   {
      // close the tczs
      RandomAccessFile lastRAF = null;
      for (int i = 0, n = tczs.size(); i < n; i++)
         if (tczs.get(i).raf != lastRAF)
            try {(lastRAF = tczs.get(i).raf).close();} catch (Exception e) {}
   }
   
   public static boolean callingZXing;
   public static String zxingResult;
   public static String zxing(String mode)
   {
      String text = null;
      try 
      {
         if (mode == null) mode = "";
         zxingResult = null;
         callingZXing = true;
         Message msg = loader.achandler.obtainMessage();
         Bundle b = new Bundle();
         b.putString("zxing.mode", mode);
         b.putInt("type",Loader.ZXING_SCAN);
         msg.setData(b);
         loader.achandler.sendMessage(msg);
         while (callingZXing)
            try {Thread.sleep(25);} catch (Exception e) {}
         return zxingResult;
      } 
      catch (Throwable e) 
      {
         e.printStackTrace();
         text = "***EXCEPTION"; // note: any tries to get the exception here will halt the vm (tested in sony ericsson xperia x1)
      }
      return text;
   }

   public static boolean callingSound;
   public static String soundResult;
   public static String soundToText(String params)
   {
      String text = null;
      try 
      {
         if (params == null) params = "";
         soundResult = null;
         callingSound = true;
         Message msg = loader.achandler.obtainMessage();
         Bundle b = new Bundle();
         b.putString("title", params);
         b.putInt("type",Loader.TOTEXT);
         msg.setData(b);
         loader.achandler.sendMessage(msg);
         while (callingSound)
            try {Thread.sleep(5);} catch (Exception e) {}
         return soundResult;
      } 
      catch (Throwable e) 
      {
         e.printStackTrace();
         text = "***EXCEPTION";
      }
      return text;
   }

   public static void soundFromText(String text)
   {
      try 
      {
         if (text == null) text = "";
         soundResult = null;
         callingSound = true;
         Message msg = loader.achandler.obtainMessage();
         Bundle b = new Bundle();
         b.putString("text", text);
         b.putInt("type",Loader.FROMTEXT);
         msg.setData(b);
         loader.achandler.sendMessage(msg);
         while (callingSound)
            try {Thread.sleep(5);} catch (Exception e) {}
      } 
      catch (Throwable e) 
      {
         e.printStackTrace();
         text = "***EXCEPTION";
      }
   }

   public static String getDefaultToString() {
      return Locale.getDefault().toString();
   }

   public static int getFreeMemory()
   {
      activityManager.getMemoryInfo(mi);
      return mi.availMem > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) mi.availMem;
   }
   ///////////////// crash controller //////////////////////
   
   static boolean createBugreport() // called by MainWindow using Vm.exec 
   {
      boolean containsUsefulInformation = false;
      long l2 = 0;
      try 
      {
         // generate the bugreport
         try {new File("/sdcard/IssueReport").mkdirs();} catch (Exception ee) {}
         String bugreportfn = "/sdcard/IssueReport/bugreport"+((int)(Math.random()*10000))+".txt";
         String[] commands =
            {
               "logcat -v threadtime -d TotalCross:I DEBUG:I *:S >"+bugreportfn+" \n",
            };
         java.lang.Process p = Runtime.getRuntime().exec("/system/bin/sh -");
         DataOutputStream os = new DataOutputStream(p.getOutputStream());
         for (String tmpCmd : commands) 
            os.writeBytes(tmpCmd);
         File f = new File(bugreportfn); // takes 33 seconds on a s3 mini
         while (true)
         {
            long l1 = f.length();
            Thread.sleep(100);
            l2 = f.length();
            if (l1 == l2)
               break;
         }
         if (l2 <= 512)   // ignore small reports
         {
            f.delete();
         }
         else
         {
            // zip the bugreport
            final String bugreportZip = "/sdcard/IssueReport/bugreport.zip";
            FileOutputStream fout = new FileOutputStream(bugreportZip);
            ZipOutputStream zout = new ZipOutputStream(fout);
            File ff = new File(bugreportfn);
            FileInputStream fin = new FileInputStream(ff);
            zout.putNextEntry(new ZipEntry("bugreport.txt"));
            byte[] buf = new byte[8192];
            for (int n; (n = fin.read(buf)) > 0;)
            {
               if (!containsUsefulInformation)
                  containsUsefulInformation = containsUsefulInformation(new String(buf,0,n));
               zout.write(buf,0,n);
            }
            zout.closeEntry();
            zout.close();
            fout.close();
            fin.close();
            ff.delete();
            if (!containsUsefulInformation)
               new File(bugreportZip).delete();
         }
      }
      catch (Exception e) 
      {
         AndroidUtils.handleException(e,false);
      }
      return containsUsefulInformation; // will be false if file is small
   }

   private static boolean containsUsefulInformation(String s)
   {
      s = s.toLowerCase();
      if (s.indexOf("totalcross") >= 0)
         return true;
      // TODO see if the tests below won't discard useful information
//      if (s.indexOf(">>> totalcross") >= 0)
//         return true;
//      if (s.indexOf("OutOfMemoryError") != -1 || s.indexOf("(tns") != -1 || s.indexOf("(tnS_") != -1)
//         return true;
//      if (s.indexOf("fontDestroy") != -1 || s.indexOf("Unhandled exception") != -1)
//         return true;
//      if (s.indexOf("#00") >= 0)
//      {            
//         if (s.indexOf("(dlfree)") != -1)
//            return true;
//         if (s.matches(".*(\\(strcmp\\))$")) // #00  pc 0000e108  /system/lib/libc.so (strcmp)
//            return true;
//      }
      return false;
   }

   private static SoundPool player;
   private static String lastSound;
   private static int lastSoundID;
   
   public static void soundPlay(String filename)
   {
      if (player == null)
         player = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
      if (!filename.equals(lastSound))
      {
         if (lastSound != null) player.unload(lastSoundID);
         lastSound = filename;
         lastSoundID =  player.load(filename, 1);
      }
      AudioManager audio = (AudioManager) loader.getSystemService(Context.AUDIO_SERVICE);
      int ring = audio.getRingerMode();
      if (ring == AudioManager.RINGER_MODE_NORMAL) // 4.4 does not returns correct values for volume methods
      {
         int volumeLevel = audio.getStreamVolume(AudioManager.STREAM_SYSTEM);
         int maxVolume   = audio.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
         float volume    = (float)volumeLevel/maxVolume;
         for (int i = 0; lastSoundID > 0 && player.play(lastSoundID, volume, volume, 0, 0, 1.0f) == 0 && i++ < 10;)
            try {Thread.sleep(50);} catch (Exception e) {}
      }
  }
   
//   int getHeightD(int size)
//   void configureD(String id);
//   void setSizeD(int s);
//   void setPositionD(int p);
//   void setVisibleD(boolean b)
//   boolean isVisibleD();
   
   public static final int GET_WH       = 1;
   public static final int CONFIGURE    = 2;
   public static final int SET_SIZE     = 3;
   public static final int SET_POSITION = 4;
   public static final int SET_VISIBLE  = 5;
   public static final int IS_VISIBLE   = 6;
   
   public static int adsRet = -1;
   public static int adsFunc(int func, int i, String s)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putInt("type",Loader.ADS_FUNC);
      b.putInt("func", func);
      b.putInt("int", i);
      b.putString("str", s);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
      while (adsRet == -1)
         try {Thread.sleep(10);} catch (Exception e) {};
      return adsRet;
   }
   
   public static void startActivity(Intent intent) {
      loader.startActivity(intent);
   }
   
   public static void enableSmsReceiver(boolean enabled, int port) {
      loader.enableSmsReceiver(enabled, port);
   }
   
    public static final PermissionHandler PHONE_STATE = new PermissionHandler(
            Loader.PermissionRequestCodes.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_STATE);

    public static final PermissionHandler LOCATION = new PermissionHandler(
            Loader.PermissionRequestCodes.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    public static final PermissionHandler STORAGE = new PermissionHandler(
            Loader.PermissionRequestCodes.EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);

    public static final PermissionHandler CAMERA = new PermissionHandler(
            Loader.PermissionRequestCodes.CAMERA,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE);

    public static class PermissionHandler {

        public static final int REQUESTING = 0;
        public static final int GRANTED = 1;
        public static final int DENIED = -1;

        public int permissionInitialized = DENIED;

        public final int requestCode;
        public final String[] permissions;

        public static final HashMap<Integer, PermissionHandler> permissionHandlerMap = new HashMap<>();

        public PermissionHandler(int requestCode, String... permissions) {
            this.requestCode = requestCode;
            this.permissions = permissions;
            permissionHandlerMap.put(requestCode, this);
        }

        public int requestPermissions(int requestCode, String... permissions) {
            List<String> missingPermissions = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat
                        .checkSelfPermission(Launcher4A.loader, permission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }
            if (missingPermissions.size() == 0) {
                return GRANTED;
            }
            // request permissions
            ActivityCompat.requestPermissions(
                    Launcher4A.loader,
                    missingPermissions.toArray(new String[missingPermissions.size()]),
                    requestCode);
            return REQUESTING;
        }

        public int requestPermissions() {
            if (permissionInitialized == GRANTED) {
                return GRANTED;
            }
            permissionInitialized = REQUESTING;
            if (requestPermissions(requestCode, permissions) == GRANTED) {
                permissionInitialized = GRANTED;
                return GRANTED;
            }
            while (permissionInitialized == REQUESTING) {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                }
            }
            return permissionInitialized;
        }

        public void onRequestPermissionsResult(String[] permissions, int[] grantResults) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.permissionInitialized = Launcher4A.PermissionHandler.GRANTED;
                // permission was granted, yay!
            } else {
                this.permissionInitialized = Launcher4A.PermissionHandler.DENIED;
                // permission denied, boo!
                // Disable the functionality that depends on this permission.
            }
        }
    }

    public static int requestStoragePermission() {
        return STORAGE.requestPermissions();
    }

    public static int requestCameraPermission() {
        return CAMERA.requestPermissions();
    }

    public static int requestPhoneStatePermission() {
        return PHONE_STATE.requestPermissions();
    }
    
    public static int requestLocationPermission() {
        return LOCATION.requestPermissions();
    }

    public static void playVideo(String id, boolean autoPlay, int start, int end) {
       loader.playVideo(id, autoPlay, start, end);
    }

}
