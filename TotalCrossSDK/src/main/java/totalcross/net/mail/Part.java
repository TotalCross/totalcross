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

package totalcross.net.mail;

import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.sys.Convert;
import totalcross.util.Vector;

/**
 * This class represents a MIME body part, which are contained by Multipart objects.
 * 
 * @since TotalCross 1.13
 */
public class Part {
  public static final String INLINE = "inline";
  public static final String ATTACHMENT = "attachment";

  static final String MULTIPART = "multipart/";
  public static final String PLAIN = "text/plain";
  public static final String HTML = "text/html";
  public static final String BINARY = "application/octet-stream";

  /**
   * Defines if this part's content should be inlined in the body of the message, or attached as a file.<br>
   * Its value MUST be set to either INLINE (default value) or ATTACHMENT, using the constants defined by the class
   * Part.
   */
  public String disposition = INLINE;
  /**
   * File name of the attachment represented by this part (if any).<br>
   * If assigned with a non-null value, the field disposition value is ignored and the part treated as ATTACHMENT.
   */
  public String fileName;

  protected Object content;
  protected String mimeType;
  protected DataContentHandler contentHandler;
  protected PartHeaders headers = new PartHeaders();

  /**
   * Add this value to the existing values for this header name.
   * 
   * @param name
   *           the name of this header
   * @param value
   *           the value for this header
   * @since TotalCross 1.22
   */
  public void addHeader(String name, String value) {
    headers.addHeader(name, value);
  }

  /**
   * Returns the Content-Type of the content of this part. Returns null if the Content-Type could not be determined.<br>
   * 
   * The MIME typing system is used to name Content-types.
   * 
   * @return The ContentType of this part, or null if it could not be determined
   * 
   * @since TotalCross 1.22
   */
  public String getContentType() {
    return mimeType;
  }

  /**
   * A convenience method for setting this part's content.<br>
   * 
   * Note that a DataContentHandler class for the specified type should be available, otherwise the content will be
   * handled by the BinaryContentHandler, which writes the content as a base 64 encoded String.<br>
   * 
   * @see totalcross.net.mail.BinaryContentHandler
   * @param content
   *           this part's content
   * @param mimeType
   *           content's MIME type
   * @throws IllegalArgumentException
   *            if the given content is a Part object
   * @since TotalCross 1.13
   */
  public void setContent(Object content, String mimeType) {
    if (content instanceof Part) {
      throw new IllegalArgumentException("A body part cannot be set as content of another body part");
    }
    this.content = content;
    this.mimeType = mimeType;
    contentHandler = DataHandler.getDataContentHandler(mimeType);
  }

  /**
   * This method sets the given Multipart object as this part's content.
   * 
   * @param multipart
   *           the multipart object that is the part's content
   * @since TotalCross 1.13
   */
  public void setContent(Multipart multipart) {
    this.content = multipart;
    contentHandler = null;
    this.mimeType = MULTIPART + multipart.subType + "; boundary=\"" + new String(multipart.boundary) + "\"";
  }

  /**
   * A convenience method for setting this part's content based on the type of the passed object.<br>
   * If the given object type is Multipart, it's MIME type is set to "multipart/mixed".<br>
   * If the given object type is String, it's MIME type is set to "text/plain".<br>
   * Otherwise, the given object will be handled as "application/octet-stream" by the BinaryContentHandler.
   * 
   * @param content
   *           this part's content
   * @throws IllegalArgumentException
   *            if the given content is a Part object
   * @since TotalCross 1.13
   */
  public void setContent(Object content) {
    if (content instanceof Part) {
      throw new IllegalArgumentException("A body part cannot be set as content of another body part");
    }
    if (content instanceof Multipart) {
      setContent((Multipart) content);
    } else {
      if (content instanceof String) {
        this.mimeType = PLAIN;
      } else {
        this.mimeType = BINARY;
      }

      this.content = content;
      contentHandler = DataHandler.getDataContentHandler(mimeType);
    }
  }

  /**
   * A convenience method that sets the given String as this part's content with a MIME type of "text/plain".
   * 
   * @param text
   *           the text that is this part's content.
   * @since TotalCross 1.13
   */
  public void setText(String text) {
    this.content = text;
    this.mimeType = PLAIN;
    contentHandler = DataHandler.getDataContentHandler(mimeType);
  }

  /**
   * Writes this part to the given Stream. The output is typically an aggregation of the Part attributes and an
   * appropriately encoded byte stream from its 'content'.
   * 
   * Classes that extends the Part class decide on the appropriate encoding algorithm to be used.
   * 
   * The received Stream is typically used for sending.
   * 
   * @param stream
   *           the output stream that will receive this part's encoded representation
   * @throws IOException
   *            if an error occurs writing to the stream
   * @throws MessagingException
   *            if an error occurs fetching the data to be written
   * @since TotalCross 1.13
   */
  public void writeTo(Stream stream) throws IOException, MessagingException {
    addHeader("Content-Type", mimeType); //flsobral@tc125: It is not an error to use "type", but better safe than sorry.

    String headerLine;
    if (headers != null && (headerLine = headers.getHeaderString()) != null) {
      stream.writeBytes(headerLine);
    }
    if (content instanceof Multipart) {
      ((Multipart) content).writeTo(stream);
    } else {
      contentHandler.writeTo(this, mimeType, stream);
    }
  }

  /**
   * Returns the content of this part.
   * 
   * @return the content of this part.
   * @since TotalCross 1.13
   */
  public Object getContent() {
    return content; //flsobral@tc124_26: now we return the content, regardless of its type.
  }
}

/**
 * Collection of headers from a Part.
 * 
 * @since TotalCross 1.22
 */
class PartHeaders {
  private Vector headers;

  PartHeaders() {
    headers = new Vector();
  }

  void addHeader(String name, String value) {
    Header newHeader = new Header(name, value);

    int insertIndex = -1;
    for (int i = headers.size() - 1; i >= 0 && insertIndex == -1; i--) {
      if (((Header) headers.items[i]).hash == newHeader.hash) {
        insertIndex = i;
      }
    }

    if (insertIndex == -1) {
      headers.addElement(newHeader);
    } else {
      headers.insertElementAt(newHeader, insertIndex);
    }
  }

  String getHeaderString() {
    int count = headers.size();
    if (count <= 0) {
      return null;
    }

    StringBuffer headerBuf = new StringBuffer(256);
    for (int i = 0; i < count; i++) {
      Header header = (Header) headers.items[i];
      headerBuf.append(header.name).append(": ").append(header.value).append(Convert.CRLF);
    }
    return headerBuf.toString();
  }
}

/**
 * A Part header.
 * 
 * @since TotalCross 1.22
 */
class Header {
  String name;
  String value;
  int hash;

  Header(String name, String value) {
    this.name = name;
    this.value = value;
    this.hash = name.toLowerCase().hashCode();
  }
}
