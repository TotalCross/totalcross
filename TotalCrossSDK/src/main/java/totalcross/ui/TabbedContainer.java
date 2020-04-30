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

import totalcross.res.Resources;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Vm;
import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.ControlAnimation.AnimationFinished;
import totalcross.ui.anim.PathAnimation;
import totalcross.ui.effect.UIEffects;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.font.Font;
import totalcross.ui.font.FontMetrics;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Vector;

/**
 * TabbedContainer is a bar of text or image tabs.
 * It is assumed that all images will have the same height, but they may have different widths.
 * <br>
 * A scroll is automatically added when the total width of the titles is bigger than the control's width.
 * <br>
 * The containers are created automatically and switched when the user press the corresponding tab.
 * <p>
 * Here is an example showing a tab bar being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 *    TabbedContainer tab;
 *
 *    public void initUI()
 *    {
 *       String names[] = {"Edition","Report"};
 *       tab = new TabbedContainer(names);
 *       add(tab);
 *       tab.setGaps(2,2,2,2); // set it before setting the rect
 *       tab.setRect(LEFT,TOP,FILL,FILL);
 *       tab.setContainer(0,new Edition()); // replace container 1 by a class that extends Container.
 *       tab.getContainer(1).add(new Label("Not implemented"),CENTER,CENTER);
 *    }
 *
 *    public void onEvent(Event event)
 *    {
 *       if (event.type == ControlEvent.PRESSED && event.target == tp)
 *       {
 *          int activeIndex = tp.getActiveTab();
 *          ... handle tab being pressed
 *       }
 *    }
 * }
 * </pre>
 * Here's another sample that will show two TabbedContainers, one with images and another one with scrolling tabs.
 * Note that you must create img1.png and img2.png.
 * <pre>
 * TabbedContainer tp1 = new TabbedContainer(new Image[]{new Image("img1.png"), new Image("img2.png")}, null);
 * add(tp1);
 * tp1.setRect(LEFT,TOP,Settings.screenWidth/2,Settings.screenHeight/2);
 * tp1.activeTabBackColor = Color.getRGB(222,222,222);
 *
 * TabbedContainer tp2 = new TabbedContainer(new String[]{"verinha","marcelo","denise","guilherme","renato","michelle","rafael","barbara","lucas","ronaldo","nenem",});
 * add(tp2);
 * tp2.setRect(LEFT,AFTER+2,FILL,FILL);
 * </pre>
 * 
 * When the user interface is Android, the tabs do not look good if the background is the same of the parent's.
 * In this case, we force the background to be slighly darker. There are a few fields that you can use to change
 * the color, like activeTabBackColor, useOnTabTheContainerColor and pressedColor.
 * 
 * Important: starting in TotalCross 1.3, with Settings.fingerTouch=true, you CANNOT call setRect in your container.
 * Otherwise, the flick and drag will not work and your container will be positioned incorrectly.
 */

public class TabbedContainer extends ClippedContainer implements Scrollable, AnimationFinished {
  private int activeIndex = -1;
  private boolean firstTabChange;
  private String[] strCaptions;
  private Image[] imgCaptions, imgDis, imgCaptions0;
  private Image activeIcon, activeIcon0, npback;
  private boolean isTextCaption = true;
  private Container containers[];
  private int count;
  private int tabH;
  private int captionColor = Color.BLACK;
  private Rect[] rects, rSel, rNotSel;
  private int fColor, cColor;
  private Rect clientRect;
  private ArrowButton btnLeft, btnRight;
  private static final byte FOCUSMODE_OUTSIDE = 0;
  private static final byte FOCUSMODE_CHANGING_TABS = 1;
  private static final byte FOCUSMODE_INSIDE_CONTAINERS = 2;
  private byte focusMode;
  private boolean brightBack;
  /** Set the arrows color right after the constructor and after calling setCaptionsColor, which also change this property. */
  public int arrowsColor = Color.BLACK;
  private Font bold;
  private int btnX;
  private int style = Window.RECT_BORDER;
  private boolean[] disabled; // guich@tc110_58
  // flick support
  private boolean isScrolling;
  private boolean flickTimerStarted = true;
  private int tempSelected = -1;
  private int[] wplains, wbolds;
  private boolean scScrolled;
  private String[] strCaptions0;
  private int tabsType = TABS_TOP;
  // material
  private int lineH;

  /** Set to true to enable the alternative tab border */
  public boolean useBorder2;
  
  /**It's changed when the user changes the NinePatch image */
  private boolean canUseBorder2 = true;

  /** Set to true to automatically shrink the captions to prevent using arrows. Works only for String-based captions. */
  public boolean autoShrinkCaptions;

  /** Enables or not the arrows if scroll is needed. */
  public boolean showArrows = true;
  /** This color is the one used to paint the background of the active tab.
   * This is specially useful for image tabs.
   * @see #setBackColor
   * @see #useOnTabTheContainerColor
   * @see #tabsBackColor
   * @since SuperWaba 5.64
   */
  public int activeTabBackColor = -1; // guich@564_14

  /** Sets the colors used on each tab. You must create and set the array with the colors. Pass -1 to keep
   * the original color. This array has precedence over the other ways that changes colors, except activeTabBackColor.
   * @see #setBackColor
   * @see #useOnTabTheContainerColor
   * @see #activeTabBackColor
   * @since TotalCross 1.52
   */
  public int[] tabsBackColor;

  /** Sets the tabs with the same colors of the container.
   * @see #setBackColor
   * @see #tabsBackColor
   * @see #activeTabBackColor
   * @since SuperWaba 5.72
   */
  public boolean useOnTabTheContainerColor; // guich@572_12

  /** Stores the last active tab index, or -1 if none was previously selected.
   * @since SuperWaba 4.21
   */
  public int lastActiveTab = -1; // guich@421_30: changed name to conform with getActiveIndex
  private int materialLastActiveIndex = -1;

  /** In finger touch devices, the user still can flick into a disabled tab. To disable this behaviour,
   * set this flag to false; so when a disabled tab is reached, the user will not be able to flick into it, and will
   * have to click on an enabled tab to continue flicking.
   * @since TotalCross 1.3
   */
  public boolean flickIntoDisabledTabs = true;

  /** To be used on the setType method: specifies that the tabs will be placed on the top. */
  public static final byte TABS_TOP = 0;
  /** To be used on the setType method: specifies that the tabs will be placed on the bottom. */
  public static final byte TABS_BOTTOM = 1;
  /** To be used on the setType method: specifies that the tabs will be hidden and you will be responsible to change them. */
  public static final byte TABS_NONE = 2;

