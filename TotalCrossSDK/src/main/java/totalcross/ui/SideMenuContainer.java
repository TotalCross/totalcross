package totalcross.ui;

import java.util.ArrayList;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.ControlAnimation.AnimationFinished;
import totalcross.ui.anim.PathAnimation;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.event.PressListener;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.icon.Icon;
import totalcross.ui.icon.IconType;
import totalcross.ui.icon.MaterialIcons;
import totalcross.ui.image.Image;

/**
 * Template for navigation based on a menu using a navbar and a sidenav.
 *
 * <p>
 * <strong>This is an incubating feature, therefore backwards compatibility is
 * not guaranteed </strong>
 */
public class SideMenuContainer extends Container implements PenListener {

  private Bar bar;

  public TopMenu topMenu;

  private Container content;

  private Control home;

  private Control[] items;

  private static final int BACK_COLOR = Color.getRGB(248, 248, 248);

  private static final int BAR_HEIGHT_IN_DP = 56;

  private int direction;

  public SideMenuContainer(String title, Control... items) {
    this(LEFT, title, items);
  }

  public SideMenuContainer(int direction, String title, Control... items) {
    this.direction = direction;
    // cannot use empty constructor for Bar, otherwise we won't be able to use setTitle later
    bar = new Bar("");
    Font medium = Font.getFont("Roboto Medium", false, 20);
    bar.setFont(medium != null ? medium : Font.getFont(bar.getFont().name, false, 20));
    bar.drawBorders = false;
    bar.backgroundStyle = Container.BACKGROUND_SOLID;
    Icon i = new Icon(MaterialIcons._MENU);
    bar.addButton(i, direction == RIGHT);

    this.items = items;
    if (items.length > 0) {
      home = items[0];
      for (final Control control : items) {
        control.setBackColor(Color.WHITE);
        if (control instanceof SideMenuContainer.Item) {
          control.addPressListener(new PressListener() {

            @Override
            public void controlPressed(ControlEvent e) {
              swap(((SideMenuContainer.Item) control));
            }
          });
        } else if (control instanceof SideMenuContainer.Sub) {
          Control[] children = ((SideMenuContainer.Sub) control).items;
          if (children == null) {
            continue;
          }
          for (Control control2 : children) {
            control2.setBackColor(Color.WHITE);
            if (control2 instanceof SideMenuContainer.Item) {
              control2.addPressListener(new PressListener() {

                @Override
                public void controlPressed(ControlEvent e) {
                  swap(((SideMenuContainer.Item) control2));
                }
              });
            }
          }
        }
      }
    }

    topMenu = new TopMenu(items, direction, title != null ? Window.RECT_BORDER : Window.NO_BORDER);
    topMenu.canDrag = false;
    topMenu.showElevation = true;
    if (direction == BOTTOM || direction == TOP) {
      topMenu.percWidth = 100;
    } else {
      // navigation drawer width - https://material.io/guidelines/patterns/navigation-drawer.html#navigation-drawer-specs
      int screenWidthInDp = (int) (Settings.screenWidth / Settings.screenDensity);
      topMenu.widthInPixels = (int) (Math.min(320, screenWidthInDp - BAR_HEIGHT_IN_DP) * Settings.screenDensity);
    }
    topMenu.totalTime = 400;
    topMenu.autoClose = true;
    topMenu.fadeOtherWindows = true;
    topMenu.setBackColor(Color.WHITE);
    topMenu.setFadeOnPopAndUnpop(false);
    if (title != null) {
      topMenu.setTitle(title);
      topMenu.titleGap = fmH;
      topMenu.titleAlign = LEFT + 25;
    }
    topMenu.setFont(this.getFont());

    bar.addPressListener(new PressListener() {
      @Override
      public void controlPressed(ControlEvent e) {
        if (bar.getSelectedIndex() == 1) {
          topMenu.popup();
        }
      }
    });

    setForeColor(Color.BLACK);
    setBackColor(BACK_COLOR);
  }

  @Override
  public void initUI() {
    add(bar, LEFT, direction == BOTTOM ? BOTTOM : TOP, FILL, DP + BAR_HEIGHT_IN_DP);
    add(content = new Container(), LEFT, direction == BOTTOM ? TOP : AFTER, FILL, direction == BOTTOM ? FIT : FILL);

    if (home instanceof SideMenuContainer.Item) {
      swap(((SideMenuContainer.Item) home));
    }

    content.getParentWindow().addPenListener(this);
    content.getParentWindow().callListenersOnAllTargets = true;
  }

  public void setBarFont(Font f) {
    bar.setFont(f);
  }

