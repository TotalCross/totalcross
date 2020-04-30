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
import totalcross.io.StreamConnectionNotifier;
import totalcross.io.device.RadioDevice;
import totalcross.sys.Settings;

/**
 * The <code>LocalDevice</code> class defines the basic functions of the Bluetooth manager. The Bluetooth manager
 * provides the lowest level of interface possible into the Bluetooth stack. It provides access to and control of the
 * local Bluetooth device. </p>
 * <p>
 * This class produces a singleton object.
 * </p>
 * 
 * @since TotalCross 1.15
 */
public class LocalDevice {
  /** Singleton object */
  private static LocalDevice instance;

  static DiscoveryAgent discoveryAgent;
  static DeviceClass deviceClass;

  /** This class produces a singleton object. */
  private LocalDevice() throws IOException {
    deviceClass = new DeviceClass(5898500); // major = computer, minor = desktop, services = Networking & Capturing & Object Transfer.
    discoveryAgent = new DiscoveryAgent();
  }

  /**
   * Retrieves the Bluetooth address of the local device. The Bluetooth address will never be <code>null</code>. The
   * Bluetooth address will be 12 characters long. Valid characters are 0-9 and A-F.
   * 
   * @return the Bluetooth address of the local device
   * @since TotalCross 1.2
   */
  public String getBluetoothAddress() {
    return "111111111111";
  }

  /**
   * Retrieves the <code>DeviceClass</code> object that represents the service classes, major device class, and minor
   * device class of the local device. This method will return <code>null</code> if the service classes, major device
   * class, or minor device class could not be determined.
   * 
   * @return the service classes, major device class, and minor device class of the local device, or <code>null</code>
   *         if the service classes, major device class or minor device class could not be determined
   * @since TotalCross 1.2
   */
  public DeviceClass getDeviceClass() {
    return deviceClass;
  }

  /**
   * Retrieves the local device's discoverable mode. The return value will be <code>DiscoveryAgent.GIAC</code>,
   * <code>DiscoveryAgent.LIAC</code>, <code>DiscoveryAgent.NOT_DISCOVERABLE</code>, or a value in the range 0x9E8B00
   * to 0x9E8B3F.
   * 
   * @return the discoverable mode the device is presently in
   * @since TotalCross 1.2
   */
  public int getDiscoverable() {
    return DiscoveryAgent.NOT_DISCOVERABLE; // not supported on JDK, always returns not discoverable.
  }

  /**
   * Returns the discovery agent for this device. Multiple calls to this method will return the same object. This
   * method will never return <code>null</code>.
   * 
   * @return the discovery agent for the local device
   * @since TotalCross 1.15
   */
  public DiscoveryAgent getDiscoveryAgent() {
    return discoveryAgent;
  }

  /**
   * Retrieves the name of the local device. The Bluetooth specification calls this name the "Bluetooth device name" or
   * the "user-friendly name".
   * 
   * @return the name of the local device; <code>null</code> if the name could not be retrieved
   * @since TotalCross 1.2
   */
  public String getFriendlyName() {
    return Settings.deviceId; // not supported on JDK, always returns the device name.
  }

  /**
   * Retrieves the <code>LocalDevice</code> object for the local Bluetooth device. Multiple calls to this method will
   * return the same object. This method will never return <code>null</code>.
   * 
   * @return an object that represents the local Bluetooth device
   * @throws IOException
   *            if the Bluetooth system could not be initialized
   * @since TotalCross 1.15
   */
  public static LocalDevice getLocalDevice() throws IOException {
    if (instance == null) {
      instance = new LocalDevice();
    }
    return instance;
  }

  public static String getProperty(String property) {
    return null;
  }

  /**
   * Retrieves the power state of the local Bluetooth device.
   * 
   * @return <code>true</code> if the local Bluetooth device is powered on, <code>false</code> if the local Bluetooth
   *         device is off.
   * @since TotalCross 1.2
   */
  public static boolean isPowerOn() {
    return RadioDevice.getState(RadioDevice.BLUETOOTH) != RadioDevice.RADIO_STATE_DISABLED;
  }

