package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.util.*;

/**
 * Flick
 * 
 * Add flick animations to Containers with ScrollBars.
 * 
 * The animations conditionally start on the PenUp after drag events. The animations simulate friction using the
 * constant acceleration formula:
 * 
 * x = x0 + v0 * (t - t0) + a * (t - t0)^2 / 2
 * 
 * The position at t -= t0 is: position = x0 + v0 * t + a * t^2 / 2;
 * 
 * This class is for internal use. You should use the ScrollContainer class instead.
 */
public class Flick implements PenListener, TimerListener
{
   public static final int BOTH_DIRECTIONS = 0;
   public static final int HORIZONTAL_DIRECTION_ONLY = 1;
   public static final int VERTICAL_DIRECTION_ONLY = 2;
   
   public int forcedFlickDirection = BOTH_DIRECTIONS;
   
   /**
    * Indicates that a flick animation is running. Only one can run at a time.
    */
   public static Flick currentFlick;

   /**
    * Desired animation frame rate in frames/second.
    */
   public static int defaultFrameRate = 40; // each frame with 25ms
   public int frameRate = defaultFrameRate;

   /**
    * Shortest flick animation allowed to start in milliseconds. Used to compute the minimum initial velocity. If a
    * flick animation will come to rest in a shorter time than this it isn't done.
    */
   public static int defaultShortestFlick = 300;
   public int shortestFlick = defaultShortestFlick;

   /**
    * Longest flick animation allowed in milliseconds. The maximum initial velocity is limited so that no flick
    * animation takes longer than this to come to rest.
    * 
    * Changing this property will affect the flick object of all Controls. If you have access to a flick
    * control, you can use the longestFlick property instead. Otherwise, set this property to a new value
    * before constructing the control and then set it back to the original value (2500) after the control
    * is constructed.
    */
   public static int defaultLongestFlick = 2500;
   public int longestFlick = defaultLongestFlick;

   /**
    * Flick acceleration in inches/second^2. This value simulates friction to slow the flick motion.
    * Defaults to 2.95 for screen height > 320, or 1.6 otherwise.
    */
   public static double defaultFlickAcceleration = Math.max(Settings.screenWidth,Settings.screenHeight)/Font.NORMAL_SIZE/(Settings.platform.equals(Settings.ANDROID) ? 10.0 : 5.0);
   public double flickAcceleration = defaultFlickAcceleration;

   // Device pixel densities in dpi.
   private int resX,resY;
   
   // Controls flick initialization and the physical drag that started it.
   private int dragId;
   
   // Acceleration converted to pixels/millisecond^2.
   private double pixelAccelerationX;
   private double pixelAccelerationY;

   // Signed acceleration used during a flick animation. Negative when motion is positive.
   private double a;

   // Beginning of a drag
   private int dragT0;
   private int dragX0;
   private int dragY0;

   // Drag progress.
   private int dragX;
   private int dragY;

   // Beginning of a flick
   private double v0;
   private int t0;

   // Flick progress
   private int flickPos;

   // Ending time of a flick.
   private int t1;

   // Direction of a flick.
   private int flickDirection;

   // Timer that runs the flick animation.
   TimerEvent timer;

   // Container owning this Flick object.
   private Scrollable target;

   // only flickStarted and flickEnded are called
   private Vector listeners;

   // used on paged scrolls
   int scrollDistance;
   private int distanceToAbortScroll;
   private int scrollDistanceRemaining;
   private int consecutiveDragCount;
   private int lastFlickDirection;
   public PagePosition pagepos;
   private int lastDragDirection;
   private double lastA;
   private boolean calledFlickStarted;
   private int initialRealPosX,initialRealPosY;
   
   /** True if the user is dragging a control that owns a Flick but the flick didn't started yet 
    * (in that case, currentFlick would not be null). 
    */
   public static boolean isDragging;
   
   /** The maximum accelleration that can be applied when the user keep dragging the container. Defaults to 5. */
   public int maximumAccelerationMultiplier = 5;
   
