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

package totalcross.ui.dialog;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.ImageControl;
import totalcross.ui.Label;
import totalcross.ui.ScrollContainer;
import totalcross.ui.Spacer;
import totalcross.ui.UIColors;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.font.Font;
import totalcross.ui.font.FontMetrics;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.StringUtils;
import totalcross.util.UnitsConverter;

/** This class implements a scrollable message box window with customized buttons, delayed
 * unpop and scrolling text.
 * <br>for example, to create an automatic unpop after 5 seconds, do:
 * <pre>
 *   MessageBox mb = new MessageBox("TotalCross","TotalCross is the most exciting tool for developing totally cross-platform programs.",null);
 *   mb.setUnpopDelay(5000);
 *   mb.popup(mb);
 * </pre>
 */

public class MessageBox extends Window {
  private Image image;
  private String titleMsg;
  protected Label title[], msg;
  public Button[] btns;
  public int buttonForeColor = -1, messageForeColor = -1;
  private int selected = -1;
  private boolean hasScroll, updateFonts = true;
  protected int xa, ya, wa, ha; // arrow coords
  private TimerEvent unpopTimer, buttonTimer;
  private boolean oldHighlighting;
  private static String[] ok = { "Ok" };
  private int captionCount;
  private String originalText;
  private int labelAlign = LEFT;
  private String[] buttonCaptions;
  /**Gap used to separate the buttons when they are too big.*/
  private int insideGap;
  private Image icon;
  protected boolean usingOwnCont = false;
  protected Container baseContainer;
  private ScrollContainer sc;
  
  @Deprecated
  /** @deprecated use paddingLeft instead.*/
  protected int lgap;
  /**Message box fonts. You can change it the way you want to control the font sizes.*/
  public Font titleFont = Font.getFont(font.name, true, 20),
		      messageFont = Font.getFont(font.name, false, 16),
		      buttonsFont = Font.getFont(font.name, false, 16);
  /**The preferred width of this component*/
  private final int PREFERRED_WIDTH = UnitsConverter.toPixels(280 + DP);
  
  /**Check if the user didn't change those values manually*/
  private boolean changeMessageBoxInsets = true;
  
  /**The paddings of this component. You can change it the way you desire.*/
  public int paddingTop = UnitsConverter.toPixels(22 + DP), 
		  paddingBottom = UnitsConverter.toPixels(8 + DP), 
		  paddingLeft = UnitsConverter.toPixels(24 + DP), 
		  paddingRight = UnitsConverter.toPixels(24 + DP);
  
  /**This is the button margin.*/
  public int buttonMargin = UnitsConverter.toPixels(8 + DP);
  
  /**This is the gap that is between some components.*/
  public int titleContGap = UnitsConverter.toPixels(20 + DP),
		  contButtonGap = UnitsConverter.toPixels(28 + DP);
  
  /**
   * Set at the object creation. if true, all the buttons will have the same width, based on the width of the largest
   * one.<br>
   * Default value is false.
   * 
   * @since TotalCross 1.27
   */
  private boolean allSameWidth; //flsobral@tc126_50: set to make all buttons to always have the same width.

  /** Defines the y position on screen where this window opens. Can be changed to TOP or BOTTOM. Defaults to CENTER.
   * @see #CENTER
   * @see #TOP
   * @see #BOTTOM
   */
  public int yPosition = CENTER; // guich@tc110_7

  /** If you set the buttonCaptions array in the construction, you can also set this
   * public field to an int array of the keys that maps to each of the buttons.
   * For example, if you set the buttons to {"Ok","Cancel"}, you can map the enter key
   * for the Ok button and the escape key for the Cancel button by assigning:
   * <pre>
   * buttonKeys = new int[]{SpecialKeys.ENTER,SpecialKeys.ESCAPE};
   * </pre>
   * Note that ENTER is also handled as ACTION, since the ENTER key is mapped to ACTION under some platforms.
   * @since TotalCross 1.27
   */
  public int[] buttonKeys; // guich@tc126_40
  
