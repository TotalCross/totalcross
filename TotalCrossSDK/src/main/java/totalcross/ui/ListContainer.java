// Copyright (C) 2009-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.ListContainerEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Vector;

/**
 *  ListContainer is a ListBox where each item is a Container.
 *  <p>
 *  The correct way to create a ListContainer item is by subclassing a 
 *  Container and adding the controls in the initUI method. Adding
 *  directly using <code>getContainer(i).add</code> will not work. Below is 
 *  an example of how to use it, taken from the UIGadgets sample.
 *  <pre>
   class LCItem extends ScrollContainer
   {
      Label lDate,lPrice,lDesc;
      Check chPaid;

      public LCItem()
      {
         super(false); // VERY IMPORTANT (a RuntimeException will be thrown if this is not used).
      }

      public void initUI()
      {
         add(chPaid = new Check("Paid"),LEFT,TOP);
         add(lDate = new Label("99/99/9999"),RIGHT,TOP); 
         add(new Label("US$"),LEFT,AFTER);
         add(lPrice = new Label("999.999.99"),AFTER,SAME);
         add(lDesc = new Label("",RIGHT),AFTER+10,SAME);
         lDesc.setText("description");
      }
   }

   private void testListContainer()
   {
      ListContainer lc;
      add(lc = new ListContainer(),LEFT,TOP,FILL,FILL);
      for (int i =0; i < 10; i++)
         lc.addContainer(new LCItem());
   }
 *  </pre>
 *  When an item is selected, a PRESSED event is dispatched.
 *  
 *  Check the ListContainerSample in the sdk for a bunch of ideas of what can be done with this component.
 *  
 *  The ListContainer supports navigation using the keys up/down, page-up/down, and enter. The left and right
 *  keys acts like clicking in the left or right buttons (if any).
 *  
 *  @since TotalCross 1.14
 */

public class ListContainer extends ScrollContainer {
  /** A set of fields and default fields that will be used to define the layout of a ListContainer's Item. 
   */
  public class Layout {
    /** Specify what to do if the left or right controls are images.
     * There are two situations that occurs when the image has a different height of the ListContainer's Item: 
     * <ol> 
     *  <li> The image is smaller than the ListContainer's Item height. It can be enlarged or vertically centered.
     *  If the flag is false (default), the image will be centered. Otherwise, a "smooth upscaled" image will
     *  be created, however the image will mostly have a bad appearance. This is the worst choice. The best 
     *  choice is always to have a big image (for the biggest possible resolution of the device) that will 
     *  always be scaled down. 
     *  <li> The image is bigger than the ListContainer's Item height. It will always be scaled down, using a 
     *  "smooth scale" algorithm. This is the best choice.
     * </ol>
     * In general way, the image never defines the height of the Item; its the opposite: the number of Item 
     * lines is that defines the image height and size.
     */
    public boolean leftImageEnlargeIfSmaller, rightImageEnlargeIfSmaller;
    /** If the left and/or right control is a fixed Image, set it here and it will be replicated on all lines. */
    public Image defaultLeftImage, defaultRightImage;
    protected int defaultLeftImageW, defaultLeftImageH, defaultRightImageW, defaultRightImageH;
    /** If the left and/or right control is a fixed Image, set it here and it will be replicated on all lines. 
     * These images can be set only if the default image was set. The image size must be the same of the default one. */
    public Image defaultLeftImage2, defaultRightImage2;
    /** The default colors of all items. Defaults to BLACK. */
    public int[] defaultItemColors;
    /** The items that will have a bold font. Defaults to false (plain font) */
    public boolean[] boldItems;
    /** The default relative font sizes. You can specify a delta compared to the font size of this ListContainer.
     * For example, specifying -1 will make the item have a font 1 size less than the standard one. */
    public int[] relativeFontSizes;
    /** The x position of the label, relative to the column's width. 
     * Can be AFTER (default for all items), CENTER (relative to container's width), CENTER_OF (relative to the space 
     * available after the last String added), RIGHT (adjustments are NOT allowed!), BEFORE.
     * The number of lines of the Item is computed based on the column count.
     * Note that this field cannot be changed after the first Item is created, since the internal computation of 
     * number of lines is done only once.
     */
    public int[] positions;

