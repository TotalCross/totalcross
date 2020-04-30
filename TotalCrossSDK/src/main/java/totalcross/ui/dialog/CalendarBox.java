// Copyright (C) 2000-2001 Allan C. Solomon 
// Copyright (C) 2001-2013 SuperWaba Ltda. 
// Copyright (C) 2013-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.dialog;

import java.util.ArrayList;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Label;
import totalcross.ui.PushButtonGroup;
import totalcross.ui.ScrollContainer;
import totalcross.ui.UIColors;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.util.Date;
import totalcross.util.InvalidDateException;

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

public class CalendarBox extends Window {
  private int day = -1, month, year;
  private int sentDay, sentMonth, sentYear;
  private Button btnOk, btnCancel;
  private Button btnMonthNext, btnMonthPrev;
  private PushButtonGroup pbgDays;
  private String[] tempDays = new String[42];
  private StringBuffer sb = new StringBuffer(20);
  private Label displayedMonth;
  private Label lDay;
  private Label titleYear;
  private Label l;
  private int fore;
  private static int leftGap;

  /** Defines the array length of year window. If array length is 20, and current year is 2010, the
   * list goes from 2000 to 2019.
   * @since TotalCross 1.66
   */
  public static int YEAR_ARRAY_LENGTH = 20;

  /** True if the user had canceled without selecting */
  public boolean canceled;

  /** The 7 week names painted in the control. */
  public static String[] weekNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" }; // guich#573_39
  /** The 12 month names painted in the control.*/
  public static String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

  /** The labels between the arrows: Month, Year */
  public static String[] yearMonth = new String[] { "year", "month" };
  
  /** The calendar title container.*/
  private Container calendarTitle;
  
  /** The lower part of the calendar.*/
  private Container lowerCalendar;
  
  /** The lower part of the calender when it's showing the years only*/
  private ScrollContainer lowerCalendarSC;
  
  /** The calendar itself.*/
  private Container calendar;
  
  /** The last and present century*/
  ArrayList<Label> years = new ArrayList<>();
  
  /** This determines if the Calendar should show the years or not.*/
  private boolean showYearCalendar;
  
  protected boolean cancelDrag;
  private int lastFocusedYear;
  private boolean showingYears;
  private int fontAmountToBeAdjusted;
  
  /**
   * Constructs Calendar set to the current day.
   */
  public CalendarBox() {
//    super(" ", uiAndroid ? ROUND_BORDER : RECT_BORDER); // no title yet
//    super.setBorderStyle(uiAndroid ? ROUND_BORDER : RECT_BORDER);
	  year = new Date().getYear();
	fontAmountToBeAdjusted = 0;
    uiAdjustmentsBasedOnFontHeightIsSupported = false;
    fadeOtherWindows = Settings.fadeOtherWindows;
    String[] defCaps = new String[42];
	lDay = new Label();
	lDay.setText(weekNames[new Date().getDayOfWeek()] + ", " + monthNames[new Date().getMonth() - 1] + " " + new Date().getDay());
    for (int i = 0; i < 42; i++) {
      defCaps[i] = Convert.toString(i + 10);
    }
    int aux = new Date().getYear()/100 * 100 - 100;
    Font auxFont = Font.getFont(16);
    for(int i = aux; i++ < aux + 201;) {
		years.add(i - aux - 1, new Label(i - 1 + "", CENTER));
		years.get(i - aux - 1).setFont(auxFont);
	}
    pbgDays = new PushButtonGroup(defCaps, false, -1, -1, fm.charWidth('@') - 2, 6, true, PushButtonGroup.NORMAL);
    pbgDays.setHighlightSelected(true);
    pbgDays.setHighlightColor(Color.getRGB("1A73E8"));
    pbgDays.setHighlightEmptyValues(false);
    pbgDays.setDoEffect(false);
    
  }
  
