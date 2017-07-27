/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2001 Daniel Tauchke                                            *
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



package totalcross.ui;

import totalcross.res.Resources;
import totalcross.sys.Settings;
import totalcross.ui.effect.MaterialEffect;
import totalcross.ui.effect.UIEffects;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.IntHashtable;
import totalcross.util.Vector;

/**
 * ComboBox is an implementation of a ComboBox, with the drop down window implemented by the ComboBoxDropDown class.
 * <p>
 * Note: the color used in the setBackground method will be used in the button
 * only. The background color of the control will be a lighter version of the
 * given color.
 */

public class ComboBox extends Container implements TimerListener, MaterialEffect.SideEffect
{
   public static final int ARROWSTYLE_DOWNDOT = 0;
   public static final int ARROWSTYLE_PAGEFLIP = 1;
   public static int arrowStyle = ARROWSTYLE_PAGEFLIP;
   protected ComboBoxDropDown pop;
   Button btn;
   private boolean armed;
   private boolean opened;
   private int btnW;
   private int bColor, fColor;
   private int fourColors[] = new int[4];
   private Image npback,nparmed;
   private int selOnPopup = -2;
   private boolean cancelPopup;
   private boolean wasOpen; // guich@tc152: fixed ComboBox problem of clicking on an open combobox was making it open again
   /** If set to true, the popup window will have the height of the screen */
   public boolean fullHeight;
   /** If set to true, the popup window will have the width of the screen */
   public boolean fullWidth;                    // guich@550_20

   /** Handler for the CustomPress */
   public CaptionPress captionPress;
   
   /** The default value that is set to the clearValueInt of all ComboBox.
    * Usually this value is 0, but sometimes you may wish set it to -1, to unselect the ComboBox when clear is called.
    */
   public static int defaultClearValueInt;
   /** The check color used to fill the radio button used in Android. Defaults to the fore color.
    * In material UI, the arrow will be filled with this color when it has focus
    * @since TotalCross 1.3 
    */
   public int checkColor = -1;

   /** The title of the PopupMenu when in Android user interface style. 
    * @since TotalCross 1.3
    */
   public String popupTitle;
   
   /** Parameter passed to PopupMenu. Defauts to true.
    * @see PopupMenu#enableSearch
    */
   public boolean enableSearch=true;

   /** The caption to draw when this ComboBox is empty.
    * In Material UI, you must set the caption BEFORE you add this control to the container; also remember to set the height to at least PREFERRED, it is 75% taller.
    * @see #captionColor 
    */
   public String caption;
   
   /** The caption's color. */
   public int captionColor = -1;
   
   /** An optional caption's icon */
   public Image captionIcon;   

   /** Set to false to don't use the PopupMenu when the user interface style is Android.
    * This affects all ComboBoxes. If you want to change a particular ComboBox to use the standard
    * popup list, but keep others with the PopupMenu, you can do something like:
    * <pre>
    *  // at the begining of your program:
    *  ComboBox.usePopupMenu = true;
    *  // when you want to create the standalone ComboBox
    *  ComboBox.usePopupMenu = false; // turn flag off
    *  .. create the ComboBox
    *  ComboBox.usePopupMenu = true; // turn flag on again
    * </pre>
    * An internal copy of the flag is set at the constructor.
    * @since TotalCross 1.5
    */
   public static boolean usePopupMenu = true;
   
   private boolean _usePopupMenu;

   // changes for material design
   private int xcap,ycap,ycap0,fmHmin,fmHtarget,xcap0,inccap;
   private Font fcap;
   private TimerEvent tea;

   /** Creates an empty ComboBox */
   public ComboBox()
   {
      this((Object[]) null);
   }

   /** Creates a ComboBox with the given items */
   public ComboBox(Object[] items)
   {
      this(new ListBox(items));
   }

   /**
    * Creates a ComboBox with a PopList containing the given ListBox. You can
    * extend the ListBox to draw the items by yourself and use this constructor
    * so the PopList will use your class and not the default ListBox one. 
    * This constructor forces the ListBox.simpleBorder to true. Note: the
    * listbox items must be already set.
    */
   public ComboBox(ListBox userListBox)
   {
      this(new ComboBoxDropDown(userListBox)); // guich@340_36
   }

