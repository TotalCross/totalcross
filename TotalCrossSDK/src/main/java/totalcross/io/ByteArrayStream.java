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

import totalcross.sys.Vm;

/** Creates a byte array stream, which is a growable array of bytes.
 * This class cannot be used for output AND input, but only for output OR input.
 * <p>
 * If you plan to read or write huge amount of data, consider using the
 * CompressedByteArrayStream class instead.
 * @see CompressedByteArrayStream
 */

public class ByteArrayStream extends RandomAccessStream {
  private byte[] buffer;
  private int len; // guich@563_5
  private byte[] writeBuf; // flsobral@tc110_71: used by readFully.

  /** Creates a ByteArrayStream.
   * @param buffer The initial buffer from where data will be read or written into.
   */
  public ByteArrayStream(byte[] buffer) {
    if (buffer == null) {
      throw new IllegalArgumentException("Argument 'buffer' cannot be null");
    }
    this.len = buffer.length;
    this.buffer = buffer;
    pos = 0;
  }

  /** Creates a ByteArrayStream.
   * @param buffer The initial buffer from where data will be read or written into.
   * @param len The length to read from the buffer.
   */
  public ByteArrayStream(byte[] buffer, int len) {
    if (buffer == null) {
      throw new IllegalArgumentException("Argument 'buffer' cannot be null");
    }
    if (len < 0) {
      throw new IllegalArgumentException("Argument 'len' must be greater or equal than 0");
    }
    if (len > buffer.length) {
      throw new IllegalArgumentException("Argument 'len' must not be greater than 'buffer.length'");
    }
    this.len = len;
    this.buffer = buffer;
    pos = 0;
  }

