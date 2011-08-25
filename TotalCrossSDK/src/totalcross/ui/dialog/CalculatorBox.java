/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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
import totalcross.ui.event.*;
import totalcross.sys.*;

/** This class is used by the Edit class when its mode is set to CURRENCY and displays
  * a calculator with the five basic operations and a numeric pad. The user
  * can paste the value entered or the total value.
  */

public class CalculatorBox extends Window
{
   private Edit edOper1,edOper2,edOrig,edTotal,curEdit;
   private PushButtonGroup pbgPaste,pbgAction,pbgOper,numericPad;
   private String answer;
   private KeyEvent ke = new KeyEvent(); // guich@421_59
   /** Strings used to display the paste messages. You can localize these strings if you wish. */
   public static String []pastes = {"Paste Op 1","Paste Total"};
   /** Strings used to display the action messages. You can localize these strings if you wish. */
   public static String []actions = {"Reuse","Clear","Cancel"}; // guich@320_44: added reuse button

   /** Specifies how many decimal places the entered value will be formatted with.
    * After you change this field, you must set <code>Edit.calculator = null</code> so a new instance
    * can be created with these settings.
    * @since SuperWaba 4.01
    */
   public static int decimalPlaces=2;

   public CalculatorBox()
   {
      super("Calculator",RECT_BORDER); // with caption and borders
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
      fadeOtherWindows = Settings.fadeOtherWindows;
      transitionEffect = Settings.enableWindowTransitionEffects ? TRANSITION_OPEN : TRANSITION_NONE;
      highResPrepared = true;
      started = true;
      curEdit = null;
   }

   private void setupUI() // guich@tc100b5_28
   {
      setBackColor(UIColors.calculatorBack); // before control definitions!

      String []opers = {"+","-","*","/","^"};

      edOper1=new Edit();
      edOper1.setFont(font);
      edOper1.setDecimalPlaces(decimalPlaces);
      edOper1.setMode(Edit.CURRENCY,true);//ValidChars(Edit.currencyCharsSet); // value - max = 999.999.999,99
      edOper1.setKeyboard(Edit.KBD_NONE);
      add(edOper1, LEFT+2,TOP+2);
      int extra = Settings.screenWidth >= 240 ? 2 : 0; // guich@571_9
      int extra2 = Settings.screenWidth >= 240 && uiAndroid ? fmH/2 : 0;

      edOper2=new Edit();
      edOper2.setFont(font);
      edOper2.setDecimalPlaces(decimalPlaces);
      edOper2.setMode(Edit.CURRENCY,true);
      edOper2.setKeyboard(Edit.KBD_NONE);
      add(edOper2, SAME,AFTER+2);

      pbgOper=new PushButtonGroup(opers,-1,1);
      pbgOper.setFont(font);
      add(pbgOper, SAME,AFTER+4,SAME,PREFERRED+2+extra2);

      edTotal=new Edit();
      edTotal.setFont(font);
      edTotal.setDecimalPlaces(decimalPlaces);
      edTotal.setMode(Edit.CURRENCY,true);
      edTotal.setKeyboard(Edit.KBD_NONE);
      edTotal.setEnabled(false);
      add(edTotal, LEFT+2,AFTER+4,PREFERRED,PREFERRED);

      pbgPaste = new PushButtonGroup(pastes,false,-1,2,14,2,true,PushButtonGroup.BUTTON);
      pbgPaste.setFont(font);
      add(pbgPaste, LEFT+2,AFTER+4+extra2*2,PREFERRED+extra2,PREFERRED+extra2*2);

      int x2,y2;
      // numeric pad
      if (!Settings.keypadOnly)
      {
         String []numerics = {"1","2","3","4","5","6","7","8","9",null,"0","-"};
         add(numericPad=new PushButtonGroup(numerics,false,-1,-1,10+extra*3,4,true,PushButtonGroup.BUTTON));
         numericPad.setFont(font);
         numericPad.setRect(AFTER+4, TOP+2,PREFERRED+extra2*3,uiAndroid ? FIT - 2 : PREFERRED+(uiCE?20:12), edOper1); // guich@571_9
         numericPad.setFocusLess(true); // guich@320_32
         numericPad.clearValueInt = -1;
      }

      pbgAction= new PushButtonGroup(actions,false,-1,2,12+extra*5,2,true,PushButtonGroup.BUTTON);
      pbgAction.setFont(font);
      pbgAction.colspan[2] = 2;
      add(pbgAction, AFTER+4,SAME,numericPad == null ? PREFERRED : numericPad.getX2() - pbgPaste.getX2() - 4,SAME,pbgPaste);

      x2 = numericPad.getAbsoluteRect().x2();
      y2 = pbgAction.getAbsoluteRect().y2();
      setRect(CENTER,CENTER,x2 + 4, y2+4);

      pbgOper.setBackColor(UIColors.calculatorFore);
      if (!Settings.keypadOnly)
         numericPad.setBackColor(UIColors.calculatorFore);
      pbgPaste.setBackColor(UIColors.calculatorAction);
      pbgAction.setBackColor(UIColors.calculatorAction);
      
      pbgPaste.clearValueInt = pbgAction.clearValueInt = pbgOper.clearValueInt = -1; // guich@580_47
      clear(false);
   }