   /** Constructs a ComboBox with the given PopList. */
   public ComboBox(ComboBoxDropDown userPopList) // guich@340_36
   {
      _usePopupMenu = usePopupMenu;
      clearValueInt = defaultClearValueInt;
      ignoreOnAddAgain = ignoreOnRemove = true;
      pop = userPopList;
      if (!uiMaterial)
      {
         if (uiAndroid)
         {
            btn = new Button(getArrowImage());
            btn.setBorder(Button.BORDER_NONE);
            btn.transparentBackground = true;
         }
         else
         {
            btn = new ArrowButton(Graphics.ARROW_DOWN, uiMaterial ? getArrowWidth()/2 : getArrowWidth(), Color.BLACK);
            btn.setBorder(Button.BORDER_NONE);
            if (uiVista)
            {
               btn.flatBackground = false;
               btn.setBackColor(Color.darker(backColor,32));
            }
         }
         btn.focusTraversable = false;
         super.add(btn);
      }
      effect = UIEffects.get(this);
      started = true; // avoid calling the initUI method
      this.focusTraversable = true; // kmeehl@tc100
      setMatFontParams();
   }
   
   private int getArrowWidth()
   {
      return uiMaterial ? fmH/2 : fmH * 3 / 11;
   }

   /** Does nothing */
  @Override
   public void add(Control control)
   {
   }

   void add(Control control, boolean dummy)
   {
      super.add(control);
   }

   /** Does nothing */
  @Override
   public void remove(Control control)
   {
   }

   /** Adds an array of Objects to the Listbox */
   public void add(Object[] items)
   {
      pop.lb.add(items);
      Window.needsPaint = true;
   }

   /** Adds an array of Objects to the Listbox */
   public void add(Object[] items, int startAt, int size)
   {
      pop.lb.add(items, startAt, size);
      Window.needsPaint = true;
   }

   /**
    * Adds an Object to the Listbox. This method is very slow if used in loop; use the
    * <code>add(Object[])</code> to add a bunch of objects instead.
    */
   public void add(Object item)
   {
      pop.lb.add(item);
      Window.needsPaint = true;
   }
   
   /** Adds the given text to this ListBox, breaking the text if it goes beyond the ListBox' limits, and also breaking if it contains \n.
    * Returns the number of lines. Note that each part of the text is considered a new item. This method is slower than the other <code>add</code> methods.
    * @since TotalCross 1.24
    */
   public int addWrapping(String text) // guich@tc124_21
   {
      Window.needsPaint = true;
      return pop.lb.addWrapping(text);
   }

   /** Adds an Object to the Listbox at the given index */
   public void insert(Object item, int index)
   {
      pop.lb.insert(item, index);
      Window.needsPaint = true;
   }

   /** Empties the ListBox */
  @Override
   public void removeAll() // guich@210_13
   {
      pop.lb.removeAll();
      Window.needsPaint = true;
   }

   /** Removes an Object from the Listbox */
   public void remove(Object item)
   {
      pop.lb.remove(item);
      Window.needsPaint = true;
   }

   /** Removes an Object from the Listbox at the given index. */
   public void remove(int itemIndex)
   {
      pop.lb.remove(itemIndex);
      Window.needsPaint = true;
   }

   /** Sets the Object at the given Index, starting from 0 */
   public void setItemAt(int i, Object s)
   {
      pop.lb.setItemAt(i, s);
      Window.needsPaint = true;
   }

   /** Get the Object at the given Index */
   public Object getItemAt(int i)
   {
      return pop.lb.getItemAt(i);
   }

   /** Returns the selected item of the ListBox */
   public Object getSelectedItem()
   {
      return pop.lb.getSelectedItem();
   }

   /** Returns the position of the selected item of the ListBox */
   public int getSelectedIndex()
   {
      return pop.lb.selectedIndex;
   }

   /** Returns all items in this ComboBox */
   public Object[] getItems()
   {
      return pop.lb.getItems();
   }

   /** Returns the index of the item specified by the name */
   public int indexOf(Object name)
   {
      return pop.lb.indexOf(name);
   }

   /**
    * Sets the cursor color for this ComboBox. The default is equal to the
    * background slightly darker. 
    */
   public void setCursorColor(int color) // guich@210_19
   {
      pop.lb.setCursorColor(color);
   }

