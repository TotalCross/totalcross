// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

/**
 * Parsed launcher configuration produced from legacy command-line arguments.
 */
final class LaunchOptions {
  final String className;
  int userFontSize = Launcher.userFontSize;
  int width = -1;
  int height = -1;
  int bpp = 24;
  int uiStyle = -1;
  int x = -1;
  int y = -1;
  boolean fullscreen;
  boolean demo;
  boolean fastScale;
  boolean keyboardFocusTraversable;
  boolean fingerTouch;
  boolean unmovableSIP;
  boolean geographicalFocus;
  boolean virtualKeyboard;
  boolean showMousePosition;
  boolean showDebugMessages;
  String commandLine = "";
  String dataPath;
  double scaleValue = -1;
  double densityValue = 1;
  double scale;
  totalcross.ui.Insets insetsPortrait;
  totalcross.ui.Insets insetsLandscape;

  LaunchOptions(String className) {
    this.className = className;
  }
}
