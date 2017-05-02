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



package litebase.android;

import android.app.*;
import android.os.*;
import android.util.*;
import totalcross.AndroidUtils;

public class Loader extends Activity
{
   public Handler achandler;
   
   /** Called when the activity is first created. */
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      try
      {
         AndroidUtils.initialize(this);
         AndroidUtils.checkInstall();
         setResult(RESULT_OK);
         finish();
      }
      catch (Exception e)
      {
         String stack = Log.getStackTraceString(e);
         AndroidUtils.debug(stack);
         AndroidUtils.error("An exception was issued when launching Litebase. Please inform this stack trace to your software's vendor:\n\n"+stack,true);
         setResult(RESULT_CANCELED);
      }
   }
}