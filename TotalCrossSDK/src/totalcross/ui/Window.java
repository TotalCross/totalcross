/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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



package totalcross.ui;

import totalcross.*;
import totalcross.sys.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.media.*;
import totalcross.unit.*;
import totalcross.util.*;

/**
 * Window is a floating top-level window.
 * <p>
 * A window cannot be added to a container; use popup or popupNonBlocking to show it on screen.
 * Trying to add it to a container will raise a RuntimeException.
 * <P>
 * The following example creates a popup window class:
 *<pre>
 *  class TestWindow extends Window
 *  {
 *     Button btnHi;
 *     public TestWindow()
 *     {
 *        super("Test",RECT_BORDER); // with caption and borders
 *        setRect(CENTER,CENTER,Settings.screenWidth/2,Settings.screenHeight/4);
 *        add(btnHi=new Button("Hi!"),CENTER,CENTER);
 *     }
 *     public void onEvent(Event event)
 *     {
 *        if (event.type == ControlEvent.PRESSED && event.target == btnHi)
 *           unpop(); // a WINDOW_CLOSED event will be posted to this PARENT window.
 *     }
 *  }
 *</pre>
 * To use it in the normal way (<b>blocking</b>):
 *<pre>
 *  public class Launcher extends MainWindow
 *  {
 *     Button btn;
 *     public void onEvent(Event e)
 *     {
 *        if (e.target == btn)
 *        {
 *           TestWindow tw = new TestWindow();
 *           tw.popup();
 *           // this line is only executed after the window is closed.
 *        }
 *     }
 *  }
 *</pre>
 * To use it <b>non-blocking</b> (the execution continues right after the popup command, even with
 * the window still open):
 *<pre>
 *  public class Launcher extends MainWindow
 *  {
 *     TestWindow tw;
 *     public void initUI()
 *     {
 *        tw = new TestWindow();
 *        tw.popupNonBlocking();
 *        // this line is executed immediately
 *     }
 *     public void onEvent(Event event)
 *     {
 *        if (event.target == tw && event.type == ControlEvent.WINDOW_CLOSED)
 *        {
 *           // any stuff
 *           break;
 *        }
 *     }
 *  }
 *</pre>
 * Blocking popup may be use in InputBox/MessageBox classes, while non-blocking popup
 * is used in MenuBar and other classes.
 * <p> Important note: you can't use popup with a delay to unpop it.
 * In this case, the correct would be to use popupNonBlocking:
 * <pre>
 * mb = new MessageBox(...);
 * mb.popupNonBlocking();
 * Vm.sleep(5000); // or do something else
 * mb.unpop;
 * </pre>
 * If you use popup in this specific case, the Vm will hang.
 */

public class Window extends Container
{
   /** True if some area of any window is invalidated */
   public static boolean needsPaint;
   /** Window's title */
   protected String title; // guich@102
   /** When this window is the top most and the user clicks outside, a beep is thrown. Set this to false do disable the beep. */
   protected boolean beepIfOut = true; // guich@102: cancels the event if the user type outside the window area. to be used in a future window manager
   /** The last poped up window. */
   protected static Window topMost;
   /** Font for the title. Usually, MainWindow.defaultFont bolded */
   protected Font titleFont; // guich@110
   /** The window's border style */
   protected byte borderStyle;
   /** The window's menu bar */
   protected Control menubar; // guich@200
   /** If true (default), the user can drag this window around */
   protected boolean canDrag = true;

   /** Must set to true if your Window is prepared for 320x320 resolutions.
    *  If false (default), the Window is doubled size (and centered) to make controls fit.
    */
   /** @deprecated */
   protected boolean highResPrepared = Settings.platform==null?false:!Settings.platform.equals(Settings.PALMOS); // guich@400_35: as default for WinCE, highres is true - use indexOf to support PalmOS/SDL - guich@552_6: added the ! - guich@553_6: check if null to let retroguard run

   /** A temporary title that will be displayed when this Windows pops up. It will be replaced by the original title when it is closed. 
    * @since TotalCross 1.53
    */
   public String tempTitle;

   /** @deprecated Flick is now enabled by default; just remove the reference to it. */
   public static boolean flickEnabled;
   
   static boolean isSipShown;
   static int []borderGaps = {0,1,2,1,0,0,0}; // guich@200final_14 - guich@400_77 - guich@564_16
   protected Control _focus,focusOnPenUp;
   private Control focusOnPopup; // last control that had focus when popup was called.
   /** the control that should get focus when a focus traversal key is pressed and none have focus */
   public Control firstFocus; // kmeehl@tc100
   private boolean isMoving;
   private int xMoving,yMoving;
   private int xDeltaMoving, yDeltaMoving;
   private Coord ptMoving;
   Control tempFocus; // guich@320_31
   private Control mouseMove; // guich@tc126_45
   protected Rect rTitle; // guich@200b4_52: the area where the title is located - guich@tc120_61: now its protected
   protected Control highlighted; // guich@550_15
   private Control grabPenEvents;
   private int gpeX,gpeY; // the relative position of the grabPenEvents control
   private static int ptDraggingX,ptDraggingY; // kmeehl@tc100 from here
   private static int ptPenDownX,ptPenDownY,shiftYAtPenDownY;
   private static boolean firstDrag = true;
   private static int lastType, lastTime, lastX, lastY;
   private static int repeatedEventMinInterval = Settings.isIOS() || Settings.platform.equals(Settings.ANDROID) ? 40 : 0;
   private String oldTitle;
   protected int footerH;
   /** If true, the next pen_up event will be ignored. This is used when a pen_down cancels a flick, or if a drag-scrollable control
    * needs to cancel the next pen_up during a drag-scrolling interaction. */
   public static boolean cancelPenUp;
   private static Control fakeControl;
   /** True if the last popup mode was blocking. */
   protected boolean blocking;
   Vector cancelPenUpListeners = new Vector(2);
   /** The starting and ending colors used to fill the gradient title.
    * @see #HORIZONTAL_GRADIENT
    * @see #VERTICAL_GRADIENT
    */
   public int gradientTitleStartColor=-1, gradientTitleEndColor=-1;
   /** The title color. The title color depends on the border type: it will be the foreground color if NO_BORDER is set, otherwise will be the background color. */
   public int titleColor = -1; // guich@tc110_13

   /** A vertical gap used to increase the title area. Defaults to fmH/2 in Android, 0 on other user interface styles.
    * @since TotalCross 1.3.4
    */
   public int titleGap;
   
   /** The title horizontal alignment in the Window's title area. 
    * It can be LEFT, CENTER or RIGHT, and you can use an adjustment on the value (E.G.: LEFT+5).
    * @since TotalCross 1.3
    */
   public int titleAlign = CENTER;

   /** Set the header and the footer colors when in Android style and border type is ROUND_BORDER. 
    * Not used in other styles.
    * @since TotalCross 1.3 
    */
   public int headerColor=-1, footerColor=-1;

   /** Set to true to make the other windows be faded when this window appears.
    * @since TotalCross 1.2
    */
   public boolean fadeOtherWindows; // guich@tc120_43
   
   /** The value used to fade the other windows. Defaults to 128.
    * @since TotalCross 1.2
    */
   public static int fadeValue = 128;
   
   /** The UIRobot instance that is being used to record or play events. */
   public static UIRobot robot;

   protected static int androidBorderThickness;
   
   /** Used in popup */
   protected boolean popped;
   /** Used in the swap method */
   protected Container mainSwapContainer;
   /** Used in the swap method */
   protected Container lastSwappedContainer;

   /** To be used in setBorderStyle */
   public static final byte NO_BORDER = 0;
   /** To be used in setBorderStyle */
   public static final byte RECT_BORDER = 1;
   /** To be used in setBorderStyle. */
   public static final byte ROUND_BORDER = 2;
   /** To be used in setBorderStyle */
   public static final byte TAB_BORDER = 3;
   /** To be used in setBorderStyle */
   public static final byte TAB_ONLY_BORDER = 4;
   /** To be used in setBorderStyle.
    * @see #gradientTitleStartColor
    * @see #gradientTitleEndColor
    */
   public static final byte HORIZONTAL_GRADIENT = 5;
   /** To be used in setBorderStyle.
    * @see #gradientTitleStartColor
    * @see #gradientTitleEndColor
    */
   public static final byte VERTICAL_GRADIENT = 6;

   /** Used to hide the virtual keyboard */
   public static final int SIP_HIDE = 10000;
   /** Used to place the virtual keyboard on top of screen.
   */
   public static final int SIP_TOP = 10001;
   /** Used to place the virtual keyboard on bottom of screen.
    */
   public static final int SIP_BOTTOM = 10002;
   /** Used to show the virtual keyboard, without changing the position */
   public static final int SIP_SHOW = 10003;
   /** Used to enable the numeric pad on devices that have a hard keyboard. */
   public static final int SIP_ENABLE_NUMERICPAD = 10004; // guich@tc110_55
   /** Used to disable the numeric pad on devices that have a hard keyboard. */
   public static final int SIP_DISABLE_NUMERICPAD = 10005; // guich@tc110_55

   /** Stack of poped up windows. Dont mess with this, unless you want to crash the VM! */
   public static totalcross.util.Vector zStack = new totalcross.util.Vector(5); // guich@102
   