  /** Creates a ByteArrayStream.
   * @param size The initial size that the byte array will have.
   */
  public ByteArrayStream(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Argument 'size' must be greater or equal than 0");
    }
    buffer = new byte[size];
    len = size;
    pos = 0;
  }

  /** Sets the current position as the maximum size of the buffer so that no
   * more than the current written data will be read. This already resets the
   * read position to zero.
   * @see #reset()
   */
  public void mark() // guich@563_5
  {
    len = pos;
    pos = 0;
  }

  /** Returns the number of bytes available in the buffer from the actual read position.
   * @since SuperWaba 4.02
   */
  public int available() {
    return len - pos;
  }

  /** does nothing.  */
  @Override
  public void close() {
    // dont set buffer to null here or the PDBFile class will stop working!
  }

  /** Gets the internal buffer used.
   * The actual read or written data may differ from the buffer's length; use the count
   * method to get the correct value.
   * @see #count()
   */
  public byte[] getBuffer() {
    return buffer;
  }

  /** Sets the buffer to be used, resetting the current position.
   * @param buffer the new internal buffer.
   * @since TotalCross 1.0.
   */
  public void setBuffer(byte[] buffer) {
    if (buffer == null) {
      throw new IllegalArgumentException("Argument 'buffer' cannot be null");
    }
    len = buffer.length;
    this.buffer = buffer;
    pos = 0;
  }

  /**
   * Returns the current position in the buffer.
   * @deprecated use {@link #getPos()} instead.
   */
  @Deprecated
  public int count() {
    return pos;
  }

  @Override
  public int getPos() {
    return pos;
  }

  @Override
  public int readBytes(byte buf[], int start, int count) {
    int remains = len - pos;
    if (count < 0) {
      throw new IllegalArgumentException();
    }
    if (count > remains) {
      if (remains <= 0) {
        return -1; // flsobral@tc111_11: return -1 on EOF.
      } else {
        count = remains;
      }
    }
    Vm.arrayCopy(buffer, pos, buf, start, count);
    pos += count;
    return count;
  }

  /** Resets the position to 0 so the buffer can be reused, and sets the mark to the buffer real limits.
   * @see #mark()
   */
  public void reset() {
    pos = 0;
    len = buffer.length;
  }

  /**
   * Moves the cursor n bytes from the current position, moving backwards if n is negative, or forward if n is
   * positive.<br>
   * The cursor cannot be placed outside the stream limits, stopping at position 0 when moving backwards, or at the
   * last position of the stream, when moving forward.
   *
   * @param n
   *           the number of bytes to move.
   * @return the number of bytes actually moved.
   */
  @Override
  public int skipBytes(int n) {
    int off = pos + n; // This here is for performance reason

    if (off < 0) {
      off = -pos;
    } else if (off > len) {
      off = len - pos; // jeffque@tc200: skip to the end of the buffer, not the last readable byte
    } else {
      off = n;
    }
    pos += off;
    return off;
  }

  /** Reuses the already read part of the buffer. This method shifts the buffer
   * from the current position to 0, so you can append more data to the buffer.
   * @return The number of bytes shifted
   * @since SuperWaba 4.01
   */
  public int reuse() // guich@401_34
  {
    int shifted = pos;
    if (pos > 0) {
      Vm.arrayCopy(buffer, pos, buffer, 0, len - pos);
      pos = 0;
    }
    return shifted;
  }

  @Override
  public int writeBytes(byte buf[], int start, int count) {
    if (len < (count + pos)) // grow the buffer
    {
      int size = (count + pos) * 12 / 10; // grows 20% above the new needed capacity
      byte[] newBuffer = new byte[size];
      Vm.arrayCopy(buffer, 0, newBuffer, 0, pos);
      buffer = newBuffer;
      len = buffer.length;
    }
    if (buf != buffer || start != pos) {
      Vm.arrayCopy(buf, start, buffer, pos, count);
    }
    pos += count;
    return count;
  }

  /** Sets the size of the current byte array. If the current size is smaller then the given one,
   * a new byte array is created with the given size. If there's already enough room for the given
   * size, nothing is made. Note that this method does not reset the current position.
   * @param newSize the new array size
   * @param copyOldData If true, the old data up to <code>pos</code> is copied into the new buffer.
   * @since SuperWaba 5.1
   */
  public void setSize(int newSize, boolean copyOldData) // guich@510_15: added method - guich@512_9: added copyOldData
  {
    if (len < newSize) {
      byte[] buf = buffer;
      buffer = new byte[newSize];
      len = buffer.length;
      if (copyOldData) {
        Vm.arrayCopy(buf, 0, buffer, 0, pos);
      }
    }
  }

  /** Returns a copy of the data inside this buffer. The returned buffer will have the exact size
   * of the stored data
   * @since SuperWaba 5.1
   */
  public byte[] toByteArray() // guich@510_15
  {
    byte[] b = new byte[pos];
    Vm.arrayCopy(buffer, 0, b, 0, pos);
    return b;
  }

  @Override
  public void setPos(int offset, int origin) throws IOException {
    int newPos;

    switch (origin) {
    case SEEK_SET:
      newPos = offset;
      break;
    case SEEK_CUR:
      newPos = this.pos + offset;
      break;
    case SEEK_END:
      newPos = this.len + offset - 1;
      break;
    default:
      throw new IllegalArgumentException();
    }

    if (newPos < 0) {
      throw new IOException();
    }
    if (newPos >= this.len) {
      this.pos = this.len; //flsobral@tc120: ensure the current data is copy by setSize.
      setSize(newPos + 1, true);
    }
    this.pos = newPos;
  }

  @Override
  public void setPos(int newPos) throws IOException // flsobral@tc120: now may throw an IOException
  {
    if (newPos < 0) {
      throw new IOException();
    }

    if (newPos >= this.len) {
      this.pos = this.len; //flsobral@tc120: ensure the current data is copy by setSize.
      setSize(newPos + 1, true);
    }
    this.pos = newPos;
  }

  /**
   * Reads all data from the input stream into our buffer.
   * When returned, data is marked as ready to be read.
   *
   * @param inputStream
   *           The input stream from where data will be read
   * @param retryCount
   *           The number of times to retry if no data is read. In remote
   *           connections, use at least 5; for files, it can be 0.
   * @param bufSize
   *           The size of buffer used to read data.
   * @throws totalcross.io.IOException
   * @since TotalCross 1.0
   */
  public void readFully(Stream inputStream, int retryCount, int bufSize) throws totalcross.io.IOException // guich@570_31
  {
    byte[] buf = (writeBuf != null && writeBuf.length >= bufSize) ? writeBuf : (writeBuf = new byte[bufSize]); // flsobral@tc110_71: readFully now uses an internal buffer to read data before writing.
    reset();

    while (true) {
      int n = inputStream.readBytes(buf, 0, buf.length);
      if (n <= 0 && --retryCount <= 0) {
        break;
      }
      if (n > 0) {
        writeBytes(buf, 0, n);
      }
    }
    mark(); // flsobral@tc100b4: replaced reset() by mark().
  }
}