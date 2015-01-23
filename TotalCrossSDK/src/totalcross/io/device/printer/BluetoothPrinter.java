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



package totalcross.io.device.printer;

import totalcross.io.*;
import totalcross.io.device.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

/** Used as interface to printers that uses Bluetooth to communicate with the device.
 * See the CitizenPrinter javadocs if you plan to use one.
 * <p>
 * Note: during tests, we found that some printers require to fill their buffer to start printing.
 * So, if you have problems printing, try the following code after you instantiate this class:
 * <pre>
 * cp = new CitizenPrinter(); // or new BluetoothPrinter();
 * cp.write(new byte[2048]); // fill the buffer
 * cp.newLine();
 * ...
 * </pre>
 */

public class BluetoothPrinter
{
   static final byte ESC = 27;
   private static final byte[] ENTER = {(byte)'\n'};
   PortConnector con;
   
   public static final byte IMAGE_MODE_8_SINGLE  = (byte)0;
   public static final byte IMAGE_MODE_8_DOUBLE  = (byte)1;
   public static final byte IMAGE_MODE_24_SINGLE = (byte)0x20;
   public static final byte IMAGE_MODE_24_DOUBLE = (byte)0x21;
   
   /** Creates a new BluetoothPrinter instance, using PortConnector.BLUETOOTH port at 57600 baud rate.
    */
   public BluetoothPrinter() throws IOException
   {
      this(new PortConnector(PortConnector.BLUETOOTH, 57600));
   }
   
   /** Creates a new BluetoothPrinter instance, using the given PortConnector as bridge to the printer.
    * Note that PortConnector can use any port (including infrared), however, it is not guaranteed 
    * that it will work with that port. For example, IR does not work on Palm OS devices.
    */
   public BluetoothPrinter(PortConnector con) throws IOException
   {
      this.con = con;
   }
   
   /** Sends the given raw data to the printer. */
   public void write(byte[] data) throws IOException
   {
      con.writeBytes(data, 0, data.length);
   }
   
   /** Sends an escape command to the printer. */
   public void escape(int command) throws IOException
   {
      write(new byte[]{ESC, (byte)command});
   }
   
   /** Sends an escape command to the printer. */
   public void escape(int command, int value1) throws IOException
   {
      write(new byte[]{ESC, (byte)command, (byte)value1});
   }

   /** Sends an escape command to the printer. */
   public void escape(int command, int value1, int value2) throws IOException
   {
      write(new byte[]{ESC, (byte)command, (byte)value1, (byte)value2});
   }
   
   /** Prints the given String. */
   public void print(String str) throws IOException
   {
      write(str.getBytes());
   }
   
   private static int getVerticalDensity(byte mode)
   {
      return mode >= IMAGE_MODE_24_SINGLE ? 24 : 8;
   }
   
   /** Prints the given MonoImage. See IMAGE_xxx for possible modes.
    * <b> IMPORTANT: the image height must be a multiple of the vertical density! </b>
    * If it's not, a new image will be created, forcing the correct behavior.
    * <br><br>
    * In the image, only black pixels are written. The maximum width 
    * for single density is 192, and for double density is 384; the image is trimmed 
    * to fit these values.
    */
   public void print(MonoImage img, byte imageMode) throws ImageException, IOException
   {
      int vd = getVerticalDensity(imageMode);
      int remains = img.getHeight() % vd;
      if (remains != 0) // if the number of vertical dots is not a multiple of the vertical density, fill the remaining with white
      {
         MonoImage img2 = new MonoImage(img.getWidth(), img.getHeight()+vd-remains);
         Graphics g = img2.getGraphics();
         g.backColor = Color.WHITE;
         g.fillRect(0,0,img2.getWidth(),img2.getHeight());
         g.drawImage(img, 0,0);
         img = img2;
      }
      img.printTo(this, imageMode);
   }
      
   /** Sends a new line to the printer. */
   public void newLine() throws IOException
   {
      write(ENTER);
   }
   
   public void close() throws IOException
   {
      con.close();
   }
}
