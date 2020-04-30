// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io;

import totalcross.sys.Convert;
import totalcross.sys.Registry;
import totalcross.sys.Settings;
import totalcross.util.Vector;

public class File4D extends RandomAccessStream implements FileStates {
  protected String path;
  Object fileRef;
  int mode = INVALID;
  int slot;
  boolean dontFinalize;

  static final boolean isAndroid = Settings.ANDROID.equals(Settings.platform);
  static final boolean isIOS = Settings.isIOS();
  static final boolean isWin32 = Settings.WIN32.equals(Settings.platform);
  static final boolean isWinCE = Settings.isWindowsCE();
  public static final int CLOSED = 6;

  public static final byte TIME_ALL = (byte) 0xF;
  public static final byte TIME_CREATED = (byte) 1;
  public static final byte TIME_MODIFIED = (byte) 2;
  public static final byte TIME_ACCESSED = (byte) 4;

  public static final int ATTR_ARCHIVE = 1;
  public static final int ATTR_HIDDEN = 2;
  public static final int ATTR_READ_ONLY = 4;
  public static final int ATTR_SYSTEM = 8;

  public static String[] winceVols = { "\\Storage Card2\\", "\\Storage Card1\\", "\\SD Card\\", "\\Storage Card\\",
      "\\SD-MMCard\\", "\\CF Card\\" }; // guich@572_3

  private static String deviceAlias;

  native private static String getDeviceAlias();

  public File4D(String path, int mode, int slot)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.FileNotFoundException, totalcross.io.IOException {
    if (deviceAlias == null) {
      deviceAlias = getDeviceAlias();
    }
    if (mode == 8) {
      mode = CREATE_EMPTY; // keep compatibility
    }
    if (path == null) {
      throw new java.lang.NullPointerException("Argument 'path' cannot have a null value.");
    }
    if (path.length() == 0 || path.length() > 255) {
      throw new totalcross.io.IllegalArgumentIOException("path", path);
    }
    if (mode < DONT_OPEN || mode > CREATE_EMPTY) {
      throw new totalcross.io.IllegalArgumentIOException("mode", Convert.toString(mode));
    }
    if (slot < -1) {
      throw new totalcross.io.IllegalArgumentIOException("slot", Convert.toString(slot));
    }

    path = Convert.normalizePath(path);
    if (path.startsWith("device/")) // flsobral@tc110_108: added support for the alias "device/".
    {
      path = path.substring(6);
      //flsobral@tc129.2: path for both iphone and ipad.
      if (Settings.isIOS()) {
        path = Convert.appendPath(deviceAlias, path);
      } else if (isAndroid || isWin32 || isWinCE) {
        path = Settings.appPath + "/" + path;
      }
      slot = 1;
    } else if (isAndroid && !path.startsWith("/") && path.indexOf("data/data") < 0) {
      path = Settings.appPath + "/" + path;
    } else if (isIOS && !path.startsWith("/")) {
      path = Convert.appendPath(deviceAlias, path);
    }

    this.path = path;
    this.mode = mode;
    this.slot = slot;
    create(path, mode, slot);
  }

  public File4D(String path, int mode)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.FileNotFoundException, totalcross.io.IOException {
    this(path, mode, -1);
  }

  public File4D(String path) throws IllegalArgumentIOException, totalcross.io.IOException {
    this(path, DONT_OPEN, -1);
  }

  @Override
  native public void close() throws totalcross.io.IOException;

  public String getPath() {
    return path;
  }

  @Override
  public int getPos() throws totalcross.io.IOException {
    if (mode == INVALID) {
      throw new IOException("Invalid file handler");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }

    return pos;
  }

  @Override
  public void setPos(int offset, int origin) throws IOException {
    if (mode <= DONT_OPEN) {
      throw new IOException(mode == INVALID ? "Invalid file handler" : "Operation cannot be used in mode DONT_OPEN");
    }

    int newPos;
    int length = this.getSize();

    switch (origin) {
    case SEEK_SET:
      newPos = offset;
      break;
    case SEEK_CUR:
      newPos = this.pos + offset;
      break;
    case SEEK_END:
      newPos = length + offset - 1;
      break;
    default:
      throw new IllegalArgumentException("origin");
    }

    if (newPos < 0) {
      throw new IOException("Invalid position: " + newPos);
    }
    setPos(newPos);
  }

  native private void create(String path, int mode, int slot)
      throws totalcross.io.FileNotFoundException, totalcross.io.IOException;

  native public static boolean isCardInserted(int slot) throws totalcross.io.IllegalArgumentIOException;

  native public void createDir() throws totalcross.io.IOException; // guich@tc122_24: void, not boolean

  native public void delete() throws totalcross.io.FileNotFoundException, totalcross.io.IOException;

  native public boolean exists() throws totalcross.io.IOException;

  native public int getSize() throws totalcross.io.IOException;

  native public boolean isDir() throws totalcross.io.IOException;

  native public String[] listFiles() throws totalcross.io.IOException;

  @Override
  native public int readBytes(byte b[], int off, int len) throws totalcross.io.IOException;

  native public void rename(String path) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;

  @Override
  native public void setPos(int pos) throws totalcross.io.IOException;

