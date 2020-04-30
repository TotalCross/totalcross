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
         AndroidUtils.checkInstall(getApplicationContext());
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