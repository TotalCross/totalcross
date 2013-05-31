/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.game;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

/**
 * The Animation control class. <br>
 * <pre>
 * This control displays an animation that can be loaded from indexed BMP files
 * (one frame per image) or by a multi-frames BMP. This kind of BMP file contains a
 * list of images having all the same size and that lay side by side.
 * </pre>
 * @author Frank Diebolt
 * @author Guilherme Campos Hazan
 * @version 1.1
 */
public class Animation extends Control
{
  /** Set to true to stop the animation if its parent window is not the top most. */
  public boolean pauseIfNotVisible; // guich@tc100b5_42

  /** Delay between two frames. */
  public int framePeriod;

  /** Reflects the animation play state. */
  public boolean isPlaying;

  /** Reflects the animation pause state. */
  public boolean isPaused;

  /** Frames buffer. */
  public Image framesBuffer;

  // true if the animation has been constructed with a multi-frame BMP
  private boolean multiFramesImage;

  private TimerEvent animTimer;
  private int startFrame,endFrame,incFrame,frameCount,loopCount;

  /** Event notification mask,
   * default value is eventFinish, that means that an event is posted
   * only when the animation finishes.
   **/
  protected int eventsMask = eventFinish; // fdie@341_3 new notification mechanism

  /** no notify at all */
  public static final int eventNone      = 0x0;
  /** notify animation endings */
  public static final int eventFinish    = 0x1;
  /** notify animation loops */
  public static final int eventLoop      = 0x2;
  /** notify animation frames */
  public static final int eventFrame     = 0x4;

  // fdie@341_6+
  /** start function "loops" argument special value to loop endlessly  */
  public static final int LOOPS_UNLIMITED 	= 0x7FFFFFFF;

  protected int curFrame;
  
  /** Dumb field to keep compilation compatibility with TC 1 */
  public int drawOp;

  /**
   * Background image.
   */
  protected Image background; // fdie@400_51 : save animation background

  /**
   * Animation constructor.
   * This constructor may be used by deriving classes.
   */
  protected Animation()
  {
     super();
  }

  /**
   * Animation constructor.
   * 
   * @param frames
   *           single image containing all frames. The number of frames and the transparent colors are gotten from the
   *           image.
   * @param framePeriod
   *           delay in milliseconds between two frames
   * @throws ImageException
   */
  public Animation(Image frames, int framePeriod) throws ImageException
  {
    this(frames, frames.getFrameCount(), framePeriod);
  }

  /**
   * Animation constructor.
   * @param frames single image containing all frames.
   * @param frameCount width in pixels of one frame.
   * @param framePeriod delay in milliseconds between two frames
   */
  public Animation(Image frames,int frameCount,int framePeriod) throws ImageException// fdie@341_2 : direct multi-frame image constructor
  {
    super();
    setImage(frames,frameCount,framePeriod);
  }

  public void setImage(Image frames,int frameCount,int framePeriod) throws ImageException
  {
      this.focusTraversable = false;
      boolean currentlyPlaying = this.isPlaying;
      if (currentlyPlaying)
         stop();
      frames.setFrameCount(frameCount);
      multiFramesImage = frames.getFrameCount() > 1;

      this.framePeriod=framePeriod;
      this.frameCount = frameCount;

      framesBuffer=frames;

      width=frames.getWidth();
      height=frames.getHeight();
      if (currentlyPlaying) this.start(loopCount);
  }

  public void onEvent(Event e)
  {
     if (animTimer != null && animTimer.triggered && !(pauseIfNotVisible && !getParentWindow().isVisible()))
        paintNextFrame();
     else
        super.onEvent(e);
  }

  /**
   * Number of frames in the animation.
   * @return frames amount
   */
  public int size()
  {
    return framesBuffer.getFrameCount();
  }

  /** Returns the prefered width of this control. */
  public int getPreferredWidth()
  {
     return width;
  }

