// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.io.IOException;
import totalcross.res.Resources;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.ListContainerEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.IntHashtable;
import totalcross.util.IntVector;
import totalcross.util.StringUtils;
import totalcross.util.UnitsConverter;

import java.util.ArrayList;

/**
 * Creates a popup menu with a single line list and some radio buttons at right,
 * like in the Android combobox styles. This is a sample of how to use it:
 * 
 * <pre>
 * String[] items = { "Always", "Never", "Only in Silent mode", "Only when not in Silent mode",
 * 		"Non of the answers above", };
 * PopupMenu pm = new PopupMenu("Vibrate", items);
 * pm.popup();
 * </pre>
 * 
 * A PRESSED event is sent when an item is selected.
 * 
 * Note: the colors must be set before the control's bounds are defined using
 * setRect or add.
 * 
 */

public class PopupMenu extends Window {
	private ListBox menu;
	private Label caption;
	private Object[] items;
	private int selected = -1;
	private Image off, ball;
	private ListContainer list;
	private Button cancel/* ,ok - future */;
	private ListContainer.Item[] containers;
	private boolean multipleSelection;
	private int cursorColor = -1;
	private int maxH;
	private static final int UNSET = -9999;
	private int desiredSelectedIndex = UNSET;
	private IntHashtable htSearchKeys;
	private ArrayList<Button> searchButtons;
	Image npback = null;
	/** The string of the button; defaults to "Cancel" */
	public static String cancelString = "Cancel";
	/**
	 * If the items is a String matrix (String[][]), this field sets the column that
	 * will be shown.
	 */
	public int dataCol;

	/**
	 * Sets the number of elements should be used from the items array passed in the
	 * constructor. Defaults to <code>items.length</code>.
	 */
	public int itemCount;

	/**
	 * The check color used to fill the radio button used in Android. Defaults to
	 * the fore color.
	 * 
	 * @since TotalCross 1.3
	 */
	public int checkColor = -1;

	/**
	 * Set to true BEFORE popping up the window to enable search on the items of
	 * this PopupMenu. Note that it only works if the items are ORDERED.
	 * 
	 * @since TotalCross 1.5
	 */
	public boolean enableSearch;

	/**
	 * Set to false BEFORE popping up the window to disable the Cancel button.
	 * 
	 * @since TotalCross 1.65
	 */
	public boolean enableCancel = true;

	/**
	 * Set to true to keep the selected index unchanged if user press the Cancel
	 * button
	 * 
	 * @since TotalCross 2.0
	 */
	public boolean keepIndexOnCancel;

	@Override
	public boolean onClickedOutside(PenEvent event) {
		selected = -1;
		unpop();
		return false;
	}

	/**
	 * Constructs a PopupMenu with the given parameters and without multiple
	 * selection support.
	 */
	public PopupMenu(String caption, Object[] items) throws IOException, ImageException {
		this(caption, items, false);
	}

