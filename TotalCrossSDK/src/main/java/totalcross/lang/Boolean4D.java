/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
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

package totalcross.lang;

public class Boolean4D {
  public static final Class<Boolean> TYPE = Boolean.class;
  public static final Boolean4D TRUE = new Boolean4D(true);
  public static final Boolean4D FALSE = new Boolean4D(false);
  boolean v;

  public Boolean4D(boolean v) {
    this.v = v;
  }

  public boolean booleanValue() {
    return v;
  }

  @Override
  public boolean equals(Object o) {
    return o != null && o instanceof Boolean4D && ((Boolean4D) o).v == this.v;
  }

  @Override
  public int hashCode() {
    return v ? 1 : 0;
  }

  @Override
  public String toString() {
    return String.valueOf(v);
  }

  public static Boolean4D valueOf(boolean b) {
    return new Boolean4D(b);
  }

  public static Boolean4D valueOf(String s) {
    return new Boolean4D(s != null && s.equalsIgnoreCase("true"));
  }
  
  public static int compare(boolean a, boolean b) {
	if (!a && b) {
		return -1;
	} else if (a && !b) {
		return 1;
	} else {
		return 0;
	}
  }
}