  /**For internal use only.*/
  private MessageBox() {
	  super(null, ROUND_BORDER);
	  setRect(CENTER, CENTER, Math.min(PREFERRED_WIDTH, Settings.screenWidth - paddingLeft - paddingRight), PREFERRED);
  }
  
  /**
   * Constructs a message box with the text and one "Ok" button. The text may be separated by '\n' as the line
   * delimiters; otherwise, it is automatically splitted if its too big to fit on screen.
   */
  public MessageBox(String title, String msg) {
    this(null, title, msg, ok, false, 4, UnitsConverter.toPixels(12 + DP));
  }

  /**
   * Constructs a message box with the text and the specified button captions. The text may be separated by '\n' as the
   * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. if buttonCaptions is
   * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
   * setUnpopDelay method
   */
  public MessageBox(String title, String text, String[] buttonCaptions) {
    this(null, title, text, buttonCaptions, false, 4, UnitsConverter.toPixels(12 + DP));
  }
  
  public MessageBox(Image image, String title, String text, String[] buttonCaptions) {
	this(image, title, text, buttonCaptions, false, 4, UnitsConverter.toPixels(12 + DP));
  }

  /**
   * Constructs a message box with the text and the specified button captions. The text may be separated by '\n' as the
   * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
   * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
   * setUnpopDelay method. The parameters allSameWidth is the same as in the constructor for PushButtonGroup.
   * 
   * @since TotalCross 1.27
   */
  public MessageBox(String title, String text, String[] buttonCaptions, boolean allSameWidth) {
    this(null, title, text, buttonCaptions, allSameWidth, 4, UnitsConverter.toPixels(12 + DP));
  }
  
  public MessageBox(Image image, String title, String text, String[] buttonCaptions, boolean allSameWidth) {
	    this(image, title, text, buttonCaptions, allSameWidth, 4, UnitsConverter.toPixels(12 + DP));
  }

  /**
   * Constructs a message box with the text and the specified button captions. The text may be separated by '\n' as the
   * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
   * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
   * setUnpopDelay method. The new parameters gap and insideGap are the same as in the constructor for PushButtonGroup.
   * 
   * @since SuperWaba 4.11
   */
  public MessageBox(String title, String text, String[] buttonCaptions, int gap, int insideGap) {
    this(null, title, text, buttonCaptions, false, gap, insideGap);
  }
  
  public MessageBox(Image image, String title, String text, String[] buttonCaptions, int gap, int insideGap) {
	    this(image, title, text, buttonCaptions, false, gap, insideGap);
  }

  /**
   * Constructs a message box with the text and the specified button captions. The text may be separated by '\n' as the
   * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
   * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
   * setUnpopDelay method. The parameters allSameWidth, gap and insideGap are the same as in the constructor for PushButtonGroup.
   * 
   * @since TotalCross 1.27
   */
  public MessageBox(Image image, String title, String text, String[] buttonCaptions, boolean allSameWidth, int gap, int insideGap) // andrew@420_5
  {
    super(null, image == null ? ROUND_BORDER : NO_BORDER);
    setRect(CENTER, CENTER, Math.min(PREFERRED_WIDTH, Settings.screenWidth - paddingLeft - paddingRight), PREFERRED);
    
    this.image = image;
    titleMsg = title;
    if(titleMsg != null) {
    	if(!titleMsg.equals(""))
    		paddingTop = UnitsConverter.toPixels(20 + DP); 
	    this.title = new Label[2];
    }
    
    this.foreColor = UIColors.messageboxFore; // assign the default colors
    this.backColor = UIColors.messageboxBack;
    
    this.buttonCaptions = buttonCaptions;
    if(this.buttonCaptions != null) {
	    btns = new Button[buttonCaptions.length];
	    for (int i = 0; i < btns.length; i++) {
			btns[i] = new Button(buttonCaptions[i], BORDER_NONE);
			btns[i].transparentBackground = true;
	    }
    }
    
    this.insideGap = insideGap;
    this.allSameWidth = allSameWidth;
    if (!Settings.onJavaSE && Settings.vibrateMessageBox) {
      Vm.vibrate(200);
    }
    uiAdjustmentsBasedOnFontHeightIsSupported = false;
    fadeOtherWindows = Settings.fadeOtherWindows;
    ha = 6 * Settings.screenHeight / 160; // guich@450_24: increase arrow size if screen size change
    wa = ha * 2 + 1; // guich@570_52: now wa is computed from ha
    if (text == null) {
      text = "";
    }
    this.originalText = text; // guich@tc100: now we use \n instead of |
    baseContainer = new Container();
    sc = new ScrollContainer(false, true);
  }
  