	/** Constructs a PopupMenu with the given parameters. */
	public PopupMenu(String caption, Object[] items, boolean multipleSelection) throws IOException, ImageException {
		super(caption, NO_BORDER);
		menu = new ListBox(items);
		menu.transparentBackground = true;

		int height = menu.getPreferredHeight();
		maxH = (int) (Settings.screenHeight * 0.60);
		int numItems = maxH / menu.getItemHeight(0) + 1;
		if (height > maxH)
			maxH = height = menu.getItemHeight(0) * numItems + menu.paddingBottom;

		
		this.caption = new Label(caption, LEFT, Color.BLACK, true);
		this.caption.transparentBackground = false;
		this.caption.setText(caption = StringUtils.shortText(caption, font.fm, width));

		this.multipleSelection = multipleSelection;
		uiAdjustmentsBasedOnFontHeightIsSupported = false;
		titleColor = uiMaterial ? Color.BLACK : Color.WHITE;

		this.items = items;
		itemCount = items.length;
		if (multipleSelection) {
			off = Resources.checkBkg.getCopy();
			ball = Resources.checkSel.getCopy();
		} else if (!uiMaterial) {
			off = Resources.radioBkg.getCopy();
			ball = Resources.radioSel.getCopy();
		}

		menu.addPressListener(new PressListener() {
			@Override
			public void controlPressed(ControlEvent e) {
				selected = menu.getSelectedIndex();
				unpop();
			}
		});

		setNinePatch(Resources.listbox, 14, 14);
	}

//	@Override
//	public void initUI() {
//		caption.transparentBackground = true;
//		this.caption.setText(
//				StringUtils.shortText(caption.getText(), font.fm, width - menu.paddingLeft - menu.paddingRight));
//		if (title != null && !title.equals(""))
//			add(caption, CENTER, TOP + fmH / 2);
//
//		add(menu, LEFT, title == null || title.equals("") ? TOP : AFTER + menu.paddingTop, FILL, FILL);
//	}
	private Image getSelectedImage(int color) throws ImageException {
		// "off" image is a composite of two images: on + selection
		Image on = off.getFrameInstance(0);
		ball.applyColor2(color); // paint it
		on.getGraphics().drawImage(ball, 0, 0);
		return on;
	}

	public void initUI() {
		try {
			caption.transparentBackground = true;
			this.caption.setText(
					StringUtils.shortText(caption.getText(), font.fm, width - menu.paddingLeft - menu.paddingRight));
			if (title != null && !title.equals("")) {
				System.out.println("Title");
				add(caption, CENTER, TOP + fmH / 2);
			}

			list = new ListContainer();
			list.setFont(this.font);
			if (cursorColor != -1) {
				list.highlightColor = cursorColor;
			}

			ListContainer.Layout layout = list.getLayout(3, 1);
			layout.insets.set(10, 50, 10, 50);
			if (multipleSelection || !uiMaterial) {
				layout.defaultRightImage = off;
				layout.defaultRightImage2 = getSelectedImage(checkColor == -1 ? foreColor : checkColor);
				layout.imageGap = 50;
				layout.controlGap = 50; // 50% of font's height
			}
			layout.centerVertically = true;
			layout.setup();
			int cw = -1;

			containers = new ListContainer.Item[itemCount];

			Vm.preallocateArray(new ListContainer.Item(layout), itemCount);
			Vm.preallocateArray(new String[3], itemCount);
			if (enableSearch && itemCount <= 10) {
				enableSearch = false;
			}
			htSearchKeys = new IntHashtable(40);
			char last = 0;
			for (int i = 0; i < itemCount; i++) {
				ListContainer.Item c = new ListContainer.Item(layout);
				containers[i] = c;
				String s = items[i] instanceof String ? (String) items[i]
						: (items[i] instanceof String[]) ? ((String[]) items[i])[dataCol] : items[i].toString();
				if (enableSearch && s.length() > 0) {
					char cc = s.charAt(0);
					if (cc != last) {
						last = cc;
						htSearchKeys.put(Convert.toUpperCase(cc), i);
					}
				}
				if (cw == -1) {
					cw = getClientRect().width - Math.abs(c.getLeftControlX()) - Math.abs(c.getRightControlX());
				}
				int sw = fm.stringWidth(s);
				if (sw <= cw) {
					c.items = new String[] { "", s, "" };
				} else {
					if(uiMaterial) {
						String[] parts = Convert.tokenizeString(Convert.insertLineBreak(cw, fm, s), '\n');
						c.items = new String[] { "", "", "" };
						for (int j = 0, n = Math.min(parts.length, c.items.length); j < n; j++) {
							c.items[j] = parts[j];
						}
					} else {
						c.items = new String[] {"", s, ""}; 
					}
				}
				c.appId = i;
			}
			if (htSearchKeys.size() <= 1) {
				enableSearch = false;
			}
			ScrollContainer sc2 = null;
			if (enableSearch) {
				IntVector v = htSearchKeys.getKeys();
				v.qsort();
				add(sc2 = new ScrollContainer(true, false),
						LEFT + UnitsConverter.toPixels(DP + 2),
						TOP + UnitsConverter.toPixels(DP + 8), FILL - UnitsConverter.toPixels(DP + 2),
						DP + 52);
				sc2.disableFlick();
				for (int i = 0; i < v.size(); i++) {
					String caps = Convert.toString((char) v.items[i]);
					Button charFilterB = new Button(caps);
					charFilterB.addPressListener(new PressListener() {
						@Override
						public void controlPressed(ControlEvent e) {
							search(caps.charAt(0));
						}
					});
					sc2.add(charFilterB, AFTER + UnitsConverter.toPixels(DP + 8),
							TOP + UnitsConverter.toPixels(DP + 4), DP + 48,  DP + 48);
				}
			}
			if (enableCancel) {
				cancel = new Button(cancelString);
				cancel.setBackColor(Color.WHITE);
				add(cancel, CENTER, BOTTOM - fmH / 2, PARENTSIZE + 90, PREFERRED + fmH);
			}
			list = new ListContainer();
			if (!uiMaterial) {
				list.setBackColor(Color.WHITE);
			}
			add(list, LEFT + UnitsConverter.toPixels(DP + 2), enableSearch || this.caption != null ? AFTER + fmH / 5 : TOP,
					FILL - UnitsConverter.toPixels(DP + 2), (enableCancel ? FIT : FILL) - fmH / 2,
					enableSearch ? sc2 : this.caption != null ? this.caption : null);
			list.addContainers(containers);
			repositionOnSize();
		} catch (Exception e) {
			if (Settings.onJavaSE) {
				e.printStackTrace();
			}
			throw new RuntimeException(e.getClass().getName() + " " + e);
		}
	}

