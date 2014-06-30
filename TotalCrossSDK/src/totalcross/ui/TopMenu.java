package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.anim.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

/** This is a top menu like those on Android. It opens and closes using animation and fading effects.
 * @since TotalCross 3.03
 */
public class TopMenu extends Window implements PathAnimation.AnimationFinished
{
   public Image[] icons;
   public String[] captions;
   public int percIcon = 20, percCap = 80;
   private ScrollContainer sc;
   private int animDir;
   private int selected=-1;
   
   private class TopMenuItem extends Container
   {
      String caption;
      Image icon;
      Label lab;
      ImageControl ic;
      
      TopMenuItem(String cap, Image icon)
      {
         this.caption = cap;
         this.icon = icon;
         lab = new Label(caption,LEFT);
         setBackForeColors(UIColors.topmenuBack,UIColors.topmenuFore);
         focusTraversable = true;
      }
      public void initUI()
      {
         int perc = percCap;
         if (icon == null)
            perc = 100;
         else
         {
            ic = null;
            try {ic = new ImageControl(icon.getSmoothScaledInstance(fmH,fmH)); ic.centerImage = true;} catch (ImageException e) {}
            add(ic == null ? (Control)new Spacer(fmH,fmH) : (Control)ic,LEFT,TOP,PARENTSIZE+percIcon,FILL);
         }
         add(lab, AFTER+(icon==null?fmH:0),TOP,PARENTSIZE+perc-10,FILL);
      }
      public void onEvent(Event e)
      {
         if (e.type == PenEvent.PEN_UP && !hadParentScrolled())
         {
            setBackColor(Color.brighter(backColor));
            lab.setBackColor(backColor);
            if (ic != null) ic.setBackColor(backColor);
            selected = this.appId;
            TopMenu.this.postPressedEvent();
            repaintNow();
            Vm.sleep(100);
            unpop();
         }
      }      
   }
   
   /** @param animDir LEFT, RIGHT, TOP, BOTTOM, CENTER */
   public TopMenu(String[] captions, Image[] icons, int animDir)
   {
      super(null,ROUND_BORDER);
      titleGap = 0;
      this.icons = icons;
      this.captions = captions;
      this.animDir = animDir;
      fadeOtherWindows = false;
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
      borderColor = UIColors.separatorFore;
      setBackForeColors(UIColors.separatorFore,UIColors.topmenuFore);
      
      switch (animDir)
      {
         case LEFT:
         case RIGHT:
            setRect(animDir,TOP,SCREENSIZE+50,FILL); 
            break;
         default:
            setRect(100000,100000,SCREENSIZE+80,WILL_RESIZE); 
            break;
      }
   }
   
   final public void initUI()
   {
      int itemH = fmH*2;
      int gap = 2;
      int n = captions.length;
      int prefH = n * itemH + gap * n;
      boolean isLR = animDir == LEFT || animDir == RIGHT;
      add(sc = new ScrollContainer(false,true),LEFT+1,TOP+2,FILL-1,isLR ? PARENTSIZE+100 : Math.min(prefH, Settings.screenHeight-fmH*2)-2);
      sc.setBackColor(backColor);
      for (int i = 0;; i++)
      {
         TopMenuItem tmi = new TopMenuItem(captions[i], icons == null ? null : icons[i]);
         tmi.appId = i;
         sc.add(tmi,LEFT,AFTER,FILL,itemH);
         if (i == n-1) break;
         Ruler r = new Ruler(Ruler.HORIZONTAL,false);
         r.setBackColor(backColor);
         sc.add(r,LEFT,AFTER,FILL, gap);
      }
      if (!isLR) resizeHeight();
   }
   
   public void screenResized()
   {
      removeAll();
      initUI();
   }
   
   protected boolean onClickedOutside(PenEvent event)
   {
      if (event.type == PenEvent.PEN_UP)
         unpop();
      return true;
   }
   public void unpop()
   {
      try
      {
         if (animDir == CENTER)
            FadeAnimation.create(this,false,this).start();
         else
            PathAnimation.create(this,-animDir,this).with(FadeAnimation.create(this,false)).start();
      }
      catch (Exception e)
      {
         if (Settings.onJavaSE) e.printStackTrace();
         super.unpop(); // no animation, just unpop
      }
   }
   public void onAnimationFinished(ControlAnimation anim)
   {
      super.unpop();
   }
   public void onPopup()
   {
      selected = -1;
      try
      {
         if (animDir == CENTER)
         {
            resetSetPositions();
            setRect(CENTER,CENTER,KEEP,KEEP);
            FadeAnimation.create(this,true).start();
         }
         else
            PathAnimation.create(this,animDir).with(FadeAnimation.create(this,true)).start();
      }
      catch (Exception e)
      {
         if (Settings.onJavaSE) e.printStackTrace();
         // no animation, just popup
      }
   }
   
   public int getSelectedIndex()
   {
      return selected;
   }
}
