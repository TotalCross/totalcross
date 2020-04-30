// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io;

public interface FileStates {
  public final static int INVALID = 0;
  /**
   * The DONT_OPEN mode allows the exists(), rename(), delete(), listFiles(), createDir(), and isDir() methods to be
   * called without requiring the file to be open for reading or writing.
   * 
   * @see #File(String)
   * @see #File(String,int)
   * @see #File(String,int,int)
   * @see #exists()
   * @see #rename(String)
   * @see #delete()
   * @see #listFiles()
   * @see #createDir()
   * @see #isDir()
   */
  public final static int DONT_OPEN = 1;
  /**
   * Read-write open mode. Works only for files, must not be used for folders.
   * 
   * @see #File(String,int)
   * @see #File(String,int,int)
   */
  public final static int READ_WRITE = 2;
  /**
   * Read-only open mode. Works only for files, must not be used for folders.
   * 
   * @see #File(String,int)
   * @see #File(String,int,int)
   * @since TotalCross 1.38
   */
  public final static int READ_ONLY = 3;
  /**
   * Used to create a file if one does not exist; if the file exists, it is not erased, and the mode is changed to
   * READ_WRITE.
   * 
   * @see #File(String,int)
   * @see #File(String,int,int)
   */
  public final static int CREATE = 4;
  /**
   * Create an empty file; destroys the file if it exists, then the mode is changed to READ_WRITE.
   * 
   * @see #File(String,int)
   * @see #File(String,int,int)
   */
  public final static int CREATE_EMPTY = 5;
  public final static int CLOSED = 6;
}
