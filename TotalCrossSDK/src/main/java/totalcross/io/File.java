// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
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

import java.net.URISyntaxException;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.util.Vector;

/**
 * File represents a file or directory.
 * <p>
 * Note that writing to a storage card can be 5x slower than writing to the main memory.
 * <p>
 * Here is an example showing data being read from a file:
 * 
 * <pre>
 * File file = new File(&quot;/temp/tempfile&quot;, File.READ_WRITE);
 * byte b[] = new byte[10];
 * file.readBytes(b, 0, 10);
 * file.close();
 * file = new File(&quot;/temp/tempfile&quot;); // opens in DONT_OPEN mode
 * file.delete();
 * </pre>
 * 
 * When creating a new file, you may start the path using the alias "device/", which evaluates to the platform's base
 * user directory:
 * <ul>
 * <li>PalmOS, WinCE and iPhone - "/" (root)
 * <li>BlackBerry - "/store/home/user/"
 * <li>Java - "/" (current directory)
 * <li>Win32 - "/" (root of the current drive)
 * <li>iOS - "/private/var/" in a installation using .deb (on a jailbroken device) or in the documents folder of the application in a installation 
 * using .ipa
 * <li>Android - "/data/data/totalcross.app.&lt;mainclass name&gt;" or "/data/data/totalcross.app.&lt;application id&gt; if using single package 
 * </ul>
 * The alias is ALWAYS relative to the built in storage, regardless of the value passed to the argument slot.<br>
 * 
 * On iOS and Android, if you don't specify a path, the file will be open in device/ path. 
 */

public class File extends RandomAccessStream implements FileStates {
  /** The path that represents this file */
  protected String path;

  /** stores a java.io.File. */
  private Object fileRef;

  /** Stores a java.io.RandomAccessFile. */
  private Object fileEx;

  /** Stores the mode used to open the file. */
  private int mode;

  /** Stores the slot number passed in the constructor. */
  private int slot;

  /**
   * Used in the setTime method, in parameter whichTime. This sets all times at once.
   * 
   * @see #setTime(byte, totalcross.sys.Time)
   */
  public static final byte TIME_ALL = (byte) 0xF;
  /**
   * Used in the setTime method. These values are platform independent, and can be ORed together in the setTime method.
   * 
   * @see #setTime(byte, totalcross.sys.Time)
   */
  public static final byte TIME_CREATED = (byte) 1;
  /**
   * Used in the setTime method. These values are platform independent, and can be ORed together in the setTime method.
   * 
   * @see #setTime(byte, totalcross.sys.Time)
   */
  public static final byte TIME_MODIFIED = (byte) 2;
  /**
   * Used in the setTime method. These values are platform independent, and can be ORed together in the setTime method.
   * 
   * @see #setTime(byte, totalcross.sys.Time)
   */
  public static final byte TIME_ACCESSED = (byte) 4;

  /**
   * Used in the getAttributes and setAttributes method. These values are platform independent.
   * 
   * @see #setAttributes(int)
   * @see #getAttributes()
   */
  public static final int ATTR_ARCHIVE = 1;
  /**
   * Used in the getAttributes and setAttributes method. These values are platform independent.
   * 
   * @see #setAttributes(int)
   * @see #getAttributes()
   */
  public static final int ATTR_HIDDEN = 2;
  /**
   * Used in the getAttributes and setAttributes method. These values are platform independent. Palm specific: Avoid
   * using this attribute on a file located on the Built-in storage, because some devices do not allow this attribute
   * to be changed after it is first set. This results in a read-only file that cannot be changed or deleted.
   * 
   * @see #setAttributes(int)
   * @see #getAttributes()
   */
  public static final int ATTR_READ_ONLY = 4;
  /**
   * Used in the getAttributes and setAttributes method. These values are platform independent.
   * 
   * @see #setAttributes(int)
   * @see #getAttributes()
   */
  public static final int ATTR_SYSTEM = 8;

  /**
   * These are the volumes that getCardVolume search to find the available one. <br>
   * <br>
   * To access the card in Android devices, prefix the path with <code>/sdcard</code>. Be sure that the sdcard is NOT MOUNTED, otherwise your application will not have access to it.
   * 
   * @see #getCardVolume()
   */
  public static String[] winceVols = { "/Storage Card2/", "/Storage Card1/", "/SD Card/", "/Storage Card/",
      "/SD-MMCard/", "/CF Card/" }; // guich@572_3

