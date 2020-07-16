// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.net.mail;

import totalcross.io.IOException;
import totalcross.io.Stream;

/**
 * Defines the basic interface for implementations of DataContentHandler.
 * 
 * @since TotalCross 1.13
 */
public abstract class DataContentHandler {
  public abstract void writeTo(Object obj, String mimeType, Stream stream) throws IOException; //flsobral@124_27: must be public.
}
