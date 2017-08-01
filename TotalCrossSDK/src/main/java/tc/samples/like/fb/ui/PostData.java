package tc.samples.like.fb.ui;

import tc.samples.like.fb.FBUtils;
import tc.samples.like.fb.db.FBDB;
import totalcross.sys.Time;
import totalcross.ui.Container;
import totalcross.ui.ImageControl;
import totalcross.ui.Label;
import totalcross.ui.Ruler;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.util.Date;

public class PostData extends FBContainer
{
  private String name;
  private Label ltext;
  private int likes;
  private Image photo;
  private Time t;

  public PostData(String name, String text, Image photo, int likes, Time t)
  {
    this.name = name;
    if (text != null){
      this.ltext = new Label(text);
    }
    this.photo = photo;
    this.likes = likes;
    this.t = t;
  }

  @Override
  public void initUI()
  {
    try
    {
      setBackColor(Color.WHITE);
      setBorderStyle(BORDER_SIMPLE);
      borderColor = BORDER;

      try
      {
        add(new ImageControl(FBDB.db.getPhoto(name).hwScaledFixedAspectRatio(fmH*2,true)),LEFT+50,TOP+50);         
      }
      catch (Throwable t)
      {
        Container c = new Container();
        c.setBackColor(CNT_BACK);
        add(c,LEFT+50,TOP+50,FONTSIZE+200,FONTSIZE+200);
      }
      Label lname = new Label(name);
      lname.setFont(Font.getFont(true, fmH*8/10));
      add(lname, AFTER+50,SAME);

      Label linfo = new Label(new Date(t)+" at "+t);
      linfo.setFont(Font.getFont(false, fmH*8/10));
      add(linfo, SAME,AFTER);

      if (photo == null) {
        add(ltext,LEFT+50, AFTER+25);
      } else
      {
        ImageControl ic = new ImageControl(photo);
        ic.scaleToFit = true;
        add(ic, LEFT+50,AFTER+25,FILL-50,FONTSIZE+800);
      }

      Label llikes = new Label(likes+" likes");
      llikes.setFont(Font.getFont(false, fmH*8/10));
      add(llikes,LEFT+50,AFTER+25);

      add(createButton("Like", FBImages.like, fmH),LEFT,BOTTOM,PARENTSIZE-3,FONTSIZE+150);
      add(createRuler(Ruler.VERTICAL),AFTER,SAME,1,FILL);
      add(createButton("Comment", FBImages.comment, fmH),AFTER,BOTTOM,PARENTSIZE-3,FONTSIZE+150);
      add(createRuler(Ruler.VERTICAL),AFTER,SAME,1,FILL);
      add(createButton("Share", FBImages.share, fmH),AFTER,BOTTOM,FILL,FONTSIZE+150);

      add(createRuler(Ruler.HORIZONTAL),0,BEFORE,PARENTSIZE+100,1);
    }
    catch (Throwable t)
    {
      FBUtils.logException(t);
    }
  }

  @Override
  public int getPreferredHeight()
  {
    return photo == null ? fmH*6+ltext.getPreferredHeight() : fmH*14;
  }
}