  /** Set the color when the user clicks on the tab. 
   * @since TotalCross 1.3.4
   */
  public int pressedColor = -1;

  /** Set to true to make all tabs have the same width.
   * @since TotalCross 1.3.4
   */
  public boolean allSameWidth;

  /** Define an extra height for the tabs. Use something line fmH/2.
   * Required when setIcons is called.
   * @see #setIcons(Image[]) 
   * @since TotalCross 1.3.4
   */
  public int extraTabHeight;

  /** The color used for the text of unselected tabs. Defaults to the foreground color. */
  public int unselectedTextColor = -1;

  private Container prevScr, curScr, nextScr;

  /** The Flick object listens and performs flick animations on PenUp events when appropriate. */
  protected Flick flick;

  /** Set to false to disable flicking between tabs. You can still switch between the tabs by clicking on them.
   * Sample:
   * <pre>
      TabbedContainer.allowFlick = false;
      TabbedContainer tc = new TabbedContainer(caps);
      TabbedContainer.allowFlick = true;
   * </pre>
   */
  public static boolean allowFlick = Settings.fingerTouch;

  /** Animation time when you click in the tab. Set to 0 to disable animation. */
  public int animationTime = 250;

  private TabbedContainer(int count) {
    ignoreOnAddAgain = ignoreOnRemove = true;
    this.count = count;
    this.focusTraversable = true; // kmeehl@tc100
    started = true;
    focusHandler = true;
    containers = new Container[count];
    if (allowFlick) {
      flick = new Flick(this);
      flick.forcedFlickDirection = Flick.HORIZONTAL_DIRECTION_ONLY;
      flick.maximumAccelerationMultiplier = 1;
    }
    // create the rects since we want to reuse them
    rects = new Rect[count];
    for (int i = count - 1; i >= 0; i--) {
      rects[i] = new Rect();
      Container c = containers[i] = new Container();
      if (flick != null) {
        flick.addEventSource(c);
      }
      c.ignoreOnAddAgain = c.ignoreOnRemove = true;
    }
    disabled = new boolean[count];
    if (uiMaterial) {
      activeTabBackColor = UIColors.materialSelectedColor;
    }
    effect = UIEffects.get(this);
    
    if(uiAndroid)
    	setNinePatch(Resources.tab, 10, 4);
  }

  @Override
  protected void computeClipRect() {
    bagClipY0 = 0; // include top otherwise the arrows will not be drawn 
    bagClipYf = this.height; // y0 + parent.height;
    bagClipX0 = clientRect.x; // -this.x;
    bagClipXf = bagClipX0 + clientRect.width; //  x0 + parent.width;
  }

  @Override
  public void initUI() {
    onBoundsChanged(false);
  }

  /** Returns the number of tabs.
   * @since TotalCross 1.15
   */
  public int getTabCount() {
    return count;
  }

  /** Sets the given tab index as enabled or not. When a tab is disabled, it is displayed faded,
   * and if the user clicks on it, nothing happens. However, you still can activate it by calling
   * setActiveTab. If there are no tabs enabled, the current tab will be made active and the controls will
   * also be enabled. So, if you plan to disable all tabs, better disable the TabbedContainer control instead.
   * @param on If true, the tab is enabled, if false it is disabled.
   * @param tabIndex The tab's index (0 to count-1)
   * @since TotalCross 1.01
   * @see #setActiveTab
   */
  public void setEnabled(int tabIndex, boolean on) // guich@tc110_58
  {
    disabled[tabIndex] = !on;
    if (!on && (!isTextCaption || imgCaptions != null) && (imgDis[tabIndex] == null)) {
      try {
        imgDis[tabIndex] = imgCaptions[tabIndex].getFadedInstance();
      } catch (ImageException e) {
        imgDis[tabIndex] = imgCaptions[tabIndex];
      }
    }
    if (!on && activeIndex == tabIndex) {
      setActiveTab(nextEnabled(activeIndex, true));
    }
    if (Settings.fingerTouch) {
      containers[tabIndex].setEnabled(on);
      if (!on) {
        containers[tabIndex].eventsEnabled = true;
      }
    }
    Window.needsPaint = true;
  }

  /** Returns if the given tab index is enabled.
   * @since TotalCross 1.01
   */
  public boolean isEnabled(int tabIndex) {
    return !disabled[tabIndex];
  }

  /** Constructs a tab bar control with Strings as captions. */
  public TabbedContainer(String[] strCaptions) {
    this(strCaptions.length);
    this.strCaptions = this.strCaptions0 = strCaptions;
    onFontChanged();
  }

  /** Constructor to keep compilation compatibility with TC 1; transparentColor is ignored. */
  public TabbedContainer(Image[] imgCaptions, int transparentColor) // guich@564_13
  {
    this(imgCaptions);
  }

  /** Constructs a tab bar control with images as captions, using the given color as transparent color.
   * If you don't want to use transparent colors, just pass -1 to the color. */
  public TabbedContainer(Image[] imgCaptions) // guich@564_13
  {
    this(imgCaptions.length);
    this.imgCaptions = imgCaptions;
    setupImageProps();
    isTextCaption = false;
    onFontChanged();
  }

  /** Sets the active icon.
   */
  public void setActiveIcon(Image newActiveIcon) {
    if (isTextCaption) {
      activeIcon0 = newActiveIcon;
    } else {
      activeIcon = newActiveIcon;
    }
  }

  /** Set the given icons to appear at the top (or bottom, if TABS_BOTTOM) of a text TabbedContainer.
   * The icon images must be squared. You must also set the extraTabHeight, because the icons
   * will be resized to (extraTabHeight-fmH) in both directions.
   * @since TotalCross 1.3.4
   */
  public void setIcons(Image[] icons) {
    if (icons.length != count) {
      throw new RuntimeException("Image array passed in setIcons must have the same length of the captions.");
    }
    imgCaptions0 = icons;
    imgCaptions = new Image[icons.length];
    setupImageProps();
  }

  /** Set the given icons to appear at the top (or bottom, if TABS_BOTTOM) of a text TabbedContainer.
   * The icon images must be squared. You must also set the extraTabHeight, because the icons
   * will be resized to (extraTabHeight-fmH) in both directions. Also, sets the active icon.
   */
  public void setIcons(Image[] icons, Image activeIcon) {
    setIcons(icons);
    this.activeIcon0 = activeIcon;
  }

  private void setupImageProps() {
    imgDis = new Image[count];
  }

  /** Sets the position of the tabs. use constants TABS_TOP or TABS_BOTTOM.
   * Since the tabs are not changed dinamicaly, this method must be called
   * after the constructor. */
  public void setType(byte type) {
    this.tabsType = type;
    if (tabsType == TABS_NONE) {
      showArrows = false;
    }
    onFontChanged();
  }

