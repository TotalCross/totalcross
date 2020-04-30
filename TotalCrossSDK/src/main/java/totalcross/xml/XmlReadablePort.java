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

import totalcross.io.device.PortConnector;
import totalcross.net.URI;

/**
 * Make an XmlReadable from a PortConnector Example:
 *
 * <PRE>
 *    XmlReader rdr = new XmlReader();
 *    rdr.setContentHandler(...);
 *    rdr.parse(new XmlReadablePort(PortConnector.DEFAULT, 19200));
 * </PRE>
 */
public class XmlReadablePort extends XmlReadableByteArray {
  private static final int BUFSIZE = 4096;
  private PortConnector stream;
  private int ofsEnd;

  /**
   * Constructor
   *
   * @param number
   *           port number. On Windows, this is the number of the COM port.
   * @param baudRate
   *           baud rate
   * @param baseURI
   *           URI for this XmlReadable
   * @throws totalcross.io.IOException
   */
  public XmlReadablePort(int number, int baudRate, URI baseURI) throws totalcross.io.IOException {
    this(new PortConnector(number, baudRate), baseURI);
  }

  /**
   * Constructor
   *
   * @param port The constructed PortConnector
   * @param baseURI
   *           URI for this XmlReadable
   * @throws totalcross.io.IOException
   */
  public XmlReadablePort(PortConnector port, URI baseURI) throws totalcross.io.IOException {
    stream = port;
    this.baseURI = baseURI;
    buf = new byte[BUFSIZE];
    ofsEnd = stream.readBytes(buf, 0, BUFSIZE);
    if (ofsEnd == -1) {
      buf = null;
    }
    stream.close();
  }

  @Override
  public void readXml(XmlReader rdr) throws SyntaxException, totalcross.io.IOException {
    rdr.parse(stream, buf, 0, ofsEnd, 0);
    stream.close();
    buf = null;
  }
}
