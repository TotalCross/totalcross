// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.io.IOException;
import totalcross.sys.Settings;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/**
 * This is a top menu like those on Android. It opens and closes using animation
 * and fading effects.
 * 
 * @since TotalCross 3.03
 */
public class TopMenu extends Container implements PresentationController.Delegate {
  /** Controls whether a fixed bar reserves viewport space or overlays it. */
  public enum BarLayoutMode {
    /** Remove the complete bar height from the scrolling viewport. */
    RESERVE_SPACE,
    /** Keep the viewport behind the bar and reserve its height in scroll content. */
    OVERLAY
  }

  /** Selects which fixed bars the menu body may scroll beneath. */
  public enum ScrollUnderMode {
    /** Reserve both fixed bars, matching the Reddit layout model. */
    NONE,
    /** Scroll below the top bar and reserve the bottom bar, matching ChatGPT. */
    TOP,
    /** Reserve the top bar and scroll below the bottom bar, matching Gmail. */
    BOTTOM,
    /** Scroll below both fixed bars. */
    BOTH
  }

  public static interface AnimationListener {
    public void onAnimationFinished();
  }

  /** The percentage of the area used for the icon and the caption */
  public static int percIcon = 20, percCap = 80;
  protected Control[] items;
  private int animDir;
  protected int selected = -1;
  /**
   * Set to false to disable the close when pressing in a button of the menu.
   */
  public boolean autoClose = true;
  /** Defines the animation delay */
  public int totalTime = 800;
  /**
   * The percentage of the screen that this TopMenu will take: LEFT/RIGHT will
   * take 50% of the screen's width, other directions will take 80% of the
   * screen's width. Must be ser before calling <code>popup()</code>.
   */
  public int percWidth;
  private AnimationListener alist;
  /** The width in pixels instead of percentage of screen's width. */
  public int widthInPixels;

  /** An optional image to be used as background */
  public Image backImage;
  /** The alpha value to be applied to the background image */
  public int backImageAlpha = 128;

  /** Insets used to place the ScrollContainer. */
  public Insets scInsets = new Insets(0, 0, 0, 0);

  public boolean drawSeparators = true;

  public int separatorColor = -1;

  public boolean showElevation = false;

  private boolean fadeOnPopAndUnpop;
  private Control topBar;
  private Control bottomBar;
  private BarLayoutMode topBarLayoutMode = BarLayoutMode.RESERVE_SPACE;
  private BarLayoutMode bottomBarLayoutMode = BarLayoutMode.RESERVE_SPACE;
  private ScrollUnderMode scrollUnderMode = ScrollUnderMode.NONE;
  ScrollContainer bodyScroller;
  Container topBarHost;
  Container bottomBarHost;
  protected boolean canDrag = true;
  public boolean fadeOtherWindows;
  public int titleColor = -1;
  public int titleGap;
  public int titleAlign = CENTER;
  private String title;
  private Label titleLabel;
  private boolean safeDrawerWidth;
  private final PresentationController presentationController;

  public static class Item extends Container {
    Control tit;
    Object icon; // Image or Control
    public boolean highlight;

    /**
     * Used when you want to fully customize your Item by extending this class.
     */
    protected Item() {
      setBackForeColors(UIColors.topmenuBack, UIColors.topmenuFore);
      this.highlight = true;
    }

    /** Pass a Control and optionally an icon */
    public Item(Control c, Image icon) {
      this();
      this.tit = c;
      this.icon = icon;
      this.highlight = true;
    }

    /** Pass a Control and optionally another control that serves as an icon */
    public Item(Control c, Control icon) {
      this();
      this.tit = c;
      this.icon = icon;
      this.highlight = true;
    }

    /** Creates a Label and optionally an icon */
    public Item(String cap, Image icon) {
      this(new Label((String) cap, LEFT), icon);
    }

    public Item(String cap, Control icon) {
      this(new Label((String) cap, LEFT), icon);
    }

    private TopMenu getTopMenu() {
      for (Control control = this; control != null; control = control.parent) {
        if (control instanceof TopMenu) {
          return (TopMenu) control;
        }
      }
      return null;
    }

