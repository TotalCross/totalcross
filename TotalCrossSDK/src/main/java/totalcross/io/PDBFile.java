// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
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

package totalcross.io;

import java.io.OutputStream;
import totalcross.Launcher;
import totalcross.sys.Convert;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

/**
 * PDBFile is a collection of records commonly referred to as a database on small devices.
 * <p>
 * Here is an example showing data being read from records in a PDBFile:
 *
 * <pre>
 * try
 * {
 *    PDBFile pdbFile = new PDBFile("MyPDBFile.CRTR.TYPE", PDBFile.READ_WRITE);
 *    DataStream ds = new DataStream(pdbFile);
 *    int count = pdbFile.getRecordCount();
 *    for (int i = 0; i < count; i++)
 *    {
 *       pdbFile.setRecordPos(i);
 *       String name = ds.readString();
 *       double salary = ds.readDouble();
 *       // ...
 *    }
 *    pdbFile.close();
 *    pdbFile = null; // just make sure we won't use it again.
 * }
 * // since the exceptions are being treated the same way, we could just catch the IOException instead,
 * // but for the purpose of this example, we'll treat them individually.
 * catch (IllegalArgumentIOException e)
 * {
 *    MessageBox.showException(e, false);
 * }
 * catch (FileNotFoundException e)
 * {
 *    MessageBox.showException(e, false);
 * }
 * catch (IOException e)
 * {
 *    MessageBox.showException(e, false);
 * }
 * </pre>
 *
 * <p>
 * Note: a PDBFile cannot be opened twice. You must close one instance before opening the other or share the same
 * instance.
 * <p>
 * In Windows, you can specify the path of the pdb file, either absolute ("\\My Documents\\Test") or relative
 * ("..\\..\\Test"). Note that the Settings.dataPath, if set, is also used as the path to the PDBFile.
 */

public class PDBFile extends Stream {
  /**
   * On desktop, is the Vector that represents this PDBFile. <br>
   * On device, stores the DmOpenRef.
   */
  private Object openRef;

  int _hvRecordPos;
  int _hvRecordOffset;
  int _hvRecordLength;
  int mode;

  private String name;
  private String fileName;

  private static final int INVALID = -1;

  /** Read-write open mode.
   * @see #PDBFile(String, int)
   */
  public static final int READ_WRITE = 3; // READ and WRITE
  /** Non-destructive create open mode. If the database doesn't exist, it is created and stays in READ_WRITE mode. Otherwise, if it does exists, it is <b>not</b> erased; it just stays in READ_WRITE mode.
   * @see #PDBFile(String, int)
   */
  public static final int CREATE = 4;
  /** Destructive-create open mode. If the file exists, it is deleted, then a new file is created, and the mode is changed to READ_WRITE.
   * @see #PDBFile(String, int)
   */
  public static final int CREATE_EMPTY = 5;

  /** Read Only database
   * @see #setAttributes(int)
   */
  public static final int DB_ATTR_READ_ONLY = 0x0002;
  /** Set if Application Info block is dirty. Optionally supported by an App's conduit
   * @see #setAttributes(int)
   */
  public static final int DB_ATTR_APPINFODIRTY = 0x0004;
  /** Set if database should be backed up to PC if no app-specific synchronization conduit has been supplied.
   * @see #setAttributes(int)
   */
  public static final int DB_ATTR_BACKUP = 0x0008;
  /** This tells the backup conduit that it's OK for it to install a newer version of this database with a different name if the current database is open. This mechanism is used to update the Graffiti Shortcuts database, for example.
   * @see #setAttributes(int)
   */
  public static final int DB_ATTR_OK_TO_INSTALL_NEWER = 0x0010;
  /** Device requires a reset after this database is installed.
   * @see #setAttributes(int)
   */
  public static final int DB_ATTR_RESET_AFTER_INSTALL = 0x0020;
  /** This database should not be copied to
   * @see #setAttributes(int)
   */
  public static final int DB_ATTR_COPY_PREVENTION = 0x0040;
  /** This database is used for file stream implementation. This attribute may not be used.
   * @see #setAttributes(int)
   */
  public static final int DB_ATTR_STREAM = 0x0080;

  /** Use this in order to explicitly release a record through the setRecordAttributes method.
   * This is a hack for a bug in the NVFS of Palm OS 5.4.9, which does not
   * release locked records during the boot (reset). You can use the following code in order to clear the locked records:
   * <pre>
   * int n = cat.getRecordCount();
   * for (int i =0; i < n; i++)
   * {
   *    cat.setRecordPos(i))
   *    cat.setRecordAttributes(i,REC_RELEASE);
   * }
   * </pre>
   * Note that this code assume that the PDBFile was NOT being used previously and that there are no records being used.
   * You must also close the PDBFile after and open it again.
   * Ignoring these restrictions will likely reset the device.
   * @since SuperWaba 5.66
   * @see #setRecordAttributes(int, byte)
   */
  public static final byte REC_RELEASE = (byte) -1;
  /** Record atribute: Deleted. Note that after a record has it delete attribute set you don't have access to it anymore.
   * @see #setRecordAttributes(int, byte)
   */
  public static final byte REC_ATTR_DELETE = (byte) 0x80;
  /** Record atribute: Dirty (has been modified since last sync)
   * @see #setRecordAttributes(int, byte)
   */
  public static final byte REC_ATTR_DIRTY = (byte) 0x40;
  /** Record atribute: Private
   * @see #setRecordAttributes(int, byte)
   */
  public static final byte REC_ATTR_SECRET = (byte) 0x10;