  /** Returns the tabs type. */
  public int getType() {
    return tabsType;
  }

  /** Returns the Container for tab i */
  public Container getContainer(int i) {
    return containers[i];
  }

  /** Sets the type of border. Currently, only the Window.NO_BORDER and Window.RECT_BORDER are supported. NO_BORDER only draws the line under the tabs. */
  @Override
  public void setBorderStyle(byte style) {
    this.style = style;
  }

  /** Replaces the default created Container with the given one. This way you can
   * avoid adding a container to a container and, as such, waste memory.
   * Note that you must do this before the first setRect for this TabbedContainer; otherwise,
   * you must explicitly call setRect again to update the added container bounds
   */
  public void setContainer(int i, Container container) {
    if (containers != null && i >= 0 && i < containers.length) {
      Container old = containers[i];
      containers[i] = container;
      if (i == activeIndex) // guich@300_34: fixed problem when the current tab was changed
      {
        remove(old);
        if (flick != null) {
          flick.removeEventSource(old);
        }
        add(container);
        tabOrder.removeAllElements(); // don't let the cursor keys get into our container
        container.requestFocus();
      }
      if (!container.started) // guich@340_58: set the container's rect
      {
        if (flick != null) {
          add(container);
          container.setRect(old.getRect());
          flick.addEventSource(container);
        } else {
          Container cp = container.parent;
          container.parent = this;
          container.setRect(clientRect);
          container.parent = cp;
        }
        container.setBackColor(container.getBackColor());
      }
    }
    if (Settings.keyboardFocusTraversable) {
      requestFocus();
    }
  }

  /**
   * Sets the currently active tab, animating it. A PRESSED event will be posted to
   * the given tab if it is not the currently active tab; then, the containers will be switched.
   */
  public void setActiveTab(int tab) {
    setActiveTab(tab, true);
  }

  /**
   * Sets the currently active tab. A PRESSED event will be posted to
   * the given tab if it is not the currently active tab; then, the containers will be switched.
   * The animation is optional and can also be defined with @see
   */
  public void setActiveTab(int tab, boolean animate) {
    if (tab >= 0) {
      firstTabChange = activeIndex == -1;
      int dif = firstTabChange ? 0 : activeIndex - tab;
      if (!firstTabChange && flick == null) {
        remove(containers[activeIndex]);
      }
      materialLastActiveIndex = lastActiveTab = activeIndex; // guich@402_4
      activeIndex = tab;
      if (flick == null) {
        add(containers[activeIndex]);
      } else {
        if (!firstTabChange && animationTime > 0 && animate) {
          try {
        	int correction = containers[lastActiveTab].x;
            PathAnimation p = PathAnimation.create(this, 0, 0, dif * width - correction, 0, this, animationTime);
            p.useOffscreen = false;
            p.setpos = new PathAnimation.SetPosition() {
              int last;

              @Override
              public void setPos(int x, int y) {
                int dx = x - last;
                last = x;
                for (int i = 0; i < containers.length; i++) {
                  containers[i].x += dx;
                }
                Window.needsPaint = true;
              }
            };
            p.start();
            return;
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          for (int xx = -activeIndex * width + clientRect.x, i = 0; i < containers.length; i++, xx += width) {
            containers[i].x = xx;
          }
        }
      }
      onAnimationFinished(null);
    }
  }

  @Override
  public void onAnimationFinished(ControlAnimation anim) {
    tabOrder.removeAllElements(); // don't let the cursor keys get into our container
    computeTabsRect();
    scrollTab(activeIndex);
    Window.needsPaint = true;
    if (!firstTabChange) {
      postPressedEvent();
    }
  }

  /** Returns the index of the selected tab */
  public int getActiveTab() {
    return activeIndex;
  }

  /** Returns the container of the active tab. 
   * @since TotalCross 1.2
   */
  public Container getActiveContainer() // guich@tc120_16
  {
    return containers[activeIndex];
  }

  /** Returns the caption height for this TabbedContainer. Note that it is not possible to compute the correct height of 
   * each container, since they will be added AFTER this TabbedContainer has their bounds set. So, you should actually use some
   * other way to specify the bounds, like FILL or FIT; using PREFERRED in the height of setRect will make your application abort. */
  @Override
  public int getPreferredHeight() {
    return tabH /* guich@564_12: + 20 */ + insets.top + insets.bottom;
  }

  /** Returns the minimum width (based on the sizes of the captions) for this TabbedContainer */
  @Override
  public int getPreferredWidth() {
    int sum = 0;
    if (count > 0) {
      // the max size is the size of the biggest bolded title plus the size of the plain titles
      int maxw = 0, maxi = 0;
      for (int i = count; --i >= 0;) {
        int w = rSel[i].width;
        if (w > maxw) {
          maxi = i;
          maxw = w;
        }
        sum += rNotSel[i].width - 1;
      }
      sum += maxw - rNotSel[maxi].width; // add the diff between the bold and the plain fonts of the biggest title
    }
    return sum + getExtraSize(); // guich@573_11: changed from 3 to 2
  }
  
  @Override
  public void setNinePatch(Image img, int side, int corner) {
	  super.setNinePatch(img, side, corner);
	  canUseBorder2 = false;
  }

  private int getExtraSize() {
    return 2 + insets.left + insets.right;
  }

  /** Returns the index of the next/prev enabled tab, or the current tab if there's none. */
  private int nextEnabled(int from, boolean forward) {
    for (int i = 0; i < count; i++) {
      boolean limitsReached = (forward && from == containers.length - 1) || (!forward && from == 0);
      if (limitsReached) {
        from = forward ? 0 : count - 1;
      } else {
        from = forward ? from + 1 : from - 1;
      }
      if (!disabled[from]) {
        break;
      }
    }
    return from < 0 ? 0 : from;
  }

  /** Used internally. resizes all the containers and add the arrows if scroll is needed. */
  @Override
  protected void onBoundsChanged(boolean screenChanged) {
    int i;
    if (autoShrinkCaptions) {
      Vm.arrayCopy(strCaptions0, 0, strCaptions = new String[strCaptions0.length], 0, strCaptions.length);
      onFontChanged();
      int idx = 0;
      double med = 0;
      for (i = 0; i < strCaptions.length; i++) {
        med += strCaptions[i].length();
      }
      int tries = (int) med;
      med /= strCaptions.length;
      while (mustScroll() && tries-- > 0) {
        String s = strCaptions[idx];
        int l = s.length();
        if (l >= med) {
          if (s.charAt(l - 1) == '.') {
            l--;
          }
          s = s.substring(0, l - 1).concat(".");
          strCaptions[idx] = s;
          onFontChanged();
          med = 0;
          for (i = 0; i < strCaptions.length; i++) {
            med += strCaptions[i].length();
          }
          med /= strCaptions.length;
        }
        if (++idx == strCaptions.length) {
          idx = 0;
        }
      }
    }
    onFontChanged();
    computeTabsRect();
    boolean isTop = tabsType == TABS_TOP;
    int borderGap = style == Window.NO_BORDER || uiAndroid ? 0 : 1; // guich@400_89
    int xx = insets.left + borderGap;
    int yy = (isTop ? tabH : borderGap) + insets.top;
    int ww = width - insets.left - insets.right - (borderGap << 1);
    int hh = height - insets.top - insets.bottom - (borderGap << 1) - (isTop ? yy : tabH);
    clientRect = new Rect(xx, yy, ww, hh);
    for (i = 0; i < count; i++) {
      Container c = containers[i];
      if (flick != null && c.parent == null) {
        add(c);
      }
      c.setRect(xx, yy, ww, hh, null, screenChanged);
      c.reposition();
      if (flick != null) {
        xx += width;
      }
    }
    if (getDoEffect() && effect != null) {
      effect.color = Color.brighter(pressedColor != -1 ? pressedColor : activeTabBackColor, 32);
    }
    if (flick != null) {
      flick.setScrollDistance(width);
    }
    if (activeIndex == -1) {
      setActiveTab(nextEnabled(-1, true), false); // fvincent@340_40
    }
    addArrows();
  }

  private boolean mustScroll() {
    if (!allSameWidth) {
      return count > 1 && getPreferredWidth() > this.width; // guich@564_10: support scroll - guich@573_2: only add arrows if there's more than one tab
    }
    // guich@tc306: if all same width, use a different formula
    int each = width / count - 12; // 12 = space between tabs
    for (int i = 0; i < count; i++) {
      if (wbolds[i] > each) {
        return true;
      }
    }
    return false;
  }

  private void addArrows() {
    boolean scroll = mustScroll();
    if (scroll && showArrows) {
      int c = parent != null ? parent.backColor : UIColors.controlsBack; // guich@573_4
      if (btnLeft == null) {
        int hh = Settings.fingerTouch ? fmH * 3 / 4 : Math.max(fmH / 2, tabH / 4); // guich@tc110_90: use tab height if its larger than font's height
        btnRight = new ArrowButton(Graphics.ARROW_RIGHT, hh, arrowsColor);
        btnRight.setBackColor(c);
        btnRight.setBorder(Button.BORDER_NONE);
        btnLeft = new ArrowButton(Graphics.ARROW_LEFT, hh, arrowsColor);
        btnLeft.setBackColor(c);
        btnLeft.setBorder(Button.BORDER_NONE);
        int yy = (tabH + btnRight.getPreferredHeight()) >> 1;
        super.add(btnRight, RIGHT, tabsType == TABS_TOP ? (tabH - yy) : (this.height - yy),
            PREFERRED + (Settings.fingerTouch ? fmH : 0), PREFERRED);
        super.add(btnLeft, BEFORE, SAME, SAME, SAME);
        btnLeft.setEnabled(false);
        btnLeft.setFocusLess(true); // guich@570_39
        btnRight.setFocusLess(true); // guich@570_39
        btnRight.autoRepeat = btnLeft.autoRepeat = true; // guich@tc122_46
        btnRight.AUTO_DELAY = btnLeft.AUTO_DELAY = 500;
      }
      btnX = btnLeft.x - 2;
    } else {
      btnX = this.width;
    }
    if (btnLeft != null) {
      btnRight.setVisible(scroll);
      btnLeft.setVisible(scroll);
    }
  }

  @Override
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    if (btnLeft != null) {
      boolean canGoLeft = activeIndex > 0;
      boolean canGoRight = activeIndex < count - 1;
      btnLeft.setEnabled(isEnabled() && canGoLeft);
      btnRight.setEnabled(isEnabled() && canGoRight);
    }
  }