   /**
    * Selects an item. If the name is not found, the currently selected item is
    * not changed.
    *
    * @since SuperWaba 4.01
    */
   public void setSelectedItem(Object name) // guich@401_25
   {
      setSelectedItem(name, Settings.sendPressEventOnChange);
   }
   
   /**
    * Selects an item. If the name is not found, the currently selected item is
    * not changed.
    *
    * @since SuperWaba 4.01
    */
   public void setSelectedItem(Object name, boolean sendPress) // guich@401_25
   {
      int idx = pop.lb.selectedIndex;
      pop.lb.setSelectedItem(name);
      animateMaterial(isDisplayed());
    if (sendPress && pop.lb.selectedIndex != idx){
         postPressedEvent();
    }
      Window.needsPaint = true;
   }

   /**
    * Select the given index. Choose "-1" if you want to blank the ComboBox view
    * box.
    */
   public void setSelectedIndex(int i)
   {
      setSelectedIndex(i,Settings.sendPressEventOnChange);
   }
   
   /**
    * Select the given index, and optionally sends a PRESSED event. Choose "-1" if you want to blank the ComboBox view
    * box.
    */
   public void setSelectedIndex(int i, boolean sendPressEvent)
   {
      int idx = pop.lb.selectedIndex;
      pop.lb.setSelectedIndex(i);
    if (sendPressEvent && pop.lb.selectedIndex != idx){
         postPressedEvent();
    }
      animateMaterial(isDisplayed());
      Window.needsPaint = true;
   }

   /** Returns the number of items */
   public int size()
   {
      return pop.lb.itemCount;
   }

  @Override
   public int getPreferredWidth()
   {
      return Math.max(fm.stringWidth(caption == null ? "" : caption), pop.getPreferredWidth()) + 1 + insets.left+insets.right + (uiMaterial ? fmH*3/2 : Settings.fingerTouch ? btn.getPreferredWidth()+4 : 0);
   }

   private Image getArrowImage() 
   {
      Image img;
      Image img0 = arrowStyle == ARROWSTYLE_PAGEFLIP ? Resources.comboArrow2 : Resources.comboArrow;
      int s = fmH;
      try
      {
         img = arrowStyle == ARROWSTYLE_PAGEFLIP ? img0.smoothScaledFixedAspectRatio(s,false) : img0.getSmoothScaledInstance(s,s);
         img.applyColor2(backColor);
      }
      catch (ImageException e)
      {
         img = img0;
      }
      return img;
   }
   
  @Override
   public int getPreferredHeight()
   {
      int ret = (pop.lb.itemCount > 0 && !isSupportedListBox() ? pop.lb.getItemHeight(0) : fmH) + Edit.prefH + insets.top+insets.bottom;;
    if (uiMaterial && caption != null){
         ret += fmHmin;
    }
      return ret;
   }

   private int getTextY()
   {
      boolean isString = pop.lb.itemCount > 0 && pop.lb.items.items[0] instanceof String;
    if (isString && caption == null){
         return (height-fmH)/2;
    }
      int ret = getPreferredHeight();
    if (uiMaterial && caption != null){
         ret = height-ret+fmHmin;
    }else {
         ret = (height-ret)/2;
    }
      return ret;
   }

   /** Passes the font to the pop list */
  @Override
   protected void onFontChanged() // guich@200b4_153
   {
    if (pop != null){
         pop.setFont(font);
    }
      
      if (!uiAndroid) // guich@tc100b3: resize the arrow based on the font.
      {
         ArrowButton ab = (ArrowButton)btn;
         int newWH = getArrowWidth();
         if (ab.prefWH != newWH)
         {
            ab.prefWH = newWH;
            onBoundsChanged(false);
         }
      }
      btnW = getArrowWidth();
      setMatFontParams();
   }
   
   private void setMatFontParams()
   {
      // material
      fmHmin = fmH*75/100;
      fcap = this.font;
   }
   
   /** Sets the ihtForeColors and ihtBackColors for the ListBox used with this ComboBox.
    * Note that null is a valid value, used when you always want to use the default color.
    * @since TotalCross 1.0 beta 4
    */
   public void setBackForeItemColors(IntHashtable ihtFore, IntHashtable ihtBack)
   {
      pop.lb.ihtForeColors = ihtFore;
      pop.lb.ihtBackColors = ihtBack;
   }

