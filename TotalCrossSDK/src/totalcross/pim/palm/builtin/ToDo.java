/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2001 Rob Nielsen                                               *
 *  Copyright (C) 2001-2012 SuperWaba Ltda.                                      *
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



package totalcross.pim.palm.builtin;

import totalcross.io.DataStream;
import totalcross.io.ObjectPDBFile;
import totalcross.io.PDBFile;
import totalcross.io.Storable;
import totalcross.sys.Time;

/**
 * Provides a link to the standard Palm ToDo database.
 *
 * @author <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,
 * @version 1.0.0 16 October 1999
 */
public class ToDo implements Storable
{
   /** the todo PDBFile */
   private static ObjectPDBFile todoCat;

   public static void initToDo() throws totalcross.io.IOException
   {
      if (todoCat == null)
         todoCat = new ObjectPDBFile("ToDoDB.todo.DATA");
   }

   /**
    * Gets the number of ToDo's in the database
    *
    * @return the number of todos
    */
   public static int todoCount()
   {
      return todoCat.getRecordCount();
   }

   /**
    * Gets a ToDo from the database
    *
    * @param i
    *           the index to get
    * @return the retrieved todo
    */
   public static ToDo getToDo(int i)
   {
      ToDo todo = new ToDo();
      if (todoCat.loadObjectAt(todo, i))
         return todo;
      return null;
   }

   /**
    * Gets a ToDo from the database and places it into the given ToDo. Any
    * previous data in the todo is erased.
    *
    * @param i
    *           the index to get
    * @param todo
    *           the todo object to place the todo into.
    */
   public static boolean getToDo(int i, ToDo todo)
   {
      return todoCat.loadObjectAt(todo, i);
   }

   /**
    * Adds a new ToDo to the database
    *
    * @param todo
    *           the todo to add
    * @return true if successful, false otherwise
    */
   public static boolean addToDo(ToDo todo)
   {
      return todoCat.addObject(todo);
   }

   /**
    * Deletes a ToDo entry in the database
    *
    * @param i
    *           the index to delete
    * @return true if successful, false otherwise
    */
   public static boolean delToDo(int i)
   {
      return todoCat.setObjectAttribute(i, PDBFile.REC_ATTR_DELETE);
   }

   /**
    * Changes the ToDo at the given index
    *
    * @param i
    *           the index to change
    * @param todo
    *           a ToDoobject with the values you want the ToDo at i to have
    * @return true if successful, false otherwise
    */
   public static boolean changeToDo(int i, ToDo todo)
   {
      if (todo == null)
         return false;
      if (todoCat.deleteObjectAt(i))
         return todoCat.insertObjectAt((Storable) todo, i);
      else
         return false;
   }

   // *************************** //
   // individual todo stuff below //
   // *************************** //
   /**
    * The time this todo item is to be completed - note only date information is
    * used
    */
   public Time    dueDate;
   /** The priority of this todo from 1-5 */
   public int     priority;
   /** Has this todo been completed? */
   public boolean completed;
   /** A description for this todo */
   public String  description;
   /** A note giving extra information */
   public String  note;

   /**
    * Constructs a new empty todo
    */
   public ToDo()
   {

   }

   /**
    * Send the state information of this object to the given object PDBFile
    * using the given DataStream. If any Storable objects need to be saved as
    * part of the state, their saveState() method can be called too.
    * @throws totalcross.io.IOException
    */
   public void saveState(DataStream ds) throws totalcross.io.IOException
   {
      priority = inRange(priority, 1, 5);
      if (dueDate == null || dueDate.year < 1904 || dueDate.year > 1904 + 127 || dueDate.month < 1
            || dueDate.month > 12 || dueDate.day < 1 || dueDate.day > 31)
         ds.writeShort(~0);
      else
      {
         int packedDate = (((dueDate.year - 1904) & 127) << 9) | ((dueDate.month & 15) << 5) | (dueDate.day & 31);
         ds.writeShort(packedDate);
      }
      priority = (priority < 1 ? 1 : priority > 5 ? 5 : priority);
      int priorityByte = priority | (completed ? 128 : 0);
      ds.writeByte(priorityByte);
      ds.writeCString(description);
      ds.writeCString(note);
   }

   private int inRange(int i, int a, int b)
   {
      return (i <= a) ? a : (i >= b) ? b : i;
   }

   /**
    * Load state information from the given DataStream into this object If any
    * Storable objects need to be loaded as part of the state, their loadState()
    * method can be called too.
    *
    * @throws totalcross.io.IOException
    */
   public void loadState(DataStream ds) throws totalcross.io.IOException
   {
      int packedDate = ds.readShort();
      if ((packedDate & (1 << 16) - 1) == (1 << 16) - 1)
         dueDate = null;
      else
      {
         if (dueDate == null)
            dueDate = new Time();
         dueDate.day = (packedDate & 31);
         packedDate >>>= 5;
         dueDate.month = (packedDate & 15);
         packedDate >>>= 4;
         dueDate.year = (packedDate & 127) + 1904;
         dueDate.hour = dueDate.minute = dueDate.second = dueDate.millis = 0;
      }
      int priorityByte = ds.readUnsignedByte();
      priority = (priorityByte & 127);
      completed = (priorityByte & 128) > 0;
      description = ds.readCString();
      if (description.length() == 0)
         description = null;
      // notes can be big so free up this one before loading in the
      // new one
      note = null;
      note = ds.readCString();
      if (note.length() == 0)
         note = null;
   }

   /**
    * Gets a unique ID for this class. It is up to the user to ensure that the
    * ID of each class of Storable contained in a single ObjectPDBFile is unique
    * and the ID of each instance in a class is the same.
    */
   public byte getID()
   {
      return 0; // not used
   }

   /**
    * Returns an object of the same class as this object.
    *
    * @return a class. Any data is irrelevent.
    */
   public Storable getInstance()
   {
      return new ToDo();
   }
}
