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
  private int formatCode;
  private byte[] bytes;
  private int length;
  private int intrinsicWidth;
  private int intrinsicHeight;
  private int logicalWidth;
  private int logicalHeight;
  private int frameCount;
  private String comment;
  private long nativeBag;
  private ImageBacking decodedBacking;
  private int decodedWidth;
  private int decodedHeight;
  private int decodedDenominator;
  private long decodedGeneration;
  private ImageException decodeFailure;

  private EncodedImageSource() {
  }

  static EncodedImageSource fromBytes(byte[] input) throws ImageException {
    if (input == null) {
      throw new ImageException("Description is null");
    }
    return fromBytes(input, input.length);
  }

  static EncodedImageSource fromOwnedBytes(byte[] input) throws ImageException {
    if (input == null) {
      throw new ImageException("Description is null");
    }
    EncodedImageSource source = new EncodedImageSource();
    source.captureNative(input, input.length);
    return source;
  }

  static EncodedImageSource fromBytes(byte[] input, int length) throws ImageException {
    if (input == null || length < 0 || length > input.length) {
      throw new ImageException("Invalid image description");
    }
    byte[] owned = new byte[length];
    System.arraycopy(input, 0, owned, 0, length);
    EncodedImageSource source = new EncodedImageSource();
    source.captureNative(owned, length);
    return source;
  }

  static EncodedImageSource fromPath(String path) throws ImageException, IOException {
    if (path == null) {
      throw new ImageException("ERROR: can't open image file " + path);
    }
    EncodedImageSource source = new EncodedImageSource();
    source.captureNativePath(path);
    return source;
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
    EncodedImageSource source = new EncodedImageSource();
    source.captureNative(owned, owned.length);
    return source;
  }

  ImageEncodedStructure.Format getFormat() {
    switch (formatCode) {
      case 1: return ImageEncodedStructure.Format.PNG;
      case 2: return ImageEncodedStructure.Format.JPEG;
      case 3: return ImageEncodedStructure.Format.GIF;
      case 4: return ImageEncodedStructure.Format.BMP;
      default: throw new IllegalStateException("Unknown encoded image format: " + formatCode);
    }
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

  @Override
  int width() {
    return frameCount > 1 ? logicalWidth : intrinsicWidth;
  }

  @Override
  int height() {
    return intrinsicHeight;
  }

  @Override
  int logicalWidth() {
    return logicalWidth;
  }

  @Override
  int logicalHeight() {
    return logicalHeight;
  }

  @Override
  int frameCount() {
    return frameCount;
  }

  @Override
  int widthOfAllFrames() {
    return intrinsicWidth;
  }

  @Override
  double contentScale() {
    return 1;
  }

  String getComment() {
    return comment;
  }

  byte[] copyBytes() {
    if (bytes == null) {
      throw new IllegalStateException("Encoded bytes are held by the native image bag");
    }
    byte[] copy = new byte[length];
    System.arraycopy(bytes, 0, copy, 0, length);
    return copy;
  }

  byte[] bytesForInternalDecode() {
    return bytes;
  }

  boolean hasJavaBackingForSmoke() {
    return bytes != null;
  }

  boolean hasNativeBackingForSmoke() {
    return nativeBag != 0;
  }

  ImageException decodeFailure() {
    return decodeFailure;
  }

  void cacheDecodeFailure(ImageException failure) {
    if (decodeFailure == null) {
      decodeFailure = failure;
    }
  }

  synchronized ImageBacking decodedBackingForReuse(int requestedDenominator) {
    if (requestedDenominator <= 0 || decodedBacking == null || !decodedBacking.isValid()
        || decodedWidth <= 0 || decodedHeight <= 0 || decodedDenominator <= 0
        || decodedDenominator > requestedDenominator) {
      return null;
    }
    return decodedBacking;
  }

  synchronized int decodedWidth() {
    return decodedWidth;
  }

  synchronized int decodedHeight() {
    return decodedHeight;
  }

  synchronized int decodedDenominator() {
    return decodedDenominator;
  }

  synchronized long decodedGeneration() {
    return decodedGeneration;
  }

  synchronized void installDecodedBacking(ImageBacking backing, int width, int height, int denominator) {
    if (backing == null || !backing.isValid() || width <= 0 || height <= 0
        || (denominator != 1 && denominator != 2 && denominator != 4 && denominator != 8)) {
      throw new IllegalArgumentException("Invalid decoded image backing");
    }
    if (decodedBacking == backing && decodedWidth == width && decodedHeight == height
        && decodedDenominator == denominator) {
      return;
    }
    decodedBacking = backing;
    decodedWidth = width;
    decodedHeight = height;
    decodedDenominator = denominator;
    decodedGeneration++;
  }

  synchronized void evictDecodedBacking() {
    if (decodedBacking == null && decodedWidth == 0 && decodedHeight == 0 && decodedDenominator == 0) {
      return;
    }
    decodedBacking = null;
    decodedWidth = 0;
    decodedHeight = 0;
    decodedDenominator = 0;
    decodedGeneration++;
  }

  void releaseForSmoke() {
    releaseNativeBag();
  }

  /** Replaced on deployed targets by an immutable native bag copy. */
  @ReplacedByNativeOnDeploy
  private void captureNative(byte[] input, int length) throws ImageException {
    ImageEncodedStructure.Inspection inspection = ImageEncodedStructure.inspect(input, length);
    this.bytes = input;
    this.length = length;
    this.formatCode = inspection.format.ordinal() + 1;
    this.intrinsicWidth = inspection.width;
    this.intrinsicHeight = inspection.height;
    this.logicalWidth = inspection.logicalWidth;
    this.logicalHeight = inspection.logicalHeight;
    this.frameCount = inspection.frameCount;
    this.comment = inspection.comment;
  }

  /** Replaced on deployed targets by TCZ-first native path capture. */
  @ReplacedByNativeOnDeploy
  private void captureNativePath(String path) throws ImageException, IOException {
    if (path == null || Launcher.instance == null) {
      throw new ImageException("ERROR: can't open image file " + path);
    }
    byte[] input = Launcher.instance.readBytes(path);
    if (input == null) {
      throw new ImageException("ERROR: can't open image file " + path);
    }
    captureNative(input, input.length);
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
