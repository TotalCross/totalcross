// Copyright (C) 2001 Daniel Tauchke 
// Copyright (C) 2001-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui;

import totalcross.Launcher;
import totalcross.res.Resources;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Vm;
import totalcross.ui.effect.UIEffects;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.IntHashtable;
import totalcross.util.IntVector;
import totalcross.util.StringUtils;
import totalcross.util.UnitsConverter;
import totalcross.util.Vector;

/**
 * ListBox is a complete implementation of a Listbox. You can use the up/down
 * keys to scroll and enter the first letter of an item to select it.
 * <p>
 * Note: the color used in the <code>setBackColor()</code> method will be used
 * in the scrollbar only. The background color of the control will be a lighter
 * version of the given color.
 * <p>
 * Here is an example showing how it can be used:
 *
 * <pre>
 * import totalcross.ui.*;
 *
 * public class MyProgram extends MainWindow
 * {
 * ListBox lb;
 *
 * public void initUI()
 * {
 *   lb = new ListBox();
 *   add(lb);
 *   lb.add(new String[]{"Daniel","Jenny","Helge","Sandra"});
 *   lb.add("Marc");
 *   // you may set the rect by using PREFERRED only after the items were added.
 *   lb.setRect(LEFT,TOP,PREFERRED,PREFERRED); // use control's preferred width based on the size of the elements
 * }
 *
 * public void onEvent(Event event)
 * {
 *    switch (event.type)
 *    {
 *       case ControlEvent.PRESSED:
 *          if (event.target == lb)
 *             Object element = lb.getSelectedItem(); // in most cases, this is just a String and may be casted to such
 *    }
 * }
 * }
 * </pre>
 * 
 * The first item has index 0.
 */

public class ListBox extends Container implements Scrollable {
	protected Vector items = new Vector();
	protected int offset;
	protected int selectedIndex = -1, tempSelectedIndex = -1;
	protected int itemCount;
	protected int visibleItems;
	protected int btnX, btnX0;
	protected ScrollBar sbar;
	protected boolean simpleBorder; // used by PopList
	protected int xOffset; // guich@500_16
	protected int back0, back1;
	private int fColor;
	private int preferredItemHeight = UnitsConverter.toPixels(48 + DP);
	private int fourColors[] = new int[4];
	protected int customCursorColor = -1;
	private IntVector ivWidths;
	private int xOffsetMin;
	private ArrowButton btnLeft, btnRight;
	private int dragDistanceY, dragDistanceX; // kmeehl@tc100
	private boolean isScrolling;
	private Image npback;
	private boolean scScrolled;
	private boolean hasIconItemLeft, hasIconItemRight;
	private int biggestIconWidth = 0;
	private int paddingBorder = UnitsConverter.toPixels(16 + DP);
	public int paddingLeft = paddingBorder;
	public int paddingRight = paddingLeft;
	public int paddingTop = UnitsConverter.toPixels(8 + DP);
	public int paddingBottom = paddingTop;
	/**Resize the icon to the iconSize automatically*/
	public boolean autoResizeIcon = true;
	/**The size used when the icon is automatically resized*/
	public int iconSize = UnitsConverter.toPixels(24 + DP);

	/**
	 * The gap between the icon and the text. Used in IconItem. Defaults to fmH*4/3.
	 * If you plan to change this value, do it after calling setFont (if you call
	 * it).
	 * 
	 * @since TotalCross 1.61
	 */
	public int iconGap = UnitsConverter.toPixels(20 + DP);

	/** Set to false to disable border drawing. */
	@Deprecated
	public boolean drawBorder = true;

	/**
	 * Used to show an icon or two and a text. You can mix IconItem with other item types
	 * in the ListBox. 
	 * <br>
	 * Tip:
	 * The icons are automatically resized by default. If you don't want this, please set autoResizeIcon to false.
	 * 
	 * <br>
	 * Example:
	 * 
	 * <pre>
	 * lb.add("this is a simple text");
	 * lb.add(new IconItem("this is a text with icon", iconImage));
	 * </pre>
	 * 
	 * The icon should have the same size of the font's height, which can be set
	 * with:
	 * 
	 * <pre>
	 * iconImage = originalImage.smoothScaledFixedAspectRatio(fmH, true, -1);
	 * </pre>
	 * 
	 * @since TotalCross 1.61
	 */
	public static class IconItem {
		public String text;
		/** This is the icon of the item. It is highly recommended the size be 24DP. */
		public Image icon, iconRight;
		public static int marginBorder = UnitsConverter.toPixels(24 + DP);

		/**
		 * This receives the item text and left icon. It is highly recommended the icon
		 * size be 24DP.
		 * <br>
		 * Tip:
		 * The icons are automatically resized by default. If you don't want this, please set autoResizeIcon to false.
		 * <br>
		 * @param text     The item text.
		 * @param iconLeft The left icon of the item.
		 */
		public IconItem(String text, Image iconLeft) {
			this(text, iconLeft, null);
		}

		/**
		 * This receives the item text and the item left and right icons. It is highly
		 * recommended the icons sizes be 24DP.
		 * <br>
		 * Tip:
		 * The icons are automatically resized by default. If you don't want this, please set autoResizeIcon to false.
		 * <br>
		 * @param text      The item text.
		 * @param iconLeft  The left icon of the item.
		 * @param iconRight The right icon of the item.
		 */
		public IconItem(String text, Image iconLeft, Image iconRight) {
			this.text = text;
			this.icon = iconLeft;
			this.iconRight = iconRight;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	private void updateBiggestIconWidth(Object[] items) {
		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				Object item = items[i];
				if (item instanceof IconItem) {
					IconItem iconItem = (IconItem) item;
					if (iconItem.icon != null && biggestIconWidth < iconItem.icon.getWidth())
						biggestIconWidth = iconItem.icon.getWidth();
					if (iconItem.iconRight != null && biggestIconWidth < iconItem.iconRight.getWidth())
						biggestIconWidth = iconItem.iconRight.getWidth();
				}
			}
			getPreferredWidth();
			getPreferredHeight();
		}
	}

	/**
	 * An interface that makes easier to draw custom items. Example:
	 * 
	 * <pre>
	 * class ItemSeek implements ListBox.CustomDrawingItem {
	 * 	int tpsinc;
	 * 	boolean admin;
	 * 	String plat, date;
	 * 
	 * 	ItemSeek(String s) {
	 * 		// 21Wi2014/12/05
	 * 		tpsinc = s.charAt(0) - '0';
	 * 		admin = s.charAt(1) == '1';
	 * 		plat = s.substring(2, 4);
	 * 		date = s.substring(4);
	 * 	}
	 * 
	 * 	public void onItemPaint(Graphics g, int dx, int dy, int w, int h) {
	 * 		g.drawText(data, dx, dy);
	 * 		// and also other items
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @since TotalCross 3.1
	 */
	public static interface CustomDrawingItem {
		public void onItemPaint(Graphics g, int dx, int dy, int w, int h);
	}

	/**
	 * When the ListBox has horizontal buttons and its height divided by the button
	 * height is greater than this value (10), the horizontal button heights are
	 * increased.
	 * 
	 * @see #extraHorizScrollButtonHeight
	 * @see #enableHorizontalScroll()
	 */
	public static int EXTRA_HEIGHT_FACTOR = 10;
	/**
	 * IntHashtable used to specify different background colors for some items.
	 * Example:
	 * 
	 * <pre>
	 * list.ihtBackColor = new IntHashtable(10);
	 * ihtBackColors.put(10, 0xAABBCC); // will make line number 10 with back color 0xAABBCC.
	 * </pre>
	 * 
	 * Specify a null value if you want to use the default back color (this also
	 * makes drawing faster). Note that its up to you to update the hashtable if an
	 * item is inserted or removed.
	 * 
	 * @since TotalCross 1.0 beta 4
	 */
	public IntHashtable ihtBackColors;

