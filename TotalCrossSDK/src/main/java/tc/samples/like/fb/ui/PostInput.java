package tc.samples.like.fb.ui;

import tc.samples.like.fb.FBUtils;
import tc.samples.like.fb.FaceBookUI;
import tc.samples.like.fb.db.FBDB;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.Edit;
import totalcross.ui.ImageControl;
import totalcross.ui.Ruler;
import totalcross.ui.Toast;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;

public class PostInput extends FBContainer {
  private Edit med;
  private Button btStat, btPhot, btChIn;
  private FBPosts posts;

  public PostInput(FBPosts posts) {
    this.posts = posts;
  }

  @Override
  public void initUI() {
    setBackColor(Color.WHITE);
    setBorderStyle(BORDER_SIMPLE);
    borderColor = BORDER;

    try {
      add(new ImageControl(FaceBookUI.defaultPhoto.smoothScaledFixedAspectRatio(fmH * 2, true)), LEFT + 50, TOP + 50);
    } catch (Throwable t) {
      Container c = new Container();
      c.setBackColor(CNT_BACK);
      add(c, LEFT + 50, TOP + 50, FONTSIZE + 200, FONTSIZE + 200);
    }
    med = createEdit("What's on your mind?");
    add(med, AFTER + 50, SAME, FILL - 100, PREFERRED);

    add(btStat = createButton("STATUS", FBImages.status, fmH), LEFT, BOTTOM, PARENTSIZE - 3, FONTSIZE + 150);
    add(btPhot = createButton("PHOTO", FBImages.photo, fmH), AFTER, BOTTOM, PARENTSIZE - 3, FONTSIZE + 150);
    add(btChIn = createButton("CHECK-IN", FBImages.checkin, fmH), AFTER, BOTTOM, FILL, FONTSIZE + 150);

    add(createRuler(Ruler.HORIZONTAL), LEFT + 100, BEFORE, FILL - 100, 1);
  }

  @Override
  public int getPreferredHeight() {
    return fmH * 9 / 2;
  }

  @Override
  public void onEvent(Event e) {
    try {
      switch (e.type) {
      case ControlEvent.PRESSED:
        if (e.target == btStat) {
          if (med.getTrimmedLength() == 0) {
            Toast.show("Fill what's on your mind and press this button to post", 2000);
          } else if (FBDB.db.addPost(med.getText())) {
            posts.reload();
          }
        } else if (e.target == btPhot) {
          Image photo = FBUtils.takePhoto();
          if (photo != null && FBDB.db.addPost(photo)) {
            posts.reload();
          }
        } else if (e.target == btChIn) {
          String coords = FBUtils.getCoords();
          if (coords != null && FBDB.db.addPost(coords)) {
            posts.reload();
          }
        }
        break;
      }
    } catch (Throwable t) {
      FBUtils.logException(t);
    }
  }
}