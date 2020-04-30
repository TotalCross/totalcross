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

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Vm;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.ElementNotFoundException;
import totalcross.util.Vector;

/**
 * Container is a control that contains child controls.
 */

public class Container extends Control {
  /** The children of the container. */
  protected Control children;
  /** The tail of the children list. */
  protected Control tail;
  /** The type of border of this Container */
  byte borderStyle = BORDER_NONE;
  private int[] fourColors = new int[4];
  private Vector childControls;
  private int pressColor = -1;
  private PenListener pe;
  private boolean cpressed;
  private Image npback;

  /** Sets the type of background of this Container. To disable the background, set the 
   * <code>transparentBackground</code> of the Control class to true. This field is used when
   * transparentBackground is set to false (default).
   * 
   * If the transparent background doesn't work, try setting
   * <code>alwaysEraseBackground = true</code>.
   * 
   * @see #BACKGROUND_SHADED
   * @see #BACKGROUND_SOLID
   * @see UIColors#shadeFactor
   * @since TotalCross 1.3
   */
  public int backgroundStyle = BACKGROUND_SOLID;

  /** used in the setBorderStyle method */
  static public final byte BORDER_NONE = 0;
  /** used in the setBorderStyle method */
  static public final byte BORDER_LOWERED = 2;
  /** used in the setBorderStyle method */
  static public final byte BORDER_RAISED = 3;
  /** used in the setBorderStyle method */
  static public final byte BORDER_SIMPLE = 5;
  /** used in the setBorderStyle method */
  static public final byte BORDER_TOP = 1;
  /** used in the setBorderStyle method.
   * @since TotalCross 2.0 */
  static public final byte BORDER_ROUNDED = 6;

  /** used in the bckgroundStyle field */
  static public final int BACKGROUND_SOLID = 0;
  /** used in the backgroundStyle field */
  static public final int BACKGROUND_SHADED = 1;
  /** used in the backgroundStyle field */
  static public final int BACKGROUND_SHADED_INV = 3;
  /** used in the backgroundStyle field. The bright color must be the fore color, and the darker color, the back color
   * @since TotalCross 2.0 
   */
  static public final int BACKGROUND_CYLINDRIC_SHADED = 2;


  /** The color used in the border.
   * @since TotalCross 2.0
   */
  public int borderColor = -1;

  /**
   * Defines the total transition time. Defaults to 1000 (1 second).
   * 
   * @since TotalCross 1.68
   */
  public static int TRANSITION_TIME = 500;

  protected int lastX = -999999, lastY, lastW, lastH; // guich@200b4_100
  protected int numChildren;
  protected boolean started; // guich@340_15
  /** Set to true to avoid calling the methods onRemove or onAddAgain */
  protected boolean ignoreOnRemove, ignoreOnAddAgain;
  protected boolean finishedStart; // guich@450_36: avoid repaints while we're creating our controls for the first time
  /** Holds the controls that will be used to transfer focus when the tab key is pressed.
   * You can add or remove controls from here, but be careful not to add repeated controls.
   * @since SuperWaba 5.5
   */
  public Vector tabOrder = new Vector(); // guich@550_15
  protected int lastScreenWidth;
  /** The insets of this container. Never change it directly, otherwise some controls
   * may not work correctly; use setInsets instead.
   * @see #setInsets
   */
  protected Insets insets = new Insets(); // guich@tc110_87

  /** Set to true to always erase the background when repainting this container.
   * @since TotalCross 1.0
   */
  public boolean alwaysEraseBackground;

  /** Returns true if the control was found in findChild, false otherwise.
   * @since TotalCross 1.2
   */
  protected static boolean controlFound; // guich@tc120_48

  // a private id used in the ListContainer class
  int containerId;
  
  private static final Control[] NO_CHILDREN = new Control[0];

  /** Creates a container with the default colors.
   * Important note: this container has no default size.
   * <br><br>
   * <b>NEVER INITIALIZE THE USER INTERFACE IN THE CONSTRUCTOR</b>
   * <br><br>
   * If you're extending the Container class and adding controls in its
   * constructor, you may come into problems if you don't set the bounds
   * as the first thing.
   */ // guich@300_58
  public Container() {
    asContainer = this;
    focusTraversable = false; // kmeehl@tc100: Container is now not focusTraversable by default. Controls extending Container will set focusTraversable explicitly.
  }

