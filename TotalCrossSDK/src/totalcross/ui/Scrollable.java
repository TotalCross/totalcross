package totalcross.ui;

/**
 * FlickableContainer is an extension that allows relative motion of the Containers contents using flick animations.
 * Used for scrollable containers that include Flick capability. This interface also exposes the add and remove Timer
 * methods to support animations.
 */
public interface Scrollable
{
   /**
    * Called when the flick animation is started.
    * @param direction The direction that the contents will be scrolled given by one of the following constants:
    * {@link totalcross.ui.event.DragEvent#UP}, {@link totalcross.ui.event.DragEvent#DOWN},
    * {@link totalcross.ui.event.DragEvent#LEFT} or {@link totalcross.ui.event.DragEvent#RIGHT}.
    */
   void flickStarted();

   /**
    * Called when the flick animation is ended.
    * @param aborted Flag indicating if the animation was aborted before ending.
    */
   void flickEnded(boolean aborted);
   
   /**
    * Checks if there is room to flick this container in the given direction.
    * @param direction The direction we want to flick given by one of the following constants: 
    * {@link totalcross.ui.event.DragEvent#UP}, {@link totalcross.ui.event.DragEvent#DOWN},
    * {@link totalcross.ui.event.DragEvent#LEFT} or {@link totalcross.ui.event.DragEvent#RIGHT}.
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
   boolean scrollContent(int xDelta, int yDelta);
   
   Flick getFlick();
}
