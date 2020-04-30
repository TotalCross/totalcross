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

package totalcross.sys;

/**
 * This exception is thrown when <code>Event.handleOneEvent()</code> finds an event that requires the application to exit. The main purpose of this 
 * event is to unwind the stack all the way back to the main event loop so that the application can exit properly. It is mostly used to exit the VM 
 * when a window is open with the <code>popup()</code> method.
 * <p>
 * WARNING: DO NOT CATCH THIS EXCEPTION IN YOUR APP UNLESS YOU KNOW WHAT YOU ARE DOING.
 */

public class AppExitException extends RuntimeException {
  /** 
   * Constructs an empty Exception. 
   */
  public AppExitException() {
    super();
  }

  /** 
   * Constructs an exception with the given message. 
   * 
   * @param msg The error message.
   */
  public AppExitException(String msg) {
    super(msg);
  }
}