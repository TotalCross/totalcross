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

/**
 * The <code>DeviceClass</code> class represents the class of device (CoD) record as defined by the Bluetooth
 * specification. This record is defined in the Bluetooth Assigned Numbers document and contains information on the type
 * of the device and the type of services available on the device.
 * <P>
 * 
 * The Bluetooth Assigned Numbers document (<a href="http://www.bluetooth.org/assigned-numbers/sdp.htm">
 * http://www.bluetooth.org/assigned-numbers/sdp.htm</a>) defines the service class, major device class, and minor
 * device class. The table below provides some examples of possible return values and their meaning:
 * <TABLE>
 * <TR>
 * <TH>Method</TH>
 * <TH>Return Value</TH>
 * <TH>Class of Device</TH>
 * </TR>
 * <TR>
 * <TD><code>getServiceClasses()</code></TD>
 * <TD>0x22000</TD>
 * 
 * <TD>Networking and Limited Discoverable Major Service Classes</TD>
 * </TR>
 * <TR>
 * <TD><code>getServiceClasses()</code></TD>
 * <TD>0x100000</TD>
 * <TD>Object Transfer Major Service Class</TD>
 * </TR>
 * <TR>
 * <TD><code>getMajorDeviceClass()</code></TD>
 * <TD>0x00</TD>
 * 
 * <TD>Miscellaneous Major Device Class</TD>
 * </TR>
 * <TR>
 * <TD><code>getMajorDeviceClass()</code></TD>
 * <TD>0x200</TD>
 * <TD>Phone Major Device Class</TD>
 * </TR>
 * <TR>
 * <TD><code>getMinorDeviceClass()</code></TD>
 * <TD>0x0C</TD>
 * <TD>With a Computer Major Device Class, Laptop Minor Device Class</TD>
 * </TR>
 * 
 * <TR>
 * <TD><code>getMinorDeviceClass()</code></TD>
 * <TD>0x04</TD>
 * <TD>With a Phone Major Device Class, Cellular Minor Device Class</TD>
 * </TR>
 * </TABLE>
 * 
 * @since TotalCross 1.2
 */
public class DeviceClass {
  /** record of this class of device. */
  private int record;

  /** record masks */
  private static final int SERVICE_CLASS_MASK = 0xFFE000;
  private static final int MAJOR_DEVICE_CLASS_MASK = 0x001F00;
  private static final int MINOR_DEVICE_CLASS_MASK = 0x0000FC;

  /**
   * Creates a <code>DeviceClass</code> from the class of device record provided. <code>record</code> must follow the
   * format of the class of device record in the Bluetooth specification.
   * 
   * @param record
   *           describes the classes of a device
   * @throws IllegalArgumentException
   *            if record has any bits between 24 and 31 set
   * @since TotalCross 1.2
   */
  public DeviceClass(int record) throws IllegalArgumentException {
    if ((record & 0xFF000000) != 0) {
      throw new IllegalArgumentException();
    }
    this.record = record;
  }

  /**
   * Retrieves the major device class. A device may have only a single major device class.
   * 
   * @return the major device class
   * @since TotalCross 1.2
   */
  public int getMajorDeviceClass() {
    return record & MAJOR_DEVICE_CLASS_MASK;
  }

  /**
   * Retrieves the minor device class.
   * 
   * @return the minor device class
   * @since TotalCross 1.2
   */
  public int getMinorDeviceClass() {
    return record & MINOR_DEVICE_CLASS_MASK;
  }

  /**
   * Retrieves the major service classes. A device may have multiple major service classes. When this occurs, the major
   * service classes are bitwise OR'ed together.
   * 
   * @return the major service classes
   * @since TotalCross 1.2
   */
  public int getServiceClasses() {
    return record & SERVICE_CLASS_MASK;
  }
}
