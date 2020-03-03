package totalcross.ui;

import totalcross.res.Resources;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.FadeAnimation;
import totalcross.ui.event.PressListener;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.util.StringUtils;
import totalcross.util.UnitsConverter;

/** Class used to show a message on screen.
 */
public class Toast {
  /** Back color used in the toast. */
  public static int backColor = 0x303030;
  /** Fore color used in the toast. */
  public static int foreColor = Color.WHITE;
  /** X position of the toast. Defaults to CENTER. */
  public static int posX = Control.CENTER;
  /** Y position of the toast. Defaults to BOTTOM - fmH. */
  public static int posY = Control.BOTTOM - Font.NORMAL_SIZE;
  /** Extra width of the toast in font's height multiples. Defaults to 2. */
  public static int extraW = 2;
  /** Width of the toast. Defaults to PREFERRED. */
  public static int width = Control.PREFERRED;
  /** Height of the toast. Defaults to PREFERRED. */
  public static int height = Control.PREFERRED;
  /** The stopping fade value, used on transparency. Defaults to 200. */
  public static int fade = 200;
  /** The font to be used. Defaults to the default bold font. */
  public static Font font = MainWindow.getDefaultFont().asBold();
  /**This button will appear on the right of this component.*/
  private static Button action;
  
  private static boolean canShow = true;
  
  /**Padding of the component.*/
  public static int paddingLeft = UnitsConverter.toPixels(16 + Control.DP), 
		     paddingRight = UnitsConverter.toPixels(16 + Control.DP),
		     paddingTop = 0, paddingBottom = 0; 
  /**Gap between the text and the button, horizontally*/
  public static int buttonHGap = UnitsConverter.toPixels(8 + Control.DP);
  /**Gap between the text and the button, vertically. Used when the button text is too big.*/
  public static int buttonVGap = UnitsConverter.toPixels(18 + Control.DP);
  private static final int MAX_HEIGHT = UnitsConverter.toPixels(344 + Control.DP);

  /** The toast component used to show the text. */
  public static Container cnt;
  private static ControlAnimation animation;

  public static final int INFINITE = Convert.MAX_INT_VALUE;
  public static final int INFINITE_NOANIM = Convert.MIN_INT_VALUE;;
  
  /** Shows a toast message using the given parameters on the main window.
   * @see #show(String, int, Window)
   */
  public static void show(String message, int delay) {
    show(message, delay, MainWindow.getMainWindow());
  }
  
  /** Shows a toast message with a button using the given parameters on the given window.*/
  public static void show(final String message, final int delay, final Window parentWindow, final String buttonCaption, final int buttonForeColor, PressListener btnPressListener) {
	  show(message, delay, parentWindow);
	  addActionButton(buttonCaption, buttonForeColor, btnPressListener);
  }
  
