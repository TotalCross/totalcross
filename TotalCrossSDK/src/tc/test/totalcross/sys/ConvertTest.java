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

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.font.*;
import totalcross.unit.*;

public class ConvertTest extends TestCase
{
   private boolean win32 = Settings.platform.equals(Settings.WIN32);
   private void detectSortTypeAndQuickSort()
   {
      Object [] asObj = {new Age(5),new Age(1),new Age(-15)};
      Object [] asStr = {"joana","andre","bocao","ziraldo","melissa"};
      Object [] asInt = {"-10","-50","-100"};
      Object [] asDbl = {"-1e-5","1e5","100.543"};
      Object [] asDat = {"1/5/2006","25/3/1970","12/6/1975"};
      char sep = Settings.dateSeparator;
      byte format = Settings.dateFormat;
      Settings.dateSeparator = '/';
      Settings.dateFormat = Settings.DATE_DMY;
      assertEquals(Convert.SORT_OBJECT, Convert.detectSortType(asObj[0]));
      assertEquals(Convert.SORT_STRING, Convert.detectSortType(asStr[0]));
      assertEquals(Convert.SORT_INT, Convert.detectSortType(asInt[0]));
      assertEquals(Convert.SORT_DOUBLE, Convert.detectSortType(asDbl[0]));
      assertEquals(Convert.SORT_DATE, Convert.detectSortType(asDat[0]));

      Convert.qsort(asObj, 0, asObj.length-1);
      Convert.qsort(asStr, 0, asStr.length-1);
      Convert.qsort(asInt, 0, asInt.length-1);
      Convert.qsort(asDbl, 0, asDbl.length-1);
      Convert.qsort(asDat, 0, asDat.length-1);

      assertEquals("-15", asObj[0].toString());
      assertEquals("1", asObj[1].toString());
      assertEquals("5", asObj[2].toString());

      assertEquals("andre", asStr[0]);
      assertEquals("bocao", asStr[1]);
      assertEquals("joana", asStr[2]);
      assertEquals("melissa", asStr[3]);
      assertEquals("ziraldo", asStr[4]);

      assertEquals("-100", asInt[0]);
      assertEquals("-50", asInt[1]);
      assertEquals("-10", asInt[2]);

      assertEquals("-1e-5", asDbl[0]);
      assertEquals("100.543", asDbl[1]);
      assertEquals("1e5", asDbl[2]);

      assertEquals("25/3/1970", asDat[0]);
      assertEquals("12/6/1975", asDat[1]);
      assertEquals("1/5/2006", asDat[2]);
      Settings.dateSeparator = sep;
      Settings.dateFormat = format;
   }

   private void insertLineBreak()
   {
      FontMetrics fm = MainWindow.getMainWindow().getFont().fm;
      String orig = "one two three four five six seven eight nine ten",s;
      s = Convert.insertLineBreak(1000, fm, orig);
      assertEquals(s,orig);
   }

   private void rol()
   {
      assertEquals(1, Convert.rol(1, 4, 4));
      assertEquals(1, Convert.rol(1, 8, 8));
      assertEquals(1, Convert.rol(1, 16, 16));
      assertEquals(1, Convert.rol(1, 32, 32));
      assertEquals(1, Convert.rol(1, 64, 64));
      assertEquals(0x100000000L, Convert.rol(1, 32, 64));
      assertEquals(0x4, Convert.rol(0x101, 2, 4));
      assertEquals(0x40, Convert.rol(0x101, 6, 8));
      assertEquals(0x4040, Convert.rol(0x101, 14, 16));
      assertEquals(0x40000040, Convert.rol(0x101, 30, 32));
      assertEquals(0x4000000000000040L, Convert.rol(0x101, 62, 64));
      assertEquals(0x4, Convert.rol(0x11, 2, 4));
      assertEquals(0x440, Convert.rol(0x11, 6, 16));
      assertEquals(0x10101000, Convert.rol(0x10101, 12, 32));
      assertEquals(0x40004040, Convert.rol(0x10101, 30, 32));
      assertEquals(0x1010000000000010L, Convert.rol(0x10101, 52, 64));
      assertEquals(0x1000000000001010L, Convert.rol(0x10101, 60, 64));
      assertEquals(0x10, Convert.rol(1, 4, 0));
      assertEquals(0, Convert.rol(1, -4, 1));
   }

   private void ror()
   {
      assertEquals(1, Convert.ror(1, 4, 4));
      assertEquals(1, Convert.ror(1, 8, 8));
      assertEquals(1, Convert.ror(1, 16, 16));
      assertEquals(1, Convert.ror(1, 32, 32));
      assertEquals(1, Convert.ror(1, 64, 64));
      assertEquals(0x4, Convert.ror(0x101, 2, 4));
      assertEquals(0x4, Convert.ror(0x101, 6, 8));
      assertEquals(0x404, Convert.ror(0x101, 14, 16));
      assertEquals(0x404, Convert.ror(0x101, 30, 32));
      assertEquals(0x404, Convert.ror(0x101, 62, 64));
      assertEquals(0x4, Convert.ror(0x11, 2, 4));
      assertEquals(0x4400, Convert.ror(0x11, 6, 16));
      assertEquals(0x10100010, Convert.ror(0x10101, 12, 32));
      assertEquals(0x40404, Convert.ror(0x10101, 30, 32));
      assertEquals(0x10101000, Convert.ror(0x10101, 52, 64));
      assertEquals(0x101010, Convert.ror(0x10101, 60, 64));
      assertEquals(0, Convert.ror(1, 4, 0));
      assertEquals(0, Convert.ror(1, -4, 1));
   }

