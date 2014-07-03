package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.anim.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

/** This is a top menu like those on Android. It opens and closes using animation and fading effects.
 * Caution: if you place an image in an Item, use getSmoothScale methods instead of getHwScale ones.
 * @since TotalCross 3.03
 */
public class TopMenu extends Window implements PathAnimation.AnimationFinished
{
   public static int percIcon = 20, percCap = 80;
   private Control []items;
   private ScrollContainer sc;
   private int animDir;
   private int selected=-1;
   
   public static class Item extends Container
   {
      Control tit;
      Image icon;
      ImageControl ic;

      /** Used when you want to fully customize your Item by extending this class. */
      protected Item()
      {
         setBackForeColors(UIColors.topmenuBack,UIColors.topmenuFore);
      }
      
      /** Pass a Control and optionally an icon */
      public Item(Control c, Image icon)
      {
         this();
         this.tit = c;
         this.icon = icon;
      }
      
      /** Creates a Label and optionally an icon */
      public Item(String cap, Image icon)
      {
         this(new Label((String)cap,LEFT),icon);
      }
      
      public void initUI()
      {
         int itemH = fmH + Edit.prefH;
         int perc = percCap;
         if (icon == null)
            perc = 100;
         else
         {
            try {ic = new ImageControl(icon.getSmoothScaledInstance(itemH,itemH)); ic.centerImage = true;} catch (ImageException e) {}
            add(ic == null ? (Control)new Spacer(itemH,itemH) : (Control)ic,LEFT,TOP,PARENTSIZE+percIcon,FILL);
         }
         add(tit, AFTER+(icon==null? tit instanceof Label ? itemH:0:0),TOP,PARENTSIZE+perc-(tit instanceof Label?10:0),FILL,ic);
      }
      
      public void onEvent(Event e)
      {
         if (e.type == PenEvent.PEN_UP && !hadParentScrolled())
         {
            int bc = backColor;
            setBackColors(Color.brighter(bc));
            repaintNow();
            Vm.sleep(100);
            postPressedEvent();
            setBackColors(bc);
         }
      }   
      private void setBackColors(int b)
      {
         setBackColor(b);
         for (Control child = children; child != null; child = child.next)
            if (child instanceof Label || child instanceof ImageControl) // changing ComboBox back does not work well... should think in an alternative
               child.setBackColor(b);
      }
   }
   
   /** @param animDir LEFT, RIGHT, TOP, BOTTOM, CENTER */
   public TopMenu(Control []items, int animDir)
   {
      super(null,ROUND_BORDER);
      this.items = items;
      this.animDir = animDir;
      titleGap = 0;
      fadeOtherWindows = false;
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
      borderColor = UIColors.separatorFore;
      setBackForeColors(UIColors.separatorFore,UIColors.topmenuFore);
      setRect();
   }
   
   private void setRect()
   {
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
      int n = items.length;
      int prefH = n * itemH + gap * n;
      boolean isLR = animDir == LEFT || animDir == RIGHT;
      add(sc = new ScrollContainer(false,true),LEFT+1,TOP+2,FILL-1,isLR ? PARENTSIZE+100 : Math.min(prefH, Settings.screenHeight-fmH*2)-2);
      sc.setBackColor(backColor);
      for (int i = 0;; i++)
      {
         Control tmi = items[i];
         tmi.appId = i;
         sc.add(tmi,LEFT,AFTER,FILL,itemH);
         if (i == n-1) break;
         Ruler r = new Ruler(Ruler.HORIZONTAL,false);
         r.setBackColor(backColor);
         sc.add(r,LEFT,AFTER,FILL, gap);
      }
      if (!isLR) resizeHeight();
   }
   
   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target != this && ((Control)e.target).isChildOf(this))
      {
         if (e.target instanceof Item)
            selected = ((Item)e.target).appId;
         postPressedEvent();
         unpop();
      }
   }
   
   public void screenResized()
   {
      setRect();
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
