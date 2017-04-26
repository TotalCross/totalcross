/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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

// Copyright (C) 2004 Nimkathana (www.nimkathana.com), USA
//
// License: LGPL
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1 of
// the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, execute to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

import totalcross.io.BufferedStream;
import totalcross.io.IOException;
import totalcross.net.*;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.util.Hashtable;

/**
 * Provides client-side HTTP communication
 * @version March 2006
 * @author Added to SuperWaba by Guich
 *@author
 * Maintained by Nimkathana
 * (<a href="http://www.nimkathana.com">www.nimkathana.com</a>)
 *@author
 * Original by IOP GmbH
 * (<a href="http://www.iop.de">www.iop.de</a>)
 */
public class StandardHttpClient
{
   protected int port;
   protected String uri;
   protected String host;
   protected String auth;
   protected Socket socket;
   protected BufferedStream reader;
   protected String hostname;
   /** The header contents, with both key and value lowercased. */
   public Hashtable htHeader;
   /** Sets the keep-alive flag. If this is false, the connection to the server will be closed
    * at each execution.
    */
   public boolean keepAlive;

   protected StringBuffer sb = new StringBuffer(2000);

   /**
    * @param hostname
    *           The server address to connect to
    * @param port
    *           The port on the server we want to connect to
    * @param uri
    *           The connecting URI. Defaults to "/RPC2"
    * @param openTimeout
    * @param readTimeout
    * @param writeTimeout
    * @throws XmlRpcException
    *            If the connection to the server could not be made
    */
   public StandardHttpClient(String hostname, int port, String uri, int openTimeout, int readTimeout, int writeTimeout) throws UnknownHostException, XmlRpcException
   {
      this.hostname = hostname;
      this.port = port;
      if (port < 1)
         port = 80;
      if (uri == null || uri.length() == 0)
         uri = "/RPC2";
      this.uri = uri; // guich@570_33: moved to here
      host = (port == 80) ? hostname : (hostname + ":" + port);
      try
      {
         socket = new Socket(hostname, port, openTimeout);
         socket.readTimeout = readTimeout;
         socket.writeTimeout = writeTimeout;
         reader = new BufferedStream(socket, BufferedStream.READ);
      }
      catch (totalcross.net.UnknownHostException e)
      {
         throw e;
      }
      catch (IOException e)
      {
         throw new XmlRpcException(e.getMessage());
      }
   }

   /**
    * @param hostname
    *           The server address to connect to
    * @param port
    *           The port on the server we want to connect to
    * @param uri
    *           The connecting URI. Defaults to "/RPC2"
    * @throws XmlRpcException
    *            If the connection to the server could not be made
    */
   public StandardHttpClient(String hostname, int port, String uri) throws UnknownHostException, XmlRpcException
   {
      this(hostname, port, uri, Socket.DEFAULT_OPEN_TIMEOUT, Socket.DEFAULT_READ_TIMEOUT, Socket.DEFAULT_WRITE_TIMEOUT);
   }

   /**
    * Base64 encodes the username and password given for basic server
    * authentication
    *
    * @param user
    *           The username for the server. Passing null disables
    *           authentication.
    * @param password
    *           The password for the username account on the server. Passing
    *           null disables authentication.
    */
   public void setBasicAuthentication(String user, String password)
   {
      if (user == null || password == null)
         auth = null;
      else
         auth = Base64.encode((user + ":" + password).getBytes()).trim();
   }

   /**
    * Writes the header followed by the given request body
    *
    * @throws IOException
    */
   protected void writeRequest(byte []requestBody, int len) throws IOException
   {
      StringBuffer header = writeRequestHeader(len).append(Convert.CRLF);
      byte[] requestHeader = Convert.getBytes(header);
      int written = 0;
      int writeRet;
      int requestHeaderLen = requestHeader.length;
      do
      {
         writeRet = socket.writeBytes(requestHeader, 0 + written, requestHeaderLen - written);
         if (writeRet > 0)
            written += writeRet;
      } while (written < requestHeaderLen);
      
      written = 0;
      do
      {
         writeRet = socket.writeBytes(requestBody, 0 + written, len - written); // write the given body
         if (writeRet > 0)
            written += writeRet;
      } while (written < len);
   }

   /**
    * Reads a line from the socket, retrying up to four times, with a 250ms
    * delay between the tries.
    * @throws IOException
    *
    * @since SuperWaba 5.68
    */
   public String readLine() throws IOException // guich@568_13
   {
      String line = null;
      for (int retry = 0; retry < 4; retry++)
         if ((line = reader.readLine()) != null)
            break;
         else
            Vm.sleep(250);
      return line;
   }