   /**
    * Create a Flick animation object for a FlickableContainer.
    */
   public Flick(Scrollable s)
   {
      target = s;
      addEvents((Control)s);
      timer = new TimerEvent();
   }

   /** Call this method to set the PagePosition control that will be updated with the current page
    * as the flick occurs. It must have all properties already set, since the Flick will only change
    * the current position.
    */
   public void setPagePosition(PagePosition pp)
   {
      this.pagepos = pp;
   }
   
   /** Used in page scrolls, defines the distance that should be scrolled. Recomputes the time and the 
    * initial velocity to ensure that this is the amount that will be scrolled.
    * Also updates distanceToAbortScroll.
    * @see #setDistanceToAbortScroll(int) 
    */
   public void setScrollDistance(int v)
   {
      scrollDistance = v;
      distanceToAbortScroll = v / (Control.isTablet ? 10 : 5);
   }
   
   /** The distance used to abort the scroll. Set to 0 to make it always scroll a page, even if it
    * dragged just a bit. Defaults to scrollDistance/5.
    * 
    * Be sure to call the method setScrollDistance before calling this one.
    * @see #setScrollDistance(int)
    */
   public void setDistanceToAbortScroll(int v)
   {
      distanceToAbortScroll = v;
   }

   /** Adds another listener of Scrollable events. */
   public void addScrollableListener(Scrollable s)
   {
      if (listeners == null)
         listeners = new Vector(3);
      listeners.addElement(s);
   }
   
   /** Adds an event source to whom this flick will grab pen events. */
   public void addEventSource(Control c)
   {
      addEvents(c);
   }
   
   private void addEvents(Control c)
   {
      c.addPenListener(this);
      c.addTimerListener(this);
      // So a container event listener can listen to events targeting the container's children.
      c.callListenersOnAllTargets = true;
   }

   /** Remove a previously added event source. */
   public void removeEventSource(Control c)
   {
      c.removePenListener(this);
      c.removeTimerListener(this);
      // So a container event listener can listen to events targeting the container's children.
      c.callListenersOnAllTargets = false;
   }
   
   private void initialize(int dragId, int x, int y, int t)
   {
      lastFlickDirection = flickDirection;
      stop(false);
      this.dragId = dragId;
      
      // Adjust resolutions, which can change during rotation. some devices don't report properly.
      resX = Settings.screenWidthInDPI <= 0 ? 96 : Settings.screenWidthInDPI;
      resY = Settings.screenHeightInDPI<= 0 ? 96 : Settings.screenHeightInDPI;
      
      if (Control.isTablet)
      {
        // Prefer high density on high res screens
        resX = (resX < 150) ? 240 : resX;
        resY = (resY < 150) ? 240 : resY;
      }
      
      // Convert inches/second^2 to pixels/millisecond^2
      pixelAccelerationX = flickAcceleration * resX / 1000000.0;
      pixelAccelerationY = flickAcceleration * resY / 1000000.0;

      a = v0 = 0;
      flickDirection = flickPos = t0 = t1 = 0;
      dragT0 = t;
      dragX0 = dragX = x;
      dragY0 = dragY = y;
   }
   
   /**
    * Indicates the start of a simple tap or a drag.
    */
   public void penDown(PenEvent e)
   {
      if (scrollDistance != 0)
      {
         initialRealPosX = target.getScrollPosition(DragEvent.LEFT);
         initialRealPosY = target.getScrollPosition(DragEvent.DOWN);
      }
      if (currentFlick == this && scrollDistance == 0)
         stop(true);
   }

   public boolean isValidDirection(int direction)
   {
      boolean isHoriz = direction == DragEvent.LEFT || direction == DragEvent.RIGHT;
      return forcedFlickDirection == BOTH_DIRECTIONS ||
             (isHoriz && forcedFlickDirection == Flick.HORIZONTAL_DIRECTION_ONLY) ||
             (!isHoriz && forcedFlickDirection == Flick.VERTICAL_DIRECTION_ONLY);
   }
   