  public void setPressColor(int color) {
    this.pressColor = color;
    if (color == -1 && pe != null) {
      removePenListener(pe);
      pe = null;
      callListenersOnAllTargets = cpressed = false;
    }
    if (color != -1 && pe == null) {
      callListenersOnAllTargets = true;
      addPenListener(pe = new PenListener() {
        @Override
        public void penUp(PenEvent e) {
          if (e.type == PenEvent.PEN_UP && isEnabled() && !hadParentScrolled()) {
            setPressed(!cpressed);
            postPressedEvent();
          }
        }

        @Override
        public void penDown(PenEvent e) {
        }

        @Override
        public void penDrag(DragEvent e) {
        }

        @Override
        public void penDragStart(DragEvent e) {
        }

        @Override
        public void penDragEnd(DragEvent e) {
        }
      });
    }
  }

  public void setPressed(boolean p) {
    cpressed = p;
    Window.needsPaint = true;
  }

  public boolean isPressed() {
    return cpressed;
  }

  /** Sets the insets value to match the given ones.
   * @since TotalCross 1.01
   */
  public void setInsets(int left, int right, int top, int bottom) // guich@tc110_87
  {
    int gap = borderStyle == BORDER_NONE || borderStyle == BORDER_TOP ? 0 : borderStyle == BORDER_SIMPLE ? 1 : 2;
    insets.left = left + gap;
    insets.right = right + gap;
    insets.top = top + gap;
    insets.bottom = bottom + gap;
  }

  /** Copy the current insets values into the given insets. If you call this method often,
   * create an Insets field and reuse it.
   * @param copyInto The created object where the insets values will be copied into.
   * @since TotalCross 1.01
   */
  public void getInsets(Insets copyInto) // guich@tc110_87
  {
    copyInto.left = insets.left;
    copyInto.right = insets.right;
    copyInto.top = insets.top;
    copyInto.bottom = insets.bottom;
  }

  /** Adds the control to this container, using the given bounds, relative to the last control added
   * Same of
   * <pre>
   * add(control);
   * control.setRect(x,y,w,h, null,false);
   * </pre>
   * @see Control#setRect(int, int, int, int, Control, boolean)
   */
  public void add(Control control, int x, int y, int w, int h) {
    add(control);
    control.setRect(x, y, w, h, null, false);
  }

  /** Adds the control to this container, using the given bounds, relative to the given control.
   * Same of
   * <pre>
   * add(control);
   * control.setRect(x,y,w,h, relative,false);
   * </pre>
   * @see Control#setRect(int, int, int, int, Control, boolean)
   */
  public void add(Control control, int x, int y, int w, int h, Control relative) // guich@200b4_138
  {
    add(control);
    control.setRect(x, y, w, h, relative, false);
  }

  /** Add the control to this container and set its rect
   * to be the given x,y and PREFERRED as width/height
   * relative to the last control added
   * Same of
   * <pre>
   * add(control);
   * control.setRect(x,y,PREFERRED,PREFERRED, null,false);
   * </pre>
   * @see Control#setRect(int, int, int, int, Control, boolean)
   */
  public void add(Control control, int x, int y) {
    add(control);
    control.setRect(x, y, PREFERRED, PREFERRED, null, false);
  }

  /** Add the control to this container and set its rect
   * to be the given x,y and PREFERRED as width/height
   * relative to the given control
   * Same of
   * <pre>
   * add(control);
   * control.setRect(x,y,PREFERRED,PREFERRED, relative,false);
   * </pre>
   * @see Control#setRect(int, int, int, int, Control, boolean)
   */
  public void add(Control control, int x, int y, Control relative) // guich@200b4_138
  {
    add(control);
    control.setRect(x, y, PREFERRED, PREFERRED, relative, false);
  }