  /**
   * Opens a PDBFile with the given name and mode.
   * <p>
   * A PalmOS creator id and type must be specified by appending
   * a 4 character creator id and 4 character type to the name separated
   * by periods. For example:
   * <pre>
   * PDBFile c = new PDBFile("MyPDBFile.CRTR.TYPE", PDBFile.CREATE);
   * </pre>
   * Will create a PalmOS database with the name "MyPDBFile", creator id
   * of "CRTR" and type of "TYPE".
   * <p>
   * The name of the PDBFile must be 31 characters or less,
   * not including the creator id and type.
   * Every time the database is modified the backup attribute is set.
   * <p>
   * Note that some file types are blocked from opening on Palm OS. These types are
   * appl, psys and rsrc ones, and also the resource databases. Hacking the system to
   * allow it to access these files may simply reboot the device.
   * @param name PDBFile name, allowing only letters, digits and '_'. Must contain the creator id and type.
   * @param mode one of READ_WRITE, CREATE, CREATE_EMPTY.
   * @see #READ_WRITE
   * @see #CREATE
   * @see #CREATE_EMPTY
   */
  public PDBFile(String name, int mode)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.FileNotFoundException, totalcross.io.IOException {
    if (name == null) {
      throw new java.lang.NullPointerException("Argument 'name' cannot have a null value.");
    }
    if (mode < 3 || mode > 5) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'mode': " + mode);
    }

