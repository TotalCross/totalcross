// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package lambda.repro;

import java.util.HashMap;
import java.util.Map;

import totalcross.ui.MainWindow;

public class GraphicsReferenceRepro extends MainWindow {
  @Override
  public void initUI() {
    String caseName = "inherited getGraphics method reference";
    System.out.println("[CASE] Lambda repro - " + caseName);
    try {
      Map<String, GraphicsAction> map = new HashMap<>();
      map.put("1", this::getGraphics);
      map.get("1").execute();
      System.out.println("[PASS] Lambda repro - " + caseName);
    } catch (Throwable failure) {
      System.out.println("[FAIL] Lambda repro - " + caseName + " - " + failure.getClass().getName() + ": "
          + failure.getMessage());
      failure.printStackTrace();
    }
    exit(0);
  }
}
