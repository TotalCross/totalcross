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
import totalcross.util.*;
import totalcross.sys.*;
import totalcross.pim.datebook.*;
import totalcross.pim.palm.builtin.*;

/**
 * An implementation of interface DateRecord for PalmOS.
 * Currently supports the following fields:
 * SUMMARY, DTSTART, DTEND, TRIGGER, EXDATE, RRULE, DESCRIPTION
 * @author Gilbert Fridgen
 */
public class PalmDateRecord implements DateRecord
{
   private int index;
   private DateNotSupportedHandler nsh;
   /**
    * Creates a PalmDateRecord from the given index
    * @param index the index, for which this Record should be created
    */
   protected PalmDateRecord(int index)
   {
      this.index = index;
   }
   /**
    * Getter for index
    * @return index
    */
   protected int getIndex()
   {
      return index;
   }
   public Vector getFields()
   {
      Datebook physicalRecord = Datebook.getDate(index);
      Vector dateFields = new Vector();
      DateField df;
      // SUMMARY
      String[] summ_value =
      {
         physicalRecord.description
      };
      String[] summ_options = {};
      df = new DateField(DateField.SUMMARY, summ_options, summ_value);
      dateFields.addElement(df);
      // DTSTART
      String[] start_value =
      {
         DateField.toISO8601(physicalRecord.startDate)
      };
      String[] start_options = {};
      df = new DateField(DateField.DTSTART, start_options, start_value);
      dateFields.addElement(df);
      // DTEND
      if (physicalRecord.endTime != null)
      {
         Time endDate = new Time();
         // Date information is the same as of startDate
         endDate.day = physicalRecord.startDate.day;
         endDate.month = physicalRecord.startDate.month;
         endDate.year = physicalRecord.startDate.year;
         // time information is new
         endDate.hour = physicalRecord.endTime.hour;
         endDate.minute = physicalRecord.endTime.minute;
         endDate.second = physicalRecord.endTime.second;
         String[] end_value =
         {
            DateField.toISO8601(endDate)
         };
         String[] end_options = {};
         df = new DateField(DateField.DTEND, end_options, end_value);
         dateFields.addElement(df);
      }
      // TRIGGER
      if (physicalRecord.alarmAdvance != -1)
      {
         String alarm;
         if (physicalRecord.alarmUnits == Datebook.ALARM_DAYS)
            alarm = DateField.toDuration(false, physicalRecord.alarmAdvance, 0, 0, 0, 0);
         else
         if (physicalRecord.alarmUnits == Datebook.ALARM_HOURS)
            alarm = DateField.toDuration(false, 0, physicalRecord.alarmAdvance, 0, 0, 0);
         else
         if (physicalRecord.alarmUnits == Datebook.ALARM_MINUTES)
            alarm = DateField.toDuration(false, 0, 0, physicalRecord.alarmAdvance, 0, 0);
         else
            alarm = DateField.toDuration(false, 0, 0, 0, physicalRecord.alarmAdvance, 0);
         String[] trigger_value =
         {
            alarm
         };
         String[] trigger_options = {};
         df = new DateField(DateField.TRIGGER, trigger_options, trigger_value);
         dateFields.addElement(df);
      }
      // RECURRENCES
      // EXDATEs
      if (physicalRecord.exceptions != null && physicalRecord.exceptions.length > 0)
      {
         String[] exdate_value = new String[physicalRecord.exceptions.length];
         for (int i = 0;i > exdate_value.length;i++)
            exdate_value[i] = DateField.toISO8601(physicalRecord.exceptions[i]);
      }
      // RRULE
      if (physicalRecord.repeatType != Datebook.REPEAT_NONE)
      {
         Vector rrule_values = new Vector();
         switch(physicalRecord.repeatType)
         {
            case Datebook.REPEAT_DAILY:
               rrule_values.addElement("FREQ=DAILY");
               break;

            case Datebook.REPEAT_WEEKLY:
               rrule_values.addElement("FREQ=WEEKLY");
               if (physicalRecord.repeatWeekStartsOnMonday)
                  rrule_values.addElement("WKST=MO");
               else
                  rrule_values.addElement("WKST=SU");
               StringBuffer weekdays = new StringBuffer(16);
               for (int i = 0;i < physicalRecord.repeatWeekdays.length;i++)
               {
                  if (physicalRecord.repeatWeekdays[i])
                     weekdays.append(",").append(intToDay(i));
               }
               weekdays.delete(0, 0); //deleting first comma
               rrule_values.addElement("BYDAY=" + weekdays.toString());
               break;

            case Datebook.REPEAT_MONTHLY_BY_DATE:
               rrule_values.addElement("FREQ=MONTHLY");
               break;

            case Datebook.REPEAT_MONTHLY_BY_DAY:
               rrule_values.addElement("FREQ=MONTHLY");
               rrule_values.addElement("BYDAY=" + physicalRecord.repeatMonthlyCount + intToDay(physicalRecord.repeatMonthlyDay));
               break;

            case Datebook.REPEAT_YEARLY:
               rrule_values.addElement("FREQ=YEARLY");
               break;
         }
         int interval = physicalRecord.repeatFrequency;
         if (interval > 1)
            rrule_values.addElement("INTERVAL=" + interval);
         if (physicalRecord.repeatEndDate != null)
            rrule_values.addElement("UNTIL=" + DateField.toISO8601(physicalRecord.repeatEndDate));
         String[] rrule_options = {};
         df = new DateField(DateField.RRULE, rrule_options, (String[])rrule_values.toObjectArray());
         dateFields.addElement(df);
      }
      // DESCRIPTION
      String[] desc_value =
      {
         physicalRecord.note
      };
      String[] desc_options = {};
      df = new DateField(DateField.DESCRIPTION, desc_options, desc_value);
      dateFields.addElement(df);
      if (nsh != null)
         return nsh.complete(this, dateFields);
      else
         return dateFields;
   }
   public void setFields(Vector fields)
   {
      Datebook physicalRecord = Datebook.getDate(index);
      Vector notSupported = new Vector();
      int summCount = 0, startCount = 0, endCount = 0, triggerCount = 0, exdateCount = 0, rruleCount = 0, descCount = 0;
      int n = fields.size();
      for (int i = 0; i < n; i++)
      {
         DateField df = (DateField)fields.items[i];
         String[] values = df.getValues();
         switch(df.getKey())
         {
            case DateField.SUMMARY:
               if (summCount < 1)
               {
                  if (values.length > 0)
                  { 
                     // check if array is long enough
                     physicalRecord.description = values[0];
                  }
                  else
                     break; // if array is malformed (too short) then ignore field
               }
               else
                  notSupported.addElement(df);
               summCount++;
               break;

            case DateField.DTSTART:
               if (startCount < 1)
               {
                  if (values.length > 0)
                  { 
                     // check if array is long enough
                     physicalRecord.startDate = DateField.parseISO8601(values[0]);
                  }
                  else
                     break; // if array is malformed (too short) then ignore field
               }
               else
                  notSupported.addElement(df);
               startCount++;
               break;

            case DateField.DTEND:
               if (endCount < 1)
               {
                  if (values.length > 0)
                  { 
                     // check if array is long enough
                     physicalRecord.endTime = DateField.parseISO8601(values[0]); // TODO check, if end time is before startDate
                  }
                  else
                     break; // if array is malformed (too short) then ignore field
               }
               else
                  notSupported.addElement(df);
               endCount++;
               break;

            case DateField.TRIGGER:
               if (triggerCount < 1)
               {
                  if (values.length > 0)
                  {
                     // check if array is long enough
                     int[] dur = DateField.parseDuration(values[0]);
                     if (dur[5] != 0)
                     {
                        // Weeks are given
                        physicalRecord.alarmUnits = Datebook.ALARM_DAYS;
                        physicalRecord.alarmAdvance = dur[5] * 7 + dur[1];
                     }
                     else
                     if (dur[1] != 0)
                     {
                        // Days are given
                        physicalRecord.alarmUnits = Datebook.ALARM_DAYS;
                        physicalRecord.alarmAdvance = dur[1];
                     }
                     else
                     if (dur[2] != 0)
                     {
                        // Hours are given
                        physicalRecord.alarmUnits = Datebook.ALARM_HOURS;
                        physicalRecord.alarmAdvance = dur[2];
                     }
                     else
                     if (dur[3] != 0)
                     {
                        // Minutes are given
                        physicalRecord.alarmUnits = Datebook.ALARM_MINUTES;
                        physicalRecord.alarmAdvance = dur[3];
                     }
                     else
                     if (dur[4] != 0)
                     {
                        // Seconds are given
                        physicalRecord.alarmUnits = Datebook.ALARM_MINUTES;
                        physicalRecord.alarmAdvance = 1; // 1 Minute, seconds are not available in palmos
                     }
                     else
                        physicalRecord.alarmAdvance = -1; // no alarm
                  }
                  else
                     break; // if array is malformed (too short) then ignore field
               }
               else
                  notSupported.addElement(df);
               triggerCount++;
               break;

            case DateField.EXDATE:
               if (exdateCount < 1)
               {
                  if (values.length > 0)
                  { 
                     // check if array is long enough
                     Time[] exceptions = new Time[values.length];
                     for (int j = 0;i > exceptions.length;j++)
                        exceptions[j] = DateField.parseISO8601(values[j]);
                     physicalRecord.exceptions = exceptions;
                  }
                  else
                     break; // if array is malformed (too short) then ignore field
               }
               else
                  notSupported.addElement(df);
               exdateCount++;
               break;

            case DateField.RRULE:
               if (rruleCount < 1)
               {
                  if (values.length > 0)
                  { 
                     // check if array is long enough
                     String freq = null;
                     String until = null;
                     String interval = null;
                     String byday = null;
                     String wkst = null;
                     for (int j = 0;j < values.length;j++)
                     {
                        if (values[j].startsWith("FREQ="))
                           freq = getValue(values[j]);
                        else
                        if (values[j].startsWith("UNTIL="))
                           until = getValue(values[j]);
                        else
                        if (values[j].startsWith("INTERVAL="))
                           interval = getValue(values[j]);
                        else
                        if (values[j].startsWith("BYDAY="))
                           byday = getValue(values[j]);
                        else
                        if (values[j].startsWith("WKST="))
                           wkst = getValue(values[j]);
                     }
                     if (freq == null)
                     {
                        physicalRecord.repeatType = Datebook.REPEAT_NONE;
                        break; // freq is always necessary!
                     }
                     // FREQ
                     if (freq.equals("DAILY"))
                        physicalRecord.repeatType = Datebook.REPEAT_DAILY;
                     else
                     if (freq.equals("WEEKLY"))
                     {
                        physicalRecord.repeatType = Datebook.REPEAT_WEEKLY;
                        if (wkst != null)
                        {
                           if (wkst.equals("MO"))
                              physicalRecord.repeatWeekStartsOnMonday = true;
                           else
                              physicalRecord.repeatWeekStartsOnMonday = false;
                        }
                        if (byday != null)
                        {
                           String[] st = Convert.tokenizeString(byday, ',');
                           if (!st.equals(byday))
                              for (int j =0; j < st.length; j++)
                              {
                                 String day = st[j];
                                 physicalRecord.repeatWeekdays[dayToInt(day)] = true;
                              }
                        }
                        else
                        {
                           //TODO quite hard without datergf: find out day of DTSTART and use it for weekly recurrence
                        }
                     }
                     else
                     if (freq.equals("MONTHLY"))
                     {
                        if (byday == null)
                        {
                           // => repeat montly by date
                           physicalRecord.repeatType = Datebook.REPEAT_MONTHLY_BY_DATE;
                        }
                        else
                        {
                           // => repeat monthly by day
                           physicalRecord.repeatType = Datebook.REPEAT_MONTHLY_BY_DAY;
                           physicalRecord.repeatMonthlyDay = dayToInt(byday.substring(byday.length() - 2));
                           try
                           {
                              physicalRecord.repeatMonthlyCount = Convert.toInt(byday.substring(0, byday.length() - 2));
                           } catch (InvalidNumberException ine) {physicalRecord.repeatMonthlyCount = 0;}
                        }
                     }
                     else
                     if (freq.equals("YEARLY"))
                        physicalRecord.repeatType = Datebook.REPEAT_YEARLY;
                     // INTERVAL
                     if (interval == null)
                        physicalRecord.repeatFrequency = 1;
                     else
                     {
                        int interv = 1;
                        try {interv = Convert.toInt(interval);} catch (InvalidNumberException ine) {}
                        if (interv > 1)
                           physicalRecord.repeatFrequency = interv;
                        else
                           physicalRecord.repeatFrequency = 1;
                     }
                     // UNTIL
                     if (until == null)
                        physicalRecord.repeatEndDate = null;
                     else
                        physicalRecord.repeatEndDate = DateField.parseISO8601(until);
                  }
                  else
                     break; // if array is malformed (too short) then ignore field
               }
               else
                  notSupported.addElement(df);
               rruleCount++;
               break;

            case DateField.DESCRIPTION:
               if (descCount < 1)
               {
                  if (values.length > 0)
                  {
                     // check if array is long enough
                     physicalRecord.note = values[0];
                  }
                  else
                     break; // if array is malformed (too short) then ignore field
               }
               else
                  notSupported.addElement(df);
               descCount++;
               break;

            default:
               notSupported.addElement(df);
               break;
         }
      }
      Datebook.changeDate(index, physicalRecord);
      if (nsh != null)
         nsh.write(notSupported, this);
   }
   public String rawReadNote()
   {
      Datebook physicalRecord = Datebook.getDate(index);
      return physicalRecord.note;
   }
   public void rawWriteNote(String note)
   {
      Datebook physicalRecord = Datebook.getDate(index);
      physicalRecord.note = note;
      Datebook.changeDate(index, physicalRecord);
   }
   private static String []int2day = {"SU","MO","TU","WE","TH","FR","SA","SU"};
   /**
    * Converts the palm integers for weekdays to abbreviated Strings
    * @param weekday
    * @return weekday-String (2 upper-case chars)
    */
   private String intToDay(int weekday)
   {
      try
      {
         return int2day[weekday];
      } catch (ArrayIndexOutOfBoundsException aioobe) {return "SU";} // guich: not sure if this is necessary
/*      switch(weekday)
      {
         case 0:
            return "SU";

         case 1:
            return "MO";

         case 2:
            return "TU";

         case 3:
            return "WE";

         case 4:
            return "TH";

         case 5:
            return "FR";

         case 6:
            return "SA";

         default:
            return "SU";
      }                 */
   }
   private static IntHashtable day2int = new IntHashtable(19);
   static
   {
      day2int.put("SU".hashCode(),0);
      day2int.put("MO".hashCode(),1);
      day2int.put("TU".hashCode(),2);
      day2int.put("WE".hashCode(),3);
      day2int.put("TH".hashCode(),4);
      day2int.put("FR".hashCode(),5);
      day2int.put("SA".hashCode(),6);
   }
   /**
    * Converts abbreviated weekdays to palm integers
    * @param weekday (2 upper-case chars)
    * @return weekday-int
    */
   private int dayToInt(String weekday)
   {
      if (weekday.length() >= 2)
      {
         int hc = weekday.substring(0,2).toUpperCase().hashCode();
         int i = -1;
         try {i = day2int.get(hc);} catch (ElementNotFoundException e) {}
         if (i >= 0)
            return i;
      }
      /*
      if (weekday.startsWith("SU"))
         return 0;
      if (weekday.startsWith("MO"))
         return 1;
      if (weekday.startsWith("TU"))
         return 2;
      if (weekday.startsWith("WE"))
         return 3;
      if (weekday.startsWith("TH"))
         return 4;
      if (weekday.startsWith("FR"))
         return 5;
      if (weekday.startsWith("SA"))
         return 6;*/
      return 0;
   }
   /**
    * Gets value from a String of type key=value
    * @param property String of the type key=value
    * @return value
    */
   private String getValue(String property)
   {
      if (property == null || property.indexOf('=') == -1 || property.endsWith("="))
         return null;
      else
         return property.substring(property.indexOf('=') + 1);
   }
   public void registerNotSupportedhandler(DateNotSupportedHandler nsh)
   {
      this.nsh = nsh;
   }
}
