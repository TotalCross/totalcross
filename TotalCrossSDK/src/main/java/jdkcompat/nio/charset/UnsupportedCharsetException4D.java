// Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.nio.charset;

public class UnsupportedCharsetException4D extends IllegalArgumentException {
  private String charsetName;

  public UnsupportedCharsetException4D(String charsetName) {
    super(String.valueOf(charsetName));
    this.charsetName = charsetName;
  }

  public String getCharsetName() {
    return charsetName;
  }
}
