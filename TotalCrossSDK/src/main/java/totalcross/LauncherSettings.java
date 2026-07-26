// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import tc.tools.JarClassPathLoader;
import tc.tools.deployer.DeploySettings;
import totalcross.io.IOException;
import totalcross.io.RandomAccessStream;
import totalcross.io.Stream;
import totalcross.preview.AppletPreviewSurface;
import totalcross.preview.AwtWindowBackend;
import totalcross.preview.PreviewRuntime;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.Container;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.MultiTouchEvent;
import totalcross.ui.event.PenEvent;
import totalcross.util.Hashtable;
import totalcross.util.IntHashtable;
import totalcross.util.zip.TCZ;

@SuppressWarnings({"deprecation", "removal"})
abstract class LauncherSettings extends LauncherStorage {
  String getDefaultCrid(String name) {
    if (name == null) {
      return null;
    }

    if (name.indexOf('.') != -1) {
      name = name.substring(name.lastIndexOf('.') + 1);
    }
    int i;
    int n = name.length();
    int hash = 0;
    byte[] creat = new byte[4];
    for (i = 0; i < n; i++) {
      hash += (byte) name.charAt(i);
    }
    for (i = 0; i < 4; i++) {
      creat[i] = (byte) ((hash % 26) + 'a');
      if ((hash & 64) > 0) {
        creat[i] += ('A' - 'a');
      }
      hash = hash / 2;
    }
    return new String(creat);
  }

  protected void storeSettings() {
    try {
      String crid = crid4settings;//totalcross.sys.Settings.applicationId;
      // first verify if the PDBFile is created but the String is null
      totalcross.sys.Settings.showDesktopMessages = false; // guich@340_49
      boolean saveSettings = totalcross.sys.Settings.appSettings != null || totalcross.sys.Settings.appSecretKey != null
          || totalcross.sys.Settings.appSettingsBin != null; // guich@570_9: also check if appSecretKey is null

      totalcross.io.PDBFile cat;

      if (!saveSettings) {
        try {
          cat = new totalcross.io.PDBFile("Settings4" + crid + ".TCVM." + crid, totalcross.io.PDBFile.READ_WRITE); // guich@241_17: changed READ_ONLY to READ_WRITE to fix "operation invalid" error
          cat.delete();
        } catch (totalcross.io.FileNotFoundException e) {
        }
      } else {
        cat = new totalcross.io.PDBFile("Settings4" + crid + ".TCVM." + crid, totalcross.io.PDBFile.CREATE);
        totalcross.io.ResizeRecord rs = new totalcross.io.ResizeRecord(cat, 256);
        totalcross.io.DataStream ds = new totalcross.io.DataStream(rs);

        try {
          cat.setRecordPos(1);
          cat.deleteRecord();
        } catch (totalcross.io.IOException e) {
        }
        try {
          cat.setRecordPos(0);
          cat.deleteRecord();
        } catch (totalcross.io.IOException e) {
        }
        rs.startRecord();
        // store the appSettings record
        ds.writeString(totalcross.sys.Settings.appSettings);
        ds.writeString(totalcross.sys.Settings.appSecretKey);
        rs.endRecord();
        // guich@573_16: store the bin in another record
        if (totalcross.sys.Settings.appSettingsBin != null) {
          int len = totalcross.sys.Settings.appSettingsBin.length;
          cat.addRecord(len);
          cat.writeBytes(totalcross.sys.Settings.appSettingsBin, 0, len);
        }
        cat.close();

      }
      totalcross.sys.Settings.showDesktopMessages = true;
    } catch (Throwable t) {
      System.out.println("Settings can't be stored: " + t.toString());
    }
  }

