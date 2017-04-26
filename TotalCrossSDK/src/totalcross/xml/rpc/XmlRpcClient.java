/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2001 Apache Software License                                   *
 *  Copyright (C) 2001-2012 SuperWaba Ltda.                                      *
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

package totalcross.xml.rpc;

/*
 * The Apache Software License, Version 1.1 Copyright (c) 2001 The Apache Software Foundation. All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution. 3. The end-user documentation included with the redistribution, if any, must include the following
 * acknowledgment: "This product includes software developed by the Apache Software Foundation
 * (http://www.apache.org/)." Alternately, this acknowledgment may appear in the software itself, if and wherever such
 * third-party acknowledgments normally appear. 4. The names "XML-RPC" and "Apache Software Foundation" must not be used
 * to endorse or promote products derived from this software without prior written permission. For written permission,
 * please contact apache@apache.org. 5. Products derived from this software may not be called "Apache", nor may "Apache"
 * appear in their name, without prior written permission of the Apache Software Foundation. THIS SOFTWARE IS PROVIDED
 * ''AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION
 * OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. This software
 * consists of voluntary contributions made by many individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see <http://www.apache.org/>.
 */

import totalcross.io.IOException;
import totalcross.net.Base64;
import totalcross.net.UnknownHostException;
import totalcross.sys.Time;
import totalcross.ui.html.TagDereferencer;
import totalcross.util.*;
import totalcross.xml.SyntaxException;
import totalcross.xml.XmlReader;

/**
 * Handles XML-RPCs to a server. This object cannot be reused.
 *
 * @version March 2006
 * @author Added to SuperWaba by Guich (THIS FILE HAS BEEN HEAVILY CHANGED BY GUICH)
 * @author Maintained by Nimkathana (<a href="http://www.nimkathana.com">www.nimkathana.com</a>)
 * @author Original by IOP GmbH (<a href="http://www.iop.de">www.iop.de</a>)
 */
public class XmlRpcClient extends XmlReader
{
   private StandardHttpClient httpClient;
   protected XmlRpcContentHandler xmlHandler;
   private XmlWriter writer;

   /**
    * @param hostname
    *           The server address to connect to
    * @param port
    *           The port on the server to connect to
    * @param uri
    *           The connecting URI. Defaults to "/RPC2"
    * @throws XmlRpcException
    *            If the connection to the server was unsuccessful
    */
   public XmlRpcClient(String hostname, int port, String uri) throws XmlRpcException, UnknownHostException
   {
      this(new StandardHttpClient(hostname, port, uri));
   }

   /**
    * Creates a xmlrpc client with compression if the given flag is true.
    */
   public XmlRpcClient(String hostname, int port, String uri, boolean doCompression) throws XmlRpcException, UnknownHostException
   {
      this(doCompression ?
            new CompressedHttpClient(hostname, port, uri) :
               new StandardHttpClient(hostname, port, uri));
   }

   public XmlRpcClient(String hostname, int port, String uri, int openTimeout, int readTimeout, int writeTimeout, boolean doCompression) throws XmlRpcException, UnknownHostException
   {
      this(doCompression ?
            new CompressedHttpClient(hostname, port, uri, openTimeout, readTimeout, writeTimeout) :
               new StandardHttpClient(hostname, port, uri, openTimeout, readTimeout, writeTimeout));
   }

   public XmlRpcClient(StandardHttpClient httpClient) throws XmlRpcException, UnknownHostException
   {
      this.httpClient = httpClient;
      xmlHandler = new XmlRpcContentHandler();
      setContentHandler(xmlHandler);
      writer = new XmlWriter();
   }

