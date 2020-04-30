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

/**
 * Represents a stream that behaves like a large array of bytes and may be randomly accessed.
 * 
 * @since TotalCross 1.01
 */
public abstract class RandomAccessStream extends Stream {
  /** The index of the next position to read or write in the stream */
  protected int pos;

  /** Beginning of file */
  public static final int SEEK_SET = 0;
  /** Current position of the file pointer */
  public static final int SEEK_CUR = 1;
  /** End of file */
  public static final int SEEK_END = 2;

  /**
   * Returns the current offset in this stream.
   * 
   * @return the offset from the beginning of the stream, in bytes, at which the next read or write occurs.
   * @throws totalcross.io.IOException
   *            if an I/O error has occurred.
   * @since TotalCross 1.2
   */
  public int getPos() throws totalcross.io.IOException {
    return pos;
  }

  /**
   * Sets the file pointer for read and write operations to a new position defined by adding offset to a reference
   * position specified by origin.
   * 
   * @param offset
   *           number of bytes to offset from origin.
   * @param origin
   *           position from where offset is added. It is specified by one of the SEEK_* constants.
   * @throws IllegalArgumentException
   *            if origin is not one of the values specified by the SEEK_* constants.
   * @throws totalcross.io.IOException
   *            if the new position is negative or if an I/O error occurs.
   * @since TotalCross 1.2
   */
  public abstract void setPos(int offset, int origin) throws totalcross.io.IOException;

  /**
   * Sets the file pointer for read and write operations to the given position. The position passed is an absolute
   * position, in bytes, from the beginning of the stream.
   * 
   * @param pos
   *           the offset position, measured in bytes from the beginning of the file, at which to set the file pointer.
   * @throws totalcross.io.IOException
   *            if pos is negative or if an I/O error occurs.
   */
  public abstract void setPos(int pos) throws totalcross.io.IOException;
}
