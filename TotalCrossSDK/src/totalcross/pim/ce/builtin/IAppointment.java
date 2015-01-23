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
 * represents the eVC++ IAppointment interface of the Pocket Outlook Object Model
 * @author Fabian Kroeher
 *
 */
public class IAppointment extends IObject
{
   /**
    * calls the constructor of superclass IObject
    * @param nativeString
    */
   protected IAppointment(StringExt nativeString)
   {
      super(Constant.iAppointmentFields(), nativeString);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.builtin.IObject#field(int)
   */
   public String field(int position)
   {
      return Constant.iAppointmentFields(position);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.builtin.IObject#fields()
   */
   public int fields()
   {
      return Constant.iAppointmentFields();
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.builtin.IObject#delete()
   */
   public void delete()
   {
      IPOutlookItemCollection.removeIAppointment(getValue(field(0)));
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.builtin.IObject#refresh()
   */
   public void refresh()
   {
      StringExt nativeString = new StringExt(IPOutlookItemCollection.getIAppointmentString(getValue(field(0))));
      this.reset();
      parseNativeString(nativeString);
   }
   /* (non-Javadoc)
   * @see pimal.pocketpc.builtin.IObject#save()
   */
   public void save()
   {
      String []fields = Constant.iAppointmentFieldRange(0,17);
      IRecurrencePattern p15 = getIRecurrencePattern(fields[15]);
      int count = IPOutlookItemCollection.editIAppointment(getValue(fields[0]),
                                                getValue(fields[1]),
                                                getValue(fields[2]),
                                                getValue(fields[3]),
                                                getValue(fields[4]),
                                                getValue(fields[5]),
                                                getValue(fields[6]),
                                                getValue(fields[7]),
                                                getValue(fields[8]),
                                                getValue(fields[9]),
                                                getValue(fields[10]),
                                                getValue(fields[11]),
                                                getValue(fields[12]),
                                                getValue(fields[13]),
                                                getValue(fields[14]),
                                                p15==null?"":p15.getIsRecurring(),
                                                p15==null?"":p15.getRecurrenceType(),
                                                p15==null?"":p15.getOccurrences(),
                                                p15==null?"":p15.getInterval(),
                                                p15==null?"":p15.getDayOfWeek(),
                                                p15==null?"":p15.getDayOfMonth(),
                                                p15==null?"":p15.getWeekOfMonth(),
                                                p15==null?"":p15.getMonthOfYear(),
                                                p15==null?"":p15.getPatternStartDate(),
                                                p15==null?"":p15.getPatternEndDate(),
                                                p15==null?"":p15.getStartTime(),
                                                p15==null?"":p15.getEndTime(),
                                                p15==null?"":p15.getNoEndDate(),
                                                getValue(fields[16]),
                                                getValue(fields[17]));
      if (count == 0)
         throw new RuntimeException("Could not save book "+getValue(fields[1]));
   }
}
