// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util;

/** Comparable interface that must be implemented by Objects that can be compared to another one.
 * @see totalcross.sys.Convert#qsort(Object[], int, int)
 * @see totalcross.sys.Convert#qsort(Object[], int, int, int)
 * @see totalcross.sys.Convert#qsort(Object[], int, int, int, boolean)
 */

public interface Comparable {
  /** Must return &gt; 0 if this object is greater than the other one, &lt; 0 if its smaller, and 0 if they are equal. */
  public int compareTo(Object other) throws ClassCastException;
}