    /** The font names to be used when creating the fonts. If the member is null, it will use the parent's name. */
    public String[] fontNames;

    /** The gap between the left/right controls and the text. 
     * This gap is a <b>percentage</b> based in the control font's height. So, if you pass 100 (default), it will be
     * 100% of the font's height, 50 will be 50% of the height, 150 will be 1.5x the font height, and so on.
     */
    public int controlGap = 100;
    /** The gap between the image and the item's top/bottom. This is useful if your item has 3 lines but you want
     * to decrase the image's height.
     */
    public int imageGap;
    /**
     * The gap between the Item and the borders can be set using this field. The values stored 
     * are not absolute pixels, but a percentage. Defaults to 0 (%) for all items.
     */
    public Insets insets;

    /** The line spacing between two consecutive lines. Again, a percentage of the font's height is used.
     * Defaults to 0.
     * @see #controlGap
     */
    public int lineGap;

    /** Set to true to center the labels vertically if there are empty lines ("") before or after the items array.
     * Only works if the items form a single column.
     */
    public boolean centerVertically;

    protected int itemCount, itemsPerLine;
    protected int[] itemY;
    protected Font[] fonts;
    protected int itemH;
    protected ListContainerEvent lce = new ListContainerEvent();

    /** Constructs a Layout component with the given columns and item count. */
    public Layout(int itemCount, int itemsPerLine) {
      this.itemCount = itemCount;
      this.itemsPerLine = itemsPerLine;
      defaultItemColors = new int[itemCount];
      relativeFontSizes = new int[itemCount];
      boldItems = new boolean[itemCount];
      fonts = new Font[itemCount];
      fontNames = new String[itemCount];
      positions = new int[itemCount];
      for (int i = itemCount; --i >= 0;) {
        positions[i] = AFTER;
      }
      
      // compute the number of lines
      lineCount = itemCount / itemsPerLine;
      if ((itemCount % itemsPerLine) != 0) {
        lineCount++;
      }
      insets = new Insets(
          (int) (Settings.screenDensity * 16), 
          (int) (Settings.screenDensity * 16), 
          (int) (Settings.screenDensity * 8), 
          (int) (Settings.screenDensity * 16));
    }
    
    int lineCount;

    /** After you set the properties, you must call this method to setup the internal coordinates.
     * If you create a Item before calling this method, a RuntimeException will be thrown.
     */
    public void setup() {
      itemY = new int[itemCount];
      itemH = 0;
      int y = insets.top * fmH / 100;
      // for each line, compute its height based on the biggest font height
      for (int i = 0, lineH = 0, col = 0, last = itemCount - 1; i <= last; i++) {
        Font f = fonts[i] = Font.getFont(fontNames[i] != null ? fontNames[i] : font.name, boldItems[i],
            font.size + relativeFontSizes[i]);
        itemY[i] = y;
        if (f.fm.height > lineH) {
          lineH = f.fm.height;
        }
        if (++col == itemsPerLine || i == last) {
          // adjust the y so that different font heights at the same line are bottom-aligned
          for (int j = i; j >= 0 && itemY[j] == y; j--) {
            itemY[j] += lineH - fonts[i].fm.height;
          }

          itemH += lineH;
          y += lineH + lineGap * fmH / 100;
          lineH = col = 0;
        }
      }
      itemH += (lineGap * fmH / 100) * (lineCount - 1);

      // if there are images, resize them accordingly
      if (defaultLeftImage != null) {
        defaultLeftImage = resizeImage(defaultLeftImage, leftImageEnlargeIfSmaller);
        defaultLeftImageW = defaultLeftImage.getWidth();
        defaultLeftImageH = defaultLeftImage.getHeight();
      }
      if (defaultRightImage != null) {
        defaultRightImage = resizeImage(defaultRightImage, rightImageEnlargeIfSmaller);
        defaultRightImageW = defaultRightImage.getWidth();
        defaultRightImageH = defaultRightImage.getHeight();
      }
      if (defaultLeftImage2 != null) {
        defaultLeftImage2 = resizeImage(defaultLeftImage2, leftImageEnlargeIfSmaller);
      }
      if (defaultRightImage2 != null) {
        defaultRightImage2 = resizeImage(defaultRightImage2, rightImageEnlargeIfSmaller);
      }
    }

