// Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.io;

import java.io.IOException;

public class UncheckedIOException4D extends RuntimeException {

  public UncheckedIOException4D(IOException cause) {
    super(cause);
  }
  
  public UncheckedIOException4D(String message, IOException cause) {
    super(message, cause);
  }
}