   private void chars2int()
   {
      try
      {
         Convert.chars2int("123"); // must have exactly 4
         fail("ArrayIndexOutOfBoundsException should have been thrown");
      }
      catch (ArrayIndexOutOfBoundsException aioobe) // device
      {
         // ok
      }
      catch (StringIndexOutOfBoundsException aioobe) // desktop
      {
         // ok
      }
      assertEquals(0x31323334,Convert.chars2int("12345")); // 5th element is ignored
      assertEquals(0x31323334,Convert.chars2int("1234"));
      assertEquals(0x5A5A7A7A,Convert.chars2int("ZZzz"));
      assertEquals(0x30306162,Convert.chars2int("00ab"));
   }

   private void tokenizeString_StringChar()
   {
      String []s;
      s = Convert.tokenizeString("",' ');
      assertEquals(1, s.length);
      s = Convert.tokenizeString(" ",(char)0);
      assertEquals(1, s.length);

      s = Convert.tokenizeString("one two three four five.six seven-eight",' ');
      assertEquals(6, s.length);
      assertEquals("one",s[0]);
      assertEquals("two", s[1]);
      assertEquals("three", s[2]);
      assertEquals("four", s[3]);
      assertEquals("five.six", s[4]);
      assertEquals("seven-eight", s[5]);

      s = Convert.tokenizeString("one two three four five.six seven-eight",'.');
      assertEquals(2, s.length);
      assertEquals("one two three four five", s[0]);
      assertEquals("six seven-eight", s[1]);

      s = Convert.tokenizeString("one two three four five.six seven-eight",',');
      assertEquals(1, s.length);
      assertEquals("one two three four five.six seven-eight", s[0]);
   }

   private void tokenizeString_StringString()
   {
      String []s;
      s = Convert.tokenizeString(""," ");
      assertEquals(1, s.length);
      s = Convert.tokenizeString(" ","");
      assertEquals(1, s.length);

      s = Convert.tokenizeString("one two three four five.six seven-eight","");
      assertEquals(1, s.length);

      assertEquals("one two three four five.six seven-eight", s[0]);
      s = Convert.tokenizeString("one two three four five.six seven-eight"," ");
      assertEquals(6, s.length);
      assertEquals("one",s[0]);
      assertEquals("two", s[1]);
      assertEquals("three", s[2]);
      assertEquals("four", s[3]);
      assertEquals("five.six", s[4]);
      assertEquals("seven-eight", s[5]);

      s = Convert.tokenizeString("one  two  three  four  five.six  seven-eight","  ");
      assertEquals(6, s.length);
      assertEquals("one",s[0]);
      assertEquals("two", s[1]);
      assertEquals("three", s[2]);
      assertEquals("four", s[3]);
      assertEquals("five.six", s[4]);
      assertEquals("seven-eight", s[5]);
      s = Convert.tokenizeString("one two three four five.six seven-eight",";:");
      assertEquals(1, s.length);
      assertEquals("one two three four five.six seven-eight", s[0]);
   }

   private void dup()
   {
      assertEquals("",Convert.dup('9',0));
      assertEquals("9",Convert.dup('9',1));
      assertEquals("99",Convert.dup('9',2));
      assertEquals("999",Convert.dup('9',3));
   }
   
   private void spacePad()
   {
      assertEquals("abc",Convert.spacePad("abc",0,true));
      assertEquals("abc",Convert.spacePad("abc",1,true));
      assertEquals("abc",Convert.spacePad("abc",2,true));
      assertEquals("abc",Convert.spacePad("abc",3,true));
      assertEquals(" abc",Convert.spacePad("abc",4,true));
      assertEquals("  abc",Convert.spacePad("abc",5,true));
      assertEquals("   abc",Convert.spacePad("abc",6,true));
      assertEquals("abc ",Convert.spacePad("abc",4,false));
      assertEquals("abc  ",Convert.spacePad("abc",5,false));
      assertEquals("abc   ",Convert.spacePad("abc",6,false));
   }
   
   private void zeroPad()
   {
      assertEquals("1",Convert.zeroPad("1",1));
      assertEquals("1",Convert.zeroPad("1",0));
      assertEquals("",Convert.zeroPad("",0));
      assertEquals("0",Convert.zeroPad("",1));
      assertEquals("",Convert.zeroPad("",-1));
      assertEquals("01",Convert.zeroPad("1",2));
      assertEquals("001",Convert.zeroPad("1",3));
   }

   private void toString_IntRadix()
   {
      assertEquals("10000000000000000000000000000000", Convert.toString(Convert.MIN_INT_VALUE,2));
      assertEquals("1111111111111111111111111111111", Convert.toString(Convert.MAX_INT_VALUE,2));
      assertEquals("20000000000", Convert.toString(Convert.MIN_INT_VALUE,8));  
      assertEquals("17777777777", Convert.toString(Convert.MAX_INT_VALUE,8));  
      assertEquals("-2147483648", Convert.toString(Convert.MIN_INT_VALUE,10)); 
      assertEquals("2147483647", Convert.toString(Convert.MAX_INT_VALUE,10)); 
      assertEquals("80000000", Convert.toString(Convert.MIN_INT_VALUE,16)); 
      assertEquals("7fffffff", Convert.toString(Convert.MAX_INT_VALUE,16)); 
   }
   
   private void toLong_StringRadix()
   {
      try
      {
         assertEquals(-1,Convert.toLong("-1",2));
         assertEquals(-1,Convert.toLong("-1",8));
         assertEquals(-1,Convert.toLong("-1",10));
         assertEquals(-1,Convert.toLong("-1",16));
         assertEquals(0,Convert.toLong("0",2));
         assertEquals(691752902764108185L,Convert.toLong("999999999999999L",16));
         assertEquals(0x999999999999999L,Convert.toLong("999999999999999",16));
         assertEquals(0x9999999999999999L,Convert.toLong("-6666666666666667",16));
         assertEquals(-0x9999999999999999L,Convert.toLong("6666666666666667",16));
         assertEquals(9223372036854775807L,Convert.toLong("7fffffffffffffff",16));
         assertEquals(-9223372036854775808L,Convert.toLong("-8000000000000000",16));
         assertEquals(9223372036854775807L,Convert.toLong("777777777777777777777",8));
         assertEquals(-9223372036854775808L,Convert.toLong("-1000000000000000000000",8));
         assertEquals(9223372036854775807L,Convert.toLong("111111111111111111111111111111111111111111111111111111111111111",2));
         assertEquals(-9223372036854775808L,Convert.toLong("-1000000000000000000000000000000000000000000000000000000000000000",2));
      } catch (InvalidNumberException ine) {fail(ine.getMessage());}
   }

