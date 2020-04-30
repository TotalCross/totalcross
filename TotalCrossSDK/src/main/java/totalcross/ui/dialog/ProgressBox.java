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

import totalcross.ui.Container;
import totalcross.ui.Label;
import totalcross.ui.Spinner;
import totalcross.ui.Window;
import totalcross.ui.font.Font;
import totalcross.util.UnitsConverter;

/** This class implements a MessageBox that shows a Spinner at left of the text.
 * You can set the spinner color and type before constructing the ProgressBox (usually you do
 * this in your application's constructor, and not for each ProgressBar created).
 * 
 * Here's a sample:
 * 
 * <pre>
 * ProgressBox pb = new ProgressBox("Message","Loading, please wait...",null);
 * pb.popupNonBlocking();
 * ... lengthy task
 * pb.unpop();
 * </pre>
 * 
 * @see totalcross.ui.Spinner#spinnerType
 * @since TotalCross 1.3
 */

public class ProgressBox extends MessageBox {
  private Spinner spinner;
  private Label msgLbl;
  public Font contentFont;

  /**
   * Constructs a progress box with the text and one "Ok" button. The text may be separated by '\n' as the line
   * delimiters; otherwise, it is automatically splitted if its too big to fit on screen.
   */
  public ProgressBox(String title, String text) {
    super(title, text);
    config(text);
  }

  /**
   * Constructs a progress box with the text and the specified button captions. The text may be separated by '\n' as the
   * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. if buttonCaptions is
   * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
   * setUnpopDelay method
   */
  public ProgressBox(String title, String text, String[] buttonCaptions) {
    super(title, text, buttonCaptions);
    config(text);
  }

  /**
   * Constructs a progress box with the text and the specified button captions. The text may be separated by '\n' as the
   * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
   * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
   * setUnpopDelay method. The parameters allSameWidth is the same as in the constructor for PushButtonGroup.
   */
  public ProgressBox(String title, String text, String[] buttonCaptions, boolean allSameWidth) {
    super(title, text, buttonCaptions, allSameWidth);
    config(text);
  }

  /**
   * Constructs a progress box with the text and the specified button captions. The text may be separated by '\n' as the
   * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
   * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
   * setUnpopDelay method. The new parameters gap and insideGap are the same as in the constructor for PushButtonGroup.
   */
  public ProgressBox(String title, String text, String[] buttonCaptions, int gap, int insideGap) {
    super(title, text, buttonCaptions, gap, insideGap);
    config(text);
  }

  /**
   * Constructs a progress box with the text and the specified button captions. The text may be separated by '\n' as the
   * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
   * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
   * setUnpopDelay method. The parameters allSameWidth, gap and insideGap are the same as in the constructor for PushButtonGroup.
   */
  public ProgressBox(String title, String text, String[] buttonCaptions, boolean allSameWidth, int gap, int insideGap) // andrew@420_5
  {
    super(null, title, text, buttonCaptions, allSameWidth, gap, insideGap);
    config(text);
  }

  public void config(String message) {
	uiAdjustmentsBasedOnFontHeightIsSupported = false;
	contButtonGap = 5;
	usingOwnCont = true;
	baseContainer = new Container() {
		@Override
		public void initUI() {
			super.initUI();
		    if (spinner != null)
	          spinner.stop();
	        
	        spinner = new Spinner();
	        spinner.setBackForeColors(backColor, foreColor);
	        
	        msgLbl = new Label(message == null ? "" : message);
	        msgLbl.autoSplit = true;
	        add(spinner, LEFT, TOP, 40 + DP, 40 + DP);
	        add(msgLbl, AFTER + 20, SAME, FILL, PREFERRED);
	        spinner.start();
		}
	};
	baseContainer.setFont(contentFont = Font.getFont(false, 16));
  }
  
  @Override
	public void setText(String text) {
		// TODO Auto-generated method stub
	    msgLbl.setText(text);
	}
  
  @Override
  protected void onUnpop() {
    spinner.stop();
    totalcross.sys.Vm.sleep(150); // wait the thread die 
    super.onUnpop();
  }
}