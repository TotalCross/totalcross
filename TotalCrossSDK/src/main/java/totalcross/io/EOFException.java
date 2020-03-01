/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2012 SuperWaba Ltda.                                      *
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

 package totalcross.io;

/**
 * Signals that an end of file or end of stream has been reached unexpectedly during input.
 * 
 * This exception is mainly used by data input streams to signal end of stream. Note that many other input operations
 * return a special value on end of stream rather than throwing an exception.
 * 
 * @since TotalCross 1.62
 */
public class EOFException extends IOException {
  /**
   * Constructs an EOFException with null as its error detail message.
   */
  public EOFException() {
    super();
  }

  /**
   * Constructs an IOException with the specified detail message.
   * 
   * @param msg
   *           the detail message.
   */
  public EOFException(String msg) {
    super(msg);
  }
}