   private void toLong_String()
   {
      try {Convert.toLong(" 12345678"); fail();} catch (InvalidNumberException ine) {}
      //try {Convert.toLong("999999999999999999L"); fail();} catch (InvalidNumberException ine) {}
      try
      {
         assertEquals(-1,Convert.toLong("-1"));
         assertEquals(1,Convert.toLong("1"));
         assertEquals(0,Convert.toLong("0"));
         assertEquals(999999999999999999L,Convert.toLong("999999999999999999"));
         assertEquals(-999999999999999999L,Convert.toLong("-999999999999999999"));
         assertEquals(9223372036854775807L,Convert.toLong("9223372036854775807"));
         assertEquals(-9223372036854775808L,Convert.toLong("-9223372036854775808"));
         assertEquals(-2147483648L,Convert.toLong("-2147483648"));
         assertEquals(-2147483647L,Convert.toLong("-2147483647"));
         assertEquals(2147483647L,Convert.toLong("2147483647"));
         assertEquals(2147483646L,Convert.toLong("2147483646"));
      } catch (InvalidNumberException ine) {fail(ine.getMessage());}
   }

   private void toString_LongRadix()
   {
      assertEquals("1",Convert.toString(-1,2));
      assertEquals("1",Convert.toString(-1,8));
      assertEquals("-1",Convert.toString(-1,10));
      assertEquals("1",Convert.toString(-1,16));
      assertEquals("0",Convert.toString(0,2));
      assertEquals("999999999999999",Convert.toString(0x999999999999999L,16));
      assertEquals("6666666666666667",Convert.toString(0x9999999999999999L,16));
      assertEquals("6666666666666667",Convert.toString(-0x9999999999999999L,16));
      assertEquals("7fffffffffffffff",Convert.toString(9223372036854775807L,16));
      assertEquals("8000000000000000",Convert.toString(-9223372036854775808L,16));
      assertEquals("777777777777777777777",Convert.toString(9223372036854775807L,8));
      assertEquals("1000000000000000000000",Convert.toString(-9223372036854775808L,8));
      assertEquals("111111111111111111111111111111111111111111111111111111111111111",Convert.toString(9223372036854775807L,2));
      assertEquals("1000000000000000000000000000000000000000000000000000000000000000",Convert.toString(-9223372036854775808L,2));
   }

   private void toString_Long()
   {
      assertEquals("-1",Convert.toString(-1));
      assertEquals("0",Convert.toString(0));
      assertEquals("999999999999999999",Convert.toString(999999999999999999L));
      assertEquals("-999999999999999999",Convert.toString(-999999999999999999L));
      assertEquals("9223372036854775807",Convert.toString(9223372036854775807L));
      assertEquals("-9223372036854775808",Convert.toString(-9223372036854775808L));
      assertEquals("-2147483648",Convert.toString(-2147483648L));
      assertEquals("-2147483647",Convert.toString(-2147483647L));
      assertEquals("2147483647",Convert.toString(2147483647L));
      assertEquals("2147483646",Convert.toString(2147483646L));
   }

   private void digitOf()
   {
      assertEquals(-1,Convert.digitOf('3',2));
      assertEquals(-1,Convert.digitOf('G',16));
      assertEquals(-1,Convert.digitOf('g',16));
      assertEquals(9,Convert.digitOf('9',10));
      assertEquals(-1,Convert.digitOf('9',8));

      assertEquals(1,Convert.digitOf('1',2));
      assertEquals(0,Convert.digitOf('0',2));
      assertEquals(0,Convert.digitOf('0',16));
      assertEquals(15,Convert.digitOf('f',16));
      assertEquals(15,Convert.digitOf('F',16));
      assertEquals(0,Convert.digitOf('0',10));
      assertEquals(7,Convert.digitOf('7',8));

      try {Convert.digitOf('7',17); fail();} catch (IllegalArgumentException iae) {}
      try {Convert.digitOf('7',1); fail();} catch (IllegalArgumentException iae) {}
      try {Convert.digitOf('7',-1); fail();} catch (IllegalArgumentException iae) {}
   }

   private void forDigit()
   {
      assertEquals('1',Convert.forDigit(1,2));
      assertEquals('0',Convert.forDigit(0,2));
      assertEquals('0',Convert.forDigit(0,16));
      assertEquals('f',Convert.forDigit(0xf,16));
      assertEquals('0',Convert.forDigit(0,10));
      assertEquals('7',Convert.forDigit(7,8));

      try {Convert.forDigit(2,-1); fail();} catch (IllegalArgumentException iae) {}
      try {Convert.forDigit(7,17); fail();} catch (IllegalArgumentException iae) {}
   }

   private void cloneStringArray()
   {
      String []s1 = {"jose","maria","jesus"};
      String []s2 = Convert.cloneStringArray(s1);
      s2[0] = "maria";
      assertNotEquals(s1[0],s2[0]);
   }

   private class Age
   {
      int age;
      public Age(int age) {this.age = age;}
      public String toString() {return Convert.toString(age);}
   }

   private void toStringArray()
   {
      Object []ages = {new Age(0), new Age(1), new Age(2)};
      String []strs = Convert.toStringArray(ages);
      assertEquals("0",strs[0]);
      assertEquals("1",strs[1]);
      assertEquals("2",strs[2]);
   }