	/**
	 * IntHashtable used to specify different foreground colors for some items.
	 * Example:
	 * 
	 * <pre>
	 * list.ihtForeColor = new IntHashtable(10);
	 * ihtForeColors.put(10, Color.RED); // will make line number 10 with fore color RED.
	 * </pre>
	 * 
	 * Specify a null value if you want to use the default fore color (this also
	 * makes drawing faster). Note that its up to you to update the hashtable if an
	 * item is inserted or removed.
	 * 
	 * @since TotalCross 1.0 beta 4
	 */
	public IntHashtable ihtForeColors;

	/**
	 * The extra height of the horizontal scroll buttons. Defaults 2 in 160x160 or a
	 * multiple of it in other resolutions.
	 * 
	 * @see #EXTRA_HEIGHT_FACTOR
	 * @see #enableHorizontalScroll()
	 */
	public int extraHorizScrollButtonHeight = Settings.screenHeight * 2 / 160; // guich@560_11: now depends on the
																				// resolution

	/**
	 * The Flick object listens and performs flick animations on PenUp events when
	 * appropriate.
	 */
	protected Flick flick;

	/**
	 * Sets the number of visible lines, used to make PREFERRED height return the
	 * given number of lines as the grid height.
	 * 
	 * @since TotalCross 1.13
	 */
	public int visibleLines = -1;

	/**
	 * If true, all ListBox will have the selection bar drawn in the full width
	 * instead of the selected's text width
	 * 
	 * @since SuperWaba 5.5
	 */
	public static boolean useFullWidthOnSelection; // guich@550_21

	public static final double DEFAULT_ITEM_HEIGHT_FACTOR = 1.5d;
	/**
	 * In finger touch devices, sets a factor by which the font height will be
	 * multiplied to increase the item's height. Defaults to 1.5 when
	 * Settings.fingerTouch is true, and 1 when its false.
	 * 
	 * You can change this value before the constructor and restore it after the
	 * constructor to change the height of a single ListBox.
	 * 
	 * <pre>
	 * ListBox.itemHeightFactor = 1;
	 * ... create listbox
	 * ListBox.itemHeightFactor = ListBox.DEFAULT_ITEM_HEIGHT_FACTOR;
	 * </pre>
	 * 
	 * @since TotalCross 1.5
	 */
	public static double itemHeightFactor = uiAndroid ? DEFAULT_ITEM_HEIGHT_FACTOR : 1;

	protected double ihFactor = itemHeightFactor;

	/** Used by the DBListBox to store the data column that is displayed. */
	protected int dataCol = -1;

	/**
	 * <p>
	 * Creates an empty Listbox.
	 * </p>
	 * 
	 * <b>Tips:</b>
	 * 
	 * <ul>
	 * <li>You can edit the paddings of this control by changing the "paddingLeft",
	 * "paddingRight", "paddingTop" and "paddingBottom" attributes.</li>
	 * 
	 * <li>If you're using ListBox.IconItem, you can edit the icon gap by changing
	 * the "iconGap" attribute.</li>
	 * </ul>
	 */
	public ListBox() {
		this(null);
	}

	/**
	 * <p>
	 * Creates a Listbox with the given items.
	 * </p>
	 * 
	 * <b>Tips:</b>
	 * 
	 * <ul>
	 * <li>You can edit the paddings of this control by changing the "paddingLeft",
	 * "paddingRight", "paddingTop" and "paddingBottom" attributes.</li>
	 * 
	 * <li>If you're using ListBox.IconItem, you can edit the icon gap by changing
	 * the "iconGap" attribute.</li>
	 * </ul>
	 */
	public ListBox(Object[] items) {
		ignoreOnAddAgain = ignoreOnRemove = true;
		sbar = Settings.fingerTouch ? new ScrollPosition() : new ScrollBar();
		sbar.focusTraversable = false;
		if (uiMaterial) {
			sbar.transparentBackground = true;
		}
		super.add(sbar);
		sbar.setLiveScrolling(true);
		if (items != null) {
			this.items = new Vector(items);
			itemCount = items.length;
		}
		sbar.setMaximum(itemCount);
		this.focusTraversable = true; // kmeehl@tc100
		if (Settings.fingerTouch) {
			flick = new Flick(this);
			flick.shortestFlick = 50; // make the listbox more responsive
		}
		iconGap = fmH * 4 / 3;
		if (uiMaterial) {
			useFullWidthOnSelection = true;
		}
		effect = UIEffects.get(this);
		hasIconItemLeft = hasIconItemLeft();
		hasIconItemRight = hasIconItemRight();
		updateBiggestIconWidth(items);
		setNinePatch(Resources.listbox, 14, 14);
	}

	@Override
	public boolean flickStarted() {
		dragDistanceX = dragDistanceY = 0;
		return isScrolling; // only start flick if already scrolling
	}

	@Override
	public void flickEnded(boolean atPenDown) {
	}

	@Override
	public boolean canScrollContent(int direction, Object target) {
		if (Settings.fingerTouch) {
			switch (direction) {
			case DragEvent.UP:
				return sbar.getValue() > sbar.getMinimum();
			case DragEvent.DOWN:
				return (sbar.getValue() + sbar.getVisibleItems()) < sbar.getMaximum();
			case DragEvent.LEFT:
				return xOffset < 0;
			case DragEvent.RIGHT:
				return xOffset > xOffsetMin;
			}
		}
		return false;
	}

	@Override
	public boolean scrollContent(int xDelta, int yDelta, boolean fromFlick) {
		boolean hFlick = xDelta != 0 && ivWidths != null;
		boolean vFlick = yDelta != 0;
		int itemH = getItemHeight(0);

		if (hFlick) {
			if ((xDelta < 0 && xOffset >= 0) || (xDelta > 0 && xOffset <= xOffsetMin)) {
				hFlick = false;
			} else {
				dragDistanceX += xDelta;
				if (dragDistanceX <= -itemH || dragDistanceX >= itemH) {
					int offsetDelta = dragDistanceX / itemH;
					dragDistanceX %= itemH;

					xOffset += -offsetDelta * itemH; // invert signal to follow weird onPaint implementation
					if (xOffset < xOffsetMin) {
						xOffset = xOffsetMin;
					} else if (xOffset > 0) {
						xOffset = 0;
					}

					enableButtons();
					Window.needsPaint = true;
				}
			}
		}
		if (vFlick) {
			int cur = sbar.getValue();

			if ((yDelta < 0 && cur <= sbar.getMinimum()) || (yDelta > 0 && cur >= sbar.getMaximum())) {
				vFlick = false;
			} else {
				dragDistanceY += yDelta;
				if (dragDistanceY <= -itemH || dragDistanceY >= itemH) {
					int offsetDelta = dragDistanceY / itemH;
					dragDistanceY %= itemH;

					sbar.setValue(offset + offsetDelta);
					int newOffset = sbar.getValue();

					if (newOffset == offset) {
						vFlick = false;
					} else {
						if (!fromFlick) {
							sbar.tempShow();
						}
						offset = newOffset;
						Window.needsPaint = true;
					}
				}
			}
		}

		return hFlick || vFlick;
	}

	@Override
	public int getScrollPosition(int direction) {
		if (direction == DragEvent.LEFT || direction == DragEvent.RIGHT) {
			return xOffset;
		}
		return offset;
	}

