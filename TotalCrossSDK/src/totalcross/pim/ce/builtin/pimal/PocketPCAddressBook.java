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
import totalcross.pim.addressbook.*;
import totalcross.pim.*;
import totalcross.util.*;
/**
 * Implements the <code>totalcross.pim.addressbook.AddressBook</code> interface for PocketPC devices
 * @author Fabian Kroeher
 *
 */
public class PocketPCAddressBook implements AddressBook
{
   private Vector records;
   /**
    * creates a new instance of PocketPCAddressBook
    *
    */
   public PocketPCAddressBook()
   {
      records = new Vector();
   }
   /* (non-Javadoc)
   * @see totalcross.pim.addressbook.AddressBook#getAddressRecords()
   */
   public RecordList getAddressRecords()
   {
      // first, clear the records Vector
      records.removeAllElements();
      // now, get the Records from the device and store them in the record Vector
      IContacts contacts = IPOutlookItemCollection.viewIContacts();
      int n = contacts.size();
      for (int i = 0; i < n; i++)
         records.addElement(new PocketPCAddressRecord(contacts.iContactAt(i)));
      return new RecordList(records);
   }
   /* (non-Javadoc)
   * @see totalcross.pim.addressbook.AddressBook.AddressBook#createAddressRecord()
   */
   public AddressRecord createAddressRecord() throws NotSupportedByDeviceException
   {
      IContact tmp = IPOutlookItemCollection.createIContact();
      PocketPCAddressRecord ppcTmp = new PocketPCAddressRecord(tmp);
      records.addElement(ppcTmp);
      return ppcTmp;
   }
   /* (non-Javadoc)
   * @see totalcross.pim.addressbook.AddressBook.AddressBook#deleteAddressRecord(totalcross.pim.addressbook.AddressRecord)
   */
   public void deleteAddressRecord(AddressRecord addressRecord)
   {
      records.items[records.indexOf((Object)addressRecord)] = null;
      ((PocketPCAddressRecord)addressRecord).delete();
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