   private void toInt()
   {
      try {Convert.toInt(" 12345678"); fail();} catch (InvalidNumberException ine) {}
      try {Convert.toInt("0x12345678"); fail();} catch (InvalidNumberException ine) {}
      try {Convert.toInt("abcd"); fail();} catch (InvalidNumberException ine) {}
      try {Convert.toInt("-abcd"); fail();} catch (InvalidNumberException ine) {}
      try {Convert.toInt("+999"); fail();} catch (InvalidNumberException ine) {}
      try {Convert.toInt("-999.023"); fail();} catch (InvalidNumberException ine) {}
      try {Convert.toInt("12345678901234567890"); fail();} catch (InvalidNumberException ine) {}
      try {Convert.toInt(""); fail();} catch (InvalidNumberException ine) {}
      try
      {
         assertEquals(1, Convert.toInt("1"));
         assertEquals(-1, Convert.toInt("-1"));
         assertEquals(12345678, Convert.toInt("12345678"));
         assertEquals(-12345678, Convert.toInt("-12345678"));
         assertEquals(-999, Convert.toInt("-999"));
         assertEquals(-2147483648,Convert.toInt("-2147483648"));
         assertEquals(-2147483647,Convert.toInt("-2147483647"));
         assertEquals(2147483647,Convert.toInt("2147483647"));
         assertEquals(2147483646,Convert.toInt("2147483646"));
      } catch (InvalidNumberException ine) {fail(ine.getMessage());}
   }

   private void toString_Boolean()
   {
      assertEquals("true",Convert.toString(true));
      assertEquals("false",Convert.toString(false));
      //assertEquals("maybe",Convert.toString(maybe)); - just kidding :-D
   }

   private void toString_Char()
   {
      assertEquals("a",Convert.toString('a'));
      assertEquals("ç",Convert.toString('ç'));
      assertEquals("\u1E00",Convert.toString((char)0x1E00));
   }

   double err = 1e-5;
   private void doubleToIntBits()
   {
      assertEquals(0, Convert.doubleToIntBits(0f));
      //assertEquals(-2147483648, Convert.doubleToIntBits(-0f));
      assertEquals(1176256512, Convert.doubleToIntBits(10000f));
      assertEquals(1066388790, Convert.doubleToIntBits(1.12345f));
      assertEquals(1343554297, Convert.doubleToIntBits(1e10f));
      assertEquals(786163455, Convert.doubleToIntBits(1e-10f));
   }

   private void intBitsToDouble()
   {
      assertEquals(0d, Convert.intBitsToDouble(0), err);
      //assertEquals(-0d, Convert.intBitsToDouble(-2147483648), err);
      assertEquals(10000d, Convert.intBitsToDouble(1176256512), err);
      assertEquals(1.12345d, Convert.intBitsToDouble(1066388790), err);
      assertEquals(1e10d, Convert.intBitsToDouble(1343554297), err);
      assertEquals(01e-10d, Convert.intBitsToDouble(786163455), err);
   }

   private void longBitsToDouble()
   {
      assertEquals(0, Convert.longBitsToDouble(0),err);
      //assertEquals(-0, Convert.longBitsToDouble(-9223372036854775808L),err);
      assertEquals(1000000.0, Convert.longBitsToDouble(4696837146684686336L),err);
      assertEquals(1.1234567891, Convert.longBitsToDouble(4607738418749404526L),err);
      assertEquals(1e101, Convert.longBitsToDouble(6117819141328598108L),err);
      assertEquals(1e-101, Convert.longBitsToDouble(3095773665367583373L),err);
      assertEquals(-1e-101, Convert.longBitsToDouble(-6127598371487192435L),err);
      assertEquals(-1e101, Convert.longBitsToDouble(-3105552895526177700L),err);
   }

   private void doubleToLongBits()
   {
      assertEquals(0, Convert.doubleToLongBits(0));
      //assertEquals(-9223372036854775808L, Convert.doubleToLongBits(-0d));
      assertEquals(4696837146684686336L, Convert.doubleToLongBits(1000000.0));
      assertEquals(4607738418749404526L, Convert.doubleToLongBits(1.1234567891));
      assertEquals(6117819141328598108L, Convert.doubleToLongBits(1e101));
      assertEquals(3095773665367583373L, Convert.doubleToLongBits(1e-101));
      assertEquals(-6127598371487192435L, Convert.doubleToLongBits(-1e-101));
      assertEquals(-3105552895526177700L, Convert.doubleToLongBits(-1e101));
   }

   private void toDouble()
   {
      double err = 1e-5;
      try {Convert.toDouble("10.1dd"); fail();} catch (InvalidNumberException ine) {}
      try {Convert.toDouble("10d.1"); fail();} catch (InvalidNumberException ine) {}
      try {Convert.toDouble("10,1"); fail();} catch (InvalidNumberException ine) {}
      try {Convert.toDouble("10.2g"); fail();} catch (InvalidNumberException ine) {}
      try
      {
         assertEquals(10d, Convert.toDouble("10"),err);
         assertEquals(10d, Convert.toDouble("10d"),err);
         assertEquals(10.1d, Convert.toDouble("10.1d"),err);
         assertEquals(10.1d, Convert.toDouble("10.1f"),err);
         assertEquals(10.3, Convert.toDouble(" 10.3"),err);
         assertEquals(0d, Convert.toDouble("0"),err);
         assertEquals(0.5, Convert.toDouble("0.5"),err);
         assertEquals(0.55555555555, Convert.toDouble("0.55555555555"),err);
         assertEquals(0.0000000000000000555555555555555d, Convert.toDouble("5.55555555555555E-17"),err);
         assertEquals(10000.0, Convert.toDouble("00010000"),err);
         assertEquals(1000.543216, Convert.toDouble("1000.543216"),err);
         assertEquals(-100.54, Convert.toDouble("-100.540000"),err);
         assertEquals(1e-40, Convert.toDouble("9.999e-41"),err);
         assertEquals(-1e-40, Convert.toDouble("-9.999e-41"),err);
         assertEquals(-1e40, Convert.toDouble("-1.000e+40"),err);
         assertEquals(1000000.0, Convert.toDouble("1000000"),err);
         assertEquals(9999999.0, Convert.toDouble("9999999"),err);
         assertEquals(0.99999999999, Convert.toDouble("0.99999999999"),err);
         assertEquals(1.99999999999, Convert.toDouble("1.99999999999"),err);
         assertEquals(-1e308d, Convert.toDouble("-1.00e+308"),err);
         assertEquals(1e-40, Convert.toDouble("9.999e-41"),err);
         assertEquals(-1e-40, Convert.toDouble("-9.999e-41"),err);
         assertEquals(-1e40, Convert.toDouble("-1.000e+40"),err);
      } catch (InvalidNumberException ine) {fail(ine.getMessage());}
   }

