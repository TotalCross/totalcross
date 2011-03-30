/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

// $Id: SpinList.java,v 1.30 2011-01-13 15:27:22 guich Exp $

package totalcross.ui;

import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.util.*;
import totalcross.sys.*;

/** Creates a control with two arrows, so you can scroll values and show
  * the current one.
  * It supports auto-scroll (by clicking and holding) and can also 
  * dynamically compute the items based on ranges.
  */

public class SpinList extends Control
{
   protected String []choices;
   protected int selected;
   protected TimerEvent timer;
   /** Timer interval in which the scroll will be done. */
   public int timerInterval=300;
   private boolean goingUp;
   private int tick;
   
   /** Constructs a SpinList with the given choices, selecting index 0 by default.
    * @see #setChoices 
    */
   public SpinList(String[] choices)
   {
      this(choices, 0);
   }
   
   /** Constructs a SpinList with the given choices, and the given selected index. 
   * @see #setChoices 
   */
   public SpinList(String[] choices, int selected)
   {
      setChoices(choices);
      this.selected = selected;
   }
   
   public int getPreferredWidth()
   {
      int w=fm.getMaxWidth(choices,0,choices.length);
      if (w == 0)
         return Settings.screenWidth/2;
      // Get the width of the arrow
      return w + getArrowWidth() * 2;
   }

   /** Sets the choices to the given ones. Searches for [i0,if] and then expands the items.
    * For example, passing some string as "Day [1,31]" will expand that to an array of 
    * <code>"Day 1","Day 2",...,"Day 31"</code>.
    */
   public void setChoices(String []choices)
   {
      int ini;
      if (choices == null) choices = new String[]{""};
      else
      {
         Vector v = new Vector(choices.length+10);
         for (int i =0; i < choices.length; i++)
            if ((ini=choices[i].indexOf('[')) != -1)
            {
               int fim = choices[i].indexOf(']');
               String prefix = choices[i].substring(0,ini);
               String sufix  = choices[i].substring(fim+1);
               try
               {
                  int j;
                  int start = Convert.toInt(choices[i].substring(ini+1,j=choices[i].indexOf(',',ini+1)));
                  int end   = Convert.toInt(choices[i].substring(j+1,fim));
                  for (int k =start; k <= end; k++)
                     v.addElement(prefix+k+sufix);
               } catch (InvalidNumberException ine) {if (Settings.onJavaSE) Vm.warning(ine.getMessage());}
            }
            else v.addElement(choices[i]);
         if (choices.length != v.size())
            choices = (String[])v.toObjectArray();
      }
      this.choices = choices;
      selected = 0;
      Window.needsPaint = true;
   }
   
   /** Returns the choices array, after the expansion (if any). */
   public String[] getChoices()
   {
   	return choices;
   }

   /** Returns the selected item. */
   public String getSelectedItem()
   {
      return choices[selected];
   }

   /** Returns the selected index. */
   public int getSelectedIndex()
   {
      return selected;
   }

   /** Sets the selected item; -1 is NOT accepted. */
	public void setSelectedIndex(int i)
	{
  	   if (0 <= i && i < choices.length)
  	   {
		   selected = i;
		   Window.needsPaint = true;
		}
	}
   
   /** Removes the item at the given index. */
	public String removeAt(int index)
	{
   	String ret = choices[index];
   	int last = choices.length-1;
   	String []ch = new String[last];
		Vm.arrayCopy(choices,0,ch,0,index);
      if (index < last)
      	Vm.arrayCopy(choices,index+1,ch,index,last-index);
      else
         selected--;
      this.choices = ch;
      Window.needsPaint = true;
      return ret;
	}
   
   /** Removes the current item */
   public String removeCurrent()
   {
   	return removeAt(selected);
   }
   
   /** Returns the index of the given item. */
   public int indexOf(String elem)
   {
      for (int i = 0; i < choices.length; i++)
         if (choices[i].equals(elem))
            return i;
      return -1;
   }
   
