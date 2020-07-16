// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.device.bluetooth;

import totalcross.sys.Convert;

/**
 * The <code>UUID</code> class defines universally unique identifiers. These 128-bit unsigned integers are guaranteed to
 * be unique across all time and space. Accordingly, an instance of this class is immutable.
 * 
 * The Bluetooth specification provides an algorithm describing how a 16-bit or 32-bit UUID could be promoted to a
 * 128-bit UUID. Accordingly, this class provides an interface that assists applications in creating 16-bit, 32-bit, and
 * 128-bit long UUIDs. The methods supported by this class allow equality testing of two UUID objects.
 * 
 * </p>
 * <p>
 * 
 * The Bluetooth Assigned Numbers document (<a href="http://www.bluetooth.org/assigned-numbers/sdp.htm">
 * http://www.bluetooth.org/assigned-numbers/sdp.htm</a>) defines a large number of UUIDs for protocols and service
 * classes. The table below provides a short list of the most common UUIDs defined in the Bluetooth Assigned Numbers
 * document.
 * <table>
 * <tbody>
 * <tr>
 * <th>Name</th>
 * <th>Value</th>
 * <th>Size</th>
 * </tr>
 * <tr>
 * <td>Base UUID Value (Used in promoting 16-bit and 32-bit UUIDs to 128-bit UUIDs)</td>
 * <td>0x0000000000001000800000805F9B34FB</td>
 * 
 * <td>128-bit</td>
 * </tr>
 * <tr>
 * <td>SDP</td>
 * <td>0x0001</td>
 * <td>16-bit</td>
 * </tr>
 * <tr>
 * <td>RFCOMM</td>
 * <td>0x0003</td>
 * <td>16-bit</td>
 * </tr>
 * <tr>
 * <td>OBEX</td>
 * <td>0x0008</td>
 * <td>16-bit</td>
 * </tr>
 * 
 * <tr>
 * <td>HTTP</td>
 * <td>0x000C</td>
 * <td>16-bit</td>
 * </tr>
 * <tr>
 * <td>L2CAP</td>
 * <td>0x0100</td>
 * <td>16-bit</td>
 * </tr>
 * <tr>
 * <td>BNEP</td>
 * <td>0x000F</td>
 * <td>16-bit</td>
 * </tr>
 * <tr>
 * <td>Serial Port</td>
 * <td>0x1101</td>
 * <td>16-bit</td>
 * </tr>
 * 
 * <tr>
 * <td>ServiceDiscoveryServerServiceClassID</td>
 * <td>0x1000</td>
 * <td>16-bit</td>
 * </tr>
 * <tr>
 * <td>BrowseGroupDescriptorServiceClassID</td>
 * <td>0x1001</td>
 * <td>16-bit</td>
 * </tr>
 * <tr>
 * <td>PublicBrowseGroup</td>
 * <td>0x1002</td>
 * <td>16-bit</td>
 * </tr>
 * 
 * <tr>
 * <td>OBEX Object Push Profile</td>
 * <td>0x1105</td>
 * <td>16-bit</td>
 * </tr>
 * <tr>
 * <td>OBEX File Transfer Profile</td>
 * <td>0x1106</td>
 * <td>16-bit</td>
 * </tr>
 * <tr>
 * <td>Personal Area Networking User</td>
 * <td>0x1115</td>
 * <td>16-bit</td>
 * </tr>
 * 
 * <tr>
 * <td>Network Access Point</td>
 * <td>0x1116</td>
 * <td>16-bit</td>
 * </tr>
 * <tr>
 * <td>Group Network</td>
 * <td>0x1117</td>
 * <td>16-bit</td> </tbody>
 * </table>
 * 
 * @since TotalCross 1.2
 */
public class UUID {
  private byte[] uuidValue;

  static final String SHORT_UUID_BASE = "00001000800000805F9B34FB";

  /**
   * Creates a <code>UUID</code> object from <code>long</code> value <code>uuidValue</code>. A UUID is defined as an
   * unsigned integer whose value can range from [0 to 2<sup>128</sup>-1]. However, this constructor allows only those
   * values that are in the range of [0 to 2<sup>32</sup> -1]. Negative values and values in the range of
   * [2<sup>32</sup>, 2<sup>63</sup> -1] are not allowed and will cause an <code>IllegalArgumentException</code> to be
   * thrown.
   * 
   * @param uuidValue
   *           the 16-bit or 32-bit value of the UUID
   * @throws IllegalArgumentException
   *            if <code>uuidValue</code> is not in the range [0, 2<sup>32</sup> -1]
   * @since TotalCross 1.2
   */
  public UUID(long uuidValue) {
    this(Convert.toString(uuidValue, 16), true);
    if (uuidValue < 0 || uuidValue > 0xffffffffl) {
      throw new IllegalArgumentException("uuidValue is not in the range [0, 2^32 -1]");
    }
  }

