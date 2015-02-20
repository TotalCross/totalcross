/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2001 Allan C. Solomon                                     *
 *  Copyright (C) 2001-2012 SuperWaba Ltda.                                      *
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



package totalcross.ui.dialog;

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.io.*;
import totalcross.sys.*;
import totalcross.util.*;

/**
 The Calendar class displays a calendar with buttons used to advance the month and the year.
 It uses the Date class for all operations.<br>
 Instead of create a new instance (which consumes memory), you may
 use the Edit's static field <i>calendar</i>
 <p>
 If there is something in the edit box which poped up the calendar, the clear
 button will clear it. Cancel will leave whatever was in there.
 <p>
 The month can be changed via keyboard using the left/right keys, and the year can be changed using up/down keys.
*/

public class CalendarBox extends Window
{
   private int day=-1,month,year;
   private int sentDay,sentMonth,sentYear;
   private Button btnToday, btnClear, btnCancel;
   private ArrowButton btnMonthNext,btnMonthPrev,btnYearNext,btnYearPrev;
   private PushButtonGroup pbgDays;
   private String []tempDays = new String[42];
   private StringBuffer sb = new StringBuffer(20);
   private Label lYear,lMonth;
   private String[] myMonthNames;

   /** Defines the array length of year window. If array length is 20, and current year is 2010, the
    * list goes from 2000 to 2019.
    * @since TotalCross 1.66
    */
   public static int YEAR_ARRAY_LENGTH = 20;
   
   /** True if the user had canceled without selecting */
   public boolean canceled;

   /** The 7 week names painted in the control. */
   public static String[] weekNames = {"S","M","T","W","T","F","S"}; // guich#573_39
   /** The labels for Today, Clear and Cancel. */
   public static String[] todayClearCancel = {"Today","Clear","Cancel"}; // guich@573_39
   
   /** The labels between the arrows: Month, Year */
   public static String[] yearMonth = new String[]{"year","month"};

   /**
     * Constructs Calendar set to the current day.
     */
   public CalendarBox()
   {
      super(" ",uiAndroid ? ROUND_BORDER : RECT_BORDER); // no title yet
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
      fadeOtherWindows = Settings.fadeOtherWindows;
      transitionEffect = Settings.enableWindowTransitionEffects ? TRANSITION_OPEN : TRANSITION_NONE;
      String[]defCaps = new String[42];
      for (int i=0; i < 42; i++)
         defCaps[i] = Convert.toString(i+10);
      pbgDays = new PushButtonGroup(defCaps, false, -1, -1, fm.charWidth('@')-2, 6, true, PushButtonGroup.NORMAL);
   }
   
