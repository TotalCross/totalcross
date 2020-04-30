// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui;

import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.EnabledStateChangeEvent;
import totalcross.ui.event.Event;
import totalcross.util.Vector;

/**
 * <p>
 * {@link DynamicScrollContainer} is a specialized type of Scroll Container intended for high performance<br/>
 * where hundreds or thousands of views need to be displayed in a scrollable list.
 * </p>
 * <p>
 * The performance is achieved by only every having the visible views on the screen occupy memory at a given time.</br> When a view is scrolled out of the visible area of the scroll container it is immediately destroyed and removed from memory.</br> When a view is scrolled into the visible area of the scroll container it is created if not already visible.
 * </p>
 * Views are created by a {@link DataSource} </p>
 * <b>Only vertical scrolling is supported.</b>
 * 
 */
public class DynamicScrollContainer extends ScrollContainer {

  private DataSource datasource;

  protected final static int SCROLL_UP = -1, SCROLL_UNKNOWN = 0, SCROLL_DOWN = 1;

  private Vector currentViews = new Vector();

  private int scrollHeight;

  /*
   * track the last vertical scroll position, lastV gets reset to 0 so is not reliable
   */
  private int lastVPos;

  public DynamicScrollContainer(DataSource datasource) {
    super(false, true);
    this.datasource = datasource;
    if (datasource != null) {
      int scrollHeight = datasource.getTotalScrollHeight(getWidth() - insets.left - insets.right);
      Container c = new Container();
      add(c, LEFT, TOP, FILL, scrollHeight);
      updateVisibleViews(SCROLL_UNKNOWN);
    }
  }

  public DynamicScrollContainer() {
    super(false, true);

  }

  @Override
  public void onEvent(Event event) {
    if (event.type == ControlEvent.PRESSED) {
      if (event.target == sbV && sbV.value != lastV) {

        int scrollDirection = SCROLL_UNKNOWN;
        scrollDirection = sbV.value < lastVPos ? SCROLL_UP : SCROLL_DOWN;
        lastV = lastVPos = sbV.value;
        bag.uiAdjustmentsBasedOnFontHeightIsSupported = false;
        bag.setRect(bag.x, TOP - lastV, bag.width, bag.height);
        bag.uiAdjustmentsBasedOnFontHeightIsSupported = true;

        try {
          updateVisibleViews(scrollDirection);
        } catch (Exception e) {
          e.printStackTrace();
        }

      } else if (event.target == sbH && sbH.value != lastH) {
        lastH = sbH.value;
        bag.uiAdjustmentsBasedOnFontHeightIsSupported = false;
        bag.setRect(LEFT - lastH, bag.y, bag.width, bag.height);
        bag.uiAdjustmentsBasedOnFontHeightIsSupported = true;
      }
    } else if (event.type == EnabledStateChangeEvent.ENABLED_STATE_CHANGE && event.target == sbV.btnDec
        && sbV.btnDec.isEnabled() == false) {
      /*
       * fixes issue where if you drag scroll handle all the way to the top the bag is never resized to start from the top
       * and the top one or two views are not painted
       */
      bag.uiAdjustmentsBasedOnFontHeightIsSupported = false;
      bag.setRect(bag.x, TOP, bag.width, bag.height);
      bag.uiAdjustmentsBasedOnFontHeightIsSupported = true;
      updateVisibleViews(SCROLL_UNKNOWN);
    } else if (event.type == EnabledStateChangeEvent.ENABLED_STATE_CHANGE && event.target == sbV.btnInc
        && sbV.btnInc.isEnabled() == false) {
      /*
       * fixes issue where if you drag scroll handle all the way to the bottom, we never get the bag resized to the 
       * end of the scroll container so the very last one or two views are not painted
       */
      scrollToView(datasource.getView(datasource.viewCount - 1));
    } else {
      super.onEvent(event);
      updateVisibleViews(SCROLL_UNKNOWN);
    }

  }