  public static class Builder {
	  private MessageBox mb;
	  
	  /**The MessageBox builder. You can use it to make your MessageBox easily.*/
	  public Builder() {
		  	mb = new MessageBox();
		  	mb.insideGap = UnitsConverter.toPixels(4 + DP);
			mb.allSameWidth = false;
			if (!Settings.onJavaSE && Settings.vibrateMessageBox) {
			  Vm.vibrate(200);
			}
			mb.uiAdjustmentsBasedOnFontHeightIsSupported = false;
		    mb.fadeOtherWindows = Settings.fadeOtherWindows;
		    mb.ha = 6 * Settings.screenHeight / 160; // guich@450_24: increase arrow size if screen size change
		    mb.wa = mb.ha * 2 + 1; // guich@570_52: now wa is computed from ha
		    mb.baseContainer = new Container();
		    mb.sc = new ScrollContainer(false, true);
	  }
	  
	  /**After you make all of your process of build, call this to return the result.*/
	  public MessageBox build() {
		  return mb;
	  }
	  
	  /**Sets the MessageBox title.*/
	  public Builder setTitle(String title) {
		  mb.titleMsg = title;
		  if(mb.titleMsg != null) {
			  if(!mb.titleMsg.equals("") && mb.changeMessageBoxInsets)
				  mb.paddingTop = UnitsConverter.toPixels(20 + DP); 
			  mb.title = new Label[2];
		  }
		  return this;
	  }
	  
	  /**Sets the MessageBox message.
	   * <br>
	   * <b>Note: </b>This message is overwriten when you call setBaseContainer.
	   * @see setBaseContainer(Container baseContainer)*/
	  public Builder setMessage(String message) {
		  mb.originalText = message == null ? "" : message;
		  return this;
	  }
	  
	  /**This is the content that will be placed between the title and the buttons. You can put anything here.
	   * <br>
	   * <b>Note: </b>After you set this, the message will not appear since you're putting a Container above it.*/
	  public Builder setBaseContainer(Container baseContainer) {
	      mb.baseContainer = baseContainer;
	      mb.usingOwnCont = true;
		  return this;
	  }
	  
	  /**Sets the insets of the Container (The container that will be your content).
	   * @see setBaseContainer(Container baseContainer)*/
	  public Builder setBaseContainerInsets(int left, int right, int top, int bottom) {
		  mb.baseContainer.setInsets(left, right, top, bottom);
		  return this;
	  }
	  
	  /**Sets the image that is displayed on the top of the MessageBox.*/
	  public Builder setImage(Image image) {
		  mb.image = image;
		  return this;
	  }
	  
	  /**Sets the insets of the MessageBox.*/
	  public Builder setMessageBoxInsets(int left, int right, int top, int bottom) {
		  mb.changeMessageBoxInsets = false;
		  mb.paddingLeft = left;
		  mb.paddingRight = right;
		  mb.paddingTop = top;
		  mb.paddingBottom = bottom;
		  return this;
	  }
	  
	  /**Sets the buttons that will appear on the MessageBox.*/
	  public Builder setButtons(String[] buttonCaptions) {
		  	mb.buttonCaptions = buttonCaptions;
			if(mb.buttonCaptions != null) {
			    mb.btns = new Button[buttonCaptions.length];
			    for (int i = 0; i < mb.btns.length; i++) {
					mb.btns[i] = new Button(buttonCaptions[i], BORDER_NONE);
					mb.btns[i].transparentBackground = true;
			    }
			}
			return this;
	  }
	  
