/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.io.Seekable;
import net.rim.device.api.io.file.FileIOException;
import net.rim.device.api.system.ControlledAccessException;
import totalcross.Launcher4B;
import totalcross.Launcher4B.Cache;
import totalcross.sys.Convert;
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

public class File4B extends RandomAccessStream
{
   protected String path;
   private String fsPath;
   private String fsPathLower;
   private int mode;
   private int slot;

   private FileConnection conn;
   private boolean connAsDir;
   private int pos;
   private int length;
   private Block block;
   private int blockOff;
   private InternalInputStream is;
   private InternalOutputStream os;
   private boolean internal;
   private boolean cacheEnabled;
   private Cache blockCache;
   private int blockSizePow2;
   private int blockSize;
   private int blockSizeMinusOne;
   boolean dontFinalize;
   
   private static Hashtable openPaths = new Hashtable(10);
   
   public static final int INVALID = 0;
   public static final int DONT_OPEN = 1;
   public static final int READ_WRITE = 2;
   public static final int READ_ONLY = 3;
   public static final int CREATE = 4;
   public static final int CREATE_EMPTY = 5;

   public static final byte TIME_ALL = (byte) 0xF;
   public static final byte TIME_CREATED = (byte) 1;
   public static final byte TIME_MODIFIED = (byte) 2;
   public static final byte TIME_ACCESSED = (byte) 4;

   public static final int ATTR_ARCHIVE = 1;
   public static final int ATTR_HIDDEN = 2;
   public static final int ATTR_READ_ONLY = 4;
   public static final int ATTR_SYSTEM = 8;

   private static final int MULTI_BLOCK_SIZE_POW2 = 10;
   private static final int SINGLE_BLOCK_SIZE_POW2 = 12;
   private static final int BLOCK_CACHE_CAPACITY = 8;
   private static final byte[] EMPTY_DATA = new byte[1024];
   
   public static String[] winceVols = {"\\Storage Card2\\", "\\Storage Card1\\", "\\SD Card\\", "\\Storage Card\\",
         "\\SD-MMCard\\", "\\CF Card\\"}; // guich@572_3
   
   private static final String INTERNAL_ROOT;
   private static final int OPEN_HANDLE_MAX_ATTEMPTS = 5;
   private static final int OPEN_HANDLE_INTERVAL = 500;
   
   private static final int OPERATION_CREATE_FILE = 1;
   private static final int OPERATION_CREATE_DIR = 2;
   private static final int OPERATION_LIST_FILES = 3;
   private static final int OPERATION_DELETE = 4;
   private static final int OPERATION_RENAME = 5;
   private static final int OPERATION_TRUNCATE = 6;
   private static final int OPERATION_OPEN_INPUT_STREAM = 7;
   private static final int OPERATION_OPEN_OUTPUT_STREAM = 8;
   
   static
   {
      Launcher4B.requestAppPermission(ApplicationPermissions.PERMISSION_FILE_API);
      
      String internalRoot = "/store";
      
      Enumeration en = FileSystemRegistry.listRoots();
      while (en.hasMoreElements())
      {
         String root = (String)en.nextElement();
         if (root.equals("system/"))
         {
            internalRoot = "/system";
            break;
         }
      }
      
      INTERNAL_ROOT = internalRoot;
   }
   
   public File4B(String path, int mode, int slot) throws IllegalArgumentIOException, FileNotFoundException, IOException
   {
      if (mode == 8) mode = CREATE_EMPTY; // keep compatibility
      if (path.length() == 0)
         throw new IllegalArgumentIOException("path", path);
      if (mode < DONT_OPEN || mode > CREATE_EMPTY)
         throw new IllegalArgumentIOException("mode", Convert.toString(mode));
      if (slot < -1)
         throw new IllegalArgumentIOException("slot", Convert.toString(slot));

      fsPath = normalizePath(path);
      fsPathLower = fsPath.toLowerCase();
      
      this.path = path;
      this.mode = mode;
      this.slot = slot;
      internal = fsPathLower.startsWith(INTERNAL_ROOT); // indicates whether this file exists in the internal memory - which does not have file handle limitations
      
      // Now, perform operations and open file
      if (mode == DONT_OPEN)
         dontFinalize = true; // these objects don't need to be finalized, so avoid holding them in memory
      else
      {
         synchronized (openPaths)
         {
            try
            {
               if (openPaths.get(fsPathLower) != null)
                  throw new java.io.IOException("Cannot access the file because it is already in use: " + path);
               
               conn = (FileConnection)Connector.open("file://" + fsPath, mode == READ_ONLY ? Connector.READ : Connector.READ_WRITE);
               is = new InternalInputStream(this);
               if (mode != READ_ONLY)
                  os = new InternalOutputStream(this);
               
               boolean exists = conn.exists();
               int length = exists ? (int)conn.fileSize() : 0;
               
               if (mode == CREATE_EMPTY)
               {
                  if (!exists)
                     operateFile(this, OPERATION_CREATE_FILE, null);
                  else if (length > 0)
                  {
                     operateFile(this, OPERATION_TRUNCATE, new Integer(0));
                     length = 0;
                  }
               }
               else if (!exists)
               {
                  if (mode == CREATE)
                     operateFile(this, OPERATION_CREATE_FILE, null);
                  else
                     throw new FileNotFoundException(path);
               }
               
               initAndSeek(length, 0, false, true); // initially, don't use the block cache
               openPaths.put(fsPathLower, ""); // file is open
            }
            catch (ControlledAccessException ex)
            {
               mode = INVALID;
               throw new IOException("Cannot access file due to security reasons: " + path);
            }
            catch (FileIOException ex)
            {
               mode = INVALID;
               if (ex.getErrorCode() == FileIOException.NO_SUCH_ROOT) // bruno@tc126_19: ignore NO_SUCH_ROOT and assume file does not exist
                  throw new FileNotFoundException(path);
               else
                  throw new IOException(getIOExceptionMessage(ex));
            }
            catch (java.io.IOException ex)
            {
               mode = INVALID;
               throw new IOException(getIOExceptionMessage(ex));
            }
         }
      }
   }