  /*
   * Repaints only the visible views on the scroll container
   */
  protected void updateVisibleViews(int scrollDirection) {
    if (datasource == null) {
      return;
    }
    int yStart = bag.y * -1;
    int yEnd = ((bag.y * -1) + height);

    Vector newViews = datasource.getVisibleViewsVec(yStart, yEnd, scrollDirection);
    int currentviewCount = currentViews.size();
    int newviewCount = newViews.size();
    for (int i = 0; i < currentviewCount; i++) {
      if (newViews.contains(currentViews.items[i]) == false) {
        changed = true;
        AbstractView toRemove = (AbstractView) currentViews.items[i];
        bag.remove(toRemove.getComponent());
        toRemove.clear();
      }
    }
    for (int i = 0; i < newviewCount; i++) {

      if (currentViews.contains(newViews.items[i]) == false) {
        changed = true;
        AbstractView view = (AbstractView) newViews.items[i];
        bag.add(view.getComponent());

      }
    }
    currentViews = newViews;
  }

  /**
   * Returns the top most {@link AbstractView} that starts within the viewable area
   */
  public AbstractView getTopMostVisibleView() {
    int viewCount = currentViews.size();
    int yStart = bag.y * -1;
    for (int i = 0; i < viewCount; i++) {
      AbstractView view = (AbstractView) currentViews.items[i];
      if (view.yStart >= yStart) {
        return view;
      }
    }
    return null;
  }

  /**
   * Returns true if the view is in the currently visible part of the scroll container
   * 
   * @param view
   */
  public boolean isViewVisible(AbstractView view) {
    int yStart = bag.y * -1;
    int yEnd = ((bag.y * -1) + height);

    if (view.yStart >= yStart && view.yStart < yEnd) {
      return true;
    }
    return false;
  }

  @Override
  public void reposition() {
    lastVPos = 0;
    int curPage = flick != null && flick.pagepos != null ? flick.pagepos.getPosition() : 0;
    super.reposition();
    bag.clear();
    setDataSource(datasource);
    currentViews = new Vector(20);
    resize();
    if (flick != null && flick.scrollDistance != 0) {
      flick.setScrollDistance(getScrollDistance());
    }
    if (curPage != 0) {
      scrollToPage(curPage);
    } else {
      if (sbH != null) {
        sbH.setValue(0);
      }
      if (sbV != null) {
        sbV.setValue(0);
      }
      bag.x = bag.y = 0;
    }
  }

  /**
   * Set the {@link DataSource} to be used with this container
   * 
   * @param datasource
   */
  public void setDataSource(DataSource datasource) {

    this.datasource = datasource;
    flick.stop(true);
    lastVPos = 0;
    if (datasource != null) {
      scrollHeight = datasource.getTotalScrollHeight(getWidth() - insets.left - insets.right);
      Container c = new Container();
      changed = true;
      bag.removeAll();
      currentViews = new Vector();
      bag.add(c, LEFT, TOP, FILL, scrollHeight);
      updateVisibleViews(SCROLL_UNKNOWN);
      resize();
    }
  }

  @Override
  public boolean scrollContent(int dx, int dy, boolean fromFlick) {
	  boolean scrollHappened = super.scrollContent(dx, dy, fromFlick);
	  updateVisibleViews(dy < 0 ? SCROLL_UP : SCROLL_DOWN);
	  return scrollHappened;
  }

  public void scrollToView(AbstractView view) {
    if (view != null && sbV != null) {
      datasource.firstVisibleView = view.viewNo;
      datasource.lastVisibleView = 999999;
      int scrollDirection = SCROLL_DOWN;

      bag.uiAdjustmentsBasedOnFontHeightIsSupported = false;
      bag.setRect(KEEP, TOP - view.yStart, KEEP, KEEP);

      if (bag.y * -1 + height > bag.height) {
        int overflow = (bag.y * -1 + height - bag.height);
        bag.setRect(KEEP, bag.y + overflow, KEEP, KEEP);
        scrollDirection = SCROLL_UP;
        datasource.lastVisibleView = datasource.viewCount - 1;
        datasource.firstVisibleView = 0;
      }
      bag.uiAdjustmentsBasedOnFontHeightIsSupported = true;
      updateVisibleViews(scrollDirection);
      sbV.setValue(view.yStart);
    }
  }