	  /**Sets the margins of the buttons.*/
	  public Builder setButtonsMargin(int margin) {
		  mb.buttonMargin = margin;
		  return this;
	  }
	  
	  /**Sets the gap between the title and the Container (MessageBox's content).
	   * @see setBaseContainer(Container baseContainer)*/
	  public Builder setTitleContGap(int gap) {
		  mb.titleContGap = gap;
		  return this;
	  }
	  
	  /**Sets the gap between the Container (MessageBox's content) and the buttons.
	   * @see setBaseContainer(Container baseContainer)*/
	  public Builder setContButtonGap(int gap) {
		  mb.contButtonGap = gap;
		  return this;
	  }
  }

  /** This method can be used to set the text AFTER the dialog was shown. However, the dialog will not be resized.
   * @since TotalCross 1.3
   */
  public void setText(String text) {
    msg.setText(text);
    msg.repaintNow();
  }
  
  public void setImage(Image image) {
	  this.image = image;
  }
  
  public Image getImage(Image image) {
	  return this.image;
  }

  @Override
  public void onPopup() {
    removeAll();
    int maxW = Math.min(PREFERRED_WIDTH, Settings.screenWidth - paddingLeft - paddingRight);
    int textWidth = maxW - paddingLeft - paddingRight;

    ImageControl imageControl = new ImageControl(image);
    imageControl.scaleToFit = true;
    imageControl.allowBeyondLimits = false;
    if(image != null) {
    	add(imageControl, LEFT, 0, FILL, PREFERRED);
    	imageControl.reposition();
    }
    
    
    if(uiMaterial && updateFonts && title != null) {
    	title[0] = new Label("");
    	title[1] = new Label("");
		title[0].setFont(titleFont);
		title[1].setFont(titleFont);
		String[] titles = breakText(titleMsg, titleFont.fm, textWidth);
		title[0].setText(titles[0]);
		title[1].setText(StringUtils.shortText(titles[1], titleFont.fm, textWidth));
	    updateFonts = false;
    }
    boolean hasTitle = false;
    if(title[0] != null) {
    	hasTitle = !title[0].getText().equals("");
    	if(uiMaterial && hasTitle) {
    		add(title[0], paddingLeft, paddingTop + (image == null ? 0 : AFTER), FILL - paddingRight, title[0].fm.height, image == null ? null : imageControl);
    		if(!title[1].getText().equals("")) {
    			int title1fmH = title[1].fm.height;
    			add(title[1], SAME, AFTER + title1fmH/5, FILL - paddingRight, title1fmH);
    		}
    	}
    }
    
    
    String text = originalText;
    msg = new Label(text == null ? "" : text, labelAlign);
    msg.autoSplit = true;
    msg.setFont(messageFont);
    int wb, hb;

    boolean multiRow = false;
    if (buttonCaptions == null) {
      wb = hb = 0;
    } else {
      captionCount = buttonCaptions.length;
      int buttonsWidth = buttonMargin;
      for (Button button : btns) {
		button.setFont(buttonsFont);
		buttonsWidth += button.getPreferredWidth();
      }
      buttonsWidth += buttonMargin;
      
      wb = btns[0].getPreferredWidth();
      multiRow = buttonsWidth > maxW - buttonMargin - buttonMargin;
      hb = btns[0].getPreferredHeight() + (multiRow ? insideGap * buttonCaptions.length : insideGap);
    }
    int wm = Math.min(msg.getPreferredWidth() + (uiAndroid ? fmH : 1), maxW);
    int hm = msg.getPreferredHeight();
    FontMetrics fm2 = titleFont.fm; // guich@220_28
    int iconH = icon == null ? 0 : icon.getHeight();
    int iconW = icon == null ? 0 : icon.getWidth();
    boolean removeTitleLine = uiAndroid && borderStyle == ROUND_BORDER && !hasTitle;
    if (removeTitleLine) {
      titleGap = 0;
    } else if (uiAndroid) {
      hm += fmH;
    }
    int captionH = (removeTitleLine ? 0 : Math.max(iconH, fm2.height) + titleGap) + 8;
    int ly = captionH - 6;
    if (captionH + hb + hm > Settings.screenHeight) // needs scroll?
    {
      if (hb == 0) {
        hb = ha;
      }
      hm = Math.max(fmH, Settings.screenHeight - captionH - hb - ha);
      hasScroll = true;
    } else if (removeTitleLine) {
      ly = androidBorderThickness + 1;
    }

    int w = lgap + Convert.max(wb, wm, (iconW > 0 ? iconW + fmH : 0) + fm2.stringWidth(hasTitle ? title[0].getText() : "")) + 7; // guich@200b4_29 - guich@tc100: +7 instead of +6, to fix 565_11
    w = Math.min(w, Settings.screenWidth); // guich@200b4_28: dont let the window be greater than the screen size
    
    if (!removeTitleLine && icon != null) {
      titleAlign = LEFT + fmH / 2 + iconW + fmH / 2;
      ImageControl ic = new ImageControl(icon);
      ic.transparentBackground = true;
      add(ic, LEFT + fmH / 2, (captionH - iconH) / 2 - titleFont.fm.descent);
    }
    if(!msg.getText().equals("") || usingOwnCont) {
	    add(baseContainer, LEFT + paddingLeft, image != null || hasTitle ? AFTER + titleContGap : TOP + paddingTop, FILL - paddingRight, WILL_RESIZE);
		if(!usingOwnCont)
			baseContainer.add(msg, LEFT, TOP, PARENTSIZE, PREFERRED);
		baseContainer.resizeHeight();
    }
    
    if (btns != null && btns[0] != null) {
      for (Button button : btns)
    	  add(button);

      int btnWidth = btns[multiRow ? 0 : btns.length-1].getPreferredWidth();
      int btnMaxW = maxW - buttonMargin*2;
	  if(uiMaterial) {
		  btns[multiRow ? 0 : btns.length-1].setRect(RIGHT - buttonMargin, AFTER + buttonMargin + contButtonGap, 
				  btnWidth > btnMaxW ? btnMaxW : PREFERRED, PREFERRED);
		  for (int i = multiRow ? 1 : btns.length-2; multiRow ? i < btns.length : i >= 0; i = multiRow ? i+1 : i-1) {
			  btnWidth = btns[i].getPreferredWidth();
			  btns[i].setRect(multiRow ? RIGHT - buttonMargin : BEFORE - buttonMargin, multiRow ? AFTER + buttonMargin : SAME, 
					  btnWidth > btnMaxW ? btnMaxW : PREFERRED, PREFERRED);
		  }
		  if(multiRow)
			  add(new Spacer(1, buttonMargin), CENTER, AFTER);
	  } else {
		  int end = btns.length;
		  if(multiRow) {
			  btns[0].setRect(RIGHT - buttonMargin, AFTER + buttonMargin + contButtonGap, 
					  btnWidth > btnMaxW ? btnMaxW : PREFERRED, PREFERRED);
			  for (int i = 1; i < btns.length; i = i+1) {
				  btnWidth = btns[i].getPreferredWidth();
				  btns[i].setRect(RIGHT - buttonMargin, AFTER + buttonMargin, 
						  btnWidth > btnMaxW ? btnMaxW : PREFERRED, PREFERRED);
			  }
			  add(new Spacer(1, buttonMargin), CENTER, AFTER);
		  } else {
			  int btnsWidth = 0;
			  for (Button btnss : btns) {
				  btnsWidth += btnss.getPreferredWidth();
			  }
			  int gap = (width - btnsWidth - buttonMargin * (btns.length - 1))/2;
			  for (int i = 0; i < end; i++) {
				  if (i == 0) {
					  btns[i].setRect(LEFT + gap ,AFTER + buttonMargin, PREFERRED, PREFERRED);
				  } else {
					  btns[i].setRect(AFTER + buttonMargin, SAME, PREFERRED, PREFERRED);
				  }
			  }
		  }
      }
	  
    }
    Rect r = sc != null ? sc.getRect() : msg.getRect();
    xa = r.x + r.width - (wa << 1);
    ya = btns != null ? (btns[0].getY() + (btns[0].getHeight() - ha) / 2) : (r.y2() + 3); // guich@570_52: vertically center the arrow buttons if the ok button is present
    msg.setBackForeColors(backColor, messageForeColor == -1 ? UIColors.messageboxMsgFore : messageForeColor);
    if (btns != null) {
      for (Button button : btns) {
    	  button.setForeColor(buttonForeColor == -1 ? UIColors.messageboxAction : buttonForeColor);
      }
      if (uiAndroid && !removeTitleLine) {
        footerH = height - (sc != null ? sc.getY2() + 2 : msg.getY2()) - 1;
      }
      if (buttonTimer != null) {
    	for (Button button : btns) {
			button.setVisible(false);
		}
      }
    }
    add(new Spacer(1, paddingBottom), CENTER, AFTER);
    
    resizeHeight();
  }
  
