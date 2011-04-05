/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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



package totalcross.io.device.gps;

import totalcross.io.*;
import totalcross.io.device.*;
import totalcross.net.*;
import totalcross.phone.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.util.*;

/**
 * Control that display GPS coordinates read from the COM (or Bluetooth, or IR) port.
 * In Windows Mobile and Android, it uses the native API instead of reading from the COM port. 
 * 
 * For example:
 * 
 * <pre>
 * add(gps = new GPS(1000),LEFT,TOP);
 * </pre>
 * See the tc.samples.io.device.GPSTest.
 * <br><br>
 * On Android, don't forget to turn on the GPS, going to Settings / Security &amp; Location / Enable GPS satellites. 
 * The other platforms may require that as well.
 * <br><br>
 * If your GPS is freezing the application, set hasCellLocation to false.
 * @see #hasCellLocation
 */

public class GPS extends Container implements Runnable
{
   /** Stores the location - latitude on first index (0) and longitude on second index (1). */
   public double[] location = new double[2];
   /** Stores the direction in degrees from the North. */
   public double direction = -1.0f;
   /** Stores the speed in knots. */
   public double velocity = -1.0f;
   /**
    * Number of satellites in view.
    * 
    * @since TotalCross 1.20
    */
   public int satellites;
   /** Stores the time of the last updated. */
   public Time lastFix = new Time();
   /** The retrieved message, or null in Windows Mobile and Android. */
   public String messageReceived;

   /** String that will be printed with there longitude can't be acquired. You can localize this string. */
   public static String LON_LOW_SIGNAL = "lon: low signal";
   /** String that will be printed with there longitude can't be acquired. You can localize this string. */
   public static String LAT_LOW_SIGNAL = "lat: low signal";
   
   /** The last PDOP, if any. 
    * @since TotalCross 1.27
    */
   public double pdop;
   
   private PortConnector sp;
   private Label[] text = new Label[5];
   private int readInterval = 2000;
   private byte[] buf = new byte[1];
   private TimerEvent timer;
   private StringBuffer sb = new StringBuffer(512);
   private boolean text4set;
   private static boolean nativeAPI = Settings.isWindowsDevice() || Settings.platform.equals(Settings.ANDROID);
   
   /**
    * If the GPS fails connecting to the satellites, and the phone has signal, it uses the cell tower location as a
    * rough location. The precision is vary between 50m to 3km, depending where the phone is. If you don't want to use
    * this feature, set this flag to false. Currently works on Windows Mobile, Android and Blackberry.
    * 
    * @since TotalCross 1.22
    */
   public static boolean hasCellLocation = nativeAPI || Settings.platform.equals(Settings.BLACKBERRY);
   
   /**
    * Returns the Windows CE GPS COM port, which can be used to open a PortConnector. Sample:
    * 
    * <pre>
    * String com;
    * if (Settings.isWindowsDevice() &amp;&amp; (com = getWinCEGPSCom()) != null)
    *    sp = new PortConnector(Convert.chars2int(com), 9600, 7, PortConnector.PARITY_EVEN, 1);
    * </pre>
    * 
    * @return A string like "COM3", or null if no keys with GPS was found under HKLM\Drivers\BuildIn.
    */
   public static String getWinCEGPSCom() // guich@tc100b5_38
   {
      try
      {
         try // guich@tc120_51
         {
            String key = "System\\CurrentControlSet\\GPS Intermediate Driver\\Multiplexer\\ActiveDevice";
            String prefix = Registry.getString(Registry.HKEY_LOCAL_MACHINE, key, "Prefix");
            int index = Registry.getInt(Registry.HKEY_LOCAL_MACHINE, key, "Index");
            if (prefix.equals("COM"))
               return "COM" + index;
         }
         catch (ElementNotFoundException enfe)
         {
            // ignore if a key was not found
         }

         String[] keys = Registry.list(Registry.HKEY_LOCAL_MACHINE, "Drivers\\BuiltIn");
         for (int i = 0; i < keys.length; i++)
         {
            String k = keys[i].toLowerCase();
            if (k.indexOf("serial") >= 0)
            {
               String key = "Drivers\\BuiltIn\\" + keys[i], prefix, name;
               try
               {
                  prefix = Registry.getString(Registry.HKEY_LOCAL_MACHINE, key, "Prefix");
                  name = Registry.getString(Registry.HKEY_LOCAL_MACHINE, key, "FriendlyName");
                  if (prefix.equals("COM") && name.toLowerCase().indexOf("gps") >= 0)
                     return "COM" + Registry.getInt(Registry.HKEY_LOCAL_MACHINE, key, "Index");
               }
               catch (ElementNotFoundException enfe)
               {
                  // ignore if a key was not found
               }
            }
         }
      }
      catch (Exception e)
      {
      }
      return null;
   }

