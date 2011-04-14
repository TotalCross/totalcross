package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.event.*;
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
 */
public class Flick implements PenListener, TimerListener
{

   /**
    * Indicates that a flick animation is running. Only one can run at a time.
    */
   public static Flick currentFlick;

   /**
    * Flick animations are enable and disabled directly.
    */
   public boolean flickEnabled = true;

   /**
    * Desired animation frame rate in frames/second.
    */
   public static int defaultFrameRate = 25;
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
    */
   public static int defaultLongestFlick = 2500;
   public int longestFlick = defaultLongestFlick;

   /**
    * Flick acceleration in inches/second^2. This value simulates friction to slow the flick motion.
    */
   public static double defaultFlickAcceleration = 2.95;
   public double flickAcceleration = defaultFlickAcceleration;
   
   // Device pixel densities in dpi.
   int resX;
   int resY;
   
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
   private TimerEvent timer;

   // Container owning this Flick object.
   private Scrollable targetAsScrollable;

   // only flickStarted and flickEnded are called
   private Vector scrollables;

   // Container owning this Flick object casted to a Control.
   protected Control targetAsControl;

   /**
    * Create a Flick animation object for a FlickableContainer.
    */
   public Flick(Scrollable c)
   {
      targetAsScrollable = c;
      targetAsControl = (Control) c;

      timer = new TimerEvent();
      targetAsControl.addPenListener(this);
      targetAsControl.addTimerListener(this);

      // So a container event listener can listen to events targeting the container's children.
      targetAsControl.callListenersOnAllTargets = true;
      
      // Adjust resolutions, some devices don't report properly.
      resX = Settings.screenWidthInDPI <= 0 ? 96 : Settings.screenWidthInDPI;
      resY = Settings.screenHeightInDPI<= 0 ? 96 : Settings.screenHeightInDPI;
      
      if ((Settings.screenHeight > 700 && Settings.screenWidth  > 400) ||
          (Settings.screenWidth  > 700 && Settings.screenHeight > 400))
      {
        // Prefer high density on high res screens
        resX = (resX < 150) ? 240 : resX;
        resY = (resY < 150) ? 240 : resY;
      }
   }
   
   public void addScrollable(Scrollable s)
   {
      if (scrollables == null)
         scrollables = new Vector(3);
      scrollables.addElement(s);
   }
   
   private void initialize(int dragId, int x, int y, int t)
   {
      this.dragId = dragId;
      
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
      if (currentFlick == this) 
         stop(true);
   }

   /**
    * Indicates the start of a drag.
    */
   public void penDragStart(DragEvent e)
   {
      if (!flickEnabled || e.target instanceof ScrollBar)
      {
         if (flickEnabled)
            stop(true);
         return;
      }

      initialize(e.dragId, e.absoluteX, e.absoluteY, Vm.getTimeStamp());
   }

   /**
    * Resets the drag start parameters if the direction changes.
    */
   public void penDrag(DragEvent e)
   {
      if (!flickEnabled || dragId != e.dragId)
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
      dragX = x;
      dragY = y;
      
      int absDeltaX = deltaX < 0 ? -deltaX : deltaX;
      int absDeltaY = deltaY < 0 ? -deltaY : deltaY;
      int direction = 0;
      double v;

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
      if (currentFlick != null || !flickEnabled || dragId != e.dragId)
         return;
      
      dragId = -1; // the drag event sequence has ended
      t0 = Vm.getTimeStamp();
      
      // If this penUp event was sent too fast, assume it was sent 1 millisecond
      // after the start of the drag so we can do our computations here.
      if (t0 <= dragT0)
         t0 = dragT0 + 1;
      
      int deltaX = e.absoluteX - dragX0;
      int deltaY = e.absoluteY - dragY0;
      
      // If we could not compute the flick direction before, try to compute
      // the direction at the penUp event
      if (flickDirection == 0)
      {
         int absDeltaX = deltaX < 0 ? -deltaX : deltaX;
         int absDeltaY = deltaY < 0 ? -deltaY : deltaY;

         if (absDeltaX > absDeltaY)
         {
            if (deltaX > 0)
            {
               flickDirection = DragEvent.RIGHT;
               a = -pixelAccelerationX;
            }
            else if (deltaX < 0)
            {
               flickDirection = DragEvent.LEFT;
               a = pixelAccelerationX;
            }
         }
         else
         {
            if (deltaY > 0)
            {
               flickDirection = DragEvent.DOWN;
               a = -pixelAccelerationY;
            }
            else if (deltaY < 0)
            {
               flickDirection = DragEvent.UP;
               a = pixelAccelerationY;
            }
         }
      }
      
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
      else if (t1 > longestFlick)
      {
         t1 = longestFlick;
         v0 = -t1 * a;
      }

      // Start the animation
      int scrollDirection = DragEvent.getInverseDirection(flickDirection);
      if (targetAsScrollable.canScrollContent(scrollDirection, e.target) && targetAsScrollable.flickStarted())
      {
         if (scrollables != null) for (int i = scrollables.size(); --i >= 0;) ((Scrollable)scrollables.items[i]).flickStarted();
         currentFlick = this;
         flickPos = 0;
         targetAsControl.addTimer(timer, 1000 / frameRate);
      }
   }
   
   /**
    * Stops a flick animation if one is running.
    */
   private void stop(boolean aborted)
   {
      if (currentFlick == null) // stop called during computation, so force end of drag sequence
         dragId = -1;
      else if (currentFlick == this) // stop calling during flick
      {
         currentFlick = null;
         targetAsControl.removeTimer(timer);
         if (scrollables != null) for (int i = scrollables.size(); --i >= 0;) ((Scrollable)scrollables.items[i]).flickEnded(aborted);
         targetAsScrollable.flickEnded(aborted);
      }
   }

   public void penUp(PenEvent e)
   {
   }

   /**
    * Processes timer ticks to run the animation.
    */
   public void timerTriggered(TimerEvent e)
   {
      if (e == timer)
      {
         double t = Vm.getTimeStamp() - t0;
         
         // No rounding is done, the maximum rounding error is 1 pixel.
         int newFlickPos = (int) (v0 * t + a * t * t / 2.0);
         int flickMotion = newFlickPos - flickPos;
         flickPos = newFlickPos;

         switch (flickDirection)
         {
            case DragEvent.UP:
            case DragEvent.DOWN:
               if (scrollables != null) for (int i = scrollables.size(); --i >= 0;) ((Scrollable)scrollables.items[i]).scrollContent(0, -flickMotion);
               if (!targetAsScrollable.scrollContent(0, -flickMotion))
                  stop(false);
            break;

            case DragEvent.LEFT:
            case DragEvent.RIGHT:
               if (scrollables != null) for (int i = scrollables.size(); --i >= 0;) ((Scrollable)scrollables.items[i]).scrollContent(-flickMotion, 0);
               if (!targetAsScrollable.scrollContent(-flickMotion, 0))
                  stop(false);
            break;
         }
         
         boolean aborted = currentFlick == null;
         if (aborted || t > t1) // Reached the end.
            stop(aborted);
         
         e.consumed = true;
      }
   }

}