    @Override
    public void initUI() {
      if (tit instanceof Label) {
        Label l = (Label) tit;
        l.setAlpha(222);
      }

      int itemH = fmH + Edit.prefH;
      int perc = percCap;
      Control c = null;
      if (icon == null) {
        perc = 100;
      } else {
        if (icon instanceof Control) {
          c = (Control) icon;
        } else {
          try {
            c = new ImageControl(((Image)icon).getHwScaledInstance(itemH, itemH));
            ((ImageControl) c).centerImage = true;
          } catch (ImageException e) {
          }
        }
        add(c == null ? (Control) new Spacer(itemH, itemH) : c, LEFT, TOP, PARENTSIZE + percIcon, FILL);
      }
      add(new Container() {
        @Override
        public void initUI() {
          /*
           * 72dp left spacing and 16 dp right spacing
           * https://material.io/guidelines/patterns/navigation-drawer.html#navigation-drawer-specs
           */
          this.setInsets(Settings.screenWidth < 320 ? 64 : 72, Settings.screenWidth < 320 ? 8 : 16, 0, 0);
          add(tit, LEFT, CENTER, FILL, PREFERRED);
        }
      }, LEFT, CENTER, FILL, PREFERRED);
    }

    @Override
    public void onEvent(Event e) {
      if (e.type == PenEvent.PEN_UP && !hadParentScrolled()) {
        // Should not accept clicks if the TopMenu animation is playing
        TopMenu menu = getTopMenu();
        if (menu == null || menu.isTransitioning()) {
          return;
        }

        if (highlight) {
        	int original = backColor;
            int highlight = Color.getBrightness(original) < 200 ? Color.brighter(original) : Color.darker(original);
            setBackColors(this, highlight);
            repaintNow();
            postPressedEvent();
            setBackColors(this, original);
        } else {
        	postPressedEvent();
        }
      }
    }

    private void setBackColors(Container c, int b) {
      c.setBackColor(b);
      for (Control child = c.children; child != null; child = child.next) {
    	  child.setBackColor(b);
    	  if (child instanceof Container) {
    		  setBackColors((Container)child, b);
    	  }
      }
    }
  }

  /**
   * @param animDir
   *          LEFT, RIGHT, TOP, BOTTOM, CENTER
   */
  public TopMenu(Control[] items, int animDir, byte borderStyle) {
    this.items = items;
    this.animDir = animDir;
    presentationController = new PresentationController(this);
    titleGap = 0;
    fadeOtherWindows = false;
    uiAdjustmentsBasedOnFontHeightIsSupported = false;
    borderColor = UIColors.separatorFore;
    setBorderStyle(toContainerBorder(borderStyle));
    setBackForeColors(UIColors.separatorFore, UIColors.topmenuFore);
    fadeOnPopAndUnpop = true;
    MainWindow main = MainWindow.getMainWindow();
    if (main != null) {
      main.addTimer(1000);
    }
  }

  /**
   * @param animDir
   *          LEFT, RIGHT, TOP, BOTTOM, CENTER
   */
  public TopMenu(Control[] items, int animDir) {
    this(items, animDir, Window.ROUND_BORDER);
  }

  public void popup() {
    presentationController.popup();
  }

  public void popupNonBlocking() {
    presentationController.popupNonBlocking();
  }

  private static byte toContainerBorder(byte windowBorder) {
    switch (windowBorder) {
    case Window.RECT_BORDER:
      return BORDER_SIMPLE;
    case Window.ROUND_BORDER:
      return BORDER_ROUNDED;
    default:
      return BORDER_NONE;
    }
  }

  public double itemHeightFactor = 2;

  public Container header = null;

  /** Sets the optional fixed top bar. The legacy {@link #header} still scrolls. */
  public void setTopBar(Control bar) {
    if (topBar != bar) {
      topBar = bar;
      rebuildBarHost(true);
      layoutMenu();
    }
  }

  /** Returns the optional fixed top bar. */
  public Control getTopBar() {
    return topBar;
  }

  /** Sets the optional fixed bottom bar. */
  public void setBottomBar(Control bar) {
    if (bottomBar != bar) {
      bottomBar = bar;
      rebuildBarHost(false);
      layoutMenu();
    }
  }

