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

import java.io.IOException;
import java.io.Reader;

public class StringReader4D extends Reader {
  private final String baseString;
  private final int len;
  private int atual;

  public StringReader4D(String str) {
    this.baseString = str;
    atual = 0;
    len = str.length();
  }

  @Override
  public int read(char[] buf, int offset, int count) throws IOException {
    if (len == atual) {
      return -1;
    }
    int qntLidoDesejado = count;
    int qntFaltanteStr = len - atual;
    int qntLidoReal = Math.min(qntLidoDesejado, qntFaltanteStr);

    baseString.getChars(atual, atual + qntLidoReal, buf, offset);

    atual += qntLidoReal;

    return qntLidoReal;
  }

  @Override
  public int read() throws IOException {
    if (atual < len) {
      char c = baseString.charAt(atual);
      atual++;

      return c;
    } else {
      return -1;
    }
  }

  @Override
  public void close() {
  }

}
