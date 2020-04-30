// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000-2012 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import java.util.ArrayList;
import java.util.List;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.effect.UIEffects;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.EnabledStateChangeEvent;
import totalcross.ui.event.EnabledStateChangeListener;
import totalcross.ui.event.Event;
import totalcross.ui.event.EventHandler;
import totalcross.ui.event.FocusListener;
import totalcross.ui.event.FontChangeEvent;
import totalcross.ui.event.FontChangeHandler;
import totalcross.ui.event.GridEvent;
import totalcross.ui.event.GridListener;
import totalcross.ui.event.HighlightListener;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.KeyListener;
import totalcross.ui.event.ListContainerEvent;
import totalcross.ui.event.ListContainerListener;
import totalcross.ui.event.Listener;
import totalcross.ui.event.MouseEvent;
import totalcross.ui.event.MouseListener;
import totalcross.ui.event.MultiTouchEvent;
import totalcross.ui.event.MultiTouchListener;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.event.PressListener;
import totalcross.ui.event.PushNotificationEvent;
import totalcross.ui.event.PushNotificationListener;
import totalcross.ui.event.SizeChangeEvent;
import totalcross.ui.event.SizeChangeHandler;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;
import totalcross.ui.event.ValueChangeEvent;
import totalcross.ui.event.ValueChangeHandler;
import totalcross.ui.event.WindowListener;
import totalcross.ui.font.Font;
import totalcross.ui.font.FontMetrics;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Coord;
import totalcross.ui.gfx.GfxSurface;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.util.Vector;

/**
 * Control is the base class for user-interface objects.
 */

public class Control extends GfxSurface {
  /** Used when this Control is translucent */
  public static enum TranslucentShape {
    NONE, RECT, ROUND, LESS_ROUND, CIRCLE
  };

  /** The type of surface. */
  int surfaceType; // don't move from here! must be at position 0
  /** The control's x location */
  protected int x; // guich@200: VERY IMPORTANT: DONT CHANGE THE LOCATION OF THIS VARIABLE! it must be the # 1
  /** The control's y location */
  protected int y; // guich@200: VERY IMPORTANT: DONT CHANGE THE LOCATION OF THIS VARIABLE! it must be the # 2
  /** The control's width */
  protected int width; // guich@200: VERY IMPORTANT: DONT CHANGE THE LOCATION OF THIS VARIABLE! it must be the # 3
  /** The control's height */
  protected int height; // guich@200: VERY IMPORTANT: DONT CHANGE THE LOCATION OF THIS VARIABLE! it must be the # 4
  /** The parent of the control. */
  protected Container parent;
  /** The control's next sibling. */
  protected Control next;
  /** The control's previous sibling. */
  protected Control prev;
  /** True if the control is enabled (accepts events) or false if not */
  private boolean enabled = true;
  /** The font used by the control. */
  protected Font font;
  /** The fontMetrics corresponding to the controls font. */
  public FontMetrics fm;
  /** True if the control is visible, false otherwise */
  protected boolean visible = true;
  /** Foreground color of this control. When the control is added, its foreground is set to be the same of the parent's. */
  protected int foreColor = -1;
  /** Background color of this control. When the control is added, its background is set to be the same of the parent's. */
  protected int backColor = -1;
  /** Application defined constant. This constant is not used; its only a placeholder for the application so it can set to any value it wants */
  public int appId;
  /** Application defined object. This field is not used; its only a placeholder for the application so it can set to any value it wants */
  public Object appObj; // guich@580_38
  /** Default value when calling clear. When the control will use a numeric value or a String, depends on the type of control. Defaults to an empty string. */
  public String clearValueStr = ""; // guich@572_19
  /** Default value when calling clear. When the control will use a numeric value or a String, depends on the type of control. Defaults to zero. */
  public int clearValueInt; // guich@572_19
  /** The next control that will receive focus when tab is hit. */
  public Control nextTabControl;
  public static final int RANGE = 10000000;
  public static final int UICONST = RANGE * 2 + 1000000;
  /** Constant used in params width and height in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: PREFERRED+2 or PREFERRED-1. */
  public static final int PREFERRED = 1 * UICONST;
  /** Constant used in param x in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: LEFT+2 or LEFT-1. */
  public static final int LEFT = 2 * UICONST;
  /** Constant used in params x and y in setRect.  You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: CENTER+2 or CENTER-1. */
  public static final int CENTER = 3 * UICONST;
  /** Constant used in param x in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: RIGHT+2 or RIGHT-1. */
  public static final int RIGHT = 4 * UICONST;
  /** Constant used in param y in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: TOP+2 or TOP-1. */
  public static final int TOP = 5 * UICONST;
  /** Constant used in param y in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: BOTTOM+2 or BOTTOM-1. */
  public static final int BOTTOM = 6 * UICONST;
  /** Constant used in params width and height in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: FILL+2 or FILL-1. Note that FILL cannot be used with other x/y positioning constants. */
  public static final int FILL = 7 * UICONST;
  /** Constant used in param x/y in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: BEFORE+2 or BEFORE-1. */
  public static final int BEFORE = 8 * UICONST; // guich@200b4_100
  /** Constant used in params x/y/width/height in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: SAME+2 or SAME-1. */
  public static final int SAME = 9 * UICONST; // guich@200b4_100
  /** Constant used in param x/y in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: AFTER+2 or AFTER-1. */
  public static final int AFTER = 10 * UICONST; // guich@200b4_100
  /** Constant used in params width and height in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: FIT+2 or FIT-1. Note that FIT cannot be used with other x/y positioning constants. FIT will make the control fit between the desired position and the last added control position. */
  public static final int FIT = 11 * UICONST; // guich@330_4
  /** Constant used in param x/y in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: CENTER_OF+2 or CENTER_OF-1. CENTER_OF is related to a control, while CENTER is related to the screen. CENTER_OF cannot be used with FILL/FIT in the widths. */
  public static final int CENTER_OF = 12 * UICONST; // guich@tc110_88
  /** Constant used in param x/y in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: RIGHT_OF+2 or RIGHT_OF-1. RIGHT_OF is related to a control, while RIGHT is related to the screen. RIGHT_OF cannot be used with FILL/FIT in the widths. */
  public static final int RIGHT_OF = 13 * UICONST; // guich@tc110_97
  /** Constant used in param x/y in setRect. You can use this constant added to a number to specify a increment/decrement to the calculated size. EG: BOTTOM_OF+2 or BOTTOM_OF-1. BOTTOM_OF is related to a control, while BOTTOM is related to the screen. BOTTOM_OF cannot be used with FILL/FIT in the widths. */
  public static final int BOTTOM_OF = 14 * UICONST; // guich@tc110_97
  /** Constant used in param width (will use screen's width) and height (will use screen's height) in setRect. 
   * You can use this constant added or subtracted to a number to specify a increment to the calculated size.
   * There are two ways to use it:<br>
   * 1. SCREENSIZE + constant: it will use as a PERCENTAGE of the screen's size. For example, SCREENSIZE+20 in width will result in 20% of screen's size.<br>
   * 2. SCREENSIZE - constant: it will use as a FRACTION of the screen's size. For example, SCREENSIZE-4 in width will result in 1/4 of screen's size.<br>
   * 
   * If there are no constant number, size will be 100% of the screen's width/height.
   * @since TotalCross 1.3 
   */
  public static final int SCREENSIZE = 15 * UICONST;
  /** Constant used in params x/y/width/height in setRect. It keeps the current value. Note that it does NOT support increment nor decrement.
   * KEEP differs from SAME in the manner that KEEP applies to the coordinates of this control, while SAME applies
   * to the coordinates of another control.
   * @since TotalCross 1.14
   */
  public static final int KEEP = 16 * UICONST; // guich@tc114_68
  /** Constant used in param width (will use parent's width) and height (will use parent's height) in setRect.
   * It can also be used in X or Y, representing the position where to draw it. 
   * You can use this constant added or subtracted to a number to specify a increment to the calculated size.
   * There are two ways to use it:<br>
   * 1. PARENTSIZE + constant: it will use as a PERCENTAGE of the parent's size. For example, PARENTSIZE+20 in width will result in 20% of parent's size.<br>
   * 2. PARENTSIZE - constant: it will use as a FRACTION of the parent's size. For example, PARENTSIZE-4 in width will result in 1/4 of parent's size.<br>
   * 
   * If there are no constant number, size will be 100% of the parent's width/height.
   * 
   * If the parent is unknown, the screen size will be used instead.
   * @since TotalCross 1.52
   */
  public static final int PARENTSIZE = 17 * UICONST;
  /** Constant used in param width or height (will use screen's minimum size between width and height) in setRect. 
   * You can use this constant added or subtracted to a number to specify a increment to the calculated size.
   * There are two ways to use it:<br>
   * 1. SCREENSIZEMIN + constant: it will use as a PERCENTAGE of the screen's size. For example, SCREENSIZEMIN+20 in width will result in 20% of screen's minimum size.<br>
   * 2. SCREENSIZEMIN - constant: it will use as a FRACTION of the screen's size. For example, SCREENSIZEMIN-4 in width will result in 1/4 of screen's minimum size.<br>
   * 
   * If there are no constant number, size will be 100% of the screen's width/height.
   * @since TotalCross 2.0
   */
  public static final int SCREENSIZEMIN = 18 * UICONST;
  /** Constant used in param width or height (will use screen's maximum size between width and height) in setRect. 
   * You can use this constant added or subtracted to a number to specify a increment to the calculated size.
   * There are two ways to use it:<br>
   * 1. SCREENSIZEMAX + constant: it will use as a PERCENTAGE of the screen's size. For example, SCREENSIZEMAX+20 in width will result in 20% of screen's maximum size.<br>
   * 2. SCREENSIZEMAX - constant: it will use as a FRACTION of the screen's size. For example, SCREENSIZEMAX-4 in width will result in 1/4 of screen's maximum size.<br>
   * 
   * If there are no constant number, size will be 100% of the screen's width/height.
   * @since TotalCross 2.0 
   */
  public static final int SCREENSIZEMAX = 19 * UICONST;
  /** Constant used in param width or height (will use parent's minimum size between width and height) in setRect. 
   * You can use this constant added or subtracted to a number to specify a increment to the calculated size.
   * There are two ways to use it:<br>
   * 1. PARENTSIZEMIN + constant: it will use as a PERCENTAGE of the parent's size. For example, PARENTSIZEMIN+20 in width will result in 20% of parent's size.<br>
   * 2. PARENTSIZEMIN - constant: it will use as a FRACTION of the parent's size. For example, PARENTSIZEMIN-4 in width will result in 1/4 of parent's size.<br>
   * 
   * If there are no constant number, size will be 100% of the parent's width/height.
   * 
   * If the parent is unknown, the screen size will be used instead.
   * @since TotalCross 2.0
   */
  public static final int PARENTSIZEMIN = 20 * UICONST;
  /** Constant used in param width or height (will use parent's maximum size between width and height) in setRect. 
   * You can use this constant added or subtracted to a number to specify a increment to the calculated size.
   * There are two ways to use it:<br>
   * 1. PARENTSIZEMAX + constant: it will use as a PERCENTAGE of the parent's size. For example, PARENTSIZEMAX+20 in width will result in 20% of parent's size.<br>
   * 2. PARENTSIZEMAX - constant: it will use as a FRACTION of the parent's size. For example, PARENTSIZEMAX-4 in width will result in 1/4 of parent's size.<br>
   * 
   * If there are no constant number, size will be 100% of the parent's width/height.
   * 
   * If the parent is unknown, the screen size will be used instead.
   * @since TotalCross 2.0
   */
  public static final int PARENTSIZEMAX = 21 * UICONST;
  /** Constant used in param width (will use parent's width) and height (will use the current font size/height) in setRect.
   * You can use this constant added or subtracted to a number to specify a increment to the calculated size.
   * There are two ways to use it:<br>
   * 1. FONTSIZE + constant: it will use as a PERCENTAGE of the parent's size. For example, FONTSIZE+20 in width will result in 20% of font's size.<br>
   * 2. FONTSIZE - constant: it will use as a FRACTION of the parent's size. For example, FONTSIZE-4 in width will result in 1/4 of font's size.<br>
   * 
   * If there are no constant number, size will be 100% of the font's width/height. So, FONTSIZE and FONTSIZE+100 is the same.
   * @since TotalCross 1.52
   */
  public static final int FONTSIZE = 22 * UICONST;
  
  public static final int DP = 23 * UICONST;
  /** Constant used in params width/height in setRect. It informs that the parent's last width/height should not be updated now, because it will be resized later. Note that it does NOT support increment nor decrement.
   * Sample:
   * <pre>
   * Container c;
   * add(c = new Container(), LEFT+5, AFTER+5, FILL-5, WILL_RESIZE);
   * // add controls to c
   * c.resizeHeight();
   * </pre>
   * @since TotalCross 1.14
   */
  public static final int WILL_RESIZE = RANGE / 3; // guich@tc114_68
  private static final int MAXABSOLUTECOORD = PREFERRED - RANGE;

  /** Set to true to ignore parent's insets when placing this control on screen. */
  public boolean ignoreInsets;

