/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

// $Id: AppExitException.java,v 1.7 2011-01-04 13:19:10 guich Exp $

package totalcross.sys;

/**
 * This exception is thrown when Event.handleOneEvent() encounteres an event
 * that requires the app to exit.  The main purpose of this event is to unwind
 * the stack all the way back to the main event loop so that the app can
 * properly exit. It is mostly used to exit the vm when a Window is open with the
 * popup method.
 * <p>
 * WARNING: DO NOT CATCH THIS EXCEPTION IN YOUR APP UNLESS YOU KNOW WHAT YOU
 * ARE DOING.
 */

public class AppExitException extends RuntimeException
{
   /** Constructs an empty Exception. */
   public AppExitException()
   {
      super();
   }

   /** Constructs an exception with the given message. */
   public AppExitException(String msg)
   {
     super(msg);
   }
}