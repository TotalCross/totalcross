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
import totalcross.util.Hashtable;

public class ServiceRecord4D {
  private Hashtable attributes = new Hashtable(5);

  private RemoteDevice hostDevice;
  Object nativeInstance;

  private String baseURL;

  public static final int NOAUTHENTICATE_NOENCRYPT = 0x00;
  public static final int AUTHENTICATE_NOENCRYPT = 0x01;
  public static final int AUTHENTICATE_ENCRYPT = 0x02;

  ServiceRecord4D() {
  }

  ServiceRecord4D(Object nativeInstance) {
    this.nativeInstance = nativeInstance;
  }

  public RemoteDevice getHostDevice() {
    return hostDevice;
  }

  public DataElement getAttributeValue(int attrID) {
    if (attrID < 0x0000 || attrID > 0xffff) {
      throw new IllegalArgumentException();
    }
    return (DataElement) attributes.get(attrID);
  }

  public String getConnectionURL() {
    return getConnectionURL(NOAUTHENTICATE_NOENCRYPT, false);
  }

  public String getConnectionURL(int requiredSecurity, boolean mustBeMaster) {
    if (baseURL == null) {
      int commChannel = -1;

      DataElement protocolDescriptor = getAttributeValue(BluetoothConsts4D.ProtocolDescriptorList);
      if ((protocolDescriptor == null) || (protocolDescriptor.getDataType() != DataElement.DATSEQ)) {
        return null;
      }

      //  get RFCOMM Channel ProtocolDescriptorList is DATSEQ of DATSEQ of UUID and optional parameters
      boolean isL2CAP = false;
      boolean isRFCOMM = false;
      boolean isOBEX = false;

      DataElement[] protocolsSeqEnum = (DataElement[]) protocolDescriptor.getValue();
      for (int i = 0; i < protocolsSeqEnum.length; i++) {
        DataElement elementSeq = (DataElement) protocolsSeqEnum[i];
        if (elementSeq.getDataType() != DataElement.DATSEQ) {
          continue;
        }

        DataElement[] elementSeqEnum = (DataElement[]) elementSeq.getValue();
        if (elementSeqEnum.length <= 0) {
          continue;
        }

        DataElement protocolElement = (DataElement) elementSeqEnum[0];
        if (protocolElement.getDataType() != DataElement.UUID) {
          continue;
        }

        Object uuid = protocolElement.getValue();
        if (BluetoothConsts4D.OBEX_PROTOCOL_UUID.equals(uuid)) {
          isOBEX = true;
          isRFCOMM = false;
          isL2CAP = false;
        } else if (elementSeqEnum.length > 1 && (BluetoothConsts4D.RFCOMM_PROTOCOL_UUID.equals(uuid))) {
          DataElement protocolPSMElement = (DataElement) elementSeqEnum[1];

          switch (protocolPSMElement.getDataType()) {
          case DataElement.U_INT_1:
          case DataElement.U_INT_2:
          case DataElement.U_INT_4:
          case DataElement.INT_1:
          case DataElement.INT_2:
          case DataElement.INT_4:
          case DataElement.INT_8:
            long val = protocolPSMElement.getLong();
            if ((val >= BluetoothConsts4D.RFCOMM_CHANNEL_MIN) && (val <= BluetoothConsts4D.RFCOMM_CHANNEL_MAX)) {
              commChannel = (int) val;
              isRFCOMM = true;
              isL2CAP = false;
            }
            break;
          }
        } else if (elementSeqEnum.length > 1 && (BluetoothConsts4D.L2CAP_PROTOCOL_UUID.equals(uuid))) {
          DataElement protocolPSMElement = (DataElement) elementSeqEnum[1];
          switch (protocolPSMElement.getDataType()) {
          case DataElement.U_INT_1:
          case DataElement.U_INT_2:
          case DataElement.U_INT_4:
          case DataElement.INT_1:
          case DataElement.INT_2:
          case DataElement.INT_4:
          case DataElement.INT_8:
            long pcm = protocolPSMElement.getLong();
            if ((pcm >= BluetoothConsts4D.L2CAP_PSM_MIN) && (pcm <= BluetoothConsts4D.L2CAP_PSM_MAX)) {
              commChannel = (int) pcm;
              isL2CAP = true;
            }
            break;
          }
        }
      }

      // check if the fields we need were set.
      if (commChannel == -1) {
        return null;
      }
      if (!isOBEX && !isRFCOMM && !isL2CAP) {
        return null;
      }

      // start the url
      StringBuffer urlBuffer = new StringBuffer();
      urlBuffer.append(isOBEX ? BluetoothConsts4D.PROTOCOL_SCHEME_BT_OBEX
          : (isRFCOMM ? BluetoothConsts4D.PROTOCOL_SCHEME_RFCOMM : BluetoothConsts4D.PROTOCOL_SCHEME_L2CAP));
      urlBuffer.append("://").append(hostDevice.address).append(":");

      // comm channel
      if (isL2CAP) {
        urlBuffer.append(Convert.zeroPad(Convert.toString(commChannel, 16), 4));
      } else {
        urlBuffer.append(commChannel);
      }

      baseURL = urlBuffer.toString();
    }

    String url = baseURL;
    // security
    switch (requiredSecurity) {
    case NOAUTHENTICATE_NOENCRYPT:
      url += ";authenticate=false;encrypt=false";
      break;
    case AUTHENTICATE_NOENCRYPT:
      url += ";authenticate=true;encrypt=false";
      break;
    case AUTHENTICATE_ENCRYPT:
      url += ";authenticate=true;encrypt=true";
      break;
    default:
      throw new IllegalArgumentException();
    }

    // master
    return url + (mustBeMaster ? ";master=true" : ";master=false");
  }

  boolean readSDP4D(RemoteDevice device, byte[] input, int[] attrIDs) throws IOException {
    this.hostDevice = device;

    boolean anyRetrieved = false;
    DataElement element = new SDPInputStream(input).readElement();
    DataElement[] elements = (DataElement[]) element.getValue();
    for (int i = 0; i < elements.length; i += 2) {
      int attrID = (int) elements[i].getLong();
      populateAttributeValue(attrID, elements[i + 1]);
      if (!anyRetrieved) {
        for (int j = attrIDs.length - 1; j >= 0; j--) {
          if (attrIDs[j] == attrID) {
            anyRetrieved = true;
            break;
          }
        }
      }
    }
    return anyRetrieved;
  }

  void populateAttributeValue(int attrID, DataElement attrValue) {
    if (attrID < 0x0000 || attrID > 0xffff) {
      throw new IllegalArgumentException();
    }
    if (attrValue == null) {
      attributes.remove(attrID);
    } else {
      attributes.put(attrID, attrValue);
    }
  }
}
