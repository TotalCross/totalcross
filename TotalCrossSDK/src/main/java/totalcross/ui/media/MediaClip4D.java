// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.media;

import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.io.RandomAccessStream;

public class MediaClip4D {
  Object mediaClipRef;
  RandomAccessStream mediaStream;
  protected String path;
  byte[] mediaHeader = new byte[46];
  int mediaDataSize;
  int currentState;

  boolean loaded;
  int size;
  boolean dontFinalize;
  boolean stopped;
  boolean finished;
  boolean isRecording;

  int dataPos;

  int internalState;

  public final static int UNREALIZED = 0;
  public final static int REALIZED = 1;
  public final static int PREFETCHED = 2;
  public final static int STARTED = 3;
  public final static int CLOSED = 4;

  public static final int VOICE = 8000;
  public static final int LOW = 11025;
  public static final int MEDIUM = 22050;
  public static final int HIGH = 44100;

  public MediaClip4D(RandomAccessStream stream) throws IOException {
    mediaStream = stream;
    if (stream instanceof File) {
      this.path = ((File) stream).getPath();
    }
    create();
    currentState = UNREALIZED;
  }

  final native private void create();

  public int getCurrentState() {
    return currentState;
  }

  final public void start() throws IOException {
    if (currentState == PREFETCHED) {
      // resume
      nativeStart();
    } else if (currentState == UNREALIZED) {
      int subChunkId;
      int subChunkSize;

      // REALIZE WAVE FILE
      dataPos += mediaStream.readBytes(mediaHeader, 0, 36);
      subChunkSize = (((mediaHeader[19] & 0xFF) << 24) | ((mediaHeader[18] & 0xFF) << 16)
          | ((mediaHeader[17] & 0xFF) << 8) | (mediaHeader[16] & 0xFF));
      if (subChunkSize > 16) {
        dataPos += mediaStream.readBytes(mediaHeader, 36, 2);
        subChunkSize = (((mediaHeader[37] & 0xFF) << 8) | (mediaHeader[36] & 0xFF));
      } else {
        subChunkSize = 0;
      }

      do {
        if (subChunkSize > 0) {
          dataPos += mediaStream.skipBytes(subChunkSize);
        }
        dataPos += mediaStream.readBytes(mediaHeader, 38, 4);
        subChunkId = (((mediaHeader[41] & 0xFF) << 24) | ((mediaHeader[40] & 0xFF) << 16)
            | ((mediaHeader[39] & 0xFF) << 8) | (mediaHeader[38] & 0xFF));
        dataPos += mediaStream.readBytes(mediaHeader, 42, 4);
        subChunkSize = (((mediaHeader[45] & 0xFF) << 24) | ((mediaHeader[44] & 0xFF) << 16)
            | ((mediaHeader[43] & 0xFF) << 8) | (mediaHeader[42] & 0xFF));
      } while (subChunkId != 0x61746164); // 'data' in big endian

      mediaDataSize = subChunkSize;

      // PREFETCH - prepare stuff before starting 
      // START - actually start playing

      nativeStart();
    }
  }

  final native private void nativeStart();

  final native public void stop();

  final native public void reset();

  final native private void nativeClose();

  final public void close() throws totalcross.io.IOException {
    if (currentState != CLOSED) {
      dontFinalize = true;
      nativeClose();
    }
  }

  public void onEvent(MediaClipEvent evt) {
  }

  native public void record(int samplesPerSecond, int bitsPerSample, boolean stereo) throws totalcross.io.IOException;

  @Override
  public void finalize() {
    if (currentState != CLOSED) {
      try {
        this.close();
      } catch (IOException e) {
      }
    }
  }
}
