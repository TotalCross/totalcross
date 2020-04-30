// Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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