   /** Used internally.
    * @see totalcross.ui.event.Event#clearQueue(int)
    */
   public static int ignoreEventOfType = -1; // guich@tc120_44
   
   // cache event objects to minimize memory usage
   protected KeyEvent _keyEvent = new KeyEvent();
   protected PenEvent _penEvent = new PenEvent();
   protected ControlEvent _controlEvent = new ControlEvent();
   protected DragEvent _dragEvent = new DragEvent();
   private static int currentDragId;
   protected MouseEvent _mouseEvent = new MouseEvent();
   protected MultiTouchEvent _multiEvent = new MultiTouchEvent();
   private static boolean lastInside;
   protected boolean multiTouching;
   
   public static int shiftY,shiftH,lastShiftY;
   
   // control the highlight rectangle
   private int[] behindHighlightRect;
   private Control lastHighlighted;
   
   // drag threshold
   public static int dragThreshold = getDefaultDragThreshold();
   private static final double DEFAULT_DRAG_THRESHOLD_IN_INCHES_PEN = 1.0 * 0.0393700787; // 0.5mm
   private static final double DEFAULT_DRAG_THRESHOLD_IN_INCHES_FINGER = 1.0 * 0.0393700787; // 1.0mm
   
   /** A key listener that have priority over all other listeners. */
   public static KeyListener keyHook;
   
