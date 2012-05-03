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

import totalcross.android.*;
import totalcross.android.compat.*;

import java.util.*;

import android.view.animation.*;
import android.view.animation.Animation.AnimationListener;
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
import android.util.*;
import android.view.*;
import android.view.View.OnKeyListener;
import android.view.inputmethod.*;
import android.widget.*;

final public class Launcher4A extends SurfaceView implements SurfaceHolder.Callback, MainClass, OnKeyListener, LocationListener
{
   public static boolean canQuit = true;
   public static Launcher4A instance;
   public static Loader loader;
   public static Bitmap sScreenBitmap;
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
                  gpsStatus = gps.getGpsStatus(null);
                  break;
               case GPSFUNC_STOP:
                  if (gps != null)
                  {
                     gps.removeUpdates(instance);
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
   
   public Launcher4A(Loader context, String tczname, String appPath, String cmdline)
   {
      super(context);
      System.loadLibrary("tcvm");
      instance = this;
      loader = context;
      surfHolder = getHolder();
      surfHolder.addCallback(this);
      setWillNotDraw(true);
      setWillNotCacheDrawing(true);
      setFocusableInTouchMode(true);
      requestFocus();
      setOnKeyListener(this);
      hardwareKeyboardIsVisible = getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
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
   
   private static int firstOrientationSize;
   
   public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
   {
      WindowManager wm = (WindowManager)instance.getContext().getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      int screenHeight = display.getHeight();
      // guich@tc130: create a bitmap with the real screen size only once to prevent creating it again when screen rotates
      if (sScreenBitmap == null) 
      {
         int screenSize = Math.max(screenHeight, display.getWidth());
         if (Build.VERSION.SDK_INT >= 13)
         {
            // if first try, check if the value was already cached
            int temp;
            if (firstOrientationSize == 0 && (temp = AndroidUtils.getSavedScreenSize()) != -1)
            {
               screenSize = temp;
               //AndroidUtils.debug("restoring size from cache: "+screenSize);
               firstOrientationSize = 1;
            }
            else
            {
               // not yet cached. first try? store the size and cache it
               if (firstOrientationSize == 0)
               {
                  //AndroidUtils.debug("first: "+screenSize);
                  firstOrientationSize = screenSize;
                  sendOrientationChange(true);
                  return;
               }
               //AndroidUtils.debug("second: "+screenSize);
               if (firstOrientationSize > screenSize)
                  screenSize = firstOrientationSize;
               AndroidUtils.setSavedScreenSize(screenSize);
               sendOrientationChange(false); // restore orientation to what user wants
            }
         }
         sScreenBitmap = Bitmap.createBitmap(screenSize,screenSize, Bitmap.Config.RGB_565/*Bitmap.Config.ARGB_8888 - ALSO CHANGE ANDROID_BPP to 32 at android/gfx_ex.h */);
         sScreenBitmap.eraseColor(0xFFFFFFFF);
         nativeSetOffcreenBitmap(sScreenBitmap); // call Native C code to set the screen buffer
         
         // guich@tc126_32: if fullScreen, make sure that we create the screen only when we are set in fullScreen resolution
         // applications start at non-fullscreen mode. when fullscreen is set, this method is called again. So we wait
         // for this second chance and ignore the first one.

         // 1. this is failing on Xoom! at first run, we get a black screen; have to exit and call the program again to work
         // 2. occurs because the xoom has a bar at the bottom that has non-physic buttons, which appears even when the app is full screen
         // 3. commenting this is now harmless because now we always create a bitmap with the size of the screen
//         if (Loader.isFullScreen && h != screenHeight) return; 
      }
      int currentOrientation = getOrientation();
      boolean rotated = currentOrientation != lastOrientation;
      lastOrientation = currentOrientation;
      
      if (h < lastScreenH && Loader.isFullScreen && lastScreenH == screenHeight && appTitleH == 0) // 1: surfaceChanged. 0 -> 480. displayH: 480  =>  2: surfaceChanged. 480 -> 455. displayH: 480
         appTitleH = lastScreenH - h; 
      
      if (sipVisible) // sip changed?
      {
         if (rotated) // close the sip if a rotation occurs
            setSIP(SIP_HIDE,true);
         return;
      }
      
      if (w != lastScreenW || h != lastScreenH)
      {
         lastScreenW = w;
         lastScreenH = h;
         eventThread.invokeInEventThread(false, new Runnable()
         {
            public void run()
            {
               deviceFontHeight = (int)new TextView(getContext()).getTextSize();
               rDirty.left = rDirty.top = 0;
               rDirty.right = lastScreenW;
               rDirty.bottom = lastScreenH;
               Canvas canvas = surfHolder.lockCanvas(rDirty);
               surfHolder.unlockCanvasAndPost(canvas);
               DisplayMetrics metrics = getResources().getDisplayMetrics();
               _postEvent(SCREEN_CHANGED, lastScreenW, lastScreenH, (int)(metrics.xdpi+0.5), (int)(metrics.ydpi+0.5),deviceFontHeight);
            }
         });
      }
   }

   private void sendOrientationChange(boolean invert)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putBoolean("invert",invert);
      b.putInt("type",Loader.INVERT_ORIENTATION);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
   }

   public void surfaceCreated(SurfaceHolder holder)
   {
      // here is where everything starts
      if (eventThread == null)
         eventThread = new TCEventThread(this);
   }

   public void surfaceDestroyed(SurfaceHolder holder)
   {
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

   public InputConnection onCreateInputConnection(EditorInfo outAttrs)
   {
      return new BaseInputConnection(this, false)
      {
         public boolean deleteSurroundingText(int leftLength, int rightLength)
         {
            if (rightLength > 0 && leftLength == 0) // do not handle right deletions
               return true;
            
            while (leftLength-- >= 0)
            {
               if (!sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)))
                  return false;
               if (!sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL)))
                  return false;
            }
            
            return true;
         }
      };
   }
   
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
      if (oldh < h)
         updateScreen(0,0,w,h,TRANSITION_NONE);
   }

   public boolean onKey(View v, int keyCode, KeyEvent event)
   {
      if (keyCode == KeyEvent.KEYCODE_BACK) // guich@tc130: if the user pressed the back key on the SIP, don't pass it to the application
      {
         if (!hardwareKeyboardIsVisible && sipVisible)
         {
            if (event.getAction() == KeyEvent.ACTION_UP)
               setSIP(SIP_HIDE,true);
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
      if (type != lastType || x != lastX || y != lastY) // skip identical events
      {
         lastType = type;
         lastX = x;
         lastY = y;
         eventThread.pushEvent(type, 0, x, y, 0, 0);
      }
      return true;
   }

   private static final int TRANSITION_NONE = 0;
   private static final int TRANSITION_OPEN = 1;
   private static final int TRANSITION_CLOSE = 2;
   
   static class ScreenView extends SurfaceView
   {
      public ScreenView(Context context)
      {
         super(context);
         setWillNotDraw(false);
         setWillNotCacheDrawing(true);
      }
      
      public void draw(Canvas c)
      {
         c.drawBitmap(sScreenBitmap,0,0,null);
      }
   }
   
   static class AnimationThread implements Runnable,AnimationListener
   {
      Bitmap bm;
      java.util.concurrent.CountDownLatch latch;
      int trans;
      ImageView newView;
      ScreenView scrView;
      
      void startTransition(int trans)
      {
         this.trans = trans;
         latch = new java.util.concurrent.CountDownLatch(1);
         loader.runOnUiThread(this);
         try {animt.latch.await();} catch (InterruptedException ie) {}
      }
      
      public void run()
      {
         if (scrView == null)
         {
            ViewGroup vg = (ViewGroup)instance.getParent();
            scrView = new ScreenView(instance.getContext());
            newView = new ImageView(instance.getContext());
            newView.setWillNotCacheDrawing(true);
            newView.setVisibility(ViewGroup.INVISIBLE);
            scrView.setVisibility(ViewGroup.INVISIBLE);
            vg.addView(scrView);
            vg.addView(newView);
         }
         // since our bitmap is greater than the screen, we have to create another one and copy only the visible part
         if (bm == null || bm.getWidth() != lastScreenW || bm.getHeight() != lastScreenH)
         {
            bm = Bitmap.createBitmap(lastScreenW,lastScreenH, Bitmap.Config.RGB_565);
            bm.eraseColor(0xFFFFFFFF);
            newView.setImageBitmap(bm);
         }
         Animation anim;
         if (trans == TRANSITION_OPEN)
         {
            new Canvas(bm).drawBitmap(sScreenBitmap,0,0,null);
            newView.setImageBitmap(bm);
            anim = new ScaleAnimation(0,1,0,1,lastScreenW/2,lastScreenH/2);
            anim.setDuration(500);
            anim.setAnimationListener(this);
            newView.setVisibility(ViewGroup.VISIBLE);
            newView.startAnimation(anim);
         }
         else // TRANSITION_CLOSE
         {
            anim = new ScaleAnimation(1,0,1,0,lastScreenW/2,lastScreenH/2);
            anim.setDuration(500);
            anim.setAnimationListener(this);

            newView.setImageBitmap(bm);
            newView.setVisibility(ViewGroup.VISIBLE);
            scrView.setVisibility(ViewGroup.VISIBLE);
            newView.startAnimation(anim);
         }
      }
      
      public void onAnimationEnd(Animation animation)
      {
         drawScreen();
         newView.setVisibility(ViewGroup.INVISIBLE);
         scrView.setVisibility(ViewGroup.INVISIBLE);
         latch.countDown();
      }

      public void onAnimationRepeat(Animation animation) {}
      public void onAnimationStart(Animation animation) {}
   }
   
   static AnimationThread animt = new AnimationThread();
   
   static void drawScreen()
   {
      Canvas canvas = surfHolder.lockCanvas(rDirty);
      if (canvas != null)
      {
         canvas.drawBitmap(sScreenBitmap, rDirty,rDirty, null);
         surfHolder.unlockCanvasAndPost(canvas);
      }
   }

   static void transitionEffectChanged(int type)
   {
      if (type == TRANSITION_CLOSE && sScreenBitmap != null && animt != null && animt.bm != null)
         new Canvas(animt.bm).drawBitmap(sScreenBitmap,0,0,null);
   }
   
   static void updateScreen(int dirtyX1, int dirtyY1, int dirtyX2, int dirtyY2, int transitionEffect)
   {
      if (!appPaused)
      try
      {
         //long ini = System.currentTimeMillis();
         if (sScreenBitmap == null || camera != null)
            return;
         
         switch (transitionEffect)
         {
            case TRANSITION_CLOSE:
            case TRANSITION_OPEN:
               animt.startTransition(transitionEffect);
               // no break!
            case TRANSITION_NONE:
               rDirty.left = dirtyX1; rDirty.top = dirtyY1; rDirty.right = dirtyX2; rDirty.bottom = dirtyY2;
               drawScreen();
               break;
         }
         //int ela = (int)(System.currentTimeMillis() - ini);
         //AndroidUtils.debug((1000/ela) + " fps "+rDirty);
      }
      catch (Throwable t)
      {
         AndroidUtils.debug(Log.getStackTraceString(t));
      }
   }

   // 1. when the program calls MainWindow.exit, exit below is called before stopVM
   // 2. when the vm is stopped because another program will run, stopVM is called before exit.
   // so, we just have to wait (canQuit=false) in situation 2.
   private final static int SOFT_EXIT = 0x40000000;
   static void exit(int ret)
   {
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
      return eventThread.eventAvailable();
   }
   
   public static void pumpEvents()
   {
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
               updateScreen(0,0,instance.getWidth(),instance.getHeight(), TRANSITION_NONE);
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
   
   static void showCamera(String fileName, int stillQuality, int width, int height)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putString("showCamera.fileName", fileName);
      b.putInt("showCamera.quality", stillQuality);
      b.putInt("showCamera.width",width);
      b.putInt("showCamera.height",height);
      b.putInt("type",Loader.CAMERA);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
   }
   
   public native static void pictureTaken(int res);
   native void initializeVM(Context context, String tczname, String appPath, String vmPath, String cmdline);
   native void nativeSetOffcreenBitmap(Bitmap bmp);
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
   
   public static void setSIP(int sipOption)
   {
      setSIP(sipOption,false);
   }
   
   class SipClosedReceiver extends ResultReceiver
   {
      public SipClosedReceiver()
      {
         super(null);
      }
      public void onReceiveResult(int resultCode, Bundle resultData)
      {
         sendCloseSIPEvent();
      }
   }
   public SipClosedReceiver siprecv = new SipClosedReceiver();
   
   public static void setSIP(int sipOption, boolean sendEvent)
   {
      InputMethodManager imm = (InputMethodManager) instance.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      switch (sipOption)
      {
         case SIP_HIDE:
            sipVisible = false;
            if (Loader.isFullScreen)
               setLoaderFullScreen(true,sendEvent);
            else
               imm.hideSoftInputFromWindow(instance.getWindowToken(), 0, instance.siprecv);
            break;
         case SIP_SHOW:
         case SIP_TOP:
         case SIP_BOTTOM:
            sipVisible = true;
            if (Loader.isFullScreen)
               setLoaderFullScreen(false,sendEvent);
            else
               imm.showSoftInput(instance, 0); 
            break;
      }
   }

   private static void setLoaderFullScreen(boolean full, boolean sendEvent)
   {
      Message msg = loader.achandler.obtainMessage();
      Bundle b = new Bundle();
      b.putBoolean("fullScreen", full);
      b.putBoolean("sendEvent", sendEvent);
      b.putInt("type",Loader.FULLSCREEN);
      msg.setData(b);
      loader.achandler.sendMessage(msg);
   }
   
   public static void sendCloseSIPEvent()
   {
      eventThread.pushEvent(SIP_CLOSED,0,0,0,0,0);
   }

   public static int getAppHeight()
   {
      return instance.getHeight();
   }
   
   public static int setElapsed(int n)
   {
      SharedPreferences pref = loader.getPreferences(Context.MODE_PRIVATE);
      if (n == 0)
         return pref.getInt("demotime",0);
      pref.edit().putInt("demotime",n).commit();
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
   private static LocationManager gps;
   private static GpsStatus gpsStatus;
   private static String lastGps = "*";
   public static String gpsFunc(int what)
   {
      switch (what)
      {
         case GPSFUNC_GETDATA:
            return gps != null && gps.isProviderEnabled(LocationManager.GPS_PROVIDER) ? lastGps : null;
         case GPSFUNC_START:
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

   public void onLocationChanged(Location loc)
   {
      String lat = Double.toString(loc.getLatitude()); //flsobral@tc126_57: Decimal separator might be platform dependent when using Location.convert with Location.FORMAT_DEGREES.
      String lon = Double.toString(loc.getLongitude());
      
      Calendar fix = new GregorianCalendar(TimeZone.getTimeZone("GMT")); //flsobral@tc126_57: Date is deprecated, and apparently bugged for some devices. Replaced with Calendar.
      fix.setTimeInMillis(loc.getTime());
      int satellites = gps.getGpsStatus(gpsStatus).getMaxSatellites();
      String sat = satellites < 255 && satellites > 0 ? String.valueOf(satellites) : "";
      String vel = loc.hasSpeed() && loc.getSpeed() != 0d ? String.valueOf(loc.getSpeed())   : "";
      String dir = loc.hasBearing() ? String.valueOf(loc.getBearing()) : "";
      String sfix = fix.get(Calendar.YEAR)+"/"+(fix.get(Calendar.MONTH)+1)+"/"+fix.get(Calendar.DAY_OF_MONTH)+" "+fix.get(Calendar.HOUR_OF_DAY)+":"+fix.get(Calendar.MINUTE)+":"+fix.get(Calendar.SECOND);
      float pdop = loc.hasAccuracy() ? loc.getAccuracy() : 0; // guich@tc126_66
      lastGps = lat+";"+lon+";"+sfix+";"+sat+";"+vel+";"+dir+";"+pdop+";";
   }

   public void onProviderDisabled(String provider)
   {
   }

   public void onProviderEnabled(String provider)
   {
   }

   public void onStatusChanged(String provider, int status, Bundle extras)
   {
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
            double lat=0,lon=0;
            boolean latlonOk = false;
            if (address.equals("")) // last known location?
            {
               // first, use the location manager
               LocationManager loc = (LocationManager) loader.getSystemService(Context.LOCATION_SERVICE);
               List<String> pros = loc.getProviders(true);
               Location position = null;
               for (String p : pros)
               {
                  position = loc.getLastKnownLocation(p);
                  //AndroidUtils.debug("Provider: "+p+"  "+position);
                  if (position != null)
                  {
                     lat = position.getLatitude();
                     lon = position.getLongitude();
                     latlonOk = true;
                     break;
                  }
               }
            }
            else
            if (address.startsWith("@"))
            {
               StringTokenizer st = new StringTokenizer(address.substring(1),",",false);
               lat = Double.parseDouble(st.nextToken());
               lon = Double.parseDouble(st.nextToken());
               latlonOk = true;
            }
            else
            {         
               Geocoder g = new Geocoder(instance.getContext());
               List<Address> al = g.getFromLocationName(address, 1);
               if (al != null && al.size() > 0)
               {
                  Address a = al.get(0);
                  if (a.hasLatitude() && a.hasLongitude())
                  {
                     lat = a.getLatitude();
                     lon = a.getLongitude();
                     latlonOk = true;
                  }
               }
            }
            if (latlonOk)
            {
               // call the loader
               showingMap = true;
               Message msg = loader.achandler.obtainMessage();
               Bundle b = new Bundle();
               b.putInt("type", Loader.MAP);
               b.putDouble("lat", lat);
               b.putDouble("lon", lon);
               b.putBoolean("sat", showSatellite);
               msg.setData(b);
               loader.achandler.sendMessage(msg);
               while (showingMap)
                  try {Thread.sleep(400);} catch (Exception e) {}
               return true;
            }
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
      if (eventThread != null)
      {
         setSIP(SIP_HIDE,true);
         eventThread.pushEvent(APP_PAUSED, 0, 0, 0, 0, 0);
      }
   }
   
   public static void appResumed()
   {
      appPaused = false;
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
         List<Camera.Size> sizes = Level5.getInstance().getSupportedPictureSizes(parameters);
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
}