  private ControlEvent pressedEvent; // guich@tc100: share the same event across all controls - guich@tc114_42: no longer share

  /** Allows to disable the ui adjustments based on font height for a single control.
   * Set this flag in the constructor. It is propagated to all child controls.
   * @since TotalCross 1.3
   */
  public boolean uiAdjustmentsBasedOnFontHeightIsSupported = true;

  /** Set to false to disallow the screen update. */
  public static boolean enableUpdateScreen = true;

  /** Stores the height of the current font. The same of <code>fm.height</code>. */
  protected int fmH;

  /** Stores the control that should handle the focus change key for this control  */
  protected boolean focusHandler; // kmeehl@tc100

  /** If true, the keyboard arrows will be used to highlight the controls until one is selected.
   * This will cause the KeyEvent to be intercepted and handled by the method changeHighlighted.
   * When the user press the ACTION (or ENTER) key to use the control, this flag is set to false
   * and the focus will be set to the control, so it be able to use the arrows to navigate
   * inside it. The control must then set this to true when the finish using it
   * or press the ACTION button again (which then sets the flag to true).
   * @since SuperWaba 5.5
   */
  protected static boolean isHighlighting = true;

  /** Defines if this control can receive focus by using the arrow keys.
   * @since SuperWaba 5.5
   */
  public boolean focusTraversable = true;

  /** Shortcuts to test the UI style. Use the setUIStyle method to change them accordingly. */
  protected static boolean uiFlat, uiVista = true, uiAndroid, uiHolo, uiMaterial;

  /** If true, this control will receive pen and key events but will never gain focus.
   * This is useful to create keypads. See totalcross.ui.Calculator.
   */
  protected boolean focusLess;

  protected EnabledStateChangeEvent esce = new EnabledStateChangeEvent();
  public boolean eventsEnabled = true;

  /** Color to fill the background in Material UI when its enabled. */
  public int fillColor = -1;

  Rect cli = new Rect();
  /** Specifies if this device is a tablet, computing the number of text lines.
   */
  public static boolean isTablet;

  protected static final int SETX_NOT_SET = -100000000;
  protected int setX = SETX_NOT_SET, setY, setW, setH;
  protected Font setFont;
  protected Control setRel;
  protected boolean repositionAllowed;
  protected int tempW = -1; // used in flowContainer
  protected TranslucentShape translucentShape = TranslucentShape.NONE;

  /** The shadow color to be applied to this control. */
  public int textShadowColor; // guich@tc126_26

  /** Set to true to call onEvent before calling the event listeners.
   * By default, the event listener is called before the onEvent. */
  public boolean onEventFirst;

  /** READ-ONLY property which is not null if this control is a container */
  protected Container asContainer; // speedup some instance_of Container
  /** READ-ONLY property which is not null if this control is a window */
  protected Window asWindow; // speedup some instance_of Window
  /** The cached Graphics objects for this control. */
  Graphics gfx;

  static ToolTip uitip;

  /** To be used in the setTextShadowColor method. */
  public static final int BRIGHTER_BACKGROUND = -2;
  /** To be used in the setTextShadowColor method. */
  public static final int DARKER_BACKGROUND = -3;

  private List<Listener> listeners = new ArrayList<>();
  private static boolean callingUpdScr, callingRepNow;

  /** Alpha to be used in some controls, ranging from 0 to 255. */
  public int alphaValue = 255;

  /** Set the background to be transparent, by not filling the control's area with the background color.
   * @since TotalCross 1.0
   */
  public boolean transparentBackground;
  /** True means this control expects to get focus on a PEN_DOWN event. If focusOnPenDown is false, focus will be set on PEN_UP instead.
   *  This can be used for things like drag-scrollable list controls that contain controls as list items, to avoid setting focus to an
   *  item (and therefore changing the selection) during a drag-scroll. */
  public boolean focusOnPenDown = true;

  /** True means that EventListeners for PenEvent and KeyEvent will be called without verifying that the event target is this. */
  public boolean callListenersOnAllTargets;

  private Control dragTarget; // holds the Control that handled the last dragEvent sent to this control.

  /** The offscreen image taken with takeScreenShot. The onPaint will use this shot until the user calls releaseScreenShot.
   */
  public Image offscreen, offscreen0;

  /** Keep the control disabled even if enabled is true. */
  public boolean keepDisabled;

  /** Keep the control enabled even if enabled is false. */
  public boolean keepEnabled;
  
  /**Sets to true to make it control float on a ScrollContainer.*/
  protected boolean floating = false;
  
  /** The current effect used in this control */
  public UIEffects effect;
  
  /**The NinePatch parts used to paint the background image*/
  protected NinePatch.Parts npParts;
  
  /**
   * Sets to true to do the effect.
   * */
  private boolean doEffect = true;
  
  /**
   * Set the NinePatch of the control. This is used to draw the background image.
   * @param img The image to make the NinePatch.
   * <i>The image must be on a NinePatch format.</i>
   * @param side The size, in pixels, of the NinePatch's side.
   * @param corner The size, in pixels, of the NinePatch's corner */
  public void setNinePatch(Image img, int corner, int side) {
	npParts = img == null ? null : NinePatch.getInstance().load(img, corner, side);
  }
  /**
   * Set the NinePatch of the control. This is used to draw the background image.
   * @param img The image with guides to make the NinePatch.
   * <i>The image must be on a NinePatch format.</i>
   * */
  public void setNinePatch(Image img) {
	  npParts = img == null ? null : NinePatch.getInstance().load(img);
  }
  
  /** creates the font for this control as the same font of the MainWindow. */
  protected Control() {
    if (MainWindow.defaultFont == null) // guich@tc130: moved from static block at MainWindow to here, so user can define if he wants to use the old or new font
    {
      MainWindow.defaultFont = Font.getFont(Font.DEFAULT, false, Font.NORMAL_SIZE);
      if (Settings.onJavaSE && !Font.DEFAULT.equals("TCFont")) {
        Vm.warning(
            "You're using the old font. Consider porting your program to the new font. See Settings.useNewFont javadocs.");
      }
      isTablet = Math.max(Settings.screenWidth, Settings.screenHeight) / Font.NORMAL_SIZE > 30;
    }
    font = MainWindow.defaultFont;
    fm = font.fm; // guich@450_36: new way of getting the fontMetrics.
    fmH = fm.height;
    gfx = new Graphics(this); // guich@tc100
    textShadowColor = UIColors.textShadowColor;
  }

  private void takeScreenShot(int nr) {
    try {
      if (nr == 1) {
        // release memory
        offscreen = null;
      } else {
        offscreen0 = null;
      }
      Image offscreen = new Image(width, height);
      paint2shot(offscreen.getGraphics(), false);
      if (nr == 1) {
        this.offscreen = offscreen;
      } else {
        this.offscreen0 = offscreen;
      }
      offscreen.applyChanges();
    } catch (Throwable t) {
      if (nr == 1) {
        this.offscreen = null;
      } else {
        this.offscreen0 = null;
      }
    }
  }

  /** Take a screen shot of this container and stores it in <code>offscreen0</code>.
   */
  public void takeInitialScreenShot() {
    takeScreenShot(0);
  }

  /** Take a screen shot of this container and stores it in <code>offscreen</code>.
   */
  public void takeScreenShot() {
    takeScreenShot(1);
  }

  void paint2shot(Graphics g, boolean shift) {
    if (!transparentBackground && parent != null
        && !(parent.parent != null && parent.parent instanceof ScrollContainer && parent.parent.transparentBackground)) // last clause prevents a white background on SAV's menu 
    {
      g.backColor = parent.backColor;
      g.fillRect(0, 0, width, height);
    }
    g.setFont(font);
    if (asWindow != null) {
      asWindow.paintWindowBackground(g);
    }
    paint2shot(g, this, shift);
  }

  /** Releases the screen shot. */
  public void releaseScreenShot() {
    offscreen = offscreen0 = null;
    Window.needsPaint = true;
  }

  void paint2shot(Graphics g, Control top, boolean shift) {
    // if (asContainer != null || asWindow != null)
    //  this.refreshGraphics(g,0,top);
    if (this.asWindow == null) {
      this.onPaint(g);
    }
    Window w = getParentWindow();
    int x0 = shift ? w.x : 0;
    int y0 = shift ? w.y : 0;
    Rect rtop = top.getRect();
    if (asContainer != null) {
      for (Control child = asContainer.children; child != null; child = child.next) {
        if (child.visible) {
          Rect r = child.getAbsoluteRect();
          if (rtop.intersects(r)) {
            child.refreshGraphics(g, 0, top, x0, y0);
            child.onPaint(g);
            if (child.asContainer != null) {
              child.asContainer.paint2shot(g, top, shift);
            }
          }
        }
      }
    }
  }

  /** Call to set the color value to place a shadow around the control's text. The shadow is made
   * drawing the button in (x-1,y-1), (x+1,y-1), (x-1,y+1), (x+1,y+1) positions.
   * Defaults to -1, which means no shadow.
   * You can set pass BRIGHTER_BACKGROUND or DARKER_BACKGROUND as parameter, AFTER calling setBackColor or setForeColor,
   * to compute the color based on the background.
   * <br><br> Example:
   * <pre>
   * c = new Label(....);
   * c.setBackColor(Color.BLUE);
   * c.setTextShadowColor(DARKER_BACKGROUND);
   * // you may also set it directly to a color: c.setTextShadowColor(Color.BLACK);
   * </pre>
   * 
   * @see #BRIGHTER_BACKGROUND
   * @see #DARKER_BACKGROUND
   * @see UIColors#textShadowColor
   * @since TotalCross 1.27
   */
  public void setTextShadowColor(int color) {
    this.textShadowColor = color == BRIGHTER_BACKGROUND ? Color.brighter(backColor)
        : color == DARKER_BACKGROUND ? Color.darker(backColor) : color;
  }

  /** Returns the textShadowColor of this control. 
   * @since TotalCross 1.27
   */
  public int getTextShadowColor() {
    return textShadowColor;
  }

  /** Returns true if the point lies inside this control.
   * If it don't lies, but the device is a finger touch one,
   * checks if the distance is below the touch tolerance.
   * @see totalcross.sys.Settings#touchTolerance
   * @see totalcross.sys.Settings#fingerTouch
   * @since TotalCross 1.2
   */
  public boolean isInsideOrNear(int x, int y) // guich@tc120_48
  {
    if (0 <= x && x < width && 0 <= y && y < height) {
      return true;
    }
    if (!Settings.fingerTouch) {
      return false;
    }
    int d = (int) (Convert.getDistancePoint2Rect(x, y, 0, 0, width, height) + 0.5);
    return d <= Settings.touchTolerance;
  }

  /** Shows a message using a global tip shared by all controls. */
  static void showTip(Control c, String s, int duration, int y) // guich@tc100b4_27
  {
    uitip.millisDisplay = duration;
    uitip.setText(s);
    Window w = c.getParentWindow(); // guich@tc114_59: exclude window position
    Rect r = c.getAbsoluteRect();
    if (y >= 0) {
      r.y += y;
      r.height = 0;
    } else {
      r.x -= w.x;
      r.y -= w.y;
    }
    uitip.setControlRect(r);
    uitip.show();
  }

  /** Shows a message using a global tip shared by all controls. */
  public void showTip(String s, int duration, int y) {
    Control.showTip(this, s, duration, y);
  }

  /** Posts a ControlEvent.PRESSED event with this control as target.
   * @since TotalCross 1.14 
   */
  public void postPressedEvent() {
    postEvent(getPressedEvent(this));
  }

  /** Creates a ControlEvent.PRESSED if not yet created and returns it. 
   * @since TotalCross 1.14
   */
  protected ControlEvent getPressedEvent(Control target) {
    if (pressedEvent == null) {
      pressedEvent = new ControlEvent(ControlEvent.PRESSED, null);
    }
    return pressedEvent.update(target);
  }

  /**
   * Adds a timer to a control. Each time the timer ticks, a TIMER
   * event will be posted to the control. The timer does
   * not interrupt the program during its execution at the timer interval,
   * it is scheduled along with application events. The timer object
   * returned from this method can be passed to removeTimer() to
   * remove the timer. Under Windows, the timer has a minimum resolution
   * of 55ms due to the native Windows system clock resolution of 55ms. Under
   * Palm OS and other platforms, the minimum timer resolution is 10ms.
   * <p>
   * If the control that holds the timer is removed from screen, the 
   * timer is also disabled. Consider using the dispatch-listener event 
   * model (addTimerListener) instead of creating a control just to catch 
   * the event (if this is the case).
   *
   * @param millis the timer tick interval in milliseconds
   * @see totalcross.ui.event.TimerEvent
   */
  public TimerEvent addTimer(int millis) {
    return MainWindow.mainWindowInstance.addTimer(this, millis);
  }

  /**
   * Add a timer to a control. This method allows you to create an instance
   * TimerEvent (or any descendant) ahead of time and add it to the control.
   * @param t the TimerEvent instance
   * @param millis the timer tick interval in milliseconds
   * @see totalcross.ui.event.TimerEvent
   */
  public void addTimer(TimerEvent t, int millis) {
    MainWindow.mainWindowInstance.addTimer(t, this, millis);
  }

