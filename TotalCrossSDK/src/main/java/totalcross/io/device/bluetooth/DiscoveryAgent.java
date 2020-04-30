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

/**
 * The <code>DiscoveryAgent</code> class provides methods to perform device and service discovery. A local device must
 * have only one <code>DiscoveryAgent</code> object. This object must be retrieved by a call to
 * <code>getDiscoveryAgent()</code> on the <code>LocalDevice</code> object.
 * 
 * @since TotalCross 1.15
 */
public class DiscoveryAgent {
  /**
   * Used with the <code>retrieveDevices()</code> method to return those devices that were found via a previous
   * inquiry. If no inquiries have been started, this will cause the method to return <code>null</code>.
   * <p>
   * The value of <code>CACHED</code> is 0x00 (0).
   * </p>
   */
  public static final int CACHED = 0x00;

  /**
   * The inquiry access code for General/Unlimited Inquiry Access Code (GIAC). This is used to specify the type of
   * inquiry to complete or respond to.
   * <p>
   * The value of <code>GIAC</code> is 0x9E8B33 (10390323). This value is defined in the Bluetooth Assigned Numbers
   * document.
   * </p>
   */
  public static final int GIAC = 0x9E8B33;

  /**
   * The inquiry access code for Limited Dedicated Inquiry Access Code (LIAC). This is used to specify the type of
   * inquiry to complete or respond to.
   * <p>
   * The value of <code>LIAC</code> is 0x9E8B00 (10390272). This value is defined in the Bluetooth Assigned Numbers
   * document.
   * </p>
   */
  public static final int LIAC = 0x9E8B00;

  /**
   * Takes the device out of discoverable mode.
   * <p>
   * The value of <code>NOT_DISCOVERABLE</code> is 0x00 (0).
   * </p>
   */
  public static final int NOT_DISCOVERABLE = 0x00;

  /**
   * Used with the <code>retrieveDevices()</code> method to return those devices that are defined to be pre-known
   * devices. Pre-known devices are specified in the BCC. These are devices that are specified by the user as devices
   * with which the local device will frequently communicate.
   * <p>
   * The value of <code>PREKNOWN</code> is 0x01 (1).
   * </p>
   */
  public static final int PREKNOWN = 0x01;

  /** Max attribute value */
  private static int maxAttrValue = (2 << 16) - 1;

  DiscoveryAgent() {
  }

  /**
   * Removes the device from inquiry mode.
   * <p>
   * An <code>inquiryCompleted()</code> event will occur with a type of <code>INQUIRY_TERMINATED</code> as a result of
   * calling this method. After receiving this event, no further <code>deviceDiscovered()</code> events will occur as a
   * result of this inquiry.
   * </p>
   * <p>
   * This method will only cancel the inquiry if the <code>listener</code> provided is the listener that started the
   * inquiry.
   * </p>
   * Not implemented on Android.
   * 
   * @param listener
   *           the listener that is receiving inquiry events
   * @return <code>true</code> if the inquiry was canceled; otherwise <code>false</code> if the inquiry was not
   *         canceled or if the inquiry was not started using <code>listener</code>
   * @throws NullPointerException
   *            if <code>listener</code> is <code>null</code>
   * @since TotalCross 1.2
   */
  public boolean cancelInquiry(DiscoveryListener listener) {
    if (listener == null) {
      throw new NullPointerException();
    }
    return false; // // not supported on JDK, always returns false.
  }

  /**
   * Cancels the service search transaction that has the specified transaction ID. The ID was assigned to the
   * transaction by the method <code>searchServices()</code>. A <code>serviceSearchCompleted()</code> event with a
   * discovery type of <code>SERVICE_SEARCH_TERMINATED</code> will occur when this method is called. After receiving
   * this event, no further <code>servicesDiscovered()</code> events will occur as a result of this search.
   * 
   * Not implemented on Android and Windows CE.
   * 
   * @param transID
   *           the ID of the service search transaction to cancel; returned by <code>searchServices()</code>
   * @return <code>true</code> if the service search transaction is terminated, else <code>false</code> if
   *         <code>transID</code> does not represent an active service search transaction
   * @since TotalCross 1.2
   */
  public boolean cancelServiceSearch(int transID) {
    return true; // not supported on JDK, always returns false.
  }

