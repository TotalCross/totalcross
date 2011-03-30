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

// $Id: PDBFile4B.java,v 1.52 2011-02-24 16:44:56 fabio Exp $

package totalcross.io;

import totalcross.Launcher4B;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.util.Hashtable;
import totalcross.util.IntVector;

public class PDBFile4B extends Stream
{
   private File file;
   int mode;
   private byte[] name;
   private String simpleName;
   private int type;
   private int creator;
   private int attributes;
   private int version;
   private long creationDate;
   private long modificationDate;
   private long lastBackupDate;
   private int modificationNumber;
   private int originalModificationNumber;
   private int appInfoOffset;
   private int sortInfoOffset;
   private int uniqueIDSeed;
   private IntVector recordOffsets;
   private IntVector recordAttributes;
   private IntVector recordUniqueIds;
   private int recordCount;
   private byte[] record = new byte[MAX_RECORD_SIZE];
   private int recordPos;
   private int recordOffset;
   private int recordOriginalSize;
   private int recordSize;
   private boolean recordIsDirty;
   private boolean recordIsNew;
   boolean dontFinalize;
   private byte[] buf = new byte[1024];

   private static final int MAX_RECORD_SIZE = 64 * 1024;
   private static final long MAC_SECS_DIF = ((66 * 365) + 17) * 24 * 60 * 60;

   private static final int INVALID = -1;
   public static final int READ_WRITE = 3;
   public static final int CREATE = 4;
   public static final int CREATE_EMPTY = 5;

   public static final int DB_ATTR_READ_ONLY = 0x0002;
   public static final int DB_ATTR_APPINFODIRTY = 0x0004;
   public static final int DB_ATTR_BACKUP = 0x0008;
   public static final int DB_ATTR_OK_TO_INSTALL_NEWER = 0x0010;
   public static final int DB_ATTR_RESET_AFTER_INSTALL = 0x0020;
   public static final int DB_ATTR_COPY_PREVENTION = 0x0040;
   public static final int DB_ATTR_STREAM = 0x0080;

   public static final byte REC_RELEASE = (byte) -1;
   public static final byte REC_ATTR_DELETE = (byte) 0x80;
   public static final byte REC_ATTR_DIRTY = (byte) 0x40;
   public static final byte REC_ATTR_SECRET = (byte) 0x10;

   public PDBFile4B(String name, int mode) throws IllegalArgumentIOException, FileNotFoundException, IOException
   {
      this(name, mode, null);
   }

   private PDBFile4B(String name, int mode, String path) throws IllegalArgumentIOException, FileNotFoundException, IOException
   {
      if ((mode < 3 || mode > 5) && mode != -1)
         throw new IllegalArgumentIOException("mode", Convert.toString(mode));

      int len;
      String[] st = Convert.tokenizeString(name, '.');
      String type, creator, s;

      if (st == null || (len = st.length) < 3 || (type = st[len - 1]).length() != 4 || (creator = st[len - 2]).length() != 4 || (s = name.substring(0, name.length() - 10)).length() > 31)
         throw new IllegalArgumentIOException("name", name);
      name = s;

      this.mode = mode;
      this.simpleName = name;
      this.name = new byte[32];
      System.arraycopy(name.getBytes(), 0, this.name, 0, name.length());

      this.type = Convert.chars2int(type);
      this.creator = Convert.chars2int(creator);

      file = path == null ? getFile(name + ".pdb", false) : new File(path, File.READ_WRITE);
      if (file == null)
      {
         if (mode == CREATE || mode == CREATE_EMPTY)
         {
            file = getFile(name + ".pdb", true);
            init();
            save();
         }
         else
            throw new FileNotFoundException(name + ".pdb");
      }
      else
      {
         if (mode == CREATE_EMPTY)
         {
            file.setSize(0);
            init();
            save();
         }
         else
         {
            load();
            if (mode == INVALID) //flsobral@tc111_20b: if INVALID we just wanted to load the header, so close the file.
               file.close();
         }
      }
   }

   public static String[] listPDBs()
   {
      return listPDBs(0, 0);
   }