  /**
   * Removes a timer from a control. True is returned if the timer was
   * found and removed and false is returned if the timer could not be
   * found (meaning it was not active).
   */
  public boolean removeTimer(TimerEvent timer) {
    return MainWindow.mainWindowInstance.removeTimer(timer);
  }

  /** Sets the font of this control. */
  final public void setFont(Font font) {
    this.setFont = this.font = font;
    this.fm = font.fm;
    this.fmH = fm.height;
    onFontChanged();
    postEvent(new FontChangeEvent(this, font));
  }

  /** Gets the font of this control. */
  public Font getFont() {
    return this.font;
  }

  /** Returns the preferred width of this control. */
  public int getPreferredWidth() {
    return 30;
  }

  /** Returns the preferred height of this control. */
  public int getPreferredHeight() {
    return fmH;
  }

  /** Sets or changes a control's position and size.
   * <pre>
   * setRect(r.x,r.y,r.width,r.height,null,false)
   * </pre>
   * @see #setRect(int, int, int, int, Control, boolean)
   */
  public void setRect(Rect r) {
    setRect(r.x, r.y, r.width, r.height, null, false);
  }

  /** Sets or changes a control's position and size. Same of
   * <pre>
   * setRect(x,y,width,height,null,false)
   * </pre>
   * @see #setRect(int, int, int, int, Control, boolean)
   */
  public void setRect(int x, int y, int width, int height) {
    setRect(x, y, width, height, null, false);
  }

  /** Sets or changes a control's position and size.
   * <pre>
   * setRect(x,y,width,height,relative,false)
   * </pre>
   * @see #setRect(int, int, int, int, Control, boolean)
   */
  public void setRect(int x, int y, int width, int height, Control relative) {
    setRect(x, y, width, height, relative, false);
  }

