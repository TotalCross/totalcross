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

import totalcross.util.Hashtable;

/**
 * Maps a MIME type into an instance of a DataContentHandler.
 * 
 * @since TotalCross 1.13
 */
public class DataHandler {
  private static Hashtable handlers = new Hashtable(5);

  static {
    TextContentHandler textHandler = new TextContentHandler();
    handlers.put(Part.PLAIN, textHandler);
    handlers.put(Part.HTML, textHandler);
    handlers.put(Part.BINARY, new BinaryContentHandler());
  }

  /**
   * May be used to add an user-defined DataContentHandler, associated to a specific MIME type.
   * 
   * @param dataContentHandler
   *           the user-defined DataContentHandler to be added
   * @param mimeType
   *           the MIME type this DataContentHandler should be associated to
   * @since TotalCross 1.13
   */
  public static void addDataContentHandler(DataContentHandler dataContentHandler, String mimeType) {
    handlers.put(mimeType, dataContentHandler);
  }

  /**
   * Retrieves the DataContentHandler associated with the given MIME type, returning a BinaryContentHandler if the
   * given MIME type is not mapped to any handler.
   * 
   * @param mimeType
   *           MIME type of the content that we need to be handled
   * @return the requested DataContentHandler, or a BinaryContentHandler if the given MIME type is not mapped to any
   *         handler.
   * @since TotalCross 1.13
   */
  public static DataContentHandler getDataContentHandler(String mimeType) {
    DataContentHandler handler = (DataContentHandler) handlers.get(mimeType);
    if (handler == null) {
      handler = (DataContentHandler) handlers.get(Part.BINARY);
    }
    return handler;
  }
}
