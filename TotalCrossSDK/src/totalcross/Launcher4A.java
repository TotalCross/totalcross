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



package totalcross;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.hardware.Camera;
import android.location.*;
import android.media.*;
import android.os.*;
import android.provider.*;
import android.telephony.*;
import android.telephony.gsm.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.OnKeyListener;
import android.view.inputmethod.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import totalcross.android.Loader;

final public class Launcher4A extends SurfaceView implements SurfaceHolder.Callback, MainClass, OnKeyListener, LocationListener, GpsStatus.Listener
{
   public static boolean canQuit = true;
   public static Launcher4A instance;
   public static Loader loader;
   static SurfaceHolder surfHolder;
   static TCEventThread eventThread;
   static Rect rDirty = new Rect();
   static boolean showKeyCodes;
   static Hashtable<String,String> htPressedKeys = new Hashtable<String,String>(5);
   static int lastScreenW, lastScreenH, lastType, lastX, lastY=-999, lastOrientation;
   static Camera camera;
   public static boolean appPaused;
   static PhoneListener phoneListener;
   static boolean showingAlert;
   static int deviceFontHeight; // guich@tc126_69
   static int appHeightOnSipOpen;
   static int appTitleH;
   private static android.text.ClipboardManager clip;
   
   static Handler viewhandler = new Handler()
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
                  gps = (LocationManager) loader.getSystemService(Context.LOCATION_SERVICE);
                  gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, instance);
                  gps.addGpsStatusListener(instance);
                  break;
               case GPSFUNC_STOP:
                  if (gps != null)
                  {
                     gps.removeUpdates(instance);
                     gps.removeGpsStatusListener(instance);
                     validSatellites = 0;
                     gps = null;
                  }
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
      // read all apk names, before loading the vm
      if (!isSingleAPK)
      {
         loadAPK("/data/data/totalcross.android/apkname.txt",true); // vm
      }
      loadAPK(appPath+"/apkname.txt",true);
      System.loadLibrary("tcvm");
      instance = this;
      loader = context;
      surfHolder = getHolder();
      surfHolder.setFormat(PixelFormat.RGBA_8888);
      surfHolder.addCallback(this);
      setWillNotDraw(true);
      setWillNotCacheDrawing(true);
      setFocusableInTouchMode(true);
      requestFocus();
      setOnKeyListener(this);
      Configuration config = getResources().getConfiguration();
      hardwareKeyboardIsVisible = config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO || config.keyboard == Configuration.KEYBOARD_QWERTY; // motorola titanium returns HARDKEYBOARDHIDDEN_YES but KEYBOARD_QWERTY. In soft inputs, it returns KEYBOARD_NOKEYS
      lastOrientation = getOrientation();
      String vmPath = context.getApplicationInfo().dataDir;
      initializeVM(context, tczname, appPath, vmPath, cmdline);
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
      WindowManager wm = (WindowManager)instance.getContext().getSystemService(Context.WINDOW_SERVICE);
      return wm.getDefaultDisplay().getOrientation();
   }
   
   private android.view.Surface lastSurface;
   
   public void surfaceChanged(final SurfaceHolder holder, int format, int w, int h) 
   {
      if (h == 0 || w == 0 || appPaused) return;
      WindowManager wm = (WindowManager)instance.getContext().getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      int screenHeight = display.getHeight();
      int currentOrientation = getOrientation();
      boolean rotated = currentOrientation != lastOrientation;
      lastOrientation = currentOrientation;
      
      if (h < lastScreenH && Loader.isFullScreen && lastScreenH == screenHeight && appTitleH == 0) // 1: surfaceChanged. 0 -> 480. displayH: 480  =>  2: surfaceChanged. 480 -> 455. displayH: 480
         appTitleH = lastScreenH - h; 
      
      if (sipVisible) // sip changed?
      {
         instance.nativeInitSize(null,-999,h); // signal vm that the keyboard will appear
         if (rotated) // close the sip if a rotation occurs
            setSIP(SIP_HIDE);
      }
      else
      {
         instance.nativeInitSize(null,-999,0); // signal vm that the keyboard will hide
         android.view.Surface surface = holder.getSurface();
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
      if (appPaused) return;
      eventThread.invokeInEventThread(false, new Runnable()
      {
         public void run()
         {
            nativeInitSize(lastSurface,lastScreenW,lastScreenH);
            deviceFontHeight = (int)new TextView(getContext()).getTextSize();
            rDirty.left = rDirty.top = 0;
            rDirty.right = lastScreenW;
            rDirty.bottom = lastScreenH;
            DisplayMetrics metrics = getResources().getDisplayMetrics();
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

   ///////////////////////// ANDROID 4.4 BACK KEY BUG ///////////////////////////////
   // https://code.google.com/p/android/issues/detail?id=62306
   // http://stackoverflow.com/questions/18581636/android-cannot-capture-backspace-delete-press-in-soft-keyboard/19980975#19980975

   class EditableAccomodatingLatinIMETypeNullIssues extends SpannableStringBuilder 
   {
      EditableAccomodatingLatinIMETypeNullIssues(CharSequence source) 
      {
         super(source);
      }

      public SpannableStringBuilder replace(final int spannableStringStart, final int spannableStringEnd, CharSequence replacementSequence, int replacementStart, int replacementEnd) 
      {
         if (replacementEnd > replacementStart) 
         {
            super.replace(0, length(), "", 0, 0);
            return super.replace(0, 0, replacementSequence, replacementStart, replacementEnd);
         }
         if (spannableStringEnd > spannableStringStart) 
         {
            super.replace(0, length(), "", 0, 0);
            return super.replace(0, 0, "/", 0, 1);
         }
         return super.replace(spannableStringStart, spannableStringEnd, replacementSequence, replacementStart, replacementEnd);
      }
   }
   
   class InputConnectionAccomodatingLatinIMETypeNullIssues extends BaseInputConnection 
   {
      Editable myEditable;
      String dummy;
      KeyEvent delUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL);
      KeyEvent delDn = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
      
      public InputConnectionAccomodatingLatinIMETypeNullIssues(View targetView, boolean fullEditor) 
      {
         super(targetView, fullEditor);
      }
      
      public Editable getEditable() 
      {
         if (Build.VERSION.SDK_INT >= 19) 
         {
            if (dummy == null)
            {
               char[] c = new char[1024]; // guich: set a reasonable size for the buffer
               for (int i = 0; i < c.length; i++)
                  c[i] = (i & 1) == 0 ? (char)255 : (char)257;
               dummy = new String(c);      
            }
            if (myEditable == null) 
            {
               myEditable = new EditableAccomodatingLatinIMETypeNullIssues(dummy);
               Selection.setSelection(myEditable, dummy.length());
            }
            else 
            {
               int myEditableLength = myEditable.length(); 
               if (myEditableLength == 0) 
               {
                  myEditable.append(dummy);
                  Selection.setSelection(myEditable, dummy.length());
               }
            }
            return myEditable;
         }
         return super.getEditable();
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
   }

   public InputConnection onCreateInputConnection(EditorInfo outAttrs)
   {
      //outAttrs.inputType = android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS; - this makes android's fullscreen keyboard appear in landscape
      outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE | 0x2000000/*EditorInfo.IME_FLAG_NO_FULLSCREEN*/; // the NO_FULLSCREEN flag fixes the problem of keyboard not being shifted correctly in android >= 3.0
      outAttrs.inputType = InputType.TYPE_NULL;
      outAttrs.actionLabel = null;
      return new InputConnectionAccomodatingLatinIMETypeNullIssues(this, false);
   }
   //////////////////////////////////////////////////////////////////////////////////
   
   private static boolean altNext;
   private static boolean shiftNext;
   
   public boolean onKeyPreIme(int keyCode, KeyEvent event)
   {
      if (keyCode == KeyEvent.KEYCODE_BACK)
         return onKey(this, keyCode, event);
      else
         return false;
   }
   
   public static boolean hardwareKeyboardIsVisible;
   
   protected void onSizeChanged(int w, int h, int oldw, int oldh)
   {
      super.onSizeChanged(w, h, oldw, oldh);
   }

   public boolean onKey(View v, int keyCode, KeyEvent event)
   {
      if (keyCode == KeyEvent.KEYCODE_BACK) // guich@tc130: if the user pressed the back key on the SIP, don't pass it to the application
      {
         if (!hardwareKeyboardIsVisible && sipVisible)
         {
            if (event.getAction() == KeyEvent.ACTION_UP)
               setSIP(SIP_HIDE);
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
            int state = event.getMetaState();
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
               alert("Key code: " + keyCode + ", Modifier: " + state);
            eventThread.pushEvent(KEY_PRESS, c, keyCode, 0, state, 0);
            break;
         case KeyEvent.ACTION_MULTIPLE: // unicode chars
         String str = event.getCharacters();
         if (str != null)
         {
            char[] chars = str.toCharArray();
            for (int i =0; i < chars.length; i++)
               eventThread.pushEvent(KEY_PRESS, chars[i],chars[i],0,event.getMetaState(),0);
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
         case MotionEvent.ACTION_DOWN: type = PEN_DOWN; break;
         case MotionEvent.ACTION_UP:   type = PEN_UP;   break;
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
         eventThread.running = false;
         loader.finish();
      }
      canQuit = true;
   }
   
   public static void stopVM()
   {
      restoreSound();
      if (gps != null) // stop the gps if still running
      {
         Message msg = viewhandler.obtainMessage();
         Bundle b = new Bundle();
         b.putInt("type", GPSFUNC_STOP);
         msg.setData(b);
         viewhandler.sendMessage(msg);
      }
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
   
   static void setDeviceTitle(String title)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putString("setDeviceTitle.title", title);
      b.putInt("type",Loader.TITLE);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
   }
   
   static void showCamera(String fileName, int stillQuality, int width, int height, boolean allowRotation)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putString("showCamera.fileName", fileName);
      b.putInt("showCamera.quality", stillQuality);
      b.putInt("showCamera.width",width);
      b.putInt("showCamera.height",height);
	  b.putBoolean("showCamera.allowRotation", allowRotation);
      b.putInt("type",Loader.CAMERA);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
   }
   
   public native static void pictureTaken(int res);
   native void initializeVM(Context context, String tczname, String appPath, String vmPath, String cmdline);
   native void nativeInitSize(Surface surface, int w, int h);
   native void nativeOnEvent(int type, int key, int x, int y, int modifiers, int timeStamp);
   
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
   public static final int SIP_ENABLE_NUMERICPAD = 10004; // guich@tc110_55
   public static final int SIP_DISABLE_NUMERICPAD = 10005; // guich@tc110_55   
   
   static boolean sipVisible;
   
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
   
   public static void setSIP(int sipOption)
   {
      InputMethodManager imm = (InputMethodManager) instance.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      switch (sipOption)
      {
         case SIP_HIDE:
            sipVisible = false;
            if (Loader.isFullScreen)
               setLoaderFullScreen(true);
            else
               imm.hideSoftInputFromWindow(instance.getWindowToken(), 0, instance.siprecv);
            break;
         case SIP_SHOW:
         case SIP_TOP:
         case SIP_BOTTOM:
            sipVisible = true;
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
   private static final int GPSFUNC_START = 7;
   private static final int GPSFUNC_STOP = 8;
   private static final int GPSFUNC_GETDATA = 9;
   private static final int CELLFUNC_START = 10;
   private static final int CELLFUNC_STOP = 11;
   private static final int VIBRATE = 12;
   private static final int CLIPBOARD = 13;
   
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
   
   public static String getSDCardPath()
   {
      java.io.File sd = Environment.getExternalStorageDirectory();
      return sd.canWrite() ? sd.toString() : null;
   }

   // gps stuff
   public static int validSatellites;

   public void onGpsStatusChanged(int event) 
   {
      if (gps != null && (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS || event == GpsStatus.GPS_EVENT_FIRST_FIX)) 
      {
         GpsStatus status = gps.getGpsStatus(null);
         Iterable<GpsSatellite> sats = status.getSatellites();
         validSatellites = 0;
         for (GpsSatellite sat : sats)
            if (sat.usedInFix())
               validSatellites++;
      }
   }

   private static LocationManager gps;
   private static String lastGps = "*";
   public static String gpsFunc(int what)
   {
      switch (what)
      {
         case GPSFUNC_GETDATA:
            return gps != null && gps.isProviderEnabled(LocationManager.GPS_PROVIDER) ? lastGps : null;
         case GPSFUNC_START:
            if (!isGpsOn())
               return null;
            lastGps = "*";
         case GPSFUNC_STOP:
            Message msg = viewhandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("type", what);
            msg.setData(b);
            viewhandler.sendMessage(msg);
            if (what == GPSFUNC_START)
            {
               while (gps == null)
                  try {Thread.sleep(100);} catch (Exception e) {}
               return gps.isProviderEnabled(LocationManager.GPS_PROVIDER) ? "*" : null;
            }
            break;
      }
      return null;
   }

   private static boolean isGpsOn()
   {
      LocationManager manager = (LocationManager) loader.getSystemService(Context.LOCATION_SERVICE);
      return manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER); 
   }

   public void onLocationChanged(Location loc)
   {
      try
      {
         int sats = loc.getExtras().getInt("satellites");
         String provider = loc.getProvider();
         if (provider == null || provider.equals("gps"))
         {
            String lat = Double.toString(loc.getLatitude()); //flsobral@tc126_57: Decimal separator might be platform dependent when using Location.convert with Location.FORMAT_DEGREES.
            String lon = Double.toString(loc.getLongitude());
            
            Calendar fix = new GregorianCalendar(TimeZone.getTimeZone("GMT")); //flsobral@tc126_57: Date is deprecated, and apparently bugged for some devices. Replaced with Calendar.
            fix.setTimeInMillis(loc.getTime());
            String sat = String.valueOf(Math.max(sats, validSatellites));
            String vel = loc.hasSpeed() && loc.getSpeed() != 0d ? String.valueOf(loc.getSpeed())   : "";
            String dir = loc.hasBearing() ? String.valueOf(loc.getBearing()) : "";
            String sfix = fix.get(Calendar.YEAR)+"/"+(fix.get(Calendar.MONTH)+1)+"/"+fix.get(Calendar.DAY_OF_MONTH)+" "+fix.get(Calendar.HOUR_OF_DAY)+":"+fix.get(Calendar.MINUTE)+":"+fix.get(Calendar.SECOND);
            float pdop = loc.hasAccuracy() ? loc.getAccuracy() : 0; // guich@tc126_66
            lastGps = lat+";"+lon+";"+sfix+";"+sat+";"+vel+";"+dir+";"+pdop+";";
         }
      }
      catch (Exception exception)
      {
         lastGps = "*";
      }
   }

   public void onProviderDisabled(String provider)   {}
   public void onProviderEnabled(String provider)    {}
   public void onStatusChanged(String provider, int status, Bundle extras)   {}

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
            position = loc.getLastKnownLocation(p);
            if (position != null)
               return new double[]{position.getLatitude(),position.getLongitude()};
         }
      }
      else
      if (address.startsWith("@"))
      {
         String[] st = address.substring(1).split(",");
         return new double[]{Double.parseDouble(st[0]),Double.parseDouble(st[0])};
      }
      else
      {         
         Geocoder g = new Geocoder(instance.getContext());
         List<Address> al = g.getFromLocationName(address, 1);
         if (al != null && al.size() > 0)
         {
            Address a = al.get(0);
            if (a.hasLatitude() && a.hasLongitude())
               return new double[]{a.getLatitude(),a.getLongitude()};
         }
      }
      return null;
   }
   
   public static boolean showRoute(String addressI, String addressF, String coords, boolean showSatellite) throws IOException
   {
      boolean tryAgain = false;
      int tryAgainCount = 0;
      do
      {
         try
         {
            AndroidUtils.debug("addrs: "+addressI+", "+addressF);
            double[] llI = getLatLon(addressI);
            double[] llF = getLatLon(addressF);
            AndroidUtils.debug(llI[0]+","+llI[1]+" - "+llF[0]+","+llF[1]);
            if (llI != null && llF != null)
            {
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
               b.putBoolean("sat", showSatellite);
               msg.setData(b);
               loader.achandler.sendMessage(msg);
               while (showingMap)
                  try {Thread.sleep(400);} catch (Exception e) {}
               return true;
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
               try {Thread.sleep(500);} catch (Exception ee) {}
               AndroidUtils.debug("Internet out of range. Trying again to get location ("+tryAgainCount+" of 5)");
            }
         }
      } while (tryAgain);
      return false;
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
                  try {Thread.sleep(400);} catch (Exception e) {}
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
               try {Thread.sleep(500);} catch (Exception ee) {}
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
            try {Thread.sleep(100);} catch (Exception e) {}
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
         setSIP(SIP_HIDE);
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
                           throw new RuntimeException("Error: cannot find real offset in APK for "+a.name);
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
            try {Thread.sleep(200);} catch (Exception e) {}
         return zxingResult;
      } 
      catch (Throwable e) 
      {
         e.printStackTrace();
         text = "***EXCEPTION"; // note: any tries to get the exception here will halt the vm (tested in sony ericsson xperia x1)
      }
      return text;
   }

}
