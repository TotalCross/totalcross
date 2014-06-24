package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.event.*;
import totalcross.ui.image.*;

public class TopMenu extends Window
{
   public Image[] icons;
   public String[] captions;
   public int percIcon = 20, percCap = 80;
   private ScrollContainer sc;
   
   private class TopMenuItem extends Container
   {
      String caption;
      Image icon;
      Label lab;
      
      TopMenuItem(String cap, Image icon)
      {
         this.caption = cap;
         this.icon = icon;
         lab = new Label(caption,LEFT);
         setBackForeColors(UIColors.topmenuBack,UIColors.topmenuFore);
      }
      public void initUI()
      {
         int perc = percCap;
         if (icon == null)
            perc = 100;
         else
         {
            ImageControl ic = null;
            try {ic = new ImageControl(icon.getSmoothScaledInstance(fmH,fmH)); ic.centerImage = true;} catch (ImageException e) {}
            add(ic == null ? (Control)new Spacer(fmH,fmH) : (Control)ic,LEFT,TOP,PARENTSIZE+percIcon,FILL);
         }
         add(lab, AFTER+(icon==null?fmH:0),TOP,PARENTSIZE+perc-10,FILL);
      }
   }
   
   public TopMenu(String[] captions, Image[] icons)
   {
      super(null,ROUND_BORDER);
      titleGap = 0;
      this.icons = icons;
      this.captions = captions;
      fadeOtherWindows = false;
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
      borderColor = UIColors.separatorFore;
      setBackForeColors(UIColors.separatorFore,UIColors.topmenuFore);
      
      setRect(100000,100000,SCREENSIZE+80,WILL_RESIZE);
   }
   
   final public void initUI()
   {
      int itemH = fmH*2;
      int gap = 2;
      int n = captions.length;
      int prefH = n * itemH + gap * n;
      add(sc = new ScrollContainer(false,true),LEFT+1,TOP+2,FILL-1,Math.min(prefH, Settings.screenHeight-fmH*2)-2);
      for (int i = 0;; i++)
      {
         sc.add(new TopMenuItem(captions[i], icons == null ? null : icons[i]),LEFT,AFTER,FILL,itemH);
         if (i == n-1) break;
         Ruler r = new Ruler(Ruler.HORIZONTAL,false);
         r.setBackColor(backColor);
         sc.add(r,LEFT,AFTER,FILL, gap);
      }
      resizeHeight();
   }
   
   protected boolean onClickedOutside(PenEvent event)
   {
      unpop();
      return true;
   }
}
