package totalcross.ui.layout;

import totalcross.sys.Settings;
import totalcross.ui.Control;

/**
 * A horizontal flow layout component. Use this layout to place components sequentially
 * in the horizontal axis.
 * For custom placements, use the specialized constructor or the 'setLayout' method.
 * Using this component, you can easily stack, distribute or fill a area with components.
 * @author João, Ygor
 */
public class HBox extends LinearBox {
	/** Organizes each element from left to right */
	public static final int LAYOUT_STACK_LEFT = 0;
	/** Organizes each element from right to left */
	public static final int LAYOUT_STACK_RIGHT = 1;
	
	/** Creates a new HBox component with the default layout options */
	public HBox() {
		super(LAYOUT_STACK_LEFT, ALIGNMENT_CENTER);
	}
	/** Creates a new HBox component with the default layout options */
	public HBox(int mode, int aligment) {
		super(mode, aligment);
	}

	@Override
	protected void resizeElements(int layout, int alignment) {
		if (numChildren == 0) {
			return;
		}

		Control[] elements = getChildren();
		this.removeAll();
		
		int xpos_first = 0;
		int xpos = 0;
		int ypos = 0;
		int width = PREFERRED;
		int height = 0;
		int spacing = this.spacing;
		int effectiveBoxWidth = this.width - insets.left - insets.right;
		switch (alignment) {
		case ALIGNMENT_LEFT:
			ypos = Control.TOP + insets.top;
			height = Control.PREFERRED;
			break;
		case ALIGNMENT_RIGHT:
			ypos = Control.BOTTOM - insets.bottom;
			height = Control.PREFERRED;
			break;
		case ALIGNMENT_CENTER:
			ypos = Control.CENTER;
			height = Control.PREFERRED;
			break;
		case ALIGNMENT_STRETCH:
			ypos = Control.CENTER;
			height = PARENTSIZE;
			break;
		}
		
		int widthSum = 0;
		for (Control control : elements) {
			widthSum += control.getPreferredWidth();
		}
		
		switch (layout) {
		case LAYOUT_STACK_LEFT:
		case LAYOUT_STACK_RIGHT:
			boolean isLeft = layout == LAYOUT_STACK_LEFT;
			xpos_first = isLeft ? LEFT : RIGHT;
			xpos = isLeft ? AFTER : BEFORE;
			spacing = isLeft ? this.spacing : -this.spacing;
			break;
		case LAYOUT_STACK_CENTER:
			widthSum += spacing * (elements.length - 1);
			xpos_first = (this.width - widthSum) / 2;
			xpos = AFTER;
			break;
		case LAYOUT_DISTRIBUTE:
			spacing = (effectiveBoxWidth - widthSum) / (elements.length - 1);
			xpos_first = LEFT;
			xpos = AFTER;
			break;
		case LAYOUT_FILL:
			width = effectiveBoxWidth / elements.length;
			width -= spacing;
			xpos_first = LEFT;
			xpos = AFTER;
			break;
		}
		
		if (layout == LAYOUT_STACK_RIGHT) {
			xpos_first -= insets.right;
		} else {
			xpos_first += insets.left;
		}
		
		this.internalAdd(elements[elements.length - 1], xpos_first, ypos, width, height, null);
		for (int i = elements.length - 2; i >= 0; i--) {
			this.internalAdd(elements[i], xpos + spacing, ypos, width, height, elements[i + 1]);
		}
	}

	/** Reposition this control, calling again setRect with the original parameters. */
	@Override
	public void reposition() {
		super.reposition();
		doLayout();
	}

	/** Returns the minimal width that encloses all components sequentially */
	@Override
	public int getPreferredWidth() {
		int totalWidth = insets.left + insets.right + spacing * (numChildren - 1);
		for (Control child : this.getChildren()) {
	      totalWidth += child.getPreferredWidth();
	    }

		return totalWidth;
	}
	/** Returns the height of the biggest child component */
	@Override
	public int getPreferredHeight() {
		int maxHeight = 0;
		for (Control child : this.getChildren()) {
	    	maxHeight = Math.max(maxHeight, child.getPreferredHeight());
	    }

		return maxHeight;
	}
}