   private void toString_Double() // guich@566_38
   {
      assertEquals("10.0", Convert.toString(10d));
      assertEquals("0.0", Convert.toString(0d));
      assertEquals("0.5", Convert.toString(0.5));
      assertEquals("0.55555555555", Convert.toString(0.55555555555));
      assertEquals("0.0055555555555", Convert.toString(0.0055555555555));
      assertEquals("0.000055555555555", Convert.toString(0.000055555555555));
      assertEquals("0.000000555555556", Convert.toString(0.00000055555555555));
      assertEquals("0.000000005555556", Convert.toString(0.0000000055555555555));
      assertEquals("0.000000005555556", Convert.toString(0.000000005555555555555));
      assertEquals("0.000000000055556", Convert.toString(0.00000000005555555555555));
      assertEquals("0.000000000000556", Convert.toString(0.000000000000555555555555555));
      assertEquals("0.000000000000006", Convert.toString(0.00000000000000555555555555555));
      assertEquals("0.0", Convert.toString(0.0000000000000000555555555555555));
      assertEquals("10000.0", Convert.toString(10000.0));
      assertEquals("1000.543216000000029", Convert.toString(1000.543216));
      assertEquals("-100.540000000000006", Convert.toString(-100.54));
      assertEquals("0.0", Convert.toString(1e-40));
      assertEquals("-0.0", Convert.toString(-1e-40));
      assertEquals("-9223372036854775807.9223372036854775807", Convert.toString(-1e40));
      assertEquals("1000000.0", Convert.toString(1000000.0));
      assertEquals("9999999.0", Convert.toString(9999999.0));
      assertEquals("0.99999999999", Convert.toString(0.99999999999));
      assertEquals("1.99999999999", Convert.toString(1.99999999999));
      assertEquals("-9223372036854775807.00", Convert.toString(-1e308d,2));
      assertEquals("-9223372036854775807.9223372036854775807", Convert.toString(-1.432132689765E140));
      assertEquals("-9.454938759257E240", Convert.toString(-9.454938759257E240));
      assertEquals("1.432132689765E140", Convert.toString(1.432132689765E140));
      assertEquals("9.454938759257E240", Convert.toString(9.454938759257E240));
      assertEquals("1.432132689765E-140", Convert.toString(1.432132689765E-140));
      assertEquals("9.454938759257E-240", Convert.toString(9.454938759257E-240));
      assertEquals("+Inf", Convert.toString(1.0/0.0));
      assertEquals("-Inf", Convert.toString(-1.0/0.0));
   }

   private void toString_DoublePrecision()
   {
      assertEquals("10.0", Convert.toString(10d,-1));
      assertEquals("0", Convert.toString(0d,0));
      assertEquals("0.00", Convert.toString(0d,2));
      assertEquals("0.0000", Convert.toString(0d,4));
      assertEquals("1", Convert.toString(0.5,0));
      assertEquals("0.5", Convert.toString(0.5,1));
      assertEquals("0.50", Convert.toString(0.5,2));
      assertEquals("0.5555556", Convert.toString(0.55555555555,7));
      assertEquals("0.555555555550", Convert.toString(0.55555555555,12));
      assertEquals("0.5555556", Convert.toString(0.55555555555,7));
      assertEquals("0.555555555550000", Convert.toString(0.55555555555,15));
      assertEquals("0.005555555555500", Convert.toString(0.0055555555555,15));
      assertEquals("0.000055555555555", Convert.toString(0.000055555555555,15));
      assertEquals("0.000000555555556", Convert.toString(0.00000055555555555,15));
      assertEquals("0.000000005555556", Convert.toString(0.0000000055555555555,15));
      assertEquals("10000", Convert.toString(10000.0,0));
      assertEquals("1000.54322", Convert.toString(1000.543216,5));
      assertEquals("-100.54", Convert.toString(-100.54,2));
      assertEquals("1.0000000000E-40", Convert.toString(1e-40,10)); // falha no desktop
      assertEquals("-1.0000000000E-40", Convert.toString(-1e-40,10));
      assertEquals("-1.0000000000E40", Convert.toString(-1e40,10));
      assertEquals("-1.0000000000E42", Convert.toString(-100e40,10));
      assertEquals("9.9991234568E225", Convert.toString(9999.1234567895E222,10));
      assertEquals("-1.432133086861E300", Convert.toString(-1.-1.4321330868609868096E300,12));
      assertEquals("-1.4321330868609860E300", Convert.toString(-1.4321330868609868096E300,16));
      assertEquals("-9.432133086861E300", Convert.toString(-9.4321330868609868096E300,12));
      assertEquals("-9.4321330868609800E300", Convert.toString(-9.4321330868609868096E300,16));
      assertEquals("1.432133086861E300", Convert.toString(1.4321330868609868096E300,12));
      assertEquals("1.4321330868609860E300", Convert.toString(1.4321330868609868096E300,16));
      assertEquals("9.432133086861E300", Convert.toString(9.4321330868609868096E300,12));
      assertEquals("9.4321330868609800E300", Convert.toString(9.4321330868609868096E300,16));
      assertEquals("1.432133086861E-300", Convert.toString(1.4321330868609868096E-300,12));
      assertEquals("1.4321330868609860E-300", Convert.toString(1.4321330868609868096E-300,16));
      assertEquals("9.432133086861E-300", Convert.toString(9.4321330868609868096E-300,12));
      assertEquals("9.4321330868609800E-300", Convert.toString(9.4321330868609868096E-300,16));
      assertEquals("1000000.00000", Convert.toString(1000000.0,5));
      assertEquals("9999999", Convert.toString(9999999.0,0));
      assertEquals("0.9999999999900", Convert.toString(0.99999999999,13)); // falha no android
      assertEquals("0.999999999999000", Convert.toString(0.999999999999,15));
      assertEquals("0.999999999999900", Convert.toString(0.9999999999999,15));
      assertEquals("0.999999999999990", Convert.toString(0.99999999999999,15));
      assertEquals("0.999999999999999", Convert.toString(0.999999999999999,15));
      assertEquals("1.000000000000000", Convert.toString(0.9999999999999999,15));
      assertEquals("1.9999999999900", Convert.toString(1.99999999999,13));
      assertEquals("1.99999999999000", Convert.toString(1.99999999999,14));
      assertEquals("1.999999999990000", Convert.toString(1.99999999999,15));
      assertEquals("1.999999999999000", Convert.toString(1.999999999999,15));
      assertEquals("1.999999999999900", Convert.toString(1.9999999999999,15));
      assertEquals("1.999999999999990", Convert.toString(1.99999999999999,15));
      assertEquals("1.999999999999999", Convert.toString(1.999999999999999,15));
      assertEquals("2.000000000000000", Convert.toString(1.9999999999999999,15));
      assertEquals("-1.00E308", Convert.toString(-1e308d,2));
      assertEquals("+Inf", Convert.toString(1.0/0.0,2));
      assertEquals("-Inf", Convert.toString(-1.0/0.0,2));
   }