   /**
    * Constructs a GPS control, with a read interval of 2 seconds.
    * 
    * @throws IOException
    * @see #GPS(int)
    */
   public GPS() throws IOException
   {
      this(2000);
   }

   /**
    * Constructs a GPS control, opening a default port at 9600 bps. Already prepared for PIDION scanners. It
    * automatically scans the Windows CE registry searching for the correct GPS COM port.
    * 
    * @param readInterval
    *           The interval used to fetch data, in milliseconds. A ControlEvent.PRESSED is posted each time the TRIGGERED event occurs.
    * @throws IOException
    */
   public GPS(int readInterval) throws IOException
   {
      String com;
      this.readInterval = readInterval; // guich@tc126_13

      if (!nativeAPI || !startGPS())
      {
         if ("PIDION".equals(Settings.deviceId)) // guich@586_7
            sp = new PortConnector(Convert.chars2int("COM4"), 9600, 7, PortConnector.PARITY_EVEN, 1);
         else if (Settings.isWindowsDevice() && (com = getWinCEGPSCom()) != null)
            sp = new PortConnector(Convert.chars2int(com), 9600, 7, PortConnector.PARITY_EVEN, 1);
         else
            sp = new PortConnector(0, 9600);
      }

      if (sp != null)
      {
         sp.readTimeout = 1500;
         sp.setFlowControl(false);
      }
      for (int i = 0; i < text.length; i++)
         text[i] = new Label("");
   }

   private boolean startGPS() throws IOException
   {
      return false;
   }

   native boolean startGPS4D() throws IOException;

   /**
    * Constructs a GPS control with the given serial port and read interval. For example:
    * 
    * <pre>
    * PortConnector sp = new PortConnector(PortConnector.BLUETOOTH, 9600);
    * sp.setReadTimeout(500);
    * add(gps = new GPS(sp, 1000), LEFT, TOP);
    * </pre>
    * Don't use this constructor under Android nor Windows Mobile.
    * 
    * @param sp
    * @param readInterval
    *           The interval used to fetch data, in milliseconds.
    * @throws IOException
    * @see #GPS(int)
    */
   public GPS(PortConnector sp, int readInterval) throws IOException
   {
      if (sp != null)
         this.sp = sp;
      else if (nativeAPI)
         startGPS();

      this.readInterval = readInterval;
      for (int i = 0; i < text.length; i++)
         text[i] = new Label("");
   }

   /**
    * Closes the underlying PortConnector and stops the timer.
    * 
    * @throws IOException
    */
   public void stop() throws IOException
   {
      removeTimer(timer);
      if (sp != null)
         sp.close();
      else
         stopGPS();
      sp = null;
   }

   private void stopGPS()
   {
   }

   native void stopGPS4D();

   /**
    * Returns the location array. The latitude is stored in position 0, and the longitude in position 1.
    */
   public double[] getLocation()
   {
      return location;
   }

   /**
    * Returns the latitude.
    */
   public double getLatitude()
   {
      return location[0];
   }

   /**
    * Returns the longitude.
    */
   public double getLongitude()
   {
      return location[1];
   }

   /**
    * Returns the coordinate of the given string and direction.
    * 
    * @param s
    *           the string in coordinate format.
    * @param dir
    *           the direction: SWEN
    */
   public double toCoordinate(String s, char dir)
   {
      double deg = 0;
      int i = s.indexOf('.'); // guich@421_58
      if (i >= 0)
      {
         int divider = 1;
         int size = s.length();
         for (int d = size - i - 1; d > 0; d--)
            divider *= 10;
         try
         {
            deg = (double) Convert.toInt(s.substring(0, i - 2)) +
                  ((double) Convert.toInt(s.substring(i - 2, i)) +
                  (double) Convert.toInt(s.substring(i + 1, size)) / divider) / 60;
         }
         catch (InvalidNumberException ine)
         {
            return 0;
         }
         if (dir == 'S' || dir == 'W')
            deg = -deg;
      }
      return deg;
   }