  /** compute the rects that represents each tab on the screen. */
  public void computeTabsRect() {
    int x0 = uiMaterial ? 0 : 1;
    int y0 = tabsType == TABS_TOP ? 0 : (height - tabH);
    int n = count;
    if (tabsType == TABS_NONE) {
      ;
    } else if (!allSameWidth && transparentBackground) // using balls? center on screen
    {
      int ww = 0;
      for (int i = 0; i < n; i++) {
        ww += (i == activeIndex ? rSel[i] : rNotSel[i]).width;
      }
      x0 = (width - ww) / 2;
    }
    for (int i = 0; i < n; i++) {
      Rect r = rects[i];
      Rect r0 = !uiMaterial && i == activeIndex ? rSel[i] : rNotSel[i];
      r.x = x0;
      r.y = r0.y + y0;
      r.width = r0.width;
      r.height = r0.height;
      x0 += r.width - (uiMaterial ? 0 : 1);
      rects[i] = r;
    }
  }

  /** Scroll the TabbedContainer to the given tab */
  private void scrollTab(int toIdx) // guich@564_10
  {
    if (btnLeft != null && mustScroll()) {
      boolean canGoLeft = toIdx > 0;
      boolean canGoRight = toIdx < count - 1;
      btnLeft.setEnabled(canGoLeft);
      btnRight.setEnabled(canGoRight);
      if (canGoLeft || canGoRight) {
        int xOfs;
        if (toIdx == 0) {
          xOfs = 0;
        } else {
          xOfs = Settings.fingerTouch ? fmH * 2 : 7 * fmH / 11; // keep part of the previous tab on screen
          for (int i = 0; i < toIdx; i++) {
            xOfs -= rNotSel[i].width - 1;
          }
        }
        offsetRects(xOfs);
        // make sure that the last tab is near the left button
        if (rects[count - 1].x2() < btnX || toIdx == count - 1) {
          int dif = btnX - rects[count - 1].x2();
          offsetRects(-xOfs);
          xOfs += dif;
          offsetRects(xOfs);
        }
        Window.needsPaint = true;
      }
    }
  }

  /** Offsets all rectangles by the given value */
  private void offsetRects(int xOfs) {
    // offset the rectangles
    for (int i = count - 1; i >= 0; i--) {
      rects[i].x += xOfs;
    }
  }

