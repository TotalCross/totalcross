// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.io.ByteArrayOutputStream;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.Launcher;
import totalcross.io.IOException;
import totalcross.io.Stream;

/** Immutable, eagerly captured encoded image source. */
final class EncodedImageSource extends ImageSource {
  private final ImageEncodedStructure.Format format;
  private byte[] bytes;
  private final int length;
  private final int intrinsicWidth;
  private final int intrinsicHeight;
  private final int logicalWidth;
  private final int logicalHeight;
  private final int frameCount;
  private final String comment;
  private long nativeBag;

  private EncodedImageSource(byte[] ownedBytes, int length) throws ImageException {
    ImageEncodedStructure.Inspection inspection = ImageEncodedStructure.inspect(ownedBytes, length);
    this.bytes = ownedBytes;
    this.length = length;
    this.format = inspection.format;
    this.intrinsicWidth = inspection.width;
    this.intrinsicHeight = inspection.height;
    this.logicalWidth = inspection.logicalWidth;
    this.logicalHeight = inspection.logicalHeight;
    this.frameCount = inspection.frameCount;
    this.comment = inspection.comment;
  }

  static EncodedImageSource fromBytes(byte[] input) throws ImageException {
    if (input == null) {
      throw new ImageException("Description is null");
    }
    return fromBytes(input, input.length);
  }

  static EncodedImageSource fromBytes(byte[] input, int length) throws ImageException {
    if (input == null || length < 0 || length > input.length) {
      throw new ImageException("Invalid image description");
    }
    byte[] owned = new byte[length];
    System.arraycopy(input, 0, owned, 0, length);
    return new EncodedImageSource(owned, length);
  }

  static EncodedImageSource fromPath(String path) throws ImageException, IOException {
    if (path == null || Launcher.instance == null) {
      throw new ImageException("ERROR: can't open image file " + path);
    }
    byte[] bytes = Launcher.instance.readBytes(path);
    if (bytes == null) {
      throw new ImageException("ERROR: can't open image file " + path);
    }
    return new EncodedImageSource(bytes, bytes.length);
  }

  static EncodedImageSource fromStream(Stream stream) throws ImageException, IOException {
    if (stream == null) {
      throw new ImageException("Can't read from Stream");
    }
    ByteArrayOutputStream captured = new ByteArrayOutputStream(1024);
    byte[] buffer = new byte[1024];
    while (true) {
      int count = stream.readBytes(buffer, 0, buffer.length);
      if (count < 0) {
        break;
      }
      if (count == 0) {
        break;
      }
      captured.write(buffer, 0, count);
    }
    byte[] owned = captured.toByteArray();
    return new EncodedImageSource(owned, owned.length);
  }

  ImageEncodedStructure.Format getFormat() {
    return format;
  }

  int getEncodedLength() {
    return length;
  }

  int getIntrinsicWidth() {
    return intrinsicWidth;
  }

  int getIntrinsicHeight() {
    return intrinsicHeight;
  }

  int getLogicalWidth() {
    return logicalWidth;
  }

  int getLogicalHeight() {
    return logicalHeight;
  }

  int getFrameCount() {
    return frameCount;
  }

  String getComment() {
    return comment;
  }

  byte[] copyBytes() {
    byte[] copy = new byte[length];
    System.arraycopy(bytes, 0, copy, 0, length);
    return copy;
  }

  byte[] bytesForInternalDecode() {
    return bytes;
  }

  /** Replaced on deployed targets by an immutable native bag copy. */
  @ReplacedByNativeOnDeploy
  private void captureNative(byte[] input, int length) {
    nativeBag = 0;
  }

  /** Replaced on deployed targets by TCZ-first native path capture. */
  @ReplacedByNativeOnDeploy
  private void captureNativePath(String path) {
    nativeBag = 0;
  }

  /** Idempotently releases the deployed encoded bag. */
  @ReplacedByNativeOnDeploy
  private void releaseNativeBag() {
    nativeBag = 0;
  }

  @Override
  protected void finalize() {
    releaseNativeBag();
  }
}
