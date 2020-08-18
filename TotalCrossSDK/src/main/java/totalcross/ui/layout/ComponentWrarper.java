package totalcross.ui.layout;

import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.util.UnitsConverter;

/**Helper class to force a given width and height to a control.
 * This class is used automatically by the layout containers and
 * should not be used manually.
 */
public class ComponentWrarper extends Container
{
  private Control component;
  private int w;
  private int h;

  /**
   * Creates a component wrarper for the given component with the
   * specified width and height
   * @param component
   * @param w
   * @param h
   */
  public ComponentWrarper(Control component, int w, int h)
  {
    this.component = component;
    this.w = w;
    this.h = h;
  }

  @Override public void initUI()
  {
    super.initUI();

    this.add(component, LEFT, TOP, PARENTSIZE, PARENTSIZE);
  }

  @Override public int getPreferredWidth()
  {
    return UnitsConverter.toPixels(w);
  }
  @Override public int getPreferredHeight()
  {
    return UnitsConverter.toPixels(h);
  }
}
