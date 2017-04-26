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
import totalcross.pim.datebook.*;
import totalcross.pim.*;
import totalcross.util.*;
/**
 * Implements the <code>pimal.datebook.DateBook</code> interface for Pocket PC devices
 * @author Fabian Kroeher
 *
 */
public class PocketPCDateBook implements DateBook
{
   private Vector records;
   /**
    * creates a new instance of PocketPCDateBook
    *
    */
   public PocketPCDateBook()
   {
      records = new Vector();
   }
   /* (non-Javadoc)
   * @see pimal.datebook.DateBook#getDateRecords()
   */
   public RecordList getDateRecords()
   {
      // first, clear the records Vector
      records.removeAllElements();
      // now, read the records from the device and store them in the records Vector
      IAppointments appointments = IPOutlookItemCollection.viewIAppointments();
      int n = appointments.size();
      for (int i = 0; i < n; i++)
      {
         IAppointment tmp = appointments.iAppointmentAt(i);
         records.addElement(new PocketPCDateRecord(tmp));
      }
      return new RecordList(records);
   }
   /* (non-Javadoc)
   * @see pimal.datebook.DateBook#createDateRecord()
   */
   public DateRecord createDateRecord() throws NotSupportedByDeviceException
   {
      IAppointment tmp = IPOutlookItemCollection.createIAppointment();
      PocketPCDateRecord ppcTmp = new PocketPCDateRecord(tmp);
      records.addElement(ppcTmp);
      return ppcTmp;
   }
   /* (non-Javadoc)
   * @see pimal.datebook.DateBook#deleteDateRecord(pimal.datebook.DateRecord)
   */
   public void deleteDateRecord(DateRecord dateRecord)
   {
      records.items[records.indexOf((Object)dateRecord)] = null;
      ((PocketPCDateRecord)dateRecord).delete();
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
    * @see totalcross.pim.addressbook.AddressBook#renameCategory(java.lang.String, java.lang.String)
    */
   public void renameCategory(String oldName, String newName) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The PPC interface doesn't provide access to categories!");
   }
}