   private void toString_StringPrecision()
   {
      try
      {
         assertEquals("10.0", Convert.toString("10d",-1));
         assertEquals("0", Convert.toString("0d",0));
         assertEquals("0.00", Convert.toString("0d",2));
         assertEquals("1", Convert.toString("0.5",0));
         assertEquals("0.5", Convert.toString("0.5",1));
         assertEquals("0.50", Convert.toString("0.5",2));
         assertEquals("0.5555556", Convert.toString("0.55555555555",7));
         assertEquals("0.555555555550", Convert.toString("0.55555555555",12));
         assertEquals("10000", Convert.toString("10000.0",0));
         assertEquals("1000.54322", Convert.toString("1000.543216",5));
         assertEquals("-100.54", Convert.toString("-100.54",2));
         assertEquals("1.0000000000E-40", Convert.toString("1e-40",10));
         assertEquals("-1.0000000000E-40", Convert.toString("-1e-40",10));
         assertEquals("-1.0000000000E40", Convert.toString("-1e40",10));
         assertEquals("1000000.00000", Convert.toString("1000000.0",5));
         assertEquals("9999999", Convert.toString("9999999.0",0));
         assertEquals("0.9999999999900", Convert.toString("0.99999999999",13));
         assertEquals("1.9999999999900", Convert.toString("1.99999999999",13));
         assertEquals("1.99999999999000", Convert.toString("1.99999999999",14));
         assertEquals("1.999999999990000", Convert.toString("1.99999999999",15));
         assertEquals("-1.00E308", Convert.toString("-1e308d",2));
      } catch (InvalidNumberException ine) {fail(ine.getMessage());}
   }

   private void toString_Float()
   {
      assertEquals("0.0", Convert.toString(0f));
      assertEquals("0.5", Convert.toString(0.5f));
      assertEquals("0.55", Convert.toString(0.55f));
      assertEquals("0.555", Convert.toString(0.555f));
      assertEquals("0.55555", Convert.toString(0.55555f));
      assertEquals("0.0055556", Convert.toString(0.00555555f));
      assertEquals("0.0000556", Convert.toString(0.0000555555f));
      assertEquals("0.0000006", Convert.toString(0.000000555555f));
      assertEquals(win32?"5.555549E-9":"5.55555E-9", Convert.toString(0.00000000555555f));
      assertEquals("10000.0", Convert.toString(10000.0f));
      assertEquals("1000.5432", Convert.toString(1000.543216f));
      assertEquals("-100.54", Convert.toString(-100.54f));
      assertEquals(win32?"9.0E-21":"1.0E-20", Convert.toString(1e-20f));
      assertEquals(win32?"-9.0E-21":"-1.0E-20", Convert.toString(-1e-20f));
      assertEquals("-1.0E20", Convert.toString(-1e20f));
      assertEquals("1000000.0", Convert.toString(1000000.0f));
      assertEquals("9999999.0", Convert.toString(9999999.0f));
      assertEquals("0.9999999", Convert.toString(0.9999999f));
      assertEquals("1.999999", Convert.toString(1.9999999f));
      assertEquals(win32?"-1.4321327E30":"-1.4321326E30", Convert.toString(-1.432132689765E30f));
      assertEquals("-9.454938E30", Convert.toString(-9.454938759257E30f));
      assertEquals(win32?"1.4321327E30":"1.4321326E30", Convert.toString(1.432132689765E30f));
      assertEquals("9.454938E30", Convert.toString(9.454938759257E30f));
      assertEquals("1.4321327E-30", Convert.toString(1.432132689765E-30f));
      assertEquals("9.454938E-30", Convert.toString(9.454938759257E-30f));
      assertEquals("-Inf", Convert.toString((float)-1e177));
      assertEquals("+Inf", Convert.toString((float)1e177));
   }

