// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io;

import java.io.IOException;
import java.io.InputStream;
import totalcross.net.Base64;
import totalcross.sys.Vm;

/**
 * Receive a stream of base64 encoded bytes and decode to the original.
 * <p/>
 * Given a base 64 encoded image (img64), one may use:
 * <pre>
 * Image createImage(String img64) {
 * 		ByteArrayStream bas = new ByteArrayStream(img64.getBytes()); // get a Stream to read the String
 * 		InputStream stringInput = bas.asInputStream(); // use ByteArrayStream as a java.io.InputStream compliant object
 *    
 * 		Base64decoderStream decoder = new Base64decoderStream(stringInput); // now we can read the original bytes
 * 		Stream imgSource = Stream.asStream(decoder); // use Base64decoderStream as a totalcross.io.Stream compliant object
 * 		return new Image(imgSource); // passing the stream directly to the image for the sake of memory
 * }
 * </pre>
 * 
 * @see {@link totalcross.io.Stream#asInputStream()}
 * @see {@link totalcross.io.Stream#asStream(java.io.InputStream)}
 * 
 * @author Jefferson Quesado
 */
public class Base64decoderStream extends InputStream {
  InputStream base64encodedStream;

  boolean eos = false;

  private static final int BYTES_ENCODED_SIZE = 4 * 1024;
  private static final int BYTES_DECODED_SIZE = 3 * 1024;
  byte[] bytesEncodedRead = new byte[BYTES_ENCODED_SIZE];
  byte[] bytesDecodedRead = new byte[BYTES_DECODED_SIZE];

  byte[] overflowEncodedRead = new byte[4];
  int overflowSize;
  // When decodedSize == -1, end of stream has been reached
  int decodedSize;
  int decodedReadPos;

  public Base64decoderStream(InputStream base64encodedStream) {
    this.base64encodedStream = base64encodedStream;

    overflowSize = 0;
    decodedSize = 0;
    decodedReadPos = 0;
  }

  @Override
  public int read() throws IOException {
    byte[] b = new byte[1];

    if (read(b) != -1) {
      return b[0];
    }
    return -1;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (eos) {
      return -1;
    }

    int arraySize = b.length;
    int maxReadable = Math.min(len, arraySize - off);
    int remaining = maxReadable;

    int offsetUsed = off;
    int totalRead = 0;

    while (remaining > 0) {
      ensureFetch();
      if (decodedSize <= 0) {
        break;
      }
      int size = Math.min(remaining, decodedSize - decodedReadPos);
      Vm.arrayCopy(bytesDecodedRead, decodedReadPos, b, offsetUsed, size);
      decodedReadPos += size;
      offsetUsed += size;
      remaining -= size;
      totalRead += size;
    }

    return totalRead;
  }

  /**
   * Unable to skip. Will throw {@link IOException}
   */
  @Override
  public long skip(long n) throws IOException {
    throw new IOException(new UnsupportedOperationException("totalcross.io.Base64Decoder.skip use is not supported"));
    // TODO Auto-generated method stub
    // return super.skip(n);
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    super.close();
  }

  private void ensureFetch() throws IOException {
    if (eos) {
      markEos();
      return;
    }
    // If read position equals size, then it must fetch more data
    if (decodedReadPos == decodedSize) {
      int oldOverflowSize = overflowSize;
      boolean hasPreviousOverflow;
      switch (oldOverflowSize) {
      case 3:
        bytesEncodedRead[2] = overflowEncodedRead[2];
      case 2:
        bytesEncodedRead[1] = overflowEncodedRead[1];
      case 1:
        bytesEncodedRead[0] = overflowEncodedRead[0];
        hasPreviousOverflow = true;
        break;
      case 0:
      default: // should not have a default case
        hasPreviousOverflow = false;
      }
      int readFromStream = base64encodedStream.read(bytesEncodedRead, oldOverflowSize, BYTES_ENCODED_SIZE);

      // Has reached end of stream?
      if (readFromStream < 0) {
        eos = true;
        if (!hasPreviousOverflow) {
          // Real end of stream, not even a single byte from a previous overflow ='(
          markEos();
          return;
        } else {
          // We still have some leftover from a previous overflow, let's use it...
          overflowSize = 0;
          for (int i = oldOverflowSize; i < 4; i++) {
            bytesEncodedRead[i] = (byte) ('=' & 0xff);
          }
          decodedReadPos = 0;
          decodedSize = Base64.decode(bytesEncodedRead, 0, 4, bytesDecodedRead, 0);
        }
      } else {
        int consideredRead = readFromStream + oldOverflowSize;
        overflowSize = consideredRead % 4;
        decodedReadPos = 0;

        if (overflowSize != 0) {
          Vm.arrayCopy(bytesEncodedRead, consideredRead - overflowSize, overflowEncodedRead, 0, overflowSize);
        }

        int usableReadData = consideredRead - overflowSize;
        if (usableReadData > 0) {
          try {
            decodedSize = Base64.decode(bytesEncodedRead, 0, usableReadData, bytesDecodedRead, 0);
          } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private void markEos() {
    eos = true;
    overflowSize = -1;
    decodedReadPos = -1;
    decodedSize = -1;
  }
}
