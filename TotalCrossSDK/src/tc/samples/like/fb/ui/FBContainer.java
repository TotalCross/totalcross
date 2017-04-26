package tc.samples.like.fb.ui;

import tc.samples.like.fb.*;

import totalcross.ui.*;
import totalcross.ui.font.*;
import totalcross.ui.image.*;

public class FBContainer extends ScrollContainer implements FBConstants
{
   public FBContainer()
   {
      super(false,true);
   }
   
   // Utility methods
   
   public static Edit createEdit(String tip)
   {
      Edit ed = new Edit();
      ed.captionColor = 0xAAAAAA;
      ed.caption = tip;
      ed.transparentBackground = true;
      return ed;
   }

   public static Ruler createRuler(int type)
   {
      Ruler r = new Ruler(type,false);
      r.setForeColor(BORDER);
      r.ignoreInsets = true;
      return r;
   }
   
   public static Button createButton(String s, Image i, int fmH)
   {
      Button b = new Button(s, i, Control.RIGHT, fmH);
      b.setFont(Font.getFont(true,fmH*8/10));
      b.setForeColor(0x9B9EA3);
      b.setBorder(Button.BORDER_NONE);
      return b;
   }
   
   public static Button noborder(Image img)
   {
      Button b = new Button(img);
      b.setBorder(Button.BORDER_NONE);
      return b;
   }

}
