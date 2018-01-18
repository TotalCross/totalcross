/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.test.totalcross.collections;

import totalcross.sys.Time;
import totalcross.unit.TestCase;
import totalcross.util.Date;

public class TestGenerics extends TestCase {
  @Override
  public void testRun() {
    GenericsType<String> type = new GenericsType();
    String string = type.toString();
    assertTrue(string.startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith("null"));

    type.set("Pankaj");
    assertTrue((string = type.toString()).startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith("Pankaj"));

    GenericsType type1 = new GenericsType();
    assertTrue((string = type1.toString()).startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith("null"));

    type1.set("Pankaj");
    assertTrue((string = type1.toString()).startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith("Pankaj"));

    Date date = new Date();
    type1.set(date);
    assertTrue((string = type1.toString()).startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith(date.toString()));

    Time time = new Time();
    type1.set(new Time());
    assertTrue((string = type1.toString()).startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith(time.toString()));

    GenericsType<Object> type2 = new GenericsType<Object>();
    assertTrue((string = type2.toString()).startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith("null"));

    type2.set("Pankaj");
    assertTrue((string = type2.toString()).startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith("Pankaj"));

    type2.set(date);
    assertTrue((string = type2.toString()).startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith(date.toString()));

    type2.set(new Time());
    assertTrue((string = type2.toString()).startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith(time.toString()));

    GenericsType<Date> type3 = new GenericsType<Date>();
    assertTrue((string = type3.toString()).startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith("null"));

    type3.set(date);
    assertTrue((string = type3.toString()).startsWith("tc.test.totalcross.collections.GenericsType@"));
    assertTrue(string.endsWith(date.toString()));
  }
}

class GenericsType<T> {
  private T t;

  public T get() {
    return this.t;
  }

  public void set(T t1) {
    this.t = t1;
  }

  @Override
  public String toString() {
    if (t == null) {
      return super.toString() + " null";
    } else {
      return super.toString() + " " + t.toString();
    }
  }
}
