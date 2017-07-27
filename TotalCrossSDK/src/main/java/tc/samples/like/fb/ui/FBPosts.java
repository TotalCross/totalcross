package tc.samples.like.fb.ui;

import tc.samples.like.fb.FBConstants;
import tc.samples.like.fb.FBUtils;
import tc.samples.like.fb.db.FBDB;
import totalcross.ui.ScrollContainer;

public class FBPosts extends ScrollContainer implements FBConstants
{
  public FBPosts()
  {
    super(false,true);
    setBackColor(CONTENTH);
  }

  @Override
  public void initUI()
  {
    setInsets(0,0,2,2);
    reload();
  }

  public void reload()
  {
    removeAll();
    try
    {
      add(new PostInput(this),CENTER,TOP+25,PARENTSIZE+96,PREFERRED);
      for (PostData p: FBDB.db.getPosts()) {
        add(p,CENTER,AFTER+25,PARENTSIZE+96,PREFERRED);
      }
    }
    catch (Throwable t)
    {
      FBUtils.logException(t);
    }
  }
}