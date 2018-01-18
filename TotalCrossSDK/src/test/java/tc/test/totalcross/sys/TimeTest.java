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

package tc.test.totalcross.sys;

import totalcross.sys.Settings;
import totalcross.sys.Time;
import totalcross.unit.TestCase;

public class TimeTest extends TestCase {
  @Override
  public void testRun() {
    Time t = new Time();
    Time lw = new Time(2006, 03, 25, 10, 30, 45, 0);
    Time up = new Time(2020, 12, 31, 23, 59, 59, 99);
    assertEquals(20060325103045L, lw.getTimeLong());
    assertBetween(lw.getTimeLong(), t.getTimeLong(), up.getTimeLong());
    boolean is24 = Settings.is24Hour;
    char ts = Settings.timeSeparator;
    Settings.timeSeparator = ':';

    t = new Time(2000, 01, 01, 0, 0, 0, 0);
    Settings.is24Hour = true;
    assertEquals("00:00:00", t.toString());
    Settings.is24Hour = false;
    assertEquals("12:00:00 AM", t.toString());

    t = new Time(2000, 01, 01, 12, 0, 0, 0);
    Settings.is24Hour = true;
    assertEquals("12:00:00", t.toString());
    Settings.is24Hour = false;
    assertEquals("12:00:00 PM", t.toString());

    t = new Time(2000, 01, 01, 13, 0, 0, 0);
    Settings.is24Hour = true;
    assertEquals("13:00:00", t.toString());
    Settings.is24Hour = false;
    assertEquals("01:00:00 PM", t.toString());

    t = new Time(2000, 01, 01, 23, 0, 0, 0);
    Settings.is24Hour = true;
    assertEquals("23:00:00", t.toString());
    Settings.is24Hour = false;
    assertEquals("11:00:00 PM", t.toString());

    t = new Time(2000, 01, 01, 19, 0, 0, 0);
    Settings.is24Hour = true;
    assertEquals("19:00:00", t.toString());
    Settings.is24Hour = false;
    assertEquals("07:00:00 PM", t.toString());

    t = new Time(2000, 01, 01, 11, 0, 0, 0);
    Settings.is24Hour = true;
    assertEquals("11:00:00", t.toString());
    Settings.is24Hour = false;
    assertEquals("11:00:00 AM", t.toString());

    t = new Time(2000, 01, 01, 9, 0, 0, 0);
    Settings.is24Hour = true;
    assertEquals("09:00:00", t.toString());
    Settings.is24Hour = false;
    assertEquals("09:00:00 AM", t.toString());
    Settings.is24Hour = true;

    t = new Time(2010, 10, 19, 10, 7, 32, 0);
    assertEquals("10:07:32", t.toString());
    t.inc(-3, 0, 0);
    assertEquals("07:07:32", t.toString());
    t.inc(128, 120, 181);
    assertEquals("17:10:33", t.toString());
    t.inc(-128, -120, -181);
    assertEquals("07:07:32", t.toString());
    t.inc(3, 0, 0);
    assertEquals("10:07:32", t.toString());

    Settings.is24Hour = is24;
    Settings.timeSeparator = ts;
  }
}
