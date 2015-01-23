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



package tc.samples.app.calc;

import totalcross.io.*;
import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.util.*;

/**
 * Full calculator program to serve as a SuperWaba example. This software is
 * released as freeware at PalmGear
 */

public class Calculator extends MainWindow
{
   // variables related to the user interface
   private Edit            edNum;
   private TabbedContainer tpOpers;
   private ListBox         lbHist;
   private PushButtonGroup pbgOpers1, pbgOpers2;
   private Radio           rbDec, rbHex, rbBin, rbRad, rbDeg;
   private MenuBar         mbar;
   private String          catName  = "History." + Settings.applicationId + ".DATA";
   private MenuItem        miSaveHist;

   private String          opers1[] = {"7", "8", "9", "/", "A", "B", "4", "5", "6", "*", "C", "D", "1", "2", "3", "-",
         "E", "F", "0", " +-", ".", "+", "=", "clr"};
   private String          opers2[] = {"e^x", "Ln", "Log", "X^2", "X^3", "X^Y", "1/x", "Sqr", "N!", "Mod", "Rand",
         "Sin", "Cos", "Tan", "Int", "Shl", "Shr", "Rol", "Ror", "clr", "And", "Or", "Xor", "Not", "="};

   public Calculator()
   {
      super("Calculator", BORDER_NONE);
      setUIStyle(Settings.Android);
      setTitle("");
   }

