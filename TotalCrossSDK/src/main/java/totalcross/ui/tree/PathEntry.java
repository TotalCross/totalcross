// Copyright (C) 2004 Trev Quang Nguyen
// Copyright (C) 2005-2013 SuperWaba Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.tree;

/** This is a class that defines a path and also if its a directory or a file. */
public class PathEntry implements totalcross.util.Comparable {
  /** Defines that this PathEntry is a directory. */
  public static final int DIR = 0;
  /** Defines that this PathEntry is a file. */
  public static final int FILE = 1;

  /** The path of this PathEntry */
  public String value;
  /** The type of this PathEntry */
  public int type;

  /** Constructs a new PathEntry based on the given parameters.
   * @param value the path value
   * @param isDir true if the path is a directory
   */
  public PathEntry(String value, boolean isDir) {
    int len = value.length();
    if (len > 0) // remove the last slash
    {
      char last = value.charAt(len - 1);
      if (last == '/' || last == '\\') {
        value = value.substring(0, len - 1);
      }
    }
    this.value = value;
    this.type = isDir ? DIR : FILE;
  }

  @Override
  public int compareTo(Object arg0) {
    PathEntry otherEntry = (PathEntry) arg0;
    if (otherEntry.type == DIR) // put dir in top of files
    {
      if (type == FILE) {
        return 1;
      }
    } else {
      if (type == DIR) {
        return -1;
      }
    }
    return value.toLowerCase().compareTo(otherEntry.value.toLowerCase());
  }

  @Override
  public String toString() {
    return value;
  }
}
