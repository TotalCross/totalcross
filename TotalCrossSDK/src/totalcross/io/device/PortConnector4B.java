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

// $Id: PortConnector4B.java,v 1.17 2011-01-04 13:19:21 guich Exp $

package totalcross.io.device;

import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;

import net.rim.device.api.bluetooth.BluetoothSerialPort;
import net.rim.device.api.system.USBPort;
import totalcross.io.IOException;
import totalcross.io.IllegalArgumentIOException;
import totalcross.io.Stream;

public class PortConnector4B extends Stream
{
   public int writeTimeout = 6000;
   public int readTimeout = 6000;
   public boolean stopWriteCheckOnTimeout = true;

   private CommConnection conn;
   private InputStream is;
   private OutputStream os;
   private boolean dontFinalize;

   public static int readTries = 10;
   public static final int DEFAULT = 0;
   public static final int IRCOMM = 0x1000;
   public static final int SIR = 0x1001;
   public static final int USB = 0x1002;
   public static final int BLUETOOTH = 0x1003;
   public static final int PARITY_NONE = 0;
   public static final int PARITY_EVEN = 1;
   public static final int PARITY_ODD = 2;

   public PortConnector4B(int number, int baudRate, int bits, boolean parity, int stopBits) throws IllegalArgumentIOException, IOException
   {
      this(number, baudRate, bits, parity ? PARITY_EVEN : PARITY_NONE, stopBits);
   }

   public PortConnector4B(int number, int baudRate, int bits, int parity, int stopBits) throws IllegalArgumentIOException, IOException
   {
      try
      {
         conn = (CommConnection)Connector.open(getURL(number, baudRate, bits, parity, stopBits), Connector.READ_WRITE, true);
         is = conn.openInputStream();
         os = conn.openOutputStream();
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(ex.getMessage());
      }
   }

   public PortConnector4B(int number, int baudRate) throws IllegalArgumentIOException, IOException
   {
      this(number, baudRate, 8, false, 1);
   }

   public void close() throws IOException
   {
      if (conn == null)
         throw new IOException("Invalid port connector handler");

      try
      {
         is.close();
         os.close();

         conn.close();
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(ex.getMessage());
      }
      finally
      {
         conn = null;
         dontFinalize = !dontFinalize;
      }
   }

   public final void setFlowControl(boolean on) throws IOException
   {
      if (conn == null)
         throw new IOException("Invalid port connector handler");
   }

   public int readBytes(byte buf[], int start, int count) throws IOException
   {
      if (conn == null)
         throw new IOException("Invalid port connector handler");

      try
      {
         return is.read(buf, start, count);
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(ex.getMessage());
      }
   }

   public final int readCheck() throws IOException
   {
      if (conn == null)
         throw new IOException("Invalid port connector handler");

      try
      {
         return is.available();
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(ex.getMessage());
      }
   }

   public int writeBytes(byte buf[], int start, int count) throws IOException
   {
      if (conn == null)
         throw new IOException("Invalid port connector handler");

      try
      {
         os.write(buf, start, count);
         return count;
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(ex.getMessage());
      }
   }

   protected void finalize()
   {
      try
      {
         close();
      }
      catch (IOException e)
      {
      }
   }

   private static String getURL(int number, int baudRate, int bits, int parity, int stopBits) throws IOException
   {
      String url = ";baudrate=" + baudRate;
      url += ";bitsperchar=" + bits;
      url += ";parity=" + (parity == PortConnector.PARITY_ODD ? "odd" : parity == PortConnector.PARITY_EVEN ? "even" : "none");
      url += ";stopbits=" + stopBits;

      switch (number)
      {
         case PortConnector.IRCOMM:
            throw new IOException("Device does not support IRCOMM port");
         case PortConnector.SIR:
            throw new IOException("Device does not support SIR port");
         case PortConnector.USB:
            if (!USBPort.isSupported())
               throw new IOException("Device does not support USB port");
            else
               url = "comm:USB" + url + ";channel=SW";
            break;
         case PortConnector.BLUETOOTH:
            if (!BluetoothSerialPort.isSupported())
               throw new IOException("Device does not support BLUETOOTH port");
            else
               url = BluetoothSerialPort.getSerialPortInfo()[0].toString();
            break;
         default:
            url = "comm:COM1" + url;
            break;
      }

      return url;
   }
}