  /**
   * Adds a child control to this container.
   * <b>Important</b>: If you're swapping containers from the MainWindow, be sure
   * that you set the focus on the new container after calling this add method.
   * Otherwise, a MenuBar will not work. Or, use the handy method Window.swap
   */
  public void add(Control control) {
    if (control.uiAdjustmentsBasedOnFontHeightIsSupported == Settings.uiAdjustmentsBasedOnFontHeight) {
      control.uiAdjustmentsBasedOnFontHeightIsSupported = this.uiAdjustmentsBasedOnFontHeightIsSupported;
    }
    if (control.parent != null) {
      control.parent.remove(control);
    }
    if (control.asWindow != null) {
      throw new RuntimeException("A Window can't be added to a container: use popup instead.");
    }
    // set children, next, prev, tail and parent
    addToList(control);
    if (foreColor < 0) {
      foreColor = UIColors.controlsFore; // assign the default colors
    }
    if (backColor < 0) {
      backColor = UIColors.controlsBack;
    }
    if (control.foreColor < 0/* || control.foreColor == UIColors.controlsFore - if the user set the container's color to something else and the control's color to black, this test overrides the black color*/) {
      control.foreColor = this.foreColor; // guich@200b4_125
    }
    if (control.backColor < 0/* || control.backColor == UIColors.controlsBack*/) {
      control.backColor = this.backColor; // guich@200b4_125
    }
    if (this.font != MainWindow.defaultFont && control.font == MainWindow.defaultFont) {
      control.setFont(this.font);
    }
    control.onColorsChanged(true);
    if (control.width > 0 && finishedStart) {
      Window.needsPaint = true; // guich@450_36: only repaint here if the setRect was already called; otherwise, repaint will be called on setRect
    }
    if (control.asContainer != null && !control.asContainer.ignoreOnAddAgain && control.asContainer.started) {
      control.asContainer.onAddAgain(); // guich@402_5
    }
    if (control.asContainer != null || (control.focusTraversable && !control.focusLess)) {
      tabOrder.addElement(control);
    }
  }

  void addToList(Control control) {
    control.next = null;
    if (children == null) {
      children = control;
    } else {
      tail.next = control;
    }
    control.prev = tail;
    tail = control;
    control.parent = this;
    numChildren++;
  }

  /**
   * Removes a child control from the container.
   */
  public void remove(Control control) {
    if (control.parent != this) {
      return;
    }

    // first of all, check if we are removing the focused control
    Window w = getParentWindow();
    if (w == null) {
      w = Window.getTopMost();
    }
    Control c = w._focus;
    while (c != null && c != control) {
      c = c.getParent();
    }
    if (c == control) {
      w.removeFocus();
    }

    // second, check if we are removing the highlighted control
    c = w.highlighted;
    while (c != null && c != control) {
      c = c.getParent();
    }
    if (c == control) {
      w.setHighlighted(null);
    }

    // finally, remove the control: set children, next, prev, tail and parent
    Control prev = control.prev;
    Control next = control.next;
    if (prev == null) {
      children = next;
    } else {
      prev.next = next;
    }
    if (next != null) {
      next.prev = prev;
    }
    if (tail == control) {
      tail = prev;
    }
    control.next = null;
    control.prev = null;
    numChildren--;
    Window.needsPaint = true; // guich@200b4_16: invalidate the hole container's area
    control.parent = null;
    if (control.asContainer != null && !control.asContainer.ignoreOnRemove) {
      control.asContainer.onRemove(); // guich@402_5
    }
    tabOrder.removeElement(control); // kmeehl@tc100: remove the element from the Vector, if it is there
  }

  /** Returns the child located at the given x and y coordinates.
   * Usually, if a control is not found, the last visited container is returned.
   * In this case, controlFound is set to false.
   * @see #controlFound
   * */
  public Control findChild(int x, int y) {
    controlFound = true;
    Container container = this;
    while (true) {
      // search tail to head since paint goes head to tail
      Control child = container.tail;
      while (child != null && (!child.visible || !child.contains(x, y))) {
        child = child.prev;
      }
      if (child == null) {
        controlFound = container.focusTraversable;
        return container;
      }
      if (child.asContainer == null) {
        return child;
      }
      x -= child.x;
      y -= child.y;
      container = child.asContainer;
    }
  }

  public Control findNearestChild(int x, int y, int minDistance) // guich@tc120_48
  {
    int minDistance0 = minDistance;
    Container container = this;
    Control minControl = null;
    while (true) {
      boolean found = false;
      // search tail to head since paint goes head to tail
      Control child = container.tail;
      while (child != null) // guich@240_8: fixed when adding two controls in the same location but one of them was not visible (!child.visible)
      {
        if (child.visible) {
          int dist = (int) (Convert.getDistancePoint2Rect(x, y, child.x, child.y, child.x + child.width,
              child.y + child.height) + 0.5);
          if (dist < minDistance) {
            found = true;
            minControl = child;
            if (dist == 0) {
              break;
            }
            minDistance = dist;
          }
        }
        child = child.prev;
      }
      if (child == null) {
        if (!found && minControl != null) {
          return minControl;
        }
        if (minControl == null) {
          return null;
        }
        child = minControl;
        minDistance = minDistance0;
      }
      if (child.asContainer == null) {
        return child;
      }
      x -= child.x;
      y -= child.y;
      container = child.asContainer;
    }
  }

