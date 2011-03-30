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



package totalcross.phone;

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.PhoneArguments;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import totalcross.Launcher4B;
import totalcross.io.IOException;

final public class Dial4B
{
   static
   {
      Launcher4B.requestAppPermission(ApplicationPermissions.PERMISSION_PHONE);
   }
   
   public static interface Listener
   {
      public void dialStatusChange(String msg);
   }

   public static Listener listener;

   public static void number(String number) throws IOException
   {
      PhoneArguments args = new PhoneArguments(PhoneArguments.ARG_CALL, number);
      Invoke.invokeApplication(Invoke.APP_TYPE_PHONE, args);
   }

   public static void hangup()
   {
   }
}
