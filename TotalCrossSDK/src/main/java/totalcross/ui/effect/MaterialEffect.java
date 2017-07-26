package totalcross.ui.effect;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class MaterialEffect extends UIEffects implements PenListener, TimerListener
{
   public interface SideEffect
   {
      public void sideStart();
      public void sideStop();
      public void sidePaint(Graphics g, int alpha);
   }
   
   public SideEffect sideEffect;
   
   private static final int TIMER_INTERVAL = 10;
   private TimerEvent te;
   private Image matImg;
   private Control target;
   private int px,py,max,alpha,iniDn,iniUp;
   private boolean isDown, sideEffOnly;
   private int x=X_UNSET,y,w,h;
   private static final int X_UNSET = X_UNKNOWN+1; // first time x will be set
   private static final int X_WAS_UNKNOWN = X_UNKNOWN+2; // x was set but was unknown, dont ask to set again until animation stops
   
   public MaterialEffect(Control target)
   {
      this.target = target;
      target.addPenListener(this);
      if (target instanceof SideEffect)
         sideEffect = (SideEffect)target;
      darkSideOnPress = target instanceof Button || target instanceof PushButtonGroup;
   }
   
   public boolean isRunning()
   {
      return te != null;
   }
   
   public void paintEffect(Graphics g)
   {
      if (te != null && enabled)
      {
         if (x == X_UNSET && x != X_WAS_UNKNOWN)
         {
            x = target.getEffectX();
            if (x == X_UNKNOWN)
            {
               x = X_WAS_UNKNOWN;
               return;
            }
            y = target.getEffectY();
            w = target.getEffectW();
            h = target.getEffectH();
         }
         if (matImg == null || matImg.getWidth() != w || matImg.getHeight() != h)
            try {matImg = new Image(w,h);} catch (Throwable t) {t.printStackTrace();}
         if (matImg != null)
         {
            int curDn = Vm.getTimeStamp() - iniDn;
            int curUp = Vm.getTimeStamp() - iniUp;
            int rad = Math.min(max, curDn * max / duration);
            alpha = isDown ? alphaValue : rad < max ? alphaValue - curUp * alphaValue / (duration-(iniUp-iniDn)) : Math.max(0, alpha - TIMER_INTERVAL);
            
            if (!sideEffOnly)
            {
               Graphics gg = matImg.getGraphics();
               gg.backColor = gg.alpha = 0;
               gg.fillRect(0,0,w,h);
               gg.alpha = alpha << 24;
               if (darkSideOnPress)
                  gg.setClip(2,2,w-4,h-4);
               int bc = target.getBackColor();
               int olda = gg.alpha;
               if (isDown) gg.alpha = alphaValue << 24;
               gg.backColor = color != -1 && color != bc ? color : Color.getBrightness(bc) < 127 ? Color.brighter(bc,64) : Color.darker(bc,64);
               gg.fillCircle(px - x,py - y, rad);
               gg.alpha = olda;
               if (isDown && darkSideOnPress) // make darker area at sides and bottom
               {
                  gg.clearClip();
                  gg.alpha = 0x40000000;
                  gg.backColor = 0;
                  gg.fillRect(2,h-2,w-4,2);
                  gg.fillRect(0,h/10,2,h);
                  gg.fillRect(w-2,h/10,2,h);
               }
               matImg.applyChanges();
               Rect r = new Rect();
               g.getClip(r);
               g.clearClip();
               g.drawImage(matImg,x,y);
               g.setClip(r);
            }
            if (sideEffect != null)
               sideEffect.sidePaint(g, alpha);
         }
      }
   }
   
   public void penDown(PenEvent e)
   {
      if (!sideEffOnly)
      {
         if (isRunning())
            postEvent();
         x = X_UNSET;
         px = e.x;
         py = e.y;
         isDown = true;
         start(false);
      }
   }
   
   PenEvent penUp;
   public void penUp(PenEvent e)
   {
      penUp = e.clone();
      if (sideEffOnly)
         start(true);
      if (sideEffect == null && enabled)
         e.consumed = true; // post pressed event only when effect finishes
      iniUp = Vm.getTimeStamp();
      isDown = false;
   }

   public void timerTriggered(TimerEvent e)
   {
      if (te.triggered)
      {
         if (alpha == 0)
            stop();
         else
            Window.needsPaint = true;
      }
   }

   public void startEffect()
   {
      if (target.isDisplayed())
      {
         px = py = 0;
         start(true);
      }
   }
   
   private void start(boolean sfo)
   {
      sideEffOnly = sfo;
      iniDn = Vm.getTimeStamp();
      if (sfo) iniUp = iniDn;
      int w = Math.max(px, target.getWidth()-px);
      int h = Math.max(py, target.getHeight()-py);
      max = (int)Math.sqrt(w*w + h*h) + 1;
      Window.needsPaint = true;
      te = target.addTimer(TIMER_INTERVAL);
      target.addTimerListener(this);
      if (sideEffect != null)
         sideEffect.sideStart();
   }

   private void stop()
   {
      if (sideEffect != null)
         sideEffect.sideStop();
      sideEffOnly = false;
      target.removeTimer(te);
      target.removeTimerListener(this);
      te = null;
      isDown = false;
      postEvent();
   }
   
   private void postEvent()
   {
      if (penUp != null && sideEffect == null && !target.hadParentScrolled())
         target.onEvent(penUp);
   }

   public void penDrag(DragEvent e) {}
   public void penDragStart(DragEvent e) {}
   public void penDragEnd(DragEvent e) {}
}
