package tc.samples.ui.gadgets;

import totalcross.io.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class FileChooserTest extends Container
{
   public void initUI()
   {
      final Button btn,btn2;
      final Label l;
      final Check ch;
      add(btn = new Button("Choose file"),CENTER,CENTER);
      add(ch = new Check("Multiple selection"),CENTER,BEFORE-20);
      add(btn2 = new Button("Delete file"),CENTER,AFTER+20,btn);
      btn2.setEnabled(false);
      add(l = new Label(),LEFT,BOTTOM);
      btn.addPressListener(
         new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               try
               {
                  FileChooserBox fcb = new FileChooserBox(null);
                  fcb.multipleSelection = ch.isChecked(); // guich@tc115_4
                  fcb.mountTree("device/");
                  fcb.popup();
                  String s = fcb.getAnswer();
                  if (s == null)
                     l.setText("Cancelled");
                  else
                  if (fm.stringWidth(s) > getWidth())
                     l.setMarqueeText(s, 100, 1, -8);
                  else
                     l.setText(s);
                  btn2.setEnabled(s != null);
               }
               catch (Exception ee)
               {
                  MessageBox.showException(ee,true);
               }
            }
         });
      btn2.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
            try
            {
               String s = l.getText();
               new File(s,File.DONT_OPEN,1).delete();
            }
            catch (Exception ee)
            {
               MessageBox.showException(ee,false);
            }
         }
      });
   }

}
