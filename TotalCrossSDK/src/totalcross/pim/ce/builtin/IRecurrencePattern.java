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
 * represents the eVC++ interface IRecurrencePattern and is capable of converting its
 * data to a vCal representation
 * TODO implement convert method
 * @author Fabian Kroeher
 *
 */
public class IRecurrencePattern extends IExtended
{
   protected String isRecurring, duration, recurrenceType, occurrences, interval, dayOfWeek, dayOfMonth, weekOfMonth, monthOfYear, noEndDate;
   protected IDate patternStartDate, patternEndDate, startTime, endTime;
   /**
    * parses the given String and reads out all the attributes of this recurrence pattern
    * @param value
    */
   public IRecurrencePattern(String value)
   {
      super(value);
      this.isRecurring = "false";
   }
   /**
    * @return "true" or "false", depends on whether there is a recurrence or not
    */
   public String getIsRecurring()
   {
      return isRecurring;
   }
   /**
    * @return the duration of the recurrence patterm
    */
   public String getDuration()
   {
      return duration;
   }
   /**
    * @return the type of the recurrence pattern
    */
   public String getRecurrenceType()
   {
      return recurrenceType;
   }
   /**
    * @return the number of occurrences of this recurrence pattern
    */
   public String getOccurrences()
   {
      return occurrences;
   }
   /**
    * @return the interval of this recurrence pattern
    */
   public String getInterval()
   {
      return interval;
   }
   /**
    * @return depends on interval whether it returns "" or the day of the week of this recurrence pattern
    */
   public String getDayOfWeek()
   {
      return dayOfWeek;
   }
   /**
    * @return depends on the interval whether it returns "" or the day of the month of this recurrence pattern
    */
   public String getDayOfMonth()
   {
      return dayOfMonth;
   }
   /**
    * @return depends on the interval whether it returns "" or the week of the month of this recurrence pattern
    */
   public String getWeekOfMonth()
   {
      return weekOfMonth;
   }
   /**
    * @return depends on the interval whether it returns "" or the month of the year of this recurrence pattern
    */
   public String getMonthOfYear()
   {
      return monthOfYear;
   }
   /**
    * @return returns the startDate of this pattern
    */
   public String getPatternStartDate()
   {
      return patternStartDate.toString();
   }
   /**
    * @return returns the endDate of this pattern
    */
   public String getPatternEndDate()
   {
      return patternEndDate.toString();
   }
   /**
    * @return the time on which the recurring Item starts every time
    */
   public String getStartTime()
   {
      return startTime.toString();
   }
   /**
    * @return the time on which the recurring Item ends every time
    */
   public String getEndTime()
   {
      return endTime.toString();
   }
   /**
    * @return "true" or "false", depends on whether the recurrence is eternal or not
    */
   public String getNoEndDate()
   {
      return noEndDate;
   }
}
