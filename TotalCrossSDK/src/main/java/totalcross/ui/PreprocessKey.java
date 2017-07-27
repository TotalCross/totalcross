package totalcross.ui;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Vm;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;

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
  public static int MAX_DELAY = 750;

  public abstract void preprocess(Control target, KeyEvent ke);

  private int curIdx=-1,curKey;
  private int lastTime;
  private TimerEvent te;
  private Control target;

  protected char[] getKeySet(Control target, KeyEvent e)
  {
    return null;
  }

  protected void handleKeypadPress(Control target, KeyEvent e)
  {
    if (Settings.onJavaSE){
      e.modifiers = SpecialKeys.SYSTEM;
    }
    this.target = target;
    int key = e.key;
    char[] chars = getKeySet(target, e);
    if (chars == null){
      return;
    }

    int curTime = Vm.getTimeStamp();
    int elapsed = curTime - lastTime;
    lastTime = curTime;
    if (curKey != key || elapsed > MAX_DELAY) // changed key or took too long?
    {
      if (target instanceof Edit) {
        ((Edit)target).selectLast = true;
      } else
        if (target instanceof MultiEdit)
        {
          if (curKey != key && te != null) {
            changeCursor(true);
          }
          ((MultiEdit)target).selectLast = true;
        }
      curIdx = 0;
      curKey = key;
      resetTimer();
    }
    else
      if (elapsed < MAX_DELAY)
      {
        resetTimer();
        if (++curIdx == chars.length) {
          curIdx = 0;
        }
        changeCursor(false);
      }

    e.key = chars[curIdx];
    if ((e.modifiers & SpecialKeys.SHIFT) != 0){
      e.key = Convert.toUpperCase((char)e.key);
    }
    e.consumed = true;
  }

  private void changeCursor(boolean advance)
  {
    if (target instanceof Edit)
    {
      Edit ed = (Edit)target;
      int[] pos = ed.getCursorPos();
      ed.persistentSelection = ed.selectLast = !advance;
      ed.setCursorPos(advance ? pos[1] : pos[1]-1, pos[1]);
    }
    else
      if (target instanceof MultiEdit)
      {
        MultiEdit ed = (MultiEdit)target;
        int[] pos = ed.getCursorPos();
        ed.persistentSelection = ed.selectLast = !advance;
        ed.setCursorPos(advance ? pos[1] : pos[1]-1, pos[1]);
      }
  }

  private void resetTimer()
  {
    final MainWindow m = MainWindow.getMainWindow();
    if (te != null){
      te.postpone();
    }else
    {
      te = m.addTimer(MAX_DELAY);
      m.addTimerListener(new TimerListener()
      {
        @Override
        public void timerTriggered(TimerEvent e)
        {
          if (te.triggered)
          {
            m.removeTimer(te);
            te = null;
            changeCursor(true);
          }
        }
      });
    }
  }
}
