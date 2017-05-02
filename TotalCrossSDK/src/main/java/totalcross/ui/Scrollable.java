package totalcross.ui;

/**
 * Scrollable is an extension that allows relative motion of the Containers contents using flick animations.
 * Used for scrollable containers that include Flick capability. This interface exposes the
 * methods used to support flick animations.
 */
public interface Scrollable
{
   /**
    * Called when the flick animation is started.
    */
   boolean flickStarted();

   /**
    * Called when the flick animation is ended.
    * @param atPenDown Flag indicating if the animation ended due to a pendown event.
    */
   void flickEnded(boolean atPenDown);
   
   /**
    * Checks if there is room to flick this container in the given direction.
    * @param direction The direction we want to flick given by one of the following constants: 
    * @param target The target of the series of PEN events that triggered this flick attempt.
    * 
    * @return true if the container can flick in the indicated direction.
    */
   boolean canScrollContent(int direction, Object target);
   
   /**
    * Performs a relative move.
    * 
    * @param xDelta The relative amount of pixels to move in the X axis.
    * @param yDelta The relative amount of pixels to move in the Y axis.
    * @returns true if the Container was able to move in the indicated direction.
    */
   boolean scrollContent(int xDelta, int yDelta, boolean fromFlick);
   
   /** Returns the current flick object. */
   Flick getFlick();
   
   /** Returns the current position given the direction. Used on page scrolls.
    */
   int getScrollPosition(int direction);
   
   /** Returns true if the control was scrolled since last pen down */
   boolean wasScrolled();
}
