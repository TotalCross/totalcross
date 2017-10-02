package tc.samples.api.ui.transluc;

import totalcross.ui.Button;
import totalcross.ui.ListBox;
import totalcross.ui.MultiEdit;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;

public class ViewScreen extends TranslucentBaseContainer {
  private static ViewScreen inst;

  public static ViewScreen getInstance() {
    return inst == null ? inst = new ViewScreen() : inst;
  }

  @Override
  public void initUI() {
    try {
      super.initUI();
      setBackgroundImage(4);

      ListBox lb = new ListBox();
      setTranslucentProps(lb);
      add(lb, CENTER, PARENTSIZE + 20, PARENTSIZE + 80, PARENTSIZE + 20);
      lb.add("Item 1");
      lb.add("Item 2");

      MultiEdit me = new MultiEdit();
      setTranslucentProps(me);
      me.transparentBackground = false;
      add(me, CENTER, PARENTSIZE + 50, PARENTSIZE + 80, PARENTSIZE + 20);

      Button t1 = createTransBarButton("ui/images/bt_view.png");
      Button t2 = createTransBarButton("ui/images/bt_back.png");
      t2.addPressListener(new PressListener() {
        @Override
        public void controlPressed(ControlEvent e) {
          goback();
        }
      });

      Button[] bar = { t1, t2 };
      for (int i = 0, n = bar.length; i < n; i++) {
        add(bar[i], i == 0 ? LEFT : AFTER, BOTTOM, PARENTSIZE - n, FONTSIZE + 300);
      }
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }
}
