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

package litebase;

import totalcross.sys.Vm;

/**
 * This is a memory file, ie, a file that is allocated in memory and never dumped to disk. Used for result sets.
 */
class MemoryFile extends XFile
{
   /**
    * The memory file, a buffer array.
    */
   private byte[] fbuf;

   /**
    * Creates a memory file
    */
   MemoryFile()
   {
      fbuf = new byte[0];
   }

   /**
    * Sets the position in the buffer.
    *
    * @param newPos The new position in the buffer.
    */
   void setPos(int newPos)
   {
      pos = newPos;
   }

   /**
    * Increases the buffer for the memory file.
    *
    * @param newSize the new size of the buffer.
    */
   void growTo(int newSize)
   {
      byte[] temp = new byte[newSize];
      Vm.arrayCopy(fbuf, 0, temp, 0, size); // Copies the old buffer.
      fbuf = temp;
      size = newSize;
   }
   
   // guich@201_9: always shrink the .db and .dbo memory files.
   /**
    * Shrinks the buffer for the memory file if there is unused space.
    *
    * @param newSize the new size of the buffer.
    */
   void shrinkTo(int newSize)
   {
      byte[] temp = new byte[newSize];
      Vm.arrayCopy(fbuf, 0, temp, 0, size = newSize); // Copies the the old buffer.
      fbuf = temp;
   }

   /**
    * Reads bytes from the buffers.
    *
    * @param buf The byte array to read data into.
    * @param start The offset position in the array.
    * @param count The number of bytes to read.
    * @return The number of bytes read.
    */
   public int readBytes(byte[] buf, int start, int count)
   {
      Vm.arrayCopy(fbuf, pos, buf, start, count);
      pos += count;
      return count;
   }

   /**
    * Writes bytes into the buffer.
    *
    * @param buf The byte array to write data from.
    * @param start The offset position in the array.
    * @param count The number of bytes to write.
    * @return The number of bytes written.
    */
   public int writeBytes(byte[] buf, int start, int count)
   {
      Vm.arrayCopy(buf, start, fbuf, pos, count);
      pos += count;
      return count;
   }
}
