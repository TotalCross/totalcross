package tc.samples.app.store;

import totalcross.io.File;
import totalcross.res.Resources;
import totalcross.sys.SpecialKeys;
import totalcross.ui.Bar;
import totalcross.ui.Container;
import totalcross.ui.ScrollContainer;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.KeyListener;
import totalcross.ui.image.BulkImageLoader;
import totalcross.ui.image.Image;

/** The products list container */

public class PSProductList extends Container implements PSConstants
{
  Bar bar;
  Container c;
  BulkImageLoader bil;

  @Override
  public void initUI()
  {
    try
    {
      setBackColor(CNT_BACKCOLOR);
      setInsets(0,0,2,2); // dont let the bar and the list glue together

      // place bar at top
      bar = new Bar("Products List");
      bar.titleAlign = CENTER;
      Image ic = Resources.menu.getCopy();
      ic.applyColor2(0); // change button to black
      bar.addButton(ic, false); 
      bar.setForeColor(BAR_COLOR);
      bar.ignoreInsets = true;
      add(bar, LEFT,TOP,FILL,PREFERRED);

      // place the list at bottom
      c = new ScrollContainer(false,true);
      c.setBackColor(LIST_BACKCOLOR);
      add(c, LEFT,AFTER,FILL,FILL);

      // create the image loader
      bil = new BulkImageLoader(3, 10, TCProductStore.imagePath, new File(TCProductStore.imagePath).listFiles());
      bil.start();

      reload();

      Window.keyHook = new KeyListener() 
      {
        @Override
        public void keyPressed(KeyEvent e) {}
        @Override
        public void actionkeyPressed(KeyEvent e) {}
        @Override
        public void specialkeyPressed(KeyEvent e)
        {
          if (e.key == SpecialKeys.ESCAPE)
          {
            e.consumed = true;
            back();
          }
        }
      };

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void reload()
  {
    // add all products
    c.removeAll();
    try
    {
      for (int i = 0, n = bil.arqs.length; i < n; )
      {
        c.add(new PSProduct(bil.new DynImage(i),"Cushion "+i, 100+i),PARENTSIZE+25, AFTER+50, PARENTSIZE+46,PREFERRED);
        i++;
        if (i < n) {
          c.add(new PSProduct(bil.new DynImage(i),"Cushion "+i, 100+i),PARENTSIZE+75, SAME, SAME, SAME);
        }
        i++;
      }
    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }

  @Override
  public void onEvent(Event e)
  {
    // go back to the login page
    if (e.type == ControlEvent.PRESSED && e.target == bar){
      back();
    }
  }

  private void back()
  {
    new PSlogin().swapToTopmostWindow();         
  }
}
