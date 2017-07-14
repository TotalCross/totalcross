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
   public static interface AnimationListener
   {
      public void onAnimationFinished();
   }
   /** The percentage of the area used for the icon and the caption */
   public static int percIcon = 20, percCap = 80;
   protected Control []items;
   private int animDir;
   protected int selected=-1;
   /** Set to false to disable the close when pressing in a button of the menu. */
   public boolean autoClose = true;
   /** Defines the animation delay */
   public int totalTime=800;
   /** The percentage of the screen that this TopMenu will take: LEFT/RIGHT will take 50% of the screen's width, 
    * other directions will take 80% of the screen's width. Must be ser before calling <code>popup()</code>. */
   public int percWidth;
   private AnimationListener alist;
   /** The width in pixels instead of percentage of screen's width. */
   public int widthInPixels;
   
   /** An optional image to be used as background */
   public Image backImage;
   /** The alpha value to be applied to the background image */
   public int backImageAlpha = 128;
   
   /** Insets used to place the ScrollContainer. */
   public Insets scInsets = new Insets(2,1,2,1);
   
   public static class Item extends Container
   {
      Control tit;
      Object icon; // Image or Control

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
      
      /** Pass a Control and optionally another control that serves as an icon */
      public Item(Control c, Control icon)
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
         Control c = null;
         if (icon == null)
            perc = 100;
         else
         {
            if (icon instanceof Control)
               c = (Control)icon;
            else // surely an Image
               try 
               {
                  c = new ImageControl(Settings.enableWindowTransitionEffects ? ((Image)icon).getSmoothScaledInstance(itemH,itemH) : ((Image)icon).getHwScaledInstance(itemH,itemH)); 
                  ((ImageControl)c).centerImage = true;
               } catch (ImageException e) {}
            add(c == null ? (Control)new Spacer(itemH,itemH) : c,LEFT,TOP,PARENTSIZE+percIcon,FILL);
         }
         add(tit, (icon==null? tit instanceof Label ? LEFT+itemH:AFTER+0:AFTER+0),TOP,PARENTSIZE+perc-(tit instanceof Label?10:0),FILL,c);
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
   public TopMenu(Control []items, int animDir, byte borderStyle)
   {
      super(null,borderStyle);
      this.items = items;
      this.animDir = animDir;
      titleGap = 0;
      sameBackgroundColor = fadeOtherWindows = false;
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
      borderColor = UIColors.separatorFore;
      setBackForeColors(UIColors.separatorFore,UIColors.topmenuFore);
   }
   
   /** @param animDir LEFT, RIGHT, TOP, BOTTOM, CENTER */
   public TopMenu(Control []items, int animDir)
   {
      this(items, animDir, ROUND_BORDER);
   }

   public void popup()
   {
      setRect(false);
      super.popup();
   }
   
   protected void setRect(boolean screenResized)
   {
      int ww = setW = widthInPixels != 0 ? widthInPixels : SCREENSIZE+(percWidth > 0 ? percWidth : 50);
      switch (animDir)
      {
         case LEFT:
         case RIGHT:
            setRect(animDir,TOP,ww,FILL,null,screenResized); 
            break;
         default:
            setRect(100000,100000,ww,WILL_RESIZE,null,screenResized); 
            break;
      }
   }
   
   public void initUI()
   {
      int gap = 2;
      int n = items.length;
      int itemH = n == 1 ? Math.max(items[0].getPreferredHeight(),getClientRect().height) : fmH*2;
      int prefH = n * itemH + gap * n;
      boolean isLR = animDir == LEFT || animDir == RIGHT;
      ScrollContainer sc = new ScrollContainer(false,true);
      if (backImage != null)
         try
         {
            sc.transparentBackground = true;
            Rect r = getClientRect();
            Image img = backImage.smoothScaledFixedAspectRatio(r.height, true);
            img = img.getClippedInstance(0,0,r.width,r.height);
            img.alphaMask = backImageAlpha;
            add(new ImageControl(img), LEFT,TOP,FILL,FILL);
         }
         catch (Throwable t)
         {
            t.printStackTrace(); // don't add the image
         }
      add(sc,LEFT+scInsets.left,TOP+scInsets.top,FILL-scInsets.right,isLR ? PARENTSIZE+100 : Math.min(prefH, Settings.screenHeight-fmH*2)-scInsets.bottom);
      sc.setBackColor(backColor);
      for (int i = 0; i < n ; i++)
      {
         Control tmi = items[i];
         tmi.appId = i+1;
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
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (autoClose && e.target != this && ((Control)e.target).isChildOf(this))
            {
               selected = ((Control)e.target).appId-1;
               postPressedEvent();
               unpop();
            }
            break;
         case PenEvent.PEN_DRAG_END:
            DragEvent de = (DragEvent)e;
            if (sameDirection(animDir, de.direction) && de.xTotal >= width/2)
               unpop();
            break;
      }
   }
   
   private boolean sameDirection(int animDir, int dragDir)
   {
      if (animDir < 0) animDir = -animDir;
      return (dragDir == DragEvent.LEFT && animDir == LEFT) ||
            (dragDir == DragEvent.RIGHT && animDir == RIGHT) || 
            (dragDir == DragEvent.UP && animDir == TOP) || 
            (dragDir == DragEvent.DOWN && animDir == BOTTOM); 
   }

   public void screenResized()
   {
      setRect(true);
      removeAll();
      initUI();
      // used for custom containers
      if (items != null)
      for (int i = 0; i < items.length; i++)
         if (items[i].asContainer != null)
         {
            Control []c = items[i].asContainer.getChildren();
            if (c != null)
            for (int j = c.length; --j >= 0;)
               c[j].reposition();
         }
   }
   
   protected boolean onClickedOutside(PenEvent event)
   {
      if (event.type == PenEvent.PEN_UP)
         unpop();
      return true;
   }
   public void unpop()
   {
      unpop(null);
   }
   
   public void unpop(AnimationListener alist)
   {
      this.alist = alist;
      if (animDir == CENTER)
         FadeAnimation.create(this,false,this,totalTime).start();
      else
         PathAnimation.create(this,-animDir,this,totalTime).with(FadeAnimation.create(this,false,null,totalTime)).start();
   }
   public void onAnimationFinished(ControlAnimation anim)
   {
      super.unpop();
   }
   public void postUnpop()
   {
      super.postUnpop();
      if (alist != null)
         alist.onAnimationFinished();
   }
   public void onPopup()
   {
      selected = -1;
      screenResized(); // fix problem when the container is on portrait, then landscape, then closed, then portrait, then open
      if (animDir == CENTER)
      {
         resetSetPositions();
         setRect(CENTER,CENTER,KEEP,KEEP);
         FadeAnimation.create(this,true,null,totalTime).start();
      }
      else
         PathAnimation.create(this,animDir,null,totalTime).with(FadeAnimation.create(this,true,null,totalTime)).start();
   }
   
   public int getSelectedIndex()
   {
      return selected;
   }
}
