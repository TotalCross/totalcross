// Copyright (C) 2000-2001 Allan C. Solomon 
// Copyright (C) 2001-2013 SuperWaba Ltda. 
// Copyright (C) 2013-2020 TotalCross Global Mobile Platform Ltda.
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
import totalcross.sys.Time;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Radio;
import totalcross.ui.RadioGroupController;
import totalcross.ui.Spacer;
import totalcross.ui.UIColors;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.util.Vector;

/**
 * Class used to input a time from the user. Correctly handles the AM/PM
 * depending on Settings.is24Hour. <br>
 * <br>
 * When the window closes, a PRESSED event is sent to the caller, and the time
 * can be retrieved using getTime. Here's a sample:
 * 
 * <pre>
 * TimeBox tb;
 * 
 * public void initUI() {
 * 	try {
 * 		(tb = new TimeBox()).popupNonBlocking();
 * 	} catch (Exception e) {
 * 		e.printStackTrace();
 * 	}
 * }
 * 
 * public void onEvent(Event e) {
 * 	if (e.type == ControlEvent.PRESSED && e.target == tb)
 * 		Vm.debug("ret: " + tb.getTime());
 * }
 * </pre>
 * 
 * The time can be entered also using the arrow keys and by typing the numbers
 * directly.
 * 
 * @see #getTime()
 * @since TotalCross 1.22
 */

public class TimeBox extends Window {
	protected Button btOK, btClear, btBack, btForw;
	protected Radio btAM, btPM;
	protected Visor visor;
	private RadioGroupController rg;
	private int pos;
	private Time sentTime;
	private char[] chars;
	private Button[] btNumbers = new Button[10];
	
	/** Used in the button. Change it if you want to localize the text. */
	public static String okCaption = "OK";
	/** Used in the button. Change it if you want to localize the text. */
	public static String clearCaption = "CLEAR";

	/**
	 * Set to false to disable the buttons instead of hiding them if they are not a
	 * valid number to appear at the current position. By default, the buttons are
	 * hidden, but setting this to false can disable them instead.
	 */
	public static boolean hideIfInvalid = true;

	/** Constructs a TimeBox with time set to midnight. */
	public TimeBox() {
		this(new Time(0, 0, 0, 0, 0, 0, 0));
	}

	/**
	 * Constructs a TimeBox with the given time. If the time is invalid, it is set
	 * to midnight.
	 */
	public TimeBox(Time time) {
		setTime(time);
		uiAdjustmentsBasedOnFontHeightIsSupported = false;
		fadeOtherWindows = Settings.fadeOtherWindows;
		setBackColor(UIColors.timeboxBack);
		setForeColor(UIColors.timeboxFore);
		
		
	}

	/** Set the time, if it was not yet set in the constructor. */
	public void setTime(Time time) {
		this.sentTime = time;
		if (!time.isValid()) {
			time.hour = time.minute = time.second = 0;
		}
	}

	@Override
	public void onFontChanged() {
		visor.setFont(Font.getFont(this.font.name, this.font.style == 1, this.font.size + 2));
	}

	private class Visor extends Container {
		private int w0, wp;

		public Visor() {
			setBorderStyle(uiVista ? BORDER_SIMPLE : BORDER_LOWERED);
			setBackColor(TimeBox.uiMaterial
					? (UIColors.timeboxVisorBack == -1 ? Color.getRGB("9fffd7") : UIColors.timeboxVisorBack)
					: (UIColors.timeboxVisorBack == -1 ? Color.WHITE : UIColors.timeboxVisorBack));
			setForeColor(UIColors.timeboxVisorFore);
		}

		@Override
		public void onFontChanged() {
			w0 = fm.charWidth('0');
			wp = (w0 - fm.charWidth(':')) / 2;
		}

		@Override
		public void onPaint(Graphics g) {
			super.onPaint(g);

			setFont(Font.getFont(false, (int) (this.height * 0.55)));
			if (getFont().size > 42)
				setFont(Font.getFont(42));

			int xx = (this.width - w0 * 8) / 2;
			int yy = (this.height - fmH) / 2;
			g.backColor = UIColors.timeboxVisorCursor == -1 ? Color.darker(backColor) : UIColors.timeboxVisorCursor;
			g.fillCircle(pos * w0 + xx - 2 + w0 / 2, ((this.height + fmH) / 2), this.height / 26);
			g.foreColor = getForeColor();
			int sh = TimeBox.this.textShadowColor;
			for (int i = 0; i < chars.length; i++, xx += w0) {
				g.drawText(chars, i, 1, i == 2 || i == 5 ? xx + wp : xx, yy, sh != -1, sh);
			}
		}