  /**Return the MessageBox buttons, so you can add events to it.*/
  public Button getButton(int index) {
	  if(btns != null && index >= 0 && index < btns.length)
		  return btns[index];
	  return null;
  }

  @Override
  public void reposition() {
    onPopup();
  }

  /** Set an icon to be shown in the MessageBox's title, at left. 
   * It only works if there's a title. If you really need an empty title, pass as title a 
   * String with a couple of spaces, like " ".
   * 
   * The icon's width and height will be set to title's font ascent.
   * @since TotalCross 1.3
   */
  public void setIcon(Image icon) throws ImageException {
    this.icon = icon.getSmoothScaledInstance(titleFont.fm.ascent, titleFont.fm.ascent);
  }

  /** Sets the alignment for the text. Must be CENTER (default), LEFT or RIGHT */
  public void setTextAlignment(int align) {
    labelAlign = align; // guich@241_4
  }

  /** sets a delay for the unpop of this dialog */
  public void setUnpopDelay(int unpopDelay) {
    if (unpopDelay <= 0) {
      throw new IllegalArgumentException("Argument 'unpopDelay' must have a positive value");
    }
    if (unpopTimer != null) {
      removeTimer(unpopTimer);
    }
    unpopTimer = addTimer(unpopDelay);
  }

  @Override
  public void onPaint(Graphics g) {
    if (hasScroll) {
      g.drawArrow(xa, ya, ha, Graphics.ARROW_UP, false,
          msg.canScroll(false) ? foreColor : Color.getCursorColor(foreColor)); // guich@200b4_143: msg.canScroll
      g.drawArrow(xa + wa, ya, ha, Graphics.ARROW_DOWN, false,
          msg.canScroll(true) ? foreColor : Color.getCursorColor(foreColor));
    }
  }