   ////////////////////////////////////////////////////////////////////////////////////
   ////////////////////////////////////////////////////////////////////////////////////
   /** Constructs a window with no title and no border. */
   public Window()
   {
      ignoreOnAddAgain = ignoreOnRemove = true;
      this.width  = Settings.screenWidth;
      this.height = Settings.screenHeight;
      asWindow = this;
      foreColor = UIColors.controlsFore; // assign the default colors
      backColor = UIColors.controlsBack;
      titleFont = MainWindow.defaultFont.asBold();
      titleGap = uiAndroid ? titleFont.fm.height/2 : 0;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Constructs a window with the given title and border.
    * @see #NO_BORDER
    * @see #RECT_BORDER
    * @see #ROUND_BORDER
    * @see #TAB_BORDER
    * @see #TAB_ONLY_BORDER
    * @see #HORIZONTAL_GRADIENT
    * @see #VERTICAL_GRADIENT
    */
   public Window(String title, byte borderStyle) // guich@112
   {
      this();
      this.borderStyle = borderStyle;
      setTitle(title);
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Set to a control to redirect all pen events directly to it. This speeds up pen event processing. Used in Whiteboard class.
    * @since TotalCross 1.0
    */
   public void setGrabPenEvents(Control c)
   {
      this.grabPenEvents = c;
      gpeX = gpeY = 0;
      while (c != null) // translate x, y to coordinate system of target
      {
         gpeX -= c.x;
         gpeY -= c.y;
         c = c.parent;
      }
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Sets the title font */
   public void setTitleFont(Font titleFont)
   {
      this.titleFont = titleFont;
      titleGap = uiAndroid ? titleFont.fm.height/2 : 0;
      rTitle = null;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Gets the title font */
   public Font getTitleFont()
   {
      return this.titleFont;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Sets the title and call repaint. */
   public void setTitle(String title) // guich@102
   {
      this.title = title != null && title.length() > 0 ? title : null;
      this.rTitle = null; // guich@400_38
      setTitleFont(this.titleFont);
      needsPaint = true;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Sets the title text in the task bar for non-Palm OS operating systems.
    * Does not work in full-screen mode.
    * @since TotalCross 1.0
    */
   final public static void setDeviceTitle(String title)
   {
      totalcross.Launcher.instance.setTitle(title);
   }
   native static void setDeviceTitle4D(String title);
   ////////////////////////////////////////////////////////////////////////////////////
   /** Sets the border borderStyle.
    * @see #NO_BORDER
    * @see #RECT_BORDER
    * @see #ROUND_BORDER
    * @see #TAB_BORDER
    * @see #TAB_ONLY_BORDER
    * @see #HORIZONTAL_GRADIENT
    * @see #VERTICAL_GRADIENT
    */
   public void setBorderStyle(byte borderStyle)
   {
      this.borderStyle = borderStyle;
      rTitle = null;
      needsPaint = true;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Returns the border style of this window. */
   public byte getBorderStyle()
   {
      return borderStyle;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Makes this window static, i.e., the user will not be able to move it around
     * the screen by dragging the title area. It just set the canDrag member to false.
     */
   public void makeUnmovable() // guich@200b4_194
   {
      canDrag = false;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /**
    * Sets focus to the given control. When a user types a key, the control with
    * focus get the key event. At any given time, only one control in a window
    * can have focus. Calling this method will cause a FOCUS_OUT control event
    * to be posted to the window's current focus control (if one exists)
    * and will cause a FOCUS_IN control event to be posted to the new focus
    * control.
    */
   public void setFocus(Control c)
   {
      if (_focus == c)
         return;

      while (c != null) // kmeehl@tc100: bubble up to find a suitable candidate
         if (!c.focusLess && c.focusTraversable) // kmeehl@tc100: focusTraversable is now a reliable way to determine if a control should get focus
         {
            removeFocus();
            if (_focus == null) // guich@tc100: maybe the user changed the focus to a new control in the FOCUS_OUT event
            {
               _focus = c;
               if (c.isEnabled()) // guich@tc152: disabled controls can't send focus events
               {
                  _controlEvent.type = ControlEvent.FOCUS_IN;
                  _controlEvent.target = c;
                  _controlEvent.touch();
                  c.postEvent(_controlEvent);
               }
               setHighlighted(_focus);
            }
            return;
         }
         else
            c = c.parent;
      if (!Settings.geographicalFocus) // kmeehl@tc100: if we are in taborder focus mode and there were no candidates
         removeFocus();
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /**
    * Calling this method will cause a FOCUS_OUT control event
    * to be posted to the window's current focus control (if one exists)
    */
   public void removeFocus() // kmeehl@tc100
   {
      if (_focus != null)
      {
         _controlEvent.type = ControlEvent.FOCUS_OUT;
         _controlEvent.target = _focus;
         _controlEvent.touch();
         Control temp = _focus;
         _focus = null; // avoid recursion
         temp.postEvent(_controlEvent);
         setHighlighting();
      }
      
      if (highlighted != null && (Settings.geographicalFocus || Settings.keyboardFocusTraversable)) // kmeehl@tc100: prevent highlighted from being trampled in taborder focus mode
      {
         _controlEvent.type = ControlEvent.HIGHLIGHT_OUT;
         _controlEvent.update(highlighted);
         highlighted.postEvent(_controlEvent); // kmeehl@tc100: send the currently highlighted control a HIGHLIGHT_OUT event
         highlighted = null;
         if (Settings.isOpenGL)
            needsPaint = true;
         else
         {
            drawHighlight(null);
            safeUpdateScreen();
         }
      }
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Changes the focus to the desired control, without posting FOCUS_IN and FOCUS_OUT events
     * @since SuperWaba 4.01
     */
   protected void swapFocus(Control c)
   {
      if (c != null && !c.focusLess && c.focusTraversable && _focus != c) // kmeehl@tc100: make sure the control is capable of receiving focus
         setHighlighted(_focus = c);
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /**
    * Returns the focus control for this window. This will be the same of
    * <control>focusedControl</control>, unless the last one has been changed manually.
    * @see totalcross.ui.Window#setFocus
    */
   public Control getFocus()
   {
      return _focus;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** called when the user clicks outside the bounds of this window. must return true
    * if the event was handled, false otherwise. If false is returned and <code>beepIfOut</code>
    * is true, then a beep is played and nothing more happens.
    * @since SuperWaba 1.2 */
   protected boolean onClickedOutside(PenEvent event) // kmeehl@tc100: changed signature to pass the event, to give the window more information
   {
      return false;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Calls _doPaint if the window needs painting.
     * @since SuperWaba 4.0
     */
   public void validate() // guich@400_44
   {
      if (needsPaint)
         repaintActiveWindows();
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /**
    * Called by the VM to post key and pen events.
    */
   final public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp)
   {
      boolean isPenEvent = !multiTouching && PenEvent.PEN_DOWN <= type && type <= PenEvent.PEN_DRAG;
      boolean isKeyEvent = type == KeyEvent.KEY_PRESS || type == KeyEvent.SPECIAL_KEY_PRESS;
      if (isKeyEvent && Settings.optionalBackspaceKey != 0 && key == Settings.optionalBackspaceKey)
         key = SpecialKeys.BACKSPACE;
      if (isKeyEvent && Settings.deviceRobotSpecialKey != 0 && Settings.deviceRobotSpecialKey == key)
      {
         onRobotKey();
         return;
      }
      if (ignoreEventOfType == 0 || ignoreEventOfType == type || (isPenEvent && type == lastType && x == lastX && y == lastY)) // guich@tc122_9: discard duplicate pen events
         return;
      
      int currentTime = Vm.getTimeStamp();
      if (timeStamp == 0)
         timeStamp = currentTime; // guich@401_13: get the timestamp - bruno@tc115: must come before setting lastInteractionTime
      
      if (robot != null && UIRobot.status == UIRobot.RECORDING && this == topMost)
         robot.onEvent(type, key, x, y, modifiers);
      
      if (isPenEvent) // do all the pen event filtering here
      {
         if (grabPenEvents != null) // guich@tc100 - guich@tc168: moved to here to prevent gaps when Whiteboard is inside a window
         {
            PenEvent pe = type == PenEvent.PEN_DOWN || type == PenEvent.PEN_UP ? _penEvent : _dragEvent; // guich@tc130: fix ClassCastException when a WhiteBoard had a ToolTip attached
            Control c = _focus;
            while (c != null)
            {
               x -= c.x;
               y -= c.y;
               c = c.parent;
            }
            grabPenEvents._onEvent(pe.update(grabPenEvents, x, x+gpeX, y, y+gpeY, type, modifiers));
            return;
         }
         if (type == PenEvent.PEN_DRAG && firstDrag) // discard first PEN_DRAG unless it exceeds the drag threshold
         {
            int absDeltaX = x > ptPenDownX ? x - ptPenDownX : ptPenDownX - x;
            int absDeltaY = y > ptPenDownY ? y - ptPenDownY : ptPenDownY - y;
            if (absDeltaX < Settings.touchTolerance && absDeltaY < Settings.touchTolerance)
               return;
         }
         if (type == lastType && ((x == lastX && y == lastY) || (timeStamp - lastTime) < repeatedEventMinInterval)) // discard pen events of the same type that have the same coordinates or that were sent too quickly
            return;
         if (type == PenEvent.PEN_UP && cancelPenUp)
         {
            for (int i = cancelPenUpListeners.size(); --i >= 0;)
               ((PenListener)cancelPenUpListeners.items[i]).penUp(null);
            cancelPenUp = false;
            if (tempFocus != null && tempFocus instanceof Button)
            {
               // allow buttons to pop back up (and prevent them from firing)
               ControlEvent ce = _controlEvent;
               ce.type = ControlEvent.FOCUS_OUT;
               ce.target = tempFocus;
               ce.timeStamp = timeStamp;
               ce.consumed = false;
               tempFocus.postEvent(_controlEvent);
               tempFocus.repaintNow();
            }
            tempFocus = null; // release tempFocus
            return;
         }
      }
         
      if (key == SpecialKeys.SCREEN_CHANGE) // dont move from here!
      {
         MainWindow.mainWindowInstance.width  = Settings.screenWidth;
         MainWindow.mainWindowInstance.height = Settings.screenHeight;
         for (int i = 0; i < zStack.size(); i++) // guich@tc114_61: if a window unpops during screenResized below, the number of windows will be decreased.
            ((Window)zStack.items[i]).screenResized(); // guich@tc123_21: only call screenResized here.
         repaintActiveWindows(); // guich@tc123_21: let this method handle the screen update
         return;
      }
      if (this != topMost) // if this isnt the top-most window, pass control to it. i must do this because the vm calls MainWindow._postEvent
      {
         if (topMost != null)
            topMost._postEvent(type, key, x, y, modifiers, timeStamp);
         return;
      }
      
      lastType = type;
      lastTime = timeStamp;
      lastX = x;
      lastY = y;
      if (isPenEvent || type == ControlEvent.SIP_CLOSED)
      {
         if (type == ControlEvent.SIP_CLOSED)
         {
            shiftScreen(null,0);
            isSipShown = false;
            repaintActiveWindows();
            return;
         }
         if (shiftY > 0) shiftY = 0; // required for WP8. not sure on other platforms
         if (shiftY != 0) // is the screen shifted?
         {
            if (y >= shiftH && type == PenEvent.PEN_DOWN) // if screen is shifted and user clicked below the visible area, unshift screen
            {
               lastY = lastShiftY = 0;
               if (Settings.onJavaSE) 
               {
                  cancelPenUp = true;
                  shiftScreen(null,0);
                  return;
               }
            }
            else
            {
               lastY = y = y + shiftY; // shift the y coordinate to the place that the component "thinks" it is.
            }
         }
         else
         if (!Settings.platform.equals(Settings.WINDOWSPHONE) && lastShiftY != 0) // if the user clicked in a button (like in a Cancel button of a Window), we have to keep shifting the coordinate until the pen_up occurs
         {
            lastY = y = y + lastShiftY;
            if (type == PenEvent.PEN_UP)
               lastY = lastShiftY = 0;
         }
      }
      
      if (Settings.debugEvents)
         Vm.debug(this+" event: type="+type+", key="+key+" ("+(char)key+"), x="+x+", y="+y+", mods="+modifiers+", time="+timeStamp);
      
      if (type < 300) // bruno@tc114_38: store the last time the user has interacted with the device (via keyboard or pen/touch)
         Settings.lastInteractionTime = currentTime; // don't use the event timestamp, since it can be wrong! it's better to get the current timestamp instead.
      
      Event event=null;
      boolean invokeMenu = false;
      if (_focus == null) _focus = this; // guich@200b4: make sure that there is always one control with focus. this test was being made in // 1 and // 2

      if (type == MultiTouchEvent.SCALE)
      {
         if (key == 1)
            multiTouching = true;
         else
         if (key == 2)
            multiTouching = false;
         else
         {
            long l = ((((long)x & 0xFFFFFFFFL) << 32) | ((long)y & 0xFFFFFFFFL));
            _multiEvent.update(_focus, Convert.longBitsToDouble(l));
            _focus.postEvent(_multiEvent);
         }
         if (needsPaint || Container.nextTransitionEffect != Container.TRANSITION_NONE) // guich@200b4_18: maybe the current event had poped up a Window.
            repaintActiveWindows(); // guich@tc100: paint the topMost, not ourselves.
         return;
      }
      if (isPenEvent) 
      {
         switch (type)
         {
            case PenEvent.PEN_DOWN:
               cancelPenUp = Flick.currentFlick != null;
               break;
            case PenEvent.PEN_DRAG:
               cancelPenUp = false;
               break;
         }
      }
      // guich@200b4: code to move the window.
      if (Flick.currentFlick == null && (isMoving || (isPenEvent && rTitle != null && rTitle.contains(x-this.x,y - this.y))))
      {
         if (shiftY != 0)
            return;
         switch (type)
         {
            case PenEvent.PEN_DOWN:
                  ptMoving = new Coord(x, y);
               isMoving = false;
               break;
            case PenEvent.PEN_DRAG:
               if (!isMoving && ptMoving != null && canDrag && (Math.abs(ptMoving.x - x) > 2 || Math.abs(ptMoving.y - y) > 2))
               {
                  isMoving = true;
                  xMoving = this.x;
                  yMoving = this.y;
                  xDeltaMoving = ptMoving.x - xMoving; // guich@200b4_157
                  yDeltaMoving = ptMoving.y - yMoving;
               }
               else if (isMoving)
               {
                  this.x = xMoving; // guich@tc110_15
                  this.y = yMoving;
                  if (Settings.onJavaSE || Settings.fingerTouch)
                     Event.clearQueue(PenEvent.PEN_DRAG);
                  repaintActiveWindows();
                  xMoving = x - xDeltaMoving;
                  yMoving = y - yDeltaMoving;
               }
               break;
            case PenEvent.PEN_UP:
               invokeMenu = ptMoving != null && (!isMoving || this instanceof MainWindow);
                  isMoving = false;
               ptMoving = null;
               break;
         }
         if (!invokeMenu) return;
      }

      // checks for mouse wheel on DESKTOP/WIN32 only
      if (Settings.scrollDistanceOnMouseWheelMove != 0 && type == MouseEvent.MOUSE_WHEEL && contains(x, y))
      {
         Control c = findChild(x - this.x, y - this.y);
         for (; c != null && !(c instanceof Scrollable); c = c.parent) {}
         if (c != null && c instanceof Scrollable)
         {
            int k = Settings.scrollDistanceOnMouseWheelMove;
            Scrollable sc = (Scrollable)c;
            boolean canScrollVert  = sc.canScrollContent(DragEvent.UP,sc)   || sc.canScrollContent(DragEvent.DOWN,sc);
            boolean canScrollHoriz = sc.canScrollContent(DragEvent.LEFT,sc) || sc.canScrollContent(DragEvent.RIGHT,sc);
            int kx = 0, ky = 0;
            if (canScrollVert && !canScrollHoriz) // only vertical?
               switch (key)
               {
                  case DragEvent.DOWN : 
                  case DragEvent.LEFT : ky =  k; break;
                  case DragEvent.UP   : 
                  case DragEvent.RIGHT: ky = -k; break;
               }
            else
            if (!canScrollVert && canScrollHoriz) // only horizontal
               switch (key)
               {
                  case DragEvent.DOWN : 
                  case DragEvent.LEFT : kx =  k; break;
                  case DragEvent.UP   : 
                  case DragEvent.RIGHT: kx = -k; break;
               }
            else // both horizontal and vertical
               switch (key)
               {
                  case DragEvent.DOWN : ky =  k; break;
                  case DragEvent.UP   : ky = -k; break;
                  case DragEvent.LEFT : kx =  k; break;
                  case DragEvent.RIGHT: kx = -k; break;
               }
            if (ky != 0)
               for (int i = 0,n=ky>0?ky:-ky, inc=ky>0?1:-1; i < n; i++)
               {
                  sc.scrollContent(0, inc, false);
                  repaintNow();
                  if (!Settings.onJavaSE) Vm.sleep(1);
               }
            else
            if (kx != 0)
               for (int i = 0,n=kx>0?kx:-kx, inc=kx>0?1:-1; i < n; i++)
               {
                  sc.scrollContent(inc, 0, false);
                  repaintNow();
                  if (!Settings.onJavaSE) Vm.sleep(1);
               }
            return;
         }
      }
      if (isPenEvent || type == MouseEvent.MOUSE_MOVE) // guich@102: user clicked outside the window? - guich@tc126_45: send MOUSE_IN/OUT when the window bounds are crossed
      {
         boolean inside = contains(x, y);
         if (type == MouseEvent.MOUSE_MOVE)
         {
            if (inside != lastInside) // if the mouse passed against the window bounds (we only have to post events to the topmost window)
            {
               lastInside = inside;
               _mouseEvent.target = this;
               _mouseEvent.consumed = false;
               _mouseEvent.timeStamp = timeStamp;
               _mouseEvent.type = inside ? MouseEvent.MOUSE_IN : MouseEvent.MOUSE_OUT;
               postEvent(_mouseEvent);
               return;
            }
         }
         else
         if (!inside && !Flick.isDragging) // guich@tc130: must check if user is dragging before a flick, otherwise, in a paged-flick, the process would end too early 
         {
            _penEvent.type = type;
            _penEvent.x = x;
            _penEvent.y = y;
            _penEvent.modifiers = modifiers;
            _penEvent.target = null;
            _penEvent.touch();
            if (_focus != null && _focus != this && (_focus == _dragEvent.target || x == 10000) && type == PenEvent.PEN_UP) // guich@gc153: fixed problem of clicking in the Calendar's button making it repeat and dragging the mouse outside the window. without this, the button will repeat forever -- x = 10000 is sent when a multitouch will begin
               _focus.postEvent(_penEvent);
            if (!onClickedOutside(_penEvent)) // if clicked outside was not handled by this method...
               if (type == PenEvent.PEN_DOWN && beepIfOut && !fadeOtherWindows) // alert him! - ds: i changed this accordingly to your comments about win32 problems
                  Sound.beep();
            return;
         }
      }

      if (isKeyEvent)
      {
         _keyEvent.key = key;
         _keyEvent.modifiers = modifiers;
         _keyEvent.type = type;
         event = _keyEvent;
         
         if (isKeyEvent && keyHook != null)
         {
            _keyEvent.consumed = false;
            switch (type)
            {
               case KeyEvent.KEY_PRESS:         keyHook.keyPressed(_keyEvent);         break;
               case KeyEvent.ACTION_KEY_PRESS:  keyHook.actionkeyPressed(_keyEvent);   break;
               case KeyEvent.SPECIAL_KEY_PRESS: keyHook.specialkeyPressed(_keyEvent);  break;
            }
            if (_keyEvent.consumed)
               return;
         }


         if (Settings.geographicalFocus && _keyEvent.isActionKey()) _keyEvent.type = KeyEvent.ACTION_KEY_PRESS; // kmeehl@tc100 from here

         if (!Settings.geographicalFocus && Settings.keyboardFocusTraversable && (_keyEvent.isPrevKey() || _keyEvent.isNextKey() || _keyEvent.isActionKey()))
         {
            if (isHighlighting && handleFocusChangeKeys(_keyEvent))
            {
               if (needsPaint) // commit any pending paint before returning
                  repaintActiveWindows(); // guich@tc100: paint the topMost, not ourselves.
               return;
            }
         }
         else
         if (Settings.keyboardFocusTraversable && (_keyEvent.isPrevKey() || _keyEvent.isNextKey()))
         {
            // geographical focus
            Control c;
            if (_focus != null && highlighted == _focus && (c = bubbleHandleFocusChangeKey(_focus, _keyEvent)) != null)
            {
               setFocus(c);
               if (needsPaint) // commit any pending paint before returning
                  repaintActiveWindows(); // guich@tc100: paint the topMost, not ourselves.
               return;
            }
            else
            {
               c = highlighted == null ? _focus : highlighted;
               if (c == this || c == null || !c.focusTraversable || c.focusLess)
               { // find a new control to set focus to
                  if (firstFocus == null || firstFocus.focusLess || !firstFocus.focusTraversable || !firstFocus.visible || !firstFocus.isEnabled()) // kmeehl@tc100: if firstfocus is set and focusable, set it as the first control to get focus
                  {
                     if (fakeControl == null) // create a fake control and find the closest control to the top-left corner
                     {
                        fakeControl = new Control();
                        fakeControl.setRect(-1, -1, 1, 1);
                     }
                     c = findNextFocusControl(fakeControl,SpecialKeys.RIGHT);
                  }
                  else
                     c = firstFocus;
               }
               else
               {
                  int direction = 0;
                  if (_keyEvent.isUpKey()) direction = SpecialKeys.UP;             // this order must
                  else if (_keyEvent.isDownKey()) direction = SpecialKeys.DOWN;    // be preserved
                  else if (_keyEvent.isNextKey()) direction = SpecialKeys.RIGHT;
                  else if (_keyEvent.isPrevKey()) direction = SpecialKeys.LEFT;
                  else return;
                  c = findNextFocusControl(c, direction);
               }

               if (c == null) return; // no focus candidates were found. nothing to do.

               setFocus(c);
               if (needsPaint) // commit any pending paint before returning
                  repaintActiveWindows();
               return;
            }
         }
         else
         if (_focus != highlighted && highlighted != null) // guich@tc100: without this, if an Edit is highlighted and the user press a key, the key is not sent to the control bypassing the need for the ACTION key
            highlighted.requestFocus();
      }
      if (menubar != null && (invokeMenu || (isKeyEvent && key == SpecialKeys.MENU))) // guich@200b4: popup the menu if was have a title and it was clicked
      {
         popupMenuBar();
         return;
      }

      if (!isKeyEvent && type == PenEvent.PEN_DOWN)
      {
         ptDraggingX = x;
         ptDraggingY = y;
         ptPenDownX = x;
         ptPenDownY = y;
         shiftYAtPenDownY = shiftY;
         firstDrag = true;
         
         Control c = findChild(x - this.x, y - this.y);
         if (!controlFound && Settings.fingerTouch) // guich@tc120_48
         {
            Control cn = findNearestChild(x - this.x, y - this.y, Settings.touchTolerance);
            if (cn != null)
               c = cn;
         }

         tempFocus = c;
         if (Flick.currentFlick == null && c != _focus && c.focusOnPenDown && !c.focusLess) // if flicking, do not transfer focus
            setFocus(c);
         tempFocus = c;
      }
      // guich@200b4_147: make sure that the focused control is an enabled one
      if (_focus != null && _focus != this && (!_focus.isEnabled() || _focus.parent == null)) // guich@300_55: make always sure that the focused control is enabled; added 2nd condition
      {
         if (_focus.isEnabled()) // guich@tc152: disabled controls can't send focus events
         {
            _controlEvent.type = ControlEvent.FOCUS_OUT;
            _controlEvent.target = _focus;
            _controlEvent.touch();
            _focus.postEvent(_controlEvent);
         }

         if (_focus == null || _focus.parent == null)
            _focus = this;
         else
            while (!_focus.isEnabled() && _focus.parent != null)
               _focus = _focus.parent;
      }
      if (!isKeyEvent)
      {
         Control lastMouseMove = mouseMove;
         mouseMove = type == MouseEvent.MOUSE_MOVE ? findChild(x - this.x, y - this.y) : null;
         Control target = mouseMove != null ? mouseMove : tempFocus != null ? tempFocus : _focus;
         PenEvent pe = type == PenEvent.PEN_DOWN || type == PenEvent.PEN_UP ? _penEvent : _dragEvent;
         pe.type = type;
         pe.modifiers = modifiers;
         pe.absoluteX = pe.x = x;
         pe.absoluteY = pe.y = y;
         for (Control c = target; c != null; c = c.parent) // translate x, y to coordinate system of target
         {
            pe.x -= c.x;
            pe.y -= c.y;
         }

         if (lastMouseMove != mouseMove)
         {
            if (lastMouseMove != null && lastMouseMove != mouseMove)
            {
               _mouseEvent.modifiers = modifiers;
               _mouseEvent.target = lastMouseMove;
               _mouseEvent.consumed = false;
               _mouseEvent.timeStamp = timeStamp;
               _mouseEvent.type = MouseEvent.MOUSE_OUT;
               lastMouseMove.postEvent(_mouseEvent);
            }
            if (mouseMove != null)
            {
               _mouseEvent.modifiers = modifiers;
               _mouseEvent.consumed = false;
               _mouseEvent.target = mouseMove;
               _mouseEvent.timeStamp = timeStamp;
               _mouseEvent.type = MouseEvent.MOUSE_IN;
               mouseMove.postEvent(_mouseEvent);
            }
         }

         if (type == PenEvent.PEN_UP)
         {
            if (Settings.unmovableSIP && (isScreenShifted() || isSipShown))
            {
               boolean keepShifted = tempFocus != null && tempFocus.willOpenKeyboard();
               if (!keepShifted && tempFocus != null)
               {
                  Control c = tempFocus;
                  while (c != null && !(c instanceof Scrollable))
                     c = c.parent;
                  if (c != null && c instanceof Scrollable && ((Scrollable)c).wasScrolled())
                     keepShifted = true;
               }
               if (!keepShifted)
               {
                  //pe.y -= lastShiftY; with this line, clicking in a control when the screen is shifted makes the pe.y to an invalid value
                  shiftScreen(null,0);
                  lastShiftY = 0;
                  if (isSipShown)
                  {
                     isSipShown = false;
                     setSIP(SIP_HIDE,null,false);
                     needsPaint = true;
                     
                  }
               }
            }
            
            if (!firstDrag)
            {
               DragEvent de = _dragEvent.update(pe); // PEN_DRAG_END has the same coordinates as the PEN_UP
               de.type = PenEvent.PEN_DRAG_END;

               de.consumed = false;
               de.target = target;
               de.timeStamp = timeStamp;
               target.postEvent(de);
            }

            if (tempFocus != null && _focus != tempFocus && !tempFocus.focusOnPenDown && !tempFocus.focusLess)
               setFocus(tempFocus); // set focus if it was not done on pen_down
         }
         else if (type == PenEvent.PEN_DRAG)
         {
            if (firstDrag)
            {
               firstDrag = false;
               
               DragEvent de = _dragEvent;
               de.dragId = currentDragId++; // set the drag id, which should be used until the end of the physical drag sequence
               de.type = PenEvent.PEN_DRAG_START;
               de.modifiers = modifiers;
               de.absoluteX = de.x = ptPenDownX; // PEN_DRAG_START has the same coordinates as the PEN_DOWN
               de.absoluteY = de.y = ptPenDownY + shiftY-shiftYAtPenDownY; // guich@tc138: fix the y position if the window was shifted since the last pen down
               for (Control c = target; c != null; c = c.parent) // translate x, y to coordinate system of target
               {
                  de.x -= c.x;
                  de.y -= c.y;
               }
               
               de.consumed = false;
               de.target = target;
               de.timeStamp = timeStamp;
               target.postEvent(de);
            }
            
            // Convert the PenEvent to a DragEvent
            DragEvent de = (DragEvent) (pe = _dragEvent.update(pe));
            de.xDelta = x - ptDraggingX;
            de.yDelta = y - ptDraggingY;
            de.xTotal = x - ptPenDownX;
            de.yTotal = y - ptPenDownY;
            de.direction = getDirection(ptDraggingX,ptDraggingY, x, y); // guich@tc122_11
            
            // Store coordinates to further distance computations
            ptDraggingX = x;
            ptDraggingY = y;
         }
         
         event = pe;
      }
      
      event.consumed = false; // guich@tc115_21
      event.target = (type == MouseEvent.MOUSE_MOVE && mouseMove != null) ? mouseMove : tempFocus != null ? tempFocus : _focus;
      event.timeStamp = timeStamp;
      
      if (event.type == PenEvent.PEN_UP) // guich@320_31: release tempFocus - bruno@tc126: release tempFocus BEFORE posting PEN_UP event
      {
         focusOnPenUp = _focus != null && (_focus instanceof Edit || _focus instanceof MultiEdit) ? _focus : null;
         tempFocus = null;
      }
      if (type == MouseEvent.MOUSE_WHEEL)
      {
         _mouseEvent.target = event.target;
         _mouseEvent.consumed = false;
         _mouseEvent.timeStamp = timeStamp;
         _mouseEvent.type = MouseEvent.MOUSE_WHEEL;
         _mouseEvent.wheelDirection = key;
         event = _mouseEvent;
         (_focus != null ? _focus : this).postEvent(event);
      }
      else
      if (type == MouseEvent.MOUSE_MOVE)
      {
         if (event instanceof MouseEvent)
            event.type = MouseEvent.MOUSE_MOVE;
         else
         {
            _mouseEvent.target = event.target;
            _mouseEvent.consumed = false;
            _mouseEvent.timeStamp = timeStamp;
            _mouseEvent.type = MouseEvent.MOUSE_MOVE;
            event = _mouseEvent;
         }
         mouseMove.postEvent(event);
      }
      else 
      if (event.target != null)
         ((Control)event.target).postEvent(event);
      
      if (needsPaint || Container.nextTransitionEffect != Container.TRANSITION_NONE) // guich@200b4_18: maybe the current event had poped up a Window.
         repaintActiveWindows(); // guich@tc100: paint the topMost, not ourselves.
   }

   private int getDirection(int originX, int originY, int x, int y) // guich@tc122_11
   {
      int xDelt = originX - x;
      int yDelt = originY - y;
      if (Math.abs(xDelt) > Math.abs(yDelt)) // take the largest as drag direction
         return xDelt >= 0 ? DragEvent.LEFT : DragEvent.RIGHT;
      else
         return yDelt >= 0 ? DragEvent.UP : DragEvent.DOWN;
   }

   ////////////////////////////////////////////////////////////////////////////////////
   /** Returns the client rect, ie, the rect minus the border and title area, in relative coords
    * In this version, you provide the created Rect to be filled with the coords.
    */
   protected void getClientRect(Rect r) // guich@450_36
   {
      int m = borderGaps[borderStyle];
      boolean onlyBorder = (title == null || title.length() == 0) && (borderStyle == NO_BORDER || (borderStyle == ROUND_BORDER && uiAndroid));
      r.x = m;
      r.y = titleGap + (onlyBorder ? m : m+titleFont.fm.height+1);
      switch (borderStyle)
      {
         case TAB_ONLY_BORDER: r.y++; break;
         case ROUND_BORDER: 
            r.y--;
            break;
      }
      r.width = this.width-m-m;
      r.height = this.height - r.y - m;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Returns the client rect, ie, the rect minus the border and title area, in relative coords
    */
   public Rect getClientRect() // guich@200final_15
   {
      Rect r = new Rect();
      getClientRect(r);
      return r;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Paints the title and border. */
   protected void paintTitle(String title, Graphics gg)
   {
      if (title != null || borderStyle > NO_BORDER) // guich@220_48: changed = NO_BORDER by > NO_BORDER to let MenuBar set borderStyle to -1 and thus we don't interfere with its paint
      {
         if (title == null) title = uiAndroid ? "" : " ";
         String tit = title;
         int ww = titleFont.fm.stringWidth(tit);
         if (ww > this.width-6)
         {
            int idx = Convert.getBreakPos(titleFont.fm,new StringBuffer(tit), 0, this.width-6,false);
            tit = tit.substring(0,idx);
            ww = titleFont.fm.stringWidth(tit);
         }            
         int hh = borderStyle == NO_BORDER && tit.length() == 0 ? 0 : titleFont.fm.height + (borderStyle == ROUND_BORDER?2:0);
         hh += titleGap;
         int xx = titleAlign, yy = (hh-titleFont.fm.height)/2;
         if ((CENTER-RANGE) <= titleAlign && titleAlign <= (CENTER+RANGE)) xx += (this.width - ww) / 2 - CENTER; else
         if ((LEFT  -RANGE) <= titleAlign && titleAlign <= (LEFT  +RANGE)) xx +=                       - LEFT; else
         if ((RIGHT -RANGE) <= titleAlign && titleAlign <= (RIGHT +RANGE)) xx += (this.width - ww)     - RIGHT;
         int f = getForeColor();
         int b = getBackColor();
         gg.foreColor = gg.backColor = f;
         if (borderStyle != NO_BORDER)
         {
            int y0 = borderStyle == RECT_BORDER?0:hh;
            if (borderStyle != TAB_ONLY_BORDER && borderStyle != ROUND_BORDER && borderStyle != HORIZONTAL_GRADIENT && borderStyle != VERTICAL_GRADIENT)
               gg.drawRect(0, y0, this.width, this.height - y0);
            switch (borderStyle)
            {
               case HORIZONTAL_GRADIENT:
               case VERTICAL_GRADIENT:
                  gg.fillShadedRect(0, 0, width,hh, true, borderStyle == HORIZONTAL_GRADIENT, gradientTitleStartColor == -1 ? f : gradientTitleStartColor, gradientTitleEndColor == -1 ? b : gradientTitleEndColor,100);
                  break;
               case TAB_BORDER:
               case TAB_ONLY_BORDER:
                  gg.foreColor = f;
                  gg.drawLine(1, 0, ww + 2, 0); // Draws the tab
                  gg.fillRect(0, 1, ww + 4, hh);
                  gg.fillRect(0, hh, width, 2); // Draws the line
                  xx = 3;
                  break;
               case ROUND_BORDER:
                  if (uiAndroid)
                  {
                     boolean hasTitle = tit != null && tit.length() > 0;
                     int c = Color.getCursorColor(f);
                     gg.drawWindowBorder(0,0,width,height,hasTitle?hh:0,footerH,borderColor != -1 ? borderColor : f,hasTitle? headerColor != -1 ? headerColor : c:b,b,footerH > 0 ? footerColor != -1 ? footerColor : c : b,borderGaps[ROUND_BORDER],hasTitle || footerH > 0);
                     if (!hasTitle)
                        return;
                     else
                        break;
                  }
                  // guich@121 - uses the new round rect methods
                  gg.fillRoundRect(0, 0, width, height, 3);
                  gg.backColor = b;
                  gg.fillRect(2, hh, width - 4, height - hh - 2);
                  gg.setPixel(2, height - 3);
                  gg.setPixel(width - 3, height - 3);
                  break;
               case RECT_BORDER:
               default:
                  gg.fillRect(0, 0, this.width, hh + 2); // black border, white text
                  break;
            }
            gg.foreColor = titleColor == -1 ? headerColor == b ? f : b : titleColor; // draws the text with inversed color
            gg.backColor = f;
         }
         else // guich@402_64: fixed colors when NO_BORDER
         {
            gg.foreColor = titleColor == -1 ? f : titleColor;
            gg.backColor = b;
         }
         gg.setFont(titleFont);
         gg.drawText(tit, xx, yy, textShadowColor != -1, textShadowColor);
         gg.setFont(font);
         if (rTitle == null)
            rTitle = new Rect(xx-2,0,ww+4,hh==0 && tit.length() > 0 ? titleFont.fm.height : hh+1); // guich@200b4_52
      }
   }
   ////////////////////////////////////////////////////////////////////////////////////
   public void paintWindowBackground(Graphics gg)
   {
      gg.backColor = backColor; // disabled here?
      if (!transparentBackground && (borderStyle != ROUND_BORDER || this instanceof MainWindow)) // guich@552_18: do not fill if round border - guich@tc122_54: not if transparent background - guich@tc130: if its a MainWindow, fill the whole background
         gg.fillRect(0, 0, width, height); // guich@110
      // guich@102: if border or title, draw it
      paintTitle(title, gg);
      onPaint(gg);
   }
   /**
    * Called by the VM to repaint an area.
    */
   public void _doPaint()
   {
      Graphics gg = getGraphics();
      if (offscreen != null)
         gg.drawImage(offscreen,0,0);
      else
      {
         // clear background
         paintWindowBackground(gg);
         paintChildren();
      }
      if (offscreen == null && Settings.onJavaSE)
         safeUpdateScreen();
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Popup a modal window, and make it child of this one. All events in the behind window are deactivated.
    */
   private void popupNonBlocking(Window newWin)
   {
      blocking = false;
      if (this != topMost) // kambiz@320_18: is the user calling in the wrong way?
         topMost.popupNonBlocking(newWin);
      else
      {
         enableUpdateScreen = false;
         if (isScreenShifted())
            shiftScreen(null,0);
         setSIP(SIP_HIDE,null,false);
         if (newWin.transitionEffect != TRANSITION_NONE)
            setNextTransitionEffect(newWin.transitionEffect);
         if (newWin.lastScreenWidth != Settings.screenWidth) // was the screen rotated since the last time this window was popped?
            newWin.reposition();
         newWin.popped = true;
         focusOnPopup = _focus;
         if (focusOnPopup instanceof totalcross.ui.MenuBarDropDown || focusOnPopup == null || _controlEvent.type == ControlEvent.FOCUS_OUT) // guich@200b4_166: case 2 consecutive clicks on the MenuBar; in the 1st, it is the control; in the 2nd, it is the MenuBar - guich@566_19: if the focus is going out of a control, don't put the focus back to it again after an unpop
            focusOnPopup = this;

         newWin.onPopup();
         if (newWin.tempTitle != null)
         {
            newWin.oldTitle = newWin.title;
            newWin.setTitle(newWin.tempTitle);
         }
         eventsEnabled = false; // disables this window
         zStack.push(topMost = newWin);
         setFocus(topMost); // guich@567_4: changed from setFocus to swapFocus to fix 566_18 problem - guich@568_17: changed back to setFocus
         topMost.eventsEnabled = true; // enable the new window
         topMost.postPopup();
         if (newWin.offscreen == null)
         {
            enableUpdateScreen = true;
            if (newWin.transitionEffect != TRANSITION_NONE)
               applyTransitionEffect();
            else
               repaintActiveWindows();
         }
      }
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Popup a modal window, blocking the program execution, and make it child of this one. All events in the behind window are deactivated.
       Important! You can't use this method in the application's constructor or in the initUI method!
       Calling this 
   */
   private void popup(Window newWin) // anodos@320_9
   {
      if (!started && this instanceof MainWindow) // guich@567_17: if the user call this method in the initUI method, repaintNow so that the back of the screen is stored correctly
      {
         started = true; // don't let this repaintNow be called again if more than one popup is called in sequence in the initUI of the MainWindow
         //repaintNow(); - guich@tc210 - prevent an empty white background on startup.
      }
      if (!enableUpdateScreen) // guich@tc114_57: if we need interaction, make sure that the screen was updated.
      {
         enableUpdateScreen = true;
         repaintActiveWindows();
      }
      popupNonBlocking(newWin);
      blocking = true;
      if (!MainWindow.quittingApp)
      do
      {
         pumpEvents();
      } while (newWin.popped);
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Hides this window. Calling unpop when only the MainWindow is active does nothing. */
   public void unpop()
   {
      if (zStack.size() == 1) // guich@400_69
         return;
      if (oldTitle != null)
      {
         setTitle(oldTitle);
         oldTitle = null;
      }
      Window lastTopMost = topMost;
      int nextTrans = lastTopMost.transitionEffect == TRANSITION_FADE ? TRANSITION_FADE : lastTopMost.transitionEffect == TRANSITION_CLOSE ? TRANSITION_OPEN : lastTopMost.transitionEffect == TRANSITION_OPEN ? TRANSITION_CLOSE : TRANSITION_NONE;
      setNextTransitionEffect(nextTrans);
      onUnpop();
      eventsEnabled = false;
      MainWindow.mainWindowInstance.removeTimers(this);
      try
      {
         zStack.pop();
         topMost = (Window)zStack.peek();
      } catch (ElementNotFoundException e) {topMost = null;}
      if (topMost != null)
      {
         topMost.eventsEnabled = true;
         if (topMost.focusOnPopup instanceof totalcross.ui.MenuBar)
            topMost.focusOnPopup = topMost; // make sure that the focus is not on the closed menu bar
         else
         if (topMost.focusOnPopup instanceof totalcross.ui.Window && !topMost.focusOnPopup.asWindow.isVisible()) // guich@200b4_207: fixed a bug when popup menu->select item->popup instance of Window class->popup MessageBox when win closed->select menu->events were wrongly dispatched to the focusOnPopup whom was the win previously popped up and nothing happens
            topMost.focusOnPopup = topMost;
         else
         if (!topMost.focusOnPopup.isDisplayed()) // guich@300_61: if we popped up a MessageBox and swapped the container...
            topMost.focusOnPopup = topMost;
         else
         if (!topMost.focusOnPopup.isEnabled()) // guich@300_62: if the button that dispatched the event is not more enabled...
            topMost.focusOnPopup = topMost;
         lastTopMost = topMost;
         topMost.focusOnPopup.postEvent(new ControlEvent(ControlEvent.WINDOW_CLOSED, this)); // tell last control that we closed
         if (topMost == lastTopMost) // guich@240_23: if the postEvent before pops up another Window, we must not set the focus back because the topMost var has changed
            topMost.setFocus(topMost.focusOnPopup);
         postUnpop();
         popped = false;
         needsPaint = true;
         if (transitionEffect != TRANSITION_NONE)
            applyTransitionEffect();
         else
            repaintActiveWindows();
      }
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Let the VM handle all the events in the event queue.
     * This method is used to implement a blocking Window.
     * Here is an example: <pre>
     * while(someCondition)
     * {
     *    Event.pumpEvents();
     * }
     * </pre>
     */
   public static void pumpEvents()
   {
      totalcross.Launcher.instance.pumpEvents();
   }

   native public static void pumpEvents4D();
   ////////////////////////////////////////////////////////////////////////////////////
   /** Returns true if this window is the top-level window */
   public boolean isTopMost()
   {
      return this == topMost;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Returns the topmost window */
   static public Window getTopMost()
   {
      return topMost;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Placeholder called imediatly before the popup began. The default implementation does nothing. */
   protected void onPopup()
   {
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Placeholder called after the popup is done and after the repaint of this window. The default implementation does nothing. */
   protected void postPopup()
   {
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Placeholder called imediatly before the unpop began. The default implementation does nothing. */
   protected void onUnpop()
   {
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Placeholder called after the unpop is done and after the repaint of the other window. The default implementation does nothing. */
   protected void postUnpop()
   {
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** True if this is the topmost window */
   public boolean isVisible()
   {
      return this == topMost || parent != null;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Used to set the position of the Soft Input Panel on devices that support it, like the Windows CE.
    * @param sipOption One of the SIP_xxx values.
    * @param edit control (Edit or MultiEdit) (iPhone only)
    * @param secret enable password entry (iPhone only)
    * @see #SIP_HIDE
    * @see #SIP_SHOW
    * @see #SIP_TOP
    * @see #SIP_BOTTOM
    * @see #SIP_ENABLE_NUMERICPAD
    * @see #SIP_DISABLE_NUMERICPAD
    * @see Edit#mapKeys
    */
   final public static void setSIP(int sipOption, Control edit, boolean secret)
   {
      Launcher.instance.setSIP(sipOption, edit, secret);
   }
   native public static void setSIP4D(int sipOption, Control edit, boolean secret);
   ////////////////////////////////////////////////////////////////////////////////////
   /** Sets the menu bar for this window */
   public void setMenuBar(Control menubar)
   {
      this.menubar = menubar;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Open the menu bar that is associated with this window */
   public void popupMenuBar() // guich@200b4_41
   {
      if (menubar != null)
         menubar.setVisible(true);
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** This method repaints all windows from bottom to top. It is automaticaly used
    * in the unpop method.
    * @since SuperWaba 2.0 beta 4
    */
   protected void loadBehind()
   {
      repaintActiveWindows();
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** This method is for internal use only. calling it directly will cause the VM to crash. */
   public static void destroyZStack()
   {
      zStack.removeAllElements();
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Used to swap containers from the Window. To restore the first
     * container, pass null as parameter. Note that null cannot be used for the first swap.
     * See samples/ui/ContainerSwitch.
     * This method correctly handles screen rotation for containers that were already swapped.
     */
   public void swap(Container newContainer) // guich@300_57: make easier to swap containers
   {
      // store the first container added as the main one
      if (mainSwapContainer == null)
         mainSwapContainer = newContainer;
      else
         isHighlighting = Settings.keyboardFocusTraversable; // guich@573_17
      // add the new container.
      if (newContainer != null && newContainer.transitionEffect != TRANSITION_NONE)
         setNextTransitionEffect(newContainer.transitionEffect);
      else
      if (lastSwappedContainer != null && lastSwappedContainer.transitionEffect != TRANSITION_NONE)
         setNextTransitionEffect(lastSwappedContainer.transitionEffect == TRANSITION_FADE ? TRANSITION_FADE : lastSwappedContainer.transitionEffect == TRANSITION_OPEN ? TRANSITION_CLOSE : TRANSITION_OPEN);
      // remove the last container
      if (lastSwappedContainer != null)
         remove(lastSwappedContainer);
      // returning back to the main one?
      if (newContainer == null)
         newContainer = mainSwapContainer;
      lastSwappedContainer = newContainer;
      add(newContainer);
      if (!newContainer.started) // guich@340_15: if the container did not start yet, set its size
         newContainer.setRect(LEFT,TOP,FILL,FILL);
      else
      if (newContainer.lastScreenWidth != Settings.screenWidth) // was the screen rotated since the last time this container was added?
         newContainer.reposition();
      Control firstTarget = (_focus != null && _focus.getParentWindow() == this) ? _focus : newContainer.tabOrder.size() > 0 ? (Control)newContainer.tabOrder.items[0] : newContainer; // guich@573_19: set focus to the first control, instead of the new container. - guich@tc100: only if the focus was not already set in the initUI method of the newContainer
      applyTransitionEffect();
      if (Toast.btn != null)
         try {Toast.btn.bringToFront();} catch (Exception e) {}
      newContainer.repaintNow(); // guich@503_7: fixed problem when this swap was being called from inside a Menu.
      firstTarget.requestFocus(); // guich@tc153: put this after repaintNow to fix transition effect problems
      topMost.focusOnPopup = firstTarget; // guich@550_15: otherwise, the ContainerSwitch app won't work for Sub3 when using pen less.
      if (Settings.keyboardFocusTraversable || Settings.geographicalFocus) highlighted = firstTarget;
      newContainer.onSwapFinished();
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Returns the size of the title if any plus the size of the border.
     * Note that the value returned here does not handle the controls inside the
     * window.
     */
   public int getPreferredWidth()
   {
      int wtitle = title == null ? 0 : titleFont.fm.stringWidth(title);
      int wborder = (borderStyle == NO_BORDER) ? 0 : (borderStyle == ROUND_BORDER?4:2);
      return wtitle + wborder + insets.left+insets.right;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Returns the size of the title if any plus the size of the border.
     * Note that the value returned here does not handle the controls inside the
     * window.
     */
   public int getPreferredHeight()
   {
      int htitle = title == null ? 0 : titleFont.fm.height;
      int hborder = (borderStyle == NO_BORDER) ? 0 : (borderStyle == ROUND_BORDER?4:2);
      return htitle + hborder + insets.top+insets.bottom;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Repaints the window stack from 0 to zStack.size().
     */
   public static void repaintActiveWindows()
   {
      int i,j,n;
      boolean eas = enableUpdateScreen;
      enableUpdateScreen = false;
      boolean neededPaint = needsPaint;
      needsPaint = false; // prevent from updating the screen
      // guich@400_73 guich@400_76
      boolean callUS = true;
      try
      {
         Object[] items = zStack.items;
         Rect mainWindowRect = MainWindow.mainWindowInstance.getRect(); // size of the MainWindow
         for (i=zStack.size(); --i > 0;) // search for the the most top window with the same size of MainWindow - 0=mainwindow, so we skip it
            if (((Window)items[i]).getRect().equals(mainWindowRect))
               break;
         // guich@tc120_43: find the last fadeOtherWindows
         int lastFade = 1000;
         for (j = 0,n=zStack.size(); j < n; j++)
            if (((Window)items[j]).fadeOtherWindows)
               lastFade = j;
         if (i == -1) i = 0;
         for (n=zStack.size(); i < n; i++) // repaints every window, from the nearest with the MainWindow size to last parent
         {
            if (i == lastFade)
               Graphics.fadeScreen(fadeValue);
            if (items[i] != null) ((Window)items[i])._doPaint();
         }
         if (neededPaint)
         {
            topMost.onWindowPaintFinished();
            if (topMost._focus != null && topMost._focus.getParentWindow() == topMost)
               topMost._focus.onWindowPaintFinished(); // guich@200b4: test if the last focused control belongs to this window; this corrects the painted control after a window is poped up
            topMost.lastHighlighted = null;
            if (topMost.highlighted != null) // fdie@570_120 repaint with clipping an xor drawn highlighted control   kmeehl@tc100: only draw the highlight on the topmost window
               topMost.drawHighlight(topMost.highlighted);
            safeUpdateScreen(); // tc100
         }
      }
      catch (Exception e) {e.printStackTrace(); callUS = false;}
      
      // guich@tc125_18: there's no need to paint the highlight here because it was already painted in the repaintNow() method called above.
      
      enableUpdateScreen = eas;
      if (callUS)
         safeUpdateScreen();
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Called by the main event handler to handle the focus change keys. Only
    * called when Settings.keyboardFocusNavigation is true.
    * @since SuperWaba 5.5
    */
   protected boolean handleFocusChangeKeys(KeyEvent ke) // guich@512_1: transfer focus on tab keys - fdie@550_15 : transfer also on arrow keys
   {
      if (isHighlighting)
      {
         boolean isForward = ke.isNextKey();
         boolean isAction = ke.isActionKey();
         if (isAction && highlighted != null)
         {
            highlighted.requestFocus();
            isHighlighting = false; // kmeehl@tc100: set isHighlighting after requesting focus so that the focus_out will not set it back
            _keyEvent.type = KeyEvent.ACTION_KEY_PRESS;
            _keyEvent.target = _focus;
            _keyEvent.touch();
            _focus._onEvent(_keyEvent);
            _keyEvent.type = KeyEvent.KEY_PRESS; // Restore the default
         }
         else
         {
            Control c = getHighlighted();
            Container p;
            if (c == null || c == this) // kmeehl@tc100: prevent nullpointer when c is a window with no controls
               c = p = this;
            else
               p = (c.asContainer != null && c.asContainer.tabOrder.size() > 0) ? c.asContainer : c.parent;
            if (p == null) // guich@tc113_22
               c = p = this;
            if (highlighted == null && firstFocus != null && !firstFocus.focusLess && firstFocus.focusTraversable && firstFocus.visible && firstFocus.isEnabled()) // kmeehl@tc100: if firstFocus is set and focusable, use it as the first control to get focus
               setHighlighted(firstFocus);
            else
               c.changeHighlighted(p,isForward);
         }
         return true;
      }
      return false;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Sets the currently highlighted control which will gain focus if the ACTION button
    * is pressed.
    * It may be the same of the one that holds focus.
    * @since SuperWaba 5.5
    */
   public void setHighlighted(Control c)
   {
      if (Settings.keyboardFocusTraversable && c != highlighted) // kmeehl@tc100: removed check for children. focusTraversable is now a reliable way to tell if a control should be focusable and highlightable; check if c is null
      {
         if (highlighted != null)
         {
            _controlEvent.type = ControlEvent.HIGHLIGHT_OUT;
            _controlEvent.update(highlighted);
            highlighted.postEvent(_controlEvent); // kmeehl@tc100: send the currently highlighted control a HIGHLIGHT_OUT event
            highlighted = null;
         }
         if (c != null && c.focusTraversable)
         {
            _controlEvent.type = ControlEvent.HIGHLIGHT_IN;
            _controlEvent.update(c);
            c.postEvent(_controlEvent); // kmeehl@tc100: send the currently highlighted control a HIGHLIGHT_IN event
            highlighted = c;
         }
         if (Settings.isOpenGL)
            needsPaint = true;
         else
         {
            drawHighlight(highlighted);
            safeUpdateScreen();
         }
      }
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Gets the currently highlighted control.
    * @since SuperWaba 5.5
    */
   public Control getHighlighted()
   {
      return highlighted;
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Avoid drawing the highlight on a Window */
   public void drawHighlight()
   {
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Pops up this window, but the program execution continues right after, before the window is dismissed.
    * @since SuperWaba 5.5
    */
   public void popupNonBlocking()
   {
      topMost.popupNonBlocking(this);
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Pops up this window, blocking the execution until the window closes.
    * @since SuperWaba 5.5
    */
   public void popup()
   {
      topMost.popup(this);
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Posts a ControlEvent.PRESSED event on the focused control.
    * @since SuperWaba 5.8 */
   public void postPressedEvent() // guich@580_27
   {
      Control c = topMost.getFocus();
      if (c == null)
         c = topMost;
      _controlEvent.type = ControlEvent.PRESSED;
      _controlEvent.target = this;
      _controlEvent.touch();
      c.postEvent(_controlEvent);
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /** Called when the screen is resized, probably caused by a rotation.
    * ATTENTION: THIS CALL CANNOT BE BLOCKED OR THE SYSTEM WILL LOCK!
    */
   public void screenResized()
   {
      enableUpdateScreen = false; requestFocus(); enableUpdateScreen = true; // if resize occured in an edit, remove the focus from it.
      rTitle = null; // guich@tc120_37
      reposition();
      MainWindow.getMainWindow().runOnMainThread(new Runnable()
      {
         public void run()
         {
            repaintActiveWindows();
         }
      });
   }
   ////////////////////////////////////////////////////////////////////////////////////
   /**
    *  Walks up from control to each parent giving each focus handler an opportunity to handle the KeyEvent.
    *  The Control passed in is given a chance to handle the focus change key regardless of its status as a focus handler.
    * @return The next control which should get focus, or null.
    */
   private Control bubbleHandleFocusChangeKey(Control c, KeyEvent ke) // kmeehl@tc100
   {
	   Control tempFocus = null;
	   while (c != null)
	   {
		   if ((tempFocus = c.handleGeographicalFocusChangeKeys(ke)) != null)
			   break;
		   do
		   {
			   c = c.getParent();
		   } while(c != null && !c.focusHandler);
	   }
	   return tempFocus;
   }
   ///////////////////////////////////////////////////////////////////////////////////
   void drawHighlight(Control c)
   {
      if (lastHighlighted == c)
         return;
      else 
      if (lastHighlighted != null)
      {
         drawHighlight(lastHighlighted, false);
         lastHighlighted = null;
      }
      
      if (c != null && c.visible && c.isDisplayed() && Settings.keyboardFocusTraversable && c.asWindow == null) // guich@tc112_34: don't draw highlight of a Window (as in MenuBarDropDown)
      {
         drawHighlight(c, true);
         lastHighlighted = c;
      }
   }
   private void drawHighlight(Control c, boolean highlighted)
   {
      int n = UIColors.highlightColors.length;
      Graphics g = c.refreshGraphics(c.gfx, n, null,0,0);
      if (g != null)
      {
         int offset = 0;
         int x = -n - 1, y = -n - 1, w = c.width + n + n + 2, h = c.height + n + n + 2;
         int[] buf = behindHighlightRect;
         
         if (highlighted)
         {
            int count = (w + w + h + h) * n;
            if (buf == null || buf.length < count)
               buf = behindHighlightRect = new int[count];
            
            int old = g.foreColor; // since the graphics is now shared among all controls, save and restore the fore color
            while (n-- > 0)
            {
               // bruno@tc112_34: save the content of what we're going to overwrite
               offset += g.getRGB(buf, offset, x, y, w, 1);
               offset += g.getRGB(buf, offset, x, y + h - 1, w, 1);
               offset += g.getRGB(buf, offset, x, y, 1, h);
               offset += g.getRGB(buf, offset, x + w - 1, y, 1, h);
               
               g.foreColor = UIColors.highlightColors[n];
               g.drawRect(x, y, w, h);
               
               x++;
               y++;
               w -= 2;
               h -= 2;
            }
            g.foreColor = old;
         }
         else
         {
            while (n-- > 0)
            {
               // bruno@tc112_34: restore the content of what overwrote
               offset += g.setRGB(buf, offset, x, y, w, 1);
               offset += g.setRGB(buf, offset, x, y + h - 1, w, 1);
               offset += g.setRGB(buf, offset, x, y, 1, h);
               offset += g.setRGB(buf, offset, x + w - 1, y, 1, h);
               
               x++;
               y++;
               w -= 2;
               h -= 2;
            }
         }
      }
   }

   /** This method resizes the Window to the needed bounds, based on added childs.
    * It changes only the size, keeping the x,y coordinates passed on setRect.
    * You can add spaces at right and bottom using the insets.right/bottom properties.
    * Sample:
    * <pre>
    * public class TestWindow extends Window
    * {
    *    public TestWindow()
    *    {
    *       ... call constructor
    *       setRect(CENTER,CENTER,Screen.width-40,1000); // height will be resized later
    *    }
    *    public void initUI()
    *    {
    *       setInsets(5,5,5,5);
    *       ... add controls
    *       resize();
    *    }   
    * }
    * </pre>
    * In this sample, since the height is variable, you can't use BOTTOM on <code>y</code> nor FILL on <code>height</code>,
    * otherwise resize will not work as expected. Same counts if you have a variable width.
    * @since TotalCross 1.14
    */
   public void resize() // guich@tc114_53
   {
      int maxX = 0;
      int maxY = 0;
      for (Control child = children; child != null; child = child.next)
      {
         maxX = Math.max(maxX,child.x+child.width);
         maxY = Math.max(maxY,child.y+child.height);
      }      
      int hborder = (borderStyle == NO_BORDER) ? 0 : (borderStyle == ROUND_BORDER?4:2);
      setRect(setX,setY,maxX+insets.right+hborder/2, maxY+insets.bottom+hborder/2);
   }
   
   public void resizeWidth()
   {
      int maxX = 0;
      for (Control child = children; child != null; child = child.next)
         maxX = Math.max(maxX,child.x+child.width);
      int hborder = (borderStyle == NO_BORDER) ? 0 : (borderStyle == ROUND_BORDER?4:2);
      setRect(setX,setY,maxX+insets.right+hborder/2, KEEP);
   }

   public void resizeHeight()
   {
      int maxY = 0;
      for (Control child = children; child != null; child = child.next)
         maxY = Math.max(maxY,child.y+child.height);
      int hborder = (borderStyle == NO_BORDER) ? 0 : (borderStyle == ROUND_BORDER?4:2);
      setRect(setX,setY,KEEP, maxY+insets.bottom+hborder/2);
   }
   
   public static int getDefaultDragThreshold()
   {
      double threshold = (Settings.fingerTouch ? DEFAULT_DRAG_THRESHOLD_IN_INCHES_FINGER : DEFAULT_DRAG_THRESHOLD_IN_INCHES_PEN) * (Settings.screenWidthInDPI + Settings.screenHeightInDPI) / 2;
      return (int)Math.round(threshold);
   }
   
   /** Returns the number of windows that are popped up. If there's only a MainWindow, returns 0.
    * @since TotalCross 1.27
    */
   public static int getPopupCount() // guich@tc126_68
   {
      return zStack.size()-1;
   }
   
   /** Called when a robot key is pressed.
    * Don't call this method directly, use Settings.deviceRobotSpecialKey instead.
    * @see Settings#deviceRobotSpecialKey
    */
   public static void onRobotKey()
   {
      new Thread()
      {
         public void run()
         {
            try 
            {
               if (UIRobot.status == UIRobot.IDLE)
                  robot = new UIRobot(); 
               else
               if (robot != null)
                  robot.stop();
            } catch (Exception e) {}
         }
      }.start();         
   }
   // guich@tc130: shift the screen if SIP can't be moved.
   
   public void shiftScreen(Control c, int deltaY)
   {
      if (c == null) // unshift the screen?
      {
         boolean force = this != topMost;
         shiftY = shiftH = 0;
         boolean wasPenEvent = PenEvent.PEN_DOWN <= lastType && lastType <= PenEvent.PEN_DRAG;
         if (force || !wasPenEvent)
            lastShiftY = 0;
         if (force && isSipShown) // guich@tc126_58: always try to close the sip
         {
            isSipShown = false;
            setSIP(SIP_HIDE,null,false);
         }
         repaintActiveWindows();
      }
      else
      {
         Rect r = c.getAbsoluteRect();

         int newShiftY = Math.max(r.y + deltaY - c.fmH, 0);
         if (newShiftY != shiftY)
         {
            lastShiftY = shiftY = newShiftY;
            shiftH = Settings.onJavaSE ? Settings.screenHeight/2 : (1+1+2)*c.fmH; // one line above and two below control, plus control's line
         }
         needsPaint = true;
      }
   }
   
   public static boolean isScreenShifted()
   {
      return shiftY != 0;
   }
}
