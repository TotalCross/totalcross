/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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

package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Hashtable;

/**
 * Button is a push button control. It supports multilined text, a standalone image,
 * or text mixed with images. Image buttons have a fade look when not enabled.
 * If you want a sticky button, set the isSticky field or use the PushButtonGroup class
 * selecting the CHECK mode.
 * <p>
 * Here is an example showing a push button being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 *    Button btn1,btn2;
 *
 *    public void initUI()
 *    {
 *       add(btn1 = new Button("Push me\nPlease"), CENTER,TOP);
 *       add(btn2 = new Button(new Image("myimage.png")), CENTER,AFTER+3);
 *    }
 *
 *    public void onEvent(Event event)
 *    {
 *       if (event.type == ControlEvent.PRESSED)
 *       {
 *          if (event.target == btn1)
 *          {
 *             ... handle btn1 being pressed
 * </pre>
 */

public class Button extends Control
{
   /** Specifies no border for this button. Used in the setBorder method. */
   public static final byte BORDER_NONE = 0;
   /** Specifies a single-lined border for this button. Used in the setBorder method. */
   public static final byte BORDER_SIMPLE = 1;
   /** Specifies a 3d border for this button. Used in the setBorder method. */
   public static final byte BORDER_3D = 2;
   /** Specifies a vertical 3d-gradient border for this button. Used in the setBorder method.
    * Note that, in this mode, the back and fore colors are set using the borderColor3DG,
    * topColor3DG and bottomColor3DG properties. */
   public static final byte BORDER_3D_VERTICAL_GRADIENT = 3;
   /** Specifies a horizontal 3d-gradient border for this button. Used in the setBorder method.
    * Note that, in this mode, the back and fore colors are set using the borderColor3DG,
    * topColor3DG and bottomColor3DG properties. */
   public static final byte BORDER_3D_HORIZONTAL_GRADIENT = 4; // guich@tc110_11
   /** Specifies that the image passed in the constructor is a gray image that will be
    * recolorized and resized to the text's size. To create the image that will work with
    * this border type, do this:
    * <ol>
    * <li> Get an empty image from somewhere in the web (<a href='http://www.aaa-buttons.com/'>this site</a> is a good place; choose a light background image to prevent problems described in step 5).
    * <li> Do not resize the button.
    * <li> Convert the button to grayscale (you can download irfanview and choose menu Image / Convert to Grayscale
    * <li> When saving the image (as png), don't forget to save the transparent color (irfanview has an option to save it in the save dialog).
    * <li> Images with a smooth round border should have the background colorized to the target background color; otherwise, the results will not be perfect. 
    * </ol>
    * <p>
    * Important: you must set the button colors before calling setBorder.
    * Here's a sample:
    * <pre>
      Button btn = new Button("Bárbara\nHazan", new Image("button1.png"), CENTER, 8);
      btn.setBackColor(backColor);
      btn.borderColor3DG = 0x008800; // used to colorize the image
      btn.setFont(font.asBold());
      btn.setBorder(Button.BORDER_GRAY_IMAGE);
      add(btn,CENTER,CENTER);
    * </pre>
    * @since TotalCross 1.12 
    */
   public static final byte BORDER_GRAY_IMAGE = 5; // guich@tc112_25
   /** Specifies a rounded border for this button. Used in the setBorder method. Note that you MUST use PREFERRED when specifying button's height.
    * @since TotalCross 3.04 
    */
   public static final byte BORDER_ROUND = 6;
   
   /** Set to true to draw the button borders if transparentBackground is true.
    * @since TotalCross 1.15 
    */
   public boolean drawBordersIfTransparentBackground; // guich@tc115_74

   /** The current frame in a multi-frame image. */
   public int currentFrame; // defaults to 0

   // guich@tc122_46: added auto-repeat for button
   /** Set to true to enable auto-repeat feature for this button. The PRESSED event will be sent while this button is held.
    * @see #INITIAL_DELAY
    * @see #AUTO_DELAY
    * @since TotalCross 1.22
    */
   public boolean autoRepeat; // guich@tc122_47
   private TimerEvent autoRepeatTimer;
   
