// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

import totalcross.ui.MainWindow;

/** Native macOS smoke for independent standard streams and DebugConsole routing. */
public class StandardStreamsSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    try {
      System.out.print("standard-out-pre-erase:");
      System.out.println("discard");
      System.err.print("standard-err-pre-erase:");
      System.err.println("discard");
      System.out.flush();
      System.err.flush();

      Vm.debug("legacy-pre-erase");
      Vm.debug(Vm.ERASE_DEBUG);
      System.out.println(Vm.ERASE_DEBUG);
      System.out.println("standard-out-post-erase");
      System.err.println("standard-err-post-erase");
      System.out.flush();
      System.err.flush();

      Vm.disableDebug = true;
      Vm.debug("legacy-disabled");
      System.out.println("standard-out-disabled");
      System.err.println("standard-err-disabled");
      System.out.flush();
      System.err.flush();

      Vm.disableDebug = false;
      Vm.debug("legacy-post-erase");
      System.out.flush();
      System.err.flush();
      System.out.println("fixture=StandardStreamsSmokeApp,overallPass=true");
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      Vm.disableDebug = false;
      System.err.println("fixture=StandardStreamsSmokeApp,overallPass=false,error="
          + failure.getMessage());
      failure.printStackTrace();
      System.err.flush();
      exit(1);
    }
  }
}