  /** Return an array of Controls that are added to this Container. */
  public Control[] getChildren() {
    if (numChildren == 0) {
      return NO_CHILDREN;
    }
    Control[] ac = new Control[numChildren];
    Control child = this.tail;
    for (int i = 0; child != null; i++, child = child.prev) {
      ac[i] = child;
    }
    return ac;
  }

  public Control getFirstChild() {
    return children;
  }

  public int getChildrenCount() {
    return numChildren;
  }

  /** Sets if this container and all childrens can or not accept events */
  @Override
  public void setEnabled(boolean enabled) {
    if (internalSetEnabled(enabled, false)) {
      for (Control child = children; child != null; child = child.next) {
        child.setEnabled(enabled);
      }
      post();
    }
  }

  /** Posts an event to the children of this container and to all containers inside this containers; recursively.
   @since SuperWaba 2.0 beta 4 */
  public void broadcastEvent(Event e) {
    _onEvent(e); // guich@200b4_110: make sure this container receive this event.
    for (Control child = children; child != null; child = child.next) {
      child._onEvent(e);
      if (child.asContainer != null) {
        child.asContainer.broadcastEvent(e);
      }
    }
  }

  /** Called by the system to draw the children of the container. */
  public void paintChildren() {
    for (Control child = children; child != null; child = child.next) {
      if (child.visible) // guich@200: ignore hidden controls - note: a window added to a container may not be painted correctly
      {
        if (child.offscreen != null) {
          Graphics g = getGraphics();
          g.drawImage(child.offscreen, child.x, child.y);
          if (child.offscreen0 != null) {
            g.drawImage(child.offscreen0, child.x, child.y);
          }
        } else {
          child.onPaint(child.getGraphics());
          if (child.asContainer != null) {
            child.asContainer.paintChildren();
          }
        }
      }
    }
  }

  /** Sets the border for this container. The insets are changed after this method is called.
   * The BORDER_ROUNDED sets the background to transparent.
   * @see #BORDER_NONE
   * @see #BORDER_LOWERED
   * @see #BORDER_RAISED
   * @see #BORDER_SIMPLE
   * @see #BORDER_TOP
   * @see #BORDER_ROUNDED
   */
  public void setBorderStyle(byte border) // guich@200final_16
  {
    int gap = border == BORDER_NONE || borderStyle == BORDER_TOP ? 0 : borderStyle == BORDER_SIMPLE ? 1 : 2;
    setInsets(gap, gap, gap, gap);
    this.borderStyle = border;
    if (border == BORDER_ROUNDED) {
      transparentBackground = true;
    }
    onColorsChanged(false);
  }

  /** Returns the client rect for this Container, in relative coords.
   * The client rectangle usually excludes the title (if applicable) and the border.
   * If you call this method often, consider using the other version, where a cached Rect is used;
   * it avoids the creation of a Rect.
   * @see #getClientRect(Rect)
   */
  public Rect getClientRect() // guich@200final_15
  {
    Rect r = new Rect();
    getClientRect(r);
    return r;
  }

  /** Returns the client rect for this Container, in relative coords, excluding the insets.
   * In this version, you provide the instantiated Rect to be filled with the coords.
   */
  protected void getClientRect(Rect r) // guich@450_36
  {
    r.set(insets.left, insets.top, width - insets.left - insets.right, height - insets.top - insets.bottom);
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    if (borderStyle != BORDER_NONE && borderStyle != BORDER_SIMPLE && borderStyle != BORDER_TOP
        && borderStyle != BORDER_ROUNDED) {
      Graphics.compute3dColors(isEnabled(), backColor, foreColor, fourColors);
    }
  }

