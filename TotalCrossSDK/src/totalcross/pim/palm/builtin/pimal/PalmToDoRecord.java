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
import totalcross.pim.palm.builtin.*;
import totalcross.pim.todobook.*;
import totalcross.sys.*;
import totalcross.util.Vector;
/**
 * An implementation of interface ToDoRecord for PalmOS.
 * Currently supports the following fields:
 * SUMMARY, STATUS, DUE, PRIORITY, DESCRIPTION
 * @author Gilbert Fridgen
 */
public class PalmToDoRecord implements ToDoRecord
{
   private int index;
   private ToDoNotSupportedHandler nsh;
   /**
    * Creates a PalmToDoRecord from the given index
    * @param index the index, for which this Record should be created
    */
   protected PalmToDoRecord(int index)
   {
      this.index = index;
   }
   /*
   *  (non-Javadoc)
   * @see pimal.todobook.ToDoRecord#getToDoFields()
   */
   public Vector getFields()
   {
      ToDo physicalRecord = ToDo.getToDo(index);
      Vector todoFields = new Vector();
      ToDoField tdf;
      // SUMMARY
      String[] summ_value =
      {
         physicalRecord.description
      };
      String[] summ_options = {};
      tdf = new ToDoField(ToDoField.SUMMARY, summ_options, summ_value);
      todoFields.addElement(tdf);
      // DESCRIPTION
      String[] desc_value =
      {
         physicalRecord.note
      };
      String[] desc_options = {};
      tdf = new ToDoField(ToDoField.DESCRIPTION, desc_options, desc_value);
      todoFields.addElement(tdf);
      // STATUS
      String stat = "NEEDS ACTION";
      if (physicalRecord.completed)
         stat = "COMPLETED";
      String[] stat_value =
      {
         stat
      };
      String[] stat_options = {};
      tdf = new ToDoField(ToDoField.STATUS, stat_options, stat_value);
      todoFields.addElement(tdf);
      // DUE
      String date = ToDoField.toISO8601(physicalRecord.dueDate);
      if (date != null)
      {
         String[] due_value =
         {
            ToDoField.toISO8601(physicalRecord.dueDate)
         };
         String[] due_options = {};
         tdf = new ToDoField(ToDoField.DUE, due_options, due_value);
         todoFields.addElement(tdf);
      }
      // PRIORITY
      String[] prio_value =
      {
         "" + physicalRecord.priority
      };
      String[] prio_options = {};
      tdf = new ToDoField(ToDoField.PRIORITY, prio_options, prio_value);
      todoFields.addElement(tdf);
      if (nsh != null)
         return nsh.complete(this, todoFields);
      else
         return todoFields;
   }
   /*
   *  (non-Javadoc)
   * @see pimal.todobook.ToDoRecord#setToDoFields(totalcross.util.Vector)
   */
   public void setFields(Vector fields)
   {
      ToDo physicalRecord = ToDo.getToDo(index);
      Vector notSupported = new Vector();
      int summCount = 0, descCount = 0, prioCount = 0, dueCount = 0, statusCount = 0;
      int n = fields.size();
      for (int i = 0; i < n; i++)
      {
         ToDoField tdf = (ToDoField)fields.items[i];
         String[] values = tdf.getValues();
         switch(tdf.getKey())
         {
            case ToDoField.SUMMARY:
               if (summCount < 1)
               {
                  if (values.length >= 1)
                  { 
                     // check if array is long enough
                     physicalRecord.description = values[0];
                  }
                  else
                     break; // if array is malformed (too short) then ignore address field
               }
               else
                  notSupported.addElement(tdf);
               summCount++;
               break;

            case ToDoField.DESCRIPTION:
               if (descCount < 1)
               {
                  if (values.length >= 1)
                  { 
                     // check if array is long enough
                     physicalRecord.note = values[0];
                  }
                  else
                     break; // if array is malformed (too short) then ignore address field
               }
               else
                  notSupported.addElement(tdf);
               descCount++;
               break;

            case ToDoField.STATUS:
               if (statusCount < 1)
               {
                  if (values.length >= 1)
                  { 
                     // check if array is long enough
                     physicalRecord.completed = values[0].toUpperCase().equals("COMPLETED");
                  }
                  else
                     break; // if array is malformed (too short) then ignore address field
               }
               else
                  notSupported.addElement(tdf);
               statusCount++;
               break;

            case ToDoField.PRIORITY:
               if (prioCount < 1)
               {
                  if (values.length >= 1)
                  { 
                     // check if array is long enough
                     int prio = 1;
                     try {prio = totalcross.sys.Convert.toInt(values[0]);} catch (InvalidNumberException ine) {}
                     physicalRecord.priority = (prio < 1) ? 1 : (prio > 5) ? 5 : prio;
                  }
                  else
                     break; // if array is malformed (too short) then ignore address field
               }
               else
                  notSupported.addElement(tdf);
               prioCount++;
               break;

            case ToDoField.DUE:
               if (dueCount < 1)
               {
                  if (values.length >= 1)
                  { 
                     // check if array is long enough
                     physicalRecord.dueDate = ToDoField.parseISO8601(values[0]);
                  }
                  else
                     break; // if array is malformed (too short) then ignore address field
               }
               else
                  notSupported.addElement(tdf);
               dueCount++;
               break;

            default:
               notSupported.addElement(tdf);
               break;
         }
      }
      ToDo.changeToDo(index, physicalRecord);
      if (nsh != null)
         nsh.write(notSupported, this);
   }
   /**
    * Getter for index
    * @return index
    */
   protected int getIndex()
   {
      return index;
   }
   /*
   *  (non-Javadoc)
   * @see pimal.todobook.ToDoRecord#rawReadNote()
   */
   public String rawReadNote()
   {
      ToDo physicalRecord = ToDo.getToDo(index);
      return physicalRecord.note;
   }
   /*
   *  (non-Javadoc)
   * @see pimal.todobook.ToDoRecord#rawWriteNote(java.lang.String)
   */
   public void rawWriteNote(String note)
   {
      ToDo physicalRecord = ToDo.getToDo(index);
      physicalRecord.note = note;
      ToDo.changeToDo(index, physicalRecord);
   }
   /*
   *  (non-Javadoc)
   * @see pimal.todobook.ToDoRecord#registerNotSupportedhandler(pimal.notsupportedhandler.ToDoNotSupportedHandler)
   */
   public void registerNotSupportedhandler(ToDoNotSupportedHandler nsh)
   {
      this.nsh = nsh;
   }
}
