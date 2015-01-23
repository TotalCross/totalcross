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



package totalcross.pim;
import totalcross.sys.*;
/**
 * Abstract superclass for Field-classes that use the vCal standard. Contains constants according to the keys, the vCalendar standard requests.
 * Please refer to rfc2445 for more information
 * @author Gilbert Fridgen
 */
public abstract class VCalField extends VersitField
{
   // Descriptive component properties
   public static final int ATTACH = 4080101;
   public static final int CATEGORIES = 4080102;
   public static final int CLASSIFICATION = 4080103;
   public static final int COMMENT = 4080104;
   public static final int DESCRIPTION = 4080105;
   public static final int GEO = 4080106;
   public static final int LOCATION = 4080107;
   public static final int PERCENT_COMPLETE = 4080108;
   public static final int PRIORITY = 4080109;
   public static final int RESOURCES = 4080110;
   public static final int STATUS = 4080111;
   public static final int SUMMARY = 4080112;
   // Date and time component properties
   public static final int COMPLETED = 4080201;
   public static final int DTEND = 4080202;
   public static final int DUE = 4080203;
   public static final int DTSTART = 4080204;
   public static final int DURATION = 4080205;
   public static final int FREEBUSY = 4080206;
   public static final int TRANSP = 4080207;
   // Time Zone Component Properties
   public static final int TZID = 4080301;
   public static final int TZNAME = 4080302;
   public static final int TZOFFSETFROM = 4080303;
   public static final int TZOFFSETTO = 4080304;
   public static final int TZURL = 4080305;
   // Relationship component Properties
   public static final int ATTENDEE = 4080401;
   public static final int CONTACT = 4080402;
   public static final int ORGANIZER = 4080403;
   public static final int RECURRENCE_ID = 4080404;
   public static final int RELATED_TO = 4080405;
   public static final int URL = 4080406;
   public static final int UID = 4080407;
   // Recurrence component properties
   public static final int EXDATE = 4080501;
   public static final int EXRULE = 4080502;
   public static final int RDATE = 4080503;
   public static final int RRULE = 4080504;
   // Alarm component properties
   public static final int ACTION = 4080601;
   public static final int REPEAT = 4080602;
   public static final int TRIGGER = 4080603;
   // Change management component properties
   public static final int CREATED = 4080701;
   public static final int DTSTAMP = 4080702;
   public static final int LAST_MODIFIED = 4080703;
   public static final int SEQUENCE = 4080704;
   // Miscellaneous component properties
   public static final int REQUEST_STATUS = 4080802;
   /**
    * @param key vCalendar key according to rfc2445
    * @param options vCalendar options according to rfc2445, format: option=value
    * @param values vCalendar values according to rfc244f
    */
   public VCalField(int key, String[] options, String[] values)
   {
      super(key, options, values);
   }
   /**
    * Parses an object of class Time from a String
    * @param time String time, in format <i>YYYYMMDD</i>T<i>hhmmss</i>Z (ISO8601, local time)
    * @return Time as Time object
    */
   public static Time parseISO8601(String time)
   {
      if (time == null || time.length() < 15)
         return null;
      Time t = new Time();
      try
      {
         t.year = Convert.toInt(time.substring(0, 4));
         t.month = Convert.toInt(time.substring(4, 6));
         t.day = Convert.toInt(time.substring(6, 8));
         t.hour = Convert.toInt(time.substring(9, 11));
         t.minute = Convert.toInt(time.substring(11, 13));
         t.second = Convert.toInt(time.substring(13, 15));
      } catch (InvalidNumberException ine) {}
      return t;
   }
   /**
    * Generates String representation of an object of class <code>Time</code>
    * @param t the time to convert
    * @return String of format <i>YYYYMMDD</i>T<i>hhmmss</i>Z (ISO8601, local time)
    */
   public static String toISO8601(Time t)
   {
      if (t == null)
         return null;
      StringBuffer time = new StringBuffer(16);
      time.append(t.year);
      if (t.month < 10)
         time.append('0');
      time.append(t.month);
      if (t.day < 10)
         time.append('0');
      time.append(t.day);
      time.append('T');
      if (t.hour < 10)
         time.append('0');
      time.append(t.hour);
      if (t.minute < 10)
         time.append('0');
      time.append(t.minute);
      if (t.second < 10)
         time.append('0');
      time.append(t.second);
      time.append('Z');
      return time.toString();
   }
   /**
    * Creates a String for a duration according to rfc2445 (iCal specification).
    * The most important issue is the fact, that if you specify the value "weeks" other than 0, the other values will be ignored!
    * All integer parameters must be positive.
    * @param positive is the duration going to the future (true) or to the past (false)
    * @param days duration in days
    * @param hours duration in hours
    * @param minutes duration in minutes
    * @param seconds duration in seconds
    * @param weeks duration in weeks
    * @return String according to rfc2445
    */
   public static String toDuration(boolean positive, int days, int hours, int minutes, int seconds, int weeks)
   {
      // bringing time to "normal" values
      minutes += seconds / 60;
      seconds = seconds % 60;
      hours += minutes / 60;
      minutes = minutes % 60;
      days += hours / 24;
      hours = hours % 24;
      //Building buffer
      StringBuffer sb = new StringBuffer(positive ? "P" : "-P");
      if (weeks > 0)
         return sb.append(weeks).append("W").toString();
      if (days > 0)
         sb.append(days).append("D");
      boolean hoursOk = hours >= 0 && hours < 24;
      boolean minutesOk = minutes >= 0 && minutes < 60;
      boolean secondsOk = seconds >= 0 && seconds < 60;
      if (hoursOk || minutesOk || secondsOk)
      {
         sb.append("T");
         if (hoursOk)
            sb.append(hours).append("H");
         if (minutesOk)
            sb.append(minutes).append("M");
         if (secondsOk)
            sb.append(seconds).append("S");
      }
      return sb.toString();
   }
   /**
    * Parses a String for a duration accourding to rfc2445 and returns it's values
    * @param s  duration in the format specified by rfc2445, e.g. generated by the method toDuration()
    * @return array of the format { pos/neg (1/-1) ; days ; hours ; minutes ; seconds ; weeks }
    */
   public static int[] parseDuration(String s)
   {
      int[] dur = new int[6];
      if (s.startsWith("-"))
      {
         dur[0] = -1; // found negative
         s = s.substring(2); // removing "-" and "P"
      }
      else
      {
         dur[0] = 1; // found positive
         if (s.startsWith("+"))
            s = s.substring(2); // removing "+" and "P"
         else
            s = s.substring(1); // removing "P"
      }
      try
      {
         int index = s.indexOf('W');
         if (index != -1)
         {
            dur[5] = Convert.toInt(s.substring(0, index));
            return dur; // we're done
         }
         index = s.indexOf('D');
         if (index != -1)
         {
            dur[1] = Convert.toInt(s.substring(0, index));
            if (s.endsWith("D"))
               return dur; // we're done
            s = s.substring(index + 1);
         }
         index = s.indexOf('H');
         if (index != -1)
         {
            dur[2] = Convert.toInt(s.substring(0, index));
            if (s.endsWith("H"))
               return dur; // we're done
            s = s.substring(index + 1);
         }
         index = s.indexOf('M');
         if (index != -1)
         {
            dur[3] = Convert.toInt(s.substring(0, index));
            if (s.endsWith("M"))
               return dur; // we're done
            s = s.substring(index + 1);
         }
         index = s.indexOf('S');
         if (index != -1)
         {
            dur[4] = Convert.toInt(s.substring(0, index));
            return dur; // we're done
         }
      } catch (InvalidNumberException ine) {Vm.debug(ine.getMessage());}
      return dur;
   }
   /* (non-Javadoc)
   * @author Kathrin Braunwarth
   * @see java.lang.Object#toString()
   */
   public String toString()
   {
      String asString = "";
      switch(key)
      {
         case VCalField.ATTACH:
            asString = "ATTACH";
            break;

         case VCalField.CATEGORIES:
            asString = "CATEGORIES";
            break;

         case VCalField.CLASSIFICATION:
            asString = "CLASSIFICATION";
            break;

         case VCalField.COMMENT:
            asString = "COMMENT";
            break;

         case VCalField.DESCRIPTION:
            asString = "DESCRIPTION";
            break;

         case VCalField.GEO:
            asString = "GEO";
            break;

         case VCalField.LOCATION:
            asString = "LOCATION";
            break;

         case VCalField.PERCENT_COMPLETE:
            asString = "PERCENT_COMPLETE";
            break;

         case VCalField.PRIORITY:
            asString = "PRIORITY";
            break;

         case VCalField.RESOURCES:
            asString = "RESOURCES";
            break;

         case VCalField.STATUS:
            asString = "STATUS";
            break;

         case VCalField.SUMMARY:
            asString = "SUMMARY";
            break;

         case VCalField.COMPLETED:
            asString = "COMPLETED";
            break;

         case VCalField.DTEND:
            asString = "DTEND";
            break;

         case VCalField.DUE:
            asString = "DUE";
            break;

         case VCalField.DTSTART:
            asString = "DTSTART";
            break;

         case VCalField.DURATION:
            asString = "DURATION";
            break;

         case VCalField.FREEBUSY:
            asString = "FREEBUSY";
            break;

         case VCalField.TRANSP:
            asString = "TRANSP";
            break;

         case VCalField.TZID:
            asString = "TZID";
            break;

         case VCalField.TZNAME:
            asString = "TZNAME";
            break;

         case VCalField.TZOFFSETFROM:
            asString = "TZOFFSETFROM";
            break;

         case VCalField.TZOFFSETTO:
            asString = "TZOFFSETTO";
            break;

         case VCalField.TZURL:
            asString = "TZURL";
            break;

         case VCalField.ATTENDEE:
            asString = "ATTENDEE";
            break;

         case VCalField.CONTACT:
            asString = "CONTACT";
            break;

         case VCalField.ORGANIZER:
            asString = "ORGANIZER";
            break;

         case VCalField.RECURRENCE_ID:
            asString = "RECURRENCE_ID";
            break;

         case VCalField.RELATED_TO:
            asString = "RELATED_TO";
            break;

         case VCalField.URL:
            asString = "URL";
            break;

         case VCalField.UID:
            asString = "UID";
            break;

         case VCalField.EXDATE:
            asString = "EXDATE";
            break;

         case VCalField.EXRULE:
            asString = "EXRULE";
            break;

         case VCalField.RDATE:
            asString = "RDATE";
            break;

         case VCalField.RRULE:
            asString = "RRULE";
            break;

         case VCalField.REPEAT:
            asString = "REPEAT";
            break;

         case VCalField.TRIGGER:
            asString = "TRIGGER";
            break;

         case VCalField.CREATED:
            asString = "CREATED";
            break;

         case VCalField.ACTION:
            asString = "ACTION";
            break;

         case VCalField.DTSTAMP:
            asString = "DTSTAMP";
            break;

         case VCalField.LAST_MODIFIED:
            asString = "LAST_MODIFIED";
            break;

         case VCalField.SEQUENCE:
            asString = "SEQUENCE";
            break;

         case VCalField.REQUEST_STATUS:
            asString = "REQUEST_STATUS";
            break;
      }
      return asString + super.toString();
   }
}