   /**
    * Indicates the start of a drag.
    */
   public void penDragStart(DragEvent e)
   {
      if (e.direction != lastDragDirection)
      {
         consecutiveDragCount = 0;
         lastDragDirection = e.direction;
      }
      else
      if (++consecutiveDragCount > maximumAccelerationMultiplier) // used in acceleration 
         consecutiveDragCount = maximumAccelerationMultiplier;
      
      isDragging = true;
      if (e.target instanceof ScrollBar)
         stop(false);
      else
         initialize(e.dragId, e.absoluteX, e.absoluteY, Vm.getTimeStamp());
   }
   
   /**
    * Resets the drag start parameters if the direction changes.
    */
   public void penDrag(DragEvent e)
   {
      if (dragId != e.dragId)
         return;

      int t = Vm.getTimeStamp();
      
      // If this penDrag event was sent too fast, assume it was sent 1 millisecond
      // after the start of the drag so we can do our computations here.
      if (t <= dragT0)
         t = dragT0 + 1;
      
      int x = e.absoluteX;
      int y = e.absoluteY;

      int deltaX = x - dragX;
      int deltaY = y - dragY;
      
      int absDeltaX = deltaX < 0 ? -deltaX : deltaX;
      int absDeltaY = deltaY < 0 ? -deltaY : deltaY;
      int direction = 0;
      double v;

      // if user specified a single direction, ignore other directions
      if (absDeltaY >= absDeltaX && forcedFlickDirection == HORIZONTAL_DIRECTION_ONLY)
         return;//deltaY = absDeltaY = 0;
      else
      if (absDeltaX >= absDeltaY && forcedFlickDirection == VERTICAL_DIRECTION_ONLY)
         return;//deltaX = absDeltaX = 0;
      
      dragX = x;
      dragY = y;
      a = 0;
      
      if (absDeltaX > absDeltaY)
      {
         v = (double) deltaX / (t - dragT0);

         if (deltaX > 0)
         {
            direction = DragEvent.RIGHT;
            a = -pixelAccelerationX;
         }
         else if (deltaX < 0)
         {
            direction = DragEvent.LEFT;
            a = pixelAccelerationX;
         }
      }
      else
      {
         v = (double) deltaY / (t - dragT0);

         if (deltaY > 0)
         {
            direction = DragEvent.DOWN;
            a = -pixelAccelerationY;
         }
         else if (deltaY < 0)
         {
            direction = DragEvent.UP;
            a = pixelAccelerationY;
         }
      }
      
      if (a == 0)
         return;
      
      if (direction != 0)
      {
         if ((flickDirection != 0 && direction != flickDirection) || (-v / a) < shortestFlick) // if flick direction changed or movement was too slow, reset flick start
         {
            dragT0 = t;
            dragX0 = x;
            dragY0 = y;
         }
         flickDirection = direction;
      }
   }

