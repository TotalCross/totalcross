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



package totalcross.pim.palm.builtin.pimal;

import totalcross.pim.NotSupportedByDeviceException;
import totalcross.pim.RecordList;
import totalcross.pim.memobook.MemoBook;
import totalcross.pim.memobook.MemoRecord;
import totalcross.pim.palm.builtin.Memo;
import totalcross.util.Vector;

/**
 * Implementation of the MemoBook interface for PalmOS
 *
 * @author Gilbert Fridgen
 */
public class PalmMemoBook implements MemoBook
{
   private Vector records = new Vector();

   public PalmMemoBook() throws totalcross.io.IOException
   {
      Memo.initMemo();
   }

   /**
    * @see totalcross.pim.memobook.MemoBook#getMemoRecords()
    */
   public RecordList getMemoRecords()
   {
      int count = Memo.memoCount();
      records.removeAllElements();
      for (int i = 0; i < count; i++)
         if (Memo.getMemo(i) != null)
            records.addElement(new PalmMemoRecord(i));
      return new RecordList(records);
   }

   /**
    * @see totalcross.pim.memobook.MemoBook#createMemoRecord()
    */
   public MemoRecord createMemoRecord() throws NotSupportedByDeviceException
   {
      Memo physicalRecord = new Memo();
      physicalRecord.text = "<empty>";
      Memo.addMemo(physicalRecord);
      MemoRecord am = new PalmMemoRecord(Memo.memoCount() - 1);
      records.addElement(am);
      return am;
   }

   /**
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see totalcross.pim.memobook.MemoBook#getCategories()
    */
   public Vector getCategories() throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /**
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see totalcross.pim.memobook.MemoBook#addCategory(java.lang.String)
    */
   public void addCategory(String category) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /**
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see totalcross.pim.memobook.MemoBook#removeCategory(java.lang.String)
    */
   public void removeCategory(String category) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /**
    * Always throws <code>NotSupportedByDeviceException</code> since
    * categories are currently not supported
    *
    * @see totalcross.pim.memobook.MemoBook#renameCategory(java.lang.String,
    *      java.lang.String)
    */
   public void renameCategory(String oldName, String newName) throws NotSupportedByDeviceException
   {
      throw new NotSupportedByDeviceException("The Palm interface doesn't provide access to categories!");
   }

   /**
    * @see totalcross.pim.memobook.MemoBook#deleteMemoRecord(totalcross.pim.memobook.MemoRecord)
    */
   public void deleteMemoRecord(MemoRecord memoRecord)
   {
      Memo.delMemo(((PalmMemoRecord) memoRecord).getIndex());
      records.items[records.indexOf((Object) memoRecord)] = null;
   }
}
