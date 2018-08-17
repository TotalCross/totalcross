package totalcross.ui;

import java.util.ArrayList;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.ControlAnimation.AnimationFinished;
import totalcross.ui.anim.PathAnimation;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Graphics;

/** A Container that can be expanded or collapsed
 * 
 * Sample:
 * <pre>
 * AccordionContainer ac = new AccordionContainer();
 * ac.maxH = fmH*10;
 * add(ac, LEFT+50,TOP+100,FILL-50,ac.minH);
 * ac.add(ac.new Caption("Type text"), LEFT,TOP,FILL,PREFERRED);
 * ac.add(new MultiEdit(),LEFT+50,AFTER+50,FILL-50,FONTSIZE+700);
 * </pre> 
 * 
 * Note that when the container is changing its height, it calls <code>parent.reposition</code> to open space for its growth.
 */

public class AccordionContainer extends ClippedContainer implements PathAnimation.SetPosition, AnimationFinished {
  public static int ANIMATION_TIME = 300;

  public static class Group {
    ArrayList<AccordionContainer> l = new ArrayList<AccordionContainer>(5);

    public void collapseAll(boolean showAnimation) {
      for (AccordionContainer a : l) {
        if (a.isExpanded()) {
          a.collapse(showAnimation);
          if(a.acCaption != null)
        	  a.acCaption.invert();
        }
      }
    }

    public void collapseAll() {
      this.collapseAll(true);
    }
  }

  /** The minH defines the height of this container when it is collapsed.
   * If defined as a negative value, the minimum height will be computed as -minH * font_height
   */
  public int minH = fmH + Edit.prefH;
  private Caption acCaption;
  private Group group;
  private boolean showUIErrorsOld;

  public AccordionContainer() {
  }

  public AccordionContainer(Group g) {
    g.l.add(this);
    group = g;
  }

  public class Caption extends Container {
    public Button btExpanded, btCollapsed;
    public Label lCaption;

    public Caption(String caption) {
      this.lCaption = new Label(caption);
      if(acCaption == null)
    	  acCaption = this;
    }

    public Caption(Label lCaption, Button btExpanded, Button btCollapsed) {
      this.lCaption = lCaption;
      this.btExpanded = btExpanded;
      this.btCollapsed = btCollapsed;
      if(acCaption == null)
    	  acCaption = this;
    }

    @Override
    public void initUI() {
      if (btExpanded == null) {
        btExpanded = new ArrowButton(Graphics.ARROW_DOWN, fmH / 2, foreColor);
        btExpanded.setBorder(Button.BORDER_NONE);
      }
      if (btCollapsed == null) {
        btCollapsed = new ArrowButton(Graphics.ARROW_RIGHT, fmH / 2, foreColor);
        btCollapsed.setBorder(Button.BORDER_NONE);
      }
      add(btExpanded, LEFT, TOP, PREFERRED, FILL);
      add(btCollapsed, SAME, SAME, SAME, SAME);
      add(lCaption, uiAdjustmentsBasedOnFontHeightIsSupported ? AFTER + 50 : AFTER + fmH / 2, CENTER);
      btExpanded.setVisible(false);
    }

    @Override
    public int getPreferredHeight() {
      return fmH + Edit.prefH;
    }

    public void invert() {
      postPressedEvent();
      if (isExpanded()) {
        collapse();
      } else {
        expand();
      }
      boolean b = isExpanded();
      btExpanded.setVisible(!b);
      btCollapsed.setVisible(b);
    }

    @Override
    public void onEvent(Event e) {
      PenEvent pe;
      switch (e.type) {
      case ControlEvent.PRESSED:
        if (e.target == btExpanded || e.target == btCollapsed) {
          invert();
        }
        break;
      case PenEvent.PEN_DOWN:
        Window.needsPaint = true;
        break;
      case PenEvent.PEN_UP:
        Window.needsPaint = true;
        pe = (PenEvent) e;
        if ((!Settings.fingerTouch || !hadParentScrolled()) && isInsideOrNear(pe.x, pe.y)) {
          invert();
        }
        break;
      }
    }
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

  @Override
  public int getPreferredHeight() {
    return minH < 0 ? (fmH + Edit.prefH) * -minH : minH;
  }
}