	private void repositionOnSize() {
		if (containers == null) {
			return;
		}
		int hh = containers[containers.length - 1].getY2();
		int hm = list.y + hh + (cancel == null ? 0 : cancel.height) + fmH;

		if (this.height > hm) {
			list.height = hh + fmH / 3;
			setRect(CENTER, CENTER, KEEP, hm);
			if (cancel != null) {
				cancel.setRect(KEEP, BOTTOM - fmH / 2, KEEP, KEEP);
			}
		}
	}

	@Override
	public void reposition() {
		super.reposition();
		repositionOnSize();
	}

	private void search(char c) {
		int pos = htSearchKeys.get(Convert.toUpperCase(c), -1);
		if (pos != -1) {
			list.disableFlick();
			int scrollToY = containers[pos].getY();
			if(list.sbV.getMinimum() > scrollToY) scrollToY = list.sbV.getMinimum() + 1;
			if(list.sbV.getMaximum() < scrollToY) scrollToY = list.sbV.getMaximum() - 1;
			list.disableFlick();
			list.scrollContent(0, scrollToY - list.sbV.getValue(), false);
			list.enableFlick();
		}
	}

	@Override
	public void onEvent(Event event) {
		switch (event.type) {
		case KeyEvent.KEY_PRESS:
			if (enableSearch) {
				search((char) ((KeyEvent) event).key);
			}
			break;
		case ControlEvent.PRESSED:
			if (cancel != null && event.target == cancel) {
				if (!keepIndexOnCancel) {
					selected = -1;
				}
				unpop();
			}
			break;
		case ListContainerEvent.ITEM_SELECTED_EVENT: {
			ListContainerEvent lce = (ListContainerEvent) event;
			selected(((Control) lce.source).appId);
			if (!multipleSelection) {
				Vm.sleep(100);
				unpop();
			}
			break;
		}
		case ListContainerEvent.RIGHT_IMAGE_CLICKED_EVENT: {
			ListContainerEvent lce = (ListContainerEvent) event;
			// if (lce.isImage2) since tc 1.5, when this event is sent the image 2 was
			// already replaced by image 1
			{
				int idx;
				if (event.target instanceof Control) {
					idx = ((Control) event.target).parent.appId;
				} else {
					idx = lce.source.appId;
				}
				selected(idx);
				if (!multipleSelection) {
					Vm.sleep(100);
					unpop();
				}
			}
			break;
		}
		}
	}