   /**
    * Checks whether or not to start a flick animation.
    */
   public void penDragEnd(DragEvent e)
   {
      boolean cancelFlick = false;
      isDragging = false;
      if (currentFlick != null || dragId != e.dragId) // the penDragEvent can be called for more than one control, so we have to handle it only on one control. That's what dragId for
         return;
            
      dragId = -1; // the drag event sequence has ended
      t0 = Vm.getTimeStamp();
      
      // If this penUp event was sent too fast, assume it was sent 1 millisecond
      // after the start of the drag so we can do our computations here.
      if (t0 <= dragT0)
         t0 = dragT0 + 1;
      
      int deltaX = e.absoluteX - dragX0;
      int deltaY = e.absoluteY - dragY0;
      int absDeltaX = deltaX < 0 ? -deltaX : deltaX;
      int absDeltaY = deltaY < 0 ? -deltaY : deltaY;
      //if (absDeltaX <= Settings.touchTolerance && absDeltaY <= Settings.touchTolerance)
        // return;

      // If we could not compute the flick direction before, try to compute
      // the direction at the penUp event
      if (flickDirection == 0)
      {
         a = 0;
         cancelFlick = (absDeltaY >= absDeltaX && forcedFlickDirection == HORIZONTAL_DIRECTION_ONLY) ||
                       (absDeltaX >= absDeltaY && forcedFlickDirection == VERTICAL_DIRECTION_ONLY);

         if (absDeltaX > absDeltaY)
         {
            if (deltaX > 0)
            {
               flickDirection = cancelFlick ? DragEvent.DOWN : DragEvent.RIGHT;
               a = -pixelAccelerationX;
            }
            else if (deltaX < 0)
            {
               flickDirection = cancelFlick ? DragEvent.UP : DragEvent.LEFT;
               a = pixelAccelerationX;
            }
         }
         else
         {
            if (deltaY > 0)
            {
               flickDirection = cancelFlick ? DragEvent.RIGHT : DragEvent.DOWN;
               a = -pixelAccelerationY;
            }
            else if (deltaY < 0)
            {
               flickDirection = cancelFlick ? DragEvent.LEFT : DragEvent.UP;
               a = pixelAccelerationY;
            }
         }
      }

      if (scrollDistance != 0)
      {
         boolean isHorizontal = flickDirection == DragEvent.RIGHT || flickDirection == DragEvent.LEFT;
         boolean forward = flickDirection == DragEvent.RIGHT || flickDirection == DragEvent.DOWN;
         // case where a flick was started in a single direction but the user made a new drag in a non-valid direction
         // e.g.: was flicking to left but user moved to up
         if (a != 0) 
         {
            lastA = a;
            if ((forward && a < 0) || (!forward && a > 0)) lastA = -a;
         }
         if (a == 0)
            a = lastA;
         forward = a < 0;
         t1 = longestFlick;
         if (lastFlickDirection != 0 && lastFlickDirection != flickDirection)
            consecutiveDragCount = 0;
         
         // compute what the user ran against our scrolling window
         // note that this is not the same of xTotal
         int realPos = target.getScrollPosition(flickDirection);
         int rpos = realPos;
         if (realPos < 0) realPos = -realPos;
         int runnedDistance    = realPos % scrollDistance;
         int remainingDistance = scrollDistance - runnedDistance;
         
         // how much the user dragged. used to go back
         int dragged = e.direction == DragEvent.LEFT || e.direction == DragEvent.RIGHT ? e.xTotal : e.yTotal;
         if (dragged < 0) dragged = -dragged;
         int initialRealPos = isHorizontal ? initialRealPosX : initialRealPosY;
         
         // not enough to move?
         if (cancelFlick || (consecutiveDragCount <= 1 && distanceToAbortScroll > 0 && dragged < distanceToAbortScroll))
         {
            int s0 = initialRealPos < 0 ? -initialRealPos : initialRealPos;
            int sf = rpos < 0 ? -rpos : rpos;
            
            if ((s0 % scrollDistance) != 0)
               scrollDistanceRemaining = forward ? runnedDistance : remainingDistance;
            else
            {
               if (s0 == sf)
                  ;
               else
               if (s0 < sf)
               {
                  if (a > 0) a = -a;
                  forward = false;
                  lastFlickDirection = flickDirection = isHorizontal ? DragEvent.LEFT : DragEvent.UP;
                  scrollDistanceRemaining = sf-s0;
               }
               else
               {
                  if (a < 0) a = -a;
                  forward = true;
                  lastFlickDirection = flickDirection = isHorizontal ? DragEvent.RIGHT : DragEvent.DOWN;
                  scrollDistanceRemaining = s0-sf;
               }
               consecutiveDragCount = 0;
            }
         }
         else scrollDistanceRemaining = forward ? runnedDistance : remainingDistance;
         if (consecutiveDragCount > 1) 
            scrollDistanceRemaining += (consecutiveDragCount-1) * scrollDistance; // acceleration
         
         v0 = (scrollDistanceRemaining - (a > 0 ? -a : a) * t1 * t1 / 2) / t1;
         if (a > 0)
            v0 = -v0;
      }
      else
      if (cancelFlick)
         return;
      else
      {
         if (a == 0)
            return;
         // Compute v0.
         switch (flickDirection)
         {
            case DragEvent.UP:
            case DragEvent.DOWN:
               v0 = (double) deltaY / (t0 - dragT0);
            break;
   
            case DragEvent.LEFT:
            case DragEvent.RIGHT:
               v0 = (double) deltaX / (t0 - dragT0);
            break;
               
            default:
               return;
         }
   
         // When the flick ends. No rounding is done, the maximum rounding error is 1 millisecond.
         t1 = (int) (-v0 / a);
   
         // Reject animations that are too slow and apply the speed limit.
         if (t1 < shortestFlick)
            return;
         if (t1 > longestFlick)
         {
            t1 = longestFlick;
            v0 = -t1 * a;
         }
      }
      
      // Start the animation
      int scrollDirection = DragEvent.getInverseDirection(flickDirection);
      calledFlickStarted = false;
      if (target.canScrollContent(scrollDirection, e.target))
      {
         calledFlickStarted = true;
         if (target.flickStarted())
         {
            callListeners(true,false);
            currentFlick = this;
            flickPos = 0;
            ((Control)target).addTimer(timer, 1000 / frameRate);
         }
      }
   }

