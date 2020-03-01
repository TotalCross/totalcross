/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.              *
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

package totalcross.firebase.iid;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

/**
 * Use it like https://firebase.google.com/docs/reference/android/com/google/firebase/iid/FirebaseInstanceId
 * 
 * In the enabled device, it will search for the default FirebaseApp name that TotalCross uses and return
 * it's FirebaseInstanceId instance token. If in a device that is not enabled Firebase (be it lack of
 * configuration file or not yet binded with TotalCross), it will return null.
 * 
 * If you are going to deliver a unicast push message, you must retrieve the must recent token and use it.
 * 
 * Currently suported platforms:
 * <ul>
 * 		<li>Android</li>
 * </ul>
 */
public class FirebaseInstanceId {
  private static FirebaseInstanceId instance = new FirebaseInstanceId();

  private FirebaseInstanceId() {

  }

  public static FirebaseInstanceId getInstance() {
    return instance;
  }

  @ReplacedByNativeOnDeploy
  public String getToken() {
    return null;
  }
}
