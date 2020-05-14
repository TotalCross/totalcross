// Copyright (C) 2003-2004 Pierre G. Richard
// Copyright (C) 2004-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.xml;

/**
 * Make an XmlReadable from a String.
 * Example:
 * <PRE>
 *    XmlReader rdr = new XmlReader();
 *    rdr.setContentHandler(...);
 *    rdr.parse(new XmlReadableString("Hello World!"));
 * </PRE>
 */
public class XmlReadableString extends XmlReadableByteArray {
  /**
   * Constructor
   *
   * @param source String that contains the XML readable HTML
   */
  public XmlReadableString(String source) {
    super(source.getBytes());
  }
}
