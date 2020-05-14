// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

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
      webview.getSettings().setJavaScriptEnabled(true);
      webview.loadUrl(getIntent().getExtras().getString("url"));
   }
}
