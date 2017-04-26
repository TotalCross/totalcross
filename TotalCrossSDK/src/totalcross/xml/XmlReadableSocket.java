/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003-2004 Pierre G. Richard                                    *
 *  Copyright (C) 2003-2012 SuperWaba Ltda.                                      *
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



package totalcross.xml;

import totalcross.io.IOException;
import totalcross.io.IllegalArgumentIOException;
import totalcross.net.HttpStream;
import totalcross.net.URI;

/**
 * An XmlReadableSocket has a Socket stream that takes care of the HTTP
 * headers and starts reading at the message body.
 */
public class XmlReadableSocket extends HttpStream implements XmlReadable
{
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
   public XmlReadableSocket(URI uri, Options options) throws IllegalArgumentIOException, IOException
   {
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
   public XmlReadableSocket(URI uri) throws IllegalArgumentIOException, IOException
   {
      super(uri);
      baseURI = uri;
   }

   public void readXml(XmlReader rdr) throws SyntaxException, totalcross.io.IOException
   {
      rdr.setCaseInsensitive(caseInsensitive);
      rdr.parse(socket, buffer, ofsStart, ofsEnd, readPos);
      socket.close();
   }

   public URI getBaseURI()
   {
      return baseURI;
   }

   public void setCaseInsensitive(boolean caseInsensitive)
   {
      this.caseInsensitive = caseInsensitive;
   }
}