  protected void fillBackground(Graphics g, int b) {
	if(npParts != null && npback == null)  {
		try {
			npback = NinePatch.getInstance().getNormalInstance(npParts, width, height, b, false);
		} catch(ImageException e) {
			if(Settings.onJavaSE)
				e.printStackTrace();
		}
	} else if(npback == null){
	    switch (backgroundStyle) {
	    case BACKGROUND_SOLID:
	      g.backColor = b;
	      g.fillRect(0, 0, width, height);
	      break;
	    case BACKGROUND_SHADED:
	      g.fillShadedRect(0, 0, width, height, true, false, foreColor, b, UIColors.shadeFactor);
	      break;
	    case BACKGROUND_SHADED_INV:
	      g.fillShadedRect(0, 0, width, height, false, false, foreColor, b, UIColors.shadeFactor);
	      break;
	    case BACKGROUND_CYLINDRIC_SHADED:
	      g.drawCylindricShade(foreColor, b, 0, 0, width, height);
	      break;
	    }
	}

	NinePatch.tryDrawImage(g, npback, 0, 0);
  }

  /** Draws the border (if any). If you override this method, be sure to call
   * <code>super.onPaint(g);</code>, or the border will not be drawn.
   */
  @Override
  public void onPaint(Graphics g) {
    int b = pressColor != -1 && cpressed ? pressColor : backColor;
    if (drawTranslucentBackground(g, alphaValue)) {
    } else if (!transparentBackground
        && (parent != null && (b != parent.backColor || parent.asWindow != null || alwaysEraseBackground))) {
      fillBackground(g, b);
    } else {
	    switch (borderStyle) {
	    case BORDER_NONE:
	      break;
	
	    case BORDER_TOP:
	      g.foreColor = borderColor != -1 ? borderColor : getForeColor();
	      g.drawRect(0, 0, width, 0);
	      break;
	
	    case BORDER_SIMPLE:
	      g.foreColor = borderColor != -1 ? borderColor : getForeColor();
	      g.drawRect(0, 0, width, height);
	      break;
	
	    case BORDER_ROUNDED:
	      g.drawWindowBorder(0, 0, width, height, 0, 0, borderColor != -1 ? borderColor : getForeColor(), b, b, b, 2,
	          false);
	      break;
	
	    default:
	      g.draw3dRect(0, 0, width, height, borderStyle, false, false, fourColors);
	    }
    }
  }

  /** When the container is added for the first time, the method initUI is called,
   * so the user interface can be initialized.
   * From the second time and up that the container is added, the onAddAgain method
   * is called instead.
   * <br><br>
   * If this container is a window, this method is not called when the window is popped.
   * <br><br>
   * If you don't want this method to be called, you must set <code>ignoreOnAddAgain = true;</code> inside
   * the method initUI or the class constructor. Doing so makes the user interface initialize faster.
   *
   * @since SuperWaba 4.1
   */
  protected void onAddAgain() {
  }

  /** Called when this container is removed from the parent. Note that, if this
   * container is a window, this method is not called when the window is unpopped.
   * <br><br>
   * If you don't want this method to be called, you must set <code>ignoreOnRemove = true;</code> inside
   * the method initUI or the class constructor. Doing so makes the user interface initialize faster.
   *
   * @since SuperWaba 4.1
   */
  protected void onRemove() {
  }

  /** Called to initialize the User Interface of this container. This differs from the onAddAgain
   * method by that this method is called only once, at the first time the control is added to the parent.
   * When the container is being setup, the initUI method is called;
   * then, the onAddAgain is called every time the container is added again.
   * @since SuperWaba 3.4
   */
  public void initUI() // guich@340_15
  {
  }

  /** Called by the event dispatcher to set highlighting back to true.
   * A class may extend this to decide when its time to turn it on again or not. */
  public void setHighlighting() {
    isHighlighting = true;
  }

  /** Call this method to swap this Container to the topmost window. Note that since
   * we have no idea who is our future parent window, we just use the topmost window.
   * The topmost window is the one that is currently visible.
   * <br>This method is useful if you have containers that are SINGLETONS and wish to
   * set them as the current container. E.G.:
   * <pre>
   * public class MainMenu extends Container // create a Singleton from MainMenu
   * {
   *    private static instance;
   *    public static getInstance()
   *    {
   *       return instance != null ? instance : (instance=new MainMenu());
   *    }
   *    private MainMenu()
   *    {
   *    }
   *  }
   *  // then at some other class, you can do:
   *  MainMenu.getInstance().swapToTopmostWindow();
   *  </pre>
   *  @since SuperWaba 5.7
   */
  public void swapToTopmostWindow() // guich@570_79
  {
    Window.getTopMost().swap(this);
  }