    private Image resizeImage(Image img, boolean imageEnlargeIfSmaller) {
      int imgH = (int) (Settings.screenDensity * (lineCount > 1 ? 40 : 56));
      int ih = img.getHeight();
      if (ih > imgH || (imageEnlargeIfSmaller && ih < imgH)) {
        try {
          return img.smoothScaledFixedAspectRatio(imgH, true);
        } catch (ImageException ime) {
        } // just keep the previous image intact
      }
      return img;
    }
  }

  /** Creates a Layout object with the given parameters. */
  public Layout getLayout(int itemCount, int itemsPerLine) {
    return new Layout(itemCount, itemsPerLine);
  }

  /** An item of the ListContainer. */
  public static class Item extends Container {
    /** The left and/or right controls that will be displayed. */
    public Object leftControl, rightControl;
    /** The Strings that will be displayed in the container. Individual items cannot be null; 
     * pass "" instead to not display it. 
     */
    public String[] items;
    /** The colors of all items. */
    private int[] itemColors;
    /** When leftControl or rightControl is an Image, set this to false to don't show it (and also disable controls) */
    public boolean leftControlVisible = true, rightControlVisible = true;

    private Layout layout;

    /** Constructs an Item based in the given layout. You must set the items array with the strings that will
     * be displayed. You may also set the leftControl/rightControl and individual itemColors and boldItems.
     */
    public Item(Layout layout) {
      if (layout.itemY == null) {
        throw new RuntimeException(
            "You must call layout.setup after setting its properties and before creating an Item. Read the javadocs!");
      }
      this.layout = layout;
      this.itemColors = layout.defaultItemColors;
      if (layout.defaultLeftImage != null) {
        leftControl = new ImageControl(layout.defaultLeftImage);
      }
      if (layout.defaultRightImage != null) {
        rightControl = new ImageControl(layout.defaultRightImage);
      }
    }

    /** Returns the colors used on this Item. You can then set the individual colors if you wish.
     * @see ListContainer.Layout#defaultItemColors
     */
    public int[] getColors() {
      if (itemColors == layout.defaultItemColors) // still the default? create a new array so user can change
      {
        itemColors = new int[layout.itemCount];
        for (int i = layout.itemCount; --i >= 0;) {
          itemColors[i] = layout.defaultItemColors[i];
        }
      }
      return itemColors;
    }

    @Override
    public void initUI() {
      super.initUI();
      if (items.length != layout.itemCount) {
        throw new IllegalArgumentException("Items have " + items.length + " but itemCount was specified as "
            + layout.itemCount + " in the Layout's constructor");
      }
      if (leftControl != null && leftControl instanceof Control) {
        Control c = (Control) leftControl;
        if (c.parent == null) {
          add(c);
        }
        c.setRect(LEFT + layout.insets.left * fmH / 100, CENTER, PREFERRED, PREFERRED);
        c.focusTraversable = true;
      }
      if (rightControl != null && rightControl instanceof Control) {
        Control c = (Control) rightControl;
        if (c.parent == null) {
          add(c);
        }
        c.setRect(RIGHT - layout.insets.right * fmH / 100, CENTER, PREFERRED, PREFERRED);
        c.focusTraversable = true;
      }
    }