   /** The initial delay to start the auto-repeat. Defaults to 600ms. */
   public int INITIAL_DELAY = 600;
   /** The frequency in which the PRESSED event will be posted after the INITIAL_DELAY was reached. Defaults to 150ms. */
   public int AUTO_DELAY = 150;
   
   protected String text;
   protected Image img,img0,imgDis;
   protected boolean armed;
   protected byte border = BORDER_3D;
   protected int tx0,ty0,ix0,iy0;
   protected int fColor,pressColor=-1;
   protected int fourColors[] = new int[4];
   private int txtPos,tiGap,maxTW;
   private boolean fixPressColor;
   private static Hashtable htGrays;
   private Image colorized;
   private Image npback;
   private boolean isAndroidStyle;
   private boolean skipPaint;
   private Rect clip;
   private int localCommonGap;
   
   private String []lines;
   private int []linesW;

   /** Sets the image that will be displayed when the user press this button.
    * Only works on Imaged buttons.
    * Here's a sample:
    * <pre>
    * Image img = btn.getImage();
    * img = img.getFrameInstance(0); // gets a copy of the image
    * img.applyColor(Color.RED); // colorize as red
    * btn.pressedImage = img;
    * // another option: btn.pressedImage = btn.getImage().getFadedInstance(Color.RED);
    * </pre>
    * @since TotalCross 1.15
    */
   public Image pressedImage; // guich@tc115_26
   
   /** If ui style is vista and border is BORDER_NONE, setting this to false will make
    * the button have a vista-like background. Otherwise, it will have a flat background.
    * @since TotalCross 1.0 beta 4.
    */
   public boolean flatBackground = true;

   /** The corner radius of this button when BORDER_3D_GRADIENT border style is active. 
    * Set to 10 when setBorder is called. */
   public int cornerRadius3DG;
   /** The border width of this button when BORDER_3D_GRADIENT border style is active. 
   * Set to 2 when setBorder is called. */
   public int borderWidth3DG;
   /** The border color of this button when BORDER_3D_GRADIENT border style is active. 
   * Set to 0x00108A when setBorder is called. */
   public int borderColor3DG;
   /** The top color of this button when BORDER_3D_GRADIENT border style is active. 
   * Set to 0xDCDCFF when setBorder is called. */
   public int topColor3DG;
   /** The bottom color of this button when BORDER_3D_GRADIENT border style is active. 
   * Set to Color.BLUE when setBorder is called. */
   public int bottomColor3DG;
   
   /** Change to a color value to highlight the button's text. The highlight is made
    * drawing the button in x-1,y-1; x+1,y-1; x-1,y+1; x+1,y+1 positions.
    * Defaults to -1.
    * @since TotalCross 1.12
    * @deprecated Fix the typo, use Control.textShadowColor instead.
    */
   public int hightlightColor = -1; // guich@tc112_29

   /** Set commonGap to a value to make all further buttons with the same internal
     * gap. 
     * Remember to save the current value and restore it when done.
     * Changing this also affects the size of the ScrollBars created.
     * The value is stored locally on the constructor so it works later if the screen is resized.
     */
   public static int commonGap; // guich@300_3
   
   /** Set to true to make this button sticky: it keeps pressed until the next click.
    * Note that this disables autoRepeat.
    * @since TotalCross 1.25
    */
   public boolean isSticky; // guich@tc125_32
   
   /** Used when the textPosition is RIGHT_OF. Specifies the text that will be used
    * as reference to center this button's text with that one. Usually, you find the biggest
    * text of a set of buttons and place it here.
    * @since TotalCross 1.27
    */
   public String relativeToText; // guich@tc126_28
   
   /** Set to true to put a line under the text. Here's an example:
    * <pre>
      Button btLink;
      btLink = new Button("(Guide 124...)");
      btLink.underlinedText = true;
      btLink.setBorder(BORDER_NONE);
      btLink.setForeColor(Color.BLUE);
      add(btLink,LEFT,TOP,FILL, PREFERRED);
    * </pre>
    * @since TotalCross 2.0
    */
   public boolean underlinedText;
   
