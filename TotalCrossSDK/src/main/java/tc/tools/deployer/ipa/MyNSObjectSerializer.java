// Copyright (C) 2012 SuperWaba Ltda.
// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer.ipa;

import java.io.UnsupportedEncodingException;

import com.dd.plist.NSObject;

public abstract class MyNSObjectSerializer {
  public static String toXMLPropertyList(NSObject o) {
    return o.toXMLPropertyList().replace("\r\n", "\n");
  }

  public static byte[] toXMLPropertyListBytesUTF8(NSObject o) throws UnsupportedEncodingException {
    return toXMLPropertyList(o).replace(".0</real>", "</real>").getBytes("UTF-8");
  }
}
