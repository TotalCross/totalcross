// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.io;

public class IOException4D extends Exception {
  private static final long serialVersionUID = -2139950177232639948L;

  public IOException4D() {
  }

  public IOException4D(final String message) {
    super(message);
  }

  public IOException4D(final Throwable cause) {
    super(cause);
  }

  public IOException4D(final String message, final Throwable cause) {
    super(message, cause);
  }

}