  /** Shows a toast message with a button using the given parameters on the main window.
   * @see #show(String, int, Window, String, PressListener)
   */
  public static void show(final String message, final int delay, final String buttonCaption, final int buttonForeColor, PressListener btnPressListener) {
	  show(message, delay, MainWindow.getMainWindow());
	  addActionButton(buttonCaption, buttonForeColor, btnPressListener);
  }
  
  
  /** Shows a toast message using the given parameters on the given window.
   * Sample:
   * <pre>
   * String message = "This is a test";
   * Toast.show("\n"+message+"\n", 2000);
   * </pre>
   * See the public static fields of this class to show how you can customize the appearance.
   * Calling with a nulled message will make the last toast disappear.
   * 
   * The text is splitted if wider than the screen.
   * 
   * If delay is INFINITE, it will wait forever until you call stopShow().
   * If delay is INFINITE_NOANIM, it will wait forever until you call stopShow() and will not use animation.
   */
  public static void show(final String message, final int delay, final Window parentWindow) {
	  if(canShow || (message == null && delay == 0)) {
		canShow = false;
		if(message == null && delay == 0) {
			action = null;
			canShow = true;
		} else if(animation != null)
			animation.stop(true);
	    MainWindow.getMainWindow().runOnMainThread(new Runnable() {
	      @Override
	      public void run() {
	        final Window parent = parentWindow;
	        if (cnt != null && cnt.parent != null) {
	          cnt.parent.remove(cnt);
	          cnt = null;
	          if (animation != null) {
	            animation.stop(true);
	            canShow = true;
	          }
	          animation = null;
	        }
	        if (message != null) {
	          String msg = message;
	          boolean hasButton = action != null;

	          cnt = new Container();
	          cnt.setBackForeColors(backColor, foreColor);
	          cnt.setFont(font);
	          cnt.setNinePatch(Resources.listbox, 14, 14);
	          cnt.setRect(posX, posY,
	        		  Math.min(Settings.screenWidth - 5, MAX_HEIGHT), 48 + Control.DP);
	          
	          Label labels[] = new Label[2];
	          String text[];
	          
	          if(hasButton) {
	        	  int actionWidth = action.getPreferredWidth();
	        	  boolean bigButton = actionWidth > (int)(cnt.getWidth() * 0.3);
	        	  
	        	  int textWidth;
	        	  if(bigButton) {
	        		  paddingTop = paddingLeft = UnitsConverter.toPixels(16 + Control.DP);
		        	  paddingRight = paddingBottom = UnitsConverter.toPixels(8 + Control.DP);
		        	  textWidth = cnt.getWidth() - paddingLeft - paddingRight;
	        	  } else {
		        	  paddingTop = paddingBottom = UnitsConverter.toPixels(6 + Control.DP);
		        	  paddingRight = UnitsConverter.toPixels(8 + Control.DP);
		        	  textWidth = cnt.getWidth() - paddingLeft - paddingRight - actionWidth - buttonHGap;
	        	  }

	        	  text = manageText(msg, textWidth);
	        	  labels[0] = new Label(text[0]);
	        	  labels[1] = new Label(text[1]);
	        	  boolean twoLines = !labels[1].getText().equals("");
	        	  if(twoLines && !bigButton) {
	        		  paddingTop = paddingBottom = paddingLeft = UnitsConverter.toPixels(16 + Control.DP);
	        		  paddingRight = UnitsConverter.toPixels(8 + Control.DP);
	    	          cnt.setRect(posX, posY - (int) (Font.getDefaultFontSize()*2*Settings.screenDensity),
	    	        		  Math.min(Settings.screenWidth - 5, MAX_HEIGHT), 48 + Control.DP);

	        	  }
	        	  cnt.add(labels[0], paddingLeft, twoLines || bigButton ? paddingTop : Control.CENTER, textWidth, cnt.fmH);
	        	  if(twoLines)
	        		  cnt.add(labels[1], paddingLeft, Control.AFTER + cnt.fmH/2, textWidth, cnt.fmH, labels[0]);
	        	  
	        	  if(bigButton)
	        		  cnt.add(action, Control.RIGHT - paddingRight, Control.AFTER + buttonVGap, twoLines ? labels[1] : labels[0]);
	        	  else
	        		  cnt.add(action, Control.RIGHT - paddingRight, Control.CENTER, twoLines ? labels[1] : labels[0]);
	        	  
	        	  Spacer padding = new Spacer();
	        	  cnt.add(padding, 0, Control.AFTER, 1, paddingBottom, bigButton ? action : twoLines ? labels[1] : labels[0]);
	        	  cnt.resizeHeight();
	        	  parent.reposition();
	          } else {
	        	  int textWidth = cnt.getWidth() - paddingLeft - paddingRight;
	        	  
	        	  text = manageText(msg, textWidth);
	        	  labels[0] = new Label(text[0]);
	        	  labels[1] = new Label(text[1]);
	        	  boolean twoLines = !labels[1].getText().equals("");
	        	  if(twoLines) {
	        		  paddingTop = paddingBottom = paddingLeft = UnitsConverter.toPixels(16 + Control.DP);
	        		  paddingRight = UnitsConverter.toPixels(8 + Control.DP);
	    	          cnt.setRect(posX, posY - (int) (Font.getDefaultFontSize()*2*Settings.screenDensity),
	    	        		  Math.min(Settings.screenWidth - 5, MAX_HEIGHT), 48 + Control.DP);
	        	  }
	        	  cnt.add(labels[0], paddingLeft, twoLines ? paddingTop : Control.CENTER, textWidth, cnt.fmH);
	        	  if(twoLines) {
	        		  cnt.add(labels[1], paddingLeft, Control.AFTER + cnt.fmH/2, textWidth, cnt.fmH, labels[0]);
	        		  Spacer padding = new Spacer();
	        		  cnt.add(padding, 0, Control.AFTER, 1, paddingBottom);
	        		  cnt.resizeHeight();
	        		  parent.reposition();
	        	  }
	          }
	          
	          parent.add(cnt);

	          if (delay != INFINITE_NOANIM) {
	            FadeAnimation.maxFade = fade;
	            animation = FadeAnimation.create(cnt, true, null, -1);
	            animation.delayAfterFinish = delay;
	            final Container thisCnt = cnt;
	            animation.then(FadeAnimation.create(cnt, false, new ControlAnimation.AnimationFinished() {
	              @Override
	              public void onAnimationFinished(ControlAnimation anim) {
	                if (cnt != null && cnt.parent != null) {
	                	cnt.parent.remove(cnt);
	                }
	                if (thisCnt == cnt) // button may change if user tries to show several Toasts
	                {
	                  cnt = null;
	                  action = null;
	                  animation = null;
	                  canShow = true;
	                }
	              }
	            }, -1)).start();
	            FadeAnimation.maxFade = FadeAnimation.DEFAULT_MAX_FADE;
	          }
	        } else
	        	canShow = true;
	      }
	    }, true);
	  }
  }
  