  protected void getAppSettings() {
    String crid = crid4settings = totalcross.sys.Settings.applicationId;
    totalcross.sys.Settings.showDesktopMessages = false; // guich@340_49
    try {
      totalcross.io.PDBFile cat = new totalcross.io.PDBFile("Settings4" + crid + ".TCVM." + crid,
          totalcross.io.PDBFile.READ_WRITE);
      totalcross.io.DataStream ds = new totalcross.io.DataStream(cat);
      cat.setRecordPos(0);
      String s;
      s = ds.readString();
      if (!"".equals(s)) {
        totalcross.sys.Settings.appSettings = s;
      }
      try {
        s = ds.readString();
        if (!"".equals(s)) {
          totalcross.sys.Settings.appSecretKey = s;
        }
      } catch (Throwable t) {
        System.out.println("Reading an old settings file; no appSecretKey available.");
      }

      if (cat.getRecordCount() > 1) // guich@573_16
      {
        cat.setRecordPos(1);
        byte[] buf = new byte[cat.getRecordSize()];
        cat.readBytes(buf, 0, buf.length);
        totalcross.sys.Settings.appSettingsBin = buf;
      }

      cat.close();
    } catch (Throwable t) {
    }
    totalcross.sys.Settings.showDesktopMessages = true; // guich@340_49
  }

  protected char getFirstSymbol(String s) {
    char[] c = s.toCharArray();
    for (int i = 0; i < c.length; i++) {
      if (c[i] != ' ' && !('0' <= c[i] && c[i] <= '9')) {
        return c[i];
      }
    }
    return ' ';
  }