	/**
	 * Adds support for horizontal scroll on this listbox. Two buttons will appear
	 * below the vertical scrollbar. The add, replace and remove operations will be
	 * a bit slower because the string's width will have to be computed in order to
	 * correctly set the max horizontal scroll.
	 * 
	 * @since SuperWaba 5.6
	 * @see #extraHorizScrollButtonHeight
	 */
	public void enableHorizontalScroll() // guich@560_9
	{
		if (itemCount > 0) {
			int n = itemCount, m = 0, w;
			int[] widths = new int[n];
			for (int i = 0; i < n; i++) {
				if ((w = widths[i] = fm.stringWidth(items.items[i].toString())) > m) {
					m = w;
				}
			}
			ivWidths = new IntVector(widths);
			verifyItemWidth(m);
		} else {
			ivWidths = new IntVector();
			xOffsetMin = 0;
			enableButtons();
		}
	}

	private void enableButtons() {
		if (xOffset < xOffsetMin) {
			xOffset = xOffsetMin;
		}
		if (btnLeft != null) {
			btnLeft.setEnabled(isEnabled() && xOffset < 0);
			btnRight.setEnabled(isEnabled() && xOffset > xOffsetMin);
		}
	}

	/** Adds an array of Objects to the Listbox */
	public void add(Object[] moreItems) {
		int size = moreItems.length;
		if (itemCount == 0) // guich@310_5: directly assign the array if this listbox is empty
		{
			Object[] array = new Object[size];
			Vm.arrayCopy(moreItems, 0, array, 0, size);
			this.items = new Vector(array);
			itemCount = size;
			if (ivWidths != null) {
				enableHorizontalScroll(); // just recompute for all items
			}
		} else {
      int n = size; //moreItems.length; // guich@450_36
			itemCount += n;
			for (int i = 0; i < n; i++) {
				items.addElement(moreItems[i]);
			}
			if (ivWidths != null) // guichich@560_9
			{
				int w, m = 0, mx = -xOffsetMin;
				for (int i = 0; i < n; i++) {
					ivWidths.addElement(w = fm.stringWidth(moreItems[i].toString()));
					if (w > mx) {
						m = w;
					}
				}
				verifyItemWidth(m);
			}
		}
		sbar.setEnabled(isEnabled() && visibleItems < itemCount);
		sbar.setMaximum(itemCount); // guich@210_12: forgot this line!

		hasIconItemLeft = hasIconItemLeft();
		hasIconItemRight = hasIconItemRight();
		updateBiggestIconWidth(moreItems);
	}

	/**
	 * Adds a range of an array of Objects to the Listbox
	 * 
	 * @deprecated Use {@link #add(Object[])} instead
	 */
	@Deprecated
	public void add(Object[] moreItems, int startAt, int size) {
		int realSize = moreItems.length < startAt + size ? moreItems.length - startAt : size;
		if (itemCount == 0) // guich@310_5: directly assign the array if this listbox is empty
		{
			Object[] array = new Object[realSize];
			Vm.arrayCopy(moreItems, startAt, array, 0, realSize);
			this.items = new Vector(array);
			itemCount = realSize;
			if (ivWidths != null) {
				enableHorizontalScroll(); // just recompute for all items
			}
		} else {
      int n = realSize; //moreItems.length; // guich@450_36
			itemCount += n;
			for (int i = startAt; i < n; i++) {
				items.addElement(moreItems[i]);
			}
			if (ivWidths != null) // guichich@560_9
			{
				int w, m = 0, mx = -xOffsetMin;
				for (int i = 0; i < n; i++) {
					ivWidths.addElement(w = fm.stringWidth(moreItems[i].toString()));
					if (w > mx) {
						m = w;
					}
				}
				verifyItemWidth(m);
			}
		}
		sbar.setEnabled(isEnabled() && visibleItems < itemCount);
		sbar.setMaximum(itemCount); // guich@210_12: forgot this line!

		hasIconItemLeft = hasIconItemLeft();
		hasIconItemRight = hasIconItemRight();
		updateBiggestIconWidth(moreItems);
	}

	/** Adds an Object to the Listbox */
	public void add(Object item) {
		items.addElement(item);
		if (ivWidths != null) // guich@560_9
		{
			int w = fm.stringWidth(item.toString());
			ivWidths.addElement(w);
			verifyItemWidth(w);
		}
		itemCount++;
		sbar.setEnabled(isEnabled() && visibleItems < itemCount);
		sbar.setMaximum(itemCount);
		getPreferredWidth();

		Object[] items = new Object[1];
		items[0] = item;

		hasIconItemLeft = hasIconItemLeft();
		hasIconItemRight = hasIconItemRight();
		updateBiggestIconWidth(items);
	}

	/**
	 * Adds the given text to this ListBox, breaking the text if it goes beyond the
	 * ListBox' limits, and also breaking if it contains \n. Returns the number of
	 * lines. Note that each part of the text is considered a new item. This method
	 * is slower than the other <code>add</code> methods.
	 * 
	 * @since TotalCross 1.24
	 */
	public int addWrapping(String text) // guich@tc124_21
	{
		if (fm.stringWidth(text) <= btnX && text.indexOf('\n') < 0) {
			add(text);
			return 1;
		}
		String[] lines = Convert.tokenizeString(Convert.insertLineBreak(btnX, fm, text), '\n');
		add(lines);
		return lines.length;
	}

	/** Adds an Object to the Listbox at the given index */
	public void insert(Object item, int index) {
		items.insertElementAt(item, index);
		if (ivWidths != null) // guich@560_9
		{
			int w = fm.stringWidth(item.toString());
			ivWidths.insertElementAt(w, index);
			verifyItemWidth(w);
		}
		itemCount++;
		sbar.setEnabled(isEnabled() && visibleItems < itemCount);
		sbar.setMaximum(itemCount);

		hasIconItemLeft = hasIconItemLeft();
		hasIconItemRight = hasIconItemRight();
		Object[] items = new Object[1];
		items[0] = item;
		updateBiggestIconWidth(items);
	}

	/**
	 * Empties this ListBox, setting all elements of the array to <code>null</code>
	 * so they can be garbage collected. <b>Attention!</b> If you used the same
	 * object array to initialize two ListBoxes (or ComboBoxes), this method will
	 * null both ListBoxes (because they use the same array reference), and you'll
	 * get a null pointer exception!
	 */
	@Override
	public void removeAll() // guich@210_13
	{
		items.removeAllElements();
		sbar.setMaximum(0);
		itemCount = 0;
		offset = 0; // wolfgang@330_23
		xOffset = xOffsetMin = 0;
		if (ivWidths != null) // guich@560_9
		{
			ivWidths.removeAllElements();
			enableButtons();
		}
		selectedIndex = -1; // seanwalton@401.26
		Window.needsPaint = true;

		hasIconItemLeft = false;
		hasIconItemRight = false;
		biggestIconWidth = 0;
	}

	/** Removes the Object at the given index from the Listbox */
	public void remove(int itemIndex) // guich@200final_12: new method
	{
		if (0 <= itemIndex && itemIndex < itemCount) {
			items.removeElementAt(itemIndex);
			itemCount--;
			if (ivWidths != null) // guich@560_9
			{
				int old = ivWidths.items[itemIndex];
				ivWidths.removeElementAt(itemIndex);
				if (old == -xOffsetMin) // was this the max offset? recompute the remaining ones
				{
					int m = 0;
					int[] widths = ivWidths.items;
					int n = ivWidths.size();
					for (int i = 0; i < n; i++) {
						if (widths[i] > m) {
							m = widths[i];
						}
					}
					verifyItemWidth(m);
				}
			}
			sbar.setMaximum(itemCount);
			sbar.setEnabled(isEnabled() && visibleItems < itemCount);

			if (selectedIndex == itemCount) {
				setSelectedIndex(selectedIndex - 1);
			}
			if (itemCount == 0) {
				selectedIndex = -1;
			}
			if (itemCount <= visibleItems && offset != 0) {
				offset = 0;
			}
			Window.needsPaint = true;
		}
		hasIconItemLeft = hasIconItemLeft();
		hasIconItemRight = hasIconItemRight();
		updateBiggestIconWidth(items.items);
	}