  /** The relative positioning will be made with the given control (relative).
   * Note that in this case, only the SAME,BEFORE,AFTER are affected by the given control.
   * Here is an example of relative positioning:
   * <p>
   * Important note: you can't use FILL/FIT with BEFORE/RIGHT/BOTTOM (for x,y).
   * <pre>
   * add(new Label("1"),CENTER,CENTER);
   * add(new Label("2"),AFTER,SAME);
   * add(new Label("3"),SAME,AFTER);
   * add(new Label("4"),BEFORE,SAME);
   * add(new Label("5"),BEFORE,BEFORE);
   * </pre>
   * You will see this on screen:
   * <pre>
   * 512
   *  43
   * </pre>
   * Note: add(control, x,y) does: <code>add(control); control.setRect(x,y,PREFERRED,PREFERRED);</code>
   * <p>
   * <b>Important! Always add the control to the container before doing a setRect.</b>
   * <p>The relative positioning does not work well if the control is placed outside screen bounds.
   * @param x One of the relative positioning constants: LEFT, RIGHT, SAME, BEFORE, AFTER, CENTER, with a small adjustment. You can also use an absolute value, but this is strongly discouraged.
   * @param y One of the relative positioning constants: TOP, BOTTOM, SAME, BEFORE, AFTER, CENTER, with a small adjustment. You can also use an absolute value, but this is strongly discouraged.
   * @param width  One of the relative positioning constants: PREFERRED, FILL, FIT, SAME. You can also use an absolute value, but this is strongly discouraged.
   * @param height One of the relative positioning constants: PREFERRED, FILL, FIT, SAME. You can also use an absolute value, but this is strongly discouraged.
   * @param relative To whom the position should be relative to; or null to be relative to the last control.
   * @param screenChanged Indicates that a screen change (resize, collapse) occured and the <code>reposition</code> method is calling this method. Set by the system. If you call this method directly, always pass false to it.
   * @see #LEFT
   * @see #TOP
   * @see #RIGHT
   * @see #BOTTOM
   * @see #BEFORE
   * @see #AFTER
   * @see #CENTER
   * @see #SAME
   * @see #FILL
   * @see #PREFERRED
   * @see #FIT
   * @see #CENTER_OF
   * @see #RIGHT_OF
   * @see #BOTTOM_OF
   * @see #SCREENSIZE
   * @see #SCREENSIZEMIN
   * @see #SCREENSIZEMAX
   * @see #PARENTSIZE
   * @see #PARENTSIZEMIN
   * @see #PARENTSIZEMAX
   * @see #FONTSIZE
   * @see Container#add(Control, int, int)
   * @see Container#add(Control, int, int, Control)
   */
  public void setRect(int x, int y, int width, int height, Control relative, boolean screenChanged) {
    if (setX == SETX_NOT_SET) {
      setX = x;
      setY = y;
      setW = width;
      setH = height;
      setRel = relative;
      setFont = this.font;
    }
    if (x + y + width + height >= MAXABSOLUTECOORD) // are there any relative coords?
    {
      if (x == KEEP) {
        x = this.x;
      }
      if (y == KEEP) {
        y = this.y;
      }
      if (width == KEEP) {
        width = this.width;
      }
      if (height == KEEP) {
        height = this.height;
      }

      repositionAllowed = true;
      int lpx = 0, lpy = 0;
      Container parent = this.parent; // guich@450_36: use local var instead of field
      Rect cli = this.cli; // guich@450_36: avoid recreating Rects
      // relative placement
      if (parent != null) {
        if (!ignoreInsets) {
          parent.getClientRect(cli);
        } else {
          cli.x = cli.y = 0;
          cli.width = parent.width;
          cli.height = parent.height;
        }

        lpx = parent.lastX;
        lpy = parent.lastY;
        if (relative != null) {
          // use the given control's coords instead of parent's ones.
          parent.lastX = relative.x;
          parent.lastY = relative.y;
          parent.lastW = relative.width;
          parent.lastH = relative.height;
        } else if (parent.lastX == -999999) // first control being added? - guich@450_36: only one check is enough
        {
          parent.lastX = cli.x;
          parent.lastY = cli.y;
        }
      } else {
        cli.y = cli.x = 0; // guich@450a_40
        cli.width = Settings.screenWidth;
        cli.height = Settings.screenHeight;
      }

      if (Settings.uiAdjustmentsBasedOnFontHeight && uiAdjustmentsBasedOnFontHeightIsSupported) {
        // non-dependant width
        if (width < MAXABSOLUTECOORD) {
        } else if ((PREFERRED - RANGE) <= width && width <= (PREFERRED + RANGE)) {
          width = getPreferredWidth() + (width - PREFERRED) * fmH / 100;
        } else // guich@450_36: changed order to be able to put an else here
        if ((SAME - RANGE) <= width && width <= (SAME + RANGE) && parent != null) {
          width = parent.lastW + (width - SAME) * fmH / 100;
        } else // can't be moved from here!
        if ((SCREENSIZE - RANGE) <= width && width <= (SCREENSIZE + RANGE)) {
          width -= SCREENSIZE;
          if (width < 0) {
            width = Settings.screenWidth / -width;
          } else if (width == 0) {
            width = Settings.screenWidth;
          } else {
            width = width * Settings.screenWidth / 100;
          }
        } else if ((SCREENSIZEMIN - RANGE) <= width && width <= (SCREENSIZEMIN + RANGE)) {
          width -= SCREENSIZEMIN;
          if (width < 0) {
            width = Math.min(Settings.screenWidth, Settings.screenHeight) / -width;
          } else if (width == 0) {
            width = Math.min(Settings.screenWidth, Settings.screenHeight);
          } else {
            width = width * Math.min(Settings.screenWidth, Settings.screenHeight) / 100;
          }
        } else if ((SCREENSIZEMAX - RANGE) <= width && width <= (SCREENSIZEMAX + RANGE)) {
          width -= SCREENSIZEMAX;
          if (width < 0) {
            width = Math.max(Settings.screenWidth, Settings.screenHeight) / -width;
          } else if (width == 0) {
            width = Math.max(Settings.screenWidth, Settings.screenHeight);
          } else {
            width = width * Math.max(Settings.screenWidth, Settings.screenHeight) / 100;
          }
        } else if ((PARENTSIZE - RANGE) <= width && width <= (PARENTSIZE + RANGE)) {
          width -= PARENTSIZE;
          if (width < 0) {
            width = cli.width / -width;
          } else if (width == 0) {
            width = cli.width;
          } else {
            width = width * cli.width / 100;
          }
        } else if ((PARENTSIZEMIN - RANGE) <= width && width <= (PARENTSIZEMIN + RANGE)) {
          width -= PARENTSIZEMIN;
          if (width < 0) {
            width = Math.min(cli.width, cli.height) / -width;
          } else if (width == 0) {
            width = Math.min(cli.width, cli.height);
          } else {
            width = width * Math.min(cli.width, cli.height) / 100;
          }
        } else if ((PARENTSIZEMAX - RANGE) <= width && width <= (PARENTSIZEMAX + RANGE)) {
          width -= PARENTSIZEMAX;
          if (width < 0) {
            width = Math.max(cli.width, cli.height) / -width;
          } else if (width == 0) {
            width = Math.max(cli.width, cli.height);
          } else {
            width = width * Math.max(cli.width, cli.height) / 100;
          }
        } else if ((FONTSIZE - RANGE) <= width && width <= (FONTSIZE + RANGE)) {
          width -= FONTSIZE;
          if (width < 0) {
            width = fmH / -width;
          } else if (width == 0) {
            width = fmH;
          } else {
            width = width * fmH / 100;
          }
        } else if ((DP - RANGE) <= width && width <= (DP + RANGE)) {
          width -= DP;
          width *= Settings.screenDensity;
        }
        tempW = width;
        // non-dependant height
        if (height < MAXABSOLUTECOORD) {
        } else if ((PREFERRED - RANGE) <= height && height <= (PREFERRED + RANGE)) {
          height = getPreferredHeight() + (height - PREFERRED) * fmH / 100;
        } else if ((SAME - RANGE) <= height && height <= (SAME + RANGE) && parent != null) {
          height = parent.lastH + (height - SAME) * fmH / 100;
        } else // can't be moved from here!
        if ((SCREENSIZE - RANGE) <= height && height <= (SCREENSIZE + RANGE)) {
          height -= SCREENSIZE;
          if (height < 0) {
            height = Settings.screenHeight / -height;
          } else if (height == 0) {
            height = Settings.screenHeight;
          } else {
            height = height * Settings.screenHeight / 100;
          }
        } else if ((SCREENSIZEMIN - RANGE) <= height && height <= (SCREENSIZEMIN + RANGE)) {
          height -= SCREENSIZEMIN;
          if (height < 0) {
            height = Math.min(Settings.screenWidth, Settings.screenHeight) / -height;
          } else if (height == 0) {
            height = Math.min(Settings.screenWidth, Settings.screenHeight);
          } else {
            height = height * Math.min(Settings.screenWidth, Settings.screenHeight) / 100;
          }
        } else if ((SCREENSIZEMAX - RANGE) <= height && height <= (SCREENSIZEMAX + RANGE)) {
          height -= SCREENSIZEMAX;
          if (height < 0) {
            height = Math.max(Settings.screenWidth, Settings.screenHeight) / -height;
          } else if (height == 0) {
            height = Math.max(Settings.screenWidth, Settings.screenHeight);
          } else {
            height = height * Math.max(Settings.screenWidth, Settings.screenHeight) / 100;
          }
        } else if ((PARENTSIZE - RANGE) <= height && height <= (PARENTSIZE + RANGE)) {
          height -= PARENTSIZE;
          if (height < 0) {
            height = cli.height / -height;
          } else if (height == 0) {
            height = cli.height;
          } else {
            height = height * cli.height / 100;
          }
        } else if ((PARENTSIZEMIN - RANGE) <= height && height <= (PARENTSIZEMIN + RANGE)) {
          height -= PARENTSIZEMIN;
          if (height < 0) {
            height = Math.min(cli.width, cli.height) / -height;
          } else if (height == 0) {
            height = Math.min(cli.width, cli.height);
          } else {
            height = height * Math.min(cli.width, cli.height) / 100;
          }
        } else if ((PARENTSIZEMAX - RANGE) <= height && height <= (PARENTSIZEMAX + RANGE)) {
          height -= PARENTSIZEMAX;
          if (height < 0) {
            height = Math.max(cli.width, cli.height) / -height;
          } else if (height == 0) {
            height = Math.max(cli.width, cli.height);
          } else {
            height = height * Math.max(cli.width, cli.height) / 100;
          }
        } else if ((FONTSIZE - RANGE) <= height && height <= (FONTSIZE + RANGE)) {
          height -= FONTSIZE;
          if (height < 0) {
            height = fmH / -height;
          } else if (height == 0) {
            height = fmH;
          } else {
            height = height * fmH / 100;
          }
        } else if ((DP - RANGE) <= height && height <= (DP + RANGE)) {
          height -= DP;
          height *= Settings.screenDensity;
        }
        // x
        if (x < MAXABSOLUTECOORD) {
        } else if ((AFTER - RANGE) <= x && x <= (AFTER + RANGE) && parent != null) {
          x = parent.lastX + parent.lastW + (x - AFTER) * fmH / 100;
        } else // guich@450_36: test parent only after testing the relative type
        if ((BEFORE - RANGE) <= x && x <= (BEFORE + RANGE) && parent != null) {
          x = parent.lastX - width + (x - BEFORE) * fmH / 100;
        } else if ((SAME - RANGE) <= x && x <= (SAME + RANGE) && parent != null) {
          x = parent.lastX + (x - SAME) * fmH / 100;
        } else if ((LEFT - RANGE) <= x && x <= (LEFT + RANGE)) {
          x = cli.x + (x - LEFT) * fmH / 100;
        } else if ((RIGHT - RANGE) <= x && x <= (RIGHT + RANGE)) {
          x = cli.x + cli.width - width + (x - RIGHT) * fmH / 100;
        } else if ((PARENTSIZE - RANGE) <= x && x <= (PARENTSIZE + RANGE)) {
          x -= PARENTSIZE;
          if (x < 0) {
            x = cli.width / -x;
          } else if (x == 0) {
            x = cli.width;
          } else {
            x = x * cli.width / 100;
          }
          x -= width / 2;
        } else if ((PARENTSIZEMIN - RANGE) <= x && x <= (PARENTSIZEMIN + RANGE)) {
          x -= PARENTSIZEMIN;
          if (x < 0) {
            x = cli.width / -x;
          } else if (x == 0) {
            x = cli.width;
          } else {
            x = x * cli.width / 100;
          }
          x -= width;
        } else if ((PARENTSIZEMAX - RANGE) <= x && x <= (PARENTSIZEMAX + RANGE)) {
          x -= PARENTSIZEMAX;
          if (x < 0) {
            x = cli.width / -x;
          } else if (x == 0) {
            x = cli.width;
          } else {
            x = x * cli.width / 100;
          }
        } else if ((CENTER - RANGE) <= x && x <= (CENTER + RANGE)) {
          x = cli.x + ((cli.width - width) >> 1) + (x - CENTER) * fmH / 100;
        } else if ((CENTER_OF - RANGE) <= x && x <= (CENTER_OF + RANGE)) {
          x = parent.lastX + (parent.lastW - width) / 2 + (x - CENTER_OF) * fmH / 100;
        } else // guich@tc110_88
        if ((RIGHT_OF - RANGE) <= x && x <= (RIGHT_OF + RANGE)) {
          x = parent.lastX + (parent.lastW - width) + (x - RIGHT_OF) * fmH / 100; // guich@tc110_97
        }
        // y
        if (y <= MAXABSOLUTECOORD) {
        } else if ((AFTER - RANGE) <= y && y <= (AFTER + RANGE) && parent != null) {
          y = parent.lastY + parent.lastH + (y - AFTER) * fmH / 100;
        } else // guich@450_36: test parent only after testing the relative type
        if ((BEFORE - RANGE) <= y && y <= (BEFORE + RANGE) && parent != null) {
          y = parent.lastY - height + (y - BEFORE) * fmH / 100;
        } else if ((SAME - RANGE) <= y && y <= (SAME + RANGE) && parent != null) {
          y = parent.lastY + (y - SAME) * fmH / 100;
        } else if ((TOP - RANGE) <= y && y <= (TOP + RANGE)) {
          y = cli.y + (y - TOP) * fmH / 100;
        } else if ((BOTTOM - RANGE) <= y && y <= (BOTTOM + RANGE)) {
          y = cli.y + cli.height - height + (y - BOTTOM) * fmH / 100;
        } else if ((PARENTSIZE - RANGE) <= y && y <= (PARENTSIZE + RANGE)) {
          y -= PARENTSIZE;
          if (y < 0) {
            y = cli.height / -y;
          } else if (y == 0) {
            y = cli.height;
          } else {
            y = y * cli.height / 100;
          }
          y -= height / 2;
        } else if ((PARENTSIZEMIN - RANGE) <= y && y <= (PARENTSIZEMIN + RANGE)) {
          y -= PARENTSIZEMIN;
          if (y < 0) {
            y = cli.height / -y;
          } else if (y == 0) {
            y = cli.height;
          } else {
            y = y * cli.height / 100;
          }
          y -= height;
        } else if ((PARENTSIZEMAX - RANGE) <= y && y <= (PARENTSIZEMAX + RANGE)) {
          y -= PARENTSIZEMAX;
          if (y < 0) {
            y = cli.height / -y;
          } else if (y == 0) {
            y = cli.height;
          } else {
            y = y * cli.height / 100;
          }
        } else if ((CENTER - RANGE) <= y && y <= (CENTER + RANGE)) {
          y = cli.y + ((cli.height - height) >> 1) + (y - CENTER) * fmH / 100;
        } else if ((CENTER_OF - RANGE) <= y && y <= (CENTER_OF + RANGE)) {
          y = parent.lastY + (parent.lastH - height) / 2 + (y - CENTER_OF) * fmH / 100;
        } else // guich@tc110_88
        if ((BOTTOM_OF - RANGE) <= y && y <= (BOTTOM_OF + RANGE)) {
          y = parent.lastY + (parent.lastH - height) + (y - BOTTOM_OF) * fmH / 100; // guich@tc110_97
        }
        // width that depends on x
        if (width > MAXABSOLUTECOORD) {
          if ((FILL - RANGE) <= width && width <= (FILL + RANGE)) {
            width = cli.width - x + cli.x + (width - FILL) * fmH / 100;
          } else if ((FIT - RANGE) <= width && width <= (FIT + RANGE) && parent != null) {
            width = lpx - x + (width - FIT) * fmH / 100;
          }
          tempW = width;
        }
        // height that depends on y
        if (height > MAXABSOLUTECOORD) {
          if ((FILL - RANGE) <= height && height <= (FILL + RANGE)) {
            height = cli.height - y + cli.y + (height - FILL) * fmH / 100;
          } else if ((FIT - RANGE) <= height && height <= (FIT + RANGE) && parent != null) {
            height = lpy - y + (height - FIT) * fmH / 100;
          }
        }
      } else {
        // non-dependant width
        if (width < MAXABSOLUTECOORD) {
        } else if ((PREFERRED - RANGE) <= width && width <= (PREFERRED + RANGE)) {
          width += getPreferredWidth() - PREFERRED;
        } else // guich@450_36: changed order to be able to put an else here
        if ((SAME - RANGE) <= width && width <= (SAME + RANGE) && parent != null) {
          width += parent.lastW - SAME;
        } else // can't be moved from here!
        if ((SCREENSIZE - RANGE) <= width && width <= (SCREENSIZE + RANGE)) {
          width -= SCREENSIZE;
          if (width < 0) {
            width = Settings.screenWidth / -width;
          } else if (width == 0) {
            width = Settings.screenWidth;
          } else {
            width = width * Settings.screenWidth / 100;
          }
        } else if ((SCREENSIZEMIN - RANGE) <= width && width <= (SCREENSIZEMIN + RANGE)) {
          width -= SCREENSIZEMIN;
          if (width < 0) {
            width = Math.min(Settings.screenWidth, Settings.screenHeight) / -width;
          } else if (width == 0) {
            width = Math.min(Settings.screenWidth, Settings.screenHeight);
          } else {
            width = width * Math.min(Settings.screenWidth, Settings.screenHeight) / 100;
          }
        } else if ((SCREENSIZEMAX - RANGE) <= width && width <= (SCREENSIZEMAX + RANGE)) {
          width -= SCREENSIZEMAX;
          if (width < 0) {
            width = Math.max(Settings.screenWidth, Settings.screenHeight) / -width;
          } else if (width == 0) {
            width = Math.max(Settings.screenWidth, Settings.screenHeight);
          } else {
            width = width * Math.max(Settings.screenWidth, Settings.screenHeight) / 100;
          }
        } else if ((PARENTSIZE - RANGE) <= width && width <= (PARENTSIZE + RANGE)) {
          width -= PARENTSIZE;
          if (width < 0) {
            width = cli.width / -width;
          } else if (width == 0) {
            width = cli.width;
          } else {
            width = width * cli.width / 100;
          }
        } else if ((PARENTSIZEMIN - RANGE) <= width && width <= (PARENTSIZEMIN + RANGE)) {
          width -= PARENTSIZEMIN;
          if (width < 0) {
            width = Math.min(cli.width, cli.height) / -width;
          } else if (width == 0) {
            width = Math.min(cli.width, cli.height);
          } else {
            width = width * Math.min(cli.width, cli.height) / 100;
          }
        } else if ((PARENTSIZEMAX - RANGE) <= width && width <= (PARENTSIZEMAX + RANGE)) {
          width -= PARENTSIZEMAX;
          if (width < 0) {
            width = Math.max(cli.width, cli.height) / -width;
          } else if (width == 0) {
            width = Math.max(cli.width, cli.height);
          } else {
            width = width * Math.max(cli.width, cli.height) / 100;
          }
        } else if ((FONTSIZE - RANGE) <= width && width <= (FONTSIZE + RANGE)) {
          width -= FONTSIZE;
          if (width < 0) {
            width = fmH / -width;
          } else if (width == 0) {
            width = fmH;
          } else {
            width = width * fmH / 100;
          }
        } else if ((DP - RANGE) <= width && width <= (DP + RANGE)) {
          width -= DP;
          width *= Settings.screenDensity;
        }
        tempW = width;
        // non-dependant height
        if (height < MAXABSOLUTECOORD) {
        } else if ((PREFERRED - RANGE) <= height && height <= (PREFERRED + RANGE)) {
          height += getPreferredHeight() - PREFERRED;
        } else if ((SAME - RANGE) <= height && height <= (SAME + RANGE) && parent != null) {
          height += parent.lastH - SAME;
        } else // can't be moved from here!
        if ((SCREENSIZE - RANGE) <= height && height <= (SCREENSIZE + RANGE)) {
          height -= SCREENSIZE;
          if (height < 0) {
            height = Settings.screenHeight / -height;
          } else if (height == 0) {
            height = Settings.screenHeight;
          } else {
            height = height * Settings.screenHeight / 100;
          }
        } else if ((SCREENSIZEMIN - RANGE) <= height && height <= (SCREENSIZEMIN + RANGE)) {
          height -= SCREENSIZEMIN;
          if (height < 0) {
            height = Math.min(Settings.screenWidth, Settings.screenHeight) / -height;
          } else if (height == 0) {
            height = Math.min(Settings.screenWidth, Settings.screenHeight);
          } else {
            height = height * Math.min(Settings.screenWidth, Settings.screenHeight) / 100;
          }
        } else if ((SCREENSIZEMAX - RANGE) <= height && height <= (SCREENSIZEMAX + RANGE)) {
          height -= SCREENSIZEMAX;
          if (height < 0) {
            height = Math.max(Settings.screenWidth, Settings.screenHeight) / -height;
          } else if (height == 0) {
            height = Math.max(Settings.screenWidth, Settings.screenHeight);
          } else {
            height = height * Math.max(Settings.screenWidth, Settings.screenHeight) / 100;
          }
        } else if ((PARENTSIZE - RANGE) <= height && height <= (PARENTSIZE + RANGE)) {
          height -= PARENTSIZE;
          if (height < 0) {
            height = cli.height / -height;
          } else if (height == 0) {
            height = cli.height;
          } else {
            height = height * cli.height / 100;
          }
        } else if ((PARENTSIZEMIN - RANGE) <= height && height <= (PARENTSIZEMIN + RANGE)) {
          height -= PARENTSIZEMIN;
          if (height < 0) {
            height = Math.min(cli.width, cli.height) / -height;
          } else if (height == 0) {
            height = Math.min(cli.width, cli.height);
          } else {
            height = height * Math.min(cli.width, cli.height) / 100;
          }
        } else if ((PARENTSIZEMAX - RANGE) <= height && height <= (PARENTSIZEMAX + RANGE)) {
          height -= PARENTSIZEMAX;
          if (height < 0) {
            height = Math.max(cli.width, cli.height) / -height;
          } else if (height == 0) {
            height = Math.max(cli.width, cli.height);
          } else {
            height = height * Math.max(cli.width, cli.height) / 100;
          }
        } else if ((FONTSIZE - RANGE) <= height && height <= (FONTSIZE + RANGE)) {
          height -= FONTSIZE;
          if (height < 0) {
            height = fmH / -height;
          } else if (height == 0) {
            height = fmH;
          } else {
            height = height * fmH / 100;
          }
        } else if ((DP - RANGE) <= height && height <= (DP + RANGE)) {
          height -= DP;
          height *= Settings.screenDensity;
        }
        // x
        if (x < MAXABSOLUTECOORD) {
        } else if ((AFTER - RANGE) <= x && x <= (AFTER + RANGE) && parent != null) {
          x += parent.lastX + parent.lastW - AFTER;
        } else // guich@450_36: test parent only after testing the relative type
        if ((LEFT - RANGE) <= x && x <= (LEFT + RANGE)) {
          x += cli.x - LEFT;
        } else if ((BEFORE - RANGE) <= x && x <= (BEFORE + RANGE) && parent != null) {
          x += parent.lastX - width - BEFORE;
        } else if ((SAME - RANGE) <= x && x <= (SAME + RANGE) && parent != null) {
          x += parent.lastX - SAME;
        } else if ((RIGHT - RANGE) <= x && x <= (RIGHT + RANGE)) {
          x += cli.x + cli.width - width - RIGHT;
        } else if ((PARENTSIZE - RANGE) <= x && x <= (PARENTSIZE + RANGE)) {
          x -= PARENTSIZE;
          if (x < 0) {
            x = cli.width / -x;
          } else if (x == 0) {
            x = cli.width;
          } else {
            x = x * cli.width / 100;
          }
          x -= width / 2;
        } else if ((PARENTSIZEMIN - RANGE) <= x && x <= (PARENTSIZEMIN + RANGE)) {
          x -= PARENTSIZEMIN;
          if (x < 0) {
            x = cli.width / -x;
          } else if (x == 0) {
            x = cli.width;
          } else {
            x = x * cli.width / 100;
          }
          x -= width;
        } else if ((PARENTSIZEMAX - RANGE) <= x && x <= (PARENTSIZEMAX + RANGE)) {
          x -= PARENTSIZEMAX;
          if (x < 0) {
            x = cli.width / -x;
          } else if (x == 0) {
            x = cli.width;
          } else {
            x = x * cli.width / 100;
          }
        } else if ((CENTER - RANGE) <= x && x <= (CENTER + RANGE)) {
          x += cli.x + ((cli.width - width) >> 1) - CENTER;
        } else if ((CENTER_OF - RANGE) <= x && x <= (CENTER_OF + RANGE)) {
          x += parent.lastX + (parent.lastW - width) / 2 - CENTER_OF;
        } else // guich@tc110_88
        if ((RIGHT_OF - RANGE) <= x && x <= (RIGHT_OF + RANGE)) {
          x += parent.lastX + (parent.lastW - width) - RIGHT_OF; // guich@tc110_97
        }
        // y
        if (y <= MAXABSOLUTECOORD) {
        } else if ((AFTER - RANGE) <= y && y <= (AFTER + RANGE) && parent != null) {
          y += parent.lastY + parent.lastH - AFTER;
        } else // guich@450_36: test parent only after testing the relative type
        if ((BEFORE - RANGE) <= y && y <= (BEFORE + RANGE) && parent != null) {
          y += parent.lastY - height - BEFORE;
        } else if ((SAME - RANGE) <= y && y <= (SAME + RANGE) && parent != null) {
          y += parent.lastY - SAME;
        } else if ((TOP - RANGE) <= y && y <= (TOP + RANGE)) {
          y += cli.y - TOP;
        } else if ((BOTTOM - RANGE) <= y && y <= (BOTTOM + RANGE)) {
          y += cli.y + cli.height - height - BOTTOM;
        } else if ((PARENTSIZE - RANGE) <= y && y <= (PARENTSIZE + RANGE)) {
          y -= PARENTSIZE;
          if (y < 0) {
            y = cli.height / -y;
          } else if (y == 0) {
            y = cli.height;
          } else {
            y = y * cli.height / 100;
          }
          y -= height / 2;
        } else if ((PARENTSIZEMIN - RANGE) <= y && y <= (PARENTSIZEMIN + RANGE)) {
          y -= PARENTSIZEMIN;
          if (y < 0) {
            y = cli.height / -y;
          } else if (y == 0) {
            y = cli.height;
          } else {
            y = y * cli.height / 100;
          }
          y -= height;
        } else if ((PARENTSIZEMAX - RANGE) <= y && y <= (PARENTSIZEMAX + RANGE)) {
          y -= PARENTSIZEMAX;
          if (y < 0) {
            y = cli.height / -y;
          } else if (y == 0) {
            y = cli.height;
          } else {
            y = y * cli.height / 100;
          }
        } else if ((CENTER - RANGE) <= y && y <= (CENTER + RANGE)) {
          y += cli.y + ((cli.height - height) >> 1) - CENTER;
        } else if ((CENTER_OF - RANGE) <= y && y <= (CENTER_OF + RANGE)) {
          y += parent.lastY + (parent.lastH - height) / 2 - CENTER_OF;
        } else // guich@tc110_88
        if ((BOTTOM_OF - RANGE) <= y && y <= (BOTTOM_OF + RANGE)) {
          y += parent.lastY + (parent.lastH - height) - BOTTOM_OF; // guich@tc110_97
        }
        // width that depends on x
        if (width > MAXABSOLUTECOORD) {
          if ((FILL - RANGE) <= width && width <= (FILL + RANGE)) {
            width += cli.width - x + cli.x - FILL;
          } else if ((FIT - RANGE) <= width && width <= (FIT + RANGE) && parent != null) {
            width += lpx - x - FIT;
          }
          tempW = width;
        }
        // height that depends on y
        if (height > MAXABSOLUTECOORD) {
          if ((FILL - RANGE) <= height && height <= (FILL + RANGE)) {
            height += cli.height - y + cli.y - FILL;
          } else if ((FIT - RANGE) <= height && height <= (FIT + RANGE) && parent != null) {
            height += lpy - y - FIT;
          }
        }
      }

      // quick check to see if all bounds were set.
      if (Settings.onJavaSE && Settings.showUIErrors) // guich@450_36: do these checks only if running on desktop
      {
        if (cli.width == 0 || cli.height == 0) {
          boolean zeroIsValid = false;
          for (Control c = parent; c != null && !zeroIsValid; c = c.parent) {
            zeroIsValid = c instanceof AccordionContainer;
          }
          if (!zeroIsValid) { // when a Control is inside a AccordionContainer, it can reach size 0, so we just ignore the exception
            throw new RuntimeException(parent + " must have its bounds set before calling " + this + ".setRect"); // guich@300_28
          }
        } else if (x + y + width + height > RANGE) {
          String error = "";
          if (isOnlyForSize(x) || isOnlyForY(x)) {
            error += "x,";
          }
          if (isOnlyForSize(y) || isOnlyForX(y)) {
            error += "y,";
          }
          if (isOnlyForPos(width)) {
            error += "width,";
          }
          if (isOnlyForPos(height)) {
            error += "height,";
          }

          x = y = 0;
          width = height = 10;

          if (!error.isEmpty()) {
            throw new RuntimeException("You are using constant positions " + error.substring(0, error.length() - 1)
                + " in a wrong place for control " + toString());
          } else {
            throw new RuntimeException(
                "To use AFTER/BEFORE/SAME you must add first the control " + toString() + " to the parent container.");
          }
        } else if (x + y < -RANGE) // guich@300_27
        {
          if (x < -RANGE) {
            throw new RuntimeException("You can't use FILL with BEFORE, CENTER or RIGHT for control " + toString());
          } else {
            throw new RuntimeException("You can't use FILL with BEFORE, CENTER or BOTTOM for control " + toString());
          }
        }
        if (height < 0 && width < 0) {
          throw new RuntimeException(
              "Invalid resulting values in width,height for control " + toString() + ": " + width + "," + height);
        } else if (width < 0) {
          throw new RuntimeException("Invalid resulting values in width for control " + toString() + ": " + width);
        } else if (height < 0) {
          throw new RuntimeException("Invalid resulting values in height for control " + toString() + ": " + height);
        }
      }
    }

    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    tempW = -1;
    if (parent != null) {
      updateTemporary();
    }
    Window.needsPaint = true;
    onBoundsChanged(screenChanged);
    if (asContainer != null) {
      if (!asContainer.started) // guich@340_15
      {
        asContainer.started = true;
        asContainer.initUI();
        asContainer.finishedStart = true;
      }
      asContainer.lastScreenWidth = Settings.screenWidth; // save the last screen resolution so we can be repositioned if a rotation occured at a time that the container was not on screen
    }
  }

