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
import totalcross.net.URI;

/** Make an XmlReadable from a byte array. */
public class XmlReadableByteArray implements XmlReadable {
  protected byte[] buf;
  protected URI baseURI;
  private boolean caseInsensitive;

  /**
   * Constructor
   *
   * @param source The byte array to be parsed
   */
  public XmlReadableByteArray(byte[] source) {
    buf = source;
  }

  protected XmlReadableByteArray() {
  }

  @Override
  public void readXml(XmlReader rdr) throws SyntaxException, IOException {
    rdr.setCaseInsensitive(caseInsensitive);
    rdr.parse(buf, 0, buf.length);
    //buf = null; - don't do this, otherwise HtmlBrowser's font change won't work 
  }

  @Override
  public URI getBaseURI() {
    return baseURI;
  }

  @Override
  public void setCaseInsensitive(boolean caseInsensitive) {
    this.caseInsensitive = caseInsensitive;
  }
}
