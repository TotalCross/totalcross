// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.math.BigDecimal;

import totalcross.sys.Settings;

/**
 * Parses legacy desktop launcher arguments into an incremental runtime config.
 */
final class LauncherArgumentParser {
  private LauncherArgumentParser() {
  }

  static LauncherParsedConfig parse(LauncherConfig config, boolean application, int fallbackWidth, int fallbackHeight)
      throws InvalidArgumentException {
    String[] args = config.getLauncherArgs();
    LauncherParsedConfig result = new LauncherParsedConfig(config.getMainWindowClass());
    int n = args.length;
    int i = 0;
    try {
      for (i = 0; i < n; i++) {
        if (args[i].equalsIgnoreCase("/fontsize")) {
          result.userFontSize = toInt(args[++i]);
        } else if (args[i].equalsIgnoreCase("/dataPath")) {
          result.dataPath = args[++i];
          System.out.println("Data path is " + result.dataPath);
        } else if (args[i].equalsIgnoreCase("/scr")) {
          parseScreen(result, args[++i]);
          System.out.println("Screen is " + result.width + "x" + result.height + "x" + result.bpp);
        } else if (args[i].equalsIgnoreCase("/fullscreen")) {
          result.fullscreen = true;
        } else if (args[i].equalsIgnoreCase("/safeAreaPortrait")) {
          result.insetsPortrait = parseInsets(args[++i],
              "Argument /safeAreaPortrait expects 4 comma separated values in the following format: top,left,bottom,right");
        } else if (args[i].equalsIgnoreCase("/safeAreaLandscape")) {
          result.insetsLandscape = parseInsets(args[++i],
              "Argument /safeAreaLandscape expects 4 comma separated values in the following format: top,left,bottom,right");
        } else if (args[i].equalsIgnoreCase("/r")) {
          ++i;
        } else if (args[i].equalsIgnoreCase("/pos")) {
          String[] scr = tokenizeString(args[++i].toLowerCase(), ',');
          if (scr.length == 1) {
            throw new Exception();
          }
          result.x = toInt(scr[0]);
          result.y = toInt(scr[1]);
        } else if (args[i].equalsIgnoreCase("/cmdline")) {
          result.commandLine = "";
          while (++i < n) {
            result.commandLine += args[i] + " ";
          }
          result.commandLine = result.commandLine.trim();
          System.out.println("Command line is '" + result.commandLine + "'");
        } else if (args[i].equalsIgnoreCase("/uiStyle")) {
          result.uiStyle = parseUiStyle(args[++i]);
          System.out.println("UI style is " + result.uiStyle);
        } else if (args[i].equalsIgnoreCase("/penlessDevice")) {
          result.keyboardFocusTraversable = true;
          System.out.println("Penless device is on");
        } else if (args[i].equalsIgnoreCase("/fingertouch")) {
          result.fingerTouch = true;
          System.out.println("Finger touch is on");
        } else if (args[i].equalsIgnoreCase("/unmovablesip")) {
          result.unmovableSIP = true;
          System.out.println("Unmovable SIP is on");
        } else if (args[i].equalsIgnoreCase("/geofocus")) {
          result.geographicalFocus = true;
          result.keyboardFocusTraversable = true;
          System.out.println("Geographical focus is on");
        } else if (args[i].equalsIgnoreCase("/virtualKeyboard")) {
          result.virtualKeyboard = true;
          System.out.println("Virtual keyboard is on");
        } else if (args[i].equalsIgnoreCase("/bpp")) {
          result.bpp = toInt(args[++i]);
          if (result.bpp != 8 && result.bpp != 16 && result.bpp != 24 && result.bpp != 32) {
            throw new Exception();
          }
          System.out.println("Bpp is " + result.bpp);
        } else if (args[i].equalsIgnoreCase("/scale")) {
          BigDecimal scaleDecimal = new BigDecimal(args[++i]);
          if (scaleDecimal.compareTo(BigDecimal.ZERO) < 0 || scaleDecimal.compareTo(BigDecimal.valueOf(8)) > 0) {
            throw new Exception();
          }
          result.scaleValue = scaleDecimal.doubleValue();
          System.out.println("Scale is " + result.scaleValue);
        } else if (args[i].equalsIgnoreCase("/fastscale")) {
          result.fastScale = true;
        } else if (args[i].equalsIgnoreCase("/showmousepos")) {
          result.showMousePosition = true;
        } else if (args[i].equalsIgnoreCase("/demo")) {
          result.demo = true;
        } else if (args[i].equalsIgnoreCase("/density")) {
          BigDecimal densityDecimal = new BigDecimal(args[++i]);
          if (densityDecimal.compareTo(BigDecimal.ZERO) <= 0 || densityDecimal.compareTo(BigDecimal.valueOf(4)) > 0) {
            throw new Exception();
          }
          result.densityValue = densityDecimal.doubleValue();
        } else if (args[i].equalsIgnoreCase("/dbginfo")) {
          result.showDebugMessages = true;
        } else {
          throw new Exception();
        }
      }
    } catch (Exception e) {
      throw new InvalidArgumentException(i, argumentAt(args, i), fullCommandLine(args), e);
    }

    if (result.width == -1 || result.height == -1) {
      if (application) {
        result.width = 320;
        result.height = 568;
      } else {
        result.width = fallbackWidth;
        result.height = fallbackHeight;
      }
    }

    result.width *= result.densityValue;
    result.height *= result.densityValue;
    scaleInsets(result.insetsPortrait, result.densityValue);
    scaleInsets(result.insetsLandscape, result.densityValue);

    if (result.scaleValue == -1) {
      Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment()
          .getDefaultScreenDevice()
          .getDefaultConfiguration()
          .getBounds();

      double useableArea = 0.88;
      int viewportW = (int) (result.width / result.densityValue);
      int viewportH = (int) (result.height / result.densityValue);
      double maxRatio = Math.max((double) viewportW / r.width, (double) viewportH / r.height);
      if (maxRatio > useableArea) {
        result.scaleValue = useableArea / maxRatio;
      }
    }
    result.scale = Math.abs(result.scaleValue) / result.densityValue;
    return result;
  }

