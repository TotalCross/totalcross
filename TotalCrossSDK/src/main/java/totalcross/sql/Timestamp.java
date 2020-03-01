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

package totalcross.sql;

import totalcross.sys.Time;
import totalcross.util.Date;

public class Timestamp extends Date {
  long t;

  public Timestamp(Time t) {
  }

  public Timestamp(long time) {
    t = time;
  }

  @Override
  public long getTime() {
    return t;
  }

  @Override
  public String toString() {
    return super.toString() + ", ts: " + t;
  }

  /*   public static Timestamp valueOf(String s)
   {
   }*/
  public int getNanos() {
    return 0;
  }

  public void setNanos(int n) {
  }

  public boolean equals(Timestamp ts) {
    return ts.t == t;
  }

  @Override
  public boolean equals(Object ts) {
    return ts instanceof Timestamp && ((Timestamp) ts).t == t;
  }

  public boolean before(Timestamp ts) {
    return t < ts.t;
  }

  public boolean after(Timestamp ts) {
    return t > ts.t;
  }

}
