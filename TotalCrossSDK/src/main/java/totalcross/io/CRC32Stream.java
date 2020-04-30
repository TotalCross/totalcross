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

/*
 * CRC32.java - Computes CRC32 data checksum of a data stream Copyright (C) 1999. 2000, 2001 Free Software Foundation,
 * Inc. This file is part of GNU Classpath. GNU Classpath is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2, or
 * (at your option) any later version. GNU Classpath is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of the GNU General Public License along with
 * GNU Classpath; see the file COPYING. If not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA. Linking this library statically or dynamically with other modules is making a combined
 * work based on this library. Thus, the terms and conditions of the GNU General Public License cover the whole
 * combination. As a special exception, the copyright holders of this library give you permission to link this library
 * with independent modules to produce an executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice, provided that you also meet, for each
 * linked independent module, the terms and conditions of the license of that module. An independent module is a module
 * which is not derived from or based on this library. If you modify this library, you may extend this exception to your
 * version of the library, but you are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 */

/*
 * Written using on-line Java Platform 1.2 API Specification, as well as "The Java class libraries", 2nd edition
 * (Addison-Wesley, 1998). The actual CRC32 algorithm is taken from RFC 1952. Status: Believed complete and correct.
 */

/**
 * Computes CRC32 data checksum of a stream. The actual CRC32 algorithm is described in RFC 1952 (GZIP file format
 * specification version 4.3). Can be used to get the CRC32 over a stream if used with input/output streams.
 *
 * @author Per Bothner
 */
public class CRC32Stream extends Stream {
  private Stream stream;
  private int crc;

  /** The fast CRC table. Computed once when the CRC32 class is loaded. */
  public static int[] crcTable = make_crc_table();

  /** Make the table for a fast CRC. */
  private static int[] make_crc_table() {
    int[] crc_table = new int[256];
    for (int n = 0; n < 256; n++) {
      int c = n;
      for (int k = 8; --k >= 0;) {
        if ((c & 1) != 0) {
          c = 0xedb88320 ^ (c >>> 1);
        } else {
          c >>>= 1;
        }
      }
      crc_table[n] = c;
    }
    return crc_table;
  }

  public CRC32Stream(Stream s) {
    this.stream = s;
  }

  /**
   * Returns the CRC32 data checksum computed so far.
   * You can safely cast it to <code>int</code>, but then the value may be negative.
   * @return an unsigned crc value in the int range.
   */
  public long getValue() {
    return crc & 0xFFFFFFFFL;
  }

  /**
   * Resets the CRC32 data checksum so a new CRC32 can be computed.
   */
  public void reset() {
    crc = 0;
  }

  /** This method does nothing. */
  @Override
  public void close() {
  }

  /** Updates the CRC32 with the values of the given buffer. */
  public void update(byte[] buf, int off, int len) {
    int c = ~crc;
    while (--len >= 0) {
      c = crcTable[(c ^ buf[off++]) & 0xff] ^ (c >>> 8);
    }
    crc = ~c;
  }

  /**
   * Computes the checksum for the bytes read from the attached stream.
   * @param buf    the byte array to read data into
   * @param start  the start position in the array
   * @param count  the number of bytes to read
   * @return The number of bytes read from the underlying stream.
   *
   * @throws totalcross.io.IOException
   */
  @Override
  public int readBytes(byte[] buf, int start, int count) throws totalcross.io.IOException {
    int n = stream.readBytes(buf, start, count);
    int[] crcs = crcTable;
    int c = ~crc;
    count = n;
    while (--count >= 0) {
      c = crcs[(c ^ buf[start++]) & 0xff] ^ (c >>> 8);
    }
    crc = ~c;
    return n; // guich@tc100b5_23
  }

  /** Computes the checksum for the given bytes, then write them to the attached stream.
   * @param buf    the byte array to write data from
   * @param start  the start position in the byte array
   * @param count  the number of bytes to write
   * @return The number of bytes written to the underlying stream.
   * @throws totalcross.io.IOException 
   */
  @Override
  public int writeBytes(byte[] buf, int start, int count) throws totalcross.io.IOException {
    int s = start;
    int n = count;
    int[] crcs = crcTable;
    int c = ~crc;
    while (--count >= 0) {
      c = crcs[(c ^ buf[start++]) & 0xff] ^ (c >>> 8);
    }
    crc = ~c;
    return stream.writeBytes(buf, s, n);
  }
}