  /** Returns the optional fixed bottom bar. */
  public Control getBottomBar() {
    return bottomBar;
  }

  /** Sets whether the top bar reserves viewport space or overlays scrolling content. */
  public void setTopBarLayoutMode(BarLayoutMode mode) {
    if (mode == null) {
      throw new NullPointerException("mode");
    }
    if (topBarLayoutMode != mode) {
      topBarLayoutMode = mode;
      updateScrollUnderModeFromBars();
      layoutMenu();
    }
  }

  /** Sets whether the bottom bar reserves viewport space or overlays scrolling content. */
  public void setBottomBarLayoutMode(BarLayoutMode mode) {
    if (mode == null) {
      throw new NullPointerException("mode");
    }
    if (bottomBarLayoutMode != mode) {
      bottomBarLayoutMode = mode;
      updateScrollUnderModeFromBars();
      layoutMenu();
    }
  }

  /** Applies one of the four fixed-bar scrolling models. */
  public void setScrollUnderMode(ScrollUnderMode mode) {
    if (mode == null) {
      throw new NullPointerException("mode");
    }
    scrollUnderMode = mode;
    topBarLayoutMode = mode == ScrollUnderMode.TOP || mode == ScrollUnderMode.BOTH
        ? BarLayoutMode.OVERLAY : BarLayoutMode.RESERVE_SPACE;
    bottomBarLayoutMode = mode == ScrollUnderMode.BOTTOM || mode == ScrollUnderMode.BOTH
        ? BarLayoutMode.OVERLAY : BarLayoutMode.RESERVE_SPACE;
    layoutMenu();
  }

  private void updateScrollUnderModeFromBars() {
    boolean top = topBarLayoutMode == BarLayoutMode.OVERLAY;
    boolean bottom = bottomBarLayoutMode == BarLayoutMode.OVERLAY;
    scrollUnderMode = top ? bottom ? ScrollUnderMode.BOTH : ScrollUnderMode.TOP
        : bottom ? ScrollUnderMode.BOTTOM : ScrollUnderMode.NONE;
  }

  void useSafeDrawerWidth() {
    safeDrawerWidth = true;
  }

  public void setTitle(String title) {
    this.title = title == null || title.length() == 0 ? null : title;
    if (titleLabel != null) {
      titleLabel.setText(this.title == null ? "" : this.title);
      layoutMenu();
    }
  }

  public String getTitle() {
    return title;
  }

  public void makeUnmovable() {
    canDrag = false;
  }

  private int getTitleHeight() {
    return title == null ? 0 : fmH + Math.max(0, titleGap);
  }

  private Rect getMenuClientRect() {
    Rect client = getClientRect();
    int titleHeight = getTitleHeight();
    client.y += titleHeight;
    client.height = Math.max(0, client.height - titleHeight);
    return client;
  }

  private int getBarHeight(Control bar) {
    return bar == null ? 0 : bar.getPreferredHeight();
  }

  private void rebuildTitle() {
    if (title == null || bodyScroller == null) {
      return;
    }
    if (titleLabel == null) {
      titleLabel = new Label(title, LEFT);
      super.add(titleLabel);
    }
    titleLabel.setForeColor(titleColor < 0 ? foreColor : titleColor);
    titleLabel.resetSetPositions();
    titleLabel.setRect(titleAlign, TOP, PREFERRED, getTitleHeight());
  }

  private int resolveWidth(Rect viewport) {
    if (widthInPixels != 0) {
      return Math.min(widthInPixels, viewport.width);
    }
    if (safeDrawerWidth && (animDir == LEFT || animDir == RIGHT)) {
      return Math.min(320, Math.max(0, viewport.width - 56));
    }
    return viewport.width * (percWidth > 0 ? percWidth : 50) / 100;
  }

  private void rebuildBarHost(boolean top) {
    Container oldHost = top ? topBarHost : bottomBarHost;
    if (oldHost != null && oldHost.parent == this) {
      super.remove(oldHost);
    }
    Control bar = top ? topBar : bottomBar;
    Container host = null;
    if (bar != null && bodyScroller != null) {
      if (bar.parent != null) {
        bar.parent.remove(bar);
      }
      host = new Container();
      host.setBackColor(bar.getBackColor());
      super.add(host);
      Rect client = getMenuClientRect();
      int barHeight = getBarHeight(bar);
      host.setRect(client.x, top ? client.y : client.y + client.height - barHeight, client.width, barHeight);
      host.add(bar, LEFT, TOP, FILL, FILL);
    }
    if (top) {
      topBarHost = host;
    } else {
      bottomBarHost = host;
    }
  }