  /** Compute the rectangles of the tabs based on the selected
   * (bolded) and unselected (plain) titles. */
  @Override
  public void onFontChanged() // guich@564_11
  {
    lineH = fmH / 10; // material
    boolean isText = isTextCaption;
    if (wplains == null) {
      wplains = new int[count];
      wbolds = new int[count];
      rSel = new Rect[count];
      rNotSel = new Rect[count];
    }
    int extraTabHeight = tabsType == TABS_NONE ? 0 : this.extraTabHeight + (uiMaterial ? lineH * 3 : 0);
    tabH = tabsType == TABS_NONE ? 0
        : isText ? uiAndroid ? (fmH + 8 + extraTabHeight) : (fmH + 4) : (imgCaptions[0].getHeight() + 4);
    int y0 = tabsType == TABS_TOP && !uiAndroid ? 2 : 0;
    bold = uiAndroid ? font : font.asBold();
    FontMetrics fmb = bold.fm;
    int medW = (this.width - getExtraSize()) / count;
    for (int i = count; --i >= 0;) {
      wplains[i] = isText ? fm.stringWidth(strCaptions[i]) : imgCaptions[i].getWidth();
      wbolds[i] = isText && !uiAndroid ? fmb.stringWidth(strCaptions[i]) : wplains[i]; // in uiandroid there's no bold font
    }
    int wp = allSameWidth ? Math.max(medW, Convert.max(wplains)) : 0;
    int wb = allSameWidth ? Math.max(medW, Convert.max(wbolds)) : 0;
    for (int i = count; --i >= 0;) {
      if (tabsType == TABS_NONE) {
        rSel[i] = rNotSel[i] = new Rect(0, 0, 0, 0);
      } else if (uiAndroid) {
        rSel[i] = new Rect(0, 0, allSameWidth ? wp : wplains[i] + 12, tabH);
        rNotSel[i] = imgCaptions == null ? new Rect(0, tabsType == TABS_TOP ? extraTabHeight / 2 : 0,
            allSameWidth ? wp : wplains[i] + 12, tabH - extraTabHeight / 2)
            : new Rect(0, 0, allSameWidth ? wp : wplains[i] + 12, tabH);
      } else {
        rSel[i] = new Rect(0, 0, allSameWidth ? wb : wbolds[i] + 5, tabH);
        rNotSel[i] = new Rect(0, y0, allSameWidth ? wp : wplains[i] + 4, tabH - 2);
      }
    }
    if (isText && imgCaptions != null) {
      if (extraTabHeight == 0) {
        Vm.warning("setIcon was called but extraTabHeight was not set.");
      } else {
        try {
          int size = extraTabHeight - fmH / 2;
          for (int i = 0; i < count; i++) {
            imgCaptions[i] = imgCaptions0[i].getSmoothScaledInstance(size, size);
          }
          if (activeIcon0 != null) {
            activeIcon = activeIcon0.getSmoothScaledInstance(size, size);
          } else {
            activeIcon = null;
          }
        } catch (ImageException ie) {
          if (Settings.onJavaSE) {
            ie.printStackTrace();
          }
        }
      }
    }
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    if (uiAndroid && parent != null && backColor == parent.backColor) // same background color in uiandroid does not look good.
    {
      activeTabBackColor = Color.brighter(backColor, 32);
      backColor = Color.darker(backColor, 32);
    }
    if (colorsChanged) {
      brightBack = Color.getBrightness(foreColor) >= 127;
    }
    fColor = (isEnabled() || !brightBack) ? getForeColor() : Color.darker(foreColor);
    cColor = (isEnabled() || !brightBack) ? getCaptionColor() : Color.darker(captionColor);
    if (colorsChanged && btnLeft != null) {
      btnRight.arrowColor = btnLeft.arrowColor = arrowsColor;
      btnRight.backColor = btnLeft.backColor = parent != null ? parent.backColor : UIColors.controlsBack; // guich@573_4
    }
  }