  @Override
   protected void onBoundsChanged(boolean screenChanged)
   {
    if (uiMaterial){
         btnW = getArrowWidth();
    }else
      {
         btnW = btn.getPreferredWidth();
         switch (Settings.uiStyle)
         {
            case Settings.HOLO_UI:
            case Settings.ANDROID_UI:
               btn.setImage(getArrowImage());
        if (arrowStyle == ARROWSTYLE_PAGEFLIP) {
                  btn.setRect(width - btnW - 1, height-fmH-2, btnW, fmH,null,screenChanged);
        } else {
                  btn.setRect(width - btnW - 3, 2, btnW, height,null,screenChanged);
        }
               break;
            default: // guich@573_6: both Flat and Vista use this
               btn.setRect(width - btnW - 3, 1, btnW + 2, height - 2, null, screenChanged);
               break;
         }
      }
    if (screenChanged && pop.isVisible()){
         updatePopRect();
    }
      npback = nparmed = null;
      // material
      int idx = getSelectedIndex();
      xcap0 = xcap = getX0();
      ycap0 = ycap = idx == -1 ? getTextY() : 0;
   }

  @Override
   public void onEvent(Event event)
   {
      PenEvent pe;
      boolean inside;
    if (opened && event.type != ControlEvent.WINDOW_CLOSED){
         return;
    }
    if (isEnabled()){
      switch (event.type)
      {
         case PenEvent.PEN_DRAG:
            pe = (PenEvent) event;
            inside = isInsideOrNear(pe.x, pe.y);
            if (event.target == this && inside != armed && pop.lb.itemCount > 0)
            {
               armed = inside;
          if (uiAndroid) {
                  Window.needsPaint = true; // guich@580_25: just call repaint instead of drawing the cursor
          } else
            if (!uiMaterial) {
                  btn.press(armed);
            }
        }
            break;
         case PenEvent.PEN_DOWN:
            if (event.target == this && !armed && pop.lb.itemCount > 0)
            {
               wasOpen = false;
          if (uiAndroid) {
                  Window.needsPaint = true; // guich@580_25: just call repaint instead of drawing the cursor
          } else
            if (!uiMaterial) {
                  btn.press(true);
            }
               armed = true;
            }
            break;
         case PenEvent.PEN_UP:
            pe = (PenEvent) event;
            if (event.target == this && !wasOpen && (armed || isActionEvent(event)))
            {
               if (captionPress != null && caption != null && pe.y <= fmHmin)
               {
                  captionPress.onCaptionPress();
                  cancelPopup = true;
               }
               else
               if (captionPress != null && captionIcon != null && pe.x <= captionIcon.getWidth())
               {
                  captionPress.onIconPress();
                  cancelPopup = true;
               }
               else
               {
              if (uiAndroid) {
                     Window.needsPaint = true; // guich@580_25: just call repaint instead of drawing the cursor
              } else
                if (!uiMaterial) {
                     btn.press(false);
                }
                  armed = false;
                  inside = isInsideOrNear(pe.x, pe.y); //  is a method of class Control that uses absolute coords
                  if (inside && (!Settings.fingerTouch || !hadParentScrolled()))
                  {
                if (!uiMaterial) {
                        open();
                  }
               }
            }
        }
            break;
         case ControlEvent.WINDOW_CLOSED:
            if (event.target == pop) // an item was selected?
            {
               wasOpen = true;
               opened = false;
               boolean isMulti = pop.lb instanceof MultiListBox;
               if (pop.lb.selectedIndex >= 0 && ((!isMulti && (!Settings.sendPressEventOnChange || pop.lb.selectedIndex != selOnPopup)) || (isMulti && ((MultiListBox)pop.lb).changed)))
               {
            if (uiMaterial && caption != null) {
                     animateMaterial(isDisplayed());
            }
                  postPressedEvent();
               }
               selOnPopup = -2;
            }
            break;
         case ControlEvent.PRESSED:
            if (!uiMaterial && event.target == btn && pop.lb.itemCount > 0)
            {
               btn.appId = this.appId; // guich@502_3: make the button have the same appid than us.
               event.consumed = true; // kevinsmotherman@450_22: avoid passing this to the other controls.
               popup();
            }
            break;
         case ControlEvent.FOCUS_IN:
        if (event.target == btn) {
               event.target = this;
        }
            break;
         case ControlEvent.FOCUS_OUT:
        if (event.target == btn) {
               event.target = this;
        }
        if (uiMaterial && caption != null) {
               animateMaterial(true);
        }
            break;
         case KeyEvent.ACTION_KEY_PRESS:
        if (!uiMaterial) {
               btn.simulatePress();
        }
            popup();
            break;
      }
   }
  }