  public CalendarBox(Date date) {
	  	year = date.getYear();
	  	fontAmountToBeAdjusted = 0;
	    uiAdjustmentsBasedOnFontHeightIsSupported = false;
	    fadeOtherWindows = Settings.fadeOtherWindows;
	    String[] defCaps = new String[42];
		lDay = new Label();
		lDay.setText(weekNames[date.getDayOfWeek()] + ", " + monthNames[date.getMonth() - 1] + " " + date.getDay());
	    for (int i = 0; i < 42; i++) {
	      defCaps[i] = Convert.toString(i + 10);
	    }
	    int aux = date.getYear()/100 * 100 - 100;
	    int range = 201;
	    for(int i = 0; aux + range - 1 + i * 100 < new Date().getYear(); i++) {
	    	range += 100;
	    }
	    Font auxFont = Font.getFont(16);
	    for(int i = aux; i++ < aux + range;) {
			years.add(i - aux - 1, new Label(i - 1 + "", CENTER));
			years.get(i - aux - 1).setFont(auxFont);
		}
	    pbgDays = new PushButtonGroup(defCaps, false, -1, -1, fm.charWidth('@') - 2, 6, true, PushButtonGroup.NORMAL);
	    pbgDays.setHighlightSelected(true);
	    pbgDays.setHighlightColor(Color.getRGB("1A73E8"));
	    pbgDays.setHighlightEmptyValues(false);
	    pbgDays.setDoEffect(false);
  }

  public CalendarBox(int fore) {
	  this();
	  this.fore = Color.WHITE;
  }
  
  public CalendarBox(int fore, Date date) {
	  this(date);
	  this.fore = Color.WHITE;
  }
  