  /** Clears all children controls that are <code>focusTraversable</code>, recursively. */
  @Override
  public void clear() // guich@572_19
  {
    for (Control child = children; child != null; child = child.next) {
      if (child.focusTraversable || (child.asContainer != null && child.asContainer.tabOrder.size() > 0)) {
        child.clear();
      }
    }
  }

  /**
   * Get a list of child controls of this container which are focus candidates
   * @param v 	A vector into which to add the focus candidates.
   */
  public void getFocusableControls(Vector v) //kmeehl@tc100
  {
    if (!visible || !isEnabled()) {
      return;
    }
    Control child = children;
    for (int i = 0; i < numChildren; i++, child = child.next) {
      if (child.focusTraversable && !child.focusLess) {
        if (child.asContainer != null && !child.focusHandler) {
          ((Container) child).getFocusableControls(v); // Note that calling this function directly on the focus handler in question will yield its focusable controls.
        }
        if (child.isVisible() && child.isEnabled() && child.height > 0 && child.width > 0) {
          v.addElement(child);
        }
      } else if (child.asContainer != null) {
        ((Container) child).getFocusableControls(v);
      }
    }
  }

  /**
   * Finds the next control that should receive focus based on the direction with respect to c.
   * @param c The reference control from which to find the next control.
   * @param direction The direction in which to look from c. One of: SpecialKeys.LEFT, SpecialKeys.RIGHT, SpecialKeys.UP, SpecialKeys.DOWN
   * @return The control which should receive focus next, or null.
   * @see totalcross.sys.Settings#geographicalFocus
   */
  public Control findNextFocusControl(Control c, int direction) // kmeehl@tc100
  {
    Rect controlRect = c.getAbsoluteRect();
    int controlRight = controlRect.x + controlRect.width;
    int controlBottom = controlRect.y + controlRect.height;

    int closestControlCoordValue = 999999;
    int closestOverlapCoordValue = 999999;

    if (direction == SpecialKeys.UP || direction == SpecialKeys.LEFT) {
      closestControlCoordValue = -1;
      closestOverlapCoordValue = -1;
    }

    if (childControls == null) {
      childControls = new Vector(2);
    } else {
      childControls.removeAllElements();
    }
    getFocusableControls(childControls);

    int controlPastTestCoord;
    boolean vertical = (direction == SpecialKeys.UP || direction == SpecialKeys.DOWN);
    if (vertical) {
      controlPastTestCoord = controlRect.y;
      if (direction == SpecialKeys.DOWN) {
        controlPastTestCoord += controlRect.height;
      }
    } else {
      controlPastTestCoord = controlRect.x;
      if (direction == SpecialKeys.RIGHT) {
        controlPastTestCoord += controlRect.width;
      }
    }

    Control closestControl = null, closestOverlap = null, childControl = null;
    try {
      childControl = (Control) childControls.pop();
    } catch (ElementNotFoundException e) {
    }
    while (childControl != null) {
      Rect childRect = childControl.getAbsoluteRect();
      int childRight = childRect.x + childRect.width;
      int childBottom = childRect.y + childRect.height;

      // is the child control past the current control;
      boolean isChildPast = false;
      int childCoordValue = 0;

      if (vertical) {
        childCoordValue = childRect.y;
        if (direction == SpecialKeys.UP) {
          childCoordValue += childRect.height;
        }
      } else {
        childCoordValue = childRect.x;
        if (direction == SpecialKeys.LEFT) {
          childCoordValue += childRect.width;
        }
      }

      if (direction == SpecialKeys.RIGHT || direction == SpecialKeys.DOWN) {
        isChildPast = childCoordValue >= controlPastTestCoord;
      } else {
        isChildPast = childCoordValue <= controlPastTestCoord;
      }

      if (isChildPast) {
        // does this child control overlap the current control
        boolean childOverlaps = (vertical && childRect.x <= controlRight && childRight >= controlRect.x)
            || (!vertical && childRect.y <= controlBottom && childBottom >= controlRect.y);
        if (closestControl == null) {
          closestControl = childControl;
          closestControlCoordValue = childCoordValue;
        } else if (isCloser(childCoordValue, closestControlCoordValue, closestControl, vertical, childRect.x,
            childRect.y, direction)) {
          closestControl = childControl;
          closestControlCoordValue = childCoordValue;
        }
        if (childOverlaps && (closestOverlap == null || isCloser(childCoordValue, closestOverlapCoordValue,
            closestOverlap, vertical, childRect.x, childRect.y, direction))) {
          closestOverlap = childControl;
          closestOverlapCoordValue = childCoordValue;
        }
      }
      try {
        childControl = (Control) childControls.pop();
      } catch (ElementNotFoundException e) {
        childControl = null;
      }
    }

    if (closestOverlap != null) {
      return closestOverlap;
    }
    if (closestControl == null && (direction == SpecialKeys.LEFT || direction == SpecialKeys.RIGHT)) {
      return findNextFocusControl(c, direction == SpecialKeys.LEFT ? SpecialKeys.UP : SpecialKeys.DOWN);
    }
    return closestControl;
  }

