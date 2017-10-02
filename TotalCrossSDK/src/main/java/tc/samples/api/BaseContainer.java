/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
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

package tc.samples.api;

import totalcross.res.Resources;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Vm;
import totalcross.ui.Bar;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.ListBox;
import totalcross.ui.MainWindow;
import totalcross.ui.Toast;
import totalcross.ui.UIColors;
import totalcross.ui.Window;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.KeyListener;
import totalcross.ui.event.PressListener;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.ElementNotFoundException;
import totalcross.util.Vector;

public class BaseContainer extends Container {
  public static final int BKGCOLOR = 0x0A246A;
  public static final int SELCOLOR = 0x829CE2; // Color.brighter(BKGCOLOR,120);
  protected Bar headerBar, footerBar;
  private static Vector containerStack = new Vector(5);
  public static String defaultTitle = "TotalCross API";
  protected int gap;
  public boolean isSingleCall;
  public String info;

  static {
    Toast.height = PREFERRED + Font.NORMAL_SIZE;
    Toast.posY = BOTTOM - Font.NORMAL_SIZE * 3;
  }

  protected String getHelpMessage() {
    return null;
  }

  public static Image getAwesomeImage(char c, int height, int color) throws ImageException {
    Font f = Font.getFont("FontAwesome", false, height);
    Image img = new Image(f.fm.charWidth(c), height);
    Graphics g = img.getGraphics();
    g.foreColor = color;
    g.setFont(f);
    g.drawText(String.valueOf(c), 0, 0);
    return img;
  }

  public <T extends Control> T setAwesome(T c, int fmH) {
    c.setFont(Font.getFont("FontAwesome", false, fmH));
    return c;
  }

  public Bar.BarButton createAwesomeBarButton(String title) {
    Bar.BarButton b = headerBar.new BarButton(title, null);
    b.buttonCanSelectTitle = true;
    b.buttonTitleAlign = CENTER;
    b.isShadedText = false;
    return setAwesome(b, font.size * 12 / 10);
  }

  @Override
  public void initUI() {
    try {
      transitionEffect = TRANSITION_OPEN;
      gap = fmH / 2;
      boolean isMainMenu = containerStack.size() == 1;
      int c1 = 0x0A246A;
      Font f = font.adjustedBy(2, true);
      headerBar = new Bar(defaultTitle);
      headerBar.setFont(f);
      headerBar.setBackForeColors(c1, Color.WHITE);
      headerBar.addControl(createAwesomeBarButton("\uf017"));
      if (isMainMenu) {
        headerBar.addControl(createAwesomeBarButton("\uf05a"));
      } else {
        headerBar.addButton(Resources.back);
      }
      add(headerBar, LEFT, 0, FILL, PREFERRED);

      footerBar = new Bar("");
      footerBar.uiAdjustmentsBasedOnFontHeightIsSupported = false;
      footerBar.setFont(f);
      footerBar.titleAlign = CENTER;
      footerBar.backgroundStyle = BACKGROUND_SOLID;
      footerBar.setBackForeColors(c1, Color.WHITE);
      setInsets(0, 0, headerBar.getHeight(), footerBar.getPreferredHeight());
      add(footerBar, LEFT, BOTTOM + insets.bottom, FILL, PREFERRED);
      // we use a PressListener so that the subclasses don't need to call super.onEvent
      headerBar.addPressListener(new PressListener() {
        @Override
        public void controlPressed(ControlEvent e) {
          e.consumed = true;
          try {
            switch (((Bar) e.target).getSelectedIndex()) {
            case 1: {
              Vm.gc();
              Vm.tweak(Vm.TWEAK_DISABLE_GC, true);
              int ini = Vm.getTimeStamp();
              for (int i = 0; i < 100; i++) {
                repaintNow();
              }
              int fim = Vm.getTimeStamp();
              Vm.tweak(Vm.TWEAK_DISABLE_GC, false);
              String s = "Paint 100x elapsed: " + (fim - ini) + "ms";
              Toast.show(s, 3000);
              Vm.debug(headerBar.getTitle() + " - " + s);
              break;
            }
            case 2: {
              boolean isMainMenu = containerStack.size() == 1;
              if (isMainMenu) {
                String helpMessage = getHelpMessage();
                if (helpMessage == null) {
                  return;
                }
                MessageBox mb = new MessageBox("Help", helpMessage, new String[] { "Close" });
                mb.transitionEffect = TRANSITION_FADE;
                mb.footerColor = mb.headerColor = UIColors.messageboxBack;
                //mb.setIcon(infoImg);
                mb.popup();
              } else {
                back();
              }
              break;
            }
            }
          } catch (Exception ee) {
            MessageBox.showException(ee, true);
          }
        }
      });

      Window.keyHook = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void actionkeyPressed(KeyEvent e) {
        }

        @Override
        public void specialkeyPressed(KeyEvent e) {
          if (e.key == SpecialKeys.ESCAPE) {
            e.consumed = true;
            back();
          }
        }
      };

      String name = getClass().getName();
      setTitle(name.endsWith("Sample") ? name.substring(name.lastIndexOf('.') + 1, name.length() - 6) : defaultTitle);
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }

  public void setTitle(String s) {
    headerBar.setTitle(s);
  }

  public String getTitle() {
    return headerBar.getTitle();
  }

  public void setInfo(String s) {
    if (footerBar != null) {
      footerBar.setTitle(s);
    }
  }

  public void show() {
    containerStack.push(this); // push ourself
    MainWindow.getMainWindow().swap(this);
  }

  public void back() {
    if (parent == null || (!(this instanceof MainMenu) && getParentWindow() == Window.getTopMost())) {
      try {
        setInfo(MainMenu.DEFAULT_INFO);
        containerStack.pop(); // pop ourself
        MainWindow.getMainWindow().swap((Container) containerStack.peek());
        Window.keyHook = null;
      } catch (ElementNotFoundException enfe) {
        //MainWindow.exit(0); // we're the last screen, so just exit the application
      }
    }
  }

  public boolean ask(String question) {
    MessageBox mb = new MessageBox("Question", question, new String[] { "Yes", "No" });
    mb.popup();
    return mb.getPressedButtonIndex() == 0;
  }

  // single place to add and log messages
  protected static ListBox lblog;

  public void addLog(int x, int y, int w, int h, Control rel) {
    addLog(this, x, y, w, h, rel);
  }

  public void addLog(Container parent, int x, int y, int w, int h, Control rel) {
    ListBox.itemHeightFactor = 1;
    parent.add(lblog = new ListBox(), x, y, w, h, rel);
    ListBox.itemHeightFactor = ListBox.DEFAULT_ITEM_HEIGHT_FACTOR;
  }

  // a log method that runs safely on threads
  public static void log(Object s) {
    log(s, true);
  }

  public static void log(Object s, boolean selLast) {
    if (s == null) {
      return;
    }
    Vm.debug(s.toString());
    final Object _s = s;
    final boolean _selLast = selLast;
    if (MainWindow.isMainThread()) {
      if (s instanceof String) {
        lblog.addWrapping((String) s);
      } else {
        lblog.add(s);
      }
      if (selLast) {
        lblog.selectLast();
      }
    } else {
      MainWindow.getMainWindow().runOnMainThread(new Runnable() {
        @Override
        public void run() {
          if (_s instanceof String) {
            lblog.addWrapping((String) _s);
          } else {
            lblog.add(_s);
          }
          if (_selLast) {
            lblog.selectLast();
          }
        }
      });
    }
  }
}