  private boolean isOnlyForSize(int k) {
    return ((SCREENSIZEMIN - RANGE) <= k && k <= (SCREENSIZEMIN + RANGE))
        || ((SCREENSIZEMAX - RANGE) <= k && k <= (SCREENSIZEMAX + RANGE))
            | ((PARENTSIZEMIN - RANGE) <= k && k <= (PARENTSIZEMIN + RANGE))
        || ((PARENTSIZEMAX - RANGE) <= k && k <= (PARENTSIZEMAX + RANGE))
        || ((FILL - RANGE) <= k && k <= (FILL + RANGE)) || ((FIT - RANGE) <= k && k <= (FIT + RANGE));
  }

  private boolean isOnlyForPos(int k) {
    return ((AFTER - RANGE) <= k && k <= (AFTER + RANGE)) || ((BEFORE - RANGE) <= k && k <= (BEFORE + RANGE))
        || ((TOP - RANGE) <= k && k <= (TOP + RANGE)) || ((BOTTOM - RANGE) <= k && k <= (BOTTOM + RANGE))
        || ((LEFT - RANGE) <= k && k <= (LEFT + RANGE)) || ((RIGHT - RANGE) <= k && k <= (RIGHT + RANGE))
        || ((CENTER - RANGE) <= k && k <= (CENTER + RANGE)) || ((CENTER_OF - RANGE) <= k && k <= (CENTER_OF + RANGE))
        || ((BOTTOM_OF - RANGE) <= k && k <= (BOTTOM_OF + RANGE))
        || ((RIGHT_OF - RANGE) <= k && k <= (RIGHT_OF + RANGE));
  }

  private boolean isOnlyForX(int k) {
    return ((LEFT - RANGE) <= k && k <= (LEFT + RANGE)) || ((RIGHT - RANGE) <= k && k <= (RIGHT + RANGE))
        || ((RIGHT_OF - RANGE) <= k && k <= (RIGHT_OF + RANGE));
  }

  private boolean isOnlyForY(int k) {
    return ((TOP - RANGE) <= k && k <= (TOP + RANGE)) || ((BOTTOM - RANGE) <= k && k <= (BOTTOM + RANGE))
        || ((BOTTOM_OF - RANGE) <= k && k <= (BOTTOM_OF + RANGE));
  }

  /** Resets the original points that are set by the first setRect, so if you call setRect again, the 
   * old positions are replaced by the new ones. The set positions are used when a rotation occurs.
   * @since TotalCross 1.25
   */
  public void resetSetPositions() {
    setX = SETX_NOT_SET;
  }

  /** Used internally. */
  public void setSet(int x, int y) {
    setX = x;
    setY = y;
  }

  protected void updateTemporary() // guich@tc114_68
  {
    if (parent != null) {
      if (x != WILL_RESIZE) {
        parent.lastX = x; // guich@200b4_100: save last positions
      }
      if (y != WILL_RESIZE) {
        parent.lastY = y;
      }
      if (width != WILL_RESIZE) {
        parent.lastW = width;
      }
      if (height != WILL_RESIZE) {
        parent.lastH = height;
      }
    }
  }

  /** Returns the current size (width,height) of this control */
  public Coord getSize() {
    return new Coord(width, height);
  }

  /** Returns the current position (x,y) of this control */
  public Coord getPos() {
    return new Coord(x, y);
  }

  /** Shows or "hides" this control. Note that it remains attached to its container.
   * Calls repaint. */
  public void setVisible(boolean visible) {
    if (visible != this.visible) {
      this.visible = visible;
      if (!visible) {
        Window w = getParentWindow();
        if (w != null) {
          for (Control c = w._focus; c != null; c = c.parent) {
            if (!c.visible) {
              w.removeFocus();
              break;
            }
          }
        }
      }
      Window.needsPaint = true;
    }
  }

  /** Returns true if this control is visible, false otherwise */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Returns a copy of the control's rectangle, relative to its parent. A control's rectangle
   * defines its location and size.
   */
  public Rect getRect() {
    return new Rect(this.x, this.y, this.width, this.height);
  }

  /** Returns the absolute coordinates of this control relative to the MainWindow. */
  public Rect getAbsoluteRect() // guich@102: changed name from getRelativeRect to getAbsoluteRect.
  {
    Rect r = getRect();
    Control c = parent;
    while (c != null) {
      r.x += c.x;
      r.y += c.y;
      c = c.parent;
    }
    return r;
  }

  /** Returns the control's parent Window or null if there's no parent
   * (eg: the control still not added to any container). If this control is a window, will return itself.
   */
  public Window getParentWindow() {
    if (asWindow != null) {
      return asWindow;
    }
    Container c = parent;
    // guich@200final_17: using again the old algorithm
    while (c != null && c.asWindow == null) {
      c = c.parent;
    }
    return c != null ? (Window) c : null;
  }

  /** Returns the control's parent container. */
  public Container getParent() {
    return parent;
  }

  /** Returns the next child in the parent's list of controls. */
  public Control getNext() {
    return next;
  }

  /** Returns the previous child in the parent's list of controls. */
  public Control getPrev() {
    return prev;
  }

  /**
   * Returns true if the given x and y coordinate in the parent's
   * coordinate system is contained within this control.
   */
  public boolean contains(int x, int y) {
    return this.x <= x && x < (this.x + this.width) && this.y <= y && y < (this.y + this.height);
  }

  /** Marks all controls in the screen for repaint.
   * Important note: when you call repaint, a flag is set indicating that the screen
   * must be repainted; then, the next time a event (a keypress, a timer, a pen event)
   * occurs, the screen is updated. If you call repaint and the control isn't
   * effectively repainted, you can use the Control.repaintNow method.
   * <br><br>
   * If you want to avoid a method call, you can do
   * <pre>
   * Window.needsPaint = true;
   * </pre>
   * @see #repaintNow()
   */
  public static void repaint() {
    Window.needsPaint = true;
  }