  private boolean isCloser(int childCoordValue, int nextControlCoordValue, Control nextControl, boolean vertical,
      int childX, int childY, int direction) // kmeehl@tc100
  {
    if (childCoordValue == nextControlCoordValue) {
      //  this control is at the same coordinate as the prior selected control.
      //  check if this child control is more to the left/top than the previous child.
      if ((vertical && childX < nextControl.getAbsoluteRect().x)
          || (!vertical && childY < nextControl.getAbsoluteRect().y)) {
        return true;
      }
    } else {
      boolean toPrev = direction == SpecialKeys.DOWN || direction == SpecialKeys.RIGHT;
      // is new closest control?
      if ((toPrev && childCoordValue < nextControlCoordValue) || (!toPrev && childCoordValue > nextControlCoordValue)) {
        nextControlCoordValue = childCoordValue;
        return true;
      }
    }
    return false;
  }

  /** Returns the border style. */
  public byte getBorderStyle() {
    return borderStyle;
  }

  /**
   * Removes all controls inside this container.
   *
   * @since TotalCross 1.0
   */
  public void removeAll() {
    if (numChildren > 0) {
      Control[] c = getChildren();
      for (Control control : c) {
        remove(control);
      }
    }
  }

  /** Increments the lastX, used in relative positioning. */
  public void incLastX(int n) {
    lastX += n;
  }

  /** Increments the lastY, used in relative positioning. */
  public void incLastY(int n) {
    lastY += n;
  }

  /** This method resizes the Container's width and height to the needed bounds, based on added childs.
   * You can add spaces at right and bottom using the insets.right/bottom properties.
   * Sample:
   * <pre>
   *   // this sample will center two buttons of different sizes on screen 
   *   Container c = new Container();
   *   add(c, CENTER,BOTTOM,1000,1000);
   *   c.add(new Button("Ok"),LEFT,TOP);
   *   c.add(new Button("Cancel"),AFTER+5,SAME);
   *   c.resize();
   * </pre>
   * Note: differently of Window.resize, this method does not call setRect again, it only
   * changes the width and height by direct assignment.
   * @since TotalCross 1.14
   * @see #resizeWidth
   * @see #resizeHeight
   */
  public void resize() // guich@tc114_53
  {
    resizeWidth();
    resizeHeight();
  }

  /** This method resizes the Container's width to the needed bounds, based on added childs.
   * You can add spaces at right using the insets.right property.
   * Sample:
   * <pre>
   *   // this sample will center two buttons of different sizes on screen 
   *   Container c = new Container();
   *   add(c, CENTER,BOTTOM,1000,1000);
   *   c.add(new Button("Ok"),LEFT,TOP);
   *   c.add(new Button("Cancel"),AFTER+5,SAME);
   *   c.resize();
   * </pre>
   * Note: differently of Window.resize, this method does not call setRect again, it only
   * changes the width by direct assignment.
   * @since TotalCross 1.14
   * @see #resize
   * @see #resizeHeight
   */
  public void resizeWidth() // guich@tc114_53
  {
    int maxX = 0;
    for (Control child = children; child != null; child = child.next) {
      maxX = Math.max(maxX, child.x + child.width);
    }
    int hborder = borderStyle == BORDER_NONE || borderStyle == BORDER_TOP ? 0 : borderStyle == BORDER_SIMPLE ? 1 : 2;
    setW = width = maxX + insets.right + hborder / 2;
    updateTemporary(); // guich@tc114_68
  }

