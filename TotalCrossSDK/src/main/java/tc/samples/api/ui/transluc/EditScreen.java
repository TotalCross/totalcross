package tc.samples.api.ui.transluc;

import totalcross.ui.Button;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;

public class EditScreen extends TranslucentBaseContainer {
  private static EditScreen inst;

  public static EditScreen getInstance() {
    return inst == null ? inst = new EditScreen() : inst;
  }

  @Override
  public void initUI() {
    try {
      super.initUI();
      setBackgroundImage(2);

      add(createTransEdit("Description"), CENTER, PARENTSIZE + 25, PARENTSIZE + 80, FONTSIZE + 200);
      add(createTransEdit("Price"), CENTER, PARENTSIZE + 40, PARENTSIZE + 80, FONTSIZE + 200);

      Button t1 = createTransBarButton("ui/images/bt_add.png");
      Button t2 = createTransBarButton("ui/images/bt_save.png");
      Button t3 = createTransBarButton("ui/images/bt_delete.png");
      Button t4 = createTransBarButton("ui/images/bt_back.png");
      t4.addPressListener(new PressListener() {
        @Override
        public void controlPressed(ControlEvent e) {
          goback();
        }
      });

      Button[] bar = { t1, t2, t3, t4 };
      for (int i = 0, n = bar.length; i < n; i++) {
        add(bar[i], i == 0 ? LEFT : AFTER, BOTTOM, PARENTSIZE - n, FONTSIZE + 300);
      }
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }
}
