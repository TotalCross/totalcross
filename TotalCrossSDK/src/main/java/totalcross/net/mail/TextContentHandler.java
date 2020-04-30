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

/**
 * Implementation of DataContentHandler that handles textual (not encoded) MIME types.
 * 
 * @since TotalCross 1.13
 */
public class TextContentHandler extends DataContentHandler {
  @Override
  public void writeTo(Object obj, String mimeType, Stream stream) throws IOException {
    String text;
    if (obj instanceof Part) {
      Part part = (Part) obj;
      text = (String) part.content;
    } else {
      text = obj.toString();
    }
    stream.writeBytes(Convert.CRLF_BYTES);
    stream.writeBytes(text);
    stream.writeBytes(Convert.CRLF_BYTES);
  }
}