  /**
   * Creates a <code>UUID</code> object from the string provided. The characters in the string must be from the
   * hexadecimal set [0-9, a-f, A-F]. It is important to note that the prefix "0x" generally used for hex
   * representation of numbers is not allowed. If the string does not have characters from the hexadecimal set, an
   * exception will be thrown. The string length has to be positive and less than or equal to 32. A string length that
   * exceeds 32 is illegal and will cause an exception. Finally, a <code>null</code> input is also considered illegal
   * and causes an exception.
   * <p>
   * If <code>shortUUID</code> is <code>true</code>, <code>uuidValue</code>
   * 
   * represents a 16-bit or 32-bit UUID. If <code>uuidValue</code> is in the range 0x0000 to 0xFFFF then this
   * constructor will create a 16-bit UUID. If <code>uuidValue</code> is in the range 0x000010000 to 0xFFFFFFFF, then
   * this constructor will create a 32-bit UUID. Therefore, <code>uuidValue</code> may only be 8 characters long.
   * </p>
   * <p>
   * On the other hand, if <code>shortUUID</code> is <code>false</code>, then <code>uuidValue</code> represents a
   * 128-bit UUID. Therefore, <code>uuidValue</code> may only be 32 character long.
   * </p>
   * 
   * @param uuidValue
   *           the string representation of a 16-bit, 32-bit or 128-bit UUID
   * @param shortUUID
   *           indicates the size of the UUID to be constructed; <code>true</code> is used to indicate short UUIDs,
   *           i.e. either 16-bit or 32-bit; <code>false</code> indicates an 128-bit UUID
   * @throws IllegalArgumentException
   *            if <code>uuidValue</code> has characters that are not defined in the hexadecimal set [0-9, a-f, A-F];
   *            if <code>uuidValue</code> length is zero; if <code>shortUUID</code> is <code>true</code> and
   *            <code>uuidValue</code>'s length is greater than 8; if <code>shortUUID</code> is <code>false</code> and
   *            <code>uuidValue</code>'s length is greater than 32
   * @throws NullPointerException
   *            if <code>uuidValue</code> is <code>null</code>
   * @since TotalCross 1.2
   */
  public UUID(String uuidValue, boolean shortUUID) {
    if (uuidValue == null) {
      throw new NullPointerException("uuidValue is null");
    }

    int length = uuidValue.length();
    if (length < 1 || length > 32 || (shortUUID && length > 8)) {
      throw new IllegalArgumentException();
    }

    StringBuffer sb = new StringBuffer("00000000000000000000000000000000");
    sb.setLength((shortUUID ? 8 : 32) - length); //flsobral@tc122_56: fixed length of UUID created from a short value
    sb.append(uuidValue);
    if (shortUUID) {
      sb.append(SHORT_UUID_BASE);
    }

    this.uuidValue = Convert.hexStringToBytes(sb.toString(), false);
  }

  /**
   * Determines if two <code>UUID</code>s are equal. They are equal if their 128 bit values are the same. This method
   * will return <code>false</code> if <code>value</code> is <code>null</code> or is not a <code>UUID</code> object.
   * 
   * @param value
   *           the object to compare to
   * @return <code>true</code> if the 128 bit values of the two objects are equal, otherwise <code>false</code>
   * @since TotalCross 1.2
   */
  @Override
  public boolean equals(Object value) {
    if (value == null || !(value instanceof UUID)) {
      return false;
    }

    for (int i = 0; i < 16; i++) {
      if (uuidValue[i] != ((UUID) value).uuidValue[i]) {
        return false;
      }
    }

    return true;
  }

  /**
   * Computes the hash code for this object. This method retains the same semantic contract as defined in the class
   * <code>java.lang.Object</code> while overriding the implementation.
   * 
   * @return the hash code for this object
   * @since TotalCross 1.2
   */
  @Override
  public int hashCode() {
    return uuidValue[12] << 24 & 0xff000000 | uuidValue[13] << 16 & 0x00ff0000 | uuidValue[14] << 8 & 0x0000ff00
        | uuidValue[15] & 0x000000ff;
  }

  /**
   * Returns the string representation of the 128-bit UUID object. The string being returned represents a UUID that
   * contains characters from the hexadecimal set, [0-9, A-F]. It does not include the prefix "0x" that is generally
   * used for hex representation of numbers. The return value will never be <code>null</code>.
   * 
   * @return the string representation of the UUID
   * @since TotalCross 1.2
   */
  @Override
  public String toString() {
    return Convert.bytesToHexString(uuidValue, 0, uuidValue.length);
  }
}
