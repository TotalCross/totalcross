// Copyright (C) 2000, 2001 Allan C. Solomon
// Copyright (C) 2000-2013 SuperWaba Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Time;

/**
 The Date class is a general date data type(Object) that is similar to those built in to other languages.
 It supports all days from January 1st, 1000 D.C. through December 31st, 2999. It checks to make sure that the dates
 that are instanciated or changed exist and if they don't an exception is thrown.  It provides
 methods to advance the date backwards and forwards by increments of day, week, and month.  It provides
 comparisons =,>,<.

 <b>Added by Allan C. Solomon</b> and modified by guich.
 @version 1.0 16 Aug 2000
 @author Allan C. Solomon
 */

public class Date implements Comparable {
  /** The month names used in some routines. You can localize it if you wish. */
  public static String monthNames[] = { "", "January", "February", "March", "April", "May", "June", "July", "August",
      "September", "October", "November", "December" }; // guich@230_15
  private int day;
  private int month;
  private int year;
  private static int epochYear = 1000;
  /** After constructing a date, can be used to verify if the date was valid */
  private static int[][] part2idx = { {}, { 1, 0, 2 }, { 0, 1, 2 }, { 2, 1, 0 } }; // guich@566_29

  /** Defines the minimum valid year. Any year before this date throws an InvalidDateException.
   * Defaults to 1000 DC */
  public static int minValidYear = epochYear;

  public static final int JANUARY = 1;
  public static final int FEBRUARY = 2;
  public static final int MARCH = 3;
  public static final int APRIL = 4;
  public static final int MAY = 5;
  public static final int JUNE = 6;
  public static final int JULY = 7;
  public static final int AUGUST = 8;
  public static final int SEPTEMBER = 9;
  public static final int OCTOBER = 10;
  public static final int NOVEMBER = 11;
  public static final int DECEMBER = 12;

  /** Used in the advance methods. */
  public static final boolean FORWARD = true;
  /** Used in the advance methods. */
  public static final boolean BACKWARD = false;

  /**  Constructs a Date object set to the current date. */
  public Date() {
    setToday();
  }

  /** Constructs a new Date object with the values from the given Time object */
  public Date(totalcross.sys.Time t) throws InvalidDateException // guich@350_24
  {
    this.day = t.day;
    this.month = t.month;
    this.year = t.year;
    verifyDate();
  }

  /** Sets the date fields to the given ones.
   * @since TotalCross 1.24
   * @return The date in the format YYYYMMDD
   */
  public int set(int day, int month, int year) throws InvalidDateException // guich@tc124_9
  {
    this.day = day;
    this.month = month;
    this.year = year;
    verifyDate();
    return year * 10000 + month * 100 + day;
  }

  /** Sets the date fields by parsing the given String, and using the dateFormat.
   * If you want to use the default date format, use <code>Settings.dateFormat</code>.
   * Trailing spaces are skipped.
   * @since TotalCross 1.24
   * @return The date in the format YYYYMMDD
   */
  public int set(String strDate, byte dateFormat) throws InvalidDateException {
    if (strDate == null || strDate.length() == 0) {
      throw new InvalidDateException(
          strDate == null ? "Error: date cannot be null" : "Error: date cannot have length 0");
    }
    try {
      int n = strDate.length(), j = 2;
      int p[] = new int[3];
      char c;
      int value = 0;
      int mult = 1;
      while (--n >= 0) // going back to front and computing the value
      {
        c = strDate.charAt(n);
        if (c <= ' ' && j == 2) {
          continue;
        }
        if ('0' <= c && c <= '9') {
          value += (c - '0') * mult;
          mult *= 10;
        } else {
          if (j == -1) {
            throw new InvalidDateException("Invalid date: " + strDate);
          }
          p[j--] = value;
          value = 0;
          mult = 1;
        }
      }
      p[j] = value;
      day = p[part2idx[dateFormat][0]];
      month = p[part2idx[dateFormat][1]];
      year = p[part2idx[dateFormat][2]];
    } catch (Exception ine) {
      day = -1;
    }
    verifyDate();
    return year * 10000 + month * 100 + day;
  }