   private void toString_FloatPrecision()
   {
      assertEquals("10.0", Convert.toString(10f,-1));
      assertEquals("0", Convert.toString(0f,0));
      assertEquals("0.00", Convert.toString(0f,2));
      assertEquals("0.0000", Convert.toString(0f,4));
      assertEquals("1", Convert.toString(0.5f,0));
      assertEquals("0.5", Convert.toString(0.5f,1));
      assertEquals("0.50", Convert.toString(0.5f,2));
      assertEquals("0.5556", Convert.toString(0.55555f,4));
      assertEquals("0.5555550", Convert.toString(0.555555f,7));
      assertEquals("0.5555550", Convert.toString(0.555555f,7));
      assertEquals("0.0055556", Convert.toString(0.00555555f,7));
      assertEquals("0.0005556", Convert.toString(0.000555555f,7));
      assertEquals("0.0000556", Convert.toString(0.0000555555f,7));
      assertEquals("0.0000006", Convert.toString(0.0000005555f,7));
      assertEquals("10000", Convert.toString(10000.0f,0));
      assertEquals("1000.54320", Convert.toString(1000.543216f,5));
      assertEquals("-100.54", Convert.toString(-100.54f,2));
      assertEquals(win32?"9.9999990000E-39":"1.0000000000E-38", Convert.toString(1e-38f,10));
      assertEquals(win32?"-9.9999990000E-39":"-1.0000000000E-38", Convert.toString(-1e-38f,10));
      assertEquals("-1.0000000000E30", Convert.toString(-1e30f,10));
      assertEquals("1000000.00000", Convert.toString(1000000.0f,5));
      assertEquals(win32?"9999999":"10000000", Convert.toString(9999999.0f,0));
      assertEquals("1.0000000", Convert.toString(0.99999999999f,7));
      assertEquals("2.0000000", Convert.toString(1.99999999999f,7));
      assertEquals("2.00000000", Convert.toString(1.99999999999f,8));
      assertEquals("2.000000000", Convert.toString(1.99999999999f,9));
      assertEquals("-1.432133E30", Convert.toString(-1.432132689765E30f,6));
      assertEquals(win32?"-1.43213270E30":"-1.43213260E30", Convert.toString(-1.432132689765E30f,8));
      assertEquals("-9.454939E30", Convert.toString(-9.454938759257E30f,6));
      assertEquals("-9.45493800E30", Convert.toString(-9.454938759257E30f,8));
      assertEquals("1.432133E30", Convert.toString(1.432132689765E30f,6));
      assertEquals(win32?"1.43213270E30":"1.43213260E30", Convert.toString(1.432132689765E30f,8));
      assertEquals("9.454939E30", Convert.toString(9.454938759257E30f,6));
      assertEquals("9.45493800E30", Convert.toString(9.454938759257E30f,8));
      assertEquals("1.432133E-30", Convert.toString(1.432132689765E-30f,6));
      assertEquals("1.43213270E-30", Convert.toString(1.432132689765E-30f,8));
      assertEquals("9.454939E-30", Convert.toString(9.454938759257E-30f,6));
      assertEquals("9.45493800E-30", Convert.toString(9.454938759257E-30f,8));
      assertEquals(win32?"-9.00E37":"-1.00E38", Convert.toString(-1e38f,2));
      assertEquals("+Inf", Convert.toString(1.0f/0.0f,2));
      assertEquals("-Inf", Convert.toString(-1.0f/0.0f,2));
   }

   private void toString_Int()
   {
      assertEquals("-2147483648",Convert.toString(-2147483648));
      assertEquals("2147483647",Convert.toString(2147483647));
      assertEquals("12345678", Convert.toString(12345678));
      assertEquals("-12345678", Convert.toString(-12345678));
      assertEquals("305419896", Convert.toString((int)0x12345678));
      assertEquals("999", Convert.toString(+999));
      assertEquals("-999", Convert.toString(-999));
      assertEquals("1", Convert.toString(1));
      assertEquals("-1", Convert.toString(-1));
      assertEquals("0", Convert.toString(0));
   }

   private void unsigned2hex()
   {
      assertEquals("BBAA", Convert.unsigned2hex(0xFFEEBBAA,4)); // signed value
      assertEquals("FFEEBBAA", Convert.unsigned2hex(0xFFEEBBAA,8)); // signed value
      assertEquals("0FEEBBAA", Convert.unsigned2hex(0xFEEBBAA,8)); // unsigned value
   }

   private void toLowerCase()
   {
      assertEquals('z', Convert.toLowerCase('Z'));
      assertEquals('a', Convert.toLowerCase('a'));
      assertEquals('ç', Convert.toLowerCase('ç'));
      assertEquals('ç', Convert.toLowerCase('Ç'));
      assertEquals( 0 , Convert.toLowerCase((char)0));
      assertEquals(0x1E01, Convert.toLowerCase((char)0x1E00));
      assertEquals(0x1E01, Convert.toLowerCase((char)0x1E01));
   }

   private void toUpperCase()
   {
      assertEquals('Z', Convert.toUpperCase('z'));
      assertEquals('A', Convert.toUpperCase('A'));
      assertEquals('Ç', Convert.toUpperCase('ç'));
      assertEquals('Ç', Convert.toUpperCase('Ç'));
      assertEquals( 0 , Convert.toUpperCase((char)0));
      assertEquals(0x1E00, Convert.toUpperCase((char)0x1E01));
      assertEquals(0x1E00, Convert.toUpperCase((char)0x1E00));
   }