  /** Returns the prefered height of this control. */
  public int getPreferredHeight()
  {
    return height;
  }

   /** Called by the system to draw the animation. **/
   public void onPaint(Graphics gfx)
   {
      // fdie@400_51 : save animation background - no need in OpenGL, since the screen is fully painted at each frame
      if (!Settings.isOpenGL)
         if (background == null)
         {
            // guich@tc100: on a screen rotation, we would have to re-get the background!
            try
            {
               background = new Image(width, height);
               // screen -> buffer
               background.getGraphics().copyRect(parent, x, y, width, height, 0, 0);
            }
            catch (ImageException e)
            {
            }            
         }
         else if (background != null)
            gfx.drawImage(background,0,0);

      // frame lookup table, for special animations
      if (multiFramesImage)
         framesBuffer.setCurrentFrame(curFrame);

      // flsobral@tc100b5_6: argument doClip is now true, this avoids exceptions when the image is larger than the screen.
      gfx.drawImage(framesBuffer, 0, 0, true);
   }

  /**
   * Enable the posting of events.
   * By default the posting of events are disabled.
   */
  public void enableEvents(int mask) // fdie@341_3
  {
    eventsMask=mask;
  }

  /**
   * Pauses a running animation.
   * if the application is not playing, this call has no effect.
   */
  public void pause()
  {
    if (isPlaying) isPaused=true;
  }

  /**
   * Resumes a paused animation.
   * if the application is not playing, this call has no effect.
   */
  public void resume()
  {
    if (isPlaying) isPaused=false;
  }

  /** Stops the animation.
   * if the application is not playing, this call has no effect.
   **/
  public void stop()
  {
    if (isPlaying)
    {
       removeTimer(animTimer);
       isPlaying = isPaused = false;
       animTimer = null;
    }
  }

  private void paintNextFrame()
  {
     if (isPlaying && !isPaused)
     {
        repaintNow();
        if ((eventsMask & eventFrame) != 0)
           postEvent(new AnimationEvent(AnimationEvent.FRAME,this));

        if (curFrame != endFrame)
        {
           curFrame += incFrame;
           if (curFrame < 0 || curFrame >= frameCount)
              curFrame = (curFrame+frameCount) % frameCount;
           framesBuffer.setCurrentFrame(curFrame);
        }
        else
        if (loopCount == LOOPS_UNLIMITED || --loopCount> 0) // fdie@341_6 will be the only loop management once the above boolean loop is removed
        {
           curFrame = startFrame;
           framesBuffer.setCurrentFrame(curFrame);
           if ((eventsMask & eventLoop) != 0)
              postEvent(new AnimationEvent(AnimationEvent.LOOP,this));
        }
        else
        {
           stop();
           if ((eventsMask & eventFinish) != 0)
              postEvent(new AnimationEvent(AnimationEvent.FINISH,this));
        }
     }
  }

  /**
   * Starts the animation with a frame range.
   * This function starts an animation by specifying the frame range and a loop flag.
   * If the application is playing, this call has no effect.
   *
   * @param sFrame start frame
   * @param eFrame end frame
   * @param step frame increment
   * @param loops number of animation iterations
   */
  public void start(int sFrame,int eFrame,int step,int loops)
  {
    if (isPlaying || loops <= 0 || animTimer != null) return;
    startFrame = Math.max(0,sFrame);
    endFrame = Math.min(eFrame,frameCount-1);
    incFrame = step;
    loopCount = loops;
    curFrame = startFrame;
    isPlaying=true;
    isPaused=false;
    animTimer = addTimer(framePeriod);
  }

  // fdie@341_6+
  /**
   * Starts the animation.
   * This function starts the animation and loops the specified amount of time.
   * If the application is playing, this call has no effect.
   *
   * @param loops integer value specifying the number of loops or LOOPS_UNLIMITED.
   * @see #LOOPS_UNLIMITED
   */
  public void start(int loops)
  {
    start(0,frameCount-1,1,loops);
  }
}
