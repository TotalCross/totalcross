// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

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
import tc.simulator.awt.AppletPreviewSurface;
import tc.simulator.awt.AwtWindow;
import tc.preview.PreviewSession;
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
abstract class StorageBridge extends FrameRenderer {
  ///////////////////////        I/O        /////////////////////////////////////
  protected File[] getClassPathDirectories() throws Exception {
    char dirSeparator = File.pathSeparatorChar;
    File[] classPath;
    String pathstr = System.getProperty("java.class.path");
    // Count the number of path separators
    int i = 0;
    int n = 0;
    int j = 0;
    while ((i = pathstr.indexOf(dirSeparator, i)) != -1) {
      n++;
      i++;
    }
    // Build the class path
    File[] path = new File[n + 1];
    int len = pathstr.length();
    for (i = n = 0; i < len; i = j + 1) {
      if ((j = pathstr.indexOf(dirSeparator, i)) == -1) {
        j = len;
      }
      if (i != j) {
        String p = pathstr.substring(i, j);
        File file = new File(p);
        if (!file.isDirectory()) {
          file = new File(getPathOf(p)); // add the parent path of the file
        }
        if (file.isDirectory()) {
          path[n++] = file;
        }
      }
    }
    // Trim class path to exact size
    classPath = new File[n];
    System.arraycopy(path, 0, classPath, 0, n);
    return classPath;
  }

  protected InputStream readJavaInputStream(java.io.InputStream is) {
    if (is == null) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
    byte[] buf = new byte[128];
    int len;
    while (true) {
      try {
        len = is.read(buf);
      } catch (java.io.IOException e) {
        break;
      }
      if (len > 0) {
        baos.write(buf, 0, len);
      } else {
        break;
      }
    }
    return new ByteArrayInputStream(baos.toByteArray());
  }

  protected String getPathOf(String pathAndFileName) {
    char[] chars = pathAndFileName.toCharArray();
    for (int i = chars.length - 1; i >= 0; i--) {
      if (chars[i] == '\\' || chars[i] == '/') {
        return new String(chars, 0, i);
      }
    }
    return ""; // no path
  }

  public String getDataPath() // guich@420_11 - this now is needed because the user may change the datapath anywhere in the program
  {
    String path = totalcross.sys.Settings.dataPath;
    if (path != null) {
      path = path.replace('\\', '/');
      if (!path.endsWith("/")) {
        path += "/";
        // don't check for folder to keep compatibility with win32 vm
        //java.io.File f = new java.io.File(newDataPath);
        //if (!f.isDirectory())
        //   System.out.println("ERROR: dataPath specified is not a directory or does not exist! "+newDataPath);
      }
    }
    return path;
  }

  protected String getMainWindowPath() {
    if (MainWindow.getMainWindow() == null) {
      return null;
    }
    String main = MainWindow.getMainWindow().getClass().getName().replace('.', '/');
    return getPathOf(main) + "/";
  }