   /**
    * 
    * @param coord
    * @param dir
    * @throws InvalidNumberException
    */
   public String toCoordinateGGMMSS(String coord, char dir) throws InvalidNumberException
   {
      try
      {
         String g, m, s;

         if (dir == 'E' || dir == 'W') // LONGITUDE
         {
            int gint = Convert.toInt(coord.substring(0, 3));
            g = gint + " ";
            m = coord.substring(3, 5) + " ";
            double temp = Convert.toDouble("0" + coord.substring(5, 10)) * 60;
            s = Convert.toString(temp, 4);
         }
         else
         {
            g = coord.substring(0, 2) + " "; // LATITUDE
            m = coord.substring(2, 4) + " ";
            double temp = Convert.toDouble("0" + coord.substring(4, 9)) * 60;
            s = Convert.toString(temp, 4);
         }
         return g + m + s + " " + dir;
      }
      catch (IndexOutOfBoundsException ioobe)
      {
         return "Err: " + coord + " - " + dir;
      }
   }

   // user interface
   public void initUI()
   {
      for (int i = 0; i < text.length; i++)
         add(text[i], LEFT, AFTER);

      text[0].setText("GPS Initialising");
      timer = addTimer(readInterval);
   }

   /** 
    * Read interval is once again implemented as a timer event and threads are only used when we actually need
    * to connect to opencellid.org for cell if location. 
    * For some reason, the Windows Mobile GPS Intermediate Driver does not work properly with threads, often causing
    * the read operation to freeze block and freeze the application.  
    * 
    *///flsobral@tc124_11: Windows Mobile GPS Intermediate Driver doesn't like threads, so timer event is back.
   public void onEvent(Event e)
   {
      if (e.type == TimerEvent.TRIGGERED && retrievingFromOpenCellID == null && timer.triggered)
      {
         try
         {
            if (sp != null) // serial gps?
               processSerial();
            else // native gps
               processNative();
         }
         catch (Exception e1)
         {
            lowSignal(e1.getMessage());
            e1.printStackTrace();            
            lowSignal(e1.getMessage());
         }
         if (text[0].getText() == LAT_LOW_SIGNAL && hasCellLocation)
            processCell();
         repaintNow();
         postPressedEvent(); // guich@tc126_67
      }
   }
   
   private void lowSignal(String ex)
   {
      text[0].setText(LAT_LOW_SIGNAL);
      text[1].setText(LON_LOW_SIGNAL);
      for (int i = 2; i < text.length; i++)
         text[i].setText("");
      if (ex != null)
         text[text.length-1].setText(ex);
   }

   private int updateLocation()
   {
      return 0;
   }

   native int updateLocation4D();

   public int getPreferredWidth()
   {
      return FILL;
   }

   public int getPreferredHeight()
   {
      return fmH * text.length + insets.top + insets.bottom;
   }

   protected void onColorsChanged(boolean colorsChanged)
   {
      for (int i = 0; i < text.length; i++)
         text[i].setBackForeColors(getBackColor(), getForeColor());
   }

   protected void onFontChanged()
   {
      for (int i = 0; i < text.length; i++)
         text[i].setFont(font);
   }

   // private methods
   private void processNative()
   {
      int result = updateLocation();
      if ((result & 3) == 0) // latitude and longitude: one doesn't make sense without the other
         lowSignal(null);
      else
      {
         double absoluteLat = location[0] < 0 ? -location[0] : location[0];
         int degrees = (int) absoluteLat;
         int minutes = (int) ((absoluteLat - degrees) * 60);
         double seconds = (((absoluteLat - degrees) * 60) - minutes) * 60;
         text[0].setText("lat: " + degrees + " " + minutes + " " + seconds + (location[0] < 0 ? " S" : " N"));

         double absoluteLon = location[0] < 0 ? -location[1] : location[1];
         degrees = (int) absoluteLon;
         minutes = (int) ((absoluteLon - degrees) * 60);
         seconds = (((absoluteLon - degrees) * 60) - minutes) * 60;
         text[1].setText("lon: " + degrees + " " + minutes + " " + seconds + (location[1] < 0 ? " W" : " E"));
         showLastFix(2); // fix only makes sense if there is valid data
         text[3].setText((result & 8) != 0 ? "speed: " + velocity : "");
         text[4].setText((result & 4) != 0 ? "direction: " + direction : "");
         if (satellites > 0)
            text[2].setText(text[2].getText() + "  " + "sat: "+satellites);
      }
   }

   private void showLastFix(int pos)
   {
      lastFix.hour += Settings.timeZone + (Settings.daylightSavings ? 1 : 0); //flsobral@tc126_58: use daylightSavings
      text[pos].setText("fix: "+lastFix);
      lastFix.hour -= Settings.timeZone + (Settings.daylightSavings ? -1 : 0);
   }

