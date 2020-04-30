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

package totalcross.io.sync;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;
import totalcross.io.PDBFile;
import totalcross.sys.Convert;

/** This class represents a Palm DataBase that is being synchronized. */

/**
 * Allows you to access a pdb file on the device from the desktop during the conduit synchronization.<br>
 * On PalmOS it may only be used to handle files on the device's internal memory.
 */
public final class RemotePDBFile {
  protected String name; // guich@572_4: made all fields protected so they are not removed by the obfuscator.
  protected int mode;
  protected boolean open;
  protected int recIndex = -1;
  protected int wRecordSize;
  protected int lastSearchedRec = -1;
  boolean dontFinalize;
  Object pdbHandle;

  private static boolean idle = true;
  private static ByteArrayStream rwbas = new ByteArrayStream(65500); // the buffer used on read and write operations - max possible size
  private static DataStream ds = new DataStream(rwbas);

  public static int RECORD_SIZE_AUTO = 0;

  /**
   * Opens a remote PDBFile with the given name. Important: only one PDBFile
   * can be open at a time. Trying to open a new PDBFile before explicitly
   * closing the former will throw a RuntimeException. Note that the
   * Settings.dataPath, if set, is used by this method as the path to the
   * PDBFile.
   *
   * @param name
   *           The PDBFile name on the form <code>name.crtr.type</code>
   * @param mode
   *           Can be PDBFile.READ_WRITE, PDBFile.CREATE (this one is non
   *           destructive: if the PDBFile don't exists, it will be created and
   *           it will remain open on READ_WRITE mode; if it exists, it will
   *           stay open on READ_WRITE mode). You can also or the mode with
   *           PDBFile.DB_ATTR_BACKUP or PDBFile.DB_ATTR_STREAM to set these
   *           attributes when creating a database (do NOT use the other
   *           attributes!)
   * @param recordSize
   *           Used only when writing records. If you plan to write records
   *           with fixed size, pass in the desired size. Otherwise, pass
   *           RemotePDBFile.RECORD_SIZE_AUTO to automatically expand or shrink
   *           the record.
   * @throws totalcross.io.IOException
   * @throws totalcross.io.FileNotFoundException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  public RemotePDBFile(String name, int mode, int recordSize)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.FileNotFoundException, totalcross.io.IOException {
    if (!idle) {
      throw new RuntimeException("Only one database can be open at a time!");
    }

    if (name == null) {
      throw new java.lang.NullPointerException("Argument 'name' cannot have a null value.");
    }

    String[] st = Convert.tokenizeString(name, '.');
    if (st == null || st.length != 3 || st[0].length() > 31 || st[1].length() != 4 || st[2].length() != 4) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'name' " + name);
    }

    this.name = name;
    this.mode = mode;
    this.wRecordSize = recordSize;
    create();
    idle = !open; // this is ok: open will be changed by the native method.
  }

  @ReplacedByNativeOnDeploy
  private void create()
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.FileNotFoundException, totalcross.io.IOException {
    pdbHandle = new PDBFile(name, mode);
    open = true;
  }

  /**
   * Opens a remote PDBFile in READ_WRITE mode.
   *
   * @throws totalcross.io.IOException
   * @throws totalcross.io.FileNotFoundException
   * @throws totalcross.io.IllegalArgumentIOException
   *
   *
   * @see #RemotePDBFile(String, int, int)
   */
  public RemotePDBFile(String name)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.FileNotFoundException, totalcross.io.IOException {
    this(name, PDBFile.READ_WRITE, 0);
  }

  /**
   * Opens a remote PDBFile in the given mode.
   *
   * @throws totalcross.io.IOException
   * @throws totalcross.io.FileNotFoundException
   * @throws totalcross.io.IllegalArgumentIOException
   *
   *
   * @see #RemotePDBFile(String, int, int)
   */
  public RemotePDBFile(String name, int mode)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.FileNotFoundException, totalcross.io.IOException {
    this(name, mode, 0);
  }

  /**
   * Completely deletes this database, closing it first.
   *
   * @throws totalcross.io.IOException
   */
  @ReplacedByNativeOnDeploy
  final public void delete() throws totalcross.io.IOException {
    ((PDBFile) pdbHandle).delete();
    idle = true;
  }

  /**
   * Returns the number of records inside this database
   *
   * @throws totalcross.io.IOException
   */
  @ReplacedByNativeOnDeploy
  final public int getRecordCount() throws totalcross.io.IOException {
    return ((PDBFile) pdbHandle).getRecordCount();
  }