	private void setupUI() // guich@tc100b5_28
	{
		if(!showYearCalendar){
			showingYears = false;
			calendar = new Container();
			calendarTitle = new Container();
			lowerCalendar = new Container();
			calendarTitle.setForeColor(fore);
			lowerCalendar.setForeColor(fore);
			titleYear = new Label();
			titleYear.setText(year + "");
			if(Settings.screenHeight > Settings.screenWidth)
			{
				String[] sAux = Convert.tokenizeString(lDay.getText(), '\n');
				StringBuffer stringBuffer = new StringBuffer();
				for(String string : sAux) {                                  
					stringBuffer.append(string);                            
				} 
				lDay = new Label(stringBuffer.toString());
				lDay.autoSplit = false;
				leftGap = Settings.screenWidth*16/360;
				setRect(CENTER, CENTER, SCREENSIZE + 80, SCREENSIZE + (48700/664));
				add(calendar, LEFT, TOP, FILL, FILL);
				calendar.add(calendarTitle, LEFT, TOP, FILL, SCREENSIZE + (9300/664));
				calendar.add(lowerCalendar, LEFT, AFTER, FILL, FILL);
				
				titleYear.transparentBackground = lDay.transparentBackground = true;
				titleYear.setFont(Font.getFont(true, Font.NORMAL_SIZE + 8));
				titleYear.setForeColor(Color.getRGB(202,222,252));
				lDay.setFont(Font.getFont(true, Font.NORMAL_SIZE + 30 + fontAmountToBeAdjusted));
				lDay.setForeColor(Color.WHITE);
				calendarTitle.add(titleYear, LEFT + leftGap, TOP + Settings.screenHeight*8/664, PREFERRED, PREFERRED);
				calendarTitle.add(lDay, SAME, TOP + Settings.screenHeight *35/664, PREFERRED, PREFERRED);
				boolean adjustFont = false;
				while (lDay.fm.height > calendarTitle.getHeight() - (Settings.screenHeight*8/664 + titleYear.fm.height)) {
					lDay.setFont(lDay.getFont().adjustedBy(-1));
					fontAmountToBeAdjusted--;
					if(!adjustFont) {
						adjustFont = true;
					}
				}
				if(adjustFont) {
					lDay.setFont(Font.getFont(lDay.getFont().name, true, lDay.getFont().size));
				}
				calendarTitle.setBackColor(Color.getRGB(66, 133, 244));
				lowerCalendar.setBackColor(Color.WHITE);
				
				started = true;
				
			    btnMonthPrev = new Button(" < ", Button.BORDER_NONE);
			    btnMonthPrev.setForeColor(Color.BLACK);
			    btnMonthNext = new Button(" > ", Button.BORDER_NONE);
			    btnMonthNext.setForeColor(Color.BLACK);
			    int mButtonGap = Settings.screenWidth*14/360;
			    lowerCalendar.add(btnMonthPrev, LEFT + mButtonGap, TOP + Settings.screenHeight*10/664, DP + 48, DP + 48);
			    
		    	displayedMonth = new Label(Date.getMonthName(new Date().getMonth()) + " " + year, CENTER);
			    displayedMonth.setForeColor(Color.BLACK);
			    lowerCalendar.add(btnMonthNext, RIGHT - mButtonGap,SAME, DP + 48, DP + 48);
			    lowerCalendar.add(displayedMonth, AFTER, CENTER_OF, FIT, PREFERRED, btnMonthPrev);
				pbgDays.setForeColor(Color.BLACK);
				pbgDays.insideGap = fm.charWidth('@') - 2;
				pbgDays.setBorder(false);
				btnOk = new Button("OK", Button.BORDER_NONE);
				btnOk.setFont(Font.getFont(true, 14));
				btnOk.setForeColor(Color.getRGB(66, 133, 244));
				btnOk.setBackColor(Color.WHITE);
				btnCancel = new Button("CANCEL", Button.BORDER_NONE);
				btnCancel.setFont(Font.getFont(true, 14));
				btnCancel.setForeColor(Color.getRGB(66, 133, 244));
				btnCancel.setBackColor(Color.WHITE);
				lowerCalendar.add(btnOk, RIGHT - Settings.screenWidth*12/664, BOTTOM - Settings.screenHeight*4/664, DP + 64, DP + 48);
				lowerCalendar.add(btnCancel, BEFORE, SAME, DP + 77, DP+ 48);
				lowerCalendar.add(pbgDays, CENTER, TOP + Settings.screenHeight*98/664, PARENTSIZE + 86, Settings.screenHeight*240/664, displayedMonth);
				
				Font bold = font.asBold();
				int xx = pbgDays.getX(); 
			    for (int i = 0; i < 7; i++) { 
			      l = new Label(weekNames[i].substring(0, 1)); 
			      l.setFont(bold);
			      l.setForeColor(Color.getRGB(220, 220, 220));
			      Rect r = pbgDays.rects[i]; 
			      lowerCalendar.add(l, xx + r.x + (r.width - l.getPreferredWidth()) / 2, pbgDays.getY() - l.getPreferredHeight()); 
			    }
			} else {
				lDay.autoSplit = true;
				leftGap = Settings.screenWidth*16/688;
				setRect(CENTER, CENTER, SCREENSIZE + (51800/688), SCREENSIZE + (3180/36));
				add(calendar, LEFT, TOP, FILL, FILL);
				calendar.add(calendarTitle, LEFT, TOP, SCREENSIZE + (1680/72), FILL);
				calendar.add(lowerCalendar, AFTER, SAME, FILL, FILL);
				
				titleYear.transparentBackground = lDay.transparentBackground = true;
				titleYear.setFont(Font.getFont(true, Font.NORMAL_SIZE + 8));
				titleYear.setForeColor(Color.getRGB(199, 219, 252));
				lDay.setFont(Font.getFont(true, 30));
				lDay.setForeColor(Color.WHITE);
				calendarTitle.add(titleYear, LEFT + leftGap, TOP + Settings.screenHeight*8/360, PREFERRED, PREFERRED);
				calendarTitle.add(lDay, SAME, TOP + Settings.screenHeight *35/360, Settings.screenWidth/6, DP + 80);
				calendarTitle.setBackColor(Color.getRGB(66, 133, 244));
				lowerCalendar.setBackColor(Color.WHITE);
				
				started = true;
				
			    btnMonthPrev = new Button(" < ", Button.BORDER_NONE);
			    btnMonthPrev.setForeColor(Color.BLACK);
			    btnMonthNext = new Button(" > ", Button.BORDER_NONE);
			    btnMonthNext.setForeColor(Color.BLACK);
			    int mButtonGap = Settings.screenWidth*17/688;
			    lowerCalendar.add(btnMonthPrev, LEFT + mButtonGap, TOP - Settings.screenHeight*4/360, DP + 48, DP + 48);
			    
		    	displayedMonth = new Label(Date.getMonthName(new Date().getMonth()) + " " + year, CENTER);
			    displayedMonth.setForeColor(Color.BLACK);
			    lowerCalendar.add(btnMonthNext, RIGHT - mButtonGap, SAME, DP + 48, DP + 48);
			    lowerCalendar.add(displayedMonth, AFTER, CENTER_OF, FIT, PREFERRED, btnMonthPrev);
				pbgDays.setForeColor(Color.BLACK);
				pbgDays.insideGap = fm.charWidth('@') - 2;
				pbgDays.setBorder(false);
				btnOk = new Button("OK", Button.BORDER_NONE);
				btnOk.setFont(Font.getFont(true, 14));
				btnOk.setForeColor(Color.getRGB(66, 133, 244));
				btnOk.setBackColor(Color.WHITE);
				btnCancel = new Button("CANCEL", Button.BORDER_NONE);
				btnCancel.setFont(Font.getFont(true, 14));
				btnCancel.setForeColor(Color.getRGB(66, 133, 244));
				btnCancel.setBackColor(Color.WHITE);
				lowerCalendar.add(btnOk, RIGHT - Settings.screenWidth*12/664, BOTTOM - Settings.screenHeight*4/688, DP + 64, DP + 48);
				lowerCalendar.add(btnCancel, BEFORE, SAME, DP + 77, DP+ 48);
				lowerCalendar.add(pbgDays, CENTER, TOP + Settings.screenHeight*80/360, PARENTSIZE + 90, Settings.screenHeight*192/360, displayedMonth);
				
				Font bold = font.asBold();
				int xx = pbgDays.getX(); 
			    for (int i = 0; i < 7; i++) { 
			      l = new Label(weekNames[i].substring(0, 1)); 
			      l.setFont(bold);
			      l.setForeColor(Color.getRGB(220, 220, 220));
			      Rect r = pbgDays.rects[i]; 
			      lowerCalendar.add(l, xx + r.x + (r.width - l.getPreferredWidth()) / 2, pbgDays.getY() - l.getPreferredHeight()); 
			    }
			}
		} else {
			showingYears = true;
			Container buttonsCtnr = new Container();
			calendar = new Container();
			calendarTitle = new Container();
			lowerCalendarSC = new ScrollContainer(false, true);
			calendarTitle.setForeColor(fore);
			titleYear = new Label();
			titleYear.setText(year + "");
			titleYear.setFont(Font.getFont(true, titleYear.getFont().size));
			lDay.autoSplit = true;
			if(Settings.screenHeight > Settings.screenWidth)
			{
				leftGap = Settings.screenWidth*16/360;
				setRect(CENTER, CENTER, SCREENSIZE + 80, SCREENSIZE + (48700/664));
				add(calendar, LEFT, TOP, FILL, FILL);
				calendar.add(calendarTitle, LEFT, TOP, FILL, SCREENSIZE + (9300/664));
				calendar.add(buttonsCtnr, LEFT, BOTTOM, FILL, Settings.screenHeight*56/688);
				calendar.add(lowerCalendarSC, LEFT, AFTER, FILL, FIT,calendarTitle);
				
				titleYear.transparentBackground = lDay.transparentBackground = true;
				titleYear.setFont(Font.getFont(true, Font.NORMAL_SIZE + 8));
				titleYear.setForeColor(Color.WHITE);
				lDay.setFont(Font.getFont(false, Font.NORMAL_SIZE + 30));
				lDay.setForeColor(Color.getRGB(202,222,252));
				calendarTitle.add(titleYear, LEFT + leftGap, TOP + Settings.screenHeight*8/664, PREFERRED, PREFERRED);
				calendarTitle.add(lDay, SAME, TOP + Settings.screenHeight *35/664, PREFERRED, PREFERRED);
				calendarTitle.setBackColor(Color.getRGB(66, 133, 244));
				
				started = true;
				
				int aux = years.size();
			    for(int i = 0; i++ < aux;) {
			    	lowerCalendarSC.add(years.get(i - 1), CENTER, i == 0? TOP: AFTER, PARENTSIZE, DP + 57);
					
					final int aux2 = i - 1;
					if(Integer.parseInt(years.get(i-1).getText()) == year) {
						lastFocusedYear = i - 1;
						years.get(i - 1).setFont(Font.getFont(true, 26));
						years.get(i - 1).setForeColor(Color.getRGB(66, 133, 244));
					}
					years.get(i-1).addPenListener(new PenListener() {
						boolean isDrag;
						@Override
						public void penUp(PenEvent e) {
							if(!isDrag && isInsideOrNear(e.x, e.y)) {
								years.get(lastFocusedYear).setFont(Font.getFont(16));
								years.get(lastFocusedYear).setForeColor(Color.BLACK);
								year =  Integer.parseInt(years.get(aux2).getText());
								updateDays();
								showYearCalendar = false;
								setupUI();
							} else if (isDrag){
								isDrag = false;
							}
							
						}
						
						@Override
						public void penDragStart(DragEvent e) {
							isDrag = true;
						}
						
						@Override
						public void penDragEnd(DragEvent e) {
						}
						
						@Override
						public void penDrag(DragEvent e) {
						}
						
						@Override
						public void penDown(PenEvent e) {}
					});
				}
			    btnOk = new Button("OK", Button.BORDER_NONE);
			    btnOk.setFont(Font.getFont(true, 14));
			    btnOk.setForeColor(Color.getRGB(66, 133, 244));
			    btnOk.setBackColor(Color.WHITE);
			    btnCancel = new Button("CANCEL", Button.BORDER_NONE);
			    btnCancel.setFont(Font.getFont(true, 14));
			    btnCancel.setForeColor(Color.getRGB(66, 133, 244));
			    btnCancel.setBackColor(Color.WHITE);
			    buttonsCtnr.add(btnOk, RIGHT - Settings.screenWidth*12/664, BOTTOM - Settings.screenHeight*4/688, DP + 64, DP + 48);
			    buttonsCtnr.add(btnCancel, BEFORE, SAME, DP + 77, DP+ 48);
			    
			} else {
				leftGap = Settings.screenWidth*16/688;
				setRect(CENTER, CENTER, SCREENSIZE + (51800/688), SCREENSIZE + (3180/36));
				add(calendar, LEFT, TOP, FILL, FILL);
				calendar.add(calendarTitle, LEFT, TOP, SCREENSIZE + (1680/72), FILL);
				calendar.add(lowerCalendarSC, AFTER, TOP, FILL, Settings.screenHeight*246/360);
				calendar.add(buttonsCtnr, SAME, AFTER, FILL, Settings.screenHeight*56/360);
				
				titleYear.transparentBackground = lDay.transparentBackground = true;
				titleYear.setFont(Font.getFont(true, Font.NORMAL_SIZE + 8));
				titleYear.setForeColor(Color.WHITE);
				lDay.setFont(Font.getFont(true, 30));
				lDay.setForeColor(Color.getRGB(202,222,252));
				calendarTitle.add(titleYear, LEFT + leftGap, TOP + Settings.screenHeight*8/360, PREFERRED, PREFERRED);
				calendarTitle.add(lDay, SAME, TOP + Settings.screenHeight *35/360, Settings.screenWidth/6, DP + 80);
				calendarTitle.setBackColor(Color.getRGB(66, 133, 244));
				lowerCalendarSC.setBackColor(Color.WHITE);
				
				started = true;
				
				int aux = years.size();
				for(int i = 0; i++ < aux;) {
					lowerCalendarSC.add(years.get(i - 1), CENTER, i == 0? TOP: AFTER, PARENTSIZE, DP + 48);
					
					final int aux2 = i - 1;
					if(Integer.parseInt(years.get(i-1).getText()) == year) {
						lastFocusedYear = i - 1;
						years.get(i - 1).setFont(Font.getFont(true, 26));
						years.get(i - 1).setForeColor(Color.getRGB(66, 133, 244));
					}
					years.get(i-1).addPenListener(new PenListener() {
						boolean isDrag;
						@Override
						public void penUp(PenEvent e) {
							if(!isDrag && isInsideOrNear(e.x, e.y)) {
								years.get(lastFocusedYear).setFont(Font.getFont(16));
								years.get(lastFocusedYear).setForeColor(Color.BLACK);
								year =  Integer.parseInt(years.get(aux2).getText());
								updateDays();
								showYearCalendar = false;
								setupUI();
							} else if (isDrag){
								isDrag = false;
							}
							
						}
						
						@Override
						public void penDragStart(DragEvent e) {
							isDrag = true;
						}
						
						@Override
						public void penDragEnd(DragEvent e) {
						}
						
						@Override
						public void penDrag(DragEvent e) {
						}
						
						@Override
						public void penDown(PenEvent e) {}
					});
				}
	    	}
			btnOk = new Button("OK", Button.BORDER_NONE);
			btnOk.setFont(Font.getFont(true, 14));
			btnOk.setForeColor(Color.getRGB(66, 133, 244));
			btnOk.setBackColor(Color.WHITE);
			btnCancel = new Button("CANCEL", Button.BORDER_NONE);
			btnCancel.setFont(Font.getFont(true, 14));
			btnCancel.setForeColor(Color.getRGB(66, 133, 244));
			btnCancel.setBackColor(Color.WHITE);
			buttonsCtnr.add(btnOk, RIGHT - Settings.screenWidth*12/664, BOTTOM - Settings.screenHeight*4/688, DP + 64, DP + 48);
			buttonsCtnr.add(btnCancel, BEFORE, SAME, DP + 77, DP+ 48);
		}
	}