		@Override
		public int getPreferredHeight() {
			return fmH + 4;
		}
	}

	@Override
	protected void onPopup() {
		pos = 0;
		removeAll();
		setRect(CENTER, CENTER, SCREENSIZEMIN + 60,
				Settings.isLandscape() ? (Settings.is24Hour ? SCREENSIZE + 85 : SCREENSIZE + 90)
						: (Settings.is24Hour ? SCREENSIZE + 60 : SCREENSIZE + 65));
		visor = new Visor();
		visor.setBorderStyle(BORDER_NONE);

		add(visor, LEFT, TOP, FILL, PARENTSIZE + 20);
		onFontChanged();

		Button.commonGap = Settings.fingerTouch && fmH > 15 ? fmH * 2 / 3 : fmH / 2;
		btNumbers[1] = new Button("1", Button.BORDER_NONE);

		int gapW = (int) (this.getWidth() * 0.06);
		int gapH = (int) (this.height * 0.03);

		int btnSize = Settings.is24Hour ? (int) (this.height * 0.14) : (int) (this.height * 0.12);

		setFont(Font.getFont(false, (int) (btnSize * 0.48)));
		if (getFont().size > 27)
			setFont(Font.getFont(27));

		Font numberFont = getFont();

		add(btNumbers[1], LEFT + gapW, AFTER + gapH, PARENTSIZE + 26, btnSize);
		add(btNumbers[2] = new Button("2", Button.BORDER_NONE), AFTER + gapW, SAME, SAME, SAME);
		add(btNumbers[3] = new Button("3", Button.BORDER_NONE), AFTER + gapW, SAME, SAME, SAME);

		add(btNumbers[4] = new Button("4", Button.BORDER_NONE), LEFT + gapW, AFTER + gapH, SAME, SAME);
		add(btNumbers[5] = new Button("5", Button.BORDER_NONE), AFTER + gapW, SAME, SAME, SAME);
		add(btNumbers[6] = new Button("6", Button.BORDER_NONE), AFTER + gapW, SAME, SAME, SAME);

		add(btNumbers[7] = new Button("7", Button.BORDER_NONE), LEFT + gapW, AFTER + gapH, SAME, SAME);
		add(btNumbers[8] = new Button("8", Button.BORDER_NONE), AFTER + gapW, SAME, SAME, SAME);
		add(btNumbers[9] = new Button("9", Button.BORDER_NONE), AFTER + gapW, SAME, SAME, SAME);
		
		add(btBack = new Button("<", Button.BORDER_NONE), LEFT + gapW, AFTER + gapH, SAME, SAME);
		add(btNumbers[0] = new Button("0", Button.BORDER_NONE), AFTER + gapW, SAME, SAME, SAME);
		add(btForw = new Button(">", Button.BORDER_NONE), AFTER + gapW, SAME, SAME, SAME);
		
		for (Button button : btNumbers) {
			button.transparentBackground = true;
		}
		btBack.transparentBackground = true;
		btForw.transparentBackground = true;
		
		// trick: store in appId the button's value (+10)
		btNumbers[0].appId = 10;
		btNumbers[1].appId = 11;
		btNumbers[2].appId = 12;
		btNumbers[3].appId = 13;
		btNumbers[4].appId = 14;
		btNumbers[5].appId = 15;
		btNumbers[6].appId = 16;
		btNumbers[7].appId = 17;
		btNumbers[8].appId = 18;
		btNumbers[9].appId = 19;

		if (!Settings.is24Hour) {
			setFont(font.percentBy(80, false));
			Spacer l;
			add(l = new Spacer(), CENTER, AFTER, 1, PARENTSIZE + 5);
			rg = new RadioGroupController();
			add(btAM = new Radio("AM", rg), BEFORE - gapW, SAME);
			add(btPM = new Radio("PM", rg), AFTER + gapW, SAME, l);
			add(new Spacer(1, fmH / 2), CENTER, AFTER + 2);
			btAM.leftJustify = btPM.leftJustify = true;
			btAM.clearValueInt = 1;
		}

		Spacer l;
		add(l = new Spacer(), CENTER, AFTER);
		setFont(numberFont);
		setFont(Font.getFont(true, font.percentBy(90).size));
		add(btOK = new Button(okCaption, Button.BORDER_NONE), LEFT + gapW, SAME, FIT - gapW, PARENTSIZE + 9);
		add(btClear = new Button(clearCaption, Button.BORDER_NONE), AFTER + gapW, SAME, FILL - gapW, PARENTSIZE + 9, l);
		
		btOK.transparentBackground = true;
		btClear.transparentBackground = true;
		
		btOK.setForeColor(
				uiMaterial ? (UIColors.timeboxOk == -1 ? Color.darker(visor.getBackColor()) : UIColors.timeboxOk)
						: (UIColors.timeboxOk == -1 ? Color.BLACK : UIColors.timeboxOk));
		btClear.setForeColor(UIColors.timeboxClear);

		Button.commonGap = 0;

		if (Settings.is24Hour) {
			tabOrder = new Vector(new Control[] { btNumbers[1], btNumbers[2], btNumbers[3], btNumbers[4], btNumbers[5],
					btNumbers[6], btNumbers[7], btNumbers[8], btNumbers[9], btNumbers[0], btOK, btClear });
		} else {
			rg.setSelectedIndex(sentTime.hour <= 11 ? 0 : 1);
			if (sentTime.hour >= 12) {
				sentTime.hour -= 12;
			}
			tabOrder = new Vector(new Control[] { btNumbers[1], btNumbers[2], btNumbers[3], btNumbers[4], btNumbers[5],
					btNumbers[6], btNumbers[7], btNumbers[8], btNumbers[9], btNumbers[0], btAM, btPM, btOK, btClear });
		}

		String s = Convert.zeroPad(sentTime.hour, 2) + ":" + Convert.zeroPad(sentTime.minute, 2) + ":"
				+ Convert.zeroPad(sentTime.second, 2);
		chars = s.toCharArray();
		enableButtons();

		setInsets(0, 0, 0, 5 + DP);
	}