  /**Unpop the Toast from the screen. If you don't want 
   * the animation of fading out, please call stopShow(false) instead.*/
  public static void stopShow() {
	  stopShow(true);
  }
  
  /**Unpop the Toast from the screen.
   * @param animate Animate the Toast with fading out.*/
  public static void stopShow(boolean animate) {
	  if(animate && animation != null)
		  animation.stop(false);
	  else
		  show(null, 0);
  }
  
  private static void addActionButton(String text, int buttonForeColor, PressListener pressListener) {
	  action = new Button(text);
	  action.transparentBackground = true;
	  action.setBorder(Button.BORDER_NONE);
	  action.setForeColor(buttonForeColor);
	  if(pressListener != null)
		  action.addPressListener(pressListener);
  }
  
  /**This method manages the Text size and the width allowed to have*/
  private static String[] manageText(String text, int width) {
	  String[] userInputedBreakedLineArray = Convert.tokenizeString(text, '\n');
	  int textSize = cnt.fm.stringWidth(text);
	  String labels[] = {"", ""};
	  int index = 0;
	  
	  if(userInputedBreakedLineArray.length == 2) {
		  labels = userInputedBreakedLineArray;
	  } else if(userInputedBreakedLineArray.length > 1){
		  labels[0] = userInputedBreakedLineArray[0];
		  labels[1] = userInputedBreakedLineArray[1];
	  } else if (textSize > width) {
		  int size = 0;
		  int textLength = text.length();
		  StringBuffer buffer = new StringBuffer(textLength);
		  for (; index < textLength; index++) {
			char letter = text.charAt(index);
			size += cnt.fm.charWidth(letter);
			if(size > width) {
				break;
			}
			buffer.append(letter);
		  }
	  } else {
		  labels[0] = text;
		  return labels;
	  }
	  if(labels[0] == userInputedBreakedLineArray[0]) {
		labels[1] = StringUtils.shortText(labels[1], font.fm, width);
	  } else if(index < text.length()) {
		  text = text.substring(index, text.length());
		  labels[1] = StringUtils.shortText(text, font.fm, width);
	  }
	  return labels;
  }
}
