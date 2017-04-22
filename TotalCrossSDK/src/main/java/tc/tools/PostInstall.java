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



import totalcross.*;
import totalcross.sys.*;

public class PostInstall implements MainClass
{
   public void _onTimerTick(boolean canUpdate)
   {
   }

   public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp)
   {
   }

   public void appEnding()
   {
   }

   public void appStarting(int time)
   {
      try
      {                 
         // create the apk file
         String tcpath = (Settings.appPath).replace('/','\\');
         tcpath = Convert.replace(tcpath,"\\dist\\vm\\win32","");
         String target = tcpath+"\\etc\\tools\\Android\\adb.exe install -r \"%1\"";
         Registry.set(Registry.HKEY_CURRENT_USER, "Software\\Classes\\*\\shell\\Install APK\\command","",target);
         
      }
      catch (Exception e)
      {
         Vm.alert(e+" "+e.getMessage());
      }
   }

}