  private void layoutMenu() {
    if (bodyScroller == null || width <= 0 || height <= 0) {
      return;
    }
    rebuildTitle();
    Rect client = getMenuClientRect();
    int bodyX = client.x + scInsets.left;
    int bodyY = client.y + scInsets.top;
    int bodyWidth = client.width - scInsets.left - scInsets.right;
    int bodyHeight = client.height - scInsets.top - scInsets.bottom;
    int topHeight = getBarHeight(topBar);
    int bottomHeight = getBarHeight(bottomBar);

    if (topBar != null && topBarLayoutMode == BarLayoutMode.RESERVE_SPACE) {
      bodyY += topHeight;
      bodyHeight -= topHeight;
    }
    if (bottomBar != null && bottomBarLayoutMode == BarLayoutMode.RESERVE_SPACE) {
      bodyHeight -= bottomHeight;
    }
    bodyScroller.resetSetPositions();
    bodyScroller.setRect(bodyX, bodyY, Math.max(0, bodyWidth), Math.max(0, bodyHeight));
    bodyScroller.setContentInsets(0, 0,
        topBar != null && topBarLayoutMode == BarLayoutMode.OVERLAY ? topHeight : 0,
        bottomBar != null && bottomBarLayoutMode == BarLayoutMode.OVERLAY ? bottomHeight : 0);

    if (topBarHost != null) {
      topBarHost.resetSetPositions();
      topBarHost.setRect(client.x, client.y, client.width, topHeight);
    }
    if (bottomBarHost != null) {
      bottomBarHost.resetSetPositions();
      bottomBarHost.setRect(client.x, client.y + client.height - bottomHeight, client.width, bottomHeight);
    }
  }

  @Override
  public void initUI() {
    int gap = 2;
    int n = items.length;
    /*
     * 48dp height - https://material.io/guidelines/patterns/navigation-drawer.html#navigation-drawer-specs
     */
    int itemH = 48;
    int prefH = n * itemH + gap * n;
    boolean isLR = animDir == LEFT || animDir == RIGHT;

    if (showElevation && animDir == LEFT) {
      this.transparentBackground = true;
      try {
        ImageControl ic = new ImageControl(new Image("totalcross/res/mat/drawer_shadow.9-hdpi.png"));
        ic.transparentBackground = true;
        ic.strechImage = true;
        ic.scaleToFit = true;
        add(ic, RIGHT, TOP, PREFERRED, FILL);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ImageException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    ScrollContainer sc = bodyScroller = new ScrollContainer(false, true);
    if (backImage != null) {
      try {
        sc.transparentBackground = true;
        Rect r = getClientRect();
        Image img = backImage.smoothScaledFixedAspectRatio(r.height, true);
        img = img.getClippedInstance(0, 0, r.width, r.height);
        img.alphaMask = backImageAlpha;
        add(new ImageControl(img), LEFT, TOP, FILL, FILL);
      } catch (Throwable t) {
        t.printStackTrace(); // don't add the image
      }
    }
    add(sc, LEFT, TOP, FILL, FILL);
    sc.setBackColor(backColor);
    if (header != null) {
      /*
       * header background aspect ratio of 16:9
       * https://material.io/guidelines/layout/metrics-keylines.html#metrics-keylines-ratio-keylines 
       */
			sc.add(header, LEFT, TOP, FILL,	widthInPixels > 0 ? (widthInPixels * 9 / 16) : (PARENTSIZE + (percWidth * 9 / 16)));
    }
    for (int i = 0; i < n; i++) {
      Control tmi = items[i];
      tmi.appId = i + 1;
      sc.add(tmi, LEFT, AFTER, FILL, tmi instanceof TopMenu.Item ? DP + 48 : PREFERRED);
      if (i == n - 1) {
        break;
      }
      if (drawSeparators) {
        Ruler r = new Ruler(Ruler.HORIZONTAL, false);
        r.setBackColor(backColor);
        if (separatorColor != -1) {
          r.setForeColor(separatorColor);
        }
        sc.add(r, LEFT, AFTER, FILL, gap);
      }
    }
    rebuildBarHost(true);
    rebuildBarHost(false);
    sc.resize();
    layoutMenu();
  }

  @Override
  protected void onBoundsChanged(boolean screenChanged) {
    super.onBoundsChanged(screenChanged);
    layoutMenu();
  }

  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case ControlEvent.PRESSED:
      if (autoClose && e.target != this && ((Control) e.target).isChildOf(this)
          && !(((Control) e.target).parent instanceof AccordionContainer)) {
        selected = ((Control) e.target).appId - 1;
        postPressedEvent();
        unpop();
      }
      break;
    case PenEvent.PEN_DRAG_END:
      DragEvent de = (DragEvent) e;
      if (sameDirection(animDir, de.direction) && de.xTotal >= width / 2) {
        unpop();
      }
      break;
    }
  }