  /**
   * Opens a file with the given name, mode and in the given card number.
   * <p>
   * Note that it's not advised to use accentuated characters in the file name. Also, the slash / MUST be the path separator. It is not forbidden 
   * to use the backslash \, but its support might be discontinued in the future to increase performance. Note
   * also that some OSes may not allow the creation of files in the ROOT directory.
   * 
   * @param path
   *           the file's path. Always use slashes (/) instead of backslashes (\\).
   * <br>
   * To access the card in Android devices, prefix the path with <code>/sdcard</code>. Be sure that the sdcard is NOT MOUNTED, otherwise your application will not have access to it.
   * @param mode
   *           one of open modes.
   * @param slot
   *           The card slot number. This currently works only on Palm OS devices, because other OSes use a different
   *           approach to specify the card. The number may be -1 to use the last available card, or a number between 0
   *           and the number of cards supported by the device. Usually, slot 0 is the main memory (CAUTION: cannot be
   *           used with File!), slot 1 is the NVFS volume, slot 2 the external card volume. This may vary on some Palm
   *           devices, so use prefer using the Settings.nvfsVolume property. You can find the available slots using
   *           this code:
   * 
   *           <pre>
   * for (int i = 0; i &lt; 10; i++)
   *    if (File.isCardInserted(i))
   *       add(new Label(&quot;found &quot; + i), LEFT, AFTER); // Zire 22 returns 1 only
   * </pre>
   * 
   * @since SuperWaba 5.52
   * @see #File(String)
   * @see #File(String,int)
   * @see FileStates#DONT_OPEN
   * @see FileStates#READ_WRITE
   * @see FileStates#READ_ONLY
   * @see FileStates#CREATE
   * @see FileStates#CREATE_EMPTY
   * @see totalcross.sys.Settings#nvfsVolume
   * @deprecated TotalCross 2 no longer uses slot
   */
  @Deprecated
  public File(String path, int mode, int slot) throws IllegalArgumentIOException, FileNotFoundException, IOException {
    if (mode == 8) {
      mode = CREATE_EMPTY; // keep compatibility
    }
    if (path == null) {
      throw new java.lang.NullPointerException("Argument 'path' cannot have a null value");
    }
    if (path.length() == 0 || path.length() > 255) {
      throw new IllegalArgumentIOException("path", path);
    }
    if (mode < DONT_OPEN || mode > CREATE_EMPTY) {
      throw new IllegalArgumentIOException("mode", Convert.toString(mode));
    }
    if (slot < -1) {
      throw new IllegalArgumentIOException("slot", Convert.toString(slot));
    }

    path = Convert.normalizePath(path);
    if (path.startsWith("device/")) {
      path = Convert.appendPath(Settings.appPath, path.substring(6)); // guich@tc310: in desktop was using the root folder of current drive
    }

    this.path = path;
    this.mode = mode; // remove the sequential flag
    this.slot = slot;
    create();
  }

  /**
   * Creates a file with the given path, mode and slot=-1.
   * 
   * @param path
   *           the file's path
   * @param mode
   *           one of open modes
   * @throws IllegalArgumentIOException
   * @throws FileNotFoundException
   * @throws IOException
   * @see #File(String)
   * @see #File(String,int,int)
   */
  public File(String path, int mode) throws IllegalArgumentIOException, FileNotFoundException, IOException {
    this(path, mode, -1);
  }

  /**
   * Opens a file with the given path and mode=DONT_OPEN and slot=-1. This constructor is useful for directory
   * manipulation and/or check if file exists. No read/write operation can be done with a file created in this mode.
   * 
   * @param path
   *           the file's path
   * @throws IllegalArgumentIOException
   * @throws FileNotFoundException
   * @throws IOException
   * @see #File(String, int, int)
   */
  public File(String path) throws IllegalArgumentIOException, IOException {
    this(path, DONT_OPEN, -1);
  }

  final private void create() throws FileNotFoundException, IOException {
    java.net.URI pathURI = null;
    if (path.length() >= 2 && path.charAt(1) == ':') // absolute path
    {
      try {
        path = path.replaceAll("\"", "");
        pathURI = new java.net.URI(("file:///" + path.replaceAll(" ", "%20")));
      } catch (URISyntaxException e) {
      }
    }

    java.io.File fileRef4Java = null;
    try {
      if (pathURI != null) {
        fileRef4Java = new java.io.File(pathURI);
      }
    } catch (IllegalArgumentException e) {
      /*
       * The path may contain characters that may not be correctly interpreted when converted to URI. Maybe we can
       * replace them with escape codes, but for now we'll just ignore this error and try again using the path as
       * provided.
       */
    }

    if (fileRef4Java == null) {
      fileRef4Java = new java.io.File(path);
    }

    if (mode != DONT_OPEN) {
      if (mode != CREATE && mode != CREATE_EMPTY && !fileRef4Java.exists()) {
        throw new FileNotFoundException(path);
      }

      if (mode == CREATE_EMPTY) {
        try {
          if (fileRef4Java.exists()) {
            fileRef4Java.delete();
          }
        } catch (java.lang.SecurityException e) {
          throw new IOException(e.getMessage());
        }
      }
      try {
        fileEx = new java.io.RandomAccessFile(fileRef4Java, mode == READ_ONLY ? "r" : "rw");

        /*
         * Attempts to get an exclusive lock for this file, using reflection to call methods from JDK 1.4
         * ((java.io.RandomAccessFile) fileEx).getChannel().tryLock();
         */
        if (mode != READ_ONLY) {
          try {
            // RandomAccessFile.getChannel()
            java.lang.reflect.Method getChannel = fileEx.getClass().getMethod("getChannel");
            Object fileChannel = getChannel.invoke(fileEx);
            // FileChannel.tryLock() -> returns null if the file is already locked.
            java.lang.reflect.Method tryLock = fileChannel.getClass().getMethod("tryLock");
            if (tryLock.invoke(fileChannel) == null) {
              // close everything and throw IOException.
              ((java.io.RandomAccessFile) fileEx).close();
              fileEx = null;
              mode = INVALID;
              throw new IOException("Cannot access the file because it is already in use");
            }
          } catch (java.lang.NoSuchMethodException e) {
            ((java.io.RandomAccessFile) fileEx).close();
            fileEx = null;
            mode = INVALID;
            throw new IOException(e.getMessage());
          } catch (java.lang.IllegalAccessException e) {
            ((java.io.RandomAccessFile) fileEx).close();
            fileEx = null;
            mode = INVALID;
            throw new IOException(e.getMessage());
          } catch (java.lang.reflect.InvocationTargetException e) {
            ((java.io.RandomAccessFile) fileEx).close();
            fileEx = null;
            mode = INVALID;
            throw new IOException("Cannot access the file because it is already in use");
          }
        }
      } catch (java.io.FileNotFoundException e) {
        boolean wasCreate = mode == CREATE || mode == CREATE_EMPTY;
        fileEx = null;
        mode = INVALID;
        if (wasCreate) {
          throw new IOException("Folder not found or there's already a folder with the same name of the file: " + path);
        }
        throw new FileNotFoundException(path);
      } catch (java.io.IOException e) {
        fileEx = null;
        mode = INVALID;
        throw new IOException(e.getMessage());
      }
    }

    this.fileRef = fileRef4Java;
  }

