// Copyright (C) 2012 SuperWaba Ltda.
// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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