   /** Gets the answer that the user selected to be pasted.
     * It can be the first operator, the total computed or null if the user canceled.
     */
   public String getAnswer() // guich@200b4_193: get the 'pasted' answer
   {
      return answer;
   }
   
   /** Clears everything in this calculator. If you call it with (true) before the window is popped up, a trash will appear when the window is unpopped. */
   public void clear(boolean requestFocusOnOper1) // guich@320_19: made public
   {
      if (requestFocusOnOper1)
         setFocus(edOper1); // guich@320_19: added this.
      clear(); // guich@580_47: call clear
      answer = null;
   }
   
   public void onUnpop()
   {
      setFocus(this);
   }
   
   public void onPopup()
   {
      if (children == null)
         setupUI();
      clear(false);
      Control c = topMost.getFocus();
      if (c instanceof Edit)
      {
         edOrig = (Edit)c;
         String s = edOrig.getTextWithoutMask();
         if (s.length() > 0 && "+-0123456789".indexOf(s.charAt(0)) != -1) // guich@401_16: added support for + and changed the routine
            edOper1.setText(s);
      } else edOrig = null;
   }

   public void postPopup()
   {
      String text1 = edOper1.getTextWithoutMask();
      try
      {
         Convert.toDouble(text1);
         setFocus(edOper2);
      } catch (InvalidNumberException ine) {setFocus(edOper1);} // guich@320_20: check not only if the field isnt empty, but also if its value is valid.
   }
   
   protected void postUnpop()
   {
      if (answer != null) // guich@580_27
         postPressedEvent();
   }
   
   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.FOCUS_IN: // handle Edit boxes focus changes
            if (event.target == edOper1)
               curEdit = edOper1;
            else
            if (event.target == edOper2)
               curEdit = edOper2;
            else
               curEdit = null;
            break;
         case KeyEvent.SPECIAL_KEY_PRESS:
            int key = ((KeyEvent)event).key;
            if (key == SpecialKeys.CALC)
               unpop();
            break;
         case ControlEvent.PRESSED:
            if (event.target == pbgPaste && pbgPaste.getSelectedIndex() != -1)
            {
               answer = pbgPaste.getSelectedIndex()==0?edOper1.getTextWithoutMask():edTotal.getTextWithoutMask();
               if (edOrig != null)
                  edOrig.setText(answer);
               unpop();
            }
            else
            if (event.target == pbgAction && pbgAction.getSelectedIndex() != -1)
            {
               switch (pbgAction.getSelectedIndex())
               {
                  case 0:
                     edOper1.setText(edTotal.getTextWithoutMask());
                     edOper2.setText("");
                     edTotal.setText("");
                     edOper2.requestFocus();
                     break;
                  case 1:
                     clear(true);
                     break;
                  case 2:
                     unpop();
                     break;
               }
            }
            else
            if (event.target == pbgOper && edOper1.getLength() > 0 && edOper2.getLength() > 0)
               try
               {
                  double oper1 = Convert.toDouble(edOper1.getTextWithoutMask());
                  double oper2 = Convert.toDouble(edOper2.getTextWithoutMask());
                  double result = 0;
                  switch (pbgOper.getSelectedIndex())
                  {
                     case 0: result = oper1 + oper2; break;
                     case 1: result = oper1 - oper2; break;
                     case 2: result = oper1 * oper2; break;
                     case 3: result = oper1 / oper2; break;
                     case 4: result = Math.pow(oper1, oper2); break; // guich@320_40
                  }
                  edTotal.setText(Convert.toString(result,decimalPlaces));
               } catch (InvalidNumberException ine) {edTotal.setText(ine.getMessage());}
            else
            if (event.target == numericPad && curEdit != null)
            {
               String s = numericPad.getSelectedItem();
               if (s != null)
               {
                  ke.key = s.charAt(0);
                  ke.target = curEdit;
                  curEdit._onEvent(ke);
               }
            }
            break;
      }
   }
}