   /** The height of the image based on the button's height, ranging from 1 to 100. Used when an image is passed as parameter in one of the constructors. 
    */
   public int imageHeightFactor;
   /** Fills the button when pressed even if transparentBackground is set. */
   public boolean fillWhenPressedOnTransparentBackground;

   /** Creates a button that shows the given text and image.
    * @param text The text to be displayed
    * @param img The image to be displayed
    * @param textPosition Where to place the text (supports only LEFT, TOP, RIGHT, 
    * BOTTOM, CENTER - no adjustments!). Also supports RIGHT_OF, which uses the relativeToText field 
    * as another button to be used as reference to be able to center the title among a set of buttons; the text will be placed at the right)
    * @param gap The space between the text and the image
    * @since TotalCross 1.0
    */
   public Button(String text, Image img, int textPosition, int gap)
   {
      if (text != null) setText(text);
      this.img = this.img0 = img;
      this.tiGap = gap;
      txtPos = textPosition;
      this.localCommonGap = commonGap;
   }

   /** Creates a button displaying the given text. */
   public Button(String text)
   {
      this(text,null,0,0);
   }

   /** Sets the text that is displayed in the button. */
   public void setText(String text)
   {
      this.text = text;
      lines = Convert.tokenizeString(text,'\n');
      onFontChanged();
      if (txtPos == 0)
         img = null;
      Window.needsPaint = true;
   }

   /** Creates a button with the given image. The transparentColor property of
    * the Image must be set before calling this constructor. */
   public Button(Image img)
   {
       this(null,img,0,0);
   }
   
   /** Sets the image that is displayed in the button. The
    transparentColor property of the Image must be set before calling this
    method.<br>
    Note: this method does not resize the button to fit the image.*/
   public void setImage(Image img)
   {
      this.img = img;
      if (txtPos == 0)
         text = null;
      onBoundsChanged(false);
      Window.needsPaint = true;
   }

   /** Sets the color that the button's background will go ('armed color') when the button gets
       a PENDOWN event. The default is the cursor color for the background.
       
       In Android user interface style, using a bright color may result in a white background. 
       Use a darker color in this case.
       @since SuperWaba 4.21
       @param newColor New color to set as the background when pressed.
   */
   public void setPressedColor(int newColor)  // vik@421_28
   {
      pressColor = newColor;
      fixPressColor = true; // don't allow it change
   }

   /** Sets the style of the border.
    * @see #BORDER_NONE
    * @see #BORDER_SIMPLE
    * @see #BORDER_3D
    * @see #BORDER_3D_HORIZONTAL_GRADIENT
    * @see #BORDER_3D_VERTICAL_GRADIENT
    * @see #BORDER_GRAY_IMAGE
    */
   public void setBorder(byte border)
   {
      this.border = border;
      switch (border)
      {
         case BORDER_ROUND:
            transparentBackground = true;
            break;
         case BORDER_3D: // guich@tc112_30
         case BORDER_3D_HORIZONTAL_GRADIENT:
         case BORDER_3D_VERTICAL_GRADIENT:
            cornerRadius3DG = 10;
            borderWidth3DG = 2;
            borderColor3DG = 0x00108A;
            topColor3DG = 0xDCDCFF;
            bottomColor3DG = Color.BLUE;
            break;
         case BORDER_GRAY_IMAGE: // guich@tc112_25
            String key = img.hashCode()+"|"+borderColor3DG+"|"+backColor;
            if (htGrays == null)
               htGrays = new Hashtable(3);
            colorized = (Image)htGrays.get(key);
            if (colorized == null)
            {
               try
               {
                  colorized = img.getFrameInstance(0);
                  colorized.applyColor(borderColor3DG);
                  htGrays.put(key,colorized);
               }
               catch (ImageException e)
               {
                  //flsobral@tc150: colorized will remain null.
               }
            }
            img = null; // guich@tc113_6: will be set again in onBoundsChanged
            break;
      }
   }

   /** Gets the text displayed in the button. */
   public String getText()
   {
      return text!=null?text:"image";
   }