  @Override
  public void resize(int maxX, int maxY) {
    super.resize(maxX, maxY);
  }

  /**
   * Represents a view to be displayed on a {@link DynamicScrollContainer}. </br>
   * You must subclass {@link AbstractView} and overwrite {@link #getHeight()} and {@link #initUI()}
   * 
   * <p>
   * <b>CRITICAL: in {@link #initUI()} YOU MUST SET <code>y</code> on the defined rectangle of your container to be <code>yStart</code> otherwise the view will not be visible in {@link DynamicScrollContainer}</b>
   */
  public static abstract class AbstractView {
    protected Container c;

    public AbstractView() {
      super();
    }

    /**
     * the width of the parent container in which this view will be visible,</br> this is the width of {@link DynamicScrollContainer} </br> DO NOT MODIFY This is set when adding this view to the {@link DataSource}
     */
    public int parentWidth;
    /**
     * 
     * Specifies the range between which the view is visible on the scroll container
     * Do NOT modify directly. These are set when adding this view to the {@link DataSource}
     */
    public int yStart, yEnd;

    /**
     * Returns the height of this view.
     * This needs to be calculated by the view itself using {@link #getHeight()} and is exposed
     * for faster referencing by {@link DataSource}.</br>
     * If you know the height of your component then set this directly and have {@link #getHeight()} return this value.
     * 
     */
    public int height;

    /**
     * The position of the view in the datasource
     */
    public int viewNo;

    /**
     * Returns the height this view will occupy in the {@link DynamicScrollContainer}. This method should not
     * initialize the component just return the height the view will take up on the scroll container. </br>
     * <p>
     * If you know the height of your component then set {@link #height} directly and return that value. If your component's height can only be determined when the component is painted, say for example because of text that may be wrapped, dynamically added controls,etc, then you need to calculate the height in this method, set {@link #height} to the value and then return it.
     * </p>
     */
    public abstract int getHeight();

    /**
     * Returns the container to display on the {@link DynamicScrollContainer}.
     * </br> Do not create your ui here but in {@link #initUI()}
     */
    public Container getComponent() {
      if (c == null) {
        initUI();
      }
      return c;
    }

    /**
     * Initializes the container that represents this view.</br>
     * You must create your UI in this method.
     * The UI must fit into the height specified by {@link #getHeight()} </br>
     * You could lazy load data onto your ui at this point in time from a datasource, however if the loading takes a long
     * time it will delay the painting of the ui in the {@link DynamicScrollContainer}
     * 
     * <p>
     * <b>CRITICAL: YOU MUST SET <code>y</code> on the defined rectangle of your container to be <code>yStart</code> otherwise the view will not be visible in {@link DynamicScrollContainer}</b>
     */
    public void initUI() {
    }

    /**
     * Called when this view is scrolled out of the visible view area.</br> The view is destroyed </br> You can overwrite
     * to include any further garbage collection and resource release
     */
    public void clear() {
      c = null;
    }

  }

  /**
   * This class is responsible for serving up the {@link AbstractView}s to display on a {@link DynamicScrollContainer}.
   * 
   * You must populate the {@link DataSource} with all the views you want to display before passing it to the {@link DynamicScrollContainer} <br>
   * 
   */
  public static class DataSource {
    protected Vector views;

    private int viewCount;

    private int firstVisibleView, lastVisibleView;

    public DataSource() {
      super();
      views = new Vector(500);
    }

    /**
     * Create a {@link DataSource} and allocate memory for <code>viewCount</code> views to be stored
     * 
     * @param viewCount
     */
    public DataSource(int viewCount) {
      super();
      views = new Vector(viewCount);
    }

