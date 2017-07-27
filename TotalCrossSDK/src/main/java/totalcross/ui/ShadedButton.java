package totalcross.ui;

import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;

public class ShadedButton extends Button
{
  String text;
  Image back0, back,fore;
  int fcolor,bcolor;
  int shadeDist=3;

  public ShadedButton(String text, Image back0, int fcolor, int bcolor)
  {
    super();
    this.text = text;
    this.back0 = back0;
    this.fcolor = fcolor;
    this.bcolor = bcolor;
  }

  @Override
  public void onBoundsChanged(boolean changed)
  {
    super.onBoundsChanged(changed);
    if (width > 0){
      try
      {
        back = back0.getSmoothScaledInstance(width-shadeDist,height-shadeDist);
        fore = back0.getSmoothScaledInstance(width-shadeDist,height-shadeDist);
        fore.applyColor2(bcolor);
        back.alphaMask = 200;
      }
      catch (Exception e) {e.printStackTrace();}
    }
  }

  @Override
  public int getPreferredWidth()
  {
    return fm.stringWidth(text)+fmH;
  }

  @Override
  public int getPreferredHeight()
  {
    return fmH+Edit.prefH;
  }

  @Override
  public void onPaint(Graphics g)
  {
    g.drawImage(back,shadeDist,shadeDist);
    int k = armed?shadeDist:0;
    g.drawImage(fore,k,k);
    g.foreColor = fcolor;
    g.drawText(text,(width-fm.stringWidth(text)-shadeDist)/2+k, (height-fmH)/2-shadeDist+k);
  }      
}