	/** Removes an Object from the Listbox */
	public void remove(Object item) {
		int index;
		if (itemCount > 0 && (index = items.indexOf(item)) != -1) {
			remove(index);
		}
		hasIconItemLeft = hasIconItemLeft();
		hasIconItemRight = hasIconItemRight();
		updateBiggestIconWidth(items.items);
	}

	/** Replace the Object at the given index, starting from 0 */
	public void setItemAt(int i, Object s) {
		if (0 <= i && i < itemCount) {
			items.items[i] = s;
			if (ivWidths != null) {
				verifyItemWidth(ivWidths.items[i] = fm.stringWidth(s.toString()));
			}
			Window.needsPaint = true;
		}
	}

	/**
	 * Get the Object at the given Index. Returns an empty string if the index is
	 * outside of range.
	 */
	public Object getItemAt(int i) {
		if (0 <= i && i < itemCount) {
			return items.items[i];
		}
		return "";
	}

	/**
	 * Returns the selected item of the Listbox or an empty String Object if none is
	 * selected
	 */
	public Object getSelectedItem() {
		int sel = getSelectedIndex();
		return sel >= 0 ? items.items[sel] : ""; // guich@200b4: handle no selected index yet.
	}

	/**
	 * Returns the position of the selected item of the Listbox or -1 if the listbox
	 * has no selected index yet.
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}

	/**
	 * Returns all items in this ListBox. If the elements are Strings, the array can
	 * be casted to String[].
	 */
	public Object[] getItems() {
		return items.toObjectArray();
	}

	/**
	 * Used internally
	 */
	protected Object[] getItemsArray() {
		return items.items;
	}

	/** Returns the index of the item specified by the name, or -1 if not found. */
	public int indexOf(Object name) {
		return items.indexOf(name);
	}

	/**
	 * Selects the given name. If the name is not found, the current selected item
	 * is not changed.
	 * 
	 * @since SuperWaba 4.01
	 */
	public void setSelectedItem(Object name) // guich@401_25
	{
		int idx = indexOf(name);
		if (idx != -1) {
			setSelectedIndex(idx);
		}
	}

	/** Select the given index and scroll to it if necessary. */
	public void setSelectedIndex(int i) {
		setSelectedIndex(i, Settings.sendPressEventOnChange);
	}

	/**
	 * Select the given index and scroll to it if necessary, sending or not the
	 * pressed event.
	 */
	public void setSelectedIndex(int i, boolean sendPressEvent) {
    if (0 <= i && i < itemCount && i != selectedIndex/* && height != 0*/) // guich@tc100: commented height!=0 otherwise Watch's combobox will not be set properly
		{
			int vi = sbar.getVisibleItems();
			int ma = sbar.getMaximum();
			if (offset + vi > ma) {
        offset = Math.max(ma - vi, 0); // guich@220_4: fixed bug when the listbox is greater than the current item count
			}

			selectedIndex = i;

			if (selectedIndex >= offset + vi) // kmeehl@tc100
			{
				offset = selectedIndex - vi + 1;
				sbar.setValue(offset);
			} else if (selectedIndex < offset) {
				offset = selectedIndex;
				sbar.setValue(offset);
			}
			Window.needsPaint = true;
			if (sendPressEvent) {
				postPressedEvent();
			}
		} else if (i == -1) // guich@200b4_191: unselect all items
		{
			offset = 0;
			selectedIndex = -1;
			if (height != 0) {
				sbar.setValue(0);
				Window.needsPaint = true;
			}
			if (sendPressEvent) {
				postPressedEvent();
			}
		}
	}

	/**
	 * Selects the last item added to this listbox, doing a scroll if needed. Calls
	 * repaintNow.
	 * 
	 * @since SuperWaba 5.6
	 */
	public void selectLast() {
		selectLast(true);
	}

	/**
	 * Selects the last item added to this listbox, doing a scroll if needed, and
	 * sending or not the event. Calls repaintNow.
	 */
	public void selectLast(boolean sendPressEvent) {
		if (itemCount > 0) {
			setSelectedIndex(itemCount - 1, sendPressEvent);
			repaintNow();
		}
	}

	/** Returns the number of items */
	public int size() {
		return itemCount;
	}

	/** Do nothing. Adding a control to a ListBox is nonsense. */
	@Override
	public void add(Control control) {
		Vm.warning("add(Control) cannot be used in the ListBox class!");
	}

	/** Do nothing. Removing a control from a ListBox is nonsense. */
	@Override
	public void remove(Control control) {
		Vm.warning("remove(Control) cannot be used in the ListBox class!");
	}

	/**
	 * Returns the preferred width, ie, the size of the largest item plus the size
	 * of the scrollbar.
	 */
	@Override
	public int getPreferredWidth() {
	    if (uiMaterial) {
    		return (hasIconItemLeft || hasIconItemRight ? UnitsConverter.toPixels(112 - biggestIconWidth + DP)
    				: UnitsConverter.toPixels(112 + DP)) + (hasIconItemLeft ? iconGap + biggestIconWidth : 0)
    				+ (hasIconItemRight ? iconGap + biggestIconWidth : 0) + paddingLeft + paddingRight;
	    }
	    int extra = (simpleBorder ? 4 : 6);
	    if (!Settings.fingerTouch && sbar.isVisible()) {
	      extra += sbar.getPreferredWidth();
	    }
	    int maxWidth = 0;
	    for (int i = itemCount - 1; i >= 0; i--) {
	      int w = getItemWidth(i);
	      if (w > maxWidth) {
	        maxWidth = w;
	      }
	    }

	    return maxWidth + extra + insets.left + insets.right;
	}

	/** Returns the number of items multiplied by the font metrics height */
	@Override
	public int getPreferredHeight() {
	    if (uiMaterial) {
    		int n = visibleLines == -1 ? itemCount : visibleLines; // guich@tc113_11: use visibleLines if set
    		int lineH = getItemHeight(0);
    		int h = Math.max(lineH * n, sbar.getPreferredHeight());
    		if (ivWidths != null && h < 4 * lineH) {
    			h = 4 * lineH;
    		}
    		return (n == 1 ? h - 1 : h) + paddingTop + paddingBottom;
	    }
	    
	    int n = visibleLines == -1 ? itemCount : visibleLines; // guich@tc113_11: use visibleLines if set
	    int lineH = getItemHeight(0);
	    int h = Math.max(lineH * n, sbar.getPreferredHeight()) + (simpleBorder ? 4 : 6);
	    if (ivWidths != null && h < 4 * lineH) {
	      h = 4 * lineH;
	    }
	    return (n == 1 ? h - 1 : h) + insets.top + insets.bottom;
	}

