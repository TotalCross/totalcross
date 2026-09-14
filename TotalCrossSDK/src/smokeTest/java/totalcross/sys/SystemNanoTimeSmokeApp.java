// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

import totalcross.ui.MainWindow;

/** macOS smoke for the converted System.nanoTime() native bridge. */
public class SystemNanoTimeSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    try {
      long t0 = System.nanoTime();
      Vm.sleep(20);
      long t1 = System.nanoTime();
      long delta = t1 - t0;
      require(t1 > t0, "nanoTime did not increase");
      require(delta >= 10_000_000L, "nanoTime delta below 10 ms: " + delta);
      System.out.println("fixture=SystemNanoTimeSmokeApp,t0=" + t0 + ",t1=" + t1
          + ",deltaNanos=" + delta + ",overallPass=true");
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("fixture=SystemNanoTimeSmokeApp,overallPass=false,error="
          + failure.getMessage());
      failure.printStackTrace();
      System.out.flush();
      exit(1);
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
