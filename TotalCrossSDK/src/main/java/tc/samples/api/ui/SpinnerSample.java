package tc.samples.api.ui;

import tc.samples.api.BaseContainer;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Label;
import totalcross.ui.Radio;
import totalcross.ui.RadioGroupController;
import totalcross.ui.Spinner;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.util.Random;

public class SpinnerSample extends BaseContainer {
  private Spinner sp;
  private Button bt;
  private Label l;
  private RadioGroupController rg;
  private Image triplex;

  @Override
  public void initUI() {
    try {
      super.initUI();
      rg = new RadioGroupController();

      l = new Label(
          "Note that we are blocked in a loop, like if we're downloading something from internet or other kind of processing.");
      l.autoSplit = true;
      add(l, LEFT, TOP, FILL, PREFERRED);
      l.setVisible(false);

      Radio r;
      add(r = new Radio(" iPhone", rg), LEFT, AFTER, PARENTSIZE + 33, PREFERRED);
      r.leftJustify = true;
      add(r = new Radio(" Android", rg), AFTER, SAME, SAME, PREFERRED);
      r.leftJustify = true;
      add(r = new Radio(" Sync", rg), AFTER, SAME, SAME, PREFERRED);
      r.leftJustify = true;
      add(r = new Radio(" Custom (triplex.gif)", rg), LEFT, AFTER, FILL, PREFERRED);
      r.leftJustify = true;
      rg.setSelectedIndex(0);

      triplex = new Image("ui/images/triplex.gif");
      sp = new Spinner(Spinner.IPHONE);
      add(sp, CENTER, AFTER + fmH * 2, FONTSIZE + 200, FONTSIZE + 200);

      add(bt = new Button("Start"), CENTER, TOP + gap, PARENTSIZE + 50, PREFERRED);
      add(new Label("(a random color is used at each Start)"), CENTER, BOTTOM);
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }

  private static Random rr = new Random();

  @Override
  public void onEvent(Event e) {
    if (e.type == ControlEvent.PRESSED) {
      if (e.target instanceof Radio) {
        int t = rg.getSelectedIndex();
        switch (t) {
        case 0:
        case 1:
        case 2:
          sp.setType(t + 1);
          break;
        case 3:
          sp.setImage(triplex);
          break;
        }
      } else if (e.target == bt) {
        if (!sp.isRunning()) {
          sp.setForeColor(Color.getRandomColor(rr));
          bt.setVisible(false);
          l.setVisible(true);
          repaintNow();
          int end = Vm.getTimeStamp() + 5000;
          while (Vm.getTimeStamp() < end) {
            // Ygor: I deprecated the update() method because it is synchronous and blocking. 
            // The start() and stop() methods should be used instead.
            sp.update(); // this makes the magic that updates the spinner
            // you may do other processing here...
          }
          onRemove();
        }
      }
    }
  }

  @Override
  public void onRemove() // stop spinners at end
  {
    l.setVisible(false);
    bt.setVisible(true);
  }
}