    /**
     * Returns the total scrollable height that all {@link AbstractView}s together will take up.</br>
     * This is calculated based on each {@link AbstractView} height and is called when a {@link DataSource} is set on
     * a {@link DataSource} or the {@link DynamicScrollContainer} detects screen rotation.
     */
    public int getTotalScrollHeight(int scrollContainerWidth) {

      viewCount = views.size();
      int height = 0;
      for (int i = 0; i < viewCount; i++) {
        AbstractView v = (AbstractView) views.items[i];
        v.parentWidth = scrollContainerWidth;
        int viewHeight = v.getHeight();
        v.clear();
        v.yStart = height;
        v.yEnd = height + viewHeight - 1;
        v.viewNo = i;
        height += viewHeight;
      }
      firstVisibleView = -1;
      lastVisibleView = -1;
      return height;
    }

    /**
     * Adds a {@link AbstractView} to the datasource if it does not already exist.
     * </br>This does not automatically recalculate the view height</br>
     * 
     * @param view
     */
    public void addView(AbstractView view) {
      if (views.contains(view) == false) {
        views.addElement(view);
      }
    }

    /**
     * Removes the {@link AbstractView} from the datasource.
     * </br>This does not automatically recalculate the view height</br>
     * 
     * @param view
     */
    public void removeView(AbstractView view) {
      views.removeElement(view);
    }

    /**
     * Returns a Vector containing the {@link AbstractView}s that are visible in the Y viewing area of {@link DynamicScrollContainer} between y position <code>yStart</code> and <code>yEnd</code>
     * 
     * @param yStart
     * @param yEnd
     */
    public Vector getVisibleViewsVec(int yStart, int yEnd, int scrollDirection) {
      Vector visibleViewsVec = new Vector(20);
      if (viewCount == 0 || views == null) {
        return visibleViewsVec;
      }
      switch (scrollDirection) {
      case SCROLL_UNKNOWN:
        firstVisibleView = 0;
        lastVisibleView = viewCount - 1;
        for (int i = 0; i < viewCount; i++) {
          AbstractView view = ((AbstractView) (views.items[i]));

          if ((view.yStart <= yStart && view.yEnd > yStart) || (view.yStart >= yStart && view.yEnd <= yEnd)
              || (view.yStart <= yEnd && view.yEnd > yEnd)) {
            visibleViewsVec.addElement(views.items[i]);
            if (firstVisibleView == -1) {
              firstVisibleView = i;
            }
            lastVisibleView = i;
          }
          if (view.yStart > yEnd) {
            break;
          }
        }
        break;
      case SCROLL_DOWN:
        int temp = firstVisibleView;
        firstVisibleView = -1;
        for (int i = temp; i < viewCount; i++) {
          AbstractView view = ((AbstractView) (views.items[i]));

          if ((view.yStart <= yStart && view.yEnd > yStart) || (view.yStart >= yStart && view.yEnd <= yEnd)
              || (view.yStart <= yEnd && view.yEnd > yEnd)) {
            visibleViewsVec.addElement(views.items[i]);
            if (firstVisibleView == -1) {
              firstVisibleView = i;
            }
            lastVisibleView = i;
          }
          if (view.yStart > yEnd) {
            break;
          }
        }
        break;
      case SCROLL_UP:
        temp = lastVisibleView;
        lastVisibleView = -1;

        for (int i = temp; i >= 0; i--) {
          AbstractView view = ((AbstractView) (views.items[i]));

          if ((view.yStart <= yStart && view.yEnd > yStart) || (view.yStart >= yStart && view.yEnd <= yEnd)
              || (view.yStart <= yEnd && view.yEnd > yEnd)) {
            visibleViewsVec.addElement(views.items[i]);
            if (lastVisibleView == -1) {
              lastVisibleView = i;
            }
            firstVisibleView = i;
          }
          if (view.yEnd < yStart) {
            break;
          }
        }
        break;

      default:
        break;
      }
      return visibleViewsVec;
    }

    /**
     * Returns view number <code>no<code> contained within this {@link DataSource}
     * 
     * @param no
     */
    public AbstractView getView(int no) {
      return (AbstractView) views.items[no];
    }

  }

  /**
   * Stops any triggered flick events
   */
  public void stopFlick() {
    flick.stop(true);
  }
}