  private void verifyDate() throws InvalidDateException // guich@552_13: wasDateValid must be set after calling setToday. Also, this code was very replicated.
  {
    if (0 <= year && year < 20) {
      year += 2000;
    } else if (20 <= year && year < 100) {
      year += 1900;
    }
    if (!(day > 0 && month >= 1 && month <= 12 && day <= getDaysInMonth() && year >= minValidYear && year < 3000)) {
      throw new InvalidDateException("Invalid date. Day: " + day + ", month: " + month + ", year: " + year);
    }
  }

  /** Constructs a Date object set to a passed string in the format specified in the dateFormat parameter. 
    The constructor auto-detects the separator. 
  
    @param strDate string that should have the format specified in the Settings.dateFormat. Note: does not have to be separated by
    the <b>'-'</b> character it can be separated by any non-number.
    @param dateFormat one of the Settings.DATE_ values.
    @see totalcross.sys.Settings#DATE_DMY
    @see totalcross.sys.Settings#DATE_MDY
    @see totalcross.sys.Settings#DATE_YMD
   */
  public Date(String strDate, byte dateFormat) throws InvalidDateException {
    set(strDate, dateFormat);
  }

  /** Constructs a Date object set to a passed string in the format specified in the current date format. The constructor auto-detects
    the separator. If an invalid date is passed, sets to the current date;
  
    @param strDate string that should have the format specified in the Settings.dateFormat. Note: does not have to be separated by
    the <b>'-'</b> character it can be separated by any non-number.
    @see totalcross.sys.Settings#dateFormat
   */
  public Date(String strDate) throws InvalidDateException {
    set(strDate, Settings.dateFormat);
  }

  /** Constructs a Date object set to the passed int in the YYYYMMDD format
  
   @param sentDate an integer in the YYYYMMDD format
   */
  public Date(int sentDate) throws InvalidDateException {
    if (sentDate < 10000101 || sentDate > 29991231) {
      throw new InvalidDateException("Argument sentDate (" + sentDate + ") must be in the format YYYYMMDD");
    }
    day = sentDate % 100;
    month = sentDate / 100 % 100;
    year = sentDate / 10000;
    verifyDate();
  }

  /** Constructs a Date object set to the passed day, month, and year.
  
   @param sentDay - an integer that must be between 1 and the last day in the month.
   @param sentMonth - an integer that must be between 1 and 12.
   @param sentYear - an integer that must be between 1000 and 2999.
   */
  public Date(int sentDay, int sentMonth, int sentYear) throws InvalidDateException {
    day = sentDay;
    month = sentMonth;
    year = sentYear;
    verifyDate();
  }

  /** Sets this date object to be the current day */
  public void setToday() {
    Time tTime = new Time();
    day = tTime.day;
    month = tTime.month;
    year = tTime.year;
  }

  /** Returns the day of week, where 0 is sunday and 6 is saturday.
  
   @return integer representation of day of week.  Integers refer to static constants of day of week.
   */
  public int getDayOfWeek() // guich@566_29: removed setDayOfWeek and placed code here.
  {
    int yy = year;
    int mm = month;
    int a = (yy - 1582) * 365;
    int b = (int) ((yy - 1581) / 4);
    int c = (int) ((yy - 1501) / 100);
    int d = (int) ((yy - 1201) / 400);
    int e = (mm - 1) * 31;
    int f = day;
    int g = (int) ((mm + 7) / 10);
    int h = (int) ((mm * 0.4f + 2.3f) * g);
    int i = (int) ((1 / ((yy % 4) + 1)) * g);
    int j = (int) ((1 / ((yy % 100) + 1)) * g);
    int k = (int) ((1 / ((yy % 400) + 1)) * g);
    int l = a + b - c + d + e + f - h + i - j + k + 5;
    int m = l % 7;
    return (int) ((m > 0) ? (m - 1) : 6);
  }