   /** Returns the preffered width of this control. */
   public int getPreferredWidth()
   {
      int border = this.border < 2 ? this.border : 2; // guich@tc112_31
      int prefW;
      int tw = text == null ? 0 : maxTW;
      int iw = (this.border == BORDER_GRAY_IMAGE || img  == null) ? 0 : img.getWidth(); // guich@tc120_1: using a gray image does not take the image into consideration
      switch (txtPos)
      {
         case BOTTOM: case TOP: 
            prefW = Math.max(tw,iw); 
            break;
         case CENTER_OF: 
         case RIGHT_OF:
         case LEFT: 
         case RIGHT: 
            prefW = tw + getGap(tiGap) + iw; 
            break;
         case CENTER:           
            prefW = Math.max(tw,iw)+1; 
            if (img != null)
               border = 0; 
            if (this.border == BORDER_GRAY_IMAGE) 
               prefW += getGap(tiGap)*2;  
            break; // guich@tc112_26 - guich@tc113_6: use tiGap too
         default:               
            prefW = tw + iw;
      }
      if (border == BORDER_ROUND)
         prefW += getPreferredHeight();
      return prefW + ((localCommonGap+border) << 1) + (img != null && text == null ? 1 : 0); // guich@tc100b4_16: add an extra pixel if image-only
   }

   /** Returns the preffered height of this control. */
   public int getPreferredHeight()
   {
      int border = this.border < 2 ? this.border : 2; // guich@tc112_31
      int prefH;
      int th = text == null ? 0 : ((uiVista?1:0)+fmH*lines.length);
      int ih = (this.border == BORDER_GRAY_IMAGE || img  == null) ? 0 : img.getHeight(); // guich@tc120_1: using a gray image does not take the image into consideration
      switch (txtPos)
      {
         case BOTTOM: 
         case TOP: 
            prefH = th + getGap(tiGap) + ih; 
            break;
         case CENTER_OF: 
         case RIGHT_OF:
         case LEFT: 
         case RIGHT: 
            prefH = Math.max(th,ih); 
            break;
         case CENTER:           
            prefH = Math.max(th,ih)+1;
            if (img != null)
               border = 0; 
            if (this.border == BORDER_GRAY_IMAGE) 
               prefH += getGap(tiGap)*2; 
            break; // guich@tc112_26 - guich@tc113_6: use tiGap too
         default:               
            prefH = th + ih;
      }
      return prefH + ((localCommonGap+border) << 1) + (img != null && text == null ? 1 : 0); // guich@tc100b4_16: add an extra pixel if image-only
   }

   /** Press and depress this Button to simulate that the user had clicked on it.
    * Does not generate events.
    * If isSticky is true, inverts the button state.
    * @see #isSticky 
    * @since SuperWaba 5.5
    */
   public void simulatePress() // guich@550_27
   {
      if (isSticky)
         press(armed = !armed);
      else
      {
         press(true);
         Vm.safeSleep(100);// on win32 is needed, but and in the devices?
         press(false);
      }
   }
   
   /** Returns true if the button is pressed or not. Only makes sense if <code>isSticky</code> is true.
    * You can change the button state programatically using simulatePress.
    * @see #simulatePress()
    * @see #isSticky 
    * @since TotalCross 1.25 
    */
   public boolean isPressed() // guich@tc125_32
   {
      return armed;
   }

   /** Called by the system to pass events to the button. */
   public void onEvent(Event event)
   {
      PenEvent pe;
      if (isEnabled())
      switch (event.type)
      {
         case TimerEvent.TRIGGERED:
            if (autoRepeatTimer != null && autoRepeatTimer.triggered && armed)
            {
               if (autoRepeatTimer.millis == INITIAL_DELAY)
                  autoRepeatTimer.millis = AUTO_DELAY;
               postPressedEvent();
            }
            break;
         case KeyEvent.ACTION_KEY_PRESS: // guich@550_15
            simulatePress();
            postPressedEvent();
            break;
         case PenEvent.PEN_DOWN:
            armed = isSticky ? !armed: true;
            repaintNow();
            if (!isSticky && autoRepeat) 
               autoRepeatTimer = addTimer(INITIAL_DELAY);
            break;
         case PenEvent.PEN_UP:
            if (autoRepeat && autoRepeatTimer != null)
               disableAutoRepeat();
            if (!isSticky && armed) press(armed = false);
            pe = (PenEvent)event;
            if ((!Settings.fingerTouch || !hadParentScrolled()) && isInsideOrNear(pe.x,pe.y))
               postPressedEvent();
            break;
         case PenEvent.PEN_DRAG:
            pe = (PenEvent)event;
            boolean lArmed = isInsideOrNear(pe.x,pe.y);
            if (armed != lArmed)
               press(armed = lArmed);
            break;
         case ControlEvent.FOCUS_OUT:
            if (!isSticky) armed = false;
      }
   }

