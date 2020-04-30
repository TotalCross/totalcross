// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
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

import totalcross.sys.Convert;

public class PDBFile4D extends Stream {
  Object dbRef;
  private Object openRef;
  private String name;

  private int hvRecordPos;
  long hvRecordHandle;
  private int hvRecordOffset;
  private int hvRecordLength;
  boolean hvRecordChanged;
  boolean dontFinalize;
  int mode;

  private static final int INVALID = -1;
  public static final int READ_WRITE = 3; // READ and WRITE
  public static final int CREATE = 4;
  public static final int CREATE_EMPTY = 5;

  public static final int DB_ATTR_READ_ONLY = 0x0002;
  public static final int DB_ATTR_APPINFODIRTY = 0x0004;
  public static final int DB_ATTR_BACKUP = 0x0008;
  public static final int DB_ATTR_OK_TO_INSTALL_NEWER = 0x0010;
  public static final int DB_ATTR_RESET_AFTER_INSTALL = 0x0020;
  public static final int DB_ATTR_COPY_PREVENTION = 0x0040;
  public static final int DB_ATTR_STREAM = 0x0080;

  public static final byte REC_RELEASE = (byte) -1;
  public static final byte REC_ATTR_DELETE = (byte) 0x80;
  public static final byte REC_ATTR_DIRTY = (byte) 0x40;
  public static final byte REC_ATTR_SECRET = (byte) 0x10;

  public PDBFile4D(String name, int mode)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.FileNotFoundException, totalcross.io.IOException {
    if (name == null) {
      throw new java.lang.NullPointerException("Argument 'name' cannot have a null value.");
    }
    if (mode < 3 || mode > 5) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'mode': " + mode);
    }

    String[] st = Convert.tokenizeString(name, '.');
    if (st == null || st.length != 3 || st[0].length() > 31 || st[1].length() != 4 || st[2].length() != 4) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'name' " + name);
    }

    this.name = name;
    this.hvRecordPos = -1;
    this.mode = mode;
    String s = st[0];
    create(s, st[1], st[2], mode);
  }

  public String getName() {
    return this.name;
  }

  public void setRecordOffset(int ofs) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (ofs < 0) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'ofs': " + ofs);
    }
    if (hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }

    if (ofs != hvRecordOffset) // if same, just exits
    {
      hvRecordOffset = 0; // skipBytes requires relative bytes
      skipBytes(ofs);
    }
  }

  public int getRecordOffset() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }
    return hvRecordOffset;
  }

  @Override
  public int skipBytes(int count) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }

    int off = hvRecordOffset + count;
    if (off < 0) {
      throw new totalcross.io.IllegalArgumentIOException("Offset cannot underflow the record size: " + off);
    }
    if (off > hvRecordLength) {
      throw new totalcross.io.IllegalArgumentIOException("Offset cannot overflow the record size: " + off);
    }
    hvRecordOffset += count;
    return count;
  }

  public int getRecordSize() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }

    return hvRecordLength;
  }

  final public int getRecordPos() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    return hvRecordPos;
  }

  @Override
  public void close() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    nativeClose();
  }

  @Override
  final public int readBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }
    if (buf == null) {
      throw new java.lang.NullPointerException("Argument 'buf' cannot have a null value.");
    }
    if (start < 0 || count < 0 || start + count > buf.length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    if (count == 0) {
      return 0; // flsobral@tc113_43: return 0 if asked to read 0.
    }
    int bytesLeft = hvRecordLength - hvRecordOffset;
    if (count > bytesLeft) {
      if (bytesLeft == 0) {
        return -1;
      }
      count = bytesLeft;
    }

    return readWriteBytes(buf, start, count, true);
  }

  @Override
  final public int writeBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }
    if (buf == null) {
      throw new java.lang.NullPointerException("Argument 'buf' cannot have a null value.");
    }
    if (start < 0 || count < 0 || start + count > buf.length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    int bytesLeft = hvRecordLength - hvRecordOffset;
    if (count > bytesLeft) {
      count = bytesLeft;
    }

    return readWriteBytes(buf, start, count, false);
  }

  public static String[] listPDBs() {
    return listPDBs(0, 0);
  }

  native private void create(String name, String creator, String type, int mode)
      throws totalcross.io.FileNotFoundException, totalcross.io.IOException;

  native public void rename(String newName) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;

  native public void addRecord(int size) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;

  native public void addRecord(int size, int pos)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;

  native public void resizeRecord(int size) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;

  native private void nativeClose() throws totalcross.io.IOException;

  native public void delete() throws totalcross.io.IOException;

  native public static String[] listPDBs(int creatorId, int type);

  native public void deleteRecord() throws totalcross.io.IOException;

  native public int getRecordCount() throws totalcross.io.IOException;

  native public void setRecordPos(int pos) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;

  native int readWriteBytes(byte buf[], int start, int count, boolean isRead) throws totalcross.io.IOException;

  native public int inspectRecord(byte buf[], int recordPos, int offsetInRec)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;

  native public byte getRecordAttributes(int recordPos) throws totalcross.io.IOException;

  native public void setRecordAttributes(int recordPos, byte attr) throws totalcross.io.IOException;

  native public int getAttributes() throws totalcross.io.IOException;

  native public void setAttributes(int i) throws totalcross.io.IOException;

  native public int searchBytes(byte[] toSearch, int length, int offsetInRec) throws totalcross.io.IOException;

  @Override
  protected void finalize() {
    try {
      nativeClose();
    } catch (totalcross.io.IOException e) {
    }
  }
}