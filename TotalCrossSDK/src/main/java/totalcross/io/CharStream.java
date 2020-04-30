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

public class CharStream {
  DataStream ds;

  public CharStream(String s) {
    this(new DataStream(new ByteArrayStream(s.getBytes())));
  }

  public CharStream(Stream ds) {
    this.ds = ds instanceof DataStream ? (DataStream) ds : new DataStream(ds);
  }

  public int read(char[] cbuf) throws IOException {
    return read(cbuf, 0, cbuf.length);
  }

  public int read(char[] cbuf, int ofs, int len) throws IOException {
    int l = len;
    while (--len >= 0) {
      cbuf[ofs++] = ds.readChar();
    }
    return l;
  }

  public int write(char c) throws IOException {
    return ds.writeChar(c);
  }

  public int write(char[] cbuf) throws IOException {
    return write(cbuf, 0, cbuf.length);
  }

  public int write(char[] cbuf, int ofs, int len) throws IOException {
    int l = len;
    while (--len >= 0) {
      ds.writeChar(cbuf[ofs++]);
    }
    return l;
  }
}