  /** Redraws the control immediately. If this control is a Window, the whole window area is
   * marked for repaint (useful if you're removing some controls from a container).
   * This method affects only this control, while the repaint method affects the whole screen.
   * 
   * If Window.enableUpdateScreen is true, the method returns immediately.
   * @since SuperWaba 2.0 beta 4 release 3
   * @see #repaint()
   */
  public void repaintNow() {
    if (!Control.enableUpdateScreen) {
      return;
    }
    Window w = asWindow != null ? asWindow : getParentWindow();
    if (w != null && Window.zStack.indexOf(w, 0) >= 0) // guich@560_12: if we're not visible, this is nonsense
    {
      if (Settings.isOpenGL || !w.isTopMost()) // guich@issue#80: if control is not topmost, must repaint all windows
      {
        Window.needsPaint = true; // make sure the whole area is marked to be repainted
        if (MainWindow.isMainThread()) {
          Window.repaintActiveWindows();
        } else {
          MainWindow.mainWindowInstance.setTimerInterval(1);
          Thread.yield();
        }
      } else if (asWindow != null) // guich@200b4: if this is a Window, paint everything
      {
        Window.needsPaint = true; // make sure the whole area is marked to be repainted
        asWindow._doPaint(); // doPaint already calls updateScreen here
      } else if (transparentBackground) {
        parent.repaintNow(); // guich@tc100: for transparent backgrounds we have to force paint everything
      } else {
        Graphics g = refreshGraphics(gfx, 0, null, 0, 0);
        if (g != null) {
          onPaint(g);
          if (asContainer != null) {
            asContainer.paintChildren();
          }
          safeUpdateScreen();
        }
      }
    }
  }

  /** Sets the given Point to the absolute coordinate relative to the origin Window.
   * @since SuperWaba 5.5
   */
  public void translateFromOrigin(Coord z) // guich@550_31
  {
    z.x = z.y = 0;
    Control c = this;
    while (c != null) {
      if (c.asWindow != null) {
        break;
      }
      z.x += c.x;
      z.y += c.y;
      c = c.parent;
    }
  }

  /**
   * Returns a Graphics object which can be used to draw in the control.
   * This method updates the single Graphics object with the
   * current control font and bounds.
   * It sets a clipping rectangle on the graphics, clipping it against all parent areas.
   */
  public Graphics getGraphics() {
    return refreshGraphics(gfx, 0, null, 0, 0);
  }

  Graphics refreshGraphics(Graphics g, int expand, Control topParent, int tx0, int ty0) {
    if (asWindow == null && parent == null) {
      return null;
    }
    int sw = this.width;
    int sh = this.height;
    int sx = this.x, sy = this.y, cx, cy, delta, tx = sx, ty = sy;
    if (this != topParent) {
      for (Container c = parent; c != topParent; c = c.parent) {
        cx = c.x;
        cy = c.y;
        tx += cx;
        ty += cy;
        sx += cx;
        sy += cy;

        // before?
        delta = sx - cx;
        if (delta < 0) {
          sw += delta;
          sx = cx;
        }
        delta = sy - cy;
        if (delta < 0) {
          sh += delta;
          sy = cy;
        }

        // after?
        delta = (sx + sw) - (cx + c.width);
        if (delta > 0) {
          sw -= delta;
        }
        delta = (sy + sh) - (cy + c.height);
        if (delta > 0) {
          sh -= delta;
        }
      }
    }
    g.refresh(sx + tx0 - expand, sy - expand + ty0, sw + expand + expand, sh + expand + expand, tx + tx0, ty + ty0,
        font);
    return g;
  }

  /**
   * Posts an event. The event pass will be posted to this control
   * and all the parent controls of this control (all the containers
   * this control is within).
   * @see totalcross.ui.event.Event
   */
  public <H extends EventHandler> void postEvent(Event<H> event) {
    // we can now go back to highlighting mode, the control had being pressed
    if (Settings.keyboardFocusTraversable && event.type == ControlEvent.PRESSED && !(event.target instanceof Edit)) {
      (asContainer != null ? asContainer : parent.asContainer).setHighlighting();
    }

    // don't dispatch events when disabled except TIMER events or (fingertouch and pen events) 
    if ((!isEnabled() && (!Settings.fingerTouch || event.type == PenEvent.PEN_DOWN))
        || (!eventsEnabled && event.type != TimerEvent.TRIGGERED)) {
      return;
    }

    boolean dragTargetCalled = false;
    if (dragTarget != null) // improve drag performance by sending the event directly to the drag handler control
    {
      if (event.type == PenEvent.PEN_DOWN) {
        dragTarget = null;
      } else if (event.type == PenEvent.PEN_DRAG || event.type == PenEvent.PEN_UP) {
        dragTarget._onEvent(event);
        dragTargetCalled = true;
      }
    }

    if (!event.consumed) {
      boolean eventTargetCalled = false;

      for (Control c = this; c != null; c = c.parent) {
        if (c == event.target) {
          eventTargetCalled = true;
        }
        if (!dragTargetCalled || c != dragTarget) {
          Control cp = c.parent;
          c._onEvent(event);
          if (event.consumed && event.type == PenEvent.PEN_DRAG) {
            dragTarget = c;
          }
          if (event.consumed || cp != c.parent) {
            break;
          }
        }
      }

      if (!eventTargetCalled && event.target instanceof Control) // guich@tc110_52: call any listeners of the target control - guich@tc112_3: if not yet called
      {
        Control target = (Control) event.target;
        if (target.listeners.size() > 0) {
          target.callEventListeners(event);
        }
      }
    }

    if (event.type == PenEvent.PEN_UP) {
      dragTarget = null;
    }
    event.consumed = false; // set to false again bc some controls reuse event objects
  }

  /** Sets if this control can or not accept events.
   * It changes the appearance of many controls to indicate they are disabled.
   */
  public void setEnabled(boolean enabled) {
    internalSetEnabled(enabled, true);
  }

  /** For internal use only. Used by derived controls to set the enabled flag. */
  public boolean internalSetEnabled(boolean enabled, boolean post) {
    if (enabled != this.enabled) {
      this.enabled = enabled;
      onColorsChanged(false);
      if (post) {
        post();
      }
      Window.needsPaint = true; // now the controls have different l&f for disabled states
      return true;
    }
    return false;
  }

  /** Posts the enable state change event. */
  public void post() {
    esce.update(this);
    postEvent(esce);
  }

  /** Returns if this control can or not accept events */
  public boolean isEnabled() {
    return keepEnabled || (this.enabled && !keepDisabled);
  }

  /**
   * Called to process key, pen, control and other posted events.
   * @param event the event to process
   * @see totalcross.ui.event.Event
   * @see totalcross.ui.event.KeyEvent
   * @see totalcross.ui.event.PenEvent
   */
  public <H extends EventHandler> void onEvent(Event<H> event) {
  }

  /**
   * Called to draw the control. When this method is called, the graphics
   * object passed has been translated into the coordinate system of the
   * control and the area behind the control has
   * already been painted.
   * @param g the graphics object for drawing
   * @see totalcross.ui.gfx.Graphics
   */
  public void onPaint(Graphics g) {
  }

  /** Called after a setRect.
   * @param screenChanged If the bounds were changed due to a screen change (rotation, collapse)
   */
  protected void onBoundsChanged(boolean screenChanged) {
  }

  /** Called after a setEnabled, setForeColor and setBackColor and when a control has
   * been added to a Container. If colorsChanged
   * is true, it was called from setForeColor/setBackColor/Container.add; otherwise, it was
   * called from setEnabled
   */
  protected void onColorsChanged(boolean colorsChanged) // guich@200b4_152
  {
  }

  /** Called after the window has finished a paint. Only called to the focused control and the parent's window. */
  protected void onWindowPaintFinished() {
  }

  /** Called after a setFont */
  protected void onFontChanged() // guich@200b4_153
  {
  }

  /** Set the background and foreground colors at once.
   * Calling this method is faster than calling setBackColor and setForeColor separately.
   */
  public void setBackForeColors(int back, int fore) // guich@200b4_170
  {
    this.backColor = back;
    this.foreColor = fore;
    onColorsChanged(true);
  }

  /** Set the foreground color of this control.
   @since SuperWaba 2.0 */
  public void setForeColor(int c) {
    this.foreColor = c;
    onColorsChanged(true);
  }

  /** Set the background color of this control.
   @since SuperWaba 2.0 */
  public void setBackColor(int c) {
    this.backColor = c;
    onColorsChanged(true);
  }

  /** Get the desired foreground color of this control.
   @since SuperWaba 2.0 */
  public int getForeColor() {
    return isEnabled() ? foreColor : uiMaterial ? Color.getGray(foreColor) : Color.brighter(foreColor);
  }

  /** Get the desired background color of this control.
   @since SuperWaba 2.0 */
  public int getBackColor() {
    // note: if were in a white back color, return the color without darking
    return (isEnabled() || parent == null) ? backColor : Color.darker(backColor);
  }

  /** Return true if the parent of this Control is added to somewhere.
   * Some containers, like the TabPanel, has n child containers, but only one is added
   * at a time. With this method, you can discover if your container is the one being shown.
   */
  public boolean isDisplayed() {
    Control c = this;
    while (c.asWindow == null) {
      c = c.parent;
      if (c == null) {
        return false; // not added yet
      }
    }
    return c.asWindow.popped || (c instanceof MainWindow);
  }

  /** Sets the focus to this control. Note that in penless devices its also needed
   * to set <code>isHighlighting = false</code>. */
  public void requestFocus() {
    Window w = getParentWindow();
    if (w != null) {
      w.setFocus(this);
      // does not work - test on UIGadgets for example - isHighlighting = false; // guich@570_39: if the user decided to place the focus here, we should disable highlighting traversals
    }
  }

  /** Sets this control to be focusless. If this control is a container, sets
   * all its children to be focusless too.
   * A focusless control can receive and dispatch events, but cannot receive focus.
   * Here's an example of how to use it to create a keypad:
   * <pre>
         // class fields
         private PushButtonGroup numericPad;
         private KeyEvent ke = new KeyEvent();
  
         // in the initUI method:
         String []numerics = {"1","2","3","4","5","6","7","8","9","0",".","-"};
         add(numericPad=new PushButtonGroup(numerics, false, -1, -1, 6, 4, true,
                                             PushButtonGroup.BUTTON), RIGHT-2, TOP+2);
         numericPad.setFocusLess(true);
  
         // in the onEvent method
         Control focus;
         if (e.target == numericPad && (focus=getParentWindow().getFocus()) instanceof Edit)
         {
            String s = numericPad.getSelectedCaption();
            if (s != null)
            {
               ke.key = s.charAt(0);
               ke.target = focus;
               focus.onEvent(ke);
            }
         }
   * </pre>
   */
  public void setFocusLess(boolean on) {
    Container p = parent;
    if (p != null) {
      if (on) {
        p.tabOrder.removeElement(this);
      } else {
        p.tabOrder.addElement(this);
      }
    }

    this.focusLess = on;
    if (asContainer != null) {
      for (Control child = asContainer.children; child != null; child = child.next) {
        if (child.asContainer != null) {
          child.setFocusLess(on);
        } else {
          child.focusLess = on;
        }
      }
    }
  }

  private static boolean uiStyleAlreadyChanged;

  /** Internal use Only. */
  public static void resetStyle() {
	  uiStyleAlreadyChanged = false;
  }
  
  /** Internal use only */
  public static void uiStyleChanged() {
    if (!uiStyleAlreadyChanged) {
      uiFlat = Settings.uiStyle == Settings.Flat;
      uiMaterial = Settings.uiStyle == Settings.Material;
      uiAndroid = Settings.uiStyle == Settings.Android || Settings.uiStyle == Settings.Holo || uiMaterial;
      uiVista = Settings.uiStyle == Settings.Vista || uiAndroid;
      uiHolo = Settings.uiStyle == Settings.Holo || uiAndroid;
      uiStyleAlreadyChanged = true;
    } else {
      throw new RuntimeException("The user interface style can be changed only once, in the MainWindow's constructor.");
    }
  }

  /** Returns the next/previous control that can be highlighted */
  private Control getNextHighlighted(Container p, boolean forward) {
    Vector v = p.tabOrder;
    int idx = v.indexOf(this);
    int last = v.size() - 1;
    if (last == -1) {
      return null;
    }

    if (p == this) {
      idx = forward ? -1 : last + 1; // return (Control)v.items[forward?0:last]; // return the first/last control - guich@573_22: can't just go to first or last - they may be disabled!
    } else if (idx == -1) {
      return null;
    }
    int limit = forward ? last : 0;
    int inc = forward ? 1 : -1;
    while (true) {
      if (idx == limit) {
        return null;
      }
      idx += inc;
      Control c = (Control) v.items[idx];
      if (c.visible && c.isEnabled() && !c.focusLess) {
        if (c.focusTraversable) {
          return c;
        } else if (c.asContainer != null) // kmeehl@tc100: look through child containers for the next focusable control
        {
          c = c.getNextHighlighted(c.asContainer, forward);
          if (c != null) {
            return c;
          }
        }
      }
    }
  }

