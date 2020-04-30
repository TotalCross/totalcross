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

import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.sys.CharacterConverter;
import totalcross.sys.Convert;
import totalcross.sys.UTF8CharacterConverter;

/**
 * Parses service record info. 
 * 
 * @since TotalCross 1.27
 */
class SDPInputStream extends DataStream {
  final private static UTF8CharacterConverter converterUTF8 = (UTF8CharacterConverter) Convert.charsetForName("UTF-8");
  final private static CharacterConverter converterASCII = (CharacterConverter) Convert.charsetForName("ISO-8859-1");
  public SDPInputStream(byte[] input) {
    super(new ByteArrayStream(input), true);
  }

  private byte[] readBytes(int size) throws IOException {
    byte[] buffer = new byte[size];
    readBytes(buffer);
    return buffer;
  }

  public DataElement readElement() throws IOException {
    int header = readByte();
    int type = header >> 3 & 0x1f;
    int sizeDescriptor = header & 0x07;

    switch (type) {
    case 0: // NULL
      return new DataElement(DataElement.NULL);
    case 1: // U_INT
      switch (sizeDescriptor) {
      case 0:
        return new DataElement(DataElement.U_INT_1, readUnsignedByte());
      case 1:
        return new DataElement(DataElement.U_INT_2, readUnsignedShort());
      case 2:
        return new DataElement(DataElement.U_INT_4, readUnsignedInt());
      case 3:
        return new DataElement(DataElement.U_INT_8, readBytes(8));
      case 4:
        return new DataElement(DataElement.U_INT_16, readBytes(16));
      default:
        throw new IOException();
      }
    case 2: // INT
      switch (sizeDescriptor) {
      case 0:
        return new DataElement(DataElement.INT_1, readByte());
      case 1:
        return new DataElement(DataElement.INT_2, readShort());
      case 2:
        return new DataElement(DataElement.INT_4, readInt());
      case 3:
        return new DataElement(DataElement.INT_8, readLong());
      case 4:
        return new DataElement(DataElement.INT_16, readBytes(16));
      default:
        throw new IOException();
      }
    case 3: // UUID
    {
      UUID uuid = null;
      switch (sizeDescriptor) {
      case 1:
        uuid = new UUID(readUnsignedShort());
        break;
      case 2:
        uuid = new UUID(readUnsignedInt());
        break;
      case 4:
        uuid = new UUID(Convert.bytesToHexString(readBytes(16)), false);
        break;
      default:
        throw new IOException();
      }
      return new DataElement(DataElement.UUID, uuid);
    }
    case 4: // STRING
    {
      int length = -1;
      switch (sizeDescriptor) {
      case 5:
        length = readUnsignedByte();
        break;
      case 6:
        length = readUnsignedShort();
        break;
      case 7:
        length = readInt();
        break;
      default:
        throw new IOException();
      }
      byte[] bytes = readBytes(length);
      return new DataElement(DataElement.STRING, new String(converterUTF8.bytes2chars(bytes, 0, bytes.length)));
    }
    case 5: // BOOL
      return new DataElement(readBoolean());
    case 6: // DATSEQ
    {
      long length;
      switch (sizeDescriptor) {
      case 5:
        length = readUnsignedByte();
        break;
      case 6:
        length = readUnsignedShort();
        break;
      case 7:
        length = readUnsignedInt();
        break;
      default:
        throw new IOException();
      }

      DataElement element = new DataElement(DataElement.DATSEQ);

      ByteArrayStream inputStream = (ByteArrayStream) super.stream;
      int started = inputStream.getPos();
      for (long end = started + length; inputStream.getPos() < end;) {
        element.addElement(readElement());
      }

      if ((started + length) != inputStream.getPos()) {
        throw new IOException("DATSEQ size corruption " + (started + length - inputStream.getPos()));
      }
      return element;
    }
    case 7: // DATALT
    {
      long length;
      switch (sizeDescriptor) {
      case 5:
        length = readUnsignedByte();
        break;
      case 6:
        length = readUnsignedShort();
        break;
      case 7:
        length = readUnsignedInt();
        break;
      default:
        throw new IOException();
      }

      DataElement element = new DataElement(DataElement.DATALT);

      ByteArrayStream inputStream = (ByteArrayStream) super.stream;
      int started = inputStream.getPos();
      for (long end = started + length; inputStream.getPos() < end;) {
        element.addElement(readElement());
      }

      if ((started + length) != inputStream.getPos()) {
        throw new IOException("DATALT size corruption " + (started + length - inputStream.getPos()));
      }
      return element;
    }
    case 8: // URL
    {
      int length;

      switch (sizeDescriptor) {
      case 5:
        length = readUnsignedByte();
        break;
      case 6:
        length = readUnsignedShort();
        break;
      case 7:
        length = readInt();
        break;
      default:
        throw new IOException();
      }
      byte[] bytes = readBytes(length);
      return new DataElement(DataElement.URL, new String(converterASCII.bytes2chars(bytes, 0, bytes.length)));
    }
    default:
      throw new IOException("Unknown type " + type);
    }
  }
}
