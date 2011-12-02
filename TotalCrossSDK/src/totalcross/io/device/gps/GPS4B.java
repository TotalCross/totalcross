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
import totalcross.sys.*;

import javax.microedition.location.*;

public class GPS4B
{
   public double[] location = {INVALID,INVALID};
   public Time lastFix = new Time();
   public double direction = INVALID;
   public double velocity = INVALID;
   public int satellites;   
   public String messageReceived;
   public double pdop = INVALID;
   public String lowSignalReason;
   public static final double INVALID = Convert.MIN_DOUBLE_VALUE;

   PortConnector sp;
   private byte[] buf = new byte[1];
   private StringBuffer sb = new StringBuffer(512);

   // blackberry exclusive fields
   private Location bbLocation;
   private LocationProvider provider;
   
   public static String getWinCEGPSCom()
   {
      return null;
   }

   public GPS4B() throws IOException
   {
      this(null);
   }

   public GPS4B(PortConnector sp) throws IOException
   {
      if (sp == null) // use blackberry api
      {
         // let's start supporting only autonomous GPS.
         Criteria criteria = new Criteria();
         criteria.setCostAllowed(false);

         try
         {
            provider = LocationProvider.getInstance(criteria);
         }
         catch (Exception e)
         {
            throw new totalcross.io.IOException(e.getMessage());
         }
      }
      else
         this.sp = sp; // use the portconnector
   }

   public void stop()
   {
      if (sp != null)
      {
         try {sp.close();} catch (Exception e) {}
         sp = null;
      }
   }

   public double getLatitude()
   {
      return location[0];
   }

   public double getLongitude()
   {
      return location[1];
   }

   public static double toCoordinate(String s, char dir)
   {
      double deg = 0;
      int i = s.indexOf('.');
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

   public static String toCoordinateGGMMSS(String coord, char dir) throws InvalidNumberException
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

   public boolean retrieveGPSData()
   {
      try
      {
         lowSignalReason = null;
         location[0] = location[1] = direction = velocity = INVALID;
         satellites = 0;
         return sp != null ? processSerial() : processNative();
      }
      catch (Exception e1)
      {
         lowSignalReason = e1.getMessage()+" ("+(e1.getClass())+")";
         return false;
      }
   }
   
   private boolean processNative() throws LocationException, InterruptedException
   {
      if (provider == null)
         return false;
      bbLocation = provider.getLocation(-1);
      if (bbLocation.isValid())
      {
         QualifiedCoordinates coordinates = bbLocation.getQualifiedCoordinates();
         location[0] = coordinates.getLatitude();
         location[1] = coordinates.getLongitude();
         lastFix.update();
         velocity = (double) bbLocation.getSpeed() / 0.514; // converting speed from m/s to knots.
         direction = (double) bbLocation.getCourse();
         try 
         {
            messageReceived = bbLocation.getExtraInfo("application/X-jsr179-location-nmea");
            if (messageReceived != null && messageReceived.startsWith("$GPGSA")) // guich@tc126_66
            {
               String[] toks = Convert.tokenizeString(messageReceived,',');
               pdop = toks.length > 15 ? Convert.toDouble(toks[15]) : 0;
            }
         } catch (Exception e) {}
         return true;
      }
      return false;
   }
   
   private boolean processSerial() throws Exception
   {
      String message;
      boolean ok = false;
      while ((message = nextMessage()) != null)
      {
         messageReceived = message;
         String[] sp = Convert.tokenizeString(message, ',');
         if (sp[0].equals("$GPGGA")) // guich@tc115_71
         {
            if ("".equals(sp[2]) || "".equals(sp[4]))
               continue;
            location[0] = toCoordinate(sp[2], sp[3].charAt(0));
            location[1] = toCoordinate(sp[4], sp[5].charAt(0));
            if (sp.length > 7 && sp[7].length() > 0)
               satellites = Convert.toInt(sp[7]); //flsobral@tc120_66: new field, satellites.
         }
         else if (sp[0].equals("$GPGLL"))
         {
            if (!"A".equals(sp[6]))
               continue;
            location[0] = toCoordinate(sp[1], sp[2].charAt(0));
            location[1] = toCoordinate(sp[3], sp[4].charAt(0));
            if (sp[5].length() > 0)
            {
               lastFix.hour = Convert.toInt(sp[5].substring(0, 2));
               lastFix.minute = Convert.toInt(sp[5].substring(2, 4));
               lastFix.second = Convert.toInt(sp[5].substring(4, 6));
               lastFix.millis = 0;
            }
            ok = true;
         }
         // fleite@421_57: Adding position and time message's decode routine
         else if (sp[0].equals("$GPRMC") && sp.length >= 8 && "A".equals(sp[2]))
         {
            if (!"A".equals(sp[2]))
               continue;
            location[0] = toCoordinate(sp[3], sp[4].charAt(0));
            location[1] = toCoordinate(sp[5], sp[6].charAt(0));
            if (sp[1].length() >= 6)
            {
               lastFix.hour = Convert.toInt(sp[1].substring(0, 2));
               lastFix.minute = Convert.toInt(sp[1].substring(2, 4));
               lastFix.second = Convert.toInt(sp[1].substring(4, 6));
               lastFix.millis = 0;
            }
            if (sp[7].length() > 0)
               velocity = Convert.toDouble(sp[7]); //knots
            if (sp[8].length() > 0)
               direction = Convert.toDouble(sp[8]); //degrees
            ok = true;
         }
         else if (sp[0].equals("$GPGSA")) // guich@tc126_66
            try 
            {
               pdop = sp.length > 15 ? Convert.toDouble(sp[15]) : 0;
            } catch (Exception e) {}
      }
      return ok;
   }

   private String nextMessage() throws IOException
   {
      StringBuffer sb = this.sb;
      byte[] buf = this.buf;
      String ret = null;
      int len = 0;

      while (sp.readBytes(buf, 0, 1) == 1)
      {
         int c = buf[0] & 0xFF;
         if (c == '\n') // stop only after \n
            break;
         else
         {
            sb.append((char) c);
            if (++len > 512 || c > 128) // just trash?
               throw new IOException("Invalid port: only trash was retrieved.");
         }
      }
      if (len > 0)
      {
         // cut everything after the *
         if (len > 3 && sb.charAt(len - 3) == '*')
            sb.setLength(len - 3);
         ret = sb.toString();
      }
      sb.setLength(0);
      return ret;
   }
   
   protected void finalize()
   {
      this.stop();
   }
}