    @Override
    public void onEvent(Event e) {
      if (e.type == PenEvent.PEN_DOWN || e.type == PenEvent.PEN_UP) {
        if (!isActionEvent(e)) {
          return;
        }

        if (leftControl != null && leftControlVisible
            && (e.target == leftControl || ((PenEvent) e).x < getLeftControlX())) {
          handleButtonClick(e.target, true);
        } else if (rightControl != null && rightControlVisible
            && (e.target == rightControl || ((PenEvent) e).x >= getRightControlX())) {
          handleButtonClick(e.target, false);
        } else {
          return;
        }
        e.consumed = true;
      }
    }

    private void handleButtonClick(Object target, boolean isLeft) {
      boolean is2 = false;
      Object c = isLeft ? leftControl : rightControl;
      // change images
      Image cur = c instanceof Image ? (Image) c : c instanceof Button ? ((Button) c).getImage() : null;
      if (cur != null) {
        Image img1 = isLeft ? layout.defaultLeftImage : layout.defaultRightImage;
        Image img2 = isLeft ? layout.defaultLeftImage2 : layout.defaultRightImage2;
        if (img1 != null && img2 != null) // if there are both images, swap them
        {
          cur = cur == img1 ? img2 : img1;
          is2 = cur == img2;
          if (c instanceof Image) {
            if (isLeft) {
              leftControl = cur;
            } else {
              rightControl = cur;
            }
          } else {
            ((Button) c).setImage(cur);
          }
        }
      }
      // flsobral: the target of the pen down event may be the left control instead of an item from this list.
      if (!(target instanceof Item)) // this if is just an optimization to avoid starting an unnecessary loop.
      {
        while (!(target instanceof Item) && target instanceof Control) {
          target = ((Control) target).parent;
        }
      }

      if (target instanceof Item) // although unlikely, it is possible for the loop above to set target to an unknown type, this is a last check to avoid a ClassCastException
      {
        Window.needsPaint = true;
        // change to the selected line so user can correctly find who was pressed
        ListContainer lc = getLC();
        if (lc != null) {
          lc.setSelectedItem((Container) target);
        }
        postListContainerEvent(c, target,
            isLeft ? ListContainerEvent.LEFT_IMAGE_CLICKED_EVENT : ListContainerEvent.RIGHT_IMAGE_CLICKED_EVENT, is2);
      }
    }

    private ListContainer getLC() {
      for (Control c = parent; c != null; c = c.parent) {
        if (c instanceof ListContainer) {
          return (ListContainer) c;
        }
      }
      return null;
    }

    public void setImage(boolean isLeft, boolean toImage1) {
      Object c = isLeft ? leftControl : rightControl;
      // change images
      if (c instanceof Image) {
        Image img1 = isLeft ? layout.defaultLeftImage : layout.defaultRightImage;
        Image img2 = isLeft ? layout.defaultLeftImage2 : layout.defaultRightImage2;
        if (img1 != null && img2 != null) {
          if (isLeft) {
            leftControl = toImage1 ? img1 : img2;
          } else {
            rightControl = toImage1 ? img1 : img2;
          }
        }
        Window.needsPaint = true;
      } else if (c instanceof Button) {
        Button b = (Button) c;
        Image img1 = isLeft ? layout.defaultLeftImage : layout.defaultRightImage;
        Image img2 = isLeft ? layout.defaultLeftImage2 : layout.defaultRightImage2;
        if (img1 != null && img2 != null) {
          b.setImage(toImage1 ? img1 : img2);
        }
      }
    }

    public void postListContainerEvent(Object target, Object source, int type, boolean is2) {
      layout.lce.touch();
      layout.lce.consumed = false;
      layout.lce.target = target;
      layout.lce.type = type;
      layout.lce.isImage2 = is2;
      layout.lce.source = (Control) source;
      postEvent(layout.lce);
    }

