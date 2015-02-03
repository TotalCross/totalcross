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
import totalcross.pim.memobook.*;
import totalcross.pim.palm.builtin.*;
import totalcross.util.Vector;

/**
 * An implementation of interface MemoRecord for PalmOS.
 * Currently supports the following fields:
 * SUMMARY, MEMO
 * @author Gilbert Fridgen
 */
public class PalmMemoRecord implements MemoRecord
{
   int index;
   private MemoNotSupportedHandler nsh;
   /**
    * Creates a PalmMemoRecord from the given index
    * @param index the index, for which this Record should be created
    */
   protected PalmMemoRecord(int index)
   {
      this.index = index;
   }
   /**
    * @see totalcross.pim.VersitRecord#getFields()
    */
   public Vector getFields()
   {
      Memo physicalRecord = Memo.getMemo(index);
      Vector fields = new Vector();
      String[] memo_options = {};
      String[] memo_values =
      {
         physicalRecord.text
      };
      fields.addElement(new MemoField(MemoField.MEMO, memo_options, memo_values));
      if (nsh != null)
         return nsh.complete(this, fields); // completed by notsupportedhanlder
      else
         return fields;
   }
   /**
    * @see totalcross.pim.VersitRecord#setFields(totalcross.util.Vector)
    */
   public void setFields(Vector fields)
   {
      Memo physicalRecord = Memo.getMemo(index);
      String summary = "";
      String memo = "";
      int n = fields.size();
      for (int i = 0; i < n; i++)
      {
         MemoField mf = (MemoField)fields.items[i];
         switch(mf.getKey())
         {
            case MemoField.SUMMARY:
               if (mf.getValues().length > 0)
                  summary = mf.getValues()[0];
               break;

            case MemoField.MEMO:
               if (mf.getValues().length > 0)
                  memo = mf.getValues()[0];
               break;
         }
      }
      physicalRecord.text = memo;
      if (!(summary == null || summary.length() == 0))
         physicalRecord.text = summary + "\n" + physicalRecord.text;
      Memo.changeMemo(index, physicalRecord);
   }
   /**
    * Getter for index
    * @return index
    */
   protected int getIndex()
   {
      return index;
   }
   /**
    * @see totalcross.pim.memobook.MemoRecord#rawReadNote()
    */
   public String rawReadNote()
   {
      return "";
   }
   /**
    * @see totalcross.pim.memobook.MemoRecord#rawWriteNote(java.lang.String)
    */
   public void rawWriteNote(String note)
   {

   }
   /**
    */
   public void registerNotSupportedhandler(MemoNotSupportedHandler nsh)
   {
      this.nsh = nsh;
   }
}
