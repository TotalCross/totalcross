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

/**
 * An interface for all objects that support loading and saving themselves
 * through the ObjectCatalog class. This works in a similar manner to the
 * Externalizable interface in Java. When saveState() is called, the value of
 * any fields should be written to the given DataStream. When loadState() is
 * called these values should be read back using the equivalent methods, making
 * sure the order remains the same. After loadState() the new object should
 * behave exactly as it did before the corresponding saveState(). Here is an
 * example of the methods in use.
 * <p>
 * <blockquote>
 *
 * <pre>
 * public class MyClass implements Storable
 * {
 *    int    i;
 *    String s;
 *
 *    public MyClass()
 *    {
 *    }
 *
 *    public MyClass(int i, String s)
 *    {
 *       this.i = i;
 *       this.s = s;
 *    }
 *
 *    public byte getID()
 *    {
 *       return (byte) 123;
 *    }
 *
 *    public Storable getInstance()
 *    {
 *       return new MyClass();
 *    }
 *
 *    public void saveState(DataStream ds)
 *    {
 *       ds.writeInt(i);
 *       ds.writeString(s);
 *    }
 *
 *    public void loadState(DataStream ds)
 *    {
 *       i = ds.readInt();
 *       s = ds.readString();
 *    }
 * }
 * </pre>
 *
 * </blockquote>
 *
 * @author <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,
 * @version 1.1.0 16 October 1999
 */
public interface Storable {
  /**
   * Gets a unique ID for this class. It is up to the user to ensure that the
   * ID of each class of Storable contained in a single ObjectCatalog is unique
   * and the ID of each instance in a class is the same. If the ID returned is
   * zero, no type information will be saved to the catalog and
   * ObjectCatalog.loadObjectAt(int) cannot be used. It is useful, however when
   * accessing Catalogs from other programs using the ObjectCatalog model.
   */
  public byte getID();

  /**
   * Returns an object of the same class as this object.
   *
   * @return a class. Any data is irrelevent.
   */
  public Storable getInstance();

  /**
   * Send the state information of this object to the given object catalog
   * using the given DataStream. If any Storable objects need to be saved as
   * part of the state, their saveState() method can be called too.
   * @throws totalcross.io.IOException
   */
  public void saveState(DataStream ds) throws totalcross.io.IOException;

  /**
   * Load state information from the given DataStream into this object If any
   * Storable objects need to be loaded as part of the state, their loadState()
   * method can be called too.
   *
   * @throws totalcross.io.IOException
   */
  public void loadState(DataStream ds) throws totalcross.io.IOException;
}