  /** Returns the day.
   @return integer value of day.
   */
  public int getDay() {
    return day;
  }

  /** Returns the month.
  
   @return integer value of the month.
   */
  public int getMonth() {
    return month;
  }

  /** Returns the year.
  
   @return integer value of the year.
   */
  public int getYear() {
    return year;
  }

  /** Calculates and returns the ordinal value of the week(1-52).
  
     @return integer representation of ordinal value of the week within the set year.
   */
  public int getWeek() {
    int weekNum = 0; // initialize with any value
    try {
      Date firstDay = new Date(1, 1, year); // find out first of year's day
      int gregorian = getGregorianDay() - firstDay.getGregorianDay() + 1; // Jan 1 = 1, Jan 2 = 2, etc...
      int dow = getDayOfWeek(); // Sun = 0, Mon = 1, etc...
      int dowJan1 = firstDay.getDayOfWeek();
      weekNum = ((gregorian + 6) / 7);
      if (dow < dowJan1) {
        ++weekNum;
      }
      if (weekNum > 53 || (weekNum > 52 && !(dowJan1 > (isLeapYear(this.year) ? 4 : 5)))) {
        weekNum = 1;
      }
    } catch (InvalidDateException e) {
      // should never happen
    }
    return weekNum;
  }

  /** Formats the day/month specified with the Settings.dateFormat, zero padded.*/
  public String formatDayMonth() {
    int i1 = day, i2 = month;
    if (Settings.dateFormat != Settings.DATE_DMY) {
      i1 = month;
      i2 = day;
    }

    return Convert.zeroPad(Convert.toString(i1), 2) + Settings.dateSeparator + Convert.zeroPad(Convert.toString(i2), 2);
  }

  /** Formats the date specified with the dateFormat parameter, zero padded, and using the given separator.
    @see totalcross.sys.Settings#DATE_DMY
    @see totalcross.sys.Settings#DATE_MDY
    @see totalcross.sys.Settings#DATE_YMD
    @since TotalCross 1.15
   */
  public static String formatDate(int day, int month, int year, byte dateFormat, String dateSeparator) // guich@tc115_22
  {
    int i1 = day, i2 = month, i3 = year;
    if (dateFormat == Settings.DATE_MDY) {
      i1 = month;
      i2 = day;
      i3 = year;
    } else if (dateFormat == Settings.DATE_YMD) {
      i1 = year;
      i2 = month;
      i3 = day;
    }

    return Convert.zeroPad(i1, 2) + dateSeparator + Convert.zeroPad(i2, 2) + dateSeparator + Convert.zeroPad(i3, 2);
  }

  /** Formats the date specified with the Settings.dateFormat, zero padded, and using the given separator.
   * @since TotalCross 1.15 
   */
  public static String formatDate(int day, int month, int year, String separator) // guich@tc115_22
  {
    return formatDate(day, month, year, Settings.dateFormat, separator);
  }

  /** Formats the date specified with the dateFormat parameter, zero padded.
   @see totalcross.sys.Settings#DATE_DMY
   @see totalcross.sys.Settings#DATE_MDY
   @see totalcross.sys.Settings#DATE_YMD
   */
  public static String formatDate(int day, int month, int year, byte dateFormat) {
    return formatDate(day, month, year, dateFormat, Convert.toString(Settings.dateSeparator));
  }

  /** Formats the date specified with the Settings.dateFormat, zero padded. */
  public static String formatDate(int day, int month, int year) {
    return formatDate(day, month, year, Settings.dateFormat);
  }

  /** Returns the date in a string format.
   @return string representation of the date in the current Settings.dateFormat.
   @deprecated use toString()
   */
  @Deprecated
  public String getDate() {
    return formatDate(day, month, year);
  }

  /** Returns the date in a string format.
   @return string representation of the date in the current Settings.dateFormat.
   */
  @Override
  public String toString() {
    return formatDate(day, month, year);
  }