    @Override
    public int getPreferredWidth() {
      return parent.getClientRect().width;
    }

    @Override
    public int getPreferredHeight() {
      return layout.itemH + (layout.insets.top + layout.insets.bottom) * fmH / 100;
    }

    public int getLeftControlX() {
      int x1 = 0;
      if (leftControl != null) {
        x1 += (leftControl instanceof Control ? ((Control) leftControl).getWidth() : layout.defaultLeftImageW)
            + (int) (Settings.screenDensity * 16);
      }
      return x1;
    }

    public int getRightControlX() {
      int x2 = width - layout.insets.right * fmH / 100;
      if (rightControl != null) {
        x2 -= (rightControl instanceof Control ? ((Control) rightControl).getWidth() : layout.defaultRightImageW)
            + layout.controlGap * fmH / 100;
      }
      return x2;
    }

    @Override
    public void onPaint(Graphics g) {
      super.onPaint(g);
      Layout layout = this.layout;
      // compute the area available for the text, excluding the left/right controls
      int x1 = getLeftControlX();
      int x2 = getRightControlX();

      if (leftControl != null && leftControl instanceof Image && leftControlVisible) {
        g.drawImage((Image) leftControl, layout.insets.left * fmH / 100, (height - layout.defaultLeftImageH) / 2);
      }
      if (rightControl != null && rightControl instanceof Image && rightControlVisible) {
        g.drawImage((Image) rightControl, width - layout.defaultRightImageW - layout.insets.right * fmH / 100,
            (height - layout.defaultRightImageH) / 2);
      }

      g.setClip(x1, 0, x2 - x1, height);
      int lastX = 0;
      int deltaY = 0;
      if (layout.centerVertically) {
        int first = 0, last = 0, firstH = 0, lastH = 0;
        for (int i = 0; i < layout.itemCount; i++) {
          if (items[i].equals("")) {
            firstH += layout.fonts[i].fm.height;
            first++;
          } else {
            break;
          }
        }
        for (int i = layout.itemCount; --i >= first;) {
          if (items[i].equals("")) {
            lastH += layout.fonts[i].fm.height;
            last++;
          } else {
            break;
          }
        }
        if (first != last) {
          deltaY += (firstH + lastH + (first - 1 + last - 1) * layout.lineGap * fmH / 100) / 2;
        }

      }
      for (int i = 0, col = 0, x = x1; i < layout.itemCount; i++) {
        Font f = layout.fonts[i];
        g.setFont(f);
        g.foreColor = itemColors[i];
        String s = items[i];
        int sw = f.fm.stringWidth(s);
        int sx;
        int sy = layout.itemY[i];
        g.backColor = backColor;
        switch (layout.positions[i]) {
        default:
        case AFTER:
          sx = x;
          break;
        case RIGHT:
          sx = x2 - sw;
          if (x > sx) {
            g.fillRect(sx, sy, sw, sy + f.fm.height);
          }
          break; // erase area only if last text is beyond our limits
        case CENTER_OF:
          sx = x + (x2 - x - sw) / 2;
          sw += sx - x;
          break;
        case CENTER:
          sx = (x2 - x1 - sw) / 2;
          sw += sx - x;
          break;
        case BEFORE:
          sx = lastX - sw;
          break;
        }
        g.drawText(s, sx, sy + deltaY);
        lastX = sx;
        x += sw;
        if (++col == layout.itemsPerLine) {
          col = 0;
          x = x1;
        }
      }
    }
  }

  private Vector vc = new Vector(20);
  protected Container lastSel; //flsobral@tc126_65: ListContainer is no longer restricted to accept only subclasses of ScrollContainer, any subclass of Container may be added to a ListContainer.
  protected int lastSelBack, lastSelIndex = -1;
  /** Color used to highlight a container. Based on the background color. */
  public int highlightColor = -1;
  /** If true (default), draws a horizontal line between each container. */
  public boolean drawHLine = true;
  private Insets scInsets;
  private int defaultHighlightColor;

