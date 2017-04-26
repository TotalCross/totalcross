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
import totalcross.pim.datebook.DateBook;
import totalcross.pim.datebook.DateRecord;
import totalcross.pim.palm.builtin.Datebook;
import totalcross.util.Vector;

/**
 * Implementation of the DateBook interface for PalmOS
 *
 * @author fridgegi
 *
 */
public class PalmDateBook implements DateBook
{
   private Vector records = new Vector();

   public PalmDateBook() throws totalcross.io.IOException
   {
      Datebook.initDatebook();
   }

   /*
    * (non-Javadoc)
    *
    * @see pimal.datebook.DateBook#getDateRecords()
    */
   public RecordList getDateRecords()
   {
      records.removeAllElements();
      int n = Datebook.datebookCount();
      for (int i = 0; i < n; i++)
         if (Datebook.getDate(i) != null)
            records.addElement(new PalmDateRecord(i));
      return new RecordList(records);
   }

   /*
    * (non-Javadoc)
    *
    * @see pimal.datebook.DateBook#createDateRecord()
    */
   public DateRecord createDateRecord() throws NotSupportedByDeviceException
   {
      Datebook physicalRecord = new Datebook();
      physicalRecord.description = "<empty>";
      Datebook.addDate(physicalRecord);
      DateRecord dr = new PalmDateRecord(Datebook.datebookCount() - 1);
      records.addElement(dr);
      return dr;
   }

   /*
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see pimal.datebook.DateBook#getCategories()
    */
   public Vector getCategories() throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /*
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see pimal.datebook.DateBook#addCategory(java.lang.String)
    */
   public void addCategory(String category) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /*
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see pimal.datebook.DateBook#removeCategory(java.lang.String)
    */
   public void removeCategory(String category) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /*
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see pimal.datebook.DateBook#renameCategory(java.lang.String,
    *      java.lang.String)
    */
   public void renameCategory(String oldName, String newName) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /*
    * (non-Javadoc)
    *
    * @see pimal.datebook.DateBook#deleteDateRecord(pimal.datebook.DateRecord)
    */
   public void deleteDateRecord(DateRecord dateRecord)
   {
      Datebook.delDate(((PalmDateRecord) dateRecord).getIndex());
      records.items[records.indexOf((Object) dateRecord)] = null;
   }
}