  private static void parseScreen(LauncherParsedConfig result, String value) throws Exception {
    if (value.equalsIgnoreCase("win32")) {
      result.width = 240;
      result.height = 320;
      result.bpp = 24;
    } else if (value.equalsIgnoreCase("iPhone")) {
      result.width = 393;
      result.height = 852;
      result.bpp = 24;
      result.densityValue = 3;
      result.insetsPortrait = new totalcross.ui.Insets(59, 0, 34, 0);
      result.insetsLandscape = new totalcross.ui.Insets(0, 59, 21, 59);
    } else if (value.equalsIgnoreCase("iPhoneSE")) {
      result.width = 375;
      result.height = 667;
      result.bpp = 24;
      result.densityValue = 2;
    } else if (value.equalsIgnoreCase("ipad")) {
      result.width = 768;
      result.height = 1024;
      result.bpp = 24;
      result.densityValue = 2;
    } else if (value.equalsIgnoreCase("android")) {
      result.width = 360;
      result.height = 592;
      result.bpp = 24;
      result.densityValue = 2;
    } else {
      String[] scr = tokenizeString(value.toLowerCase(), 'x');
      if (scr.length == 1) {
        throw new Exception();
      }
      result.width = toInt(scr[0]);
      result.height = toInt(scr[1]);
      if (scr.length == 3) {
        result.bpp = toInt(scr[2]);
      }
    }
  }

  private static totalcross.ui.Insets parseInsets(String value, String message) throws Exception {
    String[] scr = tokenizeString(value.toLowerCase(), ',');
    if (scr.length != 4) {
      throw new Exception(message);
    }
    return new totalcross.ui.Insets(toInt(scr[0]), toInt(scr[1]), toInt(scr[2]), toInt(scr[3]));
  }

  private static int parseUiStyle(String value) throws Exception {
    if (value.equalsIgnoreCase("Flat")) {
      return Settings.FLAT_UI;
    } else if (value.equalsIgnoreCase("Vista")) {
      return Settings.VISTA_UI;
    } else if (value.equalsIgnoreCase("Android")) {
      return Settings.ANDROID_UI;
    } else if (value.equalsIgnoreCase("Holo")) {
      return Settings.HOLO_UI;
    } else if (value.equalsIgnoreCase("Material")) {
      return Settings.MATERIAL_UI;
    }
    throw new Exception();
  }

  private static void scaleInsets(totalcross.ui.Insets insets, double density) {
    if (insets != null) {
      insets.top *= density;
      insets.left *= density;
      insets.bottom *= density;
      insets.right *= density;
    }
  }

  private static int toInt(String s) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return 0;
    }
  }

  private static String[] tokenizeString(String string, char c) {
    java.util.StringTokenizer st = new java.util.StringTokenizer(string, "" + c);
    String[] ret = new String[st.countTokens()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = st.nextToken();
    }
    return ret;
  }

  private static String argumentAt(String[] args, int index) {
    return index >= 0 && index < args.length ? args[index] : "";
  }

  private static String fullCommandLine(String[] args) {
    String s = "";
    for (int i = 0; i < args.length; i++) {
      s += " " + args[i];
    }
    return s.trim();
  }

  static final class InvalidArgumentException extends Exception {
    private final int index;
    private final String argument;
    private final String fullCommandLine;

    InvalidArgumentException(int index, String argument, String fullCommandLine, Throwable cause) {
      super(cause.getMessage(), cause);
      this.index = index;
      this.argument = argument;
      this.fullCommandLine = fullCommandLine;
    }

    int getIndex() {
      return index;
    }

    String getArgument() {
      return argument;
    }

    String getFullCommandLine() {
      return fullCommandLine;
    }
  }
}