	/**
	 * This is needed to recalculate the box size for the selected item if the
	 * control is resized by the main application
	 */
	@Override
	protected void onBoundsChanged(boolean screenChanged) {
		iconGap = fmH * 4 / 3;
		npback = null;
		int btnW = sbar.getPreferredWidth();
		int extraHB = 0;
		if ((this.height / btnW > EXTRA_HEIGHT_FACTOR)) {
			extraHB = Settings.screenHeight * 4 / 160;
		}
		int m = uiFlat ? 0 : simpleBorder ? 1 : 2, n = 0;
		visibleItems = (height - m - 2) / getItemHeight(0);
		btnX = width - m - btnW;
		btnX0 = btnX;
		if (!sbar.isVisible()) {
			btnX = width - 1;
		}

		if (ivWidths != null) // guich@560_9: handle horiz scroll?
		{
			if (btnRight == null) {
				int hh = 3 * fmH / 11;
				super.add(btnRight = new ArrowButton(Graphics.ARROW_RIGHT, hh, foreColor));
				super.add(btnLeft = new ArrowButton(Graphics.ARROW_LEFT, hh, foreColor));
				btnRight.focusTraversable = btnLeft.focusTraversable = false;
        tabOrder.removeElement(btnRight); // guich@572_6: remove them from the tabOrder, otherwise it will block the control navigation in some situations (AllTests)
				tabOrder.removeElement(btnLeft);
				onColorsChanged(true); // guich@tc111_6
			}
			n = (btnRight.getPreferredHeight() + extraHorizScrollButtonHeight + extraHB) << 1;
			if (uiFlat) {
				n -= 2; // in flat, make the buttons overlap a bit
			}
			enableButtons();
		}
		sbar.setMaximum(itemCount);
		sbar.setVisibleItems(visibleItems);
		sbar.setEnabled(visibleItems < itemCount);
		if (Settings.fingerTouch) {
			sbar.setRect(RIGHT - 1, m, PREFERRED, FILL, null, screenChanged);
			if (ivWidths != null) {
				btnLeft.setVisible(false);
				btnRight.setVisible(false);
			}
		} else {
			sbar.setRect(btnX, m, btnW, height - (m << 1) - n, null, screenChanged);
		}
		if (Settings.keyboardFocusTraversable) {
			sbar.setFocusLess(true); // guich@570_39
		}

		if (ivWidths != null) // guich@560_9: handle horiz scroll?
		{
			n = uiFlat ? 1 : 0; // in flat, make the buttons overlap a bit
			// add the two horizontal scroll buttons below the scrollbar
      btnLeft.setRect(SAME, AFTER - n, SAME, PREFERRED + extraHorizScrollButtonHeight + extraHB, null, screenChanged);
      btnRight.setRect(SAME, AFTER - n, SAME, PREFERRED + extraHorizScrollButtonHeight + extraHB, null, screenChanged);
			btnLeft.repositionAllowed = btnRight.repositionAllowed = false; // we'll handle the reposition ourselves
		}
		if (visibleItems >= itemCount) {
			offset = 0;
		}
	}

	/**
	 * Searches this ListBox for an item with the first letter matching the given
	 * char. The search is made case insensitive. Note: if you override this class
	 * you must implement this method.
	 */
	protected void find(char c) {
		int i;
		c = Convert.toUpperCase(c); // dbeers@570_103: make sure that the letter is uppercased.
    int foundIndex = -1; // guich@450_30: fix search when exist repeating letters (cat, chicken, cow - pressing C 3 times)
		// first search from the next item
		for (i = selectedIndex + 1; i < itemCount; i++) {
			String s = items.items[i].toString(); // guich@220_37
			if (s.length() > 0 && Convert.toUpperCase(s.charAt(0)) == c) // first letter matches?
			{
				foundIndex = i;
				break;
			}
		}
		if (foundIndex == -1 && selectedIndex >= 0) {
			for (i = 0; i < selectedIndex; i++) {
				String s = items.items[i].toString(); // guich@220_37
				if (s.length() > 0 && Convert.toUpperCase(s.charAt(0)) == c) // first letter matches?
				{
					foundIndex = i;
					break;
				}
			}
		}
		if (foundIndex != -1) {
			setSelectedIndex(foundIndex);
			Window.needsPaint = true;
		}
	}

	/** Handles the events for this control. */
	@Override
	public void onEvent(Event event) {
		PenEvent pe;
		if (isEnabled()) {
			switch (event.type) {
			case ControlEvent.PRESSED:
				if (event.target == sbar) {
					int newOffset = sbar.getValue();
					if (newOffset != offset) // guich@200final_3: avoid unneeded repaints
					{
						offset = newOffset;
						Window.needsPaint = true;
					}
				} else if (event.target == btnLeft || event.target == btnRight) {
					horizontalScroll(event.target == btnLeft);
				}
				break;
			case KeyEvent.KEY_PRESS:
				find(Convert.toUpperCase((char) ((KeyEvent) event).key));
				break;
			case KeyEvent.SPECIAL_KEY_PRESS:
				KeyEvent ke = (KeyEvent) event;
				if (Settings.keyboardFocusTraversable && ke.isActionKey()) {
					postPressedEvent();
				} else if (ke.isPrevKey() || ke.isNextKey()) // guich@220_19 - guich@330_45
				{
					if (Settings.keyboardFocusTraversable) {
						if (ke.isUpKey()) {
              setSelectedIndex(Settings.circularNavigation ? (selectedIndex == 0 ? itemCount - 1 : (selectedIndex - 1))
									: Math.max(selectedIndex - 1, 0));
						} else if (ke.isDownKey()) {
              setSelectedIndex(Settings.circularNavigation ? (selectedIndex == itemCount - 1 ? 0 : (selectedIndex + 1))
									: Math.min(selectedIndex + 1, itemCount - 1));
						} else if (ke.key == SpecialKeys.LEFT) {
							if (!horizontalScroll(true)) {
								leftReached();
							}
						} else if (ke.key == SpecialKeys.RIGHT) {
							horizontalScroll(false);
						}
					} else if (ke.key == SpecialKeys.LEFT || ke.key == SpecialKeys.RIGHT) {
						horizontalScroll(ke.key == SpecialKeys.LEFT);
					} else {
						sbar._onEvent(event);
					}
				}
				break;
			case PenEvent.PEN_UP:
				if (event.target == this && !isScrolling) // if scrolling, do not end selection
				{
					pe = (PenEvent) event;
					if (Settings.fingerTouch) {
            handleSelection(((pe.y - (simpleBorder ? 3 : 4)) / getItemHeight(0)) + offset); // guich@200b4: corrected line selection
					}
					// Post the event
          int newSelection = ((pe.y - (simpleBorder ? 3 : 4)) / getItemHeight(0)) + offset; // guich@200b4_2: corrected line selection
					if (isInsideOrNear(pe.x, pe.y) && pe.x < btnX && newSelection < itemCount) {
						postPressedEvent();
					}
					endSelection();
				}
				isScrolling = false;
				break;
			case PenEvent.PEN_DRAG:
				DragEvent de = (DragEvent) event;

				if (Settings.fingerTouch) {
					if (isScrolling) {
						scrollContent(-de.xDelta, -de.yDelta, true);
						event.consumed = true;
					} else {
						int direction = DragEvent.getInverseDirection(de.direction);
						event.consumed = true;
						if (canScrollContent(direction, de.target) && scrollContent(-de.xDelta, -de.yDelta, true)) {
							isScrolling = scScrolled = true;
						}
					}
				}
				break;
			case KeyEvent.ACTION_KEY_PRESS: // guich@tc113_9
				if (!(this instanceof MultiListBox) && selectedIndex >= 0) {
					boolean old = isHighlighting;
					postPressedEvent();
					isHighlighting = old;
				}
				break;
			case PenEvent.PEN_DOWN:
				scScrolled = false;
				pe = (PenEvent) event;
				if (event.target == this && pe.x < btnX && isInsideOrNear(pe.x, pe.y)) {
					int sel = ((pe.y - (simpleBorder ? 3 : 4)) / getItemHeight(0)) + offset;
					if (Settings.fingerTouch) {
						tempSelectedIndex = sel;
					} else {
						handleSelection(sel); // guich@200b4: corrected line selection
					}
				}
				break;
			}
		}
	}