   private void open()
   {
      if (!opened)
      {
         opened = true;
         selOnPopup = pop.lb.selectedIndex;
         popup();
      }
   }

   protected void updatePopRect()
   {
      Rect r = getAbsoluteRect();
      pop.fullHeight = fullHeight; // guich@330_52
      pop.fullWidth = fullWidth;
      pop.setRect(r.x, r.y, width, height);
   }
   
   private boolean isSupportedListBox()
   {
      String cl = pop.lb.getClass().getName();
      return cl.equals("totalcross.ui.ListBox") || cl.equals("litebase.ui.DBListBox");
   }
   
   /** Pops up the ComboBoxDropDown */
   public void popup()
   {
      requestFocus(); // guich@240_6: avoid opening the combobox when its popped up and the user presses the arrow again - guich@tc115_36: moved from the event handler to here
      boolean isMultiListBox = pop.lb instanceof MultiListBox;
      if (uiAndroid && _usePopupMenu && !isMultiListBox && isSupportedListBox()) // we don't support yet user-defined ListBox types yet
      {
      if (pop.lb.itemCount == 0) {
            opened = false;
      } else {
            try
            {
               PopupMenu pm = new PopupMenu(popupTitle != null ? popupTitle : "("+pop.lb.size()+")",pop.lb.getItemsArray(), isMultiListBox);
               pm.makeUnmovable();
               pm.enableSearch = enableSearch;
               pm.itemCount = pop.lb.size();
               pm.dataCol = pop.lb.dataCol;
               pm.checkColor = checkColor;
          if (!uiMaterial) {
            pm.setBackForeColors(pop.lb.backColor,pop.lb.foreColor);
          }
               pm.setCursorColor(pop.lb.back1);
               pm.setSelectedIndex(pop.lb.selectedIndex);
               pm.setFont(this.font);
          if (pm.itemCount > 100) {
            Flick.defaultLongestFlick = pm.itemCount > 1000 ? 9000 : 6000;
          } 
               pm.popup();
               Event.clearQueue(PenEvent.PEN_UP); // prevent problem when user selects an item that is at the top of this ComboBox
               Flick.defaultLongestFlick = 2500;
               opened = false;
               int sel = pm.getSelectedIndex();
          if (sel != -1) {
                  setSelectedIndex(sel);
            }
        }
            catch (Exception e)
            {
               e.printStackTrace();
            }
      }
    }
      else
      {
         updatePopRect();
      if (pop.lb.hideScrollBarIfNotNeeded()) {
            updatePopRect();
      }
         // guich@320_17
         if (!isMultiListBox)
         {
            int sel = pop.lb.selectedIndex;
            pop.lb.selectedIndex = -2;
            pop.lb.setSelectedIndex(sel);
         }
         pop.popupNonBlocking();
         pop.lb.requestFocus();
         isHighlighting = false; // kmeehl@tc100: allow immediate keyboard navigation of the dropdown
      }  
   }
   
   /** Unpops the ComboBoxDropDown.
    * @since TotalCross 1.2
    */
   public void unpop() // guich@tc120_64
   {
    if (pop.isVisible()){
         pop.unpop();
   }
  }

  @Override
   protected void onColorsChanged(boolean colorsChanged)
   {
      npback = nparmed = null;
      bColor = UIColors.sameColors ? backColor : Color.brighter(getBackColor()); // guich@572_15
      fColor = getForeColor();
      if (colorsChanged)
      {
      if (uiAndroid && btn != null) {
            btn.setImage(getArrowImage());
      } else if (!uiMaterial)
         {
            btn.setBackForeColors(uiVista ? Color.darker(bColor,32) : uiFlat ? bColor : backColor, foreColor);
            ((ArrowButton)btn).arrowColor = fColor;
         }
         pop.lb.setBackForeColors(backColor, foreColor);
      }
    if (!uiAndroid){
      Graphics.compute3dColors(isEnabled(), backColor, foreColor, fourColors);
   }
  }