   private void setupUI() // guich@tc100b5_28
   {
      int hh = getClientRect().y+2;
      
      int yearColor = Color.BRIGHT;
      int monthColor = Color.BRIGHT;

      started = true; // avoid calling the initUI method

      yearColor = UIColors.calendarAction;
      monthColor = UIColors.calendarAction;
      setBackForeColors(UIColors.calendarBack, UIColors.calendarFore);

      // compute the window's rect
      Font bold = font.asBold();
      Font mini = font.adjustedBy(-4);
      int labH = bold.fm.height;
      int arrowW = uiAndroid ? 2 * labH / 3 : labH / 2;
      Label l;

      Button.commonGap = 2;
      btnToday = new Button(todayClearCancel[0]);
      btnClear = new Button(todayClearCancel[1]);
      btnCancel = new Button(todayClearCancel[2]);
      btnToday.setFont(font);
      btnClear.setFont(font);
      btnCancel.setFont(font);
      int btnH = btnCancel.getPreferredHeight();
      Button.commonGap = 0;

      int labelGap = Settings.screenWidth <= 320 ? 3 : 0;
      lYear = new Label(Settings.screenWidth <= 320 ? yearMonth[0].substring(0,1) : yearMonth[0],CENTER);
      lMonth = new Label(Settings.screenWidth <= 320 ? yearMonth[1].substring(0,1) : yearMonth[1],CENTER);
      lYear.setFont(mini);
      lMonth.setFont(mini);
      
      pbgDays.insideGap = fm.charWidth('@')-2;
      pbgDays.setFont(font);
      int pbgW = uiAndroid ? (Math.min(fmH*16,Math.min(Settings.screenWidth,Settings.screenHeight))-20)/7*7 : pbgDays.getPreferredWidth();
      int cellWH = pbgW / 7;
      int captionW = bold.fm.getMaxWidth(Date.monthNames,0,Date.monthNames.length) + bold.fm.stringWidth("2011   ");
      int titleW = 4*arrowW + labelGap*4 + lYear.getPreferredWidth() + lMonth.getPreferredWidth() + captionW; // guich@tc130: avoid problems if title is too small

      setRect(CENTER,CENTER,Math.max(titleW,pbgW) + 10, 18+hh+labH+cellWH*6+btnH); // same gap in all corners

      // arrows
      btnYearPrev = new ArrowButton(Graphics.ARROW_LEFT,arrowW,yearColor);
      btnYearNext = new ArrowButton(Graphics.ARROW_RIGHT,arrowW,yearColor);
      btnMonthPrev = new ArrowButton(Graphics.ARROW_LEFT,arrowW,monthColor);
      btnMonthNext = new ArrowButton(Graphics.ARROW_RIGHT,arrowW,monthColor);
      btnYearPrev.setFont(font);
      btnYearNext.setFont(font);
      btnMonthPrev.setFont(font);
      btnMonthNext.setFont(font);
      btnYearPrev.setBorder(Button.BORDER_NONE); 
      btnYearNext.setBorder(Button.BORDER_NONE);
      btnMonthPrev.setBorder(Button.BORDER_NONE);
      btnMonthNext.setBorder(Button.BORDER_NONE);
      btnYearPrev.transparentBackground = btnYearNext.transparentBackground = btnMonthPrev.transparentBackground = btnMonthNext.transparentBackground = true;
      
      int bw = uiAndroid ? PREFERRED+fmH : PREFERRED;
      
      int yb = (titleGap+titleFont.fm.height-labH)/2;
      
      add(btnYearPrev,LEFT+2,yb, bw, PREFERRED);
      add(lYear,AFTER,CENTER_OF,PREFERRED+labelGap*2,SAME);
      add(btnYearNext,AFTER,yb, bw, PREFERRED);
      add(btnMonthNext,RIGHT-2,yb, bw, PREFERRED);
      add(lMonth,BEFORE,CENTER_OF,PREFERRED+labelGap*2,SAME);
      add(btnMonthPrev,BEFORE,yb, bw, PREFERRED);

      // change title alignment to use the area available between the buttons
      titleAlign = CENTER;

      // buttons
      add(btnToday, LEFT+4, BOTTOM-4, PARENTSIZEMIN+30, PREFERRED);
      add(btnClear, CENTER, SAME, PARENTSIZEMIN+30, PREFERRED);
      add(btnCancel, RIGHT-4, SAME, PARENTSIZEMIN+30, PREFERRED);

      // days
      add(pbgDays);
      if (!uiAndroid)
         pbgDays.setSimpleBorder(true);
      pbgDays.setRect(CENTER+3, BEFORE-4,pbgW,6*cellWH+1); // +3 = 6 (buttons that have joined sides) / 2
      pbgDays.setCursorColor(Color.brighter(yearColor));

      // weeks
      int xx = pbgDays.getX();
      for (int i =0; i < 7; i++)
      {
         l = new Label(weekNames[i]);
         l.setFont(bold);
         Rect r = pbgDays.rects[i];
         add(l, xx+r.x+(r.width-l.getPreferredWidth())/2, hh);
      }

      lYear.transparentBackground = lMonth.transparentBackground = true;
      lYear.setForeColor(backColor);
      lMonth.setForeColor(backColor);
      btnToday.setBackColor(UIColors.calendarAction);
      btnClear.setBackColor(UIColors.calendarAction);
      btnCancel.setBackColor(UIColors.calendarAction);
      btnYearPrev.setBackColor(UIColors.calendarArrows);
      btnYearNext.setBackColor(UIColors.calendarArrows);
      btnMonthNext.setBackColor(UIColors.calendarArrows);
      btnMonthPrev.setBackColor(UIColors.calendarArrows);

      tabOrder = new Vector(new Control[]{pbgDays,btnToday,btnClear,btnCancel,btnYearPrev,btnYearNext,btnMonthPrev,btnMonthNext});
      btnYearPrev.autoRepeat = btnYearNext.autoRepeat = btnMonthNext.autoRepeat = btnMonthPrev.autoRepeat = true; // guich@tc122_47
      btnYearPrev.AUTO_DELAY = btnYearNext.AUTO_DELAY = 300;
   }

   /**
   Returns the selected Date.

   @return Date object set to the selected day, or null if an error occurs.
   */
   public Date getSelectedDate()
   {
      if (day==-1)
         return null;
      try
      {
         return new Date(day,month,year);
      } catch (InvalidDateException ide) {return null;}
   }

   /** Sets the current day to the Date specified. If its null, sets the date to today. */
   public void setSelectedDate(Date d)
   {
      if (d == null) d = new Date();
      sentMonth = month = d.getMonth();
      sentYear = year = d.getYear();
      sentDay = day = d.getDay();

      // sets the pbg days
      updateDays();
   }

   private void updateDays()
   {
      try
      {
         Date date = new Date(1,month,year);
         int start = date.getDayOfWeek();
         int end = start+date.getDaysInMonth();
         pbgDays.setSelectedIndex(-1);

         int d = 1;
         String[] days = tempDays;

         for (int i=0; i < 42; i++)
         {
            pbgDays.setColor(i,-1,-1); // erase all colors
            days[i] = (start <= i && i < end) ? Convert.toString(d++) : "";
         }
         if (Settings.keyboardFocusTraversable)
            days[end] = " x"; // add a way to get out of the pbg without closing it
         // set sent color
         if (year == sentYear && month == sentMonth)
            pbgDays.setColor(sentDay-1+start, -1, UIColors.calendarAction);
         pbgDays.setNames(days);

         Window.needsPaint = true;
      } catch (InvalidDateException ide) {}
   }

