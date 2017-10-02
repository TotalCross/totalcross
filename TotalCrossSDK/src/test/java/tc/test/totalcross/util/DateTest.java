/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.test.totalcross.util;

import totalcross.sys.Settings;
import totalcross.sys.Time;
import totalcross.unit.TestCase;
import totalcross.util.Date;
import totalcross.util.InvalidDateException;

public class DateTest extends TestCase {
  private void Date() {
    Date d = new Date();
    assertBetween(1, d.getDay(), 31);
    assertBetween(1, d.getMonth(), 12);
    assertBetween(1, d.getWeek(), 52);
    assertBetween(0, d.getDayOfWeek(), 6);
    assertBetween(2006, d.getYear(), 2020);
  }

  private void Date_Time() {
    Date d = null;
    try {
      d = new Date(new Time());
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    assertBetween(1, d.getDay(), 31);
    assertBetween(1, d.getMonth(), 12);
    assertBetween(1, d.getWeek(), 52);
    assertBetween(2006, d.getYear(), 2020);
  }

  private void Date_StrByte() {
    Date d = null;
    try {
      d = new Date("31/1//2000", Settings.DATE_YMD);
      fail();
    } catch (InvalidDateException ide) {
    }
    try {
      d = new Date("31/1/2000/", Settings.DATE_YMD);
      fail();
    } catch (InvalidDateException ide) {
    }
    try {
      d = new Date("31//1/2000", Settings.DATE_YMD);
      fail();
    } catch (InvalidDateException ide) {
    }
    try {
      d = new Date("/31/1/2000", Settings.DATE_YMD);
      fail();
    } catch (InvalidDateException ide) {
    }
    try {
      d = new Date("31/1/2000", Settings.DATE_YMD);
      fail();
    } catch (InvalidDateException ide) {
    }
    try {
      d = new Date("31/1/2000", Settings.DATE_MDY);
      fail();
    } catch (InvalidDateException ide) {
    }
    try {
      d = new Date("31/1/2000", Settings.DATE_DMY);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    assertEquals(20000131, d.getDateInt());

    // test leap years
    try {
      d = new Date("29/002/1900", Settings.DATE_DMY);
      fail();
    } catch (InvalidDateException ide) {
    }
    try {
      d = new Date("29/002/2000", Settings.DATE_DMY);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    try {
      d = new Date("29/002/2004", Settings.DATE_DMY);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
  }

  private void Date_int() {
    Date d = null;
    try {
      d = new Date(20002503);
      fail();
    } catch (InvalidDateException ide) {
    }
    try {
      d = new Date(19700325);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    assertEquals(25, d.getDay());
    assertEquals(3, d.getMonth());
    assertEquals(1970, d.getYear());
    assertEquals(3, d.getDayOfWeek());
    assertEquals(19700325, d.getDateInt());
  }

  private void Date_intintint() {
    Date d = null;
    try {
      d = new Date(12, 6, 1975);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    assertEquals(19750612, d.getDateInt());
    try {
      d = new Date(42, 6, 1975);
      fail();
    } catch (InvalidDateException ide) {
    }
    try {
      d = new Date(12, 13, 1975);
      fail();
    } catch (InvalidDateException ide) {
    }
    try {
      d = new Date(12, -1, 1975);
      fail();
    } catch (InvalidDateException ide) {
    }
    try {
      d = new Date(-12, 11, 1975);
      fail();
    } catch (InvalidDateException ide) {
    }
  }

  private void setToday() {
    try {
      new Date(7, 17, 1936);
      fail();
    } catch (InvalidDateException ide) {
    }
    //d.setToday();
  }

  private void formatDayMonth() {
    Date d = null;
    try {
      d = new Date(25, 3, 1970);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    // save current settings
    byte df = Settings.dateFormat;
    char sep = Settings.dateSeparator;

    Settings.dateSeparator = ',';
    Settings.dateFormat = Settings.DATE_DMY;
    String s1 = d.formatDayMonth();
    Settings.dateFormat = Settings.DATE_MDY;
    String s2 = d.formatDayMonth();
    Settings.dateFormat = Settings.DATE_YMD;
    String s3 = d.formatDayMonth();

    // restore settings
    Settings.dateFormat = df;
    Settings.dateSeparator = sep;

    assertEquals(s1, "25,03");
    assertEquals(s2, "03,25");
    assertEquals(s3, "03,25");
  }

  private void formatDate_intintintbyte() {
    // save current settings
    char sep = Settings.dateSeparator;
    Settings.dateSeparator = ',';

    String s1 = Date.formatDate(25, 3, 1970, Settings.DATE_DMY);
    String s2 = Date.formatDate(25, 3, 1970, Settings.DATE_MDY);
    String s3 = Date.formatDate(25, 3, 1970, Settings.DATE_YMD);

    // restore settings
    Settings.dateSeparator = sep;

    assertEquals(s1, "25,03,1970");
    assertEquals(s2, "03,25,1970");
    assertEquals(s3, "1970,03,25");
  }

  private void getDate() // same of toString
  {
    Date d = null;
    try {
      d = new Date(25, 3, 1970);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    // save current settings
    byte df = Settings.dateFormat;
    char sep = Settings.dateSeparator;

    Settings.dateSeparator = ',';
    Settings.dateFormat = Settings.DATE_DMY;
    String s1 = d.toString();
    Settings.dateFormat = Settings.DATE_MDY;
    String s2 = d.toString();
    Settings.dateFormat = Settings.DATE_YMD;
    String s3 = d.toString();

    // restore settings
    Settings.dateFormat = df;
    Settings.dateSeparator = sep;

    assertEquals(s1, "25,03,1970");
    assertEquals(s2, "03,25,1970");
    assertEquals(s3, "1970,03,25");
  }

  private void getDaysInMonth_int() {
    Date d = null;
    try {
      d = new Date(1, 1, 1900);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    assertEquals(31, d.getDaysInMonth(Date.JANUARY));
    assertEquals(28, d.getDaysInMonth(Date.FEBRUARY));
    try {
      d = new Date(1, 1, 2000);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    assertEquals(31, d.getDaysInMonth(Date.JULY));
    assertEquals(29, d.getDaysInMonth(Date.FEBRUARY));
  }

  private void getMonthName() {
    assertEquals(0, Date.getMonthName(0).length());
    assertEquals(0, Date.getMonthName(13).length());
    assertEquals(0, Date.getMonthName(-1).length());
  }

  private void isBefore_isAfter() {
    Date d1 = null, d2 = null, d3 = null;
    try {
      d1 = new Date(20060227);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    try {
      d2 = new Date(20060228);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    try {
      d3 = new Date(20060301);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    assertTrue(d1.isBefore(d2));
    assertTrue(d2.isBefore(d3));
    assertTrue(d1.isBefore(d3));
    assertFalse(d1.isBefore(d1));

    assertTrue(d3.isAfter(d2));
    assertTrue(d2.isAfter(d1));
    assertTrue(d3.isAfter(d1));
    assertFalse(d1.isAfter(d1));
  }

  private void advances() {
    try {
      Date d = new Date(20001230);
      Date dd = new Date(20001230);

      d.advance(10);
      d.advance(-10);
      assertEquals(dd, d);

      d.advanceWeek();
      d.advanceWeek(Date.BACKWARD);
      assertEquals(d, new Date(20001224));

      d.advanceMonth();
      d.advanceMonth(Date.BACKWARD);
      assertEquals(d, new Date(20001201));

      d = new Date(20000227);
      dd = new Date(20000227);

      d.advance(1000);
      d.advance(-1000);
      assertEquals(dd, d);

      d.advanceWeek();
      d.advanceWeek(Date.BACKWARD);
      assertEquals(dd, d);

      d.advanceMonth();
      d.advanceMonth(Date.BACKWARD);
      assertEquals(d, new Date(20000201));
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
  }

  private void getJulianDay() {
    Date d = null;
    try {
      d = new Date(20000101);
    } catch (InvalidDateException ide) {
      fail(ide.getMessage());
    }
    assertEquals(365243, d.getGregorianDay());
    d.advance(100);
    int s = d.getGregorianDay();
    d.advance(-100);
    int f = d.getGregorianDay();
    assertEquals(100, s - f);
  }

  @Override
  public void testRun() {
    Date();
    Date_Time();
    Date_StrByte();
    Date_int();
    Date_intintint();
    setToday();
    formatDayMonth();
    formatDate_intintintbyte();
    getDate();
    getDaysInMonth_int();
    getMonthName();
    isBefore_isAfter();
    advances();
    getJulianDay();
  }
}
