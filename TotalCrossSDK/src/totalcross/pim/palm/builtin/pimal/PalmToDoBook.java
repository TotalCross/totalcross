/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003 Gilbert Fridgen                                           *
 *  Copyright (C) 2003-2012 SuperWaba Ltda.                                      *
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



package totalcross.pim.palm.builtin.pimal;

import totalcross.pim.NotSupportedByDeviceException;
import totalcross.pim.RecordList;
import totalcross.pim.palm.builtin.ToDo;
import totalcross.pim.todobook.ToDoBook;
import totalcross.pim.todobook.ToDoRecord;
import totalcross.util.Vector;

/**
 * Implementation of the ToDoBook interface for PalmOS
 *
 * @author Gilbert Fridgen
 */
public class PalmToDoBook implements ToDoBook
{
   private Vector records = new Vector();

   public PalmToDoBook() throws totalcross.io.IOException
   {
      ToDo.initToDo();
   }

   /**
    * @see totalcross.pim.todobook.ToDoBook#getToDoRecords()
    */
   public RecordList getToDoRecords()
   {
      records.removeAllElements();
      int n = ToDo.todoCount();
      for (int i = 0; i < n; i++)
         if (ToDo.getToDo(i) != null)
            records.addElement(new PalmToDoRecord(i));
      return new RecordList(records);
   }

   /**
    * @see totalcross.pim.todobook.ToDoBook#createToDoRecord()
    */
   public ToDoRecord createToDoRecord() throws NotSupportedByDeviceException
   {
      ToDo physicalRecord = new ToDo();
      physicalRecord.description = "<empty>";
      ToDo.addToDo(physicalRecord);
      ToDoRecord tdr = new PalmToDoRecord(ToDo.todoCount() - 1);
      records.addElement(tdr);
      return tdr;
   }

   /**
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see totalcross.pim.todobook.ToDoBook#getCategories()
    */
   public Vector getCategories() throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /**
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see totalcross.pim.todobook.ToDoBook#addCategory(java.lang.String)
    */
   public void addCategory(String category) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /**
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see totalcross.pim.todobook.ToDoBook#removeCategory(java.lang.String)
    */
   public void removeCategory(String category) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /**
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see totalcross.pim.todobook.ToDoBook#renameCategory(java.lang.String,
    *      java.lang.String)
    */
   public void renameCategory(String oldName, String newName) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /**
    * @see totalcross.pim.todobook.ToDoBook#deleteToDoRecord(totalcross.pim.todobook.ToDoRecord)
    */
   public void deleteToDoRecord(ToDoRecord todoRecord)
   {
      ToDo.delToDo(((PalmToDoRecord) todoRecord).getIndex());
      records.items[records.indexOf((Object) todoRecord)] = null;
   }
}