  /**
   * Returns an array of Bluetooth devices that have either been found by the local device during previous inquiry
   * requests or been specified as a pre-known device depending on the argument. The list of previously found devices
   * is maintained by the implementation of this API. (In other words, maintenance of the list of previously found
   * devices is an implementation detail.) A device can be set as a pre-known device in the Bluetooth Control Center.
   *
   * Works on Android. Pass CACHED to list the unpaired devices, and PREKNOWN to list the paired devices. Note that 
   * paired devices may not be at reach at the moment.
   * 
   * @param option
   *           <code>option</code> - <code>CACHED</code> if previously found devices should be returned;
   *           <code>PREKNOWN</code> if pre-known devices should be returned
   * @return an array containing the Bluetooth devices that were previously found if <code>option</code> is
   *         <code>CACHED</code>; an array of devices that are pre-known devices if <code>option</code> is
   *         <code>PREKNOWN</code>; <code>null</code> if no devices meet the criteria
   * @throws IllegalArgumentException
   *            if <code>option</code> is not <code>CACHED</code> or <code>PREKNOWN</code>
   */
  public RemoteDevice[] retrieveDevices(int option) {
    if (option != CACHED && option != PREKNOWN) {
      throw new IllegalArgumentException();
    }
    return null; // not supported on JDK, always returns null.
  }

  /**
   * Searches for services on a remote Bluetooth device that have all the UUIDs specified in <code>uuidSet</code>. Once
   * the service is found, the attributes specified in <code>attrSet</code> and the default attributes are retrieved.
   * The default attributes are ServiceRecordHandle (0x0000), ServiceClassIDList (0x0001), ServiceRecordState (0x0002),
   * ServiceID (0x0003), and ProtocolDescriptorList (0x0004). If <code>attrSet</code> is <code>null</code> then only the
   * default attributes will be retrieved. <code>attrSet</code> does not have to be sorted in increasing order, but
   * must only contain values in the range [0 - (2<sup>16</sup>-1)].
   * 
   * Not implemented on Android.
   * 
   * @param attrSet
   *           indicates the attributes whose values will be retrieved on services which have the UUIDs specified in
   *           <code>uuidSet</code>
   * @param uuidSet
   *           the set of UUIDs that are being searched for; all services returned will contain all the UUIDs specified
   *           here
   * @param btDev
   *           the remote Bluetooth device to search for services on
   * @param discListener
   *           the object that will receive events when services are discovered
   * @return the transaction ID of the service search, which is a positive number.
   * @throws NullPointerException
   *            if <code>uuidSet</code>, <code>btDev</code>, or <code>discListener</code> is <code>null</code>; if an
   *            element in <code>uuidSet</code> array is <code>null</code>
   * @throws IllegalArgumentException
   *            if <code>attrSet</code> has an illegal service attribute ID or exceeds the property
   *            <code>bluetooth.sd.attr.retrievable.max</code> defined in the class <code>LocalDevice</code>; if
   *            <code>attrSet</code> or <code>uuidSet</code> is of length 0; if <code>attrSet</code> or
   *            <code>uuidSet</code> contains duplicates
   * @throws IOException
   *            if the number of concurrent service search transactions exceeds the limit specified by the
   *            <code>bluetooth.sd.trans.max</code> property obtained from the class <code>LocalDevice</code> or the
   *            system is unable to start one due to current conditions
   */
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

    // uuidSet cannot have duplicated values
    UUID[] uuidSet2 = new UUID[uuidSetLen];
    Vm.arrayCopy(uuidSet, 0, uuidSet2, 0, uuidSetLen);
    Convert.qsort(uuidSet2, 0, uuidSetLen - 1, Convert.SORT_OBJECT, true); // we don't have to check uuidSet for null values, qsort already does that;
    for (int i = uuidSetLen - 1; i > 0; i--) {
      if (uuidSet2[i].equals(uuidSet2[i - 1])) {
        throw new IllegalArgumentException();
      }
    }

