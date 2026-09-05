// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Integrated Phase-2 workload for decode, draw, readback, encoding, and color. */
public class ImageRasterCombinedBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;
  private static final int DRAWS_PER_SAMPLE = 1024;

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "pre");
    int samples = ImageRasterBenchmarkSupport.integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    int completedSamples = 0;
    String error = "";

    try {
      ImageRasterBenchmarkSupport.require(samples > 0 && samples <= 200,
          "samples must be between 1 and 200");
      ImageRasterBenchmarkSupport.configureAllRasterFeatures(scenario);
      byte[] jpeg = ImageRasterBenchmarkSupport.resource("image-abi/lena512.jpg");
      byte[] png = ImageRasterBenchmarkSupport.opaquePng(512, 512);
      for (int warmup = 0; warmup < 3; warmup++) {
        runWorkload(jpeg, png);
      }
      Image.resetImageOperationAccountingForTest();
      String expectedPixelHash = null;
      String expectedPngHash = null;
      String expectedColorHash = null;
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        Result result = runWorkload(jpeg, png);
        long elapsed = Vm.getTimeStamp() - start;
        String pixelHash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullPixelHash(result.pixels));
        String pngHash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullByteHash(result.encoded, result.encodedLength));
        String colorHash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullPixelHash(result.colorPixels));
        if (expectedPixelHash == null) {
          expectedPixelHash = pixelHash;
          expectedPngHash = pngHash;
          expectedColorHash = colorHash;
        } else {
          ImageRasterBenchmarkSupport.require(expectedPixelHash.equals(pixelHash), "pixel hash drift");
          ImageRasterBenchmarkSupport.require(expectedPngHash.equals(pngHash), "PNG hash drift");
          ImageRasterBenchmarkSupport.require(expectedColorHash.equals(colorHash), "color hash drift");
        }
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed
            + ",draws=" + DRAWS_PER_SAMPLE
            + ",pixel_hash=" + pixelHash + ",png_hash=" + pngHash + ",color_hash=" + colorHash
            + ",decode_zero_copy=" + Image.zeroCopyDecodeCountForTest()
            + ",opacity_known_source=" + Image.opacityKnownFromSourceForTest()
            + ",opacity_determined_decode=" + Image.opacityDeterminedDuringDecodeForTest()
            + ",write_pixels_attempts=" + NativeImageBacking.writePixelsAttemptsForTest()
            + ",write_pixels_hits=" + NativeImageBacking.writePixelsHitsForTest()
            + ",write_pixels_fallbacks=" + NativeImageBacking.writePixelsFallbacksForTest()
            + ",row_readbacks=" + Image.rowReadbackCountForTest()
            + ",full_readbacks=" + Image.fullReadbackCountForTest()
            + ",direct_color_materializations=" + Image.directColorMaterializationCountForTest());
        System.out.flush();
        completedSamples = sample;
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = ImageRasterBenchmarkSupport.finish("ImageRasterCombinedBenchmarkApp", scenario,
        samples, completedSamples,
        "draws=" + DRAWS_PER_SAMPLE
            + ",decode_zero_copy=" + Image.zeroCopyDecodeCountForTest()
            + ",opacity_known_source=" + Image.opacityKnownFromSourceForTest()
            + ",opacity_determined_decode=" + Image.opacityDeterminedDuringDecodeForTest()
            + ",write_pixels_attempts=" + NativeImageBacking.writePixelsAttemptsForTest()
            + ",write_pixels_hits=" + NativeImageBacking.writePixelsHitsForTest()
            + ",write_pixels_fallbacks=" + NativeImageBacking.writePixelsFallbacksForTest()
            + ",row_readbacks=" + Image.rowReadbackCountForTest()
            + ",full_readbacks=" + Image.fullReadbackCountForTest()
            + ",direct_color_materializations=" + Image.directColorMaterializationCountForTest(), error);
    exit(pass ? 0 : 1);
  }

  private static Result runWorkload(byte[] jpeg, byte[] png) throws Exception {
    Image jpegImage = new Image(jpeg, jpeg.length);
    Image pngImage = new Image(png, png.length);
    ImageRasterBenchmarkSupport.require(jpegImage.getGraphics() != null, "JPEG graphics");
    ImageRasterBenchmarkSupport.require(pngImage.getGraphics() != null, "PNG graphics");

    Image target = new Image(jpegImage.getPixelWidth(), jpegImage.getPixelHeight());
    Graphics canvas = target.getGraphics();
    ImageRasterBenchmarkSupport.require(canvas != null, "target graphics");
    for (int draw = 0; draw < DRAWS_PER_SAMPLE; draw++) {
      canvas.drawImage(jpegImage, 0, 0, false);
    }
    int[] pixels = target.getPixels();

    ByteArrayStream stream = new ByteArrayStream(8192);
    target.createPng(stream);

    jpegImage.applyColor2(0x0090A0B0);
    int[] colorPixels = jpegImage.getPixels();
    return new Result(pixels, stream.getBuffer(), stream.getPos(), colorPixels);
  }

  private static final class Result {
    final int[] pixels;
    final byte[] encoded;
    final int encodedLength;
    final int[] colorPixels;

    Result(int[] pixels, byte[] encoded, int encodedLength, int[] colorPixels) {
      this.pixels = pixels;
      this.encoded = encoded;
      this.encodedLength = encodedLength;
      this.colorPixels = colorPixels;
    }
  }
}