  /** Returns the date in a string format and using the given format and separator.
   @return string representation of the date in the current Settings.dateFormat.
   @since TotalCross 1.15
   */
  public String toString(byte dateFormat, String separator) // guich@tc115_22
  {
    return formatDate(day, month, year, dateFormat, separator);
  }

  /** Returns the date as a string, in the given format. 
   @return string representation of the date in the current Settings.dateFormat
   @see totalcross.sys.Settings#DATE_DMY
   @see totalcross.sys.Settings#DATE_MDY
   @see totalcross.sys.Settings#DATE_YMD
   @since SuperWaba 5.70.
   */
  public String toString(byte format) // guich@570_85
  {
    return formatDate(day, month, year, format);
  }

  /** Returns the date in a integer format.
   @return integer representation of the date (year * 10000) + (month *100) + day
   */
  public int getDateInt() {
    return (year * 10000) + (month * 100) + day;
  }

  /** Returns number of days in the set month.
    @return integer containing number of days in set month.
   */
  public int getDaysInMonth() {
    return getDaysInMonth(month);
  }

  /** Returns number of days in the passed month of the current date's year.
    @param month integer between 1 and 12.
    @return integer containing number of days in passed month.
   */
  public int getDaysInMonth(int month) // 1 <= month <= 12
  {
    return getDaysInMonth(month, year);
  }

  static private byte monthDays[] = new byte[] { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

  /** Returns number of days in the passed month and year.
   @param month integer between 1 and 12.
   @param year integer with the year.
   @return integer containing number of days in passed month.
   */
  public static int getDaysInMonth(int month, int year) // guich@573_36
  {
    if (month != 2) {
      return monthDays[month];
    }
    boolean leap = ((year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0)));
    return leap ? 29 : 28;
  }

  /** Returns the string representation of the month passed
    @param m integer between 1 and 12.
    @return string representation of month passed.
   */
  public static String getMonthName(int m) {
    if (JANUARY <= m && m <= DECEMBER) {
      return monthNames[m];
    }
    return "";
  }

  /** Checks to see if the Date object passed occurs before the existing Date object.
    @param sentDate object to compare with existing.
    @return boolean stating whether or not it occurs before existing date.
   */
  public boolean isBefore(Date sentDate) {
    return (getDateInt() < sentDate.getDateInt());
  }

  /** Checks to see if the Date object passed occurs after the existing Date object.
    @param sentDate object to compare with existing.
    @return boolean stating whether or not it occurs after existing date.
   */
  public boolean isAfter(Date sentDate) {
    return (getDateInt() > sentDate.getDateInt());
  }

  /** Checks to see if the Date object passed occurs at the same time as the existing Date object.
    @param sentDate object to compare with existing.
    @return boolean stating whether or not it occurs at the same time as existing date.
   */
  @Override
  public boolean equals(Object sentDate) // guich@341_11
  {
    if (sentDate instanceof Date) {
      return (getDateInt() == ((Date) sentDate).getDateInt());
    } else {
      return sentDate == this;
    }
  }

  /** Advances the date to the beginning of the next week. */
  public void advanceWeek() {
    advanceWeek(FORWARD);
  }

  /** Advances the date to the beginning of the next or previous week.
    @param direction - static variables FORWARD or BACKWARD instructs the method to either move to the next
    or previous week
   */
  public void advanceWeek(boolean direction) {
    advance(-1 * getDayOfWeek());
    advance(direction ? 7 : -7);
  }

  /** Advances the date to the beginning of the next month. */
  public void advanceMonth() {
    advanceMonth(FORWARD);
  }

  /** Advances the date to the beginning of the next or previous month.
    @param direction - static variables FORWARD or BACKWARD instructs the method to either move to the next
    or previous month
   */
  public void advanceMonth(boolean direction) {
    if (direction) {
      month++;
      if (month == 13) {
        year++;
        month = 1;
      }
    } else {
      month--;
      if (month == 0) {
        year--;
        month = 12;
      }
    }
    day = 1;
  }

