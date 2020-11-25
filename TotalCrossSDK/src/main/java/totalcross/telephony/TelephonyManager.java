// Copyright (C) 2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.telephony;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.util.concurrent.Lock;

public class TelephonyManager {
   private static TelephonyManager instance;

   private String[] deviceIds;
   private String[] simSerialNumbers;
   private String[] lineNumbers;

   private boolean initialized = false;
   private Lock initializeLock = new Lock();

   private TelephonyManager() {
      synchronized (initializeLock) {
         if (!initialized) {
            initialized = nativeInitialize();
         }
      }
   }

   /** Get the default TelephonyManager. */
   public static TelephonyManager getDefault() {
      if (instance == null) {
         instance = new TelephonyManager();
      }
      return instance;
   }

   @ReplacedByNativeOnDeploy
   private boolean nativeInitialize() {
      return true;
   }

   private String getWithIndex(String[] values, int index) {
      if (values != null && values.length > index) {
         return values[index];
      }
      return null;
   }

   public String getLine1Number() {
      return getWithIndex(lineNumbers, 0);
   }

   public String getDeviceId() {
      return getWithIndex(deviceIds, 0);
   }

   public String getDeviceId(int index) {
      return getWithIndex(deviceIds, index);
   }

   public String getSimSerialNumber() {
      return getWithIndex(simSerialNumbers, 0);
   }

   public String getSimSerialNumber(int index) {
      return getWithIndex(simSerialNumbers, index);
   }
}
