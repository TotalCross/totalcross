// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.device.gpiod;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import java.util.HashMap;
import java.util.Map;

public class GpiodChip
{

  Object handle;

  Map<Integer, GpiodLine> linesMap = new HashMap<>();

  private GpiodChip() {}

  @ReplacedByNativeOnDeploy public static GpiodChip open(int bank)
  {
    return null;
  }

  public GpiodLine line(int line)
  {
    GpiodLine gline = linesMap.get(line);
    if (gline == null) {
      gline = GpiodLine.open(this, line);
      linesMap.put(line, gline);
    }
    return gline;
  }
}