  /** Transfers the focus to the next or previous control.
   * @since SuperWaba 5.5
   */
  public void changeHighlighted(Container p, boolean forward) {
    Control c = getNextHighlighted(p, forward); // allow the control to find the next control to highlight
    if (c != null) {
      Window w = getParentWindow();
      if (w != null) {
        w.setHighlighted(c);
      }
    } else if (parent != null) {
      if (this == p) {
        changeHighlighted(parent, forward); // look in parent for the next focusable control
      } else {
        parent.changeHighlighted(parent.parent != null ? parent.parent : parent, forward); // return to the parent of the parent and continues - p.p!=null: if our parent is a window (E.G. UIGadgets' scrollbar), his parent is null; so we restart from the window
      }
    } else if (p != this.asWindow) {
      changeHighlighted(this.asWindow, forward); // we're the window - restart everything
    }
  }

  /** Placeholder for the clear method. The class that inherits control is responsible for cleaning it up. */
  public void clear() {
  }

  /**
   * This method causes the immediate screen update. The screen
   * is saved in a buffer and, when this method is called,
   * the buffer is transfered to the screen, using the 
   * nextTransitionEffect set.
   * NOTE: for a thread-safe version, use safeUpdateScreen
   * @see #safeUpdateScreen() 
   * @see Container#nextTransitionEffect
   * @since SuperWaba 5.0
   */
  @ReplacedByNativeOnDeploy
  public static void updateScreen() {
    if (enableUpdateScreen) {
      totalcross.Launcher.instance.updateScreen();
      Graphics.needsUpdate = false;
    }
  }

  /** This method causes the screen's update. If called at the main thread,
   * the screen is updated immediatly. If not, the updated is schedulled to occur as
   * soon as possible.
   * @see #updateScreen()
   * @since TotalCross 3.1
   */
  public static void safeUpdateScreen() {
    if (MainWindow.isMainThread()) {
      updateScreen();
    } else if (!callingUpdScr) {
      callingUpdScr = true;
      MainWindow.getMainWindow().runOnMainThread(new Runnable() {
        @Override
        public void run() {
          updateScreen();
          Thread.yield();
          callingUpdScr = false;
        }
      });
    }
  }

  /** Returns the control's width. */
  @Override
  public int getWidth() {
    return width;
  }

  /** Returns the control's height. */
  @Override
  public int getHeight() {
    return height;
  }

  /** Returns the control's x position. */
  @Override
  public int getX() {
    return x;
  }

  /** Returns the control's y position. */
  @Override
  public int getY() {
    return y;
  }

  /** Returns x+width-1
   * @since TotalCross 1.14
   */
  public int getX2() {
    return x + width - 1;
  }

  /** Returns y+height-1
   * @since TotalCross 1.14
   */
  public int getY2() {
    return y + height - 1;
  }

  /** Repositions this control, and dives into other controls if this is a container and recursive is true. */
  protected void reposition(boolean recursive) {
    if (setX != SETX_NOT_SET) // bounds already set?
    {
      if (repositionAllowed) {
        Font current = this.font;
        font = setFont;
        fm = font.fm;
        fmH = font.fm.height;
        setRect(setX, setY, setW, setH, setRel, true);
        font = current;
        fm = font.fm;
        fmH = font.fm.height;
        refreshGraphics(gfx, 0, null, 0, 0);
      }
      if (recursive) {
        repositionChildren();
      }
    }
    if (asContainer != null) {
      asContainer.lastScreenWidth = Settings.screenWidth; // save the last screen resolution so we can be repositioned if a rotation occured at a time that the container was not on screen
    }
  }

  /** Repositions the children controls. This is usually called from reposition, but you can call by yourself if you are changing this control's position and size by yourself (for example during rotaton) */
  protected void repositionChildren() {
    if (asContainer != null) {
      asContainer.lastX = -999999;
      asContainer.lastY = asContainer.lastW = asContainer.lastH = 0;
      for (Control child = asContainer.children; child != null; child = child.next) {
        child.reposition();
      }
    }
  }

  /** Reposition this control, calling again setRect with the original parameters. */
  public void reposition() {
    reposition(true);
  }

  /** Calls the event listeners and the onEvent method for this control. */
  public <H extends EventHandler >void _onEvent(Event<H> e) {
    if (!e.consumed && onEventFirst) {
      onEvent(e);
    }
    if (!e.consumed && listeners.size() > 0 && (callListenersOnAllTargets || e.target == this)) {
      callEventListeners(e);
    }
    if (!e.consumed && !onEventFirst) {
      onEvent(e);
    }
  }
  
  protected <H extends EventHandler> void addHandler(int type, H handler) {
    Listener l = new Listener(this, type, handler);
    if (listeners.indexOf(l) == -1) {
      listeners.add(l);
    }
  }

  /** Removes the given listener from the list of listeners of this control. 
   */
  protected <H extends EventHandler> boolean removeHandler(int type, H handler) {
    return listeners.remove(new Listener(this, type, handler));
  }

  /** Adds a listener for Pen events.
   * @see totalcross.ui.event.PenListener
   */
  public void addPenListener(PenListener listener) {
    addHandler(Listener.PEN, listener);
  }

  /** Adds a listener for MultiTouch events.
   * @see totalcross.ui.event.MultiTouchListener
   */
  public void addMultiTouchListener(MultiTouchListener listener) {
    addHandler(Listener.MULTITOUCH, listener);
  }

  /** Adds a listener for mouse events.
   * @see totalcross.ui.event.MouseListener
   */
  public void addMouseListener(MouseListener listener) {
    addHandler(Listener.MOUSE, listener);
  }

  /** Adds a listener for Window events.
   * @see totalcross.ui.event.WindowListener
   */
  public void addWindowListener(WindowListener listener) {
    addHandler(Listener.WINDOW, listener);
  }

  /** Adds a listener for Grid events.
   * @see totalcross.ui.event.GridListener
   */
  public void addGridListener(GridListener listener) {
    addHandler(Listener.GRID, listener);
  }

  /** Adds a listener for ListContainer events.
   * @see totalcross.ui.event.ListContainerListener
   */
  public void addListContainerListener(ListContainerListener listener) {
    addHandler(Listener.LISTCONTAINER, listener);
  }

  /** Adds a listener for Focus events.
   * @see totalcross.ui.event.FocusListener
   */
  public void addFocusListener(FocusListener listener) {
    addHandler(Listener.FOCUS, listener);
  }
  
  public void addFontChangeHandler(FontChangeHandler listener) {
    addHandler(0, listener);
  }
  
  public void addSizeChangeHandler(SizeChangeHandler listener) {
	    addHandler(0, listener);
	  }

  /** Adds a listener for Press events.
   * @see totalcross.ui.event.PressListener
   */
  public void addPressListener(PressListener listener) {
    addHandler(Listener.PRESS, listener);
  }

  /** Adds a listener for PushNotification events.
   * @see totalcross.ui.event.PushNotificationEvent
   */
  public void addPushNotificationListener(PushNotificationListener listener) {
    addHandler(Listener.PUSHNOTIFICATION, listener);
  }

  /** Adds a listener for Timer events.
   * @see totalcross.ui.event.TimerListener
   */
  public void addTimerListener(TimerListener listener) {
    addHandler(Listener.TIMER, listener);
  }

  /** Adds a listener for Key events.
   * @see totalcross.ui.event.KeyListener
   */
  public void addKeyListener(KeyListener listener) {
    addHandler(Listener.KEY, listener);
  }

  /** Adds a listener for Highlight events.
   * @see totalcross.ui.event.HighlightListener
   */
  public void addHighlightListener(HighlightListener listener) {
    addHandler(Listener.HIGHLIGHT, listener);
  }

  /** Adds a listener for enabled state changes.
   */
  public void addEnabledStateListener(EnabledStateChangeListener listener) {
    addHandler(Listener.ENABLED, listener);
  }
  
  public <T> void addValueChangeHandler(ValueChangeHandler<T> listener) {
    addHandler(0, listener);
  }

  /** Removes a listener for enabled state changes.
   * @since TotalCross 1.67
   */
  public void removeEnabledStateListener(EnabledStateChangeListener listener) {
    removeHandler(Listener.ENABLED, listener);
  }

  /** Removes a listener for MultiTouch events.
   * @see totalcross.ui.event.MultiTouchListener
   * @since TotalCross 1.22
   */
  public void removeMultiTouchListener(MultiTouchListener listener) {
    removeHandler(Listener.MULTITOUCH, listener);
  }

  /** Removes a listener for Pen events.
   * @see totalcross.ui.event.PenListener
   * @since TotalCross 1.22
   */
  public void removePenListener(PenListener listener) {
    removeHandler(Listener.PEN, listener);
  }

  /** Removes a listener for PushNotification events.
   * @see totalcross.ui.event.PushNotificationEvent
   */
  public void removePushNotificationListener(PushNotificationListener listener) {
    removeHandler(Listener.PUSHNOTIFICATION, listener);
  }

  /** Removes a listener for mouse events.
   * @see totalcross.ui.event.MouseListener
   * @since TotalCross 1.22
   */
  public void removeMouseListener(MouseListener listener) {
    removeHandler(Listener.MOUSE, listener);
  }

  /** Removes a listener for Window events.
   * @see totalcross.ui.event.WindowListener
   * @since TotalCross 1.22
   */
  public void removeWindowListener(WindowListener listener) {
    removeHandler(Listener.WINDOW, listener);
  }

  /** Removes a listener for Grid events.
   * @see totalcross.ui.event.GridListener
   * @since TotalCross 1.22
   */
  public void removeGridListener(GridListener listener) {
    removeHandler(Listener.GRID, listener);
  }

  /** Removes a listener for ListContainer events.
   * @see totalcross.ui.event.ListContainerListener
   * @since TotalCross 1.22
   */
  public void removeListContainerListener(ListContainerListener listener) {
    removeHandler(Listener.LISTCONTAINER, listener);
  }

  /** Removes a listener for Focus events.
   * @see totalcross.ui.event.FocusListener
   * @since TotalCross 1.22
   */
  public void removeFocusListener(FocusListener listener) {
    removeHandler(Listener.FOCUS, listener);
  }

  /** Removes a listener for Press events.
   * @see totalcross.ui.event.PressListener
   * @since TotalCross 1.22
   */
  public void removePressListener(PressListener listener) {
    removeHandler(Listener.PRESS, listener);
  }

  /** Removes a listener for Timer events.
   * @see totalcross.ui.event.TimerListener
   * @since TotalCross 1.22
   */
  public void removeTimerListener(TimerListener listener) {
    removeHandler(Listener.TIMER, listener);
  }

  /** Removes a listener for Key events.
   * @see totalcross.ui.event.KeyListener
   * @since TotalCross 1.22
   */
  public void removeKeyListener(KeyListener listener) {
    removeHandler(Listener.KEY, listener);
  }

  /** Removes a listener for Highlight events.
   * @see totalcross.ui.event.HighlightListener
   * @since TotalCross 1.22
   */
  public void removeHighlightListener(HighlightListener listener) {
    removeHandler(Listener.HIGHLIGHT, listener);
  }
  
