package totalcross.util;

import totalcross.sys.Settings;
import totalcross.ui.Control;

public abstract class UnitsConverter {

  public static int toPixels(int value) {
    if ((Control.DP - Control.RANGE) <= value && value <= (Control.DP + Control.RANGE)) {
      return (int) (Settings.screenDensity * (value - Control.DP));
    }
    return value;
  }
}