  /** This method resizes the Container's width to the needed bounds, based on added childs.
   * You can add spaces at bottom using the insets.bottom property.
   * Sample:
   * <pre>
   *   // this sample will center two buttons of different sizes on screen 
   *   Container c = new Container();
   *   add(c, CENTER,BOTTOM,1000,1000);
   *   c.add(new Button("Ok"),LEFT,TOP);
   *   c.add(new Button("Cancel"),AFTER+5,SAME);
   *   c.resize();
   * </pre>
   * Note: differently of Window.resize, this method does not call setRect again, it only
   * changes the height by direct assignment.
   * @since TotalCross 1.14
   * @see #resizeWidth
   * @see #resize
   */
  public void resizeHeight() // guich@tc114_53
  {
    int maxY = 0;
    for (Control child = children; child != null; child = child.next) {
      maxY = Math.max(maxY, child.y + child.height);
    }
    int hborder = borderStyle == BORDER_NONE || borderStyle == BORDER_TOP ? 0 : borderStyle == BORDER_SIMPLE ? 1 : 2;
    setH = height = maxY + insets.bottom + hborder / 2;
    updateTemporary(); // guich@tc114_68
  }

  /** Moves the focus to the next Edit or MultiEdit control.
   * @return The selected control or null if none was found.
   * @since TotalCross 1.25
   */
  public Control moveFocusToNextEditable(Control control, boolean forward) // guich@tc125_26
  {
    if (control.nextTabControl != null && changeTo(control.nextTabControl)) {
      return control.nextTabControl;
    }

    Vector v = tabOrder;
    int idx = v.indexOf(control);
    int n = v.size();
    if ((idx == -1 && n >= 0) || n > 1) {
      if (idx == -1 && !forward) {
        idx = n;
      }
      for (int i = n - 1; i >= 0; i--) {
        if (forward && ++idx == n) {
          if (Settings.virtualKeyboard && Settings.enableVirtualKeyboard) {
            if (parent != null && (control instanceof Edit && ((Edit) control).editable)) {
              getParentWindow().shiftScreen(parent, 0);
              ((Edit) control).hideSip();
            }
          }
        } else if (!forward && --idx < 0) {
          idx = n - 1;
        }
        if(idx == n) return null;
        Control c = (Control) v.items[idx];
        if (changeTo(c)) {
          return c;
        }
      }
    }
    return parent != null ? parent.moveFocusToNextEditable(control, forward) : null;
  }

  private boolean changeTo(Control c) {
    if (c != null && c != this && c.isEnabled() && c.visible && (c instanceof Edit && ((Edit) c).editable)
        || (c instanceof MultiEdit && ((MultiEdit) c).editable)) // guich@tc100b4_12: also check for enabled/visible/editable - guich@tc120_49: skip ourself
    {
      c.requestFocus();
      if (Settings.virtualKeyboard && Settings.enableVirtualKeyboard) {
        if (c instanceof Edit) {
          ((Edit) c).popupKCC();
        } else {
          ((MultiEdit) c).popupKCC();
        }
      }
      return true;
    }
    return false;
  }

  /** Moves the focus to the next control, which can be an Edit, a MultiEdit, or another control type.
   * It does not show the keyboard.
   * @return The selected control or null if none was found.
   * @since TotalCross 1.53
   */
  public Control moveFocusToNextControl(Control control, boolean forward) // guich@tc125_26
  {
    Vector v = tabOrder;
    int idx = v.indexOf(control);
    int n = v.size();
    if (idx >= 0 && n > 1) {
      for (int i = n - 1; i >= 0; i--) {
        if (forward && ++idx == n) {
          idx = 0;
        } else if (!forward && --idx < 0) {
          idx = n - 1;
        }
        Control c = (Control) v.items[idx];
        if (c != this && c.isEnabled() && c.visible) {
          c.requestFocus();
          return c;
        }
      }
    }
    return null;
  }

  /** Changes the focusTraversable property for this container and all controls, recursively */
  public void setFocusTraversable(boolean b) {
    focusTraversable = b;
    for (Control cc = children; cc != null; cc = cc.next) {
      cc.focusTraversable = true;
      if (cc.asContainer != null) {
        cc.asContainer.setFocusTraversable(b);
      }
    }
  }

  /** Called when this container has been swapped into the Window and the swap is done.
   * @since TotalCross 2.0
   */
  public void onSwapFinished() {
  }
}
