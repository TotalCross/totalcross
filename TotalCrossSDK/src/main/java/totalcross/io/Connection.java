// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io;

import java.io.Closeable;

/**
 * Base class for I/O connections.
 */
abstract class Connection implements Closeable {
  /**
   * Closes this I/O connection, releasing any associated resources. Once closed a connection is no longer valid.
   * 
   * @throws IOException
   *            If an I/O error occurs.
   */
  @Override
  abstract public void close() throws IOException;
}
