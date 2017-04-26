/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003 Fabian Kroeher                                            *
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



package totalcross.pim.ce.builtin.pimal;

import totalcross.pim.ce.builtin.*;
import totalcross.pim.todobook.*;
import totalcross.pim.*;
import totalcross.util.*;

/**
 * Implements the <code>pimal.todobook.ToDoBook</code> interface for PocketPC devices
 * @author Fabian Kroeher
 *
 */
public class PocketPCToDoBook implements ToDoBook
{
   private Vector records;
   /**
    * creates a new instance of PocketPCToDoBook
    *
    */
   public PocketPCToDoBook()
   {
      records = new Vector();
   }
   /* (non-Javadoc)
   * @see pimal.todobook.ToDoBook#getToDoRecords()
   */
   public RecordList getToDoRecords()
   {
      // first, clear the records Vector
      records.removeAllElements();
      // now, get the records from the device and store them in the records Vector
      ITasks tasks = IPOutlookItemCollection.viewITasks();
      int n = tasks.size();
      for (int i = 0; i < n; i++)
         records.addElement(new PocketPCToDoRecord(tasks.iTaskAt(i)));
      return new RecordList(records);
   }
   /* (non-Javadoc)
   * @see pimal.todobook.ToDoBook#createToDoRecord()
   */
   public ToDoRecord createToDoRecord() throws NotSupportedByDeviceException
   {
      ITask tmp = IPOutlookItemCollection.createITask();
      PocketPCToDoRecord ppcTmp = new PocketPCToDoRecord(tmp);
      records.addElement(ppcTmp);
      return ppcTmp;
   }
   /* (non-Javadoc)
   * @see pimal.todobook.ToDoBook#deleteToDoRecord(pimal.todobook.ToDoRecord)
   */
   public void deleteToDoRecord(ToDoRecord todoRecord)
   {
      records.items[records.indexOf((Object)todoRecord)] = null;
      ((PocketPCToDoRecord)todoRecord).delete();
   }
   /**
    * Always throws <code>NotSupportedByDeviceException</code> since categories are currently not supported
    * @see totalcross.pim.addressbook.AddressBook#getCategories()
    */
   public Vector getCategories() throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The PPC interface doesn't provide access to categories!");
   }
   /**
    * Always throws <code>NotSupportedByDeviceException</code> since categories are currently not supported
    *     * @see totalcross.pim.addressbook.AddressBook#addCategory(java.lang.String)
    */
   public void addCategory(String category) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The PPC interface doesn't provide access to categories!");
   }
   /**
    * Always throws <code>NotSupportedByDeviceException</code> since categories are currently not supported
    * @see totalcross.pim.addressbook.AddressBook#removeCategory(java.lang.String)
    */
   public void removeCategory(String category) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The PPC interface doesn't provide access to categories!");
   }
   /**
    * Always throws <code>NotSupportedByDeviceException</code> since categories are currently not supported
    */
   public void renameCategory(String oldName, String newName) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The PPC interface doesn't provide access to categories!");
   }
}