   /** Calls the listeners of this flick. */
   public void callListeners(boolean started, boolean atPenDown)
   {
      if (listeners != null) 
         for (int i = listeners.size(); --i >= 0;)
            if (started)
               ((Scrollable)listeners.items[i]).flickStarted();
            else
               ((Scrollable)listeners.items[i]).flickEnded(atPenDown);
   }
   
   /**
    * Stops a flick animation if one is running.
    */
   void stop(boolean atPenDown)
   {
      if (currentFlick == null) // stop called during computation, so force end of drag sequence
         dragId = -1;
      else 
      if (currentFlick == this) // stop calling during flick
         currentFlick = null;
      
      if (calledFlickStarted)
      {
         calledFlickStarted = false;
         ((Control)target).removeTimer(timer);
         callListeners(false, atPenDown);
         target.flickEnded(atPenDown);
      }
   }

   public void penUp(PenEvent e)
   {
      if (currentFlick == null)
         stop(false);
   }

   /**
    * Processes timer ticks to run the animation.
    */
   public void timerTriggered(TimerEvent e)
   {
      if (e == timer && !totalcross.unit.UIRobot.abort)
      {
         double t = Vm.getTimeStamp() - t0;
         
         // No rounding is done, the maximum rounding error is 1 pixel.
         int newFlickPos = (int) (v0 * t + a * t * t / 2.0);
         int absNewFlickPos = newFlickPos < 0 ? -newFlickPos : newFlickPos;
         // check if the amount will overflow the scrollDistance
         if (scrollDistance != 0 && absNewFlickPos > scrollDistanceRemaining) 
            newFlickPos = newFlickPos < 0 ? -scrollDistanceRemaining : scrollDistanceRemaining;
         int flickMotion = newFlickPos - flickPos;
         flickPos = newFlickPos;
         boolean endReached = flickMotion == 0;

         if (!endReached)
            switch (flickDirection)
            {
               case DragEvent.UP:
               case DragEvent.DOWN:
                  if (listeners != null) for (int i = listeners.size(); --i >= 0;) ((Scrollable)listeners.items[i]).scrollContent(0, -flickMotion, true);
                  if (!target.scrollContent(0, -flickMotion, true))
                     endReached = true;
               break;
   
               case DragEvent.LEFT:
               case DragEvent.RIGHT:
                  if (listeners != null) for (int i = listeners.size(); --i >= 0;) ((Scrollable)listeners.items[i]).scrollContent(-flickMotion, 0, true);
                  if (!target.scrollContent(-flickMotion, 0, true))
                     endReached = true;
               break;
            }
         if (endReached || currentFlick == null || t > t1) // Reached the end.
         {
            lastDragDirection = lastFlickDirection = consecutiveDragCount = 0;
            stop(false);
         }
         if (pagepos != null)
         {
            int p = target.getScrollPosition(flickDirection);
            if (p < 0) p = -p;
            pagepos.setPosition((p/scrollDistance)+1);
         }
         
         e.consumed = true;
      }
   }

}
