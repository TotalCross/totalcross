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
import totalcross.ui.image.ImageException;

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

public class BluetoothPrinter {
  public static final byte ESC = 27;
  public static final byte GS = 29;

  private static final byte[] ENTER = { (byte) '\n' };

  public static final byte IMAGE_MODE_8_SINGLE = (byte) 0;
  public static final byte IMAGE_MODE_8_DOUBLE = (byte) 1;
  public static final byte IMAGE_MODE_24_SINGLE = (byte) 0x20;
  public static final byte IMAGE_MODE_24_DOUBLE = (byte) 0x21;

  Stream con;

  /** Creates a new BluetoothPrinter instance, using PortConnector.BLUETOOTH port at 57600 baud rate.
   */
  public BluetoothPrinter() throws IOException {
    this(new PortConnector(PortConnector.BLUETOOTH, 57600));
  }

  /** Creates a new BluetoothPrinter instance, using the given PortConnector as bridge to the printer.
   * Note that PortConnector can use any port (including infrared), however, it is not guaranteed 
   * that it will work with that port. For example, IR does not work on Palm OS devices.
   */
  public BluetoothPrinter(Stream con) throws IOException {
    this.con = con;
  }

  /** Sends the given raw data to the printer. */
  public void write(byte[] data) throws IOException {
    con.writeBytes(data, 0, data.length);
  }

  /** Sends the given raw data to the printer. */
  public void write(byte[] data, int ofs, int len) throws IOException {
    con.writeBytes(data, ofs, len);
  }

  /** Sends an escape command to the printer. */
  public void escape(int command) throws IOException {
    write(new byte[] { ESC, (byte) command });
  }

  /** Sends an escape command to the printer. */
  public void escape(int command, int value1) throws IOException {
    write(new byte[] { ESC, (byte) command, (byte) value1 });
  }

  /** Sends an escape command to the printer. */
  public void escape(int command, int value1, int value2) throws IOException {
    write(new byte[] { ESC, (byte) command, (byte) value1, (byte) value2 });
  }

  /** Sends a GS command to the printer. */
  public void gs(int command, int value1) throws IOException {
    write(new byte[] { GS, (byte) command, (byte) value1 });
  }

  /** Sends an escape command to the printer. */
  public void gs(int command, int value1, int value2) throws IOException {
    write(new byte[] { GS, (byte) command, (byte) value1, (byte) value2 });
  }

  /** Prints the given String. */
  public void print(String str) throws IOException {
    write(str.getBytes());
  }

  /** Prints the given MonoImage. 
   * @deprecated Use the other print method without imageMode parameter.
   */
  @Deprecated
  public void print(MonoImage img, byte imageMode) throws ImageException, IOException {
    img.printTo(this);
  }

  /** Prints the given MonoImage. 
   * In the image, only black pixels are written. The maximum width is 576 pixels
   * for single density is 192, and for double density is 384; the image is trimmed 
   * to fit these values.
   */
  public void print(MonoImage img) throws ImageException, IOException {
    img.printTo(this);
  }

  /** Sends a new line to the printer. */
  public void newLine() throws IOException {
    write(ENTER);
  }

  /** Sends a number of new lines to the printer. */
  public void newLine(int count) throws IOException {
    while (--count >= 0) {
      write(ENTER);
    }
  }

  public void close() throws IOException {
    con.close();
  }
}