   private void disableAutoRepeat() // luciana@570_22
   {
      removeTimer(autoRepeatTimer);
      autoRepeatTimer = null;
      armed = false;
      Window.needsPaint = true; // guich@tc123_3
   }

   /** Simulate the press or release of this button. Does not generate events. */
   public void press(boolean pressed)
   {
      if (transparentBackground && !isAndroidStyle) // guich@tc114_77: repaint now the parent's background otherwise it will leave dirt in the background
      {
         boolean eus = Window.enableUpdateScreen;
         Window.enableUpdateScreen = false;
         skipPaint = true;
         parent.repaintNow();
         skipPaint = false;
         Window.enableUpdateScreen = eus;
      }
      armed = pressed; // some drawing routines does not receive the armed parameter, so we must set it here.
      repaintNow();
   }

   /** Called by the system to draw the button. it cuts the text if the button is too small. */
   public void onPaint(Graphics g)
   {
      if (skipPaint) return;
      boolean isRound = border == BORDER_ROUND;
      if (isAndroidStyle)
      {
         if (!Settings.isOpenGL && !Settings.onJavaSE)
         {
            g.getClip(clip);
            g.backColor = g.getPixel(clip.x,clip.y); // use color painted by the parent
            g.fillRect(0,0,width,height);
         }
      }
      else
      if (!isRound && (!transparentBackground || (armed && fillWhenPressedOnTransparentBackground) || drawBordersIfTransparentBackground))
         paintBackground(g);

      if (isRound)
      {
         g.backColor = backColor;
         g.fillRoundRect(0,0,width,height,height/2);
      }
      else
      if (isAndroidStyle)
         paintImage(g, true, 0,0);
      
      int border = txtPos == CENTER ? 0 : Math.min(2,this.border); // guich@tc112_31
      g.setClip(border,border,width-(border<<1),height-(border<<1)); // guich@101: cut text if button is too small - guich@510_4

      int tx=tx0;
      int ty=ty0;
      int ix=ix0;
      int iy=iy0;
      boolean is3d = border == BORDER_3D_HORIZONTAL_GRADIENT || border == BORDER_3D_VERTICAL_GRADIENT;
      if (armed && !isAndroidStyle && (is3d || uiVista || (img != null && text == null))) // guich@tc100: if this is an image-only button, let the button be pressed
      {
         int inc = is3d ? borderWidth3DG : 1;
         tx += inc; ix += inc;
         ty += inc; iy += inc;
      }
      g.foreColor = fColor;
      if (img != null)
         paintImage(g, false, ix,iy);
      
      if (text != null)
         paintText(g,tx,ty);
   }

   protected void onFontChanged()
   {
       if (text != null)
       {
          if (linesW == null || linesW.length != lines.length)
             linesW = new int[lines.length];
          int []linesW = this.linesW;
          maxTW = 0;
          for (int i = lines.length-1; i >= 0; i--)
          {
             linesW[i] = fm.stringWidth(lines[i]);
             maxTW = Math.max(maxTW, linesW[i]);
          }
       }
      onBoundsChanged(false);
   }

