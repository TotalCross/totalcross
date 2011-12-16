/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
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

import totalcross.sys.Convert;
import totalcross.sys.Vm;

/**
 * BufferedStream offers a faster way to read and write data from streams in a
 * buffered manner. This is especially useful when reading or writing large
 * amounts of data. It works like the CompressedByteArrayStream, however it does not
 * compresses the data like that one.
 *
 * Here's a sample code:
 * <pre>
 *   public void writeLargeFile(String path, byte[] largeData) throws IOException
 *   {
 *      File f = new File(path, File.CREATE_EMPTY);
 *      BufferedStream bs = new BufferedStream(f, BufferedStream.WRITE, 4096);
 *      bs.writeBytes(largeData, 0, largeData.length);
 *      bs.close(); // important!
 *      f.close();
 *   }
 * </pre>
 *
 * @since TotalCross 1.0
 */

public class BufferedStream extends Stream
{
   private Stream stream;
   private int mode;
   private byte[] buffer;
   private int size;
   private int pos;

   /** Used for opening this buffered stream for reading. */
   public static final int READ = 0;
   /** Used for opening this buffered stream for writing. */
   public static final int WRITE = 1;

   /**
    * Creates a new buffered stream given the underlying stream and the mode to use.
    * This constructor will use the default buffer size: 2048 bytes.
    * @param stream The underlying stream.
    * @param mode The mode to use - READ or WRITE.
    * @throws IllegalArgumentIOException if the mode is invalid.
    */
   public BufferedStream(Stream stream, int mode) throws IllegalArgumentIOException
   {
      this(stream, mode, 2048);
   }

   /**
    * Creates a new buffered stream given the underlying stream, the mode to use and
    * the buffer size.
    * @param stream The underlying stream.
    * @param mode The mode to use - READ or WRITE.
    * @param bufferSize The buffer size.
    * @throws IllegalArgumentIOException if the mode is invalid or the bufferSize is not
    * a positive number.
    */
   public BufferedStream(Stream stream, int mode, int bufferSize) throws IllegalArgumentIOException
   {
      if (mode != READ && mode != WRITE)
         throw new IllegalArgumentIOException("mode", Convert.toString(mode));
      if (bufferSize < 0)
         throw new IllegalArgumentIOException("bufferSize", Convert.toString(bufferSize));

      this.stream = stream;
      this.mode = mode;

      buffer = new byte[bufferSize];
      pos = 0;
      size = mode == READ ? 0 : bufferSize;
   }

   public final int readBytes(byte[] buf, int start, int count) throws IllegalArgumentIOException, IOException
   {
      if (mode != READ)
         throw new IOException("Operation can only be used in READ mode");
      if (start < 0)
         throw new IllegalArgumentIOException("start", Convert.toString(start));
      if (count <= 0)
         throw new IllegalArgumentIOException("count", Convert.toString(count));

      int r = 0, step, max;
      while (r != count)
      {
         if (pos == size) // read next block, if needed
         {
            pos = 0;
            size = stream.readBytes(buffer, 0, buffer.length);
            if (size < 0)
            {
               size = 0;
               if (r == 0)
                  r = -1;
               break;
            }
         }

         // Get the maximum to read on this iteration
         step = count - r;
         max = size - pos;
         if (step > max)
            step = max;

         // Read bytes
         Vm.arrayCopy(buffer, pos, buf, start, step);

         // Update positions
         r += step;
         start += step;
         pos += step;
      }

      return r;
   }

   public final int writeBytes(byte[] buf, int start, int count) throws IllegalArgumentIOException, IOException
   {
      if (mode != WRITE)
         throw new IOException("Operation can only be used in WRITE mode");
      if (start < 0)
         throw new IllegalArgumentIOException("start", Convert.toString(start));
      if (count <= 0)
         throw new IllegalArgumentIOException("count", Convert.toString(count));

      int w = 0, step, max;
      while (w != count)
      {
         if (pos == size) // write block, if needed
         {
            stream.writeBytes(buffer, 0, size);
            pos = 0;
         }

         // Get the maximum to read on this iteration
         step = count - w;
         max = size - pos;
         if (step > max)
            step = max;

         // Read bytes
         Vm.arrayCopy(buf, start, buffer, pos, step);

         // Update positions
         w += step;
         start += step;
         pos += step;
      }

      return w;
   }

   /** This method closes this stream, flushing any pending WRITE data.
    * It does NOT close the underlying stream.
    */
   public final void close() throws IOException
   {
      if (mode == WRITE && pos > 0)
         stream.writeBytes(buffer, 0, pos);
   }

   /** Change the initial Stream to the attached one.
    * Reusing a BufferedStream throught this method can preserve memory.
    * @since TotalCross 1.23
    */
   public void setStream(Stream f) throws IOException // guich@tc123_34
   {
      this.stream = f;
      pos = 0;
      size = mode == READ ? 0 : buffer.length;
   }
   
   /** Returns the Stream attached to this LineReader.
    * @since TotalCross 1.23
    */
   public Stream getStream()
   {
      return stream;
   }
   
}