  /**
   * Can be used to verify if a card is inserted into the given slot. Only works on Palm OS and Android devices. In all
   * other platforms, always returns true.
   * 
   * @param slot
   *           The slot number, or -1 to use the last slot number (which, in most devices, will be the only slot
   *           available).
   * @since SuperWaba 5.52
   */
  final public static boolean isCardInserted(int slot) throws IllegalArgumentIOException {
    if (slot < -1) {
      throw new IllegalArgumentIOException("slot", Convert.toString(slot));
    }
    return true;
  }

  /**
   * Closes the file.
   * 
   * @throws IOException
   *            If a file is closed more than once,
   */
  @Override
  public void close() throws IOException {
    if (mode == CLOSED) {
      return;
    }
    if (mode == INVALID) {
      throw new IOException("Invalid file handle");
    }

    try {
      if (mode != DONT_OPEN) {
        java.io.RandomAccessFile fileEx4Java = (java.io.RandomAccessFile) fileEx;
        try {
          fileEx4Java.close();
        } catch (java.io.IOException e) {
          throw new IOException(e.getMessage());
        }
      }
    } finally {
      fileRef = null;
      fileEx = null;
      mode = CLOSED;
    }
  }

  /**
   * Flushes a file. This causes any pending data to be written to disk. Calling this method too much may decrease the
   * performance. Has no effect on JavaSE.
   */
  public void flush() throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }
    if (mode == READ_ONLY) {
      throw new IOException("Operation cannot be used in READ_WRITE mode");
    }

    // do nothing
  }

  /**
   * Recursively creates a directory that is represented by the current file. The file must have been open in DONT_OPEN
   * mode (remember that the constructor File(String) already opens the file in DONT_OPEN mode).
   * 
   * @throws IOException
   *            If the file was closed, or if it was open in anything else than DONT_OPEN, or if the directory already
   *            exists, or if the directories could not be created.
   *            <p>
   *            Example:
   * 
   *            <pre>
   * new File(&quot;/my/new/recursive/folder&quot;).createDir();
   * </pre>
   */

  final public void createDir() throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode != DONT_OPEN) {
      throw new IOException("Operation can ONLY be used in DONT_OPEN mode");
    }

    java.io.File fileRef4Java = (java.io.File) fileRef;
    try {
      if (fileRef4Java.exists()) {
        throw new IOException("Directory already exists");
      }
      if (!fileRef4Java.mkdirs()) {
        throw new IOException("Could not create all the directories listed on the path");
      }
    } catch (java.lang.SecurityException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Deletes the file or directory (which must be empty). The file is automatically closed before it is deleted. The
   * file could have been opened in any of the available modes, except READ_ONLY. Example:
   * 
   * <pre>
   * new File(&quot;/my/file.c&quot;).delete();
   * </pre>
   */

  final public void delete() throws FileNotFoundException, IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == READ_ONLY) {
      throw new IOException("Operation cannot be used in READ_ONLY mode");
    }

    java.io.File fileRef4Java = (java.io.File) fileRef;
    if (mode != DONT_OPEN) {
      this.close();
    }
    try {
      if (!fileRef4Java.exists()) {
        throw new FileNotFoundException(path);
      }
      if (!fileRef4Java.delete()) {
        throw new IOException("Could not remove the file. Possible reason: "
            + (fileRef4Java.isDirectory() ? "The directory is not empty" : "The file is in use"));
      }
    } catch (java.lang.SecurityException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Returns true if the file exists and false otherwise.<br>
   * <br>
   * Example:
   * 
   * <pre>
   * if (new File("dummy.txt").exists())
   *    ...
   * </pre>
   * 
   * @throws IOException
   */

  final public boolean exists() throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }

    java.io.File fileRef4Java = (java.io.File) fileRef;
    try {
      return fileRef4Java.exists();
    } catch (java.lang.SecurityException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Returns the size of the file in bytes. If the file is a directory (ends with slash, not backslash), it returns the
   * amount of free space in bytes of the card/file system (also works on JDK 1.6 and above). <br>
   * <br>
   * If the total amount is greater than 2 GB, 2 GB is returned. In other cases, if the file is not opened, an
   * exception will be thrown. <br>
   * <br>
   * Examples:
   * 
   * <pre>
   * int freeSpace;
   * if (Settings.platform.equals(&quot;Win32&quot;) || Settings.platform.equals(&quot;Java&quot;))
   *    freeSpace = new File(&quot;c:\\&quot;).getSize();
   * else if (Settings.platform.equals(&quot;PalmOS&quot;))
   *    freeSpace = new File(&quot;\\&quot;, 1).getSize(); // hidden volume
   * else
   *    freeSpace = new File(&quot;\\&quot;).getSize(); // WinCE and Posix
   * </pre>
   */
  final public int getSize() throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (path.endsWith("/")) {
      try {
        long size = ((java.io.File) fileRef).getFreeSpace();
        return (int) (size > 2147483647 ? 2147483647 : size);
      } catch (Throwable t) {
        return 1024 * 1024; // 1mb
      }
    }
    if (mode == DONT_OPEN) {
      throw new IOException("The file can't be open in the DONT_OPEN mode to get its size.");
    }

    java.io.File fileRef4Java = (java.io.File) fileRef;
    try {
      long size = fileRef4Java.length();
      return (int) (size > 2147483647 ? 2147483647 : size);
    } catch (Throwable t) {
      throw new IOException(t.getMessage());
    }
  }

  /** Return the file's path passed in the constructor. */
  public String getPath() {
    return path;
  }

  /**
   * Returns the file's parent, or null if its the root.
   * 
   * @throws IOException
   */
  public File getParent() throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (path.equals("/")) {
      return null;
    }

    java.io.File fileRef4Java = (java.io.File) fileRef;
    return new File(fileRef4Java.getParent());
  }

  /**
   * Returns true if the file is a directory and false otherwise. The file must have been open in DONT_OPEN mode.
   * 
   * @throws IOException
   */
  final public boolean isDir() throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode != DONT_OPEN) {
      return false;
    }
    java.io.File fileRef4Java = (java.io.File) fileRef;
    try {
      return fileRef4Java.isDirectory();
    } catch (java.lang.SecurityException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Lists the files contained in a directory. The strings returned are the names of the files and directories
   * contained within this directory. Paths are suffixed by a slash.
   */
  final public String[] listFiles() throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode != DONT_OPEN) {
      throw new IOException("Operation can ONLY be used in DONT_OPEN mode");
    }

    java.io.File fileRef4Java = (java.io.File) fileRef;
    if (!fileRef4Java.exists()) {
      throw new FileNotFoundException(path);
    }
    if (!fileRef4Java.isDirectory()) {
      throw new IOException("File is not a directory: " + path);
    }
    try {
      String[] files = fileRef4Java.list();
      path = path.replace('\\', '/');
      String pathWithSlash = path.endsWith("/") ? path : (path + '/'); // guich@564_5: check if the path ends with /
      if (files != null) {
        for (int i = 0; i < files.length; i++) {
          if (new java.io.File(pathWithSlash + files[i]).isDirectory()) {
            files[i] += '/'; // add a / to a directory end
          }
        }
      }
      return files;
    } catch (java.lang.SecurityException e) {
      throw new IOException(e.getMessage());
    }
  }

  /** If this is a file, returns true if the file has 0 bytes. If this is a folder, returns true if there are 
   * no files nor folders inside of it.
   * @since TotalCross 1.27
   */
  final public boolean isEmpty() throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    java.io.File fileRef4Java = (java.io.File) fileRef;
    if (fileRef4Java.isDirectory()) {
      String[] list = fileRef4Java.list();
      return list == null || list.length == 0;
    } else {
      return fileRef4Java.length() == 0;
    }
  }

  @Override
  final public int readBytes(byte b[], int off, int len) throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }
    if (b == null) {
      throw new java.lang.NullPointerException("Argument 'b' cannot have a null value");
    }
    if (off < 0 || len < 0 || off + len > b.length) {
      throw new java.lang.ArrayIndexOutOfBoundsException();
    }
    if (len == 0) {
      return 0; // flsobral@tc113_43: return 0 if asked to read 0.
    }

    java.io.RandomAccessFile fileEx4Java = (java.io.RandomAccessFile) fileEx;
    try {
      int ret = fileEx4Java.read(b, off, len);
      pos += ret;
      return ret;
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Renames the file. You must give the full directory specification for the file, to keep compatibility between all
   * platforms. WinCE platform lets you move a file using rename, while Palm OS does not let you move the file. File is
   * automatically closed prior to renaming. After this operation, this File object is invalid.
   * 
   * Cannot be used in READ_ONLY mode.
   * 
   * @param path
   *           the new name of the file.
   */
  final public void rename(String path) throws IllegalArgumentIOException, IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == READ_ONLY) {
      throw new IOException("Operation cannot be used in READ_ONLY mode");
    }
    if (path == null) {
      throw new java.lang.NullPointerException("Argument 'path' cannot have a null value");
    }
    if (path.length() == 0 || path.length() > 255) {
      throw new IllegalArgumentIOException("path", path);
    }

    path = path.replace('\\', '/');
    java.io.File fileRef4Java = (java.io.File) fileRef;
    this.close();
    try {
      fileRef4Java.renameTo(new java.io.File(path));
    } catch (java.lang.SecurityException e) {
      throw new IOException(e.getMessage());
    }
  }

  @Override
  public int getPos() throws totalcross.io.IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }

    return pos;
  }

  /**
   * Sets the file pointer for read and write operations to the given position. The position passed is an absolute
   * position, in bytes, from the beginning of the file. To set the position to just after the end of the file, you can
   * call:
   * 
   * <pre>
   * file.setPos(file.getSize());
   * </pre>
   * 
   * Note: if you plan to change the file size using setPos, you must write something on the new size to effectively
   * change the size. For example, on some devices if you call setPos and then read (assuming that the new pos is past
   * the end of the file, the read method will fail. Here's a code that will change the size for sure:
   * 
   * <pre>
   * private static byte[] zeros = new byte[4096];
   * 
   * public void setSize(int newSize)
   * {
   *    int size = f.getSize();
   *    f.setPos(newSize - 1); // note: setPos(1) makes the file 2 bytes long (0, 1)
   *    f.setPos(size);
   *    for (int dif = newSize - size, n = 0; dif &gt; 0; dif -= n)
   *       n = f.writeBytes(zeros, 0, dif &gt; zeros.length ? zeros.length : dif);
   * }
   * </pre>
   */
  @Override
  final public void setPos(int pos) throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }
    if (pos < 0) {
      throw new IOException("Argument 'pos' cannot be negative");
    }

    java.io.RandomAccessFile fileEx4Java = (java.io.RandomAccessFile) fileEx;
    try {
      if (pos > fileEx4Java.length()) {
        fileEx4Java.setLength(pos + 1); // suppose the file is empty. seeking to pos 8 makes the file with 9 bytes (0-8).
      }
      fileEx4Java.seek(pos);
      this.pos = pos;
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  @Override
  final public void setPos(int offset, int origin) throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }

    try {
      java.io.RandomAccessFile fileEx4Java = (java.io.RandomAccessFile) fileEx;
      int newPos;
      int fileLen = (int) fileEx4Java.length();

      switch (origin) {
      case SEEK_SET:
        newPos = offset;
        break;
      case SEEK_CUR:
        newPos = pos + offset;
        break;
      case SEEK_END:
        newPos = fileLen + offset - 1;
        break;
      default:
        throw new IllegalArgumentException();
      }

      if (newPos < 0) {
        throw new IOException();
      }
      if (newPos >= fileLen) {
        fileEx4Java.setLength(newPos + 1);
      }
      fileEx4Java.seek(newPos);
      pos = newPos;
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  @Override
  final public int writeBytes(byte b[], int off, int len) throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }
    if (mode == READ_ONLY) {
      throw new IOException("Operation cannot be used in READ_ONLY mode");
    }
    if (b == null) {
      throw new java.lang.NullPointerException("Argument 'b' cannot have a null value");
    }
    if (off < 0 || len < 0 || off + len > b.length) {
      throw new java.lang.ArrayIndexOutOfBoundsException();
    }
    if (len == 0) {
      return 0;
    }

    java.io.RandomAccessFile fileEx4Java = (java.io.RandomAccessFile) fileEx;
    try {
      fileEx4Java.write(b, off, len);
      pos += len;
      return len;
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Sets the attributes of the opened file - cannot be used with <code>DONT_OPEN</code>.<br>
   * Platform specific notes:
   * <ul>
   * <li>JDK - This method has no effect on any file when running on JDK, it only checks if the object state and the
   * received argument are valid.
   * <li>BLACKBERRY - Supports only <code>ATTR_HIDDEN</code> and <code>ATTR_READ_ONLY</code>.
   * <li>LINUX, IPHONE, and ANDROID - The attributes <code>ATTR_HIDDEN</code> and <code>ATTR_ARCHIVE</code> are not
   * supported by Unix based systems. Using them will not throw an exception, but it will have no effect on the file.
   * <li>PALMOS - Avoid using the attribute <code>ATTR_READ_ONLY</code> on files located in the device's internal
   * storage. Marking a file as read only affects also its attributes, which means it can't be undone. The only way to
   * remove a file marked as read only is performing a hard reset.
   * </ul>
   * 
   * @param attr
   *           one ore more ATTR_xxx constants ORed together.
   * @see #ATTR_ARCHIVE
   * @see #ATTR_HIDDEN
   * @see #ATTR_READ_ONLY
   * @see #ATTR_SYSTEM
   */
  final public void setAttributes(int attr) throws IllegalArgumentIOException, IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }
    if (mode == READ_ONLY) {
      throw new IOException("Operation cannot be used in READ_ONLY mode");
    }
    if (attr < 0 || attr > 15) {
      throw new IllegalArgumentIOException("attr", null);
    }
  }

  /**
   * Gets this file attributes. The file must be opened in a mode different of DONT_OPEN.
   * <p>
   * This method does not work on desktop, but the arguments are still checked.
   * 
   * @return The file attributes ORed together.
   * @see #ATTR_ARCHIVE
   * @see #ATTR_HIDDEN
   * @see #ATTR_READ_ONLY
   * @see #ATTR_SYSTEM
   */
  final public int getAttributes() throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }

    return 0;
  }

  /**
   * Sets the time attribute of the opened filed - cannot be used with <code>DONT_OPEN</code> or <code>READ_ONLY</code>.<br>
   * Platform specific notes:
   * <ul>
   * <li>JDK - This method has no effect on any file when running on JDK, it only checks if the object state and the
   * received arguments are valid.
   * <li>WINCE - Supports only <code>TIME_MODIFIED</code> if the file is stored on the device's non-volatile memory. If the file is stored in an 
   * external FAT storage, it also supports <code>TIME_CREATED</code>.
   * <li>BLACKBERRY - Not supported.
   * <li>LINUX, IPHONE and ANDROID - Unix based systems do not keep record of the file's creation time. Attempting to
   * do so will not thrown an exception, but it will have no effect on the file.
   * </ul>
   * 
   * @param whichTime
   *           One or more of the TIME_xxx constants, ORed together.
   * @param time
   *           The new time.
   * 
   * @see #TIME_ALL
   * @see #TIME_ACCESSED
   * @see #TIME_CREATED
   * @see #TIME_MODIFIED
   */
  final public void setTime(byte whichTime, totalcross.sys.Time time) throws IllegalArgumentIOException, IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }
    if (mode == READ_ONLY) {
      throw new IOException("Operation cannot be used in READ_ONLY mode");
    }
    if (time == null) {
      throw new java.lang.NullPointerException("Argument 'time' cannot have a null value");
    }
    if (whichTime != 0x1 && whichTime != 0x2 && whichTime != 0x4 && whichTime != 0xF) {
      throw new IllegalArgumentIOException("whichTime", null);
    }
  }

  /**
   * Retrieves the specified time attribute of the opened file - cannot be used with <code>DONT_OPEN</code>.<br>
   * <ul>
   * <li>JDK - If the object state and the received argument are valid, it will always return the time of the last
   * modification.
   * <li>WINCE - Supports only <code>TIME_MODIFIED</code> if the file is stored on the device's non-volatile memory. If the file is stored in an 
   * external FAT storage, it also supports <code>TIME_CREATED</code>.
   * <li>BLACKBERRY - Supports only <code>TIME_MODIFIED</code>.
   * <li>LINUX, IPHONE and ANDROID - Unix based systems do not keep record of the file's creation time. Using the
   * constant <code>TIME_CREATED</code> will return the last time the file was changed, which is updated when changes
   * are made to the file's inode (owner, permissions, etc.), and also when the contents of the file are modified.<br>
   * The constant <code>TIME_MODIFIED</code> returns the last time the contents of the file were modified.<br>
   * </ul>
   * 
   * @param whichTime
   *           value must be <code>TIME_ACCESSED</code>, <code>TIME_CREATED</code> or <code>TIME_MODIFIED</code>. Any
   *           other value will result in an exception.
   * 
   * @see #TIME_ACCESSED
   * @see #TIME_CREATED
   * @see #TIME_MODIFIED
   */
  final public totalcross.sys.Time getTime(byte whichTime) throws IllegalArgumentIOException, IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }
    if (whichTime != 0x1 && whichTime != 0x2 && whichTime != 0x4) {
      throw new IllegalArgumentIOException("whichTime", null);
    }

    java.util.Calendar cal = java.util.Calendar.getInstance();
    java.io.File fileRef4Java = (java.io.File) fileRef;
    cal.setTime(new java.util.Date(fileRef4Java.lastModified()));
    return new totalcross.sys.Time(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1,
        cal.get(java.util.Calendar.DATE), cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE),
        cal.get(java.util.Calendar.SECOND), cal.get(java.util.Calendar.MILLISECOND));
  }

  /**
   * Returns the volume File for the Windows CE and Pocket PC, and BlackBerry devices. On these devices, the volume has a special
   * folder name, but since there's no system call that informs this, we must just test the existence of each folder,
   * returning the first one that exists. You can set the winceVols string array to the ones you want to be searched. <br>
   * <br>
   * To access the card on Android devices, prefix the path with <code>/sdcard</code>. Be sure that the sdcard is NOT MOUNTED, otherwise your application will not have access to it.
   * Some android devices have more than one sdcard, an internal and an external ones. On such devices, /sdcard is the internal one; to find the external path, you must get into the device
   * because there's no API to get it. For example, on Galaxy devices, it is /mnt/extSdCard.
   * 
   * @return The File object which references the volume, ended with backslash, or null if none found.
   * @see #winceVols
   */
  final public static File getCardVolume() throws IOException {
    return null;
  }

  /**
   * Sets the file size, growing the size or truncating it.
   * 
   * @param newSize
   *           The new file size.
   * 
   * @since SuperWaba 5.83
   */
  final public void setSize(int newSize) throws IOException {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle");
    }
    if (mode == DONT_OPEN) {
      throw new IOException("Operation cannot be used in DONT_OPEN mode");
    }
    if (mode == READ_ONLY) {
      throw new IOException("Operation cannot be used in READ_ONLY mode");
    }

    java.io.RandomAccessFile fileEx4Java = (java.io.RandomAccessFile) fileEx;
    try {
      fileEx4Java.setLength(newSize); // suppose the file is empty. seeking to pos 8 makes the file with 9 bytes (0-8).
      fileEx4Java.seek(newSize);
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Returns the card serial number for the given slot.
   * <p>
   * This method only works on Palm OS.
   * 
   * @param slot
   *           The slot number, or -1 to use the last slot (which is usually an external card if the device has such
   *           slot).
   * @since TotalCross 1.0
   */
  final public static String getCardSerialNumber(int slot) throws IllegalArgumentIOException, IOException {
    if (slot < -1) {
      throw new IllegalArgumentIOException("slot", Convert.toString(slot));
    }
    return null;
  }

  @Override
  protected void finalize() {
    try {
      if (mode != INVALID) {
        this.close();
      }
    } catch (Throwable t) {
    }
  }

  /**
   * Returns the slot number passed in the constructor, or -1 if no slot was given.
   * 
   * @since TotalCross 1.0
   */
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

  /**
   * Returns a recursive list of all files inside the given directory (including it). The array is sorted upon return.
   * 
   * @since TotalCross 1.15
   */
  public static String[] listFiles(String dir) throws IOException { // guich@tc115_92
    return listFiles(dir, true);
  }

  /**
   * Lists all the files in the specified directory, and also the files in the subdirectories if recursive is true.
   * 
   * @param dir
   * @param recursive
   * @throws IOException
   */
  public static String[] listFiles(String dir, boolean recursive) throws IOException {
    Vector files = new Vector(50);
    dir = Convert.appendPath(dir, "/");
    files.addElement(dir);
    listFiles(dir, files, recursive);
    files.qsort(Convert.SORT_STRING_NOCASE);
    return (String[]) files.toObjectArray();
  }

  /**
   * Deletes a directory and all its subdirectories and files.
   * If you have problems trying to recreate the directory, be sure to call <code>Vm.gc()</code> 
   * after calling this method.
   * 
   * @since TotalCross 1.15
   */
  public static void deleteDir(String dir) throws IOException // guich@tc115_92
  {
    String[] files = listFiles(dir);
    for (int i = files.length; --i >= 0;) {
      new File((String) files[i]).delete();
    }
  }

  /** List the root drives. If there are no roots, returns null.
   * Works on Win32, Java, and Blackberry platforms.
   * @since TotalCross 1.22
   */
  public static String[] listRoots() // fabio@tc122_14
  {
    java.io.File[] roots = java.io.File.listRoots();
    if (roots == null) {
      return null;
    }
    String[] result = new String[roots.length];
    for (int i = roots.length; --i >= 0;) {
      result[i] = roots[i].getPath();
    }
    return result;
  }

  /** Copies the current file to the given one. 
   * You must close both files after calling this method.
   * Here's a sample of how to copy a file:
   * <pre>
      File src = new File(srcFileName,File.READ_WRITE);
      File dest = new File(destFileName,File.CREATE_EMPTY);
      src.copyTo(dest);
      src.close();
      dest.close();
   * </pre>
   * This method is thread-safe.
   * @see #moveTo(File)
   * @since TotalCross 1.27
   */
  public void copyTo(File dest) throws IOException // guich@tc126_8
  {
    byte[] buf = new byte[4096];
    int n = 0;
    while ((n = readBytes(buf, 0, buf.length)) > 0) {
      dest.writeBytes(buf, 0, n);
    }
  }

  /** Moves the current file to the given one (the original file is deleted). 
   * You must explicitly close the destination file after this operation is done.
   * Here's a sample of how to move a file:
   * <pre>
      File src = new File(srcFileName,File.READ_WRITE);
      File dest = new File(destFileName,File.CREATE_EMPTY);
      src.moveTo(dest);
      // src.close(); - not needed! src was deleted
      dest.close();
   * </pre>
   * This method is thread-safe.
   * @see #copyTo(File)
   * @since TotalCross 1.27
   */
  public void moveTo(File dest) throws IOException // guich@tc126_8
  {
    if (mode == READ_ONLY) {
      throw new IOException("Operation cannot be used in READ_ONLY mode");
    }
    copyTo(dest);
    delete();
  }

  /** A handy method to call copyTo creating two File instances and closing them.
   * The target file is erased if it exists.
   * This method is thread-safe. If you want to have more control, use the copyTo method
   * @see #copyTo(File)
   * @since TotalCross 1.27
   */
  public static void copy(String src, String dst) throws IOException // guich@tc126_43
  {
    try (File fin = new File(src, READ_ONLY)) {
      try (File fout = new File(dst, CREATE_EMPTY)) {
        fin.copyTo(fout);
      }
    }
  }

  /** A handy method to call moveTo creating two File instances and closing them.
   * The target file is erased if it exists.
   * This method is thread-safe. If you want to have more control, use the other moveTo method
   * @see #moveTo(File)
   * @since TotalCross 1.27
   */
  public static void move(String src, String dst) throws IOException // guich@tc126_43
  {
    try (File fin = new File(src, READ_WRITE)) {
      try (File fout = new File(dst, CREATE_EMPTY)) {
        fin.moveTo(fout);
      }
    }
  }

  /** Applies the given permissions to this file. Works only on Unix-based operating systems: Linux, Android, and iOS. On JDK 1.6, the first number (user) is applied to all groups.
   * Below you see a table with some chmod values (r = read, w = write, x = execute).
   * <pre>
   * Number   Permission
       000     ---------
       400     r--------
       444     r--r--r--
       600     rw-------
       620     rw--w----
       640     rw-r-----
       644     rw-r--r--
       645     rw-r--r-x
       646     rw-r--rw-
       650     rw-r-x---
       660     rw-rw----
       661     rw-rw---x
       662     rw-rw--w-
       663     rw-rw--wx
       664     rw-rw-r--
       666     rw-rw-r--
       700     rwx------
       750     rwxr-x---
       755     rwxr-xr-x
       777     rwxrwxrwx
   * </pre>
   * The numbers represents a group of 3. The first number is the permission for <i>user</i>, the second number for <i>group</i>, and the third number for <i>others</i>
   * These are the possible permission values for each number:
   * <pre>
       Permission   Binary  Decimal
         ---         000       0
         --x         001       1
         -w-         010       2
         -wx         011       3
         r--         100       4
         r-x         101       5
         rw-         110       6
         rwx         111       7    
   * </pre>
   * Failing to change the permission returns -1.
   * <br><br>
   * Here's a sample:
   * <pre>
      try
      {
         // testing in a folder
         File f = new File(Settings.appPath);
         add(new Label("mods of appPath = "+f.chmod(-1)),CENTER,CENTER);
  
         // testing in a file
         String name = "test";
         f = new File(Settings.appPath+'/'+name,File.CREATE_EMPTY);
         int m0 = f.chmod(777); // change it
         int m1 = f.chmod(-1); // retrieve the changed value
         add(new Label("mods of "+name+" = "+m0+" -> "+m1+" (777)"),CENTER,AFTER+5);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   * </pre>
   * @param mod The modifiers you want to set in DECIMAL, or -1 to just return the current ones.
   * @return The modifiers that were set before you called this method (or the current modifiers, if -1 is being passed). Some platforms may return more than 3 digits, indicating extra attributes (for example, if it's a file or a directory).
   * @since TotalCross 1.27
   */
  public int chmod(int mod) throws IOException // guich@tc126_16
  {
    if (mode == INVALID || mode == CLOSED) {
      throw new IOException("Invalid file handle"); //flsobral@tc126: object must be valid.
    }

    java.io.File f = (java.io.File) fileRef;
    if (!f.exists()) {
      throw new FileNotFoundException(path); //flsobral@tc126: throw exception if the file does not exist.
    }

    try {
      int current = (f.canExecute() ? 1 : 0) | (f.canWrite() ? 2 : 0) | (f.canRead() ? 4 : 0);
      current = current + current * 10 + current * 100;
      mod /= 100; // 753 -> 7
      f.setExecutable((mod & 1) != 0, false);
      f.setWritable((mod & 2) != 0, false);
      f.setReadable((mod & 4) != 0, false);
      return current;
    } catch (java.lang.SecurityException e) {
      throw new IOException(e.getMessage()); //flsobral@tc126: throw exception on error.
    }
  }

  /** Reads the entire file into a byte array and closes itself. A handy method that can be used like this:
   * <pre>
   * byte[] bytes = new File(...,File.READ_ONLY).readAndClose();
   * </pre>
   * The only drawback is that this method consumes lots of memory if the file is big; use it carefully.
   * @since TotalCross 1.53
   */
  @Deprecated
  public byte[] readAndClose() throws IOException {
    try {
      return read();
    } finally {
      close();
    }
  }

  /** Reads the entire file into a byte array and DELETES itself. A handy method that can be used like this:
   * <pre>
   * byte[] bytes = new File(...,File.READ_ONLY).readAndDelete();
   * </pre>
   * The only drawback is that this method consumes lots of memory if the file is big; use it carefully.
   * @since TotalCross 1.53
   */
  @Deprecated
  public byte[] readAndDelete() throws IOException {
    try {
      return read();
    } finally {
      delete();
    }
  }

  /** Writes byte array to this file and closes itself. A handy method that can be used like this:
   * <pre>
   * new File(...,File.CREATE_EMPTY).writeAndClose(Vm.getFile("myfile.txt"));
   * </pre>
   * The only drawback is that this method consumes lots of memory if the file is big; use it carefully.
   * @since TotalCross 1.53
   */
  @Deprecated
  public void writeAndClose(byte[] bytes) throws IOException {
    try {
      writeBytes(bytes, 0, bytes.length);
    } finally {
      close();
    }
  }

  /** Reads the file and returns a byte array with its contents.
   * @since TotalCross 3.1
   */
  @Deprecated
  public byte[] read() throws IOException {
    int len = getSize();
    byte[] ret = new byte[len];
    setPos(0);
    readBytes(ret, 0, len);
    return ret;
  }
}