  public ListContainer() {
    super(false, true);
    focusTraversable = bag.verticalOnly = true;
  }

  @Override
  public void onColorsChanged(boolean colorsChanged) {
    super.onColorsChanged(colorsChanged);
    defaultHighlightColor = Color.getCursorColor(backColor);
  }

  /** Returns the number of items of this list */
  public int size() // guich@tc126_53
  {
    return vc.size();
  }

  /** Adds a new Container to this list. 
   * @see #addContainers
   */
  public void addContainer(Container c) {
    boolean isSC = c instanceof ScrollContainer;
    if (isSC) {
      ScrollContainer sc = (ScrollContainer) c;
      if (sc.sbH != null || sc.sbV != null) {
        throw new RuntimeException("The given ScrollContainer " + c + " must have both ScrollBars disabled.");
      }
      sc.shrink2size = true;
    }
    if (drawHLine && !(c instanceof Item)) // make sure that the container has a gap for the border 
    {
      if (scInsets == null) {
        scInsets = new Insets();
      }
      c.getInsets(scInsets);
      if (scInsets.top == 0) {
        c.setInsets(scInsets.left, scInsets.right, 1, scInsets.bottom);
      }
    }
    c.setFocusTraversable(true);

    c.containerId = vc.size() + 1;
    vc.addElement(c);
    add(c, LEFT, AFTER, FILL, Math.max(c.getPreferredHeight(), (int) (Settings.screenDensity * 56)));
    if (isSC) {
      c.resize();
    }
    int n = tabOrder.size();
    if (n >= 2 && ((Control) tabOrder.items[n - 1]).y < ((Control) tabOrder.items[n - 2]).y) {
      bag.verticalOnly = false;
    }
    if (drawHLine && c.borderStyle == BORDER_NONE) {
      c.borderStyle = BORDER_TOP;
    }
    resize();
  }

  /** Adds an array of Containers to this list. Adding hundreds of containers is hundred times faster using
   * this method instead of adding one at a time.
   * 
   * Consider also to increase the value of 
   * Flick.defaultLongestFlick BEFORE creating the ListContainer, otherwise the user may take forever
   * to flick. You can set it to 3 * all.length, if all.length is above 1000.
   * @see Flick#defaultLongestFlick
   */
  public void addContainers(Container[] all) {
    changed = true;
    if (all.length > 0) {
      int vcs = vc.size();
      vc.addElements(all);
      boolean isSC = all[0] instanceof ScrollContainer; // assume that if one is a ScrollContainer, all are.
      boolean isItem = all[0] instanceof Item;
      int s = bag.tabOrder.size();
      bag.tabOrder.setSize(s + all.length); // increase taborder's size to the final one
      if (!isItem) {
        bag.tabOrder.setSize(s); // Item is added directly 
      }
      for (int i = 0; i < all.length; i++) {
        Container c = all[i];
        c.setFocusTraversable(true);
        if (isSC) {
          ScrollContainer sc = (ScrollContainer) c;
          if (sc.sbH != null || sc.sbV != null) {
            throw new RuntimeException("The given ScrollContainer " + c + " must have both ScrollBars disabled.");
          }
          sc.shrink2size = true;
        }
        c.containerId = ++vcs;
        if (!isItem) {
          bag.add(c);
        } else {
          bag.addToList(c);
          if (c.foreColor == -1) {
            c.foreColor = this.foreColor;
          }
          if (c.backColor == -1) {
            c.backColor = this.backColor;
          }
          if (c.font == null) {
            c.font = this.font;
            c.fm = font.fm;
            c.fmH = fm.height;
          }
          bag.tabOrder.items[s++] = c;
        }

        c.x = LEFT;
        c.y = AFTER;
        c.width = FILL;
        c.height = Math.max(c.getPreferredHeight(), (int) (Settings.screenDensity * 56)); // positions will be set later on resize
        if (isSC) {
          c.resize();
        }
        if (drawHLine && c.borderStyle == BORDER_NONE) {
          c.borderStyle = BORDER_TOP;
        }
      }
    }
    resize(); // all lines except this one take 10% of the method's time, and this one takes 90%
  }

