/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.util;

/** Comparable interface that must be implemented by Objects that can be compared to another one.
 * @see totalcross.sys.Convert#qsort(Object[], int, int)
 * @see totalcross.sys.Convert#qsort(Object[], int, int, int)
 * @see totalcross.sys.Convert#qsort(Object[], int, int, int, boolean)
 */

public interface Comparable
{
   /** Must return &gt; 0 if this object is greater than the other one, &lt; 0 if its smaller, and 0 if they are equal. */
   public int compareTo(Object other) throws ClassCastException;
}
