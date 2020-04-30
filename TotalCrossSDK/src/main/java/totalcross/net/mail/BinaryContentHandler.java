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
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.net.Base64;
import totalcross.sys.Convert;

/**
 * Implementation of DataContentHandler that handles MIME types handled as base64 encoded byte arrays.<br>
 * It also handle Streams, reading from the input stream on demand to avoid excessive memory load. This will usually be
 * slower than reading the whole content of the stream to a byte array and using it as the Part content.
 * 
 * @since TotalCross 1.13
 */
public class BinaryContentHandler extends DataContentHandler {
  static byte[] ContentTransferEncoding = "Content-Transfer-Encoding: base64\r\n".getBytes();
  private static final int bytesPerLine = 57;

  @Override
  public void writeTo(Object obj, String mimeType, Stream stream) throws IOException {
    Part part = (Part) obj;
    String fileName = part.fileName;
    String disposition = (fileName != null ? Part.ATTACHMENT : part.disposition);

    stream.writeBytes(ContentTransferEncoding);
    stream.writeBytes("Content-Disposition: " + disposition
        + (disposition == Part.ATTACHMENT ? "; filename=\"" + fileName + "\"" : ""));
    stream.writeBytes(Convert.CRLF_BYTES);
    writeEncoded(stream, part.content);
  }

  private void writeEncoded(Stream outputStream, Object input) throws IOException {
    byte[] inputBytes;
    if (input instanceof byte[]) {
      inputBytes = (byte[]) input;
      outputStream.writeBytes(Convert.CRLF_BYTES);
      outputStream.writeBytes(Base64.encode(inputBytes, inputBytes.length));
      outputStream.writeBytes(Convert.CRLF_BYTES);
    } else if (input instanceof Stream) //flsobral@tc123_45: now we use ByteArrayStream with fixed length, which should GREATLY reduce the memory usage when processing large files.
    {
      DataStream inputStream = (input instanceof DataStream) ? ((DataStream) input)
          : new DataStream((Stream) input, true);
      ByteArrayStream inputBAS = new ByteArrayStream(bytesPerLine + 1);
      ByteArrayStream outputBAS = new ByteArrayStream(bytesPerLine * 2);
      inputBytes = inputBAS.getBuffer();
      byte[] encodedLineBytes = outputBAS.getBuffer();
      int bytesRead;

      outputStream.writeBytes(Convert.CRLF_BYTES);
      do {
        bytesRead = inputStream.readBytes(inputBytes, 0, bytesPerLine);

        inputBAS.setPos(bytesRead);
        Base64.encode(inputBAS, outputBAS);
        outputStream.writeBytes(encodedLineBytes, 0, outputBAS.getPos());
        outputStream.writeBytes(Convert.CRLF_BYTES);

        inputBAS.reset();
        outputBAS.reset();
      } while (bytesRead == bytesPerLine);
      outputStream.writeBytes(Convert.CRLF_BYTES);
    } else if (input != null) //flsobral@tc122_40: handle unknown types using toString().getBytes()
    {
      inputBytes = input.toString().getBytes();
      outputStream.writeBytes(Convert.CRLF_BYTES);
      outputStream.writeBytes(Base64.encode(inputBytes, inputBytes.length));
      outputStream.writeBytes(Convert.CRLF_BYTES);
    }
  }
}