  private <H extends EventHandler> void callEventListeners(Event<H> e) {
    // although this code is not much eficient, the number of listeners for a single control will be only one, most of the times.
    for (int i = 0; listeners != null && i < listeners.size() && !e.consumed; i++) {
      try {
        Listener l = listeners.get(i);
        if (e.target == l.target || (callListenersOnAllTargets
            && (e instanceof ListContainerEvent || e instanceof KeyEvent || e instanceof PenEvent))) {
          if (e instanceof ValueChangeEvent && l.listener instanceof ValueChangeHandler) {
            ((ValueChangeEvent) e).dispatch((ValueChangeHandler) l.listener);
            continue;
          } else if (e instanceof FontChangeEvent && l.listener instanceof FontChangeHandler) {
            ((FontChangeEvent) e).dispatch((FontChangeHandler) l.listener);
            continue;
          } else if (e instanceof SizeChangeEvent && l.listener instanceof SizeChangeHandler) {
              ((SizeChangeEvent) e).dispatch((SizeChangeHandler) l.listener);
              continue;
            }
          switch (e.type) {
          case MouseEvent.MOUSE_MOVE:
            if (l.type == Listener.MOUSE) {
              ((MouseListener) l.listener).mouseMove((MouseEvent) e);
            }
            break;
          case MouseEvent.MOUSE_IN:
            if (l.type == Listener.MOUSE) {
              ((MouseListener) l.listener).mouseIn((MouseEvent) e);
            }
            break;
          case MouseEvent.MOUSE_OUT:
            if (l.type == Listener.MOUSE) {
              ((MouseListener) l.listener).mouseOut((MouseEvent) e);
            }
            break;
          case MouseEvent.MOUSE_WHEEL:
            if (l.type == Listener.MOUSE) {
              ((MouseListener) l.listener).mouseWheel((MouseEvent) e);
            }
            break;
          case MultiTouchEvent.SCALE:
            if (l.type == Listener.MULTITOUCH) {
              ((MultiTouchListener) l.listener).scale((MultiTouchEvent) e);
            }
            break;
          case PenEvent.PEN_DOWN:
            if (l.type == Listener.PEN) {
              ((PenListener) l.listener).penDown((PenEvent) e);
            }
            break;
          case PenEvent.PEN_UP:
            if (l.type == Listener.PEN) {
              ((PenListener) l.listener).penUp((PenEvent) e);
            }
            break;
          case PenEvent.PEN_DRAG:
            if (l.type == Listener.PEN) {
              ((PenListener) l.listener).penDrag((DragEvent) e);
            }
            break;
          case PenEvent.PEN_DRAG_START:
            if (l.type == Listener.PEN) {
              ((PenListener) l.listener).penDragStart((DragEvent) e);
            }
            break;
          case PenEvent.PEN_DRAG_END:
            if (l.type == Listener.PEN) {
              ((PenListener) l.listener).penDragEnd((DragEvent) e);
            }
            break;
          case ControlEvent.PRESSED:
            if (l.type == Listener.PRESS) {
              ((PressListener) l.listener).controlPressed((ControlEvent) e);
            }
            break;
          case ControlEvent.FOCUS_IN:
            if (l.type == Listener.FOCUS) {
              ((FocusListener) l.listener).focusIn((ControlEvent) e);
            }
            break;
          case ControlEvent.FOCUS_OUT:
            if (l.type == Listener.FOCUS) {
              ((FocusListener) l.listener).focusOut((ControlEvent) e);
            }
            break;
          case ControlEvent.HIGHLIGHT_IN:
            if (l.type == Listener.HIGHLIGHT) {
              ((HighlightListener) l.listener).highlightIn((ControlEvent) e);
            }
            break;
          case ControlEvent.HIGHLIGHT_OUT:
            if (l.type == Listener.HIGHLIGHT) {
              ((HighlightListener) l.listener).highlightOut((ControlEvent) e);
            }
            break;
          case ControlEvent.WINDOW_CLOSED:
            if (l.type == Listener.WINDOW) {
              ((WindowListener) l.listener).windowClosed((ControlEvent) e);
            }
            break;
          case GridEvent.SELECTED_EVENT:
            if (l.type == Listener.GRID) {
              ((GridListener) l.listener).gridSelected((GridEvent) e);
            }
            break;
          case GridEvent.CHECK_CHANGED_EVENT:
            if (l.type == Listener.GRID) {
              ((GridListener) l.listener).gridCheckChanged((GridEvent) e);
            }
            break;
          case GridEvent.TEXT_CHANGED_EVENT:
            if (l.type == Listener.GRID) {
              ((GridListener) l.listener).gridTextChanged((GridEvent) e);
            }
            break;
          case TimerEvent.TRIGGERED:
            if (l.type == Listener.TIMER) {
              ((TimerListener) l.listener).timerTriggered((TimerEvent) e);
            }
            break;
          case KeyEvent.KEY_PRESS:
            if (l.type == Listener.KEY) {
              ((KeyListener) l.listener).keyPressed((KeyEvent) e);
            }
            break;
          case KeyEvent.ACTION_KEY_PRESS:
            if (l.type == Listener.KEY) {
              ((KeyListener) l.listener).actionkeyPressed((KeyEvent) e);
            }
            break;
          case KeyEvent.SPECIAL_KEY_PRESS:
            if (l.type == Listener.KEY) {
              ((KeyListener) l.listener).specialkeyPressed((KeyEvent) e);
            }
            break;
          case PushNotificationEvent.MESSAGE_RECEIVED:
            if (l.type == Listener.PUSHNOTIFICATION) {
              ((PushNotificationListener) l.listener).messageReceived((PushNotificationEvent) e);
            }
            break;
          case PushNotificationEvent.TOKEN_RECEIVED:
            if (l.type == Listener.PUSHNOTIFICATION) {
              ((PushNotificationListener) l.listener).tokenReceived((PushNotificationEvent) e);
            }
            break;
          case ListContainerEvent.ITEM_SELECTED_EVENT:
            if (l.type == Listener.LISTCONTAINER) {
              ((ListContainerListener) l.listener).itemSelected((ListContainerEvent) e);
            }
            break;
          case ListContainerEvent.LEFT_IMAGE_CLICKED_EVENT:
            if (l.type == Listener.LISTCONTAINER) {
              ((ListContainerListener) l.listener).leftImageClicked((ListContainerEvent) e);
            }
            break;
          case ListContainerEvent.RIGHT_IMAGE_CLICKED_EVENT:
            if (l.type == Listener.LISTCONTAINER) {
              ((ListContainerListener) l.listener).rightImageClicked((ListContainerEvent) e);
            }
            break;
          case EnabledStateChangeEvent.ENABLED_STATE_CHANGE:
            if (l.type == Listener.ENABLED) {
              ((EnabledStateChangeListener) l.listener).enabledStateChange((EnabledStateChangeEvent) e);
            }
            break;
          }
        }
      } catch (ClassCastException ee) // prevent totalcross.ui.event.PenEvent is not compatible with totalcross.ui.event.DragEvent
      {
        if (Settings.onJavaSE) {
          throw ee;
        }
      }
    }
  }

  /**
   * Used by the main event loop to give the currently focused control an opportunity to act directly on
   * the KeyEvent.
   * @param ke The KeyEvent to be processed
   * @return The control that should get focus as a result of this KeyEvent. Null if this control did not
   * handle the KeyEvent.
   * @see totalcross.sys.Settings#geographicalFocus
   */
  public Control handleGeographicalFocusChangeKeys(KeyEvent ke) // kmeehl@tc100
  {
    return null;
  }

  /** Returns the event listeners array.
   * Note that each element is an instance of Control.Listener.
   * @see Listener 
   */
  public List<Listener> getEventListeners() {
    return listeners;
  }

  /** Returns true of this control is visible and inside these bounds
   * @since TotalCross 1.15
   */
  public boolean isVisibleAndInside(int x0, int y0, int xf, int yf) // guich@tc115_40
  {
    return this.visible && this.y < yf && (this.y + this.height) > y0 && this.x < xf && (this.x + this.width) > x0; // guich@200: ignore hidden controls - note: a window added to a container may not be painted correctly
  }

  boolean isVisibleAndInside(int y0, int yf) // guich@tc115_40
  {
    return this.visible && this.y < yf && (this.y + this.height) > y0; // guich@200: ignore hidden controls - note: a window added to a container may not be painted correctly
  }

  public int getGap(int gap) {
    return Settings.uiAdjustmentsBasedOnFontHeight && uiAdjustmentsBasedOnFontHeightIsSupported ? gap * fmH / 100 : gap;
  }

  /**
   * Send this control to the top of the parent's.
   * @since TotalCross 1.3
   */
  public void bringToFront() {
    if (parent != null && parent.tail != this) {
      if (parent.children == this) {
        parent.children = this.next;
      }
      if (this.prev != null) {
        this.prev.next = this.next;
      }
      if (this.next != null) {
        this.next.prev = this.prev;
      }
      this.prev = parent.tail;
      this.next = null;
      parent.tail.next = this;
      parent.tail = this;
      Window.needsPaint = true;
    }
  }

  /**
   * Send this control to the last place of the parent's.
   * @since TotalCross 1.3
   */
  public void sendToBack() {
    if (parent != null && parent.children != this) {
      if (parent.tail == this) {
        parent.tail = this.prev;
      }
      if (this.prev != null) {
        this.prev.next = this.next;
      }
      if (this.next != null) {
        this.next.prev = this.prev;
      }

      this.next = parent.children;
      this.prev = null;

      parent.children.prev = this;
      parent.children = this;
      Window.needsPaint = true;
    }
  }

  /** Returns true if the parent of this control is a Scrollable and had scrolled since the last
   * pen down.
   * 
   * A scroll occurs before a flick is started.
   * @since TotalCross 1.3
   */
  public boolean hadParentScrolled() {
    for (Container c = parent; c != null; c = c.parent) {
      if (c instanceof Scrollable && ((Scrollable) c).wasScrolled()) {
        return true;
      }
    }
    return false;
  }

  /** Returns true if this is a MultiEdit or an Edit that has a standard keyboard.
   */
  protected boolean willOpenKeyboard() {
    return false;
  }

  /** Returns if this event should be handled as an action. */
  protected <H extends EventHandler> boolean isActionEvent(Event<H> event) {
    return (Settings.fingerTouch && event.type == PenEvent.PEN_UP && !hadParentScrolled())
        || (!Settings.fingerTouch && event.type == PenEvent.PEN_DOWN);
  }

  /** Returns true if this control is added to the given container at some higher level. 
   * @since TotalCross 1.53
   */
  public boolean isChildOf(Container p) {
    for (Control c = this; c != null; c = c.parent) {
      if (c == p) {
        return true;
      }
    }
    return false;
  }

  /** Returns true of the parent window is the top most one. 
   * @since TotalCross 1.66
   */
  public boolean isTopMost() {
    Window w = getParentWindow();
    return w != null && w == Window.topMost;
  }

  /** Returns true if this control is obscured by the topmost window. 
   * Note: parentWindow is retrieved with getParentWindow.
   * @since TotalCross 2.1
   */
  public boolean isObscured(Window parentWindow) {
    if (parentWindow == Window.topMost) {
      return false;
    }
    int cx1 = Window.topMost.x, cy1 = Window.topMost.y, cx2 = cx1 + Window.topMost.width,
        cy2 = cy1 + Window.topMost.height;
    int x1 = this.x, y1 = this.y, x2, y2;
    for (Control c = parent; c != null; c = c.parent) {
      x1 += c.x;
      y1 += c.y;
    }
    x2 = x1 + this.width;
    y2 = y1 + this.height;
    return x1 >= cx1 && x2 < cx2 && y1 >= cy1 && y2 < cy2;
  }

  /** Called by code that runs on threads to safely repaint now.
   * @since TotalCross 3.1
   */
  protected void safeRepaintNow() {
    if (Settings.isOpenGL || MainWindow.isMainThread()) {
      repaintNow();
    } else if (!callingRepNow) {
      Window.needsPaint = true;
      callingRepNow = true;
      MainWindow.getMainWindow().runOnMainThread(new Runnable() {
        @Override
        public void run() {
          repaintNow();
          callingRepNow = false;
        }
      });
    }
  }

  // for internal use only. Used by Tree
  public void intXYWH(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    if (w != 0) {
      this.width = w;
    }
    if (h != 0) {
      this.height = h;
    }
  }

  /** Make this control a translucent one by setting the desired shape instead of NONE. 
   * Calling this resets the backColor to BLACK, foreColor to WHITE, and the textShadowColor to 0x444444, but you may change that value later.
   * You can also change the translucentAlpha value.
   * Note that a translucent button does not have a visual DISABLED state.
   * It requires the Android or Holo user interface styles.
   */
  public void setTranslucent(TranslucentShape shape) {
    translucentShape = shape;
    transparentBackground = true;
    backColor = Color.BLACK;
    foreColor = Color.WHITE;
    textShadowColor = 0x444444;
    alphaValue = 0x80;
    if (doEffect && effect != null) {
      effect.enabled = translucentShape == TranslucentShape.NONE || translucentShape == TranslucentShape.RECT;
    }
  }

  private Image transback;

  public boolean drawTranslucentBackground(Graphics g, int alphaValue) {
    if (translucentShape != TranslucentShape.NONE) {
      if (transback == null || transback.getWidth() != width || transback.getHeight() != height) {
        try {
          transback = new Image(width, height);
          Graphics gg = transback.getGraphics();
          gg.backColor = backColor;
          switch (translucentShape) {
          case NONE:
            return false;
          case RECT:
            gg.fillRect(0, 0, width, height);
            break;
          case LESS_ROUND:
            gg.fillRoundRect(0, 0, width, height, height / 8);
            break;
          case ROUND:
            gg.fillRoundRect(0, 0, width, height, height / 4);
            break;
          case CIRCLE:
            gg.fillRoundRect((width - height) / 2, 0, height, height, height / 2);
            break;
          }
        } catch (Throwable t) {
        }
      }
      if (transback != null) {
        transback.alphaMask = alphaValue;
        g.drawImage(transback, 0, 0);
      }
      return true;
    }
    return false;
  }

  public int getEffectW() {
    return this.width;
  }

  public int getEffectH() {
    return this.height;
  }

  public int getEffectX() {
    return 0;
  }

  public int getEffectY() {
    return 0;
  }

  /** Returns true if this control has focus */
  public boolean hasFocus() {
    Window w = getParentWindow();
    return w != null && w.getFocus() == this;
  }
  
  /**Sets to true to make it control float on a ScrollContainer.*/
  public void setFloating(boolean floating) {
	  this.floating = floating;
  }
  
  /**Sets to true to make it control float on a ScrollContainer.*/
  public boolean isFloating() {
	  return floating;
  }
  
  /** Returns true if this Cotrol should do it's effect.*/
  public boolean getDoEffect() {
	return doEffect;
  }
  
  /** Set to false to make this control not to do it's effect.*/
  public void setDoEffect(boolean doEffect) {
	this.doEffect = doEffect;
  }
  
}
