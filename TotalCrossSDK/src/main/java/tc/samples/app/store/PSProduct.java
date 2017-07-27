package tc.samples.app.store;

import totalcross.sys.Convert;
import totalcross.ui.Container;
import totalcross.ui.Label;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.BulkImageLoader;
import totalcross.ui.image.BulkImageLoader.DynImage;

/** A container that shows a single product, with an image, a description and a price. */

public class PSProduct extends Container implements PSConstants
{
  private String description;
  private BulkImageLoader.DynImage photo;
  private double price;

  public PSProduct(DynImage photo, String description, double price)
  {
    this.description = description;
    this.price = price;
    this.photo = photo;
    setPressColor(PROD_PRESSCOLOR);
  }

  @Override
  public void initUI()
  {
    Label l;
    setBackColor(CNT_BACKCOLOR);
    // add photo image at top
    add(photo, LEFT+50,TOP+50,FILL-50,FONTSIZE+300);
    // add description after
    add(l = new Label(description),LEFT+50,AFTER+25); l.transparentBackground = true;
    // and then the price
    add(l = new Label(Convert.toString(price, 2),LEFT,Color.RED,true),RIGHT-50,AFTER); l.transparentBackground = true; 
  }

  /** The preferred height is 6 times the font height */
  @Override
  public int getPreferredHeight()
  {
    return fmH*6;
  }

  @Override
  public void onEvent(Event e)
  {
    if (e.target == this && e.type == ControlEvent.PRESSED)
    {
      // do something 
    }
  }
}
