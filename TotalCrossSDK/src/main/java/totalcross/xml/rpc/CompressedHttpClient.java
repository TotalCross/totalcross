// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.xml.rpc;

import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.net.UnknownHostException;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.util.zip.ZLib;

/**
 * @author pvanhout
 */
public class CompressedHttpClient extends StandardHttpClient {
  private static ByteArrayStream out = new ByteArrayStream(65500);

  /**
   * See the constructor for the StandardHttpClient
   */
  public CompressedHttpClient(String hostname, int port, String uri, int openTimeout, int readTimeout, int writeTimeout)
      throws XmlRpcException, UnknownHostException {
    super(hostname, port, uri, openTimeout, readTimeout, writeTimeout);
  }

  public CompressedHttpClient(String hostname, int port, String uri) throws XmlRpcException, UnknownHostException {
    super(hostname, port, uri);
  }

  private Object readCompressedResponse(boolean asBytes) throws XmlRpcException, IOException {
    String v;
    int contentLength = 0;
    if ((v = (String) htHeader.get("content-length")) != null) {
      try {
        contentLength = Convert.toInt(v);
      } catch (InvalidNumberException ine) {
        throw new XmlRpcException("Invalid response from server - No Content");
      }
    }

    byte[] contentBuff = new byte[contentLength];
    int contentRead = reader.readBytes(contentBuff, 0, contentLength);
    if (contentRead != contentLength) {
      throw new XmlRpcException("Could not read answer: " + contentRead + " is less than " + contentLength);
    }
    ByteArrayStream is = new ByteArrayStream(contentBuff);
    ByteArrayStream os = out;
    os.reset();
    int inflateSize = ZLib.inflate(is, os);
    return asBytes ? (Object) os.toByteArray() : new String(os.getBuffer(), 0, inflateSize);
  }

  /**
   * Executes a HTTP request to the connected server
   *
   * @param requestBody
   *           The contents of the HTTP request. Headers are added
   *           appropriately by this method after the request is compressed
   * @throws XmlRpcException
   *            If the server returns a status code other than 200 OK
   */
  @Override
  public String execute(byte[] requestBody) throws XmlRpcException {
    return (String) privateExecute(requestBody, false);
  }

  /**
   * Executes a HTTP request to the connected server
   *
   * @param requestBody
   *           The contents of the HTTP request. Headers are added
   *           appropriately by this method after the request is compressed
   * @throws XmlRpcException
   *            If the server returns a status code other than 200 OK
   */
  @Override
  public byte[] executeReturnBytes(byte[] requestBody) throws XmlRpcException {
    return (byte[]) privateExecute(requestBody, true);
  }

  private Object privateExecute(byte[] requestBody, boolean asBytes) throws XmlRpcException {
    try {
      // compress the body
      ByteArrayStream is = new ByteArrayStream(requestBody);
      ByteArrayStream os = out;
      out.reset();
      int compressedSize = ZLib.deflate(is, os, ZLib.BEST_COMPRESSION);

      // send to the server
      writeRequest(os.getBuffer(), compressedSize);
      checkResponse();
      parseHeader();

      // read the answer
      boolean inflateResults = "deflate".equals(htHeader.get("content-encoding"));
      return inflateResults ? readCompressedResponse(asBytes) : asBytes ? readResponseBytes() : readResponse();
    } catch (IOException e) {
      throw new XmlRpcException(e.getMessage());
    }
  }

  /** Writes the headers for a HTTP request, adding the deflate method as content-encoding.  */
  @Override
  protected StringBuffer writeRequestHeader(int requestLength) {
    StringBuffer requestHeader = super.writeRequestHeader(requestLength);
    requestHeader.append("Content-Encoding: deflate\r\n"); // append our specific header
    return requestHeader;
  }
}
