package totalcross.ui.effect;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

public abstract class UIEffects
{
   public enum Effects
   {
      NONE,
      MATERIAL,
   }
   
   public static int X_UNKNOWN = -9999999; // used clicked outside the component
   public static int duration = 200;
   public static Effects defaultEffect = Effects.NONE;
   
   public boolean darkSideOnPress;
   public boolean enabled=true;
   public int color = -1;
   
   public static UIEffects get(Control c)
   {
      switch (defaultEffect)
      {
         case MATERIAL:
            return new MaterialEffect(c);
         default:
            return null;
      }
   }
   
   public abstract boolean isRunning();
   public abstract void startEffect();
   public abstract void paintEffect(Graphics g);
}
