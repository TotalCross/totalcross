// Copyright (C) 2003-2004 Pierre G. Richard
// Copyright (C) 2004-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.xml;

import totalcross.io.File;
import totalcross.io.IOException;

/**
 * Make an XmlReadable from a File
 * Example:
 * <PRE>
 *    XmlReader rdr = new XmlReader();
 *    rdr.setContentHandler(...);
 *    File f = new File(...);
 *    rdr.parse(new XmlReadableFile(f));
 * </PRE>
 */
public class XmlReadableFile extends XmlReadableByteArray {
  /**
   * Constructor
   *
   * @param f The file you want to read. Be sure to don't open it with DONT_OPEN mode.
   * @throws IOException
   */
  public XmlReadableFile(File f) throws totalcross.io.IOException {
    buf = new byte[f.getSize()];
    f.readBytes(buf, 0, buf.length);
  }
}
