/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.              *
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

package jdkcompat.lang;

import java.io.Serializable;

public class AssertionError4D extends Error implements Serializable {
  private static final long serialVersionUID = -78080685734455754L;

  public AssertionError4D() {
    super();
  }

  public AssertionError4D(String message, Throwable cause) {
    super(message, cause);
  }

  public AssertionError4D(boolean detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(char detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(double detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(float detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(int detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(long detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError4D(Object detailMessage) {
    this(String.valueOf(detailMessage));
  }

  private AssertionError4D(String message) {
    super(message);
  }

}