   /** paint the combo's border and the current selected item */
  @Override
   public void onPaint(Graphics g)
   {
      boolean enabled = isEnabled();
      if (!uiMaterial)
      {
         if (!transparentBackground) // guich@tc115_18
         {
        if (!uiAndroid && uiVista && enabled) {
               g.fillVistaRect(0, 0, width, height, bColor, false, false);
        } else
            {
               g.backColor = uiAndroid ? parent.backColor : bColor;
               g.fillRect(0, 0, width, height);
            }
         }
      if (uiAndroid) {
            try
            {
          if (npback == null) {
                  npback = NinePatch.getInstance().getNormalInstance(NinePatch.COMBOBOX, width, height, enabled ? bColor : Color.interpolate(bColor,parent.backColor), false);
          }
          if ((armed || btn.armed) && nparmed == null) {
                  nparmed = npback.getTouchedUpInstance((byte)25,(byte)0);
          }
               Image img = armed || btn.armed ? nparmed : npback;
               g.drawImage(img, 0,0);
               g.setClip(2,2,width-btnW-(arrowStyle == ARROWSTYLE_PAGEFLIP ? 0 : 8),height-4);
            }
            catch (ImageException e) {e.printStackTrace();}
      } else
         {
            g.draw3dRect(0, 0, width, height, Graphics.R3D_CHECK, false, false, fourColors);
            g.setClip(2, 2, width - btnW - 3, height - 4);
         }
      }
      else
      {
         boolean hasFocus = super.hasFocus();
      if (captionIcon != null) {
            g.drawImage(captionIcon, 0, getTextY());
      }
         // material
         g.foreColor = hasFocus ? (captionColor != -1 ? captionColor : backColor) : Color.getGray(backColor);
         g.setFont(fcap);
         g.drawText(caption, xcap, ycap);
         g.setFont(font);
      }
    if (effect != null){
         effect.paintEffect(g);
    }
      if (uiMaterial)
      {
         if (fillColor != -1)
         {
            g.backColor = fillColor;
            g.fillRect(0,0,width,height);
         }
         int y = isSupportedListBox() ? getTextY() : (height-fmH)/2;         
         g.drawArrow(width-btnW*2-1,y+(fmH-btnW)/2,btnW, Graphics.ARROW_DOWN, false, checkColor != -1 && hasFocus() ? checkColor : fColor);
         g.setClip(0,0,width-btnW*2,height);
      }
    if (pop.lb.itemCount > 0 && pop.lb.selectedIndex >= 0){
         drawSelectedItem(g);
   }
  }

   private int getX0()
   {
      return captionIcon == null ? fmH/4 : captionIcon.getWidth() + fmH/4;
   }

   protected void drawSelectedItem(Graphics g)
   {
      g.foreColor = fColor;
      boolean trickW = pop.lb.width == 0; // guich@tc125_35
    if (trickW){
      pop.lb.width = width - btnW;
    }
      pop.lb.drawSelectedItem(g, pop.lb.selectedIndex, getX0(), getTextY()); // guich@200b4: let the listbox draw the item
    if (trickW){
      pop.lb.width = 0;
    }
   }

   /** Sorts the items of this combobox, and then unselects the current item. */
   public void qsort() // guich@220_35
   {
      pop.lb.qsort();
      setSelectedIndex(-1);
   }

   /** Sorts the elements of this ListBox. The current selection is cleared.
    * @param caseless Pass true to make a caseless sort, if the items are Strings. 
    */
   public void qsort(boolean caseless) // guich@tc113_5
   {
      pop.lb.qsort(caseless);
      setSelectedIndex(-1);
   }
   
   /**
    * Adds support for horizontal scroll on this listbox. Two buttons will
    * appear below the vertical scrollbar. The add, replace and remove
    * operations will be a bit slower because the string's width will have to be
    * computed in order to correctly set the maximum horizontal scroll.
    *
    * @since SuperWaba 5.6
    */
   public void enableHorizontalScroll() // guich@560_9
   {
      pop.lb.enableHorizontalScroll();
   }

   /**
    * Selects the last item added to this combobox, doing a scroll if needed.
    * Calls repaintNow.
    *
    * @since SuperWaba 5.6
    */
   public void selectLast()
   {
      pop.lb.selectLast();
      repaintNow();
   }

