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
 * The <code>DiscoveryListener</code> interface allows an application to receive device discovery and service discovery
 * events. This interface provides four methods, two for discovering devices and two for discovering services.
 * 
 * @since TotalCross 1.2
 */
public interface DiscoveryListener {
  /**
   * Indicates the normal completion of device discovery. Used with the <code>inquiryCompleted()</code> method.
   * <p>
   * The value of <code>INQUIRY_COMPLETED</code> is 0x00 (0).
   * </p>
   */
  public static final int INQUIRY_COMPLETED = 0x00;

  /**
   * Indicates that the inquiry request failed to complete normally, but was not cancelled.
   * <p>
   * The value of <code>INQUIRY_ERROR</code> is 0x07 (7).
   * </p>
   */
  public static final int INQUIRY_ERROR = 0x07;

  /**
   * Indicates device discovery has been canceled by the application and did not complete. Used with the
   * <code>inquiryCompleted()</code> method.
   * <p>
   * The value of <code>INQUIRY_TERMINATED</code> is 0x05 (5).
   * </p>
   */
  public static final int INQUIRY_TERMINATED = 0x05;

  /**
   * Indicates the normal completion of service discovery. Used with the <code>serviceSearchCompleted()</code> method.
   * <p>
   * The value of <code>SERVICE_SEARCH_COMPLETED</code> is 0x01 (1).
   * </p>
   */
  public static final int SERVICE_SEARCH_COMPLETED = 0x01;

  /**
   * Indicates the service search could not be completed because the remote device provided to
   * <code>DiscoveryAgent.searchServices()</code> could not be reached.
   * 
   * Used with the <code>serviceSearchCompleted()</code> method.
   * <p>
   * The value of <code>SERVICE_SEARCH_DEVICE_NOT_REACHABLE</code> is 0x06 (6).
   * </p>
   */
  public static final int SERVICE_SEARCH_DEVICE_NOT_REACHABLE = 0x06;

  /**
   * Indicates the service search terminated with an error.
   * 
   * Used with the <code>serviceSearchCompleted()</code> method.
   * <p>
   * The value of <code>SERVICE_SEARCH_ERROR</code> is 0x03 (3).
   * </p>
   */
  public static final int SERVICE_SEARCH_ERROR = 0x03;

  /**
   * Indicates the service search has completed with no service records found on the device.
   * 
   * Used with the <code>serviceSearchCompleted()</code> method.
   * <p>
   * The value of <code>SERVICE_SEARCH_NO_RECORDS</code> is 0x04 (4).
   * </p>
   */
  public static final int SERVICE_SEARCH_NO_RECORDS = 0x04;

  /**
   * Indicates the service search has been canceled by the application and did not complete. Used with the
   * <code>serviceSearchCompleted()</code> method.
   * <p>
   * The value of <code>SERVICE_SEARCH_TERMINATED</code> is 0x02 (2).
   * </p>
   */
  public static final int SERVICE_SEARCH_TERMINATED = 0x02;

  /**
   * Called when a device is found during an inquiry. An inquiry searches for devices that are discoverable. The same
   * device may be returned multiple times.
   * 
   * @param btDevice
   *           the device that was found during the inquiry
   * @param cod
   *           the service classes, major device class, and minor device class of the remote device
   * @since TotalCross 1.2
   */
  public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod);

  /**
   * Called when an inquiry is completed. <code>discType</code> will be <code>INQUIRY_COMPLETED</code> if the
   * inquiry ended normally or <code>INQUIRY_TERMINATED</code> if the inquiry was canceled by a call to
   * <code>DiscoveryAgent.cancelInquiry()</code>. The <code>discType</code> will be <code>INQUIRY_ERROR</code> if an
   * error occurred while processing the inquiry causing the inquiry to end abnormally.
   * 
   * @param discType
   *           the type of request that was completed; either <code>INQUIRY_COMPLETED</code>,
   *           <code>INQUIRY_TERMINATED</code>, or <code>INQUIRY_ERROR</code>
   * @since TotalCross 1.2
   */
  public void inquiryCompleted(int discType);

  /**
   * Called when service(s) are found during a service search.
   * 
   * @param transID
   *           the transaction ID of the service search that is posting the result
   * @param servRecord
   *           a list of services found during the search request
   * @since TotalCross 1.2
   */
  public void servicesDiscovered(int transID, ServiceRecord[] servRecord);

  /**
   * Called when a service search is completed or was terminated because of an error. Legal status values in the
   * <code>respCode</code> argument include <code>SERVICE_SEARCH_COMPLETED</code>,
   * <code>SERVICE_SEARCH_TERMINATED</code>, <code>SERVICE_SEARCH_ERROR</code>, <code>SERVICE_SEARCH_NO_RECORDS</code>
   * and <code>SERVICE_SEARCH_DEVICE_NOT_REACHABLE</code>. The following table describes when each
   * <code>respCode</code> will be used:
   * <table>
   * 
   * <tbody>
   * <tr>
   * <th><code>respCode</code></th>
   * <th>Reason</th>
   * </tr>
   * <tr>
   * <td><code>SERVICE_SEARCH_COMPLETED</code></td>
   * <td>if the service search completed normally</td>
   * </tr>
   * <tr>
   * <td><code>SERVICE_SEARCH_TERMINATED</code></td>
   * <td>if the service search request was cancelled by a call to <code>DiscoveryAgent.cancelServiceSearch()</code></td>
   * </tr>
   * 
   * <tr>
   * <td><code>SERVICE_SEARCH_ERROR</code></td>
   * <td>if an error occurred while processing the request</td>
   * </tr>
   * <tr>
   * <td><code>SERVICE_SEARCH_NO_RECORDS</code></td>
   * <td>if no records were found during the service search</td>
   * </tr>
   * <tr>
   * <td><code>SERVICE_SEARCH_DEVICE_NOT_REACHABLE</code></td>
   * <td>if the device specified in the search request could not be reached or the local device could not establish a
   * connection to the remote device</td>
   * </tr>
   * 
   * </tbody>
   * </table>
   * 
   * @param transID
   *           the transaction ID identifying the request which initiated the service search
   * @param respCode
   *           the response code that indicates the status of the transaction
   * @since TotalCross 1.2
   */
  public void serviceSearchCompleted(int transID, int respCode);
}