   /** Inserts the given element in order (based in the assumption that the original choices was ordered). */
	public void insertInOrder(String elem)
	{
   	// find the correct position to insert
      int index = 0;
      while (index < choices.length && elem.compareTo(choices[index]) > 0)
         index++;
      if (index == choices.length || !elem.equals(choices[index]))
      {
	      String []ch = new String[choices.length+1];
	      Vm.arrayCopy(choices,0,ch,0,index);
	      ch[index] = elem;
	      if (index < choices.length)
	      	Vm.arrayCopy(choices,index,ch,index+1,choices.length-index);
	      choices = ch;
	      selected = index;
	      Window.needsPaint = true;
      }
	}

   private int getArrowWidth()
   {
      return 4*fmH/11; // guich@550_25: fix for other resolutions
   }
   
   public void onPaint(Graphics g)
   {
      g.backColor = getBackColor(); // guich@341_3
      g.fillRect(0,0,width,height);
      int fore = enabled ? foreColor : Color.getCursorColor(foreColor);
      g.foreColor = fore;
      int yoff = (height - fmH) / 2 + 1;
      int wArrow = getArrowWidth();
      g.drawArrow(0,yoff,wArrow,Graphics.ARROW_UP,false,fore);
      g.drawArrow(0,yoff+height/2,wArrow,Graphics.ARROW_DOWN,false,fore);

      if (choices.length > 0) g.drawText(choices[selected],wArrow<<1,yoff-1, textShadowColor != -1, textShadowColor);
   }
   
   private void scroll(boolean up, boolean doPostEvent)
   {
      int max = choices.length-1;
  	   if (up)
      {
   	   selected--;
	      if (selected < 0) selected = max;
      }
      else
      {
         selected++;
	      if (selected > max) selected = 0;
      }
      Window.needsPaint = true;
      if (doPostEvent) 
         postPressedEvent();
   }
   
   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case KeyEvent.KEY_PRESS:
         {
            KeyEvent ke = (KeyEvent)event;
            int key = ke.key;
            if (key == ' ') selected = 0; // restart a search
            else
            {
               key = Convert.toLowerCase((char)key); // converts to uppercase
               for (int i =0; i < choices.length; i++)
                  if (choices[i].charAt(0) == (char)key)
                  {
                     selected = i;
                     Window.needsPaint = true;
                     break;
                  }
            }
            break;
         }
         case KeyEvent.SPECIAL_KEY_PRESS:
         {
            KeyEvent ke = (KeyEvent)event;
            if (Settings.keyboardFocusTraversable && ke.isActionKey()) // guich@550_15
               postPressedEvent();
            else
            if (ke.isUpKey())
               scroll(true,!Settings.keyboardFocusTraversable);
            else
            if (ke.isDownKey())
               scroll(false,!Settings.keyboardFocusTraversable);
            break;
         }
	      case PenEvent.PEN_DOWN:
   	      if (((PenEvent)event).x < getArrowWidth())
      	   {
        	 	   goingUp = (((PenEvent)event).y) > height /2;
            	scroll(goingUp,true);
            }
				if (timer == null)
            {
	            tick = 0;
					timer = addTimer(timerInterval);
            }
            break;
	      case PenEvent.PEN_UP:
       		if (timer != null)
       	 	{
					removeTimer(timer);
					timer = null;
				}
	 	   	break;
 		   case TimerEvent.TRIGGERED:
            if (timer.triggered && tick++ > 3)
               scroll(goingUp,!Settings.keyboardFocusTraversable);
            break;
   	}
   }

   /** Clears this control, selecting element clearValueInt. */
   public void clear()
   {
      setSelectedIndex(clearValueInt);
   }

   public Control handleGeographicalFocusChangeKeys(KeyEvent ke)
   {
       if (!ke.isUpKey() && !ke.isDownKey()) return null;
       _onEvent(ke);
       return this;
   }
}