  /** called by totalcross.Launcher.init() */
  public void fillSettings() {
    if (settingsFilled) {
      return;
    }
    settingsFilled = true;
    java.util.Calendar cal = java.util.Calendar.getInstance();
    // guich@340_34: since java can't provide us good methods to return these values, we use parse the return of some formatting methods
    cal.set(2002, 11, 25, 20, 0, 0); // guich@401_32
    java.text.DateFormat df = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT); // guich@401_32: fixed wrong results in some systems
    String d = df.format(cal.getTime());
    totalcross.sys.Settings.dateFormat = d.startsWith("25") ? totalcross.sys.Settings.DATE_DMY
        : d.startsWith("12") ? totalcross.sys.Settings.DATE_MDY : totalcross.sys.Settings.DATE_YMD;
    totalcross.sys.Settings.dateSeparator = getFirstSymbol(d);
    df = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT); // guich@401_32
    d = df.format(cal.getTime());

    totalcross.sys.Settings.is24Hour = d.toLowerCase().indexOf("am") == -1 && d.toLowerCase().indexOf("pm") == -1;
    totalcross.sys.Settings.timeSeparator = getFirstSymbol(d);
    //

    totalcross.sys.Settings.weekStart = (byte) (cal.getFirstDayOfWeek() - 1);
    settingsRefresh(false);

    java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols();
    totalcross.sys.Settings.thousandsSeparator = dfs.getGroupingSeparator();
    totalcross.sys.Settings.decimalSeparator = dfs.getDecimalSeparator();
    totalcross.sys.Settings.screenBPP = toBpp;
    try {
      totalcross.sys.Settings.screenWidthInDPI = totalcross.sys.Settings.screenHeightInDPI = Toolkit.getDefaultToolkit()
          .getScreenResolution();
    } catch (Throwable t) {
      totalcross.sys.Settings.screenWidthInDPI = 96;
    }
    totalcross.sys.Settings.romVersion = 0x02000000;
    totalcross.sys.Settings.uiStyle = totalcross.sys.Settings.VISTA_UI;
    totalcross.sys.Settings.screenWidth = toWidth;
    totalcross.sys.Settings.screenHeight = toHeight;
    totalcross.sys.Settings.onJavaSE = true;
    totalcross.sys.Settings.platform = Settings.JAVA;
    totalcross.sys.Settings.applicationId = getDefaultCrid(className); // dhaysmith@420_4
    totalcross.sys.Settings.deviceId = "Desktop"; // guich@568_2
    if (totalcross.sys.Settings.applicationId != null) {
      getAppSettings(); // guich@330_47
    }
    try {
      // Fill all paths
      String basePath = System.getProperty("user.dir");
      totalcross.sys.Settings.vmPath = basePath;
      totalcross.sys.Settings.appPath = basePath;
      // guich@tc112_21: commented - if (totalcross.sys.Settings.dataPath == null) totalcross.sys.Settings.dataPath = basePath; // flsobral@tc100b5_51: Settings.dataPath was being overwritten if set before the Launcher was initialized.

      if (totalcross.sys.Settings.appPath != null) // guich@582_17: make sure that it ends with a slash
      {
        if (totalcross.sys.Settings.appPath.indexOf('/') >= 0 && !totalcross.sys.Settings.appPath.endsWith("/")) {
          totalcross.sys.Settings.appPath += "/";
        } else if (totalcross.sys.Settings.appPath.indexOf('\\') >= 0
            && !totalcross.sys.Settings.appPath.endsWith("\\")) {
          totalcross.sys.Settings.appPath += "\\";
        }
      }
      totalcross.sys.Settings.userName = !isApplication ? null : java.lang.System.getProperty("user.name");
    } catch (SecurityException se) {
      totalcross.sys.Settings.userName = null;
    }
  }

  @SuppressWarnings("deprecation")
  public void settingsRefresh(boolean callStoreSettings) // guich@tc115_81
  {
    java.util.TimeZone tz = java.util.TimeZone.getDefault(); // guich@340_33
    Settings.daylightSavingsMinutes = tz.getDSTSavings() / 60000;
    Settings.daylightSavings = Settings.daylightSavingsMinutes != 0;
    Settings.timeZone = tz.getRawOffset() / (60 * 60000);
    Settings.timeZoneMinutes = tz.getRawOffset() / 60000;
    Settings.timeZoneStr = java.util.TimeZone.getDefault().getID();
    if (callStoreSettings) {
      try {
        storeSettings();
      } catch (Exception e) {
      }
    }
  }

  static totalcross.ui.font.Font getBaseFont(String name, boolean bold, int size, String suffix) {
    String key = name + "|" + bold + "|" + size + "|" + suffix;
    totalcross.ui.font.Font f = (totalcross.ui.font.Font) htBaseFonts.get(key);
    if (f == null) {
      int i;
      if (!name.endsWith("noaa")) {
        TCZ z = (TCZ) loadedTCZs.get((name + ".tcz").toLowerCase());
        if (z == null) {
          return null;
        }
        FontInfo fi = (FontInfo) z.bag;
        for (i = 0; i < fi.sizes.length - 1; i++) {
          if (size <= fi.sizes[i]) {
            size = fi.sizes[i];
            break;
          }
        }
      }

      int idx = Integer.parseInt(suffix.substring(suffix.indexOf('u') + 1));
      totalcross.ui.font.Font.baseChar = (char) idx;
      f = totalcross.ui.font.Font.getFont(name, bold, size);
      totalcross.ui.font.Font.baseChar = ' ';
      if (f != null) {
        f.removeFromCache();
        htBaseFonts.put(key, f);
      }
    }

    return f;
  }

  protected Launcher.UserFont loadUF(String fontName, String suffix) {
    boolean hasTriedToLoadBefore = loadedFontsMap.containsKey(fontName.toLowerCase());
    if (hasTriedToLoadBefore && loadedFontsMap.get(fontName.toLowerCase()) == null) {
      return null;
    }
    try {
      if (totalcross.ui.font.Font.baseChar == ' ' && !fontName.endsWith("noaa")) // test if there's another 8bpp native font. - base font
      {
        boolean bold = suffix.charAt(1) == 'b';
        int size = Integer.parseInt(suffix.substring(2, suffix.indexOf('u')));
        totalcross.ui.font.Font base = getBaseFont(fontName, bold, size, suffix);
        if (base == null) {
          ((Launcher) this).new UserFont(fontName, suffix); // load sizes
          base = getBaseFont(fontName, bold, size, suffix);
        }
        if (base != null) {
          return ((Launcher) this).new UserFont(fontName, suffix, size, base);
        }
      }
      return ((Launcher) this).new UserFont(fontName, suffix);
    } catch (Exception e) {
      String msg = "" + e.getMessage();
      if (!msg.startsWith("name") && !msg.endsWith("not found")) {
        if (Settings.onJavaSE) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  public Launcher.UserFont getFont(totalcross.ui.font.Font f, char c) {
    Launcher.UserFont uf = null;
    try {
      // verify if its in the cache.
      String fontName = f.name;
      int size = (int) (Math.max(f.size, totalcross.ui.font.Font.MIN_FONT_SIZE) * Settings.screenDensity); // guich@tc122_15: don't check for the maximum font size here

      char faceType = c < 0x3000 && f.style == 1 ? 'b' : 'p';
      int uIndex = ((int) c >> 8) << 8;
      String suffix = "$" + faceType + size + "u" + uIndex;
      String key = fontName + suffix;
        uf = (Launcher.UserFont) htLoadedFonts.get(key);
      if (uf != null) {
        return uf;
      }

      boolean hasTriedToLoadBefore = loadedFontsMap.containsKey(fontName.toLowerCase());
      if (fontName.charAt(0) == '$') {
        print("Native fonts are not supported on Desktop");
      } else if (!hasTriedToLoadBefore || loadedFontsMap.get(fontName.toLowerCase()) != null) {
        // first, try to load the font itself using the current font pattern
        uf = loadUF(fontName, suffix);
        if (uf == null) {
          uf = loadUF(fontName, "$p" + size + "u" + uIndex); // guich@tc122_15: ... check only here
        }
        if (uf == null && f.size != totalcross.ui.font.Font.NORMAL_SIZE) {
          int t = f.size;
          while (uf == null && ++t <= 120) {
            uf = loadUF(fontName, "$p" + t + "u" + uIndex);
          }
          t = f.size;
          while (uf == null && --t >= 5) {
            uf = loadUF(fontName, "$p" + t + "u" + uIndex);
          }
        }
        if (uf == null) {
          uf = loadUF(fontName, "$" + faceType + totalcross.ui.font.Font.NORMAL_SIZE + "u" + uIndex);
        }
        if (uf == null && faceType != 'p') {
          uf = loadUF(fontName, "$p" + totalcross.ui.font.Font.NORMAL_SIZE + "u" + uIndex);
        }
      }

      // at last, use the default font
      if (uf == null) {
        uf = loadUF(totalcross.ui.font.Font.DEFAULT, suffix);
      }
      if (uf == null && fontName.charAt(0) != '$') {
        for (int i = totalcross.ui.font.Font.MIN_FONT_SIZE; i <= totalcross.ui.font.Font.MAX_FONT_SIZE; i++) {
          if ((uf = loadUF(fontName, "$p" + i + "u" + uIndex)) != null) {
            break;
          }
        }
      }
      if (uf == null) {
        for (int i = totalcross.ui.font.Font.MIN_FONT_SIZE; i <= totalcross.ui.font.Font.MAX_FONT_SIZE; i++) {
          if ((uf = loadUF(totalcross.ui.font.Font.DEFAULT, "$p" + i + "u" + uIndex)) != null) {
            break;
          }
        }
      }

      if (uf != null) {
        if (totalcross.ui.font.Font.baseChar == ' ') {
          htLoadedFonts.put(key, uf); // note that we will use the original key to avoid entering all exception handlers.
        }
        f.name = uf.fontName; // update the name, the font may have been replaced.
      } else if (htLoadedFonts.size() > 0) {
        return c == ' ' ? null : getFont(f, ' '); // probably the index was outside the available ranges at this font - guich@tc110_28: if space, just return null
      } else if (appletInitialized) // guich@500_1: when retroguard is loaded, Applet.init is never called, so we just skip here.
      {
        System.err.println("No fonts found! be sure to place the file " + totalcross.ui.font.Font.DEFAULT
            + ".tcz in the same directory from where you're running your application"
            + (isApplication ? " or put a reference to TotalCross3/etc folder in the classpath!"
                : "or in your applet's codebase or in a jar file!"));
        System.exit(2);
      }
    } catch (Exception e) {
      System.out.println("Launcher.getFont: " + e);
    }
    return uf;
  }

  /** Represents the internal font structure, read from a pdb file. used internally. */
  // created by guich@200b2

}
