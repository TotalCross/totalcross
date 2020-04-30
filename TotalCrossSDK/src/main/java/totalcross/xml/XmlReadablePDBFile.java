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

import totalcross.io.IOException;
import totalcross.io.PDBFile;
import totalcross.net.URI;

/**
 * Make an XmlReadable from a PDBFile
 * Example:
 * <PRE>
 *    XmlReader rdr = new XmlReader();
 *    rdr.setContentHandler(...);
 *    rdr.parse(new XmlReadablePDBFile("HtmlSampleDB.memo.DATA",1));
 * </PRE>
 */
public class XmlReadablePDBFile extends XmlReadableByteArray {
  /**
   * Constructor
   *
   * @param baseURI URI for this XmlReadable
   * @throws IOException
   *
   * Impl Note:
   * We need to convert to the TotalCross representation of a DM scheme:
   * From:
   * <pre>
   *    "dm://memo.DATA/HtmlSampleDB/html/18"
   *    Scheme:    "dm"
   *    Authority: "memo.DATA"
   *    UserInfo:  (undefined)
   *    Host:      "memo.DATA"
   *    Port:      "-1"
   *    Path:      "/HtmlSampleDB/html/0"
   * </pre>
   * To:
   * <pre>
   *    "HtmlSampleDB.memo.DATA"  recordNo=18
   * </pre>
   * Does not take into account the Category filter
   * ("html" in the sample above.)
   */
  public XmlReadablePDBFile(URI baseURI) throws totalcross.io.IOException {
    this(baseURI.path.substring(1, baseURI.path.indexOf((byte) '/', 1)).toString() + '.' + baseURI.host.toString(),
        baseURI.path.convertToInt(baseURI.path.lastIndexOf((byte) '/') + 1));
    this.baseURI = baseURI;
  }

  /**
   * Constructor
   *
   * @param file The file name to open. E.G.: HtmlSampleDB.memo.DATA
   * @param recordNo The record number to read
   * @throws IOException If an I/O error occurs or if the record's size is 0.
   */
  public XmlReadablePDBFile(String file, int recordNo) throws totalcross.io.IOException {
    PDBFile c = new PDBFile(file, PDBFile.READ_WRITE);
    c.setRecordPos(recordNo);
    int size = c.getRecordSize();
    if (size <= 0) {
      throw new IOException("Record size is 0.");
    }
    buf = new byte[size];
    c.readBytes(buf, 0, size);
    c.close();
  }
}
