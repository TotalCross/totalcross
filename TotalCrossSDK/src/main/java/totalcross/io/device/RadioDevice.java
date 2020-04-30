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

package totalcross.io.device;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

/**
 * Provides access to the device's radios and information about their status.
 * 
 * Refer to each method documentation for detailed information about the supported usage.
 * 
 * @since TotalCross 1.15
 */
final public class RadioDevice {
  // types
  public static final int WIFI = 0;
  public static final int PHONE = 1;
  public static final int BLUETOOTH = 2;

  // generic states
  public static final int RADIO_STATE_DISABLED = 0;
  public static final int RADIO_STATE_ENABLED = 1;

  // bluetooth states
  public static final int BLUETOOTH_STATE_DISCOVERABLE = 2;

  /** Cannot be instantiated */
  private RadioDevice() {
  }

  /**
   * Determines if the provided radio type is supported by the device.<br>
   * <br>
   * Fully functional on Android devices.<br>
   * Always return false on other platforms.
   * 
   * @param type
   *           The radio type to check for support.
   * @return True if the provided radio type is supported; otherwise false.
   * @throws IllegalArgumentException
   * @since TotalCross 1.15
   */
  @ReplacedByNativeOnDeploy
  public static boolean isSupported(int type) throws IllegalArgumentException {
    switch (type) {
    case WIFI:
      return false;
    case PHONE:
      return false;
    case BLUETOOTH:
      return false;
    default:
      throw new IllegalArgumentException();
    }
  }

  /**
   * Retrieves the current state of the provided radio.<br>
   * <br>
   * Currently works only on Android.<br>
   * Always return RADIO_STATE_DISABLED on other platforms.
   * 
   * @param type
   *           The radio type to have its state returned.
   * @return One of the RadioDevice representing the radio state.
   * @throws IllegalArgumentException
   * @since TotalCross 1.15
   */
  @ReplacedByNativeOnDeploy
  public static int getState(int type) throws IllegalArgumentException {
    switch (type) {
    case WIFI:
      return RADIO_STATE_DISABLED;
    case PHONE:
      return RADIO_STATE_DISABLED;
    case BLUETOOTH:
      return RADIO_STATE_DISABLED;
    default:
      throw new IllegalArgumentException();
    }
  }

  /**
   * Change the state of the provided radio.<br>
   * <br>
   * Fully functional only on Android devices.<br>
   * Does nothing on other platforms.
   * 
   * @param type
   *           The radio type to have its state changed.
   * @param state
   *           The new state for the given radio type.
   * @throws IllegalArgumentException
   * @since TotalCross 1.15
   */
  @ReplacedByNativeOnDeploy
  public static void setState(int type, int state) throws IllegalArgumentException {
    if (state < RADIO_STATE_DISABLED || state > BLUETOOTH_STATE_DISCOVERABLE
        || (type != BLUETOOTH && state > RADIO_STATE_ENABLED)) {
      throw new IllegalArgumentException();
    }
    if (type < WIFI || type > BLUETOOTH) {
      throw new IllegalArgumentException();
    }
  }
}
