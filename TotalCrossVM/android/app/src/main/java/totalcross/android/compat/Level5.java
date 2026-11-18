// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android.compat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


// must be called from totalcross.android.Loader thread

public abstract class Level5
{
   private static volatile Level5 instance;
   private static final Map<BluetoothRequest, Boolean> outstandingRequests = new ConcurrentHashMap<>();
   private static volatile boolean shutdownRequested;
   
   public static synchronized Level5 getInstance()
   {
      if (instance == null)
         instance = new Level5Impl();
      return instance;
   }
   
   public static final int BT_IS_SUPPORTED = 100;
   public static final int BT_ACTIVATE = 101;
   public static final int BT_GET_PAIRED_DEVICES = 102;
   public static final int BT_GET_UNPAIRED_DEVICES = 103;
   public static final int BT_MAKE_DISCOVERABLE = 104;
   public static final int BT_CONNECT = 105;
   public static final int BT_DEACTIVATE = 106;
   public static final int BT_WRITE = 107;
   public static final int BT_READ = 108;
   public static final int BT_CLOSE = 109;
   public static final int BT_IS_RADIO_ON = 110;
   public static final int BT_IS_DISCOVERABLE = 111;
   public static final int BT_SERVER_ACCEPT = 112;
   public static final int BT_SERVER_CLOSE = 113;

   public static final int ERROR = -999;
   public static final int INVALID_PASSWORD = -998;
   public static final int NO_ERROR = 0;

   public static void trackRequest(BluetoothRequest request)
   {
      if (request == null) return;
      if (shutdownRequested)
      {
         request.cancel();
         return;
      }
      outstandingRequests.put(request, Boolean.TRUE);
      request.registerCompletionCallback(() -> outstandingRequests.remove(request));
      if (shutdownRequested) request.cancel();
   }
   
   public abstract void processMessage(BluetoothRequest request);

   public abstract boolean handleActivityResult(int requestCode, int resultCode);

   public void onActivityResult(int requestCode, int resultCode)
   {
      handleActivityResult(requestCode, resultCode);
   }

   public void shutdown()
   {
      shutdownRequested = true;
      for (BluetoothRequest request : outstandingRequests.keySet())
         request.cancel();
   }
}
