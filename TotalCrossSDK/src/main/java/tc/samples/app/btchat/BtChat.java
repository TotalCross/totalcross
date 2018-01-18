package tc.samples.app.btchat;

import totalcross.ui.MainWindow;

public class BtChat extends MainWindow {
  @Override
  public void initUI() {
    swap(new FirstScreen());
  }
}
