// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.io.device.printer;

import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.io.device.PortConnector;

/** This class extends BluetoothPrinter to send special MTP commands to the printer.
 * 
 * Reference: http://www.rego.ro/down/REGO-MPT-TM.pdf
 */

public class MPTPrinter extends BluetoothPrinter {
  /** Creates a new CitizenPrinter instance, using PortConnector.BLUETOOTH port at 57600 baud rate.
   */
  public MPTPrinter() throws IOException {
    super();
  }

  /** Creates a new CitizenPrinter instance, using the given PortConnector as bridge to the printer.
   * Note that PortConnector can use any port (including infrared), however, it is not guaranteed 
   * that it will work with that port. For example, IR does not work on Palm OS devices.
   */
  public MPTPrinter(PortConnector con) throws IOException {
    super(con);
  }

  /** Creates a new CitizenPrinter instance, using the given Stream as bridge to the printer.
   */
  public MPTPrinter(Stream con) throws IOException {
    super(con);
  }

  /** Resets the printer, canceling previous font and line spacing */
  public void reset() throws IOException {
    escape('@');
  }

  /** Sets the current font based on the given attributes. */
  public void setFont(boolean bold, boolean doubleWidth, boolean doubleHeight, boolean underline) throws IOException {
    escape('!', (bold ? (1 << 3) : 0) | (doubleHeight ? (1 << 4) : 0) | (doubleWidth ? (1 << 5) : 0)
        | (underline ? (1 << 7) : 0));
  }

  /** Sets the character size */
  public void setCharSize(boolean doubleW, boolean doubleH) throws IOException {
    gs('!', (doubleH ? 1 : 0) | (doubleW ? (1 << 4) : 0));
  }

  /** Sets the space character width, between 0 and 32. */
  public void setSpaceWidth(int w) throws IOException {
    escape(' ', w);
  }

  /** Sets the absolute position to start printing. */
  public void setXPos(int x) throws IOException {
    escape('$', x % 256, x / 256);
  }

  /** Turns off the printer. */
  public void turnOff() throws IOException {
    escape('+');
  }

  /** Single-space line height. */
  public void singleLineHeight() throws IOException {
    escape('2');
  }

  /** User-defined line height (n / 203 inches height). */
  public void setLineHeight(int n) throws IOException {
    escape('3', n);
  }

  /** Sets bold state. */
  public void bold(boolean on) throws IOException {
    escape('E', on ? 1 : 0);
  }

  /** Enables 90-degree rotation. */
  public void setRotation90(boolean on) throws IOException {
    escape('V', on ? 1 : 0);
  }

  /** Enables 180-degree rotation. */
  public void setRotation1800(boolean on) throws IOException {
    escape('{', on ? 1 : 0);
  }

  /** Sets the font density, between 0 and 5. */
  public void setDensity(int n) throws IOException {
    escape('Y', n);
  }

  /** Sets the horizontal text alignment. Use Control.LEFT, Control.CENTER, Control.RIGHT. */
  public void setHorizontalAlignment(int v) throws IOException {
    escape('a', v == totalcross.ui.Control.CENTER ? 1 : v == totalcross.ui.Control.RIGHT ? 2 : 0);
  }
}