   public File4B(String path, int mode) throws IllegalArgumentIOException, FileNotFoundException, IOException
   {
      this(path, mode, -1);
   }

   public File4B(String path) throws IllegalArgumentIOException, IOException
   {
      this(path, DONT_OPEN, -1);
   }

   public static final boolean isCardInserted(int slot) throws IllegalArgumentIOException
   {
      if (slot < -1)
         throw new totalcross.io.IllegalArgumentIOException("slot", Convert.toString(slot));

      return true;
   }

   public static final String getCardSerialNumber(int slot) throws IllegalArgumentIOException, IOException
   {
      return null;
   }

   public static final File4B getCardVolume() throws IOException
   {
      Enumeration en = FileSystemRegistry.listRoots();
      while (en.hasMoreElements())
      {
         String root = (String)en.nextElement();
         if (!root.equals("store/") && !root.equals("system/"))
         {
            try
            {
               File4B f = new File4B("/" + root);
               if (f.isDir())
                  return f;
            }
            catch (FileNotFoundException e) { }
         }
      }

      return null;
   }

   public String getPath()
   {
      return path;
   }
   
   public int getSlot()
   {
      return slot;
   }

   public final int getSize() throws IOException
   {
      if (mode == DONT_OPEN)
      {
         assertOpen();
         if (!conn.isDirectory())
            throw new IOException("File is not a root directory");
         
         long size = conn.availableSize();
         return (int)(size > 2147483647 ? 2147483647 : size);
      }
      else if (mode == INVALID)
         throw new IOException("Invalid file handle");

      return length;
   }

   public final int getAttributes() throws IOException
   {
      if (mode == DONT_OPEN)
         assertOpen();
      else if (mode == INVALID)
         throw new IOException("Invalid file handle");

      int attr = 0;
      if (conn.isHidden())
         attr += ATTR_HIDDEN;
      if (!conn.canWrite())
         attr += ATTR_READ_ONLY;

      return attr;
   }

   public final void setAttributes(int attr) throws IllegalArgumentIOException, IOException
   {
      if (mode == DONT_OPEN)
         assertOpen();
      else if (mode == INVALID)
         throw new IOException("Invalid file object");
      if (mode == READ_ONLY)
         throw new IOException("Operation cannot be used in READ_ONLY mode");

      try
      {
         conn.setHidden((attr & ATTR_HIDDEN) == ATTR_HIDDEN);
         conn.setWritable((attr & ATTR_READ_ONLY) == 0);
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(getIOExceptionMessage(ex));
      }
   }

   public final Time getTime(byte whichTime) throws IllegalArgumentIOException, IOException
   {
      if (mode == DONT_OPEN)
         assertOpen();
      else if (mode == INVALID)
         throw new IOException("Invalid file handle");

      long time;
      switch (whichTime)
      {
         case File4B.TIME_CREATED:
         case File4B.TIME_ACCESSED:
            time = -1;
            break;
         case TIME_MODIFIED:
            time = conn.lastModified();
            break;
         default:
            throw new IllegalArgumentIOException("whichTime", Convert.toString(whichTime));
      }

      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date(time));
      int year = cal.get(Calendar.YEAR);
      int month = cal.get(Calendar.MONTH) + 1;
      int day = cal.get(Calendar.DAY_OF_MONTH);
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      int minute = cal.get(Calendar.MINUTE);
      int second = cal.get(Calendar.SECOND);
      int millis = cal.get(Calendar.MILLISECOND);

