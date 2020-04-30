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

package totalcross.io;

/** Thrown when a file was not found. */

public class FileNotFoundException extends IOException {
  /** Constructs an empty Exception. */
  public FileNotFoundException() {
    super();
  }

  /** Constructs an exception with the given message. */
  public FileNotFoundException(String path) {
    super("File not found: " + path);
  }

  /** Returns the file name from the exception's message.
   * If there's no file name, returns an empty string.
   * @since TotalCross 1.15
   */
  public String getFileName() // guich@tc115_44
  {
    String msg = getMessage();
    int idx;
    if (msg != null && (idx = msg.indexOf(':')) >= 0) {
      msg = msg.substring(idx).trim();
    } else {
      msg = "";
    }
    return msg;
  }
}
