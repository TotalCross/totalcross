/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/


package totalcross.io;

import java.io.IOException;
import java.io.InputStream;

import totalcross.sys.*;

/**
 * Stream is the base class for all stream-based I/O classes.
 */
public abstract class Stream extends Connection
{
   protected static byte skipBuffer[] = new byte[128];

   /**
    * Reads bytes from the stream. Returns the number of bytes actually read or -1 if the end of the stream was reached.
    * (if applicable to the stream)
    * 
    * @param buf
    *           the byte array to read data into
    * @param start
    *           the start position in the array
    * @param count
    *           the number of bytes to read
    * @throws totalcross.io.IOException
    */
   public abstract int readBytes(byte buf[], int start, int count) throws totalcross.io.IOException;

   /**
    * Writes bytes to the stream. Returns the number of bytes actually written or throws an <code>IOException</code> if an error prevented the write
    * operation from occurring.
    * 
    * @param buf
    *           the byte array to write data from
    * @param start
    *           the start position in the byte array
    * @param count
    *           the number of bytes to write
    * @return the number of bytes actually written
    * @throws totalcross.io.IOException
    */
   public abstract int writeBytes(byte buf[], int start, int count) throws totalcross.io.IOException;

   /**
    * Writes bytes to the stream. Returns the number of bytes actually written or throws an <code>IOException</code> if an error prevented the write
    * operation from occurring.
    * 
    * @param buf
    *           the byte array to write data from
    * @return the number of bytes actually written
    * @throws totalcross.io.IOException
    */
   public int writeBytes(byte buf[]) throws totalcross.io.IOException
   {
      if (buf.length == 0) // throws NPE if null.
         return 0; // nothing to write, just return 0.
      return writeBytes(buf, 0, buf.length);
   }

   /**
    * Writes the string to the stream as a byte array. Returns the number of bytes actually written or throws an <code>IOException</code> if an error prevented the write
    * operation from occurring.<br>
    * The String is written as-is, nothing is placed before or after it.
    * 
    * @param string
    *           the string whose bytes will be written
    * @return the number of bytes actually written
    * @throws totalcross.io.IOException
    */
   public int writeBytes(String string) throws totalcross.io.IOException
   {
      byte buf[] = string.getBytes(); // throws NPE if null.
      if (buf.length == 0)
         return 0; // nothing to write, just return 0.
      return writeBytes(buf, 0, buf.length);
   }

   /**
    * Writes the given StringBuffer as a byte array, retrieved using Convert.getBytes.<br>
    * The String is written as-is, nothing is placed before or after it.
    * 
    * @param sb
    *           the StringBuffer to get the bytes from
    * @return the number of bytes actually written
    * @throws totalcross.io.IOException
    * @since TotalCross 1.23
    */
   final public int writeBytes(StringBuffer sb) throws totalcross.io.IOException // guich@tc123_44
   {
      byte[] b = Convert.getBytes(sb); // throws NPE if null.
      if (b.length == 0)
         return 0; // nothing to write, just return 0.
      return writeBytes(b, 0, b.length);
   }

   /**
    * Skips over and discards n bytes of data from this stream. The skip method may, for a variety of reasons, end up
    * skipping over some smaller number of bytes, possibly 0. This may result from any of a number of conditions;
    * reaching end of file before n bytes have been skipped is only one possibility. The actual number of bytes skipped
    * is returned. If n is negative, no bytes are skipped, but this may vary among some implementations of skipBytes in
    * classes that inherit them (like File and PDBFile). <br>
    * <br>
    * The skip method of this class uses a static byte array, repeatedly reading into it until n bytes have been read or
    * the end of the stream has been reached. Subclasses are encouraged to provide a more efficient implementation of
    * this method. For instance, the implementation may depend on the ability to seek.
    * 
    * @param n
    *           the number of bytes to be skipped.
    * @return the actual number of bytes skipped.
    * @throws totalcross.io.IOException
    *            if the stream does not support skip, or if some other I/O error occurs.
    */
   public int skipBytes(int n) throws totalcross.io.IOException
   {
      int readBytesRet;
      int bytesSkipped = 0;

      while (n > 0)
      {
         int c = n > skipBuffer.length ? skipBuffer.length : n;
         readBytesRet = readBytes(skipBuffer, 0, c);
         if (readBytesRet <= 0)
            break;
         bytesSkipped += readBytesRet;
         n -= c;
      }

      return bytesSkipped;
   }
   
   public InputStream wrapInputStream() {
	   return new WrapInputStream(this);
   }

   public static Stream wrapInputStreamToStream(InputStream inputStream) {
      if (inputStream instanceof WrapInputStream) {
         return ((WrapInputStream) inputStream).stream;
      }
      return new WrapFromInputStream(inputStream);
   }
}

class WrapInputStream extends InputStream {
	Stream stream;

	WrapInputStream(Stream stream) {
		this.stream = stream;
	}

	@Override
	public int read() throws IOException {
		byte[] b = new byte[1];
		if (read(b) == -1) {
			return -1;
		}
		
		return b[0];
	}
	
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		try {
			return stream.readBytes(b, off, len);
		} catch (totalcross.io.IOException e) {
			throw new java.io.IOException(e);
		}
	}
	
}

class WrapFromInputStream extends Stream {
	InputStream inputStream;
	
	WrapFromInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	@Override
	public int readBytes(byte[] buf, int start, int count) throws totalcross.io.IOException {
		try {
			return inputStream.read(buf, start, count);
		} catch (IOException e) {
			throw new totalcross.io.IOException(e);
		}
	}
	
	@Override
	public int writeBytes(byte[] buf, int start, int count) throws totalcross.io.IOException {
		throw new totalcross.io.IOException("Can't write to a wrapped input stream");
	}
	
	@Override
	public int writeBytes(byte[] buf) throws totalcross.io.IOException {
		throw new totalcross.io.IOException("Can't write to a wrapped input stream");
	}
	
	@Override
	public int writeBytes(String string) throws totalcross.io.IOException {
		throw new totalcross.io.IOException("Can't write to a wrapped input stream");
	}
	
	@Override
	public int skipBytes(int n) throws totalcross.io.IOException {
		try {
			return (int) inputStream.skip(n);
		} catch (IOException e) {
			throw new totalcross.io.IOException(e);
		}
	}
	
	@Override
	public InputStream wrapInputStream() {
		return inputStream;
	}
	
	@Override
	public void close() throws totalcross.io.IOException {
		try {
			inputStream.close();
		} catch (IOException e) {
			throw new totalcross.io.IOException(e);
		}
	}
	
	
}
