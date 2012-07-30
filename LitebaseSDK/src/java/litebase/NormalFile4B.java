/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package litebase;

import totalcross.io.*;

/**
 * This is a normal file for BlackBerry, not using cache, ie, a file that is stored in disk.
 */
class NormalFile4B extends XFile
{
   /** 
    * The cache size for the table files. Used only on JavaSE.
    */
   static final int CACHE_INITIAL_SIZE = 2048;
   
   /**
    * .db extension (database file).
    */
   static final String DB_EXT = ".db";

   /**
    * .dbo extension (strings and blobs file).
    */
   static final String DBO_EXT = ".dbo";
   
   /**
    * The file.
    */
   File f;
   
   /**
    * Not used. Only here to be consistent with NormalFile.
    */
   boolean cacheIsDirty;
   
   // juliana@227_3: improved table files flush dealing.
   /**
    * Indicates if the cache file should not be flushed.
    */
   boolean dontFlush;
      
   // All methods just call the File ones.
   /**
    * Creates a disk file to store tables.
    *
    * @param name The name of the file.
    * @param isCreation Indicates if the file must be created or just open.
    * @param cacheSize Unused. It is here only because this constructor needs a third parameter. 
    * @throws IOException If an internal method throws it.
    */
   NormalFile4B(String name, boolean isCreation, int cachSize) throws IOException
   {
      f = new File(name, isCreation? File.CREATE : File.READ_WRITE) // Opens or creates the file.
      {
         // juliana@230_24: solved a possible TableNotClosedException on BB when not closing the connection before exiting the application.
         protected synchronized void finalize() 
         {
            if (LitebaseConnection.htDrivers.size() == 0)
               super.finalize();
         }
      };
      size = f.getSize(); // Gets its size.
      f.setPos(0); // Its current position is the first one.
      
      // juliana@227_3: improved table files flush dealing.
      if (name.indexOf('$') >= 0 || name.indexOf('&') >= 0)
         dontFlush = true;
   }

   /**
    * Reads file bytes.
    *
    * @param buf The byte array to read data into.
    * @param start The offset position in the array.
    * @param count The number of bytes to read.
    * @return The number of bytes read.
    * @throws IOException If an internal method throws it.
    */
   public int readBytes(byte[] buf, int start, int count) throws IOException
   {
      pos += count;
      if (f.readBytes(buf, start, count) == -1 && pos < size)
         throw new IOException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_READ));
      return count;
   }

   /**
    * Write bytes in a file.
    *
    * @param buf The byte array to write data from.
    * @param start The offset position in the array.
    * @param count The number of bytes to write.
    * @return count, to indicate that everything is ok.
    * @throws IOException If an internal method throws it. 
    */
   public int writeBytes(byte[] buf, int start, int count) throws IOException
   {
      pos += count;
      f.writeBytes(buf, start, count);
      if (!dontFlush) // juliana@227_3: improved table files flush dealing.
         f.flush();
      return count;
   }

   /**
    * Enlarges the file. This method MUST be called to grow the file - otherwise, <code>getSize</code> won't work correctly.
    *
    * @param newSize The new size for the file.
    * @throws IOException If an internal method throws it.
    */
   void growTo(int newSize) throws IOException
   {
      f.setSize(size = newSize); // Enlarges the file and sets the new size.
      pos = newSize - 1; // The current position is the last one.
   }

   /**
    * Sets the current file position.
    *
    * @param newPos The new file position.
    * @throws IOException If an internal method throws it.
    */
   void setPos(int newPos) throws IOException
   {
      if (pos != newPos) // Optimization: ignores if the position is unchanged.
         f.setPos(pos = newPos);
   }
   
   /** 
    * Closes this file.
    * 
    * @throws IOException If an internal method throws it.
    */
   public void close() throws IOException
   {
      if (finalPos > 0) // juliana@210a_11: fixed a problem that could crop data from database indices.
         f.setSize(finalPos); // juliana@201_5: the .dbo file must be cropped so that it wont't be too large with zeros at the end of the file.
      f.close();
   }
   
   /** 
    * Does nothing. Just to be consistent with NormalFile 
    * 
    * @param turnOn ignored.
    * @throws IOException Never happens.
    */
   void loadIntoMemory(boolean turnOn) throws IOException {}
   
   /**
    * Does nothing. Just to be consistent with NormalFile 
    * 
    * @throws IOException Never happens.
    */
   void flushCache() throws IOException {}
   
   // juliana@253_19: corrected a possible table corruption after a purge or a rename table only on Java SE.
   /**
    * Renames a Litebase normal file.
    * 
    * @param newName The new file name.
    * @throws IOException If an internal method throws it.
    */
   void rename(String newName) throws IOException
   {
      f.rename(newName);
      f = new File(newName, File.READ_WRITE); // Opens or creates the file.
   }
}