  /** Removes all containers of this ListContainer.
   * Note that onRemove is not called in the containers. 
   */
  public void removeAllContainers() {
    if (!vc.isEmpty()) {
      bag.verticalOnly = true;
      setSelectedIndex(-1);
      scrollToControl(bag.children); // reset scrollbars and scroll position
      bag.numChildren = 0;
      bag.tail = bag.children = null; // faster removeAll()
      // reset relative-positioning values
      bag.lastX = -999999;
      bag.lastY = bag.lastW = bag.lastH = 0;
      bag.height = getClientRect().height;
      if (sbV != null) {
        sbV.setMaximum(0);
      }
      vc.removeAllElements();
      bag.tabOrder.removeAllElements();
      Window.needsPaint = true;
    }
  }

  private boolean dragged;

  @Override
  public void onEvent(Event e) {
    super.onEvent(e);
    switch (e.type) {
    case PenEvent.PEN_DRAG_START:
      if (Settings.fingerTouch) {
        dragged = true;
      }
      break;
    case PenEvent.PEN_UP:
      //case ControlEvent.FOCUS_IN: - will not work now that it can use keys to traverse
      if (!(e.target instanceof Ruler)) {
        if (dragged) // don't select if user decided to drag the item
        {
          dragged = false;
          return;
        }
        // find the container that was added to this ListContainer
        Control c = (Control) e.target;
        while (c != null) {
          if (c.asContainer != null && c.asContainer.containerId != 0) {
            if (c == lastSel) {
              return;
            }
            setSelectedItem((Container) c);
            if (c instanceof Item) {
              ((Item) c).postListContainerEvent(this, e.target, ListContainerEvent.ITEM_SELECTED_EVENT, false);
            } else {
              postPressedEvent();
            }
            break;
          }
          c = c.parent;
        }
      }
      break;
    case KeyEvent.SPECIAL_KEY_PRESS:
      if (vc.size() > 1) {
        Item sel = lastSelIndex != -1 && vc.items[lastSelIndex] instanceof Item ? (Item) vc.items[lastSelIndex] : null;
        KeyEvent ke = (KeyEvent) e;
        if (sel != null && ke.isActionKey()) {
          sel.postListContainerEvent(sel, e.target, ListContainerEvent.ITEM_SELECTED_EVENT, false);
        } else {
          switch (ke.key) {
          case SpecialKeys.UP:
            scroll(-1);
            break;
          case SpecialKeys.DOWN:
            scroll(1);
            break;
          case SpecialKeys.PAGE_UP:
            scroll(-height / ((Container) vc.items[0]).height);
            break;
          case SpecialKeys.PAGE_DOWN:
            scroll(height / ((Container) vc.items[0]).height);
            break;
          case SpecialKeys.LEFT:
          case SpecialKeys.RIGHT:
            if (sel != null) {
              sel.handleButtonClick(e.target, ke.key == SpecialKeys.LEFT);
            }
            break;
          }
        }
      }
      break;

    }
  }

  private void scroll(int v) {
    int idx = lastSelIndex + v;
    if (idx < 0) {
      idx = 0;
    } else if (idx >= vc.size()) {
      idx = vc.size() - 1;
    }
    if (idx != lastSelIndex) {
      setSelectedIndex(idx);
    }
  }

  /** Returns the selected container, or null if none is selected. */
  public Container getSelectedItem() {
    return lastSel;
  }

