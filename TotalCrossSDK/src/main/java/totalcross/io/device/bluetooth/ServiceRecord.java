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
 * The {@code ServiceRecord} class describes characteristics of a Bluetooth 
 * service. A {@code ServiceRecord} contains a set of service attributes, where 
 * each service attribute is an (ID, value) pair. A Bluetooth attribute 
 * ID is a 16-bit unsigned integer, and an attribute value is a 
 * {@link totalcross.io.device.bluetooth.DataElement}.
 * <p>
 * The structure and use of service records is specified by the Bluetooth 
 * specification in the Service Discovery Protocol (SDP) document. Most of 
 * the Bluetooth Profile specifications also describe the structure of the
 * service records used by the Bluetooth services that conform to the profile.
 * <p>
 * An SDP Server maintains a Service Discovery Database (SDDB) of service 
 * records that describe the services on the local device. Remote SDP 
 * clients can use the SDP to query an SDP server for any service records 
 * of interest. A service record provides sufficient information to allow 
 * an SDP client to connect to the Bluetooth service on the SDP server's device.
 * <p>
 * {@code ServiceRecords} are made available to a client application via an argument 
 * of the {@link totalcross.io.device.bluetooth.DiscoveryListener#servicesDiscovered} method
 * of the {@link totalcross.io.device.bluetooth.DiscoveryListener} interface. 
 * {@code ServiceRecords} are available to server applications via the method 
 * {@link totalcross.io.device.bluetooth.LocalDevice#getRecord(totalcross.io.Connection)}.
 * <p>
 * There might be many service attributes in a service record, and the SDP 
 * protocol makes it possible to specify the subset of the service 
 * attributes that an SDP client wants to retrieve from a remote service 
 * record. The {@code ServiceRecord} interface treats certain service attribute 
 * IDs as default IDs, and, if present, these service attributes are 
 * automatically retrieved during service searches.
 * <p>
 * The Bluetooth Assigned Numbers document 
 * (<a href="http://www.bluetooth.org/assigned-numbers/sdp.htm">
 * http://www.bluetooth.org/assigned-numbers/sdp.htm</a>) defines a large 
 * number of service attribute IDs. Here is a subset of the most common 
 * service attribute IDs and their types.
 *
 * <TABLE BORDER=1>
 * <TR><TH>Attribute Name</TH><TH>Attribute ID</TH><TH>Attribute Value Type</TH></TR>
 * <TR><TD>ServiceRecordHandle</TD><TD>0x0000</TD><TD>32-bit unsigned integer</TD></TR>
 * <TR><TD>ServiceClassIDList</TD><TD>0x0001</TD><TD>DATSEQ of UUIDs</TD></TR>
 * <TR><TD>ServiceRecordState</TD><TD>0x0002</TD><TD>32-bit unsigned integer</TD></TR>
 * <TR><TD>ServiceID</TD><TD>0x0003</TD><TD>UUID</TD></TR>
 * <TR><TD>ProtocolDescriptorList</TD><TD>0x0004</TD><TD>DATSEQ of DATSEQ of UUID and optional parameters</TD></TR>
 * <TR><TD>BrowseGroupList</TD><TD>0x0005</TD><TD>DATSEQ of UUIDs</TD></TR>
 * <TR><TD>LanguageBasedAttributeIDList</TD><TD>0x0006</TD><TD>DATSEQ of DATSEQ triples</TD></TR>
 * <TR><TD>ServiceInfoTimeToLive</TD><TD>0x0007</TD><TD>32-bit unsigned integer</TD></TR>
 * <TR><TD>ServiceAvailability</TD><TD>0x0008</TD><TD>8-bit unsigned integer</TD></TR>
 * <TR><TD>BluetoothProfileDescriptorList</TD><TD>0x0009</TD><TD>DATSEQ of DATSEQ pairs</TD></TR>
 * <TR><TD>DocumentationURL</TD><TD>0x000A</TD><TD>URL</TD></TR>
 * <TR><TD>ClientExecutableURL</TD><TD>0x000B</TD><TD>URL</TD></TR>
 * <TR><TD>IconURL</TD><TD>0x000C</TD><TD>URL</TD></TR>
 * <TR><TD>VersionNumberList</TD><TD>0x0200</TD><TD><code>DATSEQ</code> of 16-bit unsigned integers</TD></TR>
 * <TR><TD>ServiceDatabaseState</TD><TD>0x0201</TD><TD>32-bit unsigned integer</TD></TR>
 * </TABLE>
 * <p>
 * The following table lists the common string-valued attribute ID offsets used in a 
 * {@code ServiceRecord}. These offsets must be added to a base value to obtain the actual 
 * service ID. (For more information, see the Service Discovery Protocol Specification 
 * located in the Bluetooth Core Specification.
 * <p>
 * <TABLE BORDER=1> 
 * <TR><TH>Attribute Name</TH><TH>Attribute ID Offset</TH><TH>Attribute Value Type</TH></TR>
 * <TR><TD>ServiceName</TD><TD>0x0000</TD><TD>String</TD></TR>
 * <TR><TD>ServiceDescription</TD><TD>0x0001</TD><TD>String</TD></TR>
 * <TR><TD>ProviderName</TD><TD>0x0002</TD><TD>String</TD></TR>
 * </TABLE>
 * @since TotalCross 1.15
 */
public class ServiceRecord {
  Object nativeInstance;

  /**
   * Authentication and encryption are not needed on a connection to this service. Used with
   * <code>getConnectionURL()</code> method.
   * <p>
   * <code>NOAUTHENTICATE_NOENCRYPT</code> is set to the constant value 0x00 (0).
   * </p>
   */
  public static final int NOAUTHENTICATE_NOENCRYPT = 0x00;

  /**
   * Authentication is required for connections to this service, but not encryption. It is OK for encryption to be
   * either on or off for the connection. Used with <code>getConnectionURL()</code> method.
   * <p>
   * <code>AUTHENTICATE_NOENCRYPT</code> is set to the constant value 0x01 (1).
   * </p>
   */
  public static final int AUTHENTICATE_NOENCRYPT = 0x01;

  /**
   * Authentication and encryption are required for connections to this service. Used with
   * <code>getConnectionURL()</code> method.
   * <p>
   * <code>AUTHENTICATE_ENCRYPT</code> is set to the constant value 0x02 (2).
   * </p>
   */
  public static final int AUTHENTICATE_ENCRYPT = 0x02;

  ServiceRecord() {
  }

  ServiceRecord(Object nativeInstance) {
    this.nativeInstance = nativeInstance;
  }

  /**
   * Returns the remote Bluetooth device that populated the service record with attribute values. It is important to
   * note that the Bluetooth device that provided the value might not be reachable anymore, since it can move, turn
   * off, or change its security mode denying all further transactions.
   * 
   * @return the remote Bluetooth device that populated the service record, or null if the local device populated this
   *         ServiceRecord
   * @since TotalCross 1.27
   */
  public RemoteDevice getHostDevice() {
    return null;
  }

  /**
   * Returns the value of the service attribute ID provided it is present in the service record, otherwise this method
   * returns null.
   * 
   * @param attrID
   *           the attribute whose value is to be returned
   * @return the value of the attribute ID if present in the service record, otherwise null
   * @throws IllegalArgumentException
   *            if attrID is negative or greater than or equal to 2<sup>16</sup>
   * @since TotalCross 1.27
   */
  public DataElement getAttributeValue(int attrID) {
    if (attrID < 0x0000 || attrID > 0xffff) {
      throw new IllegalArgumentException();
    }
    return null;
  }

  /**
   * Returns a string including optional parameters that can be used by a client to connect to the service described by
   * this ServiceRecord. The return value can be used as the first argument to Connector.open(). In the case of a
   * Serial Port service record, this string might look like
   * "btspp://0050CD00321B:3;authenticate=true;encrypt=false;master=true", where "0050CD00321B" is the Bluetooth
   * address of the device that provided this ServiceRecord, "3" is the RFCOMM server channel mentioned in this
   * ServiceRecord, and there are three optional parameters related to security and master/slave roles.
   * 
   * @return a string that can be used to connect to the service or null if the ProtocolDescriptorList in this
   *         ServiceRecord is not formatted according to the Bluetooth specification
   * @since TotalCross 1.15
   */
  public String getConnectionURL() {
    return getConnectionURL(NOAUTHENTICATE_NOENCRYPT, false);
  }

  /**
   * Returns a String including optional parameters that can be used by a client to connect to the service described by
   * this ServiceRecord. The return value can be used as the first argument to Connector.open(). In the case of a
   * Serial Port service record, this string might look like
   * "btspp://0050CD00321B:3;authenticate=true;encrypt=false;master=true", where "0050CD00321B" is the Bluetooth
   * address of the device that provided this ServiceRecord, "3" is the RFCOMM server channel mentioned in this
   * ServiceRecord, and there are three optional parameters related to security and master/slave roles.
   * 
   * @param requiredSecurity
   *           determines whether authentication or encryption are required for a connection
   * @param mustBeMaster
   *           true indicates that this device must play the role of master in connections to this service; false
   *           indicates that the local device is willing to be either the master or the slave
   * @return a string that can be used to connect to the service or null if the ProtocolDescriptorList in this
   *         ServiceRecord is not formatted according to the Bluetooth specification
   * @throws IllegalArgumentException
   *            if requiredSecurity is not one of the constants NOAUTHENTICATE_NOENCRYPT, AUTHENTICATE_NOENCRYPT, or
   *            AUTHENTICATE_ENCRYPT
   * @since TotalCross 1.27
   * @see #NOAUTHENTICATE_NOENCRYPT
   */
  public String getConnectionURL(int requiredSecurity, boolean mustBeMaster) {
    // security
    switch (requiredSecurity) {
    case NOAUTHENTICATE_NOENCRYPT:
    case AUTHENTICATE_NOENCRYPT:
    case AUTHENTICATE_ENCRYPT:
      break;
    default:
      throw new IllegalArgumentException();
    }
    return null;
  }
}