   private void processSerial() throws IOException
   {
      try
      {
         String message;
         while ((message = nextMessage()) != null)
         {
            messageReceived = message;
            String[] sp = Convert.tokenizeString(message, ',');
            if (sp[0].equals("$GPGGA")) // guich@tc115_71
            {
               if ("".equals(sp[2]) || "".equals(sp[4]))
                  lowSignal(null);
               else
               {
                  text[0].setText("lat: " + toCoordinateGGMMSS(sp[2], sp[3].charAt(0)));
                  text[1].setText("lon: " + toCoordinateGGMMSS(sp[4], sp[5].charAt(0)));
               }
               if (sp.length > 7 && sp[7].length() > 0)
               {
                  satellites = Convert.toInt(sp[7]); //flsobral@tc120_66: new field, satellites.
                  text[2].setText("sat: " + sp[7]);
               }
            }
            else if (sp[0].equals("$GPGLL"))
            {
               location[0] = toCoordinate(sp[1], sp[2].charAt(0));
               location[1] = toCoordinate(sp[3], sp[4].charAt(0));
               if (sp[5].length() > 0)
               {
                  lastFix.hour = Convert.toInt(sp[5].substring(0, 2));
                  lastFix.minute = Convert.toInt(sp[5].substring(2, 4));
                  lastFix.second = Convert.toInt(sp[5].substring(4, 6));
                  lastFix.millis = 0;
               }

               text[0].setText("lat: " + toCoordinateGGMMSS(sp[1], sp[2].charAt(0)));
               text[1].setText("lon: " + toCoordinateGGMMSS(sp[3], sp[4].charAt(0)));
               showLastFix(2);
               text[3].setText("valid: " + "A".equals(sp[6]));
            }
            // fleite@421_57: Adding position and time message's decode routine
            else if (sp[0].equals("$GPRMC") && sp.length >= 8 && "A".equals(sp[2]))
            {
               location[0] = toCoordinate(sp[3], sp[4].charAt(0));
               location[1] = toCoordinate(sp[5], sp[6].charAt(0));
               if (sp[1].length() >= 6)
               {
                  lastFix.hour = Convert.toInt(sp[1].substring(0, 2));
                  lastFix.minute = Convert.toInt(sp[1].substring(2, 4));
                  lastFix.second = Convert.toInt(sp[1].substring(4, 6));
                  lastFix.millis = 0;
               }
               text[0].setText("lat: " + toCoordinateGGMMSS(sp[3], sp[4].charAt(0)));
               text[1].setText("lon: " + toCoordinateGGMMSS(sp[5], sp[6].charAt(0)));
               showLastFix(2);

               if (sp[7].length() > 0)
               {
                  velocity = Convert.toDouble(sp[7]); //knots
                  text[3].setText("speed: " + velocity); //flsobral@tc120_10: speed and direction labels were misplaced
               }
               if (sp[8].length() > 0)
               {
                  direction = Convert.toDouble(sp[8]); //degrees
                  text[4].setText("direction: " + direction); //flsobral@tc120_10: speed and direction labels were misplaced
                  text4set = true;
               }
            }
            else if (sp[0].equals("$GPGSA")) // guich@tc126_66
               try 
               {
                  pdop = sp.length > 15 ? Convert.toDouble(sp[15]) : 0;
               } catch (Exception e) {}

            if (!text4set)
            {
               text[4].setText(message);
               text[4].repaintNow();
            }
         }
      }
      catch (Exception e)
      {
         lowSignal(e.getMessage());
      }
   }

   /**
    * Reads the next message from the port connector.
    *  
    * guich@tc115_71: completely changed
    * flsobral@tc120_66: messages are now validated.
    * 
    * @return
    * @throws IOException
    */
   private String nextMessage() throws IOException
   {
      StringBuffer sb = this.sb;
      byte[] buf = this.buf;
      String ret = null;
      int len = 0;

      while (sp.readBytes(buf, 0, 1) != -1)
      {
         int c = buf[0] & 0xFF;
         if (c == '$')
            sb.setLength(0);

         sb.append((char) c);
         if (c == '\n')
            break;
         else if (c > 128) // just trash?
            throw new IOException("Invalid port: only trash was retrieved.");
         else if (++len > 512)
         {
            int idx = sb.toString().indexOf('$');
            if (idx == -1)
               sb.setLength(0);
            else
               sb.delete(0, idx);
            len = sb.length();
         }
      }

      if (len > 0)
      {
         ret = sb.toString();
         if (ret.length() <= 3)
            return null;

         sb.setLength(0);
         ret = validateMessage(ret);
      }
      return ret;
   }

