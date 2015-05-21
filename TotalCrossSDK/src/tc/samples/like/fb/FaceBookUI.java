package tc.samples.like.fb;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class FaceBookUI extends MainWindow
{
   private static final int TOPBAR = 0x3C5998;
   private static final int BOTBAR = 0x47639E;

   class MyEdit extends Edit
   {
      public void onPaint(Graphics g)
      {
         super.onPaint(g);
         if (!hasFocus && getLength() == 0)
         {
            g.foreColor = 0xAAAAAA;
            g.drawText("Busca",0,fmH/4);
         }
      }
   }
   
   class TopBar extends Container
   {
      Button btSearch, btOnline;
      
      public void initUI()
      {
         setBackColor(TOPBAR);
         add(btSearch = noborder(FBImages.search), LEFT+100,CENTER);
         add(btOnline = noborder(FBImages.online), RIGHT-100,CENTER);
         Edit ed = new MyEdit();
         ed.setForeColor(Color.WHITE);
         ed.transparentBackground = true;
         add(ed, AFTER,SAME,FIT,PREFERRED,btSearch);
         Ruler r = new Ruler();
         r.dots = true;
         add(r, SAME,AFTER,SAME-100,1);
      }
   }
   
   class TopMenu extends Container
   {
      private Button create(Image img)
      {
         Button b = noborder(img);
         try {(b.pressedImage = img.getCopy()).applyColor2(TOPBAR);} catch (Exception e) {}
         b.shiftOnPress = false;
         b.isSticky = true;
         return b;
      }
      
      public void initUI()
      {
         setBackColor(Color.WHITE);
         add(create(FBImages.content),LEFT,CENTER,PARENTSIZE+20,PREFERRED);
         add(create(FBImages.friends),AFTER,CENTER,PARENTSIZE+20,PREFERRED);
         add(create(FBImages.chat),AFTER,CENTER,PARENTSIZE+20,PREFERRED);
         add(create(FBImages.news),AFTER,CENTER,PARENTSIZE+20,PREFERRED);
         add(create(FBImages.menu),AFTER,CENTER,PARENTSIZE+20,PREFERRED);
      }
   }
   
   private static Button noborder(Image img)
   {
      Button b = new Button(img);
      b.setBorder(Button.BORDER_NONE);
      return b;
   }
   
   public FaceBookUI()
   {
      Settings.uiAdjustmentsBasedOnFontHeight = true;
      setUIStyle(Settings.Android);
   }

   public void initUI()
   {
      try
      {
         FBImages.load(fmH);
         add(new TopBar(), LEFT,TOP,FILL,fmH*5/2);
         add(new TopMenu(), LEFT,AFTER,FILL,SAME);
         add(h = new ContentHolder(),LEFT,AFTER,FILL,FILL); 
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
         exit(0);
      }
   }
   
   ContentHolder h;
   
   class Content extends Container
   {
      public void initUI()
      {
         setBackColor(Color.WHITE);
      }
   }
   
   class ContentHolder extends ScrollContainer
   {
      public ContentHolder()
      {
         super(false,true);
         setBackColor(0xDCDEE3);         
      }
      
      public void initUI()
      {
         setInsets(fmH/2,fmH/2,2,2);
         add(new Content(),LEFT,AFTER,FILL,PARENTSIZE+40);
      }
   }
}