  /**
   * Sets the discoverable mode of the device. The <code>mode</code> may be any number in the range 0x9E8B00 to
   * 0x9E8B3F as defined by the Bluetooth Assigned Numbers Document. When this specification was defined, only GIAC (
   * <code>DiscoveryAgent.GIAC</code>) and LIAC ( <code>DiscoveryAgent.LIAC</code>) were defined, but Bluetooth
   * profiles may add additional access codes in the future. To determine what values may be used, check the Bluetooth
   * Assigned Numbers document at <A HREF="http://www.bluetooth.org/assigned-numbers/baseband.htm">
   * http://www.bluetooth.org/assigned-numbers/baseband.htm</A>. If <code>DiscoveryAgent.GIAC</code> or
   * <code>DiscoveryAgent.LIAC</code> are provided, then this method will attempt to put the device into general or
   * limited discoverable mode, respectively. To take a device out of discoverable mode, provide the
   * <code>DiscoveryAgent.NOT_DISCOVERABLE</code> flag. The BCC decides if the request will be granted. In addition to
   * the BCC, the Bluetooth system could effect the discoverability of a device.
   * <P>
   * According to the Bluetooth Specification, a device should only be limited discoverable (
   * <code>DiscoveryAgent.LIAC</code>) for 1 minute. This is handled by the implementation of the API. After the minute
   * is up, the device will revert back to the previous discoverable mode.
   * 
   * @see DiscoveryAgent#GIAC
   * @see DiscoveryAgent#LIAC
   * @see DiscoveryAgent#NOT_DISCOVERABLE
   * 
   * @param mode
   *           the mode the device should be in; valid modes are <code>DiscoveryAgent.GIAC</code>,
   *           <code>DiscoveryAgent.LIAC</code>, <code>DiscoveryAgent.NOT_DISCOVERABLE</code> and any value in the
   *           range 0x9E8B00 to 0x9E8B3F
   * @return <code>true</code> if the request succeeded, otherwise <code>false</code> if the request failed because the
   *         BCC denied the request; <code>false</code> if the Bluetooth system does not support the access mode
   *         specified in <code>mode</code>
   * @exception IllegalArgumentException
   *               if the <code>mode</code> is not <code>DiscoveryAgent.GIAC</code>, <code>DiscoveryAgent.LIAC</code> ,
   *               <code>DiscoveryAgent.NOT_DISCOVERABLE</code>, or in the range 0x9E8B00 to 0x9E8B3F
   * @exception IOException
   *               if the Bluetooth system is in a state that does not allow the discoverable mode to be changed
   */
  public boolean setDiscoverable(int mode) throws IOException {
    if ((mode != DiscoveryAgent.GIAC) && (mode != DiscoveryAgent.LIAC) && (mode != DiscoveryAgent.NOT_DISCOVERABLE)
        && (mode < 0x9E8B00 || mode > 0x9E8B3F)) {
      throw new IllegalArgumentException("Invalid discoverable mode");
    }
    return false; // not supported on JDK, always returns false.
  }

  public void updateRecord(ServiceRecord srvRecord) {
  }

  /**
   * Gets the service record corresponding to a <code>btspp</code>, <code>btl2cap</code>, or <code>btgoep</code>
   * notifier. In the case of a run-before-connect service, the service record returned by <code>getRecord()</code> was
   * created by the same call to <code>Connector.open()</code> that created the <code>notifier</code>.
   * 
   * <p>
   * If a connect-anytime server application does not already have a service record in the SDDB, either because a
   * service record for this service was never added to the SDDB or because the service record was added and then
   * removed, then the <code>ServiceRecord</code> returned by <code>getRecord()</code> was created by the same call to
   * <code>Connector.open()</code> that created the notifier.
   * <p>
   * In the case of a connect-anytime service, there may be a service record in the SDDB corresponding to this service
   * prior to application startup. In this case, the <code>getRecord()</code> method must return a
   * <code>ServiceRecord</code> whose contents match those of the corresponding service record in the SDDB. If a
   * connect-anytime server application made changes previously to its service record in the SDDB (for example, during
   * a previous execution of the server), and that service record is still in the SDDB, then those changes must be
   * reflected in the <code>ServiceRecord</code> returned by <code>getRecord()</code>.
   * 
   * <p>
   * Two invocations of this method with the same <code>notifier</code> argument return objects that describe the same
   * service attributes, but the return values may be different object references.
   * 
   * @param notifier
   *           a connection that waits for clients to connect to a Bluetooth service
   * @return the <code>ServiceRecord</code> associated with <code>notifier</code>
   * @exception IllegalArgumentException
   *               if <code>notifier</code> is closed, or if <code>notifier</code> is not a Bluetooth notifier, e.g., a
   *               <code>StreamConnectionNotifier</code> created with a scheme other than <code>btspp</code>.
   * @exception NullPointerException
   *               if <code>notifier</code> is <code>null</code>
   */
  public ServiceRecord getRecord(StreamConnectionNotifier notifier) {
    if (notifier == null) {
      throw new NullPointerException();
    }
    if (!(notifier instanceof totalcross.io.device.bluetooth.SerialPortServer)) {
      throw new IllegalArgumentException();
    }
    return null;
  }
}
