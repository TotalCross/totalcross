package tc.samples.ui.controls;

import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class ButtonSample extends BaseContainer
{
   ScrollContainer sc;
   
   public void initUI()
   {
      super.initUI();
      try
      {
         super.initUI();
         setTitle("Button");
         sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         add(sc,LEFT,TOP,FILL,FILL);
         Button c;
   
         sc.add(new Button("Simple button"), LEFT, AFTER, PREFERRED+gap, PREFERRED );
   
         sc.add(c = new Button("This is\na multi-line\nButton"), LEFT, AFTER + gap, PREFERRED+gap, PREFERRED+gap);
         c.setPressedColor(Color.ORANGE);
         
         Image img = Resources.warning.getSmoothScaledInstance(fmH, fmH);
         img.applyColor2(BKGCOLOR);
         sc.add(c = new Button("This is an image Button", img, LEFT, gap), LEFT, AFTER + gap, PREFERRED+gap, PREFERRED+gap);
         c.setBackColor(SELCOLOR);
   
         
         img = new Image("imgbut.png").smoothScaledFromResolution(320);
         Font f = Font.getFont(true, Font.NORMAL_SIZE+2);
   
         sc.add(new Label("Text with\nimage: "),LEFT,AFTER+gap);
         Button btn;
         
         btn = new Button("Search", img, TOP, 8);
         btn.setFont(f);
         sc.add(btn,AFTER+5,SAME);
   
         btn = new Button("Search", img, BOTTOM, 8);
         btn.setFont(f);
         sc.add(btn,AFTER+5,SAME);
   
         btn = new Button("Search", img, LEFT, 8);
         btn.setFont(f);
         sc.add(btn,LEFT+5,AFTER+gap);
   
         btn = new Button("Search", img, RIGHT, 8);
         btn.setFont(f);
         sc.add(btn,AFTER+gap,SAME);
   
         btn = new Button(" Horizontal ");
         btn.setForeColor(0xEEEEEE);
         btn.setTextShadowColor(Color.BLACK);
         btn.setBorder(Button.BORDER_3D_HORIZONTAL_GRADIENT);
         sc.add(btn, RIGHT-5,AFTER+gap,PREFERRED,PREFERRED+10);
         
         btn = new Button(" Vertical ");
         btn.setForeColor(0xEEEEEE);
         btn.setTextShadowColor(Color.BLACK);
         btn.setBorder(Button.BORDER_3D_VERTICAL_GRADIENT);
         sc.add(btn, BEFORE-5,SAME,SAME,SAME);
   
         sc.add(new Label("Gradient: "),BEFORE-5,SAME,PREFERRED,SAME);
         
         addbtn(0xFF0000,RIGHT-5,AFTER+gap);
         addbtn(0x00FF00,BEFORE-4,SAME);
         addbtn(0xFFFF00,BEFORE-4,SAME);
         if (Settings.screenWidth > 240) addbtn(0x00FFFF,BEFORE-4,SAME);
         sc.add(new Label("Colorized: "),BEFORE-5,SAME,PREFERRED,SAME);

         addImageOnly();

         final Check cc = new Check("Enabled");
         sc.add(cc, LEFT,AFTER+gap);
         cc.setChecked(true);
         cc.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               Control []btns = sc.getBagChildren();
               for (int i = btns.length; --i >= 0;)
                  if (btns[i] instanceof Button)
                     btns[i].setEnabled(cc.isChecked());
            }
         });
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
         back();
      }
   }

   private void addImageOnly() throws Exception
   {
      Image[] images = {new Image("cancel.png"), new Image("ok.png")}; // images are 300x300
      int imgRes = 2048;
      int targetRes[] = {480,320,240};
      int k=0;
      final Button []btns = new Button[images.length * targetRes.length];
      
      for (int j = 0; j < targetRes.length; j++)
         for (int i = 0; i < 2; i++)
         {
            Image original = images[i];
            double factor = (double) targetRes[j] / (double) imgRes;
            Image img2 = original.smoothScaledBy(factor, factor);
            Button btn = btns[k++] = new Button(img2);
            if (j == 0) // just a demo for the user
               btn.pressedImage = img2.getTouchedUpInstance((byte)64,(byte)0);
            btn.setBorder(Button.BORDER_NONE);
            sc.add(btn, i == 0 ? LEFT : CENTER, i == 0 ? AFTER+gap : SAME, PARENTSIZE+30,PREFERRED);
         }
   }

   void addbtn(int color, int xpos, int ypos) throws Exception
   {
      Button btn = new Button("Rect", new Image("buttontemplate.png"), CENTER, 6);
      btn.setBackColor(backColor);
      btn.borderColor3DG = color;
      btn.setTextShadowColor(Color.BLACK);
      btn.setForeColor(color);
      btn.setFont(font.asBold());
      btn.setBorder(Button.BORDER_GRAY_IMAGE);
      sc.add(btn,xpos,ypos);
   }
}