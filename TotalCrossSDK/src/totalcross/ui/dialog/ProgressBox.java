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

package totalcross.ui.dialog;

import totalcross.ui.*;

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
 * @see totalcross.ui.UIColors#spinnerFore
 * @see totalcross.ui.UIColors#spinnerBack
 * @since TotalCross 1.3
 */

public class ProgressBox extends MessageBox
{
   private Spinner spinner;
   
   /**
    * Constructs a progress box with the text and one "Ok" button. The text may be separated by '\n' as the line
    * delimiters; otherwise, it is automatically splitted if its too big to fit on screen.
    */
   public ProgressBox(String title, String msg)
   {
      super(title,msg);
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
   }

   /**
    * Constructs a progress box with the text and the specified button captions. The text may be separated by '\n' as the
    * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. if buttonCaptions is
    * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
    * setUnpopDelay method
    */
   public ProgressBox(String title, String text, String[] buttonCaptions)
   {
      super(title, text, buttonCaptions);
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
   }
   
   /**
    * Constructs a progress box with the text and the specified button captions. The text may be separated by '\n' as the
    * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
    * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
    * setUnpopDelay method. The parameters allSameWidth is the same as in the constructor for PushButtonGroup.
    */   
   public ProgressBox(String title, String text, String[] buttonCaptions, boolean allSameWidth)
   {
      super(title, text, buttonCaptions, allSameWidth);
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
   }
   
   /**
    * Constructs a progress box with the text and the specified button captions. The text may be separated by '\n' as the
    * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
    * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
    * setUnpopDelay method. The new parameters gap and insideGap are the same as in the constructor for PushButtonGroup.
    */   
   public ProgressBox(String title, String text, String[] buttonCaptions, int gap, int insideGap)
   {
      super(title, text, buttonCaptions, gap, insideGap);
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
   }

   /**
    * Constructs a progress box with the text and the specified button captions. The text may be separated by '\n' as the
    * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
    * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
    * setUnpopDelay method. The parameters allSameWidth, gap and insideGap are the same as in the constructor for PushButtonGroup.
    */
   public ProgressBox(String title, String text, String[] buttonCaptions, boolean allSameWidth, int gap, int insideGap) // andrew@420_5
   {
      super(title, text, buttonCaptions, allSameWidth, gap, insideGap);
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
   }
   
   protected void onPopup()
   {
      lgap = fmH * 2 + fmH/4; // gap of fmH/8 at left and at right
      super.onPopup();
      if (spinner != null) // guich@tc200: two spinners was being created during popup, because MessageBox' reposition was calling onPopup
         spinner.stop();
      spinner = new Spinner();
      spinner.setBackForeColors(backColor, foreColor);
      boolean multiline = msg.getLineCount() > 1;
      int s = multiline ? fmH*2 : fmH;
      int y = btns == null ? CENTER : titleGap == 0 ? TOP+androidBorderThickness : TOP;
      if (multiline)
         msg.setRect(KEEP,y,KEEP,KEEP);
      else
         msg.setRect(msg.getX()-fmH/2,y,KEEP,KEEP);
      add(spinner,LEFT+fmH/2,CENTER_OF+2,s,s,msg);
      spinner.start();
   }
   
   protected void onUnpop()
   {
      spinner.stop();
      totalcross.sys.Vm.sleep(150); // wait the thread die 
      super.onUnpop();
   }
}