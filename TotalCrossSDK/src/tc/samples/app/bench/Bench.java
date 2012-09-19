/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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



package tc.samples.app.bench;

import totalcross.sys.*;
import totalcross.ui.Label;
import totalcross.ui.MainWindow;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.media.Sound;

public class Bench extends MainWindow
{
   static
   {
      totalcross.sys.Settings.applicationId = "TCBe";
   }

   class TestClass
   {
      public int field1 = 70;
      public int field2 = 80;

      public int method1(int value)
      {
         return (field1 % value) + field2 / 2;
      }

      public int method2(int value)
      {
         return (field2 % value) + field1 / 2;
      }
   }

   ////////// random - by Sean Luke /////////////////
   private static int lastSeed = 234123;

   /** bits should be <= 31. used by the random method */
   private static int next(final int bits)
   {
      int IA = 16807;
      int IM = 2147483647;
      int IQ = 127773;
      int IR = 2836;
      int k = lastSeed / IQ;
      lastSeed = IA * (lastSeed - k * IQ) - IR * k;
      if (lastSeed < 0)
         lastSeed += IM;
      return lastSeed >> (31 - bits);
   }

   /**
    * This is a simple Linear Congruential Generator which produces random
    * numbers in the range [0,2^31), derived from ran0 in Numerical Recipies.
    * Note that ran0 isn't wonderfully random -- there are much better
    * generators out there -- but it'll do the job, and it's fast and has low
    * memory consumption.
    *
    * @param _seed
    *           if > 0, sets the seed, if = 0, uses the last seed. You should
    *           call <code>Math.random(Vm.getTimeStamp());</code>
    * @return a positive 32 bits random int
    */
   // guich@120: corrected so it uses the last rand if seed == 0.
   public static int rrandom(int _seed)
   {
      if (_seed != 0)
      {
         // strip out the minus-sign if any
         lastSeed = (_seed << 1) >>> 1;
      }
      int val = next(31);
      if (val < 0)
         val = -val;
      return val;
   }

   public int field1 = 70;
   public int field2 = 80;

   public Bench()
   {
   }

   public void initUI()
   {
      runTests();
   }

   public void runTests()
   {
      Sound.beep();
      Graphics g = getGraphics();
      String s;
      int start, end, value;

      // paint tests
      g.backColor = Color.WHITE;
      g.fillRect(0, 0, 160, 160);
      g.foreColor = Color.BLACK;
      start = Vm.getTimeStamp();
      for (int i = 0; i < 8000; i++)
      {
         switch (rrandom(0) % 2)
         {
            case 0:
               g.drawRect(rrandom(0) % 150, rrandom(0) % 150, 10, 10);
               break;
            case 1:
               g.drawLine(rrandom(0) % 150, rrandom(0) % 150, rrandom(0) % 150, rrandom(0) % 150);
               break;
         }
      }
      end = Vm.getTimeStamp();
      updateScreen();
      add(new Label(s = ("native meth./graph: " + (end - start) + " ms")), LEFT, AFTER);
      Vm.debug(s);

      // loop test
      start = Vm.getTimeStamp();
      value = 100;
      for (int i = 0; i < 100000; i++)
      {
         value = value / 7;
         if (value == 0)
            value = i / 3;
      }
      end = Vm.getTimeStamp();
      add(new Label(s = ("Loop: " + (end - start) + " ms")), LEFT, AFTER);
      Vm.debug(s);
      add(new Label("" + value));

      // field test
      TestClass test = new TestClass();
      start = Vm.getTimeStamp();
      value = 100;
      for (int i = 0; i < 10000; i++)
      {
         value += test.field1 + test.field2;
         test.field1 = i;
         test.field2 = i;
         value = value % 100;
      }
      end = Vm.getTimeStamp();
      add(new Label(s = ("Field: " + (end - start) + " ms")), LEFT, AFTER);
      Vm.debug(s); // method test
      start = Vm.getTimeStamp();
      value = 100;
      for (int i = 1; i < 10000; i++)
      {
         value = test.method1(i);
         value += test.method2(i);
         value = value % 100;
      }
      end = Vm.getTimeStamp();
      add(new Label(s = ("Method: " + (end - start) + " ms")), LEFT, AFTER);
      Vm.debug(s);
      add(new Label("" + value));

      // array test
      byte array[] = new byte[30];
      start = Vm.getTimeStamp();
      for (int i = 0; i < 10000; i++)
         for (int j = 0; j < 30; j++)
            array[j] = (byte) (array[j] + (j / 2));
      end = Vm.getTimeStamp();
      add(new Label(s = ("Array: " + (end - start) + " ms")), LEFT, AFTER);
      Vm.debug(s);

      // string test
      String strings[] = new String[30];
      for (int i = 0; i < 30; i++)
         strings[i] = "Num" + i;
      start = Vm.getTimeStamp();
      String z = null;
      for (int i = 0; i < 100; i++)
      {
         z = "All";
         for (int j = 0; j < 30; j++)
            z += strings[j];
      }
      s = z;
      end = Vm.getTimeStamp();
      add(new Label(s = ("String: " + (end - start) + " ms")), LEFT, AFTER);
      Vm.debug(s);

      add(new Label("click to exit"), CENTER, BOTTOM);
      add(new Label("results dumped to console"), CENTER, BEFORE - 2);
   }

   public void onEvent(Event e)
   {
      if (e.type == PenEvent.PEN_DOWN)
         exit(0);
   }
}