   /**
    * Checks if the response of the server has status 200
    *
    * @throws IOException
    */
   protected void checkResponse() throws XmlRpcException, IOException
   {
      String line = readLine();
      if (line == null)
         throw new XmlRpcException("Could not read response from Server.");
      String[] tokens = Convert.tokenizeString(line, ' ');
      if (tokens.length != 3)
         throw new XmlRpcException("Unexpected Response from Server: " + line);
      String httpversion = tokens[0];
      String statusCode = tokens[1];
      String statusMsg = tokens[2];
      keepAlive &= "HTTP/1.1".equals(httpversion);
      if (!"200".equals(statusCode))
         throw new XmlRpcException("Unexpected Response from Server: " + statusMsg);
   }

   /**
    * Places all the returning values frm the header in a hashtable for easy
    * access. Already parses the keepAlive flag.
    *
    * @throws IOException
    */
   protected void parseHeader() throws IOException
   {
      Hashtable ht = new Hashtable(13);
      while (true)
      {
         String line = reader.readLine();
         if (line == null)
            break;
         int dp = line.indexOf(':'); //  xxxx: yyyy
         if (dp >= 0)
         {
            String key = line.substring(0,dp).toLowerCase();
            String value = line.substring(dp+2).toLowerCase();
            ht.put(key,value);
         }
         else break; // guich@585_4: end of Http Response Header properties.
      }
      keepAlive &= ht.exists("keep-alive") || "keep-alive".equals(ht.get("connection"));
      htHeader = ht;
   }

   /** Used internally by readResponse and readResponseBytes. */
   protected StringBuffer privateReadResponse() throws XmlRpcException, IOException
   {
      StringBuffer xmlBuffer = sb;
      xmlBuffer.setLength(0);

      while (true)
      {
         String line = reader.readLine();
         if (line == null)
            break;
         xmlBuffer.append(line);
      }
      return xmlBuffer;
   }
   
   /**
    * Reads all the lines and place them in a single contiguous String.
    * @param returnAsBytes If true, the result can be casted to <code>byte[]</code>, otherwise, it can be casted to <code>String</code>.
    * @throws IOException
    */
   protected String readResponse() throws XmlRpcException, IOException
   {
      return privateReadResponse().toString();
   }

   /**
    * Reads all the lines and place them in a single contiguous String.
    * @param returnAsBytes If true, the result can be casted to <code>byte[]</code>, otherwise, it can be casted to <code>String</code>.
    * @throws IOException
    */
   protected Object readResponseBytes() throws XmlRpcException, IOException
   {
      return Convert.getBytes(privateReadResponse());
   }

   /**
    * Executes a HTTP request to the connected server
    *
    * @param requestBody
    *           The contents of the HTTP request. Headers are added
    *           appropriately by this method
    * @throws XmlRpcException
    *            If the server returns a status code other than 200 OK
    * @throws IOException
    */
   public String execute(byte[] requestBody) throws XmlRpcException, IOException
   {
      writeRequest(requestBody, requestBody.length);
      checkResponse();
      parseHeader();
      /* nothing to do with the header here */
      return (String)readResponse();
   }

   /**
    * Executes a HTTP request to the connected server.
    *
    * @param requestBody
    *           The contents of the HTTP request. Headers are added
    *           appropriately by this method
    * @throws XmlRpcException
    *            If the server returns a status code other than 200 OK
    * @throws IOException
    * @since TotalCross 1.23
    */
   public byte[] executeReturnBytes(byte[] requestBody) throws XmlRpcException, IOException
   {
      writeRequest(requestBody, requestBody.length);
      checkResponse();
      parseHeader();
      /* nothing to do with the header here */
      return (byte[])readResponseBytes();
   }

   /**
    * Terminates the server connection
    *
    * @throws IOException
    */
   protected void closeConnection() throws IOException
   {
      if (socket != null)
         socket.close();
      socket = null;
   }

   /*
    * Writes the headers for a HTTP request
    *@param requestLength
    * The length of the request's payload
    *@return
    * The headers correctly formatted in a StringBuffer.
    * The payload of the request can be appended to this StringBuffer.
    */
   protected StringBuffer writeRequestHeader(int requestLength)
   {
      StringBuffer requestHeader = new StringBuffer(256);
      requestHeader.append("POST " + uri + " HTTP/1.0\r\n");
      requestHeader.append("Accept-Encoding: identity\r\n");
      requestHeader.append("Content-Length: " + requestLength + Convert.CRLF);
      requestHeader.append("Host: " + host + Convert.CRLF);
      requestHeader.append("Content-Type: text/xml\r\n");
      requestHeader.append("User-Agent: IOP SWX XML-RPC 0.1\r\n");
      if (keepAlive)
         requestHeader.append("Connection: Keep-Alive\r\n");
      if (auth != null)
         requestHeader.append("Authorization: Basic " + auth + Convert.CRLF);
      // do not close with \r\n! This will be done later
      return requestHeader;
   }
}
