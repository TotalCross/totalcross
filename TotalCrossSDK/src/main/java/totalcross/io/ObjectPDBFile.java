// Copyright (C) 2003 Rob Nielsen
// Copyright (C) 2003-2013 SuperWaba Ltda.
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

import totalcross.util.Vector;

/**
 * An extension to PDBFile that allows storage and retrieval of objects that
 * implement the Storable interface. Create an ObjectPDBFile and use the
 * addObject() method on the objects you want to store. If you want a particular
 * object you can use loadObjectAt() to load the stored details into an object
 * or to search through all records call resetSearch() and then loop with
 * nextObject() until it returns false. The example below shows an example of
 * it's use with a PDBFile of identical data:
 * <p>
 * <blockquote>
 *
 * <pre>
 * ObjectPDBFile oc = new ObjectPDBFile(&quot;Test.DATA&quot;);
 * MyObject obj = new MyObject();
 * oc.resetSearch();
 * while (oc.nextObject(obj))
 * {
 *    // do something with obj
 * }
 * </pre>
 *
 * </blockquote>
 * <p>
 * Here's an example using unknown data. The two sections of code save a vector
 * containing a number of Lines, Circles, and Squares (all implementing
 * Storable) in no particular order, then loads it back in again.
 * <p>
 * <blockquote>
 *
 * <pre>
 *   // save data
 *   ObjectPDBFile oc=new ObjectPDBFile(&quot;Test.DATA&quot;,ObjectPDBFile.CREATE);
 *   for(int i=0,size=objs.getCount();i++)
 *     oc.addObject((Storable)objs.get(i));
 *   oc.close();
 *
 *   // load data
 *   ObjectPDBFile oc=new ObjectPDBFile(&quot;Test.DATA&quot;);
 *   oc.registerClass(new Line());
 *   oc.registerClass(new Circle());
 *   oc.registerClass(new Square());
 *   objs=new Vector();
 *   oc.resetSearch();
 *   Storable obj;
 *   while ((obj=oc.nextObject())!=null)
 *   {
 *     objs.add(obj);
 *   }
 *   oc.close();
 * </pre>
 *
 * </blockquote>
 *
 * @author <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,
 * @version 1.2.0 16 October 1999
 */
public class ObjectPDBFile extends PDBFile {
  /* the registered classes */
  protected Vector classes;

  /* the position in the search through the records */
  protected int cnt;

  protected byte[] buf;

  protected ByteArrayStream bs;

  protected DataStream ds;

  /**
   * Constructs a new ObjectPDBFile
   *
   * @param name
   *           the name of the PDBFile
   * @param type
   *           the mode to open the PDBFile with
   * @throws totalcross.io.IOException
   * @throws totalcross.io.FileNotFoundException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  public ObjectPDBFile(String name, int type)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.FileNotFoundException, totalcross.io.IOException {
    super(name, type);
  }

  /**
   * Construct a new ObjectPDBFile
   *
   * @param name
   *           the name of the PDBFile
   * @throws totalcross.io.IOException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  public ObjectPDBFile(String name) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    super(name, PDBFile.CREATE);
  }

  /**
   * Registers this class with the PDBFile. Classes must be registered before
   * using the loadObjectAt(int) method.
   *
   * @param s
   *           an instance of the class to register. The contents are ignored.
   */
  public void registerClass(Storable s) {
    if (classes == null) {
      classes = new Vector();
    }
    classes.addElement(s);
  }

