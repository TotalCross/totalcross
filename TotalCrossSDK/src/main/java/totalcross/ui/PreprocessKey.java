package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.event.*;

/** Class used to customize the way that Edit and MultiEdit handle keys. Useful, for example, 
 * to handle devices with special keypads and translate them into standard characters.
 * 
 * You have to extend the preprocess method and change the key to one you may want to use.
 * Then, assign an instance of the class to the <code>instance</code> member, and the Edit and MultiEdit will use them
 * 
 * It has two handy methods, to be used to map char sequences. Below, an implementation for the the MC67.
 * <pre>
   PreprocessKey.instance = 
      new PreprocessKey()
      {
         private final char[][] keys = 
         {
            {'.',','},         
            {'\0'},            
            {'a','b','c'},     
            {'d','e','f'},     
            {'g','h','i'},     
            {'j','k','l'},     
            {'m','n','o'},     
            {'p','q','r','s'}, 
            {'t','u','v'},     
            {'w','x','y','z'}, 
         };

         protected char[] getKeySet(Control target, KeyEvent e)
         {
            if (e.type == KeyEvent.KEY_PRESS && (Settings.onJavaSE || (e.modifiers & SpecialKeys.SYSTEM) != 0)) // use only if key came from physical keyboard
            {
               char c = Convert.toLowerCase((char)e.key);
               for (int i = 0; i < keys.length; i++)
                  if (keys[i][0] == c)
                     return keys[i];
            }
            return null;
         }

         public void preprocess(Control target, KeyEvent e)
         {
            handleKeypadPress(target, e);
         }
      };
 * 
 */

public abstract class PreprocessKey
{
   public static PreprocessKey instance;
   
   /** Maximum millis delay to consider that the user is repeating the current key */
   public static int MAX_DELAY = 1000;
   
   public abstract void preprocess(Control target, KeyEvent ke);
   
   private int curIdx=-1,curKey;
   private int lastTime;
   private KeyEvent backspace = new KeyEvent(KeyEvent.SPECIAL_KEY_PRESS, SpecialKeys.BACKSPACE, 0);
   
   protected char[] getKeySet(Control target, KeyEvent e)
   {
      return null;
   }
   
   protected void handleKeypadPress(Control target, KeyEvent e)
   {
      //if (Settings.onJavaSE) e.modifiers = SpecialKeys.SYSTEM;         
      int key = e.key;
      char[] chars = getKeySet(target, e);
      if (chars == null) return;
      
      int curTime = Vm.getTimeStamp();
      int elapsed = curTime - lastTime;
      lastTime = curTime;
      if (curKey != key || elapsed > MAX_DELAY) // changed key or took too long?
      {
         curIdx = 0;
         curKey = key;
      }
      else
      if (elapsed < MAX_DELAY)
      {
         if (++curIdx == chars.length)
            curIdx = 0;
         backspace.target = target;
         target.postEvent(backspace);
      }
      e.key = chars[curIdx];
      e.consumed = true;
   }
}