  /** handle scroll buttons and normal buttons */
  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case TimerEvent.TRIGGERED:
      if (buttonTimer != null && buttonTimer.triggered) {
        removeTimer(buttonTimer);
        if (btns != null) {
          for (Button button : btns)
			if(button != null)
				button.setVisible(true);
        }
      } else if (e.target == this) {
        removeTimer(unpopTimer);
        if (popped) {
          unpop();
        }
      }
      break;
    case PenEvent.PEN_DOWN:
      if (hasScroll) {
        int px = ((PenEvent) e).x;
        int py = ((PenEvent) e).y;

        if (ya <= py && py <= ya + ha && xa <= px && px < xa + (wa << 1) && msg.scroll((px - xa) / wa != 0)) {
          Window.needsPaint = true;
        } else if (msg.isInsideOrNear(px, py) && msg.scroll(py > msg.getHeight() / 2)) {
          Window.needsPaint = true;
        }
      }
      break;
    case KeyEvent.SPECIAL_KEY_PRESS: // guich@200b4_42
      KeyEvent ke = (KeyEvent) e;
      if (ke.isUpKey()) // guich@330_45
      {
        msg.scroll(false);
        Window.needsPaint = true; // guich@300_16: update the arrow's state
      } else if (ke.isDownKey()) // guich@330_45
      {
        msg.scroll(true);
        Window.needsPaint = true; // guich@300_16: update the arrow's state
      } else if (!Settings.keyboardFocusTraversable && captionCount == 1 && ke.isActionKey()) // there's a single button and the enter key was pressed?
      {
        selected = 0;
        unpop();
      } else if (buttonKeys != null && captionCount > 0) {
        int k = ke.key;
        for (int i = buttonKeys.length; --i >= 0;) {
          if (buttonKeys[i] == k || (buttonKeys[i] == SpecialKeys.ENTER && k == SpecialKeys.ACTION)) // handle ENTER as ACTION too
          {
            selected = i;
            unpop();
            break;
          }
        }
      }
      break;
    case ControlEvent.PRESSED:
      selected = -1;
      for (int i = 0; i < btns.length; i++) {
		Button button = btns[i];
		if (e.target == button) {
            selected = i;
            unpop();
            break;
        }
	  }
      break;
    }
  }

  /** Returns the pressed button index, starting from 0 */
  public int getPressedButtonIndex() {
    return selected;
  }

  @Override
  protected void postPopup() {
    if (Settings.keyboardFocusTraversable) // guich@570_39: use this instead of pen less
    {
      if (btns != null) // guich@572_
      {
        if(btns[0] != null) {
	        btns[0].requestFocus(); // without a pen, select the first button
	        selected = 0;
        }
      }
      oldHighlighting = isHighlighting;
      isHighlighting = false; // allow a direct click to dismiss this dialog
    }
  }

  @Override
  protected void postUnpop() {
    if (Settings.keyboardFocusTraversable) {
      isHighlighting = oldHighlighting;
    }
    postPressedEvent(); // guich@580_27
  }

  /** Title shown in the showException dialog. */
  public static String showExceptionTitle = "Exception Thrown"; // guich@tc113_8

  /** Shows the exception, with its name, message and stack trace in a new MessageBox.
   * @since TotalCross 1.0
   */
  public static void showException(Throwable t, boolean dumpToConsole) {
    String exmsg = t.getMessage();
    exmsg = exmsg == null ? "" : "Message: " + t.getMessage() + "\n";
    String msg = "Exception: " + t.getClass() + "\n" + exmsg + "Stack trace:\n" + Vm.getStackTrace(t);
    if (dumpToConsole) {
      Vm.debug(msg);
    }
    MessageBox mb = new MessageBox(showExceptionTitle, "");
    mb.originalText = Convert.insertLineBreak(Settings.screenWidth - mb.fmH, mb.font.fm, msg);
    mb.labelAlign = LEFT;
    mb.popup();
  }

  @Override
  protected void onFontChanged() {
	  updateFonts = true;
  }

  /** Calling this method will make the buttons initially hidden and will show them after
   * the specified number of milisseconds.
   * 
   * Here's a sample:
   * <pre>
   * MessageBox mb = new MessageBox("Novo Tweet!",tweet);
   * mb.setDelayToShowButton(7000);
   * mb.popup();
   * </pre>
   * @since TotalCross 1.53
   */
  public void setDelayToShowButton(int ms) {
    buttonTimer = addTimer(ms);
  }
  
  /**This method splits the text into two labels when it's too long.*/
  private String[] breakText(String text, FontMetrics fm, int width) {
	  int textSize = fm.stringWidth(text);

	  String labels[] = {"", ""};
	  
	  int index = 0;
	  if(textSize > width) {
		  int size = 0;
		  for (; index < text.length(); index++) {
			char letter = text.charAt(index);
			size += fm.charWidth(letter);
			if(size <= width)
				labels[0] += letter;
			else
				break;
		  }
	  } else {
		  labels[0] = text;
		  return labels;
	  }
	  
	  if(index < text.length()) {
		  text = text.substring(index, text.length());
		  labels[1] = StringUtils.shortText(text, font.fm, width);
	  }
	  labels[1] = StringUtils.shortText(labels[1], font.fm, Math.min(PREFERRED_WIDTH - paddingLeft - paddingRight, Settings.screenWidth - paddingLeft - paddingRight));
	  return labels;
  }
}