  /** Returns the selected index, or -1 if none is selected. */
  public int getSelectedIndex() {
    return lastSelIndex;
  }

  /** Sets the selected container based on its index.
   * @param idx The index or -1 to unselect all containers.
   */
  public void setSelectedIndex(int idx) {
    if (idx < 0) {
      if (lastSel != null) {
        setBackColor(lastSel, lastSelBack);
      }
      lastSel = null;
      lastSelIndex = -1;
    } else {
      setSelectedItem((Container) vc.items[idx]);
    }
  }

  /** Sets the selected container. */
  public void setSelectedItem(Container c) {
    if (lastSel != null) {
      setBackColor(lastSel, lastSelBack);
    }
    lastSelBack = c.backColor;
    lastSel = c;
    int hc = highlightColor != -1 ? highlightColor : defaultHighlightColor;
    setBackColor(lastSel, hc);
    c.setBackColor(hc); //flsobral@tc126_70: highlight the whole selected container
    Window.needsPaint = true;
    lastSelIndex = c.containerId == 0 ? -1 : c.containerId - 1;
    // guich@tc150: make sure the item is visible
    int yy = c.y + c.parent.y;
    if (yy < 0) {
      scrollContent(0, yy - height + c.height, true);
    } else if ((yy + c.height) > height) {
      scrollContent(0, yy, true);
    }
  }

  /** Returns the given container number or null if its invalid. */
  public Container getContainer(int idx) {
    return idx >= vc.size() ? null : (Container) vc.items[idx];
  }

  /** Changes the color of all controls inside the given container that matches the background color
   * of this ListContainer.
   */
  public void setBackColor(Container c, int back) {
    int hc = highlightColor != -1 ? highlightColor : defaultHighlightColor;
    c.setBackColor(back);
    for (Control child = c.children; child != null; child = child.next) {
      if (child.asContainer == null && (child.backColor == lastSelBack || child.backColor == hc)) {
        child.setBackColor(back);
      }
      if (child.asContainer != null) {
        setBackColor(child.asContainer, back);
      }
    }
  }

  @Override
  public void resize() {
    boolean isItem = bag.children != null && bag.children instanceof Item;
    for (Control child = bag.children; child != null; child = child.next) {
      if (!(child instanceof ScrollContainer)) {
        child.setRect(KEEP, KEEP, FILL, KEEP, null, true);
        if (child.asContainer != null && child.asContainer.numChildren > 0) {
          Control[] children2 = ((Container) child).getChildren();
          for (int j = children2.length; --j >= 0; ) {
            children2[j].reposition();
          }
        }
      }
    }
    if (isItem) // if Item, we know that all of them have the same width and height
    {
      Control last = bag.tail;
      int maxX = last.x + last.width;
      int maxY = last.y + last.height;
      super.resize(maxX, maxY);
    } else {
      super.resize();
    }
  }

  /** Positions the given Container (that should be a control added to this ListContainer) at the top
   * of the list.
   */
  @Override
  public void scrollToControl(Control c) // kmeehl@tc100
  {
    if (c != null && sbV != null) {
      int yy = c.y;
      Control f = c.parent;
      while (f.parent != this) {
        yy += f.y;
        f = f.parent;
        if (f == null) {
          return;// either c is not in this container, or it has since been removed from the UI
        }
      }
      // vertical
      int lastV = sbV.value;
      int val = yy - c.parent.y;
      if (val < sbV.minimum) {
        val = sbV.minimum;
      } else if (val >= sbV.maximum) {
        val = sbV.maximum;
      }
      sbV.setValue(val);
      if (lastV != sbV.value) {
        lastV = sbV.value;
        bag.uiAdjustmentsBasedOnFontHeightIsSupported = false;
        bag.setRect(bag.x, TOP - lastV, bag.width, bag.height);
        bag.uiAdjustmentsBasedOnFontHeightIsSupported = true;
      }
    }
  }
}
