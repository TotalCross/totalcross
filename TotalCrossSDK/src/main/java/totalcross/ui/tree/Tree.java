// Copyright (C) 2004 Trev Quang Nguyen
// Copyright (C) 2005-2013 SuperWaba Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.tree;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Flick;
import totalcross.ui.ScrollBar;
import totalcross.ui.ScrollPosition;
import totalcross.ui.Scrollable;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.KeyListener;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Vector;

/**
 * This class is a simple implementation of a tree widget. Since it's
 * natural to render the tree in rows, this class borrows most of the code from ListBox.
 * Features:
 * <ul>
 * <li> similar to Microsoft Windows Explorer tree
 * <li> horizontal and vertical scrolling
 * <li> allows setting of folder and leaf icons.
 * <li> expands and collapse of folder
 * <li> allowsChildren flag to determine if the node is a leaf or folder
 * <li> delete, insert, and modify (user object) of a node
 * <li> clicking on leaf node will swap leaf icon (like hyperlink)
 * <li> allows creation of tree to show or hide root node.
 * </ul>
 * You should use TreeModel class to modify the tree after.
 * Here's a sample:
 * <pre>
 * TreeModel tmodel = new TreeModel();
 * Tree tree = new Tree(tmodel);
 * add(tree,LEFT,TOP,FILL,FILL);
 * Node root = new Node("Tree");
 * tmodel.setRoot(root);
 * Node n;
 * root.add(n = new Node("Branch1"));
 * n.add(new Node("SubBranch1"));
 * n.add(new Node("SubBranch2"));
 * </pre>
 * You can also see the FileChooserBox control and FileChooserTest (in UIGadgets sample).
 *
 * @see Node#userObject
 */
public class Tree extends Container implements PressListener, PenListener, KeyListener, Scrollable {
  public static final int SCROLLBAR_ALWAYS = 0;
  public static final int SCROLLBAR_AS_NEEDED = 1;
  public static final int SCROLLBAR_NEVER = 2;

  public static final int ICON_PLUS = 0;
  public static final int ICON_MINUS = 1;
  public static final int ICON_OPEN = 2;
  public static final int ICON_CLOSE = 3;
  public static final int ICON_FILE = 4;

  /** Set to true to allow multiple selections using a Check drawn before the nodes.
   * @since TotalCross 1.15 
   */
  public boolean multipleSelection; // guich@tc115_4

  /** Set to false to only expand or collapse if you click on the +- buttons.
   * @since TotalCross 1.2
   */
  public boolean expandClickingOnText = true; // guich@tc120_15

  /** If true, all Tree will have the selection bar drawn in
   * the full width instead of the selected's text width
   * @since TotalCross 1.27
   */
  public boolean useFullWidthOnSelection; // guich@tc125_29

  /** The Flick object listens and performs flick animations on PenUp events when appropriate. */
  protected Flick flick;

  protected Image imgPlus; // the expand icon "-"
  protected Image imgMinus; // the expand icon "+"
  protected Image imgOpen; // the open folder icon
  protected Image imgClose; // the close folder icon
  protected Image imgFile; // the unvisited file icon

  protected ScrollBar vbar; // vertical scrollbar
  protected ScrollBar hbar; // horizontal scrollbar

  protected TreeModel model; // holds the (original) node tree structure
  protected Vector items = new Vector(); // hold the nodes to be drawn

  protected int offset; // the vertical offset
  protected int hsOffset; // the horizontal offset
  protected int selectedIndex = -1; // the selected index
  protected int itemCount; // the vertical scrollbar maximum (size of items vector)
  protected int hsCount; // the horizontal scrollbar maximum
  protected int visibleItems; // the first visible item display
  protected int btnX; // the vertical scrollbar's x position

  private boolean showRoot; // flag to show the root node or hide it.
  private boolean allowsChildren = true; // flag to use the node's allowsChildren to determine
  // if the node is a leaf or not

  private int fColor = -1;
  private int bgColor0 = -1;
  private int bgColor1 = -1;
  private int cursorColor = -1;
  private int fourColors[] = new int[4];

  private int imgPlusSize; // width of collapse[imgPlus] icon
  private int imgOpenW; // width of folder [imgOpen] icon
  private int imgOpenH; // height of folder [imgOpen] icon
  private static final int hline = 3; // number of pixel used to draw horizontal line (from
  // plus icon to gap between plus icon and folder or leaf icon)
  private static final int gap = 1; // the number of space(in pixel) between the plus icon
  // and the folder or leaf icon.

  private int hbarPolicy = SCROLLBAR_AS_NEEDED;
  private boolean showIcons = true;
  private int x0;

  private static Image imgOpenDefault, imgCloseDefault, imgFileDefault;
  private boolean isScrolling;
  private int lastV = -10000000, lastH = -10000000; // eliminate duplicate events

  /** @deprecated Use setLineHeight and getLineHeight */
  @Deprecated
  public int lineH;
  private boolean lineHset;

  /** Default line height. */
  public void setLineHeight(int k) {
    this.lineH = k;
    lineHset = true;
  }

  public int getLineHeight() {
    return lineH;
  }

  /** Constructs a new Tree based on an empty TreeModel. */
  public Tree() {
    this(new TreeModel());
  }

  /** Constructs a new Tree based on the given parameters.
   * @param model the TreeModel to be used
   */
  public Tree(TreeModel model) {
    this(model, true);
  }

