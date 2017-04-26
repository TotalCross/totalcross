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
import totalcross.pim.addressbook.AddressBook;
import totalcross.pim.addressbook.AddressRecord;
import totalcross.pim.palm.builtin.Address;
import totalcross.util.Vector;

/**
 * Implementation of the AddressBook interface for PalmOS
 *
 * @author Gilbert Fridgen
 */
public class PalmAddressBook implements AddressBook
{
   private Vector records = new Vector();

   public PalmAddressBook() throws totalcross.io.IOException
   {
      Address.initAddress();
   }

   /*
    * (non-Javadoc)
    *
    * @see pimal.addressbook.AddressBook#getAddressRecords()
    */
   public RecordList getAddressRecords()
   {
      records.removeAllElements();
      int n = Address.addressCount();
      for (int i = 0; i < n; i++)
         if (Address.getAddress(i) != null)
            records.addElement(new PalmAddressRecord(i));
      return new RecordList(records);
   }

   /*
    * (non-Javadoc)
    *
    * @see pimal.addressbook.AddressBook#createAddressRecord()
    */
   public AddressRecord createAddressRecord() throws NotSupportedByDeviceException
   {
      Address physicalRecord = new Address();
      physicalRecord.name = "<empty>";
      Address.addAddress(physicalRecord);
      AddressRecord ar = new PalmAddressRecord(Address.addressCount() - 1);
      records.addElement(ar);
      return ar;
   }

   /*
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see pimal.addressbook.AddressBook#getCategories()
    */
   public Vector getCategories() throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /*
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see pimal.addressbook.AddressBook#addCategory(java.lang.String)
    */
   public void addCategory(String category) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /*
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see pimal.addressbook.AddressBook#removeCategory(java.lang.String)
    */
   public void removeCategory(String category) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /*
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see pimal.addressbook.AddressBook#renameCategory(java.lang.String,
    *      java.lang.String)
    */
   public void renameCategory(String oldName, String newName) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /*
    * (non-Javadoc)
    *
    * @see pimal.addressbook.AddressBook#deleteAddressRecord(pimal.addressbook.AddressRecord)
    */
   public void deleteAddressRecord(AddressRecord addressRecord)
   {
      Address.delAddress(((PalmAddressRecord) addressRecord).getIndex());
      records.items[records.indexOf((Object) addressRecord)] = null;
   }
}
