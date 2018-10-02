/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package totalcross.android;

import android.app.*;
import android.os.*;
import android.view.*;
import android.webkit.*;

public class WebViewer extends Activity 
{
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      WebView webview = new WebView(this);
      setContentView(webview);
      if (Loader.isFullScreen)
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
      webview.loadUrl(getIntent().getExtras().getString("url"));
   }
}