   public void onEvent(Event event)
   {
      try
      {
         switch (event.type)
         {
            case KeyEvent.SPECIAL_KEY_PRESS:
               KeyEvent ke = (KeyEvent)event;
               if (ke.key == SpecialKeys.KEYBOARD_ABC || ke.key == SpecialKeys.KEYBOARD_123) // closes this window without selecting any day
               {
                  day = sentDay; // guich@tc100
                  unpop();
               }
               else
               if (ke.isDownKey()) // guich@tc122_47: here to below
                  incYear(false);
               else
               if (ke.isUpKey())
                  incYear(true);
               else
               if (ke.isNextKey())
                  incMonth(true);
               else
               if (ke.isPrevKey())
                  incMonth(false);
               break;
            case ControlEvent.PRESSED:
               if (event.target == btnToday)
               {
                  setSelectedDate(null);
                  day = sentDay; // guich@401_14
                  unpop();  // guich@401_14
               } else
               if (event.target == btnClear)
               {
                  day = -1;
                  unpop();
               } else
               if (event.target == btnCancel)
               {
                  canceled = true;
                  unpop();
               }
               else
               if (event.target == pbgDays && pbgDays.getSelectedIndex() >= 0)
               {
                  try
                  {
                     Date date = new Date(Convert.toInt(pbgDays.getSelectedItem()),month,year);
                     day = date.getDay();
                     unpop(); // closes this window
                  } catch (Exception ide) {day = -1;}
               }
               else
               if (event.target == btnMonthNext)
                  incMonth(true);
               else
               if (event.target == btnMonthPrev)
                  incMonth(false);
               else
               if (event.target == btnYearNext)
                  incYear(true);
               else
               if (event.target == btnYearPrev)
                  incYear(false);
               break;
            case PenEvent.PEN_UP:
               if (event.target == lMonth)
               {
                  if (myMonthNames == null) // monthNames have a initial month with spaces, so we remove it.
                  {
                     myMonthNames = new String[12];
                     for (int i = 0; i < 12; i++) myMonthNames[i] = Date.monthNames[i+1];
                  }
                  int sel = showPopup(yearMonth[1].toUpperCase(),myMonthNames,month-1);
                  if (sel >= 0)
                  {
                     month = sel+1;
                     updateDays();
                  }
               }
               else
               if (event.target == lYear)
               {
                  String[] yy = new String[YEAR_ARRAY_LENGTH];
                  int sel = -1;
                  for (int i = 0; i < yy.length; i++)
                  {
                     int y = year-yy.length/2+i;
                     yy[i] = String.valueOf(y);
                     if (y == year)
                        sel = i;
                  }
                  sel = showPopup(yearMonth[0].toUpperCase(),yy,sel);
                  if (sel >= 0)
                  {
                     year = Convert.toInt(yy[sel]);
                     updateDays();
                  }
               }
         }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   private int showPopup(String title, String[] items, int sel) throws IOException, ImageException
   {
      if (uiAndroid)
      {
         PopupMenu pm = new PopupMenu(title,items);
         pm.setSelectedIndex(sel);
         pm.popup();
         return pm.getSelectedIndex();
      }
      else
      {
         ComboBoxDropDown cb = new ComboBoxDropDown(new ListBox(items));
         cb.fullHeight = true;
         cb.lb.setSelectedIndex(sel);
         Rect r = getClientRect();
         r.x += this.x; r.y += this.y;
         cb.setRect(r);
         cb.popup();
         return cb.lb.getSelectedIndex();
      }
   }

   private void incMonth(boolean inc)
   {
      if (inc)
      {
         if (++month == 13)
         {
            month=1;
            year++;
         }
      }
      else
      {
         if (--month == 0)
         {
            year--;
            month=12;
         }
      }
      updateDays();
   }
   
   private void incYear(boolean inc)
   {
      if (inc)
         year++;
      else
         year--;
      updateDays();
   }

   public void onPaint(Graphics g)
   {
      //Draw title with appropriote month and year
      sb.setLength(0);
      paintTitle(sb.append(Date.getMonthName(month)).append(' ').append(year).toString(),g);
   }

   protected void onPopup()
   {
      if (children == null)
         setupUI();
      canceled = false;
      day = -1;
      setTitle(Date.getMonthName(month) + " " + (new Date().getYear()));
      if (sentDay <= 0) setSelectedDate(null); // guich@300_45: makes the sentDate the default
   }

   protected void onUnpop()
   {
      setFocus(this);
   }

   protected void postUnpop()
   {
      if (!canceled) // guich@580_27
         postPressedEvent();
      sentDay = 0;
   }
}