	/**
	 * Returns the time placed in this control, which is the time passed in the
	 * constructor (if any) updated with the hours, minutes and seconds.
	 */
	public Time getTime() {
		if (chars != null) {
			sentTime.hour = (chars[0] - '0') * 10 + (chars[1] - '0');
			sentTime.minute = (chars[3] - '0') * 10 + (chars[4] - '0');
			sentTime.second = (chars[6] - '0') * 10 + (chars[7] - '0');

			if (!Settings.is24Hour && rg.getSelectedIndex() == 1) {
				sentTime.hour += 12;
			}
			return sentTime;
		} else
			return null;
	}

	@Override
	public void reposition() {
		Time tb = getTime();
		int pb = pos;
		onPopup();
		if (tb != null)
			setTime(tb);
		pos = pb;
	}
	
	/**Changing this method will also change the UIColors.timeboxBack attribute*/
	@Override
	public void setBackColor(int c) {
		UIColors.timeboxBack = c;
		super.setBackColor(c);
	}
	
	/**Changing this method will also change the UIColors.timeboxFore attribute*/
	@Override
	public void setForeColor(int c) {
		UIColors.timeboxFore = c;
		super.setForeColor(c);
	}
	
	/**Changing this method will also change the UIColors.timeboxBack and UIColors.timeboxFore attributes*/
	@Override
	public void setBackForeColors(int back, int fore) {
		UIColors.timeboxBack = back;
		UIColors.timeboxFore = fore;
		super.setBackForeColors(back, fore);
	}