  /** Called by the system to draw the tab bar. */
  @Override
  public void onPaint(Graphics g) {
    if (activeIndex == -1) {
      return;
    }

    boolean atTop = tabsType == TABS_TOP;
    Rect r;
    int n = count;
    int y = atTop ? (tabH - 1) : 0;
    int h = atTop ? (height - y) : (height - tabH + 1);
    int yl = atTop ? y : (y + h - 1);
    // erase area with parent's color
    int containerColor = containers[activeIndex].backColor; // guich@580_26: use current container's backcolor instead of TabbedContainer's backcolor
    g.backColor = parent.backColor;
    if (!transparentBackground) {
      if (parent.backColor == containerColor) {
        g.fillRect(0, 0, width, height);
      } else {
        // otherwise, erase tab area...
        if (atTop) {
          g.fillRect(0, 0, width, y);
        } else {
          g.fillRect(0, yl, width, height - yl);
        }
        // ...and erase containers area
        g.backColor = containerColor;
        g.fillRect(0, y, width, h);
      }
    }
    if (!uiAndroid) {
      g.foreColor = fColor;
      if (style != Window.NO_BORDER) {
        g.drawRect(0, y, width, h); // guich@200b4: now the border is optional
      } else {
        g.drawLine(0, yl, width, yl);
      }
    }

    int back = backColor;
    g.backColor = backColor;
    if (btnLeft != null && mustScroll()) {
      g.setClip(1, 0, btnX, height);
    }

    // draw the tabs
    if (tabsType != TABS_NONE) {
		boolean drawSelectedTabAlone = !transparentBackground && (activeTabBackColor >= 0 || uiAndroid);
		
		if (!transparentBackground && (uiAndroid || useOnTabTheContainerColor || tabsBackColor != null
		    || parent.backColor != backColor || uiVista)) {
		  for (int i = 0; i < n; i++) {
		    if (drawSelectedTabAlone && i == activeIndex) {
		      continue;
		    }
		    r = rects[i];
		    g.backColor = back = getTabColor(i); // guich@580_7: use the container's color if containersColor was not set - guich@tc110_59: use default back color if container was not yet shown.
		
		    if (npParts != null) {
		      try {
		    	  if(canUseBorder2 && useBorder2)
		    		  setNinePatch(Resources.tab2, 18, 3);
		        npback = NinePatch.getInstance().getNormalInstance(npParts,
		            r.width, r.height, i == tempSelected && pressedColor != -1 ? pressedColor : back, !atTop);
		        npback.alphaMask = alphaValue;
		        NinePatch.tryDrawImage(g, npback, r.x, r.y);
		      } catch (ImageException ie) {
		        if (Settings.onJavaSE) {
		          ie.printStackTrace();
		        }
		      }
		    } else if (!uiMaterial && uiFlat) {
		      g.fillRect(r.x, r.y, r.width, r.height);
		    } else if (!uiMaterial && uiVista && isEnabled()) {
		      g.fillVistaRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, back, atTop, false);
		    } else if (!uiMaterial) {
		      g.fillRect(r.x, r.y, r.width, r.height); // (*)
		    }
		  }
		}
		
		if (drawSelectedTabAlone) // draw again for the selected tab if we want to use a different color
		{
		  int b = containers[activeIndex].backColor;
		  if (tabsBackColor == null && useOnTabTheContainerColor && activeTabBackColor != -1) {
		    g.backColor = b == backColor ? activeTabBackColor : b;
		  } else {
		    boolean dontUseTabs = tabsBackColor == null || tabsBackColor[activeIndex] == -1;
		    g.backColor = activeTabBackColor != -1 && dontUseTabs ? activeTabBackColor
		        : activeTabBackColor != -1 && !dontUseTabs
		            ? Color.interpolate(activeTabBackColor, tabsBackColor[activeIndex]) : getTabColor(activeIndex);
		  }
		  r = rects[activeIndex];
		  if (npParts != null) {
		    try {
	          if(canUseBorder2 && useBorder2)
		    	  setNinePatch(Resources.tab2, 18, 3);
		      npback = NinePatch.getInstance().getNormalInstance(npParts, r.width, r.height, g.backColor, !atTop);
		      NinePatch.tryDrawImage(g, npback, r.x, r.y);
		    } catch (ImageException ie) {
		      if (Settings.onJavaSE) {
		        ie.printStackTrace();
		      }
		    }
		  } else if (!uiMaterial && uiFlat) {
		    g.fillRect(r.x, r.y, r.width, r.height);
		  } else if (!uiMaterial) {
		    g.fillRect(r.x, r.y, r.width, r.height); // (*)
		  }

		  g.backColor = backColor;
		}
      

      if (getDoEffect() && effect != null) {
        effect.paintEffect(g);
      }

      if (uiMaterial && activeIcon == null) {
        r = rects[activeIndex];
        int x = r.x, w = r.width;
        // draw all tabs
        int bc = pressedColor != -1 ? pressedColor : getTabColor(activeIndex);
        y = atTop ? r.y + r.height - lineH : r.y;
        g.backColor = Color.getGray(bc);
        g.fillRect(0, y, width, lineH);

        // draw selected tab
        g.backColor = bc;

        if (materialLastActiveIndex != -1 && lastActiveTab != activeIndex) {
          int iold = materialLastActiveIndex == -1 ? lastActiveTab : activeIndex;
          int inew = materialLastActiveIndex == -1 ? activeIndex : materialLastActiveIndex;
          // maps the container position to the line position
          int dif = iold - inew;
          int wcurr = containers[inew].x - clientRect.x;
          int perc = wcurr * 100 * (dif > 0 ? 1 : -1) / this.width;
          perc = 100 + perc / (dif < 0 ? -dif : dif);

          int xdest = rects[inew].x;
          int xorig = rects[iold].x;
          x = (xdest - xorig) * perc / 100 + xorig;

          int wdest = rects[inew].width;
          int worig = rects[iold].width;
          w = (wdest - worig) * perc / 100 + worig;
        }
        int yy = atTop ? y - lineH * 2 : y + lineH;
        g.fillRect(x, yy, w, lineH * 2);
      }

      // draw text         
      boolean isText = isTextCaption;
      for (int i = 0; i < n; i++) {
        r = rects[i];
        int xx = r.x + (r.width - (i == activeIndex ? wbolds[i] : wplains[i])) / 2;
        int yy = r.y + (r.height - fmH) / 2;
        //if (uiMaterial) yy -= lineH*3;
        if (isText) {
          g.foreColor = disabled[i] ? Color.getCursorColor(cColor)
              : i != activeIndex && unselectedTextColor != -1 ? unselectedTextColor : cColor; // guich@200b4_156
          if (uiAndroid) {
            g.drawText(strCaptions[i], xx,
                atTop ? (extraTabHeight > 0 ? (r.y + r.height - fmH - 7 - (uiMaterial ? lineH * 3 : 0)) : yy - 2)
                    : (extraTabHeight > 0 ? r.y + 7 : yy),
                textShadowColor != -1, textShadowColor);
          } else if (i != activeIndex) {
            g.drawText(strCaptions[i], xx, yy, textShadowColor != -1, textShadowColor);
          } else {
            g.setFont(bold); // guich@564_11
            g.drawText(strCaptions[i], xx, yy, textShadowColor != -1, textShadowColor);
            g.setFont(font);
          }
          if (disabled[i]) {
            g.foreColor = Color.getCursorColor(cColor);
          }
          if (imgCaptions != null && imgCaptions[i] != null) {
            g.drawImage(i == activeIndex && activeIcon != null ? activeIcon : disabled[i] ? imgDis[i] : imgCaptions[i],
                r.x + (r.width - imgCaptions[i].getWidth()) / 2,
                atTop ? r.y + (extraTabHeight - imgCaptions[i].getHeight()) / 2
                    : r.y + (extraTabHeight + imgCaptions[i].getHeight()) / 2);
          }
        } else {
          g.drawImage(i == activeIndex && activeIcon != null ? activeIcon : disabled[i] ? imgDis[i] : imgCaptions[i],
              r.x + (r.width - imgCaptions[i].getWidth()) / 2, r.y + 1);
        }
        g.drawRect(r.x, r.y, r.width, r.height);
      }

      // guich@200b4: remove the underlaying line of the active tab.
      r = rects[activeIndex];

      if (!uiAndroid) {
        g.foreColor = getTabColor(activeIndex); // guich@580_7: use the container's back color
        g.drawLine(r.x, yl, r.x2(), yl);
        g.drawLine(r.x + 1, yl, r.x2() - 1, yl);
      }

      if (Settings.keyboardFocusTraversable && focusMode == FOCUSMODE_CHANGING_TABS) // draw the focus around the current tab - guich@580_52: draw the cursor only when changing tabs
      {
        g.drawDottedRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
        if (Settings.screenWidth == 320) {
          g.drawDottedRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4);
        }
      }
    }
  }

  @Override
  public int getEffectW() {
    return tempSelected == -1 ? 0 : rects[tempSelected].width;
  }

  @Override
  public int getEffectH() {
    return tempSelected == -1 ? 0 : rects[tempSelected].height;
  }

  @Override
  public int getEffectX() {
    return tempSelected == -1 ? UIEffects.X_UNKNOWN : rects[tempSelected].x;
  }

  @Override
  public int getEffectY() {
    return tempSelected == -1 ? 0 : rects[tempSelected].y;
  }

  /** Returns the color of the given tab.
   * @since TotalCross 1.52
   */
  public int getTabColor(int tab) {
    return tabsBackColor != null && tabsBackColor[tab] != -1 ? tabsBackColor[tab]
        : useOnTabTheContainerColor && containers[tab].backColor != -1 ? containers[tab].backColor : backColor;
  }

  /** Sets the text color of the captions in the tabs. */
  public void setCaptionColor(int capColor) {
    this.captionColor = this.arrowsColor = capColor;
    onColorsChanged(true); // guich@200b4_169
  }

  /** Gets the text color of the captions. return a grayed value if this control is not enabled. */
  public int getCaptionColor() {
    return isEnabled() ? captionColor : Color.brighter(captionColor);
  }

  /** Returns the area excluding the tabs and borders for this TabbedContainer.
   * Note: do not change the returning rect object ! */
  @Override
  public Rect getClientRect() // guich@340_27
  {
    return clientRect;
  }

  /** Returns the area excluding the tabs and borders for this TabbedContainer.
   * In this version, you provide the created Rect to be filled with the coords.*/
  @Override
  protected void getClientRect(Rect r) // guich@450_36
  {
    r.set(clientRect);
  }

  /** Called by the system to pass events to the tab bar control. */
  @Override
  public void onEvent(Event event) {
    if (event.type == PenEvent.PEN_DOWN) {
      scScrolled = false;
    }

    switch (event.type) // material: when the container is dragged and we have to move the selected tab 
    {
    case PenEvent.PEN_DRAG_START:
      DragEvent de = (DragEvent) event;
      if (de.direction == DragEvent.LEFT || de.direction == DragEvent.RIGHT) {
        int p = activeIndex + (de.direction == DragEvent.RIGHT ? -1 : 1);
        if (0 <= p && p < count) {
          materialLastActiveIndex = p;
        }
      }
      break;
    case PenEvent.PEN_DRAG_END:
    	setActiveTab(getPositionedTab(false), true);
    	break;
    }

    if (event.target != this) {
      if (event.type == ControlEvent.PRESSED) {
        if (event.target == btnLeft || event.target == btnRight) {
          setActiveTab(nextEnabled(activeIndex, event.target == btnRight), true);
        }
      }
      if (!(flick != null && (event.type == PenEvent.PEN_DRAG || event.type == PenEvent.PEN_UP))) {
        return;
      }
    }

    switch (event.type) {
    case PenEvent.PEN_UP:
      if (tempSelected != -1) {
        setActiveTab(tempSelected, true);
      }
      tempSelected = -1;
      if (uiAndroid) {
        Window.needsPaint = true;
      }
      if (!flickTimerStarted) {
        flickEnded(false);
      }
      isScrolling = false;
      break;
    case PenEvent.PEN_DRAG:
      if (flick != null) {
        Window w = getParentWindow();
        if (w != null && w._focus == w.focusOnPenUp) {
          break;
        }
        DragEvent de = (DragEvent) event;
        scrollContent(-de.xDelta, 0, true);
        event.consumed = true;
      }
      break;
    case ControlEvent.FOCUS_IN: // guich@580_53: when focus is set, activate tab changing mode.
      if (Settings.keyboardFocusTraversable) {
        focusMode = FOCUSMODE_CHANGING_TABS;
      }
      break;
    case PenEvent.PEN_DOWN:
      PenEvent pe = (PenEvent) event;
      tempSelected = -1;
      if (uiAndroid) {
        Window.needsPaint = true;
      }
      if (pe.x < btnX && (flick != null || (rects[0].y <= pe.y && pe.y <= rects[0].y2()))) // guich@tc100b4_7 - guich@tc120_48: when fingerTouch, the y position may be below the tabbed container
      {
        int sel = -1;
        if (flick != null) // guich@tc120_48
        {
          int minDist = Settings.touchTolerance;
          for (int i = count - 1; i >= 0; i--) {
            Rect r = rects[i];
            int d = (int) (Convert.getDistancePoint2Rect(pe.x, pe.y, r.x, r.y, r.x + r.width, r.y + r.height) + 0.5);
            if (d <= minDist) {
              minDist = d;
              sel = i;
            }
          }
        } else {
          for (int i = count - 1; i >= 0; i--) {
            if (rects[i].contains(pe.x, pe.y)) {
              sel = i;
              break;
            }
          }
        }
        if (sel >= 0 && !disabled[sel]) {
          tempSelected = sel;
          if (!uiAndroid) {
            setActiveTab(sel, true);
          }
        }
      }
      break;
    case KeyEvent.ACTION_KEY_PRESS:
      focusMode = FOCUSMODE_CHANGING_TABS;
      // guich@573_23 - super.drawHighlight(); // remove the highlight around the TabbedContainer
      Window.needsPaint = true; // guich@573_23
      break;
    case KeyEvent.SPECIAL_KEY_PRESS:
      if (Settings.keyboardFocusTraversable) {
        KeyEvent ke = (KeyEvent) event;
        int key = ke.key;
        if (focusMode == FOCUSMODE_CHANGING_TABS) {
          if (key == SpecialKeys.LEFT || key == SpecialKeys.RIGHT) {
            setActiveTab(nextEnabled(activeIndex, key == SpecialKeys.RIGHT), true);
          } else if (ke.isUpKey() || ke.isDownKey()) {
            focusMode = FOCUSMODE_INSIDE_CONTAINERS;
            Window.needsPaint = true; // guich@573_23 - drawHighlight();
            containers[activeIndex].changeHighlighted(containers[activeIndex], ke.isDownKey());
            isHighlighting = true;
          }
        }
        if (ke.isActionKey()) {
          focusMode = FOCUSMODE_OUTSIDE;
          //getParent().requestFocus(); - guich@580_54
          isHighlighting = true;
          Window.needsPaint = true; // guich@573_23 - drawHighlight();
        }
      }
      break;
    }
  }

  private void takeScreenShots() {
    try {
      if (activeIndex > 0) {
        (prevScr = containers[activeIndex - 1]).takeScreenShot();
      }
      (curScr = containers[activeIndex]).takeScreenShot();
      if (activeIndex < count - 1) {
        (nextScr = containers[activeIndex + 1]).takeScreenShot();
      }
    } catch (Throwable t) {
      if (Settings.onJavaSE) {
        t.printStackTrace();
      }
      releaseScreenShots();
    }
  }

  private void releaseScreenShots() {
    if (prevScr != null) {
      prevScr.releaseScreenShot();
      prevScr = null;
    }
    if (curScr != null) {
      curScr.releaseScreenShot();
      curScr = null;
    }
    if (nextScr != null) {
      nextScr.releaseScreenShot();
      nextScr = null;
    }
  }

  /** Tranfer the focus between the containers on this TabbedContainer */
  @Override
  public void changeHighlighted(Container p, boolean forward) {
    Window w = getParentWindow();
    if (w != null) {
      switch (focusMode) {
      case FOCUSMODE_OUTSIDE: // focus just got here
        if (w.getHighlighted() != this) {
          w.setHighlighted(this);
        } else {
          super.changeHighlighted(p, forward);
        }
        break;
      case FOCUSMODE_INSIDE_CONTAINERS: // was changing a control and the limits has been reached
        focusMode = FOCUSMODE_CHANGING_TABS;
        w.setHighlighted(this); // remove the focus from the last control
        Window.needsPaint = true; // guich@573_23 - drawHighlight();
        requestFocus();
        isHighlighting = false;
        break;
      default:
        super.changeHighlighted(p, forward);
      }
    }
  }

  /** Only return to highlighting when we want */
  @Override
  public void setHighlighting() {
    isHighlighting = false;
  }

  @Override
  public void reposition() {
    super.reposition();
    computeTabsRect();
    addArrows(); // this is needed because the btnX was not yet repositioned when onBounds called addArrows.
    if (mustScroll()) {
      scrollTab(activeIndex);
    }
    if (Settings.fingerTouch) {
      int tab = activeIndex;
      activeIndex = -1;
      setActiveTab(tab, false);
    }
  }

  @Override
  public void getFocusableControls(Vector v) {
    if (visible && isEnabled()) {
      v.addElement(this);
    }
    super.getFocusableControls(v);
  }

  @Override
  public Control handleGeographicalFocusChangeKeys(KeyEvent ke) {
    boolean atTop = tabsType == TABS_TOP;
    if (MainWindow.mainWindowInstance._focus == this) {
      if ((atTop && ke.isUpKey()) || (!atTop && ke.isDownKey())) {
        return null;
      }

      if ((atTop && ke.isDownKey()) || (!atTop && ke.isUpKey())) {
        Control c = containers[activeIndex].children;
        while (c != null && !c.focusTraversable) {
          c = c.next;
        }
        return c;
      }
      if ((ke.isNextKey() && activeIndex == containers.length - 1) || (ke.isPrevKey() && activeIndex == 0)) {
        return null;
      }
      ke.target = this;
      _onEvent(ke);
      return this;
    }

    int direction = 0;
    if (ke.isUpKey()) {
      direction = SpecialKeys.UP; // this order must
    } else if (ke.isDownKey()) {
      direction = SpecialKeys.DOWN; // be preserved
    } else if (ke.isNextKey()) {
      direction = SpecialKeys.RIGHT;
    } else if (ke.isPrevKey()) {
      direction = SpecialKeys.LEFT;
    } else {
      return null;
    }

    Control c = findNextFocusControl(MainWindow.mainWindowInstance._focus, direction);
    if (c == null) {
      boolean prev = direction == SpecialKeys.UP || direction == SpecialKeys.LEFT;
      c = (prev == atTop) ? this : MainWindow.mainWindowInstance.findNextFocusControl(this, direction);
    }
    return c;
  }

  /** Returns true of the type is set to TABS_TOP.
   * @deprecated Use getType 
   */
  @Deprecated
  public boolean isAtTop() {
    return tabsType == TABS_TOP;
  }

  /** Resizes the height of each added container and sets the height of this TabbedContainer to the maximum height of the containers. */
  @Override
  public void resizeHeight() // guich@tc120_12
  {
    int h = 0;
    for (int i = 0; i < containers.length; i++) {
      containers[i].resizeHeight();
      h = Math.max(h, containers[i].getHeight());
    }
    setRect(KEEP, KEEP, KEEP, getPreferredHeight() + h + 3);
  }

  @Override
  public boolean flickStarted() {
    flickTimerStarted = true;
    return isScrolling;
  }

  @Override
  public void flickEnded(boolean atPenDown) {
    int tab = getPositionedTab(false);
    //setActiveTab(tab, false);
    if (tab != activeIndex && tab >= 0) {
        firstTabChange = activeIndex == -1;
        int dif = firstTabChange ? 0 : activeIndex - tab;
        if (!firstTabChange && flick == null) {
          remove(containers[activeIndex]);
        }
        materialLastActiveIndex = lastActiveTab = activeIndex; // guich@402_4
        activeIndex = tab;
    }
    releaseScreenShots();
    materialLastActiveIndex = -1;
  }

  private int getPositionedTab(boolean exact) {
    int betterV = width;
    int betterI = -1;
    for (int i = 0; i < containers.length; i++) {
      int dif = containers[i].x - clientRect.x;
      if (dif < 0) {
        dif = -dif;
      }
      if (dif < betterV) {
        betterV = dif;
        betterI = i;
      }
    }
    return !exact || betterV == 0 ? betterI : -1;
  }

  @Override
  public boolean canScrollContent(int direction, Object target) // called when 
  {
    return getPositionedTab(true) == -1
        || (direction == DragEvent.LEFT && activeIndex > 0 && (flickIntoDisabledTabs || !disabled[activeIndex - 1]))
        || (direction == DragEvent.RIGHT && activeIndex < containers.length - 1
            && (flickIntoDisabledTabs || !disabled[activeIndex + 1]));
  }

  @Override
  public boolean scrollContent(int xDelta, int yDelta, boolean fromFlick) {
    if (containers.length == 1) {
      return false;
    }
    // prevent it from going beyond limits
    int maxX = -(containers[0].width * (containers.length - 1) + containers.length) - 1;
    int minX = 1;
    int curX = containers[0].x;
    int newX = curX - xDelta;
    if (newX > minX) {
      newX = minX;
    } else if (newX < maxX) {
      newX = maxX;
    }
    xDelta = curX - newX;
    if (xDelta == 0) {
      return false;
    }

    for (int i = containers.length; --i >= 0;) {
      containers[i].x -= xDelta;
    }
    Window.needsPaint = true;
    return true;
  }

  @Override
  public int getScrollPosition(int direction) {
    return containers[0].getX() - clientRect.x;
  }

  @Override
  public Flick getFlick() {
    return flick;
  }

  @Override
  public boolean wasScrolled() {
    return scScrolled;
  }

  /** Changes the tab captions. The new array must have the same length or an exception is thrown. */
  public void setCaptions(String[] caps) {
    if (strCaptions == null || caps.length != strCaptions.length) {
      throw new IllegalArgumentException("The TabbedContainer's captions does not match the given ones.");
    }
    strCaptions = strCaptions0 = caps;
    onFontChanged();
    computeTabsRect();
  }
}
