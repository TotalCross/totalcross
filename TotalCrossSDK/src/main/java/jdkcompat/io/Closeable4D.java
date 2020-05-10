// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.io;

import java.io.IOException;

public interface Closeable4D extends AutoCloseable {
  @Override
  public void close() throws IOException;
}