	@Override
	public void onEvent(Event event) {
		switch (event.type) {
		case KeyEvent.KEY_PRESS: {
			KeyEvent ke = (KeyEvent) event;
			if ('0' <= ke.key && ke.key <= '9') {
				if (isShown((char) ke.key)) {
					onKey((char) ke.key);
				}
			}
			break;
		}
		case KeyEvent.SPECIAL_KEY_PRESS: {
			KeyEvent ke = (KeyEvent) event;
			if (ke.isUpKey()) {
				chars[pos]++;
				if (chars[pos] > '9' || !isShown(chars[pos])) {
					chars[pos] = '0';
				}
				Window.needsPaint = true;
			} else if (ke.isDownKey()) {
				chars[pos]--;
				if (chars[pos] < '0') {
					chars[pos] = (char) ('9' + 1);
					do {
						chars[pos]--;
					} while (!isShown(chars[pos]));
				}
				Window.needsPaint = true;
			} else if (ke.isNextKey()) {
				if (++pos == 8) {
					pos = 0;
				}
				if (pos == 2 || pos == 5) {
					pos++;
				}
				enableButtons();
			} else if (ke.isPrevKey()) {
				if (--pos == -1) {
					pos = 7;
				}
				if (pos == 2 || pos == 5) {
					pos--;
				}
				enableButtons();
			} else if (ke.isActionKey()) {
				doClose();
			} else if (ke.key == SpecialKeys.ESCAPE) {
				doClear();
			}
			break;
		}
		case ControlEvent.PRESSED: {
			Control c = (Control) event.target;
			if (c.appId > 0) {
				onKey((char) (c.appId - 10 + '0'));
			} else if (event.target == btOK) {
				doClose();
			} else if (event.target == btClear) {
				doClear();
			} else if (event.target == btBack){
				if (--pos == -1) {
					pos = 7;
				}
				if (pos == 2 || pos == 5) {
					pos--;
				}
				enableButtons();
			} else if (event.target == btForw) {
				if (++pos == 8) {
					pos = 0;
				}
				if (pos == 2 || pos == 5) {
					pos++;
				}
				enableButtons();
			}
			break;
		}
		}
	}

	private void doClear() {
		clear();
		chars[0] = chars[1] = chars[3] = chars[4] = chars[6] = chars[7] = '0';
		pos = 0;
		enableButtons();
	}

	private void doClose() {
		for (pos = 0; pos < 8; pos++) // check all positions before closing
		{
			if (pos == 2 || pos == 5) {
				pos++;
			}
			enableButtons();
		}
		unpop();
	}

	private void onKey(char key) {
		chars[pos++] = key;
		if (pos == 2 || pos == 5) {
			pos++; // skip the :
		}
		if (pos == 8) {
			pos = 0;
		}
		enableButtons();
	}

	@Override
	protected void postUnpop() {
		postPressedEvent();
	}

	private final static int H1 = 0;
	private final static int H2 = 1;
	private final static int M2 = 4;
	private final static int S2 = 7;

	private void enableButtons() {
		// 01234567
		// 00:00:00
		boolean aboveH1 = pos > H1;
		boolean aboveH2 = pos > H2;
		boolean on2 = pos == H2 || pos == M2 || pos == S2;
		if (Settings.is24Hour) {
			boolean fourOrMore = aboveH2 || (aboveH1 && chars[0] <= '1');
			show(3, aboveH1);
			show(4, fourOrMore);
			show(5, fourOrMore);
			show(6, on2 && fourOrMore);
			show(7, on2 && fourOrMore);
			show(8, on2 && fourOrMore);
			show(9, on2 && fourOrMore);
		} else {
			boolean ok2_5 = aboveH2 || (aboveH1 && chars[0] == '0');
			show(2, ok2_5);
			show(3, ok2_5);
			show(4, ok2_5);
			show(5, ok2_5);
			show(6, ok2_5 && on2);
			show(7, ok2_5 && on2);
			show(8, ok2_5 && on2);
			show(9, ok2_5 && on2);
		}
		if (!isShown(chars[pos])) {
			chars[pos] = '0';
		}
		Window.needsPaint = true;
	}

	private void show(int idx, boolean ok) {
		Button b = btNumbers[idx];
		if (hideIfInvalid) {
			b.setVisible(ok);
		} else {
			b.setEnabled(ok);
			b.setForeColor(ok ? Color.BLACK : Color.getRGB("c4c4c4"));
		}
	}

	private boolean isShown(char c) {
		Button b = btNumbers[c - '0'];
		return hideIfInvalid ? b.isVisible() : b.isEnabled();
	}
}