   /**
    * Checks if the received message is valid.
    * 
    * @param message 
    * @return the given message without the ending CRLF if valid, otherwise null is returned.
    * 
    * @since TotalCross 1.20
    */
   private String validateMessage(String message)
   {
      char[] msgChars = message.toCharArray();
      int checksum = 0;

      if (msgChars[0] != '$' || msgChars[msgChars.length - 2] != '\r' || msgChars[msgChars.length - 1] != '\n')
         return null;
      for (int i = msgChars.length - 6; i > 0; i--)
      {
         if (msgChars[i] == '\r' || msgChars[i] == '\n' || msgChars[i] == '$' || msgChars[i] == '*')
            return null;
         checksum ^= msgChars[i];
      }
      if (msgChars[msgChars.length - 5] == '*')
      {
         int checksum2 = Convert.hex2signed(new String(msgChars, msgChars.length - 4, 2));
         if (checksum != checksum2)
            return null;
      }

      return new String(msgChars, 0, msgChars.length - 2);
   }

   protected void finalize()
   {
      try
      {
         this.stop();
      }
      catch (IOException e)
      {
      }
   }
   
   private static Hashtable htCell = new Hashtable(3);
   private Thread retrievingFromOpenCellID;
   
   private boolean processCell()
   {
      double[] cached = null;
      CellInfo.update();
      if (CellInfo.cellId != null && CellInfo.lac != null && CellInfo.mcc != null && CellInfo.mnc != null)
      {
         String key = CellInfo.cellId+"|"+CellInfo.lac+"|"+CellInfo.mcc+"|"+CellInfo.mnc;
         cached = (double[])htCell.get(key);
         if (cached != null)
         {
            text[0].setText("lat: " + cached[0]);
            text[1].setText("lon: " + cached[1]);
            lastFix.update();
            showLastFix(2);
            text[3].setText("(Cell ID Location)");            
         }
         else if (retrievingFromOpenCellID == null)
         {
            retrievingFromOpenCellID = new Thread(this);
            retrievingFromOpenCellID.start();
         }
      }
      return cached != null;
   }
   
   public void run()
   {
      double[] cached = null;
      String key = CellInfo.cellId+"|"+CellInfo.lac+"|"+CellInfo.mcc+"|"+CellInfo.mnc;
      
      // http://www.opencellid.org/cell/get?key=6cae7558507ace91c32d5ae0d3c18c61&mnc=11&mcc=724&lac=43521&cellid=1420859
      // <rsp stat="ok"><cell range="6000" lac="43521" lat="-22.9011525122194" nbSamples="401" lon="-43.1716875965087" cellId="1420859" mcc="724" mnc="11"/></rsp>
      String url = "http://www.opencellid.org/cell/get?key=6cae7558507ace91c32d5ae0d3c18c61&mnc="+CellInfo.mnc+"&mcc="+CellInfo.mcc+"&lac="+CellInfo.lac+"&cellid="+CellInfo.cellId;
      text[text.length-1].setText("Retrieving "+key+" info...");
      repaintNow();
      
      try
      {
         HttpStream http = new HttpStream(new URI(url));
         byte[] buf = new byte[http.contentLength];
         http.readBytes(buf, 0, http.contentLength);
         String str = new String(buf);
         String toks[] = Convert.tokenizeArguments(str); // 0 <rsp  1 stat=ok  2 ><cell  3 range=6000  4 lac=43521  5 lat=-22.9011525122194  6 nbSamples=401  7 lon=-43.1716875965087  8 cellId=1420859  9 mcc=724  10 mnc=11  11 /></rsp>
         String lon=null,lat=null;
         for (int i =0; i < toks.length; i++)
         {
            String t = toks[i];
            if (t.startsWith("lat="))
               lat = t.substring(4);
            else
            if (t.startsWith("lon="))
               lon = t.substring(4);
         }
         if (lat != null && lat.length() > 0 && lon != null && lon.length() > 0)
         {
            location[0] = Convert.toDouble(lat);
            location[1] = Convert.toDouble(lon);
            htCell.put(key, cached = new double[]{location[0],location[1]});
         }
         else
         {
            lowSignal("CellID " + key + " not in database");
            cached = null;
         }
         if (cached != null)
         {
            text[0].setText("lat: " + cached[0]);
            text[1].setText("lon: " + cached[1]);
            lastFix.update();
            showLastFix(2);
            text[3].setText("(Cell ID Location)");
         }
      }
      catch (IllegalArgumentIOException e)
      {
         // ignore
      }
      catch (IOException e)
      {
         lowSignal(e.getMessage());
      }
      catch (InvalidNumberException e)
      {
         // ignore
      }
      
      retrievingFromOpenCellID = null;
   }
}