   private static final int BACK = 0x50A0FF;
   public void initUI()
   {
      setBackColor(Color.WHITE);
      final Bar bar = new Bar("Calculator");
      bar.setBackForeColors(BACK,Color.WHITE);
      bar.addButton(Resources.menu);
      bar.addButton(Resources.exit);
      bar.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
            switch (bar.getSelectedIndex())
            {
               case 0: exit(0); break;
               case 1: popupMenuBar(); break;
            }
         }
      });
      add(bar,LEFT,0,FILL,fmH*3/2);
      // add a menubar
      MenuItem col0[] = {
            new MenuItem(" File "),
            new MenuItem("Set precision"),
            new MenuItem(),
            miSaveHist = new MenuItem("Save history", false),
            new MenuItem("Clear history"),
            new MenuItem(),
            new MenuItem("Exit"),
      };
      MenuItem col1[] = {
            new MenuItem(" Edit "),
            new MenuItem("Copy"),
            new MenuItem("Paste"),
      };
      MenuItem col2[] = {
            new MenuItem("   ?   "),
            new MenuItem("Instructions"),
            new MenuItem("About"),
      };
      setMenuBar(mbar = new MenuBar(new MenuItem[][] {col0, col1, col2}));
      mbar.setBackForeColors(BACK, Color.WHITE);
      mbar.setCursorColor(BACK);
      mbar.setBorderStyle(NO_BORDER);
      mbar.setPopColors(0x0078FF, Color.CYAN, -1); // use the default cursor color for the popup menu (last null param)

      // restore app settings
      String[] history = null;

      if (Settings.appSettings != null)
      {
         try
         {
            miSaveHist.isChecked = Settings.appSettings.charAt(0) == '1';
            places = Convert.toInt(Settings.appSettings.substring(1));
         }
         catch (Exception e)
         {
            Settings.appSettings = null;
         } // corrupted appSettings...

         if (miSaveHist.isChecked)
         {
            PDBFile cat;
            try
            {
               cat = new PDBFile(catName, PDBFile.READ_WRITE);
               DataStream ds = new DataStream(cat);
               history = ds.readStringArray();
               cat.close();
            }
            catch (totalcross.io.IOException e)
            {
               e.printStackTrace();
            } // corrupted or not found catalog
         }
      }
      dotZero = '.' + Convert.zeroPad("", places);

      String tits[] = {"Basic", "Advanced", "History"};
      int x; // load the right font and check if it was successfully loaded
      Font tinyFont = Settings.screenWidth < 200 ? Font.getFont(false, Font.getDefaultFontSize()-3) : this.font;
      // add the numbers Edit filling until end and with preferred height
      edNum = new Edit(); // here we must use the tiny font so a 32bit binary number can be entirely shown
      edNum.setFont(tinyFont);
      edNum.setMode(Edit.CURRENCY);
      edNum.setText("0");
      edNum.setEditable(false);
      edNum.setKeyboard(Edit.KBD_NONE);
      add(edNum);
      edNum.setRect(4, AFTER + 2, FILL - 4, PREFERRED); // The operations TabPanel will occupy the rest of the available space
      add(tpOpers = new TabbedContainer(tits));
      tpOpers.setRect(0, AFTER + 10, FILL, FILL);
      tpOpers.getContainer(0).setBackColor(Color.WHITE);
      tpOpers.getContainer(1).setBackColor(Color.WHITE);
      tpOpers.getContainer(2).setBackColor(Color.WHITE);
      tpOpers.setBackForeColors(BACK,Color.WHITE);
      // panel 0: Basic operations // add the basic operation PushButtonGroup
      Container panel = tpOpers.getContainer(0);
      panel.add(pbgOpers1 = new PushButtonGroup(opers1, false, -1, 5, 8, 4, true, PushButtonGroup.BUTTON)); // add the base radios
      RadioGroupController rg = new RadioGroupController();
      panel.add(rbDec = new Radio("Dec", rg));
      panel.add(rbHex = new Radio("Hex", rg));
      panel.add(rbBin = new Radio("Bin", rg)); // appId can be used to store any application data
      // Here we store the base that the radio corresponds to
      rbDec.appId = 10;
      rbHex.appId = 16;
      rbBin.appId = 2; // calculate the ideal position to center the three radios
      x = (getSize().x - (rbDec.getPreferredWidth() + rbHex.getPreferredWidth() + rbBin.getPreferredWidth() + 20)) / 2;
      rbDec.setRect(x, 3, PREFERRED, PREFERRED);
      rbHex.setRect(AFTER + 10, 3, PREFERRED, PREFERRED);
      rbBin.setRect(AFTER + 10, 3, PREFERRED, PREFERRED);
      rbDec.setChecked(true);

      int s = Math.min(Settings.screenWidth, Settings.screenHeight) * 8 / 10;
      pbgOpers1.setRect(CENTER, CENTER, s, s); // panel 1: Advanced operations
      panel = tpOpers.getContainer(1); // add the radians/degrees radios
      rg = new RadioGroupController(); // criamos outro radio group
      panel.add(rbDeg = new Radio("Deg", rg));
      panel.add(rbRad = new Radio("Rad", rg)); // calculate the ideal position to center the two radios
      x = (getSize().x - (rbRad.getPreferredWidth() + rbDeg.getPreferredWidth() + 10)) / 2;
      rbDeg.setRect(x, 2, PREFERRED, PREFERRED);
      rbRad.setRect(AFTER + 10, 2, PREFERRED, PREFERRED);
      rbDeg.setChecked(true); // add the PushButtonGroup with the math operations
      panel.add(pbgOpers2 = new PushButtonGroup(opers2, false, -1, 3, 6, 5, true, PushButtonGroup.BUTTON));
      pbgOpers2.setRect(CENTER, CENTER, s,s); // panel 2: History
      tpOpers.getContainer(2).add(lbHist = new ListBox(history));
      lbHist.enableHorizontalScroll();
      lbHist.setFont(tinyFont); // make the history occupy all the available area
      lbHist.setRect(-2, -2, FILL + 2, FILL + 2);
      pbgOpers1.setBackForeColors(BACK,Color.WHITE);
      pbgOpers2.setBackForeColors(BACK,Color.WHITE);
   }

   // ///////////////////////////////////////////////////////////////////////////////////
   public void onExit()
   {
      // Save the current settings
      Settings.appSettings = "" + (miSaveHist.isChecked ? '1' : '0') + places;
      // Save the current history (if wanted)
      PDBFile cat;
      try
      {
         cat = new PDBFile(catName, PDBFile.CREATE_EMPTY);

         if (miSaveHist.isChecked && lbHist.size() > 0)
         {
            ResizeRecord rs = new ResizeRecord(cat, 2048);
            DataStream ds = new DataStream(rs);
            rs.startRecord();
            ds.writeStringArray((String[]) lbHist.getItems());
            rs.endRecord();
            ds.close();
         }
      }
      catch (IOException e1)
      {
         e1.printStackTrace();
      }
   }

   // ///////////////////////////////////////////////////////////////////////////////////
   // application related variables
   private int     DEC     = 10;
   private int     HEX     = 16;
   private int     BIN     = 2;
   private boolean radians;
   private Random  rand    = new Random();
   private int     places  = 6;
   private String  dotZero;
   private boolean startOver;

   /** current base */
   private int     base    = DEC;
   /** current angle */
   // private int angle = GRA;
   /** current operation */
   private char    op      = (char) -1;
   /** operators */
   private String  oper1;

   /** if true the number is replaced by a new one */

   /** Process the application's events */
   public void onEvent(Event event)
   {
      if (edNum == null)
         return;
      int ind;
      String text = edNum.getText();

      switch (event.type)
      // always good put event.type in a switch instead of various if's
      {
         case ControlEvent.PRESSED: // anything pressed?
            try
            {
               if (event.target == mbar)
                  handleMenuEvent(mbar.getSelectedIndex());
               else
               // changed tabs?
               if (event.target == tpOpers && tpOpers.getActiveTab() == 2)
                  lbHist.requestFocus(); // let the user use the page up/down key to scroll the history
               else
               // basic/advanced operation?
               if (event.target instanceof PushButtonGroup && (ind = ((PushButtonGroup) event.target).getSelectedIndex()) != -1)
               {
                  char c;
                  if (event.target == pbgOpers2)
                     c = (char) ind;
                  else
                     c = opers1[ind].charAt(0);
                  switch (c)
                  {
                     // was a decimal number?
                     case '0':
                     case '1':
                     case '2':
                     case '3':
                     case '4':
                     case '5':
                     case '6':
                     case '7':
                     case '8':
                     case '9':
                     {
                        if (startOver)
                        {
                           text = "";
                           startOver = false;
                        }
                        if (base != BIN || c <= '1') // is the char valid in this base?
                           setNum(text + c);
                        break;
                     }
                        // was a hexadecimal number?
                     case 'A':
                     case 'B':
                     case 'C':
                     case 'D':
                     case 'E':
                     case 'F':
                     {
                        if (startOver)
                        {
                           text = "";
                           startOver = false;
                        }
                        if (base == HEX)
                           setNum(text + c);
                        break;
                     }
                     case ' ': // +-
                     {
                        try
                        {
                           double value = Convert.toDouble(text);
                           if (value < 0)
                              setNum(text.substring(1)); // remove the minus sign
                           else
                              setNum('-' + text); // put the minus sign
                        } catch (InvalidNumberException ine) {}
                        break;
                     }
                     case 19: // Clr - opers2
                     case 'c': // clr - opers1
                     {
                        setNum("0");
                        oper1 = null;
                        break;
                     }
                     case '.':
                     {
                        if (startOver)
                        {
                           text = "";
                           startOver = false;
                        }
                        if (base == DEC && text.indexOf('.') == -1) // base other than decimal can't have a decimal point
                           setNum(text + '.');
                        break;
                     }
                     case 24:
                        c = '='; // make easier
                     default: // =+-/* and advanced operations
                        preCompute(c);
                  }
               }
               else
               // base conversion ?
               if ((event.target == rbDec || event.target == rbHex || event.target == rbBin) && base != ((Control) event.target).appId) // appId stores the base that is represented by the control
               {
                  if (!text.equals("0")) // if not empty...
                  {
                     String hist = (base == 2 ? trimZeros(text) : text) + '(' + base + ") -> ";
                     // if is base 10 and just finished an operation, maybe ends with .0000; strip it
                     if (base == 10 && text.indexOf('.') >= 0)
                        text = text.substring(0, text.indexOf('.'));
                     // convert from the preceding base to base 10
                     long l = Convert.toLong(text, base);
                     // store the new base
                     base = ((Control) event.target).appId;
                     // and convert to the new one
                     String s = Convert.toString(l, base).toUpperCase();
                     // show it
                     hist += (base == 2 ? trimZeros(s) : s) + '(' + base + ')';
                     addHist(hist);
                     setNum(s);
                  }
                  else
                  {
                     // just inform the base was changed
                     base = ((Control) event.target).appId;
                     addHist("-> " + base);
                  }
               }
               else
               // grade convertion (radians)?
               if (event.target == rbRad)
               {
                  double d = Convert.toDouble(text);
                  if (d != 0)
                  {
                     d = Math.toDegrees(d);
                     String s = Convert.toString(d, places);
                     setNum(s);
                     addHist("Rad(" + text + ") -> Deg(" + s + ")");
                  }
                  else
                     addHist("-> Rad");
                  radians = true;
               }
               else
               // grade convertion (degrees)?
               if (event.target == rbDeg)
               {
                  double d = Convert.toDouble(text);
                  if (d != 0)
                  {
                     d = Math.toRadians(d);
                     String s = Convert.toString(d, places);
                     setNum(s);
                     addHist("Deg(" + text + ") -> Rad(" + s + ")");
                  }
                  else
                     addHist("-> Deg");
                  radians = false;
               }
               else
               // any history item was clicked?
               if (event.target == lbHist)
               {
                  String s = (String) lbHist.getSelectedItem();
                  if ((ind = s.indexOf('=')) != -1)
                     setNum(s.substring(ind + 2));
               }
            } catch (InvalidNumberException ine) {addHist(ine.getMessage());}
            break; // case ControlEvent.PRESSED
      }
   }

   private void handleMenuEvent(int item)
   {
      switch (item)
      {
         case 1: // set precision
            InputBox id = new InputBox("Set Precision",
                  "Please enter the number\nof decimal places to be used\n(2 to 12):", "" + places);
            id.getEdit().setMode(Edit.CURRENCY);
            id.popup();
            if (id.getPressedButtonIndex() == 0)
               try
               {
                  int n = Convert.toInt(id.getValue());
                  if (n < 2)
                     n = 2;
                  else
                  if (n > 12)
                     n = 12;
                  places = n;
                  dotZero = '.' + Convert.zeroPad("", places);
               } catch (InvalidNumberException ine) {}
            break;
         case 4: // clear history
            lbHist.removeAll();
            break;
         case 6: // exit
            exit(0);
            break;
         case 101: // copy
            Vm.clipboardCopy(edNum.getText());
            break;
         case 102: // paste
            String temp = Vm.clipboardPaste();
            if (temp.length() > 0)
            {
               oper1 = temp;
               setNum(edNum.getText());
            }
            break;
         case 201: // instructions
            new MessageBox(
                  "Instructions",
                  "You must type the operator 1,\nthe operation, and, in some\ncases, the operator 2 and\nthen type = to compute\nthe result. Some operations\nrequire two or one operators.\nThere's no operator precedence.\nAfter computing the value,\nthe result is assigned to\noperator 1. Some operations\nin the advanced tab require\nan integer; if there is\na floating point value, it\nwill be truncated (E.g.:\n3.67! = 3! = 6). Clicking\non the history places the\nresult in the operator 1.\nSelecting File/Save History\nstores and retrieves the history\nfrom a database.").popupNonBlocking();
            break;
         case 202: // about
            new MessageBox(
                  "About",
                  "TotalCross Calculator 3.0\nExample program for the\nTotalCross SDK. This software\nis freeware.\nCreated by Guilherme C. Hazan\nwww.totalcross.com").popupNonBlocking();
            break;
      }
   }

   private String format(String s)
   {
      if (base == BIN)
      {
         // remove the minus sign if base=2
         if (s.length() > 1 && s.charAt(0) == '-')
            s = s.substring(1);
         // the result must have exactly 32 chars
         int d = s.length() - 32;
         if (d < 0) // add zeros
            s = Convert.zeroPad(s, 32);
         else
            if (d > 0) // remove zeros
               s = s.substring(d);
      }
      else
      {
         // remove starting zeroes
         if (s.length() > 1)
            s = trimZeros(s);
         // change .00 to 0.00
         if (s.charAt(0) == '.')
            s = '0' + s;
         // strip .00
         if (s.endsWith(dotZero))
            s = s.substring(0, s.length() - places - 1);
         // if hex mode, convert to upper case
         if (base == HEX)
            s = s.toUpperCase();
      }
      return s;
   }

   // format the output and display it.
   private void setNum(String s)
   {
      edNum.setText(format(s));
   }

   // remove zeroes at the start of the string
   private String trimZeros(String s)
   {
      if (s.length() == 0)
         return "0";
      char[] ac = s.toCharArray();
      int i = 0;
      while (i < ac.length && ac[i] == '0')
         i++;
      return i == 0 ? s : new String(ac, i, ac.length - i);
   }

   // add to the history
   private void addHist(String s)
   {
      // add and select the item
      lbHist.add(s);
      lbHist.setSelectedIndex(lbHist.size() - 1);
      // warn the user if the history gets too big
      if (lbHist.size() % 1000 == 0)
      {
         MessageBox mb = new MessageBox("Attention!", "The history is getting\ntoo large. Do you\nwant to erase it?",
               new String[] {"Yes", "No"});
         mb.popup();
         if (mb.getPressedButtonIndex() == 0)
            lbHist.removeAll();
      }
   }

   private void preCompute(char newOp)
   {
      String text = edNum.getText();
      boolean isEqual = newOp == '=';
      if (!isEqual && requiresOneOperator(newOp))
      {
         String res;
         setNum(res = compute(text, null, newOp));
         addHist(format(text) + ' ' + getOpText(newOp) + " = " + format(res));
         oper1 = null;
      }
      else
      {
         if (oper1 == null)
            oper1 = text;
         else
         {
            String s = format(oper1) + ' ' + getOpText(op) + ' ' + format(text) + " = ";
            setNum(oper1 = compute(oper1, text, op));
            addHist(s + format(oper1));
         }

         if (isEqual)
            oper1 = null;
         else
            op = newOp;
      }
      startOver = true;
   }

   private String getOpText(char op)
   {
      return op >= '*' ? Convert.toString(op) : opers2[op];
   }

   private String compute(String oper1, String oper2, char op)
   {
      // Vm.debug(oper1+" "+(op>='*'?((char)op+"") : opers2[op])+" "+oper2);

      try
      {
         if (op >= '*') // basic operations?
         {
            // in base 10 we can make operations using floating point
            if (base == DEC)
            {
               double r = 0;
               double op1 = Convert.toDouble(oper1);
               double op2 = Convert.toDouble(oper2);
               switch (op)
               {
                  case '+':
                     r = op1 + op2;
                     break;
                  case '-':
                     r = op1 - op2;
                     break;
                  case '*':
                     r = op1 * op2;
                     break;
                  case '/':
                     r = op1 / op2;
                     break;
               }
               return Convert.toString(r, places); // trunc with the desired places
            }
            else
            // else, we must use the long data type
            {
               long r = 0;
               long op1 = Convert.toLong(oper1, base);
               long op2 = Convert.toLong(oper2, base);
               switch (op)
               {
                  case '+':
                     r = op1 + op2;
                     break;
                  case '-':
                     r = op1 - op2;
                     break;
                  case '*':
                     r = op1 * op2;
                     break;
                  case '/':
                     r = op1 / op2;
                     break;
               }
               return Convert.toString(r, base); // convert back to the desired base
            }
         }
         else
            // advanced
            if (op <= 14 && op != 8)
            {
               double r = 0;
               double op1 = Convert.toDouble(oper1);
               double op2 = oper2 == null ? 0 : Convert.toDouble(oper2);
               switch ((int) op)
               {
                  // double
                  case 0: // exp
                     r = Math.exp(op1);
                     break;
                  case 1: // ln
                     r = Math.log(op1);
                     break;
                  case 2: // log
                     r = Math.log(op1) / Math.log(10);
                     break;
                  case 3: // x^2
                     r = op1 * op1;
                     break;
                  case 4: // x^3
                     r = op1 * op1 * op1;
                     break;
                  case 5: // x^y
                     r = Math.pow(op1, op2);
                     break;
                  case 6: // 1/x
                     r = 1 / op1;
                     break;
                  case 7: // sqr
                     r = Math.sqrt(op1);
                     break;
                  case 9: // Mod
                     r = op1 % op2;
                     break;
                  case 10: // Rnd
                     r = rand.nextDouble();
                     break;
                  case 11: // Sin
                     if (!radians)
                        op1 = Math.toRadians(op1);
                     r = Math.sin(op1);
                     // if (!radians) r = Math.toDegrees(r);
                     break;
                  case 12: // Cos
                     if (!radians)
                        op1 = Math.toRadians(op1);
                     r = Math.cos(op1);
                     // if (!radians) r = Math.toDegrees(r);
                     break;
                  case 13: // Tan
                     if (!radians)
                        op1 = Math.toRadians(op1);
                     r = Math.tan(op1);
                     // if (!radians) r = Math.toDegrees(r);
                     break;
                  case 14: // Int
                     return (long) op1 + "";
               }
               return Convert.toString(r, places);
            }
            else
            {
               long r = 0;
               long op1 = oper1.indexOf('.') != -1 ? (long) Convert.toDouble(oper1) : Convert.toLong(oper1, base);
               long op2 = oper2 == null ? 0 : oper2.indexOf('.') != -1 ? (long) Convert.toDouble(oper2)
                     : Convert.toLong(oper2, base);
               switch ((int) op)
               {
                  case 15: // Shl
                     r = op1 << 1;
                     break;
                  case 16: // Shr
                     r = op1 >> 1;
                     break;
                  case 17: // Rol
                     r = rol(op1);
                     break;
                  case 18: // Ror
                     r = ror(op1);
                     break;
                  case 20: // And
                     r = op1 & op2;
                     break;
                  case 21: // Or
                     r = op1 | op2;
                     break;
                  case 22: // Xor
                     r = op1 ^ op2;
                     break;
                  case 23: // Not
                     r = ~op1;
                     break;
                  case 8: // N!
                     r = factorial((long) Convert.toDouble(oper1));
                     if (r == 0)
                        return "Number too big or invalid";
                     break;
               }
               return Convert.toString(r, base);
            }
      }
      catch (Exception ae)
      {
         return "Err";
      }
   }

   private boolean requiresOneOperator(char op)
   {
      switch (op)
      {
         case '+':
         case '-':
         case '*':
         case '/':
         case 20:
         case 21:
         case 22:
         case 5:
         case 9:
            return false;
      }
      return true;
   }

   private long factorial(long n)
   {
      if (n == 0)
         return 1;
      if (n > 25)
         return 0;
      long r = n;
      while (--n > 1)
         r *= n;
      return r;
   }

   private long rol(long i)
   {
      return ((i << 1) | (((int) i < 0) ? 1 : 0)) & 0xFFFFFFFFL;
   }

   private long ror(long i)
   {
      return (i >> 1) | ((i & 1) != 0 ? 0x80000000L : 0L);
   }
}
