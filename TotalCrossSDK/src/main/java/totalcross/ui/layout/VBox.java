package totalcross.ui.layout;

import totalcross.sys.Settings;
import totalcross.ui.Control;

/**
 * A vertical flow layout component. Use this layout to place components
 * sequentially in the vertical axis. For custom placements, use the specialized
 * constructor or the 'setLayout' method. Using this component, you can easily
 * stack, distribute or fill a area with components.
 * @author João, Ygor
 */
public class VBox extends LinearBox
{
  /** Organizes each element from left to right */
  public static final int LAYOUT_STACK_TOP = 0;
  /** Organizes each element from right to left */
  public static final int LAYOUT_STACK_BOTTOM = 1;

  /** Creates a new VBox component with the default layout options */
  public VBox() { super(LAYOUT_STACK_TOP, ALIGNMENT_CENTER); }
  /** Creates a new VBox component with the given layout options */
  public VBox(int mode, int aligment) { super(mode, aligment); }

  @Override protected void resizeElements(int layout, int alignment)
  {
    if (numChildren == 0) {
      return;
    }

    Control[] elements = getChildren();
    this.removeAll();

    int xpos = 0;
    int ypos_first = 0;
    int ypos = 0;
    int width = 0;
    int height = PREFERRED;
    int spacing = this.spacing;
    int effectiveBoxHeight = this.height - insets.top - insets.bottom;
    switch (alignment) {
      case ALIGNMENT_LEFT:
        xpos = Control.LEFT + insets.left;
        width = Control.PREFERRED;
        break;
      case ALIGNMENT_RIGHT:
        xpos = Control.RIGHT - insets.right;
        width = Control.PREFERRED;
        break;
      case ALIGNMENT_CENTER:
        xpos = Control.CENTER;
        width = Control.PREFERRED;
        break;
      case ALIGNMENT_STRETCH:
        xpos = Control.CENTER;
        width = PARENTSIZE;
        break;
    }

    int heightSum = 0;
    for (Control control : elements) {
      heightSum += control.getPreferredHeight();
    }

    switch (mode) {
      case LAYOUT_STACK_TOP:
      case LAYOUT_STACK_BOTTOM:
        boolean isLeft = mode == LAYOUT_STACK_TOP;
        ypos_first = isLeft ? TOP : BOTTOM;
        ypos = isLeft ? AFTER : BEFORE;
        spacing = isLeft ? this.spacing : -this.spacing;
        break;
      case LAYOUT_STACK_CENTER:
        heightSum += spacing * (elements.length - 1);
        ypos_first = (this.height - heightSum) / 2;
        ypos = AFTER;
        break;
      case LAYOUT_DISTRIBUTE:
        spacing = (effectiveBoxHeight - heightSum) / (elements.length - 1);
        ypos_first = TOP;
        ypos = AFTER;
        break;
      case LAYOUT_FILL:
        height = effectiveBoxHeight / elements.length;
        height -= spacing;
        ypos_first = TOP;
        ypos = AFTER;
        break;
    }

    if (layout == LAYOUT_STACK_BOTTOM) {
      ypos_first -= insets.bottom;
    } else {
      ypos_first += insets.top;
    }

    this.internalAdd(
      elements[elements.length - 1], xpos, ypos_first, width, height, null);
    for (int i = elements.length - 2; i >= 0; i--) {
      this.internalAdd(
        elements[i], xpos, ypos + spacing, width, height, elements[i + 1]);
    }
  }

  /**
   * Reposition this control, calling again setRect with the original
   * parameters.
   */
  @Override public void reposition()
  {
    super.reposition();
    doLayout();
  }

  /** Returns the width of the biggest child component */
  @Override public int getPreferredWidth()
  {
    int maxWidth = 0;
    for (Control child : this.getChildren()) {
      maxWidth = Math.max(maxWidth, child.getPreferredWidth());
    }

    return maxWidth;
  }
  /** Returns the minimal height that encloses all components sequentially */
  @Override public int getPreferredHeight()
  {
    int totalHeight = insets.top + insets.bottom + spacing * (numChildren - 1);
    for (Control child : this.getChildren()) {
      totalHeight += child.getPreferredHeight();
    }

    return totalHeight;
  }
}
