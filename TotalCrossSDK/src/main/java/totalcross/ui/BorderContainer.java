package totalcross.ui;

import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;

/** Class used to show a border with a title.
 * See the tc.Help sources to know how to use it.
 */

public class BorderContainer extends Container
{
  public boolean fillW;
  String title;
  Label l;
  public int borderColor = 0xAAAAAA;
  public int borderThickness = 2;
  public BorderContainer(String title)
  {
    this.title = title;
  }

  @Override
  public void initUI()
  {
    setInsets(fmH/2,fmH/2,fmH+fmH/4,fmH);
    add(l = new Label(title),LEFT,0,PREFERRED,PREFERRED+25); l.ignoreInsets = true; l.vAlign = TOP;
    setBackColor(Color.WHITE);
  }

  @Override
  public int getPreferredWidth()
  {
    return fillW ? FILL : WILL_RESIZE;
  }
  @Override
  public int getPreferredHeight()
  {
    return WILL_RESIZE;
  }

  public void finish()
  {
    if (fillW){
      resizeHeight();
    }else {
      resize();
    }
    setW = width;
    setH = height;
    l.setRect(CENTER,TOP,PREFERRED,PREFERRED);
    l.setSet(CENTER,TOP);
  }

  @Override
  public void onPaint(Graphics g)
  {
    super.onPaint(g);
    g.foreColor = getForeColor();
    g.backColor = getBackColor();
    g.drawWindowBorder(0,fmH/2,width,height-fmH/2,0,0,borderColor,g.backColor,g.backColor,g.backColor,borderThickness,false);
    int w = fm.stringWidth(title);
    int x = (width-w)/2;
    g.fillRect(x-fmH/2, 0, w+fmH,fmH);
  }
}