      return new Time(year, month, day, hour, minute, second, millis);
   }

   public final void setTime(byte whichTime, Time time) throws IllegalArgumentIOException, IOException
   {
      if (mode == DONT_OPEN)
         assertOpen();
      else if (mode == INVALID)
         throw new IOException("Invalid file handle");
      if (mode == READ_ONLY)
         throw new IOException("Operation cannot be used in READ_ONLY mode");

      switch (whichTime)
      {
         case File4B.TIME_CREATED:
         case File4B.TIME_ACCESSED:
         case File4B.TIME_MODIFIED:
         case File4B.TIME_ALL:
            break;
         default:
            throw new IllegalArgumentIOException("whichTime", Convert.toString(whichTime));
      }
   }

   public File4B getParent() throws IOException
   {
      if (mode == DONT_OPEN)
         assertOpen();
      else if (mode == INVALID)
         throw new IOException("Invalid file handle");
      if (fsPath.equals("/"))
         return null;

      return new File4B(conn.getPath());
   }

   public final String[] listFiles() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid file handle");
      else if (mode != DONT_OPEN)
         throw new IOException("Operation can ONLY be used in DONT_OPEN mode");
      
      assertOpen();
      
      if (!conn.exists())
         throw new FileNotFoundException(path);

      try
      {
         Enumeration e = (Enumeration)operateFile(this, OPERATION_LIST_FILES, null);
         Vector v = new Vector(20);

         while (e.hasMoreElements())
            v.addElement(e.nextElement());

         String[] list = new String[v.size()];
         v.copyInto(list);
         return list;
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(getIOExceptionMessage(ex));
      }
   }
   
   final public boolean isEmpty() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid file handle");

      assertOpen(false);
      
      if (!conn.exists())
         throw new FileNotFoundException(path);

      try
      {
         if (conn.isDirectory())
         {
            Enumeration e = (Enumeration)operateFile(this, OPERATION_LIST_FILES, null);
            return !e.hasMoreElements();
         }
         else
         {
            return conn.fileSize() == 0;
         }      
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(getIOExceptionMessage(ex));
      }
   }

   public final boolean exists() throws IOException
   {
      if (mode == DONT_OPEN)
         assertOpen();
      else if (mode == INVALID)
         throw new IOException("Invalid file handle");

      return conn.exists();
   }

   public final void delete() throws FileNotFoundException, IOException
   {
      if (mode == DONT_OPEN)
         assertOpen();
      else if (mode == INVALID)
         throw new IOException("Invalid file handle");
      if (mode == READ_ONLY)
         throw new IOException("Operation cannot be used in READ_ONLY mode");
      if (!conn.exists())
         throw new FileNotFoundException(path);

      try
      {
         if (mode != DONT_OPEN)
         {
            if (mode != READ_ONLY)
               os.close();
            is.close();
         }

         operateFile(this, OPERATION_DELETE, null);
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(getIOExceptionMessage(ex));
      }
      finally
      {
         synchronized (openPaths)
         {
            openPaths.remove(fsPathLower); // file is closed
         }
         
         mode = INVALID;
         dontFinalize = true;
      }
   }

   public final boolean isDir() throws IOException
   {
      if (mode == DONT_OPEN)
         assertOpen();
      else if (mode == INVALID)
         throw new IOException("Invalid file handle");

      return conn.isDirectory();
   }

   public final void createDir() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid file handle");
      else if (mode != DONT_OPEN)
         throw new IOException("Operation can ONLY be used in DONT_OPEN mode");
      
      File4B parent = getParent();
      if (parent != null && !parent.exists()) // create parent first
         parent.createDir();
      
      assertOpen(true);
      
      try
      {
         if (conn.exists())
            throw new IOException("File or directory already exists");

         operateFile(this, OPERATION_CREATE_DIR, null);
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(getIOExceptionMessage(ex));
      }
   }

   public final void rename(String path) throws IllegalArgumentIOException, IOException
   {
      if (mode == DONT_OPEN)
         assertOpen();
      else if (mode == INVALID)
         throw new IOException("Invalid file handle");
      if (mode == READ_ONLY)
         throw new IOException("Operation cannot be used in READ_ONLY mode");
      if (path.length() == 0)
         throw new IllegalArgumentIOException("path", path);

      path = normalizePath(path);
      String fromParent = conn.getPath();
      
      try
      {
         // Check if files are in the same directory
         FileConnection newConn = (FileConnection)Connector.open("file://" + path, mode == READ_ONLY ? Connector.READ : Connector.READ_WRITE);
         String toParent = newConn.getPath();
         String newName = newConn.getName();
         newConn.close();

         if (!fromParent.equals(toParent))
            throw new IOException("Cannot rename file to a different directory");

         operateFile(this, OPERATION_RENAME, newName);
         close();
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(getIOExceptionMessage(ex));
      }
   }

   public void flush() throws totalcross.io.IOException
   {
      if (mode <= DONT_OPEN)
         throw new IOException(mode == INVALID ? "Invalid file handle" : "Operation cannot be used in mode DONT_OPEN");
      if (mode == READ_ONLY)
         throw new IOException("Operation cannot be used in READ_ONLY mode");

      try
      {
         if (cacheEnabled)
         {
            int blockCount = length >> blockSizePow2;
            if ((length & blockSizeMinusOne) > 0)
               blockCount++;
            
            // Flush all cached blocks
            for (int idx = 0; idx < blockCount; idx++)
            {
               Block block = (Block)blockCache.get(idx);
               if (block != null)
                  flushBlock(block);
            }
         }
         else // cache is disabled
            flushBlock(block);
         
         if (mode != READ_ONLY || os.flush(0, Integer.MAX_VALUE)) // guich@tc138: os is null in READ_ONLY
            is.close(); // if output stream had to be flushed, close input stream to force it to be reopened later
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(getIOExceptionMessage(ex));
      }
   }

   public void close() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid file handle");

      try
      {
         close(false);
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(getIOExceptionMessage(ex));
      }
      finally
      {
         synchronized (openPaths)
         {
            openPaths.remove(fsPathLower); // file is closed
         }
         
         mode = INVALID;
         dontFinalize = true;
      }
   }

   public final int readBytes(byte[] b, int off, int len) throws IOException
   {
      if (mode <= DONT_OPEN)
         throw new IOException(mode == INVALID ? "Invalid file handle" : "Operation cannot be used in mode DONT_OPEN");
      if (off < 0)
         throw new IllegalArgumentIOException("off", Convert.toString(off));
      if (len < 0) // guich@tc112_32: <, not <=
         throw new IllegalArgumentIOException("len", Convert.toString(len));
      if (len == 0)
         return 0; // flsobral@tc113_43: return 0 if asked to read 0.
      
      int r = 0, step, max;
      while (r != len)
      {
         if (pos == length) // EOF
            break;
         
         step = len - r;
         max = block.size - blockOff;
         if (step > max)
            step = max;

         System.arraycopy(block.data, blockOff, b, off, step);

         r += step;
         off += step;
         pos += step;
         blockOff += step;
            
         // Nothing more to read from this block, load next block
         if (blockOff == blockSize)
         {
            try
            {
               block = loadBlock(block.idx + 1); // load next block
               blockOff = 0;
            }
            catch (java.io.IOException ex)
            {
               throw new IOException(getIOExceptionMessage(ex));
            }
         }
      }

      return r == 0 ? -1 : r;
   }

   public final int writeBytes(byte[] b, int off, int len) throws IOException
   {
      if (mode <= DONT_OPEN)
         throw new IOException(mode == INVALID ? "Invalid file handle" : "Operation cannot be used in mode DONT_OPEN");
      if (mode == READ_ONLY)
         throw new IOException("Operation cannot be used in READ_ONLY mode");
      if (off < 0)
         throw new IllegalArgumentIOException("off", Convert.toString(off));
      if (len < 0) // guich@tc112_32: <, not <=
         throw new IllegalArgumentIOException("len", Convert.toString(len));
      if (len == 0)
         return 0;
      
      int w = 0, step, max;
      while (w != len)
      {
         step = len - w;
         max = blockSize - blockOff;
         if (step > max)
            step = max;

         System.arraycopy(b, off, block.data, blockOff, step);
         
         if (blockOff < block.dirtyStart)
            block.dirtyStart = blockOff; // update the start of the dirty part of the block
         
         w += step;
         off += step;
         pos += step;
         blockOff += step;
         
         if (blockOff > block.dirtyEnd)
            block.dirtyEnd = blockOff; // update the end of the dirty part of the block
         
         if (blockOff > block.size)
            block.size = blockOff;
         
         if (pos > length)
            length = pos;
         
         // Nothing more to write on this block, load next block
         if (blockOff == blockSize)
         {
            try
            {
               block = loadBlock(block.idx + 1);
               blockOff = 0;
            }
            catch (java.io.IOException ex)
            {
               throw new IOException(getIOExceptionMessage(ex));
            }
         }
      }

      return w;
   }
   
   public int getPos() throws totalcross.io.IOException
   {
      if (mode <= DONT_OPEN)
         throw new IOException(mode == INVALID ? "Invalid file handle" : "Operation cannot be used in mode DONT_OPEN");  
      
      return pos;
   }
   
   public final void setPos(int offset, int origin) throws IOException
   {
      if (mode <= DONT_OPEN)
         throw new IOException(mode == INVALID ? "Invalid file handle" : "Operation cannot be used in mode DONT_OPEN");      
      
      int newPos;

      switch (origin)
      {
         case SEEK_SET: newPos = offset; break;
         case SEEK_CUR: newPos = this.pos + offset; break;
         case SEEK_END: newPos = this.length + offset - 1; break;
         default: throw new IllegalArgumentException();
      }

      if (newPos < 0)
         throw new IOException();
      setPos(newPos);
   }   

   public final void setPos(int pos) throws IOException
   {
      if (mode <= DONT_OPEN)
         throw new IOException(mode == INVALID ? "Invalid file handle" : "Operation cannot be used in mode DONT_OPEN");
      if (pos < 0)
         throw new IOException("Argument 'pos' cannot be negative");

      if (pos > length)
         pos = length;

      if (pos != this.pos)
      {
         try
         {
            if (cacheEnabled)
            {
               // Get the number of the block that contains this position
               int idx = pos >> blockSizePow2;
      
               // Load the block, if necessary
               if (block == null || idx != block.idx)
                  block = loadBlock(idx);
      
               blockOff = pos & blockSizeMinusOne;
               this.pos = pos;
            }
            else // cache is disable, so enable cache since this file is being used as a random access file
            {
               close(true);
               initAndSeek(length, pos, true, false);
            }
         }
         catch (java.io.IOException ex)
         {
            throw new IOException(getIOExceptionMessage(ex));
         }
      }
   }
   
   public final void setSize(int newSize) throws IOException
   {
      if (mode <= DONT_OPEN)
         throw new IOException(mode == INVALID ? "Invalid file handle" : "Operation cannot be used in mode DONT_OPEN");
      if (mode == READ_ONLY)
         throw new IOException("Operation cannot be used in READ_ONLY mode");
      if (newSize < 0)
         throw new IllegalArgumentIOException("newSize", Convert.toString(newSize));

      try
      {
         if (newSize < length) // if file is being truncated
         {
            close(true); // close streams, keeping the connection opened
            operateFile(this, OPERATION_TRUNCATE, new Integer(newSize));
            
            initAndSeek(newSize, newSize, cacheEnabled, false);
         }
         else if (newSize > length) // else, file is being expanded
         {
            // Seek the last position and write 0
            setPos(length);

            int r = newSize - length;
            while (r > 0)
               r -= writeBytes(EMPTY_DATA, 0, (r > EMPTY_DATA.length ? EMPTY_DATA.length : r));
         }
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(getIOExceptionMessage(ex));
      }
   }

   protected void finalize()
   {
      try
      {
         close();
      }
      catch (IOException ex)
      {
      }
   }
   
   private void initAndSeek(int length, int pos, boolean enableCache, boolean forceUpdate) throws IOException, java.io.IOException
   {
      if (forceUpdate || enableCache != cacheEnabled)
      {
         if (enableCache) // cache is enabled, use multiple blocks
         {
            blockCache = new Cache("BlockCache", BLOCK_CACHE_CAPACITY);
            blockSizePow2 = MULTI_BLOCK_SIZE_POW2;
         }
         else // cache is disabled, use a single block
         {
            blockCache = null;
            blockSizePow2 = SINGLE_BLOCK_SIZE_POW2;
         }
         
         blockSize = 1 << blockSizePow2;
         blockSizeMinusOne = blockSize - 1;
         block = null; // release block before loading the new block to avoid size conflicts
         
         this.cacheEnabled = enableCache;
      }
      
      this.length = length;
      this.pos = pos;
      
      block = loadBlock(pos >> blockSizePow2);
      blockOff = pos & blockSizeMinusOne;
   }
   
   private void assertOpen() throws IOException
   {
      assertOpen(false);
   }
   
   private void assertOpen(boolean asDir) throws IOException
   {
      try
      {
         if (conn != null && conn.isOpen() && asDir != connAsDir)
         {
            conn.close();
            conn = null;
         }
         
         if (conn == null || !conn.isOpen())
         {
            conn = (FileConnection)Connector.open("file://" + fsPath + (asDir ? "/" : ""), mode == READ_ONLY ? Connector.READ : Connector.READ_WRITE);
            connAsDir = asDir;
         }
         
         if (conn.exists() && asDir != conn.isDirectory()) // make sure if the path is an existing directory, it has the trailing slash
            assertOpen(!asDir);
      }
      catch (java.io.IOException ex)
      {
         throw new IOException(getIOExceptionMessage(ex));
      }
   }
   
   private void flushBlock(Block block) throws java.io.IOException
   {
      int idx = block.idx;
      int dirtyStart = block.dirtyStart;
      int dirtyEnd = block.dirtyEnd;
      int dirtyLength = dirtyEnd - dirtyStart;
      
      if (dirtyLength > 0) // block is dirty, so it has to be flushed
      {
         if (cacheEnabled)
         {
            Block prev = (Block)blockCache.get(idx - 1);
            if (prev != null && prev.physicalSize < blockSize) // cannot position output stream at "start" offset until previous block fully exists physically
               flushBlock(prev);
         }
         
         // Now, write data and update positions
         int start = (idx << blockSizePow2) + dirtyStart;
         if (os != null) os.write(start, block.data, dirtyStart, dirtyLength);
         
         // Update block attributes
         block.dirtyStart = Integer.MAX_VALUE;
         block.dirtyEnd = 0;
         block.physicalSize = block.size;
      }
   }
   
   private Block loadBlock(int idx) throws java.io.IOException
   {
      Block block;
      boolean initialize = false;
      
      if (cacheEnabled) // cache is enabled, use multiple blocks
      {
         block = (Block)blockCache.get(idx);
         if (block == null) // not in cache
         {
            if (blockCache.isFull())
            {
               block = (Block)blockCache.removeLRU(); // remove LRU block from cache            
               flushBlock(block); // flush removed block
            }
            else
               block = new Block(blockSize);
            initialize = true;
         }
         
         if (!blockCache.put(new Integer(idx), block))
            throw new java.io.IOException("Unexpected I/O error; failed to cache file block");
      }
      else // cache is disabled, use a single block
      {
         block = this.block;
         if (block != null)
            flushBlock(block);
         else
            block = new Block(blockSize);
         initialize = true;
      }
      
      if (initialize)
      {
         int start = idx << blockSizePow2, size = 0;
         boolean exists = start < length;
         
         if (exists) // existing block, so load from disk
         {
            int end = start + blockSize;
            if (end > length)
               end = length;
            size = end - start;
            
            if (mode != READ_ONLY || os.flush(start, end))
               is.close(); // if output stream had to be flushed, close input stream to force it to be reopened later

            // Now read data
            int r = is.read(start, block.data, 0, size);
            if (r != size)
               throw new java.io.IOException("Unexpected I/O error; wrong block size (expected " + size + ", but got " + r + ")");
         }
         
         // Initialize block attributes
         block.idx = idx;
         block.dirtyStart = Integer.MAX_VALUE;
         block.dirtyEnd = 0;
         block.physicalSize = block.size = size;
      }
      
      return block;
   }
   
   private void close(boolean keepConnection) throws IOException, java.io.IOException
   {
      if (mode != DONT_OPEN)
      {
         flush();
         
         block = null;
         if (cacheEnabled)
            blockCache.clear();
         
         if (mode != READ_ONLY)
            os.close();
         is.close();
      }
      
      if (!keepConnection)
         conn.close();
   }
   
   private static String normalizePath(String path)
   {
      path = Convert.normalizePath(path);
      if (path.startsWith("device/")) // flsobral@tc110_108: added support for the alias "device/".
         path = "/store/home/user" + path.substring(6);
      else if (!path.startsWith("/")) // bruno@tc123_23: the path must always start with a slash, otherwise strange things can happen
         path = "/" + path;
      if (path.endsWith("/")) // remove trailing '/', if present, to normalize path (it will be added later if necessary)
         path = path.substring(0, path.length() - 1);      
      
      return path;
   }
   
   private static Object operateFile(File4B file, int operation, Object param) throws java.io.IOException
   {
      FileConnection conn = file.conn;
      boolean internal = file.internal;
      
      synchronized (InternalStream.streamCacheLock)
      {
         int attempts = 1;
         while (true)
         {
            try
            {
               switch (operation)
               {
                  case OPERATION_CREATE_FILE:
                     conn.create();
                     return null;
                     
                  case OPERATION_CREATE_DIR:
                     conn.mkdir();
                     return null;
                     
                  case OPERATION_LIST_FILES:
                     return conn.list();
                     
                  case OPERATION_DELETE:
                     conn.delete();
                     return null;
                     
                  case OPERATION_RENAME:
                     conn.rename((String)param);
                     return null;
                     
                  case OPERATION_TRUNCATE:
                     conn.truncate(((Integer)param).intValue());
                     return null;
                     
                  case OPERATION_OPEN_INPUT_STREAM:
                     InputStream is = conn.openInputStream();
                     is.skip(((Integer)param).intValue());
                     return is;
                     
                  case OPERATION_OPEN_OUTPUT_STREAM:
                     return conn.openOutputStream(((Integer)param).intValue());
                     
                  default:
                     return null;
               }
            }
            catch (FileIOException ex)
            {
               switch (ex.getErrorCode())
               {
                  case FileIOException.FILE_NOT_OPEN:
                     conn = file.conn = (FileConnection)Connector.open("file://" + file.fsPath, file.mode == READ_ONLY ? Connector.READ : Connector.READ_WRITE);
                     break;
                  case FileIOException.NO_FREE_HANDLES:
                     if (internal)
                        throw ex;
                     else if (!InternalStream.releaseHandle())
                     {
                        if (attempts++ >= OPEN_HANDLE_MAX_ATTEMPTS)
                           throw ex; // max attempts exceeded, so throw inner exception
                        else
                           Vm.sleep(OPEN_HANDLE_INTERVAL); // wait a little bit; maybe a file handle is freed?
                     }
                     break;
                  default:
                     throw ex;
               }
            }
         }
      }
   }
   
   private static String getIOExceptionMessage(java.io.IOException ex)
   {
      if (ex instanceof FileIOException)
      {
         int errCode = ((FileIOException)ex).getErrorCode();
         switch (errCode)
         {
            case FileIOException.CONTENT_BUILT_IN:
               return ex.getMessage() + "; cause: The operation is not allowed because the content is built-in (preloaded).";
            case FileIOException.DIRECTORY_ALREADY_EXISTS:
               return ex.getMessage() + "; The directory already exists.";
            case FileIOException.DIRECTORY_FULL:
               return ex.getMessage() + "; cause: The directory is full.";
            case FileIOException.DIRECTORY_NOT_EMPTY:
               return ex.getMessage() + "; cause: The directory is not empty.";
            case FileIOException.DIRECTORY_NOT_FOUND:
               return ex.getMessage() + "; cause: The directory cannot be found.";
            case FileIOException.FILE_HANDLES_OPEN:
               return ex.getMessage() + "; cause: The file handle used is already open.";
            case FileIOException.FILE_NOT_OPEN:
               return ex.getMessage() + "; cause: The file is no longer open.";
            case FileIOException.FILE_SYSTEM_UNAVAILABLE:
               return ex.getMessage() + "; cause: The file system is unavailable.";
            case FileIOException.FILE_TOO_LARGE:
               return ex.getMessage() + "; cause: The operation failed because the file create is too large for the system.";
            case FileIOException.FILENAME_ALREADY_EXISTS:
               return ex.getMessage() + "; cause: The filename already exists.";
            case FileIOException.FILENAME_NOT_FOUND:
               return ex.getMessage() + "; cause: The filename cannot be found.";
            case FileIOException.FILENAME_TOO_LONG:
               return ex.getMessage() + "; cause: The filename is too long.";
            case FileIOException.FILESYSTEM_EMPTY:
               return ex.getMessage() + "; cause: The file system is empty.";
            case FileIOException.FILESYSTEM_FULL:
               return ex.getMessage() + "; cause: The file system is full.";
            case FileIOException.FS_ALREADY_MOUNTED:
               return ex.getMessage() + "; cause: The file system is already mounted.";
            case FileIOException.FS_NOT_MOUNTED:
               return ex.getMessage() + "; cause: The file system is not mounted.";
            case FileIOException.FS_VERIFICATION_FAILED:
               return ex.getMessage() + "; cause: The file system failed to be mounted because of a verification error.";
            case FileIOException.GENERAL_ERROR:
               return ex.getMessage() + "; cause: A general error occurred.";
            case FileIOException.INVALID_CHARACTERS:
               return ex.getMessage() + "; cause: The string specified contains invalid characters.";
            case FileIOException.INVALID_HANDLE:
               return ex.getMessage() + "; cause: The file system handle used in the file operation is currently invalid.";
            case FileIOException.INVALID_OPERATION:
               return ex.getMessage() + "; cause: The operation requested is invalid.";
            case FileIOException.INVALID_PARAMETER:
               return ex.getMessage() + "; cause: The file system received an invalid parameter.";
            case FileIOException.IS_A_DIRECTORY:
               return ex.getMessage() + "; cause: The filename requested is a directory.";
            case FileIOException.MEDIUM_NOT_FORMATTED:
               return ex.getMessage() + "; cause: The medium is not formatted.";
            case FileIOException.NO_FREE_HANDLES:
               return ex.getMessage() + "; cause: There are no more free handles.";
            case FileIOException.NO_SUCH_ROOT:
               return ex.getMessage() + "; cause: The root specified is not available.";
            case FileIOException.NOT_A_DIRECTORY:
               return ex.getMessage() + "; cause: The filename requested is not a directory.";
            case FileIOException.NOT_A_FILE:
               return ex.getMessage() + "; cause: The filename requested is not a file.";
            case FileIOException.OS_BUSY:
               return ex.getMessage() + "; cause: The operating system is busy.";
            case FileIOException.STREAM_ALREADY_OPENED:
               return ex.getMessage() + "; cause: The requested stream is already opened.";
            //$IF,OSVERSION,>=,450
            case FileIOException.FILE_BUSY:
               return ex.getMessage() + "; cause: The operation failed because file is currently opened.";
            //$END
            //$IF,OSVERSION,>=,460
            case FileIOException.FS_LOCKED_BY_OTHER_DEVICE:
               return ex.getMessage() + "; cause: The operation failed because the sdcard is already locked by another device.";
            //$END
            //$IF,OSVERSION,>=,500
            case FileIOException.DIRPATH_TOO_LONG:
               return ex.getMessage() + "; cause: The directory path is too long.";
            //$END
            default:
               return ex.getMessage() + "; cause: An unknown error occurred (" + errCode + ").";
         }
      }
      else
         return ex.getMessage();
   }
   
   private static void listFiles(String dir, Vector files) throws IOException // guich@tc115_92
   {
      String[] list = new File(dir).listFiles();
      if (list != null)
         for (int i =0; i < list.length; i++)
         {
            String p = list[i];
            String full = Convert.appendPath(dir,p);
            files.addElement(full);
            if (p.endsWith("/"))
               listFiles(full, files);
         }
   }
   
   public static String[] listFiles(String dir) throws IOException // guich@tc115_92
   {
      Vector files = new Vector(50);
      dir = Convert.appendPath(dir,"/");
      files.addElement(dir);
      listFiles(dir,files);
      files.qsort();
      return (String[])files.toObjectArray();
   }

   public static void deleteDir(String dir) throws IOException // guich@tc115_92
   {
      String[] files = listFiles(dir);
      for (int i = files.length; --i >= 0;)
         new File((String)files[i]).delete();
   }
   
   public static String[] listRoots() // fabio@tc122_14
   {
      Vector v = new Vector();
      Enumeration en = FileSystemRegistry.listRoots();
      
      while (en.hasMoreElements())
         v.addElement("/" + (String)en.nextElement());
      
      String[] result = new String[v.size()];
      v.copyInto(result);
      
      return result;
   }
   
   public void copyTo(File dest) throws IOException // guich@tc126_8
   {
      setPos(0);
      try {dest.setPos(0);} catch (IOException ioe) {}
      byte[] buf = new byte[4096];
      int n = 0;
      while ((n=readBytes(buf, 0, buf.length)) > 0)
         dest.writeBytes(buf,0,n);
   }
   
   public void moveTo(File dest) throws IOException // guich@tc126_8
   {
      if (mode == READ_ONLY)
         throw new IOException("Operation cannot be used in READ_ONLY mode");
      copyTo(dest);
      delete();
   }
   

   public int chmod(int mod) throws IOException // guich@tc126_16
   {
      if (mode == INVALID)
         throw new IOException("Invalid file handle"); //flsobral@tc126: object must be valid.

      if (!exists())
         throw new FileNotFoundException(path); //flsobral@tc126: throw exception if the file does not exist.      
      return -1;
   }
   
   public static void copy(String src, String dst) throws IOException // guich@tc126_43
   {
      File fin=null,fout=null;
      try
      {
         fin = new File(src,File.READ_ONLY);
         fout = new File(dst,File.CREATE_EMPTY);
         fin.copyTo(fout);
      }
      finally
      {
         try {if (fin != null) fin.close();} catch (Exception e) {}
         try {if (fout != null) fout.close();} catch (Exception e) {}
      }
   }
   
   public static void move(String src, String dst) throws IOException // guich@tc126_43
   {
      File fin=null,fout=null;
      try
      {
         fin = new File(src,File.READ_WRITE);
         fout = new File(dst,File.CREATE_EMPTY);
         fin.moveTo(fout);
      }
      finally
      {
         try {if (fout != null) fout.close();} catch (Exception e) {}
      }
   }
   
   private static class Block
   {
      public byte[] data;
      public int idx;
      public int size;
      public int physicalSize;
      public int dirtyStart;
      public int dirtyEnd;
      
      public Block(int capacity)
      {
         data = new byte[capacity];
      }
   }
   
   private static abstract class InternalStream
   {
      protected Object id;
      protected File4B file;
      protected int pos;
      protected boolean isOpen;
      protected boolean isCached;
      
      protected static Cache streamCache = new Cache("StreamCache");
      private static long streamCount = Long.MIN_VALUE;
      protected static Object streamCacheLock = new Object();
      private static Object streamCountLock = new Object();
      
      public InternalStream(File4B file)
      {
         synchronized (streamCountLock)
         {
            id = new Long(streamCount++);
         }
         
         this.file = file;
         isCached = !file.internal; // internal streams are never cached
      }
      
      public static boolean releaseHandle() throws java.io.IOException
      {
         synchronized (streamCacheLock)
         {
            InternalStream stream = (InternalStream)streamCache.removeLRU();
            if (stream != null)
            {
               stream.close();
               return true;
            }
            else
               return false;
         }
      }
      
      public abstract void close() throws java.io.IOException;
   }
   
   private static class InternalInputStream extends InternalStream
   {
      private InputStream stream;
      //$IF,OSVERSION,>=,500
      private Seekable seekableStream;
      //$END
      
      public InternalInputStream(File4B file)
      {
         super(file);
      }
      
      public int read(int pos, byte[] b, int off, int len) throws java.io.IOException
      {
         synchronized (streamCacheLock)
         {
            if (!isOpen)
            {
               stream = (InputStream)operateFile(file, OPERATION_OPEN_INPUT_STREAM, new Integer(pos));
               //$IF,OSVERSION,>=,500
               seekableStream = stream instanceof Seekable ? (Seekable)stream : null;
               //$END
               
               isOpen = true;
               this.pos = pos;
            }
            else if (this.pos != pos)
            {
               //$IF,OSVERSION,>=,500
               if (seekableStream != null)
                  seekableStream.setPosition(pos);
               else
               {
               //$END
               if (pos > this.pos)
                  stream.skip(pos - this.pos);
               else
               {
                  stream.close();
                  stream = (InputStream)operateFile(file, OPERATION_OPEN_INPUT_STREAM, new Integer(pos));
                  //$IF,OSVERSION,>=,500
                  seekableStream = stream instanceof Seekable ? (Seekable)stream : null;
                  //$END
               }
               //$IF,OSVERSION,>=,500
               }
               //$END
               this.pos = pos;
            }
            
            int r = stream.read(b, off, len);
            if (r > 0)
               this.pos += r;
            
            if (isCached)
               streamCache.put(id, this);
            
            return r;
         }
      }
      
      public void close() throws java.io.IOException
      {
         synchronized (streamCacheLock)
         {
            if (isOpen)
            {
               stream.close();
               isOpen = false;
               
               if (isCached)
                  streamCache.remove(id);
            }
         }
      }
   }
   
   private static class InternalOutputStream extends InternalStream
   {
      private OutputStream stream;
      //$IF,OSVERSION,>=,500
      private Seekable seekableStream;
      //$END
      private int dirtyStart;
      private int dirtyEnd;
      
      public InternalOutputStream(File4B file)
      {
         super(file);
         dirtyStart = Integer.MAX_VALUE;
      }
      
      public void write(int pos, byte[] b, int off, int len) throws java.io.IOException
      {
         synchronized (streamCacheLock)
         {
            if (!isOpen)
            {
               stream = (OutputStream)operateFile(file, OPERATION_OPEN_OUTPUT_STREAM, new Integer(pos));
               //$IF,OSVERSION,>=,500
               seekableStream = stream instanceof Seekable ? (Seekable)stream : null;
               //$END
               
               isOpen = true;
               this.pos = pos;
            }
            else if (this.pos != pos)
            {
               //$IF,OSVERSION,>=,500
               if (seekableStream != null)
                  seekableStream.setPosition(pos);
               else
               {
               //$END
               stream.close();
               stream = (OutputStream)operateFile(file, OPERATION_OPEN_OUTPUT_STREAM, new Integer(pos));
               //$IF,OSVERSION,>=,500
               seekableStream = stream instanceof Seekable ? (Seekable)stream : null;
               //$END
               //$IF,OSVERSION,>=,500
               }
               //$END
               this.pos = pos;
            }
            
            stream.write(b, off, len);
            
            if (pos < dirtyStart)
               dirtyStart = pos;
            
            this.pos = pos += len;
            
            if (pos > dirtyEnd)
               dirtyEnd = pos;
            
            if (isCached)
               streamCache.put(id, this);
         }
      }
      
      public boolean flush(int start, int end) throws java.io.IOException
      {
         synchronized (streamCacheLock)
         {
            if (!isOpen || end <= dirtyStart || start >= dirtyEnd)
               return false;
            else
            {
               stream.flush();
               
               dirtyStart = Integer.MAX_VALUE;
               dirtyEnd = 0;
               
               if (isCached)
                  streamCache.put(id, this);
               
               return true;
            }
         }
      }
      
      public void close() throws java.io.IOException
      {
         synchronized (streamCacheLock)
         {
            if (isOpen)
            {
               stream.close();
               isOpen = false;
               
               dirtyStart = Integer.MAX_VALUE;
               dirtyEnd = 0;
               
               if (isCached)
                  streamCache.remove(id);
            }
         }
      }
   }
}
