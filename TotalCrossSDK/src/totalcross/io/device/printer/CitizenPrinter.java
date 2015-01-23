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

/** This class extends BluetoothPrinter to send special Citizen-like commands to the printer.
 * Tested with Citizen CMP-10 thermal printer.
 * <p>Instructions of how to setup the devices to work with the printer.
 * <ol> 
 * <li>First, run the self-test: turning the printer off, and pressing the LF + ON button at the same time and then releasing the LF button.
 * <li>Write down the last 2 bytes (4 letters) of the ADDRESS (e.g.: A4 08)
 * <li>Discover the "Citizen Systems" printer with the PDA.
 * <li>When asked for the PIN (password), write the last 4 letters of the address in UPPER CASE (e.g.: A408); if it fails,
 * write it in lower case (e.g.: a408).
 * <li>That's it. On some devices, you can choose to always use this printer as default Bluetooth device.
 * </ol>
 */

public class CitizenPrinter extends BluetoothPrinter
{
   /** Creates a new CitizenPrinter instance, using PortConnector.BLUETOOTH port at 57600 baud rate.
    */
   public CitizenPrinter() throws IOException
   {
      super();
   }
   
   /** Creates a new CitizenPrinter instance, using the given PortConnector as bridge to the printer.
    * Note that PortConnector can use any port (including infrared), however, it is not guaranteed 
    * that it will work with that port. For example, IR does not work on Palm OS devices.
    */
   public CitizenPrinter(PortConnector con) throws IOException
   {
      super(con);
   }
   
   /** Sets the current font based on the given attributes. */
   public void setFont(boolean fontA, boolean bold, boolean doubleWidth, boolean doubleHeight, boolean underline) throws IOException
   {
      escape('!', (fontA ? 0 : 1) | (bold ? (1 << 3) : 0) | (doubleHeight ? (1 << 4) : 0) | (doubleWidth ? (1 << 5) : 0) | (underline ? (1 << 7) : 0));
   }
   
   /** Sets the space character width, between 0 and 32. */
   public void setSpaceWidth(int w) throws IOException
   {
      escape(' ', w);
   }
   
   /** Sets the absolute position to start printing. */
   public void setXPos(int x) throws IOException
   {
      escape('$', x % 256, x / 256);
   }
   
   /** Turns off the printer. */
   public void turnOff() throws IOException
   {
      escape('+');
   }

   /** Double-space line height. */
   public void doubleLineHeight() throws IOException
   {
      escape('2');
   }
   
   /** Single-space line height. */
   public void singleLineHeight() throws IOException
   {
      escape('3',0);
   }
   
   /** User-defined line height (n / 203 inches height). */
   public void setLineHeight(int n) throws IOException
   {
      escape('3', n);
   }
   
   /** Sets bold state. */
   public void bold(boolean on) throws IOException
   {
      escape('E', on ? 1 : 0);
   }
   
   /** Enables 90-degree rotation. */
   public void setRotation90(boolean on) throws IOException
   {
      escape('V', on ? 1 : 0);
   }
   
   /** Enables 180-degree rotation. */
   public void setRotation1800(boolean on) throws IOException
   {
      escape('{', on ? 1 : 0);
   }
   
   /** Sets the font density, between 0 and 5. */
   public void setDensity(int n) throws IOException
   {
      escape('Y', n);
   }
   
   /** Sets the horizontal text alignment. Use Control.LEFT, Control.CENTER, Control.RIGHT. */
   public void setHorizontalAlignment(int v) throws IOException
   {
      escape('a', v == totalcross.ui.Control.CENTER ? 1 : v == totalcross.ui.Control.RIGHT ? 2 : 0);
   }
}
