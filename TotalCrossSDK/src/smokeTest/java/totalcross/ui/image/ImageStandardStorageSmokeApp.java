// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.runtime.ImageRuntimeConfigurationStartup;
import totalcross.sys.runtime.ImageRuntimePolicy;
import totalcross.sys.runtime.RuntimeCondition;
import totalcross.sys.runtime.RuntimeConfiguration;
import totalcross.sys.runtime.RuntimeWhen;
import totalcross.ui.MainWindow;

/** Confirms encoded sources keep full-precision storage under STANDARD on macOS ARM64. */
@ImageRuntimeRule(when = @RuntimeWhen(allOf = {
    @RuntimeCondition(platform = Platform.MACOS),
    @RuntimeCondition(family = RuntimeFamily.DESKTOP),
    @RuntimeCondition(architecture = Architecture.ARM64),
    @RuntimeCondition(backend = GraphicsBackend.RASTER)
}), storage = ImageStorageProfile.STANDARD)
@RuntimeConfiguration
public final class ImageStandardStorageSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean requested = false;
    boolean effective = false;
    boolean jpegRgba = false;
    boolean jpegPixelParity = false;
    boolean pngRgba = false;
    boolean pngPixelParity = false;
    boolean alphaRgba = false;
    boolean alphaPixelParity = false;
    boolean alphaCompositeParity = false;
    int jpegPixelError = -1;
    int pngPixelError = -1;
    int alphaPixelError = -1;
    int alphaCompositeError = -1;
    long compactDecodeCount = -1;
    long compactDecodeBytes = -1;
    String error = "";
    try {
      NativeImageBacking.resetBackingAccountingForTest();
      ImageRuntimePolicy policy = ImageRuntimeConfigurationStartup.currentPolicy();
      requested = policy.requestedStorageProfile() == ImageStorageProfile.STANDARD;
      effective = policy.effectiveStorageProfile() == ImageStorageProfile.STANDARD;
      Image jpeg = ImageCompactStorageSmokeSupport.load("opaque-color.jpg");
      int[] jpegPixels = jpeg.getPixels();
      jpegRgba = fullPrecisionBacking(jpeg);
      jpegPixelError = ImageCompactStorageSmokeSupport.maxChannelError(jpegPixels,
          ImageCompactStorageSmokeSupport.expectedOpaqueSource());
      jpegPixelParity = jpegPixelError <= 24;
      Image png = ImageCompactStorageSmokeSupport.load("opaque-color.png");
      int[] pngPixels = png.getPixels();
      pngRgba = fullPrecisionBacking(png);
      pngPixelError = ImageCompactStorageSmokeSupport.maxChannelError(pngPixels,
          ImageCompactStorageSmokeSupport.expectedOpaqueSource());
      pngPixelParity = pngPixelError <= 9;
      Image alpha = ImageCompactStorageSmokeSupport.load("alpha-gradient.png");
      int[] alphaPixels = alpha.getPixels();
      alphaRgba = fullPrecisionBacking(alpha);
      int[] expectedAlpha = ImageCompactStorageSmokeSupport.expectedAlphaSource();
      alphaPixelError = ImageCompactStorageSmokeSupport.maxChannelError(alphaPixels, expectedAlpha);
      alphaCompositeError = ImageCompactStorageSmokeSupport.compositingError(expectedAlpha, alphaPixels);
      alphaPixelParity = alphaPixelError <= 1;
      alphaCompositeParity = alphaCompositeError <= 1;
      compactDecodeCount = ImageCompactStorageSmokeSupport.compactDecodeCount();
      compactDecodeBytes = ImageCompactStorageSmokeSupport.compactDecodeBytes();
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean overallPass = requested && effective && jpegRgba && jpegPixelParity
        && pngRgba && pngPixelParity && alphaRgba && alphaPixelParity && alphaCompositeParity
        && compactDecodeCount == 0 && compactDecodeBytes == 0;
    System.out.println("fixture=ImageStandardStorageSmokeApp,requested=" + requested
        + ",effective=" + effective + ",standardJpegRgba=" + jpegRgba
        + ",standardJpegPixelParity=" + jpegPixelParity
        + ",requestedStorage=STANDARD,effectiveStorage=STANDARD"
        + ",standardPngRgba=" + pngRgba + ",standardPngPixelParity=" + pngPixelParity
        + ",standardAlphaRgba=" + alphaRgba
        + ",standardAlphaPixelParity=" + alphaPixelParity + ",standardAlphaCompositeParity="
        + alphaCompositeParity + ",compactDecodeCount=" + compactDecodeCount + ",compactDecodeBytes="
        + compactDecodeBytes
        + ",overallPass=" + overallPass + ",jpegPixelError=" + jpegPixelError
        + ",pngPixelError=" + pngPixelError + ",alphaPixelError=" + alphaPixelError
        + ",alphaCompositeError=" + alphaCompositeError
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }

  private static boolean fullPrecisionBacking(Image image) {
    NativeImageBacking backing = ImageCompactStorageSmokeSupport.nativeBacking(image);
    return !backing.isCompact()
        && backing.formatForTest() == NativeImageBacking.FORMAT_RGBA8888
        && backing.rowBytesForTest() == ImageCompactStorageSmokeSupport.WIDTH * 4
        && backing.backingBytesForTest()
            == (long) ImageCompactStorageSmokeSupport.WIDTH * ImageCompactStorageSmokeSupport.HEIGHT * 4;
  }
}