  @Override
  native public int writeBytes(byte b[], int off, int len) throws totalcross.io.IOException;

  native public void setAttributes(int attr) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;

  native public int getAttributes() throws totalcross.io.IOException;

  native public void setTime(byte whichTime, totalcross.sys.Time time)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;

  native public totalcross.sys.Time getTime(byte whichTime)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;

  native public void setSize(int newSize) throws totalcross.io.IOException;

  native public static String getCardSerialNumber(int slot)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;

  native public boolean isEmpty() throws IOException;

  public File getParent() throws totalcross.io.IOException {
    if (mode == INVALID) {
      throw new totalcross.io.IOException("Invalid file handler");
    }
    if (path.equals("/")) {
      return null;
    }

    return new File(path.substring(0, path.lastIndexOf('/')), DONT_OPEN);
  }

  public static File getCardVolume() throws totalcross.io.IOException {
    if (Settings.isWindowsCE()) //flsobral@tc112_10: Fixed to also work on devices recognized as WindowsMobile.
    {
      String cardName = null;
      try {
        cardName = Registry.getString(Registry.HKEY_LOCAL_MACHINE, "System\\StorageManager\\Profiles\\SDMemory",
            "Folder"); //flsobral@tc112_15: Attempt to retrieve the name from the registry.
      } catch (Exception e) {
        File f;
        for (int i = winceVols.length - 1; i >= 0; i--) {
          try {
            if ((f = new File(winceVols[i])).isDir()) {
              return f;
            }
          } catch (FileNotFoundException fnfe) {
          }
        }
      }
      if (cardName != null) {
        cardName = cardName.replace('\\', '/'); //flsobral@tc113_15: Always wrap the path with slashes.
        if (cardName.charAt(0) != '/') {
          cardName = "/" + cardName;
        }
        if (!cardName.endsWith("/")) {
          cardName = cardName + "/";
        }
        File cardFile = new File(cardName);
        if (cardFile.isDir()) {
          return cardFile;
        }
      }
    }
    return null;
  }

  native public void flush() throws totalcross.io.IOException;

  @Override
  protected void finalize() {
    try {
      if (mode != INVALID) {
        this.close();
      }
    } catch (totalcross.io.IOException e) {
    }
  }

  public int getSlot() {
    return slot;
  }

  private static void listFiles(String dir, Vector files, boolean recursive) throws IOException // guich@tc115_92
  {
    try (File f = new File(dir)) {
      String[] list = f.listFiles();
      if (list != null) {
        for (int i = 0; i < list.length; i++) {
          String p = list[i];
          String full = Convert.appendPath(dir, p);
          files.addElement(full);
          if (recursive && p.endsWith("/")) {
            listFiles(full, files, recursive);
          }
        }
      }
    }
  }

  public static String[] listFiles(String dir) throws IOException // guich@tc115_92
  {
    return listFiles(dir, true);
  }

  public static String[] listFiles(String dir, boolean recursive) throws IOException {
    Vector files = new Vector(50);
    dir = Convert.appendPath(dir, "/");
    files.addElement(dir);
    listFiles(dir, files, recursive);
    files.qsort();
    return (String[]) files.toObjectArray();
  }

  public static void deleteDir(String dir) throws IOException // guich@tc115_92
  {
    String[] files = listFiles(dir);
    for (int i = files.length; --i >= 0;) {
      new File((String) files[i]).delete();
    }
  }

  native public static String[] listRoots();

  public void copyTo(File dest) throws IOException // guich@tc126_8
  {
    setPos(0);
    try {
      dest.setPos(0);
    } catch (IOException ioe) {
    }
    byte[] buf = new byte[4096];
    int n = 0;
    while ((n = readBytes(buf, 0, buf.length)) > 0) {
      dest.writeBytes(buf, 0, n);
    }
  }

  public void moveTo(File dest) throws IOException // guich@tc126_8
  {
    if (mode == READ_ONLY) {
      throw new IOException("Operation cannot be used in READ_ONLY mode");
    }
    copyTo(dest);
    delete();
  }

  native public int chmod(int mod) throws IOException; // guich@tc126_16

  public static void copy(String src, String dst) throws IOException // guich@tc126_43
  {
    try (File fin = new File(src, READ_ONLY)) {
      try (File fout = new File(dst, CREATE_EMPTY)) {
        fin.copyTo(fout);
      }
    }
  }

  public static void move(String src, String dst) throws IOException // guich@tc126_43
  {
    try (File fin = new File(src, READ_WRITE)) {
      try (File fout = new File(dst, CREATE_EMPTY)) {
        fin.moveTo(fout);
      }
    }
  }

  @Deprecated
  public byte[] readAndClose() throws IOException {
    try {
      return read();
    } finally {
      close();
    }
  }

  @Deprecated
  public byte[] readAndDelete() throws IOException {
    try {
      return read();
    } finally {
      delete();
    }
  }

  @Deprecated
  public byte[] read() throws IOException {
    int len = getSize();
    byte[] ret = new byte[len];
    readBytes(ret, 0, len);
    return ret;
  }

  @Deprecated
  public void writeAndClose(byte[] bytes) throws IOException {
    try {
      writeBytes(bytes, 0, bytes.length);
    } finally {
      close();
    }
  }
}
