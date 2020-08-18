// Copyright (C) 2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.nio.channels.spi;

import java.io.Closeable;
import java.io.IOException;
import totalcross.util.concurrent.Lock;

public abstract class AbstractInterruptibleChannel4D implements Closeable
{
  private boolean isOpen = true;
  private Lock mutex = new Lock();

  protected AbstractInterruptibleChannel4D() {}

  public final boolean isOpen() { return isOpen; }

  public final void close() throws IOException
  {
    synchronized (mutex)
    {
      if (isOpen) {
        implCloseChannel();
      }
      isOpen = false;
    }
  }

  protected abstract void implCloseChannel() throws IOException;

  @Override protected void finalize() throws Throwable { this.close(); }
}