   /** Clears this control, selecting index clearValueInt (0 by default); uses clearValueStr if set, instead. */
  @Override
   public void clear() // guich@572_19
   {
      final int clearValueStrIdx = clearValueStr != null ? indexOf(clearValueStr) : -1;
      setSelectedIndex(clearValueStrIdx > -1 ? clearValueStrIdx : clearValueInt);
   }

  @Override
   public void getFocusableControls(Vector v) // kmeehl@tc100
   {
    if (visible && isEnabled()){
      v.addElement(this);
    }
   }

  @Override
   public Control handleGeographicalFocusChangeKeys(KeyEvent ke) // kmeehl@tc100
   {
      if (armed)
      {
         _onEvent(ke);
         return this;
      }
      return null;
   }
   
   /** Returns the ListBox used when this combobox is opened. */
   public ListBox getListBox()
   {
      return pop.lb;
   }
   
   /** Selects the item that starts with the given text
    * @param text The text string to search for
    * @param caseInsensitive If true, the text and all searched strings are first converted to lowercase.
    * @return If an item was found and selected.
    * @since TotalCross 1.13
    */
   public boolean setSelectedItemStartingWith(String text, boolean caseInsensitive) // guich@tc113_2
   {
      return setSelectedItemStartingWith(text, caseInsensitive, Settings.sendPressEventOnChange);
   }

   /** Selects the item that starts with the given text
    * @param text The text string to search for
    * @param caseInsensitive If true, the text and all searched strings are first converted to lowercase.
    * @param sendPress If true, sends the PRESSED event
    * @return If an item was found and selected.
    * @since TotalCross 1.13
    */
   public boolean setSelectedItemStartingWith(String text, boolean caseInsensitive, boolean sendPress) // guich@tc310
   {
      int idx = pop.lb.selectedIndex;
      boolean b = pop.lb.setSelectedItemStartingWith(text, caseInsensitive);
    if (sendPress && pop.lb.selectedIndex != idx){
         postPressedEvent();
    }
      return b;
   }

   /** Replaces the original ComboBoxDropDown by the given one.
    * @since TotalCross 1.5
    */
   public void setPop(ComboBoxDropDown lb)
   {
      pop = lb;
      setSelectedIndex(-1);
   }
   
  @Override
   public boolean equals(Object o)
   {
      if (o != null && o instanceof String[])
      {
         String[] s = (String[])o;
         Vector v = pop.lb.items;
      if (v.size() != s.length) {
            return false;
      } else
         {
        for (int i = s.length; --i >= 0;) {
          if ((s[i] == null && s[i] != v.items[i]) || (s[i] != null && !s[i].equals(v.items[i]))) {
                  return false;
          }
        }
            return true;
         }
      }
      return super.equals(o);
   }
   
   // material
   private void animateMaterial(boolean slow)
   {
      if (tea == null)
      {
         int sel = getSelectedIndex();
         fmHtarget = sel != -1 ? fmHmin : fmH;
         if (fcap.size != fmHtarget)
         {
            if (slow)
            {
               inccap = fcap.size == fmH ? -1 : 1;
               addTimerListener(this);
               tea = addTimer(10);
            }
            else
            {  
               inccap = fmHtarget-fcap.size;
               singleStep();
            }
         }
      }
   }
   
  @Override
   public void timerTriggered(TimerEvent e)
   {
      if (tea != null && tea.triggered)
      {
         singleStep();
         Window.needsPaint = true;
         if (fcap.size == fmHtarget)
         {
            removeTimer(tea);
            removeTimerListener(this);
            tea = null;
         }
      }
   }

   private void singleStep()
   {
      fcap = fcap.adjustedBy(inccap);
      ycap = ycap0 * (fcap.size - fmHmin) / (fmH - fmHmin);
      xcap = xcap0; // no horizontal movement - xcap0 * (fcap.size - fmHmin) / (fmH - fmHmin);
   }

  @Override
   public void sideStart()
   {
   }

  @Override
   public void sideStop()
   {
    if (cancelPopup){
         cancelPopup = false;
    }else {
         open();
   }
  }

  @Override
   public void sidePaint(Graphics g, int alpha)
   {
   }
}
