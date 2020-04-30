// Copyright (C) 2000-2012 SuperWaba Ltda.
// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.device.bluetooth;

import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.util.IntVector;

public class DiscoveryAgent4D {
  DiscoveryListener deviceInquiryListener;
  Object inquiryNativeFields;

  public static final int CACHED = 0x00;
  public static final int GIAC = 0x9E8B33;
  public static final int LIAC = 0x9E8B00;
  public static final int NOT_DISCOVERABLE = 0x00;
  public static final int PREKNOWN = 0x01;

  DiscoveryAgent4D() {
    nativeDiscoveryAgent();
  }

  native private void nativeDiscoveryAgent();

  native public boolean cancelInquiry(DiscoveryListener listener);

  native public boolean cancelServiceSearch(int transID);

  native public RemoteDevice[] retrieveDevices(int option);

  private static int maxAttrValue = (2 << 16) - 1;

  public int searchServices(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev, DiscoveryListener discListener)
      throws IOException {
    if (uuidSet == null || btDev == null || discListener == null) {
      throw new NullPointerException();
    }

    // arrays cannot be empty
    int attrSetLen = attrSet == null ? 0 : attrSet.length;
    int uuidSetLen = uuidSet.length;
    if ((attrSet != null && attrSetLen == 0) || uuidSetLen == 0) {
      throw new IllegalArgumentException();
    }

    if (attrSet == null) {
      attrSet = new int[] { 0, 1, 2, 3, 4 };
    } else {
      IntVector attrSetVector = new IntVector(attrSet);
      attrSetVector.qsort();

      if (attrSetVector.items[0] < 0 || attrSetVector.items[0] > maxAttrValue) {
        throw new IllegalArgumentException("attrSet values must be in the range [0 - (2^16 - 1)]");
      }
      for (int i = attrSetLen - 1; i > 0; i--) {
        // attrSet cannot have duplicated values
        if (attrSetVector.items[i] == attrSetVector.items[i - 1]) {
          throw new IllegalArgumentException("Duplicated value in attrSet");
        }
        // values must be in range
        if (attrSetVector.items[i] < 0 || attrSetVector.items[i] > maxAttrValue) {
          throw new IllegalArgumentException("attrSet values must be in the range [0 - (2^16 - 1)]");
        }
      }

      // not pretty, but this way we avoid some extra method calls and a short loop.
      if (attrSetVector.items[0] != 0) {
        attrSetVector.addElement(0);
      }
      if (attrSetVector.indexOf(1, 0) == -1) {
        attrSetVector.addElement(1);
      }
      if (attrSetVector.indexOf(2, 0) == -1) {
        attrSetVector.addElement(2);
      }
      if (attrSetVector.indexOf(3, 0) == -1) {
        attrSetVector.addElement(3);
      }
      if (attrSetVector.indexOf(4, 0) == -1) {
        attrSetVector.addElement(4);
      }

      attrSetVector.qsort();
      attrSet = attrSetVector.toIntArray(); // final array, after adding the default attributes and sorting.
    }
    final int[] attrSet2 = attrSet;

    // uuidSet cannot have duplicated values
    final UUID[] uuidSet2 = new UUID[uuidSetLen];
    Vm.arrayCopy(uuidSet, 0, uuidSet2, 0, uuidSetLen);
    Convert.qsort(uuidSet2, 0, uuidSetLen - 1, Convert.SORT_OBJECT, true); // we don't have to check uuidSet for null values, qsort already does that;
    for (int i = uuidSetLen - 1; i > 0; i--) {
      if (uuidSet2[i].equals(uuidSet2[i - 1])) {
        throw new IllegalArgumentException();
      }
    }

    final RemoteDevice btDev2 = btDev;
    final DiscoveryListener discListener2 = discListener;
    Runnable searchThread = new Runnable() {
      @Override
      public void run() {
        try {
          nativeSearchServices(attrSet2, uuidSet2, btDev2, discListener2);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };
    Thread t = new Thread(searchThread);
    t.start();
    return 1;
  }

  native public int nativeSearchServices(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev,
      DiscoveryListener discListener) throws IOException;

  native public String selectService(UUID uuid, int security, boolean master) throws IOException;

  native public boolean startInquiry(int accessCode, DiscoveryListener listener) throws IOException;
}
