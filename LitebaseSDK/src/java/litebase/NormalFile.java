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
 * This is a normal file, ie, a file that is stored in disk.
 */
class NormalFile extends XFile
{
   /** 
    * The cache size for the table files.
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

   // Cache support.
   /**
    * The cache for the file.
    */
   private byte[] cache;
   
   /**
    * The initial position of the cache.
    */
   private int cacheIni;
   
   /**
    * The final position of the cache.
    */
   private int cacheEnd;
   
   /**
    * The initial size of the cache.
    */
   private int cacheInitialSize; 
   
   /** 
    * The initial position of the cache that is dirty.
    */
   private int cacheDirtyIni; 
   
   /**
    * The final position of the cache that is dirty.
    */
   private int cacheDirtyEnd;
   
   /**
    * Indicates if the cache is dirty and must be saved.
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
    * @param cacheSize The cache size to be used. -1 should be passed if the default value is to be used.
    * @throws IOException If an internal method throws it.
    */
   NormalFile(String name, boolean isCreation, int cacheSize) throws IOException
   {
      f = new File(name, isCreation? File.CREATE_EMPTY : File.READ_WRITE) // Opens or creates the file.
      {
         // flsobral@lb201_1: Overriding finalize method prevents files from being finalized before the LitebaseConnection.
         protected synchronized void finalize() 
         {
            if (LitebaseConnection.htDrivers.size() == 0)
               super.finalize();
         }
      };
      size = f.getSize(); // Gets its size.
      f.setPos(0); // Its current position is the first one.
      
      if (cacheSize != -1)
         cache = new byte[cacheInitialSize = cacheSize];
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
      if (cacheInitialSize < count || pos < cacheIni || (pos + count) > cacheEnd)
         refreshCache(count);
      System.arraycopy(cache, pos - cacheIni, buf, start, count);
      pos += count;
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
      if (cacheInitialSize < count || pos < cacheIni || (pos + count) > cacheEnd)
         refreshCache(count);
      System.arraycopy(buf, start, cache, pos - cacheIni, count);
      cacheIsDirty = true;
      if (pos < cacheDirtyIni) 
         cacheDirtyIni = pos;
      pos += count;
      if (pos > cacheDirtyEnd) 
         cacheDirtyEnd = pos;
      return count;
   }

   /**
    * Enlarges the file. This method MUST be called to grow the file - otherwise, <code>getSize()</code> won't work correctly.
    *
    * @param newSize The new size for the file.
    * @throws IOException If an internal method throws it.
    */
   void growTo(int newSize) throws IOException
   {
      f.setSize(newSize); // Enlarges the file and sets the new size.
      
      // juliana@227_23: solved possible crashes when using a table recovered which was being used with setRowInc().
      if (newSize - size > 0) // juliana@230_18: removed possible garbage in table files.
      {
         f.setPos(size);
         f.writeBytes(new byte[newSize - size]);
      }
      pos = (size = newSize) - 1; // The current position is the last one.
   }

   /**
    * Sets the current file position.
    *
    * @param newPos The new file position.
    * @throws IOException If an internal method throws it.
    * @throws DriverException If the table is corrupted and its access tries to read/write after the file end.
    */
   void setPos(int newPos) throws IOException, DriverException
   {
      if (newPos > size)
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_TABLE_CORRUPTED));
      if (pos != newPos) // Optimization: ignores if the position is unchanged.
         f.setPos(pos = newPos); 
   }
   
   /**
    * Flushs the cache into the disk.
    * 
    * @throws IOException If an internal method throws it.
    */
   void flushCache() throws IOException
   {
      f.setPos(cacheDirtyIni);
      cacheIsDirty = false;
      f.writeBytes(cache, cacheDirtyIni - cacheIni, cacheDirtyEnd - cacheDirtyIni);
   }
   
   /**
    * The cache must be refreshed if what is desired is not in it.
    * 
    * @param count The number of bytes that must be read.
    * @throws IOException If an internal method throws it or it is not possible to write all the data. 
    */
   private void refreshCache(int count) throws IOException, DriverException
   {
      if (cacheIsDirty)
         flushCache();
      if (cacheInitialSize < count)
         cache = new byte[cacheInitialSize = Math.max(CACHE_INITIAL_SIZE, count << 2)];
      if (size != 0)
      {
         f.setPos(cacheDirtyEnd = cacheIni = pos);
         
         // juliana@212_8: when reading a file, an exception must not be thrown when reading zero bytes.
         if (f.readBytes(cache, 0, cacheInitialSize) == -1 && pos != size && count > 0)
            throw new IOException(LitebaseMessage.getMessage(LitebaseMessage.ERR_CANT_READ));
      
         // Uses the cache size even if less was read, otherwise when filling a table, the cache will have to be refreshed all the times.
         cacheDirtyIni = cacheEnd = cacheIni + cacheInitialSize;       
      }
   }

   /** 
    * Sets the cache size. Pass -1 to set it back to the default value. 
    * 
    * @param newSize The new cache size.
    * @throws IOException If an internal method throws it.
    */
   private void setCacheSize(int newSize) throws IOException
   {
      int newCacheSize = Math.max(CACHE_INITIAL_SIZE, newSize);
      if (newCacheSize != cacheInitialSize)
      {
         if (cacheIsDirty)
            flushCache();
         cacheEnd = -1;                                                                                                        
         cache = null;
         cache = new byte[cacheInitialSize = newCacheSize];
      }
   }
   
   /** 
    * Closes this file.
    * 
    * @throws IOException If an internal method throws it.
    */
   public void close() throws IOException
   {
      if (cacheIsDirty)
         flushCache();
      if (finalPos > 0) // juliana@210a_11: fixed a problem that could crop data from database indices.
         f.setSize(finalPos);  // juliana@201_5: the .dbo file must be cropped so that it wont't be too large with zeros at the end of the file.
      f.close();
   }
   
   /** 
    * If there's enough memory, loads this file into memory by increasing the cache size. 
    * 
    * @param turnOn <code>true</code> to use a very big cache size; <code>false</code> to use the default cahce size.
    * @throws IOException If an internal method throws it.
    */
   void loadIntoMemory(boolean turnOn) throws IOException
   {
      if (!turnOn)
         setCacheSize(-1);
      else
      {
         int cacheSize = size == 0? 8192 : size; // If nothing was loaded, starts with a good cache size.
         if (cacheInitialSize < cacheSize && totalcross.sys.Vm.getFreeMemory() > cacheSize << 2)
            setCacheSize(cacheSize);
      }
   }
   
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
      f = new File(newName, File.READ_WRITE) // Opens or creates the file.
      {
         protected synchronized void finalize() 
         {
            // flsobral@lb201_1: Overriding finalize method prevents files from being finalized before the LitebaseConnection.
            if (LitebaseConnection.htDrivers.size() == 0)
               super.finalize();
         }
      };
   }
}
