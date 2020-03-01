/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
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

package totalcross.phone;

import java.util.ArrayList;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;
import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.sys.Settings;

@Deprecated
public class PushNotification {
  @Deprecated
  public static String readToken() {
    String ret = null;
    String name = Settings.vmPath + "/push_token.dat";
    try {
      // get an exclusive write to the file
      byte[] b = new File(name, File.READ_WRITE).readAndClose();
      ByteArrayStream bas = new ByteArrayStream(b);
      DataStream ds = new DataStream(bas);
      ret = new String(ds.readChars());
    } catch (FileNotFoundException fnfe) {
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ret;
  }

  @Deprecated
  public static String[] readMessages() {
    ArrayList<String> ret = new ArrayList<String>(5);
    String name = Settings.vmPath + "/push_messages.dat";
    try {
      // get an exclusive write to the file
      byte[] b = new File(name, File.READ_WRITE).readAndDelete();
      ByteArrayStream bas = new ByteArrayStream(b);
      DataStream ds = new DataStream(bas);
      while (bas.available() > 0) {
        ret.add(new String(ds.readChars()));
      }
    } catch (FileNotFoundException fnfe) {
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ret.toArray(new String[0]);
  }
}
