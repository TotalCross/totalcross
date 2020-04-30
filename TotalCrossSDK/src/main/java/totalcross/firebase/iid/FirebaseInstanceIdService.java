// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.firebase.iid;

/**
 * Use it like https://firebase.google.com/docs/reference/android/com/google/firebase/iid/FirebaseInstanceIdService
 * 
 * Instead of declaring it in the manifest, you should override MainWindow.initFirebaseInstanceIdService() with
 * your implementation.
 * 
 * If you are going to deliver a unicast push message, you must retrieve the must recent token and use it.
 * 
 * Currently not bound with native Firebase
 */
public class FirebaseInstanceIdService {

  public void onTokenRefresh() {
  }
}
