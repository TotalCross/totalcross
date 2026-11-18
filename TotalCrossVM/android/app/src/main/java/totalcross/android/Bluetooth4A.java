// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android;

import android.os.Looper;
import android.os.Message;

import totalcross.Launcher4A;
import totalcross.android.compat.BluetoothRequest;
import totalcross.android.compat.Level5;

// may be running in tcvm android's thread (static methods in Launcher4A)

public class Bluetooth4A
{
   private static final long REQUEST_WAIT_SLICE_MS = 20L;
   private static BluetoothRequest callLoaderAndWait(int callType, String param, byte[] array, int ofs, int len)
   {
      BluetoothRequest request = new BluetoothRequest(callType, param, array, ofs, len);
      if (Looper.myLooper() == Looper.getMainLooper())
      {
         request.complete(Level5.ERROR, null);
         return request;
      }
      Loader loader = Launcher4A.loader;
      if (loader == null || loader.achandler == null)
      {
         request.complete(Level5.ERROR, null);
         return request;
      }
      Level5.trackRequest(request);
      Message message = loader.achandler.obtainMessage(Loader.LEVEL5, request);
      if (!loader.achandler.sendMessage(message))
      {
         request.complete(Level5.ERROR, null);
         return request;
      }
      while (!request.isComplete())
      {
         if (request.isDeadlineExpired())
         {
            request.cancel();
            break;
         }
         try
         {
            request.await(REQUEST_WAIT_SLICE_MS);
         }
         catch (InterruptedException interrupted)
         {
            request.cancel();
            Thread.currentThread().interrupt();
            break;
         }
         if (request.isDeadlineExpired())
         {
            request.cancel();
            break;
         }
         if (!request.isComplete() && Launcher4A.eventIsAvailable())
            Launcher4A.pumpEvents();
      }
      return request;
   }
   
   private static BluetoothRequest callLoaderAndWait(int callType, String param)
   {
      return callLoaderAndWait(callType,param, null,0,0);
   }

   private static BluetoothRequest callLoaderAndWait(int callType)
   {
      return callLoaderAndWait(callType,null,null,0,0);
   }

   // used to control bluetooth hardware
   
   public static int isSupported()
   {
      return callLoaderAndWait(Level5.BT_IS_SUPPORTED).getResultCode();
   }
   
   public static int isRadioOn()
   {
      return callLoaderAndWait(Level5.BT_IS_RADIO_ON).getResultCode();
   }
   
   public static int isDiscoverable()
   {
      return callLoaderAndWait(Level5.BT_IS_DISCOVERABLE).getResultCode();
   }
   
   public static int activate()
   {
      return callLoaderAndWait(Level5.BT_ACTIVATE).getResultCode();
   }
   
   public static int deactivate()
   {
      return callLoaderAndWait(Level5.BT_DEACTIVATE).getResultCode();
   }
   
   public static String[] getPairedDevices()
   {
      return (String[])callLoaderAndWait(Level5.BT_GET_PAIRED_DEVICES).getPayload();
   }
   
   public static String[] getUnpairedDevices()
   {
      return (String[])callLoaderAndWait(Level5.BT_GET_UNPAIRED_DEVICES).getPayload();
   }
   
   public static int makeDiscoverable()
   {
      return callLoaderAndWait(Level5.BT_MAKE_DISCOVERABLE).getResultCode();
   }

   public static int connectTo(String addr)
   {
      return callLoaderAndWait(Level5.BT_CONNECT,addr).getResultCode();
   }
   
   // used for a specific connection
   
   public static void close(String addr)
   {
      callLoaderAndWait(Level5.BT_CLOSE, addr);
   }
   
   public static int read(String addr, byte[] array, int ofs, int len)
   {
      return callLoaderAndWait(Level5.BT_READ, addr, array, ofs, len).getResultCode();
   }
   
   public static int write(String addr, byte[] array, int ofs, int len)
   {
      return callLoaderAndWait(Level5.BT_WRITE, addr, array, ofs, len).getResultCode();
   }
   
   // server methods
   
   public static int serverAccept(String uuid)
   {
      return callLoaderAndWait(Level5.BT_SERVER_ACCEPT, uuid).getResultCode();
   }

   public static void serverClose()
   {
      callLoaderAndWait(Level5.BT_SERVER_CLOSE);
   }
}
