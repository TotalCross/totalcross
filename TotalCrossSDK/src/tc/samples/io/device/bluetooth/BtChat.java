package tc.samples.io.device.bluetooth;

import totalcross.sys.*;
import totalcross.ui.MainWindow;

public class BtChat extends MainWindow
{
   static
   {
      Settings.useNewFont = true;
   }

   public void initUI()
   {
      swap(new FirstScreen());
   }
}
