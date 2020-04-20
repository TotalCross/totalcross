/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.              *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 2.1    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-2.1.txt                                     *
 *                                                                               *
 *********************************************************************************/

package jdkcompat.io;

import java.io.IOException;

public class UncheckedIOException4D extends RuntimeException {

  public UncheckedIOException4D(IOException cause) {
    super(cause);
  }
  
  public UncheckedIOException4D(String message, IOException cause) {
    super(message, cause);
  }
}
