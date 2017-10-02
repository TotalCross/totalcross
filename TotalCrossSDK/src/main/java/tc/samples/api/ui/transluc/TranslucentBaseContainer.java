package tc.samples.api.ui.transluc;

import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.ImageControl;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;

public class TranslucentBaseContainer extends Container {
  private ImageControl icb;
  private String[] backs = { "ui/images/back1.jpg", "ui/images/back2.jpg", "ui/images/back3.jpg", "ui/images/back4.jpg",
      "ui/images/back5.jpg" };
  private Image[] imgback = new Image[backs.length];

  @Override
  public void initUI() {
    setFont(Font.getFont(false, Font.NORMAL_SIZE * 125 / 100));
    add(icb = new ImageControl(), LEFT, TOP, FILL, FILL);
    transitionEffect = TRANSITION_FADE;
  }

  protected void setBackgroundImage(int id) {
    try {
      Image img = imgback[id];
      if (img == null) {
        img = imgback[id] = new Image(backs[id]).smoothScaledFixedAspectRatio(Math.max(width, height), true);
      }
      icb.setImage(img);
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }

  protected Image getBtnImage(String name) throws Exception {
    return new Image(name).smoothScaledFixedAspectRatio(fmH * 2, true);
  }

  protected void setTranslucentProps(Control t) {
    t.setForeColor(Color.WHITE);
    t.textShadowColor = 0x444444;
    t.alphaValue = 128;
    t.transparentBackground = true;
  }

  protected Edit createTransEdit(String cap) {
    Edit ed = new Edit();
    ed.caption = cap;
    ed.captionColor = Color.WHITE;
    if (uiMaterial) {
      ed.setBackColor(Color.WHITE);
    }
    ed.setForeColor(Color.WHITE);
    ed.textShadowColor = 0x444444;
    if (!uiMaterial) {
      ed.alphaValue = 128;
    }
    return ed;
  }

  protected Button createTransButton(String cap, String img) throws Exception {
    Button b = new Button(cap, getBtnImage(img), TOP, fmH / 6);
    b.setTranslucent(TranslucentShape.ROUND);
    return b;
  }

  protected Button createTransBarButton(String img) throws Exception {
    Button b = new Button(getBtnImage(img));
    b.setTranslucent(TranslucentShape.RECT);
    b.setBackColor(0xAAAAAA);
    return b;
  }

  protected void goback() {
    MenuScreen.getInstance().swapToTopmostWindow();
  }

  protected void show() {
    this.swapToTopmostWindow();
  }
}