  /**
   * Add an object to this PDBFile.
   *
   * @param s
   *           the storable object to add
   * @throws totalcross.io.IOException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  public void addObject(Storable s) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (bs == null) {
      bs = new ByteArrayStream(1024);
      ds = new DataStream(bs);
    } else {
      bs.reset();
    }

    if (s.getID() != 0) {
      ds.writeByte(s.getID());
    }
    s.saveState(ds);

    byte[] buf = bs.getBuffer();
    addRecord(buf.length);
    writeBytes(buf, 0, buf.length);
    setRecordPos(-1);
  }

  /**
   * Insert an object to this PDBFile.
   *
   * @param s
   *           the storable object to add
   * @param i
   *           the index where to insert
   * @throws totalcross.io.IOException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  public void insertObjectAt(Storable s, int i)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (bs == null) {
      bs = new ByteArrayStream(1024);
      ds = new DataStream(bs);
    } else {
      bs.reset();
    }

    if (s.getID() != 0) {
      ds.writeByte(s.getID());
    }
    s.saveState(ds);

    byte[] buf = bs.getBuffer();
    addRecord(buf.length, i);
    writeBytes(buf, 0, buf.length);
    setRecordPos(-1);
  }

  /**
   * Load an object from the PDBFile into the given storable. Unpredictable
   * results will occur if the object in the PDBFile is not of the same class
   * as the storable given. Good for when you know what each record will
   * contain.
   *
   * @param s
   *           the object to load the data into
   * @param i
   *           the index in the PDBFile to load from
   * @throws totalcross.io.IOException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  public void loadObjectAt(Storable s, int i)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    // bs=null;
    // buf=null;
    setRecordPos(i);
    int size = getRecordSize();
    if (buf == null || buf.length < size) {
      buf = new byte[size];
    }
    readBytes(buf, 0, size);
    setRecordPos(-1);
    if (bs == null) {
      bs = new ByteArrayStream(buf);
      ds = new DataStream(bs);
    } else {
      bs.setBuffer(buf);
    }

    if (s.getID() != 0) {
      ds.readByte();
    }
    s.loadState(ds);
  }

  /**
   * Loads an object from the PDBFile. Good for when you don't know which
   * classes are going to be in records. Note that you must call the
   * registerClass() with each storable class before this method will work
   * properly.
   *
   * @param i
   *           the index in the PDBFile to load from
   * @return the loaded object, or null if unsucessful
   * @throws totalcross.io.IOException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  public Storable loadObjectAt(int i) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    Storable s = null;

    setRecordPos(i);
    if (classes != null) {
      int recsize = getRecordSize();
      if (buf == null || buf.length < recsize) {
        buf = new byte[recsize];
      }
      readBytes(buf, 0, recsize);

      if (bs == null) {
        bs = new ByteArrayStream(buf);
        ds = new DataStream(bs);
      } else {
        bs.setBuffer(buf);
      }

      setRecordPos(-1);
      byte type = ds.readByte();

      for (int j = 0, size = classes.size(); j < size; j++) {
        if ((s = (Storable) classes.items[j]).getID() == type) {
          s = s.getInstance();
          s.loadState(ds);
          break;
        }
      }
    }
    return s;
  }

  /**
   * Delete an object from the PDBFile
   *
   * @param i
   *           the index to delete from
   * @throws totalcross.io.IOException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  public void deleteObjectAt(int i) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    setRecordPos(i);
    deleteRecord();
  }

  /**
   * Set attributes of an object record from the PDBFile
   *
   * @param i
   *           the index to set the attribute
   * @param a
   *           the attribute to set, use the REC_ATTR_XXXX constants from
   *           PDBFile class
   * @return true if sucessful, false otherwise
   * @throws totalcross.io.IOException
   */
  public boolean setObjectAttribute(int i, byte a) throws totalcross.io.IOException {
    setRecordAttributes(i, a);
    return true;
  }

  /**
   * Get attributes of an object record from the PDBFile
   *
   * @param i
   *           the index to get the attribute from
   * @return the record attributes
   * @throws totalcross.io.IOException
   */
  public byte getObjectAttribute(int i) throws totalcross.io.IOException {
    return getRecordAttributes(i);
  }

  /**
   * Get the size of this PDBFile
   *
   * @return the number of records contained by it
   * @throws totalcross.io.IOException
   */
  public int size() throws totalcross.io.IOException {
    return getRecordCount();
  }

  /**
   * Resets a counter for iterating through the PDBFile. Should be called
   * before iterating with nextObject().
   */
  public void resetSearch() {
    setSearchIndex(0);
  }

  /**
   * Sets the search counter at the given index in the PDBFile.
   *
   * @param i
   *           the index to start
   */
  public void setSearchIndex(int i) {
    cnt = i;
  }

  /**
   * Gets the next object in the PDBFile and places it in the given storable.
   *
   * @return true if sucessful, false if the end of the PDBFile has been
   *         reached
   * @throws totalcross.io.IOException
   * @throws totalcross.io.IllegalArgumentIOException
   */
  public boolean nextObject(Storable s) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    boolean ret = cnt < size();

    if (ret) {
      loadObjectAt(s, cnt++);
    }
    return ret;
  }

  /**
   * Gets the next object in the PDBFile.
   *
   * @return the next object, or null on error or if the end has been reached
   * @throws totalcross.io.IOException
   */
  public Storable nextObject() throws totalcross.io.IOException {
    return cnt < size() ? loadObjectAt(cnt++) : null;
  }
}