  /**
   * Presents the given SideMenuContainer.Item, swapping the caption and the
   * contents of this SideMenu.
   * 
   * @param item
   *          The item to be presented
   */
  public void swap(SideMenuContainer.Item item) {
    bar.setTitle(item.showTitle ? item.caption : "");
    content.removeAll();
    Control view = item.presenter.getView();
    content.add(view, LEFT, TOP, FILL, FILL);
    view.addPenListener(this);
  }

  @Override
  public void setForeColor(int color) {
    topMenu.titleColor = color;
    bar.setForeColor(color);
  }

  public void setItemForeColor(int color) {
    for (Control control : items) {
      control.setForeColor(color);
    }
  }

  @Override
  public void setBackColor(int color) {
    topMenu.setForeColor(color);
    bar.setBackColor(color);
  }

  public static class Item extends TopMenu.Item {
    private Presenter<Container> presenter;
    private String caption;
    private boolean showTitle = true;

    public Item(String caption, Image icon, final Presenter<Container> presenter) {
      super(caption, icon);
      this.caption = caption;
      this.presenter = presenter;
    }

    public Item(String caption, IconType icon, int iconColor, final Presenter<Container> presenter) {
      this(caption, icon, iconColor, true, presenter);
    }

    public Item(String caption, IconType icon, int iconColor, boolean showTitle, final Presenter<Container> presenter) {
      super(caption, new Container() {
        @Override
        public void initUI() {
          // 16dp left spacing - https://material.io/guidelines/patterns/navigation-drawer.html#navigation-drawer-specs
          this.setInsets((int) ((Settings.screenWidth < 320 ? 8 : 16) * Settings.screenDensity), 0, 0, 0);
          Icon i = new Icon(icon);
          i.setForeColor(iconColor);
          i.setAlpha(137);
          add(i, LEFT, CENTER);
        }
      });
      this.caption = caption;
      this.presenter = presenter;
      this.showTitle = showTitle;
      // List item: 14sp - https://material.io/guidelines/patterns/navigation-drawer.html#navigation-drawer-specs
      Font medium = Font.getFont("Roboto Medium", false, 14);
      this.setFont(medium != null ? medium : Font.getFont(14));
    }
  }

  private int lastXStart = width/2;
  private int lastYStart = height/2;
  @Override
  public void penDrag(DragEvent e) {
    double margin = 0.20;
    if (direction == LEFT && e.direction == DragEvent.RIGHT && e.xTotal > 150 && (e.x - e.xTotal) < width*(margin)) {
      topMenu.popup();
    }
    if (direction == TOP && e.direction == DragEvent.DOWN && e.yTotal > 150 && (e.y - e.yTotal) < height*(margin)) {
      topMenu.popup();
    }
    if (direction == RIGHT && e.direction == DragEvent.LEFT && e.xTotal < -150 && (e.x - e.xTotal) > width*(1-margin)) {
      topMenu.popup();
    }
    if (direction == BOTTOM && e.direction == DragEvent.UP && e.yTotal < -150 && (e.y - e.yTotal) > height*(1-margin)) {
      topMenu.popup();
    }
  }

  @Override
  public void penDown(PenEvent e) {
  }

  @Override
  public void penUp(PenEvent e) {
  }

  @Override
  public void penDragStart(DragEvent e) {
  }

  @Override
  public void penDragEnd(DragEvent e) {
  }

  public static class Sub extends ClippedContainer implements PathAnimation.SetPosition, AnimationFinished {

    private Group group;
    private boolean showUIErrorsOld;
    public static int ANIMATION_TIME = 300;

    private Caption caption;

    private Control[] items;

    public Sub(String name, Control... items) {
      this(new Caption(name), items);
    }

    public Sub(Caption caption, Control... items) {
      this.caption = caption;
      this.items = items;
    }

    @Override
    public void initUI() {
      add(caption, LEFT, TOP, FILL, DP + 48);
      if (items != null) {
        for (Control control : items) {
          control.setForeColor(parent.foreColor);
          add(control, (int) ((Settings.screenWidth < 320 ? 4 : 8) * Settings.screenDensity), AFTER, FILL, DP + 48);
        }
      }
    }

    @Override
    public int getPreferredHeight() {
      return (int) (Settings.screenDensity * 48);
    }

    @Override
    public void onAnimationFinished(ControlAnimation anim) {
      Window w = getParentWindow();
      if (w != null) {
        w.reposition();
      }
      Settings.showUIErrors = showUIErrorsOld;
    }

    @Override
    public void setPos(int x, int y) {
      this.height = setH = y;
      Window.needsPaint = true;
      Window w = getParentWindow();
      if (w != null) {
        w.reposition();
      }
    }

