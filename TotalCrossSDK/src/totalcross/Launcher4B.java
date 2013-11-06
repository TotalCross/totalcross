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

import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CDMAInfo;
import net.rim.device.api.system.Device;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.system.OwnerInfo;
import net.rim.device.api.system.PersistentContentException;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.Touchscreen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.VirtualKeyboard;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.StringUtilities;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;
import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.net.SocketTimeoutException;
import totalcross.net.ssl.SSLSocket;
import totalcross.net.ssl.SSLSocket4B;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.Flick;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Coord;
import totalcross.ui.gfx.Graphics4B;
import totalcross.ui.gfx.Rect;
import totalcross.util.Hashtable;
import totalcross.util.IntHashtable;
import totalcross.util.IntVector;
import totalcross.util.Logger;
import totalcross.util.Vector;
import totalcross.util.zip.TCZ;
import totalcross.util.zip.ZipException;

public class Launcher4B
{
   public static Launcher4B instance;
   public static int userFontSize = -1;
   private UiApplication stub;
   private boolean started;
   private String className;
   private ApplicationData appData;
   public MainWindow mainWindow;
   private WinTimer winTimer;
   private TCEventThread eventThread;
   private Thread demoThread;
   public int threadCount;
   public String commandLine;
   public IntHashtable keysPressed = new IntHashtable(129);
   public boolean rebootWhenExit;
   public boolean showKeyCodes;
   private String[] resourcesList;
   private String moduleName;
   private Hashtable finalizables = new Hashtable(127);
   private IntHashtable finalizablesCount = new IntHashtable(127);
   private Vector alertQueue = new Vector();
   private boolean isHandlingTouch;
   private boolean isSipVisible;
   private Control sipControl;
   private boolean ignoreNextSubLayout;
   private boolean isTouchSupported;
   private boolean clickOnTouch;

   private TCScreen screen;
   private int screenXOffset;
   private int screenYOffset;
   public int screenWidth;
   public int screenHeight;
   private int screenXRes;
   private int screenYRes;
   private int screenXShift;
   private int screenYShift;
   public boolean screenResizePending = true; // force creation of mainWindowPixels bitmap
   
   //$IF,OSVERSION,>=,460
   public CameraScreen cameraScreen;
   private Field viewFinder;
   //$END

   static Hashtable htLoadedFonts = new Hashtable(13);
   static UserFont lastUF;
   static Font lastF;

   private static Logger logger = Logger.getLogger("totalcross");

   public static String vmPath;
   public static String appPath;
   public static String tempPath;
   public static String mainWindowPath;
   private boolean activated=true;
   
   /**
    * Constructs a new instance of Launcher
    * @param className the main class name
    * @param applicationId the application identifier
    */
   public Launcher4B(UiApplication stub, String className, String applicationId, String commandLine)
   {
      this.stub = stub;
      this.className = className;
      this.commandLine = commandLine;
      
      appData = new ApplicationData(applicationId);

      // Get module name
      moduleName = ApplicationDescriptor.currentApplicationDescriptor().getModuleName();
      int idx;
      while ((idx = moduleName.indexOf('$')) >= 0)
      {
         String hex = moduleName.substring(idx + 1, idx + 3);
         moduleName = moduleName.substring(0, idx) + ((char)Integer.parseInt(hex, 16)) + moduleName.substring(idx + 3);
      }
   }
   
   /**
    * Initialize this application by creating a new instance of the main application class.
    * @throws ClassNotFoundException if main application class was not found
    * @throws IllegalAccessException if can't access main application class
    * @throws InstantiationException if can't instantiate main application class
    *
    */
   public void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException
   {
      // Load settings
      fillSettings();
      
      vmPath = Convert.normalizePath(Settings.vmPath);
      if (!vmPath.endsWith("/"))
         vmPath += "/";
      Settings.vmPath = vmPath;

      appPath = Convert.normalizePath(Settings.appPath);
      if (!appPath.endsWith("/"))
         appPath += "/";
      Settings.appPath = appPath;

      tempPath = Convert.appendPath(vmPath, "temp/");

      // Root directory
      logger.info("Setting TotalCross root folder to '" + vmPath + "'");
      File f = new File(vmPath);
      if (!f.exists())
         f.createDir();
      f.close();

      // Application directory
      logger.info("Setting application root folder to '" + appPath + "'");
      f = new File(appPath);
      if (!f.exists())
         f.createDir();
      f.close();

      // Temporary files directory
      logger.info("Setting TotalCross temporary folder to '" + tempPath + "'");
      f = new File(tempPath);
      if (!f.exists())
         f.createDir();
      f.close();

      //$IF,FULLVERSION
      activated = ras.ActivationClient.getInstance().isActivatedSilent();
      //$END
      Class clazz = Class.forName(className);
      logger.info("Class '" + className + "' loaded");
      
      String path = clazz.getName().replace('.', '/');
      int idx = path.lastIndexOf('/');
      mainWindowPath = idx <= 0 ? "" : path.substring(0, idx);

      // Create screen components
      screen = new TCScreen();
      if (Settings.isFullScreen && Settings.fullScreenPlatforms != null && Settings.fullScreenPlatforms.indexOf(Settings.BLACKBERRY) < 0) // guich@tc120_59
         Settings.isFullScreen = false;
      if (!Settings.isFullScreen) // isFullScreen was set in the main class static initializer
         screen.setTitle(moduleName);
      stub.pushScreen(screen);

      mainWindow = (MainWindow)clazz.newInstance(); // instantiate main window class
      logger.info("Class '" + className + "' instantiated");

      logger.exiting("totalcross.Launcher", "init");
   }

   public static void checkLitebaseAllowed()
   {
      //$IF,FULLVERSION
      if (!ras.ActivationClient.getInstance().isLitebaseAllowed())
         throw new RuntimeException("The key signed with this application does not allow Litebase.");
      //$END
   }

   //$IF,OSVERSION,>=,460
   class CameraScreen extends PopupScreen implements Runnable
   {
      byte[] imageBytes;

      public CameraScreen()
      {
         super(new VerticalFieldManager());
      }

      public void run()
      {
         stub.pushModalScreen(this);
      }
   }
   //$END

