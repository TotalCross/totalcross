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

/**
 * Base class for connection notifiers.
 */
public abstract class StreamConnectionNotifier extends Connection {
  /**
   * Returns a Stream that represents a server side connection.
   * 
   * @return A stream to communicate with a client.
   * @throws IOException
   *            If an I/O error occurs.
   */
  abstract public Stream accept() throws IOException;
}