   /**
    * Generates an XML-RPC request and sends it to the server. Parses the result
    * and returns the corresponding Java object.
    *
    * @param method
    *           The remote procedure to call
    * @param params
    *           The parameters to the corresponding <code>method</code>
    * @throws XmlRpcException
    *            If the remote procedure call was unsuccessful
    * @throws IOException
    */
   public Object execute(String method, Vector params) throws XmlRpcException, IOException
   {
      writer.reset();
      writeRequest(method, params);
      byte[] writerBytes = writer.getBytes();
      byte[] response = httpClient.executeReturnBytes(writerBytes);

      // parse the response
      try
      {
         parse(response, 0, response.length);
      }
      catch (SyntaxException e)
      {
         throw new XmlRpcException(e.getMessage());
      }

      // httpClient keepalive is always false if XmlRpc.keepalive is false
      if (!httpClient.keepAlive)
         httpClient.closeConnection();

      Object result = xmlHandler.result;

      if (xmlHandler.faultOccured)
      {
         // this is an XML-RPC-level problem, i.e. the server reported an error.
         // throw a XmlRpcException.
         Hashtable f = (Hashtable) result;
         //flsobral@tc123_29: fixed handling of faultString and faultCode
         Object temp = f.get("faultString");
         String faultString = temp instanceof String ? (String) temp : temp.toString();
         temp = f.get("faultCode");
         String faultCode = temp instanceof String ? (String) temp : temp.toString();
         throw new XmlRpcException(faultCode + ": " + faultString.trim());
      }

      return result;
   }

   protected int getTagCode(byte[] b, int offset, int count)
   {
      int hash = TagDereferencer.hashCode(b, offset, count);
      try {return XmlRpcValue.tag2code.get(hash);} catch (ElementNotFoundException e) {return -1;}
   }

   /*
    * Generate an XML-RPC request from a method name and a parameter vector.
    */
   private void writeRequest(String method, Vector params)
   {
      XmlWriter writer = this.writer;
      writer.startElement("methodCall");

      writer.startElement("methodName");
      writer.write(method);
      writer.endElement("methodName");

      writer.startElement("params");
      int n = params.size();
      for (int i = 0; i < n; i++)
      {
         writer.startElement("param");
         writeObject(params.items[i]);
         writer.endElement("param");
      }
      writer.endElement("params");
      writer.endElement("methodCall");
   }

   /**
    * Writes the XML representation of a supported Java object to the XML writer.
    *
    * @param what
    *           The object to write
    */
   protected void writeObject(Object what)
   {
      XmlWriter writer = this.writer;
      writer.startElement("value");
      if (what == null)
      {
         throw new RuntimeException("null value not supported by XML-RPC");
      }
      else if (what instanceof String)
      {
         writer.chardata(what.toString());
      }
      else if (what instanceof Properties.Value) // handles: int, double, long, boolean
      {
         Properties.Value val = (Properties.Value)what;
         writer.startElement(val.typeStr);
         writer.write(what.toString());
         writer.endElement(val.typeStr);
      }
      else if (what instanceof byte[])
      {
         writer.startElement("base64");
         writer.write(Base64.encode((byte[]) what));
         writer.endElement("base64");
      }
      else if (what instanceof Vector)
      {
         writer.startElement("array");
         writer.startElement("data");
         Vector v = (Vector) what;
         int n = v.size();
         for (int i = 0; i < n; i++)
            writeObject(v.items[i]);
         writer.endElement("data");
         writer.endElement("array");
      }
      else if (what instanceof Hashtable)
      {
         writer.startElement("struct");
         Hashtable h = (Hashtable) what;
         Vector keys = h.getKeys();
         Vector vals = h.getValues();
         int n = keys.size();
         for (int i = 0; i < n; i++)
         {
            Object nextkey = keys.items[i];
            Object nextval = vals.items[i];
            writer.startElement("member");
            writer.startElement("name");
            writer.write(nextkey.toString());
            writer.endElement("name");
            writeObject(nextval);
            writer.endElement("member");
         }
         writer.endElement("struct");
      }
      else if (what instanceof Time)
      {
         writer.startElement("dateTime.iso8601");
         Time dateTime = (Time) what;
         writer.write(dateTime.toIso8601());
         writer.endElement("dateTime.iso8601");
      }
      else
         throw new RuntimeException("unsupported Java type: " + what.getClass());
      writer.endElement("value");
   }
}