  /**
   * Fetches the given index, passing to the rec a DataStream to read the
   * record information.
   *
   * @throws totalcross.io.IOException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  public boolean readRecord(int index, RemotePDBRecord rec)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {

    rec.rc = this; // assign fields to the custom record
    if ((rec.size = rwRecord(index, rwbas, true)) > 0) // fetch and fill our buffer
    {
      // fill in the record. rwbas is already reseted by native counterpart.
      rec.read(ds);
      return true;
    }
    return false;
  }

  /**
   * Write the record at the given index. <b>IMPORTANT</b>: if index is -1,
   * the record is appended, otherwise, the given record is OVERWRITTEN. It is
   * not possible to insert a record into a given position due to limitations
   * of the native API. Also, passing a record index greater than the number of
   * records may cause unexpected results.
   *
   * @throws totalcross.io.IOException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  public boolean writeRecord(int index, RemotePDBRecord rec)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    rwbas.reset(); // reset the buffer
    rec.write(ds); // write to it
    return rwRecord(index, rwbas, false) > 0; // send it to the pda
  }

  /**
   * return the PDBFile's record size
   *
   * @throws totalcross.io.IOException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  @ReplacedByNativeOnDeploy
  int rwRecord(int idx, ByteArrayStream bas, boolean read)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    recIndex = idx;
    PDBFile pdbFile = ((PDBFile) pdbHandle);
    if (read) {
      pdbFile.setRecordPos(idx);
      int size = pdbFile.getRecordSize();
      bas.setSize(size, false);
      pdbFile.readBytes(bas.getBuffer(), 0, size);
      return size;
    } else {
      int size = bas.getPos();
      if (idx < 0) {
        pdbFile.addRecord(size);
      } else {
        pdbFile.setRecordPos(idx);
        pdbFile.resizeRecord(size);
      }
      // if ((idx < 0 && >= 0) || (idx >= 0 && cat.setRecordPos(idx) && cat.resizeRecord(size)))
      {
        pdbFile.writeBytes(bas.getBuffer(), 0, size);
        // guich@570_14: reset the dirty flag
        pdbFile.setRecordAttributes(idx, (byte) (pdbFile.getRecordAttributes(idx) & ~PDBFile.REC_ATTR_DIRTY));
        return size;
      }
    }
  }

  /** Returns the current record position or -1 if there is no current record. */
  public int getRecordPos() {
    return recIndex;
  }

  /**
   * Deletes the given record index. The record is immediately removed from the
   * PDBFile and all subsequent records are moved up one position. IMPORTANT:
   * due to this behaviour, if you plan to delete all records from a database,
   * it is <b>much faster</b> if you delete them in REVERSE ORDER (from last
   * to first). If you plan to delete all records, delete the whole database
   * using <code>delete</code> and then create it again.
   *
   * @throws totalcross.io.IOException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  @ReplacedByNativeOnDeploy
  final public void deleteRecord(int index) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    ((PDBFile) pdbHandle).setRecordPos(index);
    ((PDBFile) pdbHandle).deleteRecord();
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
  public int skipBytes(int n) {
    return rwbas.skipBytes(n);
  }

  /**
   * Returns the next modified record index. Can be used with the readRecord to
   * only retrieve the changed records since the last synchronization.
   *
   * @throws totalcross.io.IOException
   */
  @ReplacedByNativeOnDeploy
  final public int getNextModifiedRecordIndex() throws totalcross.io.IOException {
    PDBFile pdbFile = ((PDBFile) pdbHandle);
    int n = pdbFile.getRecordCount();
    for (int i = pdbFile.getRecordPos(); i < n; i++) {
      if ((pdbFile.getRecordAttributes(i) & PDBFile.REC_ATTR_DIRTY) != 0) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Closes this PDBFile.
   *
   * @throws totalcross.io.IOException
   */
  @ReplacedByNativeOnDeploy
  final public void close() throws totalcross.io.IOException {
    idle = true; // guich@tc114_95: set it here before close, because if close throws an exception the user will never be able to sync again.
    ((PDBFile) pdbHandle).close();
  }

  /**
   * Lists the available PDBFiles on the device that has the given creator id
   * and type. If you don't pass a creator id and/or a type, this method will
   * return with NO RESULTS. Note that the Settings.dataPath, if set, is used
   * by this method as the path to the PDBFiles.
   *
   * @return A String array with the answer, or null if no db was found that
   *         matched the criteria.
   */
  @ReplacedByNativeOnDeploy
  public static String[] listPDBs(int crtr, int type) {
    // get the list
    String[] list = PDBFile.listPDBs(crtr, type);
    // now strip the first _ so that the user gets the expected name of a RemotePDBFile
    if (list != null) {
      for (int i = list.length - 1; i >= 0; i--) {
        if (list[i].charAt(0) == '_') {
          list[i] = list[i].substring(1);
        }
      }
    }
    return list;
  }

  @Override
  protected void finalize() {
    try {
      close();
    } catch (totalcross.io.IOException e) {
    }
  }
}
