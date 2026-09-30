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
    String inputJpegHash = "unavailable";
    String inputPngHash = "unavailable";

    try {
      ImageRasterBenchmarkSupport.require(samples > 0 && samples <= 200,
          "samples must be between 1 and 200");
      byte[] jpeg = ImageRasterBenchmarkSupport.resource("image-abi/lena512.jpg");
      byte[] png = ImageRasterBenchmarkSupport.opaquePng(512, 512);
      inputJpegHash = ImageRasterBenchmarkSupport.hashString(
          ImageRasterBenchmarkSupport.fullByteHash(jpeg, jpeg.length));
      inputPngHash = ImageRasterBenchmarkSupport.hashString(
          ImageRasterBenchmarkSupport.fullByteHash(png, png.length));
      ImageRasterBenchmarkSupport.configureAllRasterFeatures(scenario);
      for (int warmup = 0; warmup < 3; warmup++) {
        runWorkload(jpeg, png);
      }
      Image.resetImageOperationAccountingForTest();
      String expectedPixelHash = null;
      String expectedPngHash = null;
      String expectedColorHash = null;
      String expectedIdentityHash = null;
      String expectedVariantHash = null;
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
        String identityHash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullPixelHash(result.identityPixels));
        String variantHash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullPixelHash(result.variantPixels));
        if (expectedPixelHash == null) {
          expectedPixelHash = pixelHash;
          expectedPngHash = pngHash;
          expectedColorHash = colorHash;
          expectedIdentityHash = identityHash;
          expectedVariantHash = variantHash;
        } else {
          ImageRasterBenchmarkSupport.require(expectedPixelHash.equals(pixelHash), "pixel hash drift");
          ImageRasterBenchmarkSupport.require(expectedPngHash.equals(pngHash), "PNG hash drift");
          ImageRasterBenchmarkSupport.require(expectedColorHash.equals(colorHash), "color hash drift");
          ImageRasterBenchmarkSupport.require(expectedIdentityHash.equals(identityHash), "identity hash drift");
          ImageRasterBenchmarkSupport.require(expectedVariantHash.equals(variantHash), "variant hash drift");
        }
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed
            + ",draws=" + DRAWS_PER_SAMPLE
            + ",input_jpeg_hash=" + inputJpegHash + ",input_png_hash=" + inputPngHash
            + ",pixel_hash=" + pixelHash + ",png_hash=" + pngHash + ",color_hash=" + colorHash
            + ",identity_hash=" + identityHash + ",variant_hash=" + variantHash
            + ",decode_zero_copy=" + Image.zeroCopyDecodeCountForTest()
            + ",opacity_known_source=" + Image.opacityKnownFromSourceForTest()
            + ",opacity_determined_decode=" + Image.opacityDeterminedDuringDecodeForTest()
            + ",write_pixels_attempts=" + NativeImageBacking.writePixelsAttemptsForTest()
            + ",write_pixels_hits=" + NativeImageBacking.writePixelsHitsForTest()
            + ",write_pixels_fallbacks=" + NativeImageBacking.writePixelsFallbacksForTest()
            + ",row_readbacks=" + Image.rowReadbackCountForTest()
            + ",full_readbacks=" + Image.fullReadbackCountForTest()
            + ",direct_color_materializations=" + Image.directColorMaterializationCountForTest()
            + ",target_color_attempts=" + ImageRasterBenchmarkSupport.targetColorAttemptsForTest()
            + ",target_color_materializations=" + ImageRasterBenchmarkSupport.targetColorMaterializationsForTest()
            + ",target_color_hits=" + ImageRasterBenchmarkSupport.targetColorHitsForTest()
            + ",target_color_fallbacks=" + ImageRasterBenchmarkSupport.targetColorFallbacksForTest()
            + ",target_color_converted_bytes=" + ImageRasterBenchmarkSupport.targetColorConvertedBytesForTest()
            + ",physical_variant_lookups=" + ImageRasterBenchmarkSupport.physicalVariantLookupsForTest()
            + ",physical_variant_hits=" + ImageRasterBenchmarkSupport.physicalVariantHitsForTest()
            + ",physical_variant_misses=" + ImageRasterBenchmarkSupport.physicalVariantMissesForTest()
            + ",physical_variant_materializations="
            + ImageRasterBenchmarkSupport.physicalVariantMaterializationsForTest()
            + ",physical_variant_evictions=" + ImageRasterBenchmarkSupport.physicalVariantEvictionsForTest()
            + ",physical_variant_bytes=" + ImageRasterBenchmarkSupport.physicalVariantBytesForTest());
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
            + ",input_jpeg_hash=" + inputJpegHash
            + ",input_png_hash=" + inputPngHash
            + ",decode_zero_copy=" + Image.zeroCopyDecodeCountForTest()
            + ",opacity_known_source=" + Image.opacityKnownFromSourceForTest()
            + ",opacity_determined_decode=" + Image.opacityDeterminedDuringDecodeForTest()
            + ",write_pixels_attempts=" + NativeImageBacking.writePixelsAttemptsForTest()
            + ",write_pixels_hits=" + NativeImageBacking.writePixelsHitsForTest()
            + ",write_pixels_fallbacks=" + NativeImageBacking.writePixelsFallbacksForTest()
            + ",row_readbacks=" + Image.rowReadbackCountForTest()
            + ",full_readbacks=" + Image.fullReadbackCountForTest()
            + ",direct_color_materializations=" + Image.directColorMaterializationCountForTest()
            + ",target_color_attempts=" + ImageRasterBenchmarkSupport.targetColorAttemptsForTest()
            + ",target_color_materializations=" + ImageRasterBenchmarkSupport.targetColorMaterializationsForTest()
            + ",target_color_hits=" + ImageRasterBenchmarkSupport.targetColorHitsForTest()
            + ",target_color_fallbacks=" + ImageRasterBenchmarkSupport.targetColorFallbacksForTest()
            + ",target_color_converted_bytes=" + ImageRasterBenchmarkSupport.targetColorConvertedBytesForTest()
            + ",physical_variant_lookups=" + ImageRasterBenchmarkSupport.physicalVariantLookupsForTest()
            + ",physical_variant_hits=" + ImageRasterBenchmarkSupport.physicalVariantHitsForTest()
            + ",physical_variant_misses=" + ImageRasterBenchmarkSupport.physicalVariantMissesForTest()
            + ",physical_variant_materializations="
            + ImageRasterBenchmarkSupport.physicalVariantMaterializationsForTest()
            + ",physical_variant_evictions=" + ImageRasterBenchmarkSupport.physicalVariantEvictionsForTest()
            + ",physical_variant_bytes=" + ImageRasterBenchmarkSupport.physicalVariantBytesForTest(), error);
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

    Image identitySource = Image.createLogical(100, 100, 2);
    fill(identitySource, 100, 100);
    Image identity = identitySource.getSmoothScaledInstance(100, 100);
    Image identityTarget = Image.createLogical(100, 100, 2);
    requireGraphics(identityTarget).drawImage(identity, 0, 0, false);
    int[] identityPixels = identityTarget.getPixels();

    Image variantSource = Image.createLogical(200, 200, 2);
    fill(variantSource, 200, 200);
    Image variant = variantSource.getSmoothScaledInstance(100, 100);
    Image variantTarget = createTarget(100, 100, 2, 1);
    Graphics variantCanvas = requireGraphics(variantTarget);
    drawBatch(variantCanvas, variant, 4);
    int[] variantPixels = variantTarget.getPixels();
    ImageRasterBenchmarkSupport.mutateDeferredRootForTest(variant, variantTarget.getContentScale());
    drawBatch(variantCanvas, variant, 2);
    return new Result(pixels, stream.getBuffer(), stream.getPos(), colorPixels,
        identityPixels, variantPixels);
  }

  private static void fill(Image image, int width, int height) throws Exception {
    Graphics graphics = requireGraphics(image);
    for (int y = 0; y < height; y += 16) {
      for (int x = 0; x < width; x += 16) {
        graphics.foreColor = 0xFF000000 | ((x * 11) & 0xFF) << 16
            | ((y * 13) & 0xFF) << 8 | ((x + y * 3) & 0xFF);
        graphics.fillRect(x, y, Math.min(16, width - x), Math.min(16, height - y));
      }
    }
  }

  private static Graphics requireGraphics(Image image) {
    Graphics graphics = image.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "target graphics");
    return graphics;
  }

  private static Image createTarget(int width, int height, double contentScale, int colorType)
      throws Exception {
    return Image.createTestRaster(width, height, contentScale, colorType);
  }

  private static void drawBatch(Graphics canvas, Image image, int draws) {
    for (int draw = 0; draw < draws; draw++) {
      canvas.drawImage(image, 0, 0, false);
    }
  }

  private static final class Result {
    final int[] pixels;
    final byte[] encoded;
    final int encodedLength;
    final int[] colorPixels;
    final int[] identityPixels;
    final int[] variantPixels;

    Result(int[] pixels, byte[] encoded, int encodedLength, int[] colorPixels,
        int[] identityPixels, int[] variantPixels) {
      this.pixels = pixels;
      this.encoded = encoded;
      this.encodedLength = encodedLength;
      this.colorPixels = colorPixels;
      this.identityPixels = identityPixels;
      this.variantPixels = variantPixels;
    }
  }
}
