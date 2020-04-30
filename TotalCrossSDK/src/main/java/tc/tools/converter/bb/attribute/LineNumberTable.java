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
package tc.tools.converter.bb.attribute;

import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.util.Vector;

public class LineNumberTable implements AttributeInfo {
  public Vector lineNumbers;

  public LineNumberTable() {
    lineNumbers = new Vector();
  }

  @Override
  public int length() {
    return 2 + (lineNumbers.size() * 4);
  }

  @Override
  public void load(DataStream ds) throws IOException {
    int count = ds.readUnsignedShort();
    lineNumbers.removeAllElements();
    for (int i = 0; i < count; i++) {
      LineNumber number = new LineNumber(this);
      number.load(ds);
      lineNumbers.addElement(number);
    }
  }

  @Override
  public void save(DataStream ds) throws IOException {
    int count = lineNumbers.size();
    ds.writeShort(count);
    for (int i = 0; i < count; i++) {
      ((LineNumber) lineNumbers.items[i]).save(ds);
    }
  }

  public static class LineNumber {
    public LineNumberTable table;
    public int startPC;
    public int lineNumber;

    public LineNumber(LineNumberTable table) {
      this.table = table;
    }

    public void load(DataStream ds) throws IOException {
      startPC = ds.readUnsignedShort();
      lineNumber = ds.readUnsignedShort();
    }

    public void save(DataStream ds) throws IOException {
      ds.writeShort(startPC);
      ds.writeShort(lineNumber);
    }
  }
}