  /** used in some classes so they can correctly open files. now can open jar files. */
  public InputStream openInputStream(String path) {
    String sread = "\nopening for read " + path + "\n";
    String dataPath = getDataPath();
    InputStream stream = null;
    String mainpath = getMainWindowPath();
    try {
      try // guich@tc100: removed the nonGuiApp flag
      {
        sread += "#0 - the file given: " + path + "\n";
        stream = new FileInputStream(path); // guich@421_72
      } catch (Exception e) {
        stream = null;
      }
      if (stream == null && isApplication) {
        // search in the Settings.dataPath
        try {
          String p = isOk(dataPath) ? (dataPath + path) : path;
          sread += "#1 - dataPath: " + p + "\n";
          stream = new FileInputStream(p);
          htOpenedAt.put(path, getPathOf(p)); // guich@200b4_82 - jr: i changed getPathOf(path) to getPathOf(p)
        } catch (Exception e) {
          stream = null;
        }
        if (stream == null && mainpath != null) {
          try {
            String p = mainpath + path;
            sread += "#2 - MainWindow's path from current folder: " + p + "\n";
            stream = new FileInputStream(p);
            htOpenedAt.put(path, getPathOf(p)); // guich@200b4_82 - jr: i changed getPathOf(path) to getPathOf(p)
          } catch (Exception e) {
            stream = null;
          }
        }
        // search in the classpath
        if (stream == null) {
          sread += "#3 - classpath\n";
          File[] dirs = getClassPathDirectories();
          File f = null;
          for (int i = 0; i < dirs.length; i++) {
            try {
              f = new File(dirs[i], path);
              if (!f.isFile() && mainpath != null) {
                f = new File(dirs[i], mainpath + path); // guich@tc100: search in the path of the main window
              }
              if (f.isFile()) {
                String ff = getPathOf(f.getAbsolutePath());
                htOpenedAt.put(path, ff); // guich@200b4_82 - jr: changed dirs[i].getAbsolutePath - guich@tc112_20: using f.getAbsolutePath instead of dirs[i].getAbsolutePath
                break;
              } else {
                f = null; // guich@400_8: fixed problem when file was not found so the #3 can be tried below
              }
            } catch (Exception e) {
              f = null;
            }
          }
          if (f != null) {
            stream = new FileInputStream(f);
          }
        }
        if (stream == null && _class != null) // guich@400_6: now the resources can be read from the jar file
        {
          sread += "#4 - jar file\n";
          try {
            InputStream is = (InputStream) _class.getResourceAsStream("/" + path);
            if (is != null) {
              stream = readJavaInputStream(is);
            }
          } catch (Throwable tt) {
            if (tt.getMessage() != null) {
              System.out.println(tt.getMessage());
            }
          }
        }
        String sjar;
        if (stream == null && !path.endsWith(".class")
            && (sjar = getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).contains(".jar")) // guich@330 - let tc.Help work from inside a jar
        {
          sread += "#4b - " + sjar.substring(1) + "\n";
          try {
            URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
            ZipInputStream zIn = new ZipInputStream(url.openStream());
            String spath = "/" + path;
            for (java.util.zip.ZipEntry zEntry = zIn.getNextEntry(); zEntry != null; zEntry = zIn.getNextEntry()) {
              if (zEntry.getName().endsWith(spath)) {
                stream = readJavaInputStream(zIn);
                break;
              }
            }
            zIn.close();
          } catch (Throwable tt) {
            if (tt.getMessage() != null) {
              System.out.println(tt.getMessage());
            }
          }
        }
        if (stream == null && htAttachedFiles.size() > 0) // guich@tc100: load from attached libraries too
        {
          sread += "#5 - attached libraries\n";
          totalcross.io.ByteArrayStream bas = (totalcross.io.ByteArrayStream) htAttachedFiles.get(path.toLowerCase());
          if (bas != null) {
            stream = new ByteArrayInputStream(bas.getBuffer()); // buffer is the same size of the loaded file.
          }
        }
      } else if (stream == null) {
        URL url;
        // zero in the jar file (normal way)
        InputStream is = null;
        try {
          is = (InputStream) _class.getResourceAsStream("/" + path);
        } catch (Throwable tt) {
          if (tt.getMessage() != null) {
            System.out.println(tt.getMessage());
          }
        }
        sread += "#1 - resource: " + is + "\n"; // guich@200b4_59
        if (is != null) {
          stream = readJavaInputStream(is);
        }
        // first in the jar file
        // guich@200b4: using this in Internet makes the archive be fetched from the server at each call of this function.
        if (stream == null) {
          String archive = getLauncherParameter("archive");
          sread += "#2 - archive: " + archive + "\n";
          if (isOk(archive) && !archive.equals("null")) {
            String[] archives = tokenizeString(archive, ','); // guich@580_39: if there are more than one file, split them
            for (int i = 0; i < archives.length; i++) {
              archive = archives[i];
              if (archive.startsWith("null")) {
                archive = archive.substring(4);
              }
              URL codeBase = getLauncherCodeBase();
              url = new URL(codeBase + "/" + archive);
              try {
                ZipInputStream zIn = new ZipInputStream(url.openStream());
                java.util.zip.ZipEntry zEntry = zIn.getNextEntry();
                while (!zEntry.getName().equals(path)) {
                  zEntry = zIn.getNextEntry();
                  if (zEntry == null) {
                    throw new Exception("doh");
                  }
                }
                // guich@200b2: ok. the zIn.available() returns 1 and not the real size of the zip entry. so, here we read all into a byte stream
                stream = readJavaInputStream(zIn);
              } catch (Exception e) {
                if (!e.getMessage().equals("doh")) {
                  e.printStackTrace();/* doh didn't find it in the jar thing */
                }
              }
            }
          }
        }
        // second under the codebase
        if (stream == null) {
          try {
            URL codeBase = getLauncherCodeBase();
            String cb = codeBase.toString();
            char lastc = cb.charAt(cb.length() - 1);
            char firstc = path.charAt(0);
            if (lastc != '/' && firstc != '/') {
              cb += "/";
            }
            sread += "#3 - url: " + cb + path + "\n";
            url = new URL(cb + path);
            stream = url.openStream();
          } catch (FileNotFoundException ee) {
          } catch (Exception e) {
            e.printStackTrace();
            /* neither in the codebase */}
        }
        // third in the localhost
        if (stream == null) {
          try {
            sread += "#4- url: file://localhost/" + dataPath + path + "\n";
            url = new URL("file://localhost/" + dataPath + path); // guich@120
            stream = url.openStream();
          } catch (Exception e) {
          }
        }
        ;
        if (stream == null && htAttachedFiles.size() > 0) // guich@tc100: load from attached libraries too
        {
          sread += "#5 - attached libraries\n";
          totalcross.io.ByteArrayStream bas = (totalcross.io.ByteArrayStream) htAttachedFiles.get(path.toLowerCase());
          if (bas != null) {
            stream = new ByteArrayInputStream(bas.getBuffer()); // buffer is the same size of the loaded file.
          }
        }
      }
      if (stream == null) {
        debug(sread + "file not found\n");
      }
    } catch (FileNotFoundException ee) {
      print("file not found");
    } catch (Exception e) // guich@120
    {
      if (isOk(e.getMessage())) {
        print("error in JavaBridge.openInputStream: " + e.getMessage());
      }
      return null;
    }
    return stream;
  }