   private void replace()
   {
      assertEquals("-8-8123--8856-8-890", Convert.replace("7878123778856787890","7","-"));
      assertEquals("--8--8123----8856--8--890", Convert.replace("7878123778856787890","7","--"));
      assertEquals("--1237-856--90", Convert.replace("7878123778856787890","78","-"));
      assertEquals("------1237---856------90", Convert.replace("7878123778856787890","78","---"));
      assertEquals("8812388568890", Convert.replace("7878123778856787890","7",""));
      assertEquals("123785690", Convert.replace("7878123778856787890","78",""));
      assertEquals("-8-8123--8856-890", Convert.replace("78781237788567890","7","-"));
      assertEquals("--8--8123----8856--890", Convert.replace("78781237788567890","7","--"));
      assertEquals("--1237-856-90", Convert.replace("78781237788567890","78","-"));
      assertEquals("------1237---856---90", Convert.replace("78781237788567890","78","---"));
      assertEquals("881238856890", Convert.replace("78781237788567890","7",""));
      assertEquals("123785690", Convert.replace("78781237788567890","78",""));
      assertEquals("-8-8123-856-890", Convert.replace("787812378567890","7","-"));
      assertEquals("--8--8123--856--890", Convert.replace("787812378567890","7","--"));
      assertEquals("--123-56-90", Convert.replace("787812378567890","78","-"));
      assertEquals("------123---56---90", Convert.replace("787812378567890","78","---"));
      assertEquals("88123856890", Convert.replace("787812378567890","7",""));
      assertEquals("1235690", Convert.replace("787812378567890","78",""));
      assertEquals("-123-856-890", Convert.replace("712378567890","7","-"));
      assertEquals("--123--856--890", Convert.replace("712378567890","7","--"));
      assertEquals("7123-56-90", Convert.replace("712378567890","78","-"));
      assertEquals("7123---56---90", Convert.replace("712378567890","78","---"));
      assertEquals("123856890", Convert.replace("712378567890","7",""));
      assertEquals("71235690", Convert.replace("712378567890","78",""));
      assertEquals("123-856-890", Convert.replace("12378567890","7","-"));
      assertEquals("123--856--890", Convert.replace("12378567890","7","--"));
      assertEquals("123-56-90", Convert.replace("12378567890","78","-"));
      assertEquals("123---56---90", Convert.replace("12378567890","78","---"));
      assertEquals("123856890", Convert.replace("12378567890","7",""));
      assertEquals("1235690", Convert.replace("12378567890","78",""));
      assertEquals("12356-890", Convert.replace("123567890","7","-"));
      assertEquals("12356--890", Convert.replace("123567890","7","--"));
      assertEquals("12356-90", Convert.replace("123567890","78","-"));
      assertEquals("12356---90", Convert.replace("123567890","78","---"));
      assertEquals("12356890", Convert.replace("123567890","7",""));
      assertEquals("1235690", Convert.replace("123567890","78",""));
      assertEquals("123456-890", Convert.replace("1234567890","7","-"));
      assertEquals("123456--890", Convert.replace("1234567890","7","--"));
      assertEquals("123456-90", Convert.replace("1234567890","78","-"));
      assertEquals("123456---90", Convert.replace("1234567890","78","---"));
      assertEquals("123456890", Convert.replace("1234567890","7",""));
      assertEquals("12345690", Convert.replace("1234567890","78",""));
      assertEquals("1234-56-890", Convert.replace("12347567890","7","-"));
      assertEquals("1234--56--890", Convert.replace("12347567890","7","--"));
      assertEquals("1234756-90", Convert.replace("12347567890","78","-"));
      assertEquals("1234756---90", Convert.replace("12347567890","78","---"));
      assertEquals("123456890", Convert.replace("12347567890","7",""));
      assertEquals("123475690", Convert.replace("12347567890","78",""));
      assertEquals("123-56-890", Convert.replace("1237567890","7","-"));
      assertEquals("123--56--890", Convert.replace("1237567890","7","--"));
      assertEquals("123756-90", Convert.replace("1237567890","78","-"));
      assertEquals("123756---90", Convert.replace("1237567890","78","---"));
      assertEquals("12356890", Convert.replace("1237567890","7",""));
      assertEquals("12375690", Convert.replace("1237567890","78",""));
      assertEquals("123-56-890-", Convert.replace("12375678907","7","-"));
      assertEquals("123--56--890--", Convert.replace("12375678907","7","--"));
      assertEquals("123756-907", Convert.replace("12375678907","78","-"));
      assertEquals("123756---907", Convert.replace("12375678907","78","---"));
      assertEquals("12356890", Convert.replace("12375678907","7",""));
      assertEquals("123756907", Convert.replace("12375678907","78",""));
      assertEquals("123-56-890-8", Convert.replace("123756789078","7","-"));
      assertEquals("123--56--890--8", Convert.replace("123756789078","7","--"));
      assertEquals("123756-90-", Convert.replace("123756789078","78","-"));
      assertEquals("123756---90---", Convert.replace("123756789078","78","---"));
      assertEquals("123568908", Convert.replace("123756789078","7",""));
      assertEquals("12375690", Convert.replace("123756789078","78",""));
      assertEquals("-", Convert.replace("7","7","-"));
      assertEquals("--", Convert.replace("7","7","--"));
      assertEquals("7", Convert.replace("7","78","-"));
      assertEquals("7", Convert.replace("7","78","---"));
      assertEquals("", Convert.replace("7","7",""));
      assertEquals("7", Convert.replace("7","78",""));
      assertEquals("-8", Convert.replace("78","7","-"));
      assertEquals("--8", Convert.replace("78","7","--"));
      assertEquals("-", Convert.replace("78","78","-"));
      assertEquals("---", Convert.replace("78","78","---"));
      assertEquals("8", Convert.replace("78","7",""));
      assertEquals("", Convert.replace("78","78",""));
   }
   
   public void testRun()
   {
      learning = true;
      // the tests are in the same order of implementation - so its easy to find the assertion #
      detectSortTypeAndQuickSort();
      insertLineBreak();
      rol();
      ror();
      chars2int();
      tokenizeString_StringChar();
      tokenizeString_StringString();
      zeroPad();
      toString_IntRadix();
      toLong_StringRadix();
      toLong_String();
      toString_LongRadix();
      toString_Long();
      digitOf();
      forDigit();
      cloneStringArray();
      toStringArray();
      toInt();
      toString_Boolean();
      toString_Char();
      doubleToIntBits();
      intBitsToDouble();
      longBitsToDouble();
      doubleToLongBits();
      toDouble();
      toString_Double();
      toString_DoublePrecision();
      toString_StringPrecision();
      toString_Float();
      toString_FloatPrecision();
      toString_Int();
      unsigned2hex();
      toLowerCase();
      toUpperCase();
      replace();
      dup();
      spacePad();
   }
}