	@Override
	public void onPaint(Graphics g) {

		if (!transparentBackground) {
			if (npback == null && npParts != null) {
				try {
					npback = NinePatch.getInstance().getNormalInstance(npParts, width, height, backColor, false);
				} catch (ImageException e) {
					if (Settings.onJavaSE)
						e.printStackTrace();
				}
			}
		}
		NinePatch.tryDrawImage(g, npback, 0, 0);
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title);
		caption = new Label(title, LEFT, foreColor, true);
		reposition();
	}

	@Override
	public int getPreferredWidth() {
		if(uiMaterial) {
			return menu.getPreferredWidth();
		} else {
			return super.getPreferredWidth();
		}
	}

	@Override
	public int getPreferredHeight() {
		return uiMaterial
				? (caption == null || caption.getText().equals("") ? 0 : menu.paddingTop * 2 + fmH)
						+ Math.min(menu.getPreferredHeight(), maxH)
				: super.getPreferredHeight();
	}

	/**
	 * Returns the ListBox used to show the menu. You can edit the paddings the way
	 * you want calling this method.
	 */
	public ListBox getMenu() {
		return menu;
	}

	public Object getSelectedItem() {
		return menu.getSelectedItem();
	}

	/** Selects the given index. */
	public int setSelectedIndex(int index) {
		if (containers == null) {
			desiredSelectedIndex = index;
			return -1;
		}
		if (-1 <= index && index < containers.length) {
			selected(index);
		}
		if (0 <= selected && selected < containers.length) {
			list.scrollToControl(containers[selected]);
		}
		return selected;
	}

	/**
	 * Returns the selected index when this window was closed or -1 if non was
	 * selected
	 */
	public int getSelectedIndex() {
		return desiredSelectedIndex != UNSET ? desiredSelectedIndex : selected;
	}

	public void popupAt(int x, int y, int width, int height) {
		remove(caption);
		remove(menu);
		setRect(x, y, width, height);
		initUI();
		popup();
	}

	public void popupAt(int x, int y) {
		popupAt(x, y, PREFERRED, PREFERRED);
	}

	/** Setup some important variables */
	@Override
	protected void onPopup() {
		selected = -1;
		menu.setSelectedIndex(-1);
		menu.requestFocus();
		if (list == null) {
			int maxW = Math.max(!enableCancel ? 0 : fm.stringWidth(cancelString),
					title == null ? 0 : titleFont.fm.stringWidth(title)) + fmH * 4;
			for (int i = 0; i < itemCount; i++) {
				String s = items[i] instanceof String ? (String) items[i]
						: (items[i] instanceof String[]) ? ((String[]) items[i])[dataCol] : items[i].toString();
				int w = fm.stringWidth(s) + (uiMaterial ? fmH * 4 : fmH * 6);
				if (w > maxW) {
					maxW = w;
				}
			}
			setRect(CENTER, CENTER,
					SCREENSIZE + 80,
					SCREENSIZE + 80);
		}
		if (desiredSelectedIndex != UNSET) {
			setSelectedIndex(desiredSelectedIndex);
		}
		desiredSelectedIndex = UNSET;
	}

	@Override
	protected void postUnpop() {
		if (selected != -1) {
			postPressedEvent();
		}
	}

	private void selected(int newSel) {
		if (0 <= selected && selected < containers.length) {
			containers[selected].setImage(false, true);
		}
		selected = newSel;
		if (0 <= selected && selected < containers.length) {
			containers[newSel].setImage(false, false);
		}
		if (selected == -1) {
			list.setSelectedIndex(-1);
		}

		repaintNow();
	}

	/** Sets the cursor color. By default, it is based in the background color */
	public void setCursorColor(int c) {
		cursorColor = c;
	}
}