   public byte[] initCamera(Player player)
   {
      byte[] bytes = null;
      
      //$IF,OSVERSION,>=,460
      if (stub.isHandlingEvents())
      {
         final VideoControl vc = (VideoControl) player.getControl("VideoControl");
         if (viewFinder == null)
            viewFinder = (Field)vc.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, "net.rim.device.api.ui.Field");
   
         //flsobral@tc120_11: fixed video control being initialized twice and layout of camera window.
         if (cameraScreen == null)
         {
            cameraScreen = new CameraScreen();
            cameraScreen.add(viewFinder);
            HorizontalFieldManager hfm = new HorizontalFieldManager();
            cameraScreen.add(hfm);
            ButtonField bOk = new ButtonField("  Ok   ");
            ButtonField bCancel = new ButtonField("Cancel");
            hfm.add(bOk);
            hfm.add(bCancel);
            bOk.setChangeListener(new FieldChangeListener()
            {
               public void fieldChanged(Field arg0, int arg1)
               {
                  try
                  {
                     cameraScreen.imageBytes = vc.getSnapshot("encoding=jpeg&width=640&height=480&quality=normal");
                  }
                  catch (MediaException e)
                  {
                  }
                  finally
                  {
                     stub.popScreen(cameraScreen);
                  }
               }
            });
            bCancel.setChangeListener(new FieldChangeListener()
            {
               public void fieldChanged(Field arg0, int arg1)
               {
                  stub.popScreen(cameraScreen);
               }
            });
         }
   
         stub.invokeAndWait(cameraScreen);
         bytes = cameraScreen.imageBytes;
      }
      //$END
      return bytes;
   }

   /**
    * Starts this application and set this thread as the event
    * dispatcher thread
    */
   public void start()
   {
      logger.entering("totalcross.Launcher", "start", "");

      if (mainWindow instanceof MainClass && !(mainWindow instanceof MainWindow))
         logger.info("Non-GUI application; skipping start process");
      else if (!started)
      {
         logger.info("Starting event thread");
         eventThread = new TCEventThread(mainWindow);
         
         new Thread()
         {
            public void run()
            {
               while (!stub.isHandlingEvents())
                  Thread.yield();
               int demoTime = -1;
               if (!activated)
                  demoTime = -999999;
               //$IF,DEMOVERSION
               else
               {
                  logger.info("Starting demo controller");
                  DemoController dc = new DemoController(5000); // sleeps 5 seconds before updating demo time
                  long current = dc.getAvailableTime();
                  int hours = (int)(current / 3600);
                  int mins = (int)((current % 3600) / 60);
                  demoTime = hours*100+mins;
                  demoThread = new Thread(dc);
                  demoThread.setPriority(Thread.MIN_PRIORITY);
                  demoThread.start();
               }
               //$END
               logger.info("Starting application");
               mainWindow.appStarting(demoTime);
               started = true;
            }
         }.start();
      }

      logger.exiting("totalcross.Launcher", "start");
   }

   /**
    * Terminates this application
    * @param code the exit code
    */
   public void exit(int code)
   {
      //alert("blockLoaded: "+totalcross.io.File4B.blockLoaded);
      logger.entering("totalcross.Launcher", "exit", Integer.toString(code));

      if (started)
      {
         //$IF,DEMOVERSION
         logger.info("Stopping demo controller");
         if (demoThread.isAlive())
         {
            demoThread.interrupt();
            try
            {
               demoThread.join();
            }
            catch (InterruptedException ex) { }
         }
         //$END

         logger.info("Exiting application");
         mainWindow.appEnding();

         logger.info("Stopping timer");
         winTimer.stopGracefully(); // timer must be running when appEnding is called

         logger.info("Saving application data");
         appData.appSettings = Settings.appSettings;
         appData.appSettingsBin = Settings.appSettingsBin;
         appData.appSecretKey = Settings.appSecretKey;
         appData.save();
      }

      logger.info("Finalizing objects");
      runFinalizes();

      if (rebootWhenExit)
         Device.requestPowerOff(true);

      logger.exiting("totalcross.Launcher", "exit");
      System.exit(code);
   }

   public void minimize()
   {
      stub.requestBackground();
   }

   public void restore()
   {
      stub.requestForeground();
   }

   public void updateScreen(int transitionEffect)
   {
      if (screen != null)
      {
         synchronized (UiApplication.getEventLock())
         {
            if (transitionEffect == -1)
               transitionEffect = totalcross.ui.Container.TRANSITION_NONE;
            Graphics g = screen.getGraphics();
            g.clear();

            screen.paint(g, transitionEffect);
         }
      }
   }

   public static void print(String s)
   {
      System.err.println(s);
   }

   public UserFont getFont(totalcross.ui.font.Font f, char c)
   {
      UserFont uf=null;

      // verify if its in the cache.
      String fontName = f.name;
      int size = Math.max(f.size,totalcross.ui.font.Font.MIN_FONT_SIZE); // guich@tc122_15: don't check for the maximum font size here
      char faceType = c < 0x3000 && f.style == 1 ? 'b' : 'p';
      int uIndex = ((int)c >> 8) << 8;
      String suffix = "$"+faceType+size+"u"+uIndex;
      String key = fontName+suffix;
      uf = (UserFont)htLoadedFonts.get(key);
      if (uf != null)
         return uf;

      // first, try to load the font itself using the current font pattern
      uf = loadUF(fontName, suffix);
      if (uf == null) // try now as a plain font
         uf = loadUF(fontName, "$p"+size+"u"+uIndex); // guich@tc122_15: ... check only here
      if (uf == null && f.size != totalcross.ui.font.Font.NORMAL_SIZE)
      {
         int t = f.size;
         while (uf == null && --t >= 5) // try to find the nearest size
            uf = loadUF(fontName, "$p"+t+"u"+uIndex);
      }
      if (uf == null) // try now as the default size with desired face
         uf = loadUF(fontName, "$"+faceType+totalcross.ui.font.Font.NORMAL_SIZE+"u"+uIndex);
      if (uf == null && faceType != 'p') // try now as the default size plain font
         uf = loadUF(fontName, "$p"+totalcross.ui.font.Font.NORMAL_SIZE+"u"+uIndex);
      // at last, use the default font
      if (uf == null)
         uf = loadUF(totalcross.ui.font.Font.DEFAULT, suffix);
      if (uf == null) // check if there's a font of any size - maybe the file has only one font?
         for (int i = totalcross.ui.font.Font.MIN_FONT_SIZE; i <= totalcross.ui.font.Font.MAX_FONT_SIZE; i++)
            if ((uf = loadUF(fontName,"$p"+i+"u"+uIndex)) != null)
               break;
      if (uf == null) // check if there's a font of any size - at least with the default font
         for (int i = totalcross.ui.font.Font.MIN_FONT_SIZE; i <= totalcross.ui.font.Font.MAX_FONT_SIZE; i++)
            if ((uf = loadUF(totalcross.ui.font.Font.DEFAULT,"$p"+i+"u"+uIndex)) != null)
               break;

      if (uf != null)
      {
         htLoadedFonts.put(key,uf); // note that we will use the original key to avoid entering all exception handlers.
         f.name = uf.fontName; // update the name, the font may have been replaced.
      }
      else if (htLoadedFonts.size() > 0)
         return getFont(f, ' '); // probably the index was outside the available ranges at this font

      if (uf == null)
      {
         String s = "No fonts found! be sure to place the file "+totalcross.ui.font.Font.DEFAULT+".tcz inside your application's directory.";
         logger.warning(s);
         throw new RuntimeException(s);
      }

      return uf;
   }

   private UserFont loadUF(String fontName, String suffix)
   {
      UserFont uf = null;

      if (fontName.charAt(0) == '$') // bruno@tc114_37: native fonts always start with '$'
         uf = new UserFont(fontName, suffix);
      else
      {
         String path;
         if (Settings.dataPath != null)
         {
            path = Convert.appendPath(Settings.dataPath, fontName + ".tcz");
            try
            {
               uf = new UserFont(new File(path, File.READ_WRITE), fontName, suffix);
            }
            catch (FileNotFoundException e) {} // do not log if file does not exist
            catch (Exception e)
            {
               logger.warning("Failed to load font '" + fontName + suffix + " from file '" + path + "'; reason: " + e.getMessage());
            }
         }
         if (uf == null && Launcher4B.vmPath != null && !Launcher4B.vmPath.equals(Settings.dataPath))
         {
            path = Convert.appendPath(Launcher4B.vmPath, fontName + ".tcz");
            try
            {
               uf = new UserFont(new File(path, File.READ_WRITE), fontName, suffix);
            }
            catch (FileNotFoundException e) {} // do not log if file does not exist
            catch (Exception e)
            {
               logger.warning("Failed to load font '" + fontName + suffix + " from file '" + path + "'; reason: " + e.getMessage());
            }
         }
         if (uf == null)
         {
            try
            {
               String res = searchResource(fontName + ".tcz");
               if (res != null)
               {
                  byte[] b = Vm.getFile(res);
                  if (b != null)
                     uf = new UserFont(new ByteArrayStream(b), fontName, suffix);
               }
            }
            catch (Exception e)
            {
               logger.warning("Failed to load font '" + fontName + suffix + " from resources; reason: " + e.getMessage());
            }
         }

         if (uf == null)
            logger.warning("Font '" + fontName + "' not found!");
      }

      return uf;
   }

   public static byte[] readStream(Stream in) throws IOException
   {
      return readStream(in, -1);
   }

   public static byte[] readStream(Stream in, int size) throws IOException
   {
      Chunk curr = new Chunk(null);
      int r, total = 0, pos = 0;

      while ((size == -1 || total < size) && (r = in.readBytes(curr.data, pos, Chunk.CHUNK_SIZE)) > 0)
      {
         pos += r;
         total += r;

         if (pos == Chunk.CHUNK_SIZE)
         {
            Chunk newChunk = new Chunk(curr);
            curr = newChunk;
            pos = 0;
         }
      }

      if (total == 0)
         return null;
      else
      {
         byte[] data = new byte[total];
         int len = pos;
         pos = total - pos;

         System.arraycopy(curr.data, 0, data, pos, len); // last chunk
         curr = curr.prev;

         while (curr != null)
         {
            pos -= Chunk.CHUNK_SIZE;
            System.arraycopy(curr.data, 0, data, pos, Chunk.CHUNK_SIZE);
            curr = curr.prev;
         }

         return data;
      }
   }

   // Used by SSL4B to read all available data from a input stream without
   // blocking forever. This behavior is the same as in the berkeley socket
   // model, where the read blocks until at least one byte is read (or the
   // operation times out), and then read all available bytes and return.
   public static byte[] readNonBlocking(InputStream is, int timeout) throws java.io.IOException, SocketTimeoutException
   {
      ReadResult res = readNonBlockingInternal(is, timeout, null, 0, Integer.MAX_VALUE); // try to read as many bytes as possible
      int result = res.result;
      byte[] data = res.data;

      if (result == 0)
         data = new byte[0];
      else if (result > 0 && result < data.length) // if buffer is not full, shrink it
      {
         byte[] temp = new byte[result];
         System.arraycopy(data, 0, temp, 0, result);
         data = temp;
      }

      return data;
   }

   // Used by Socket4B to read data from a input stream without blocking
   // forever. The behavior is the same as in the berkeley socket model, where
   // the read blocks until at least one byte is read (or the operation times
   // out), and then read all available bytes up to "count" and return.
   public static int readNonBlocking(InputStream is, int timeout, byte[] buf, int start, int count) throws java.io.IOException, SocketTimeoutException
   {
      return readNonBlockingInternal(is, timeout, buf, start, count).result;
   }

   //flsobral@tc115_53: Reverted this change.
   private static ReadResult readNonBlockingInternal(final InputStream is, int timeout, byte[] buf, int start, int count) throws java.io.IOException, SocketTimeoutException
   {
      final java.io.IOException[] innerEx = new java.io.IOException[1];
      final ReadResult res = new ReadResult();
      final byte[] oneByte = new byte[1];

      Thread reader = new Thread()
      {
         public void run()
         {
            try
            {
               res.result = is.read(oneByte, 0, 1); // read first byte, only
            }
            catch (InterruptedIOException ex) // forced timeout (thread interrupted)
            {
               res.result = ex.bytesTransferred;
            }
            catch (java.io.IOException ex)
            {
               String msg = ex.getMessage();
               if (msg != null && msg.indexOf("timed out") >= 0) // read timed out
                  res.result = 0;
               else
                  innerEx[0] = ex;
            }
            catch (Exception ex)
            {
               innerEx[0] = new java.io.IOException(ex.toString());
            }
         }
      };

      reader.start();

      long t = System.currentTimeMillis();
      while (reader.isAlive() && (System.currentTimeMillis() - t) <= timeout) // wait until the read finishes or a timeout occurs
      {
         try
         {
            Thread.sleep(100);
         }
         catch (InterruptedException ex) {}
      }

      if (reader.isAlive()) // reader didn't finish yet, but a timeout has occurred, so interrupt reader thread
      {
         reader.interrupt();
         try
         {
            reader.join(); // wait until the reader thread finishes
         }
         catch (InterruptedException ex) {}
      }

      if (innerEx[0] != null) // Exception inside reader
         throw innerEx[0];
      else if (res.result == 0) // Timeout
         throw new SocketTimeoutException("Read timed out");
      else if (res.result == 1)
      {
         count--;
         if (count > 0)
         {
            int available = is.available();
            
            if (available < 0)
               count = 0;
            else if (available < count)
               count = available;
         }
         
         if (buf == null) // create buffer, if necessary
         {
            buf = new byte[count + 1];
            start = 0;
         }
         
         buf[start++] = oneByte[0];
         int r = count <= 0 ? 0 : is.read(buf, start, count);
         
         if (r < 0) // read failed
            r = 0;

         res.data = buf;
         res.result = r + 1;
      }

      return res;
   }

   public String[] getResourcesList() throws IOException
   {
      if (resourcesList == null)
      {
         Vector v = new Vector(), modules = new Vector();
         listResources(moduleName, v, modules);

         int vSize = v.size();
         resourcesList = new String[vSize];
         System.arraycopy(v.items, 0, resourcesList, 0, vSize);
      }

      return resourcesList;
   }

   private void listResources(String module, Vector v, Vector modules) throws IOException
   {
      logger.info("Retrieving " + module + " resources");
      modules.addElement(module);

      byte[] b = Vm.getFile("/" + module + ".resources");
      if (b != null)
      {
         DataStream ds = new DataStream(new ByteArrayStream(b));

         int count = ds.readInt();
         for (int i = 0; i < count; i ++)
         {
            String res = ds.readString();
            v.addElement(res);
         }

         count = ds.readInt();
         for (int i = 0; i < count; i ++)
         {
            module = ds.readString();
            if (modules.indexOf(module) < 0)
               listResources(module, v, modules);
         }
      }
   }

   public String searchResource(String name) throws IOException
   {
      String path = null;

      String[] list = getResourcesList();
      if (list != null)
      {
         for (int i = list.length - 1; i >= 0; i--)
         {
            String res = list[i];
            int idx = res.lastIndexOf('/');
            if (idx >= 0)
               res = res.substring(idx + 1);

            if (res.equals(name))
            {
               path = list[i];
               break;
            }
         }
      }

      logger.info("Resource '" + name + "' " + (path != null ? "found at '" + path + "'" : "not found"));
      return path;
   }

   /**
    * Called by the main window to register itself
    * @param mainWindow the main window
    */
   public void registerMainWindow(MainWindow mainWindow)
   {
      this.mainWindow = mainWindow;
      winTimer = new WinTimer();
      winTimer.start();
   }

   public void setTitle(String title)
   {
      synchronized (UiApplication.getEventLock())
      {
         screen.setTitle(title);
      }
   }

   public void setSIP(int option, Control edit, boolean secret)
   {
      if (isTouchSupported)
      {
         if ((option & Window.SIP_BOTTOM) == Window.SIP_BOTTOM || (option & Window.SIP_TOP) == Window.SIP_TOP || (option & Window.SIP_SHOW) == Window.SIP_SHOW)
         {
            sipControl = edit;
            setSIPVisible(true, true);
         }
         else if ((option & Window.SIP_HIDE) == Window.SIP_HIDE)
         {
            sipControl = null;
            setSIPVisible(false, true);
         }
      }
   }

   private void setSIPVisible(boolean visible, boolean ignoreNextSubLayout)
   {
      //$IF,OSVERSION,>=,470
      isSipVisible = visible;
      if (!isHandlingTouch)
      {
         int visibility = visible ? VirtualKeyboard.SHOW : VirtualKeyboard.HIDE;
         
         VirtualKeyboard keyboard = screen.getVirtualKeyboard();
         if (keyboard.getVisibility() != visibility)
         {
            this.ignoreNextSubLayout = ignoreNextSubLayout;
            keyboard.setVisibility(visibility);
         }
         else
         {
            int oldScreenXShift = screenXShift;
            int oldScreenYShift = screenYShift;
            
            updateScreenParams();
            
            if (oldScreenXShift != screenXShift || oldScreenYShift != screenYShift)
               updateScreen(Window.TRANSITION_NONE);
         }
      }
      //$END
   }
   
   private void updateScreenParams()
   {
      synchronized (UiApplication.getEventLock())
      {
         XYRect rect = screen.getMainManager().getContentRect();
         
         screenXOffset = rect.x;
         screenYOffset = rect.y;
         screenWidth = rect.width;
         screenHeight = rect.height;
         screenXRes = (int)Math.round(Display.getHorizontalResolution() * 0.0254);
         screenYRes = (int)Math.round(Display.getVerticalResolution() * 0.0254);
         
         if (isSipVisible && sipControl != null) // bruno@tc122_36: make sure the edit control does not get overlapped by the virtual keyboard
         {
            Rect r = sipControl.getAbsoluteRect();
            int x = r.x - screenXShift, y = r.y - screenYShift, x2 = x + r.width, y2 = y + r.height;
            
            if (x < 0)
               screenXShift += x;
            else if (x2 > screenWidth)
               screenXShift += x2 - screenWidth;
            
            if (y < 0)
               screenYShift += y;
            else if (y2 > screenHeight)
               screenYShift += y2 - screenHeight;
         }
         else // sip is not visible, clear screen shifts
         {
            screenXShift = 0;
            screenYShift = 0;
         }
         
         totalcross.ui.Keypad.Y_ALIGN = totalcross.ui.Keypad.Y_ALIGN_ALT = screenYShift + 3; // show Keypad always on top of the visible portion of the screen
      }
   }
   
   public void alert(final String s)
   {
      Runnable run = new Runnable()
      {
         public void run()
         {
            Dialog.alert(s);
         }
      };

      if (!stub.isHandlingEvents())
         alertQueue.push(run);
      else
         stub.invokeAndWait(run);
   }

   /**
    * Retrieves all device settings and map them to totalcross.sys.Settings
    */
   public void fillSettings()
   {
      // Date and time settings
      java.util.Calendar cal = java.util.Calendar.getInstance();
      cal.set(java.util.Calendar.YEAR, 2002);
      cal.set(java.util.Calendar.MONTH, 11);
      cal.set(java.util.Calendar.DAY_OF_MONTH, 25);
      cal.set(java.util.Calendar.HOUR_OF_DAY, 20);

      DateFormat df = DateFormat.getInstance(DateFormat.DATE_SHORT);
      String s = df.format(cal.getTime());
      Settings.dateFormat = s.startsWith("25") ? totalcross.sys.Settings.DATE_DMY
            : s.startsWith("12") ? totalcross.sys.Settings.DATE_MDY
            : totalcross.sys.Settings.DATE_YMD;
      Settings.dateSeparator = s.charAt(2);
      Settings.weekStart = (byte) (java.util.Calendar.SUNDAY - 1);

      df = DateFormat.getInstance(DateFormat.TIME_SHORT);
      s = df.format(cal.getTime());
      Settings.is24Hour = s.startsWith("20");
      Settings.timeSeparator = Settings.is24Hour ? s.charAt(2) : s.charAt(1);

      settingsRefresh(false);
      // Numbers and currency settings
      Settings.thousandsSeparator = ',';
      Settings.decimalSeparator = '.';

      // Device settings
      Settings.uiStyle = Settings.WinCE;
      Settings.screenWidth = Display.getWidth();
      Settings.screenHeight = Display.getHeight();
      
      Settings.platform = Settings.BLACKBERRY;
      Settings.applicationId = appData.applicationId;
      
      //$IF,OSVERSION,>=,470
      isTouchSupported = Touchscreen.isSupported();
      clickOnTouch = DeviceInfo.getDeviceName().startsWith("98"); // emulate click on Torch 98xx
      //$END
      Settings.fingerTouch = Settings.virtualKeyboard = isTouchSupported;
      Settings.keyboardFocusTraversable = !isTouchSupported;
      
      Settings.romVersion = convertVersion(DeviceInfo.getSoftwareVersion());
      Settings.romSerialNumber = Integer.toString(DeviceInfo.getDeviceId());
      Settings.deviceId = DeviceInfo.getDeviceName();
      Settings.userName = OwnerInfo.getOwnerName();
      Settings.timeZoneStr = java.util.TimeZone.getDefault().getID(); //flsobral@tc115_54: added field Settings.timeZoneStr

      // Get network attributes
      int network = RadioInfo.getNetworkType();
      if (network == RadioInfo.NETWORK_GPRS || network == RadioInfo.NETWORK_UMTS)
         Settings.imei = GPRSInfo.imeiToString(GPRSInfo.getIMEI(), false);
      else if (network == RadioInfo.NETWORK_CDMA)
         Settings.esn = Integer.toString(CDMAInfo.getESN());

      // Application settings
      ApplicationData appData = new ApplicationData(Settings.applicationId);
      Settings.appSettings = appData.appSettings;
      Settings.appSettingsBin = appData.appSettingsBin;
      Settings.appSecretKey = appData.appSecretKey;
      Settings.closeButtonType = Settings.CLOSE_BUTTON;

      // Path settings
      Settings.vmPath = "/store/home/user/totalcross/";
      Settings.appPath = "/store/home/user/totalcross/" + Settings.applicationId + "/";
      //Settings.dataPath = Settings.vmPath;  - guich@tc112_21
   }

   private static int convertVersion(String v) // 4.7.0.75 -> 470
   {
      int d1 = v.indexOf('.',0);
      int d2 = v.indexOf('.',d1+1);
      int d3 = v.indexOf('.',d2+1);
      if (d3 == -1) d3 = v.length();
      v = v.substring(0,d1)+v.substring(d1+1,d2)+v.substring(d2+1,d3);
      try
      {
         return Convert.toInt(v);
      }
      catch (InvalidNumberException ine)
      {
         return 0;
      }
   }

   public void settingsRefresh(boolean callStoreSettings) // guich@tc115_81
   {
      java.util.TimeZone tz = java.util.TimeZone.getDefault(); // guich@340_33
      Settings.daylightSavingsMinutes = tz.getDSTSavings() / 60000;
      Settings.daylightSavings = Settings.daylightSavingsMinutes != 0;
      Settings.timeZone = tz.getRawOffset() / (60*60000);
      Settings.timeZoneMinutes = tz.getRawOffset() / 60000;
      Settings.timeZoneStr = java.util.TimeZone.getDefault().getID();
      if (callStoreSettings)
         try
         {
            appData.save();
         }
         catch (Exception e) {}
   }

   public boolean eventIsAvailable()
   {
      return eventThread.eventAvailable();
   }

   public void pumpEvents()
   {
      eventThread.pumpEvents();
   }

   public void setTimerInterval(int milliseconds)
   {
      winTimer.setInterval(milliseconds);
   }

   /**
    * This is the main entry point. This method will always receive
    * the classname and the application id (args[0] and args[1] respectively)
    */
   public static void run(UiApplication stub, String className, String applicationId, String commandLine)
   {
      try
      {
         instance = new Launcher4B(stub, className, applicationId, commandLine);
         instance.init();
         instance.start();
      }
      catch (final Throwable t)
      {
         logger.throwing("Launcher", "run", t);
         
         stub.invokeLater(new Runnable()
         {
            public void run()
            {
               Dialog.alert("Caught unhandled exception " + t.getClass().getName() + " in application initialization.");
               System.exit(1);
            }
         });
      }
      finally
      {
         stub.enterEventDispatcher();
      }
   }

   private class TCScreen extends MainScreen
   {
      private int keypadLayout;
      private boolean isQwerty;
      private int symbolsTriggerChar;
      private byte[] lock = new byte[] {0};
      private boolean leftConvPressed;
      private boolean rightConvPressed;
      
      private static final int MENU_LAUNCHER_DELAY = 1000;
      private Thread menuLauncher;
      private Runnable menuLauncherRunnable = new Runnable()
      {
         public void run()
         {
            try
            {
               Thread.sleep(MENU_LAUNCHER_DELAY);

               synchronized (lock)
               {
                  if (lock[0] == 1)
                  {
                     lock[0] = 0;
                     eventThread.pushEvent(KeyEvent.SPECIAL_KEY_PRESS, SpecialKeys.MENU, 0, 0, 0, 0);
                  }
               }
            }
            catch (InterruptedException e) { }
         }
      };

      protected boolean keyDown(int keyCode, int time)
      {
         int key = Keypad.key(keyCode);
         int mod = Keypad.status(keyCode);
         
         if (key == Keypad.KEY_CONVENIENCE_2)
         {
            leftConvPressed = true;
            return true;
         }
         else if (key == Keypad.KEY_CONVENIENCE_1)
         {
            rightConvPressed = true;
            return true;
         }
         else if (key == Keypad.KEY_END)
         {
            if (leftConvPressed && (leftConvPressed ^ rightConvPressed))
            {
               if (Dialog.ask(Dialog.D_YES_NO, "Killing this application may result in losing or corrupting data. Do you really want to proceed?", Dialog.NO) == Dialog.YES)
                  System.exit(-1); // bruno@tc122_35: Kill application if LEFT_CONVENIENCE + END is pressed
            }
            else if (Settings.closeButtonType == Settings.CLOSE_BUTTON)
               exit(0);
            else if (Settings.closeButtonType == Settings.MINIMIZE_BUTTON)
               minimize();
            return true;
         }
         
         if (isQwerty) // if this is a QWERTY device, shows keypad only when pressing the SYM key
            Settings.keypadOnly = key == symbolsTriggerChar;

         if (key != symbolsTriggerChar)
         {
            key = getTCDeviceKey(key, mod);
            mod = getTCModifiers(mod);
         }
         
         if (key == 0) // printable key or special key that is not mapped to TC; do not handle!
            return false;
         else // special key mapped to TC
         {
            if (showKeyCodes)
               alert("Key code: " + key + ", Modifier: " + mod);

            eventThread.pushEvent(KeyEvent.SPECIAL_KEY_PRESS, key, 0, 0, mod, 0); // send first key event
            return true;
         }
      }
      
      protected boolean keyUp(int keyCode, int time)
      {
         int key = Keypad.key(keyCode);

         if (key == Keypad.KEY_CONVENIENCE_2)
         {
            leftConvPressed = false;
            return true;
         }
         else if (key == Keypad.KEY_CONVENIENCE_1)
         {
            rightConvPressed = false;
            return true;
         }
         
         return false;
      }
      
      protected boolean keyChar(char c, int status, int time)
      {
         int mod = getTCModifiers(status);
         mod &= ~SpecialKeys.ALT; // never send ALT with printable chars, since they have already been "alted"
         if (c >= 'A' && c <= 'Z')
            mod &= ~SpecialKeys.SHIFT; // never send SHIFT with upper case chars, since they have already been "shifted"
         
         eventThread.pushEvent(KeyEvent.KEY_PRESS, c, 0, 0, mod, 0); // send first key event
         return true;
      }
      
      protected boolean navigationMovement(int dx, int dy, int status, int time)
      {
         // On devices equipped with a track ball, holding the ALT key and moving the track ball UP, DOWN, LEFT and RIGHT
         // emulates the following special keys, respectively: PAGE_UP, PAGE_DOWN, HOME, END.
         // On devices equipped with a track wheel, holding the ALT key and moving the track wheel UP and DOWN
         // emulates the UP and DOWN movements, respectively. The LEFT and RIGHT movements are achieved by just moving
         // the track wheel UP and DOWN without holding the ALT key.
         
         boolean hasTrackball = (status & KeypadListener.STATUS_FOUR_WAY) == KeypadListener.STATUS_FOUR_WAY;
         boolean isAltPressed = (status & KeypadListener.STATUS_ALT) == KeypadListener.STATUS_ALT;
         
         if (hasTrackball)
         {
            if (dx != 0)
            {
               int key;
               if (dx < 0)
               {
                  key = isAltPressed ? SpecialKeys.HOME : SpecialKeys.LEFT;
                  dx = -dx;
               }
               else
                  key = isAltPressed ? SpecialKeys.END : SpecialKeys.RIGHT;

               while (dx-- > 0)
                  eventThread.pushEvent(KeyEvent.SPECIAL_KEY_PRESS, key, 0, 0, 0, 0);
            }
            if (dy != 0)
            {
               int key;
               if (dy < 0)
               {
                  key = isAltPressed ? SpecialKeys.PAGE_UP : SpecialKeys.UP;
                  dy = -dy;
               }
               else
                  key = isAltPressed ? SpecialKeys.PAGE_DOWN : SpecialKeys.DOWN;

               while (dy-- > 0)
                  eventThread.pushEvent(KeyEvent.SPECIAL_KEY_PRESS, key, 0, 0, 0, 0);
            }
         }
         else // device has only the simple track wheel (which moves UP and DOWN, only)
         {
            int key;
            if (dy < 0)
            {
               key = isAltPressed ? SpecialKeys.UP : SpecialKeys.LEFT;
               dy = -dy;
            }
            else
               key = isAltPressed ? SpecialKeys.DOWN : SpecialKeys.RIGHT;

            while (dy-- > 0)
               eventThread.pushEvent(KeyEvent.SPECIAL_KEY_PRESS, key, 0, 0, 0, 0);
         }

         return true;
      }

      protected boolean navigationClick(int status, int time)
      {
         lock[0] = 1;
         menuLauncher = new Thread(menuLauncherRunnable, "actionOnRelease");
         menuLauncher.start();

         return true;
      }

      protected boolean navigationUnclick(int status, int time)
      {
         synchronized (lock)
         {
            if (lock[0] == 1)
            {
               lock[0] = 0;
               menuLauncher.interrupt();
               eventThread.pushEvent(KeyEvent.SPECIAL_KEY_PRESS, SpecialKeys.ACTION, 0, 0, 0, 0);
            }
         }

         return true;
      }

      //$IF,OSVERSION,>=,470
      private Coord ptPenDown = new Coord(), ptClick = new Coord();
      private boolean isPenDownOnTouch, isPenDownOnClick, isClicking, isDragging;
      
      protected boolean touchEvent(TouchEvent event)
      {
         int x = event.getX(1) + screenXShift - screenXOffset;
         int y = event.getY(1) + screenYShift - screenYOffset;
         
         if (x < 0)
            x = 0;
         else if (x > screenWidth)
            x = screenWidth;
         if (y < 0)
            y = 0;
         else if (y > screenHeight)
            y = screenHeight;
         
         Window w;
         
         switch (event.getEvent())
         {
            case TouchEvent.DOWN:
               isHandlingTouch = true;
               ptPenDown.x = x;
               ptPenDown.y = y;
               if (clickOnTouch || Flick.currentFlick != null) // if we are flicking, send a PEN_DOWN to stop it
               {
                  eventThread.pushEvent(PenEvent.PEN_DOWN, 0, x, y, 0, 0);
                  isPenDownOnTouch = true;
               }
               else // otherwise, just highlight the control under (x, y)
               {
                  w = Window.getTopMost();
                  w.setFocus(w.findNearestChild(x - w.getX(), y - w.getY(), Settings.touchTolerance));
                  isPenDownOnTouch = false;
               }
               break;
               
            case TouchEvent.UP:
               isDragging = false;
               if (isPenDownOnTouch)
               {
                  eventThread.pushEvent(PenEvent.PEN_UP, 0, x, y, 0, 0);
                  isPenDownOnTouch = false;
               }
               isHandlingTouch = false;
               setSIPVisible(isSipVisible, true);
               break;
               
            case TouchEvent.CLICK:
               if (!isPenDownOnTouch)
               {
                  ptClick.x = x;
                  ptClick.y = y;
                  eventThread.pushEvent(PenEvent.PEN_DOWN, 0, x, y, 0, 0);
                  isPenDownOnClick = true;
               }
               isClicking = true;
               break;
               
            case TouchEvent.UNCLICK:
               if (isPenDownOnClick)
               {
                  eventThread.pushEvent(PenEvent.PEN_UP, 0, ptClick.x, ptClick.y, 0, 0); // use original click point as PEN_UP, since we assume a click is static
                  isPenDownOnClick = false;
               }
               isClicking = false;
               break;
            
            case TouchEvent.MOVE:
               if (!isClicking) // move is only allowed if not clicking
               {
                  if (!isDragging) // dragging has not started yet, check if threshold was exceeded
                  {
                     int xDelta = x > ptPenDown.x ? x - ptPenDown.x : ptPenDown.x - x;
                     int yDelta = y > ptPenDown.y ? y - ptPenDown.y : ptPenDown.y - y;
                     isDragging = xDelta > Window.dragThreshold || yDelta > Window.dragThreshold;
                     if (isDragging && !isPenDownOnTouch)
                     {
                        eventThread.pushEvent(PenEvent.PEN_DOWN, 0, ptPenDown.x, ptPenDown.y, 0, 0);
                        isPenDownOnTouch = true;
                     }
                  }
                  if (isDragging)
                     eventThread.pushEvent(PenEvent.PEN_DRAG, 0, x, y, 0, 0);
               }
               break;
         }

         return true;
      }
      //$END

      protected void sublayout(int width, int height)
      {
         super.sublayout(width, height);
         
         updateScreenParams();
         if (started)
            updateKeypad();
         
         if (ignoreNextSubLayout)
            ignoreNextSubLayout = false;
         else if (isSipVisible)
            setSIPVisible(false, false);
         else if (screenWidth != Settings.screenWidth || screenHeight != Settings.screenHeight) // guich@tc126_2: allow user to disable screen rotation support
         {
            synchronized (UiApplication.getEventLock())
            {
               Settings.screenWidth = screenWidth;
               Settings.screenHeight = screenHeight;
               Settings.screenWidthInDPI = screenXRes;
               Settings.screenHeightInDPI = screenYRes;
               screenResizePending = true;
               
               if (started)
                  eventThread.pushEvent(KeyEvent.SPECIAL_KEY_PRESS, SpecialKeys.SCREEN_CHANGE, 0, 0, 0, 0);
            }
         }
      }

      private void updateKeypad()
      {
         int layout = Keypad.getHardwareLayout();
         if (layout != keypadLayout)
         {
            String[] keys = null;
            String symbolKeys = null;
            symbolsTriggerChar = -1;
            
            switch (layout)
            {
               case Keypad.HW_LAYOUT_32:
               case Keypad.HW_LAYOUT_39:
               case Keypad.HW_LAYOUT_LEGACY:
               case Keypad.HW_LAYOUT_PHONE:
                  symbolsTriggerChar = 127;
                  symbolKeys = "[<{~=\"^%]>}\\`&$|";
               //$IF,OSVERSION,>=,470
               case Keypad.HW_LAYOUT_TOUCHSCREEN_29:
               //$END
                  isQwerty = true;
                  break;
                  
               case Keypad.HW_LAYOUT_REDUCED:
               case Keypad.HW_LAYOUT_REDUCED_24:
                  symbolsTriggerChar = 20;
                  symbolKeys = "[<{~=\"^%]>}\\`&$|";
               //$IF,OSVERSION,>=,470
               case Keypad.HW_LAYOUT_TOUCHSCREEN_24:
               //$END
                  isQwerty = false;
                  keys = getReducedKeypadKeys();
                  break;
               
               //$IF,OSVERSION,>=,470
               case Keypad.HW_LAYOUT_TOUCHSCREEN_12:
                  isQwerty = false;
                  keys = getDialPadKeys();
                  break;
               //$END
            }
            
            totalcross.ui.Keypad.defaultKeyset = keys;
            totalcross.ui.Keypad.numberKeyset = keys;
            totalcross.ui.Keypad.defaultSymbolKeyset = symbolKeys;

            totalcross.ui.Keypad.firstChar = 0;
            totalcross.ui.Keypad.lastChar = 255;
            totalcross.ui.Keypad.backspaceChar = SpecialKeys.BACKSPACE;
            totalcross.ui.Keypad.symbolsTriggerChar = symbolsTriggerChar;

            totalcross.ui.Keypad.getInstance().setKeys(null); // use default keys
            totalcross.ui.Keypad.getInstance().setSymbolKeys(null); // use default symbol keys
            
            Settings.keypadOnly = !isQwerty;
            keypadLayout = layout;
         }
      }
      
      //$IF,OSVERSION,>=,470
      private String[] getDialPadKeys()
      {
         String[] keys = new String[256];
         keys['.'] = ".,!?";
         keys['a'] = "abc";
         keys['A'] = "ABC";
         keys['d'] = "def";
         keys['D'] = "DEF";
         keys['g'] = "ghi";
         keys['G'] = "GHI";
         keys['j'] = "jkl";
         keys['J'] = "JKL";
         keys['m'] = "mno";
         keys['M'] = "MNO";
         keys['p'] = "pqrs";
         keys['P'] = "PQRS";
         keys['t'] = "tuv";
         keys['T'] = "TUV";
         keys['w'] = "wxyz";
         keys['W'] = "WXYZ";
         keys[':'] = ":;()";
         
         return keys;
      }
      //$END
      
      private String[] getReducedKeypadKeys()
      {
         String[] keys = new String[256];
         keys['q'] = "qw";
         keys['e'] = "er";
         keys['t'] = "ty";
         keys['u'] = "ui";
         keys['o'] = "op";
         keys['a'] = "as";
         keys['d'] = "df";
         keys['g'] = "gh";
         keys['j'] = "jk";
         keys['z'] = "zx";
         keys['c'] = "cv";
         keys['b'] = "bn";
         keys['Q'] = "QW";
         keys['E'] = "ER";
         keys['T'] = "TY";
         keys['U'] = "UI";
         keys['O'] = "OP";
         keys['A'] = "AS";
         keys['D'] = "DF";
         keys['G'] = "GH";
         keys['J'] = "JK";
         keys['Z'] = "ZX";
         keys['C'] = "CV";
         keys['B'] = "BN";
         
         if (!Keypad.isOnKeypad('+'))
            keys['0'] = "0+";
         
         return keys;
      }

      protected void onExposed()
      {
         Window.enableUpdateScreen = true;
         updateScreen(totalcross.ui.Container.TRANSITION_NONE);
      }

      protected void onObscured()
      {
         Window.enableUpdateScreen = false;
      }

      public void close()
      {
         // bruno@tc115_42: never close the app screen unless System.exit is called
      }

      protected void paint(Graphics g)
      {
         paint(g, totalcross.ui.Container.TRANSITION_NONE);
      }
      
      protected void paint(Graphics g, int transitionEffect) // guich@tc126_18: add transition effects
      {
         super.paint(g);
         if (started && !screenResizePending) // avoid half-painted screen when showing the pending alerts
         {
/*            switch (transitionEffect)
            {
               case totalcross.ui.Container.TRANSITION_NONE:
               {
*/
                  g.drawBitmap(screenXOffset, screenYOffset, screenWidth, screenHeight, (Bitmap)Graphics4B.mainWindowPixels, screenXShift, screenYShift);
                  screen.updateDisplay();
/*                  break;
               }
               case totalcross.ui.Container.TRANSITION_CLOSE:
               case totalcross.ui.Container.TRANSITION_OPEN:
               {
                  // NOT WORKING CORRECTLY: the old bitmap is cleared and this bitmap is displayed against a white background. to fix this, we would have to save the old bitmap and merge it someway. 
                  try
                  {
                     final int step = 4;
                     int w = screenWidth;
                     int h = screenHeight;
                     int n = Math.min(w,h)/2;
                     int mx = w/2;
                     int my = h/2;
                     int incX=step,incY=step;
                     if (w > h)
                        incX = step*w/h + 1;
                      else
                        incY = step*h/w + 1;
                     int i0 = transitionEffect == totalcross.ui.Container.TRANSITION_CLOSE ? n : 1;
                     int iinc = transitionEffect == totalcross.ui.Container.TRANSITION_CLOSE ? -1 : 1;
                     n = n / step + 1;
                     i0 /= step;
                     for (int i =i0; --n >= 0; i+=iinc)
                     {
                        int minx = (int)(mx - i*incX);
                        int miny = (int)(my - i*incY);
                        int maxx = (int)(mx + i*incX);
                        int maxy = (int)(my + i*incY);
                        drawImageLine(g,minx-step,miny-step,maxx+step,miny+step);
                        drawImageLine(g,minx-step,miny-step,minx+step,maxy+step);
                        drawImageLine(g,maxx-step,miny-step,maxx+step,maxy+step);
                        drawImageLine(g,minx-step,maxy-step,maxx+step,maxy+step);
                        updateDisplay();
                     }
                  }
                  catch (Exception e) // if an exception occurs, we just draw the final bitmap
                  {
                     g.drawBitmap(screenXOffset, screenYOffset, screenWidth, screenHeight, (Bitmap)Graphics4B.mainWindowPixels, screenXShift, screenYShift);
                     screen.updateDisplay();
                  }
                  break;
               }
            }
*/         }
      }
      
/*      private void drawImageLine(Graphics g, int minx, int miny, int maxx, int maxy)
      {
         if (minx < 0) minx = 0;
         if (miny < 0) miny = 0;
         if (maxx > screenWidth) maxx = screenWidth;
         if (maxy > screenHeight) maxy = screenHeight;
         g.drawBitmap(screenXOffset+minx, screenYOffset+miny, maxx-minx, maxy-miny, (Bitmap)Graphics4B.mainWindowPixels, screenXShift+minx, screenYShift+miny);
      }
*/
      private int getTCDeviceKey(int key, int mod)
      {
         switch (key)
         {
            case Keypad.KEY_BACKSPACE: return SpecialKeys.BACKSPACE;
            case Keypad.KEY_DELETE: return SpecialKeys.DELETE;
            case Keypad.KEY_ENTER: return SpecialKeys.ENTER;
            case Keypad.KEY_ESCAPE: return SpecialKeys.ESCAPE;
            case Keypad.KEY_MENU: return SpecialKeys.MENU;
            default: return 0;
         }
      }

      private int getTCModifiers(int status)
      {
         int modifiers = 0;

         if ((status & KeypadListener.STATUS_ALT) == KeypadListener.STATUS_ALT || (status & KeypadListener.STATUS_ALT_LOCK) == KeypadListener.STATUS_ALT_LOCK)
            modifiers |= SpecialKeys.ALT;
         if ((status & KeypadListener.STATUS_SHIFT) == KeypadListener.STATUS_SHIFT)
            modifiers |= SpecialKeys.SHIFT;

         return modifiers;
      }
   }

   private class WinTimer extends Thread
   {
      private int interval;
      private boolean shouldStop;

      private Runnable timerTick = new Runnable()
      {
         public void run()
         {
            mainWindow._onTimerTick(true);
         }
      };

      public void run()
      {
         long lastTick = System.currentTimeMillis(), sleep;

         while (!shouldStop)
         {
            try
            {
               sleep = interval - (System.currentTimeMillis() - lastTick);
               if (sleep > 0)
                  Thread.sleep(sleep);

               lastTick = System.currentTimeMillis();
               if (eventThread != null)
                  eventThread.invokeInEventThread(true, timerTick);
            }
            catch (InterruptedException e)
            {
            }
         }
      }

      public void setInterval(int millis)
      {
         interval = millis < 55 ? 55 : millis;
         interrupt();
      }

      public void stopGracefully()
      {
         shouldStop = true;
         interrupt();
      }
   }

   //$IF,DEMOVERSION
   private static class DemoController implements Runnable
   {
      private PersistentObject pObj;
      private long current;
      private long last;
      private final long sleepTime;
      private static final long MAXIMUM_TIME = 80 * 3600; // 80 hours, in seconds
      private static final long OBJECT_UID = StringUtilities.stringHashToLong("TC1_DC");

      public DemoController(long sleepTime)
      {
         this.sleepTime = sleepTime;

         pObj = PersistentStore.getPersistentObject(OBJECT_UID);
         Long value = (Long)pObj.getContents();
         if (value == null)
         {
            value = new Long(0);
            pObj.setContents(value);
            pObj.commit();
         }

         current = value.longValue();
      }

      public long getAvailableTime()
      {
         long r = MAXIMUM_TIME - current;
         if (r < 0) r = 0;
         return r;
      }

      private boolean setElapsed(long time)
      {
         current = time;
         try
         {
            pObj.setContents(new Long(current));
            pObj.commit();

            return true;
         }
         catch (PersistentContentException e)
         {
            current = -1;
            return false;
         }
      }

      private boolean updateDemoTime()
      {
         long time = System.currentTimeMillis();
         current += (time - last) / 1000;
         last = time;

         return setElapsed(current) && current < MAXIMUM_TIME;
      }

      public void run()
      {
         last = System.currentTimeMillis();
         boolean stop = false;

         while (!stop)
         {
            try
            {
               Thread.sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
               stop = true;
            }

            updateDemoTime();
         }
      }
   }
   //$END

   private static class ApplicationData
   {
      private String applicationId;
      private String appSecretKey;
      private String appSettings;
      private byte[] appSettingsBin;
      
      static
      {
         Launcher4B.requestAppPermission(ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION);
      }
      
      /**
       * Creates a new ApplicationData instance by trying to load the
       * properties from persistent storage
       * @param applicationId the application identifier
       */
      public ApplicationData(String applicationId)
      {
         this.applicationId = applicationId;
         load();
      }

      /**
       * Loads the application data from persistent storage
       */
      private void load()
      {
         long uid = StringUtilities.stringHashToLong(applicationId);
         PersistentObject pObj = PersistentStore.getPersistentObject(uid);

         Object[] wrapper = (Object[]) pObj.getContents();
         if (wrapper != null)
         {
            appSecretKey = (String) wrapper[0];
            appSettings = (String) wrapper[1];
            appSettingsBin = (byte[]) wrapper[2];
         }
      }

      /**
       * Saves the application data to persistent storage
       */
      public void save()
      {
         long uid = StringUtilities.stringHashToLong(applicationId);
         PersistentObject pObj = PersistentStore.getPersistentObject(uid);

         Object[] wrapper = new Object[3];
         wrapper[0] = appSecretKey;
         wrapper[1] = appSettings;
         wrapper[2] = appSettingsBin;

         pObj.setContents(wrapper);
         pObj.commit();
      }
   }

   public static class CharBits         // pgr@402_50 - describe the bitmap for a given character
   {
      public int rowWIB;          // width in bytes
      public byte[] charBitmapTable;
      public int offset;          // offset relative to the bitmap table
      public int width;
      public CharBits()
      {
      }

      public CharBits setFrom(UserFont f)
      {
         rowWIB = f.rowWidthInBytes;
         charBitmapTable = f.bitmapTable;
         offset = width = 0;
         return this;
      }
   }

   private static Hashtable loadedTCZs = new Hashtable(31);
   public static class UserFont
   {
      public Object nativeFont;    // stores the system font in some platforms
      public boolean antialiased;  // true if its antialiased
      public int firstChar;        // ASCII code of first character
      public int lastChar;         // ASCII code of last character
      public int spaceWidth;       // width of the space char
      public int maxWidth;         // width of font rectangle
      public int maxHeight;        // height of font rectangle
      public int owTLoc;           // offset to offset/width table
      public int ascent;           // ascent
      public int descent;          // descent
      public int rowWords;         // row width of bit image / 2

      public int rowWidthInBytes;
      public int bitmapTableSize;
      public byte[] bitmapTable;
      public int[]bitIndexTable;
      public String fontName;
      public int numberWidth;

      private net.rim.device.api.ui.Font prvNativeFont;
      private IntHashtable sysFontMaxWidths = new IntHashtable(5);

      private UserFont(String fontName, String suffix)
      {
         int style = suffix.charAt(1) == 'b' ? net.rim.device.api.ui.Font.BOLD : net.rim.device.api.ui.Font.PLAIN;
         int height = Integer.parseInt(suffix.substring(2, suffix.indexOf('u', 3)));

         FontFamily family = null;
         String familyName = fontName.length() == 1 ? null : fontName.substring(1);

         if (familyName != null) // only try to get family if it was specified
         {
            try
            {
               family = FontFamily.forName(familyName);
            }
            catch (ClassNotFoundException ex) // font family not found
            {
               logger.warning("Native font family \"" + familyName + "\" not found; using default native font");
            }
         }
         if (family == null) // family not found, get directly from default font
            family = net.rim.device.api.ui.Font.getDefault().getFontFamily();

         familyName = family.getName();
         this.fontName = fontName = "$" + familyName;

         net.rim.device.api.ui.Font font = family.getFont(style, height);
         nativeFont = prvNativeFont = font;
         antialiased = true;
         firstChar = 0;
         lastChar = 255;
         spaceWidth = font.getAdvance(' ');
         numberWidth = font.getAdvance('0');
         maxWidth = sysFontMaxWidths.get((fontName + suffix).hashCode(), -1);
         maxHeight = font.getHeight();
         ascent = font.getLeading() + font.getAscent();
         descent = font.getDescent();

         if (maxWidth == -1) // maximum width not found, so calculate it
         {
            int maxW = 0;
            for (char c = 0; c < 255; c++)
            {
               int w = font.getAdvance(c);
               if (w > maxW)
                  maxW = w;
            }

            sysFontMaxWidths.put((fontName + suffix).hashCode(), maxW);
            maxWidth = maxW;
         }
      }

      private UserFont(Stream s, String fontName, String suffix) throws IOException, ZipException
      {
         this.fontName = fontName;
         TCZ z = (TCZ)loadedTCZs.get(fontName.toLowerCase());
         if (z == null)
         {
            z = new TCZ(s);
            totalcross.io.ByteArrayStream fontChunks[];
            fontChunks = new totalcross.io.ByteArrayStream[z.numberOfChunks];
            for (int i =0; i < fontChunks.length; i++)
            {
               int len = z.getNextChunkSize();
               fontChunks[i] = new totalcross.io.ByteArrayStream(len);
               z.readNextChunk(fontChunks[i]);
            }
            z.bag = fontChunks;
            loadedTCZs.put(fontName.toLowerCase(), z);
         }

         fontName += suffix;
         int index  = z.findNamePosition(fontName.toLowerCase());
         if (index == -1)
            throw new IOException("Cannot find font \'" + fontName + "'\'");

         totalcross.io.ByteArrayStream bas = ((totalcross.io.ByteArrayStream[])z.bag)[index];
         bas.reset();
         totalcross.io.DataStreamLE ds = new totalcross.io.DataStreamLE(bas);
         antialiased = ds.readUnsignedShort()==1;
         firstChar   = ds.readUnsignedShort();
         lastChar    = ds.readUnsignedShort();
         spaceWidth  = ds.readUnsignedShort();
         maxWidth    = ds.readUnsignedShort();
         maxHeight   = ds.readUnsignedShort();
         owTLoc      = ds.readUnsignedShort();
         ascent      = ds.readUnsignedShort();
         descent     = ds.readUnsignedShort();
         rowWords    = ds.readUnsignedShort();

         rowWidthInBytes = rowWords << (antialiased ? 3 : 1);
         bitmapTableSize = (int)rowWidthInBytes * (int)maxHeight;

         bitmapTable     = new byte[bitmapTableSize];
         ds.readBytes(bitmapTable);
         bitIndexTable   = new int[lastChar - firstChar + 1 + 1];
         for (int i=0; i < bitIndexTable.length; i++)
            bitIndexTable[i] = ds.readUnsignedShort();
      }

      // Get the source x coordinate and width of the character
      public void setCharBits(char ch, CharBits bits)
      {
         if (firstChar <= ch && ch <= lastChar)
         {
            int index = (int)ch - (int)firstChar;
            bits.rowWIB = rowWidthInBytes;
            bits.charBitmapTable = bitmapTable;
            bits.offset = bitIndexTable[index];
            bits.width = bitIndexTable[index+1] - bits.offset;
         }
         else
         {
            bits.width = spaceWidth;
            bits.offset = -1;
         }
      }
   }
   
   public int getCharWidth(totalcross.ui.font.Font f, char ch) // guich@tc122_16: moved to outside UserFont, because each char may be in a different UserFont
   {
      UserFont font = (UserFont)f.hv_UserFont;
      if (ch < font.firstChar || ch > font.lastChar)
         f.hv_UserFont = font = Launcher4B.instance.getFont(f, ch);
      if (font.nativeFont == null)
      {
         if (ch < ' ')
            return (ch == '\t') ? font.spaceWidth * totalcross.ui.font.Font.TAB_SIZE : 0; // guich@tc100: handle tabs
         int index = (int)ch - (int)font.firstChar;
         return (font.firstChar <= ch && ch <= font.lastChar) ? font.bitIndexTable[index+1] - font.bitIndexTable[index] : font.spaceWidth;
      }
      else
         return font.prvNativeFont.getAdvance(ch);
   }

   private static class ReadResult
   {
      byte[] data;
      int result;
   }

   public static class IS2S extends totalcross.io.Stream
   {
      private InputStream is;

      public IS2S(InputStream is)
      {
         this.is = is;
      }
      public void close()
      {
         try {is.close();} catch (Exception e) {}
         is = null;
      }
      public int readBytes(byte[] buf, int start, int count)
      {
         try
         {
            return is.read(buf, start, count);
         } catch (Exception e) {return -1;}
      }
      public int writeBytes(byte[] buf, int start, int count)
      {
         return 0; // not supported
      }
   }

   public static class ISCopy extends InputStream
   {
      private InputStream is;
      private boolean closeUnderlying;

      public ISCopy(InputStream is, boolean closeUnderlying)
      {
         this.is = is;
         this.closeUnderlying = closeUnderlying;
      }

      public int available() throws java.io.IOException
      {
         return is.available();
      }

      public int read() throws java.io.IOException
      {
         return is.read();
      }

      public int read(byte[] buf) throws java.io.IOException
      {
         return is.read(buf);
      }

      public int read(byte[] buf, int off, int len) throws java.io.IOException
      {
         return is.read(buf, off, len);
      }

      public void close() throws java.io.IOException
      {
         if (closeUnderlying)
            is.close();
      }
   }

   public static class OSCopy extends OutputStream
   {
      private OutputStream os;
      private boolean closeUnderlying;

      public OSCopy(OutputStream os, boolean closeUnderlying)
      {
         this.os = os;
         this.closeUnderlying = closeUnderlying;
      }

      public void write(int b) throws java.io.IOException
      {
         os.write(b);
      }

      public void write(byte[] buf) throws java.io.IOException
      {
         os.write(buf);
      }

      public void write(byte[] buf, int off, int count) throws java.io.IOException
      {
         os.write(buf, off, count);
      }

      public void close() throws java.io.IOException
      {
         if (closeUnderlying)
            os.close();
      }
   }

   public static class S2IS extends InputStream
   {
      private Stream s;
      private byte[] oneByte = new byte[1];
      private int left;
      private boolean closeUnderlying;

      public S2IS(Stream s)
      {
         this(s, -1, true);
      }

      public S2IS(Stream s, int max)
      {
         this(s, max, true);
      }

      public S2IS(Stream s, int max, boolean closeUnderlying)
      {
         this.s = s;
         this.left = max;
         this.closeUnderlying = closeUnderlying;
      }

      public int read() throws java.io.IOException
      {
         if (left == 0)
            return -1;

         try
         {
            int r = s instanceof SSLSocket4B ? ((SSLSocket4B) s).superReadBytes(oneByte, 0, 1) : s.readBytes(oneByte, 0, 1);

            if (left != -1 && r == 1)
               left--;

            return r > 0 ? ((int)oneByte[0] & 0xFF) : -1;
         }
         catch (IOException e)
         {
            throw new java.io.IOException(e.getMessage());
         }
      }

      public int read(byte[] buf, int off, int len) throws java.io.IOException
      {
         if (left == 0)
            return -1;

         try
         {
            if (left != -1 && len > left)
               len = left;

            int r = s instanceof SSLSocket4B ? ((SSLSocket4B) s).superReadBytes(buf, off, len) : s.readBytes(buf, off, len);

            if (left != -1 && r > 0)
               left -= r;

            return r;
         }
         catch (IOException e)
         {
            throw new java.io.IOException(e.getMessage());
         }
      }

      public void close() throws java.io.IOException
      {
         if (closeUnderlying)
         {
            try
            {
               s.close();
            }
            catch (IOException e)
            {
               throw new java.io.IOException(e.getMessage());
            }
         }
      }
   }

   public static class S2OS extends java.io.OutputStream
   {
      private Stream s;
      private byte[] oneByte = new byte[1];
      private int count;
      private boolean closeUnderlying;

      public S2OS(Stream s)
      {
         this(s, true);
      }

      public S2OS(Stream s, boolean closeUnderlying)
      {
         this.s = s;
         this.closeUnderlying = closeUnderlying;
      }

      public int count()
      {
         return count;
      }

      public void write(int b) throws java.io.IOException
      {
         try
         {
            oneByte[0] = (byte)(b & 0xFF);

            int w = s instanceof SSLSocket4B ? ((SSLSocket4B) s).superWriteBytes(oneByte, 0, 1) : s.writeBytes(oneByte, 0, 1);
            if (w < 0)
               throw new java.io.IOException("Unknown error when writing to stream");

            count += w;
         }
         catch (IOException e)
         {
            throw new java.io.IOException(e.getMessage());
         }
      }

      public void write(byte[] buf, int off, int len) throws java.io.IOException
      {
         try
         {
            int w = s instanceof SSLSocket4B ? ((SSLSocket4B) s).superWriteBytes(buf, off, len) : s.writeBytes(buf, off, len);
            if (w < 0)
               throw new java.io.IOException("Unknown error when writing to stream");

            count += w;
         }
         catch (IOException e)
         {
            throw new java.io.IOException(e.getMessage());
         }
      }

      public void close() throws java.io.IOException
      {
         if (closeUnderlying)
         {
            try
            {
               s.close();
            }
            catch (IOException e)
            {
               throw new java.io.IOException(e.getMessage());
            }
         }
      }
   }

   private static class Chunk
   {
      Chunk prev;
      byte[] data;
      static int CHUNK_SIZE = 512;

      public Chunk(Chunk prev)
      {
         this.prev = prev;
         data = new byte[CHUNK_SIZE];
      }
   }
   
   // ----------------------------------------------------------------------
   // Fix String encoding problem on BlackBerry
   // ----------------------------------------------------------------------
   // We do this by hacking each class file that calls new String(byte[])
   // and new String(byte[], int, int) and replacing the calls by
   // Launcher4B.stringInit(byte[]) and Launcher4B.stringInit(byte[], int,
   // int), respectively.
   
   public static String stringInit(byte[] b)
   {
      return new String(Convert.charConverter.bytes2chars(b, 0, b.length));
   }
   
   public static String stringInit(byte[] b, int off, int count)
   {
      return new String(Convert.charConverter.bytes2chars(b, off, count));
   }
   
   // ----------------------------------------------------------------------
   // Add support to String.lastIndexOf(String[, int]) on BlackBerry
   // ----------------------------------------------------------------------
   // We do this by hacking each class file that calls String.lastIndexOf(
   // String) or String.lastIndexOf(String, int) and replacing the calls by
   // Launcher4B.stringLastIndexOf(String) and Launcher4B.stringLastIndexOf(
   // String, int), respectively.
   //
   // Implementation was taken and adapted from:
   // http://www.docjar.com/html/api/java/lang/String.java.html
   
   public static int stringLastIndexOf(String str, String subStr)
   {
      return stringLastIndexOf(str, subStr, str.length());
   }
   
   public static int stringLastIndexOf(String str, String subStr, int start)
   {
      int count = str.length();
      int subCount = subStr.length();
      
      if (subCount <= count && start >= 0)
      {
         if (subCount > 0)
         {
            if (start > count - subCount)
               start = count - subCount;
            
            // count and subCount are both >= 1
            char[] source = str.toCharArray();
            char[] target = subStr.toCharArray();
            char firstChar = target[0];
            int end = subCount;
            
            while (true)
            {
               int i = str.lastIndexOf(firstChar, start);
               if (i == -1)
                  return -1;
               
               int o1 = i, o2 = 0;
               while (++o2 < end && source[++o1] == target[o2]);
               
               if (o2 == end)
                  return i;
               
               start = i - 1;
            }
         }
         
         return start < count ? start : count;
      }
      
      return -1;
   }

   // ----------------------------------------------------------------------
   // Emulate object finalization on BlackBerry
   // ----------------------------------------------------------------------
   // We do this by hacking each class file that implements the
   // Object.finalize method, adding Launcher.Finalizable to its
   // list of interfaces and adding the following instructions
   // to every constructor that calls the super constructor:
   //
   // getstatic [totalcross.Launcher][instance,Ltotalcross.Launcher]
   // aload_0 [this]
   // invokevirtual [addFinalizable,(Ltotalcross/Launcher$Finalizable;)V]
   //
   // This would be the same of including the following java code
   // in each class that we want to finalize:
   //
   // BEFORE:
   // public MyClass
   // {
   //    public MyClass()
   //    {
   //        this(false);
   //    }
   //
   //    public MyClass(boolean b)
   //    {
   //        super();
   //    }
   // }
   //
   // AFTER:
   // public MyClass implements Launcher.Finalizable
   // {
   //    public MyClass()
   //    {
   //        this(false);
   //    }
   //
   //    public MyClass(boolean b)
   //    {
   //        super();
   //        Launcher.addFinalizable(this);
   //    }
   // }

   public interface Finalizable
   {
      public void finalize();
   }

   public void addFinalizable(Finalizable f)
   {
      int hash = f.hashCode();
      int count = finalizablesCount.get(hash, 0);
      if (count == 0)
      {
         finalizables.put(hash, f);
         finalizablesCount.put(hash, 1);
      }
      else
      {
         Vector v;

         if (count == 1)
         {
            Finalizable obj = (Finalizable)finalizables.get(hash);
            if (obj == f)
               return;

            v = new Vector();
            v.addElement(obj);

            finalizables.put(hash, v);
         }
         else
         {
            v = (Vector)finalizables.get(hash);
            for (int i = v.size() - 1; i >= 0; i--)
               if (v.items[i] == f)
                  return;
         }

         v.addElement(f);
         finalizablesCount.put(hash, count + 1);
      }
   }

   public void removeFinalizable(Finalizable f)
   {
      int hash = f.hashCode();
      int count = finalizablesCount.get(hash, 0);
      if (count == 1)
      {
         Finalizable obj = (Finalizable)finalizables.get(hash);
         if (obj != f)
            return;

         finalizables.remove(hash); // flsobral@tc100b4: use remove instead of finalizables.put(hash, null), which now results in NPE.
         finalizablesCount.put(hash, 0);
      }
      else if (count > 1)
      {
         Vector v = (Vector)finalizables.get(hash);

         int i;
         for (i = v.size() - 1; i >= 0; i--)
            if (v.items[i] == f)
               break;

         if (i < 0) // not found
            return;

         v.removeElementAt(i);

         if (count == 2)
            finalizables.put(hash, v.items[0]);
         finalizablesCount.put(hash, count - 1);
      }
   }

   public void addOrRemoveFinalizable(Finalizable f, boolean isRemove)
   {
      if (isRemove)
         removeFinalizable(f);
      else
         addFinalizable(f);
   }

   private void runFinalizes()
   {
      int hash, count;
      IntVector hashs = finalizablesCount.getKeys();
      for (int i = hashs.size() - 1; i >= 0; i--)
      {
         hash = hashs.items[i];
         count = finalizablesCount.get(hash, 0);

         if (count == 1)
         {
            try
            {
               ((Finalizable)finalizables.get(hash)).finalize();
            }
            catch (Exception ex) { }
            catch (Error err) { }
         }
         else if (count > 1)
         {
            Vector v = (Vector)finalizables.get(hash);
            for (int j = v.size() - 1; j >= 0; j--)
            {
               try
               {
                  ((Finalizable)v.items[j]).finalize();
               }
               catch (Exception ex) { }
               catch (Error err) { }
            }
         }
      }

      finalizables.clear();
      finalizablesCount.clear();
   }

   public static String stringReplace(String str, String src, String dst)
   {
      StringBuffer sb = new StringBuffer(str);
      stringReplace(sb, src, dst);

      return sb.toString();
   }

   public static void stringReplace(StringBuffer sb, String src, String dst)
   {
      int idx, srcLen = src.length();
      while ((idx = sb.toString().indexOf(src)) >= 0)
      {
         sb.delete(idx, idx + srcLen);
         sb.insert(idx, dst);
      }
   }
   
   public static boolean requestAppPermission(int permissionID)
   {
      return requestAppPermissions(new int[] { permissionID });
   }
   
   public static boolean requestAppPermissions(int[] permissionIDs)
   {
      ApplicationPermissionsManager manager = ApplicationPermissionsManager.getInstance();
      ApplicationPermissions permissions = new ApplicationPermissions();
      
      for (int i = permissionIDs.length - 1; i >= 0; i--)
      {
         int value = manager.getPermission(permissionIDs[i]);
         if (value == ApplicationPermissions.VALUE_DENY)
            permissions.addPermission(permissionIDs[i]);
      }
      
      return permissions.getPermissionKeys().length == 0 || manager.invokePermissionsRequest(permissions);
   }
   
   public static class Cache
   {
      private int capacity;
      private int size;
      private final String name;
      private final Hashtable table;
      private long lastUsedSequence = Long.MIN_VALUE;
      
      public Cache(String name)
      {
         this(name, Integer.MAX_VALUE);
      }
      
      public Cache(String name, int capacity)
      {
         if (capacity <= 0)
            throw new CacheException("Cache capacity must be greater than zero");
            
         this.name = name;
         this.capacity = capacity;
         size = 0;
         table = new Hashtable(capacity > 10 ? 10 : capacity);
      }
      
      public String toString()
      {
         return name;
      }
      
      public String getName()
      {
         return name;
      }
      
      public int getCapacity()
      {
         return capacity;
      }
      
      public boolean setCapacity(int capacity)
      {
         if (capacity < size)
            return false;
         else
         {
            this.capacity = capacity;
            return true;
         }
      }
      
      public boolean isFull()
      {
         return size == capacity;
      }
      
      public Object get(int hash)
      {
         Item item;
         return (size == 0 || (item = (Item)table.get(hash)) == null) ? null : item.value;
      }
      
      public Object get(Object key)
      {
         Item item;
         return (size == 0 || (item = (Item)table.get(key)) == null) ? null : item.value;
      }
      
      public boolean put(Object key, Object value)
      {
         Item item = (Item)table.get(key);
         if (item == null)
         {
            if (size == capacity)
               return false;
            else
            {
               table.put(key, item = new Item(key, value));
               size++;
            }
         }
         else if (item.value != value)
            throw new CacheException("Unique identifier restriction was broken: " + key);
         
         item.lastUsed = lastUsedSequence++;
         return true;
      }
      
      public Object remove(int hash)
      {
         Item item;
         if (size == 0 || (item = (Item)table.remove(hash)) == null)
            return null;
         else
         {
            size--;
            return item.value;
         }
      }
      
      public Object remove(Object key)
      {
         Item item;
         if (size == 0 || (item = (Item)table.remove(key)) == null)
            return null;
         else
         {
            size--;
            return item.value;
         }
      }
      
      public Object removeLRU()
      {
         if (size == 0)
            return null;
         else
         {
            Vector keys = table.getKeys();
            
            Item cur, lru;
            cur = lru = (Item)table.get(keys.items[0]);
            
            for (int i = size - 1; i > 0; i--)
            {
               cur = (Item)table.get(keys.items[i]);
               if (cur.lastUsed < lru.lastUsed)
                  lru = cur;
            }
            
            table.remove(lru.key);
            size--;
            return lru.value;
         }
      }
      
      public void clear()
      {
         table.clear();
         size = 0;
         lastUsedSequence = Long.MIN_VALUE;
      }
      
      private static class Item
      {
         public Object key;
         public Object value;
         public long lastUsed;
         
         public Item(Object key, Object value)
         {
            this.key = key;
            this.value = value;
         }
      }
   }
   
   public static class CacheException extends RuntimeException
   {
      public CacheException(String message)
      {
         super(message);
      }
   }

   public String[] getSupportedResolutions()
   {
      return new String[]{"640x480"};
   }
}
