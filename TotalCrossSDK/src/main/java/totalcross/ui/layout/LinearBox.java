package totalcross.ui.layout;

import totalcross.ui.Container;
import totalcross.ui.Control;


/**
 * Base class for HBox and VBox components. Contains most of the functionality needed
 * for these layouts to work. 
 * This class should not be used explicitly or inherited.
 * @author João, Ygor
 */
public abstract class LinearBox extends Container {
	/** Organizes each element around the center */
	public static final int LAYOUT_STACK_CENTER = 2;
	/** Distributes the elements along the width of the container */
	public static final int LAYOUT_DISTRIBUTE = 3;
	/** Distribute and scale each element to fill the entire width of this component */
	public static final int LAYOUT_FILL = 4;
	
	/** Aligns each child along the left/top border */
	public static final int ALIGNMENT_LEFT = 0;
	/** Aligns each child along the right/bottom border */
	public static final int ALIGNMENT_RIGHT = 1;
	/** Centers each child object */
	public static final int ALIGNMENT_CENTER = 2;
	/** Stretches each child object */
	public static final int ALIGNMENT_STRETCH = 3;
	

	/** The mode that the components will be displayed. */
	protected int mode;
	/** The elements alignment with regard to this component. */
	protected int alignment;
	/** The spacing between each component. */
	protected int spacing = 0;
	/**
	 * Switches if the 'add' method will also layout each component or if this is
	 * deferred until a 'resumeLayout' call
	 */
	protected boolean suspendLayout = false;

	public LinearBox() {
		this(LAYOUT_STACK_CENTER, ALIGNMENT_CENTER);
	}
	public LinearBox(int mode, int alignment) {
		uiAdjustmentsBasedOnFontHeightIsSupported = false;
		setLayout(mode, alignment);
	}

	@Override
	public void setRect(int x, int y, int width, int height, Control relative, boolean screenChanged) {
		super.setRect(x, y, width, height, relative, screenChanged);
		doLayout();
	}

	/**Adds the given control to this component and performs all layout operations
	 * to fit it within the layout. Consider using the 'suspendLayout' and 
	 * 'resumeLayout' methods if you plan on adding many controls at once.
	 */
	@Override
	public void add(Control control) {
		super.add(control);
		if (!suspendLayout && this.setX != SETX_NOT_SET) {
			suspendLayout = true;
			resizeElements(this.mode, this.alignment);
			suspendLayout = false;
		}
	}
	
	/**Adds the given control to this component using the given width and height
	 * and performs all layout operations to fit it within the layout. 
	 * Consider using the 'suspendLayout' and  'resumeLayout' methods if you plan 
	 * on adding many controls at once.
	 */
	@Override
	public void add(Control control, int w, int h) {
		this.add(new ComponentWrarper(control, w, h));
	}
	/** Adds several controls to this layout at once */
	public void add(Control[] controls) {
		if (this.suspendLayout) { // If the user manually asked to suspend layout, do not interfere
			for (Control control : controls) {
				add(control);
			}
		} else { // If no suspend layout, do it automatically to improve performance
			this.suspendLayout();
			for (Control control : controls) {
				add(control);
			}

			this.resumeLayout();
		}
	}
	/** This layout does not supports this action. Use the add(control) or add(control, w, h) instead.*/
	@Override
	public void add(Control control, int x, int y, int w, int h) {
		throw new RuntimeException("This layout does not supports this action. Use the add(control), add(control, w, h) or pin(control, x, y, w, h) instead.");
	}
	/** This layout does not supports this action. Use the add(control) or add(control, w, h) instead.*/
	@Override
	public void add(Control control, int x, int y, int w, int h, Control relative) {
		throw new RuntimeException("This layout does not supports this action. Use the add(control), add(control, w, h) or pin(control, x, y, w, h) instead.");
	}
	
	
	/** Used internally to add controls to this layout */
	protected void internalAdd(Control control, int x, int y, int w, int h, Control relative) {
		super.add(control, x, y, w, h, relative);
	}

	

	/**
	 * Suspends all layout operations from 'add' calls until a 'resumeLayout' call.
	 */
	public void suspendLayout() {
		this.suspendLayout = true;
	}

	/**
	 * Performs all queued layout operations and resumes the default layout
	 * behaviour of the 'add' method.
	 */
	public void resumeLayout() {
		if (!this.suspendLayout) {
			throw new RuntimeException("Calling 'resumeLayout' without first calling 'suspendLayout'");
		}
		
		if (this.setX != SETX_NOT_SET) {
			resizeElements(this.mode, this.alignment);
			suspendLayout = false;
		}
	}

	/**Recalculates every element's width and height, and also the paddings and
	 * spacings every time one of these values are changed.
	 */
	protected void doLayout() {
		if (this.setX != SETX_NOT_SET) {
			resizeElements(mode, alignment);
		}
	}
	/**
	 * Recalculates every element's width and height, and also the paddings and
	 * spacings every time one of these values are changed.
	 */
	protected abstract void resizeElements(int mode, int alignment);


	/** Sets the layout mode of this component */
	public void setLayout(int mode, int alignment) {
		this.mode = mode;
		this.alignment = alignment;
		if (!suspendLayout && this.setX != SETX_NOT_SET) {
			resizeElements(mode, alignment);
		}
	}

	/** Gets the spacing between components. */
	public int getSpacing() {
		return spacing;
	}

	/** Sets the spacing between components. */
	public void setSpacing(int spacing) {
		this.spacing = spacing;
	}

	/** Sets the internal paddings of this component. */
	@Override
	public void setInsets(int left, int right, int top, int bottom) {
		super.setInsets(left, right, top, bottom);
		if (!suspendLayout && this.setX != SETX_NOT_SET) {
			resizeElements(mode, alignment);
		}
	}
}