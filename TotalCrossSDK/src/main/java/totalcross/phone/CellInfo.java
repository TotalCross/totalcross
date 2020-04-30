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

package totalcross.phone;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;
import totalcross.io.LineReader;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;

/** Contains information about the anthena that this cell phone is receiving signal. 
 * Used by the GPS class.
 * @since TotalCross 1.22
 */

public class CellInfo {
  public static String cellId;
  public static String mnc;
  public static String mcc;
  public static String lac;
  public static int signal;

  static CellInfo instance = new CellInfo();

  private CellInfo() {
    loadResources();
  }

  @ReplacedByNativeOnDeploy
  public static void update() {
  }

  @ReplacedByNativeOnDeploy
  private void loadResources() {
  }

  @ReplacedByNativeOnDeploy
  private void releaseResources() {
  }

  @Override
  protected void finalize() {
    releaseResources();
  }

  public static boolean isSupported() {
    return Settings.isWindowsCE() || Settings.platform.equals(Settings.ANDROID);
  }

  /** Converts the current cellId and lac into the latitude and longitude.
   * You need to manually update the values by calling the <code>update</code> method.
   * 
   * This method requires direct connection to the internet and uses the google mmap service.
   * @since TotalCross 1.3
   */
  public static double[] toCoordinates() throws Exception {
    double[] ret = null;

    if (cellId == null || lac == null) {
      return null;
    }

    ByteArrayStream bas = new ByteArrayStream(256);
    DataStream ds = new DataStream(bas);

    ds.writeBytes("POST /glm/mmap HTTP/1.1\r\n");
    ds.writeBytes("Content-Type: application/binary\r\n");
    ds.writeBytes("Content-Length: 72\r\n");
    ds.writeBytes("\r\n");
    ds.writeShort(21);
    ds.writeLong(0);
    ds.writeShort(2);
    ds.writeBytes("en");
    ds.writeShort(7);
    ds.writeBytes("Android");
    ds.writeShort(3);
    ds.writeBytes("1.0");
    ds.writeShort(3);
    ds.writeBytes("Web");
    ds.writeByte(27);
    ds.writeInt(0);
    ds.writeInt(0);
    ds.writeInt(3);
    ds.writeShort(0);
    ds.writeInt(Convert.toInt(cellId));
    ds.writeInt(Convert.toInt(lac));
    ds.writeInt(0);
    ds.writeInt(0);
    ds.writeInt(0);
    ds.writeInt(0);

    totalcross.net.Socket sock = new totalcross.net.Socket("www.google.com", 80, 20000);
    sock.writeBytes(bas.toByteArray());

    Vm.sleep(250); // wait for a response

    // read the whole answer into a byte array
    byte[] buf = new byte[1024];
    int total = sock.readBytes(buf);
    bas = new ByteArrayStream(buf, total);
    ds = new DataStream(bas);

    LineReader lr = new LineReader(bas);
    lr.returnEmptyLines = true;
    String line = lr.readLine(); // first must be HTTP/1.1 200 OK
    if (line != null && line.indexOf(" 200 ") >= 0) {
      // find out the length of the data-chunk
      int len = 0;
      while ((line = lr.readLine()) != null) // skip all other header information
      {
        if (line.toLowerCase().startsWith("content-length:")) {
          len = Convert.toInt(line.substring(16).trim());
          break;
        } else if (line.equals("")) {
          break;
        }
      }
      if (len > 0) {
        // point to the data
        bas.setPos(total - len);
        ds.readShort();
        ds.readByte();
        int code = ds.readInt();
        if (code == 0) {
          ret = new double[] { ds.readInt() / 1e6d, ds.readInt() / 1e6d };
        }
      }
    }

    sock.close();

    return ret;
  }
}