    throw new IOException("Bluetooth API is not supported on JDK"); // not supported on JDK, always throws IOException.
  }

  /**
   * Attempts to locate a service that contains <code>uuid</code> in the ServiceClassIDList of its service record. This
   * method will return a string that may be used in <code>Connector.open()</code> to establish a connection to the
   * service. How the service is selected if there are multiple services with <code>uuid</code> and which devices to
   * search is implementation dependent.
   *
   * Not implemented on Android.
   * 
   * @param uuid
   *           the UUID to search for in the ServiceClassIDList
   * @param security
   *           specifies the security requirements for a connection to this service; must be one of
   *           <code>ServiceRecord.NOAUTHENTICATE_NOENCRYPT</code>, <code>ServiceRecord.AUTHENTICATE_NOENCRYPT</code>,
   *           or <code>ServiceRecord.AUTHENTICATE_ENCRYPT</code>
   * @param master
   *           determines if this client must be the master of the connection; <code>true</code> if the client must be
   *           the master; <code>false</code> if the client can be the master or the slave
   * @return the connection string used to connect to the service with a UUID of <code>uuid</code>; or
   *         <code>null</code> if no service could be found with a UUID of <code>uuid</code> in the ServiceClassIDList
   * @throws NullPointerException
   *            if <code>uuid</code> is <code>null</code>
   * @throws IllegalArgumentException
   *            if <code>security</code> is not <code>ServiceRecord.NOAUTHENTICATE_NOENCRYPT</code>,
   *            <code>ServiceRecord.AUTHENTICATE_NOENCRYPT</code>, or <code>ServiceRecord.AUTHENTICATE_ENCRYPT</code>
   * @throws IOException
   *            if the Bluetooth system cannot start the request due to the current state of the Bluetooth system
   * @since TotalCross 1.2
   */
  public String selectService(UUID uuid, int security, boolean master) throws IOException {
    if (uuid == null) {
      throw new NullPointerException();
    }
    if (security != ServiceRecord.NOAUTHENTICATE_NOENCRYPT && security != ServiceRecord.AUTHENTICATE_NOENCRYPT
        && security != ServiceRecord.AUTHENTICATE_ENCRYPT) {
      throw new IllegalArgumentException();
    }

    throw new IOException("Bluetooth API is not supported on JDK"); // not supported on JDK, always throws IOException.
  }

  /**
   * Places the device into inquiry mode. The length of the inquiry is implementation dependent. This method will
   * search for devices with the specified inquiry access code. Devices that responded to the inquiry are returned to
   * the application via the method <code>deviceDiscovered()</code> of the interface <code>DiscoveryListener</code>.
   * The <code>cancelInquiry()</code> method is called to stop the inquiry.
   *
   * Not implemented on Android.
   * 
   * @param accessCode
   *           the type of inquiry to complete
   * @param listener
   *           the event listener that will receive device discovery events
   * @return <code>true</code> if the inquiry was started; <code>false</code> if the inquiry was not started because
   *         the <code>accessCode</code> is not supported
   * @throws IllegalArgumentException
   *            if the access code provided is not <code>LIAC</code>, <code>GIAC</code>, or in the range 0x9E8B00 to
   *            0x9E8B3F
   * @throws NullPointerException
   *            if <code>listener</code> is <code>null</code>
   * @throws IOException
   *            if the Bluetooth device does not allow an inquiry to be started due to other operations that are being
   *            performed by the device
   * @since TotalCross 1.2
   */
  public boolean startInquiry(int accessCode, DiscoveryListener listener) throws IOException {
    if (accessCode != LIAC || accessCode != GIAC || accessCode < 0x9E8B00 || accessCode > 0x9E8B3F) {
      throw new IllegalArgumentException();
    }
    if (listener == null) {
      throw new NullPointerException();
    }

    throw new IOException("Bluetooth API is not supported on JDK"); // not supported on JDK, always throws IOException.
  }
}
