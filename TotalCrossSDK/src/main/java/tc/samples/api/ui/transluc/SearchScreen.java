package tc.samples.api.ui.transluc;

import totalcross.ui.Button;
import totalcross.ui.Check;
import totalcross.ui.Label;
import totalcross.ui.Radio;
import totalcross.ui.RadioGroupController;
import totalcross.ui.Switch;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Color;

public class SearchScreen extends TranslucentBaseContainer {
  private static SearchScreen inst;

  public static SearchScreen getInstance() {
    return inst == null ? inst = new SearchScreen() : inst;
  }

  private Check ch;
  private Radio r1, r2;

  @Override
  public void initUI() {
    super.initUI();
    try {
      transparentBackground = true;

      setBackgroundImage(1);

      add(createTransEdit("Filter"), CENTER, PARENTSIZE + 15, PARENTSIZE + 80, FONTSIZE + 200);

      final Switch sw = new Switch(false);
      setTranslucentProps(sw);
      add(sw, SAME, PARENTSIZE + 30, fmH * 2, PREFERRED);
      sw.addPressListener(new PressListener() {
        @Override
        public void controlPressed(ControlEvent e) {
          boolean b = sw.isOn();
          ch.setVisible(b);
          r1.setVisible(b);
          r2.setVisible(b);
        }
      });
      Label l = new Label(" Enable options");
      setTranslucentProps(l);
      add(l, AFTER, CENTER_OF);

      ch = new Check(" Case insensitive");
      setTranslucentProps(ch);
      ch.checkColor = Color.YELLOW;
      add(ch, SAME, PARENTSIZE + 45, sw);

      RadioGroupController rg = new RadioGroupController();
      r1 = new Radio("Ascending", rg);
      setTranslucentProps(r1);
      r1.checkColor = Color.YELLOW;
      add(r1, SAME, PARENTSIZE + 55);
      r2 = new Radio("Descending", rg);
      setTranslucentProps(r2);
      r2.checkColor = Color.YELLOW;
      add(r2, SAME, PARENTSIZE + 65);

      ch.setVisible(false);
      r1.setVisible(false);
      r2.setVisible(false);

      Button t1 = createTransBarButton("ui/images/bt_search.png");
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