  /** Advances the date by a passed integer.
   * 
   * @param numberDays integer containing number of days that the date should change can be positive or negative
   */
  public void advance(int numberDays) // flsobral@tc100: advance was calculating leap years based on the julian calendar, created the method isLeapYear to fix it.
  {
    int numberofDays = getGregorianDay() + numberDays;

    int i = epochYear;
    while (numberofDays >= 366) {
      if ((numberofDays == 366) && isLeapYear(i)) {
        break;
      }
      if (isLeapYear(i)) {
        numberofDays -= 366;
      } else {
        numberofDays -= 365;
      }
      i++;
    }

    year = i;
    for (i = 1; numberofDays > getDaysInMonth(i); i++) {
      numberofDays -= getDaysInMonth(i);
    }
    month = (i);
    day = numberofDays;
  }

  private int getDaysFromMonth() {
    int numberofDays = 0;
    for (int i = month - 1; i >= 1; i--) {
      numberofDays += getDaysInMonth(i);
    }
    return numberofDays;
  }

  /** Returns the number of days since the January 1 of the epoch year (1000). */
  public int getGregorianDay() // flsobral@tc100b4_17: renamed getJulianDay to getGregorianDay.
  {
    // dhs@340_29
    int numberofDays = (year - epochYear) * 365 + getNumLeapDays(year) - getNumLeapDays(epochYear);

    numberofDays += getDaysFromMonth();
    numberofDays += day;
    return numberofDays;
  }

  /** Returns the difference in days from this date and the given one (other - this).
   * @since TotalCross 1.0
   */
  public int subtract(Date other) {
    return other.getGregorianDay() - getGregorianDay();
  }

  // dhs@340_29
  private int getNumLeapDays(int iYear) {
    int div4 = (int) ((iYear + 3) / 4);
    int div100 = (int) ((iYear + 99) / 100);
    int div400 = (int) ((iYear + 399) / 400);

    return div4 - div100 + div400;
  }

  /**
   * Checks if the year is a leap year
   * 
   * @param year year to be checked
   * @return true if the year is a leap year, false otherwise
   * 
   * @since TotalCross 1.0 beta 4
   */
  public static boolean isLeapYear(int year) // flsobral@tc100b4_18: made it static and public.
  {
    if (year % 400 == 0) {
      return true;
    }
    if (year % 100 != 0 && year % 4 == 0) {
      return true;
    }
    return false;
  }

  /** Implementation of the Comparable interface. */
  @Override
  public int compareTo(Object other) {
    Date d = (Date) other;
    return (year - d.year) * 10000 + (month - d.month) * 100 + (day - d.day);
  }

  /** Returns this date in the format <code>YYYY-MM-DD 00:00:00.000</code>
   * @since TotalCross 2.0 
   */
  public String getSQLString(StringBuffer sb) {
    sb.append(year);
    sb.append('-');
    if (month < 10) {
      sb.append('0');
    }
    sb.append(month);
    sb.append('-');
    if (day < 10) {
      sb.append('0');
    }
    sb.append(day);
    //sb.append(" 00:00:00.000");
    return sb.toString();
  }

  /** Returns this date in the format <code>YYYY-MM-DD 00:00:00.000</code>
   * @since TotalCross 2.0 
   */
  public String getSQLString() {
    return getSQLString(new StringBuffer(20));
  }

  /** Returns this date in the Time.getTimeLong format, with hour/minute/second/millis equal to 0.
   * @since TotalCross 2.0 
   */
  public long getSQLLong() {
    return year * 10000000000000L + month * 100000000000L + day * 1000000000L;
  }

  public static Date SQL_EPOCH;

  static {
    try {
      SQL_EPOCH = new Date(1, 1, 1970);
    } catch (Exception e) {
    }
  }

  /** Returns the number of seconds since SQL_EPOCH 1/1/1970.
   * @since TotalCross 2.1
   */
  public long getTime() {
    long days = SQL_EPOCH.subtract(this);
    return days * 24L * 60L * 60L;
  }
}