    private int getMaxHeight() {
      int maxH = 0, minH = Convert.MAX_INT_VALUE;
      for (Control child = children; child != null; child = child.next) {
        if (child.y < minH) {
          minH = child.y;
        }
        int yy = child.getY2();
        if (yy > maxH) {
          maxH = yy;
        }
      }
      return maxH + minH; // use the distance of the first container as a gap at bottom  
    }

    public void expand() {
      this.expand(true);
    }

    public void expand(boolean showAnimation) {
      if (group != null) {
        group.collapseAll(showAnimation);
      }
      final int maxH = getMaxHeight();
      if (showAnimation) {
        PathAnimation p = PathAnimation.create(this, 0, this.height, 0, maxH, this, ANIMATION_TIME);
        p.useOffscreen = false;
        p.setpos = this;
        showUIErrorsOld = Settings.showUIErrors;
        Settings.showUIErrors = false;
        p.start();
      } else {
        setPos(0, maxH);
        onAnimationFinished(null);
      }
    }

    public void collapse(boolean showAnimation) {
      if (showAnimation) {
        PathAnimation p = PathAnimation.create(this, 0, this.height, 0, getPreferredHeight(), this, ANIMATION_TIME);
        p.useOffscreen = false;
        p.setpos = this;
        showUIErrorsOld = Settings.showUIErrors;
        Settings.showUIErrors = false;
        p.start();
      } else {
        setPos(0, getPreferredHeight());
        onAnimationFinished(null);
      }
    }

    public void collapse() {
      this.collapse(true);
    }

    public boolean isExpanded() {
      return this.height != getPreferredHeight();
    }

    public static class Group {
      ArrayList<AccordionContainer> l = new ArrayList<>(5);

      public void collapseAll(boolean showAnimation) {
        for (AccordionContainer a : l) {
          if (a.isExpanded()) {
            a.collapse(showAnimation);
          }
        }
      }

      public void collapseAll() {
        this.collapseAll(true);
      }
    }

    public static class Caption extends Container {
      private IconType expanded;
      private IconType collapsed;
      private Label lCaption;
      private Icon expand;

      public Caption(String caption) {
        this(caption, MaterialIcons._ARROW_DROP_DOWN, MaterialIcons._ARROW_DROP_UP);
      }

      public Caption(String caption, IconType collapsed, IconType expanded) {
        this(new Label(caption, LEFT), collapsed, expanded);
      }

      public Caption(Label lCaption, IconType collapsed, IconType expanded) {
        this.lCaption = lCaption;
        this.collapsed = collapsed;
        this.expanded = expanded;

        lCaption.setAlpha(222);
        Font medium = Font.getFont("Roboto Medium", false, 14);
        this.setFont(medium != null ? medium : Font.getFont(14));
      }

      @Override
      public void initUI() {
        this.setInsets((int) ((Settings.screenWidth < 320 ? 8 : 16) * Settings.screenDensity), 0, 0, 0);

        expand = new Icon(collapsed);
        expand.setAlpha(137);
        add(expand, LEFT, CENTER);

        add(new Container() {
          @Override
          public void initUI() {
            /*
             * 72dp left spacing and 16 dp right spacing
             * https://material.io/guidelines/patterns/navigation-drawer.html#navigation-drawer-specs
             */
            this.setInsets((int) ((Settings.screenWidth < 320 ? 64 - 8 : 72 - 16) * Settings.screenDensity),
                (int) ((Settings.screenWidth < 320 ? 8 : 16) * Settings.screenDensity), 0, 0);
            add(lCaption, LEFT, CENTER, FILL, PREFERRED);
          }
        }, LEFT, CENTER, FILL, PREFERRED);
      }

      @Override
      public int getPreferredHeight() {
        return (int) (Settings.screenDensity * 48);
      }

      public void invert() {
        SideMenuContainer.Sub sub = (SideMenuContainer.Sub) this.parent;
        if (sub.isExpanded()) {
          sub.collapse();
        } else {
          sub.expand();
        }
        expand.setGlyph(sub.isExpanded() ? collapsed : expanded);
      }

      @Override
      public void onEvent(Event e) {
        PenEvent pe;
        switch (e.type) {
        case PenEvent.PEN_DOWN:
          Window.needsPaint = true;
          break;
        case PenEvent.PEN_UP:
          Window.needsPaint = true;
          pe = (PenEvent) e;
          if ((!Settings.fingerTouch || !hadParentScrolled()) && isInsideOrNear(pe.x, pe.y)) {
            invert();
          }
          e.consumed = true;
          break;
        }
      }
    }
  }
}
