package tc.samples.ui.gadgets;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class ImageAndTextButton extends Container
{
   void addbtn(int color, int xpos, int ypos) throws Exception
   {
      Button btn = new Button("Rect", new Image("buttontemplate.png"), CENTER, 6);
      btn.setBackColor(backColor);
      btn.borderColor3DG = color;
      btn.setTextShadowColor(Color.BLACK);
      btn.setForeColor(color);
      btn.setFont(font.asBold());
      btn.setBorder(Button.BORDER_GRAY_IMAGE);
      add(btn,xpos,ypos);
   }
   
   public void initUI()
   {
      Button btn;
      try
      {
         Image img = new Image("imgbut.png").smoothScaledFromResolution(320, backColor);
         Font f = Font.getFont(true, Font.NORMAL_SIZE+2);

         add(new Label("Text with\nimage: "),LEFT+5,TOP+5);
         
         Button.commonGap = 2;
         btn = new Button("Search", img, TOP, 8);
         btn.setFont(f);
         add(btn,AFTER+5,TOP+5);

         btn = new Button("Search", img, BOTTOM, 8);
         btn.setFont(f);
         add(btn,AFTER+5,SAME);

         btn = new Button("Search", img, LEFT, 8);
         btn.setFont(f);
         add(btn,LEFT+5,AFTER+5);

         btn = new Button("Search", img, RIGHT, 8);
         btn.setFont(f);
         add(btn,AFTER+5,SAME);
         Button.commonGap = 0;

         btn = new Button(" Horizontal ");
         btn.setForeColor(0xEEEEEE);
         btn.setTextShadowColor(Color.BLACK);
         btn.setBorder(Button.BORDER_3D_HORIZONTAL_GRADIENT);
         add(btn, RIGHT-5,AFTER+5,PREFERRED,PREFERRED+10);
         
         btn = new Button(" Vertical ");
         btn.setForeColor(0xEEEEEE);
         btn.setTextShadowColor(Color.BLACK);
         btn.setBorder(Button.BORDER_3D_VERTICAL_GRADIENT);
         add(btn, BEFORE-5,SAME,SAME,SAME);

         add(new Label("Gradient: "),BEFORE-5,SAME,PREFERRED,SAME);
         
         addbtn(0xFF0000,RIGHT-5,AFTER+5);
         addbtn(0x00FF00,BEFORE-4,SAME);
         addbtn(0xFFFF00,BEFORE-4,SAME);
         if (Settings.screenWidth > 240) addbtn(0x00FFFF,BEFORE-4,SAME);
         add(new Label("Colorized\nimage: "),BEFORE-5,SAME,PREFERRED,SAME);
         
         btn = new Button("This is a\nmulti-lined\ntext button");
         add(btn, LEFT+5,AFTER+5);

         final Check c = new Check("Enabled");
         add(c, RIGHT-5,CENTER_OF,PREFERRED,PREFERRED);
         c.setChecked(true);
         c.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               Control []btns = getChildren();
               for (int i = btns.length; --i >= 0;)
                  if (btns[i] instanceof Button)
                     btns[i].setEnabled(c.isChecked());
            }
         });
         
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

}
