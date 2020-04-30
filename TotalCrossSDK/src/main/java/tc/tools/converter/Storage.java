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
package tc.tools.converter;

import totalcross.io.ByteArrayStream;
import totalcross.io.DataStreamLE;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.util.zip.ZLib;

public final class Storage {
  /** ******************************************** */
  /** FILE COMPRESSION * */
  /** ******************************************** 
   * @throws IOException */

  public static void readAndDecompress(Stream dsIn, ByteArrayStream bufOut, int size) throws IOException {
    ZLib.inflate(dsIn, bufOut, size);
    bufOut.mark();
  }

  public static void compressAndWrite(ByteArrayStream bufIn, Stream dsOut) throws IOException {
    if (bufIn.getPos() > 0) {
      bufIn.mark(); // mark the end of the stream - don't mark if already marked
    }
    ZLib.deflate(bufIn, dsOut, 9); // the smaller the compression level, the slower is the vm's startup
  }

  /** ******************************************** */
  /** UTILITY * */
  /**
   * @throws totalcross.io.IOException  ******************************************** */

  public static int writeUnsignedShortArray(DataStreamLE ds, int[] out) throws totalcross.io.IOException {
    for (int i = 0; i < out.length; i++) {
      ds.writeShort(out[i]);
    }
    return 2 * out.length;
  }

  public static void writeIntArray(DataStreamLE ds, int[] opcode) throws totalcross.io.IOException {
    int ofs = 0, len = opcode.length;
    while (len-- > 0) {
      ds.writeInt(opcode[ofs++]);
    }
  }

  public static int writeChars(DataStreamLE ds, String s) throws totalcross.io.IOException {
    int len = s.length(), i = 0;
    ds.writeShort(len);
    while (--len >= 0) {
      ds.writeShort(s.charAt(i++));
    }
    return s.length() * 2 + 2;
  }
}