	protected void leftReached() {
	}

	protected void endSelection() {
	}

	protected void handleSelection(int newSelection) {
		if (newSelection != selectedIndex && newSelection < itemCount) {
			if (transparentBackground) // guich@tc115_18: on transparent backgrounds, we must repaint everything
			{
				selectedIndex = newSelection;
				Window.needsPaint = true;
			} else {
        //Graphics g = getGraphics();
        //if (selectedIndex >= 0)
				// drawCursor(g,selectedIndex,false);
				selectedIndex = newSelection;
				Window.needsPaint = true;
        //drawCursor(g,selectedIndex,true);
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (internalSetEnabled(enabled, false)) {
			sbar.setEnabled(enabled && visibleItems < itemCount);
			if (btnLeft != null) {
				enableButtons();
			}
		}
	}

	@Override
	protected void onColorsChanged(boolean colorsChanged) {
		npback = null;
		fColor = getForeColor();
		back0 = Color.brighter(getBackColor());
		back1 = customCursorColor != -1 ? customCursorColor
        : (back0 != Color.WHITE) ? backColor : Color.getCursorColor(back0);//guich@300_20: use backColor instead of: back0.getCursorColor(); // guich@210_19
		if (fColor == back1) {
			fColor = foreColor;
		}
		if (!uiAndroid) {
			Graphics.compute3dColors(isEnabled(), backColor, foreColor, fourColors);
		}
		if (btnRight != null) {
			btnRight.setBackForeColors(uiVista ? back0 : backColor, foreColor);
			btnLeft.setBackForeColors(uiVista ? back0 : backColor, foreColor);
		}
		if (colorsChanged) {
			sbar.setBackForeColors(uiVista ? back0 : backColor, foreColor);
		}
	}

	@Override
	public void onPaint(Graphics g) {
		// Draw background and borders
	    if (uiMaterial) {
    		g.backColor = uiAndroid ? parent.backColor : back0;
    		if (!transparentBackground) {
    			if (npParts != null && npback == null) {
    				try {
    					npback = NinePatch.getInstance().getNormalInstance(npParts, width, height,
    							isEnabled() ? back0 : Color.interpolate(back0, parent.backColor), false);
    					npback.alphaMask = alphaValue;
    				} catch (ImageException e) {
    				}
    			}
    			NinePatch.tryDrawImage(g, npback, 0, 0);
    			g.foreColor = foreColor;
    		}
    		g.foreColor = fColor;
    
    		int dx = 0; // guich@580_41: changed from 3 to 2
    		int dy = 0;
    		if (uiFlat) {
    			dy--;
    		}
    		if (simpleBorder) {
    			dx--;
    			dy--;
    		}
    
    		if (getDoEffect() && effect != null) {
    			effect.paintEffect(g);
    		}
    
    		setTextAreaClip(g, dx, dy); // guich@tc100b4_5
    
    		int greatestVisibleItemIndex = Math.min(itemCount, visibleItems + offset); // code corrected by Bjoem Knafla
    		dx++;
    		drawItems(g, dx, dy, greatestVisibleItemIndex);
	    } else {
    	    g.backColor = uiAndroid ? parent.backColor : back0;
    	    if (!transparentBackground) {
    	      g.fillRect(0, 0, width, height); // guich@tc115_77: fill till end because the scrollbar may not being shown
    	    }
    	    if (drawBorder) {
    	      if (uiAndroid) {
    	        if (npback == null) {
    	          try {
    	            npback = NinePatch.getInstance().getNormalInstance(NinePatch.LISTBOX, width, height,
    	                isEnabled() ? back0 : Color.interpolate(back0, parent.backColor), false);
    	            npback.alphaMask = alphaValue;
    	          } catch (ImageException e) {
    	          }
    	        }
    	        NinePatch.tryDrawImage(g, npback, 0, 0);
    	      }
    	      g.foreColor = foreColor;
    	      if (!uiAndroid) {
    	        g.draw3dRect(0, 0, width, height, Graphics.R3D_CHECK, false, false, fourColors);
    	      }
    	    }
    	    g.foreColor = fColor;
    
    	    int dx = 2; // guich@580_41: changed from 3 to 2
    	    int dy = 3;
    	    if (uiFlat) {
    	      dy--;
    	    }
    	    if (simpleBorder) {
    	      dx--;
    	      dy--;
    	    }
    
    	    if (effect != null) {
    	      effect.paintEffect(g);
    	    }
    
    	    setTextAreaClip(g, dx, dy); // guich@tc100b4_5
    	    dx += xOffset;
    	    int greatestVisibleItemIndex = Math.min(itemCount, visibleItems + offset); // code corrected by Bjoem Knafla
    	    dx++;
    	    drawItems(g, dx, dy, greatestVisibleItemIndex);
	    }
	}

	@Override
	public int getEffectW() {
		return width - (getEffectX() + xOffset);
	}

	@Override
	public int getEffectH() {
		return getItemHeight(tempSelectedIndex);
	}

	@Override
	public int getEffectX() {
		int dx = 2;
		if (simpleBorder) {
			dx--;
		}
		return dx + xOffset;
	}

	@Override
	public int getEffectY() {
		int dy = 7;
		if (uiFlat) {
			dy--;
		}
		if (simpleBorder) {
			dy--;
		}
		return dy + (tempSelectedIndex - offset) * getItemHeight(tempSelectedIndex);
	}

	protected void drawItems(Graphics g, int dx, int dy, int greatestVisibleItemIndex) {
	    if (uiMaterial) {
    		int itemHeight = getItemHeight(0);
    		for (int i = offset; i < greatestVisibleItemIndex; dy += itemHeight, i++) {
    			drawItem(g, i, dx, dy); // guich@200b4: let the user extend ListBox and draw the items himself
    		}
    		drawSelectedItem(g, offset, greatestVisibleItemIndex);
	    } else {
    	    for (int i = offset; i < greatestVisibleItemIndex; dy += getItemHeight(i++)) {
    	        drawItem(g, i, dx, dy); // guich@200b4: let the user extend ListBox and draw the items himself
    	      }
    	      drawSelectedItem(g, offset, greatestVisibleItemIndex);
	    }
	}

	protected int getItemHeight(int i) {
		if (uiMaterial) {
    	     int itemHeight = preferredItemHeight;
    	        int biggestIconWidth = autoResizeIcon ? iconSize : this.biggestIconWidth;
    	        if (fmH >= preferredItemHeight)
    	            itemHeight += fmH + UnitsConverter.toPixels(17 + DP);
    	        if (biggestIconWidth > itemHeight)
    	            itemHeight = biggestIconWidth + UnitsConverter.toPixels(17 + DP);
    	        return itemHeight;
	    }
		return Settings.fingerTouch ? (int) (fmH * ihFactor) : fmH;
	}

	protected void setTextAreaClip(Graphics g, int dx, int dy) // guich@tc100b4_5: use a common routine to prevent errors
	{
		int yy = dy;
		if (uiMaterial) {
		    g.setClip(dx + 1, yy, width, Math.min(height - 2 - yy, getItemHeight(0) * visibleItems + 2));
		} else {
			g.setClip(dx + 1, yy, btnX - dx - 2,
                Math.min(height - (uiAndroid ? 2 : 1) - yy, getItemHeight(0) * visibleItems + (uiAndroid ? 1 : 2))); // guich@tc100b5_20: don't let get over the border - guich@tc115_77: if scrollbar is not shown, use the whole area
		}
	}

	protected void drawSelectedItem(Graphics g, int from, int to) {
		if (selectedIndex >= 0) {
			drawCursor(g, selectedIndex, true);
		}
	}

	/**
	 * Sets the cursor color for this ListBox. The default is equal to the
	 * background slightly darker.
	 */
	public void setCursorColor(int color) {
		this.customCursorColor = color;
		onColorsChanged(true);
	}

	/** You can extend ListBox and overide this method to draw the items */
	protected void drawItem(Graphics g, int index, int dx, int dy) {
	    if (uiMaterial) {
    		Object obj = items.items[index];
    		boolean isIconItem = obj instanceof IconItem;
    		IconItem iconItem = null;
    		int biggestIconWidth = autoResizeIcon ? iconSize : this.biggestIconWidth;
    		int right = biggestIconWidth;
    
    		dx += paddingLeft;
    		dy += paddingTop;
    		right += paddingRight;
    
    		if (obj == null) {
    			return;
    		}
    		if (obj instanceof CustomDrawingItem) {
    			((CustomDrawingItem) obj).onItemPaint(g, dx, dy, width, getItemHeight(index));
    			return;
    		}
    		if (isIconItem) {
    			try {
    				iconItem = ((IconItem) obj);
    				if (iconItem.icon != null) {
    					if(autoResizeIcon) {
    						Image newIcon = iconItem.icon.getSmoothScaledInstance(iconSize, iconSize);
    						g.drawImage(newIcon, dx, dy + getItemHeight(index) / 2 - newIcon.getHeight() / 2);
    					} else
    						g.drawImage(iconItem.icon, dx, dy + getItemHeight(index) / 2 - iconItem.icon.getHeight() / 2);
    				}
    			} catch (ImageException e) {
    				g.drawImage(iconItem.icon, dx, dy + getItemHeight(index) / 2 - iconItem.icon.getHeight() / 2);
    				if(Settings.onJavaSE)
    					e.printStackTrace();
    			}
    			try {
    				if(autoResizeIcon) {
    					Image newIcon = iconItem.iconRight.getSmoothScaledInstance(iconSize, iconSize);
    					g.drawImage(newIcon, width - right, dy + getItemHeight(index) / 2 - newIcon.getHeight() / 2);
    				} else
    					g.drawImage(iconItem.iconRight, width - right, dy + getItemHeight(index) / 2 - iconItem.iconRight.getHeight() / 2);
    			} catch (ImageException e) {
    				g.drawImage(iconItem.iconRight, width - right, dy + getItemHeight(index) / 2 - iconItem.iconRight.getHeight() / 2);
    				if(Settings.onJavaSE)
    					e.printStackTrace();
    			}
    		}
    
    		int textWidth = width - paddingLeft - paddingRight;
    
    		if (hasIconItemLeft)
    			textWidth -= biggestIconWidth + iconGap;
    		if (hasIconItemRight)
    			textWidth -= biggestIconWidth + iconGap;
    
    		String s = StringUtils.shortText(obj.toString(), font.fm, textWidth);
    
    		int f = g.foreColor;
    		if (ihtForeColors != null) {
    			g.foreColor = ihtForeColors.get(index, f);
    		}
    		if (ihtBackColors != null) {
    			int b = g.backColor;
    			g.backColor = ihtBackColors.get(index, b);
    			g.fillRect(dx, dy, useFullWidthOnSelection ? btnX : fm.stringWidth(s), getItemHeight(index));
    			g.backColor = b;
    		}
    
    		if (hasIconItemLeft) {
    			dx += biggestIconWidth + iconGap;
    		}
    
    		g.drawText(s, dx, dy + (!uiAndroid ? 0 : (getItemHeight(index) - fmH) / 2), textShadowColor != -1, textShadowColor); // guich@402_31: don't test for index out of bounds. this will be catched in the caller
    		g.foreColor = f;
	    } else {
    	    Object obj = items.items[index];
    	    if (obj == null) {
    	      return;
    	    }
    	    if (obj instanceof CustomDrawingItem) {
    	      ((CustomDrawingItem) obj).onItemPaint(g, dx, dy, width, getItemHeight(index));
    	      return;
    	    }
    	    if (obj instanceof IconItem) {
    	      g.drawImage(((IconItem) obj).icon, dx, dy);
    	      dx += iconGap;
    	    }
    	    String s = obj.toString();
    	    // guich@tc100b4: allow change of back/fore colors
    	    int f = g.foreColor;
    	    if (ihtForeColors != null) {
    	      g.foreColor = ihtForeColors.get(index, f);
    	    }
    	    if (ihtBackColors != null) {
    	      int b = g.backColor;
    	      g.backColor = ihtBackColors.get(index, b);
    	      g.fillRect(dx, dy, useFullWidthOnSelection ? btnX : fm.stringWidth(s), getItemHeight(index));
    	      g.backColor = b;
    	    }
    	    g.drawText(s, dx, dy + (!uiAndroid ? 0 : (getItemHeight(index) - fmH) / 2), textShadowColor != -1, textShadowColor); // guich@402_31: don't test for index out of bounds. this will be catched in the caller
    	    g.foreColor = f;
	    }
	}

	/** You can extend ListBox and overide this method to draw the items */
	protected void drawSelectedItem(Graphics g, int index, int dx, int dy, int w) {
//	    int textWidth = width - paddingLeft - paddingRight - arrowSize - 
//	            (this.captionIcon == null ? 0 : iconGap + this.captionIcon.getWidth());
//	    g.drawText(StringUtils.shortText(pop.lb.getText(), font.fm, textWidth), paddingLeft + (this.captionIcon == null ? 0 : iconGap + this.captionIcon.getWidth()), 
//	            height/2 + paddingTop - paddingBottom - fmH/2);
	    
	    
		g.drawText(
		        uiMaterial ?
		                StringUtils.shortText(getText(), font.fm, w) :
		        getText(), dx, dy, textShadowColor != -1, textShadowColor);
	}

	/**
	 * Returns the width of the given item index with the current fontmetrics. Note:
	 * if you overide this class you must implement this method.
	 */
	protected int getItemWidth(int index) {
		if (uiMaterial) {
		    return width;
		}
        Object obj = items.items[index];
        return obj == null ? 0 : fm.stringWidth(obj.toString()) + (obj instanceof IconItem ? iconGap : 0);
	}

	int getIndexY(int sel) {
		int dy = 3;
		if (uiFlat) {
			dy--;
		}
		if (simpleBorder) {
			dy--;
		}
		int ih = getItemHeight(sel);
		dy += (sel - offset) * ih;
		return dy + ih + fm.descent;
	}

	/** This method is used to draw the cursor around the desired item */
	protected void drawCursor(Graphics g, int sel, boolean on) {
	    if (uiMaterial) {
    		if (offset <= sel && sel < visibleItems + offset && sel < itemCount) // guich@555_10: fixed in all ui styles
    		{
    			g.foreColor = fColor; // guich@520_15: by using fillRect we must set to the textcolor
    			g.backColor = on ? getCursorColor(sel) : back0;
    
    			int dx = 0;
    			int dy = 0;
    			int ddx = dx;
    			if (uiFlat) {
    				dy--;
    			}
    			if (simpleBorder) {
    				dx--;
    				ddx--;
    				dy--;
    			}
    
    			setTextAreaClip(g, dx, dy); // guich@tc100b4_5
    
    			int ih = getItemHeight(sel);
    			dy += (sel - offset) * ih;
    
    			g.fillRect(dx - 1, dy + paddingTop - 1, width + 1, ih + 2); // pgr@520_4: if this is an image or an
    																		// antialiased font, using eraseRect will make
    																		// it ugly. - guich@552_7: added -1 to fix
    																		// cursor not overwriting border.
    
    			drawItem(g, sel, dx - ddx + 1, dy); // pgr@520_4
    			// if (on && getParentWindow() instanceof ComboBoxDropDown && !(this instanceof
    			// MultiListBox)) Window.updateScreen(); // guich@tc114_80: update screen before
    			// the combobox closes. not comparing with ComboBoxDropDown results in screen
    			// FLICKERing - guich@tc115_89: prevent flicker in MultiListBox
    		}
	    } else {
    	    if (offset <= sel && sel < visibleItems + offset && sel < itemCount) // guich@555_10: fixed in all ui styles
    	    {
    	      g.foreColor = fColor; // guich@520_15: by using fillRect we must set to the textcolor
    	      g.backColor = on ? getCursorColor(sel) : back0;
    
    	      int dx = 3; // guich@580_41: cursor must be drawn at 3 or will overwrite the border on a combobox with PalmOS style
    	      int dy = 3;
    	      if (uiFlat) {
    	        dy--;
    	      }
    	      if (simpleBorder) {
    	        dx--;
    	        dy--;
    	      }
    
    	      setTextAreaClip(g, dx - 1, dy); // guich@tc100b4_5
    
    	      int ih = getItemHeight(sel);
    	      dx += xOffset; // guich@552_24: added this to make scroll apply to the item
    	      dy += (sel - offset) * ih;
    	      int sw;
    	      if (useFullWidthOnSelection || (sw = getItemWidth(sel)) == 0) {
    	        sw = btnX - 4;
    	      }
    	      g.fillRect(dx - 1, dy - 1, sw + 2, ih + 2); // pgr@520_4: if this is an image or an antialiased font, using eraseRect will make it ugly. - guich@552_7: added -1 to fix cursor not overwriting border.
    	      drawItem(g, sel, dx, dy); // pgr@520_4
    	      //if (on && getParentWindow() instanceof ComboBoxDropDown && !(this instanceof MultiListBox)) Window.updateScreen(); // guich@tc114_80: update screen before the combobox closes. not comparing with ComboBoxDropDown results in screen FLICKERing - guich@tc115_89: prevent flicker in MultiListBox
    	    }
	    }
	}

	protected boolean hasIconItemLeft() {
		for (int i = 0; i < items.size(); i++)
			if (items.items[i] instanceof IconItem && ((IconItem) items.items[i]).icon != null) {
				paddingBorder = IconItem.marginBorder;
				paddingLeft = paddingRight = paddingBorder;
				getPreferredWidth();
				return true;
			}
		return false;
	}

	protected boolean hasIconItemRight() {
		for (int i = 0; i < items.size(); i++)
			if (items.items[i] instanceof IconItem && ((IconItem) items.items[i]).iconRight != null) {
				paddingBorder = IconItem.marginBorder;
				paddingLeft = paddingRight = paddingBorder;
				getPreferredWidth();
				return true;
			}
		return false;
	}

	protected int getCursorColor(int index) {
		return back1;
	}

	/** Sets the border of the listbox to be not 3d if flag is true. */
	public void setSimpleBorder(boolean simpleBorder) // guich@200b4_93
	{
		this.simpleBorder = simpleBorder;
	}

	/** Sorts the elements of this ListBox. The current selection is cleared. */
	public void qsort() // guich@220_35
	{
		items.qsort();
		setSelectedIndex(-1);
	}

	/**
	 * Sorts the elements of this ListBox. The current selection is cleared.
	 * 
	 * @param caseless Pass true to make a caseless sort, if the items are Strings.
	 */
	public void qsort(boolean caseless) // guich@tc113_5
	{
		if (size() > 0) {
			Convert.qsort(items.items, 0, items.size() - 1,
          (caseless && items.items[0] instanceof String) ? Convert.SORT_STRING_NOCASE : Convert.SORT_AUTODETECT, true);
			setSelectedIndex(-1);
		}
	}

	/**
	 * Check if it is needed to change the xOffsetMin based in the given item's text
	 * width
	 */
	private void verifyItemWidth(int w) {
		int newxOffsetMin = -Math.max(w - width + sbar.width + 5, 0);
		if (newxOffsetMin < xOffsetMin) {
			xOffsetMin = newxOffsetMin;
			enableButtons();
		}
	}

	/** Scrolls the grid horizontaly as needed */
	private boolean horizontalScroll(boolean toLeft) {
		int step = this.width >> 1;
		int newOffset = toLeft ? Math.min(xOffset + step, 0) : Math.max(xOffset - step, xOffsetMin);
		if (newOffset != xOffset) {
			xOffset = newOffset;
			enableButtons();
			Window.needsPaint = true;
			return true;
		}
		return false;
	}

	/** Clears this control, selecting index clearValueInt. */
	@Override
	public void clear() // guich@572_19
	{
		setSelectedIndex(clearValueInt);
	}

	@Override
	public void getFocusableControls(Vector v) {
		if (visible && isEnabled()) {
			v.addElement(this);
		}
	}

	@Override
	public Control handleGeographicalFocusChangeKeys(KeyEvent ke) // any change here must synchronize with MultiListBox'
	{
		if ((ke.isPrevKey() && !ke.isUpKey()) || (ke.isNextKey() && !ke.isDownKey())) {
			int oldXOffset = xOffset;
			_onEvent(ke);
			return (oldXOffset != xOffset) ? this : null;
		}
		if ((ke.isUpKey() && selectedIndex <= 0) || (ke.isDownKey() && selectedIndex == itemCount - 1)) {
			return null;
		}
		_onEvent(ke);
		return this;
	}

	/** Returns the string of the selected item or "" if none is selected. */
	public String getText() {
		return selectedIndex < 0 ? "" : getSelectedItem().toString();
	}

	/**
	 * Selects the item that starts with the given text
	 * 
	 * @param text            The text string to search for
	 * @param caseInsensitive If true, the text and all searched strings are first
	 *                        converted to lowercase.
	 * @return If an item was found and selected.
	 * @since TotalCross 1.13
	 */
	public boolean setSelectedItemStartingWith(String text, boolean caseInsensitive) // guich@tc113_2
	{
		if (caseInsensitive) {
			text = text.toLowerCase();
		}
		for (int i = 0; i < itemCount; i++) {
			String s = items.items[i].toString();
			if (caseInsensitive) {
				s = s.toLowerCase();
			}
			if (s.startsWith(text)) {
				setSelectedIndex(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * This method hides the scrollbar if its not needed, i.e., if horizontal scroll
	 * is disabled and the preferred height is smaller than the actual height. You
	 * may have to call <code>reposition</code> if this method returns true. You can
	 * call this method after all items were added.
	 * 
	 * @return True if the scrollbar was hidden.
	 * @since TotalCross 1.15
	 */
	public boolean hideScrollBarIfNotNeeded() // guich@tc115_77
	{
		boolean showSB = ivWidths != null || getPreferredHeight() > getHeight();
		if (sbar.isVisible() != showSB) {
			sbar.setVisible(showSB);
			btnX = showSB ? btnX0 : width - 1;
			return true;
		}
		return false;
	}

	@Override
	public Flick getFlick() {
		return flick;
	}

	@Override
	public boolean wasScrolled() {
		return scScrolled;
	}
}
