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

import totalcross.io.*;

/**
 * This is a cross-file interface. It defines operations common to NormalFile and MemoryFile.
 */
abstract class XFile extends Stream
{
   /**
    * The file size.
    */
   int size;

   /**
    * The current position in the file.
    */
   int pos;

   /**
    * The final position of the file.
    */
   int finalPos;

   /**
    * Sets the file pointer to the given position. If it is beyond the end of the file, an exception will be thrown.
    *
    * @param position The desired position of the file.
    */
   abstract void setPos(int position) throws IOException;

   /**
    * Enlarges the file.
    *
    * @param newSize The new file size.
    */
   abstract void growTo(int newSize) throws IOException;
   
   /**
    * Does nothing. This method is here because it extends Stream.
    *
    * @throws IOException If an internal method throws it.
    */
   public void close() throws IOException {}
   
   /**
    * Does nothing. This method is here because it extends Stream.
    *
    * @param newName Ignored.
    */
   void rename(String newName) throws IOException {}
}
