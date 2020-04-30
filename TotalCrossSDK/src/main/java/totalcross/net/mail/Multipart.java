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

import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.net.Socket;
import totalcross.sys.Convert;
import totalcross.sys.Time;
import totalcross.util.Vector;

/**
 * Multipart is a container that holds multiple body parts.
 * 
 * @since TotalCross 1.13
 */
public class Multipart {
  private static final byte[] slash2x = { '-', '-' };
  /** List of parts contained in this container */
  protected Vector parts = new Vector();

  protected String subType;

  public static final String MIXED = "mixed";
  public static final String FORM_DATA = "form-data";

  final byte[] boundary = ("===" + new Time().getTimeLong()).getBytes();

  public Multipart() {
    subType = MIXED;
  }

  public Multipart(String subType) {
    this.subType = subType;
  }

  /**
   * Adds a MIME body part to this part container.
   * 
   * @param part
   *           part to be added to this part container.
   * @since TotalCross 1.13
   */
  public void addPart(Part part) {
    parts.addElement(part);
  }

  /**
   * Output an appropriately encoded bytestream to the given stream, which is typically used for sending.
   * 
   * @param stream
   *           the output stream that will receive the encoded bytestream
   * @throws IOException
   *            if an IO related exception occurs
   * @throws MessagingException
   *            if an error occurs fetching the data to be written
   * @since TotalCross 1.13
   */
  public void writeTo(Stream stream) throws IOException, MessagingException {
    stream.writeBytes(Convert.CRLF_BYTES);
    if (subType.equals(FORM_DATA)) //flsobral@tc125_36: added support for the content type "form-data" with chunked transfer encoding when used by HttpStream.
    {
      stream = new ChunkedStream(stream);
      ((ChunkedStream) stream).start();
    }
    stream.writeBytes("MIME Multipart Media Encapsulation, Type: multipart/" + subType + ", Boundary: \""
        + new String(boundary) + "\"");

    int len = parts.size();
    for (int i = 0; i < len; i++) {
      stream.writeBytes(Convert.CRLF_BYTES);
      stream.writeBytes(slash2x);
      stream.writeBytes(boundary);
      stream.writeBytes(Convert.CRLF_BYTES);
      ((Part) parts.items[i]).writeTo(stream);
    }
    stream.writeBytes(Convert.CRLF_BYTES);
    stream.writeBytes(slash2x);
    stream.writeBytes(boundary);
    stream.writeBytes(slash2x);
    stream.writeBytes(Convert.CRLF_BYTES);

    if (stream instanceof ChunkedStream) {
      ((ChunkedStream) stream).flush();
      stream = ((ChunkedStream) stream).s;
    }
  }

  /**
   * Used by multipart with Content-Type "form-data" to support Transfer-Encoding "chunked"
   * 
   * @since TotalCross 1.25
   */
  private class ChunkedStream extends Socket {
    private final int SIZE = 1024;
    ByteArrayStream bas = new ByteArrayStream(SIZE);
    public Stream s;
    boolean start = false;

    public ChunkedStream(Stream s) {
      this.s = s;
    }

    @Override
    public int writeBytes(byte[] buf, int start, int count) throws IOException {
      bas.writeBytes(buf, start, count);
      return count;
    }

    public void start() throws IOException {
      flush();
      start = true;
    }

    public void flush() throws IOException {
      int chunkSize = bas.getPos();
      if (chunkSize > 0) {
        if (start) {
          s.writeBytes(Convert.toString(chunkSize, 16) + Convert.CRLF);
        }
        int written = 0;
        byte[] basbuf = bas.getBuffer();
        do {
          written += s.writeBytes(basbuf, written, chunkSize - written);
        } while (written < chunkSize);
        bas.reset();
      }
    }

    @Override
    public void close() throws IOException {
      if (s != null) {
        flush();
        s = null;
      }
    }

    @Override
    protected void finalize() {
    }
  }
}