    String[] st = Convert.tokenizeString(name, '.');
    if (st == null || st.length != 3 || st[0].length() > 31 || st[1].length() != 4 || st[2].length() != 4) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'name': " + name
          + ". The name must be in format Name.CRTR.TYPE and 'Name' must be <= 31 characters in length.");
    }

    this.name = name;
    this.mode = mode;
    create(st[0], st[1], st[2], mode);
  }

  final private void create(String name, String creator, String type, int mode)
      throws totalcross.io.FileNotFoundException, totalcross.io.IOException {
    _name = name;
    _creator = creator;
    _type = type;
    fileName = name + ".pdb";

    // read the PDBFile from disk
    byte[] fileBytes = Launcher.instance.readBytes(fileName);
    doSetDate();

    openRef = new Vector(50);
    if (fileBytes == null) // PDBFile not found?
    {
      if (mode != PDBFile.CREATE && mode != PDBFile.CREATE_EMPTY) // if not creating, the file was not found.
      {
        openRef = null; // don't let the finalizer close an empty db!
        throw new totalcross.io.FileNotFoundException("Could not find the pdb file: " + this.name);
      }
    } else {
      // if PDBFile found and mode = create, continue.
      if (mode == PDBFile.CREATE_EMPTY) {
        removePDBFileFromDisk();
        deleted = false;
      } else {
        openRef = fromPDB(fileBytes, _creator, _type, _attrs);
      }
    }
    _dbHash.put(this.name, this);
  }

  /**
   * Returns the name passed on the constructor.
   *
   * @since SuperWaba 5.5
   */
  public String getName() {
    return this.name;
  }

  /**
   * Renames the currently open PDBFile to the given name. The name
   * must be in the form "MyNewPDBFileName.CRTR.TYPE". <b>Note</b>: in desktop
   * only, the old file <b>may</b> remain if its deletion is not possible (in
   * other words, in desktop the rename operation may work like a "copy oldName
   * newName").
   * <p>
   * Here is an example of use:
   *
   * <pre>
   * PDBFile c = new PDBFile(&quot;guich.Crtr.Type&quot;, PDBFile.READ_WRITE);
   * c.rename(&quot;flor.CrTr.TyPe&quot;);
   * </pre>
   *
   * @throws totalcross.io.IOException
   */
  final public void rename(String newName) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (newName == null) {
      throw new java.lang.NullPointerException("Argument 'newName' cannot have a null value.");
    }

    String[] st = Convert.tokenizeString(newName, '.');
    if (st == null || st.length != 3 || st[0].length() > 31 || st[1].length() != 4 || st[2].length() != 4) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'newName': " + newName);
    }

    // update the htOpenedAt with the new name, otherwise the file will be saved in a different location
    String path = removePDBFileFromDisk(); // guich@400_2: putted the delete routine in a method.

    _name = st[0];
    _creator = st[1];
    _type = st[2];

    name = newName; // guich@552_21: assign the new internal name
    fileName = _name + ".pdb";

    deleted = false; // guich@400_58: was set to true in removePDBFileFromDisk
    modificationNumber++; // guich@400_58: needed for PDBFile.close.
    _dbHash.put(this.name, this); // guich@550_11
    if (path != null && path.length() > 0) {
      Launcher.instance.htOpenedAt.put(fileName, path);
    }
  }

  /**
   * Adds a record to the end of the PDBFile.
   *
   * @param size The size in bytes of the record to add
   * @throws totalcross.io.IOException If the operation could not be completed.
   */
  final public void addRecord(int size) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (size < 0 || size > 65535) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'size': " + size);
    }

    Vector records = (Vector) openRef;
    _hvRecordPos = records.size();
    records.addElement(new byte[size]);
    _attrs.addElement(new Byte(PDBFile.REC_ATTR_DIRTY));
    _hvRecordOffset = 0;
    _hvRecordLength = size;
    modificationNumber++;
  }

  /**
   * Insert a record to the given position of the PDBFile.
   * <p><i>Historic notes: this was the first change implemented by Guich in Waba in 06/30/2000.</i>
   *
   * @param size The size in bytes of the record to add
   * @param pos The position where to add the record. You can specify getRecordCount() to add it to the end.
   * Cannot be lower than 0.
   * @throws totalcross.io.IOException If the operation could not be completed.
   * @throws totalcross.io.IllegalArgumentIOException If size or pos have invalid values.
   */
  final public void addRecord(int size, int pos)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (size < 0 || size > 65535) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'size': " + size);
    }
    Vector records = (Vector) openRef;
    if (pos < 0 || pos > records.size()) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'pos': " + pos);
    }

    records.insertElementAt(new byte[size], pos);
    _attrs.insertElementAt(new Byte(PDBFile.REC_ATTR_DIRTY), pos);
    modificationNumber++;
    _hvRecordPos = pos;
    _hvRecordOffset = 0;
    _hvRecordLength = size;
  }

  /**
   * Resizes a record. This method changes the size (in bytes) of the current record,
   * which is specified using setRecordPos.
   * The contents of the existing record are preserved if the new size is larger
   * than the existing size. If the new size is less than the existing size, the
   * contents of the record are also preserved but truncated to the new size.
   * @param size the new size of the record
   * @throws totalcross.io.IOException When the method fails.
   */
  final public void resizeRecord(int size) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (size < 0 || size > 65535) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'size': " + size);
    }
    if (_hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }

    Vector records = (Vector) openRef;
    byte oldRec[] = (byte[]) records.items[_hvRecordPos];
    byte newRec[] = new byte[size];
    int copyLen;
    if (oldRec.length < newRec.length) {
      copyLen = oldRec.length;
    } else {
      copyLen = newRec.length;
    }
    System.arraycopy(oldRec, 0, newRec, 0, copyLen);
    records.items[_hvRecordPos] = newRec;
    _hvRecordLength = size;
    modificationNumber++;
  }

  /**
   * Closes the PDBFile. If you don't close the PDBFile, it will be closed when it gets
   * garbage collected. Note that in desktop the PDBFile is written to disk
   * only after it is closed; during the program's execution, it is held in main memory.
   * @throws IOException If the PDBFile was already closed.
   */
  @Override
  public void close() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }

    try {
      nativeClose();
    } finally {
      openRef = null;
      mode = INVALID;
    }
  }

  final private void nativeClose() throws totalcross.io.IOException {
    if (!deleted
        && (modificationNumber != originalModificationNumber || (modificationNumber == 0 && getRecordCount() == 0))) {
      writeCB();
    }
  }

  private void writeCB() throws totalcross.io.IOException // guich@552_19
  {
    OutputStream os = Launcher.instance.openOutputStream(fileName); // guich@552_19: write directly to the stream
    if (os != null) {
      toPDB(os, (Vector) openRef, _creator, _name, _type, _attrs);
    } else {
      throw new totalcross.io.IOException("Could not write " + name);
    }
  }

  /**
   * Delete and close the PDBFile.
   *
   * @throws totalcross.io.IOException When the method fails.
   */
  final public void delete() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }

    _dbHash.remove(this.name);
    ((Vector) openRef).removeAllElements(); // erases the vector
    _attrs.removeAllElements();
    _hvRecordPos = -1;
    openRef = null;
    mode = INVALID;
    // guich@300_19: try to delete the file from disk first
    if (removePDBFileFromDisk() == null) {
      writeCB(); // could not phisicaly delete the file? write a zero-sized pdb
    }
  }

  static private int makeInt(byte b1, byte b2, byte b3, byte b4) {
    return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
  }

  /**
   * Returns the complete list of existing PDBFiles. <b>If no PDBFiles exist, this
   * method returns null.</b>
   * In the desktop, it searches in the already opened files and also the pdb
   * files in the current "." folder or at Settings.dataPath folder, if it was set.
   * @return A String array with the names
   */
  public static String[] listPDBs() {
    return listPDBs(0, 0);
  }

  /**
   * Returns the list of existing PDBFiles with the given creator id and/or type.
   * If no PDBFiles exist, this method returns null.
   * In the desktop, it searches in the already opened files and also the pdb
   * files in the current "." directory.
   * To convert the four characters of a creator id or type into an int,
   * use, you can use the function Convert.chars2int.
   * @param creatorIdWild The creator id wild card, or 0 to recover all types
   * @param typeWild The type wild card, or 0 to recover all types
   * @since SuperWaba 3.3
   * @see totalcross.sys.Convert#chars2int(String)
   * @return A String array with the names or null if no matches were found.
   */
  final public static String[] listPDBs(int creatorIdWild, int typeWild) // guich@330_17
  {
    boolean includeVersion = creatorIdWild == 0xFFFFFFFF; // guich@570_27
    if (includeVersion) {
      creatorIdWild = 0;
    }
    // guich@220_8: now returns also the prc/pdb files of the current dir
    // gets the already open files
    Object[] keys = _dbHash.getKeys().toObjectArray();
    Vector v = (keys != null ? new Vector(keys) : new Vector()); //flsobral@tc111_20a: Avoid NPE if keys is null.
    // guich@330_17: check the creators of the current items
    if (creatorIdWild != 0 || typeWild != 0) {
      for (int i = v.size(); --i >= 0;) {
        byte[] b = v.items[i].toString().getBytes();
        int l = b.length;
        int crtrI = makeInt(b[l - 9], b[l - 8], b[l - 7], b[l - 6]);
        int typeI = makeInt(b[l - 4], b[l - 3], b[l - 2], b[l - 1]);
        if ((creatorIdWild != 0 && creatorIdWild != crtrI) || (typeWild != 0 && typeWild != typeI)) {
          v.removeElementAt(i);
        } else if (includeVersion) {
          PDBFile pb = ((PDBFile) _dbHash.get(v.items[i]));
          if (pb != null) {
            v.items[i] = v.items[i] + "#" + pb.version;
          }
        }
      }
    }

    // gets from the current dir
    String path = Launcher.instance.getDataPath() != null ? Launcher.instance.getDataPath() : "."; // guich@570_21: use datapath if assigned
    java.io.File f = new java.io.File(path);
    String[] s = f.list();
    if (s != null) {
      byte[] buf = new byte[68];
      for (int i = 0; i < s.length; i++) {
        if (v.indexOf(s[i]) == -1 && (/*s[i].toLowerCase().endsWith("prc") ||*/ s[i].toLowerCase().endsWith("pdb"))) {
          try {
            // get the type and creator for this file
            java.io.FileInputStream fis = new java.io.FileInputStream(new java.io.File(path, s[i]));
            int readLength = fis.read(buf);
            if (buf.length == readLength) {
              int typeI = makeInt(buf[60], buf[61], buf[62], buf[63]); // guich@330_17
              int crtrI = makeInt(buf[64], buf[65], buf[66], buf[67]);
              if ((creatorIdWild != 0 && creatorIdWild != crtrI) || (typeWild != 0 && typeWild != typeI)) {
                fis.close();
                continue;
              }
              String type = "" + (char) buf[60] + (char) buf[61] + (char) buf[62] + (char) buf[63];
              String crtr = "" + (char) buf[64] + (char) buf[65] + (char) buf[66] + (char) buf[67];
              String version = "" + ((buf[34] << 8) | buf[35]);
              String fullName = s[i].substring(0, s[i].indexOf('.')) + '.' + crtr + '.' + type;
              if (includeVersion) {
                fullName += "#" + version;
              }
              if (v.indexOf(fullName) == -1) {
                v.addElement(fullName);
              }
            }
            fis.close();
          } catch (java.io.IOException e) {
          }
        }
      }
    }
    return (String[]) v.toObjectArray(); // guich@200b4_27: fix array cast exception - guich@220_32: now we can use the cast.
  }

  /**
   * Deletes the current record and sets the current record position to -1. The
   * record is immediately removed from the PDBFile and all subsequent records
   * are moved up one position.
   *
   * @throws totalcross.io.IOException When the method fails.
   */
  final public void deleteRecord() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (_hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }

    ((Vector) openRef).removeElementAt(_hvRecordPos);
    _attrs.removeElementAt(_hvRecordPos);
    _hvRecordPos = -1;
    modificationNumber++;
  }

  /**
   * Returns the number of records in the PDBFile.
   *
   * @throws totalcross.io.IOException If the PDBFile is not open.
   */
  final public int getRecordCount() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }

    return ((Vector) openRef).size();
  }

  /**
   * Returns the size of the current record in bytes.
   *
   * @throws totalcross.io.IOException When the method fails.
   */
  final public int getRecordSize() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (_hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }

    return _hvRecordLength;
  }

  /**
   * Sets the current record position and locks the given record. The value -1 can be passed to unset and unlock the
   * current record. If the operation is succesful, the read/write cursor is set to the beggining of the given record.
   *
   * @param pos Record to be locked for use. Must be between 0 and the current number of records, or -1.
   * @throws totalcross.io.IllegalArgumentIOException If the argument pos has an invalid value.
   * @throws totalcross.io.IOException If the method fails
   */
  final public void setRecordPos(int pos) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }

    Vector records = (Vector) openRef;
    if (pos == -1) {
      _hvRecordPos = -1;
    } else if (pos < -1 || pos >= records.size()) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'pos': " + pos);
    } else {
      _hvRecordPos = pos;
      _hvRecordLength = ((byte[]) records.items[_hvRecordPos]).length;
      _hvRecordOffset = 0;
    }
  }

  private int readWriteBytes(byte buf[], int start, int count, boolean isRead) throws totalcross.io.IOException {
    byte rec[] = null;
    Vector records = (Vector) openRef;
    try {
      rec = (byte[]) records.items[_hvRecordPos];
      if (isRead) {
        System.arraycopy(rec, _hvRecordOffset, buf, start, count);
      } else {
        System.arraycopy(buf, start, rec, _hvRecordOffset, count);
        if (_attrs != null) {
          setRecordAttributes(_hvRecordPos, (byte) (getRecordAttributes(_hvRecordPos) | PDBFile.REC_ATTR_DIRTY)); // set dirty
        }
        modificationNumber++;
      }
      _hvRecordOffset += count;
    } catch (Exception ee) {
      System.out
          .println("Exception " + ee + " in _readWriteBytes: rec.length: " + (rec == null ? "null" : "" + rec.length)
              + ", buf.length: " + buf.length + ", start: " + start + ", count: " + count + ", isRead? " + isRead);
      count = -1;
    }
    return count;
  }

  /**
   * Reads bytes from the current record into a byte array. Returns the
   * number of bytes actually read or -1 if an error prevented the
   * read operation from occurring. After the read is complete, the location of
   * the cursor in the current record (where read and write operations start from)
   * is advanced by the number of bytes read.
   * @param buf the byte array to read data into
   * @param start the start position in the <code>buf</code> array
   * @param count the number of bytes to read
   * @return The number of bytes read
   * @throws IOException If you request more bytes than available or if the PDBFile is closed.
   */
  @Override
  final public int readBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (_hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }
    if (buf == null) {
      throw new java.lang.NullPointerException("Argument 'buf' cannot have a null value.");
    }
    if (start < 0 || count < 0 || start + count > buf.length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    if (count == 0) {
      return 0; // flsobral@tc113_43: return 0 if asked to read 0.
    }
    int bytesLeft = _hvRecordLength - _hvRecordOffset;
    if (count > bytesLeft) {
      if (bytesLeft == 0) {
        return -1;
      }
      count = bytesLeft;
    }

    return readWriteBytes(buf, start, count, true);
  }

  /**
   * Changes the internal read/write cursor of the current record in a number of bytes. It can
   * be negative but cannot underflow neither overflow the record's size.
   *
   * @param count the number of bytes to skip.
   */
  @Override
  public int skipBytes(int count) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (_hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }

    int off = _hvRecordOffset + count;
    if (off < 0) {
      throw new totalcross.io.IllegalArgumentIOException("Offset cannot underflow the record size: " + off);
    }
    if (off > _hvRecordLength) {
      throw new totalcross.io.IllegalArgumentIOException("Offset cannot overflow the record size: " + off);
    }
    _hvRecordOffset += count;
    return count;
  }

  /**
   * Set the cursor on the given position. This is equivalent to
   * <code>skipBytes(-getRecordOffset()+ofs)</code>. If the offset is
   * already this one, nothing is changed.
   *
   * @param ofs The new read/write cursor offset value.
   * @since SuperWaba 5.5
   */
  public void setRecordOffset(int ofs) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (ofs < 0) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'ofs': " + ofs);
    }
    if (_hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }

    if (ofs != _hvRecordOffset) // if same, just exits
    {
      _hvRecordOffset = 0; // skipBytes requires relative bytes
      skipBytes(ofs);
    }
  }

  /**
   * Writes to the current record. After the write is
   * complete, the location of the cursor in the current record (where read and
   * write operations start from) is advanced by the number of bytes written.
   *
   * @param buf the byte array to write data from
   * @param start the start position in the byte array
   * @param count the number of bytes to write
   * @return The number of bytes written.
   */
  @Override
  final public int writeBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (_hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }
    if (buf == null) {
      throw new java.lang.NullPointerException("Argument 'buf' cannot have a null value.");
    }
    if (start < 0 || count < 0 || start + count > buf.length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    int bytesLeft = _hvRecordLength - _hvRecordOffset;
    if (count > bytesLeft) {
      count = bytesLeft;
    }

    return readWriteBytes(buf, start, count, false);
  }

  /**
   * Inspects a record, even if its locked. The cursor is not advanced, neither the current
   * record position. This method can be used to load a locked record.
   * The number of bytes read will be <i>buf.length</i> or the record's size, which is smaller.
   * @param buf The buffer where the data will be read into.
   * @param recordPos The record to be read
   * @param offsetInRec The offset in the record from where to start reading.
   * @return the number of bytes read, which can be different of buf.length if buf.length is greater than the record size.
   *
   * @since SuperWaba 1.1
   * @throws totalcross.io.IOException When the method fails.
   */
  final public int inspectRecord(byte buf[], int recordPos, int offsetInRec)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (buf == null) {
      throw new java.lang.NullPointerException("Argument 'buf' cannot have a null value.");
    }
    if (offsetInRec < 0 || offsetInRec > 65535) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'offsetInRec': " + offsetInRec);
    }

    Vector records = (Vector) openRef;
    if (recordPos < 0 || recordPos >= records.size()) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'recordPos': " + recordPos);
    }

    byte rec[] = (byte[]) records.items[recordPos];
    if (offsetInRec >= rec.length) {
      return 0;
    }
    int count = Math.min(buf.length, rec.length);
    System.arraycopy(rec, offsetInRec, buf, 0, count);
    return count;
  }

  /**
   * Retrieves the attributes of the given record.
   * Note that the current record, if modified
   * after you call this method, will have its attributes changed.
   *
   * @param recordPos the record position from where the record attributes will be retrieved.
   * @return The record attributes
   * @throws totalcross.io.IOException If the method fails
   * @see #REC_ATTR_DELETE
   * @see #REC_ATTR_DIRTY
   * @see #REC_ATTR_SECRET
   */
  final public byte getRecordAttributes(int recordPos)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }

    Vector records = (Vector) openRef;
    if (recordPos < 0 || recordPos >= records.size()) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'recordPos': " + recordPos);
    }

    Byte b = (Byte) _attrs.items[recordPos];
    return b.byteValue();
  }

  /**
   * Sets the attributes of the given record. Note that the current record, if modified after
   * you call this method, will have its attributes changed.
   *
   * @param recordPos the record position from where the record attributes will be retrieved.
   * @param attr the new record attribute.
   * @throws totalcross.io.IOException If the method fails
   * @see #REC_ATTR_DELETE
   * @see #REC_ATTR_DIRTY
   * @see #REC_ATTR_SECRET
   */
  final public void setRecordAttributes(int recordPos, byte attr)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }

    Vector records = (Vector) openRef;
    if (recordPos < 0 || recordPos >= records.size()) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'recordPos': " + recordPos);
    }

    _attrs.items[recordPos] = new Byte(attr);
    modificationNumber++;
  }

  /**
   * Retrieves this PDBFile's attributes.
   *
   * @return The attributes.
   * @see #DB_ATTR_APPINFODIRTY
   * @see #DB_ATTR_BACKUP
   * @see #DB_ATTR_COPY_PREVENTION
   * @see #DB_ATTR_OK_TO_INSTALL_NEWER
   * @see #DB_ATTR_READ_ONLY
   * @see #DB_ATTR_RESET_AFTER_INSTALL
   * @see #DB_ATTR_STREAM
   */
  final public int getAttributes() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }

    return attributes;
  }

  /**
   * Sets this PDBFile's attributes. The
   * original attributes must be retrieved prior applying this value. If not, you
   * can loose your database and your app will crash.
   *
   * @param i The new attributes.
   * @see #DB_ATTR_APPINFODIRTY
   * @see #DB_ATTR_BACKUP
   * @see #DB_ATTR_COPY_PREVENTION
   * @see #DB_ATTR_OK_TO_INSTALL_NEWER
   * @see #DB_ATTR_READ_ONLY
   * @see #DB_ATTR_RESET_AFTER_INSTALL
   * @see #DB_ATTR_STREAM
   */
  final public void setAttributes(int i) throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }

    attributes = (short) i;
    modificationNumber++;
  }

  /**
   * Returns the current record position or -1 if there is no current record.
   *
   * @throws totalcross.io.IOException If the PDBFile is closed
   */
  final public int getRecordPos() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    return _hvRecordPos;
  }

  /**
   * Returns the internal record offset used to read and write data.
   * You may use this value in conjunction
   * with the skipBytes method. Note that setRecordPos resets the offset to 0.
   *
   * @throws totalcross.io.IOException
   *
   * @since SuperWaba 4.0
   */
  public int getRecordOffset() throws totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (_hvRecordPos == -1) {
      throw new totalcross.io.IOException("No record selected for this operation.");
    }
    return _hvRecordOffset;
  }

  /**
   * Searches the underlying PDBFile for the given byte array.
   * Note that its not possible to search using case insensitive, due to the obvious
   * reason that the byte array may not represent a String.
   *
   * @param toSearch The byte array used to compare with the contents of each record.
   * @param length   How many bytes will be searched inside <code>toSearch</code>
   *           (may be smaller than <code>oSearch.length</code>)
   * @param offsetInRec How many bytes to skip from the record start.
   * @return the record index if found, or -1 if toSearch is null, or the
   *         PDBFile is null or closed or empty, or length <= 0, or length >
   *         toSearch.length.
   * @throws totalcross.io.IOException If the method fails
   */
  final public int searchBytes(byte[] toSearch, int length, int offsetInRec)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (openRef == null) {
      throw new totalcross.io.IOException("The pdb file is closed.");
    }
    if (toSearch == null) {
      throw new java.lang.NullPointerException("Argument 'toSearch' cannot have a null value.");
    }
    if (offsetInRec < 0) {
      throw new totalcross.io.IllegalArgumentIOException("Invalid value for argument 'offsetInRec': " + offsetInRec);
    }
    if (length <= 0 || length > toSearch.length) {
      throw new ArrayIndexOutOfBoundsException();
    }

    int n = getRecordCount();
    if (n == 0) {
      return -1;
    }

    int found = -1;
    byte buf[] = new byte[length];
    int lenM1 = length - 1;
    for (int i = Math.max(_hvRecordPos, 0); i < n && found == -1; i++) // guich@tc110_34: consider recordPos in the search
    {
      int bytesRead;
      try {
        bytesRead = inspectRecord(buf, i, offsetInRec);
        if (bytesRead >= length && buf[lenM1] == toSearch[lenM1] && buf[0] == toSearch[0]) {
          found = i;
          if (length > 2) {
            for (int j = 1; j < lenM1; j++) {
              if (buf[j] != toSearch[j]) {
                found = -1;
                break;
              }
            }
          }
        }
      } catch (ArrayIndexOutOfBoundsException e) {
      }
    }
    return found;
  }

  private String removePDBFileFromDisk() {
    String path = (String) Launcher.instance.htOpenedAt.get(fileName);
    if (path == null || path.length() == 0) {
      path = System.getProperty("user.dir");
    }
    if (path == null) {
      path = ".";
    }

    java.io.File f = new java.io.File(path, fileName);
    if (!f.exists()) {
      path = "";
      f = new java.io.File(path, fileName);
      if (!f.exists() && Launcher.instance.getDataPath() != null) // guich@450_: test also with dataPath
      {
        path = Launcher.instance.getDataPath();
        f = new java.io.File(path, fileName);
      }
    }
    deleted = true; // guich@400_3

    // guich@400_3: if the file was created in the same session it is being deleted, it may not have been written to disk, so we can simply return as if it was created.
    if (f.exists()) {
      try {
        f.delete();
        return path;
      } catch (SecurityException e) {
        //            throw new totalcross.io.IOException(e.getMessage(), e);
      }
    }
    return null;
  }

  // JDK's implementation
  private static Hashtable _dbHash = new Hashtable(31);
  private String _name;
  private Vector _attrs = new Vector(); // guich@200
  private String _creator; // guich@120
  private String _type; // guich@120
  private int originalModificationNumber; // guich@120
  private boolean deleted; // guich@400_3
  /** The following structure declaration represents an application
   * information block for an application that uses standard Palm OS
   * category information:
   * <pre>
   * typedef struct
   * {
   *    UInt16 renamedCategories;
   *    Char categoryLabels[16][16];
   *    UInt8 categoryUniqIDs[16];
   *    UInt8 lastUniqID;
   *    UInt8 padding;
   * } AppInfoType;.PDB and PRC Database Formats
   * </pre>
   * The Sort Information Block<br>
   * Field Descriptions<br>
   * . renamedCategories Specifies which categories have been renamed.<br>
   * . categoryLabel An array of 16 null-terminated category labels, each of which is 16 bytes long.<br>
   * . categoryID An array of 16 category ID values, each of which is one byte long.<br>
   * . lastUniqID The last unique category ID assigned.<br>
   * . padding Unused.<br>
   *
   */
  private byte[] appInfoBlock; // guich@200b4: store here the appinfoblock (with the categories)
  private byte[] sortInfoBlock; // guich@200b4

  /** The creation date of the database, specified as the number of seconds since
   * 12:00 A.M. on January 1, 1904.
   */
  private int creationDate;
  /** The date of the most recent modification of the database, specified as the
   * number of seconds since 12:00 A.M. on January 1, 1904.
   */
  private int modificationDate;
  /** The date of the most recent backup of the database, specified as the number
   * of seconds since 12:00 A.M. on January 1, 1904.
   */
  private int lastBackupDate;
  /** The modification number of the database. */
  private int modificationNumber;
  /** The attribute flags for the database. */
  private short attributes = PDBFile.DB_ATTR_BACKUP; // guich@330_44: don't force the Attrbackup bc the user may want to clear it
  /** The application-specific version of the database layout. */
  private short version = 1;
  /** The local offset from the beginning of the database header data to the start
   * of the optional, application-specific appInfo block. This value is set to NULL
   * for databases that do not include an appInfo block.
   */
  private int appInfoOffset;
  /** The local offset from the beginning of the PDB header data to the start of the
   * optional, application-specific sortInfo block. This value is set to NULL for
   * databases that do not include a sortInfo block.
   */
  private int sortInfoOffset;
  /** Used internally by the Palm OS to generate unique identifiers for records on the
   * Palm device when the database is loaded into the device. For PRC databases, this
   * value is normally not used and is set to 0. For PQA databases, this value is not
   * used, and is set to 0.
   */
  private int uniqueIDSeed;
  /** The local chunk ID of the next record list in this database. This is 0 if there
   * is no next record list, which is almost always the case. <b>Important!</b> In
   * SuperWaba, this type of database is not supported!
   */
  private int nextRecordListID;

  // created by Mr. Tines to correct the date error
  private long getNow() {
    long timeX = new java.util.Date().getTime() / 1000; // seconds since 1970
    // add of 66 years - Mac date is based in # of seconds since 1904
    // Don't forget the leap days!
    timeX += ((66 * 365) + 17) * 24 * 60 * 60;
    return timeX;
  }

  // created by Mr. Tines to correct the date error
  private void doSetDate() {
    long timeX = getNow();
    creationDate = (int) timeX;
    modificationDate = creationDate;
    timeX -= 365 * 24 * 60 * 60; // sub 1 year
    lastBackupDate = (int) timeX; // make sure we will be hotsync'ed!
  }

  /** converts the records to a pdb file. @param records a vector of array of bytes */
  private void toPDB(OutputStream outStream, Vector records, String creatorId, String dbName, String typeId,
      Vector _attrs) throws totalcross.io.IOException {
    byte name[] = new byte[32];
    byte type[] = typeId.getBytes();
    byte creator[] = creatorId.getBytes();

    // guich@200b4: verifies the db name
    int slash = -1;
    if ((slash = dbName.lastIndexOf('\\')) != -1 || (slash = dbName.lastIndexOf('/')) != -1) {
      dbName = dbName.substring(slash + 1);
    }
    if (dbName.length() > 31) {
      dbName = dbName.substring(0, 31);
    }
    // copies the db name to inside the array <name>
    byte[] bn = dbName.getBytes();
    for (int i = 0; i < name.length; i++) {
      name[i] = (i < bn.length) ? bn[i] : 0;
    }

    int numRecords = records == null ? 0 : records.size(); // guich@tc110_35: delete may call writeCB, which then calls toPDB. but records is nulled by delete.
    ByteArrayStream bas = new ByteArrayStream(8192); // guich@552_19: write the header to a temp bytearray
    DataStream os = new DataStream(bas);

    int offset = 80 + numRecords * 8;
    if (appInfoBlock != null) {
      appInfoOffset = offset;
      offset += appInfoBlock.length;
    }
    if (sortInfoBlock != null) {
      sortInfoOffset = offset;
      offset += sortInfoBlock.length;
    }

    //guich@330_44: commented out -> attributes |= PDBFile.DB_ATTR_BACKUP;
    modificationDate = (int) getNow();

    // DatabaseHdrType
    os.writeBytes(name);
    os.writeShort(attributes);
    os.writeShort(version);
    os.writeInt(creationDate);
    os.writeInt(modificationDate);
    os.writeInt(lastBackupDate);
    os.writeInt(modificationNumber);
    os.writeInt(appInfoOffset);
    os.writeInt(sortInfoOffset);
    os.writeBytes(type);
    os.writeBytes(creator);
    os.writeInt(uniqueIDSeed);

    // RecordListType
    int nextRecordListID = 0;
    os.writeInt(nextRecordListID);
    os.writeShort(numRecords);
    byte[] recUniqueID = { 0, 0, 0 }; // let the system create them again
    for (int i = 0; i < numRecords; i++) {
      os.writeInt(offset); // LocalChunkID
      if (_attrs == null || i >= _attrs.size()) {
        os.writeByte(0); // attributes
      } else {
        os.writeByte(((Byte) _attrs.items[i]).byteValue() & ~0x20); // guich@300_64: disable all busy attributes
      }
      // get back the uniqueID if already stored
      os.writeBytes(recUniqueID);
      int recSize = ((byte[]) records.items[i]).length;
      if (recSize > 65520) {
        Launcher.print("CAUTION! RECORD " + i + " HAS A SIZE GREATER THAN 65520 AND WILL BE REJECTED BY PALM OS!"); // guich@230_12
      }
      offset += recSize;
    }
    os.writeShort(0); // pad

    // AppInfoBlock (guich@200b4)
    if (appInfoBlock != null) {
      os.writeBytes(appInfoBlock);
    }

    try {
      outStream.write(bas.getBuffer(), 0, bas.getPos()); // write the header
      for (int i = 0; i < numRecords; i++) {
        outStream.write((byte[]) records.items[i]);
        records.items[i] = null; // guich@552_19: frees some memory
      }
      outStream.close();
    } catch (java.io.IOException e) {
      throw new totalcross.io.IOException(e.getMessage());
    }
  }

  /** reads a pdb file and returns a vector of records. */
  private Vector fromPDB(byte[] all, String creatorId, String typeId, Vector _attrs) throws totalcross.io.IOException {
    // ps: i had to read everything to an array because reading one record at a time was reading trash.
    DataStream is = new DataStream(new ByteArrayStream(all));

    if (all.length == 0) {
      throw new totalcross.io.IOException("Invalid or corrupted PDBFile with length 0.");
    }
    // DatabaseHdrType
    byte name[] = new byte[32]; // ps: the written string is in c++ format, so this routine doesnt loads the name correctly (comes trash with it)
    byte type[] = new byte[4];
    byte creator[] = new byte[4];

    is.readBytes(name);
    attributes = is.readShort();
    version = is.readShort();
    creationDate = is.readInt();
    modificationDate = is.readInt();
    lastBackupDate = is.readInt();
    modificationNumber = is.readInt();
    appInfoOffset = is.readInt();
    sortInfoOffset = is.readInt();
    is.readBytes(type);
    is.readBytes(creator);
    uniqueIDSeed = is.readInt();
    originalModificationNumber = modificationNumber;

    if (all.length >= 8 && new String(all, 0, 8).trim().equalsIgnoreCase("<HTML>")) {
      return new Vector(); // return a empty db
    }

    // verify if the creatorId is valid
    boolean creatorOk = creatorId.equals(new String(creator));
    boolean typeOk = typeId.equals(new String(type));

    if (!creatorOk && !typeOk) {
      throw new totalcross.io.IOException("Database already exists with different creator and type.");
    }
    if (!creatorOk) {
      throw new totalcross.io.IOException("Database already exists with different creator.");
    }
    if (!typeOk) {
      throw new totalcross.io.IOException("Database already exists with different type.");
    }

    // RecordListType
    nextRecordListID = is.readInt();
    if (nextRecordListID != 0) {
      throw new totalcross.io.IOException(
          "Invalid database file! TotalCross does not support multiple record or resource lists in a Database!!!");
    }
    int numRecords = is.readUnsignedShort(); // guich@340_48
    // reads the header (meaningless)
    int recOffsets[] = new int[numRecords + 1];
    byte recAttributes;
    byte recUniqueID[] = new byte[3];
    if (_attrs != null) {
      _attrs.removeAllElements();
    }
    for (int i = 0; i < numRecords; i++) {
      recOffsets[i] = is.readInt(); // offset
      recAttributes = is.readByte();
      is.readBytes(recUniqueID);
      if (_attrs != null) {
        _attrs.addElement(new Byte(recAttributes));
      }
    }
    recOffsets[numRecords] = all.length; // add the total size so we can compute the size of each record

    int offset = 80 + numRecords * 8;
    int offset2 = recOffsets[0];
    is.skipBytes(2);

    // guich@200b4: read the appInfoBlock and sortInfoBlock and store it in a safe place
    if (appInfoOffset > 0) {
      int len = sortInfoOffset > 0 ? (sortInfoOffset - offset) : (recOffsets[0] - offset);
      appInfoBlock = new byte[len];
      is.readBytes(appInfoBlock);
      offset += len;
    }
    if (sortInfoOffset > 0) {
      int len = recOffsets[0] - offset;
      sortInfoBlock = new byte[len];
      is.readBytes(sortInfoBlock);
      offset += len;
    }
    int toSkip = (offset2 - offset); // pad - guich@580_3: fixed sf_1432481  - guich@tc112_22: moved to after the app and sort blocks are read
    is.skipBytes(toSkip);

    // the records were written in sequence from here
    Vector v = new Vector(numRecords);
    int size = 0;
    for (int i = 0; i < numRecords; i++) {
      if (_attrs != null && (((Byte) _attrs.items[i]).byteValue() & PDBFile.REC_ATTR_DELETE) != 0) {
        _attrs.removeElementAt(i);
      } else {
        size = recOffsets[i + 1] - recOffsets[i];
        if (size <= 0) {
          throw new totalcross.io.IOException("Invalid record " + i + "! Size <= 0 (" + size + ")");
        }
        byte[] bytes = new byte[size];
        is.readBytes(bytes);
        v.addElement(bytes);
      }
    }
    is.close();

    return v;
  }

  @Override
  protected void finalize() {
    try {
      close();
    } catch (totalcross.io.IOException e) {
    }
  }
}