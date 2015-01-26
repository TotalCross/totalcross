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



package totalcross.pim.ce.builtin;
/**
 * represents the eVC++ ITask interface of the Pocket Outlook Object Model
 * @author Fabian Kroeher
 *
 */
public class ITask extends IObject
{
   /** calls the constructor of superclass IObject */
   protected ITask(StringExt nativeString)
   {
      super(Constant.iTaskFields(), nativeString);
   }

   /* @see pimal.pocketpc.builtin.IObject#field(int)  */
   public String field(int position)
   {
      return Constant.iTaskFields(position);
   }

   /* @see pimal.pocketpc.builtin.IObject#fields() */
   public int fields()
   {
      return Constant.iTaskFields();
   }

   /* @see pimal.pocketpc.builtin.IObject#delete() */
   public void delete()
   {
      IPOutlookItemCollection.removeITask(getValue(field(0)));
   }

   /* @see pimal.pocketpc.builtin.IObject#refresh() */
   public void refresh()
   {
      StringExt nativeString = new StringExt(IPOutlookItemCollection.getITaskString(getValue(field(0))));
      this.reset();
      parseNativeString(nativeString);
   }

   /* @see pimal.pocketpc.builtin.IObject#save() */
   public void save()
   {
      String []fields = Constant.iTaskFieldRange(0,13);
      IRecurrencePattern f = getIRecurrencePattern(fields[7]);
      int count = IPOutlookItemCollection.editITask(getValue(fields[0]), // String restriction
                                         getValue(fields[1]), // String subject
                                         getValue(fields[2]), // String categories
                                         getValue(fields[3]), // String startDate
                                         getValue(fields[4]), // String dueDate
                                         getValue(fields[5]), // String importance
                                         getValue(fields[6]), // String completed
                                         f==null?"":f.getIsRecurring(), // String isRecurring
                                         f==null?"":f.getDuration(),         // String duration
                                         f==null?"":f.getRecurrenceType(),   // String recurrenceType
                                         f==null?"":f.getOccurrences(),      // String occurrences
                                         f==null?"":f.getInterval(),         // String interval
                                         f==null?"":f.getDayOfWeek(),        // String dayOfWeek
                                         f==null?"":f.getDayOfMonth(),       // String dayOfMonth
                                         f==null?"":f.getWeekOfMonth(),      // String weekOfMonth
                                         f==null?"":f.getMonthOfYear(),      // String monthOfYear
                                         f==null?"":f.getPatternStartDate(), // String patternStartDate
                                         f==null?"":f.getPatternEndDate(),   // String patternEndDate
                                         f==null?"":f.getStartTime(),        // String startTime
                                         f==null?"":f.getEndTime(),          // String endTime
                                         f==null?"":f.getNoEndDate(),        // String noEndDate
                                         getValue(fields[8]),                // String sensitivity
                                         getValue(fields[9]),                // String teamTask
                                         getValue(fields[10]),               // String reminderSet
                                         getValue(fields[11]),               // String reminderOptions
                                         getValue(fields[12]),               // String reminderTime
                                         getValue(fields[13])                // String note
                                         );
      if (count == 0)
         throw new RuntimeException("Could not save book "+getValue(fields[1]));
   }
}