  /**
   Returns the selected Date.
  
   @return Date object set to the selected day, or null if an error occurs.
   */
  public Date getSelectedDate() {
    if (day == -1) {
      return null;
    }
    try {
      return new Date(day, month, year);
    } catch (InvalidDateException ide) {
      return null;
    }
  }

  /** Sets the current day to the Date specified. If its null, sets the date to today. */
  public void setSelectedDate(Date d) {
    if (d == null) {
      d = new Date();
    }
    sentMonth = month = d.getMonth();
    sentYear = year = d.getYear();
    sentDay = day = d.getDay();

    // sets the pbg days
    updateDays();
  }
  

  private void updateDays() {
    try {
      Date date = new Date(1, month, year);
      int start = date.getDayOfWeek();
      int end = start + date.getDaysInMonth();
      pbgDays.setSelectedIndex(-1);

      int d = 1;
      String[] days = tempDays;

      for (int i = 0; i < 42; i++) {
        pbgDays.setColor(i, -1, -1); // erase all colors
        days[i] = (start <= i && i < end) ? Convert.toString(d++) : "";
      }
      if (Settings.keyboardFocusTraversable) {
        days[end] = " x"; // add a way to get out of the pbg without closing it
      }
      // set sent color
      if (year == sentYear && month == sentMonth) {
        pbgDays.setColor(sentDay - 1 + start, -1, UIColors.calendarAction);
      }
      pbgDays.setNames(days);

      Window.needsPaint = true;
      for(int i = 0; i < pbgDays.names.length; i++) {
    	  if((pbgDays.names[i].equals(sentDay+"") && sentMonth == date.getMonth() && sentYear == date.getYear()) && !pbgDays.names[i].equals("")) {
    		  pbgDays.setSelectedIndex(i);
    	  }
      }
    } catch (InvalidDateException ide) {
    }
  }