  protected OutputStream openOutputUrl(URL url) {
    try {
      URLConnection con = url.openConnection();
      con.setUseCaches(false);
      con.setDoOutput(true);
      con.setDoInput(false);
      return con.getOutputStream();
    } catch (Exception u) // try another way
    {
      try {
        String path = url + "";
        return new FileOutputStream(isOk(totalcross.sys.Settings.dataPath) ? (getDataPath() + path) : path);
      } catch (Exception ee) {
        return null;
      }
    }
  }

  /** used in some classes so they can correctly open files. used internally by readBytes. */
  public OutputStream openOutputStream(String path) {
    debug("\nopening for write " + path);
    String dataPath = getDataPath();
    OutputStream stream = null;
    String readPath = (String) htOpenedAt.get(path); // guich@tc112_20
    try {
      try // guich@tc100: removed the nonGuiApp flag
      {
        String pp = isOk(dataPath) ? (dataPath + path)
            : isOk(readPath) ? totalcross.sys.Convert.appendPath(readPath, path) : path; // guich@tc112_20: use readPath if not null
        stream = new FileOutputStream(pp); // guich@421_11: added support for dataPath
      } catch (Exception e) {
        stream = null;
      }

      if (stream == null && isApplication) {
        // search in the place where it was read - guich@200b4_82
        if (readPath != null) {
          try {
            debug("#1 - read path");
            stream = new FileOutputStream(new java.io.File(readPath, path));
            debug("found in " + readPath);
          } catch (Exception e) {
            stream = null;
          }
        }
        if (stream != null) {
          return stream;
        }
        // search in the Settings.dataPath
        try {
          String p = isOk(dataPath) ? (dataPath + path) : path;
          debug("#2 - Settings.dataPath");
          stream = new FileOutputStream(p);
          debug("found in " + p);
        } catch (Exception e) {
          stream = null;
        }
        // search in the classpath
        if (stream == null) {
          debug("#3 - classpath");
          File[] dirs = getClassPathDirectories();
          File f = null;
          for (int i = 0; i < dirs.length; i++) {
            try {
              f = new File(dirs[i], path);
              if (f.isFile()) {
                debug("found in " + dirs[i]);
                break;
              }
            } catch (Exception e) {
              f = null;
            }
          }
          if (f == null) {
            debug("could not find file in the classpath");
          } else {
            stream = new FileOutputStream(f);
          }
        }
      } else if (stream == null) {
        URL url;
        // first under the codebase
        if (stream == null) {
          try {
            URL codeBase = getLauncherCodeBase();
            debug("#1- codeBase: " + codeBase);
            String cb = codeBase.toString();
            char lastc = cb.charAt(cb.length() - 1);
            char firstc = path.charAt(0);
            if (lastc != '/' && firstc != '/') {
              cb += "/";
            }
            url = new URL(cb + path);
            stream = openOutputUrl(url);
            debug("found under codebase: " + url);
          } catch (Exception e) {
            e.printStackTrace();
            /* neither in the codebase */}
        }
        // third in the localhost
        if (stream == null) {
          try {
            debug("#2- url: file://localhost/" + dataPath + path);
            url = new URL("file://localhost/" + dataPath + path); // guich@120
            stream = openOutputUrl(url);
            debug("found under localhost: " + url);
          } catch (Exception e) {
          }
        }
        ;
      }
      if (stream == null) {
        debug("file not found");
      }
    } catch (FileNotFoundException ee) {
      debug("file not found");
    } catch (Exception e) {
      /*if (!msgShowed) */print("error in Vm.openOutputStream: " + e.getMessage());
      return null;
    } // guich@200
    return stream;
  }

  /** read the available bytes from the stream getted with openInputStream.
    * called by totalcross.ui.image.Image and totalcross.io.PDBFile
    */
  public byte[] readBytes(String path) {
    byte[] bytes = null;
    try {
      InputStream is = openInputStream(path);
      if (is != null) {
        int n = is.available();
        bytes = new byte[n];
        is.read(bytes);
        is.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return bytes;
  }

  /** write the available bytes to the stream getted with openOutputStream.
    * called by totalcross.io.PDBFile
    */
  public boolean writeBytes(String path, byte[] buf, int len) {
    boolean ret = true;
    try {
      OutputStream os = openOutputStream(path);
      if (os != null) {
        if (buf != null) {
          os.write(buf, 0, len);
          os.close(); // pietj@330_1
        } else {
          print("ATT: you sent to stream.writeBytes a null buffer!");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      ret = false;
    }
    return ret;
  }

  /** return true is the string is valid. called by openInputStream and openOutputStream in this class. */
  protected boolean isOk(String s) {
    return s != null && s.length() > 0;
  }


}
