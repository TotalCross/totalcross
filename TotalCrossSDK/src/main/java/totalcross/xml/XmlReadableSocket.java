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
import totalcross.io.IllegalArgumentIOException;
import totalcross.net.HttpStream;
import totalcross.net.URI;

/**
 * An XmlReadableSocket has a Socket stream that takes care of the HTTP
 * headers and starts reading at the message body.
 */
public class XmlReadableSocket extends HttpStream implements XmlReadable {
  private URI baseURI;
  private boolean caseInsensitive;

  /**
   * Constructor
   *
   * @param uri
   *           to connect to
   * @param options
   *           The options for this socket
   * @throws IOException
   * @throws IllegalArgumentIOException
   */
  public XmlReadableSocket(URI uri, Options options) throws IllegalArgumentIOException, IOException {
    // guich@510_14
    super(uri, options);
    baseURI = uri;
  }

  /**
   * Constructor
   *
   * @param uri
   *           to connect to
   * @throws IOException
   * @throws IllegalArgumentIOException
   */
  public XmlReadableSocket(URI uri) throws IllegalArgumentIOException, IOException {
    super(uri);
    baseURI = uri;
  }

  @Override
  public void readXml(XmlReader rdr) throws SyntaxException, totalcross.io.IOException {
    rdr.setCaseInsensitive(caseInsensitive);
    rdr.parse(socket, buffer, ofsStart, ofsEnd, readPos);
    socket.close();
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