   protected void onBoundsChanged(boolean screenChanged)
   {
      int tiGap = getGap(this.tiGap);
      if (imageHeightFactor != 0 && img0 != null)
         try 
         {
            img = Settings.enableWindowTransitionEffects ? img0.smoothScaledFixedAspectRatio(height*imageHeightFactor/100,true) : img0.hwScaledFixedAspectRatio(height*imageHeightFactor/100,true);
            if (img.getWidth() > this.width-4)
               img = Settings.enableWindowTransitionEffects ? img0.smoothScaledFixedAspectRatio(width-4,false) : img0.hwScaledFixedAspectRatio(width-4,false);
            img.setCurrentFrame(currentFrame);
            imgDis = null;
         } catch (Throwable t) {img = img0;}
      isAndroidStyle = uiAndroid && this.border == BORDER_3D;
      if (isAndroidStyle && clip == null)
         clip = new Rect();
      npback = null;
      int th=0,iw=0,ih=0;
      
      if (isAndroidStyle && width > 0 && height > 0)
         transparentBackground = true;
      // compute where to draw each item to keep it centered
      if (text != null)
      {
         th = fmH * lines.length;
         tx0 = (width  - maxTW) / 2;
         ty0 = (height - th) / 2;
      }
      if (border == BORDER_GRAY_IMAGE) // guich@tc113_6: recompute image's
      {
         if (colorized != null) //flsobral@tc150: colorized may now be null if there was not enough memory to create it at setBorder
            try
            {
               img = colorized.getSmoothScaledInstance(width,height);
            }
            catch (ImageException e)
            {
               //flsobral@tc150: keep old value 
            }
      }
      if (img != null)
      {
         iw = img.getWidth();
         ih = img.getHeight();
         ix0 = (width - iw) >> 1;
         iy0 = (height- ih) >> 1;
      }
      if (txtPos != 0)
      {
         int restH = (height - (ih + tiGap + th)) >> 1;
         int restW = (width  - (iw + tiGap + maxTW)) >> 1;
         switch (txtPos)
         {
            case TOP:    ty0 = restH; iy0 = ty0 + th + tiGap; break;
            case BOTTOM: iy0 = restH; ty0 = iy0 + ih + tiGap; break;
            case LEFT:   tx0 = restW; ix0 = tx0 + maxTW + tiGap; break;
            case RIGHT:  ix0 = restW; tx0 = ix0 + iw + tiGap; break;
            case RIGHT_OF: // guich@tc126_28
            {
               if (relativeToText == null)
                  throw new NullPointerException("When using RIGHT_OF, you must set the Button's relativeToText field.");
               int rw = 0;
               if (relativeToText.indexOf('\n') == -1)
                  rw = fm.stringWidth(relativeToText);
               else
               {
                  String []relToLines = Convert.tokenizeString(relativeToText,'\n');
                  rw = fm.getMaxWidth(relToLines, 0, relToLines.length);
               }
               ix0 = (width  - (iw + tiGap + rw)) >> 1;; 
               tx0 = ix0 + iw + tiGap + (rw-maxTW)/2; break;
            }
            case CENTER: break;
         }
      }
   }

   protected void onColorsChanged(boolean colorsChanged)
   {
      boolean enabled = isEnabled();
      npback = null;
      if (!enabled && autoRepeatTimer != null)
         disableAutoRepeat();
      fColor = enabled ? foreColor : Color.getCursorColor(foreColor); // guich@tc110_49: use getCursorColor so a white forecolor shows up as changed
      if (!isAndroidStyle)
         Graphics.compute3dColors(enabled,backColor,foreColor,fourColors);
      if (!fixPressColor) pressColor = Color.getCursorColor(backColor); // guich@450_35: only assign a new color if none was set. - guich@567_11: moved to outside the if above
      if (!isAndroidStyle)
         fourColors[1] = pressColor;
   }