  /** Constructs a new Tree based on the given parameters.
   * @param model the TreeModel to be used
   * @param showRoot if true, the root node is shown
   */
  public Tree(TreeModel model, boolean showRoot) {
    super.add(vbar = Settings.fingerTouch ? new ScrollPosition(ScrollBar.VERTICAL) : new ScrollBar(ScrollBar.VERTICAL));
    super.add(
        hbar = Settings.fingerTouch ? new ScrollPosition(ScrollBar.HORIZONTAL) : new ScrollBar(ScrollBar.HORIZONTAL));
    vbar.addPressListener(this);
    hbar.addPressListener(this);
    addPenListener(this);
    addKeyListener(this);
    vbar.setLiveScrolling(true);
    hbar.setLiveScrolling(true);
    vbar.setFocusLess(true);
    hbar.setFocusLess(true);
    this.showRoot = showRoot;
    this.allowsChildren = model == null || model.allowsChildren;
    setModel(model);
    focusTraversable = true;
    ignoreOnAddAgain = ignoreOnRemove = true;

    setCursorColor(0xE1FFFF); // aqua (cursor color) highlight

    if (Settings.fingerTouch) {
      flick = new Flick(this);
    }
  }

  @Override
  public boolean flickStarted() {
    isFlicking = true;
    return isScrolling;
  }

  @Override
  public void flickEnded(boolean atPenDown) {
    isFlicking = false;
    flickDirection = NONE;
  }

  @Override
  public boolean canScrollContent(int direction, Object target) {
    if (flickDirection == NONE) {
      flickDirection = direction == DragEvent.UP || direction == DragEvent.DOWN ? VERTICAL : HORIZONTAL;
    }
    if (Settings.fingerTouch) {
      switch (direction) {
      case DragEvent.UP:
        return vbar.getValue() > vbar.getMinimum();
      case DragEvent.DOWN:
        return (vbar.getValue() + vbar.getVisibleItems()) < vbar.getMaximum();
      case DragEvent.LEFT:
        return hbar.getValue() > hbar.getMinimum();
      case DragEvent.RIGHT:
        return (hbar.getValue() + hbar.getVisibleItems()) < hbar.getMaximum();
      }
    }
    flickDirection = NONE;
    return false;
  }

  private int hbarX0, vbarY0, hbarDX, vbarDY;
  private boolean scScrolled;
  private static final int NONE = 0;
  private static final int VERTICAL = 1;
  private static final int HORIZONTAL = 2;
  private int flickDirection = NONE;
  private boolean isFlicking;

