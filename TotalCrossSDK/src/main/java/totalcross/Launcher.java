// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import tc.preview.PreviewFrameSink;

/** Historical entry point retained as a compatibility facade. */
public final class Launcher extends tc.simulator.Launcher {
  private double toScale = -1;
  private int toBpp = 24;

  public Launcher() {
    super();
  }

  public class UserFont extends tc.simulator.Launcher.UserFont {
    protected UserFont(String fontName, String suffix, int size, totalcross.ui.font.Font base) throws Exception {
      super(fontName, suffix, size, base);
    }

    protected UserFont(String fontName, String suffix) throws Exception {
      super(fontName, suffix);
    }
  }

  @Override
  public void updateScreen() {
    if (toScale != -1 || super.toScale == -1) {
      super.toScale = toScale;
    }
    if (toBpp != 24 || super.toBpp == 24) {
      super.toBpp = toBpp;
    }
    super.updateScreen();
  }

  /** Historical spelling retained for source compatibility. */
  public void setPreviewFrameConsumer(PreviewFrameSink sink) {
    setPreviewFrameSink(sink);
  }

  public static void main(String[] args) {
    tc.simulator.Launcher.main(args);
  }
}