  @Override
  public void onEvent(Event event) {
    try {
      switch (event.type) {
      case KeyEvent.SPECIAL_KEY_PRESS:
        KeyEvent ke = (KeyEvent) event;
        if (ke.key == SpecialKeys.KEYBOARD_ABC || ke.key == SpecialKeys.KEYBOARD_123) // closes this window without selecting any day
        {
          day = sentDay; // guich@tc100
          unpop();
        } else if (ke.isDownKey()) {
          incYear(false);
        } else if (ke.isUpKey()) {
          incYear(true);
        } else if (ke.isNextKey()) {
          incMonth(true);
        } else if (ke.isPrevKey()) {
          incMonth(false);
        }
        break;
      case ControlEvent.PRESSED:
        if (event.target == btnOk) {
        	canceled = false;
            unpop();
        } else if (event.target == btnCancel) {
			canceled = true;
			unpop();
        } else if (event.target == pbgDays && pbgDays.getSelectedIndex() >= 0) {
          try {
            Date date = new Date(Convert.toInt(pbgDays.getSelectedItem()), month, year);
            day = date.getDay();
            lDay.setText(weekNames[date.getDayOfWeek()] + ", " + monthNames[date.getMonth() - 1] + " " + date.getDay());
          } catch (Exception ide) {
            day = -1;
          }
        } else if (event.target == btnMonthNext) {
          incMonth(true);
        } else if (event.target == btnMonthPrev) {
          incMonth(false);
        } 
        break;
      case PenEvent.PEN_UP:
    	if(!cancelDrag) {
    		if (event.target == titleYear) {
    			showYearCalendar = true;
    			setupUI();
    			Control c = years.get(lastFocusedYear);
			    int dy = c.getHeight()*(lastFocusedYear - 2);
			    lowerCalendarSC.sbV.setMaximum((int)(dy*1.5));
			    lowerCalendarSC.scrollContent(0, dy, true);
		    } else if (event.target == lDay) {
        		if(showingYears) {
        			showYearCalendar = false;
        			setupUI();
        		}
        	}
    	}
    	cancelDrag = false;
    	break;
      case PenEvent.PEN_DRAG:
    	cancelDrag = true;
    	break;
      }
      
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }

  private void incMonth(boolean inc) {
    if (inc) {
      if (++month == 13) {
        month = 1;
        year++;
      }
    } else {
      if (--month == 0) {
        year--;
        month = 12;
      }
    }
    displayedMonth.setText(sb.append(Date.getMonthName(month)).append(' ').append(year).toString());
    updateDays();
  }

  private void incYear(boolean inc) {
    if (inc) {
      year++;
    } else {
      year--;
    }
    updateDays();
  }

  @Override
  public void onPaint(Graphics g) {
    //Draw title with appropriote month and year
    sb.setLength(0);
  }

  @Override
  protected void onPopup() {
    if (children == null) {
      setupUI();
    }
    canceled = false;
    day = -1;
    if (sentDay <= 0) {
      setSelectedDate(null); // guich@300_45: makes the sentDate the default
    }
  }
 
  @Override
	public void reposition() {
		// TODO Auto-generated method stub
		if (showingYears) {
			showYearCalendar = true;
			setupUI();
			Control c = years.get(lastFocusedYear);
		    int dy = c.getHeight()*(lastFocusedYear - 2);
		    lowerCalendarSC.sbV.setMaximum((int)(dy*1.5));
		    lowerCalendarSC.scrollContent(0, dy, true);
		} else {
			setupUI();
		}
	  
  }

  @Override
  protected void onUnpop() {
    setFocus(this);
  }

  @Override
  protected void postUnpop() {
    if (!canceled) {
      postPressedEvent();
    }
    sentDay = 0;
  }
}