   public final static String[] listPDBs(int creatorIdWild, int typeWild)
   {
      boolean includeVersion = creatorIdWild == 0xFFFFFFFF;
      if (includeVersion)
         creatorIdWild = 0;

      return listFiles(Convert.int2chars(creatorIdWild), Convert.int2chars(typeWild), includeVersion);
   }

   public String getName()
   {
      return simpleName;
   }

   public final int getAttributes() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");

      return attributes;
   }

   public final void setAttributes(int attr) throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");

      attributes = attr;
      modificationNumber++;
   }

   public final byte getRecordAttributes(int pos) throws IllegalArgumentIOException, IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (pos < 0 || pos >= recordCount)
         throw new IllegalArgumentIOException("pos", Convert.toString(pos));

      return (byte)(recordAttributes.items[pos]); // bruno@tc120_26: Now returns the attributes from the right record.
   }

   public final void setRecordAttributes(int pos, byte attr) throws IllegalArgumentIOException, IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (pos < 0 || pos >= recordCount)
         throw new IllegalArgumentIOException("pos", Convert.toString(pos));

      recordAttributes.items[pos] = attr;
      modificationNumber++;
   }

   public final int getRecordCount() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");

      return recordCount;
   }

   public int getRecordOffset() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (recordPos == -1)
         throw new IOException("No record selected for this operation");

      return recordOffset;
   }

   public void setRecordOffset(int ofs) throws IllegalArgumentIOException, IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (ofs < 0)
         throw new IllegalArgumentIOException("ofs", Convert.toString(ofs));
      if (recordPos == -1)
         throw new IOException("No record selected for this operation");

      if (ofs != recordOffset)
      {
         recordOffset = 0;
         skipBytes(ofs);
      }
   }

   public final int getRecordSize() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (recordPos == -1)
         throw new IOException("No record selected for this operation");

      return recordSize;
   }

   public final int getRecordPos() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");

      return recordPos;
   }

   public final void setRecordPos(int pos) throws IllegalArgumentIOException, IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (pos < -1 || pos >= recordCount)
         throw new IllegalArgumentIOException("pos", Convert.toString(pos));

      if (pos != recordPos)
      {
         if (recordPos >= 0 && (recordIsNew || recordIsDirty || recordOriginalSize != recordSize)) // is the loaded record dirty?
            save();

         recordPos = -1; // release current record

         if (pos >= 0)
         {
            int offset1 = recordOffsets.items[pos];
            int offset2 = (pos == recordCount - 1) ? file.getSize() : recordOffsets.items[pos + 1];
            file.setPos(offset1);
            recordSize = file.readBytes(record, 0, offset2 - offset1);

            if (recordSize >= 0)
            {
               recordPos = pos;
               recordOriginalSize = recordSize;
               recordIsNew = false;
               recordIsDirty = false;
            }
         }
      }
      recordOffset = 0; // guich@tc110_32 - always set offset to 0
   }

   public final void addRecord(int size) throws IllegalArgumentIOException, IOException
   {
      addRecord(size, recordCount);
   }

   public final void addRecord(int size, int pos) throws IllegalArgumentIOException, IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (size <= 0 || size >= MAX_RECORD_SIZE)
         throw new IllegalArgumentIOException("size", Convert.toString(size));
      if (pos < 0 || pos > recordCount)
         throw new IllegalArgumentIOException("pos", Convert.toString(pos));

      if (recordPos >= 0 && (recordIsNew || recordIsDirty || recordOriginalSize != recordSize)) // is the loaded record dirty?
         save();

      // Create new record entry
      int offset = pos == recordCount ? file.getSize() : recordOffsets.items[pos];

      recordOffsets.insertElementAt(offset, pos);
      recordAttributes.insertElementAt(0, pos);
      recordUniqueIds.insertElementAt(0, pos);
      recordCount++;

      // Set record parameters
      recordPos = pos;
      recordOffset = 0;
      recordSize = size;
      recordOriginalSize = size;
      recordIsNew = true;
      recordIsDirty = false;
      modificationNumber++;
   }

   public final void resizeRecord(int size) throws IllegalArgumentIOException, IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler.");
      if (recordPos == -1)
         throw new IOException("No record selected for this operation");
      if (size <= 0 || size >= MAX_RECORD_SIZE)
         throw new IllegalArgumentIOException("size", Convert.toString(size));

      // Update size (resize will occur when flushing this record)
      recordSize = size;

      if (recordIsNew)
         recordOriginalSize = size;

      if (recordOffset > size)
         recordOffset = size;

      modificationNumber++;
   }

   public final void deleteRecord() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (recordPos == -1)
         throw new IOException("No record selected for this operation");

      // Remove record entry
      recordOffsets.removeElementAt(recordPos);
      recordAttributes.removeElementAt(recordPos);
      recordUniqueIds.removeElementAt(recordPos);
      recordCount--;

      recordSize = -1; // Mark record as logically deleted
      modificationNumber++;

      save(); // Physically deletes the record
   }

   public void close() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");

      mode = INVALID;
      dontFinalize = true;

      if (originalModificationNumber != modificationNumber)
         modificationDate = (System.currentTimeMillis() / 1000) + MAC_SECS_DIF;

      save();
      file.close();
   }

   public final void delete() throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");

      mode = INVALID;
      dontFinalize = true;

      file.delete();
   }

   public final void rename(String newName) throws IllegalArgumentIOException, IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");

      int len;
      String[] st = Convert.tokenizeString(newName, '.');
      String type, creator, s;

      if (st == null || (len = st.length) < 3 || (type = st[len - 1]).length() != 4 || (creator = st[len - 2]).length() != 4 || (s = newName.substring(0, newName.length() - 10)).length() > 31)
         throw new IllegalArgumentIOException("Invalid value for argument 'newName': " + newName);
      newName = s;

      File f = getFile(newName + ".pdb", false);
      if (f != null)
      {
         f.close();
         throw new IOException("Pdb file " + newName + " already exists");
      }

      f = getFile(newName + ".pdb", true);
      int r;

      file.setPos(0);
      while ((r = file.readBytes(buf, 0, buf.length)) > 0)
         f.writeBytes(buf, 0, r);

      file.delete();
      file = f;

      this.simpleName = newName;
      name = new byte[32];
      System.arraycopy(newName.getBytes(), 0, name, 0, newName.length());

      this.type = Convert.chars2int(type);
      this.creator = Convert.chars2int(creator);

      save();
   }

   public final int readBytes(byte[] buf, int start, int count) throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (recordPos == -1)
         throw new IOException("No record selected for this operation");
      if (count == 0)
         return 0; // flsobral@tc113_43: return 0 if asked to read 0.

      int bytesLeft = recordSize - recordOffset;
      if (count > bytesLeft)
      {
         if (bytesLeft == 0) // guich@tc110_65: return -1 if eof
            return -1;
         count = bytesLeft;
      }

      System.arraycopy(record, recordOffset, buf, start, count);
      recordOffset += count;

      return count;
   }

   public final int writeBytes(byte[] b, int off, int len) throws IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (recordPos == -1)
         throw new IOException("No record selected for this operation");

      int max = recordSize - recordOffset;
      if (len > max)
         len = max;

      System.arraycopy(b, off, record, recordOffset, len);
      recordOffset += len;

      recordIsDirty = true;
      modificationNumber++;

      return len;
   }

   public int skipBytes(int count) throws IllegalArgumentIOException, IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (recordPos == -1)
         throw new IOException("No record selected for this operation");

      int off = recordOffset + count;

      if (off < 0)
         throw new totalcross.io.IllegalArgumentIOException("Offset cannot underflow the record size: " + off);
      if (off > recordSize)
         throw new totalcross.io.IllegalArgumentIOException("Offset cannot overflow the record size: " + off);

      recordOffset += count;
      return count;
   }

   public int skip(int n) throws totalcross.io.IOException // flsobral@tc110_105: Better implementation.
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (recordPos == -1)
         throw new IOException("No record selected for this operation");

      if (n <= 0)
         return 0;

      int off = recordOffset + n;
      if (off > recordSize)
         off = recordSize - recordOffset;
      recordOffset += off;
      return off;
   }

   public final int inspectRecord(byte[] b, int pos, int offsetInRec) throws IllegalArgumentIOException, IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (pos < 0 || pos > recordCount)
         throw new IllegalArgumentIOException("pos", Convert.toString(pos));
      if (offsetInRec < 0 || offsetInRec > 65535)
         throw new IllegalArgumentIOException("offsetInRec", Convert.toString(offsetInRec));

      if (pos == recordPos)
      {
         if (offsetInRec >= recordSize)
            return 0;

         System.arraycopy(record, offsetInRec, b, 0, recordSize);
         return recordSize;
      }
      else
      {
         int offset1 = recordOffsets.items[pos];
         int offset2 = pos == recordCount - 1 ? file.getSize() : recordOffsets.items[pos + 1];
         int recSize = offset2 - offset1;

         if (offsetInRec >= recSize)
            return 0;

         offset1 += offsetInRec;
         file.setPos(offset1);
         return file.readBytes(b, 0, offset2 - offset1);
      }
   }

   public final int searchBytes(byte[] toSearch, int length, int offsetInRec) throws IllegalArgumentIOException, IOException
   {
      if (mode == INVALID)
         throw new IOException("Invalid pdb file handler");
      if (offsetInRec < 0)
         throw new IllegalArgumentIOException("offsetInRec", Convert.toString(offsetInRec));

      byte[] b = new byte[length];
      for (int i = Math.max(recordPos,0), n = recordCount ; i < n ; i++) // guich@tc110_34: consider recordPos in the search
      {
         if (i == recordPos)
         {
            if ((recordSize - offsetInRec) < length)
               continue;

            System.arraycopy(record, offsetInRec, b, 0, length);
            if (arrayEquals(toSearch, b, length))
               return i;
         }
         else
         {
            try
            {
               int offset1 = recordOffsets.items[i];
               int offset2 = i == recordCount - 1 ? file.getSize() : recordOffsets.items[i + 1];

               if (((offset2 - offset1) - offsetInRec) < length)
                  continue;

               file.setPos(offset1 + offsetInRec);
               file.readBytes(b, 0, length);
               if (arrayEquals(toSearch, b, length))
                  return i;
            }
            catch (IOException ex) { }
         }
      }

      return -1;
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

   private boolean arrayEquals(byte[] b1, byte[] b2, int len)
   {
      for (int i = 0; i < len; i ++)
         if (b1[i] != b2[i])
            return false;

      return true;
   }

   private File getFile(String name, boolean create) throws IOException
   {
      // PDBFiles are searched in the following order:
      // 1: Settings.dataPath (if set)
      // 2: appPath
      // 3: vmPath

      File f = null;
      if (Settings.dataPath != null)
         f = getFileFromPath(Settings.dataPath, name, true);
      if (f == null)
         f = getFileFromPath(Launcher4B.appPath, name, true);
      if (f == null)
         f = getFileFromPath(Launcher4B.vmPath, name, true);
      if (f == null && create) // not found anywhere, so create it
      {
         String basePath = Settings.dataPath;
         if (basePath == null)
            basePath = Launcher4B.appPath;

         f = new File(Convert.appendPath(basePath, name), File.CREATE);
      }

      return f;
   }

   private File getFileFromPath(String basePath, String file, boolean recursive) throws IOException
   {
      File f = null;

      try
      {
         f = new File(Convert.appendPath(basePath, file), File.READ_WRITE);
      }
      catch (FileNotFoundException ex) // not found
      {
         if (recursive)
         {
            String[] dirs = new File(basePath).listFiles();
            String subPath;

            for (int i = dirs.length - 1; i >= 0; i--)
            {
               subPath = Convert.appendPath(basePath, dirs[i]);
               if ((new File(subPath)).isDir())
               {
                  f = getFileFromPath(subPath, file, true);
                  if (f != null)
                     break;
               }
            }
         }
      }

      return f;
   }

   private static String[] listFiles(String creator, String type, boolean includeVersion)
   {
      Hashtable ht = new Hashtable(5);

      if (Settings.dataPath != null)
         listFilesFromPath(Settings.dataPath, creator, type, includeVersion, ht, false);
      listFilesFromPath(Launcher4B.appPath, creator, type, includeVersion, ht, true);
      listFilesFromPath(Launcher4B.vmPath, creator, type, includeVersion, ht, true);

      String[] list = new String[ht.size()];
      ht.getValues().copyInto(list);
      return list;
   }

   private static void listFilesFromPath(String basePath, String creator, String type, boolean includeVersion, Hashtable ht, boolean recursive)
   {
      try
      {
         String[] files = new File(basePath).listFiles();
         for (int i = files.length - 1; i >= 0; i--)
         {
            String name = files[i];
            String path = Convert.appendPath(basePath, name);

            try
            {
               if ((new File(path)).isDir())
               {
                  if (recursive)
                     listFilesFromPath(path, creator, type, includeVersion, ht, true);
               }
               else if (name.toLowerCase().endsWith(".pdb"))
               {
                  name = name.substring(0, name.length() - 4);
                  int idx;
                  if ((idx = name.lastIndexOf('/')) >= 0)
                     name = name.substring(idx + 1);

                  if (ht.get(name) == null)
                  {
                     PDBFile4B pdbFile = new PDBFile4B(name + "." + creator + "." + type, INVALID, path); // open INVALID to avoid loading record table

                     String fullName = pdbFile.simpleName;
                     fullName += "." + Convert.int2chars(pdbFile.creator);
                     fullName += "." + Convert.int2chars(pdbFile.type);
                     if (includeVersion)
                        fullName += "#" + pdbFile.version;

                     ht.put(name, fullName);
                  }
               }
            }
            catch (IOException ex)
            {
            }
         }
      }
      catch (IOException ex)
      {
      }
   }

   private void init()
   {
      long t = (System.currentTimeMillis() / 1000) + MAC_SECS_DIF;
      creationDate = t;
      modificationDate = t;
      lastBackupDate = t;
      recordOffsets = new IntVector();
      recordAttributes = new IntVector();
      recordUniqueIds = new IntVector();
      recordPos = -1;
   }

   private void load() throws IOException
   {
      DataStream ds = new DataStream(file);
      int r;

      file.setPos(72);
      if (ds.readInt() != 0) // check nextRecordList == 0
         throw new IOException("Invalid database file; TotalCross does not support multiple record or resource lists in a database.");

      file.setPos(60);
      r = ds.readInt();
      if (type != 0 && type != r)
         throw new IOException("Invalid database file; unexpected type '" + Convert.int2chars(r) + "'");
      type = r;

      r = ds.readInt();
      if (creator != 0 && creator != r)
         throw new IOException("Invalid database file; unexpected creator '" + Convert.int2chars(r) + "'");
      creator = r;

      // Start reading the header (78 bytes)
      file.setPos(0);
      ds.readBytes(name);
      attributes = ds.readShort();
      version = ds.readShort();
      creationDate = ds.readInt() & 0xFFFFFFFFL;
      modificationDate = ds.readInt() & 0xFFFFFFFFL;
      lastBackupDate = ds.readInt() & 0xFFFFFFFFL;
      modificationNumber = ds.readInt();
      originalModificationNumber = modificationNumber;
      appInfoOffset = ds.readInt();
      sortInfoOffset = ds.readInt();
      ds.skipBytes(8); // type and creator was already read
      uniqueIDSeed = ds.readInt();

      ds.readInt(); // nextRecordList
      recordCount = ds.readUnsignedShort();
      recordPos = -1;

      if (mode != INVALID) // load records?
      {
         // Read record offsets and attributes (numRecords * 8 bytes)
         recordOffsets = new IntVector(recordCount);
         recordAttributes = new IntVector(recordCount);
         recordUniqueIds = new IntVector(recordCount);

         for (int i = 0; i < recordCount; i++)
         {
            recordOffsets.addElement(ds.readInt());
            recordAttributes.addElement(ds.readByte() & 0xFF);

            ds.readBytes(buf, 0, 3);
            recordUniqueIds.addElement(((buf[0] & 0xFF) << 16) | ((buf[1] & 0xFF) << 8) | (buf[2] & 0xFF));
         }

         if (recordCount > 0)
            setRecordPos(0); // load the first record
      }
   }

   private void save() throws IOException
   {
      if (recordPos >= 0)
      {
         if (recordSize < 0) // if the record has been deleted
         {
            if (!recordIsNew) // only update offsets and file if this record was already persisted
            {
               if (appInfoOffset > 0)
                  appInfoOffset -= 8;

               if (sortInfoOffset > 0)
                  sortInfoOffset -= 8;

               if (recordCount > 0)
               {
                  for (int i = recordCount - 1; i >= 0; i--)
                     recordOffsets.items[i] -= (i >= recordPos) ? (8 + recordOriginalSize) : 8;
   
                  shiftFile(recordOffsets.items[recordPos], -recordOriginalSize); // remove the record data
                  shiftFile(78, -8); // remove space for the record entry
               }
               else
                  file.setSize(file.getSize() - (recordOriginalSize + 8)); // flsobral@tc120_28: fixed removal of the last record of the file.

               recordPos = -1;
            }
         }

         else
            if (recordIsNew) // new record, not deleted;
            {
               if (appInfoOffset > 0)
                  appInfoOffset += 8;

               if (sortInfoOffset > 0)
                  sortInfoOffset += 8;

               for (int i = recordCount - 1; i >= 0; i--)
                  recordOffsets.items[i] += (i > recordPos) ? (8 + recordSize) : 8;

               shiftFile(78, 8); // add space to the record entry
               shiftFile(recordOffsets.items[recordPos], recordSize); // add space to the record data
            }

            else
               if (recordSize != recordOriginalSize) // record has been resized
               {
                  int inc = recordSize - recordOriginalSize;

                  for (int i = recordCount - 1; i > recordPos; i--)
                     recordOffsets.items[i] += inc;

                  shiftFile(recordOffsets.items[recordPos] + recordOriginalSize, inc); // resize the record data
               }
      }

      // Re-write database header
      file.setPos(0);
      DataStream ds = new DataStream(file);

      ds.writeBytes(name);
      ds.writeShort(attributes);
      ds.writeShort(version);
      ds.writeInt((int)creationDate);
      ds.writeInt((int)modificationDate);
      ds.writeInt((int)lastBackupDate);
      ds.writeInt(modificationNumber);
      ds.writeInt(appInfoOffset);
      ds.writeInt(sortInfoOffset);
      ds.writeInt(type);
      ds.writeInt(creator);
      ds.writeInt(uniqueIDSeed);

      ds.writeInt(0); // nextRecordList
      ds.writeShort(recordCount);

      // Re-write record entries
      for (int i = 0; i < recordCount; i++)
      {
         ds.writeInt(recordOffsets.items[i]);
         ds.writeByte(recordAttributes.items[i]);

         int uniqueId = recordUniqueIds.items[i];
         buf[2] = (byte)(uniqueId & 0xFF);
         uniqueId >>= 8;
         buf[1] = (byte)(uniqueId & 0xFF);
         uniqueId >>= 8;
         buf[0] = (byte)(uniqueId & 0xFF);
         ds.writeBytes(buf, 0, 3);
      }

      ds.pad(2);

      if (recordPos >= 0 && recordIsDirty) // Flush record
      {
         file.setPos(recordOffsets.items[recordPos]);
         file.writeBytes(record, 0, recordSize);
      }
      
      // bruno@tc120_25: Do not forget to update/clear all record flags
      recordOriginalSize = recordSize;
      recordIsNew = false;
      recordIsDirty = false;
   }

   private void shiftFile(int start, int len) throws IOException
   {
      int max = buf.length;
      int end = file.getSize();

      if (len > 0) // insert bytes at current position?
      {
         // Expand file
         file.setSize(file.getSize() + len);

         if (end > start)
         {
            while ((end - max) > start)
            {
               end -= max;
               file.setPos(end);
               file.readBytes(buf, 0, max);

               file.setPos(end + len);
               file.writeBytes(buf, 0, max);
            }

            max = end - start;

            file.setPos(start);
            file.readBytes(buf, 0, max);

            file.setPos(start + len);
            file.writeBytes(buf, 0, max);
         }
      }
      else // delete bytes at current position
      {
         while ((start + max) < end)
         {
            file.setPos(start);
            file.readBytes(buf, 0, max);

            file.setPos(start + len);
            file.writeBytes(buf, 0, max);

            start += max;
         }

         max = end - start;

         file.setPos(start);
         file.readBytes(buf, 0, max);

         file.setPos(start + len);
         file.writeBytes(buf, 0, max);

         // Truncate file
         file.setSize(file.getSize() + len);
      }
   }
}
