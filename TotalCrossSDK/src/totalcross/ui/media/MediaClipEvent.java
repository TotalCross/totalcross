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



package totalcross.ui.media;

import totalcross.sys.Vm;

/**
 * Events posted by a soundclip control.
 * @since SuperWaba 5.66
 */

public class MediaClipEvent extends totalcross.ui.event.Event
{
   /** The event type when the play starts. */
   public static final int STARTED = 550;
   /** The event type when the play ends. */
   public static final int STOPPED = 551;
   /** The event type when the play pauses. */
   public static final int CLOSED = 552;
   public static final int ERROR = 553;
   public static final int END_OF_MEDIA = 554;

   /** Constructs a MediaClipEvent, setting the type and target to the given parameters. */
   public MediaClipEvent(int type, Object target)
   {
     super(type,target,Vm.getTimeStamp());
   }

   /** Constructs an empty MediaClipEvent. */
   public MediaClipEvent()
   {
   }
}