   /** Paint button's background. */
   protected void paintBackground(Graphics g)
   {
      boolean enabled = isEnabled();
      if (!transparentBackground || (armed && fillWhenPressedOnTransparentBackground))
      {
         if (border == BORDER_GRAY_IMAGE)
         {
            g.backColor = backColor;
            g.fillRect(0,0,width,height);
            return;
         }
         if (border == BORDER_3D_HORIZONTAL_GRADIENT || border == BORDER_3D_VERTICAL_GRADIENT)
         {
            g.drawRoundGradient(0,0,width - 1, height - 1, cornerRadius3DG,cornerRadius3DG,cornerRadius3DG,cornerRadius3DG,borderColor3DG,borderColor3DG,border == BORDER_3D_VERTICAL_GRADIENT);
            if (armed)
               g.drawRoundGradient(borderWidth3DG,borderWidth3DG,width - 1, height - 1,cornerRadius3DG,cornerRadius3DG - borderWidth3DG,cornerRadius3DG - borderWidth3DG,cornerRadius3DG,armed && fixPressColor ? pressColor : topColor3DG,enabled ? bottomColor3DG : topColor3DG,border == BORDER_3D_VERTICAL_GRADIENT); // guich@tc120_42: use pressColor if defined
            else
               g.drawRoundGradient(0,0,width - borderWidth3DG - 1, height - borderWidth3DG - 1,cornerRadius3DG,cornerRadius3DG - borderWidth3DG,cornerRadius3DG - borderWidth3DG,cornerRadius3DG,armed && fixPressColor ? pressColor : topColor3DG, enabled ? bottomColor3DG : topColor3DG,border == BORDER_3D_VERTICAL_GRADIENT); // guich@tc120_42
            return;
         }
         switch (Settings.uiStyle)
         {
            case Settings.Flat:
               g.backColor = img == null && armed ? pressColor : backColor; // guich@tc100b4_13: also check if img is null
               g.fillRect(0,0,width,height);
               break;
            case Settings.Android:
            case Settings.Vista: // guich@573_6
            {
               if (border == BORDER_NONE && flatBackground) // guich@582_14
               {
                  g.backColor = armed && fixPressColor ? pressColor : backColor;
                  g.fillRect(0,0,width,height);
               }
               else
               if (enabled)
                  g.fillVistaRect(0,0,width,height,backColor, armed,false);
               else
               // guich@582_14 - commented if (border != BORDER_NONE)
               {
                  g.backColor = backColor;
                  g.fillRect(0,0,width,height);
                  if (border != BORDER_NONE)
                     g.drawVistaRect(0,0,width,height,fColor,fColor,fColor,fColor);
               }
               break;
            }
         }
      }
      if (border != BORDER_NONE && !isAndroidStyle && !(uiVista && !enabled))
         g.draw3dRect(0,0,width,height,armed ?Graphics.R3D_LOWERED:Graphics.R3D_RAISED,false,border == BORDER_SIMPLE,fourColors);
   }

   /** Paint button's text. */
   protected void paintText(Graphics g, int tx, int ty)
   {
      int shade = !isEnabled() ? -1 : textShadowColor != -1 ? textShadowColor : hightlightColor;
      if (underlinedText) g.backColor = foreColor;
      for (int i = 0; i < lines.length; i++, ty += fmH)
      {
         int txx = tx + ((maxTW - linesW[i]) >> 1);
         g.drawText(lines[i], txx, ty, shade != -1, shade);
         if (underlinedText)
            g.fillRect(txx,ty+fm.ascent+1, linesW[i], (fmH-1)>>3); 
      }
   }

   protected void paintImage(Graphics g, boolean bkg, int ix, int iy)
   {
      boolean enabled = isEnabled();
      if (bkg) // only in uiAndroid
         try
         {
            if (npback == null)
               npback = NinePatch.getInstance().getNormalInstance(NinePatch.BUTTON,width,height,backColor,false);
            NinePatch.tryDrawImage(g, enabled ? armed ? 
                  NinePatch.getInstance().getPressedInstance(npback, backColor, pressColor) : 
                  npback : NinePatch.getInstance().getNormalInstance(NinePatch.BUTTON,width,height,Color.interpolate(parent.backColor,backColor),false),ix,iy);
         }
         catch (ImageException ie) {ie.printStackTrace();}
      else
      if (!enabled)
      {
         if (img != null && imgDis == null) // guich@tc110_50 - guich@tc310: moved to here the generation of the disabled image
            try
            {
               imgDis = img.getFadedInstance();
            }
            catch (ImageException e)
            {
               imgDis = img;
            }
         g.drawImage(imgDis,ix,iy);
      }
      else g.drawImage(armed && pressedImage != null ? pressedImage : img,ix,iy);
   }

   /** Returns the image that is assigned to this Button, or null if none. */
   public Image getImage()
   {
      return img;
   }
}
