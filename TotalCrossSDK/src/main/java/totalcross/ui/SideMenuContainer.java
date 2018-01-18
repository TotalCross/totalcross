package totalcross.ui;

import totalcross.io.IOException;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/**
 * Template for navigation based on a menu using a navbar and a sidenav.
 *
 * <p><strong>This is an incubating feature, therefore backwards compatibility is not guaranteed
 * </strong>
 */
public class SideMenuContainer extends Container {

  private Bar bar;

  private TopMenu topMenu;

  private Container content;

  private Control home;

  private Control[] items;

  private static final int BACK_COLOR = Color.getRGB(248, 248, 248);

  public SideMenuContainer(String title, Control... items) {
    // cannot use empty constructor for Bar, otherwise we won't be able to use setTitle later
    bar = new Bar("");
    bar.drawBorders = false;
    bar.backgroundStyle = Container.BACKGROUND_SOLID;
    try {
      bar.addButton(new Image("totalcross/res/menu-hamburger.png"), false);
    } catch (ImageException e1) {
      // should never happen
      e1.printStackTrace();
    } catch (IOException e1) {
      // should never happen
      e1.printStackTrace();
    }

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
        }
      }
    }

    topMenu = new TopMenu(items, LEFT, Window.RECT_BORDER);
    topMenu.percWidth = 85;
    topMenu.totalTime = 250;
    topMenu.autoClose = true;
    topMenu.fadeOtherWindows = true;
    topMenu.setBackColor(Color.WHITE);
    topMenu.setTitle(title);
    topMenu.titleGap = fmH;
    topMenu.titleAlign = LEFT + 25;
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
    add(bar, LEFT, TOP, FILL, PREFERRED);
    add(content = new Container(), LEFT, AFTER, FILL, FILL);

    if (home instanceof SideMenuContainer.Item) {
      swap(((SideMenuContainer.Item) home));
    }
  }

  private void swap(SideMenuContainer.Item item) {
    bar.setTitle(item.caption);
    content.removeAll();
    content.add(item.presenter.getView(), LEFT, TOP, FILL, FILL);
  }

  @Override
  public void setForeColor(int color) {
    topMenu.titleColor = color;
    bar.setForeColor(color);
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

    public Item(String caption, Image icon, final Presenter<Container> presenter) {
      super(caption, icon);
      this.caption = caption;
      this.presenter = presenter;
    }
  }
}