  @Override
  public boolean scrollContent(int dx, int dy, boolean fromFlick) {
    boolean scrolled = false;

    if (flickDirection == HORIZONTAL && dx != 0) {
      hbarDX += dx;
      int oldValue = hbar.getValue();
      hbar.setValue(hbarX0 + hbarDX);
      lastH = hbar.getValue();

      if (oldValue != lastH) {
        hsOffset = lastH;
        scrolled = true;
        if (!fromFlick) {
          hbar.tempShow();
        }
      }
    }
    if (flickDirection == VERTICAL && dy != 0) {
      vbarDY += dy;
      int oldValue = vbar.getValue();
      vbar.setValue(vbarY0 + vbarDY / lineH);
      lastV = vbar.getValue();

      if (oldValue != lastV) {
        offset = lastV;
        scrolled = true;
        if (!fromFlick) {
          vbar.tempShow();
        }
      }
    }

    if (scrolled) {
      Window.needsPaint = true;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int getScrollPosition(int direction) {
    if (direction == DragEvent.LEFT || direction == DragEvent.RIGHT) {
      return hsOffset;
    }
    return offset;
  }

  @Override
  public Flick getFlick() {
    return flick;
  }

  @Override
  public boolean wasScrolled() {
    return scScrolled;
  }

  /** Call this method to hide the file and folder icons. */
  public void dontShowFileAndFolderIcons() {
    this.showIcons = false;
    this.imgOpenH = this.imgOpenW = 0;
  }

  /**
   * Method to set the tree model.
   *
   * @param model
   *           the tree model.
   */
  public void setModel(TreeModel model) {
    clear();
    this.model = (model != null) ? model : new TreeModel();
    model.setTree(this);
    initTree(model.getRoot());
  }

  /**
   * Method to set the scrollbar appearance.
   * @see #SCROLLBAR_ALWAYS
   * @see #SCROLLBAR_AS_NEEDED
   * @see #SCROLLBAR_NEVER
   */
  public void setScrollBarPolicy(int horiz, int vert) {
    hbarPolicy = horiz;
    switch (horiz) {
    case SCROLLBAR_ALWAYS: {
      if (!hbar.isVisible()) {
        resetScrollBars();
      }
      break;
    }
    case SCROLLBAR_NEVER: {
      hbar.setVisible(false);
      break;
    }
    default:
    case SCROLLBAR_AS_NEEDED: {
      resetScrollBars();
      break;
    }
    }
  }

  /**
   * Method to set the tree root node with the new root node. If the new root node is null, the tree is unchanged.
   *
   * @param root
   *           the new tree root node.
   */
  public void initTree(Node root) {
    if (root != null) {
      if (showRoot) {
        items.addElement(root);
        root.expanded = false;
        root.level = 0;
        expand(root);
      } else {
        for (int i = 0, n = root.size(); i < n; i++) {
          Node node = (Node) root.items[i];
          items.addElement(node);
          node.level = 1;
          node.expanded = false;
        }
      }
    }
    resetScrollBars();
  }

  @Override
  public void onFontChanged() {
    if (!lineHset) {
      lineH = Settings.fingerTouch ? fmH * 3 / 2 : fmH;
    }
  }

  /**
   * Method to initialize the vertical and horizontal scrollbars maximum.
   */
  protected void initScrollBars() {
    // initialize the vertical scrollbar
    itemCount = items.size();
    vbar.setEnabled(isEnabled() && visibleItems < itemCount);
    vbar.setMaximum(itemCount);

    // initialize the horizontal scrollbar
    int maxWidth = 0;
    for (int i = 0, n = items.size(); i < n; i++) {
      maxWidth = Math.max(getItemWidth(i), maxWidth);
    }
    maxWidth = maxWidth - (width - vbar.getPreferredWidth());
    hsCount = (maxWidth > 0) ? maxWidth : 0;
    hbar.setEnabled(isEnabled() && hsCount > width - vbar.getPreferredWidth());
    hbar.setMaximum(hsCount);

    switch (hbarPolicy) {
    case SCROLLBAR_ALWAYS:
      hbar.setVisible(true);
      break;
    case SCROLLBAR_AS_NEEDED:
      hbar.setVisible(hsCount > width - vbar.getPreferredWidth());
      break;
    case 2:
      hbar.setVisible(false);
      break;
    }
  }

  /**
   * Method to load icons use for the tree. You can change the icon by using the setIcon(int iconType, Filename
   * imageFilename).
   */
  protected void initImage() {
    try {
      if (imgOpenDefault == null) {
        imgCloseDefault = new Image("totalcross/res/closed_folder.png");
        imgOpenDefault = new Image("totalcross/res/open_folder.png");
        imgFileDefault = new Image("totalcross/res/document.png");
      }
      setIcon(ICON_PLUS, getIcon(true));
      setIcon(ICON_MINUS, getIcon(false));
      setIcon(ICON_CLOSE, imgCloseDefault);
      setIcon(ICON_OPEN, imgOpenDefault);
      setIcon(ICON_FILE, imgFileDefault);
    } catch (Exception e) {
      // Should never happen
    }
    x0 = Math.max(imgOpenW, imgPlusSize) + (Settings.isWindowsCE() ? 4 : 3);
  }

  /**
   * plusIcon dynamically creates a "+" icon, based on current size of the font.
   * 
   * @return icon of a boxed plus icon
   * @throws ImageException
   */
  private Image getIcon(boolean plus) throws ImageException {
    int w;
    int mid;
    Image img;
    Graphics gImg;

    w = fmH / 2;
    if ((w % 2) == 0) {
      w++; // make sure we have an odd number of pixels for our plus sign
    }

    img = new Image(w, w);
    gImg = img.getGraphics();
    gImg.backColor = Color.WHITE;
    gImg.foreColor = Color.BLACK;
    gImg.drawRect(0, 0, w, w);
    gImg.fillRect(1, 1, w - 2, w - 2);

    mid = (w / 2); // where is the midpoint of our +
    if (plus) {
      gImg.drawLine(mid, 2, mid, w - 3); // vertical slash
    }
    gImg.drawLine(2, mid, w - 3, mid); // draw horizontal slash
    return img;
  }

  /**
   * Method to set the icon of the tree based on the icon type. Note: You should not change the plus and minus icons.
   * You can set the open/close to null (setting one will null out the other).
   * 
   * @param iconType
   *           one of the ICON_xxx constants.
   * @param img
   *           The image to be used.
   * @throws ImageException
   * @see #ICON_PLUS
   * @see #ICON_MINUS
   * @see #ICON_OPEN
   * @see #ICON_CLOSE
   * @see #ICON_FILE
   */
  public void setIcon(int iconType, Image img) throws ImageException {
    if (iconType > 1) {
      img = img.smoothScaledFixedAspectRatio(lineH > fmH ? fmH : lineH * 8 / 10, true); // guich@tc110_19
      if (iconType == ICON_OPEN || iconType == ICON_CLOSE) {
        img.applyColor2(backColor);
      }
    }
    switch (iconType) {
    case ICON_PLUS:
      imgPlus = getIcon(true);
      imgPlusSize = imgPlus.getWidth();
      break;
    case ICON_MINUS:
      imgMinus = getIcon(false);
      break;
    case ICON_CLOSE:
      imgClose = img;
      break;
    case ICON_OPEN:
      imgOpen = img;
      imgOpenW = imgOpen.getWidth();
      imgOpenH = imgOpen.getHeight();
      break;
    case ICON_FILE:
      imgFile = img;
      break;
    }
  }

  /**
   * Method to return the width of the given item index with the current fontmetrics. Note: if you override this class
   * you must implement this method.
   */
  protected int getItemWidth(int index) {
    return fm.stringWidth(items.items[index].toString());
  }

  /**
   * Method to empties this Tree, setting all elements of the array to null, so they can be garbage collected.
   */
  @Override
  public void removeAll() {
    model = new TreeModel();
    clear();
  }

  /**
   * Same as removeAll() method. Just more clearer method name
   */
  @Override
  public void clear() {
    items.removeAllElements();
    vbar.setMaximum(0);
    hbar.setMaximum(0);
    itemCount = hsCount = offset = hsOffset = 0;
    selectedIndex = -1;
    Window.needsPaint = true;
  }

  /**
   * Method to insert the items to the tree (For internal uses) Note: this method does not reset the scroll bar, you
   * need to call this resetScrollBars() after you have performed an insert.
   */
  private void insert(int index, Node node, int level, int expandValue) {
    items.insertElementAt(node, index);
    node.level = level;
    node.expanded = expandValue == 1;
  }

  /**
   * Method to remove the given index from the Tree items vector. This method will not remove the node from the
   * original node.
   *
   * @param index
   *           the item index in the items vector.
   */
  public void remove(int index) {
    if (index < 0 || index > itemCount - 1) {
      return;
    }

    int level = ((Node) items.items[index]).level;
    do {
      items.removeElementAt(index);
      itemCount--;
    } while (index < itemCount && level < ((Node) items.items[index]).level); // guich@tc126_42: inverted test

    resetScrollBars();
    Window.needsPaint = true;
  }

  /**
   * Method to remove an Object from the Tree's items vector.
   *
   * @param item the Node to delete from the tree's item vector.
   */
  public void remove(Object item) {
    int index = items.indexOf(item);
    if (itemCount > 0 && index != -1) {
      remove(index);
    }
  }

  /**
   * Method to reset the horizontal scroll bar properties.
   */
  private void resetHBar() {
    if (hbarPolicy == SCROLLBAR_NEVER) {
      return;
    }

    // calculate the horizontalscrollbar maximum
    int max = 0;
    int indent = 3 + (imgPlusSize + hline + imgOpenW / 2 - imgPlusSize / 2);
    for (int i = 0, n = items.size(); i < n; i++) {
      Node nn = (Node) items.items[i];
      max = Math.max(max, fm.stringWidth(nn.getNodeName()) + indent * nn.level);
    }
    max += vbar.getPreferredWidth(); // remember to take into account of the pixels used to draw the icons and scrollbar

    hbar.setMaximum(max);
    if (hbarPolicy == SCROLLBAR_ALWAYS || (width - vbar.getPreferredWidth()) < max) {
      hbar.setEnabled(isEnabled() && (width - vbar.getPreferredWidth()) < max);
      hbar.setVisible(true);
    } else {
      hbar.setVisible(false);
    }
  }

  /**
   * Method to reset the horizontal scroll bar properties.
   */
  private void resetVBar() {
    itemCount = items.size();
    vbar.setMaximum(itemCount);
    boolean wasDisabled = !vbar.isEnabled();
    vbar.setEnabled(isEnabled() && visibleItems < itemCount);

    if (vbar.isEnabled() && wasDisabled) {
      vbar.setValue(0);
    }

    if (selectedIndex == itemCount) {
      setSelectedIndex(selectedIndex - 1);
    }

    if (itemCount == 0) {
      selectedIndex = -1;
    }

    if (itemCount <= visibleItems && offset != 0) {
      offset = 0;
    }
  }

  /**
   * Method to rest the vertical and horizontal scrollbars properties. Note: there's still a bug in resetting the
   * horizontal scroll bar.
   */
  private void resetScrollBars() {
    resetVBar();
    resetHBar();
  }

  /**
   * Method to expand a collapsed node.
   *
   * @param node
   *           the collapse node to expand.
   * @return True if the item was expanded, false otherwise.
   */
  public boolean expand(Node node) {
    int index, n;
    if (!node.expanded && (index = indexOf(node)) != -1 && !node.isLeaf(allowsChildren) && (n = node.size()) > 0) // guich@tc126_5: added last test to prevent that a root-only tree sets expand to true
    {
      node.expanded = true;
      int level = node.level + 1; // our children have one less our level
      for (int i = 0; i < n; i++) {
        index++;
        insert(index, (Node) node.items[i], level, 0);
      }
      resetScrollBars();
      Window.needsPaint = true;
      return true;
    }
    return false;
  }

  /** Expands all nodes until the given path is reached */
  public void expandTo(String filePath) {
    Vector its = items;
    for (String p : filePath.split("/")) {
      for (int i = 0, size = its.size(); i < size; i++) {
        Node n = (Node) its.items[i];
        String s = n.toString();
        if (s.equals(p)) {
          expand(n);
          its = n;
          break;
        }
      }
    }
  }

  /**
   * Method to collapse an expanded node.
   *
   * @param node
   *           the expanded node to collapse.
   * @return True if the item was collapsed, false otherwise.
   *
   */
  public boolean collapse(Node node) {
    int index;

    if (node.expanded && (index = indexOf(node)) != -1 && !node.isLeaf(allowsChildren)) {
      int level = node.level;
      index++;
      while (index < itemCount && level < ((Node) items.items[index]).level) {
        items.removeElementAt(index);
        itemCount--;
      }
      ((Node) items.items[index - 1]).expanded = false;
      resetScrollBars();
      Window.needsPaint = true;
      return true;
    }
    return false;
  }

  /**
   * Method to set the Object at the given Index, starting from 0.
   *
   * @param i
   *           the index
   * @param s
   *           the object to set.
   */
  public void setItemAt(int i, Object s) {
    if (0 <= i && i < itemCount) {
      items.items[i] = s;
      Window.needsPaint = true;
    }
  }

  /**
   * Method to get the Object at the given Index. Returns an empty string in case of error.
   *
   * @param i
   *           the index.
   */
  public Object getItemAt(int i) {
    if (0 <= i && i < itemCount) {
      return items.items[i];// get(i);
    }
    return "";
  }

  /**
   * Method to return the selected item of the Tree or an empty String if no selection has been made.
   *
   * @return the selected object, or null is no selection has been made.
   */
  public Node getSelectedItem() {
    return selectedIndex >= 0 ? (Node) items.items[selectedIndex] : null;
  }

  /**
   * Method to return the position of the selected item of the Tree or -1 if the Tree has no selected index yet.
   *
   * @return the selected index or -1 if no selection has been made.
   */
  public int getSelectedIndex() {
    return selectedIndex;
  }

  /**
   * Method to return all items in the items vector as an array of object. The objects are of the class Node.
   *
   * @return all items in items vector as an array of Objects.
   */
  public Object[] getItems() {
    return items.toObjectArray();
  }

  /**
   * Method to return the index of the item specified by the name, or -1 if not found.
   *
   * @param name
   *           the object to find.
   * @return the index of the item specified by the name, or -1 if not found.
   */
  public int indexOf(Object name) {
    return items.indexOf(name);
  }

  /**
   * Method to select the given name. If the name is not found, the current selected item is not changed.
   *
   * @since SuperWaba 4.01
   * @param name
   *           the object to select.
   */
  public void setSelectedItem(Object name) {
    int pos = indexOf(name);
    if (pos != -1) {
      setSelectedIndex(pos);
    }
  }

  /**
   * Method to select the given index and scroll to it if necessary. Note: select must be called only after the control
   * has been added to the container and its rect has been set.
   *
   * @param i
   *           the index of the item.
   */
  public void setSelectedIndex(int i) {
    if (0 <= i && i < itemCount && i != selectedIndex && height != 0) {
      int vi = vbar.getVisibleItems();
      if (i < offset || i >= offset + vi) // guich@tc125_20: change offset only if the item is not visible
      {
        offset = i - vi / 2; // guich@tc125_20: make it centered on screen
        if (offset < 0) {
          offset = 0;
        }
        int ma = vbar.getMaximum();
        if (offset + vi > ma) {
          offset = Math.max(ma - vi, 0);
        }
      }

      selectedIndex = i;
      vbar.setValue(offset);
      Window.needsPaint = true;
    } else if (i == -1) {
      offset = 0;
      vbar.setValue(0);
      selectedIndex = -1;
      Window.needsPaint = true;
    }
  }

  /**
   * Returns the number of items (Nodes)
   */
  public int size() {
    return itemCount;
  }

  /**
   * Do nothing.
   */
  @Override
  public void add(Control control) {
  }

  /**
   * Do nothing.
   */
  @Override
  public void remove(Control control) {
  }

  /**
   * Method to return the preferred width, ie, size of the largest item plus 20.
   *
   * @return the preferred width of this control.
   */
  @Override
  public int getPreferredWidth() {
    int maxWidth = 0;
    int n = itemCount;
    for (int i = 0; i < n; i++) {
      maxWidth = Math.max(getItemWidth(i), maxWidth);
    }
    return maxWidth + 6 + vbar.getPreferredWidth() + insets.left + insets.right;
  }

  /**
   * Method to return the number of items multiplied by the font metrics height
   *
   * @return the preferred height of this control.
   */
  @Override
  public int getPreferredHeight() {
    int n = itemCount;
    int h = Math.max(lineH * n, vbar.getPreferredHeight()) + 6;
    return (n == 1 ? h - 1 : h) + insets.top + insets.bottom;
  }

  /**
   * Method to search this Tree for an item with the first letter matching the given char. The search is made case
   * insensitive. Note: if you override this class you must implement this method.
   */
  protected void find(char c) {
    for (int i = 0; i < itemCount; i++) {
      String s = items.items[i].toString();

      // first letter matches and not the already selected index?
      if (s.length() > 0 && Convert.toUpperCase(s.charAt(0)) == c && selectedIndex != i) {
        setSelectedIndex(i);
        Window.needsPaint = true;
        break; // end the for loop
      }
    }
  }

  /**
   * Method to enable this control if the specified enabled flag is true.
   */
  @Override
  public void setEnabled(boolean enabled) {
    if (internalSetEnabled(enabled, false)) {
      vbar.setEnabled(isEnabled() && visibleItems < itemCount);
      hbar.setEnabled(isEnabled());
    }
  }

  @Override
  public void setBackColor(int c) {
    super.setBackColor(c);
    if (imgPlus == null) {
      initImage();
    }
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    if (colorsChanged) {
      vbar.setBackForeColors(backColor, foreColor);
      hbar.setBackForeColors(backColor, foreColor);
    }
    fColor = getForeColor();
    bgColor0 = Color.brighter(getBackColor());
    bgColor1 = cursorColor != -1 ? cursorColor : (bgColor0 != Color.WHITE) ? backColor : Color.getCursorColor(bgColor0);

    if (fColor == bgColor1) {
      fColor = foreColor;
    }
    Graphics.compute3dColors(isEnabled(), backColor, foreColor, fourColors);
  }

  /**
   * Method to recalculate the box size for the selected item if the control is resized by the main application .
   */
  @Override
  protected void onBoundsChanged(boolean screenChanged) {
    onFontChanged();
    int btnW = vbar.getPreferredWidth();
    int btnH = hbar.getPreferredHeight();
    if (Settings.fingerTouch && ScrollPosition.AUTO_HIDE) {
      btnW = btnH = 0;
    }
    visibleItems = ((height - 2 - btnH) / lineH);
    vbar.setMaximum(itemCount);
    vbar.setVisibleItems(visibleItems);
    vbar.setEnabled(visibleItems < itemCount);
    btnX = width - btnW;

    vbar.setRect(Settings.fingerTouch ? RIGHT - 2 : RIGHT, 0, PREFERRED, FILL, null, screenChanged);
    hbar.setRect(0, Settings.fingerTouch ? BOTTOM - 2 : BOTTOM, btnW == 0 ? FILL : FIT, PREFERRED, null, screenChanged);

    resetScrollBars();
    Window.needsPaint = true;
  }

  /**
   * Method to notify the tree that a node has been removed from the tree model and to repaint the tree to reflect the
   * changes, if necessary
   *
   * @param node
   *           the node that has been removed from the tree model
   */
  public void nodeRemoved(Node node) {
    remove(indexOf(node));
  }

  /**
   * Method to notify the tree that a node has been added to the tree model
   * and to repaint the tree to reflect the changes.
   *
   * @param parent
   *           the parent node of the new added node
   * @param child
   *           the new ly added node
   * @param index
   *           the index of the new node
   */
  public void nodeInserted(Node parent, Node child, int index) {
    int pos = indexOf(parent);
    if (pos < 0) {
      return; // didn't find parent node
    }
    if (!parent.expanded) {
      return; // node is not expanded, so we don't have to paint the node
    }

    int lvl = parent.level + 1;
    int count = 0;
    for (int i = pos + 1, n = items.size(); i < n; i++) {
      int l = ((Node) items.items[i]).level;
      if (lvl == l) {
        if (count == index) {
          insert(i, child, lvl, 0);
          break;
        }
        count++;
      }
      // else  - guich@tc120_14: no else here!
      if (lvl > l || i == n - 1) {
        insert(lvl > l ? i : i + 1, child, lvl, 0);
        break;
      }
    }
    resetScrollBars();
    Window.needsPaint = true;
  }

  /**
   * Method to notify the tree that a node in the tree model has been modified (currently - only changing the user
   * object)
   *
   * @param node
   *           the node that has been modified
   */
  public void nodeModified(Node node) {
    if (indexOf(node) != -1) {
      resetScrollBars();
      Window.needsPaint = true;
    }
  }

  @Override
  public void penDrag(DragEvent de) {
    if (Settings.fingerTouch) {
      int dx = -de.xDelta;
      int dy = -de.yDelta;

      if (isScrolling) {
        scrollContent(dx, dy, true);
        de.consumed = true;
      } else {
        int direction = DragEvent.getInverseDirection(de.direction);
        //de.consumed = true; - with this, the ScrollPositions don't appear
        if (canScrollContent(direction, de.target) && scrollContent(dx, dy, true)) {
          scScrolled = isScrolling = true;
        }
      }
      de.consumed = true; // guich@tc166: if inside a TabbedContainer, prevent it from scrolling
    }
  }

  @Override
  public void penDragEnd(DragEvent e) {
  }

  @Override
  public void penDragStart(DragEvent e) {
  }

  @Override
  public void penDown(PenEvent pe) {
    if (pe.target != this) {
      return;
    }
    scScrolled = false;
    vbarY0 = vbar.getValue();
    hbarX0 = hbar.getValue();
    hbarDX = vbarDY = 0;

    if (!(pe.target instanceof ScrollBar || Settings.fingerTouch)) {
      computeSel(pe);
    }
  }

  private void computeSel(PenEvent pe) {
    lastControl = null;
    int sel = ((pe.y - 4) / lineH) + offset;
    if (sel < itemCount && sel >= 0) {
      if (multipleSelection && pe.x < imgPlusSize * 3) {
        checkClicked(sel);
      } else if (pe.x < btnX) {
        Node node = (Node) items.items[sel];
        if (sel != selectedIndex) {
          selectedIndex = sel;
          Window.needsPaint = true;
        }
        if (node.userObject != null && node.userObject instanceof Control) {
          postControlEvent(node, pe);
          Window.needsPaint = true;
        }
      }
    }
  }

  @Override
  public void penUp(PenEvent pe) {
    if (pe.target != this) {
      return;
    }
    if (!isFlicking) {
      flickDirection = NONE;
    }
    isScrolling = false;
    if (pe.target instanceof ScrollBar) {
      return;
    }
    if (!scScrolled/* && Settings.fingerTouch*/) {
      computeSel(pe);
    }

    // Post the event
    int sel = ((pe.y - 4) / lineH) + offset;
    if (isInsideOrNear(pe.x, pe.y) && pe.x < btnX && sel < itemCount) {
      postPressedEvent();
      if (multipleSelection && pe.x < imgPlusSize + 2) {
        return;
      }

      Node node = (Node) items.items[sel];
      int xstart = getTextX(node.level);
      boolean isLeaf = node.isLeaf(allowsChildren);
      if (isLeaf && pe.x >= xstart) {
        node.visited = true;
      } else if (((!scScrolled || !Settings.fingerTouch) && expandClickingOnText) || (!scScrolled && pe.x < xstart)) {
        // call expand and collapse or change the leaf icon on when clicked anywhere
        if (!node.expanded) {
          expand(node);
        } else {
          collapse(node);
        }
      }
      Window.needsPaint = true;
    }
  }

  public void checkClicked(int sel) {
    Node n = ((Node) items.items[sel]);
    n.isChecked = !n.isChecked;
    Window.needsPaint = true;
  }

  @Override
  public void controlPressed(ControlEvent e) {
    if (e.target == vbar) {
      int newOffset = vbar.getValue();
      if (newOffset != offset) {
        offset = newOffset;
        Window.needsPaint = true;
      }
    } else if (e.target == hbar) {
      int hsValue = hbar.getValue();
      if (hsValue != hsOffset && hsValue > -1) {
        hsOffset = hsValue;
        Window.needsPaint = true;
      }
    }
  }

  @Override
  public void actionkeyPressed(KeyEvent e) {
    postPressedEvent(); // guich@tc111_21
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (lastControl != null) {
      e.target = lastControl;
      lastControl.onEvent(e);
    } else if (multipleSelection && selectedIndex >= 0 && ((KeyEvent) e).key == ' ') {
      checkClicked(selectedIndex);
    } else {
      find(Convert.toUpperCase((char) ((KeyEvent) e).key));
    }
  }

  @Override
  public void specialkeyPressed(KeyEvent e) {
    if (lastControl != null) {
      e.target = lastControl;
      lastControl.onEvent(e);
    } else {
      handleKeys((KeyEvent) e);
    }
  }

  private boolean handleKeys(KeyEvent ke) {
    if (ke.modifiers != 0 && (ke.isDownKey() || ke.isUpKey())) {
      vbar.onEvent(ke);
    } else if (ke.modifiers != 0 && (ke.key == SpecialKeys.LEFT || ke.key == SpecialKeys.RIGHT)) {
      hbar.onEvent(ke);
    } else if (ke.isDownKey() && selectedIndex < itemCount - 1) {
      setSelectedIndex(selectedIndex + 1);
    } else if (ke.isUpKey() && selectedIndex > 0) {
      setSelectedIndex(selectedIndex - 1);
    } else if (selectedIndex >= 0 && ke.key == SpecialKeys.LEFT) {
      return collapse((Node) items.items[selectedIndex]);
    } else if (selectedIndex >= 0 && ke.key == SpecialKeys.RIGHT) {
      return expand((Node) items.items[selectedIndex]);
    } else if (ke.isActionKey() || ke.type == KeyEvent.ACTION_KEY_PRESS) {
      postPressedEvent();
    } else if (multipleSelection && ke.key == ' ') {
      checkClicked(selectedIndex);
    } else {
      return false;
    }
    return true;
  }

  @Override
  public Control handleGeographicalFocusChangeKeys(KeyEvent ke) // kmeehl@tc100
  {
    return handleKeys(ke) ? this : null;
  }

  @Override
  public void onPaint(Graphics g) {
    if (imgPlus == null) {
      initImage();
    }
    // Draw background and borders
    g.backColor = bgColor0;
    g.fillRect(0, 0, btnX, height);
    g.foreColor = foreColor;

    g.draw3dRect(0, 0, width, height, Graphics.R3D_CHECK, false, false, fourColors);

    // draw scrollbar border (why is it disappear in the first place? or is there a border for the scrollbar class??)
    g.drawRect(btnX - 1, 0, vbar.getPreferredWidth() + 1, height);
    if (!Settings.fingerTouch && hbar.isVisible()) {
      g.drawRect(0, height - hbar.getPreferredHeight() - 1, width - vbar.getPreferredWidth() + 1,
          hbar.getPreferredHeight());
    }

    int dx = multipleSelection ? x0 + imgPlusSize + 2 : x0;
    int dy = 2;

    g.foreColor = fColor;
    g.setClip(2, 1, btnX - 4, lineH * visibleItems + 1);
    int greatestVisibleItemIndex = Math.min(itemCount, visibleItems + offset); // code corrected by Bjoem Knafla
    for (int i = offset; i < greatestVisibleItemIndex; ++i, dy += lineH) {
      if (i == selectedIndex) {
        drawCursor(g, selectedIndex);
      }
      drawNode(g, i, dx - hsOffset, dy);
    }
  }

  /**
   * Method to draw the icons and node text
   */
  protected void drawNode(Graphics g, int index, int dx, int dy) {
    Node node = (Node) items.items[index];
    int level = node.level - 1;

    dx += 2;
    dy += 2;
    if (index > 0) {
      drawConnector(g, index, dx, dy, node); // draw the line that connect the nodes
    }
    boolean expand = node.expanded;
    int x = dx + (imgPlusSize + hline + gap + imgOpenW / 2 - imgPlusSize / 2) * level;
    int y = dy;
    boolean nodeIsLeaf = node.isLeaf(allowsChildren);

    // draw plus minus icon
    y = dy + lineH / 2;
    if (nodeIsLeaf) {
      g.drawDots(x + imgPlusSize / 2, y, x + imgPlusSize, y);
    } else if (node.size() == 0) {
      g.drawDots(x + imgPlusSize / 2, y, x + imgPlusSize, y);
    } else {
      g.drawImage(expand ? imgMinus : imgPlus, x, y - imgPlusSize / 2);
    }
    // draw horizontal line
    x += imgPlusSize;
    g.drawDots(x, y, x + hline, y);

    if (multipleSelection) {
      g.foreColor = fColor; // restore text color
      g.backColor = backColor;
      int k = imgPlusSize + 1;
      y = (lineH - k) / 2 + dy + 1;
      int rx = 3 - hsOffset;
      if (node.isChecked) {
        if (uiVista || uiAndroid) {
          g.fillVistaRect(rx, y, k, k, backColor, false, false);
        } else {
          g.fillRect(rx, y, k, k);
        }
      }
      g.drawRect(rx, y, k, k); // guich@220_28
      g.foreColor = foreColor; // restore text color
    }

    // draw folder icon (remember the gap needed)
    x += hline + gap;

    y = dy + lineH / 2 - imgOpenH / 2;
    if (showIcons) {
      if (nodeIsLeaf) {
        drawFileImage(g, node, x, y);
      } else {
        g.drawImage(expand ? imgOpen : imgClose, x, y);
      }
    }

    if (node.userObject != null && node.userObject instanceof Control) {
      x += imgOpenW + gap + 1;
      y = dy;
      Control c = (Control) node.userObject;
      int ww = width - x - (!uiAndroid ? vbar.getWidth() : 0) - 2 - hsOffset;
      int hh = lineH - 2;
      if (c.getWidth() == 0) {
        super.add(c); // caution: this.add does nothing!
        c.setRect(0, 0, ww, hh);
      } else if (c.getWidth() != ww) // allow rotation
      {
        c.intXYWH(0, 0, ww, hh);
        c.reposition();
      }
      c.intXYWH(x, y, ww, hh);
      c.onPaint(c.getGraphics());
      if (c instanceof Container) {
        ((Container) c).paintChildren();
      }
      c.intXYWH(100000, 0, ww, hh);
    } else {
      dy--;
      x += imgOpenW + gap + gap + 2;
      y = dy + (lineH - fmH) / 2;
      String text = node.toString();
      if (node.backColor != -1) // guich@tc120_13
      {
        g.backColor = node.backColor;
        g.fillRect(x, y, fm.stringWidth(text), lineH);
      }
      if (node.foreColor != -1) {
        g.foreColor = node.foreColor;
      }
      g.drawText(text, x, y, textShadowColor != -1, textShadowColor);
    }
  }

  private PenEvent pec = new PenEvent();
  private Control lastControl;

  private void postControlEvent(Node node, PenEvent peorig) {
    Control c = (Control) node.userObject;
    pec.type = peorig.type;
    pec.touch();
    int xstart = getTextX(node.level);
    int sel = ((peorig.y - 4) / lineH) + offset;
    pec.x = peorig.x - xstart + c.getX() - gap;
    pec.y = peorig.y - sel * lineH - 4;
    if (c instanceof Container) {
      c = ((Container) c).findChild(pec.x -= 100000, pec.y);
      pec.x -= c.getX();
      pec.y -= c.getY();
    }
    pec.target = lastControl = c;
    c.postEvent(pec);
  }

  private int getTextX(int level) // guich@tc125_24
  {
    int dx = multipleSelection ? x0 + imgPlusSize + 2 : x0;
    dx -= hsOffset;
    level--;
    int x = dx + (imgPlusSize + hline + gap + imgOpenW / 2 - imgPlusSize / 2) * level;
    x += imgPlusSize;
    x += hline + gap;
    x += imgOpenW + gap + gap + 2;
    return x - 1;
  }

  /** Allows the draw of customized file images. */
  protected void drawFileImage(Graphics g, Node node, int x, int y) {
    if (imgFile != null) {
      g.drawImage(imgFile, x, y);
    }
  }

  /**
   * Method to draw the (line connector) angled line.
   */
  protected void drawConnector(Graphics g, int index, int dx, int dy, Node node) {
    if (node == null) {
      return;
    }

    int level = node.level - 1;
    Node prev = null, next = null;
    Node parent = node.parent;
    if (parent != null) {
      int pos = parent.indexOf(node);
      if (pos > 0) {
        prev = (Node) parent.items[pos - 1];
      }
      if (pos >= 0 && pos < parent.size() - 1) {
        next = (Node) parent.items[pos + 1];
      }
    }

    // calculate the x-start position
    int x = dx;
    if (level == 0) {
      x += imgPlusSize / 2;
    } else if (level == 1) {
      x += imgPlusSize + hline + gap + imgOpenW / 2;
    } else {
      x += imgPlusSize + hline + gap + imgOpenW / 2 + (imgPlusSize / 2 + hline + gap + imgOpenW / 2 + 1) * (level - 1);
    }

    // calculate the y-start and y-end position
    int ystart;
    int yend;

    // handles the last level 1 node
    if (level == 0 && next == null && prev != null && items.items[index] == node) {
      ystart = dy - (lineH - imgPlusSize) / 2;
      yend = dy + (lineH - imgPlusSize) / 2;
      g.drawDots(x, ystart, x, yend);
    }

    // draw vertical connector lines for leaf node
    if (node.isLeaf(allowsChildren) || node.size() == 0) {
      ystart = dy - (lineH - imgOpenH) / 2;
      yend = dy + (lineH / 2);
      g.drawDots(x, ystart, x, yend);

      if (next != null) {
        ystart = yend;
        yend += (lineH / 2);
        g.drawDots(x, ystart, x, yend);
      }
    }
    // draw vertical connector lines for folder node
    else {
      if (next == null && node == items.items[index]) {
        ystart = dy - (lineH - imgPlusSize) / 2;
        yend = dy + (lineH - imgPlusSize) / 2;
        g.drawDots(x, ystart, x, yend); // draw from "+" to end of line
      }

      if (next != null) {
        ystart = dy - (lineH - imgPlusSize) / 2;
        yend = dy + lineH;
        g.drawDots(x, ystart, x, yend);
      }
    }
    drawConnector(g, index, dx, dy, node.parent);
  }

  /**
   * Method to draw the highlight box when user select a listbox's item.
   */
  protected void drawCursor(Graphics g, int sel) {
    if (offset <= sel && sel < visibleItems + offset && sel < itemCount) {
      Node n = (Node) items.items[sel];
      int level = n.level;
      int x0 = this.x0;
      if (multipleSelection) {
        x0 += imgPlusSize + 2;
      }

      int dx = x0 - hsOffset - 1;
      if (level == 1) {
        dx += (imgPlusSize + hline + gap + imgOpenW) * level;
      } else {
        dx += imgPlusSize + hline + gap + imgOpenW
            + (imgPlusSize + hline + gap + imgOpenW / 2 - imgPlusSize / 2) * (level - 1);
      }

      dx += 3;

      int dy = 4;

      dy += (sel - offset) * lineH;
      g.setClip(useFullWidthOnSelection ? 2 : dx - 1, dy - 1, btnX - (useFullWidthOnSelection ? 2 : dx),
          Math.min(lineH * visibleItems, this.height - dy));
      int oldb = g.backColor;
      g.backColor = bgColor1;
      int extraH = n.userObject != null && n.userObject instanceof Control ? 0 : fm.descent - 1;
      if (useFullWidthOnSelection) {
        g.fillRect(2, dy - 1, btnX - 2, lineH + extraH);
      } else {
        g.fillRect(dx + 1, dy - 1, this.width - dx, lineH + extraH);
      }
      g.clearClip();
      g.backColor = oldb;
    }
  }

  /**
   * Method to set the cursor color for this Tree. The default is equal to the background slightly darker.
   */
  public void setCursorColor(int color) {
    this.cursorColor = color;
    onColorsChanged(true);
  }

  /**
   * Method to reload the tree. Use this method when the tree model has made a drastic change.
   */
  public void reload() {
    clear();
    initTree(model.getRoot());
    Window.needsPaint = true;
  }

  /**
   * Method to clear the tree and release the tree model references.
   */
  public void unload() {
    clear();
    model = null;
  }

  @Override
  public void getFocusableControls(Vector v) {
    if (visible && isEnabled()) {
      v.addElement(this);
    }
  }
}
