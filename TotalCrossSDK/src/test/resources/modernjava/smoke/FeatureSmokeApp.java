// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

import totalcross.ui.Label;
import totalcross.ui.MainWindow;

public class FeatureSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    add(new Java4FeatureSmokeTest(), LEFT, TOP, FILL, 28);
    add(new Java5FeatureSmokeTest(), LEFT, AFTER, FILL, 28);
    add(new Java6FeatureSmokeTest(), LEFT, AFTER, FILL, 28);
    add(new Java7FeatureSmokeTest(), LEFT, AFTER, FILL, 28);
    add(new Java8FeatureSmokeTest(), LEFT, AFTER, FILL, 28);
    add(new Java9FeatureSmokeTest(), LEFT, AFTER, FILL, 28);
    add(new Java10FeatureSmokeTest(), LEFT, AFTER, FILL, 28);
    add(new Java11FeatureSmokeTest(), LEFT, AFTER, FILL, 28);
    add(new Java14FeatureSmokeTest(), LEFT, AFTER, FILL, 28);
    add(new Java15FeatureSmokeTest(), LEFT, AFTER, FILL, 28);
    add(new Java16FeatureSmokeTest(), LEFT, AFTER, FILL, 28);
    add(new Java17FeatureSmokeTest(), LEFT, AFTER, FILL, 28);

    System.out.println("[PASS] Feature smoke app - all suites scheduled");
    add(new Label("Feature smoke app OK"), LEFT + 8, AFTER + 4);

    exit(0);
  }
}
