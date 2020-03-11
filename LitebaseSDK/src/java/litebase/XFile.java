/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
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