  private boolean sameDirection(int animDir, int dragDir) {
    if (animDir < 0) {
      animDir = -animDir;
    }
    return (dragDir == DragEvent.LEFT && animDir == LEFT) || (dragDir == DragEvent.RIGHT && animDir == RIGHT)
        || (dragDir == DragEvent.UP && animDir == TOP) || (dragDir == DragEvent.DOWN && animDir == BOTTOM);
  }

  public void screenResized() {
    presentationController.relayout();
  }

  /**
   * Unpops the current TopMenu
   */
  public void unpop() {
    unpop(null);
  }

  /**
   * Unpops the current TopMenu with the given animation listener. This listener
   * will be notified when the unpop animation ends.
   * 
   * @param alist
   */
  public void unpop(AnimationListener alist) {
    this.alist = alist;
    presentationController.unpop();
  }

  @Override
  public PresentationEntry createPresentationEntry() {
    PresentationTransition transition = animDir == CENTER
        ? new FadePresentationTransition()
        : new SlidePresentationTransition(animDir, fadeOnPopAndUnpop);
    return new PresentationEntry(this, PresentationEntry.Layer.OVERLAY,
        new PresentationEntry.BoundsResolver() {
          @Override
          public void resolve(Rect viewport, Rect bounds) {
            int menuWidth = resolveWidth(viewport);
            int menuHeight = animDir == LEFT || animDir == RIGHT ? viewport.height
                : Math.min(viewport.height * 3 / 4, items.length * 50);
            int x = animDir == RIGHT ? viewport.width - menuWidth
                : animDir == CENTER ? (viewport.width - menuWidth) / 2 : 0;
            int y = animDir == BOTTOM ? viewport.height - menuHeight
                : animDir == CENTER ? (viewport.height - menuHeight) / 2 : 0;
            bounds.set(x, y, menuWidth, menuHeight);
          }
        }, transition, true, true, true, -1, totalTime);
  }

  @Override
  public void onPresentationPopup() {
    selected = -1;
  }

  @Override
  public void postPresentationPopup() {
  }

  @Override
  public void onPresentationUnpop() {
  }

  @Override
  public void postPresentationUnpop() {
    if (alist != null) {
      AnimationListener listener = alist;
      alist = null;
      listener.onAnimationFinished();
    }
  }

  private boolean isTransitioning() {
    PresentationHandle handle = presentationController.handle();
    return handle != null && (handle.state() == PresentationHandle.State.PRESENTING
        || handle.state() == PresentationHandle.State.DISMISSING);
  }

  PresentationHandle presentationHandle() {
    return presentationController.handle();
  }

  public int getSelectedIndex() {
    return selected;
  }

  /** Returns if this control fades in/out on pop/unpop */
  public boolean isFadeOnPopAndUnpop() {
    return fadeOnPopAndUnpop;
  }

  /** sets if this control fades in/out on pop/unpop */
  public void setFadeOnPopAndUnpop(boolean fadeOnPopAndUnpop) {
    this.fadeOnPopAndUnpop = fadeOnPopAndUnpop;
  }
}
