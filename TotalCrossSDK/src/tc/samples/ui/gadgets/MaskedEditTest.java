package tc.samples.ui.gadgets;

import totalcross.ui.*;
import totalcross.ui.event.*;

public class MaskedEditTest extends Container
{
   public void initUI()
   {
      Edit ed;
      add(new Label("Currency masked edit:"),LEFT,TOP);
      ed = new Edit("999.999.999,99");
      ed.setMode(Edit.CURRENCY,true);
      add(ed,RIGHT_OF,AFTER+2);
      ed.setKeyboard(Edit.KBD_NONE);

      add(new Label("Date masked edit:"),LEFT,AFTER+10);
      ed = new Edit();
      ed.setMode(Edit.DATE,true);
      add(ed,LEFT,AFTER+2);
      ed.setKeyboard(Edit.KBD_NONE);

      add(new Label("Normal masked edit:\n(Brazilian's 8-digit postal code)"),LEFT,AFTER+10);
      ed = new Edit("99.999-999");
      ed.setMode(Edit.NORMAL,true);
      add(ed,LEFT,AFTER+2);
      ed.setKeyboard(Edit.KBD_NONE);

      final PushButtonGroup pbg;
      add(pbg = new PushButtonGroup(new String[]{"1","2","3","4","5","6","7","8","9","0"}, 2, 2), LEFT+10,AFTER+10, FILL-10,FILL-10);
      pbg.setFocusLess(true);
      final KeyEvent ke = new KeyEvent();
      ke.type = KeyEvent.KEY_PRESS;
      pbg.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
            Control focus = getParentWindow().getFocus();
            if (focus instanceof Edit && pbg.getSelectedIndex() >= 0)
            {
               ke.touch();
               ke.target = focus;
               ke.key = pbg.getSelectedItem().charAt(0);
               focus.onEvent(ke);
            }
         }
      });
   }

}
